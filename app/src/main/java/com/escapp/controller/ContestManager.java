package com.escapp.controller;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.support.v4.util.ArrayMap;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.escapp.R;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.EscObjectList;
import com.escapp.model.Rate;
import com.escapp.model.UserContest;
import com.escapp.view.App;

/**
 * ContestManager is the bridge between the model and the view of the ESCGuide.
 *
 * @author  Laura Vuorenoja
 */
public class ContestManager {
    public final static int CONTEST_TYPE_COUNT = 2;
    public final static String CONTEST_TYPE_ESC = "CONTEST_TYPE_ESC";
    public final static String CONTEST_TYPE_OSC = "CONTEST_TYPE_OSC";
    public final static String CONTEST_TYPES[] = {CONTEST_TYPE_ESC, CONTEST_TYPE_OSC};

    /**
     * Single instance for app that provides access to contest data.
     */
    private static ContestManager contestManager = new ContestManager();
    private DatabaseManager databaseManager = null;
    private CountryManager countryManager = null;
    private ContestHeader activeContest = null;

    /**
     * Currently active contest.
     */
    private Contest currentContest = null;

    private ArrayList<EscObjectList<ContestHeader>> availableContests =
            new ArrayList<>(CONTEST_TYPE_COUNT);

    /**
     * Cache of previously active contests.
     */
    private List<Contest> contestCache = new EscObjectList<Contest>(Contest.class);

    /**
     * Provides currently active contest.
     *
     * @return Currently active contest.
     */
    public Contest getCurrentContest() {
        return currentContest;
    }

    public ContestHeader getActiveContest() {
        SharedPreferences settings = App.getSharedPreferences();
        String id = settings.getString(App.PREFS_ACTIVE_CONTEST_ID_KEY, "");
        if (activeContest == null || !activeContest.getId().equals(id)) {
            int year = settings.getInt(App.PREFS_ACTIVE_CONTEST_YEAR_KEY, 0);
            String city = settings.getString(App.PREFS_ACTIVE_CONTEST_CITY_KEY, "");
            String motto = settings.getString(App.PREFS_ACTIVE_CONTEST_MOTTO_KEY, "");
            int state = settings.getInt(App.PREFS_ACTIVE_CONTEST_STATE_KEY, 0);
            double version = settings.getFloat(App.PREFS_ACTIVE_CONTEST_VERSION_KEY, 0);
            String updateSrc = settings.getString(App.PREFS_ACTIVE_CONTEST_UPDATE_SRC_KEY, "");
            if (id.length() > 0) {
                Logger.i("Restoring active contest: " + id);
                activeContest =
                        new ContestHeader(id, year, city, motto, ContestHeader.State.values()[state], version, updateSrc);
            }
        }
        return activeContest;
    }

    /**
     * Provides access to the contest manager.
     *
     * @return Single contest manager instance for the app.
     */
    public static ContestManager getContestManager() {
        return contestManager;
    }

    private CountryManager getCountryManager() {
        if (countryManager == null) {
            this.countryManager = new CountryManager(this.databaseManager);
        }
        return countryManager;
    }

    private ContestManager() {
        Logger.d("Creating contest manager.");
        databaseManager = new DatabaseManager(App.getContext());
    }

    public void findAvailableContests() {
        Logger.v("findAvailableContests");
        if (availableContests.size() == 0) {
            for (int i = 0; i < CONTEST_TYPE_COUNT; i++) {
                availableContests.add(new EscObjectList<ContestHeader>(ContestHeader.class));
            }

            boolean versionChanged = handleAppUpgrade();
            EscObjectList<ContestHeader> allContests = databaseManager.getContests();

            // Set initial entry id when fetching the available contests in app initialization
            XmlParser.setInitialEntryId(databaseManager.getNextEntryId());

            // Contests are parsed for the first time
            if (allContests.size() == 0) {
                ArrayMap<Integer, String> ids = getContestResourceIds();
                for (Map.Entry<Integer, String> entry : ids.entrySet()) {
                    ContestHeader contestHeader = parseContestHeader(entry.getValue(), entry.getKey());
                    addContestHeader(contestHeader);
                    allContests.add(contestHeader);
                }
                databaseManager.setContests(allContests);

            // Check if new contests are added with app upgrade
            } else if (versionChanged) {
                ArrayMap<Integer, String> ids = getContestResourceIds();
                for (Map.Entry<Integer, String> entry : ids.entrySet()) {
                    boolean contestFound = false;
                    for (ContestHeader header : allContests) {
                        if (header.getId().equals(entry.getValue())) {
                            contestFound = true;
                            ContestHeader contestHeader = parseContestHeader(entry.getValue(), entry.getKey());
                            // Update possible new values other than version
                            // Version and contest entries are updated when contest is set
                            contestHeader.setVersion(header.getVersion());
                            addContestHeader(contestHeader);
                            databaseManager.updateContestHeader(contestHeader);
                            break;
                        }
                    }
                    if (!contestFound) {
                        ContestHeader newHeader = parseContestHeader(entry.getValue(), entry.getKey());
                        if (newHeader != null) {
                            addContestHeader(newHeader);
                            databaseManager.addContest(newHeader);
                        }
                    }
                }
            // Otherwise just add contests found from the db
            } else {
                for (ContestHeader header : allContests) {
                    addContestHeader(header);
                }
            }

            for (EscObjectList<ContestHeader> list : availableContests) {
                // Sort available contests from new to old
                Collections.sort(list);
            }
        }
    }

    private void addContestHeader(ContestHeader contestHeader) {
        int index = contestHeader.getType().ordinal();
        availableContests.get(index).add(contestHeader);
    }

    private ContestHeader parseContestHeader(String headerId, int resourceId) {
        ContestHeader contestHeader = null;
        ContestHeader.Type contestType = ContestHeader.getType(headerId);
        try {
            contestHeader = XmlParser.getContestHeader(
                App.getContext().getResources().openRawResource(resourceId), headerId, contestType);
        } catch (Exception e) {
            Logger.e("Failed to parse contest header " + headerId + " from resources: " + e.toString());
        }
        return contestHeader;
    }

    public ArrayList<EscObjectList<ContestHeader>> getAvailableContests() {
        if (availableContests.size() == 0) {
            findAvailableContests();
        }
        return availableContests;
    }

    public boolean setCurrentContest(String id) throws ContestNotFoundException {
        boolean contestChanged = false;
        boolean headerUpdated = false;
        Logger.d("setCurrentContest: " + id);
        if (currentContest == null || !currentContest.getId().equals(id)) {
            Contest contest = findCachedContest(id);
            if (contest == null) {
                contest = CreateContest(id);
            }
            // Set the new/found contest as the current one
            if (contest != null) {
                currentContest = contest;
                // Store previous contest to cache if it is not already there
                if (currentContest != null && !contestCache.contains(currentContest)) {
                    // Fit only 5 previous contests to cache
                    if (contestCache.size() == 5) {
                        contestCache.remove(0);
                    }
                    contestCache.add(currentContest);
                }
                contestChanged = true;
            } else {
                throw new ContestNotFoundException(id);
            }
        }
        if (currentContest != null && currentContest.getUpdateSrc().length() > 0) {
            try {
                Context context = App.getContext();
                File file = App.getContestUpdateFile(currentContest.getId());
                InputStream inputStream = new FileInputStream(file);
                // convert to buffered input stream to support marks and reset
                BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
                headerUpdated = updateContest(bufferedInputStream, currentContest);
                file.delete();
                contestChanged = true;
            } catch (FileNotFoundException exception) {
                Logger.d("No updated data found from web for contest " + currentContest.getId());
            }
        }
        if (contestChanged) {
            StoreActiveContest();
        }
        return headerUpdated;
    }

    public boolean setRate(Rate rate) {
        Logger.d("Setting rate " + rate.getRate() + " for entry " + rate.getEntryId());

        databaseManager.setRate(rate);
        if (currentContest != null) {
            return currentContest.getUserData().setRate(rate);
        }
        return false;
    }

    public void setRateMode(String contestId, UserContest.RateMode rateMode) {
        Logger.d("Setting rate mode " + rateMode.ordinal() + " for contest " + contestId);

        databaseManager.updateRateMode(contestId, rateMode);
        if (currentContest != null) {
            // Update also rates by creating new user data
            currentContest.setUserContest(databaseManager.getUserContest(contestId));
        }
    }

    public void resetRateMode(String contestId, UserContest.RateMode rateMode) {
        Logger.d("Resetting rates for rate mode " + rateMode.ordinal() + " for contest " + contestId);

        databaseManager.resetRates(contestId, rateMode);
        if (currentContest != null) {
            // Update also rates by creating new user data
            currentContest.setUserContest(databaseManager.getUserContest(contestId));
        }
    }

    public void deleteComments(String contestId) {
        Logger.d("Resetting comments for contest " + contestId);

        databaseManager.resetComments(contestId);
        if (currentContest != null) {
            // Update also rates by creating new user data
            currentContest.setUserContest(databaseManager.getUserContest(contestId));
        }
    }

    public Contest findCachedContest(String id) {
        for (Contest contest : contestCache) {
            if (contest.getId().equals(id)) {
                return contest;
            }
        }
        return null;
    }
    
    public ContestHeader getHeaderForId(String id) {
        List<EscObjectList<ContestHeader>> lists = getAvailableContests();
        for (EscObjectList<ContestHeader> list : lists) {
            for (ContestHeader contest : list) {
                if (contest.getId().equals(id)) {
                    return contest;
                }
            }
        }

        Logger.w("Unable to find header for " + id);
        return null;
    }

    public void storeShareMode(int currentShareMode) {
        SharedPreferences settings = App.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(App.PREFS_CURRENT_SHARE_MODE, currentShareMode);
        editor.commit();
    }

    public int getStoredShareMode() {
        return App.getSharedPreferences().getInt(App.PREFS_CURRENT_SHARE_MODE, 0);
    }

    private boolean updateHeader(EscObjectList<ContestHeader> headers, ContestHeader newHeader) {
        for (int i = 0; i < headers.size(); i++) {
            if (headers.get(i).equals(newHeader.getId())) {
                headers.set(i, newHeader);
                return true;
            }
        }
        return false;
    }

    private void updateAvailableHeaders(ContestHeader contestHeader) {
        for (int i = 0; i < availableContests.size(); i++) {
            EscObjectList<ContestHeader> contestsForType = availableContests.get(i);
            if (updateHeader(contestsForType, contestHeader)) {
                break;
            }
        }
    }

    private ArrayMap<Integer, String> getContestResourceIds() {
        try {
            //final R.raw rawResources = R.raw.class.newInstance();
            //final Class<R.raw> c = R.raw.class;
            final Field[] fields = R.raw.class.getDeclaredFields();
            ArrayMap<Integer, String> ids = new ArrayMap<Integer, String>();

            String name = "";
            for (int i = 0, max = fields.length; i < max; i++) {
                name = fields[i].getName();
                if (name.startsWith("contest_")) {
                    ids.put(fields[i].getInt(null), name);
                }
            }
            return ids;
        } catch (Exception e) {
            Logger.e("Failed getting raw resource.");
        }
        return null;
    }

    private Contest CreateContest(String id) {
        Contest contest = databaseManager.getContest(getHeaderForId(id), getCountryManager());
        if (contest.getEntries().size() == 0 || contest.getUserData().getUpdateFromXml() == true) {
            ContestHeader.Type contestType = contest.getHeader().getType();
            try {
                boolean updateExistingContest = contest.getUserData().getUpdateFromXml() == true;
                Context context = App.getContext();
                int resId = context.getResources().getIdentifier(id, "raw", context.getApplicationInfo().packageName);
                InputStream inputStream = context.getResources().openRawResource(resId);

                // No entries found - altogether a new contest
                if (!updateExistingContest) {
                    EscObjectList<ContestEntry> entries = XmlParser.getEntries(
                            inputStream, contest.getId(), getCountryManager(), true, contestType);
                    if (entries != null) {
                        contest.clearEntries();
                        for (ContestEntry entry : entries) {
                            contest.addEntry(entry);
                        }
                        databaseManager.setContest(contest);
                    }

                // Update entries found from database
                } else {
                    updateContest(inputStream, contest);
                    databaseManager.resetUpdateFromXml(contest.getId());
                }
            } catch (Exception e) {
                Logger.e("Error parsing contest " + id  + " XML file: " + e.toString());
            }
        }
        return contest;
    }

    private boolean updateContest(InputStream inputStream, Contest contest) {
        boolean contestUpdated = false;
        EscObjectList<ContestEntry> removedEntries = new EscObjectList<>(ContestEntry.class);
        EscObjectList<ContestEntry> addedEntries = new EscObjectList<>(ContestEntry.class);
        ContestHeader.Type contestType = contest.getHeader().getType();
        try {
            contestUpdated = XmlParser.updateContest(
                    inputStream, contest, removedEntries, addedEntries, getCountryManager(), contestType);
        } catch (Exception e) {
            Logger.e("Error parsing contest " + contest.getId()  + " XML file: " + e.toString());
        }
        if (contestUpdated) {
            // If greater version was found, update also database tables
            databaseManager.updateContest(contest, removedEntries, addedEntries);
            updateAvailableHeaders(contest.getHeader());
        }
        return contestUpdated;
    }

    private void StoreActiveContest() {
        if (currentContest != null) {
            Logger.i("Storing active contest: " + currentContest.getId());
            SharedPreferences settings = App.getSharedPreferences();
            SharedPreferences.Editor editor = settings.edit();
            editor.putString(App.PREFS_ACTIVE_CONTEST_ID_KEY, currentContest.getId());
            editor.putInt(App.PREFS_ACTIVE_CONTEST_YEAR_KEY, currentContest.getYear());
            editor.putString(App.PREFS_ACTIVE_CONTEST_CITY_KEY, currentContest.getCity());
            editor.putString(App.PREFS_ACTIVE_CONTEST_MOTTO_KEY, currentContest.getMotto());
            editor.putInt(App.PREFS_ACTIVE_CONTEST_STATE_KEY, currentContest.getState().ordinal());
            editor.putFloat(App.PREFS_ACTIVE_CONTEST_VERSION_KEY, (float)currentContest.getVersion());
            editor.commit();
        }
    }

    private boolean handleAppUpgrade() {
        int storedAppVersion = getStoredAppVersion();
        int currentAppVersion = getCurrentAppVersion();
        if (currentAppVersion > 0 && currentAppVersion != storedAppVersion) {
            databaseManager.invalidateUserContestDataForXml();
            storeAppVersion(currentAppVersion);
            return true;
        }
        return false;
    }

    private void storeAppVersion(int currentAppVersion) {
        SharedPreferences settings = App.getSharedPreferences();
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(App.PREFS_CURRENT_APP_VERSION_KEY, currentAppVersion);
        editor.commit();
    }

    private int getStoredAppVersion() {
        return App.getSharedPreferences().getInt(App.PREFS_CURRENT_APP_VERSION_KEY, 0);
    }

    private int getCurrentAppVersion() {
        Context appContext = App.getContext();
        int versionCode = 0;
        try {
            PackageInfo packageInfo = appContext.getPackageManager()
                    .getPackageInfo(appContext.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (Exception e) {
            Logger.e("Exception when getting version code: " + e.toString());
        }
        return versionCode;
    }

    /**
     * Class for year not found exception.
     *
     * Exception is thrown if contest with a given year is not found in app resources.
     */
    public class ContestNotFoundException extends Exception {

        private String id;

        private ContestNotFoundException(String id) {
            super();
            this.id = id;
        }

        public String getId() {
            return id;
        }

    }

}
