package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

/**
 * Created by laura on 25.10.2014.
 */
public class Country extends EscObject {

    public static final String EXTRA_NAME = Country.class.getName();

    public static final EscObjectCreator<Country> CREATOR
            = new EscObjectCreator<Country>(Country.class);

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_NAME = "name";
        public static final String PROP_FLAGPATH = "flagPath";
        public static final String PROP_WIKIPATH = "wikiPath";
        public static final String PROP_ESCWIKIPATH = "escWikiPath";
    }

    public Country() {
        addProperties("", "", "");
    }

    public Country(String name, String flagPath, String wikiPath) {
        addProperties(name, flagPath, wikiPath);
    }

    public Country(Parcel in) {
        this();
        readFromParcel(in);
    }

    public String getFlagPath() {
        return getPropertyString(Columns.PROP_FLAGPATH);
    }

    public String getName() {
        return getPropertyString(Columns.PROP_NAME);
    }

    public String getWikiPath() {
        return getPropertyString(Columns.PROP_WIKIPATH);
    }

    private void addProperties(String name, String flagPath, String wikiPath) {
        putProperty(Columns.PROP_NAME, name);
        putProperty(Columns.PROP_FLAGPATH, flagPath);
        putProperty(Columns.PROP_WIKIPATH, wikiPath);
    }
}
