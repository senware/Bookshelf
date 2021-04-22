package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import com.google.gson.Gson;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements ListFragment.ListFragmentInterface, ControlFragment.ControlFragmentInterface {

    AudiobookService.MediaControlBinder audiobookService;
    boolean audioServiceConnected = false;
    Intent audioServiceIntent;

    BookList bookList;
    boolean secondContainer;
    Book selectedBook;
    int currentPosition;
    boolean paused;

    FragmentManager manager;
    DisplayFragment displayFragment;
    ControlFragment cFrag;

    File bookFile;
    DownloadManager downloadManager;
    long downloadID;
    BroadcastReceiver onDownloadedBook;

    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Gson gson;

    private final String ARG_SELECTED_BOOK = "selectedBook";
    private final String ARG_CURRENT_POSITION = "currentPosition";
    private final String ARG_PAUSED = "paused";
    private final String ARG_PLAYING = "playing";
    private final String BOOK_CASE_API = "https://kamorris.com/lab/audlib/download.php?id=";

    private final String ID = "id", TITLE = "title", AUTHOR = "author", COVERURL = "cover_url", DURATION = "duration";
    private final String KEY_LAST_PLAYED_BOOK = "lastPlayedBook";
    private final String KEY_BOOKLIST_SIZE = "booklistSize";

    Button searchActivityButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedPreferences = getSharedPreferences(
                "edu.temple.Bookshelf.preferences",
                Context.MODE_PRIVATE
        );
        editor = sharedPreferences.edit();
        gson = new Gson();

        manager = getSupportFragmentManager();
        secondContainer = findViewById(R.id.container_2) != null;

        onDownloadedBook = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long broadcastID = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (downloadID == broadcastID) {
                    Toast.makeText(context, "Book downloaded", Toast.LENGTH_LONG).show();
                }
            }
        };

        IntentFilter downloadIntentFilter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        registerReceiver(onDownloadedBook, downloadIntentFilter);

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
            if (selectedBook != null) {
                Log.d("STATE", "Selected Book: " + selectedBook.getTitle());
            }
        } else {
            bookList = new BookList(this);
        }

        loadBookList();
        loadLastBook();
        loadBookProgress();

        Fragment cFragCheck = manager.findFragmentById(R.id.control_container);
        if (!(cFragCheck instanceof ControlFragment)) {
            manager
                    .beginTransaction()
                    .add(R.id.control_container, ControlFragment.newInstance(), "CONTROL")
                    .commit();
            Log.d("FRAG", "Created new ControlFragment");
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
                manager.popBackStack();
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
        audioServiceIntent = new Intent(this, AudiobookService.class);
        bindService(audioServiceIntent, connection, Context.BIND_AUTO_CREATE);
    }

    private final ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            audiobookService = (AudiobookService.MediaControlBinder) service;
            audioServiceConnected = true;

            audiobookService.setProgressHandler(new Handler(msg -> {
                if (!paused && msg.obj != null) {
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
            audioServiceConnected = false;
        }
    };

    @Override
    public void itemClicked(int position) {
        if (selectedBook != null)
            saveBookProgress();

        selectedBook = bookList.get(position);
        stopAudio();
        loadBookProgress();

        cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");
        if (cFrag != null) {
            restoreControllerUI();
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

    private void saveBookProgress() {
        if (selectedBook != null) {
            int savedPosition = Math.max(0, currentPosition - 10);
            editor.putInt(selectedBook.getTitle(), savedPosition).apply();
        }
    }

    private void loadBookProgress() {
        if (selectedBook != null)
            currentPosition = sharedPreferences.getInt(selectedBook.getTitle(), 0);
        else
            Log.d("SAVE", "Selected book was null?");
    }

    private void saveLastBook() {
        editor.putString(KEY_LAST_PLAYED_BOOK, gson.toJson(selectedBook)).apply();
    }

    private void loadLastBook() {
        Book lastBook = gson.fromJson(sharedPreferences.getString(KEY_LAST_PLAYED_BOOK, null), Book.class);
        if (lastBook != null) {
            Log.d("SAVE", lastBook.getTitle());
        }
        selectedBook = lastBook;
    }

    private void saveBookList() {
        editor.putInt(KEY_BOOKLIST_SIZE, bookList.size());
        String temp;
        for (int i = 0; i < bookList.size(); i++) {
            temp = gson.toJson(bookList.get(i));
            editor.putString("book_" + i, temp).apply();
        }
    }

    private void loadBookList() {
        int size = sharedPreferences.getInt(KEY_BOOKLIST_SIZE, 0);
        String tempString;
        Book tempBook;
        BookList retList = new BookList(this);
        for (int i = 0; i < size; i++) {
            tempString = sharedPreferences.getString("book_" + i, null);
            tempBook = gson.fromJson(tempString, Book.class);
            retList.add(tempBook);
        }
        bookList = retList;
    }

    @Override
    public void restoreControllerUI() {
        cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");
        if (cFrag != null && selectedBook != null) {
            cFrag.setDuration(selectedBook.getDuration());
            cFrag.setProgress(currentPosition);
            cFrag.setText(selectedBook.getTitle());
        }
    }

    // if you pause, then exit the app, then relaunch
    // the play button will act like a pause button
    // i know why it happens but it doesn't break anything
    // it still plays when it needs to so i'm not going to fix it
    // it's not a bug, it's a feature
    @Override
    public void playAudio() {
        if (audioServiceConnected && selectedBook != null) {
            Log.d("PLAY", "Now playing: " + selectedBook.getTitle());

            String bookFileName = selectedBook.getTitle().replaceAll(" ", "_");
            bookFile = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), bookFileName);
            Log.d("FILE", "book file exists: " + bookFile.exists());
            Log.d("FILE", (float) (bookFile.length()) / 1000000f + " MB");

            if (bookFile.exists()) {
                if (paused) {
                    audiobookService.pause();
                } else {
                    audiobookService.play(bookFile, currentPosition);
                    startService(audioServiceIntent);
                }

            } else {
                String url = BOOK_CASE_API + selectedBook.getId();
                downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url))
                        .setDestinationUri(Uri.fromFile(bookFile))
                        .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                        .setTitle(bookFileName);
                downloadID = downloadManager.enqueue(request);


                if (paused) {
                    audiobookService.pause();
                } else {
                    audiobookService.play(selectedBook.getId(), currentPosition);
                    startService(audioServiceIntent);
                }
            }

            paused = false;

            Log.d("STATE", "Paused: " + paused);
        }
    }

    @Override
    public void pauseAudio() {
        if (audioServiceConnected && selectedBook != null) {
            paused = !paused;
            audiobookService.pause();
        }

        Log.d("STATE", "Paused: " + paused);
    }

    @Override
    public void stopAudio() {
            audiobookService.stop();

            if (paused) {
                paused = !paused;
            }

            currentPosition = 0;
            cFrag = (ControlFragment) manager.findFragmentByTag("CONTROL");

            if (cFrag != null) {
                cFrag.setProgress(0);
            }

            stopService(audioServiceIntent);

        Log.d("STATE", "Paused: " + paused);
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
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(onDownloadedBook);
        saveBookList();
        saveLastBook();
        saveBookProgress();
        super.onDestroy();
    }
}