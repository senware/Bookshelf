package edu.temple.bookshelf;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {

    private int id;
    private String title;
    private String author;
    private String coverURL;

    public Book (String title, String author) {
        this.title = title;
        this.author = author;
    }

    public Book(int id, String title, String author, String coverURL) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.coverURL = coverURL;
    }

    protected Book(Parcel in) {
        id = in.readInt();
        title = in.readString();
        author = in.readString();
        coverURL = in.readString();
    }

    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };

    public int getId() { return id; }
    public String getTitle() { return this.title; }
    public String getAuthor() { return this.author; }
    public String getCoverURL() { return coverURL; }

    public void setTitle(String title) { this.title = title; }
    public void setAuthor(String author) { this.author = author; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(title);
        dest.writeString(author);
        dest.writeString(coverURL);
    }
}
