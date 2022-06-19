package sg.edu.np.ignight;

public class UserAccount {
    // this class is a data model to be used for user registration.
    // do not delete or modify

    public String email;
    public String phone;
    public String username;

    public UserAccount(){ }

    public UserAccount(String email, String phone, String username){
        this.email = email;
        this.phone = phone;
        this.username = username;
    }
}
