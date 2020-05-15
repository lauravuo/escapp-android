package com.escapp.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.escapp.R;
import com.escapp.controller.Logger;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.Rate;
import com.escapp.model.UserComment;
import com.escapp.model.UserContest;

/**
 * Created by laura on 3.4.15.
 */

public class DisplayContestArrayListFragment extends ListFragment {
    public static final String UPDATE_CONTEST_LIST_INTENT = "UPDATE_CONTEST_LIST_INTENT";

    private int num = 1;
    private int pageCount = 0;
    private ListFragmentReceiver listFragmentReceiver = null;

    public static DisplayContestArrayListFragment newInstance(int num, int pageCount) {
        DisplayContestArrayListFragment newInstance = new DisplayContestArrayListFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        args.putInt("count", pageCount);
        newInstance.setArguments(args);
        newInstance.setHasOptionsMenu(true);
        return newInstance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            num = getArguments().getInt("num");
            pageCount = getArguments().getInt("count");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.display_contest_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (listFragmentReceiver == null) {
            listFragmentReceiver = new ListFragmentReceiver(
                    this, LocalBroadcastManager.getInstance(getActivity()));
        }
        updateListAdapter();
    }

    public void updateListAdapter() {
        if (getActivity() != null) {
            Contest contest = DisplayContestActivity.getContest((DisplayContestActivity)getActivity());
            if (contest != null) {
                Logger.v("updateListAdapter: " + num);
                setListAdapter(new DisplayContestListAdapter(
                        getActivity(),
                        R.layout.display_contest_list_item,
                        contest,
                        num,
                        pageCount));
            }
        }
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Contest contest = DisplayContestActivity.getContest((DisplayContestActivity)getActivity());

        ContestEntry item = (ContestEntry) getListAdapter().getItem(position);

        Intent intent = new Intent(getActivity(), DisplayEntryActivity.class);
        Rate rate = contest.getUserData().getRate(item.getId());
        if (rate == null) {
            rate = new Rate(
                    contest.getRateMode(), 0, item.getId(), new UserComment(item.getId(), ""));
        }
        Logger.v("Starting entry activity with rate: " + rate.getRate());
        intent.putExtra(Rate.EXTRA_NAME, rate);
        intent.putExtra(ContestEntry.EXTRA_NAME, item);
        intent.putExtra(UserContest.EXTRA_NAME, contest.getUserData());
        intent.putExtra(ContestHeader.Columns.PROP_YEAR, contest.getYear());
        intent.putExtra("CONTEST_TYPE", contest.getHeader().getType().ordinal());
        startActivity(intent);
    }

    private class ListFragmentReceiver extends BroadcastReceiver {
        private DisplayContestArrayListFragment listFragment = null;

        public ListFragmentReceiver(
                DisplayContestArrayListFragment listFragment, LocalBroadcastManager bcManager) {
            this.listFragment = listFragment;
            bcManager.registerReceiver(this, new IntentFilter(UPDATE_CONTEST_LIST_INTENT));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            this.listFragment.updateListAdapter();
        }
    }
}
