package com.wfl.application;


import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class FoodModel {

    static final String LOG_TAG = "messaging_activity";

    private int id;
    private String name;
    private String drawableName;

    public FoodModel(int id, String name, String drawableName) {
        this.id = id;
        this.name = name;
        this.drawableName = drawableName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDrawableName() {
        return drawableName;
    }

    public void setDrawableName(String drawableName) {
        this.drawableName = drawableName;
    }

    public String toJSON() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("id", getId());
            jsonObject.put("name", getName());
            jsonObject.put("drawableName", getDrawableName());

            return jsonObject.toString();
        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage());
            return "";
        }
    }
}
