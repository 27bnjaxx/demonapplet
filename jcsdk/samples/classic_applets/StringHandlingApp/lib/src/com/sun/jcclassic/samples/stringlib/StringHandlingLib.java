/**
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */
package com.sun.jcclassic.samples.stringlib;

import javacard.framework.APDU;
import javacard.framework.Applet;
import javacard.framework.ISO7816;
import javacard.framework.ISOException;
import javacard.framework.Util;
import javacard.framework.*;
import javacardx.annotations.*;
import javacardx.framework.string.StringUtil;

@StringPool(value = {
    @StringDef(name = "Location", value = "World"),
    @StringDef(name = "Hello", value = "Hello World!"),
    @StringDef(name = "Error", value = "Invalid command.")},
export = true,
name = "LibStrings")
public class StringHandlingLib {

    private StringHandlingLib() {
    }

    public static boolean testSubstring(byte[] srcString, short srcOffset, short srcLength,
            short codePointBeginIndex, short codePointEndIndex,
            byte[] dstString, short dstOffset,
            byte[] expected) {
        short l = substring(srcString, srcOffset, srcLength, codePointBeginIndex, codePointEndIndex, dstString, dstOffset);
        return Util.arrayCompare(dstString, dstOffset, expected, (short) 0, (short) expected.length) == 0;
    }

    public static boolean testStartsWith(byte[] aString, short offset, short length,
            byte[] prefix, short poffset, short plength, short codePointCount,
            boolean expected){
        boolean b = startsWith(aString, offset, length, prefix, poffset, plength, codePointCount);
        return b == expected;
    }

    public static boolean testEndsWith(byte[] aString, short offset, short length,
            byte[] suffix, short soffset, short slength, short codePointCount,
            boolean expected) {
        boolean b = endsWith(aString, offset, length, suffix, soffset, slength, codePointCount);
        return b == expected;
    }

    /**
     * Tests if the UTF-8 encoded character sequence
     * designated by <code>aString</code>, <code>offset</code> and <code>length</code> starts
     * with the first <code>codePointCount</code> characters of the character sequence
     * designated by <code>prefix</code>, <code>poffset</code> and <code>plength</code>
     * <br>
     * If <code>codePointCount</code> is negative, the whole prefix character sequence is
     * considered.
     *
     * @param aString the byte array containing the reference UTF-8 encoded character sequence.
     * @param offset the starting offset of the reference character sequence in <code>aString</code>.
     * @param length the length (in bytes) of the reference character sequence.
     * @param prefix the byte array containing the prefixing UTF-8 encoded character sequence.
     * @param poffset the starting offset in <code>prefix</code> of the prefixing character sequence.
     * @param plength the length (in bytes) of the prefixing character sequence.
     * @param codePointCount the number of code points to be used for testing.
     * @return <code>true</code> if the character sequence designated by <code>prefix</code>,
     * <code>poffset</code> and <code>plength</code> is a prefix of the character sequence designated by
     * <code>aString</code>, <code>offset</code> and <code>length</code>; <code>false</code> otherwise.
     *
     * @exception java.lang.NullPointerException
     *                if <code>aString</code> or <code>prefix</code> is <code>null</code>.
     *
     */
    public static boolean startsWith(byte[] aString, short offset, short length,
            byte[] prefix, short poffset, short plength, short codePointCount) {
        short endOffset;
        if (codePointCount >= 0) {
            endOffset = StringUtil.offsetByCodePoints(prefix, poffset, plength, (short)0, codePointCount);
        }
        else {
            endOffset = plength;
        }
        return length >= endOffset &&
                Util.arrayCompare(aString, offset, prefix, poffset, endOffset) == 0;
    }


    /**
     * Tests if the UTF-8 encoded character sequence
     * designated by <code>aString</code>, <code>offset</code> and <code>length</code> ends
     * with the first <code>codePointCount</code> characters of the character sequence
     * designated by <code>suffix</code>, <code>soffset</code> and <code>slength</code>
     * <br>
     * If <code>codePointCount</code> is negative, the whole suffix character sequence is
     * considered.
     *
     * @param aString the byte array containing the reference UTF-8 encoded character sequence.
     * @param offset the starting offset of the reference character sequence in <code>aString</code>.
     * @param length the length (in bytes) of the reference character sequence.
     * @param suffix the byte array containing the suffixing UTF-8 encoded character sequence.
     * @param soffset the starting offset in <code>suffix</code> of the suffixing character sequence.
     * @param slength the length (in bytes) of the suffixing character sequence.
     * @param codePointCount the number of code points to be used for testing.
     * @return <code>true</code> if the character sequence designated by <code>suffix</code>,
     * <code>soffset</code> and <code>slength</code> is a suffix of the character sequence designated by
     * <code>aString</code>, <code>offset</code> and <code>length</code>; <code>false</code> otherwise.
     *
     * @exception java.lang.NullPointerException
     *                if <code>aString</code> or <code>suffix</code> is <code>null</code>.
     *
     */
    public static boolean endsWith(byte[] aString, short offset, short length,
            byte[] suffix, short soffset, short slength, short codePointCount) {
        short endOffset;
        if (codePointCount >= 0) {
            endOffset = StringUtil.offsetByCodePoints(suffix, soffset, slength, (short)0, codePointCount);
        }
        else {
            endOffset = slength;
        }
        return length >= endOffset &&
                Util.arrayCompare(aString, (short) (offset + length - endOffset), suffix, soffset, endOffset) == 0;
    }

    /**
     * Returns a new string that is a substring of this string.
     * Copies to the destination byte array the substring of the provided source string.
     * The substring begins at the specified <code>codePointbeginIndex</code> and
     * extends to the character at index <code>codePointEndIndex - 1</code>.
     * Thus the length of the substring in codepoints (that is its codepoint count)is <code>endIndex-beginIndex</code>.
     * <p>
     * Corrupted or incomplete byte sequences within the text range count as one code point each.
     * <br>
     * If <code>codePointEndIndex</code> is negative, then the whole remaining character
     * sequence from the source string is considered.
     *
     * @param srcString the byte array containing the source UTF-8 encoded character sequence.
     * @param srcOffset the starting offset of the source character sequence in <code>aString</code>.
     * @param srcLength the length (in bytes) of the source character sequence.
     * @param codePointbeginIndex the beginning index, inclusive.
     * @param codePointEndIndex the ending index, exclusive.
     * @param dstString the byte array for copying the resulting character sequence.
     * @param dstOffset the starting offset in <code>dstString</code> for copying the resulting character sequence.
     * @return the number of bytes copied.
     */
    public static short substring(byte[] srcString, short srcOffset, short srcLength,
            short codePointBeginIndex, short codePointEndIndex,
            byte[] dstString, short dstOffset) {
        short endOffset;
        short beginOffset = (short)(srcOffset + StringUtil.offsetByCodePoints(srcString, srcOffset, srcLength, (short) 0, codePointBeginIndex));
        if (codePointEndIndex >= 0) {
            endOffset = (short)(srcOffset + StringUtil.offsetByCodePoints(srcString, srcOffset, srcLength, (short) 0, codePointEndIndex));
        }
        else {
            endOffset = (short)(srcOffset + srcLength);
        }
        short l = Util.arrayCopy(srcString, beginOffset, dstString, dstOffset, (short) (endOffset - beginOffset));
        return l;
    }
}
