package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Andrew on 23/01/2015.
 */
public class Photo implements Parcelable {
    public static final Creator<Photo> CREATOR = new Creator<Photo>() {
        public Photo createFromParcel(Parcel in) {
            return new Photo(in);
        }

        public Photo[] newArray(int size) {
            return new Photo[size];
        }
    };
    private String id;            // DB id
    private String description;
    private String url;

    public Photo() {
        this.id = "";
        this.description = "";
        this.url = "";
    }

    public Photo(String id,
                 String description,
                 String url) {
        this.id = id;
        this.description = description;
        this.url = url;
    }

    // Parcelable
    public Photo(Parcel in) {
        this.id = in.readString();
        this.description = in.readString();
        this.url = in.readString();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }


    @Override
    public String toString() {
        return "Photo ["
                + "id=" + id
                + ", description=" + description
                + ", url=" + url
                + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.description);
        dest.writeString(this.url);
    }
}
