package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.service.controls.Control;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.MediaController;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface, ControlFragment.ControlFragmentInterface {

    AudiobookService.MediaControlBinder audiobookService;
    boolean mBound = false;

    BookList bookList;
    DisplayFragment displayFragment;
    boolean secondContainer;
    Book selectedBook;
    FragmentManager manager;

    private final String ARG_SELECTED_BOOK = "selectedBook";

    private final String ID = "id", TITLE = "title", AUTHOR = "author", COVERURL = "cover_url", DURATION="duration";

    Button searchActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



        searchActivityButton = findViewById(R.id.launch_search_button);

        searchActivityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), SearchActivity.class);
                startActivity(intent);
            }
        });

        if (savedInstanceState != null) {
            selectedBook = savedInstanceState.getParcelable(ARG_SELECTED_BOOK);
            bookList = (BookList) savedInstanceState.getParcelableArrayList(ListFragment.ARG_BOOKLIST);
        } else if (getIntent().getExtras() != null){
            bookList = new BookList(this);
            JSONArray bookListJson = null;
            try {
                bookListJson = new JSONArray(getIntent().getExtras().getString(SearchActivity.BOOKLIST_JSON));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            JSONObject bookJson;
            try {
                for(int i = 0; i < bookListJson.length(); i++) {
                    bookJson = bookListJson.getJSONObject(i);
                    int id = bookJson.getInt(ID);
                    String title = bookJson.getString(TITLE);
                    String author = bookJson.getString(AUTHOR);
                    String coverURL = bookJson.getString(COVERURL);
                    int duration = bookJson.getInt(DURATION);
                    bookList.add(new Book(id, title, author, coverURL, duration));
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        } else {
            bookList = new BookList(this);
        }

        manager = getSupportFragmentManager();
        secondContainer = findViewById(R.id.container_2) != null;

        Fragment cFrag;
        cFrag = manager.findFragmentById(R.id.control_container);

        if(!(cFrag instanceof ControlFragment)) {
            manager
                    .beginTransaction()
                    .add(R.id.control_container, new ControlFragment())
                    .commit();
        }

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

    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audiobookService =  (AudiobookService.MediaControlBinder) service;
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

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
    public void playAudio() {
        if(mBound && selectedBook != null) {
            audiobookService.play(selectedBook.getId());
        }
    }

    @Override
    public void pauseAudio() {

    }

    @Override
    public void stopAudio() {

    }

    @Override
    public void seekAudio() {

    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_SELECTED_BOOK, selectedBook);
        outState.putParcelableArrayList(ListFragment.ARG_BOOKLIST, bookList);
    }

    @Override
    public void onBackPressed() {
        selectedBook = null;
        super.onBackPressed();
    }

}