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
        System.out.println("create a listing");
        double lat, lon;
        String amenities, type;
        int start, end, num, address_id, city_id, country_id, type_id, listing_id;
        String city, country, postalcode, street, buff;
        boolean[] boolarr = new boolean[3];

        System.out.println("please insert the postalcode with space");
        postalcode = App.scan.nextLine();
        System.out.println("please insert num");
        num = App.scan.nextInt();
        System.out.println("please insert the street");
        street = App.scan.nextLine();
        System.out.println("please insert the city");
        city = App.scan.nextLine();
        System.out.println("please insert the country");
        country = App.scan.nextLine();
        System.out.println("please insert the lat");
        lat = App.scan.nextDouble();
        System.out.println("please insert the lon");
        lon = App.scan.nextDouble();
        System.out.println("please insert the type of your listing (print the options?)");
        type = App.scan.nextLine();

        // check if a listing already exists
        // TA:  more efficient
        try {
            MySQLObj.addToAddress(postalcode, street, num);
            address_id = MySQLObj.getRecentID();
        } catch (SQLException e) {
            boolarr[0] = true;
        }

        try {
            MySQLObj.addToCity(city);
            city_id = MySQLObj.getRecentID();
        } catch (SQLException e) {
            boolarr[1] = true;
        }

        try {
            MySQLObj.addToCountry(country);
            country_id = MySQLObj.getRecentID();
        } catch (SQLException e) {
            boolarr[2] = true;
        }

        if (boolarr[0] && boolarr[1] && boolarr[3]) {
            System.out.println("listing already exists! plz try again");
            return;
        }

        if (boolarr[0])
            address_id = MySQLObj.getAddressID(postalcode, num);
        if (boolarr[1])
            city_id = MySQLObj.getCityID(city);
        if (boolarr[2])
            country_id = MySQLObj.getCountryID(country);


        MySQLObj.addToType(type);
        type_id = MySQLObj.getRecentID();

        MySQLObj.addToListings(lat, lon);
        listing_id = MySQLObj.getRecentID();

        // Linking corresponding tables
        MySQLObj.addToListingsType(listing_id, type_id);





        System.out.println("please insert one amenity at a time. press q to finish");
        boolean stop = false;
        while (!stop) {
            amenities = App.scan.nextLine();
            if (!amenities.equals("q")) {
                // insert to tables
            } else {
                stop = true;
            }
        }

        System.out.println("please insert the available periods of your listing");
        stop = false;
        while (!stop) {
            System.out.println("start date(e.g 20220907):");
            start = App.scan.nextInt();
            System.out.println("end date(e.g 20220907):");
            end = App.scan.nextInt();
            System.out.println("press y to add more periods, q to finish");
            buff = App.scan.nextLine();
            if (!buff.equals("q")) {
                stop = true;
            } else {
                // add these to table
            }
        }
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

    public void rate() {}
}
