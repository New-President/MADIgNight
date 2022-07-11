package sg.edu.np.ignight.Map;

import java.io.Serializable;

public class LocationObject implements Serializable {
    private String Name;
    private String Desc;
    private String Category;
    private String imgUri;

    public LocationObject(String Name, String Desc, String Category, String imgUri){
        this.Name = Name;
        this.Desc = Desc;
        this.Category = Category;
        this.imgUri = imgUri;
    }

    public String getName() {
        return Name;
    }

    public String getDesc() {
        return Desc;
    }

    public String getCategory() {
        return Category;
    }

    public String getImgUri() {
        return imgUri;
    }

    public void setImgUri(String imgUri) {
        this.imgUri = imgUri;
    }

}
