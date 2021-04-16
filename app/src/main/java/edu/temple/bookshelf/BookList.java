package edu.temple.bookshelf;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class BookList extends ArrayList<Parcelable> implements Parcelable {
    private Context context;

    public BookList(Context context) {
        this.context = context;
    }

    protected BookList(Parcel in) {
    }

    public static final Creator<BookList> CREATOR = new Creator<BookList>() {
        @Override
        public BookList createFromParcel(Parcel in) {
            return new BookList(in);
        }

        @Override
        public BookList[] newArray(int size) {
            return new BookList[size];
        }
    };

    public Book get(int index) {
        return (Book) super.get(index);
    }

    public void readFromFile(String filename) {
        try {
            DataInputStream bookFileStream = new DataInputStream(context.getAssets().open(filename));
            Scanner scanner = new Scanner(bookFileStream);
            String temp;
            String title, author;
            while (scanner.hasNext()) {
                temp = scanner.next();
                title = temp.replaceAll("_", " ");
                temp = scanner.next();
                author = temp.replaceAll("_", " ");
                this.add(new Book(title, author));
                Log.d("FILE", "Adding " + title + ", by " + author + ".");
            }
        } catch (Exception e) {
            Log.d("FILE", "File not found.");
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }
}
