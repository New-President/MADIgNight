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


    final ContentValues event = new ContentValues();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_propose_date);

        backButton = findViewById(R.id.proposeDateBackBtn);
        calendarButton = findViewById(R.id.calendarButton);
        /*createButton = findViewById(R.id.createButton);*/
        proposeDateButton = findViewById(R.id.sendDateButton);
        mainActivityText = findViewById(R.id.mainActivityEditText);
        locationText = findViewById(R.id.locationEditText);
        date = findViewById(R.id.editTextDateProposeDate);
        time = findViewById(R.id.editTextTimeProposeDate);


        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent back = new Intent(getApplicationContext(), ChatActivity.class);
                back.putExtra("dateMessage", false);
                setResult(1000, back);
                finish();
            }
        });

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

        proposeDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String description = mainActivityText.getText().toString();
                String location = locationText.getText().toString();

                if (validateFields()){
                    Calendar beginTime = Calendar.getInstance();
                    beginTime.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), time.getHour(), time.getMinute());
                    long startMillis = beginTime.getTimeInMillis();
                    Calendar endTime = Calendar.getInstance();
                    endTime.set(date.getYear(), date.getMonth(), date.getDayOfMonth(), 23, 59);
                    long endMillis = endTime.getTimeInMillis();
                    Intent insertCalendarIntent = new Intent(Intent.ACTION_INSERT);
                    insertCalendarIntent.setData(CalendarContract.Events.CONTENT_URI);
                    insertCalendarIntent.putExtra(CalendarContract.Events.TITLE, "Date")
                            .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                            .putExtra(CalendarContract.Events.EVENT_LOCATION, location)
                            .putExtra(CalendarContract.Events.DESCRIPTION, description) // Description
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                            .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                            .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                    startActivity(insertCalendarIntent);

                    Intent dateMessage = new Intent(getApplicationContext(), ChatActivity.class);
                    dateMessage.putExtra("dateMessage", true);
                    dateMessage.putExtra("dateDescription", description);
                    dateMessage.putExtra("dateLocation", location);
                    dateMessage.putExtra("datetime", startMillis);
                    setResult(1000, dateMessage);

                    /*Intent toChat = new Intent(getApplicationContext(), ChatListAdapter.class);
                    toChat.putExtra("fromProposeDate", true);
                    startActivity(toChat);
*/
                    finish();

                }else{
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
        /*createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                *//*long calID = 3;
                long startMillis = 0;
                long endMillis = 0;
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(2022, 6, 19, 7, 30);
                startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(2022, 6, 19, 8, 45);
                endMillis = endTime.getTimeInMillis();

                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.EXTRA_EVENT_END_TIME, startMillis);
                values.put(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
                values.put(CalendarContract.Events.TITLE, "Jazzercise");
                values.put(CalendarContract.Events.DESCRIPTION, "Group workout");
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE, "America/Los_Angeles");
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

// get the event ID that is the last element in the Uri

                Intent back = new Intent(getApplicationContext(), ChatActivity.class);
                finish();*//*

                *//*Calendar beginTime = Calendar.getInstance();
                beginTime.set(2022, Calendar.JULY, 19, 3, 00);
                long startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(2012, Calendar.JULY, 19, 4, 00);
                long endMillis = endTime.getTimeInMillis();
                CalendarEvent evt = new CalendarEvent(1, "LOL", "FWERWF", "Singapore", (int)startMillis, (int)endMillis);
                addEvent(evt);*//*


                Calendar beginTime = Calendar.getInstance();
                beginTime.set(2022, 6, 19, 7, 30);
                long startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(2012, 6, 19, 8, 45);
                long endMillis = endTime.getTimeInMillis();
                Intent insertCalendarIntent = new Intent(Intent.ACTION_INSERT);
                insertCalendarIntent.setData(CalendarContract.Events.CONTENT_URI);
                insertCalendarIntent.putExtra(CalendarContract.Events.TITLE, "TITLE")
                        .putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, false)
                        .putExtra(CalendarContract.Events.EVENT_LOCATION, "Hong Kong")
                        .putExtra(CalendarContract.Events.DESCRIPTION, "DESCRIPTION") // Description
                        .putExtra(Intent.EXTRA_EMAIL, "fooInviteeOne@gmail.com,fooInviteeTwo@gmail.com")
                        .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                        .putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                        .putExtra(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE)
                        .putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE);
                startActivity(insertCalendarIntent);





                *//*long calID = 4;
                long startMillis = 0;
                long endMillis = 0;
                Calendar beginTime = Calendar.getInstance();
                beginTime.set(2022, 6, 18, 7, 30);// set(int year, int month, int day, int hourOfDay, int minute)
                startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(2022, 6, 19, 8, 30);
                endMillis = endTime.getTimeInMillis();

                TimeZone tz = TimeZone.getDefault();

                ContentResolver cr = getContentResolver();
                ContentValues values = new ContentValues();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
                values.put(CalendarContract.Events.TITLE, "Jazzercise");
                values.put(CalendarContract.Events.DESCRIPTION, "Group workout");
                values.put(CalendarContract.Events.CALENDAR_ID, calID);
                values.put(CalendarContract.Events.EVENT_TIMEZONE,  tz.getID());
                Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);

                // get the event ID that is the last element in the Uri
                long eventID = Long.parseLong(uri.getLastPathSegment());


                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                startActivity(intent);*//*






                *//*Calendar beginTime = Calendar.getInstance();
                beginTime.set(2022, Calendar.JULY, 19, 3, 00);
                long startMillis = beginTime.getTimeInMillis();
                Calendar endTime = Calendar.getInstance();
                endTime.set(2012, Calendar.JULY, 19, 4, 00);
                long endMillis = endTime.getTimeInMillis();


                event.put(CalendarContract.Events.CALENDAR_ID, 10001);
                event.put(CalendarContract.Events.TITLE, "Test Android");
                event.put(CalendarContract.Events.EVENT_LOCATION, "Test Location");
                event.put(CalendarContract.Events.DESCRIPTION, "Test Description Examples");

                event.put(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis);
                event.put(CalendarContract.EXTRA_EVENT_END_TIME, endMillis);
                event.put(CalendarContract.Events.ALL_DAY, false);
                event.put(CalendarContract.Events.EVENT_END_TIMEZONE, "Europe/London");

                String timeZone = TimeZone.getDefault().getID();
                event.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone);

                event.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
                event.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
                Uri baseUri = Uri.parse("content://com.android.calendar/events");
                if (Build.VERSION.SDK_INT >= 8) {
                    baseUri = Uri.parse("content://com.android.calendar/events");
                } else {
                    baseUri = Uri.parse("content://calendar/events");
                }

                context.getContentResolver().insert(baseUri, event);

                Intent back = new Intent(getApplicationContext(), ChatActivity.class);
                finish();*//*

                *//*Intent date = new Intent(Intent.ACTION_INSERT);
                date.setData(CalendarContract.Events.CONTENT_URI);
                date.putExtra(CalendarContract.Events.ALL_DAY, false);
                date.putExtra(CalendarContract.Events.EVENT_LOCATION, "ChinaTown, Singapore");
                String title = date.getStringExtra(CalendarContract.Events.TITLE);
                Log.e(TAG, "Hello: " + title);

                // see if there is a calendar app
                if(getIntent().resolveActivity(getPackageManager()) != null){
                    startActivityForResult(date, RESULT_CANCELED);
                }
                else{
                    Toast.makeText(ProposeDateActivity.this, "There is no app that can support this action", Toast.LENGTH_SHORT).show();
                }*//*
            }
        });*/

    }

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

    private void checkPermissions(int callbackId, String... permissionsId) {
        boolean permissions = true;
        for (String p : permissionsId) {
            permissions = permissions && ContextCompat.checkSelfPermission(this, p) == PERMISSION_GRANTED;
        }

        if (!permissions)
            ActivityCompat.requestPermissions(this, permissionsId, callbackId);
    }
}