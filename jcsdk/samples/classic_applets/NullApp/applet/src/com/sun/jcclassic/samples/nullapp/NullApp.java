/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

// /*
// Workfile:@(#)NullApp.java	1.7
// Version:1.7
// Date:01/03/06
//
// Archive:  /Products/Europa/samples/com/sun/javacard/samples/NullApp/NullApp.java
// Modified:01/03/06 12:13:10
// Original author:  Mitch Butler
// */
package com.sun.jcclassic.samples.nullapp;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;

/**
 */

public class NullApp extends Applet {
    /**
     * Only this class's install method should create the applet object.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU containing the INSTALL command.
     */
    protected NullApp(APDU apdu) {
        register();
    }

    /**
     * Installs this applet.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU containing the INSTALL command.
     * @exception ISOException
     *                with the response bytes per ISO 7816-4
     */
    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new NullApp(null);
    }

    /**
     * Returns <0x6D,INS> response status always.
     * 
     * @see APDU
     * @param apdu
     *            the incoming APDU containing the INSTALL command.
     * @exception ISOException
     *                with the response bytes per ISO 7816-4
     */
    @Override
    public void process(APDU apdu) throws ISOException {
        byte buffer[] = apdu.getBuffer();
        ISOException.throwIt(Util.makeShort((byte) (ISO7816.SW_INS_NOT_SUPPORTED >> 8), buffer[1]));
    }

}
