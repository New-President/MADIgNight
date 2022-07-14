package sg.edu.np.ignight.Objects;

import java.util.Date;

public class Comment {
    private UserObject user;
    private String content;
    private TimestampObject timestamp;
    private int likes;

    public UserObject getUser() {
        return user;
    }

    public String getContent() {
        return content;
    }

    public TimestampObject getTimestamp() {
        return timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Comment(UserObject user, String content, TimestampObject timestamp, int likes){
        this.user = user;
        this.content = content;
        this.timestamp = timestamp;
        this.likes = likes;
    }
}
