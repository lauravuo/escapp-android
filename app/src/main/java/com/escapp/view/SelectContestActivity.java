package com.escapp.view;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.PagerTabStrip;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.escapp.R;
import com.escapp.controller.ContestTask;
import com.escapp.controller.Logger;
import com.escapp.model.ContestHeader;
import com.escapp.controller.ContestManager;
import com.escapp.model.EscObjectList;


/**
 * SelectContestActivity provides a view for user to select the active ESC year.
 *
 * @author  Laura Vuorenoja
 */
public class SelectContestActivity extends AppCompatActivity {
    private final static long COUNTDOWN_TIMESTAMP = 1589655600;

    private SelectContestListAdapter listAdapter = null;
    private TextView countdownText = null;
    private PagerAdapter pagerAdapter = null;
    private ViewPager pager = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_contest);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.long_app_name);

        countdownText = (TextView)findViewById(R.id.countdownText);
        updateCountdown();

        ContestHeader activeContest = null;
        if (savedInstanceState == null) {
            activeContest = ContestManager.getContestManager().getActiveContest();
            if (activeContest != null) {
                Logger.d("Jump to active contest: " + activeContest.getId());

                Intent intent = new Intent(this, DisplayContestActivity.class);
                intent.putExtra(ContestHeader.EXTRA_NAME, activeContest);
                startActivity(intent);
            } else {
                Logger.d("No active contest found.");
            }
        }

        int pageCount = ContestManager.CONTEST_TYPE_COUNT;

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), pageCount);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(pageCount - 1);
        PagerTabStrip tabStrip = (PagerTabStrip)findViewById(R.id.pagerTabStrip);
        tabStrip.setDrawFullUnderline(false);
        tabStrip.setTabIndicatorColor(App.getThemeColor(R.attr.colorAccent, this));
        tabStrip.setBackgroundColor(App.getThemeColor(R.attr.colorPrimary, this));
        if (activeContest != null) {
            pager.setCurrentItem(activeContest.getType().ordinal());
        }

        // No time-consuming task if already done once so do also in orientation change
        // to get the updated headers
        Logger.v("onCreate: init contests");
        ContestTask.initializeContests();

    }

    @Override
    protected void onResume() {
        Logger.v("onResume");
        super.onResume();
        updateCountdown();
        if (App.shouldShowRateAppDialog()) {
            App.showRateAppDialog(this);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.select_contest, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_about) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // Set the dialog title
            builder.setTitle(R.string.about_title);
            builder.setIcon(R.drawable.ic_launcher);
            String versionName = "";
            String message = "";
            try {
                versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
            } catch (Exception e) {
                Logger.e("Exception when getting app version. " + e.toString());
            }
            if (versionName != "") {
                message = getString(R.string.about_version) + " " + versionName + "\n\n";
            }
            try{
                ApplicationInfo ai = getPackageManager().getApplicationInfo(getPackageName(), 0);
                ZipFile zf = new ZipFile(ai.sourceDir);
                ZipEntry ze = zf.getEntry("classes.dex");
                long time = ze.getTime();
                message += getString(R.string.about_date) + " ";
                message += SimpleDateFormat.getInstance().format(new java.util.Date(time));
                message += "\n\n";
                zf.close();
            }catch(Exception e){
                Logger.e("Exception when getting app build date. " + e.toString());
            }
            message += getString(R.string.about_email);
            builder.setMessage(message);
            builder.setPositiveButton(R.string.about_ok, null);
            builder.create().show();
            return true;
        } else if (id == R.id.action_share_mode) {
            App.showShareModeDialog(this, false);
            return true;
        } else if (id == R.id.action_set_user_name) {
            App.showUsernameDialog(this, null);
            return true;
        } else if (id == R.id.action_rate_app) {
            App.showRateAppDialog(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateCountdown() {
        if (countdownText != null) {
            long currentTime = App.currentTimeInSeconds();
            if (currentTime < COUNTDOWN_TIMESTAMP) {
                int daysToFinal = getCountDownDays(currentTime);
                if (daysToFinal > 0) {
                    String text = getResources().getString(R.string.countdown_days);
                    countdownText.setText(String.format(text, daysToFinal));
                } else {
                    int hoursToFinal = getCountDownHours(currentTime);
                    if (hoursToFinal > 0) {
                        String text = getResources().getString(R.string.countdown_hours);
                        countdownText.setText(String.format(text, hoursToFinal));
                    } else {
                        int minutesToFinal = getCountDownMinutes(currentTime);
                        if (minutesToFinal > 0) {
                            String text = getResources().getString(R.string.countdown_minutes);
                            countdownText.setText(String.format(text, minutesToFinal));
                        }
                    }
                }
            } else {
                countdownText.setVisibility(View.GONE);
            }
        }
    }
    private static int getCountDownDays(long currentTimeSecs) {
        long timeDiff = COUNTDOWN_TIMESTAMP - currentTimeSecs;
        int timeDiffInDays = 0;

        if (timeDiff > 0) {
            timeDiffInDays = (int)(timeDiff / 60 / 60 / 24);
        }
        return timeDiffInDays;
    }

    private static int getCountDownHours(long currentTimeSecs) {
        long timeDiff = COUNTDOWN_TIMESTAMP - currentTimeSecs;
        int timeDiffInHours = 0;

        if (timeDiff > 0) {
            timeDiffInHours = (int)(timeDiff / 60 / 60);
        }
        return timeDiffInHours;
    }

    private static int getCountDownMinutes(long currentTimeSecs) {
        long timeDiff = COUNTDOWN_TIMESTAMP - currentTimeSecs;
        int timeDiffInMinutes = 0;

        if (timeDiff > 0) {
            timeDiffInMinutes = (int)(timeDiff / 60);
        }
        return timeDiffInMinutes;
    }

    public void onSoMeButtonClicked(View view) {
        App.onSoMeButtonClicked(view);
    }

    private class PagerAdapter extends FragmentPagerAdapter {
        private int count = 0;

        public PagerAdapter(FragmentManager fm, int count) {
            super(fm);
            this.count = count;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public String getPageTitle(int position) {
            int titleResId = 0;
            if (position == 0) {
                titleResId = R.string.select_contest_tab_0_header;
            } else if (position == 1) {
                titleResId = R.string.select_contest_tab_1_header;
            }
            return getApplicationContext().getString(titleResId).toUpperCase();
        }

        @Override
        public Fragment getItem(int position) {
            return SelectContestArrayListFragment.newInstance(position, getCount());
        }
    }
}
