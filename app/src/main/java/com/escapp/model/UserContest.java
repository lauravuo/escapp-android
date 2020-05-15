package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

import com.escapp.controller.Logger;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by laura on 29.12.14.
 */
public class UserContest extends EscObject {
    public static final String EXTRA_NAME = UserContest.class.getName();

    public static final EscObjectCreator<UserContest> CREATOR
            = new EscObjectCreator<UserContest>(UserContest.class);

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_CONTEST_ID = "contestId";
        public static final String PROP_RATE_MODE = "rateMode";
        public static final String PROP_UPDATE_FROM_XML = "updateFromXml";
        public static final String PROP_UPDATE_FROM_WEB = "updateFromWeb";
        public static final String PROP_RATE_MODE_SET = "rateModeSet";
        public static final String PROP_RATES = "rates";
    }

    public enum RateMode {
        TRADITIONAL, EUROVISION
    }
    private static final int[] eurovisionRates = {12, 10, 8, 7, 6, 5, 4, 3, 2, 1};

    public UserContest() {
        addProperties("", RateMode.TRADITIONAL, false, false);
    }

    public UserContest(String contestId, RateMode rateMode, boolean updateFromXml, boolean rateModeSet) {
        Logger.v("Constructor: " + contestId + " " + rateMode.ordinal() + " " + rateModeSet);

        addProperties(contestId, rateMode, updateFromXml, rateModeSet);
    }

    public UserContest(Parcel in) {
        this();
        readFromParcel(in);
        Logger.v("Constructor from parcel: " + isRateModeSet());
    }

    public String getContestId() {
        return getPropertyString(Columns.PROP_CONTEST_ID);
    }

    public RateMode getRateMode() {
        return RateMode.values()[getPropertyInt(Columns.PROP_RATE_MODE)];
    }

    public boolean getUpdateFromXml() {
        return getPropertyBoolean(Columns.PROP_UPDATE_FROM_XML);
    }
    public boolean getUpdateFromWeb()  {
        return getPropertyBoolean(Columns.PROP_UPDATE_FROM_WEB);
    }

    public void setUpdateFromWeb(boolean updateFromWeb) {
        putProperty(Columns.PROP_UPDATE_FROM_WEB, updateFromWeb);
    }

    public void addRate(Rate rate) {
        EscObjectList<Rate> rates = getRates();
        rates.add(rate);
    }

    public boolean setRate(Rate newRate) {
        boolean countChanged = false;
        EscObjectList<Rate> rates = getRates();
        boolean found = false;
        for (Rate rate : rates) {
            if (rate.getEntryId() == newRate.getEntryId()) {
                Logger.v("Setting new rate " + newRate.getRate() + " to entry " + rate.getEntryId());
                if (newRate.getRate() == 0 &&
                        (rate.getComment() == null || rate.getComment().getComment().length() == 0)) {
                    rates.remove(rate);
                    countChanged = true;

                } else {
                    rate.setRate(newRate.getRate());
                    rate.setComment(newRate.getComment().getComment());
                }
                found = true;
                break;
            }
        }
        if (!found) {
            addRate(newRate);
            countChanged = true;
        }
        return countChanged;
    }

    public Rate getRate(int entryId) {
        EscObjectList<Rate> rates = getRates();
        for (Rate rate : rates) {
            if (rate.getEntryId() == entryId) {
                Logger.v("Found rate " + rate.getRate() + " for entry " + rate.getEntryId());
                return rate;
            }
        }
        return null;
    }

    public HashMap<Rate, ContestEntry> getRatesForSemifinal(
            EscObjectList<ContestEntry> entries, int semifinalNbr) {
        HashMap<Rate, ContestEntry> allRates = new HashMap<>();
        EscObjectList<Rate> rates = getRates();
        for (Rate rate : rates) {
            ContestEntry foundEntry = null;
            for (ContestEntry entry : entries) {
                if (entry.getId() == rate.getEntryId()) {
                    foundEntry = entry;
                    break;
                }
            }
            if (foundEntry != null && isEntryRequested(semifinalNbr, foundEntry)) {
                allRates.put(rate, foundEntry);
            }
        }
        return allRates;
    }

    private static boolean isEntryRequested(int semifinalNbr, ContestEntry contestEntry) {
        if (semifinalNbr == 0 ||
                semifinalNbr == 1 && (contestEntry.getFlags() & ContestEntry.ENTRY_FLAG_1ST_SEMI) == ContestEntry.ENTRY_FLAG_1ST_SEMI ||
                semifinalNbr == 2 && (contestEntry.getFlags() & ContestEntry.ENTRY_FLAG_2ND_SEMI) == ContestEntry.ENTRY_FLAG_2ND_SEMI ||
                semifinalNbr == 3 && ((contestEntry.getFlags() & ContestEntry.ENTRY_FLAG_BIG_FIVE) == ContestEntry.ENTRY_FLAG_BIG_FIVE ||
                        (contestEntry.getFlags() & ContestEntry.ENTRY_FLAG_QUALIFIED) == ContestEntry.ENTRY_FLAG_QUALIFIED)) {
            return true;
        }
        return false;
    }

    public ArrayList<String> getAvailableRates(Rate rateToInclude) {
        if (this.getRateMode() == RateMode.EUROVISION) {
            ArrayList<String> availableRates = new ArrayList<String>();
            ArrayList<Integer> intRates = new ArrayList<Integer>();
            for (int rate : eurovisionRates) {
                intRates.add(rate);
            }
            EscObjectList<Rate> rates = getRates();
            for (Rate rate : rates) {
                if (rate.getRate() != rateToInclude.getRate()) {
                    for (Integer intRate : intRates) {
                        if (intRate == rate.getRate()) {
                            intRates.remove(intRate);
                            break;
                        }
                    }
                }
            }
            for (Integer intRate : intRates) {
                availableRates.add(intRate.toString());
            }
            return availableRates;
        }
        return null;
    }

    public EscObjectList<Rate> getRates() {
        return (EscObjectList<Rate>)getProperty(Columns.PROP_RATES);
    }

    public void setRateModeSet(boolean rateModeSet) {
        putProperty(Columns.PROP_RATE_MODE_SET, rateModeSet);
    }

    public boolean isRateModeSet() {
        Logger.v("isRateModeSet: " + getPropertyBoolean(Columns.PROP_RATE_MODE_SET));
        return getPropertyBoolean(Columns.PROP_RATE_MODE_SET);
    }

    private void addProperties(String contestId, RateMode rateMode, boolean updateFromXml, boolean rateModeSet) {
        putProperty(Columns.PROP_CONTEST_ID, contestId);
        putProperty(Columns.PROP_RATE_MODE, rateMode.ordinal());
        putProperty(Columns.PROP_UPDATE_FROM_XML, updateFromXml);
        putProperty(Columns.PROP_UPDATE_FROM_WEB, true);
        putProperty(Columns.PROP_RATE_MODE_SET, rateModeSet);
        putProperty(Columns.PROP_RATES, new EscObjectList<Rate>(Rate.class));
    }

}
