package sg.edu.np.ignight.Objects;

import java.io.Serializable;
import java.util.ArrayList;

public class UserObject implements Serializable {
    private String uid;
    private String email;
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

    public UserObject() {}

    public UserObject(String uid, String email, String phone, String username, String gender, String aboutMe, ArrayList<String> interestList, String relationshipPref, String genderPref, ArrayList<String> dateLocList, String profilePicUrl, int age) {
        this.uid = uid;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.gender = gender;
        this.aboutMe = aboutMe;
        this.interestList = interestList;
        this.relationshipPref = relationshipPref;
        this.genderPref = genderPref;
        this.dateLocList = dateLocList;
        this.profilePicUrl = profilePicUrl;
        this.age = age;
    }
    @Override
    public String toString(){
        return "uid: " + uid + "username: " + username;
    }
    public String getUid() {
        return uid;
    }
    public String getEmail() {
        return email;
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

    public void setEmail(String email) {
        this.email = email;
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
