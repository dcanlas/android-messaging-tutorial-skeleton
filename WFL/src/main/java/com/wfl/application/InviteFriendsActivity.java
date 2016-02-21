package com.wfl.application;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.ui.FirebaseListAdapter;
import com.waffle.wfl.R;

import java.util.ArrayList;

public class InviteFriendsActivity extends Activity implements View.OnClickListener{

    ArrayList<String> names = new ArrayList<String>();

    private String LOG_TAG = "INVITE_ACTIVITY";

    private UserDAO userDAO;
    FirebaseListAdapter<UserModel> namesAdapter;
    private Button findButton;
    private EditText friendSearch;
    private ListView inviteableListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_friends);
        userDAO = new UserDAO();
        inviteableListView = (ListView) findViewById(R.id.inviteableListView);

        findButton = (Button) findViewById(R.id.findButton);
        friendSearch = (EditText) findViewById(R.id.friendSearch);

        findButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == findButton) {
            findUsers();
        }
    }

    public void findUsers() {
        names.clear();
        String name = friendSearch.getText().toString();
        userDAO.getRef().orderByChild("displayName").startAt(name.toLowerCase()).endAt(name.toLowerCase()+"\uf8ff")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot userSnapShot : dataSnapshot.getChildren()) {
                            UserModel user = userSnapShot.getValue(UserModel.class);
                            Log.i(LOG_TAG, "the user is " + user.getName());
                            if (!MainDAO.getCurrentUser().getEmail().equals(user.getEmail())) {
                                names.add(user.getName());
                            }
                        }
                        if (names.isEmpty()) {
                            Toast.makeText(getApplicationContext(),
                                    "No users found.", Toast.LENGTH_LONG).show();
                        }
                        FriendListAdapter namesAdapter = new FriendListAdapter(names, getApplicationContext());
                        inviteableListView.setAdapter(namesAdapter);
                    }

                    @Override
                    public void onCancelled(FirebaseError firebaseError) {
                        Toast.makeText(getApplicationContext(),
                                "Trying to find user error " + firebaseError.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        namesAdapter.cleanup();
    }
}
