package com.example.linememo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ListAdapter extends BaseAdapter
{
    LayoutInflater inflater = null;
    private ArrayList<Item> items;

    public ListAdapter() {};

    public ListAdapter (ArrayList<Item> item)
    {
        items = item;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null)
        {
            final Context context = parent.getContext();
            if (inflater == null)
            {
                inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            }
            convertView = inflater.inflate(R.layout.list_item, parent, false);
        }

        TextView textTitle = (TextView) convertView.findViewById(R.id.textTitle);
        TextView textContent = (TextView) convertView.findViewById(R.id.textContent);
        TextView textDate = (TextView) convertView.findViewById(R.id.textDate);
        Item temp = items.get(position);
        textTitle.setText(temp.getTitle());
        textContent.setText(temp.getText());
        textDate.setText(temp.getDate());
        ImageView imageView = (ImageView) convertView.findViewById(R.id.imageItem);
        if (temp.getImage() != null)//이미지는 0개 이상
        {
            imageView.setVisibility(View.VISIBLE);
            imageView.setImageURI(temp.getImage());
        }
        else
        {
            imageView.setVisibility(View.INVISIBLE);
        }

        return convertView;
    }

    public void setItem(ArrayList<Item> addedItem)
    {
        items = addedItem;
        notifyDataSetChanged();
    }

}
