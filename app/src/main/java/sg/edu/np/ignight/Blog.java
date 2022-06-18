package sg.edu.np.ignight;

import java.util.ArrayList;

public class Blog {
    public String description;
    public String location;
    public int likes;
    public int comments;
    public ArrayList<String> commentsList;
    public String imgUri;

    public Blog(String description, String location, String imgUrl){
        this.description = description;
        this.location = location;
        this.imgUri = imgUrl;
        this.likes = 0;
        this.comments = 0;
    }

    public Blog(){ }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

    public ArrayList<String> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(ArrayList<String> commentsList) {
        this.commentsList = commentsList;
    }
}
