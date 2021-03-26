package edu.temple.bookshelf;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class DisplayFragment extends Fragment {

    public DisplayFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_list, container, false);

        Log.d("FRAG", "Creating Fragment View!");

        ListView listView = layout.findViewById(R.id.bookListView);
        Context context = getActivity();
        BookList bookList = new BookList(context);
        bookList.readFromFile("bookfile");
        BookAdapter bookAdapter = new BookAdapter(context, bookList);
        listView.setAdapter(bookAdapter);


        return layout;
    }
}