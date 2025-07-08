/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringutilapp;

final class AppUtilStrings {
    private static com.sun.jcclassic.samples.stringlib.LibStrings __x0;
    private static com.sun.jcclassic.samples.stringliblocal.LibStringsLocal __x1;

    static byte[] Generic;
    static byte[] Greeting;
    static byte[] Correct;
    static byte[] Location;
    static byte[] Default;
    static byte[] seperator;
    static byte[] delimiter;
    static byte[] end;
    static final byte[] Contacts = new byte[] {0x63, 0x6f, 0x6e, 0x74, 0x61, 0x63, 0x74, 0x73, };
    static final byte[] c1Name = new byte[] {(byte)0xe5, (byte)0x80, (byte)0x89, (byte)0xe6, (byte)0x9c, (byte)0xa8, (byte)0xe9, (byte)0xba, (byte)0xbb, (byte)0xe8, (byte)0xa1, (byte)0xa3, };
    static final byte[] c2Name = new byte[] {0x4a, 0x6f, 0x68, 0x6e, 0x20, 0x41, 0x64, 0x61, 0x6d, 0x73, };
    static final byte[] c1Value = new byte[] {0x6b, 0x75, 0x72, 0x61, 0x6b, 0x69, 0x6d, 0x61, 0x69, 0x40, 0x41, 0x62, 0x63, 0x4d, 0x61, 0x69, 0x6c, 0x2e, 0x63, 0x6f, 0x6d, };
    static final byte[] c2Value = new byte[] {0x4a, 0x6f, 0x68, 0x6e, 0x2e, 0x41, 0x64, 0x61, 0x6d, 0x73, 0x40, 0x31, 0x32, 0x33, 0x2e, 0x63, 0x6f, 0x6d, };
    static final byte[] Settings = new byte[] {0x73, 0x65, 0x74, 0x74, 0x69, 0x6e, 0x67, 0x73, };
    static final byte[] s1Name = new byte[] {0x41, 0x75, 0x74, 0x6f, 0x43, 0x6f, 0x72, 0x72, 0x65, 0x63, 0x74, };
    static final byte[] s2Name = new byte[] {0x57, 0x69, 0x66, 0x69, };
    static final byte[] s1Value1 = new byte[] {0x4f, 0x66, 0x66, };
    static final byte[] s1Value2 = new byte[] {0x4f, 0x6e, };
    static final byte[] s2Value1 = new byte[] {0x45, 0x6e, 0x61, 0x62, 0x6c, 0x65, 0x64, };
    static final byte[] s2Value2 = new byte[] {0x44, 0x69, 0x73, 0x61, 0x62, 0x6c, 0x65, 0x64, };
    static final byte[] Welcome = new byte[] {0x77, 0x65, 0x6c, 0x63, 0x6f, 0x6d, 0x65, };

    static void importLibConstants() {
        __x0 = new com.sun.jcclassic.samples.stringlib.LibStrings();
        __x1 = new com.sun.jcclassic.samples.stringliblocal.LibStringsLocal();
        Generic = __x0.Location;
        Greeting = __x0.Hello;
        Correct = __x1.Hello;
        Location = __x1.Location;
        Default = __x1.Default;
        seperator = __x1.seperator;
        delimiter = __x1.delimiter;
        end = __x1.end;
    }
}
