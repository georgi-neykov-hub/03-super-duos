
package it.jaschke.alexandria.api.pojo;

import com.google.gson.annotations.Expose;

public class ImageLinks {

    @Expose
    private String smallThumbnail;
    @Expose
    private String thumbnail;
    @Expose
    private String small;
    @Expose
    private String medium;
    @Expose
    private String large;

    public String getSmallThumbnail() {
        return smallThumbnail;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public String getSmall() {
        return small;
    }

    public String getMedium() {
        return medium;
    }

    public String getLarge() {
        return large;
    }
}
