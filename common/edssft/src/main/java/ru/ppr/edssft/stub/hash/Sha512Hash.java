package ru.ppr.edssft.stub.hash;

import android.support.annotation.NonNull;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/**
 * Created by Артем on 29.01.2016.
 */
class Sha512Hash implements Hash {

    private final MessageDigest messageDigest;

    Sha512Hash() {
        messageDigest = createMessageDigest();
    }

    private MessageDigest createMessageDigest(){
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-512", "BC");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return messageDigest;
    }

    @NonNull
    @Override
    public byte[] computeHash(byte[] data) {
        byte[] sign = new byte[64];
        if (messageDigest != null) {
            sign = messageDigest.digest(data);
        }
        return sign;
    }
}
