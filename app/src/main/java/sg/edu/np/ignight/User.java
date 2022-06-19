package sg.edu.np.ignight;

public class User {
    String Email;
    String Phone;
    String Username;
    String Gender;
    String Aboutme;
    String Interest;
    String Relationship_pref;
    String Gender_pref;
    String Date_location;
    String ProfilePicUrl;
    Integer Age;

    public User() {}

    public User(String email, String phone, String username, String gender, String aboutme, String interest, String relationship_pref, String gender_pref, String date_location, String profilepic, Integer age) {
        this.Email = email;
        this.Phone = phone;
        this.Username = username;
        this.Gender = gender;
        this.Aboutme = aboutme;
        this.Interest = interest;
        this.Relationship_pref = relationship_pref;
        this.Gender_pref = gender_pref;
        this.Date_location = date_location;
        this.ProfilePicUrl = profilepic;
        this.Age = age;
    }
}
