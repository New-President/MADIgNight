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
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sg.edu.np.ignight.Menu.MainMenuAdapter;
import sg.edu.np.ignight.Objects.UserObject;

public class Homepage_fragment extends Fragment {

    private RecyclerView userRV;
    private MainMenuAdapter userListAdapter;
    private LinearLayoutManager userLayoutManager;

    private ArrayList<UserObject> userList;


    public Homepage_fragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_homepage_fragment, container, false);

        userList = new ArrayList<>();

        getUserList();
        initRecyclerView(view);

        ImageView profile = view.findViewById(R.id.ownerprofile_menu); //display slide menu
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

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

    private void getUserList() {
        DatabaseReference userDB = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("user");
        userDB.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot childSnapshot : snapshot.getChildren()) {

                        String uid = FirebaseAuth.getInstance().getUid();
                        boolean exists = false;

                        if (!childSnapshot.getKey().equals(uid)) {

                            for (UserObject existingUser : userList) {
                                if (childSnapshot.getKey().equals(existingUser.getUid())) {
                                    exists = true;
                                    break;
                                }
                            }

                            if (exists) {
                                continue;
                            }

                            String phone = "";
                            String email = "";
                            String aboutMe = "";
                            String gender = "";
                            String genderPref = "";
                            String profilePicUrl = "";
                            String relationshipPref = "";
                            String username = "";
                            int age = 0;
                            ArrayList<String> dateLocList = new ArrayList<>();
                            ArrayList<String> interestList = new ArrayList<>();

                            if (childSnapshot.child("phone").getValue() != null) {
                                phone = childSnapshot.child("phone").getValue().toString();
                            }
                            if (childSnapshot.child("email").getValue() != null) {
                                email = childSnapshot.child("email").getValue().toString();
                            }
                            if (childSnapshot.child("About Me").getValue() != null) {
                                aboutMe = childSnapshot.child("About Me").getValue().toString();
                            }
                            if (childSnapshot.child("Gender").getValue() != null) {
                                gender = childSnapshot.child("Gender").getValue().toString();
                            }
                            if (childSnapshot.child("Gender Preference").getValue() != null) {
                                genderPref = childSnapshot.child("Gender Preference").getValue().toString();
                            }
                            if (childSnapshot.child("Profile Picture").getValue() != null) {
                                profilePicUrl = childSnapshot.child("Profile Picture").getValue().toString();
                            }
                            if (childSnapshot.child("Relationship Preference").getValue() != null) {
                                relationshipPref = childSnapshot.child("Relationship Preference").getValue().toString();
                            }
                            if (childSnapshot.child("username").getValue() != null) {
                                username = childSnapshot.child("username").getValue().toString();
                            }
                            if (childSnapshot.child("Age").getValue() != null) {
                                age = Integer.parseInt(childSnapshot.child("Age").getValue().toString());
                            }
                            if (childSnapshot.child("Date Location").hasChildren()) {
                                for (DataSnapshot dateLocSnapshot : childSnapshot.child("Date Location").getChildren()) {
                                    dateLocList.add(dateLocSnapshot.getKey());
                                }
                            }
                            if (childSnapshot.child("Interest").hasChildren()) {
                                for (DataSnapshot interestSnapshot : childSnapshot.child("Interest").getChildren()) {
                                    interestList.add(interestSnapshot.getKey());
                                }
                            }

                            UserObject user = new UserObject(uid, email, phone, username, gender, aboutMe, interestList, relationshipPref, genderPref, dateLocList, profilePicUrl, age);

                            userList.add(user);
                            userListAdapter.notifyDataSetChanged();

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
}