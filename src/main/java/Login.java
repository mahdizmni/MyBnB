import java.util.Scanner;

public class Login {
    public static User login(){
        Scanner scan = new Scanner(System.in);
        Utils.printDivider("Login");
        System.out.print("Email: ");
        String emailInput = scan.next();
        System.out.print("Password: ");
        String passwordInput = scan.next();
        User user;
        try{
            if((user = MySQLObj.validateLoginCredentials(emailInput, passwordInput)) != null){
                return user;
            }
            else{
                Utils.printInfo("Login credentials are incorrect, please try again!");
            }
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
        return null;
    }
}
