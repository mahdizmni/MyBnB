import java.util.ArrayList;
import java.util.Scanner;

public class Search {

    private int distance = 5;
    private String priceSort = "ASC";
    private ArrayList<String> amenitiesList;
    private double[] priceRange = {50.0, 200.0};
    private String[] availibilityRange;
    private String[] dateRange;

    public Search() {
        amenitiesList = new ArrayList<>();
        String defaultStartDate = Utils.formatDateToString(Utils.getToday());
        String defaultEndDate = Utils.formatDateToString(Utils.addDays(Utils.getToday(), 30));
        availibilityRange = new String[]{defaultStartDate, defaultEndDate};
        dateRange = new String[]{defaultStartDate, defaultEndDate};
    }

    public void mainLoop(){
        Scanner scan = new Scanner(System.in);
        boolean exit = false;
        while(!exit){
            Utils.printMenu("Search Menu",
                    new String[]{
                            "Add specific distance option",
                            "Add price sort (ASC/DESC) option",
                            "Add price range filter option",
                            "Add amenities filter option",
                            "Add availability window filter option",
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
                case "5"-> setAvailabilityOption();
                case "6"-> setDataRangeOption();
                case "7"-> viewAllFilterOptions();
                case "8"-> searchListingByAddress();
                case "9"-> searchListingByPostalCode();
                case "10"-> searchListingByCoordinates();
                case "q" -> exit = true;
            }

            Utils.printInfo("Press enter to go back to menu.");
            scan.nextLine();
        }
    }

    public void setDistanceOption(){
        System.out.println("Add specific distance option");
    }
    public void setPriceSortOption(){
        System.out.println("Add price sort (ASC/DESC) option");
    }
    public void setPriceRangeOption(){
        System.out.println("Add price range filter option");
    }
    public void setAmenitiesOption(){
        System.out.println("Add amenities filter option");
    }
    public void setAvailabilityOption(){
        System.out.println("Add availability window filter option");
    }
    public void setDataRangeOption(){
        System.out.println("Add date range filter option");
    }
    public void viewAllFilterOptions(){
        System.out.println("Below are all the filter options: ");
        System.out.println(" - Distance = " + distance);
        System.out.println(" - Price Sort Option (ASC/DESC) = " + priceSort);
        System.out.println(" - Amenities List = " + amenitiesList);
        System.out.println(" - Price Range (min - max) = $" + priceRange[0] + " - $" + priceRange[1]);
        System.out.println(" - Availability Range (start -> end) = " + availibilityRange[0] + " -> " + availibilityRange[1]);
        System.out.println(" - Date Range (start -> end) = " + dateRange[0] + " -> " + dateRange[1]);
    }
    public void searchListingByAddress(){
        System.out.println("Search listing by address");
    }
    public void searchListingByPostalCode(){
        System.out.println("Search listing by postal code");
    }
    public void searchListingByCoordinates(){
        System.out.println("Search listing by coordinates");
    }
}
