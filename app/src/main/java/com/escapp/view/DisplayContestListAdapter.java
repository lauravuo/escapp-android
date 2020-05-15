package com.escapp.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.escapp.R;
import com.escapp.model.Contest;
import com.escapp.model.ContestEntry;
import com.escapp.model.ContestHeader;
import com.escapp.model.EscObjectList;
import com.escapp.model.Rate;

/**
 * ListAdapter for contest entries.
 *
 * @author  Laura Vuorenoja
 */
public class DisplayContestListAdapter extends ArrayAdapter<ContestEntry> {

    private int layoutResourceId;
    private Context context;
    private int viewId = 0;
    private int totalPageCount = 0;
    private Contest contest = null;
    private List<ContestEntry> items = null;

    public DisplayContestListAdapter(
            Context context,
            int resource,
            Contest contest,
            int viewId,
            int totalPageCount) {

        this(context,
                resource,
                filterEntries(contest.getEntries(), contest.getUserData().getRates(),
                        contest.getHeader(), viewId, totalPageCount));
        this.viewId = viewId;
        this.totalPageCount = totalPageCount;
        this.contest = contest;
    }

    private DisplayContestListAdapter(
            Context context, int resource, List<ContestEntry> items) {
        super(context, resource, items);
        this.layoutResourceId = resource;
        this.context = context;
        this.items = items;
    }

    public int getPosition(int itemId) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId() == itemId) {
                return i;
            }
        }
        return -1;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        ViewHolder viewHolder = null;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.flag = (ImageView)row.findViewById(R.id.countryFlag);
            viewHolder.country = (TextView)row.findViewById(R.id.entryCountry);
            viewHolder.artist = (TextView)row.findViewById(R.id.entryArtist);
            viewHolder.song = (TextView)row.findViewById(R.id.entrySong);
            viewHolder.rate = (TextView)row.findViewById(R.id.rate_value);
            row.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder)row.getTag();
        }

        ContestEntry item = getItem(position);
        if (item != null) {
            viewHolder.flag.setImageResource(App.getFlagResId(item.getCountry().getFlagPath()));

            int number = 0;
            if (contest.getHeader().getType() == ContestHeader.Type.OSC) {
                if (viewId == 0) {
                    number = item.getFinalNbr();
                } else {
                    number = item.getResult();
                }
            } else {
                if (viewId == DisplayContestActivity.VIEW_1ST_SEMI ||
                        viewId == DisplayContestActivity.VIEW_2ND_SEMI) {
                    number = item.getSemifinalNbr();
                } else if (viewId == DisplayContestActivity.VIEW_FINAL) {
                    number = item.getFinalNbr();
                } else if (viewId == DisplayContestActivity.VIEW_RESULTS) {
                    number = item.getResult();
                }
            }

            if (number != 0) {
                viewHolder.country.setText(number + ". " + App.getCountryName(item));
            } else {
                viewHolder.country.setText(App.getCountryName(item));
            }
            viewHolder.artist.setText(item.getArtist());
            viewHolder.song.setText(item.getTitle());

            Rate rate = contest.getUserData().getRate(item.getId());
            if (rate != null && rate.getRate() > 0) {
                viewHolder.rate.setVisibility(View.VISIBLE);
                viewHolder.rate.setText(Integer.toString(rate.getRate()));
            } else {
                viewHolder.rate.setVisibility(View.GONE);
            }
        }

        return row;
    }

    private static class ViewHolder {
        public ImageView flag;
        public TextView country;
        public TextView artist;
        public TextView song;
        public TextView rate;
    }

    private static boolean showEntryInView(ContestHeader contestHeader, ContestEntry entry, int viewId) {
        boolean show = true;
        int flags = entry.getFlags();
        if (contestHeader.getType() != ContestHeader.Type.OSC &&
                contestHeader.getState() != ContestHeader.State.CONTESTANTS)
        {
            switch (viewId) {
                case DisplayContestActivity.VIEW_1ST_SEMI:
                    show = (flags & ContestEntry.ENTRY_FLAG_1ST_SEMI) ==
                            ContestEntry.ENTRY_FLAG_1ST_SEMI;
                    break;
                case DisplayContestActivity.VIEW_2ND_SEMI:
                    show = (flags & ContestEntry.ENTRY_FLAG_2ND_SEMI) ==
                            ContestEntry.ENTRY_FLAG_2ND_SEMI;
                    break;
                case DisplayContestActivity.VIEW_FINAL:
                case DisplayContestActivity.VIEW_RESULTS:
                    show = ((flags & ContestEntry.ENTRY_FLAG_BIG_FIVE) ==
                            ContestEntry.ENTRY_FLAG_BIG_FIVE) ||
                            ((flags & ContestEntry.ENTRY_FLAG_QUALIFIED) ==
                            ContestEntry.ENTRY_FLAG_QUALIFIED);
                    break;
                default:
                    break;
            }
        }
        return show;
    }

    private static EscObjectList<ContestEntry> filterEntries(
            EscObjectList<ContestEntry> entries,
            EscObjectList<Rate> rates,
            ContestHeader contestHeader,
            int viewId,
            int pageCount) {
        if (viewId < (pageCount - 1)) {
            return filterEntries(entries, contestHeader, viewId);
        }
        return filterRatedEntries(entries, rates);
    }

    private static EscObjectList<ContestEntry> filterEntries(
            EscObjectList<ContestEntry> entries, ContestHeader contestHeader, int viewId) {
        EscObjectList<ContestEntry> filteredEntries =
                new EscObjectList<ContestEntry>(ContestEntry.class);
        for (ContestEntry entry : entries) {
            if (showEntryInView(contestHeader, entry, viewId)) {
                filteredEntries.add(entry);
            }
        }
        sortEntries(contestHeader, filteredEntries, viewId);

        return filteredEntries;
    }

    private static void sortEntries(
            ContestHeader contestHeader, List<ContestEntry> entries, int viewId) {
        if (contestHeader.getType() == ContestHeader.Type.OSC) {
            if (viewId == 0) {
                Collections.sort(entries, new FinalNbrComparator());
            } else {
                Collections.sort(entries, new ResultComparator());
            }
        } else {
            if (viewId == DisplayContestActivity.VIEW_1ST_SEMI ||
                    viewId == DisplayContestActivity.VIEW_2ND_SEMI) {
                Collections.sort(entries, new SemifinalNbrComparator());
            } else if (viewId == DisplayContestActivity.VIEW_FINAL) {
                Collections.sort(entries, new FinalNbrComparator());
            } else if (viewId == DisplayContestActivity.VIEW_RESULTS) {
                Collections.sort(entries, new ResultComparator());
            } else {
                Collections.sort(entries, new AlphabeticalComparator());
            }
        }
    }

    private static EscObjectList<ContestEntry> filterRatedEntries(
            EscObjectList<ContestEntry> entries, EscObjectList<Rate> rates) {
        EscObjectList<ContestEntry> filteredEntries =
                new EscObjectList<ContestEntry>(ContestEntry.class);

        Collections.sort(rates);
        for (Rate rate : rates) {
            for (ContestEntry entry : entries) {
                if (rate.getEntryId() == entry.getId()) {
                    if (rate.getRate() > 0) {
                        filteredEntries.add(entry);
                    }
                    break;
                }
            }
        }

        return filteredEntries;
    }

    private static class AlphabeticalComparator implements Comparator<ContestEntry> {
        @Override
        public int compare(ContestEntry entry1, ContestEntry entry2) {
            return App.getCountryName(entry1).compareTo(App.getCountryName(entry2));
        }
    }

    private static class SemifinalNbrComparator extends AlphabeticalComparator {
        @Override
        public int compare(ContestEntry entry1, ContestEntry entry2) {
            if (entry1.getSemifinalNbr() == 0  ||
                    entry2.getSemifinalNbr() == 0)
            {
                return super.compare(entry1, entry2);
            }
            return entry1.getSemifinalNbr() < entry2.getSemifinalNbr() ? -1 : 1;
        }
    }

    private static class FinalNbrComparator extends AlphabeticalComparator {
        @Override
        public int compare(ContestEntry entry1, ContestEntry entry2) {
            if (entry1.getFinalNbr() == 0  ||
                    entry2.getFinalNbr() == 0)
            {
                return super.compare(entry1, entry2);
            }
            return entry1.getFinalNbr() < entry2.getFinalNbr() ? -1 : 1;
        }
    }

    private static class ResultComparator extends AlphabeticalComparator  {
        @Override
        public int compare(ContestEntry entry1, ContestEntry entry2) {
            if (entry1.getResult() == 0  ||
                    entry2.getResult() == 0)
            {
                return super.compare(entry1, entry2);
            }
            return entry1.getResult() < entry2.getResult() ? -1 : 1;
        }
    }
}
