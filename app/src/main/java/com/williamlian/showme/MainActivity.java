package com.williamlian.showme;

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
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.etsy.android.grid.StaggeredGridView;
import com.williamlian.showme.adaptor.ImageResultAdaptor;
import com.williamlian.showme.client.GoogleImageSearchClient;
import com.williamlian.showme.client.model.GoogleImageSearchResult;
import com.williamlian.showme.model.Setting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private GoogleImageSearchClient googleImageSearchClient = new GoogleImageSearchClient();
    private List<GoogleImageSearchResult> images;
    private ImageResultAdaptor imageResultAdaptor;
    private String lastSearchString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        StaggeredGridView sgv_images = (StaggeredGridView)findViewById(R.id.sgv_results);

        images = new ArrayList<>();
        imageResultAdaptor = new ImageResultAdaptor(this, images);
        sgv_images.setAdapter(imageResultAdaptor);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        final MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(searchItem);

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
                ((SearchView)v).setQuery(lastSearchString, false);
            }
        });
        return super.onCreateOptionsMenu(menu);
    }

    public void search(String query) {
        if(query == null || query.isEmpty()) {
            showSearchError(0, "Search Text is Empty");
        } else {
            Setting setting = Setting.getSetting(this);
            int resultSize = getResources().getInteger(R.integer.result_size);
            multiSearch(query, setting, resultSize);
            lastSearchString = query;
        }
    }

    public void search() {
        search(lastSearchString);
    }

    public void multiSearch(String query, Setting setting, int size) {
        Log.i(this.getClass().getName(),String.format("Searching google for: %s, result size=%d, setting=%s", query, size, setting));
        googleImageSearchClient.multiPageSearch(query, size, 0, setting, new GoogleImageSearchClient.GoogleImageSearchResultCallback() {
            private boolean initial = true;
            @Override
            public void onSuccess(List<GoogleImageSearchResult> results) {
                if (initial) {
                    imageResultAdaptor.clear();
                    initial = false;
                }
                imageResultAdaptor.addAll(results);
            }

            @Override
            public void onFailure(int statusCode, String message) {
                showSearchError(statusCode, message);
            }
        });
    }

    private void showSearchError(int statusCode, String message) {
        Log.i(this.getClass().getName(),String.format("Error Searching Google: [%d] %s", statusCode, message));
        Toast.makeText(MainActivity.this, String.format("Error Searching Google: %s", message), Toast.LENGTH_SHORT).show();
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
