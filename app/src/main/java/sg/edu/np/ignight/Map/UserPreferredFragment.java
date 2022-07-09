package sg.edu.np.ignight.Map;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import sg.edu.np.ignight.R;

public class UserPreferredFragment extends Fragment {
    private ArrayList<LocationObject> locList;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_user_preferred, container, false);

        locList = new ArrayList<>();
        locList.add(new LocationObject("test", "this is test loc", "Mall", ""));
        locList.add(new LocationObject("test2", "this is test loc2", "Park", ""));
        locList.add(new LocationObject("test3", "this is test loc3", "Amuse", ""));
        locList.add(new LocationObject("test4", "this is test loc4", "Cafe", ""));

        RecyclerView recyclerView = view.findViewById(R.id.userPrefRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(view.getContext()));
        recyclerView.setAdapter(new MapAdapter(locList, view.getContext()));

        return view;
    }


}