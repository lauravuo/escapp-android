package com.escapp.view;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.escapp.R;
import com.escapp.controller.ContestManager;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by laura on 25.3.15.
 */
public class RatingsSharer {
    public static final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1;
    private static class ButtonClickListener implements View.OnClickListener {
        protected Activity activity = null;
        protected Contest contest = null;
        protected Dialog dialog = null;
        protected int semifinalNbr = 0;
        protected String username = "";

        ButtonClickListener(Activity activity, Contest contest, int semifinalNbr, String username) {
            this.activity = activity;
            this.contest = contest;
            this.semifinalNbr = semifinalNbr;
            this.username = username;
        }

        public void setDialog(Dialog dialog) {
            this.dialog = dialog;
        }

        @Override
        public void onClick(View v) {
            if (dialog != null) {
                dialog.dismiss();
            }
        }

    }

    private static class TextButtonClickListener extends ButtonClickListener {

        TextButtonClickListener(Activity activity, Contest contest, int semifinalNbr, String username) {
            super(activity, contest, semifinalNbr, username);
        }

        @Override
        public void onClick(View v) {
            shareText(activity, contest, semifinalNbr);
            super.onClick(v);
        }

    }
    private static class ImageButtonClickListener extends ButtonClickListener {

        ImageButtonClickListener(Activity activity, Contest contest, int semifinalNbr, String username) {
            super(activity, contest, semifinalNbr, username);
        }

        @Override
        public void onClick(View v) {
            shareImage(activity, contest, semifinalNbr, username);
            super.onClick(v);
        }

        private static void shareImage(Activity activity, Contest contest, int semifinalNbr, String username) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
            } else {
                saveImage(activity, contest, semifinalNbr, username);
            }
        }
    }

    private static void showShareDialog(
            final Activity activity,
            final Contest contest,
            int semifinalNbr,
            String username) {
        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        alert.setTitle(R.string.share_dialog_title);
        LayoutInflater inflater = activity.getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_share_ratings, null);

        ImageButton textButton = (ImageButton)contentView.findViewById(R.id.share_text_button);
        TextButtonClickListener textButtonListener =
                new TextButtonClickListener(activity, contest, semifinalNbr, username);

        ImageButton imageButton = (ImageButton)contentView.findViewById(R.id.share_image_button);
        ImageButtonClickListener imageButtonListener =
                new ImageButtonClickListener(activity, contest, semifinalNbr, username);

        alert.setView(contentView);

        alert.setNegativeButton(activity.getResources().getText(R.string.cancel), null);

        Dialog dialog = alert.create();
        textButtonListener.setDialog(dialog);
        textButton.setOnClickListener(textButtonListener);
        imageButtonListener.setDialog(dialog);
        imageButton.setOnClickListener(imageButtonListener);

        dialog.show();
    }

    public static void showShareDialog(final Activity activity, final Contest contest, final int semifinalNbr) {
        String username = App.getUsername();
        if (username == null || username.isEmpty()) {
            App.showUsernameDialog(activity, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    showShareDialog(activity, contest, semifinalNbr, App.getUsername());
                }
            });
        } else {
            showShareDialog(activity, contest, semifinalNbr, username);
        }
    }

    private static void shareText(Activity activity, Contest contest, int semifinalNbr) {
        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, getRatingsText(contest, semifinalNbr));
        sendIntent.setType("text/plain");
        activity.startActivity(Intent.createChooser(
                sendIntent, activity.getResources().getText(R.string.share_dialog_title)));
    }

    public static void saveImage(Activity activity, Contest contest, int semifinalNbr, String username) {
        String imagePath = RatingsImageCreator.saveImage(activity, contest, semifinalNbr, username);

        if (imagePath.length() <= 0) {
            Toast.makeText(activity, R.string.error_something_wrong, Toast.LENGTH_SHORT);
        }
    }

    private static String getRatingsText(Contest contest, int semifinalNbr) {
        String ratingsText = "";
        if (contest.getState() == ContestHeader.State.CONTESTANTS) {
            semifinalNbr = 0;
        }
        if (contest != null) {
            ratingsText = App.getShareIntroText(contest, semifinalNbr, false, App.getUsername()) + "\n\n";
            HashMap<Rate, ContestEntry> rates =
                    contest.getUserData().getRatesForSemifinal(contest.getEntries(), semifinalNbr);
            ArrayList<Rate> sortedRates = new ArrayList<>(rates.keySet());
            Collections.sort(sortedRates);
            int shareMode = ContestManager.getContestManager().getStoredShareMode();
            for (Rate rate : sortedRates) {
                ContestEntry entry = rates.get(rate);
                if (entry != null) {
                    boolean addComment = shareMode < 2 &&
                            (rate.getComment() != null && rate.getComment().getComment().length() > 0);
                    boolean addRate = addComment || rate.getRate() > 0;
                    if (addRate) {
                        ratingsText += App.getCountryName(entry) + ": " + App.getPointsString(rate) + "\n"
                                + entry.getArtist() + " - " + entry.getTitle() + "\n";
                    }
                    if (addComment) {
                        ratingsText += rate.getComment().getComment() + "\n";
                    }
                    if (addRate) {
                        ratingsText += "\n";
                    }
                }

            }
            ratingsText += App.getShareOutroText(contest) + "\n";
        }
        return ratingsText;
    }
}
