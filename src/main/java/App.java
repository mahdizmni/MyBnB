import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class App {
    static Scanner scan;
    public static void main(String[] args) throws SQLException, ParseException {
        MySQLObj sqlObj = MySQLObj.getInstance();
        scan = new Scanner(System.in);
        User user = null;
        boolean exit = false;
        while(!exit && user == null){
            Utils.printMenu("Register/Login", new String[]{"Register", "Login"});
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
            Utils.printMenu("Renter/Host", new String[]{"Renter", "Host"});
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
