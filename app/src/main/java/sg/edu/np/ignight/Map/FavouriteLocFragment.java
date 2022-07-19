package sg.edu.np.ignight.Map;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class FavouriteLocFragment extends Fragment {
    private ArrayList<LocationObject> locationsList;
    private FirebaseDatabase firebaseDatabase;
    private String uid;
    private ArrayList<String> favLocationNames;
    private ArrayList<LocationObject> filteredLocs;
    private MapAdapter mapAdapter;
//    public FavouriteLocFragment(ArrayList<LocationObject> locList){
//        filteredLocList = locList;
//    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        uid = FirebaseAuth.getInstance().getUid();
        firebaseDatabase = FirebaseDatabase.getInstance();

        locationsList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_all_loc, container, false);

        getLocationsList();
        getUserFavLoc();
        initRecyclerView(view);

        // Inflate the layout for this fragment
        return view;
    }

    public void initRecyclerView(View view){
        RecyclerView recyclerView = view.findViewById(R.id.allLocRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        mapAdapter = new MapAdapter(filteredLocs, view.getContext());
        recyclerView.setAdapter(mapAdapter);
    }
    public void getUserFavLoc(){
        DatabaseReference favLocationsReference = firebaseDatabase.getReference("user")
                .child(uid).child("Favourite Locations");
        favLocationNames = new ArrayList<>();
        filteredLocs = new ArrayList<>();
        favLocationsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot locNode : snapshot.getChildren()) {
                    if ((boolean) locNode.getValue()){
                        String name = locNode.getKey();
                        favLocationNames.add(name);
                        for (LocationObject loc: locationsList){
                            if (loc.getName().equals(name)){
                                filteredLocs.add(loc);
                            }
                        }
                        mapAdapter.notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                for (DataSnapshot locNode : snapshot.getChildren()) {
                    Log.d("snapshotKey", "snapshot: " + snapshot.getKey());
                    if ((boolean) locNode.getValue() && !favLocationNames.contains(locNode.getKey())){
                        favLocationNames.add(locNode.getKey());

                        for (LocationObject loc: locationsList){
                            if(loc.getName().equals(locNode.getKey())){
                                Log.d("AddedLoc", "onChildChanged: " + locNode.getKey());
                                filteredLocs.add(loc);
                            }
                        }

                        mapAdapter.notifyDataSetChanged();
                    }
                    else if (!(boolean) locNode.getValue()){
                        favLocationNames.remove(locNode.getKey());
                        for (LocationObject loc: locationsList){
                            if(loc.getName().equals(locNode.getKey())){
                                Log.d("RemovedLoc", "onChildChanged: " + locNode.getKey());
                                filteredLocs.remove(loc);
                            }
                        }

                        mapAdapter.notifyDataSetChanged();
                    }

                }

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
    public void getLocationsList(){

        DatabaseReference locationsReference = firebaseDatabase.getReference("location");

        locationsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot locNode : snapshot.getChildren()) {
                    String Name = locNode.child("Name").getValue().toString();
                    String Desc = locNode.child("Description").getValue().toString();
                    String Addr = locNode.child("Address").getValue().toString();
                    String imgUri = locNode.child("imgUri").getValue().toString();
                    locationsList.add(new LocationObject(Name, Desc, snapshot.getKey(), Addr, imgUri));


                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot snapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


    }
}