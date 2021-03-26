package edu.temple.bookshelf;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface {

    BookList bookList;
    DisplayFragment displayFragment;
    boolean secondContainer;
    boolean itemChosen;
    FragmentManager manager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();
        secondContainer = findViewById(R.id.container_2) != null;

        Log.d("ACTIVITY", "Launched main activity! Value of itemChosen: ");

        bookList = new BookList(this);
        bookList.readFromFile("bookfile");

        manager
                .beginTransaction()
                .replace(R.id.container_1, ListFragment.newInstance(bookList))
                .commit();


        displayFragment = new DisplayFragment();
        if(secondContainer) {
            manager
                    .beginTransaction()
                    .add(R.id.container_2, displayFragment)
                    .addToBackStack(null)
                    .commit();
        }

    }

    @Override
    public void itemClicked(int position) {
        if(secondContainer) {
            displayFragment.changeBook(bookList.get(position));
        } else {
            displayFragment = DisplayFragment.newInstance(bookList.get(position));
            manager
                .beginTransaction()
                .replace(R.id.container_1, displayFragment)
                .addToBackStack(null)
                .commit();
        }
    }
}