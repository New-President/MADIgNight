package sg.edu.np.ignight.Objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

// object to manipulate formatting of Date objects
public class TimestampObject {

    private Date timestamp;

    // takes in the default Date().toString() format and stores as Date
    public TimestampObject(String timestamp) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        this.timestamp = df.parse(timestamp);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    // returns the date portion of timestamp
    public String getDate() {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timestamp);
    }

    // returns the time portion of timestamp
    public String getTime() {
        DateFormat df = new SimpleDateFormat("hh:mm a");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timestamp);
    }

    // returns date and time of timestamp
    public String getDateTime() {
        return getDate() + " " + getTime();
    }

    // returns default Date().toString() format
    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        return df.format(timestamp);
    }
}
