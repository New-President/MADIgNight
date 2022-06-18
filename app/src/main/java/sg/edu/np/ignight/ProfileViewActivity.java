package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import java.util.ArrayList;

public class ProfileViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_view);

        ArrayList<User> data = new ArrayList<>(); // placeholder
        // insert profile interest data here


        // Interest data from the database will be extracted.
        // Interests are stored as children



        RecyclerView rv = findViewById(R.id.recyclerView);
        ProfileViewInterestsAdapter adapter = new ProfileViewInterestsAdapter(data);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.HORIZONTAL);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);

        // add friend and ignight intent things here

    }
}