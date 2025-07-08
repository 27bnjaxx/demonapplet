/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * Main.java
 *
 * Created on September 19, 2001, 3:24 PM
 */

package com.sun.jcclassic.samples.service;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.service.Dispatcher;
import javacard.framework.service.Service;

/**
 *
 * @author vo113324
 */
public class Main extends javacard.framework.Applet {

    private Dispatcher disp;
    private Service serv;

    public Main() {
        disp = new Dispatcher((short) 1);
        serv = new TestService();
        disp.addService(serv, Dispatcher.PROCESS_COMMAND);

        register();
    }

    public static void install(byte[] aid, short s, byte b) {
        new Main();
    }

    @Override
    public void process(APDU apdu) throws ISOException {

		if(!selectingApplet()){
        	disp.process(apdu);
		}

    }

}
