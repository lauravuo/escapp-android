package com.escapp.model;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by laura on 26.10.2014.
 */
public abstract class EscObject implements Parcelable{

    // Use linked map to keep order
    LinkedHashMap<String, Object> properties = new LinkedHashMap<String, Object>();

    protected static EscObject readObject(Parcel in, Class cls){
        return in.readBundle(cls.getClassLoader()).getParcelable(cls.getName());
    }

    protected void putProperty(String key, Object value) {
        properties.put(key, value);
    }

    protected void putProperty(String key, boolean value) {
        properties.put(key, new Boolean(value));
    }

    protected void putProperty(String key, int value) {
        properties.put(key, new Integer(value));
    }

    protected void putProperty(String key, double value) {
        properties.put(key, new Double(value));
    }

    protected boolean getPropertyBoolean(String key) {
        Object obj = properties.get(key);
        if (obj.getClass() == Boolean.class) {
            return ((Boolean) obj).booleanValue();
        }
        return false;
    }

    protected String getPropertyString(String key) {
        Object obj = properties.get(key);
        if (obj.getClass() == String.class) {
            return (String) obj;
        }
        return "";
    }

    protected int getPropertyInt(String key) {
        Object obj = properties.get(key);
        if (obj.getClass() == Integer.class) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    protected double getPropertyDouble(String key) {
        Object obj = properties.get(key);
        if (obj.getClass() == Double.class) {
            return ((Double) obj).doubleValue();
        }
        return 0;
    }

    protected Object getProperty(String key) {
        return properties.get(key);
    }

    protected void readFromParcel(Parcel in) {
        Object obj = null;
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            obj = entry.getValue();
            if (obj.getClass() == Integer.class) {
                entry.setValue(new Integer(in.readInt()));
            } else if (obj.getClass() == Boolean.class) {
                entry.setValue(in.readByte() == 0 ? false : true);
            } else if (obj.getClass() == Double.class) {
                entry.setValue(in.readDouble());
            } else if (obj.getClass() == String.class) {
                entry.setValue(in.readString());
            }else if (obj.getClass() == EscObjectList.class) {
                EscObjectList list = (EscObjectList)obj;
                in.readList(list, list.getType().getClassLoader());
                entry.setValue(list);
            } else {
                entry.setValue(readObject(in, obj.getClass()));
            }
        }
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        Object obj = null;
        Bundle bundle = new Bundle();
        for (Map.Entry<String, Object> entry : properties.entrySet()) {
            obj = entry.getValue();
            if (obj.getClass() == Integer.class) {
                parcel.writeInt(((Integer)obj).intValue());
            } else if (obj.getClass() == Boolean.class) {
                parcel.writeByte(((Boolean) obj).booleanValue() ? (byte)1 : 0);
            } else if (obj.getClass() == Double.class) {
                parcel.writeDouble((Double) obj);
            } else if (obj.getClass() == String.class) {
                parcel.writeString((String) obj);
            } else if (obj.getClass() == EscObjectList.class) {
                parcel.writeList((List)obj);
            } else {
                bundle.putParcelable(obj.getClass().getName(), (Parcelable)obj);
                parcel.writeBundle(bundle);
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }
}
