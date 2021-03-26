package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface {

    BookList bookList;
    DisplayFragment displayFragment;
    boolean secondContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        secondContainer = findViewById(R.id.container_2) != null;

        Log.d("ACTIVITY", "Launched main activity!");

        bookList = new BookList(this);
        bookList.readFromFile("bookfile");

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container_1, ListFragment.newInstance(bookList))
                .commit();

        if (secondContainer) {
            displayFragment = new DisplayFragment();
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.container_2, displayFragment)
                    .commit();
        }
    }

    @Override
    public void itemClicked(int position) {
        if(secondContainer) {
            displayFragment.changeBook(bookList.get(position)) ;
        } else {
        getSupportFragmentManager()
            .beginTransaction()
            .replace(R.id.container_1, DisplayFragment.newInstance(bookList.get(position)))
            .addToBackStack(null)
            .commit();
        }
    }
}