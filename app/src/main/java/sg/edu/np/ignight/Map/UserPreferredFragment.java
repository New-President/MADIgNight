package sg.edu.np.ignight.Map;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class UserPreferredFragment extends Fragment {
    private ArrayList<LocationObject> locList;
    private ArrayList<String> userPrefList;

    public UserPreferredFragment(ArrayList<String> userPrefList){
        this.userPrefList = userPrefList;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference("location");
        locList = new ArrayList<>();

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_preferred, container, false);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot categoryNode: snapshot.getChildren()){
                    for (String loc: userPrefList){
                        Log.d("locationsstuff", loc);
                        if (categoryNode.getKey().equals(loc) && categoryNode.child("1").exists()){
                            for (DataSnapshot locNode : categoryNode.getChildren()){
                                String Name = locNode.child("Name").getValue().toString();
                                String Desc = locNode.child("Description").getValue().toString();
                                String imgUri = locNode.child("imgUri").getValue().toString();

                                locList.add(new LocationObject(Name, Desc, loc, imgUri));

                                Log.d("MapList", Name);
                            }
                        }
                    }

                }

                RecyclerView recyclerView = view.findViewById(R.id.userPrefRecyclerView);
                recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                recyclerView.setAdapter(new MapAdapter(locList, view.getContext()));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        return view;
    }

    public ArrayList<String> userPreferredLocList(){
        ArrayList<String> locStringList = new ArrayList<>();


        return locStringList;
    }


}