package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

public class MainMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        Button home = findViewById(R.id.home_menu);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        Button chat = findViewById(R.id.chat_menu);
        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
        ImageView profile = findViewById(R.id.ownerprofile_menu);
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