package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

/**
 * Created by laura on 25.10.2014.
 */
public class UserComment extends EscObject{

    public static final String EXTRA_NAME = UserComment.class.getName();

    public static final EscObjectCreator<UserComment> CREATOR
            = new EscObjectCreator<UserComment>(UserComment.class);

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_ENTRY_ID = "entryId";
        public static final String PROP_COMMENT = "comment";
    }

    public UserComment() {
        addProperties(0, "");
    }

    public UserComment(int entryId, String comment) {
        addProperties(entryId, comment);
    }

    public UserComment(Parcel in) {
        this();
        readFromParcel(in);
    }

    public int getEntryId() {
        return getPropertyInt(Columns.PROP_ENTRY_ID);
    }

    public String getComment() {
        return getPropertyString(Columns.PROP_COMMENT);
    }

    public void setComment(String comment) {
        putProperty(Columns.PROP_COMMENT, comment);
    }

    private void addProperties(int entryId, String comment) {
        putProperty(Columns.PROP_ENTRY_ID, entryId);
        putProperty(Columns.PROP_COMMENT, comment);
    }
}
