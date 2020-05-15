package com.escapp.model;

import android.os.Parcel;

/**
 * Class for storing the contest related data.
 *
 * @author  Laura Vuorenoja
 */
public class Contest extends EscObject {

    public static final String EXTRA_NAME = Contest.class.getName();

    public static final EscObjectCreator<Contest> CREATOR
            = new EscObjectCreator<Contest>(Contest.class);

    private static final String PROP_HEADER = "header";
    private static final String PROP_ENTRIES = "entries";
    private static final String PROP_USER_DATA = "userData";

    public Contest() {
        addProperties(new ContestHeader(), new UserContest());
    }

    public Contest(ContestHeader header, UserContest userContest) {
        addProperties(header, userContest);
    }

    public Contest(Parcel in) {
        this();
        readFromParcel(in);
    }

    private void addProperties(ContestHeader header, UserContest userContest) {
        putProperty(PROP_HEADER, header);
        putProperty(PROP_ENTRIES, new EscObjectList<ContestEntry>(ContestEntry.class));
        putProperty(PROP_USER_DATA, userContest);
    }

    /**
     * Adds a new entry to the contest entry list.
     *
     * @param entry New entry object.
     */
    public void addEntry(ContestEntry entry) {
        EscObjectList<ContestEntry> entries = getEntries();
        entries.add(entry);
        putProperty(PROP_ENTRIES, entries);
    }

    /**
     * Provides the contest entries.
     *
     * @return Contest entries.
     */
    public EscObjectList<ContestEntry> getEntries() {
        return (EscObjectList<ContestEntry>)getProperty(PROP_ENTRIES);
    }

    public void clearEntries() {
        EscObjectList<ContestEntry> entries = getEntries();
        entries.clear();
    }

    public ContestHeader getHeader() {
        return (ContestHeader)getProperty(PROP_HEADER);
    }

    public void setHeader(ContestHeader contestHeader) {
        putProperty(PROP_HEADER, contestHeader);
    }

    public UserContest getUserData() {
        return (UserContest)getProperty(PROP_USER_DATA);
    }

    /**
     * Provides the year of the contest.
     *
     * @return Year of the contest.
     */
    public int getYear() {
        return getHeader().getYear();
    }

    public String getId() {
        return getHeader().getId();
    }

    /**
     * Provides the contest city name.
     *
     * @return Name of the contest city.
     */
    public String getCity() {
        return getHeader().getCity();
    }

    /**
     * Provides the motto of the contest.
     *
     * @return The motto of the contest.
     */
    public String getMotto() {
        return getHeader().getMotto();
    }

    public ContestHeader.State getState() {
        return getHeader().getState();
    }

    public double getVersion() { return getHeader().getVersion(); }

    public String getUpdateSrc() { return getHeader().getUpdateSrc(); }

    public UserContest.RateMode getRateMode() {
        return getUserData().getRateMode();
    }

    public void setUserContest(UserContest userContest) {
        putProperty(PROP_USER_DATA, userContest);
    }

}
