package com.wfl.application;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.waffle.wfl.R;

import java.util.List;

public class FriendListAdapter extends BaseAdapter {

    Context context;
    LayoutInflater layoutInflater;
    List<String> names;

    public FriendListAdapter(List<String> names, Context context) {
        this.names = names;
        this.context = context;
        layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return names.size();
    }

    @Override
    public Object getItem(int i) {
        return names.get(i);
    }

    @Override
    public long getItemId(int i) {
        //doesn't really have an id for now
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = layoutInflater.inflate(R.layout.inviteable_user_list_item, viewGroup, false);
        }
        TextView name = (TextView) view.findViewById(R.id.inviteableUserName);
        name.setText(names.get(i));

        return view;
    }
}
