import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Search {

    public double distance = 10.0;
    public String priceSort = "NONE";
    public ArrayList<String> amenitiesList;
    public double[] priceRange = {50.0, 200.0};
    public String[] dateRange;

    public Search() {
        amenitiesList = new ArrayList<>();
        // default days are today and today + 30 days
        String defaultStartDate = Utils.formatDateToString(Utils.getToday());
        String defaultEndDate = Utils.formatDateToString(Utils.addDays(Utils.getToday(), 30));
        dateRange = new String[]{defaultStartDate, defaultEndDate};
    }

    public void mainLoop() throws SQLException {
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Utils.printMenu("Search Menu",
                    new String[]{
                            "Add specific distance option",
                            "Add price sort (ASC/DESC) option",
                            "Add price range filter option",
                            "Add amenities filter option",
                            "Add date range filter option",
                            "View all filter options",
                            "Search listing by address",
                            "Search listing by postal code",
                            "Search listing by coordinates"});
            String userInput = scan.nextLine();
            switch (userInput) {
                case "1"-> setDistanceOption();
                case "2"-> setPriceSortOption();
                case "3"-> setPriceRangeOption();
                case "4"-> setAmenitiesOption();
                case "5"-> setDataRangeOption();
                case "6"-> viewAllFilterOptions();
                case "7"-> searchListingByAddress();
                case "8"-> searchListingByPostalCode();
                case "9"-> searchListingByCoordinates();
                case "q" -> exit = true;
            }

            if(!userInput.equals("q")){
                Utils.printInfo("Press enter to go back to menu.");
                scan.nextLine();
            }
        }
    }

    public void setDistanceOption(){
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter a valid distance: ");
        this.distance = scan.nextDouble();
        System.out.println("");
        Utils.printInfo("Successfully updated distance option");
    }

    public void setPriceSortOption(){
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter a valid price sort option (ASC/DESC/NONE): ");
        String userPriceSort = scan.next();
        if(userPriceSort.equals("ASC") || userPriceSort.equals("DESC") || userPriceSort.equals("NONE")){
            System.out.println("");
            this.priceSort = userPriceSort;
            Utils.printInfo("Successfully updated price sort option.");
            return;
        }
        System.out.println("");
        Utils.printInfo("Please select a valid option.");
    }

    public void setPriceRangeOption(){
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter the MIN price for the price range option: ");
        double minPrice = scan.nextDouble();
        System.out.print("Please enter the MAX price for the price range option: ");
        double maxPrice = scan.nextDouble();
        if (minPrice > maxPrice){
            Utils.printInfo("Min price is greater than to max price. Please input a valid range.");
            return;
        }
        this.priceRange[0] = minPrice;
        this.priceRange[1] = maxPrice;
        System.out.println("");
        Utils.printInfo("Successfully updated price range option.");
    }
    public void setAmenitiesOption(){
        amenitiesList.clear();
        System.out.println("Please insert one amenity at a time. Press q to finish.");
        boolean stop = false;
        Scanner scan = new Scanner(System.in);
        String amen;
        while (!stop) {
            amen = scan.nextLine();
            if (!amen.equals("q")) {
                amenitiesList.add(amen);
            } else {
                stop = true;
            }
        }
        Utils.printInfo("Successfully updated amenities list option.");
    }
    public void setDataRangeOption(){
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter the START date for the date range option: ");
        String startDate = scan.next();
        System.out.print("Please enter the END price for the date range option: ");
        String endDate = scan.next();
        if (Utils.formatStringToDate(startDate).compareTo(Utils.formatStringToDate(endDate)) > 0){
            Utils.printInfo("Start date is after end date. Please input a valid range.");
            return;
        }
        this.dateRange[0] = startDate;
        this.dateRange[1] = endDate;
        System.out.println("");
        Utils.printInfo("Successfully updated date range option.");
    }
    public void viewAllFilterOptions(){
        System.out.println("Below are all the filter options: ");
        System.out.println(" - Distance = " + distance);
        System.out.println(" - Price Sort Option (ASC/DESC) = " + priceSort);
        System.out.println(" - Amenities List = " + amenitiesList);
        System.out.format(" - Price Range (min - max) = $%f - $%f\n", priceRange[0], priceRange[1]);
        System.out.format(" - Date Range (start -> end) = %s -> %s\n", dateRange[0], dateRange[1]);
    }
    public void searchListingByAddress() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter a valid address: ");
        String userAddress = scan.nextLine();

        if (!MySQLObj.checkIfAddressIsValid(userAddress)){
            Utils.printInfo("Address is not valid. (It is either unavailable or does not exist)");
            return;
        }

        ArrayList<ArrayList<Object>> sbaList = new ArrayList<>();
        ResultSet sbars = null;
        try{
            sbars = MySQLObj.searchByAddress(userAddress, this);
            while (sbars.next()) {
                ArrayList<Object> sbaData = new ArrayList<Object>();
                sbaData.add(sbars.getInt("Listing_ID"));
                String address = Utils.formatAddress(
                        new Object[]{
                                sbars.getInt("num"),
                                sbars.getString("street"),
                                sbars.getString("ci.name"),
                                sbars.getString("postalcode")
                        }
                );
                sbaData.add(address);
                sbaData.add(sbars.getDouble("avgPrice"));
                sbaList.add(sbaData);
            }
            if(!sbaList.isEmpty()){
                Utils.printTable(new String[]{"Listing ID", "Address", "Average Price"}, sbaList);
            }
            else{
                Utils.printInfo("No listings match the given criteria. Please try again.");
            }
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public void searchListingByPostalCode(){
        System.out.println("Search listing by postal code");
    }
    public void searchListingByCoordinates() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.print("Please enter a valid latitude: ");
        double searchLat = scan.nextDouble();
        System.out.print("Please enter a valid longitude: ");
        double searchLon = scan.nextDouble();

        ArrayList<ArrayList<Object>> sbcList = new ArrayList<>();
        ResultSet sbcrs = null;
        try{
            sbcrs = MySQLObj.searchByCoordinates(searchLat, searchLon, this);
            while (sbcrs.next()) {
                ArrayList<Object> sbcData = new ArrayList<Object>();
                sbcData.add(sbcrs.getInt("Listing_ID"));
                String address = Utils.formatAddress(
                        new Object[]{
                                sbcrs.getInt("num"),
                                sbcrs.getString("street"),
                                sbcrs.getString("ci.name"),
                                sbcrs.getString("postalcode")
                        }
                );
                sbcData.add(address);
                sbcData.add(sbcrs.getDouble("avgPrice"));
                sbcData.add(sbcrs.getDouble("distance"));
                sbcList.add(sbcData);
            }
            if(!sbcList.isEmpty()){
                Utils.printTable(new String[]{"Listing ID", "Address", "Average Price", "Distance"}, sbcList);
            }
            else{
                Utils.printInfo("No listings match the given criteria. Please try again.");
            }
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
}
