package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity {

    BookList bookList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d("ACTIVITY", "Launched main activity!");

        bookList = new BookList(this);
        bookList.readFromFile("bookfile");

        getSupportFragmentManager()
                .beginTransaction()
                .add(R.id.container_1, ListFragment.newInstance(bookList, this))
                .commit();
    }
}