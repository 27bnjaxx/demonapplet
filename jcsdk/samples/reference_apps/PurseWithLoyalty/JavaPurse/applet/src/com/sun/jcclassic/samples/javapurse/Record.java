/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

// /*
// Workfile:@(#)Record.java	1.8
// Version:1.8
// Date:01/03/06
//
// Archive:  /Products/Europa/samples/com/sun/javacard/samples/JavaPurse/Record.java
// Modified:01/03/06 12:13:09
// Original author: Zhiqun Chen
// */
package com.sun.jcclassic.samples.javapurse;

/**
 * A Record.
 * <p>
 * The main reason for this class is that Java Card doesn't support
 * multidimensional arrays, but supports array of objects
 */

class Record {

    byte[] record;

    Record(byte[] data) {
        this.record = data;
    }

    Record(short size) {
        record = new byte[size];
    }

}
