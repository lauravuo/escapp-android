package com.escapp.view;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.PagerTabStrip;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import com.escapp.R;
import com.escapp.controller.ContestManager;
import com.escapp.controller.ContestTask;
import com.escapp.controller.Logger;
import com.escapp.model.UserComment;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;
import com.escapp.model.UserContest;


public class DisplayContestActivity extends AppCompatActivity {

    public static final int VIEW_1ST_SEMI = 0;
    public static final int VIEW_2ND_SEMI = 1;
    public static final int VIEW_FINAL = 2;
    public static final int VIEW_RESULTS = 3;

    private Contest contest = null;
    private ContestHeader contestHeader = null;
    private PagerAdapter pagerAdapter = null;
    private ViewPager pager = null;
    private Receiver intentReceiver = null;

    public static Contest getContest(DisplayContestActivity activity) {
        return activity.contest;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_contest);

        Contest savedContest = null;
        if (null == savedInstanceState) {
            contestHeader = getIntent().getParcelableExtra(ContestHeader.EXTRA_NAME);
        } else {
            savedContest = savedInstanceState.getParcelable(Contest.EXTRA_NAME);
            if (savedContest != null) {
                contestHeader = savedContest.getHeader();
            }
        }

        if (contestHeader != null && contestHeader.getType() == ContestHeader.Type.OSC) {
            setTitle(getString(R.string.select_contest_tab_1_header_short) + " " + contestHeader.getYear());
        } else {
            setTitle(App.getContestName(contestHeader));
        }

        int pageCount = getPageCount(contestHeader);
        int totalPageCount = pageCount + 1;  // Add "My list" page

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), totalPageCount);
        pager = (ViewPager)findViewById(R.id.pager);
        pager.setAdapter(pagerAdapter);
        pager.setOffscreenPageLimit(totalPageCount - 1);
        // TODO: change when app support lib is updated to use the accent colors etc.
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            pager.setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
        PagerTabStrip tabStrip = (PagerTabStrip)findViewById(R.id.pagerTabStrip);
        tabStrip.setDrawFullUnderline(false);
        tabStrip.setTabIndicatorColor(App.getThemeColor(R.attr.colorAccent, this));
        tabStrip.setBackgroundColor(App.getThemeColor(R.attr.colorPrimary, this));

        final ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        intentReceiver = new Receiver();
        LocalBroadcastManager bcManager = LocalBroadcastManager.getInstance(this);
        bcManager.registerReceiver(intentReceiver, new IntentFilter(Contest.EXTRA_NAME));
        bcManager.registerReceiver(intentReceiver, new IntentFilter(ContestHeader.EXTRA_NAME));
        bcManager.registerReceiver(intentReceiver, new IntentFilter(Rate.EXTRA_NAME));

        if (savedContest != null) {
            setContest(savedContest);
        }
        ContestTask.setContest(contestHeader.getId());
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putParcelable(Contest.EXTRA_NAME, contest);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.display_contest, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_set_rate_mode) {
            showRateModeDialog();
            return true;
        } else if (id == R.id.action_reset_rates) {
            showResetRatingsQuery();
        } else if (id == R.id.action_delete_comments) {
            showDeleteCommentsQuery();
        } else if (id == R.id.menu_item_share) {
            RatingsSharer.showShareDialog(this, contest, getSemifinalNbr());
        } else if (id == R.id.menu_item_share_options) {
            // Share all ratings when activated from options menu
            RatingsSharer.showShareDialog(this, contest, 0);
        }

         /*else if (id == R.id.menu_item_share) {
            // TODO: move sharemode storing to other class such as App
            int currentShareMode = ContestManager.getContestManager().getStoredShareMode();
            if (currentShareMode == 0) {
                App.showShareModeDialog(this, true);
            }
        }*/
        return super.onOptionsItemSelected(item);
    }

    private int getSemifinalNbr() {
        int semifinalNbr = 0;
        if (contestHeader.getType() != ContestHeader.Type.OSC && pager != null) {
            int currentItem = pager.getCurrentItem();
            int itemCount = pager.getChildCount();
            if (itemCount > 2 && currentItem >= 0 && currentItem <= 2) {
                semifinalNbr = currentItem + 1;
            }
        }
        return semifinalNbr;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case RatingsSharer.PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    RatingsSharer.saveImage(this, contest, getSemifinalNbr(), App.getUsername());
                }
            }
        }
    }
    private void restartActivity(final ContestHeader contestHeader) {
        finish();
        Intent refresh = new Intent(this, DisplayContestActivity.class);
        refresh.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        refresh.putExtra(ContestHeader.EXTRA_NAME, contestHeader);
        startActivity(refresh);
    }

    private void showUpdateContestQuery(final ContestHeader contestHeader) {
        if (!isFinishing() && this.contestHeader.getId().equals(contestHeader.getId())) {
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which){
                        case DialogInterface.BUTTON_POSITIVE:
                            restartActivity(contestHeader);
                            break;

                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                        default:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            // TODO: sometimes we show dialog so early(?) that activity is not running
            try {
                builder.setMessage(R.string.updated_data_found)
                        .setPositiveButton(R.string.dialog_yes, dialogClickListener)
                        .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
            } catch (Exception e) {
                Logger.e("Unable to show update dialog: " + e.toString());
            }
        }
    }

    private void showResetRatingsQuery() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (contest != null) {
                            ContestTask.resetRateMode(contest.getId(), contest.getUserData().getRateMode());
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.reset_ratings_title)
                .setMessage(R.string.reset_ratings_message)
                .setPositiveButton(R.string.dialog_yes, dialogClickListener)
                .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
    }

    private void showDeleteCommentsQuery() {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case DialogInterface.BUTTON_POSITIVE:
                        if (contest != null) {
                            ContestTask.deleteComments(contest.getId());
                        }
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        break;
                    default:
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.delete_comments_title)
                .setMessage(R.string.delete_comments_message)
                .setPositiveButton(R.string.dialog_yes, dialogClickListener)
                .setNegativeButton(R.string.dialog_no, dialogClickListener).show();
    }

    private int getPageCount(ContestHeader contest) {
        int pageCount = 0;
        if (contest.getType() == ContestHeader.Type.OSC) {
            if (contest.getState() == ContestHeader.State.RESULTS_OUT) {
                pageCount = 2;
            } else {
                pageCount = 1;
            }
        } else {
            if (contest.getState() == ContestHeader.State.RESULTS_OUT) {
                pageCount = 4;
            } else if (contest.getState() == ContestHeader.State.CONTESTANTS) {
                pageCount = 1;
            } else {
                pageCount = 3;
            }
        }
        return pageCount;
    }

    private boolean setContest(Contest newContest) {
        if (newContest != null && contestHeader != null &&
                contestHeader.getId().equals(newContest.getId())) {
            contest = newContest;
            if (!contest.getUserData().isRateModeSet() && !isFinishing()) {
                showRateModeDialog();
            }
            return true;
        }
        return false;
    }

    private void showRateModeDialog() {
        final RateModeDialog dialog = RateModeDialog.newInstance(contest);
        dialog.show(getFragmentManager(), "rateModeDialog");
        contest.getUserData().setRateModeSet(true);
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
            String title = "";
            if (contestHeader != null) {
                Resources resources = App.getContext().getResources();
                if (position == getCount() - 1) {
                    title = resources.getString(R.string.my_list);
                } else if (contestHeader.getState() == ContestHeader.State.CONTESTANTS) {
                    title = resources.getString(R.string.contestants);
                } else {
                    if (contestHeader.getType() == ContestHeader.Type.OSC) {
                        if (position == 0) {
                            title = resources.getString(R.string.final_contest);
                        } else {
                            title = resources.getString(R.string.results);
                        }
                    } else {
                        if (position == 0) {
                            title = resources.getString(R.string.first_semi);
                        } else if (position == 1) {
                            title = resources.getString(R.string.second_semi);
                        } else if (position == 2) {
                            title = resources.getString(R.string.final_contest);
                        } else {
                            title = resources.getString(R.string.results);
                        }
                    }
                }
            }
            return title.toUpperCase();
        }

        @Override
        public Fragment getItem(int position) {
            return DisplayContestArrayListFragment.newInstance(position, getCount());
        }
    }

    private class Receiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            boolean updateLists = false;
            if (intent.hasExtra(ContestHeader.EXTRA_NAME)) {
                boolean contestUpdated = intent.getBooleanExtra("updatedDataFound", false);
                if (contestUpdated) {
                    final ContestHeader contestHeader = intent.getParcelableExtra(ContestHeader.EXTRA_NAME);
                    showUpdateContestQuery(contestHeader);
                }
            } else if (intent.hasExtra(Contest.EXTRA_NAME)) {
                Contest newContest = intent.getParcelableExtra(Contest.EXTRA_NAME);
                if (newContest != null) {
                    updateLists = setContest(newContest);
                }
            }

            // Update list content
            if (intent.getAction().equals(Contest.EXTRA_NAME) || intent.getAction().equals(Rate.EXTRA_NAME)) {
                Intent listIntent = new Intent(DisplayContestArrayListFragment.UPDATE_CONTEST_LIST_INTENT);
                listIntent.putExtra("contestUpdated", updateLists);
                LocalBroadcastManager.getInstance(context).sendBroadcast(listIntent);
            }
        }
    }
}
