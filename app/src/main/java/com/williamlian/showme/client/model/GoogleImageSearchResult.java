package com.williamlian.showme.client.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class GoogleImageSearchResult {

    private String width;

    private String height;

    public String imageId;

    @JsonProperty("tbWidth")
    private String thumbnailWidth;

    @JsonProperty("tbHeight")
    private String thumbnailHeight;

    public String url;

    public String title;

    @JsonProperty("titleNoFormatting")
    public String plainTitle;

    @JsonProperty("tbUrl")
    public String thumbnailUrl;

    public int getWidth() {
        return Integer.parseInt(width);
    }

    public int getHeight() {
        return Integer.parseInt(height);
    }

    public int getThumbnailWidth() {
        return Integer.parseInt(thumbnailWidth);
    }

    public int getThumbnailHeight() {
        return Integer.parseInt(thumbnailHeight);
    }
}
