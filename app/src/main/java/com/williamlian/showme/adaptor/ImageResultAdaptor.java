package com.williamlian.showme.adaptor;

import android.content.Context;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.williamlian.showme.R;
import com.williamlian.showme.client.model.GoogleImageSearchResult;

import java.util.List;

// some code borrowed from https://gist.github.com/nesquena/a988aac278cff59a9a69
public class ImageResultAdaptor extends ArrayAdapter<GoogleImageSearchResult> {
    protected List<GoogleImageSearchResult> dataList;
    public static final int VIEW_TYPE_LOADING = 0;
    public static final int VIEW_TYPE_ACTIVITY = 1;

    protected int maxSize = -1;

    static class ViewHolder {
        ImageView iv_thumbnail;
        TextView tv_resolution;
        TextView tv_url;
    }

    public ImageResultAdaptor(Context context, List<GoogleImageSearchResult> objects) {
        super(context, 0, objects);
        dataList = objects;
    }

    /**
     * disable click events on indicating rows
     */
    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }

    @Override
    public boolean isEnabled(int position) {
        return getItemViewType(position) == VIEW_TYPE_ACTIVITY;
    }

    /**
     * One type is normal data row, the other type is Progressbar
     */
    @Override
    public int getViewTypeCount() {
        return 2;
    }

    /**
     * the size of the List plus one, the one is the last row, which displays a Progressbar
     */
    @Override
    public int getCount() {
        return dataList.size() + 1;
    }

    /**
     * return the type of the row,
     * the last row indicates the user that the ListView is loading more data
     */
    @Override
    public int getItemViewType(int position) {
        return (position >= dataList.size()) ? VIEW_TYPE_LOADING  : VIEW_TYPE_ACTIVITY;
    }

    @Override
    public GoogleImageSearchResult getItem(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? dataList.get(position) : null;
    }

    @Override
    public long getItemId(int position) {
        return (getItemViewType(position) == VIEW_TYPE_ACTIVITY) ? position : -1;
    }

    /**
     *  returns the correct view
     */
    @Override
    public  View getView(int position, View convertView, ViewGroup parent){
        if (getItemViewType(position) == VIEW_TYPE_LOADING) {
            // display the last row
            return getFooterView(position, convertView, parent);
        }
        View dataRow = convertView;
        dataRow = getDataRow(position, convertView, parent);

        return dataRow;
    };


    /*
     * custom functions
     */
    public View getDataRow(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_image_result,parent,false);
        }
        GoogleImageSearchResult result = getItem(position);
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if(viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.iv_thumbnail = (ImageView)convertView.findViewById(R.id.iv_thumbnail);
            viewHolder.tv_resolution = (TextView)convertView.findViewById(R.id.tv_resolution);
            viewHolder.tv_url = (TextView)convertView.findViewById(R.id.tv_url);
            convertView.setTag(viewHolder);
        }

        Picasso.with(getContext()).load(result.thumbnailUrl).into(viewHolder.iv_thumbnail);
        viewHolder.tv_resolution.setText(String.format("%s x %s", result.width, result.height));
        viewHolder.tv_url.setText(result.visibleUrl);

        return convertView;
    }

    public View getFooterView(int position, View convertView,
                              ViewGroup parent) {
        if (position >= maxSize && maxSize > 0) {
            // the ListView has reached the last row
            TextView tvLastRow = new TextView(getContext());
            tvLastRow.setHint(R.string.reached_last_row);
            tvLastRow.setGravity(Gravity.CENTER);
            return tvLastRow;
        }

        View row = convertView;
        if (row == null) {
            row = LayoutInflater.from(getContext()).inflate(R.layout.content_progress_row, parent, false);
        }

        return row;
    }
}
