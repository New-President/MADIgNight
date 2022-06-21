package sg.edu.np.ignight.Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class UserObject implements Serializable {
    private String uid;
    private String phone;
    private String username;
    private String gender;
    private String aboutMe;
    private ArrayList<String> interestList;
    private String relationshipPref;
    private String genderPref;
    private ArrayList<String> dateLocList;
    private String profilePicUrl;
    private int age;
    private String profileCreated;

    public UserObject() {}

    public UserObject(String uid, String aboutMe, int age, ArrayList<String> dateLocList, String gender, String genderPref, ArrayList<String> interestList, String profilePicUrl, String relationshipPref, String phone, String profileCreated, String username) {
        this.uid = uid;
        this.aboutMe = aboutMe;
        this.age = age;
        this.dateLocList = dateLocList;
        this.gender = gender;
        this.genderPref = genderPref;
        this.interestList = interestList;
        this.profilePicUrl = profilePicUrl;
        this.relationshipPref = relationshipPref;
        this.phone = phone;
        this.profileCreated = profileCreated;
        this.username = username;
    }

    @Override
    public String toString(){
        return "uid: " + uid + "username: " + username;
    }
    public String getUid() {
        return uid;
    }
    public String getPhone() {
        return phone;
    }
    public String getUsername() {
        return username;
    }
    public String getGender() {
        return gender;
    }
    public String getAboutMe() {
        return aboutMe;
    }
    public ArrayList<String> getInterestList() {
        return interestList;
    }
    public String getRelationshipPref() {
        return relationshipPref;
    }
    public String getGenderPref() {
        return genderPref;
    }
    public ArrayList<String> getDateLocList() {
        return dateLocList;
    }
    public String getProfilePicUrl() {
        return profilePicUrl;
    }
    public int getAge() {
        return age;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public void setGender(String gender) {
        this.gender = gender;
    }
    public void setAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }
    public void setInterestList(ArrayList<String> interestList) {
        this.interestList = interestList;
    }
    public void setRelationshipPref(String relationshipPref) {
        this.relationshipPref = relationshipPref;
    }
    public void setGenderPref(String genderPref) {
        this.genderPref = genderPref;
    }
    public void setDateLocList(ArrayList<String> dateLocList) {
        this.dateLocList = dateLocList;
    }
    public void setProfilePicUrl(String profilePicUrl) {
        this.profilePicUrl = profilePicUrl;
    }
    public void setAge(int age) {
        this.age = age;
    }
}
