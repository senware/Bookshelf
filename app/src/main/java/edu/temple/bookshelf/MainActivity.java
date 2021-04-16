package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface, ControlFragment.ControlFragmentInterface {

    AudiobookService.MediaControlBinder audiobookService;
    boolean mBound = false;

    BookList bookList;
    boolean secondContainer;
    Book selectedBook;
    Book playingBook;
    int currentPosition;
    boolean paused, playing;

    FragmentManager manager;
    DisplayFragment displayFragment;
    ControlFragment cFrag;

    private final String ARG_SELECTED_BOOK = "selectedBook";
    private final String ARG_PLAYING_BOOK = "playingBook";
    private final String ARG_CURRENT_POSITION = "currentPosition";
    private final String ARG_PAUSED = "paused";
    private final String ARG_PLAYING = "playing";

    private final String ID = "id", TITLE = "title", AUTHOR = "author", COVERURL = "cover_url", DURATION = "duration";

    Button searchActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        manager = getSupportFragmentManager();
        secondContainer = findViewById(R.id.container_2) != null;

        searchActivityButton = findViewById(R.id.launch_search_button);

        searchActivityButton.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), SearchActivity.class);
            startActivityForResult(intent, 1);
        });

        if (savedInstanceState != null) {
            selectedBook = savedInstanceState.getParcelable(ARG_SELECTED_BOOK);
            bookList = (BookList) savedInstanceState.getParcelableArrayList(ListFragment.ARG_BOOKLIST);
            currentPosition = savedInstanceState.getInt(ARG_CURRENT_POSITION);
            paused = savedInstanceState.getBoolean(ARG_PAUSED);
            playing = savedInstanceState.getBoolean(ARG_PLAYING);
            playingBook = savedInstanceState.getParcelable(ARG_PLAYING_BOOK);
            if (selectedBook != null && playingBook != null) {
                Log.d("STATE", "Selected Book: " + selectedBook.getTitle());
                Log.d("STATE", "Playing Book: " + playingBook.getTitle());
            }
        } else {
            bookList = new BookList(this);
        }

        Fragment cFragCheck = manager.findFragmentById(R.id.control_container);
        if (!(cFragCheck instanceof ControlFragment)) {
            manager
                    .beginTransaction()
                    .add(R.id.control_container, ControlFragment.newInstance(), "CONTROL")
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                bookList = new BookList(this);
                JSONArray bookListJson = null;
                try {
                    bookListJson = new JSONArray(data.getExtras().getString(SearchActivity.BOOKLIST_JSON));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                JSONObject bookJson;
                try {
                    for (int i = 0; i < bookListJson.length(); i++) {
                        bookJson = bookListJson.getJSONObject(i);
                        int id = bookJson.getInt(ID);
                        String title = bookJson.getString(TITLE);
                        String author = bookJson.getString(AUTHOR);
                        String coverURL = bookJson.getString(COVERURL);
                        int duration = bookJson.getInt(DURATION);
                        bookList.add(new Book(id, title, author, coverURL, duration));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                manager
                        .beginTransaction()
                        .replace(R.id.container_1, ListFragment.newInstance(bookList))
                        .commit();
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, AudiobookService.class);
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audiobookService = (AudiobookService.MediaControlBinder) service;
            mBound = true;

            audiobookService.setProgressHandler(new Handler(msg -> {
                if (!paused && playing) {
                    AudiobookService.BookProgress bookProgress = (AudiobookService.BookProgress) msg.obj;
                    currentPosition = bookProgress.getProgress();
                    cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");
                    if (cFrag != null) {
                        cFrag.setProgress(currentPosition);
                    }
                    return true;
                }
                return false;
            }));
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mBound = false;
        }
    };

    @Override
    public void itemClicked(int position) {
        selectedBook = bookList.get(position);
        stopAudio();
        cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");
        if (cFrag != null) {
            cFrag.setDuration(selectedBook.getDuration());
            cFrag.setText(selectedBook.getTitle());
        }

        if (secondContainer) {
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
        if (mBound && selectedBook != null) {
            if (selectedBook != playingBook) {
                currentPosition = 0;
                audiobookService.play(selectedBook.getId(), currentPosition);
                playing = true;
                playingBook = selectedBook;
            } else if (paused) {
                audiobookService.pause();
            } else if (!playing) {
                audiobookService.play(selectedBook.getId(), currentPosition);
                playing = true;
                playingBook = selectedBook;
            }

            paused = false;

            Log.d("STATE", "Paused: " + paused);
            Log.d("STATE", "Playing: " + playing);
        }
    }

    @Override
    public void pauseAudio() {
        if (mBound && selectedBook != null && playing) {
            paused = !paused;
            audiobookService.pause();
        }

        Log.d("STATE", "Paused: " + paused);
        Log.d("STATE", "Playing: " + playing);
    }

    @Override
    public void stopAudio() {
        if (playing) {
            audiobookService.stop();
            playing = false;

            if (paused) {
                paused = !paused;
            }

            currentPosition = 0;
            cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");

            if (cFrag != null) {
                cFrag.setProgress(0);
            }
        }
        Log.d("STATE", "Paused: " + paused);
        Log.d("STATE", "Playing: " + playing);
    }

    @Override
    public void seekAudio(int position) {
        currentPosition = position;
        audiobookService.seekTo(position);
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(ARG_SELECTED_BOOK, selectedBook);
        outState.putParcelableArrayList(ListFragment.ARG_BOOKLIST, bookList);
        outState.putInt(ARG_CURRENT_POSITION, currentPosition);
        outState.putBoolean(ARG_PAUSED, paused);
        outState.putBoolean(ARG_PLAYING, playing);
        outState.putParcelable(ARG_PLAYING_BOOK, playingBook);
    }

    @Override
    public void onBackPressed() {
        selectedBook = null;
        super.onBackPressed();
    }

}