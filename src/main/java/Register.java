import java.util.Scanner;

public class Register {
    public static void register(){
        Scanner scan = new Scanner(System.in);
        Utils.printDivider("Register");
        System.out.println("Please enter your information accordingly: ");
        System.out.print("SIN: ");
        int sinInput = scan.nextInt();
        System.out.print("First Name: ");
        String firstnameInput = scan.next();
        System.out.print("Last Name: ");
        String lastnameInput = scan.next();
        System.out.print("Date of Birth (YYYYMMDD i.e. 20680420): ");
        int birthDateInput = scan.nextInt();
        System.out.print("Occupation: ");
        String occupation = scan.next();
        System.out.print("Email: ");
        String emailInput = scan.next();
        System.out.print("Password: ");
        String passwordInput = scan.next();
        System.out.print("Credit Card (All digits, no spaces): ");
        String creditCardInput = scan.next();
        try{
            if(MySQLObj.checkEmailExists(emailInput)){
                Utils.printInfo("Email already exists!");
            }
            else if (MySQLObj.registerUser(
                    sinInput,
                    firstnameInput,
                    lastnameInput,
                    birthDateInput,
                    occupation,
                    emailInput,
                    passwordInput,
                    creditCardInput)){
                Utils.printInfo("Successfully registered user!");
            }
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
}
