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

public class AllLocFragment extends Fragment {
    private ArrayList<LocationObject> locList;
//    public AllLocFragment(ArrayList<LocationObject> locList){
//        this.locList = locList;
//
//    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference("location");
        locList = new ArrayList<>();

        View view = inflater.inflate(R.layout.fragment_all_loc, container, false);

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot categoryNode : snapshot.getChildren()) {
                    for (DataSnapshot locNode : categoryNode.getChildren()) {
                        String Name = locNode.child("Name").getValue().toString();
                        String Desc = locNode.child("Description").getValue().toString();
                        String Addr = locNode.child("Address").getValue().toString();
                        String imgUri = locNode.child("imgUri").getValue().toString();

                        locList.add(new LocationObject(Name, Desc, categoryNode.getKey(), Addr, imgUri));

                    }
                    RecyclerView recyclerView = view.findViewById(R.id.allLocRecyclerView);
                    recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
                    recyclerView.setAdapter(new MapAdapter(locList, view.getContext()));

                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        // Inflate the layout for this fragment
        return view;
    }
}