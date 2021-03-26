package edu.temple.bookshelf;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

public class BookAdapter extends BaseAdapter {
    Context context;
    BookList bookList;

    public BookAdapter(Context context, BookList bookList) {
        this.context = context;
        this.bookList = bookList;
    }

    @Override
    public int getCount() {
        return bookList.size();
    }

    @Override
    public Book getItem(int position) {
        return (Book) bookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LinearLayout layout;
        TextView title;
        TextView author;

        if (convertView == null) {
            layout = new LinearLayout(context);
            title = new TextView(context);
            author = new TextView(context);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.addView(title);
            layout.addView(author);
            title.setTextSize(14);
            author.setTextSize(12);
        } else {
            layout = (LinearLayout) convertView;
            title = (TextView) layout.getChildAt(0);
            author = (TextView) layout.getChildAt(1);
        }

        title.setText(bookList.get(position).getTitle());
        author.setText(bookList.get(position).getAuthor());

        return layout;
    }
}
