package com.escapp.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.escapp.BuildConfig;
import com.escapp.R;
import com.escapp.controller.ContestManager;
import com.escapp.controller.Logger;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

/**
 * Created by laura on 23.3.15.
 */
public class RatingsImageCreator {
    public static String saveImage(final Activity activity, Contest contest, int semifinalNbr, String username) {
        if (isExternalStorageWritable()) {
            return saveFile(activity, contest, semifinalNbr, username);
        }
        return "";
    }

    private static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    private static String saveFile(final Activity activity, Contest contest, int semifinalNbr, String username) {
        String filePath = "";
        LayoutInflater inflater = activity.getLayoutInflater();
        View view = inflater.inflate(R.layout.image_ratings, null);
        storeData(view, contest, semifinalNbr, username);
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, bytes);

        File storageDir = getStorageDir();
        if (storageDir != null) {
            String path = storageDir.getPath() + File.separator + getFileName();
            File file = new File(path);
            FileOutputStream fileOutputStream = null;
            try {
                file.createNewFile();
                fileOutputStream = new FileOutputStream(file);
                fileOutputStream.write(bytes.toByteArray());
                filePath = path;
            } catch (Exception e) {
                Logger.e("Exception when creating ratings image: " + e.toString());
            } finally {
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (Exception e) {
                        Logger.e("Exception when closing file stream: " + e.toString());
                    }
                }
            }
            if (filePath.length() > 0) {
                scanFile(activity, file);
            }
        }
        return filePath;
    }

    private static File getStorageDir() {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), "ESCApp");
        if (!file.mkdirs()) {
            Logger.w("Directory not created or it already exists.");
        }
        return file;
    }

    private static String getFileName() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String formattedDate = dateFormat.format(calendar.getTime());
        return "ratings_" + formattedDate + ".png";
    }

    private static void scanFile(final Context context, final File file) {
        MediaScannerConnection.scanFile(context,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                        Logger.i("Scan completed for " + path);
                        Uri fileUri = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider",file);
                        Intent shareIntent = new Intent(Intent.ACTION_SEND);
                        shareIntent.setType("image/*");
                        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
                        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(Intent.createChooser(
                                shareIntent, context.getResources().getText(R.string.share_dialog_title)));
                    }
                });
    }

    private static void storeData(View view, Contest contest, int semifinalNbr, String username) {
        Context context = view.getContext();
        if (contest.getState() == ContestHeader.State.CONTESTANTS) {
            semifinalNbr = 0;
        }
        if (context != null) {
            TextView title = (TextView) view.findViewById(R.id.image_ratings_title);
            TextView footer = (TextView) view.findViewById(R.id.image_ratings_footer);
            String text = App.getShareOutroText(contest);
            footer.setText(text);

            HashMap<Rate, ContestEntry> rates =
                    contest.getUserData().getRatesForSemifinal(contest.getEntries(), semifinalNbr);
            ArrayList<Rate> sortedRates = new ArrayList<>(rates.keySet());
            Collections.sort(sortedRates);

            title.setText(App.getShareIntroText(contest, semifinalNbr, true, username));

            int count = 0;
            for (Rate rate : sortedRates) {
                if (rate.getRate() > 0) {
                    int id = App.getIdResId("image_rating_" + Integer.toString(count + 1));
                    if (id > 0) {
                        View entryView = view.findViewById(id);
                        if (entryView != null) {
                            ContestEntry entry = rates.get(rate);
                            setEntryText(count + 1, entryView, rate, entry);
                        }
                    }
                    count++;
                }
            }
            if (count < 10) {
                for (int i = count; i < 10; i++) {
                    int id = App.getIdResId("image_rating_" + Integer.toString(i + 1));
                    if (id > 0) {
                        View entryView = view.findViewById(id);
                        if (entryView != null) {
                            entryView.setVisibility(View.INVISIBLE);
                        }
                    }
                }
            }
        }
    }

    private static void setEntryText(int position, View entryView, Rate rate, ContestEntry contestEntry) {
        ImageView flagView = (ImageView)entryView.findViewById(R.id.countryFlag);
        flagView.setImageResource(App.getFlagResId(contestEntry.getCountry().getFlagPath()));
        TextView textView = (TextView)entryView.findViewById(R.id.entryCountry);
        textView.setText(position + ". " + App.getCountryName(contestEntry));
        textView = (TextView)entryView.findViewById(R.id.entrySong);
        textView.setText(contestEntry.getArtist() + " - " + contestEntry.getTitle());
        textView = (TextView)entryView.findViewById(R.id.entryRate);
        textView.setText(Integer.toString(rate.getRate()));
    }

}
