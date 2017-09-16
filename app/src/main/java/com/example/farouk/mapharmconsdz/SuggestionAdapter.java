package com.example.farouk.mapharmconsdz;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

public class SuggestionAdapter<String> extends ArrayAdapter<String> {

    private List<String> items;
    private List<String> filteredItems;
    private ArrayFilter mFilter;

    public SuggestionAdapter(Context context, @LayoutRes int resource, @NonNull List<String> objects) {
        super(context, resource, new ArrayList());
        this.items = objects;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public String getItem(int position) {
        return items.get(position);
    }

    @Override
    public Filter getFilter() {
        if (mFilter == null) {
            mFilter = new ArrayFilter();
        }
        return mFilter;
    }

    public int getCount() {
        //todo: change to pattern-size
        return items.size();
    }

    private class ArrayFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence prefix) {
            FilterResults results = new FilterResults();

            //custom-filtering of results
            results.values = items;
            results.count = items.size();

            return results;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            filteredItems = (List<String>) results.values;
            if (results.count > 0) {
                notifyDataSetChanged();
                items = filteredItems;
            } else {
                notifyDataSetInvalidated();
            }
        }
    }
}