package com.wfl.application;

import android.util.Log;

import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;

public class UserDAO {

    private String LOG_TAG = "UserDAO";
    private Firebase myRef = MainDAO.getInstance().getFirebase().child("users");

    public UserDAO() {}

    public void addUser(String uid, UserModel user) {
        myRef.child(uid).setValue(user, new Firebase.CompletionListener() {
            @Override
            public void onComplete(FirebaseError firebaseError, Firebase firebase) {
                if (firebaseError != null) {
                    Log.e(LOG_TAG, "Saving user data error " + firebaseError.getMessage());
                }
            }
        });
    }

    //Just return reference for now since we can't return model
    public Firebase getUserRef(String uid) {
        return myRef.child(uid);
    }


    //Todo: how do we implement this? cause it needs to be async.
    /*
    public UserModel getUser(String uid) {
        myRef.child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserModel user = dataSnapshot.getValue(UserModel.class);
                return user;
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(LOG_TAG, "User Data retrieval failed - " + firebaseError.getMessage());
            }
        });
    }
    */

}
