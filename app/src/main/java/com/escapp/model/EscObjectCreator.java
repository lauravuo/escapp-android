package com.escapp.model;

import android.os.Parcel;

import com.escapp.controller.Logger;

import java.lang.reflect.Array;
import java.lang.reflect.Constructor;

/**
 * Created by laura on 26.10.2014.
 */

public class EscObjectCreator <T> implements android.os.Parcelable.Creator<T> {
    private Constructor<? extends T> ctor = null;

    EscObjectCreator(Class<? extends T> impl) {
        try {
            this.ctor = impl.getConstructor(Parcel.class);
        } catch (NoSuchMethodException e) {
            Logger.e("Proper constructor missing from " + impl.getName());
        }
    }

    public T createFromParcel(Parcel parcel) {
        if (null != ctor)
        {
            try {
                return ctor.newInstance(parcel);
            } catch (Exception e) {
                Logger.e("Invalid constructor encountered.");
            }
        }
        return null;
    }

    public T[] newArray(int i) {
        return (T[]) Array.newInstance(this.getClass(), i);
    }
}

