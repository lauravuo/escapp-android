package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

public class ContestHeader extends EscObject implements Comparable<ContestHeader> {

    public enum State {
        CONTESTANTS, SEMIFINALS, SEMIFINAL_NBRS, FINAL, FINAL_NBRS, RESULTS_OUT
    };

    public enum Type {
        ESC, OSC
    };

    public static final String EXTRA_NAME = ContestHeader.class.getName();
    public static final String EXTRA_NAME_LIST = EXTRA_NAME + "_list";

    public static final EscObjectCreator<ContestHeader> CREATOR
            = new EscObjectCreator<ContestHeader>(ContestHeader.class);

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_ID = "id";
        public static final String PROP_YEAR = "year";
        public static final String PROP_CITY = "city";
        public static final String PROP_MOTTO = "motto";
        public static final String PROP_STATE = "state";
        public static final String PROP_VERSION = "version";
        public static final String PROP_UPDATE_SRC = "updateSrc";
    }

    private static String STR_CONTESTANTS = "CONTESTANTS";
    private static String STR_SEMIFINALS = "SEMIFINALS";
    private static String STR_SEMIFINAL_NBRS = "SEMIFINAL_NBRS";
    private static String STR_FINAL = "FINAL";
    private static String STR_FINAL_NBRS = "FINAL_NBRS";
    private static String STR_RESULTS_OUT = "RESULTS_OUT";

    public ContestHeader() {
        addProperties("", 0, "", "", State.CONTESTANTS, 0.0, "");
    }

    public ContestHeader(String id, int year, String city,
                         String motto, State state, double version, String updateSrc) {
        addProperties(id, year, city, motto, state, version, updateSrc);
    }

    public ContestHeader(Parcel in) {
        this();
        readFromParcel(in);
    }

    @Override
    public int compareTo(ContestHeader contestHeader) {
        return Integer.compare(contestHeader.getYear(), this.getYear());
    }

    private void addProperties(String id, int year, String city,
                               String motto, State state, double version, String updateSrc) {
        putProperty(Columns.PROP_ID, id);
        putProperty(Columns.PROP_YEAR, year);
        putProperty(Columns.PROP_CITY, city);
        putProperty(Columns.PROP_MOTTO, motto);
        putProperty(Columns.PROP_STATE, state.ordinal());
        putProperty(Columns.PROP_VERSION, version);
        putProperty(Columns.PROP_UPDATE_SRC, updateSrc);
    }
    /**
     * Provides the year of the contest.
     *
     * @return Year of the contest.
     */
    public int getYear() {
        return getPropertyInt(Columns.PROP_YEAR);
    }

    public String getId() {
        return getPropertyString(Columns.PROP_ID);
    }

    /**
     * Provides the contest city name.
     *
     * @return Name of the contest city.
     */
    public String getCity() {
        return getPropertyString(Columns.PROP_CITY);
    }

    /**
     * Provides the motto of the contest.
     *
     * @return The motto of the contest.
     */
    public String getMotto() {
        return getPropertyString(Columns.PROP_MOTTO);
    }

    public State getState() {
        return State.values()[getPropertyInt(Columns.PROP_STATE)];
    }

    public double getVersion() {
        return getPropertyDouble(Columns.PROP_VERSION);
    }
    public void setVersion(double newVersion) { putProperty(Columns.PROP_VERSION, newVersion);}

    public String getUpdateSrc() {
        return getPropertyString(Columns.PROP_UPDATE_SRC);
    }

    public static State getState(String stateStr) {
        State state = State.CONTESTANTS;
        if (stateStr.equals(STR_CONTESTANTS)) {
            state = State.CONTESTANTS;
        } else if (stateStr.equals(STR_SEMIFINALS)) {
            state = State.SEMIFINALS;
        } else if (stateStr.equals(STR_SEMIFINAL_NBRS)) {
            state = State.SEMIFINAL_NBRS;
        } else if (stateStr.equals(STR_FINAL)) {
            state = State.FINAL;
        } else if (stateStr.equals(STR_FINAL_NBRS)) {
            state = State.FINAL_NBRS;
        } else if (stateStr.equals(STR_RESULTS_OUT)) {
            state = State.RESULTS_OUT;
        }
        return state;
    }

    public Type getType() {
        return getType(getId());
    }

    static public Type getType(String id) {
        if (id.startsWith("contest_osc")) {
            return Type.OSC;
        }
        return Type.ESC;
    }
}

