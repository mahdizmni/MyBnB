import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

public class Search {

    public int distance = 5;
    public String priceSort = "NONE";
    public ArrayList<String> amenitiesList;
    public double[] priceRange = {50.0, 200.0};
    public String[] dateRange;

    public Search() {
        amenitiesList = new ArrayList<>();
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
        System.out.format("Please enter a valid distance (current distance = %s): ", this.distance);
        this.distance = scan.nextInt();
        System.out.println("");
        Utils.printInfo("Successfully updated distance option");
    }
    public void setPriceSortOption(){
        System.out.println("Add price sort (ASC/DESC/NONE) option");
    }
    public void setPriceRangeOption(){
        System.out.println("Add price range filter option");
    }
    public void setAmenitiesOption(){
        System.out.println("Add amenities filter option");
    }
    public void setDataRangeOption(){
        System.out.println("Add date range filter option");
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
    public void searchListingByCoordinates(){
        System.out.println("Search listing by coordinates");
    }
}
