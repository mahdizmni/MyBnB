import java.util.Scanner;

public class Host extends User{
    public Host(User u) {
        super(u.getSin(), u.firstname, u.lastname);
        this.setEmail(u.getEmail());
    }

    public void mainLoop(){
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Utils.printMenu(this.firstname + "'s Menu (Host)",
                    new String[]{
                            "Create a listing",
                            "Cancel a booking",
                            "Remove a listing",
                            "Update price of listing",
                            "Update availability of listing",
                            "Get history of past renters",
                            "Comment on past renters"});
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> createListing();
                case "2"-> cancelBooking();
                case "3"-> removeListing();
                case "4"-> updatePrice();
                case "5"-> updateAvailability();
                case "6"-> getHistory();
                case "7"-> comment();
                case "q" -> exit = true;
            }
        }
    }

    public void createListing(){
        System.out.println("create a listing");
    }

    public void cancelBooking(){
        System.out.println("Cancel a booking");
    }

    public void removeListing(){
        System.out.println("Remove a listing");
    }

    public void updatePrice(){
        System.out.println("Update Price");
    }

    public void updateAvailability(){
        System.out.println("Update Availability");
    }

    public void getHistory(){
        System.out.println("History");
    }

    public void comment(){
        System.out.println("Comment on past renters");
    }
}
