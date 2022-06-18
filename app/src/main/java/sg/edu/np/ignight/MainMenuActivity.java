package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {

    // Edit profile, Logout, about page, stage 2: map, paywalls, terms & conditions??
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button home = findViewById(R.id.home_menu);// go back to home menu
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frameLayout_menu, new Homepage_fragment());
                ft.commit();
            }
        });
        Button chat = findViewById(R.id.chat_menu);// list of chats with other people (Use fragment view)
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
                //ft.replace(R.id. , new ());
                //ft.commit();
            }
        });
        ImageView profile = findViewById(R.id.ownerprofile_menu); //display slide menu
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ArrayList<User> data = new ArrayList<>();

        RecyclerView rv = findViewById(R.id.recyclerView2);
        MainMenuAdapter adapter = new MainMenuAdapter(MainMenuActivity.this, data);
        LinearLayoutManager layout = new LinearLayoutManager(this);
        layout.setOrientation(LinearLayoutManager.VERTICAL);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);
    }
}