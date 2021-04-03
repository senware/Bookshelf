package edu.temple.bookshelf;

import android.net.Uri;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

public class DisplayFragment extends Fragment {

    private static final String ARG_BOOK = "book";

    private Book book;

    private TextView title;
    private TextView author;
    private ImageView cover;

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
        Log.d("FRAG", "Creating Display Fragment!");
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

        title = layout.findViewById(R.id.book_title);
        author = layout.findViewById((R.id.book_author));
        cover = layout.findViewById(R.id.book_cover);
        if (book != null && title != null && author != null) {
            changeBook(book);
        }

        return layout;
    }

    public void changeBook(Book book) {
        title.setText(book.getTitle());
        author.setText(book.getAuthor());
        String coverURL = book.getCoverURL();
        if (coverURL != null)
            Picasso.with(getContext()).load(Uri.parse(coverURL)).into(cover);
    }
}