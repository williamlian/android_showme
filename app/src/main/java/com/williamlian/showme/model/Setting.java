package com.williamlian.showme.model;

import android.content.Context;

import com.williamlian.showme.R;

public class Setting {
    public String imageSize;
    public String imageType;
    public String imageColor;
    public String siteFilter;

    private Setting(String size, String type, String color, String filter) {
        imageColor = color;
        imageSize = size;
        imageType = type;
        siteFilter = filter;
    }

    private static Setting instance = null;

    public static Setting getSetting(Context context) {
        if(instance == null) {
            instance = new Setting(
                    context.getResources().getStringArray(R.array.google_image_client_imgsz)[0],
                    context.getResources().getStringArray(R.array.google_image_client_imgtype)[0],
                    context.getResources().getStringArray(R.array.google_image_client_imgcolor)[0],
                    ""
            );
        }
        return instance;
    }

    public boolean isImageSizeFiltered() {
        return imageSize != null && !imageSize.equals("any");
    }

    public boolean isImageColorFiltered() {
        return imageColor != null && !imageColor.equals("any");
    }

    public boolean isImageTypeFiltered() {
        return imageType != null && !imageType.equals("any");
    }

    public boolean isSiteFiltered() {
        return siteFilter != null && !siteFilter.isEmpty();
    }

    public String toString() {
        return String.format("size=%s type=%s color=%s site=%s",imageSize,imageType,imageColor,siteFilter);
    }
}
