package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.os.Bundle;
import android.util.Log;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface {

    BookList bookList;
    DisplayFragment displayFragment;
    boolean secondContainer;
    Book selectedBook;
    FragmentManager manager;
    private final String ARG_SELECTED_BOOK = "selectedBook";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState != null) {
            selectedBook = savedInstanceState.getParcelable(ARG_SELECTED_BOOK);
        }

        manager = getSupportFragmentManager();
        secondContainer = findViewById(R.id.container_2) != null;

        bookList = new BookList(this);
        bookList.readFromFile("bookfile");

        Fragment f1;
        f1 = manager.findFragmentById((R.id.container_1));

        if (f1 instanceof DisplayFragment) {
            manager.popBackStack();
        } else if (!(f1 instanceof ListFragment)) {
            manager
                    .beginTransaction()
                    .add(R.id.container_1, ListFragment.newInstance(bookList), "BOOKLIST")
                    .commit();
        }

        displayFragment = (selectedBook == null) ? new DisplayFragment() : DisplayFragment.newInstance(selectedBook);

        if (secondContainer) {
            manager
                    .beginTransaction()
                    .replace(R.id.container_2, displayFragment, "DISPLAY")
                    .commit();
        } else if (selectedBook != null) {
            manager
                    .beginTransaction()
                    .replace(R.id.container_1, displayFragment, "DISPLAY")
                    .addToBackStack(null)
                    .commit();
        }

//        manager
//                .beginTransaction()
//                .replace(R.id.container_1, ListFragment.newInstance(bookList))
//                .commit();
//
//
//        displayFragment = new DisplayFragment();
//        if(secondContainer) {
//            manager
//                    .beginTransaction()
//                    .add(R.id.container_2, displayFragment)
//                    .addToBackStack(null)
//                    .commit();
//        }

    }

    @Override
    public void itemClicked(int position) {
        selectedBook = bookList.get(position);

        if(secondContainer) {
            displayFragment.changeBook(bookList.get(position));
        } else {
            displayFragment = DisplayFragment.newInstance(selectedBook);
            manager
                .beginTransaction()
                .replace(R.id.container_1, displayFragment, "DISPLAY")
                .addToBackStack(null)
                .commit();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_SELECTED_BOOK, selectedBook);
    }

    @Override
    public void onBackPressed() {
        selectedBook = null;
        super.onBackPressed();
    }
}