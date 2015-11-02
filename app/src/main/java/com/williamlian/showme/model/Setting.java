package com.williamlian.showme.model;

import android.content.Context;
import android.util.Log;

import com.williamlian.showme.R;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class Setting implements Serializable {
    public String imageSize;
    public String imageType;
    public String imageColor;
    public String siteFilter;
    public String lastSearchString;

    private transient Context context;

    private Setting(String size, String type, String color, String filter) {
        imageColor = color;
        imageSize = size;
        imageType = type;
        siteFilter = filter;
    }

    private static final String SETTING_FILE = "settings";
    private static Setting instance = null;

    public static Setting getSetting(Context context) {
        if(instance == null) {
            instance = read(context);
            if(instance == null) {
                instance = new Setting(
                        context.getResources().getStringArray(R.array.google_image_client_imgsz)[0],
                        context.getResources().getStringArray(R.array.google_image_client_imgtype)[0],
                        context.getResources().getStringArray(R.array.google_image_client_imgcolor)[0],
                        ""
                );
            }
            instance.context = context;
        }
        return instance;
    }

    public void save() {
        try {
            FileOutputStream fos = context.openFileOutput(SETTING_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream out = new ObjectOutputStream(fos);
            out.writeObject(this);
            out.close();
            fos.close();
            Log.i(this.getClass().getName(), "Settings saved to file: " + toString());
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "cannot save settings to file");
        }
    }

    private static Setting read(Context context) {
        try {
            FileInputStream fis = context.openFileInput(SETTING_FILE);
            ObjectInputStream in = new ObjectInputStream(fis);
            Setting setting = (Setting)in.readObject();
            in.close();
            fis.close();
            Log.i(Setting.class.getName(),"Settings read from file: " + setting.toString());
            return setting;
        } catch(IOException|ClassNotFoundException e) {
            Log.w(Setting.class.getName(), "cannot read saved settings");
            return null;
        }
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
        return String.format("size=%s type=%s color=%s site=%s search=%s",imageSize,imageType,imageColor,siteFilter,lastSearchString);
    }
}
