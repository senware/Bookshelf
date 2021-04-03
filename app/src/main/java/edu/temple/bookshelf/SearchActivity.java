package edu.temple.bookshelf;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

public class SearchActivity extends AppCompatActivity {

    EditText searchEditText;
    Button searchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        Context context = this;

        searchEditText = findViewById(R.id.search_edit_text);
        searchButton = findViewById(R.id.search_button);

        searchEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                clearPrompt();
            }
        }); // setOnFocusChangeListener

        Handler handler = new Handler(new Handler.Callback() {
            @Override
            public boolean handleMessage(@NonNull Message msg) {
                Intent intent = new Intent(context, MainActivity.class);
                Bundle extras = new Bundle();
                extras.putString("booklistJson", (String) msg.obj);
                intent.putExtras(extras);
                startActivity(intent);
                return true;
            }
        }); // Handler

        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearPrompt();
                String searchText = searchEditText.getText().toString();

                new Thread() {
                    public void run() {
                        String urlString = "https://kamorris.com/lab/cis3515/search.php?term=" + searchText;
                        Log.d("URL", urlString);

                        try {
                            URL url = new URL(urlString);
                            BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                            StringBuilder results = new StringBuilder();
                            String line;

                            while((line = reader.readLine()) != null) {
                                results.append(line);
                            }

                            Message message = Message.obtain();
                            message.obj = results.toString();
                            handler.sendMessage(message);

                        } catch (Exception e) {
                            e.printStackTrace();
                        } // try/catch
                    } // run
                }.start(); // Thread
            } // onClick
        }); // setOnClickListener

    } // onCreate

    private void clearPrompt() {
        if(searchEditText.getText().toString().equals(getString(R.string.search_prompt))) {
            searchEditText.setText("");
        }
    } // clearPrompt

}