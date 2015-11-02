package com.williamlian.showme;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.etsy.android.grid.StaggeredGridView;
import com.helper.NetworkHelper;
import com.williamlian.showme.adaptor.ImageResultAdaptor;
import com.williamlian.showme.client.GoogleImageSearchClient;
import com.williamlian.showme.client.model.GoogleImageSearchResult;
import com.williamlian.showme.model.Setting;
import com.williamlian.showme.widget.EndlessScrollListener;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private GoogleImageSearchClient googleImageSearchClient = new GoogleImageSearchClient();
    private ArrayList<GoogleImageSearchResult> images;
    private ImageResultAdaptor imageResultAdaptor;
    private StaggeredGridView sgv_images;
    private Setting setting;

    private static final int MAX_PAGE = 8;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Log.i(this.getClass().getName(),"Creating Main Activity");

        if(!NetworkHelper.isNetworkAvailable(this)) {
            new MaterialDialog.Builder(this)
                    .title("No Internet Connection")
                    .content("No Internet Connection, Please check your network configurations.")
                    .positiveText("OK")
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog materialDialog, @NonNull DialogAction dialogAction) {
                            moveTaskToBack(true);
                        }
                    })
                    .show();
        }

        sgv_images = (StaggeredGridView)findViewById(R.id.sgv_results);
        setting = Setting.getSetting(this);
        images = new ArrayList<>();
        imageResultAdaptor = new ImageResultAdaptor(this, images);
        imageResultAdaptor.setMaxSize((MAX_PAGE-1) * GoogleImageSearchClient.MAX_RESULT_SIZE);

        sgv_images.setAdapter(imageResultAdaptor);
        sgv_images.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                GoogleImageSearchResult result = (GoogleImageSearchResult)parent.getItemAtPosition(position);
                Intent viewImageIntent = new Intent(MainActivity.this, ViewImageActivity.class);
                viewImageIntent.putExtra("result", result);
                startActivity(viewImageIntent);
            }
        });

        sgv_images.setOnScrollListener(new EndlessScrollListener() {
            @Override
            public boolean onLoadMore(int page, int totalItemsCount) {
                if (page > MAX_PAGE) {
                    return false;
                } else {
                    if(setting.lastSearchString != null) {
                        singleSearch(setting.lastSearchString, totalItemsCount - 1, setting, true);
                        return true;
                    } else {
                        return  false;
                    }
                }
            }
        });

        sgv_images.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onStart() {
        super.onStart();
        setting = Setting.getSetting(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        setting.save();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

        if(setting.lastSearchString != null) {
            searchView.setQuery(setting.lastSearchString, false);
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                searchItem.collapseActionView();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SearchView) v).setQuery(setting.lastSearchString, false);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void search(String query) {
        if(query == null || query.isEmpty()) {
            showError("Search Text is Empty");
        } else {
            singleSearch(query, 0, setting, false);
            setting.lastSearchString = query;
            setting.save();
            sgv_images.setVisibility(View.VISIBLE);
        }
    }

    public void search() {
        search(setting.lastSearchString);
    }

    public void singleSearch(String query, int start, Setting setting, boolean append) {
        Log.i(this.getClass().getName(),String.format("Searching google for: %s, result start=%d, setting=%s append=%s", query, start, setting,append));
        googleImageSearchClient.simpleSearch(query, GoogleImageSearchClient.MAX_RESULT_SIZE, start, setting, new SearchResultCallback(imageResultAdaptor,append,this));
    }

    static class SearchResultCallback implements GoogleImageSearchClient.GoogleImageSearchResultCallback {
        ImageResultAdaptor imageResultAdaptor;
        boolean append;
        Context context;
        public SearchResultCallback(ImageResultAdaptor adaptor, boolean append, Context context) {
            this.imageResultAdaptor = adaptor;
            this.append = append;
            this.context = context;
        }
        @Override
        public void onSuccess(ArrayList<GoogleImageSearchResult> results) {
            if(!append) {
                Log.i(this.getClass().getName(),"Cleaning result adaptor");
                imageResultAdaptor.clear();
                imageResultAdaptor.notifyDataSetChanged();
            }
            imageResultAdaptor.addAll(results);
        }

        @Override
        public void onFailure(int statusCode, String message) {
            Log.i(this.getClass().getName(), String.format("Error Searching Google: [%d] %s", statusCode, message));
            MaterialDialog dialog = new MaterialDialog.Builder(context)
                    .title("Error")
                    .content(message)
                    .positiveText("OK")
                    .show();
        }
    }

    private void showError(String message) {
        new MaterialDialog.Builder(this)
                .title("Error")
                .content(message)
                .positiveText("OK")
                .show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            MaterialDialog settingsDialog = new MaterialDialog.Builder(this)
                    .title(R.string.action_settings)
                    .customView(R.layout.fragment_settings, false)
                    .positiveText(R.string.setting_save)
                    .negativeText(R.string.setting_cancel)
                    .cancelable(true)
                    .onPositive(new MaterialDialog.SingleButtonCallback() {
                        @Override
                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                            Spinner sp_imageSize = (Spinner) dialog.findViewById(R.id.sp_image_size);
                            Spinner sp_imageType = (Spinner) dialog.findViewById(R.id.sp_image_type);
                            Spinner sp_imageColor = (Spinner) dialog.findViewById(R.id.sp_image_color);
                            EditText et_siteFilter = (EditText) dialog.findViewById(R.id.et_site_filter);

                            Setting setting = Setting.getSetting(dialog.getContext());
                            setting.imageType = (String)sp_imageType.getSelectedItem();
                            setting.imageSize = (String)sp_imageSize.getSelectedItem();
                            setting.imageColor = (String)sp_imageColor.getSelectedItem();
                            setting.siteFilter = et_siteFilter.getText().toString();

                            Log.i(this.getClass().getName(), String.format("Update setting: type=%s size=%s color=%s site=%s",
                                    setting.imageType,setting.imageSize,setting.imageColor,setting.siteFilter));

                            setting.save();
                            search();
                        }
                    })
                    .show();
            populateDialog(settingsDialog);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void populateDialog(MaterialDialog dialog) {
        View view = dialog.getCustomView();
        Setting setting = Setting.getSetting(view.getContext());

        Spinner sp_imageSize = (Spinner) view.findViewById(R.id.sp_image_size);
        Spinner sp_imageType = (Spinner) view.findViewById(R.id.sp_image_type);
        Spinner sp_imageColor = (Spinner) view.findViewById(R.id.sp_image_color);
        EditText et_siteFilter = (EditText) view.findViewById(R.id.et_site_filter);

        String[] imageSizeData = getResources().getStringArray(R.array.google_image_client_imgsz);
        String[] imageTypeData = getResources().getStringArray(R.array.google_image_client_imgtype);
        String[] imageColorData = getResources().getStringArray(R.array.google_image_client_imgcolor);

        ArrayAdapter<CharSequence> sizeAdaptor = new ArrayAdapter(this,android.R.layout.simple_spinner_item,imageSizeData);
        ArrayAdapter<CharSequence> typeAdaptor = new ArrayAdapter(this,android.R.layout.simple_spinner_item,imageTypeData);
        ArrayAdapter<CharSequence> colorAdaptor = new ArrayAdapter(this,android.R.layout.simple_spinner_item,imageColorData);

        sizeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorAdaptor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        sp_imageSize.setAdapter(sizeAdaptor);
        sp_imageType.setAdapter(typeAdaptor);
        sp_imageColor.setAdapter(colorAdaptor);

        int sizeIndex = Arrays.asList(imageSizeData).indexOf(setting.imageSize);
        int typeIndex = Arrays.asList(imageTypeData).indexOf(setting.imageType);
        int colorIndex = Arrays.asList(imageColorData).indexOf(setting.imageColor);

        sp_imageSize.setSelection(sizeIndex);
        sp_imageType.setSelection(typeIndex);
        sp_imageColor.setSelection(colorIndex);
        et_siteFilter.setText(setting.siteFilter);
    }
}
