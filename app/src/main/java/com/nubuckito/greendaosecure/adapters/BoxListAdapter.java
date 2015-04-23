package com.nubuckito.greendaosecure.adapters;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.nubuckito.greendaosecure.R;

import java.util.List;

import greendao.Box;

/**
 * Adapter for listview using Box
 * Created by Buzinga on 23/04/2015.
 */
public class BoxListAdapter extends ArrayAdapter<Box> {

    private boolean useList = true;

    public BoxListAdapter(Context context, List<Box> items) {
        super(context, android.R.layout.simple_list_item_1, items);
    }

    /**
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Box item = getItem(position);
        View viewToUse = null;

        // This block exists to inflate the settings list item conditionally based on whether
        // we want to support a grid or list view.
        LayoutInflater mInflater = (LayoutInflater) super.getContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            if (useList) {
                viewToUse = mInflater.inflate(R.layout.fragment_box_list_item, null);
            } else {
                viewToUse = mInflater.inflate(R.layout.fragment_box_grid_item, null);
            }

            holder = new ViewHolder();
            holder.titleText = (TextView) viewToUse.findViewById(R.id.titleTextView);
            holder.descriptionText = (TextView) viewToUse.findViewById(R.id.descriptionTextView);
            viewToUse.setTag(holder);
        } else {
            viewToUse = convertView;
            holder = (ViewHolder) viewToUse.getTag();
        }

        holder.titleText.setText(item.getName());
        holder.descriptionText.setText(item.getDescription());
        return viewToUse;
    }

    /**
     * Holder for the list items.
     */
    private class ViewHolder {
        TextView titleText;
        TextView descriptionText;
    }
}
