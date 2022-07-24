import java.util.Scanner;

public class Renter extends User{
    public Renter(User u) {
        super(u.getSin(), u.firstname, u.lastname);
        this.setEmail(u.getEmail());
    }

    public void mainLoop(){
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Utils.printDivider(this.firstname + "'s Menu (Renter)");
            System.out.print("Please select an option from below: ");
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> bookListing();
                case "2"-> cancelBooking();
                case "3"-> getHistory();
                case "4"-> comment();
                case "q" -> exit = true;
            }
        }
    }

    public void bookListing(){
        System.out.println("Book a listing");
    }

    public void cancelBooking(){
        System.out.println("Cancel a booking");
    }

    public void getHistory(){
        System.out.println("History");
    }

    public void comment(){
        System.out.println("Comment on past hosts");
    }
}
