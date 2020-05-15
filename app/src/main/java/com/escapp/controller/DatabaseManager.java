package com.escapp.controller;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;
import java.util.HashMap;

import com.escapp.model.UserComment;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Country;
import com.escapp.model.EscObjectList;
import com.escapp.model.Rate;
import com.escapp.model.UserContest;

/**
 * Created by laura on 7.12.14.
 */
public class DatabaseManager extends SQLiteOpenHelper {
    private static String TABLE_NAME_V1(String extraName) {
        return extraName.replaceAll(".", "_");
    }

    private static String TABLE_NAME(String extraName) {
        return extraName.replace('.', '_');
    }

    private static final String COUNTRY_TABLE = TABLE_NAME(Country.EXTRA_NAME);
    private static final String CONTEST_ENTRY_TABLE = TABLE_NAME(ContestEntry.EXTRA_NAME);
    private static final String CONTEST_HEADER_TABLE = TABLE_NAME(ContestHeader.EXTRA_NAME);
    private static final String RATE_TABLE = TABLE_NAME(Rate.EXTRA_NAME);
    private static final String COMMENT_TABLE = TABLE_NAME(UserComment.EXTRA_NAME);
    private static final String USER_CONTEST_TABLE = TABLE_NAME(UserContest.EXTRA_NAME);

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRY =
            "CREATE TABLE " + CONTEST_ENTRY_TABLE + " (" +
                    ContestEntry.Columns.PROP_ID + " INTEGER PRIMARY KEY," +
                    ContestEntry.Columns.PROP_CONTEST_ID + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_ARTIST + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_COUNTRY + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_ENGTITLE + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_ENTRYFLAGS + " INTEGER," +
                    ContestEntry.Columns.PROP_FINAL_NBR + " INTEGER," +
                    ContestEntry.Columns.PROP_LANGUAGE + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_LYRICSLINK + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_SPOTIFYLINK + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_RESULT + " INTEGER," +
                    ContestEntry.Columns.PROP_SEMIFINAL_NBR + " INTEGER," +
                    ContestEntry.Columns.PROP_TITLE  + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_YOUTUBELINK  + TEXT_TYPE + COMMA_SEP +
                    ContestEntry.Columns.PROP_OFFICIALVIDEO + " INTEGER," +
                    "CONSTRAINT foreignKey FOREIGN KEY(" + ContestEntry.Columns.PROP_CONTEST_ID + ")" +
                    " REFERENCES " + CONTEST_HEADER_TABLE + "(" + ContestHeader.Columns.PROP_ID + ")" + COMMA_SEP +
                    "CONSTRAINT foreignKey FOREIGN KEY(" + ContestEntry.Columns.PROP_COUNTRY + ")" +
                    " REFERENCES " + COUNTRY_TABLE + "(" + Country.Columns.PROP_NAME + ")" +
            " )";

    private static final String SQL_ALTER_ENTRY_v1_TO_v2 =
            "ALTER TABLE " + CONTEST_ENTRY_TABLE + " ADD COLUMN " +
                    ContestEntry.Columns.PROP_OFFICIALVIDEO + " INTEGER";

    private static final String SQL_DELETE_ENTRY =
            "DROP TABLE IF EXISTS " + CONTEST_ENTRY_TABLE;

    private static final String SQL_CREATE_CONTEST =
            "CREATE TABLE " + CONTEST_HEADER_TABLE + " (" +
                    ContestHeader.Columns.PROP_ID + TEXT_TYPE + " PRIMARY KEY," +
                    ContestHeader.Columns.PROP_CITY + TEXT_TYPE + COMMA_SEP +
                    ContestHeader.Columns.PROP_MOTTO + TEXT_TYPE + COMMA_SEP +
                    ContestHeader.Columns.PROP_STATE + " INTEGER," +
                    ContestHeader.Columns.PROP_YEAR + " INTEGER," +
                    ContestHeader.Columns.PROP_VERSION + " DOUBLE," +
                    ContestHeader.Columns.PROP_UPDATE_SRC + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_CONTEST =
            "DROP TABLE IF EXISTS " + CONTEST_HEADER_TABLE;

    private static final String SQL_CREATE_COUNTRY =
            "CREATE TABLE " + COUNTRY_TABLE + " (" +
                    Country.Columns.PROP_NAME + TEXT_TYPE + " PRIMARY KEY," +
                    Country.Columns.PROP_FLAGPATH + TEXT_TYPE + COMMA_SEP +
                    Country.Columns.PROP_WIKIPATH + TEXT_TYPE + COMMA_SEP +
                    Country.Columns.PROP_ESCWIKIPATH + TEXT_TYPE +
                    " )";

    private static final String SQL_DELETE_COUNTRY =
            "DROP TABLE IF EXISTS " + COUNTRY_TABLE;

    private static final String SQL_CREATE_RATE =
            "CREATE TABLE " + RATE_TABLE + " (" +
                    Rate.Columns.PROP_RATE_MODE + " INTEGER," +
                    Rate.Columns.PROP_ENTRY_ID + " INTEGER," +
                    Rate.Columns.PROP_RATE + " INTEGER," +
                    "CONSTRAINT primaryKey PRIMARY KEY " + "(" + Rate.Columns.PROP_RATE_MODE + "," +
                    Rate.Columns.PROP_ENTRY_ID + ")," +
                    "CONSTRAINT foreignKey FOREIGN KEY(" + Rate.Columns.PROP_ENTRY_ID + ")" +
                    " REFERENCES " + CONTEST_ENTRY_TABLE + "(" + ContestEntry.Columns.PROP_ID + ")" +
                    " )";

    private static final String SQL_DELETE_RATE =
            "DROP TABLE IF EXISTS " + RATE_TABLE;

    private static final String SQL_CREATE_COMMENT =
            "CREATE TABLE " + COMMENT_TABLE + " (" +
                    UserComment.Columns.PROP_ENTRY_ID + " INTEGER PRIMARY KEY," +
                    UserComment.Columns.PROP_COMMENT + TEXT_TYPE + COMMA_SEP +
                    "CONSTRAINT foreignKey FOREIGN KEY(" + UserComment.Columns.PROP_ENTRY_ID + ")" +
                    " REFERENCES " + CONTEST_ENTRY_TABLE + "(" + ContestEntry.Columns.PROP_ID + ")" +
                    " )";

    private static final String SQL_DELETE_COMMENT =
            "DROP TABLE IF EXISTS " + COMMENT_TABLE;

    private static final String SQL_CREATE_USER_CONTEST =
            "CREATE TABLE " + USER_CONTEST_TABLE + " (" +
                    UserContest.Columns.PROP_CONTEST_ID + TEXT_TYPE + " PRIMARY KEY," +
                    UserContest.Columns.PROP_RATE_MODE + " INTEGER," +
                    UserContest.Columns.PROP_UPDATE_FROM_XML + " INTEGER," +
                    "CONSTRAINT foreignKey FOREIGN KEY(" + UserContest.Columns.PROP_CONTEST_ID + ")" +
                    " REFERENCES " + CONTEST_HEADER_TABLE + "(" + ContestHeader.Columns.PROP_ID + ")" +
                    " )";

    private static final String SQL_DELETE_USER_CONTEST =
            "DROP TABLE IF EXISTS " + USER_CONTEST_TABLE;

    // Increment the database version when changing schema.
    public static final int DATABASE_VERSION = 2;
    public static final String DATABASE_NAME = "EscGuide.db";

    public DatabaseManager(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRY);
        db.execSQL(SQL_CREATE_CONTEST);
        db.execSQL(SQL_CREATE_COUNTRY);
        db.execSQL(SQL_CREATE_RATE);
        db.execSQL(SQL_CREATE_COMMENT);
        db.execSQL(SQL_CREATE_USER_CONTEST);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion == DATABASE_VERSION) {
            switch (oldVersion) {
                // upgrade logic from version 1 to 2
                case 1:
                    // Rename old tables (bug in db version 1 in table naming)
                    String renameSql = "ALTER TABLE " + TABLE_NAME_V1(Country.EXTRA_NAME) + " RENAME TO " + COUNTRY_TABLE;
                    db.execSQL(renameSql);
                    renameSql = "ALTER TABLE " + TABLE_NAME_V1(ContestEntry.EXTRA_NAME) + " RENAME TO " + CONTEST_ENTRY_TABLE;
                    db.execSQL(renameSql);
                    renameSql = "ALTER TABLE " + TABLE_NAME_V1(ContestHeader.EXTRA_NAME) + " RENAME TO " + CONTEST_HEADER_TABLE;
                    db.execSQL(renameSql);
                    renameSql = "ALTER TABLE " + TABLE_NAME_V1(Rate.EXTRA_NAME) + " RENAME TO " + RATE_TABLE;
                    db.execSQL(renameSql);
                    renameSql = "ALTER TABLE " + TABLE_NAME_V1(UserContest.EXTRA_NAME) + " RENAME TO " + USER_CONTEST_TABLE;
                    db.execSQL(renameSql);
                    // New schema
                    db.execSQL(SQL_ALTER_ENTRY_v1_TO_v2);
                    db.execSQL(SQL_CREATE_COMMENT);
                // add here flowing cases for new versions
                    break;
                default:
                    Logger.e("Encountered unknown database version " + oldVersion);
                    break;
            }
        } else {
            Logger.e("Try to install unsupported db version" + newVersion);
        }
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public ArrayList<Country> getCountries() {
        ArrayList<Country> countries = new ArrayList<Country>();
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                Country.Columns.PROP_NAME,
                Country.Columns.PROP_FLAGPATH,
                Country.Columns.PROP_WIKIPATH,
                Country.Columns.PROP_ESCWIKIPATH,
        };

        String sortOrder =
                Country.Columns.PROP_NAME + " DESC";

        Cursor cursor = db.query(
                COUNTRY_TABLE,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);

        cursor.moveToFirst();
        String name = null;
        String flagPath = null;
        String wikiPath = null;
        while (!cursor.isAfterLast()) {
            name = cursor.getString(cursor.getColumnIndex(Country.Columns.PROP_NAME));
            flagPath = cursor.getString(cursor.getColumnIndex(Country.Columns.PROP_FLAGPATH));
            wikiPath = cursor.getString(cursor.getColumnIndex(Country.Columns.PROP_WIKIPATH));
            // TODO: escWikiPath
            countries.add(new Country(name, flagPath, wikiPath));
            cursor.moveToNext();
        }
        cursor.close();
        return countries;
    }

    public void setCountries(ArrayList<Country> countries) {
        clearCountries();
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = null;
        for (Country country : countries) {
            values = new ContentValues();
            values.put(Country.Columns.PROP_NAME, country.getName());
            values.put(Country.Columns.PROP_FLAGPATH, country.getFlagPath());
            values.put(Country.Columns.PROP_WIKIPATH, country.getWikiPath());
            // TODO:
            values.put(Country.Columns.PROP_ESCWIKIPATH, "");
            db.insert(COUNTRY_TABLE,
                    null,
                    values);
        }
    }

    public void clearCountries() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(COUNTRY_TABLE, null, null);
    }

    public EscObjectList<ContestHeader> getContests() {
        EscObjectList<ContestHeader> contests = new EscObjectList<ContestHeader>(ContestHeader.class);
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                ContestHeader.Columns.PROP_ID,
                ContestHeader.Columns.PROP_CITY,
                ContestHeader.Columns.PROP_MOTTO,
                ContestHeader.Columns.PROP_STATE,
                ContestHeader.Columns.PROP_YEAR,
                ContestHeader.Columns.PROP_VERSION,
                ContestHeader.Columns.PROP_UPDATE_SRC,
        };

        String sortOrder =
                ContestHeader.Columns.PROP_YEAR + " DESC";

        Cursor cursor = db.query(
                CONTEST_HEADER_TABLE,
                projection,
                null,
                null,
                null,
                null,
                sortOrder);

        String id, city, motto, updateSrc;
        int state, year;
        double version;
        while (cursor.moveToNext()) {
            id = cursor.getString(cursor.getColumnIndex(ContestHeader.Columns.PROP_ID));
            city = cursor.getString(cursor.getColumnIndex(ContestHeader.Columns.PROP_CITY));
            motto = cursor.getString(cursor.getColumnIndex(ContestHeader.Columns.PROP_MOTTO));
            state = cursor.getInt(cursor.getColumnIndex(ContestHeader.Columns.PROP_STATE));
            year = cursor.getInt(cursor.getColumnIndex(ContestHeader.Columns.PROP_YEAR));
            version = cursor.getDouble(cursor.getColumnIndex(ContestHeader.Columns.PROP_VERSION));
            updateSrc = cursor.getString(cursor.getColumnIndex(ContestHeader.Columns.PROP_UPDATE_SRC));
            contests.add(
                    new ContestHeader(id, year, city, motto, ContestHeader.State.values()[state], version, updateSrc));
        }
        cursor.close();
        return contests;
    }

    public void setContests(EscObjectList<ContestHeader> contests) {
        clearContests();
        SQLiteDatabase db = getWritableDatabase();

        for (ContestHeader contest : contests) {
            addContestHeader(db, contest);
        }
    }

    public void addContest(ContestHeader newContest) {
        SQLiteDatabase db = getWritableDatabase();
        addContestHeader(db, newContest);
    }

    public void updateContestHeader(ContestHeader contestHeader) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = null;
        values = new ContentValues();
        values.put(ContestHeader.Columns.PROP_CITY, contestHeader.getCity());
        values.put(ContestHeader.Columns.PROP_MOTTO, contestHeader.getMotto());
        values.put(ContestHeader.Columns.PROP_STATE, contestHeader.getState().ordinal());
        values.put(ContestHeader.Columns.PROP_YEAR, contestHeader.getYear());
        values.put(ContestHeader.Columns.PROP_VERSION, contestHeader.getVersion());
        values.put(ContestHeader.Columns.PROP_UPDATE_SRC, contestHeader.getUpdateSrc());
        String where = ContestHeader.Columns.PROP_ID + "=?";
        String[] args = {contestHeader.getId()};
        db.update(CONTEST_HEADER_TABLE, values, where, args);
    }

    private void addContestHeader(SQLiteDatabase db, ContestHeader contestHeader) {
        ContentValues values = new ContentValues();
        values.put(ContestHeader.Columns.PROP_ID, contestHeader.getId());
        values.put(ContestHeader.Columns.PROP_CITY, contestHeader.getCity());
        values.put(ContestHeader.Columns.PROP_MOTTO, contestHeader.getMotto());
        values.put(ContestHeader.Columns.PROP_STATE, contestHeader.getState().ordinal());
        values.put(ContestHeader.Columns.PROP_YEAR, contestHeader.getYear());
        values.put(ContestHeader.Columns.PROP_VERSION, contestHeader.getVersion());
        values.put(ContestHeader.Columns.PROP_UPDATE_SRC, contestHeader.getUpdateSrc());
        db.insert(CONTEST_HEADER_TABLE, null, values);
    }

    private void clearContests() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(CONTEST_HEADER_TABLE, null, null);
    }

    public Contest getContest(ContestHeader contestHeader, CountryManager countryManager) {
        Contest contest = new Contest(contestHeader, getUserContest(contestHeader.getId()));
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                ContestEntry.Columns.PROP_ID,
                ContestEntry.Columns.PROP_CONTEST_ID,
                ContestEntry.Columns.PROP_ARTIST,
                ContestEntry.Columns.PROP_COUNTRY,
                ContestEntry.Columns.PROP_ENGTITLE,
                ContestEntry.Columns.PROP_ENTRYFLAGS,
                ContestEntry.Columns.PROP_FINAL_NBR,
                ContestEntry.Columns.PROP_LANGUAGE,
                ContestEntry.Columns.PROP_LYRICSLINK,
                ContestEntry.Columns.PROP_SPOTIFYLINK,
                ContestEntry.Columns.PROP_RESULT,
                ContestEntry.Columns.PROP_SEMIFINAL_NBR,
                ContestEntry.Columns.PROP_TITLE,
                ContestEntry.Columns.PROP_YOUTUBELINK,
                ContestEntry.Columns.PROP_OFFICIALVIDEO,
        };

        String sortOrder =
                ContestEntry.Columns.PROP_ID + " DESC";

        String where = ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {contest.getId()};
        Cursor cursor = db.query(
                CONTEST_ENTRY_TABLE,
                projection,
                where,
                args,
                null,
                null,
                sortOrder);

        cursor.moveToFirst();
        int id = 0;
        String artist = null;
        String country = null;
        String engTitle = null;
        int entryFlags = 0;
        int finalNbr = 0;
        String language = null;
        String lyricsLink = null;
        int result = 0;
        int semifinalNbr = 0;
        String title = null;
        String youtubeLink = null;
        boolean officialVideo = false;
        String spotifyLink = null;

        while (!cursor.isAfterLast()) {
            id = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_ID));
            artist = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_ARTIST));
            country = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_COUNTRY));
            engTitle = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_ENGTITLE));
            entryFlags = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_ENTRYFLAGS));
            finalNbr = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_FINAL_NBR));
            language = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_LANGUAGE));
            lyricsLink = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_LYRICSLINK));
            spotifyLink = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_SPOTIFYLINK));
            result = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_RESULT));
            semifinalNbr = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_SEMIFINAL_NBR));
            title = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_TITLE));
            youtubeLink = cursor.getString(cursor.getColumnIndex(ContestEntry.Columns.PROP_YOUTUBELINK));
            officialVideo = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_OFFICIALVIDEO)) == 0 ? false : true;
            contest.addEntry(new ContestEntry(id, contestHeader.getId(), artist, title, engTitle,
                    countryManager.getCountry(country), youtubeLink, officialVideo,
                    spotifyLink, lyricsLink, entryFlags,
                    language, semifinalNbr, finalNbr, result));
            cursor.moveToNext();
        }
        cursor.close();
        return contest;
    }

    public int getNextEntryId() {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                "MAX("+ ContestEntry.Columns.PROP_ID +") as " + ContestEntry.Columns.PROP_ID
        };

        Cursor cursor = db.query(
                CONTEST_ENTRY_TABLE,
                projection,
                null,
                null,
                null,
                null,
                null);

        int id = 0;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            id = cursor.getInt(cursor.getColumnIndex(ContestEntry.Columns.PROP_ID));
        }
        cursor.close();
        return id + 1;
    }

    public void setContest(Contest contest) {
        clearContest(contest.getId());
        SQLiteDatabase db = getWritableDatabase();

        for (ContestEntry entry : contest.getEntries()) {
            insertEntry(db, entry);
        }
    }

    public void updateContest(
            Contest contest, EscObjectList<ContestEntry> removedEntries, EscObjectList<ContestEntry> addedEntries) {

        SQLiteDatabase db = getWritableDatabase();
        updateContestHeader(contest.getHeader());

        // Clear removed entries
        for (ContestEntry removedEntry : removedEntries) {
            removeEntry(db, removedEntry.getId());
        }
        removedEntries.clear();

        // Add new entries
        for (ContestEntry addedEntry : addedEntries) {
            insertEntry(db, addedEntry);
        }

        // Update existing entries
        for (ContestEntry entry : contest.getEntries()) {
            updateEntry(db, entry);
        }
    }

    private void insertEntry(SQLiteDatabase db, ContestEntry entry) {
        ContentValues values = new ContentValues();
        values.put(ContestEntry.Columns.PROP_ID, entry.getId());
        values.put(ContestEntry.Columns.PROP_CONTEST_ID, entry.getContestId());
        values.put(ContestEntry.Columns.PROP_ARTIST, entry.getArtist());
        values.put(ContestEntry.Columns.PROP_COUNTRY, entry.getCountry().getName());
        values.put(ContestEntry.Columns.PROP_ENGTITLE, entry.getEngTitle());
        values.put(ContestEntry.Columns.PROP_ENTRYFLAGS, entry.getFlags());
        values.put(ContestEntry.Columns.PROP_FINAL_NBR, entry.getFinalNbr());
        values.put(ContestEntry.Columns.PROP_LANGUAGE, entry.getLanguage());
        values.put(ContestEntry.Columns.PROP_LYRICSLINK, entry.getLyricsLink());
        values.put(ContestEntry.Columns.PROP_SPOTIFYLINK, entry.getSpotifyLink());
        values.put(ContestEntry.Columns.PROP_RESULT, entry.getResult());
        values.put(ContestEntry.Columns.PROP_SEMIFINAL_NBR, entry.getSemifinalNbr());
        values.put(ContestEntry.Columns.PROP_TITLE, entry.getTitle());
        values.put(ContestEntry.Columns.PROP_YOUTUBELINK, entry.getYouTubeLink());
        values.put(ContestEntry.Columns.PROP_OFFICIALVIDEO, entry.isOfficialVideo());
        db.insert(CONTEST_ENTRY_TABLE, null, values);
    }

    private void updateEntry(SQLiteDatabase db, ContestEntry entry) {
        ContentValues values = new ContentValues();
        values.put(ContestEntry.Columns.PROP_CONTEST_ID, entry.getContestId());
        values.put(ContestEntry.Columns.PROP_ARTIST, entry.getArtist());
        values.put(ContestEntry.Columns.PROP_COUNTRY, entry.getCountry().getName());
        values.put(ContestEntry.Columns.PROP_ENGTITLE, entry.getEngTitle());
        values.put(ContestEntry.Columns.PROP_ENTRYFLAGS, entry.getFlags());
        values.put(ContestEntry.Columns.PROP_FINAL_NBR, entry.getFinalNbr());
        values.put(ContestEntry.Columns.PROP_LANGUAGE, entry.getLanguage());
        values.put(ContestEntry.Columns.PROP_LYRICSLINK, entry.getLyricsLink());
        values.put(ContestEntry.Columns.PROP_SPOTIFYLINK, entry.getSpotifyLink());
        values.put(ContestEntry.Columns.PROP_RESULT, entry.getResult());
        values.put(ContestEntry.Columns.PROP_SEMIFINAL_NBR, entry.getSemifinalNbr());
        values.put(ContestEntry.Columns.PROP_TITLE, entry.getTitle());
        values.put(ContestEntry.Columns.PROP_YOUTUBELINK, entry.getYouTubeLink());
        values.put(ContestEntry.Columns.PROP_OFFICIALVIDEO, entry.isOfficialVideo());

        String where = ContestEntry.Columns.PROP_ID + "=?";
        String[] args = {Integer.toString(entry.getId())};
        db.update(CONTEST_ENTRY_TABLE, values, where, args);
    }

    private void removeEntry(SQLiteDatabase db, int id) {
        String where = ContestEntry.Columns.PROP_ID + "=?";
        String[] args = {new Integer(id).toString()};
        db.delete(CONTEST_ENTRY_TABLE, where, args);
    }

    private void clearContest(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String where = ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {id};
        db.delete(CONTEST_ENTRY_TABLE, where, args);
    }

    public UserContest getUserContest(String contestId) {
        UserContest userContest = getUserContestData(contestId);

        Cursor cursor = getCommentsCursor(contestId);
        HashMap<Integer, UserComment> comments = new HashMap<>();
        while (cursor.moveToNext()) {
            int entryId = cursor.getInt(cursor.getColumnIndex(UserComment.Columns.PROP_ENTRY_ID));
            String comment = cursor.getString(cursor.getColumnIndex(UserComment.Columns.PROP_COMMENT));
            Logger.d("Found comment " + comment + " for entry " + entryId);
            comments.put(entryId, new UserComment(entryId, comment));
        }
        cursor.close();

        cursor = getRatesCursor(contestId, userContest.getRateMode());
        UserComment userComment = null;
        while (cursor.moveToNext()) {
            int entryId = cursor.getInt(cursor.getColumnIndex(Rate.Columns.PROP_ENTRY_ID));
            int rate = cursor.getInt(cursor.getColumnIndex(Rate.Columns.PROP_RATE));
            Logger.d("Found rate " + rate + " for entry " + entryId);
            userComment = comments.remove(entryId);
            if (userComment == null) {
                userComment = new UserComment(entryId, "");
            }
            userContest.addRate(new Rate(userContest.getRateMode(), rate, entryId, userComment));
        }
        cursor.close();

        while (comments.size() > 0) {
            int entryId = comments.keySet().iterator().next();
            userComment = comments.remove(entryId);
            userContest.addRate(new Rate(userContest.getRateMode(), 0, entryId, userComment));
        }
        return userContest;
    }

    public void setRate(Rate rate) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(Rate.Columns.PROP_RATE, rate.getRate());
        if (!rateExists(db, rate.getRateMode(), rate.getEntryId())) {
            Logger.v("Adding new rate " + rate.getRate() + " for entry " + rate.getEntryId());

            values.put(Rate.Columns.PROP_RATE_MODE, rate.getRateMode().ordinal());
            values.put(Rate.Columns.PROP_ENTRY_ID, rate.getEntryId());
            db.insert(RATE_TABLE, null, values);
        } else {
            if (rate.getRate() == 0) {
                Logger.v("Removing rate " + rate.getRate() + " for entry " + rate.getEntryId());

                String where = Rate.Columns.PROP_RATE_MODE + "=? AND " + Rate.Columns.PROP_ENTRY_ID + "=?";
                String[] args = {Integer.toString(rate.getRateMode().ordinal()), Integer.toString(rate.getEntryId())};
                db.delete(RATE_TABLE, where, args);
            } else {
                Logger.v("Updating rate " + rate.getRate() + " for entry " + rate.getEntryId());

                String where = Rate.Columns.PROP_RATE_MODE + "=? AND " + Rate.Columns.PROP_ENTRY_ID + "=?";
                String[] args = {Integer.toString(rate.getRateMode().ordinal()), Integer.toString(rate.getEntryId())};
                db.update(RATE_TABLE, values, where, args);
            }
        }
        setComment(rate.getComment());
    }

    public void setComment(UserComment userComment) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(UserComment.Columns.PROP_COMMENT, userComment.getComment());
        if (!commentExists(db, userComment.getEntryId())) {
            Logger.v("Adding new comment " + userComment.getComment() + " for entry " + userComment.getEntryId());

            values.put(UserComment.Columns.PROP_ENTRY_ID, userComment.getEntryId());
            db.insert(COMMENT_TABLE, null, values);
        } else {
            if (userComment.getComment().length() == 0) {
                Logger.v("Removing comment " + userComment.getComment() + " for entry " + userComment.getEntryId());

                String where = UserComment.Columns.PROP_ENTRY_ID + "=?";
                String[] args = {Integer.toString(userComment.getEntryId())};
                db.delete(COMMENT_TABLE, where, args);
            } else {
                Logger.v("Updating comment " + userComment.getComment() + " for entry " + userComment.getEntryId());

                String where = UserComment.Columns.PROP_ENTRY_ID + "=?";
                String[] args = {Integer.toString(userComment.getEntryId())};
                db.update(COMMENT_TABLE, values, where, args);
            }
        }
    }

    public boolean rateExists(SQLiteDatabase db, UserContest.RateMode rateMode, int entryId) {
        String[] projection = {
                Rate.Columns.PROP_RATE,
        };

        String where = Rate.Columns.PROP_RATE_MODE + "=? AND " + Rate.Columns.PROP_ENTRY_ID + "=?";
        String[] args = {Integer.toString(rateMode.ordinal()), Integer.toString(entryId)};
        Cursor cursor = db.query(
                RATE_TABLE,
                projection,
                where,
                args,
                null,
                null,
                null);
        int rate = 0;
        boolean found = false;
        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            rate = cursor.getInt(cursor.getColumnIndex(Rate.Columns.PROP_RATE));
            found = true;
            Logger.d("getRate: Found rate " + rate + " for entry " + entryId);
        }
        cursor.close();
        return found;
    }

    public boolean commentExists(SQLiteDatabase db, int entryId) {
        String[] projection = {
                UserComment.Columns.PROP_COMMENT,
        };

        String where = UserComment.Columns.PROP_ENTRY_ID + "=?";
        String[] args = {Integer.toString(entryId)};
        Cursor cursor = db.query(
                COMMENT_TABLE,
                projection,
                where,
                args,
                null,
                null,
                null);
        boolean found = false;
        if (cursor.getCount() == 1) {
            found = true;
        }
        cursor.close();
        return found;
    }

    public UserContest getUserContestData(String contestId) {
        SQLiteDatabase db = getReadableDatabase();

        String[] projection = {
                UserContest.Columns.PROP_RATE_MODE,
                UserContest.Columns.PROP_UPDATE_FROM_XML
        };

        String where = UserContest.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {contestId};
        Cursor cursor = db.query(
                USER_CONTEST_TABLE,
                projection,
                where,
                args,
                null,
                null,
                null);

        UserContest.RateMode rateMode = null;
        boolean updateFromXml = false;

        if (cursor.getCount() == 1) {
            cursor.moveToFirst();
            rateMode = UserContest.RateMode.values()[
                    cursor.getInt(cursor.getColumnIndex(UserContest.Columns.PROP_RATE_MODE))];
            updateFromXml =
                    cursor.getInt(cursor.getColumnIndex(UserContest.Columns.PROP_UPDATE_FROM_XML)) == 0 ? false : true;
        }
        cursor.close();

        boolean rateModeSet = (rateMode != null);
        if (!rateModeSet) {
            rateMode = UserContest.RateMode.TRADITIONAL;
        }

        UserContest userContest = new UserContest(contestId, rateMode, updateFromXml, rateModeSet);
        if (!rateModeSet) {
            setUserContestData(userContest);
        }
        return userContest;
    }

    public void setUserContestData(UserContest userContest) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContest.Columns.PROP_CONTEST_ID, userContest.getContestId());
        values.put(UserContest.Columns.PROP_RATE_MODE, userContest.getRateMode().ordinal());
        values.put(UserContest.Columns.PROP_UPDATE_FROM_XML, userContest.getUpdateFromXml() ? 1 : 0);
        db.insert(USER_CONTEST_TABLE, null, values);
    }

    public void updateRateMode(String contestId, UserContest.RateMode rateMode) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContest.Columns.PROP_RATE_MODE, rateMode.ordinal());
        String where = ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {contestId};
        db.update(USER_CONTEST_TABLE, values, where, args);
    }

    public void resetRates(String contestId, UserContest.RateMode rateMode) {
        Cursor cursor = getRatesCursor(contestId, rateMode);
        SQLiteDatabase db = getReadableDatabase();

        while (cursor.moveToNext()) {
            int entryId = cursor.getInt(cursor.getColumnIndex(Rate.Columns.PROP_ENTRY_ID));
            String where = Rate.Columns.PROP_ENTRY_ID + "=? AND " + Rate.Columns.PROP_RATE_MODE + "=?";
            String[] args = {Integer.toString(entryId), Integer.toString(rateMode.ordinal()), };
            db.delete(RATE_TABLE, where, args);
        }
        cursor.close();
    }

    public void resetComments(String contestId) {
        Cursor cursor = getCommentsCursor(contestId);
        SQLiteDatabase db = getReadableDatabase();

        while (cursor.moveToNext()) {
            int entryId = cursor.getInt(cursor.getColumnIndex(UserComment.Columns.PROP_ENTRY_ID));
            String where = UserComment.Columns.PROP_ENTRY_ID + "=?";
            String[] args = {Integer.toString(entryId)};
            db.delete(COMMENT_TABLE, where, args);
        }
        cursor.close();
    }

    public void resetUpdateFromXml(String contestId) {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContest.Columns.PROP_UPDATE_FROM_XML, 0);
        String where = ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {contestId};
        db.update(USER_CONTEST_TABLE, values, where, args);
    }

    public void invalidateUserContestDataForXml() {
        SQLiteDatabase db = getReadableDatabase();

        ContentValues values = new ContentValues();
        values.put(UserContest.Columns.PROP_UPDATE_FROM_XML, 1);
        db.update(USER_CONTEST_TABLE, values, null, null);
    }

    private Cursor getRatesCursor(String contestId, UserContest.RateMode rateMode) {
        // Get rates for this rate mode
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(RATE_TABLE
                        + " INNER JOIN "
                        + CONTEST_ENTRY_TABLE
                        + " ON "
                        + Rate.Columns.PROP_ENTRY_ID
                        + " = "
                        + (CONTEST_ENTRY_TABLE + "." + ContestEntry.Columns.PROP_ID));
        // Get cursor
        String where = Rate.Columns.PROP_RATE_MODE + "=? AND " + ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {Integer.toString(rateMode.ordinal()), contestId};
        Cursor cursor = queryBuilder.query(db, new String[] {
                Rate.Columns.PROP_ENTRY_ID,
                Rate.Columns.PROP_RATE_MODE,
                Rate.Columns.PROP_RATE}, where, args, null, null, null);
        Logger.d("Found " + cursor.getCount() + " rates for contest " + contestId);

        return cursor;
    }

    private Cursor getCommentsCursor(String contestId) {
        // Get rates for this rate mode
        SQLiteDatabase db = getReadableDatabase();
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder
                .setTables(COMMENT_TABLE
                        + " INNER JOIN "
                        + CONTEST_ENTRY_TABLE
                        + " ON "
                        + UserComment.Columns.PROP_ENTRY_ID
                        + " = "
                        + (CONTEST_ENTRY_TABLE + "." + ContestEntry.Columns.PROP_ID));
        // Get cursor
        String where = ContestEntry.Columns.PROP_CONTEST_ID + "=?";
        String[] args = {contestId};
        Cursor cursor = queryBuilder.query(db, new String[] {
                UserComment.Columns.PROP_ENTRY_ID,
                UserComment.Columns.PROP_COMMENT}, where, args, null, null, null);
        Logger.d("Found " + cursor.getCount() + " comments for contest " + contestId);

        return cursor;
    }
}
