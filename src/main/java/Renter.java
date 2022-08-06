import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Scanner;

public class Renter extends User{
    public Renter(User u) {
        super(u.getSin(), u.firstname, u.lastname);
        this.setEmail(u.getEmail());
    }

    public void mainLoop() throws SQLException, ParseException {
        boolean exit = false;
        while(!exit){
            Utils.printMenu(this.firstname + "'s Menu (Renter)",
                    new String[]{
                            "View all available listings",
                            "View all available periods of a listing",
                            "Book a listing",
                            "Cancel a booking",
                            "Get history of past hosts",
                            "Comment on past hosts"});
            String userInput = App.scan.nextLine();
            switch (userInput) {
                case "1"-> viewAllAvailableListings();
                case "2"-> viewAllAvailablePeriodsOfListing();
                case "3"-> bookListing();
                case "4"-> cancelBooking();
                case "5"-> getHistory();
                case "6"-> comment();
                case "q" -> exit = true;
            }
        }
    }

    public void viewAllAvailableListings(){
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = MySQLObj.getAllAvailableListings();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Listing_ID"));
                listingData.add(listings.getString("postalcode"));
                listingData.add(listings.getString("num") + " " + listings.getString("street"));
                listingData.add(listings.getDouble("AVG(price)"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Listing ID", "Postal Code", "Address", "Average Price"},listingsList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public void viewAllAvailablePeriodsOfListing() throws SQLException {
        System.out.print("Choose a valid listing id: ");
        int userListingID = new Scanner(System.in).nextInt();

        if(!MySQLObj.genericCheckIfIDExists("Listings", "ID", userListingID)){
            Utils.printInfo("Listing ID does not exist!");
            return;
        }

        ArrayList<ArrayList<Object>> apList = new ArrayList<>();
        ResultSet aprs = null;
        try{
            aprs = MySQLObj.getAllAvailablePeriodsOfListing(userListingID);
            while (aprs.next()) {
                ArrayList<Object> apd = new ArrayList<Object>();
                apd.add(aprs.getInt("Period_ID"));
                apd.add(aprs.getInt("Listing_ID"));
                apd.add(aprs.getString("postalcode"));
                apd.add(aprs.getString("num") + " " + aprs.getString("street"));
                apd.add(aprs.getDouble("price"));
                apd.add(aprs.getString("start"));
                apd.add(aprs.getString("end"));
                apList.add(apd);
            }
            Utils.printTable(new String[]{"Period ID", "Listing ID", "Postal Code", "Address", "Price", "Start Date", "End Date"}, apList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public void bookListing() throws SQLException, ParseException {
        System.out.print("Choose a valid listing id: ");
        Scanner scan = new Scanner(System.in);
        int userListingID = scan.nextInt();

        if(!MySQLObj.genericCheckIfIDExists("Listings", "ID", userListingID)){
            Utils.printInfo("Listing ID does not exist!");
            return;
        }

        System.out.println("Start date of stay. (Please make sure this date is between (inclusive) the available period of the listing");
        String userStartDateString = scan.next();
        System.out.println("End date of stay. (Please make sure this date is between (inclusive) the available period of the listing");
        String userEndDateString = scan.next();

        ResultSet apse;
        if ((apse = MySQLObj.getAvailablePeriodFromStartEnd(userListingID, userStartDateString, userEndDateString)) == null){
            Utils.printInfo("Start or end date is not valid.");
            return;
        }
        int targetPeriodID = apse.getInt("ID");
        String targetStartDateString = apse.getString("start");
        String targetEndDateString = apse.getString("end");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date targetStartDate = sdf.parse(targetStartDateString);
        Date targetEndDate = sdf.parse(targetEndDateString);
        Date userStartDate = sdf.parse(userStartDateString);
        Date userEndDate = sdf.parse(userEndDateString);

        // check case 1
        if(targetStartDate.compareTo(userStartDate) == 0 && targetEndDate.compareTo(userEndDate) == 0){
            // remove tuple in AvailableIn
            // add tuple in Books of with new dates
            return;
        }
        // check case 2
        boolean b1 = targetStartDate.compareTo(userStartDate) == 0 && targetEndDate.compareTo(userEndDate) > 0;
        boolean b2 = targetStartDate.compareTo(userStartDate) < 0 && targetEndDate.compareTo(userEndDate) == 0;
        if(b1 || b2){
            // edit AvailableIN Period to become less
            // add tuple in Books of with new dates
            return;
        }
        // check case 3
        boolean b3 = targetStartDate.compareTo(userStartDate) < 0 && targetEndDate.compareTo(userEndDate) > 0;
        if(b3){
            // edit 1st AvailableIN Period to become less
            // create 2nd AvailableIn Period with less dates
            // add tuple in Books of with new dates
            return;
        }
        // this line should not be reached
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
