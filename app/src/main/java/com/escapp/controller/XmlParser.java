package com.escapp.controller;

import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Country;
import com.escapp.model.EscObjectList;

/**
 * Created by laura on 17.11.2014.
 */
public class XmlParser {

    private static int entryId = 1;

    // We don't use namespaces
    private static final String ns = null;

    private static final String PROP_YEAR = "year";
    private static final String PROP_CITY = "city";
    private static final String PROP_MOTTO = "motto";
    private static final String PROP_VERSION = "version";
    private static final String PROP_STATE = "state";
    private static final String PROP_UPDATE_SRC = "updateSrc";

    private static final String TAG_CONTEST_ESC = "esc";
    private static final String TAG_CONTEST_OSC = "osc";
    private static final String TAG_ENTRY = "entry";
    private static final String TAG_COUNTRY = "country";
    private static final String TAG_ARTIST = "artist";
    private static final String TAG_TITLE = "title";
    private static final String TAG_ENG_TITLE = "englishTitle";
    private static final String TAG_LANGUAGE = "language";
    private static final String TAG_VIDEO = "video";
    private static final String TAG_OFFICIAL = "official";
    private static final String TAG_SPOTIFY = "spotify";
    private static final String TAG_LYRICS = "lyrics";
    private static final String TAG_SEMIFINAL = "semifinal";
    private static final String TAG_SEMIFINAL_NBR = "semifinalNbr";
    private static final String TAG_FINAL_NBR = "finalNbr";
    private static final String TAG_RESULT = "result";
    private static final String TAG_QUALIFIED = "qualified";

    private static final String TAG_COUNTRIES = "countries";
    private static final String TAG_NAME = "name";
    private static final String TAG_WIKIPATH = "wikiPath";
    private static final String TAG_FLAGPATH = "flagPath";


    public static void setInitialEntryId(int initialEntryId) {
        entryId = initialEntryId;
    }
    public static int getNextEntryId() {
        int nextEntryId = entryId;
        entryId++;
        return nextEntryId;
    }

    public static ContestHeader getContestHeader(
            InputStream inputStream, String id, ContestHeader.Type contestType)
            throws XmlPullParserException, IOException {
        ContestHeader contest = null;
        try {
            contest = getContestHeaderFromStream(inputStream, id, contestType);
        } finally {
            inputStream.close();
        }
        return contest;
    }

    private static ContestHeader getContestHeaderFromStream(
            InputStream inputStream, String id, ContestHeader.Type contestType) {
        ContestHeader contest = null;
        XmlPullParser parser = Xml.newPullParser();
        String contestTag = TAG_CONTEST_ESC;
        if (contestType == ContestHeader.Type.OSC) {
            contestTag = TAG_CONTEST_OSC;
        }
        try {
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(inputStream, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, contestTag);

            String city = parser.getAttributeValue(null, PROP_CITY);
            String year = parser.getAttributeValue(null, PROP_YEAR);
            String motto = parser.getAttributeValue(null, PROP_MOTTO);
            double version = Double.parseDouble(parser.getAttributeValue(null, PROP_VERSION));
            ContestHeader.State state = ContestHeader.getState(parser.getAttributeValue(null, PROP_STATE));
            String updateSrc = parser.getAttributeValue(null, PROP_UPDATE_SRC);
            if (updateSrc == null) {
                updateSrc = "";
            }
            contest = new ContestHeader(id, Integer.parseInt(year), city, motto, state, version, updateSrc);
        } catch (Exception e) {
            Logger.e("Exception when parsing contest header: " + id + ": " + e.toString());
        }
        return contest;
    }

    public static EscObjectList<ContestEntry> getEntries(
            InputStream in, String contestId, CountryManager countryManager,
            boolean generateId, ContestHeader.Type contestType)
            throws XmlPullParserException, IOException {
        EscObjectList<ContestEntry> entries = null;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            entries = readEntries(parser, contestId, countryManager, generateId, contestType);
        } finally {
            in.close();
        }
        return entries;
    }

    public static boolean updateContest(
            InputStream in, Contest contest, EscObjectList<ContestEntry> removedEntries,
            EscObjectList<ContestEntry> addedEntries, CountryManager countryManager, ContestHeader.Type contestType)
            throws XmlPullParserException, IOException {
        boolean versionChanged = false;
        try {
            in.mark(in.available());
            ContestHeader newHeader = getContestHeaderFromStream(in, contest.getId(), contestType);
            versionChanged = newHeader != null && newHeader.getVersion() > contest.getVersion();
            if (versionChanged) {
                EscObjectList<ContestEntry> oldEntries = new EscObjectList<>(ContestEntry.class);
                oldEntries.addAll(contest.getEntries());
                in.reset();
                EscObjectList<ContestEntry> entries =
                        getEntries(in, contest.getId(), countryManager, false, contestType);
                if (entries != null) {
                    // Set updated header
                    contest.setHeader(newHeader);
                    // Set updated entries
                    contest.clearEntries();
                    for (ContestEntry entry : entries) {
                        contest.addEntry(entry);
                    }
                    // Copy old id from previous entries
                    EscObjectList<ContestEntry> newEntries = new EscObjectList<ContestEntry>(ContestEntry.class);
                    newEntries.addAll(contest.getEntries());
                    for (ContestEntry oldEntry : oldEntries) {
                        boolean newEntryFound = false;
                        for (ContestEntry newEntry : newEntries) {
                            if (oldEntry.getCountry().getName().equals(newEntry.getCountry().getName())) {
                                Logger.d("Updating entry nbr " + oldEntry.getId());
                                newEntry.setId(oldEntry.getId());
                                newEntries.remove(newEntry);
                                newEntryFound = true;
                                break;
                            }
                        }
                        if (!newEntryFound) {
                            removedEntries.add(oldEntry);
                        }
                    }
                    if (newEntries.size() > 0) {
                        for (ContestEntry newEntry : newEntries) {
                            newEntry.setId(getNextEntryId());
                            addedEntries.add(newEntry);
                        }
                    }
                } else {
                    versionChanged = false;
                }
            }
        } finally {
            in.close();
        }
        return versionChanged;
    }

    public static double getCountriesVersion(InputStream in) throws XmlPullParserException, IOException {
        double version = 0;
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            parser.require(XmlPullParser.START_TAG, ns, TAG_COUNTRIES);

            version = Double.parseDouble(parser.getAttributeValue(null, PROP_VERSION));
        } finally {
            in.close();
        }
        return version;
    }

    public static ArrayList<Country> parseCountries(InputStream in) throws XmlPullParserException, IOException {
        try {
            XmlPullParser parser = Xml.newPullParser();
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
            parser.setInput(in, null);
            parser.nextTag();
            return readCountries(parser);
        } finally {
            in.close();
        }
    }

    private static EscObjectList<ContestEntry> readEntries(
            XmlPullParser parser, String contestId, CountryManager countryManager,
            boolean generateId, ContestHeader.Type contestType)
            throws XmlPullParserException, IOException {

        String contestTag = TAG_CONTEST_ESC;
        if (contestType == ContestHeader.Type.OSC) {
            contestTag = TAG_CONTEST_OSC;
        }
        EscObjectList<ContestEntry> entries = null;
        parser.require(XmlPullParser.START_TAG, ns, contestTag);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            // Starts by looking for the entry tag
            if (name.equals(TAG_ENTRY)) {
                ContestEntry entry = readEntry(parser, contestId, countryManager, generateId);
                if (null != entry) {
                    if (entries == null) {
                        entries = new EscObjectList<>(ContestEntry.class);
                    }
                    entries.add(entry);
                } else {
                    Logger.e("Unable to parse entry, stopping parsing.");
                    entries = null;
                }
            } else {
                skip(parser);
            }
        }
        return entries;
    }

    private static ArrayList<Country> readCountries(XmlPullParser parser)
            throws XmlPullParserException, IOException {

        ArrayList<Country> countries = new ArrayList<Country>();
        parser.require(XmlPullParser.START_TAG, ns, TAG_COUNTRIES);

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() == XmlPullParser.START_TAG) {
                String name = parser.getName();
                // Starts by looking for the entry tag
                if (name.equals(TAG_COUNTRY)) {
                    countries.add(readCountry(parser));
                } else {
                    skip(parser);
                }
            }
        }
        return countries;
    }

    private static ContestEntry readEntry(
            XmlPullParser parser, String contestId, CountryManager countryManager, boolean generateId)
            throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_ENTRY);
        Country country = null;
        String artist = "";
        String title = "";
        String englishTitle = "";
        String language = "";
        String video = "";
        boolean officialVideo = true;
        String spotify = "";
        String lyrics = "";
        String semiFinal = null;
        String qualified = null;
        int flags = 0;
        int semifinalNbr = 0;
        int finalNbr = 0;
        int result = 0;
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_COUNTRY)) {
                country = countryManager.getCountry(readText(parser, TAG_COUNTRY));
            } else if (name.equals(TAG_ARTIST)) {
                artist = readText(parser, TAG_ARTIST);
            } else if (name.equals(TAG_TITLE)) {
                title = readText(parser, TAG_TITLE);
            } else if (name.equals(TAG_ENG_TITLE)) {
                englishTitle = readText(parser, TAG_ENG_TITLE);
            } else if (name.equals(TAG_LANGUAGE)) {
                language = readText(parser, TAG_LANGUAGE);
            } else if (name.equals(TAG_VIDEO)) {
                video = readText(parser, TAG_VIDEO);
            } else if (name.equals(TAG_OFFICIAL)) {
                officialVideo = readText(parser, TAG_OFFICIAL).equals("true");
            } else if (name.equals(TAG_SPOTIFY)) {
                spotify = readText(parser, TAG_SPOTIFY);
            } else if (name.equals(TAG_LYRICS)) {
                lyrics = readText(parser, TAG_LYRICS);
            } else if (name.equals(TAG_SEMIFINAL)) {
                semiFinal = readText(parser, TAG_SEMIFINAL);
            } else if (name.equals(TAG_SEMIFINAL_NBR)) {
                semifinalNbr = Integer.parseInt(readText(parser, TAG_SEMIFINAL_NBR));
            } else if (name.equals(TAG_FINAL_NBR)) {
                finalNbr = Integer.parseInt(readText(parser, TAG_FINAL_NBR));
            } else if (name.equals(TAG_RESULT)) {
                result = Integer.parseInt(readText(parser, TAG_RESULT));
            } else if (name.equals(TAG_QUALIFIED)) {
                qualified = readText(parser, TAG_QUALIFIED);
            } else {
                skip(parser);
            }
        }
        if (semiFinal != null) {
            int semiFinalNbr = Integer.parseInt(semiFinal);
            if (semiFinalNbr == 1) {
                flags |= ContestEntry.ENTRY_FLAG_1ST_SEMI;
            } else if (semiFinalNbr == 2) {
                flags |= ContestEntry.ENTRY_FLAG_2ND_SEMI;
            } else if (semiFinalNbr == 3) {
                flags |= ContestEntry.ENTRY_FLAG_BIG_FIVE;
            }
        }
        if (qualified != null) {
            if (qualified.equals("true")) {
                flags |= ContestEntry.ENTRY_FLAG_QUALIFIED;
            }
        }

        if (country != null) {
            int id = 0;
            if (generateId) {
                id = getNextEntryId();
            }

            return new ContestEntry(id, contestId, artist,
                    title, englishTitle, country,
                    video, officialVideo, spotify, lyrics, flags, language,
                    semifinalNbr, finalNbr, result);
        }
        return null;
    }

    private static Country readCountry(XmlPullParser parser) throws XmlPullParserException, IOException {
        parser.require(XmlPullParser.START_TAG, ns, TAG_COUNTRY);
        String countryName = "";
        String wikiPath = "";
        String flagPath = "";
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.getEventType() != XmlPullParser.START_TAG) {
                continue;
            }
            String name = parser.getName();
            if (name.equals(TAG_NAME)) {
                countryName = readText(parser, TAG_NAME);
            } else if (name.equals(TAG_WIKIPATH)) {
                wikiPath = readText(parser, TAG_WIKIPATH);
            } else if (name.equals(TAG_FLAGPATH)) {
                flagPath = readText(parser, TAG_FLAGPATH);
            } else {
                skip(parser);
            }
        }
        return new Country(countryName, flagPath, wikiPath);
    }

    private static String readText(XmlPullParser parser, String tag) throws IOException, XmlPullParserException {
        String result = "";
        parser.require(XmlPullParser.START_TAG, ns, tag);
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.getText();
            parser.nextTag();
        }
        parser.require(XmlPullParser.END_TAG, ns, tag);
        return result;
    }

    private static void skip(XmlPullParser parser) throws XmlPullParserException, IOException {
        if (parser.getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (parser.next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }
}