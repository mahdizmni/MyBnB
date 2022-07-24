import java.util.Scanner;

public class Register {
    public static void register(){
        Scanner scan = new Scanner(System.in);
        Utils.printDivider("Register");
        System.out.print("Email: ");
        String emailInput = scan.next();
        System.out.print("Password: ");
        String passwordInput = scan.next();
        Utils.printInfo("Account made!" + emailInput + passwordInput);
    }
}
