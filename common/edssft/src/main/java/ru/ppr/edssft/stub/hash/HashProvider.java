package ru.ppr.edssft.stub.hash;

import android.support.annotation.NonNull;

/**
 * Created by Артем on 29.01.2016.
 */
public class HashProvider {

    @NonNull
    public static Hash provideHash(@NonNull HashVariants variants) {

        Hash hash;

        switch (variants){
            case SHA_512:
                hash = new Sha512Hash();
                break;

            default:
                throw new IllegalArgumentException("Incorrect has variant");
        }
        return hash;
    }

}
