/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringutilapp;

import javacard.framework.*;
import javacardx.annotations.*;
import javacardx.framework.string.StringUtil;
import javacardx.framework.string.StringException;

import static com.sun.jcclassic.samples.stringutilapp.AppUtilStrings.*;

/**
 * This sample applet explores the usage of the
 * javacardx.framework.string.StringUtil class.  It combines the
 * use of string annotations, two libraries with string constants, and various
 * methods from the StringUtil class. It imports string constants from a library
 * that itself imports constants from another library.  In addition, it also
 * helps demonstrate the use of a two applets in different contexts both
 * importing a single string constant from a common library.  It imports and
 * uses one of the same string constants from StringHandlingLib as 
 * StringHandlingApp.
 * 
 * The process method handles APDUs containing a command string composed of a
 * command type and  optional arguments. It then sends a response APDU based on
 * the command string it received.  Contained in this applet's string pool are
 * string constants defining various stored items. As an example, this applet
 * uses default contacts with a name and an e-mail address.  It also contains
 * default settings and the values that each setting can take on.  When the
 * applet receives a command for contacts, it sends a response message with the
 * contact name, corresponding to the number in the command argument, along with
 * the associated e-mail address value for that name.  When a settings command
 * is received with only one argument, the setting corresponding to the number
 * in the command argument, along with the default value for that setting, is
 * sent in the response message.  If the setting command has two arguments, the
 * second argument signifies the state that the setting should be placed in.
 * The applet then responds with the name of the setting and the value that was
 * selected in the command.
 * 
 * Command and response strings are represented as a series of bytes following
 * utf-8 representation of strings.  To demonstrate the applet's functionality,
 * string versions of the byte sequences are used in the examples below.
 * 
 * Command String Requirements: 
 * - Command strings must be terminated by a period "." 
 * - Command types must be separated from arguments with a space " " 
 * - Command types are case insensitive 
 * - Command types are: "Welcome" "Contacts" "Settings" 
 * - Arguments for "Contacts" or "Settings" are: "1" or "2" 
 * - An optional second argument of "1" or "2" can be used for "Settings" 
 * - Example valid command strings are: 
 *	1. "Welcome."
 *	2. "Settings 1."
 *	3. "contacts 2."
 *	4. "Settings 2 2."
 * 
 * Response String Requirements: 
 * - If the command string is invalid, a default response string will be sent.
 * - If the welcome command is received, a welcome response string will be sent.
 * - For either of the command types with arguments, a string will be sent 
 * corresponding to the argument(s) and command type received.  This string is
 * composed of a name and value, separated by a comma ","
 * - Example response strings corresponding to the command strings above are: 
 *	1. "Hello California!"
 *	2. "AutoCorrect, Off"
 *	3. "John Adams, John.Adams@123.com"
 *	4. "Wifi, On"
 * 
 * The local library, StringHandlingLibLocal, is an example of a
 * library that contains some location specific string constants.  For example,
 * it controls the formatting of input and output strings including delimiters
 * for arguments and command string terminators.  It also provides the location
 * to be used in the welcome message, and a default error message. In the case
 * of the error message, this shows how a library can use string constants from
 * another library, and how an applet can in turn use the constants from either
 * library.
 * 
 * The main library, StringHandlingLib, is used for both this
 * applet and the StringHandlingApp.  It contains string constants
 * for a default location, hello greeting, and error message.  It also contains
 * examples of substring, startsWith, and endsWith methods showing how to
 * implement them using the offsetByCodePoints method from StringUtil. Included
 * in this library are also test methods for each of these aforementioned
 * methods.
 *
 */
@StringPool(value = {
    @StringDef(name = "Generic", reference =
        "com.sun.jcclassic.samples.stringlib.LibStrings.Location"), //"World"
    @StringDef(name = "Greeting", reference =
        "com.sun.jcclassic.samples.stringlib.LibStrings.Hello"), //"Hello World!"
    @StringDef(name = "Correct", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.Hello"), //"Hello California!"
    @StringDef(name = "Location", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.Location"), //"California"
    @StringDef(name = "Default", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.Default"), //"Invalid command."
    @StringDef(name = "seperator", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.seperator"), //", "
    @StringDef(name = "delimiter", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.delimiter"), //" "
    @StringDef(name = "end", reference =
        "com.sun.jcclassic.samples.stringliblocal.LibStringsLocal.end"), //"."
    @StringDef(name = "Contacts", value = "contacts"),
    @StringDef(name = "c1Name", value = "\u5009\u6728\u9ebb\u8863"),
    @StringDef(name = "c2Name", value = "John Adams"),
    @StringDef(name = "c1Value", value = "kurakimai@AbcMail.com"),
    @StringDef(name = "c2Value", value = "John.Adams@123.com"),
    @StringDef(name = "Settings", value = "settings"),
    @StringDef(name = "s1Name", value = "AutoCorrect"),
    @StringDef(name = "s2Name", value = "Wifi"),
    @StringDef(name = "s1Value1", value = "Off"),
    @StringDef(name = "s1Value2", value = "On"),
    @StringDef(name = "s2Value1", value = "Enabled"),
    @StringDef(name = "s2Value2", value = "Disabled"),
    @StringDef(name = "Welcome", value = "welcome")
},
name = "AppUtilStrings")
public class StringUtilApp extends Applet {

    private byte[] echoBytes;
    private static final short LENGTH_ECHO_BYTES = 256;

    /**
     * Only this class's install method should create the applet object.
     */
    protected StringUtilApp() {
        echoBytes = new byte[LENGTH_ECHO_BYTES];
        
        AppUtilStrings.importLibConstants(); // initializes lib constants
        register();
    }

    /**
     * Installs this applet.
     * 
     * @param bArray
     *            the array containing installation parameters
     * @param bOffset
     *            the starting offset in bArray
     * @param bLength
     *            the length in bytes of the parameter data in bArray
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new StringUtilApp();
    }

    /**
     * Processes an incoming APDU.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU
     * @exception ISOException
     *                with the response bytes per ISO 7816-4
     */
    @Override
    public void process(APDU apdu) {
        byte buffer[] = apdu.getBuffer();
        short cmdArgs[] = new short[3]; //{command type, arg1 value, arg2 value}
        byte msg[] = new byte[100];
        short msgLen;
        short bytesRead = apdu.setIncomingAndReceive();
        short echoOffset = (short) 0;

        while (bytesRead > 0) {
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, echoBytes,
                                    echoOffset, bytesRead);
            echoOffset += bytesRead;
            bytesRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
        }

        parseCommand(buffer, cmdArgs);
        msgLen = prepareMessage(cmdArgs, msg);

        //Send Message:
        apdu.setOutgoing();
        apdu.setOutgoingLength((short) (echoOffset + msgLen));
        // echo data
        apdu.sendBytesLong(echoBytes, (short) 0, echoOffset);
        // echo msg
        apdu.sendBytesLong(msg, (short) 0, msgLen);
    }

    private void parseCommand(byte buffer[], short cmdArgs[]) {
        byte cmd[] = new byte[20];
        short cmdLen = 0;
        byte arg[] = new byte[4];
        short argLen = 0;
        byte arg2[] = new byte[4];
        short arg2Len = 0;
        short cmdEnd;
        short cmdDel;
        cmdArgs[0] = -1;
        
        //Find command:
        cmdEnd = StringUtil.indexOf(buffer, (short)5, (short)(buffer.length - 5),
                                    end, (short)0, (short)end.length);
        if (cmdEnd > 0) {
            cmdLen = Util.arrayCopy(buffer, (short)5, cmd, (short)0, cmdEnd);
            if (StringUtil.check(cmd, (short)0, cmdLen)) {
                StringUtil.toLowerCase(cmd, (short)0, cmdLen, cmd, (short)0);
                //Check if there is a delimiter:
                cmdDel = StringUtil.indexOf(cmd, (short)0, cmdLen, delimiter,
                                        (short)0, (short)delimiter.length);
                if (cmdDel > 0) { //Command with an argument
                    //Find command name:
                    if (StringUtil.startsWith(cmd, (short)0, cmdLen,
                            Contacts, (short)0, (short)Contacts.length,
                            StringUtil.codePointCount(Contacts, (short)0,
                            (short)Contacts.length))) {
                        cmdArgs[0] = 1;
                    }
                    else if (StringUtil.startsWith(cmd, (short)0, cmdLen,
                            Settings, (short)0, (short)Settings.length,
                            StringUtil.codePointCount(Settings, (short)0,
                            (short)Settings.length))) {
                        cmdArgs[0] = 2;
                    }
                    //Find argument:
                    if (cmdArgs[0] > 0) {
                        argLen = StringUtil.codePointAt(cmd, (short)0, cmdLen,
                                (short)(cmdDel + 1), arg, (short)0);
                        try {
                            cmdArgs[1] = StringUtil.parseShortInteger(arg, (short)0, (short)argLen);
                        } catch (StringException e) {
                            cmdArgs[1] = -1;
                        }
                    }
                    if (cmdArgs[0] == 2 && (short)(argLen + cmdDel + 1) < cmdLen) { //Check for 2nd argument
                        arg2Len = StringUtil.trim(cmd, (short)(argLen + cmdDel + 1),
                                (short)(cmdLen - argLen - cmdDel - 1), arg2, (short)0);
                        try {
                            cmdArgs[2] = StringUtil.parseShortInteger(arg2, (short)0, (short)arg2Len);
                        } catch (StringException e) {
                            cmdArgs[2] = -1;
                        }
                    }
                }
                else { //Command with no args
                    //Check for "Welcome" command
                    short cps = StringUtil.codePointCount(Welcome, (short)0, (short)Welcome.length);
                    if (StringUtil.startsWith(Welcome, (short)0, (short)Welcome.length, cmd,
                                              (short)0, cmdLen, cps)){
                        cmdArgs[0] = 0;
                    }
                }
            }
        }
    }

    private short prepareMessage(short cmdArgs[], byte msg[]) {
        short msgLen = 0;
        byte dst[] = new byte[20];

        //Prepare outgoing message:
        if (cmdArgs[0] == 0){ //Local welcome
            StringUtil.replace(Greeting, (short)0, (short)Greeting.length,
                               Generic, (short)0, (short)Generic.length,
                               Location, (short)0, (short)Location.length,
                               dst, (short)0);
            if (StringUtil.compare(true, Correct, (short)0, (short)Correct.length,
                                   dst, (short)0, (short)Correct.length) == 0) { //send local welcome
                msgLen = (short)Correct.length;
                Util.arrayCopy(Correct, (short)0, msg, (short)0, msgLen);
            }
            else { //Generic welcome
                msgLen = (short)Generic.length;
                Util.arrayCopy(Generic, (short)0, msg, (short)0, msgLen);
            }
        }
        else if(cmdArgs[0] == 1 && cmdArgs[1] == 1) { //Contacts 1 message
            Util.arrayCopy(c1Name, (short)0, msg, msgLen, (short)c1Name.length);
            msgLen += c1Name.length;
            Util.arrayCopy(seperator, (short)0, msg, msgLen, (short)seperator.length);
            msgLen += seperator.length;
            Util.arrayCopy(c1Value, (short)0, msg, msgLen, (short)c1Value.length);
            msgLen += c1Value.length;
        }
        else if (cmdArgs[0] == 1 && cmdArgs[1] == 2) { //Contacts 2 message
            Util.arrayCopy(c2Name, (short)0, msg, msgLen, (short)c2Name.length);
            msgLen += c2Name.length;
            Util.arrayCopy(seperator, (short)0, msg, msgLen, (short)seperator.length);
            msgLen += seperator.length;
            Util.arrayCopy(c2Value, (short)0, msg, msgLen, (short)c2Value.length);
            msgLen += c2Value.length;
        }
        else if(cmdArgs[0] == 2 && cmdArgs[1] == 1) { //Settings 1 message
            Util.arrayCopy(s1Name, (short)0, msg, msgLen, (short)s1Name.length);
            msgLen += s1Name.length;
            Util.arrayCopy(seperator, (short)0, msg, msgLen, (short)seperator.length);
            msgLen += seperator.length;
            if(cmdArgs[2] == 2) {
                Util.arrayCopy(s1Value2, (short)0, msg, msgLen, (short)s1Value2.length);
                msgLen += s1Value2.length;
            }
            else {
                Util.arrayCopy(s1Value1, (short)0, msg, msgLen, (short)s1Value1.length);
                msgLen += s1Value1.length;
            }
        }
        else if(cmdArgs[0] == 2 && cmdArgs[1] == 2) { //Settings 2 message
            Util.arrayCopy(s2Name, (short)0, msg, msgLen, (short)s2Name.length);
            msgLen += s2Name.length;
            Util.arrayCopy(seperator, (short)0, msg, msgLen, (short)seperator.length);
            msgLen += seperator.length;
            if (cmdArgs[2] == 2) {
                Util.arrayCopy(s2Value2, (short)0, msg, msgLen, (short)s2Value2.length);
                msgLen += s2Value2.length;
            }
            else {
                Util.arrayCopy(s2Value1, (short)0, msg, msgLen, (short)s2Value1.length);
                msgLen += s2Value1.length;
            }
        }
        else { //cmdArgs[0] == 0: default message
            msgLen = (short)Default.length;
            Util.arrayCopy(Default, (short)0, msg, (short)0, msgLen);
        }
        
        return msgLen;
    }
}
