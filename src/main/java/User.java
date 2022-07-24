
public class User {

    private int sin;
    public String firstname;
    public String lastname;
    private String email;
    private String creditcard;

    public User(int sin, String firstname, String lastname){
        this.sin = sin;
        this.firstname = firstname;
        this.lastname = lastname;
    }

    public void setEmail(String email){
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public int getSin() {
        return sin;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public void setCreditcard(String creditcard) {
        this.creditcard = creditcard;
    }

    public String getCreditcard() {
        return creditcard;
    }
}
