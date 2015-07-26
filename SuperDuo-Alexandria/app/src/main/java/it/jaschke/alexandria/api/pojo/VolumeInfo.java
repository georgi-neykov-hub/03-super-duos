
package it.jaschke.alexandria.api.pojo;

import java.util.ArrayList;
import java.util.List;
import com.google.gson.annotations.Expose;

public class VolumeInfo {

    @Expose
    private String subtitle;
    @Expose
    private String title;
    @Expose
    private String[] authors;
    @Expose
    private String description;
    @Expose
    private ImageLinks imageLinks;
    @Expose
    private String[] categories;

    public String[] getCategories() {
        return categories;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getTitle() {
        return title;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String getDescription() {
        return description;
    }

    public ImageLinks getImageLinks() {
        return imageLinks;
    }
}
