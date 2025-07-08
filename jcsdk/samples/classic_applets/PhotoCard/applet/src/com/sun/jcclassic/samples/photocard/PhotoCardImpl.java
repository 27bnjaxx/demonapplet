/** 
 * Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved.
 * 
 */

/*
 */

/*
 * @(#)PhotoCardImpl.java	1.5 06/01/03
 */

package com.sun.jcclassic.samples.photocard;

import java.rmi.RemoteException;

import javacard.framework.ISOException;
import javacard.framework.JCSystem;
import javacard.framework.UserException;
import javacard.framework.Util;
import javacard.framework.service.CardRemoteObject;
import javacard.security.CryptoException;
import javacard.security.MessageDigest;

public class PhotoCardImpl extends CardRemoteObject implements PhotoCard {

    // Internal fields
    // Arrat containing photo objects
    private Object[] photos;

    private boolean[] photoReady;
    private byte[] buffer;

    PhotoCardImpl() {
        super(); // export it
        // Initialize here
        photos = new Object[MAX_PHOTO_COUNT];
        photoReady = new boolean[MAX_PHOTO_COUNT];
        buffer = new byte[MAX_BUFFER_BYTES];
        for (short i = (short) 0; i < MAX_PHOTO_COUNT; i++) {
            photoReady[i] = false;
        }
    }

    // Implementation methods from Interface PhotoCard
    public short requestPhotoStorage(short size) throws RemoteException, UserException {

        // 16-byte safety factor. For object header.
        if (JCSystem.getAvailableMemory(JCSystem.MEMORY_TYPE_PERSISTENT) < (short) (size + (short) 16)) {
            UserException.throwIt(NO_SPACE_AVAILABLE);
        }

        for (short i = (short) 0; i < MAX_PHOTO_COUNT; i++) {
            byte[] thePhoto = (byte[]) photos[i];

            if (photos[i] == null) {
                photos[i] = new byte[size];
                return (short) (i + 1);
            }
        }
        UserException.throwIt(NO_SPACE_AVAILABLE);
        // Compiler requires this unreachable statement
        return (short) (-1);
    }

    public void loadPhoto(short photoID, byte[] data, short size, short offset, boolean more) throws RemoteException,
            UserException {

        if (photoID > MAX_PHOTO_COUNT) {
            UserException.throwIt(PhotoCard.INVALID_PHOTO_ID);
        }

        byte[] selPhoto = (byte[]) photos[(short) (photoID - (short) 1)];

        if (selPhoto == null) {
            UserException.throwIt(PhotoCard.NO_PHOTO_STORED);
        }

        if (((short) (offset + size)) > selPhoto.length) {
            UserException.throwIt(PhotoCard.INVALID_ARGUMENT);
        }

        Util.arrayCopy(data, (short) 0, selPhoto, offset, size);
        if (more == false) {
            photoReady[(short) (photoID - (short) 1)] = true;
        }
    }

    public void deletePhoto(short photoID) throws RemoteException, UserException {

        if (photoID > MAX_PHOTO_COUNT) {
            UserException.throwIt(PhotoCard.INVALID_PHOTO_ID);
        }

        byte[] thePhoto = (byte[]) photos[(short) (photoID - (short) 1)];
        if (thePhoto != null) {
            thePhoto = null;
            JCSystem.requestObjectDeletion();
            photoReady[(short) (photoID - (short) 1)] = false;
        } else {
            UserException.throwIt(PhotoCard.NO_PHOTO_STORED);
        }
    }

    public short getPhotoSize(short photoID) throws RemoteException, UserException {

        if (photoID > MAX_PHOTO_COUNT) {
            UserException.throwIt(PhotoCard.INVALID_PHOTO_ID);
        }

        byte[] thePhoto = (byte[]) photos[(short) (photoID - (short) 1)];
        if (thePhoto != null) {
            return (short) (thePhoto.length);
        }
        return (short) 0;
    }

    public byte[] getPhoto(short photoID, short offset, short size) throws RemoteException, UserException {

        if (photoID > MAX_PHOTO_COUNT) {
            UserException.throwIt(PhotoCard.INVALID_PHOTO_ID);
        }

        byte[] selPhoto = (byte[]) photos[(short) (photoID - (short) 1)];

        if ((selPhoto == null) || (photoReady[(short) (photoID - (short) 1)] == false)) {
            UserException.throwIt(PhotoCard.NO_PHOTO_STORED);
        }

        if (((short) (offset + size)) > selPhoto.length) {
            UserException.throwIt(PhotoCard.INVALID_ARGUMENT);
        }
        Util.arrayCopy(selPhoto, offset, buffer, (short) 0, size);
        return buffer;
    }

    public short verifyPhoto(short photoID, byte[] photoDigest, short photoOffset) throws RemoteException,
            UserException {

        MessageDigest md = null;
        short response;

        if (photoOffset > (short) photoDigest.length) {
            UserException.throwIt(PhotoCard.INVALID_OFFSET);
        }

        if (photoID > MAX_PHOTO_COUNT) {
            UserException.throwIt(PhotoCard.INVALID_PHOTO_ID);
        }

        byte[] selPhoto = (byte[]) photos[(short) (photoID - (short) 1)];

        if ((selPhoto == null) || (photoReady[(short) (photoID - (short) 1)] == false)) {
            UserException.throwIt(PhotoCard.NO_PHOTO_STORED);
        }

        try {
            md = MessageDigest.getInstance(MessageDigest.ALG_SHA_256, false);
        } catch (CryptoException e) {
            UserException.throwIt(PhotoCard.DOES_NOT_SUPPORT_PHOTO_VERIFICTAION);
        }

        short resultLength = md.doFinal(selPhoto, (short) 0, (short) selPhoto.length, buffer, (short) 0);
        response = compare(buffer, photoDigest, photoOffset, resultLength);
        return response;
    }

    private short compare(byte[] src, byte[] destination, short photoOffset, short length) {

        try {
            if (length != (short) 32) {
                ISOException.throwIt(FAIL4);
            }

            short comp = Util.arrayCompare(src, (short) 0, destination, photoOffset, length);

            if (comp == 0) {
                return (short) 0;
            }
            return FAIL1;

        } catch (CryptoException e) {
            return FAIL3;
        }
    }

}
