package com.williamlian.showme.client;

import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.williamlian.showme.client.model.GoogleImageSearchResult;
import com.williamlian.showme.model.Setting;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/*
 * https://developers.google.com/image-search/v1/jsondevguide#json_reference *
 */
public class GoogleImageSearchClient {

    private static final String BASE_URL = "https://ajax.googleapis.com/ajax/services/search/images";
    public static final int    MAX_RESULT_SIZE = 8;

    // Required Parameter
    private static final String VERSION_KEY = "v";
    private static final String VERSION = "1.0";

    private static final String KEYWORD_KEY = "q";

    //Optional Parameter
    private static final String RESULT_SIZE_KEY = "rsz";
    private static final String START_KEY       = "start";
    private static final String IMAGE_SIZE_KEY  = "imgsz";
    private static final String SITE_FILTER_KEY = "as_sitesearch";
    private static final String IMAGE_COLOR_KEY = "imgcolor";
    private static final String IMAGE_TYPE_KEY  = "imgtype";

    public void simpleSearch(
            String keyword,
            int resultSize,
            int start,
            Setting setting,
            GoogleImageSearchResultCallback callback
    ) {
        RequestParams params = new RequestParams();
        params.put(KEYWORD_KEY, keyword);
        params.put(RESULT_SIZE_KEY, resultSize);
        params.put(START_KEY, start);
        if(setting.isImageColorFiltered()) {
            params.put(IMAGE_COLOR_KEY, setting.imageColor);
        }
        if(setting.isImageSizeFiltered()) {
            params.put(IMAGE_SIZE_KEY, setting.imageSize);
        }
        if(setting.isImageTypeFiltered()) {
            params.put(IMAGE_TYPE_KEY, setting.imageType);
        }
        if(setting.isSiteFiltered()) {
            params.put(SITE_FILTER_KEY, setting.siteFilter);
        }
        send("", params, callback);
    }

    public void multiPageSearch(
            String keyword,
            int resultSize,
            int start,
            Setting setting,
            GoogleImageSearchResultCallback callback
    ) {
        ArrayList<GoogleImageSearchResult> combined = new ArrayList<>();
        int pagesNeeded = (int)Math.ceil((float)resultSize / (float)MAX_RESULT_SIZE);
        int current = start;
        for(int i = 0; i < pagesNeeded; i++) {
            int size = MAX_RESULT_SIZE;
            if(current + size > start + resultSize) {
                size = start + resultSize - current;
            }
            simpleSearch(keyword,size,current,setting,callback);
            current += size;
        }
    }

    protected void send(String path, RequestParams params, final GoogleImageSearchResultCallback callback) {
        AsyncHttpClient client = new AsyncHttpClient();
        params.put(VERSION_KEY, VERSION);
        Log.i(this.getClass().getName(),"request params: " + params.toString());
        client.get(BASE_URL + path, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int status = response.getInt("responseStatus");
                    String message = response.getString("responseDetails");
                    if(status != 200) {
                        callback.onFailure(status,message);
                    } else {
                        callback.onSuccess(parseResult(response.getJSONObject("responseData")));
                    }
                } catch (JSONException e) {
                    Log.e(this.getClass().getName(),"exception parsing result", e);
                    callback.onFailure(statusCode, "Error calling Google Serivce");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                callback.onFailure(statusCode, responseString);
            }
        });
    }

    public interface GoogleImageSearchResultCallback {
        void onSuccess(ArrayList<GoogleImageSearchResult> results);
        void onFailure(int statusCode, String message);
    }

    protected ArrayList<GoogleImageSearchResult> parseResult(JSONObject response) throws JSONException{
        JSONArray results = response.getJSONArray("results");
        ArrayList<GoogleImageSearchResult> images = new ArrayList<>();
        ObjectMapper mapper = new ObjectMapper();
        for(int i = 0; i < results.length(); i++) {
            try {
                GoogleImageSearchResult image = mapper.readValue(results.getJSONObject(i).toString(), GoogleImageSearchResult.class);
                images.add(image);
            } catch (IOException e) {
                Log.i(this.getClass().getName(), "Failed to parse result", e);
            }
        }
        Log.i(this.getClass().getName(),String.format("Parsed %d images",images.size()));
        return images;
    }
}
