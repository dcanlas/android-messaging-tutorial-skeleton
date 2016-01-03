package com.wfl.application;

import android.app.Activity;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.example.messagingtutorialskeleton.R;
import com.sinch.android.rtc.messaging.WritableMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MessageAdapter extends BaseAdapter {

    public static final int DIRECTION_INCOMING = 0;
    public static final int DIRECTION_OUTGOING = 1;
    public static final String LOG_TAG = "message_adapter";
    private List<Pair<WritableMessage, Integer>> messages;
    private LayoutInflater layoutInflater;
    private Activity mActivity;

    public MessageAdapter(Activity activity) {
        layoutInflater = activity.getLayoutInflater();
        mActivity = activity;
        messages = new ArrayList<Pair<WritableMessage, Integer>>();
    }

    public void addMessage(WritableMessage message, int direction) {
        messages.add(new Pair(message, direction));
        notifyDataSetChanged();
    }

    public FoodModel parseMessage(WritableMessage message) {
        try {
            JSONObject jsonObject = new JSONObject(message.getTextBody());
            return new FoodModel(jsonObject.getInt("id"), jsonObject.getString("name"), jsonObject.getString("drawableName"));
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public int getCount() {
        return messages.size();
    }

    @Override
    public Object getItem(int i) {
        return messages.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int i) {
        return messages.get(i).second;
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        int direction = getItemViewType(i);
        //show message on left or right, depending on if
        //it's incoming or outgoing
        if (convertView == null) {
            int res = 0;
            if (direction == DIRECTION_INCOMING) {
                res = R.layout.message_right;
            } else if (direction == DIRECTION_OUTGOING) {
                res = R.layout.message_left;
            }
            convertView = layoutInflater.inflate(res, viewGroup, false);
        }
        WritableMessage message = messages.get(i).first;
        FoodModel food = parseMessage(message);
        ImageView txtImage = (ImageView) convertView.findViewById(R.id.txtImage);
        int foodIcon = mActivity.getResources().getIdentifier(food.getDrawableName(), "drawable", mActivity.getPackageName());
        txtImage.setImageResource(foodIcon);
        return convertView;
    }
}
