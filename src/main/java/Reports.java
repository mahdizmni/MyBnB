import java.sql.SQLException;
import java.text.ParseException;
import java.util.Scanner;


public class Reports {
    public void mainLoop() throws SQLException, ParseException {
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Utils.printMenu("Report" + "'s Menu (Admin)",
                    new String[]{
                            "Total number of bookings in a date range in a city",
                            "Total number of bookings in a city with a zipcode",
                            "Remove a listing",
                            "Update price of listing",
                            "Update availability of listing",
                            "Get history of past renters",
                            "Comment on past renters"});
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> NumBookings1();
                case "2"-> NumBookings2();
                case "q" -> exit = true;
            }
        }
    }

    public void NumBookings1() {
        Scanner input = new Scanner(System.in);
        System.out.println("insert start date");
        int start = Integer.parseInt(input.nextLine());
        System.out.println("insert end date");
        int end = Integer.parseInt(input.nextLine());
        System.out.println("insert city");
        String city = input.nextLine();
        int res = MySQLObj.CountBookings1(start, end, city);
        if (res > -1)
            System.out.println("Number of Bookings in the specified date range and location: " + res);
    }
    public void NumBookings2() {
        Scanner input = new Scanner(System.in);
        System.out.println("insert postalcode");
        String postalcode = input.nextLine();
        System.out.println("insert city");
        String city = input.nextLine();
        int res = MySQLObj.CountBookings2(postalcode, city);
        if (res > -1)
            System.out.println("Number of Bookings in the specified date range and location: " + res);
    }
    public void ListingsNumberPerCountry() {
        MySQLObj.ViewCountListingsByCountry();
    }

    public void ListingsNumberPerCountryCity() {
        MySQLObj.ViewCountListingsByCountryCity();
    }

    public void ListingsNumberPerCountryCityPostalcode() {
        MySQLObj.ViewCountListingsByCountryCityPostalcode();
    }
    public void RankHostsByListingsPerCountry() {
        MySQLObj.ViewRankHostsByListingsPerCountry();
    }
    public void RankHostsByListingsPerCity() {
        MySQLObj.ViewRankHostsByListingsPerCity();
    }
    public void CommercialHosts() {
        MySQLObj.ViewCommercialHosts();
    }
    public void RankRentersByBookingInPeriod() {
        Scanner scan = new Scanner(System.in);
        System.out.println("enter start date");
        String start = scan.nextLine();
        System.out.println("enter end date");
        String end = scan.nextLine();

        MySQLObj.ViewRankRentersByBookingInPeriod(start, end);
    }
    public void RankRentersByBookingInPeriodPerCity() {
        Scanner scan = new Scanner(System.in);
        System.out.println("enter start date");
        String start = scan.nextLine();
        System.out.println("enter end date");
        String end = scan.nextLine();

        MySQLObj.ViewRankRentersByBookingInPeriodPerCity(start, end);

    }

    public void LargestCancelationsHosts () {
        MySQLObj.ViewLargestCancellationsHosts();
    }
    public void LargestCancelationsRenters () {
        MySQLObj.ViewLargestCancellationsRenters();
    }
    public void DisplayMostPopularNPForListing() {
    }

}
