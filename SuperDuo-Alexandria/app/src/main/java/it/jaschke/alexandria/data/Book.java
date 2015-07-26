package it.jaschke.alexandria.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {
    private String ean;
    private String title;
    private String subtitle;
    private String description;
    private String coverImageUrl;
    private String[] authors;
    private String[] categories;

    public Book(String ean, String title, String subtitle, String description, String coverImageUrl, String[] authors, String[] categories) {
        this.ean = ean;
        this.title = title;
        this.subtitle = subtitle;
        this.description = description;
        this.coverImageUrl = coverImageUrl;
        this.authors = authors;
        this.categories = categories;
    }

    public String getEan() {
        return ean;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public String getDescription() {
        return description;
    }

    public String getCoverImageUrl() {
        return coverImageUrl;
    }

    public String[] getAuthors() {
        return authors;
    }

    public String[] getCategories() {
        return categories;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.ean);
        dest.writeString(this.title);
        dest.writeString(this.subtitle);
        dest.writeString(this.description);
        dest.writeString(this.coverImageUrl);
        dest.writeStringArray(this.authors);
        dest.writeStringArray(this.categories);
    }

    protected Book(Parcel in) {
        this.ean = in.readString();
        this.title = in.readString();
        this.subtitle = in.readString();
        this.description = in.readString();
        this.coverImageUrl = in.readString();
        this.authors = in.createStringArray();
        this.categories = in.createStringArray();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        public Book createFromParcel(Parcel source) {
            return new Book(source);
        }

        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
}