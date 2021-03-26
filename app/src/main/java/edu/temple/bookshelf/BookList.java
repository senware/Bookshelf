package edu.temple.bookshelf;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.io.DataInputStream;
import java.util.ArrayList;
import java.util.Scanner;

public class BookList extends ArrayList<Parcelable> {
    private Context context;

    public BookList(Context context) {
        this.context = context;
    }

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
}
