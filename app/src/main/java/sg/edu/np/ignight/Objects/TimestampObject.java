package sg.edu.np.ignight.Objects;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimestampObject {

    private Date timestamp;

    public TimestampObject(String timestamp) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        this.timestamp = df.parse(timestamp);
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getDate() {
        DateFormat df = new SimpleDateFormat("dd MMM yyyy");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timestamp);
    }

    public String getTime() {
        DateFormat df = new SimpleDateFormat("hh:mm a");
        df.setTimeZone(TimeZone.getDefault());
        return df.format(timestamp);
    }

    public String getDateTime() {
        return getDate() + " " + getTime();
    }

    @Override
    public String toString() {
        SimpleDateFormat df = new SimpleDateFormat("E MMM dd HH:mm:ss z yyyy");
        return df.format(timestamp);
    }

    public boolean earlierThan(TimestampObject timestampObject) {
        return this.timestamp.compareTo(timestampObject.getTimestamp()) < 0;
    }
}
