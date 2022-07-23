package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;

import java.util.ArrayList;
import java.util.Random;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ArrayList<AboutUs> userList = new ArrayList<AboutUs>() {
            {
                // add description
                add(new AboutUs("Lee Wee Kang",""));
                add(new AboutUs("Yong Zi Ren",""));
                add(new AboutUs("Lim Long Teck",""));
                add(new AboutUs("Han Xihe",""));
                add(new AboutUs("Keefe Cheong Wenkai",""));
            }
        };

        RecyclerView rv = findViewById(R.id.about_view);
        rv.setNestedScrollingEnabled(false);
        AboutUsAdapter adapter = new AboutUsAdapter(userList,AboutUsActivity.this);
        LinearLayoutManager layout = new LinearLayoutManager(this);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);

        ImageButton back_main = findViewById(R.id.aboutus_back_to_main);
        back_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutus_to_main = new Intent(AboutUsActivity.this,MainMenuActivity.class);
                aboutus_to_main.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(aboutus_to_main);
                finish();
            }
        });
    }
}