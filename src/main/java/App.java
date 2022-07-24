import java.util.Scanner;

public class App {
    public static void main(String[] args){
        MySQLObj sqlObj = MySQLObj.getInstance();

        Scanner scan = new Scanner(System.in);
        User user = null;
        boolean exit = false;
        while(!exit && user == null){
            Utils.printDivider("Login/Register");
            System.out.println("Please select an option from below: ");
            System.out.println("\t 1 -> Register");
            System.out.println("\t 2 -> Login");
            System.out.println("\t q -> exit");
            System.out.print("Choose an option: ");
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> Register.register();
                case "2"-> user = Login.login();
                case "q" -> exit = true;
            }
        }
        Utils.printInfo("Login Successful!");

        exit = false;
        while(!exit){
            Utils.printDivider("Renter/Host");
            System.out.println("Please select an option from below: ");
            System.out.println("\t 1 -> Renter");
            System.out.println("\t 2 -> Host");
            System.out.println("\t q -> exit");
            System.out.print("Choose an option: ");
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1":
                    assert user != null;
                    Renter renter = new Renter(user);
                    renter.mainLoop();
                    break;
                case "2":
                    assert user != null;
                    Host host = new Host(user);
                    host.mainLoop();
                    break;
                case "q":
                    exit = true;
                    break;
            }
        }
        MySQLObj.closeConnection(); // closes sql connection, keep this in the bottom
    }
}
