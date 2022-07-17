package sg.edu.np.ignight.Map;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MapViewPagerAdapter extends FragmentStateAdapter {
    private ArrayList<LocationObject> locationObjectList;
    ArrayList<String> userPrefList;
    public MapViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        //this.locationObjectList = locationObjectList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        userPrefList = new ArrayList<>();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance("https://madignight-default-rtdb.asia-southeast1.firebasedatabase.app");
        DatabaseReference databaseReference = firebaseDatabase.getReference("user").child(uid).child("Date Location");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot keynode : snapshot.getChildren()) {
                    userPrefList.add(keynode.getValue().toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        switch (position){
            case (1):
                return new FavouriteLocFragment();
            case (2):
                return new AllLocFragment(locationObjectList);
            default:
                Log.d("prefsize", String.valueOf(userPrefList.size()));
                return new UserPreferredFragment(userPrefList, locationObjectList);

        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }

}
