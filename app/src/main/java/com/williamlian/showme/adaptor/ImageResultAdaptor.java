package com.williamlian.showme.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.williamlian.showme.R;
import com.williamlian.showme.client.model.GoogleImageSearchResult;

import java.util.List;

public class ImageResultAdaptor extends ArrayAdapter<GoogleImageSearchResult> {

    static class ViewHolder {
        ImageView iv_thumbnail;
    }

    public ImageResultAdaptor(Context context, List<GoogleImageSearchResult> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_image_result,parent,false);
        }
        GoogleImageSearchResult result = getItem(position);
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if(viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.iv_thumbnail = (ImageView)convertView.findViewById(R.id.iv_thumbnail);
            convertView.setTag(viewHolder);
        }

        Picasso.with(getContext()).load(result.thumbnailUrl).into(viewHolder.iv_thumbnail);
        return convertView;
    }
}
