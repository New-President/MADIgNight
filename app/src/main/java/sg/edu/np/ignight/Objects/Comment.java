package sg.edu.np.ignight.Objects;

import java.util.Date;

public class Comment {
    private final UserObject user;
    private final String comment;
    private final Date timestamp;
    private int likes;

    public UserObject getUser() {
        return user;
    }

    public String getComment() {
        return comment;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public Comment(UserObject user, String comment, Date timestamp, int likes){
        this.user = user;
        this.comment = comment;
        this.timestamp = timestamp;
        this.likes = likes;
    }
}
