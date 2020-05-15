package com.escapp.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.escapp.R;
import com.escapp.controller.ContestManager;
import com.escapp.controller.Logger;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;

import java.io.File;
import java.util.List;

/**
 * Created by laura on 19.11.2014.
 */
public class App extends Application {

    public static final String PREFS_FILE_NAME = "EscGuidePrefs";
    public static final String PREFS_ACTIVE_CONTEST_ID_KEY = "ActiveContestId";
    public static final String PREFS_ACTIVE_CONTEST_YEAR_KEY = "ActiveContestYear";
    public static final String PREFS_ACTIVE_CONTEST_CITY_KEY = "ActiveContestCity";
    public static final String PREFS_ACTIVE_CONTEST_MOTTO_KEY = "ActiveContestMotto";
    public static final String PREFS_ACTIVE_CONTEST_STATE_KEY = "ActiveContestState";
    public static final String PREFS_ACTIVE_CONTEST_VERSION_KEY = "ActiveContestVersion";
    public static final String PREFS_ACTIVE_CONTEST_UPDATE_SRC_KEY = "ActiveContestUpdatedSrc";
    public static final String PREFS_CURRENT_APP_VERSION_KEY = "CurrentAppVersion";
    public static final String PREFS_CURRENT_SHARE_MODE = "CurrentShareMode";
    public static final String PREFS_RATE_APP_IS_RATED = "RateAppIsRated";
    public static final String PREFS_RATE_APP_INSTALL_TIME = "RateAppInstallTime";
    public static final String PREFS_RATE_APP_LAUNCH_COUNT = "RateAppLaunchCount";
    public static final String PREFS_COUNTRIES_VERSION = "CountriesVersion";
    public static final String PREFS_USERNAME = "Username";

    private final static int LAUNCH_COUNT_BEFORE_APP_RATE = 5;
    private final static long INSTALL_TIME_BEFORE_APP_RATE = 60 * 60 * 24 * 7; // 7 days

    private static Context context;
    private static Boolean spotifyInstalled = null;

    @Override
    public void onCreate() {
        super.onCreate();
        context = this;
        increaseAppLaunchCount();
    }

    public static Context getContext(){
        return context;
    }

    public static String getPointsString(Rate rate) {
        Resources resources = context.getResources();
        return rate.getRate() + " " + (rate.getRate() == 1 ?
                resources.getString(R.string.point) : resources.getString(R.string.points));
    }

    public static int getIdResId(String id) {
        return context.getResources().getIdentifier(
                id, "id", context.getApplicationInfo().packageName);
    }

    public static int getFlagResId(String path) {
        return context.getResources().getIdentifier(
                path, "drawable", context.getApplicationInfo().packageName);
    }

    public static int getLogoResId(String contestId) {
        return context.getResources().getIdentifier(
                contestId.replace("contest", "logo"), "drawable", context.getApplicationInfo().packageName);
    }

    public static int getThemeColor(int resId, Context context) {
        TypedValue typedValue = new TypedValue();
        context.getTheme().resolveAttribute(resId, typedValue, true);
        return typedValue.data;
    }

    public static String getCountryName(ContestEntry entry) {
        String countryId = entry.getCountry().getName().toLowerCase().trim().replace(' ', '_');
        int resId =
                context.getResources().getIdentifier("country_" + countryId, "string", context.getApplicationInfo().packageName);
        if (resId != 0) {
            return context.getString(resId);
        }
        return entry.getCountry().getName();
    }

    public static String getLanguage(ContestEntry entry) {
        String languageId = entry.getLanguage();
        String resultStr = "";
        if (languageId.contains(",")) {
            String languages[] = languageId.split(",");
            for (int i = 0; i < languages.length; i++) {
                String language = languages[i].toLowerCase().trim().replace(' ', '_');
                int resId =
                        context.getResources().getIdentifier("language_" + language, "string", context.getApplicationInfo().packageName);
                if (resId != 0) {
                    resultStr += context.getString(resId);
                } else {
                    Logger.e("Language not found for id " + languageId);
                    resultStr += entry.getLanguage();
                }
                if (i < languages.length - 1) {
                    resultStr += ", ";
                }
            }
        } else {
            languageId = languageId.toLowerCase().trim().replace(' ', '_');
            int resId =
                    context.getResources().getIdentifier("language_" + languageId, "string", context.getApplicationInfo().packageName);
            if (resId != 0) {
                resultStr = context.getString(resId);
            } else {
                Logger.e("Language not found for id " + languageId);
                resultStr = entry.getLanguage();
            }
        }
        return resultStr;
    }

    public static String getContestName(ContestHeader contestHeader) {
        String cityName = contestHeader.getCity();
        String cityId = cityName.toLowerCase().trim().replace(' ', '_');
        int resId =
                context.getResources().getIdentifier("city_" + cityId, "string", context.getApplicationInfo().packageName);
        if (resId != 0) {
            cityName = context.getResources().getString(resId);
        }
        return cityName + " " + contestHeader.getYear();
    }

    public static boolean isSpotifyInstalled() {
        if (spotifyInstalled == null) {
            String uri = "spotify:track:" + 0;
            Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));

            List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(
                    launcher, PackageManager.MATCH_DEFAULT_ONLY);
            spotifyInstalled = new Boolean(list.size() > 0);
        }
        return spotifyInstalled.booleanValue();
    }

    public static void showShareModeDialog(Activity activity, final boolean showCheckBox) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.share_mode_title);
        LayoutInflater inflater = activity.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_share_mode, null);
        int currentShareMode = ContestManager.getContestManager().getStoredShareMode();
        final RadioButton includeComments =
                (RadioButton)contentView.findViewById(R.id.radio_include_comments);
        final RadioButton includeOnlyRates =
                (RadioButton)contentView.findViewById(R.id.radio_include_only_rates);
        final CheckBox checkBox = (CheckBox)contentView.findViewById(R.id.remember_share_mode);
        if (showCheckBox) {
            checkBox.setVisibility(View.VISIBLE);
            checkBox.setChecked(true);
        }
        if (currentShareMode < 2) {
            includeComments.setChecked(true);
        } else {
            includeOnlyRates.setChecked(true);
        }
        alert.setView(contentView);

        alert.setPositiveButton(context.getResources().getText(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                if (!showCheckBox || checkBox.isChecked()) {
                    if (includeComments.isChecked()) {
                        // TODO: use enum
                        ContestManager.getContestManager().storeShareMode(1);
                    } else {
                        ContestManager.getContestManager().storeShareMode(2);
                    }
                }
            }
        });

        alert.setNegativeButton(context.getResources().getText(R.string.cancel), null);

        alert.show();
    }

    public static String getUsername() {
        return getSharedPreferences().getString(PREFS_USERNAME, "");
    }

    public static void showUsernameDialog(Activity activity, final DialogInterface.OnClickListener listener) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.set_user_name_title);
        LayoutInflater inflater = activity.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.set_username_dialog_edit, null);
        final EditText textEdit = (EditText)contentView.findViewById(R.id.username_edit);
        textEdit.setText(getUsername());
        alert.setView(contentView);

        alert.setPositiveButton(activity.getResources().getText(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                SharedPreferences sharedPreferences = getSharedPreferences();
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREFS_USERNAME, textEdit.getText().toString());
                editor.commit();
                if (listener != null) {
                    listener.onClick(dialog,  whichButton);
                }
            }
        });

        alert.setNegativeButton(activity.getResources().getText(R.string.cancel), listener);

        alert.show().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    public static void showRateAppDialog(final Activity activity) {
        setAppRated(true);

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.rate_app_title);
        LayoutInflater inflater = activity.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_rate_app, null);

        TextView descView = (TextView)contentView.findViewById(R.id.rate_app_desc);
        descView.setClickable(true);
        descView.setOnClickListener(new View.OnClickListener() {
            private int clickCount = 0;
            private boolean loggingEnabled = false;

            @Override
            public void onClick(View v) {
                clickCount++;
                if (!loggingEnabled && clickCount >= 5) {
                    loggingEnabled = true;
                    Logger.setLogLevel(5, true);
                    Toast.makeText(v.getContext(), "Logging enabled", Toast.LENGTH_SHORT).show();
                }
            }
        });

        alert.setView(contentView);

        alert.setPositiveButton(context.getResources().getText(R.string.rate_app_play_store), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                final String appPackageName = activity.getPackageName();
                try {
                    activity.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                } catch (android.content.ActivityNotFoundException anfe) {
                    activity.startActivity(
                            new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
                }
            }
        });

        alert.setNegativeButton(context.getResources().getText(R.string.no_thanks), null);

        alert.show();
    }

    public static void onSoMeButtonClicked(View view) {
        String path = "";
        if (view.getId() == R.id.facebook_button) {
            path = "http://www.facebook.com/esc.application";
        } else if (view.getId() == R.id.twitter_button) {
            path = "http://twitter.com/ESC_app";
        } else if (view.getId() == R.id.instagram_button) {
            path = "http://instagram.com/esc_app/";
        }
        if (path.length() > 0) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(path));
            view.getContext().startActivity(intent);
        }
    }

    public static File getContestUpdateFile(String id) {
        return new File(context.getCacheDir(), id + ".xml");
    }

    public static SharedPreferences getSharedPreferences() {
        return context.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE);
    }

    public static long currentTimeInSeconds() {
        return System.currentTimeMillis() / 1000;
    }

    public static boolean shouldShowRateAppDialog() {
        boolean showDialog = false;
        SharedPreferences sharedPreferences = getSharedPreferences();
        boolean isRated = sharedPreferences.getBoolean(PREFS_RATE_APP_IS_RATED, false);
        if (!isRated) {
            int launchCount = sharedPreferences.getInt(PREFS_RATE_APP_LAUNCH_COUNT, 0);
            if (launchCount > LAUNCH_COUNT_BEFORE_APP_RATE) {
                long installTime = sharedPreferences.getLong(PREFS_RATE_APP_INSTALL_TIME, 0);
                if ((currentTimeInSeconds() - installTime) > INSTALL_TIME_BEFORE_APP_RATE) {
                    showDialog = true;
                }
            }
        }
        return showDialog;
    }

    public static double getCountriesVersion() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        double countriesVersion = sharedPreferences.getFloat(PREFS_COUNTRIES_VERSION, 1.0f);
        return countriesVersion;
    }

    public static String getShareIntroText(Contest contest, int sfNbr, boolean image, String username) {
        if (contest.getState() == ContestHeader.State.CONTESTANTS) {
            sfNbr = 0;
        }
        if (image) {
            int introTextId = R.string.share_image_all;
            if (contest.getHeader().getType() == ContestHeader.Type.OSC) {
                String text = String.format(
                        context.getString(introTextId),
                        context.getString(R.string.select_contest_tab_1_header) + " " + contest.getYear());
                return username == null || username.isEmpty() ? text : username + ": " + text;
            } else if (sfNbr == 1) {
                introTextId = R.string.share_image_1st_sf;
            } else if (sfNbr == 2) {
                introTextId = R.string.share_image_2nd_sf;
            } else if (sfNbr == 3) {
                introTextId = R.string.share_image_final;
            }
            String text = String.format(context.getString(introTextId), App.getContestName(contest.getHeader()));
            return username == null || username.isEmpty() ? text : username + ": " + text;
        } else {
            int introTextId = R.string.share_intro_all;
            if (contest.getHeader().getType() == ContestHeader.Type.OSC) {
                String text = String.format(
                        context.getString(introTextId),
                        context.getString(R.string.select_contest_tab_1_header) + " " + contest.getYear());
                return username == null || username.isEmpty() ? text : username + ": " + text;
            } else if (sfNbr == 1) {
                introTextId = R.string.share_intro_1st;
            } else if (sfNbr == 2) {
                introTextId = R.string.share_intro_2nd;
            } else if (sfNbr == 3) {
                introTextId = R.string.share_intro_final;
            }
            String text = String.format(context.getString(introTextId), App.getContestName(contest.getHeader()));
            return username == null || username.isEmpty() ? text : username + ": " + text;
        }
    }

    public static String getShareOutroText(Contest contest) {
        if (contest.getMotto().isEmpty() ||
                contest.getHeader().getType() == ContestHeader.Type.OSC) {
            return context.getString(R.string.share_outro_short);
        } else {
            return contest.getMotto() + " " + context.getString(R.string.share_outro);
        }
    }

    public static void setCountriesVersion(float countriesVersion) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat(PREFS_COUNTRIES_VERSION, countriesVersion);
        editor.commit();
    }

    private static void increaseAppLaunchCount() {
        SharedPreferences sharedPreferences = getSharedPreferences();
        boolean isRated = sharedPreferences.getBoolean(PREFS_RATE_APP_IS_RATED, false);
        if (!isRated) {
            int launchCount = sharedPreferences.getInt(PREFS_RATE_APP_LAUNCH_COUNT, 0);
            launchCount++;
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putInt(PREFS_RATE_APP_LAUNCH_COUNT, launchCount);

            long installTime = sharedPreferences.getLong(PREFS_RATE_APP_INSTALL_TIME, 0);
            if (installTime == 0) {
                editor.putLong(PREFS_RATE_APP_INSTALL_TIME, currentTimeInSeconds());
            }

            editor.commit();
        }
    }

    private static void setAppRated(boolean isRated) {
        SharedPreferences sharedPreferences = getSharedPreferences();
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(PREFS_RATE_APP_IS_RATED, isRated);
        editor.commit();
    }

}