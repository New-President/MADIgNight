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

        ArrayList<AboutUs> usrarry = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (i == 0) {
                usrarry.add(new AboutUs("Lee Wee Kang",""));
            }
            else if (i == 1) {
                usrarry.add(new AboutUs("Yong Zi Ren",""));
            }
            else if (i == 2) {
                usrarry.add(new AboutUs("Lim Long Teck",""));
            }
            else if (i == 3) {
                usrarry.add(new AboutUs("Han Xihe",""));
            }
            else if (i == 4) {
                usrarry.add(new AboutUs("Keefe Choong Wenkai",""));
            }
        }

        RecyclerView rv = findViewById(R.id.about_view);
        AboutUsAdapter adapter = new AboutUsAdapter(usrarry,AboutUsActivity.this);
        LinearLayoutManager layout = new LinearLayoutManager(this);

        rv.setAdapter(adapter);
        rv.setLayoutManager(layout);

        ImageButton back_main = findViewById(R.id.aboutus_back_to_main);
        back_main.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent aboutus_to_main = new Intent(AboutUsActivity.this,MainMenuActivity.class);
                startActivity(aboutus_to_main);
            }
        });
    }
}