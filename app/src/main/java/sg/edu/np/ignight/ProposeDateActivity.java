package sg.edu.np.ignight;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.View;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.Calendar;

import sg.edu.np.ignight.Chat.ChatListAdapter;

public class ProposeDateActivity extends AppCompatActivity {

    private ImageView backButton;
    private Button calendarButton, proposeDateButton;
    private EditText mainActivityText, locationText;
    private DatePicker date;
    private TimePicker time;

    private final ArrayList<String> invalidList = new ArrayList<>();

    Context context = this;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_date);

        // initializing the different Button, ImageView etc.
        backButton = findViewById(R.id.proposeDateBackBtn);
        calendarButton = findViewById(R.id.calendarButton);
        /*createButton = findViewById(R.id.createButton);*/
        proposeDateButton = findViewById(R.id.sendDateButton);
        mainActivityText = findViewById(R.id.mainActivityEditText);
        locationText = findViewById(R.id.locationEditText);
        date = findViewById(R.id.editTextDateProposeDate);
        time = findViewById(R.id.editTextTimeProposeDate);

        // Back button to navigate back to chat activity
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(getApplicationContext(), ChatActivity.class);
                back.putExtra("dateMessage", false);
                setResult(1000, back);
                finish();
            }
        });

        // Open up google calendar for the user to see his/her schedule
        calendarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Uri calendarUri = CalendarContract.CONTENT_URI
                        .buildUpon()
                        .appendPath("time")
                        .build();
                startActivity(new Intent(Intent.ACTION_VIEW, calendarUri));

            }
        });

        // After clicking on the propose date button:
        proposeDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Retrieving the user's input from the description and location field.
                String description = mainActivityText.getText().toString();
                String location = locationText.getText().toString();

                // If the input fields are not empty
                if (validateFields()){
                    Calendar beginTime = Calendar.getInstance();
                    // set the date and time according to the user's input
                    beginTime.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getHour(), time.getMinute());
                    // Change the date and time into milliseconds
                    long startMillis = beginTime.getTimeInMillis();

                    Calendar endTime = Calendar.getInstance();
                    // Set the time to the end of the day the user had inputted, assuming the date would last the whole day, starting from their inputted time.
                    endTime.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59);
                    // Change the date and time into milliseconds
                    long endMillis = endTime.getTimeInMillis();

                    Intent insertCalendarIntent = new Intent(Intent.ACTION_INSERT);
                    // Set the intent data for calendar
                    insertCalendarIntent.setData(CalendarContract.Events.CONTENT_URI);
                    // initializing the data for the calendar
                    // Set the title for the calendar event to "DATE"
                    insertCalendarIntent.putExtra(CalendarContract.Events.TITLE, "Date")
                            // Set the value for event all day to be false, as we might not know when the user want the date to start
                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, location) // Location
                            .putExtra(CalendarContract.Events.DESCRIPTION, description) // Description
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis) //Start Date and Time
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis) //End date and Time
                            .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE) // The calendar event is private
                            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY); // Indicate that the event would make the user busy
                    //Start the calendar activity on the user's screen
                    // The user would have the above inputted fields filled up for them
                    startActivity(insertCalendarIntent);

                    // pass the user's inputted data back into Chatactivity to display in the chat
                    Intent dateMessage = new Intent(getApplicationContext(), ChatActivity.class);
                    dateMessage.putExtra("dateMessage", true);
                    dateMessage.putExtra("dateDescription", description);
                    dateMessage.putExtra("dateLocation", location);
                    dateMessage.putExtra("startDateTime", startMillis);
                    dateMessage.putExtra("endDateTime", endMillis);
                    setResult(1000, dateMessage);

                    /*Intent toChat = new Intent(getApplicationContext(), ChatListAdapter.class);
                    toChat.putExtra("fromProposeDate", true);
                    startActivity(toChat);
*/
                    finish();

                }else{
                    // When the user did not fill up all the input fields, display a alert dialog
                    AlertDialog.Builder alert = new AlertDialog.Builder(ProposeDateActivity.this);
                    alert.setTitle("Invalid Inputs");
                    String message = "";
                    // Put the location of the invalid fields
                    for (int i = 0; i < invalidList.size(); i++) {
                        if (i > 0) {
                            message = message + ", " + invalidList.get(i);
                        } else {
                            message = invalidList.get(i);
                        }
                    }
                    alert.setMessage(message);
                    alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                        }
                    });
                    alert.show();

                    invalidList.clear();

                }
            }
        });
    }

    // Ensure that the user have filled up all the input fields
    private Boolean validateFields(){
        String description = mainActivityText.getText().toString();
        String location = locationText.getText().toString();

        int invalidFieldCount = 0;

        if (description.equals("")){
            invalidList.add("Main Activity");
            invalidFieldCount++;
        }

        if (location.equals("")){
            invalidList.add("Location");
            invalidFieldCount++;
        }
        return (invalidFieldCount == 0);
    };
}