package com.wfl.application;

import com.firebase.client.Firebase;

public class MainDAO {
    private Firebase firebaseRef;

    private static MainDAO instance;

    public static void initInstance() {
        if (instance == null) {
            instance = new MainDAO();
        }
    }

    public static MainDAO getInstance() {
        return instance;
    }

    private MainDAO() {
        firebaseRef = new Firebase("https://dazzling-heat-6981.firebaseio.com/");
    }

    public Firebase getFirebase() {
        return firebaseRef;
    }
}
