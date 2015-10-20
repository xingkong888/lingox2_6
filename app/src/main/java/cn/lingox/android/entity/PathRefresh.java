package cn.lingox.android.entity;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PathRefresh implements Parcelable {
    // Constants
    public static final Creator<PathRefresh> CREATOR = new Creator<PathRefresh>() {
        public PathRefresh createFromParcel(Parcel in) {
            return new PathRefresh(in);
        }

        public PathRefresh[] newArray(int size) {
            return new PathRefresh[size];
        }
    };

    private String user_src;//发起评论的用户id
    private String user_tar;//被评论的用户id
    private String path_id;//活动id
    private String content;//评论内容
    private ArrayList<PathRefreshReply> replays;

    // Parcelable
    public PathRefresh(Parcel in) {
        this.user_src = in.readString();
        this.user_tar = in.readString();
        this.path_id = in.readString();
        this.content = in.readString();
        this.replays = in.createTypedArrayList(PathRefreshReply.CREATOR);
    }

    @Override
    public String toString() {
        return "[ user_src=" + user_src +
                ", user_tar=" + user_tar +
                ", path_id=" + path_id +
                ", content=" + content +
                ", replays=" + replays +
                "]";
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(user_src);
        dest.writeString(user_tar);
        dest.writeString(path_id);
        dest.writeString(content);
        dest.writeTypedList(replays);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}