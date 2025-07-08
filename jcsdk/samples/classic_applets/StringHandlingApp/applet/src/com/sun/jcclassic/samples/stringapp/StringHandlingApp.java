/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringapp;

import javacard.framework.*;
import javacardx.annotations.*;

import com.sun.jcclassic.samples.stringlib.StringHandlingLib;

import static com.sun.jcclassic.samples.stringapp.AppStrings.*;

/**
 * This applet focuses on the use of javacardx.annotations.StringDef and
 * javacardx.annotations.StringPool annotations. It defines its own set of
 * string constants and also imports string constants from the main library,
 * StringHandlingLib.  It also helps demonstrate the use of a two applets in
 * different contexts both importing a single string constant from a common
 * library.  It imports and uses one of the same string constants from
 * StringHandlingLib as StringUtilApp.  
 *
 * StringHandlingApp imports a string constant from the StringHandlingLib, and
 * also defines some string constants for itself. When the StringHandlingApp is
 * selected, in the process method, it uses each of the test methods defined in
 * the StringHandlingLib library.  If the results from each of the tested
 * methods match with the expected string constants that have already been
 * defined in the app, a response message is created containing a "Hello World!"
 * message and a copy of the incoming message appended to the end.  In the case
 * that the tested methods do not produce the expected outcome, a message
 * containing the header bytes from the buffer and a copy of the incoming
 * message appended to the end is sent instead.
 *
 */

@StringPool(value = {
    @StringDef(name = "S1", value = "Hello World!"),
    @StringDef(name = "S1_expected", value = "llo "),
    @StringDef(name = "S2", reference = "com.sun.jcclassic.samples.stringlib.LibStrings.Hello"), //Hello World!
    @StringDef(name = "S2_start", value = "Hello "),
    @StringDef(name = "S2_end", value = "World")},
name = "AppStrings")
public class StringHandlingApp extends Applet {

    private byte[] echoBytes;
    private byte[] scratchpad;
    private static final short LENGTH_ECHO_BYTES = 256;

    /**
     * Only this class's install method should create the applet object.
     */
    protected StringHandlingApp() {
        echoBytes = new byte[LENGTH_ECHO_BYTES];
        scratchpad = new byte[1024];
        AppStrings.importLibConstants(); // initializes lib constants
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
        new StringHandlingApp();
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

        short bytesRead = apdu.setIncomingAndReceive();
        short echoOffset = (short) 0;

        while (bytesRead > 0) {
            Util.arrayCopyNonAtomic(buffer, ISO7816.OFFSET_CDATA, echoBytes, echoOffset, bytesRead);
            echoOffset += bytesRead;
            bytesRead = apdu.receiveBytes(ISO7816.OFFSET_CDATA);
        }

        apdu.setOutgoing();

        byte[] z = S2; // Because it is imported from a library. AppStrings.getInstance() or AppStrings.importLibConstants() must have been called before

        if (StringHandlingLib.testSubstring(S1, (short) 0, (short) S1.length, (short) 2,
                                            (short) 6, scratchpad, (short) 0, S1_expected)
                && StringHandlingLib.testStartsWith(S2, (short)0, (short)S2.length, S2_start,
                                                    (short)0, (short)S2_start.length, (short)S2_start.length, true)
                && StringHandlingLib.testEndsWith(S2, (short)0, (short)(S2.length - 1), S2_end, 
                                                  (short)0, (short)S2_end.length, (short)S2_end.length, true)) {

            short msgLen = (short) S1.length;

            apdu.setOutgoingLength((short) (echoOffset + msgLen));

            // echo msg
            apdu.sendBytesLong(S1, (short) 0, msgLen);
            // echo data
            apdu.sendBytesLong(echoBytes, (short) 0, echoOffset);
        } else {
            apdu.setOutgoingLength((short) (echoOffset + 5));

            // echo header
            apdu.sendBytes((short) 0, (short) 5);
            // echo data
            apdu.sendBytesLong(echoBytes, (short) 0, echoOffset);
        }
    }
}
