package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class TNC extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tnc);
        Button goback = findViewById(R.id.goback_btn_tnc);
        goback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent tnc_to_main = new Intent(TNC.this,MainMenuActivity.class);
                startActivity(tnc_to_main);
            }
        });
    }
}