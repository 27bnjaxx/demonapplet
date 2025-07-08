/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringliblocal;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.*;
import javacardx.annotations.*;

/**
 * This library contains location and grammar specific string constants. The
 * strings here are used for tasks such as formatting input and output strings
 * including delimiters and other punctuation.  It also provides the local
 * location, and a default error message initialized from the other library. 
 */
@StringPool(value = {
    @StringDef(name = "Default", reference =
        "com.sun.jcclassic.samples.stringlib.LibStrings.Error"),
    @StringDef(name = "Location", value = "California"),
    @StringDef(name = "Hello", value = "Hello California!"),
    @StringDef(name = "delimiter", value = " "),
    @StringDef(name = "end", value = "."),
    @StringDef(name = "seperator", value = ", ")
},
export = true,
name = "LibStringsLocal")
public class StringHandlingLibLocal {

    private StringHandlingLibLocal() {
    }
}
