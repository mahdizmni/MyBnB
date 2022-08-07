import java.sql.*;
import java.util.ArrayList;

public class MySQLObj {
    private final String uriDb = "jdbc:mysql://localhost:3306/MyBnB";
    private final String username = "root";
    private final String password = "Mz2468!0";
    private static Connection con;
    private static Statement st;
    private static MySQLObj instance = null;


    private MySQLObj(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(uriDb, username, password);
            st = con.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static MySQLObj getInstance(){
        if (instance == null){
            instance = new MySQLObj();
        }
        return instance;
    }

    public static void closeConnection() {
        try {
            st.close();
            con.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public static User validateLoginCredentials(String email, String password) throws SQLException {
        String query = "SELECT * FROM User WHERE email = '%s' AND password = '%s'";
        query = String.format(query, email, password);
        ResultSet rs = st.executeQuery(query);
        if (rs.next()){
            User user = new User(rs.getInt("SIN"), rs.getString("firstname"), rs.getString("lastname"));
            user.setEmail(rs.getString("email"));
            return user;
        }
        return null;
    }

    public static boolean checkEmailExists(String email) throws SQLException {
        String query = "SELECT * FROM User WHERE email = '%s'";
        query = String.format(query, email);
        ResultSet rs = st.executeQuery(query);
        return rs.next();
    }

    public static boolean registerUser(int sin, String firstname, String lastname, int birthDate, String occupation, String email, String password, String creditcard) {
        try{
            String query = "INSERT INTO User VALUES (%d,'%s','%s',%d,'%s','%s','%s','%s')";
            query = String.format(query, sin, firstname, lastname, birthDate, occupation, email, password, creditcard);
            st.execute(query);
            return true;
        }
        catch(Exception e){
            Utils.printError("Failed to register user: ", e.getMessage());
        }
        return false;
    }

    public ResultSet getAllUsers() throws SQLException {
        String query = "SELECT * FROM User";
        return st.executeQuery(query);
    }

    public static void addToListings(double latitude, double longitude) throws SQLException {
        try {
            String query = "INSERT INTO Listings(latitude, longitude) VALUES (%f, %f)";
            query = String.format(query, latitude, longitude);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
        }
    }

    public static int getRecentID() throws SQLException {
        try {
            String query = "SELECT LAST_INSERT_ID()";
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
 //           System.out.println(e.getMessage());
            return -1;
        }
    }

    public static boolean addToCountry(String country) {
        try {
            String query = "INSERT INTO Country (name) VALUES ('%s')";
            query = String.format(query, country);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
  //          System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean addToCity(String city) {
        try {
            String query = "INSERT INTO City (name) VALUES ('%s')";
            query = String.format(query, city);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e ) {
   //         System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean addToAddress(String postalcode, String street, int num) {
        try {
            String query = "INSERT INTO Address(postalcode, street, num) VALUES('%s', '%s', %d)";
            query = String.format(query, postalcode, street, num);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
    //        System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean addToType(String type) throws SQLException {
        try {
            String query = "INSERT INTO Type (type) VALUES ('%s')";
            query = String.format(query, type);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
     //       System.out.println(e.getMessage());
            return false;
        }
    }
    public static void addToListingsType(int listings_id, int type_id) throws SQLException {
        try {
            String query = "INSERT INTO ListingsType VALUES (%d, %d)";
            query = String.format(query, listings_id, type_id);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
     //       System.out.println(e.getMessage());
        }
    }
    public static void addToBelongsTo(int city_id, int country_id) throws SQLException {
        try {
            String query = "INSERT INTO BelongsTo VALUES ('%s', '%s')";
            query = String.format(query, city_id, country_id);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
        } catch (SQLException e) {
       //     System.out.println(e.getMessage());
        }
    }
    public static boolean addToAmenities(String name) throws SQLException {
        try {
            String query = "INSERT INTO Amenities(type) VALUES ('%s')";
            query = String.format(query, name);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        }
        catch (SQLException e) {
        //    System.out.println(e.getMessage());
            return false;
        }
    }
    public static void addToLocatedIn(int address_id, int listing_id) throws SQLException {
        try {
            String query = "INSERT INTO LocatedIn VALUES (%d, %d)";
            query = String.format(query, address_id, listing_id);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
        }
        catch (SQLException e) {
         //   System.out.println(e.getMessage());
        }
    }
    public static void addToIsIn(int address_id, int city_id) throws SQLException {
        try {
            String query = "INSERT INTO IsIn VALUES (%d, %d)";
            query = String.format(query, address_id, city_id);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
        }
        catch (SQLException e) {
        //    System.out.println(e.getMessage());
        }
    }
    public static boolean addToPeriod(int start, int end) throws SQLException {
        try {
            String query = "INSERT INTO Period(start, end) VALUES (%d, %d)";
            query = String.format(query, start, end);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        }
        catch (SQLException e) {
         //   System.out.println(e.getMessage());
            return false;
        }
    }
    public static void addToHas(int amenities_id, int listing_id) throws SQLException {
        try {
        String query = "INSERT INTO Has VALUES (%d, %d)";
        query = String.format(query, amenities_id, listing_id);
        PreparedStatement ps = con.prepareStatement(query);
        ps.executeUpdate();
        } catch (SQLException e) {
         //   System.out.println(e.getMessage());
        }
    }
    public static boolean addToOwns(int host_sin, int listing_id) throws SQLException {
        try {
            String query = "INSERT INTO Owns VALUES (%d, %d)";
            query = String.format(query, host_sin, listing_id);
            PreparedStatement ps = con.prepareStatement(query);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean addToAvailableIn(int listing_id, int period_id, int price) throws SQLException {
        try {

        String query = "INSERT INTO AvailableIn VALUES (%d, %d, %d)";
        query = String.format(query, listing_id, period_id, price);
        PreparedStatement ps = con.prepareStatement(query);
        ps.executeUpdate();
        return true;
        } catch (SQLException e) {
          //  System.out.println(e.getMessage());
            return false;
        }
    }
    public static int getPeriodID(int start, int end) throws SQLException {
        try {
            String query = "SELECT ID FROM Period WHERE start = %d AND end = %d";
            query = String.format(query, start, end);
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.next())
                return rs.getInt(1);
            else
                return -1;
        } catch (SQLException e) {
         //   System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int getAddressID(String postalcode, int num) throws SQLException {
        try {
        String query = "SELECT ID FROM Address WHERE postalcode = '%s' AND num = %d";
        query = String.format(query, postalcode, num);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
            rs.next();
        return rs.getInt(1);
        } catch (SQLException e) {
//            System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int getCountryID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Country WHERE name = '%s'";
            query = String.format(query, name);
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
 //           System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int getAmenitiesID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Amenities WHERE type = '%s'";
            query = String.format(query, name);
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
  //          System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int getCityID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM City WHERE name = '%s'";
            query = String.format(query, name);
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
   //         System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int getTypeID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Type WHERE type = '%s'";
            query = String.format(query, name);
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e)
        {
    //        System.out.println(e.getMessage());
            return -1;
        }
    }
    public static ResultSet AllHostListings(int host_sin) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT Owns.Listing_ID FROM Owns
                    WHERE Owns.Host_SIN = ?; 
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, host_sin);
            rs = ps.executeQuery();

            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewAllHostListings(int host_sin) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = AllHostListings(host_sin);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Listing_ID"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Listing ID"},listingsList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static ResultSet AllBookedListings(int host_sin) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT DISTINCT Owns.Listing_ID, b.BookingID, b.start, b.end FROM
                    Owns JOIN Books AS b ON
                    Owns.Listing_ID = b.Listing_ID 
                    WHERE Owns.Host_SIN = ? AND b.isReserved = True;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, host_sin);
            rs = ps.executeQuery();

            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static boolean CancelBookedListing(int booking_id, int host_sin) {
        try {
            String query = """
                    UPDATE Books
                    set isReserved = False || isReserved
                    WHERE BookingID = ? AND Listing_ID IN (SELECT Owns.Listing_ID
                                                            WHERE Owns.Host_SIN = ?);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, booking_id);
            ps.setInt(2, host_sin);
            ResultSet rs = ps.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static void ViewAllBookedListings(int host_sin) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = AllBookedListings(host_sin);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Listing_ID"));
                listingData.add(listings.getString("BookingID"));
                listingData.add(listings.getString("start"));
                listingData.add(listings.getString("end"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Listing ID", "Booking ID", "start", "end"},listingsList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static boolean RemoveListing(int listing_id, int host_sin) {
        try {
            String query = """
                    DELETE FROM Listings
                    WHERE Listings.ID = ? AND Listings.ID IN (SELECT Owns.Listing_ID 
                                                    WHERE Owns.Host_SIN = ?);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.setInt(2, host_sin);
            ResultSet rs = ps.executeQuery();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
