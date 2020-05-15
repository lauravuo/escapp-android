package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

/**
 * Created by laura on 25.10.2014.
 */
public class Rate extends EscObject implements Comparable<Rate>{

    public static final String EXTRA_NAME = Rate.class.getName();

    public static final EscObjectCreator<Rate> CREATOR
            = new EscObjectCreator<Rate>(Rate.class);

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_RATE_MODE = "rateMode";
        public static final String PROP_ENTRY_ID = "entryId";
        public static final String PROP_RATE = "rate";
        public static final String PROP_COMMENT = "comment";
    }

    public Rate() {
        addProperties(UserContest.RateMode.TRADITIONAL, 0, 0, new UserComment(0, ""));
    }

    public Rate(UserContest.RateMode rateMode, int rate, int entryId, UserComment userComment) {
        addProperties(rateMode, rate, entryId, userComment);
    }

    public Rate(Parcel in) {
        this();
        readFromParcel(in);
    }

    public UserContest.RateMode getRateMode() {
        return UserContest.RateMode.values()[getPropertyInt(Columns.PROP_RATE_MODE)];
    }

    public int getEntryId() {
        return getPropertyInt(Columns.PROP_ENTRY_ID);
    }

    public int getRate() {
        return getPropertyInt(Columns.PROP_RATE);
    }

    public UserComment getComment() {
        return (UserComment)getProperty(Columns.PROP_COMMENT);
    }

    public void setRate(int rate) {
        putProperty(Columns.PROP_RATE, rate);
    }

    public void setComment(String comment) {
        ((UserComment)getProperty(Columns.PROP_COMMENT)).setComment(comment);
    }

    @Override
    public int compareTo(Rate rate) {
        return Integer.valueOf(rate.getRate()).compareTo(Integer.valueOf(this.getRate()));
    }

    private void addProperties(
            UserContest.RateMode rateMode, int rate, int entryId, UserComment userComment) {
        putProperty(Columns.PROP_RATE_MODE, rateMode.ordinal());
        putProperty(Columns.PROP_ENTRY_ID, entryId);
        putProperty(Columns.PROP_RATE, rate);
        putProperty(Columns.PROP_COMMENT, userComment);
    }
}
