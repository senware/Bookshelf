package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class DisplayFragment extends Fragment {

    private static final String ARG_BOOK = "book";

    private Book book;

    private TextView title;
    private TextView author;

    public DisplayFragment() {
        // Required empty public constructor
    }

    public static DisplayFragment newInstance(Book book) {
        DisplayFragment instance = new DisplayFragment();
        Bundle args = new Bundle();
        Log.d("PARCEL", "Packaged book with Title: " + book.getTitle());
        args.putParcelable(ARG_BOOK, book);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(ARG_BOOK);
            Log.d("PARCEL", "Unpackaged book with Title: " + book.getTitle());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_display, container, false);

        Log.d("FRAG", "Creating Display Fragment View!");

        title = layout.findViewById(R.id.book_title);
        author = layout.findViewById((R.id.book_author));
        if (book != null && title != null && author != null) {
            changeBook(book);
        }

        return layout;
    }

    public void changeBook(Book book) {
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
    }
}