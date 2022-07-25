package sg.edu.np.ignight;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ViewProfileCustomisation extends AppCompatActivity implements AdapterView.OnItemSelectedListener {

    private ImageButton backButton3;
    private boolean customiseProfileBackground;
    private boolean customiseProfileFont;
    private boolean customiseProfileAccentTheme;
    private Spinner fontSpinner, accentThemeSpinner ;

    // Spinner options for fonts
    private String[] fontDropdownOptions = {"Amaranth", "Cormorant", "Poppins", "Ropa", "Square Peg"};
    private String[] accentThemeDropdownOptions = {"Green", "IgNight Yellow", "Black", "Purple",
            "Blue", "Red", "Brown"};

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

        // Init spinner for font change
        // Font spinner drop down list
        fontSpinner = (Spinner)findViewById(R.id.FontDropdown);
        ArrayAdapter<String>fontArrayAdapter = new ArrayAdapter<String>(
                ViewProfileCustomisation.this,
                android.R.layout.simple_spinner_item,
                fontDropdownOptions);

        fontArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fontSpinner.setAdapter(fontArrayAdapter);
        fontSpinner.setOnItemSelectedListener(this);






        // - Change the accent theme of their own profile
        accentThemeSpinner = (Spinner) findViewById(R.id.ChangeAccentThemeDropdown1);
        ArrayAdapter<String>accentThemeArrayAdapter = new ArrayAdapter<String>(
                ViewProfileCustomisation.this,
                android.R.layout.simple_spinner_item,
                accentThemeDropdownOptions);

        accentThemeArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        accentThemeSpinner.setAdapter(accentThemeArrayAdapter);
        accentThemeSpinner.setOnItemSelectedListener(this);
    }

    // For spinner dropdown lists
    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
        String text = adapterView.getItemAtPosition(position).toString();
        Toast.makeText(adapterView.getContext(), text, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {
        // None
    }
}