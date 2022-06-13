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

    public Blog(String u, String desc, String dev, String loc, int likes, int com, String iUrl, String pUrl){
        username = u;
        description = desc;
        device = dev;
        location = loc;
        this.likes = likes;
        comments = com;
        imgUrl = iUrl;
        profileUrl = pUrl;
    }
}
