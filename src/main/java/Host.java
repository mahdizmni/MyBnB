import javax.security.sasl.SaslServer;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;
import java.text.SimpleDateFormat;
import java.util.Date;

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
    // TODO: check date

    public void createListing() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("create a listing");

        double lat, lon;
        String amenities, type;
        int start, end, num, address_id = 0, city_id = 0, country_id = 0, type_id,
                listing_id, amenities_id = 0, period_id;
        float price;
        String city, country, postalcode, street, buff;
        boolean addtoaddress, addtocity, addtocountry;
        addtoaddress = false; addtocity = false; addtocountry = false;

        System.out.println("please insert the postalcode with space");
        postalcode = input.nextLine();
        System.out.println("please insert num");
        num = Integer.parseInt(input.nextLine());
        System.out.println("please insert the street");
        street = input.nextLine();
        System.out.println("please insert the city");
        city = input.nextLine();
        System.out.println("please insert the country");
        country = input.nextLine();
        System.out.println("please insert the lat");
        lat = Double.parseDouble(input.nextLine());
        System.out.println("please insert the lon");
        lon = Double.parseDouble(input.nextLine());
        System.out.println("please insert the type of your listing (print the options?)");
        type = input.nextLine();

        // check if a listing already exists
        // TA:  more efficient
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
            System.out.println("listing already exists! plz try again");
            return;
        }
        // TODO: efficiency

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

//        System.out.println("listing id: " + listing_id + "type id :" + type_id + "address id : " + address_id + "city id : " + city_id + "country id : " + country_id);
        // Linking corresponding tables
        MySQLObj.addToListingsType(listing_id, type_id);
        MySQLObj.addToBelongsTo(city_id, country_id);
        MySQLObj.addToLocatedIn(address_id, listing_id);
        MySQLObj.addToIsIn(address_id, city_id);


        System.out.println("please insert one amenity at a time. press q to finish");
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

        System.out.println("please insert the available periods and price of your listing one at a time. press q to finish");
        stop = false;
        while (!stop) {
            System.out.println("press y to add a period, q to finish");
            buff = input.nextLine();
            if (!buff.equals("q")) {
                System.out.println("start date(e.g 20220907):");
                start = Integer.parseInt(input.nextLine());
                System.out.println("end date(e.g 20220907):");
                end = Integer.parseInt(input.nextLine());
                System.out.println("price:");
                price = Float.parseFloat(input.nextLine());

                // add these to table
                if (MySQLObj.addToPeriod(start, end))
                    period_id = MySQLObj.getRecentID();
                else
                    period_id = MySQLObj.getPeriodID(start ,end);

                if (!MySQLObj.addToAvailableIn(listing_id, period_id, price))
                    System.out.println("already added!");
            } else {
                stop = true;
            }

        }

    }

    public void cancelBooking(){
        // TODO: today's date in consideration
        Scanner input = new Scanner(System.in);
        System.out.println("Cancel a booking");
        System.out.println("list of all booked listings");
        MySQLObj.ViewAllBookedListings(getSin());
        System.out.println("enter a booking id to cancel");
        int booking_id = Integer.parseInt(input.nextLine());
        int listing_id = MySQLObj.BookingIDToListingID(booking_id);
        if (listing_id == -1) {
            System.out.println("invalid booking id");
            return;
        }
        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }
        if (!MySQLObj.isReserved(booking_id)) {
            System.out.println("has not been reserved");
            return;
        }
        if (MySQLObj.CancelBookedListing(booking_id))
            System.out.println("success");
    }

    public void removeListing(){
        Scanner input = new Scanner(System.in);
        System.out.println("Remove a listing");
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("enter a listing id");
        int listing_id = Integer.parseInt(input.nextLine());

        if (!MySQLObj.ValidateListingID(listing_id)) {
            System.out.println("invalid listing id");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }

        if (MySQLObj.RemoveListing(listing_id))
            System.out.println("success");
    }

    public void updatePrice() throws ParseException, SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("Update Price");
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("enter a listing id");
        int listing_id = Integer.parseInt(input.nextLine());

        if (!MySQLObj.ValidateListingID(listing_id)) {
            System.out.println("invalid listing id");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }

        MySQLObj.ViewAllListingsWithPrice(getSin(), listing_id);

        System.out.println("choose an ID");
        int period_id = Integer.parseInt(input.nextLine());

        listing_id = MySQLObj.PeriodIDToListingID(period_id);
        if (listing_id == -1)  {
            System.out.println("invalid id!");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }

        if (MySQLObj.BookedWithinAvailability(listing_id, period_id) == -1) {
            System.out.println("already booked!");
            return;
        }

        System.out.println("set a price");          // TODO: valid price
        float price = Float.parseFloat(input.nextLine());

        if (MySQLObj.UpdatePrice(listing_id, period_id, price))
            System.out.println("success");


        return;
    }

    public void updateAvailability() throws SQLException, ParseException {
        Scanner input = new Scanner(System.in);
        System.out.println("Update Availability");
        System.out.println("All listings");
        MySQLObj.ViewAllHostListings(getSin());
        System.out.println("enter a listing id");
        int listing_id = Integer.parseInt(input.nextLine());

        if (!MySQLObj.ValidateListingID(listing_id)) {
            System.out.println("invalid listing id");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }

        MySQLObj.ViewAllListingsWithPrice(getSin(), listing_id);

        System.out.println("choose an ID");
        int period_id = Integer.parseInt(input.nextLine());

        listing_id = MySQLObj.PeriodIDToListingID(period_id);
        if (listing_id == -1)  {
            System.out.println("invalid id!");
            return;
        }

        if (!MySQLObj.OwnsListing(listing_id, getSin())) {
            System.out.println("you dont own this listing!");
            return;
        }

        if (MySQLObj.BookedWithinAvailability(listing_id, period_id) == -1) {
            System.out.println("already booked!");
            return;
        }

        System.out.println("start date(e.g 20220907):");
        int start = Integer.parseInt(input.nextLine());
        System.out.println("end date(e.g 20220907):");
        int end = Integer.parseInt(input.nextLine());

        int previous_period_id = period_id;

        if (MySQLObj.addToPeriod(start, end))
            period_id = MySQLObj.getRecentID();
        else
            period_id = MySQLObj.getPeriodID(start ,end);

        // new time does not overlap
        if (MySQLObj.DoesNotOverlap(start, listing_id)) {
            MySQLObj.UpdateAvailability(listing_id, period_id, previous_period_id);
            System.out.println("success");
        } else {
            System.out.println("overlaps!");
        }


        return;

    }

    public void getHistory(){
        MySQLObj.ViewGetHistory(getSin());
    }


    public void comment() throws SQLException {
        // TODO: recently
        Scanner input = new Scanner(System.in);
        System.out.println("Comment on past renters");
        // show all past renters with associated listing id
        MySQLObj.ViewListingsHistory(getSin());
        // choose a renter
        System.out.println("insert a renter id");
        int renter_sin = Integer.parseInt(input.nextLine());
        // check if it's a valid renter (user sin)
        if (!MySQLObj.ValidateUserID(renter_sin)) {
            System.out.println("invalid User!");
            return;
        }
        // check if has rented from this host before
        if (!MySQLObj.MyRenter(renter_sin, getSin())) {
            System.out.println("has not rented from you!");
            return;
        }
        // insert comment
        System.out.println("insert a comment. hit enter to send.");
        String comment = input.nextLine();
        if (MySQLObj.CommentsOnUser(getSin(), renter_sin, comment))
            System.out.println("success");
    }
    public void rate() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("rate on past renters");
        // show all past renters with associated listing id
        MySQLObj.ViewListingsHistory(getSin());
        // choose a renter
        System.out.println("insert a renter id");
        int renter_sin = Integer.parseInt(input.nextLine());
        // check if it's a valid renter (user sin)
        if (!MySQLObj.ValidateUserID(renter_sin)) {
            System.out.println("invalid User!");
            return;
        }
        // check if has rented from this host before
        if (!MySQLObj.MyRenter(renter_sin, getSin())) {
            System.out.println("has not rented from you!");
            return;
        }
        // insert score
        System.out.println("insert a rating out of 10. hit enter to send.");
        int rate = Integer.parseInt(input.nextLine());
        if (MySQLObj.RatesOnUser(getSin(), renter_sin, rate))
            System.out.println("success");
    }
}
