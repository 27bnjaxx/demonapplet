/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

package com.sun.jcclassic.samples.securermi;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.service.Dispatcher;
import javacard.framework.service.RMIService;
import javacard.framework.service.RemoteService;
import javacard.framework.service.SecurityService;

public class SecurePurseApplet extends javacard.framework.Applet {

    private Dispatcher disp;

    public SecurePurseApplet() {

        SecurityService sec = new MySecurityService();

        Purse purse = new SecurePurseImpl(sec);

        RemoteService rmi = new RMIService(purse);

        disp = new Dispatcher((short) 4);
        disp.addService(sec, Dispatcher.PROCESS_INPUT_DATA);
        disp.addService(sec, Dispatcher.PROCESS_COMMAND);
        disp.addService(rmi, Dispatcher.PROCESS_COMMAND);
        disp.addService(sec, Dispatcher.PROCESS_OUTPUT_DATA);

        register();
    }

    public static void install(byte[] aid, short s, byte b) {
        new SecurePurseApplet();
    }

    @Override
    public void process(APDU apdu) throws ISOException {

        disp.process(apdu);

    }

}
