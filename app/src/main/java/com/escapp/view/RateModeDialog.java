package com.escapp.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;

import com.escapp.R;
import com.escapp.controller.ContestTask;
import com.escapp.model.Contest;
import com.escapp.model.UserContest;

/**
 * Created by laura on 2.1.15.
 */
public class RateModeDialog extends DialogFragment implements View.OnClickListener {

    private Contest contest = null;

    public static RateModeDialog newInstance(Contest contest) {
        RateModeDialog newInstance = new RateModeDialog();
        Bundle args = new Bundle();
        args.putParcelable(Contest.EXTRA_NAME, contest);
        newInstance.setArguments(args);
        return newInstance;
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        if (getArguments() != null) {
            this.contest = getArguments().getParcelable(Contest.EXTRA_NAME);
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View contentView = inflater.inflate(R.layout.dialog_rate_mode, null);
        builder.setView(contentView);

        RadioButton tradButton = (RadioButton)contentView.findViewById(R.id.radio_traditional);
        tradButton.setOnClickListener(this);
        RadioButton euroButton = (RadioButton)contentView.findViewById(R.id.radio_eurovision);
        euroButton.setOnClickListener(this);
        if (contest != null && contest.getRateMode() == UserContest.RateMode.TRADITIONAL) {
            tradButton.setChecked(true);
            euroButton.setChecked(false);
        } else {
            tradButton.setChecked(false);
            euroButton.setChecked(true);
        }
        builder.setTitle(R.string.action_set_rate_mode);

        Dialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        return dialog;
    }

    @Override
    public void onClick(View view) {
        UserContest.RateMode selectedMode = ((view.getId() == R.id.radio_eurovision) ?
                UserContest.RateMode.EUROVISION : UserContest.RateMode.TRADITIONAL);
        if (contest != null && contest.getRateMode() != selectedMode) {
            ContestTask.setRateMode(contest.getId(), selectedMode);
        }
        this.dismiss();
    }
}
