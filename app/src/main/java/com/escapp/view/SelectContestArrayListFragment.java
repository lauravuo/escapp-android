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
import com.escapp.controller.ContestManager;
import com.escapp.controller.ContestTask;
import com.escapp.controller.Logger;
import com.escapp.model.ContestHeader;
import com.escapp.model.EscObjectList;

import java.util.ArrayList;

/**
 * Created by laura on 30.5.15.
 */
public class SelectContestArrayListFragment extends ListFragment {
    private int num = 1;
    private int pageCount = 0;
    private ListFragmentReceiver listFragmentReceiver = null;

    public static SelectContestArrayListFragment newInstance(int num, int pageCount) {
        SelectContestArrayListFragment newInstance = new SelectContestArrayListFragment();
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
        View v = inflater.inflate(R.layout.select_contest_list, container, false);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Logger.v("onActivityCreated! " + this.num);

        super.onActivityCreated(savedInstanceState);
        if (listFragmentReceiver == null) {
            listFragmentReceiver = new ListFragmentReceiver(
                    this, LocalBroadcastManager.getInstance(getActivity()));
            ContestTask.updateContestsForType(this.num);
        }
    }

    public void updateListAdapter(ArrayList<ContestHeader> contests) {
        if (getActivity() != null) {
            EscObjectList<ContestHeader> headers = new EscObjectList<ContestHeader>(ContestHeader.class);
            if (contests != null) {
                Logger.v("updateListAdapter " + this.num + " header count " + contests.size());
                for (ContestHeader contestHeader : contests) {
                    headers.add(contestHeader);
                }
            }
            SelectContestListAdapter listAdapter =
                    new SelectContestListAdapter(getActivity(), R.layout.select_contest_list_item, headers);
            setListAdapter(listAdapter);
        }
    }

    @Override
    public void onListItemClick(ListView listView, View v, int position, long id) {
        ContestHeader item = ((SelectContestListAdapter)listView.getAdapter()).getItem(position);
        Intent intent = new Intent(getActivity(), DisplayContestActivity.class);
        intent.putExtra(ContestHeader.EXTRA_NAME, item);
        startActivity(intent);
    }

    private class ListFragmentReceiver extends BroadcastReceiver {
        private SelectContestArrayListFragment listFragment = null;

        public ListFragmentReceiver(
                SelectContestArrayListFragment listFragment, LocalBroadcastManager bcManager) {
            this.listFragment = listFragment;
            bcManager.registerReceiver(this, new IntentFilter(ContestHeader.EXTRA_NAME_LIST));
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            int thisIndex = listFragment.num;
            Logger.v("Received list update intent for tab index " + thisIndex);
            final String extraName = ContestManager.CONTEST_TYPES[thisIndex];
            if (intent.hasExtra(extraName)) {
                ArrayList<ContestHeader> contests = intent.getParcelableArrayListExtra(extraName);
                this.listFragment.updateListAdapter(contests);
            }
        }
    }
}
