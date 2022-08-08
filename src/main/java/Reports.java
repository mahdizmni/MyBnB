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
                            "Get total number of listings by country",
                            "Get total number of listings by country & city",
                            "Get total number of listings by country & city & postal code",
                            "Get ranked host by listings per country",
                            "Get ranked host by listings per city",
                            "Get commercial hosts",
                            "Get ranked renters by booking in period",
                            "Get ranked renters by booking in period per city",
                            "Get hosts with the largest cancellations",
                            "Get renters with the largest cancellations",
                            "Get most popular noun phrases from comments",
                    });
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> NumBookings1();
                case "2"-> NumBookings2();
                case "3"-> MySQLObj.ViewCountListingsByCountry();
                case "4"-> MySQLObj.ViewCountListingsByCountryCity();
                case "5"-> MySQLObj.ViewCountListingsByCountryCityPostalcode();
                case "6"-> MySQLObj.ViewRankHostsByListingsPerCountry();
                case "7"-> MySQLObj.ViewRankHostsByListingsPerCity();
                case "8"-> MySQLObj.ViewCommercialHosts();
                case "9"-> RankRentersByBookingInPeriod();
                case "10"-> RankRentersByBookingInPeriodPerCity();
                case "11"-> MySQLObj.ViewLargestCancellationsHosts();
                case "12"-> MySQLObj.ViewLargestCancellationsRenters();
                case "13"-> MySQLObj.ViewPopularNounPhrase();
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

}
