package sg.edu.np.ignight;

import java.util.Date;

public class Comment {
    private final UserAccount user;
    private final String comment;
    private final Date timestamp;
    private int likes;

    public UserAccount getUser() {
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

    public Comment(UserAccount user, String comment, Date timestamp, int likes){
        this.user = user;
        this.comment = comment;
        this.timestamp = timestamp;
        this.likes = likes;
    }
}
