/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * TestService.java
 *
 * Created on September 19, 2001, 4:28 PM
 */

package com.sun.jcclassic.samples.service;

import javacard.framework.APDU;
import javacard.framework.service.BasicService;

/**
 *
 */
public class TestService extends BasicService {

    @Override
    public boolean processCommand(APDU command) {

        if (getINS(command) == (byte) 0x10) {
            setOutputLength(command, (short) 1);
            command.getBuffer()[5] = (byte) 0xAB;
            succeedWithStatusWord(command, (short) 0x6617);

            return true;
        }

        if (getINS(command) == (byte) 0x20) {

            setOutputLength(command, (short) 0);

            succeedWithStatusWord(command, (short) 0x6618);

            return true;
        }

        if (getINS(command) == (byte) 0x30) {

            setOutputLength(command, (short) 0);
            succeed(command);
            return true;
        }

        return false;
    }

}
