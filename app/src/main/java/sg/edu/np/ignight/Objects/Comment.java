package sg.edu.np.ignight.Objects;

import java.util.Date;

public class Comment {
    private String commentID;
    private String uid;
    private String username;
    private String profUrl;
    private String content;
    private String timestamp;

    public String getContent() {
        return content;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public String getUid() {
        return uid;
    }

    public String getUsername() {
        return username;
    }

    public String getProfUrl() {
        return profUrl;
    }

    public String getCommentID() {
        return commentID;
    }

    public void setContent(String content) {
        this.content = content;
    }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Comment(String commentID, String uid, String username, String profUrl, String content, String timestamp){
        this.commentID = commentID;
        this.uid = uid;
        this.username = username;
        this.profUrl = profUrl;
        this.content = content;
        this.timestamp = timestamp;
    }
}
