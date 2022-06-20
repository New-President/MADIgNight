package sg.edu.np.ignight.Objects;

import java.util.ArrayList;

public class BlogObject {
    public String description, location, imgID, blogID;
    public int likes;
    public int comments;
    public boolean liked;
    public ArrayList<String> commentsList;

    public BlogObject(String description, String location, String imgID, String blogID){
        this.description = description;
        this.location = location;
        this.imgID = imgID;
        this.blogID = blogID;
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

    public String getImgID() {
        return imgID;
    }

    public void setImgID(String imgUri) {
        this.imgID = imgUri;
    }

    public ArrayList<String> getCommentsList() {
        return commentsList;
    }

    public void setCommentsList(ArrayList<String> commentsList) {
        this.commentsList = commentsList;
    }

    public String getBlogID() {
        return blogID;
    }

    public void setBlogID(String blogID) {
        this.blogID = blogID;
    }

    public int getLikes() {
        return likes;
    }

    public void setLikes(int likes) {
        this.likes = likes;
    }

    public int getComments() {
        return comments;
    }

    public void setComments(int comments) {
        this.comments = comments;
    }

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }
}
