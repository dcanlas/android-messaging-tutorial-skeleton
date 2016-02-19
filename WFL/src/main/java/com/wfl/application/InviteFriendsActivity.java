package com.wfl.application;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
        //Clear the names array first
//        if (namesAdapter != null) {
//            namesAdapter.cleanup();
//        }
        inviteableListView.setAdapter(null);
        String name = friendSearch.getText().toString();
        namesAdapter = new FirebaseListAdapter<UserModel>(this,
                UserModel.class,
                R.layout.inviteable_user_list_item,
                userDAO.getRef().orderByChild("displayName").startAt(name.toLowerCase())) {
            @Override
            protected void populateView(View view, UserModel userModel, int i) {
                //FIXME: I need to check for ID here really. as there can be duplicate emails when testing?
                Log.i(LOG_TAG, "the user " + i + " is " + userModel.getName());
                if (!MainDAO.getCurrentUser().getEmail().equals(userModel.getEmail())) {
                    ((TextView) view.findViewById(R.id.inviteableUserName)).setText(userModel.getName());
                }
            }
        };
        inviteableListView.setAdapter(namesAdapter);
        /*
        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereStartsWith("username", name);
        query.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> userList, ParseException e) {
                if (e == null) {
                    for (int i = 0; i < userList.size(); i++) {
                        names.add(userList.get(i).getUsername().toString());
                    }
                    ListView inviteableListView = (ListView) findViewById(R.id.inviteableListView);
                    FriendListAdapter namesAdapter = new FriendListAdapter(names, getApplicationContext());
                    inviteableListView.setAdapter(namesAdapter);
                }
                else {
                    Toast.makeText(getApplicationContext(),
                            "Cannot find matching user",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        */
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        namesAdapter.cleanup();
    }
}
