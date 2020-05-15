package com.escapp.view;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.escapp.controller.Logger;
import com.google.android.youtube.player.YouTubeInitializationResult;
import com.google.android.youtube.player.YouTubePlayer;
import com.google.android.youtube.player.YouTubePlayerFragment;

import java.util.ArrayList;

import com.escapp.R;
import com.escapp.controller.ContestTask;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;
import com.escapp.model.UserContest;

public class DisplayEntryActivity extends AppCompatActivity  implements
        YouTubePlayer.OnInitializedListener, SeekBar.OnSeekBarChangeListener,
        AdapterView.OnItemSelectedListener {

    private YouTubePlayerFragment youTubePlayerFragment = null;
    private YouTubePlayer youTubePlayer = null;
    private SeekBar rateBar = null;
    private TextView rateValue = null;
    private TextView commentValue = null;
    private Spinner rateSpinner = null;
    private ArrayList<String> availableRates = null;

    private Rate rate = null;
    private ContestEntry entry = null;

    private boolean isTablet() {
        int screenLayout = getResources().getConfiguration().screenLayout;
        screenLayout &= Configuration.SCREENLAYOUT_SIZE_MASK;

        if (screenLayout == Configuration.SCREENLAYOUT_SIZE_LARGE ||
                screenLayout == Configuration.SCREENLAYOUT_SIZE_XLARGE) {
            return true;
        }

        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Logger.v("DisplayEntryActivity: onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_entry);

        rate = getIntent().getParcelableExtra(Rate.EXTRA_NAME);
        entry = getIntent().getParcelableExtra(ContestEntry.EXTRA_NAME);

        youTubePlayerFragment =
                (YouTubePlayerFragment) getFragmentManager().findFragmentById(R.id.youtube_fragment);
        if (entry.isOfficialVideo()) {
            youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
            if (isTablet()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        } else {
            youTubePlayerFragment.getView().setVisibility(View.INVISIBLE);
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            ContestHeader.Type contestType = ContestHeader.Type.values()[getIntent().getIntExtra(("CONTEST_TYPE"), 0)];
            if (contestType == ContestHeader.Type.OSC) {
                View viewToHide = findViewById(R.id.youtube_text_row_1);
                if (viewToHide != null) {
                    viewToHide.setVisibility(View.GONE);
                }
                viewToHide = findViewById(R.id.youtube_text_row_2);
                if (viewToHide != null) {
                    viewToHide.setVisibility(View.GONE);
                }
            }
        }

        rateValue = (TextView)findViewById(R.id.rate_value);

        if (rateValue != null) {
            rateBar = (SeekBar)findViewById(R.id.rate_bar);
            rateSpinner = (Spinner)findViewById(R.id.rate_spinner);
            if (rate.getRateMode() == UserContest.RateMode.TRADITIONAL) {
                rateSpinner.setVisibility(View.GONE);
                rateValue.setText(Integer.toString(rate.getRate()));
                rateBar.setOnSeekBarChangeListener(this);
                rateBar.setProgress(rate.getRate());
            } else {
                rateValue.setVisibility(View.GONE);
                rateBar.setVisibility(View.GONE);

                UserContest userContest = getIntent().getParcelableExtra(UserContest.EXTRA_NAME);
                if (userContest != null) {
                    availableRates = userContest.getAvailableRates(rate);
                    availableRates.add(getResources().getString(R.string.no_points));
                    ArrayAdapter adapter = new ArrayAdapter(this,
                            android.R.layout.simple_spinner_dropdown_item, availableRates);
                    rateSpinner.setAdapter(adapter);
                    rateSpinner.setOnItemSelectedListener(this);
                    if (rate.getRate() > 0) {
                        rateSpinner.setSelection(availableRates.indexOf(Integer.toString(rate.getRate())));
                    } else {
                        rateSpinner.setSelection(availableRates.size() - 1);
                    }
                }
            }
            TextView textView = (TextView)findViewById(R.id.entryArtist);
            textView.setText(entry.getArtist());
            textView = (TextView)findViewById(R.id.entryTitle);
            textView.setText(entry.getTitle());
            textView = (TextView)findViewById(R.id.entryLanguage);
            textView.setText(App.getLanguage(entry));
            String engTitle = entry.getEngTitle();
            textView = (TextView)findViewById(R.id.entryEngTitle);
            if (engTitle.length() > 0) {
                textView.setText(entry.getEngTitle());

                textView = (TextView)findViewById(R.id.artist);
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.engTitle);
                textView.setLayoutParams(params);

                textView = (TextView)findViewById(R.id.title);
                params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.engTitle);
                textView.setLayoutParams(params);

                textView = (TextView)findViewById(R.id.language);
                params = (RelativeLayout.LayoutParams)textView.getLayoutParams();
                params.addRule(RelativeLayout.ALIGN_RIGHT, R.id.engTitle);
                textView.setLayoutParams(params);
            } else {
                textView.setVisibility(View.GONE);
                textView = (TextView)findViewById(R.id.engTitle);
                textView.setVisibility(View.GONE);
            }
            int flagId = App.getFlagResId(entry.getCountry().getFlagPath());
            ImageView imageView = (ImageView)findViewById(R.id.countryFlag);
            imageView.setImageResource(flagId);

            ImageButton spotifyButton = (ImageButton)findViewById(R.id.spotifyButton);
            if (!App.isSpotifyInstalled()) {
                spotifyButton.setVisibility(View.GONE);
            } else if (entry.getSpotifyLink() == null ||
                    entry.getSpotifyLink().length() == 0) {
                spotifyButton.setEnabled(false);
            }
            View lyricsButton = findViewById(R.id.lyricsButton);
            if (entry.getLyricsLink() == null || entry.getLyricsLink().length() == 0) {
                lyricsButton.setEnabled(false);
            }

            if (!entry.isOfficialVideo()) {
                View youtubeButton = findViewById(R.id.youtube_button);
                youtubeButton.setVisibility(View.VISIBLE);
            }
            commentValue = (TextView)findViewById(R.id.commentValue);
            updateCommentText();
        }
        int year = getIntent().getIntExtra(ContestHeader.Columns.PROP_YEAR, 0);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(App.getCountryName(entry) + " " + year);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private static final int RECOVERY_DIALOG_REQUEST = 1;

    @Override
    public void onInitializationFailure(YouTubePlayer.Provider provider,
                                        YouTubeInitializationResult errorReason) {
        if (errorReason.isUserRecoverableError()) {
            errorReason.getErrorDialog(this, RECOVERY_DIALOG_REQUEST).show();
        } else {
            String errorMessage = String.format(getString(R.string.utube_error), errorReason.toString());
            Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RECOVERY_DIALOG_REQUEST) {
            // Retry initialization if user performed a recovery action
            youTubePlayerFragment.initialize(DeveloperKey.DEVELOPER_KEY, this);
        }
    }

    @Override
    public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer player,
                                        boolean wasRestored) {
        if (youTubePlayer == null) {
            youTubePlayer = player;
            youTubePlayer.setShowFullscreenButton(isTablet());
            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                youTubePlayer.setFullscreen(true);
            }
        }
        if (!wasRestored && entry.getYouTubeLink() != null && entry.getYouTubeLink().length() > 0) {
            youTubePlayer.cueVideo(entry.getYouTubeLink());
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        rateValue.setText(Integer.toString(progress));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        setRate(seekBar.getProgress());
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        if (availableRates != null) {
            String selection = availableRates.get(position);
            int intRate = 0;
            if (position != (availableRates.size() - 1) && selection.length() > 0) {
                intRate = Integer.parseInt(selection);
            }
            setRate(intRate);
        }
    }

    private void setRate(int newRate) {
        if (rate.getRate() != newRate) {
            rate.setRate(newRate);
            ContestTask.setRate(rate);
        }
    }

    private void setComment(String newComment) {
        if (!rate.getComment().getComment().equals(newComment)) {
            rate.setComment(newComment);
            ContestTask.setRate(rate);
            updateCommentText();
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    public void onSpotifyButtonClicked(View view) {
        String uri = "spotify:track:" + entry.getSpotifyLink();
        Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(launcher);
    }

    public void onLyricsButtonClicked(View view) {
        String uri = entry.getLyricsLink();
        Intent launcher = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
        startActivity(launcher);
    }

    public void onYouTubeButtonClicked(View view) {
        try{
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("vnd.youtube:" + entry.getYouTubeLink()));
            startActivity(intent);
        }catch (ActivityNotFoundException ex){
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + entry.getYouTubeLink()));
            startActivity(intent);
        }
    }

    public void onEditCommentClicked(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View contentView = inflater.inflate(R.layout.entry_comment_edit, null);
        final EditText textEdit = (EditText)contentView.findViewById(R.id.comment_edit);
        if (rate != null && rate.getComment() != null && rate.getComment().getComment().length() > 0) {
            textEdit.setText(rate.getComment().getComment());
        }
        alert.setView(contentView);

        alert.setPositiveButton(getResources().getText(R.string.save), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                setComment(textEdit.getText().toString());
            }
        });

        alert.setNegativeButton(getResources().getText(R.string.cancel), null);

        alert.show().getWindow().setSoftInputMode (
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    }

    private void updateCommentText() {
        if (rate != null && rate.getComment() != null) {
            commentValue.setText(rate.getComment().getComment());
        }
    }
}
