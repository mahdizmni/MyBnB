import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;

public class Host extends User{
    public Host(User u) {
        super(u.getSin(), u.firstname, u.lastname);
        this.setEmail(u.getEmail());
    }

    public void mainLoop() throws SQLException, ParseException {
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
                            "Comment on past renters",
                            "Rate past renters"});
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> createListing();
                case "2"-> cancelBooking();
                case "3"-> removeListing();
                case "4"-> updatePrice();
                case "5"-> updateAvailability();
                case "6"-> getHistory();
                case "7"-> comment();
                case "8"-> rate();
                case "q" -> exit = true;
            }
        }
    }

    // TODO: check date
    public void createListing() throws SQLException {
        Scanner input = new Scanner(System.in);
// todo: period overlaps or not
        double lat, lon;
        String amenities, type;
        int start, end, num, address_id = 0, city_id = 0, country_id = 0, type_id,
                listing_id, amenities_id = 0, period_id;
        float price;
        String city, country, postalcode, street, buff;
        boolean addtoaddress, addtocity, addtocountry;
        addtoaddress = false; addtocity = false; addtocountry = false;

        System.out.println("Please insert the street number.");
        num = Integer.parseInt(input.nextLine());
        System.out.println("Please insert the street.");
        street = input.nextLine();
        System.out.println("Please insert the city.");
        city = input.nextLine();
        System.out.println("Please insert the country.");
        country = input.nextLine();
        System.out.println("Please insert the postal code with space.");
        postalcode = input.nextLine();
        System.out.println("Please insert the latitude.");
        lat = Double.parseDouble(input.nextLine());
        System.out.println("Please insert the longitude.");
        lon = Double.parseDouble(input.nextLine());
        System.out.println("Please insert the type of your listing.");
        type = input.nextLine();

        // check if a listing already exists
        if (MySQLObj.addToAddress(postalcode, street, num))
            address_id = MySQLObj.getRecentID();
        else  addtoaddress = true;

        if (MySQLObj.addToCity(city))
            city_id = MySQLObj.getRecentID();
        else  addtocity = true;

        if (MySQLObj.addToCountry(country))
            country_id = MySQLObj.getRecentID();
        else  addtocountry = true;

        if (addtoaddress && addtocity && addtocountry) {
            Utils.printInfo("Listing already exists! Please try again.");
            return;
        }

        if (addtoaddress)
            address_id = MySQLObj.getAddressID(postalcode, num);
        if (addtocity)
            city_id = MySQLObj.getCityID(city);
        if (addtocountry)
            country_id = MySQLObj.getCountryID(country);

        if (MySQLObj.addToType(type))
            type_id = MySQLObj.getRecentID();
        else
            type_id = MySQLObj.getTypeID(type);

        MySQLObj.addToListings(lat, lon);
        listing_id = MySQLObj.getRecentID();

        MySQLObj.addToOwns(getSin(), listing_id);

        // Linking corresponding tables
        MySQLObj.addToListingsType(listing_id, type_id);
        MySQLObj.addToBelongsTo(city_id, country_id);
        MySQLObj.addToLocatedIn(address_id, listing_id);
        MySQLObj.addToIsIn(address_id, city_id);

        System.out.println("Please insert one amenity at a time. Press q to finish.");
        boolean stop = false;
        while (!stop) {
            amenities = input.nextLine();
            if (!amenities.equals("q")) {
                // insert to tables
                if (MySQLObj.addToAmenities(amenities))
                    amenities_id = MySQLObj.getRecentID();
                else
                    amenities_id = MySQLObj.getAmenitiesID(amenities);

                MySQLObj.addToHas(amenities_id, listing_id);
            } else {
                stop = true;
            }
        }

        System.out.println("Please insert the available periods and price of your listing one at a time. Press q to finish");
        stop = false;
        while (!stop) {
            System.out.println("Press y to add a period, q to finish.");
            buff = input.nextLine();
            if (!buff.equals("q")) {
                System.out.println("Start date(e.g 20220907):");
                start = Integer.parseInt(input.nextLine());
                System.out.println("End date(e.g 20220907):");
                end = Integer.parseInt(input.nextLine());
                System.out.println("Period price:");
                price = Float.parseFloat(input.nextLine());

                // add these to table
                if (MySQLObj.addToPeriod(start, end))
                    period_id = MySQLObj.getRecentID();
                else
                    period_id = MySQLObj.getPeriodID(start ,end);

                if (!MySQLObj.addToAvailableIn(listing_id, period_id, price))
                    Utils.printInfo("Listing already added.");
                if (!MySQLObj.DoesNotOverlap(start, end, listing_id))
                    System.out.println("overlap!");
            } else {
                stop = true;
            }

        }
        Utils.printInfo("Listing successfully created!");
    }

    public void cancelBooking(){
        // TODO: today's date in consideration
        Scanner input = new Scanner(System.in);
        System.out.println("List of all booked listings: ");
        MySQLObj.ViewAllBookedListings(getSin());
        System.out.println("Choose a valid booking id: ");
        int booking_id = Integer.parseInt(input.nextLine());
        int listing_id = MySQLObj.BookingIDToListingID(booking_id);
        if (listing_id == -1) {
            Utils.printInfo("Invalid booking ID.");
            return;
        }
        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }
        if (!MySQLObj.isReserved(booking_id)) {
            Utils.printInfo("Booking has not been reserved");
            return;
        }
        if (!MySQLObj.CancelBookedListing(booking_id)){
            Utils.printInfo("Cancel booking was not successful.");
            return;
        }
        Utils.printInfo("Successfully cancelled booking!");
    }

    public void removeListing(){
        Scanner input = new Scanner(System.in);
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("Choose a valid listing ID: ");
        int listing_id = Integer.parseInt(input.nextLine());
        if (!MySQLObj.ValidateListingID(listing_id)) {
            Utils.printInfo("Invalid listing ID.");
            return;
        }
        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }
        if (!MySQLObj.RemoveListing(listing_id)){
            Utils.printInfo("Remove listing was not successful.");
            return;
        }
        Utils.printInfo("Successfully removed listing!");
    }

    public void updatePrice() throws ParseException, SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("Choose a valid listing ID: ");
        int listing_id = Integer.parseInt(input.nextLine());

        if (!MySQLObj.ValidateListingID(listing_id)) {
            Utils.printInfo("Invalid listing ID.");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }

        System.out.println("All Periods of Listing:");
        MySQLObj.ViewAllListingsWithPrice(getSin(), listing_id);

        System.out.println("Choose a valid period ID: ");
        int period_id = Integer.parseInt(input.nextLine());

        listing_id = MySQLObj.PeriodIDToListingID(period_id);
        if (listing_id == -1)  {
            Utils.printInfo("Invalid period ID.");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }

        if (MySQLObj.BookedWithinAvailability(listing_id, period_id) == -1) {
            Utils.printInfo("Listing has already been booked in that period.");
            return;
        }

        System.out.println("Set a price");
        float price = Float.parseFloat(input.nextLine());

        if (!MySQLObj.UpdatePrice(listing_id, period_id, price)){
            Utils.printInfo("Price update was unsuccessful.");
            return;
        }
        Utils.printInfo("Successfully updated price!");
    }

    public void updateAvailability() throws SQLException, ParseException {
        Scanner input = new Scanner(System.in);
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("Choose a valid listing ID: ");
        int listing_id = Integer.parseInt(input.nextLine());

        if (!MySQLObj.ValidateListingID(listing_id)) {
            Utils.printInfo("Invalid listing id");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }

        MySQLObj.ViewAllListingsWithPrice(getSin(), listing_id);

        System.out.println("Choose a valid period ID: ");
        int period_id = Integer.parseInt(input.nextLine());

        listing_id = MySQLObj.PeriodIDToListingID(period_id);
        if (listing_id == -1)  {
            Utils.printInfo("Invalid period ID.");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            Utils.printInfo("User does not own this listing.");
            return;
        }

        if (MySQLObj.BookedWithinAvailability(listing_id, period_id) == -1) {
            Utils.printInfo("Listing has already been booked in that period.");
            return;
        }

        System.out.println("Please choose a valid start date (e.g 20220907):");
        int start = Integer.parseInt(input.nextLine());
        System.out.println("Please choose a valid end date (e.g 20220907):");
        int end = Integer.parseInt(input.nextLine());

        int previous_period_id = period_id;

        if (MySQLObj.addToPeriod(start, end))
            period_id = MySQLObj.getRecentID();
        else
            period_id = MySQLObj.getPeriodID(start ,end);

        // new time does not overlap
        if (MySQLObj.DoesNotOverlap(start, end, listing_id)) {
            MySQLObj.UpdateAvailability(listing_id, period_id, previous_period_id);
            Utils.printInfo("Updated availability successfully!");
        } else {
            Utils.printInfo("Period overlaps. Please try again.");
        }
    }

    public void getHistory(){
        MySQLObj.ViewGetHistory(getSin());
    }

    public void comment() throws SQLException {
        // TODO: recently
        Scanner input = new Scanner(System.in);
        System.out.println("Comment on past renters: ");
        // show all past renters with associated listing id
        if (!MySQLObj.ViewListingsHistory(getSin())) {
            return;
        }
        // choose a renter
        System.out.println("Choose a valid renter ID: ");
        int renter_sin = Integer.parseInt(input.nextLine());
        // check if it's a valid renter (user sin)
        if (!MySQLObj.ValidateUserID(renter_sin)) {
            Utils.printInfo("User ID is not valid.");
            return;
        }
        // check if renter has rented from this host before
        if (!MySQLObj.MyRenter(renter_sin, getSin())) {
            Utils.printInfo("User has not rented your listings before.");
            return;
        }
        // insert comment
        System.out.println("insert a comment. hit enter to send.");
        String comment = input.nextLine();
        if (!MySQLObj.CommentsOnUser(getSin(), renter_sin, comment)){
            Utils.printInfo("Comment was unsuccessful.");
            return;
        }
        Utils.printInfo("Successfully commented on User.");
    }
    public void rate() throws SQLException {
        Scanner input = new Scanner(System.in);
        // show all past renters with associated listing id
        MySQLObj.ViewListingsHistory(getSin());
        // choose a renter
        System.out.println("Choose a valid renter ID:");
        int renter_sin = Integer.parseInt(input.nextLine());
        // check if it's a valid renter (user sin)
        if (!MySQLObj.ValidateUserID(renter_sin)) {
            Utils.printInfo("User ID is not valid.");
            return;
        }
        // check if renter has rented from this host before
        if (!MySQLObj.MyRenter(renter_sin, getSin())) {
            Utils.printInfo("User has not rented your listings before.");
            return;
        }
        // insert score
        System.out.println("Insert a rating out of 5. Hit enter to send.");
        int rate = Integer.parseInt(input.nextLine());
        if (!MySQLObj.RatesOnUser(getSin(), renter_sin, rate)){
            Utils.printInfo("Rating was unsuccessful.");
            return;
        }
        Utils.printInfo("Successfully rated User.");
    }
}
