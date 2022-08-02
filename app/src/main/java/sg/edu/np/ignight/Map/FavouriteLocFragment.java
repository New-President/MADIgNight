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

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class FavouriteLocFragment extends Fragment {
    private ArrayList<LocationObject> allLocationsList;
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

        allLocationsList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_all_loc, container, false);

        initRecyclerView(view);
        getLocationsList();
        getUserFavLoc();

        // Inflate the layout for this fragment
        return view;
    }

    public void initRecyclerView(View view){
        filteredLocs = new ArrayList<>();
        RecyclerView recyclerView = view.findViewById(R.id.allLocRecyclerView);
        mapAdapter = new MapAdapter(filteredLocs, view.getContext());
        recyclerView.setAdapter(mapAdapter);
        LinearLayoutManager mapLayoutManager = new LinearLayoutManager(view.getContext());
        mapLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(mapLayoutManager);
    }
    public void getUserFavLoc(){
        DatabaseReference favLocationsReference = firebaseDatabase.getReference("user")
                .child(uid).child("Favourite Locations");
        favLocationNames = new ArrayList<>();

        favLocationsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                // Filters off the locations with user's liked preferences stored in a string array
                // , stored into filteredlocations list
                for (DataSnapshot locNode : snapshot.getChildren()) {
                    if ((boolean) locNode.getValue()){
                        String name = locNode.getKey();
                        favLocationNames.add(name);
                        for (LocationObject loc: allLocationsList){
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
                // Updates the location list if user likes/unlikes a location
                for (DataSnapshot locNode : snapshot.getChildren()) {
                    // if location preference is now set to true and is already stored in the favLocationNames list
                    // , location name is added into favLocationNames and the location with the same name is extracted
                    // from the allLocationsList and stored in filteredLocs
                    if ((boolean) locNode.getValue() && !favLocationNames.contains(locNode.getKey())){
                        favLocationNames.add(locNode.getKey());

                        for (LocationObject loc: allLocationsList){
                            if(loc.getName().equals(locNode.getKey())){
                                filteredLocs.add(loc);

                                Log.d("Added", locNode.getKey());
                            }
                        }
                    }
                    // if not specified, all location names will be removed under the same category (can be true but stored in list)
                    else if (!(boolean) locNode.getValue() && favLocationNames.contains(locNode.getKey())){
                        favLocationNames.remove(locNode.getKey());

                        for (LocationObject loc: allLocationsList){
                            if(loc.getName().equals(locNode.getKey())){
                                filteredLocs.remove(loc);

                                Log.d("Removed", loc.getName());

                            }
                        }
                    }
                    for (LocationObject loc:filteredLocs){
                        Log.d("Locations", loc.getName());
                    }

                    mapAdapter.notifyDataSetChanged();
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

        // Stores *all* locations kept in Firebase in a Location list
        locationsReference.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                for (DataSnapshot locNode : snapshot.getChildren()) {
                    String Name = locNode.child("Name").getValue().toString();
                    String Desc = locNode.child("Description").getValue().toString();
                    String Addr = locNode.child("Address").getValue().toString();
                    String imgUri = locNode.child("imgUri").getValue().toString();
                    allLocationsList.add(new LocationObject(Name, Desc, snapshot.getKey(), Addr, imgUri));
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