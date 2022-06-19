package sg.edu.np.ignight.Objects;

import java.util.ArrayList;

public class BlogObject {
    public String description;
    public String location;
    public int likes;
    public int comments;
    public boolean liked;
    public ArrayList<String> commentsList;
    public String imgID;

    public BlogObject(String description, String location, String imgID){
        this.description = description;
        this.location = location;
        this.imgID = imgID;
        this.likes = 0;
        this.comments = 0;
        this.liked = false;
        this.commentsList = new ArrayList<>();
    }

    public BlogObject(){ }

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
        return imgID;
    }

    public void setImgUri(String imgUri) {
        this.imgID = imgUri;
    }

    public ArrayList<String> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(ArrayList<String> commentsList) {
        this.commentsList = commentsList;
    }

}
