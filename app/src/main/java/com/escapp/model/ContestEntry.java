package com.escapp.model;

import android.os.Parcel;
import android.provider.BaseColumns;

import com.escapp.controller.Logger;

/**
 * Class for storing single contest entry data.
 *
 * @author  Laura Vuorenoja
 */
public class ContestEntry extends EscObject {

    public static final String EXTRA_NAME = ContestEntry.class.getName();

    public static final EscObjectCreator<ContestEntry> CREATOR
            = new EscObjectCreator<ContestEntry>(ContestEntry.class);

    public static final int ENTRY_FLAG_1ST_SEMI = 0x01;
    public static final int ENTRY_FLAG_2ND_SEMI = 0x02;
    public static final int ENTRY_FLAG_BIG_FIVE = 0x04;
    public static final int ENTRY_FLAG_QUALIFIED = 0x08;

    public static abstract class Columns implements BaseColumns {
        public static final String PROP_ID = "id";
        public static final String PROP_CONTEST_ID = "contestId";
        public static final String PROP_ARTIST = "artist";
        public static final String PROP_TITLE = "title";
        public static final String PROP_ENGTITLE = "engTitle";
        public static final String PROP_COUNTRY = "country";
        public static final String PROP_YOUTUBELINK = "youTubeLink";
        public static final String PROP_OFFICIALVIDEO = "officialVideo";
        public static final String PROP_LYRICSLINK = "lyricsLink";
        public static final String PROP_SPOTIFYLINK = "spotifyLink";
        public static final String PROP_ENTRYFLAGS = "entryFlags";
        public static final String PROP_LANGUAGE = "language";
        public static final String PROP_SEMIFINAL_NBR = "semifinalNbr";
        public static final String PROP_FINAL_NBR = "finalNbr";
        public static final String PROP_RESULT = "result";
    }

    public ContestEntry() {
        addProperties(
                0, "", "", "", "",
                new Country(), "", false, "", "",
                0, "", 0, 0, 0);
    }

    public ContestEntry(
            int id,
            String contestId,
            String artist,
            String title,
            String engTitle,
            Country country,
            String youTubeLink,
            boolean officialVideo,
            String spotifyLink,
            String lyricsLink,
            int flags,
            String language,
            int semifinalNbr,
            int finalNbr,
            int result) {
        Logger.d("Creating entry nbr " + id);
        addProperties(
                id, contestId, artist, title, engTitle,
                country, youTubeLink, officialVideo,
                spotifyLink, lyricsLink,
                flags, language, semifinalNbr,
                finalNbr, result);
    }

    public ContestEntry(Parcel in) {
        this();
        readFromParcel(in);
    }

    public int getId() {
        return getPropertyInt(Columns.PROP_ID);
    }

    public void setId(int newId) {
        putProperty(Columns.PROP_ID, newId);
    }

    public String getContestId() {
        return getPropertyString(Columns.PROP_CONTEST_ID);
    }

    public Country getCountry() {
        return (Country)getProperty(Columns.PROP_COUNTRY);
    }

    public String getArtist() {
        return getPropertyString(Columns.PROP_ARTIST);
    }

    public String getTitle() {
        return getPropertyString(Columns.PROP_TITLE);
    }

    public String getEngTitle() {
        return getPropertyString(Columns.PROP_ENGTITLE);
    }

    public String getLanguage() {
        return getPropertyString(Columns.PROP_LANGUAGE);
    }

    public String getYouTubeLink() {
        return getPropertyString(Columns.PROP_YOUTUBELINK);
    }

    public boolean isOfficialVideo() {
        return getPropertyBoolean(Columns.PROP_OFFICIALVIDEO);
    }

    public String getSpotifyLink() {
        return getPropertyString(Columns.PROP_SPOTIFYLINK);
    }

    public String getLyricsLink() {
        return getPropertyString(Columns.PROP_LYRICSLINK);
    }

    public int getFlags() {
        return getPropertyInt(Columns.PROP_ENTRYFLAGS);
    }

    public int getResult() {
        return getPropertyInt(Columns.PROP_RESULT);
    }

    public int getSemifinalNbr() {
        return getPropertyInt(Columns.PROP_SEMIFINAL_NBR);
    }

    public int getFinalNbr() {
        return getPropertyInt(Columns.PROP_FINAL_NBR);
    }

    void addProperties(
            int id,
            String contestId,
            String artist,
            String title,
            String engTitle,
            Country country,
            String youTubeLink,
            boolean officialVideo,
            String spotifyLink,
            String lyricsLink,
            int flags,
            String language,
            int semifinalNbr,
            int finalNbr,
            int result) {
        Logger.v("Adding properties for entry " + id);
        putProperty(Columns.PROP_ID, id);
        putProperty(Columns.PROP_CONTEST_ID, contestId);
        putProperty(Columns.PROP_ARTIST, artist);
        putProperty(Columns.PROP_TITLE, title);
        putProperty(Columns.PROP_ENGTITLE, engTitle);
        putProperty(Columns.PROP_COUNTRY, country);
        putProperty(Columns.PROP_YOUTUBELINK, youTubeLink);
        putProperty(Columns.PROP_OFFICIALVIDEO, officialVideo);
        putProperty(Columns.PROP_SPOTIFYLINK, spotifyLink);
        putProperty(Columns.PROP_LYRICSLINK, lyricsLink);
        putProperty(Columns.PROP_ENTRYFLAGS, flags);
        putProperty(Columns.PROP_LANGUAGE, language);
        putProperty(Columns.PROP_SEMIFINAL_NBR, semifinalNbr);
        putProperty(Columns.PROP_FINAL_NBR, finalNbr);
        putProperty(Columns.PROP_RESULT, result);
    }
}
