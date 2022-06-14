package sg.edu.np.ignight.Models;

public class UserAccount {
    // This is a data model to be used for account registration.
    // It is used to create a model from the information from FireBase.
    // Do not use this for anything other than data modelling of database!

    // Note: I may need to update this model class if there are any changes
            // to the structure of the firebase database

    private String phone;
    private String username;
    private String email;

    public UserAccount(String phone, String username, String email) {
        this.phone = phone;
        this.username = username;
        this.email = email;
    }

    public UserAccount() {
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
