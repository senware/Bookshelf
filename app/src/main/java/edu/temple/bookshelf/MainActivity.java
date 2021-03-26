package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface {

    BookList bookList;
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
    }

    @Override
    public void itemClicked(int position) {
        int target;
        if(secondContainer){
            target = R.id.container_2;
        } else {
            target = R.id.container_1;
        }
        getSupportFragmentManager()
            .beginTransaction()
            .replace(target, DisplayFragment.newInstance(bookList.get(position)))
            .addToBackStack(null)
            .commit();
    }
}