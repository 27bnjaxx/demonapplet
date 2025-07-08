/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * @(#)PhotoCardApplet.java	1.3 06/01/03
 */

package com.sun.jcclassic.samples.photocard;

import java.rmi.Remote;

import javacard.framework.APDU;
import javacard.framework.ISOException;
import javacard.framework.service.Dispatcher;
import javacard.framework.service.RMIService;
import javacard.framework.service.RemoteService;

/**
 * 
 * @author oscarm
 */
public class PhotoCardApplet extends javacard.framework.Applet {

    private Dispatcher disp;
    private RemoteService serv;
    private Remote photoStorage;

    public PhotoCardApplet() {
        photoStorage = new PhotoCardImpl();

        disp = new Dispatcher((short) 1);
        serv = new RMIService(photoStorage);
        disp.addService(serv, Dispatcher.PROCESS_COMMAND);

        register();
    }

    public static void install(byte[] aid, short s, byte b) {
        new PhotoCardApplet();
    }

    @Override
    public void process(APDU apdu) throws ISOException {

        disp.process(apdu);

    }

}
