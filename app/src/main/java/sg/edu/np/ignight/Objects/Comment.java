package sg.edu.np.ignight.Objects;

import java.util.ArrayList;

public class Comment {
    private String commentID;
    private String uid;
    private String username;
    private String profUrl;
    private String content;
    private String timestamp;
    private ArrayList<String> likedUsersList;
    private int likes;

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

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public ArrayList<String> getLikedUsersList() {
        return likedUsersList;
    }

    public void setLikedUsersList(ArrayList<String> likedUsersList) {
        this.likedUsersList = likedUsersList;
    }

    public Comment(String commentID, String uid, String username, String profUrl, String content, String timestamp, ArrayList<String> likedUsers, int likes){
        this.commentID = commentID;
        this.uid = uid;
        this.username = username;
        this.profUrl = profUrl;
        this.content = content;
        this.timestamp = timestamp;
        this.likedUsersList = likedUsers;
        this.likes = likes;
    }
}
