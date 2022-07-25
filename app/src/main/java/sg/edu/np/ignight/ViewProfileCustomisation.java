package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

public class ViewProfileCustomisation extends AppCompatActivity {

    private ImageButton backButton3;
    private boolean customiseProfileBackground;
    private boolean customiseProfileFont;
    private boolean customiseProfileAccentTheme;

    String[] FontDropdownOptions = {"Amaranth", "Cormorant", "Poppins", "Ropa", "Square Peg"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile_customisation);



        // Return back to main menu
        backButton3 = findViewById(R.id.backButton2);
        backButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent backToMainMenu = new Intent(getApplicationContext(), MainMenuActivity.class);
                backToMainMenu.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(backToMainMenu);
                finish();
            }
        });

        // There are three kinds of customisations for a user to personalize their own profile view:
        // - Change font used in profile view
        // - Change profile background to a video or photo
        // - Change the accent theme of their own profile


        // Init customisation fields


        // - Change font used in profile view
        // Set dropdown and its hints


    }
}