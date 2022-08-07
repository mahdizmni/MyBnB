import java.sql.SQLException;
import java.util.Scanner;


public class Host extends User{
    public Host(User u) {
        super(u.getSin(), u.firstname, u.lastname);
        this.setEmail(u.getEmail());
    }

    public void mainLoop() throws SQLException {
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

    public void createListing() throws SQLException {
        Scanner input = new Scanner(System.in);
        System.out.println("create a listing");

        double lat, lon;
        String amenities, type;
        int start, end, num, address_id = 0, city_id = 0, country_id = 0, type_id,
                listing_id, amenities_id = 0, period_id, price;
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
                price = Integer.parseInt(input.nextLine());

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
        if (!MySQLObj.CancelBookedListing(booking_id, getSin()))
            System.out.println("invalid booking id");
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

    public void rate() {}
}
