package sg.edu.np.ignight;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;

import sg.edu.np.ignight.Menu.MainMenuAdapter;
import sg.edu.np.ignight.Menu.SearchUserCallBack;
import sg.edu.np.ignight.Objects.UserObject;

public class Homepage_fragment extends Fragment {

    private RecyclerView userRV;
    private MainMenuAdapter userListAdapter;
    private LinearLayoutManager userLayoutManager;

    private ArrayList<UserObject> userList;

    // callback to sort userList based on query
    public SearchUserCallBack searchUserCallBack = new SearchUserCallBack() {
        @Override
        public void onQueryChanged() {
            Log.v("homepage frag", "query changed");
            Collections.sort(userList);
            userListAdapter.notifyDataSetChanged();
        }
    };

    private String preferredGender, preferredRelationship;

    public Homepage_fragment() {
    }

    //get the current user's UID
    String Uid = FirebaseAuth.getInstance().getUid();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_homepage_fragment, container, false);

        userList = new ArrayList<>();

        getUserList();
        initRecyclerView(view);
        return view;
    }

    private void initRecyclerView(View view) {
        userRV = view.findViewById(R.id.mainMenuUserList);
        userLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext()) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        userLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        userListAdapter = new MainMenuAdapter(getActivity().getApplicationContext(), userList, userLayoutManager);

        userRV.setAdapter(userListAdapter);
        userRV.setLayoutManager(userLayoutManager);
    }

    // get list of users to display
    private void getUserList() {
        // Saving to Firebase database
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/");
        // getting the child user
        DatabaseReference userDB = database.getReference("user");

        userDB.child(Uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get the user's Gender preference
                preferredGender = snapshot.child("Gender Preference").getValue(String.class);
                preferredRelationship = snapshot.child("Relationship Preference").getValue(String.class);

                userDB.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                                boolean exists = false;

                                if (!childSnapshot.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                                    for (UserObject existingUser : userList) {
                                        if (childSnapshot.getKey().equals(existingUser.getUid())) {
                                            exists = true;
                                            break;
                                        }
                                    }

                                    if (exists || childSnapshot.child("profileCreated").getValue().toString().equals("false")) {
                                        continue;
                                    }

                                    String uid = childSnapshot.getKey();
                                    ArrayList<String> dateLocList = new ArrayList<>();
                                    ArrayList<String> interestList = new ArrayList<>();

                                    String phone = childSnapshot.child("phone").getValue().toString();
                                    String aboutMe = childSnapshot.child("About Me").getValue().toString();
                                    String gender = childSnapshot.child("Gender").getValue().toString();
                                    String genderPref = childSnapshot.child("Gender Preference").getValue().toString();
                                    String profilePicUrl = childSnapshot.child("profileUrl").getValue().toString();
                                    String relationshipPref = childSnapshot.child("Relationship Preference").getValue().toString();
                                    String username = childSnapshot.child("username").getValue().toString();
                                    String profileCreated = childSnapshot.child("profileCreated").getValue().toString();
                                    int age = Integer.parseInt(childSnapshot.child("Age").getValue().toString());

                                    for (DataSnapshot dateLocSnapshot : childSnapshot.child("Date Location").getChildren()) {
                                        dateLocList.add(dateLocSnapshot.getValue().toString());
                                    }
                                    for (DataSnapshot interestSnapshot : childSnapshot.child("Interest").getChildren()) {
                                        interestList.add(interestSnapshot.getValue().toString());
                                    }

                                    UserObject user = new UserObject(uid, aboutMe, age, dateLocList, gender, genderPref, interestList, profilePicUrl, relationshipPref, phone, profileCreated, username);

                                    if (user.getGender().equals(preferredGender) || user.getRelationshipPref().equals(preferredRelationship)){
                                        userList.add(user);
                                        userListAdapter.notifyDataSetChanged();
                                    }
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "onCancelled: " + error.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCancelled: " + error.getMessage());
            }
        });
    }

    public SearchUserCallBack getCallBack() {
        return this.searchUserCallBack;
    }
}