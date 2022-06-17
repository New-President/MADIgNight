package sg.edu.np.ignight;

import android.widget.ImageButton;

public class Blog {
    public String username;
    public String description;
    public String device;
    public String location;
    public int likes;
    public int comments;
    public String imgUrl;
    public String profileUrl;

    public Blog(String username, String description, String device, String location, int likes, int comments, String imgUrl, String profileUrl){
        this.username = username;
        this.description = description;
        this.device = device;
        this.location = location;
        this.likes = likes;
        this.comments = comments;
        this.imgUrl = imgUrl;
        this.profileUrl = profileUrl;
    }

    public Blog(){ }
}
