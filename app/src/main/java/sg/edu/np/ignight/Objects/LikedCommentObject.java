package sg.edu.np.ignight.Objects;

import java.util.ArrayList;

public class LikedCommentObject {
    public String userUID, content, imgID;
    public Boolean liked;

    public LikedCommentObject(String userUID, String imgID, Boolean liked, String content){
        this.userUID = userUID;
        this.imgID = imgID;
        this.liked = liked;
        this.content = content;
    }


}
