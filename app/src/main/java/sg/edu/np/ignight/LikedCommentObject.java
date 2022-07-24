package sg.edu.np.ignight;

import java.util.ArrayList;

public class LikedCommentObject {
    public String userUID, content;
    public Boolean liked;

    public LikedCommentObject(String userUID, Boolean liked, String content){
        this.userUID = userUID;
        this.liked = liked;
        this.content = content;
    }


}
