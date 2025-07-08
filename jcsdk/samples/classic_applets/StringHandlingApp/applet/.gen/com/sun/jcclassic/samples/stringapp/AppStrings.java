/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringapp;

final class AppStrings {
    private static com.sun.jcclassic.samples.stringlib.LibStrings __x0;

    static final byte[] S1 = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, 0x57, 0x6f, 0x72, 0x6c, 0x64, 0x21, };
    static final byte[] S1_expected = new byte[] {0x6c, 0x6c, 0x6f, 0x20, };
    static byte[] S2;
    static final byte[] S2_start = new byte[] {0x48, 0x65, 0x6c, 0x6c, 0x6f, 0x20, };
    static final byte[] S2_end = new byte[] {0x57, 0x6f, 0x72, 0x6c, 0x64, };

    static void importLibConstants() {
        __x0 = new com.sun.jcclassic.samples.stringlib.LibStrings();
        S2 = __x0.Hello;
    }
}
