package edu.temple.bookshelf;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

public class ListFragment extends Fragment {

    private ListFragmentInterface parentActivity;

    private static final String ARG_BOOKLIST = "bookList";

    private BookList bookList;

    public ListFragment() {
        // Required empty public constructor
    }

    public static ListFragment newInstance(BookList bookList) {
        ListFragment instance = new ListFragment();
        Bundle args = new Bundle();
        args.putParcelableArrayList(ARG_BOOKLIST, bookList);
        instance.setArguments(args);
        return instance;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bookList = (BookList) getArguments().getParcelableArrayList(ARG_BOOKLIST);
        }
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Activity parentActivityTemp = getActivity();
        if (parentActivityTemp instanceof ListFragmentInterface) {
            parentActivity = (ListFragmentInterface) parentActivityTemp;
        }
        else {
            throw new RuntimeException("ListFragmentInterface must be implemented in attached activity.");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View layout = inflater.inflate(R.layout.fragment_list, container, false);

        Log.d("FRAG", "Creating Fragment View!");

        ListView listView = layout.findViewById(R.id.bookListView);
        BookAdapter bookAdapter = new BookAdapter((Context) parentActivity, bookList);
        listView.setAdapter(bookAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                parentActivity.itemClicked(position);
            }
        });

        return layout;
    }

    interface ListFragmentInterface {
        public void itemClicked(int position);
    }
}