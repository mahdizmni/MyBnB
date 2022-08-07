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
                            "Get history of all bookings",
                            "Comment on past hosts",
                            "Comment on past listings",
                            "Rate on past hosts",
                            "Rate on past listings"});
            String userInput = App.scan.nextLine();
            switch (userInput) {
                case "1"-> viewAllAvailableListings();
                case "2"-> viewAllAvailablePeriodsOfListing();
                case "3"-> bookListing();
                case "4"-> cancelBooking();
                case "5"-> getHistory();
                case "6"-> commentOnHost();
                case "7"-> commentOnListing();
                case "8"-> rateOnHost();
                case "9"-> rateOnListing();
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
        System.out.println("Please make sure the follow dates entered are between (inclusive) the available period of the listing");
        System.out.print("Please enter your start date of stay: ");
        String userStartDateString = scan.next();
        System.out.print("Please enter your end date of stay: ");
        String userEndDateString = scan.next();

        ResultSet apse = MySQLObj.getAvailablePeriodFromStartEnd(userListingID, userStartDateString, userEndDateString);
        if (!apse.next()){
            Utils.printInfo("Start or end date is not valid.");
            return;
        }
        int targetPeriodID = apse.getInt("ID");
        double targetPeriodPrice = apse.getDouble("price");
        String targetStartDateString = apse.getString("start");
        String targetEndDateString = apse.getString("end");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date targetStartDate = sdf.parse(targetStartDateString);
        Date targetEndDate = sdf.parse(targetEndDateString);
        Date userStartDate = sdf.parse(userStartDateString);
        Date userEndDate = sdf.parse(userEndDateString);

        // check case 1
        if(targetStartDate.compareTo(userStartDate) == 0 && targetEndDate.compareTo(userEndDate) == 0){
            // remove Period
            MySQLObj.genericRemoveByID("Period", "ID", targetPeriodID);
            // add tuple in Books with new dates
            MySQLObj.createBooking(this.getSin(), userListingID, userStartDateString, userStartDateString);
            Utils.printInfo("Booking successful!");
            return;
        }
        // check case 2
        boolean b1 = targetStartDate.compareTo(userStartDate) == 0 && targetEndDate.compareTo(userEndDate) > 0;
        boolean b2 = targetStartDate.compareTo(userStartDate) < 0 && targetEndDate.compareTo(userEndDate) == 0;
        if(b1 || b2){
            // edit AvailableIn Period to become less
            if (b1){
                // available period is right skewed i.e. |--------------s=======e|
                MySQLObj.editPeriod(targetPeriodID, sdf.format(Utils.addDays(userEndDate, 1)), targetEndDateString);
            }
            else {
                // available period is left skewed i.e. |s=======e--------------|
                MySQLObj.editPeriod(targetPeriodID, targetStartDateString, sdf.format(Utils.addDays(userStartDate, -1)));
            }
            // add tuple in Books with new dates
            MySQLObj.createBooking(this.getSin(), userListingID, userStartDateString, userEndDateString);
            Utils.printInfo("Booking successful!");
            return;
        }
        // check case 3
        boolean b3 = targetStartDate.compareTo(userStartDate) < 0 && targetEndDate.compareTo(userEndDate) > 0;
        if(b3){
            // edit 1st AvailableIn Period to become less
            MySQLObj.editPeriod(targetPeriodID, targetStartDateString, sdf.format(Utils.addDays(userStartDate, -1)));
            // create 2nd AvailableIn Period with less dates
            MySQLObj.createAvailablePeriod(userListingID, sdf.format(Utils.addDays(userEndDate, 1)), targetEndDateString, targetPeriodPrice);
            // add tuple in Books with new dates
            MySQLObj.createBooking(this.getSin(), userListingID, userStartDateString, userEndDateString);
            Utils.printInfo("Booking successful!");
            return;
        }
        // this line should not be reached
        Utils.printInfo("this line should not be reached!");
    }

    public void cancelBooking() throws SQLException {
        System.out.print("Choose a valid booking id: ");
        Scanner scan = new Scanner(System.in);
        int userBookingID = scan.nextInt();
        if(!MySQLObj.genericCheckIfIDExists("Books", "BookingID", userBookingID)){
            Utils.printInfo("Booking ID does not exist.");
            return;
        }
        if(!MySQLObj.checkIfRenterCreatedBooking(userBookingID, this.getSin())){
            Utils.printInfo("Booking ID is not created by this user.");
            return;
        }
        if(!MySQLObj.checkIfBookingIsReserved(userBookingID, this.getSin())){
            Utils.printInfo("Booking ID is already not reserved.");
            return;
        }
        MySQLObj.cancelBooking(userBookingID, this.getSin());
        Utils.printInfo("Booking ID successfully cancelled.");
    }

    public void getHistory(){
        ArrayList<ArrayList<Object>> grhList = new ArrayList<>();
        ResultSet grhrs = null;
        try{
            grhrs = MySQLObj.getRenterHistory(this.getSin());
            while (grhrs.next()) {
                ArrayList<Object> grhData = new ArrayList<Object>();
                grhData.add(grhrs.getInt("BookingID"));
                grhData.add(grhrs.getInt("Listing_ID"));
                String address = Utils.formatAddress(
                        new Object[]{
                                grhrs.getInt("num"),
                                grhrs.getString("street"),
                                grhrs.getString("ci.name"),
                                grhrs.getString("postalcode")
                        }
                );
                grhData.add(address);
                grhData.add(grhrs.getString("start"));
                grhData.add(grhrs.getString("end"));
                grhData.add(grhrs.getBoolean("isReserved"));
                grhList.add(grhData);
            }
            Utils.printTable(new String[]{"Booking ID", "Listing ID", "Address", "Start Date", "End Date", "Is Reserved"}, grhList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    private void getPastBookings(){
        ArrayList<ArrayList<Object>> gpbList = new ArrayList<>();
        ResultSet gpbrs = null;
        try{
            gpbrs = MySQLObj.getPastBookings(this.getSin());
            while (gpbrs.next()) {
                ArrayList<Object> gpbData = new ArrayList<Object>();
                gpbData.add(gpbrs.getInt("BookingID"));
                gpbData.add(gpbrs.getInt("Host_SIN"));
                gpbData.add(gpbrs.getInt("Listing_ID"));
                String address = Utils.formatAddress(
                        new Object[]{
                                gpbrs.getInt("num"),
                                gpbrs.getString("street"),
                                gpbrs.getString("ci.name"),
                                gpbrs.getString("postalcode")
                        }
                );
                gpbData.add(address);
                gpbData.add(gpbrs.getString("start"));
                gpbData.add(gpbrs.getString("end"));
                gpbList.add(gpbData);
            }
            Utils.printTable(new String[]{"Booking ID", "Host ID", "Listing_ID", "Address", "Start Date", "End Date"}, gpbList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public void commentOnHost() throws SQLException {
        Scanner scan = new Scanner(System.in);
        // show all past hosts the renter has previously booked from
        getPastBookings();
        // choose a host ID
        System.out.print("Choose a valid host ID: ");
        int userHostID = scan.nextInt();
        scan.nextLine();
        // check if it's a valid host (user sin)
        if(!MySQLObj.genericCheckIfIDExists("Owns", "Host_SIN", userHostID)){
            Utils.printInfo("Host ID is not an ID of a valid host");
            return;
        }
        // check if user has booked from this host before
        if(!MySQLObj.checkIfRenterHasRentedFromHost(userHostID, this.getSin())){
            Utils.printInfo("Renter has not rented from this host before");
            return;
        }
        // insert comment
        System.out.print("Insert a comment. Hit enter to send: ");
        String comment = scan.nextLine();
        if (MySQLObj.CommentsOnUser(getSin(), userHostID, comment))
            Utils.printInfo("Successfully commented on Host!");
    }

    public void commentOnListing() throws SQLException {
        Scanner scan = new Scanner(System.in);
        // show all past listings the renter has previously booked
        getPastBookings();
        // choose a listing ID
        System.out.print("Choose a valid listing ID: ");
        int userListingID = scan.nextInt();
        scan.nextLine();
        // check if it's a valid listing ID
        if(!MySQLObj.genericCheckIfIDExists("Listings", "ID", userListingID)){
            Utils.printInfo("Listing ID does not exist");
            return;
        }
        // check if user has booked the listing before
        if(!MySQLObj.checkIfRenterHasBookedListing(userListingID, this.getSin())){
            Utils.printInfo("Renter has not booked this listing before");
            return;
        }
        // insert comment
        System.out.print("Insert a comment. Hit enter to send: ");
        String comment = scan.nextLine();
        if (MySQLObj.CommentsOnListing(userListingID, getSin(), comment))
            Utils.printInfo("Successfully commented on Listing!");
    }

    public void rateOnHost() throws SQLException {
        Scanner scan = new Scanner(System.in);
        // show all past hosts the renter has previously booked from
        getPastBookings();
        // choose a host ID
        System.out.print("Choose a valid host ID: ");
        int userHostID = scan.nextInt();
        scan.nextLine();
        // check if it's a valid host (user sin)
        if(!MySQLObj.genericCheckIfIDExists("Owns", "Host_SIN", userHostID)){
            Utils.printInfo("Host ID is not an ID of a valid host");
            return;
        }
        // check if user has booked from this host before
        if(!MySQLObj.checkIfRenterHasRentedFromHost(userHostID, this.getSin())){
            Utils.printInfo("Renter has not rented from this host before");
            return;
        }
        // insert rating
        System.out.print("Insert a rating out of 5. Hit enter to send: ");
        int rating = scan.nextInt();
        scan.nextLine();
        if (MySQLObj.RatesOnUser(getSin(), userHostID, rating))
            Utils.printInfo("Successfully rated Host!");
    }

    public void rateOnListing() throws SQLException {
        Scanner scan = new Scanner(System.in);
        // show all past listings the renter has previously booked
        getPastBookings();
        // choose a listing ID
        System.out.print("Choose a valid listing ID: ");
        int userListingID = scan.nextInt();
        scan.nextLine();
        // check if it's a valid listing ID
        if(!MySQLObj.genericCheckIfIDExists("Listings", "ID", userListingID)){
            Utils.printInfo("Listing ID does not exist");
            return;
        }
        // check if user has booked the listing before
        if(!MySQLObj.checkIfRenterHasBookedListing(userListingID, this.getSin())){
            Utils.printInfo("Renter has not booked this listing before");
            return;
        }
        // insert rating
        System.out.print("Insert a rating out of 5. Hit enter to send: ");
        int rating = scan.nextInt();
        scan.nextLine();
        if (MySQLObj.RatesOnListing(userListingID, getSin(), rating))
            Utils.printInfo("Successfully rated listing!");
    }
}
