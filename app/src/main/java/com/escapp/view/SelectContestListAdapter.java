package com.escapp.view;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.escapp.R;
import com.escapp.model.ContestHeader;
import com.escapp.model.EscObjectList;

/**
 * ListAdapter for year list view.
 *
 * @author  Laura Vuorenoja
 */
public class SelectContestListAdapter extends ArrayAdapter<ContestHeader> {

    private int layoutResourceId;
    private Context context;

    public SelectContestListAdapter(Context context, int resource, EscObjectList<ContestHeader> items) {
        super(context, resource, items);
        this.layoutResourceId = resource;
        this.context = context;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
        }

        ContestHeader item = getItem(position);
        if (item != null) {
            ImageView imageView = (ImageView)row.findViewById(R.id.contestLogo);
            imageView.setImageResource(App.getLogoResId(item.getId()));
            TextView textView = (TextView)row.findViewById(R.id.contestName);
            textView.setText(App.getContestName(item));
            textView = (TextView)row.findViewById(R.id.contestMotto);
            textView.setText(item.getMotto());
        }

        return row;
    }
}
