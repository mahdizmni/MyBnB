import java.sql.*;
import java.text.ParseException;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MySQLObj {
    private final String uriDb = "jdbc:mysql://localhost:3306/MyBnB";
    private final String username = "root";
    private final String password = "";
    private static Connection con;
    private static Statement st;
    private static MySQLObj instance = null;

    private MySQLObj() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            con = DriverManager.getConnection(uriDb, username, password);
            st = con.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static MySQLObj getInstance() {
        if (instance == null) {
            instance = new MySQLObj();
        }
        return instance;
    }

    public static void closeConnection() {
        try {
            st.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static User validateLoginCredentials(String email, String password) throws SQLException {
        String query = "SELECT * FROM User WHERE email = '%s' AND password = '%s'";
        query = String.format(query, email, password);
        ResultSet rs = st.executeQuery(query);
        if (rs.next()) {
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

    public static boolean registerUser(int sin, String firstname, String lastname, int birthDate, String occupation,
            String email, String password, String creditcard) {
        try {
            String query = "INSERT INTO User VALUES (%d,'%s','%s',%d,'%s','%s','%s','%s')";
            query = String.format(query, sin, firstname, lastname, birthDate, occupation, email, password, creditcard);
            st.execute(query);
            return true;
        } catch (Exception e) {
            Utils.printError("Failed to register user: ", e.getMessage());
        }
        return false;
    }

    public ResultSet getAllUsers() throws SQLException {
        String query = "SELECT * FROM User";
        return st.executeQuery(query);
    }

    public static ResultSet getAllAvailableListings() throws SQLException {
        // price of the listing is the average price of all available periods of that particular listing
        String query = """
                SELECT DISTINCT ai.Listing_ID, postalcode, num, street, AVG(price) FROM AvailableIn AS ai
                JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                GROUP BY ai.Listing_ID, postalcode, num, street
                ORDER BY ai.Listing_ID;
                """;
        return st.executeQuery(query);
    }

    public static ResultSet getAllAvailablePeriodsOfListing(int listing_ID) throws SQLException {
        String query = """
                SELECT DISTINCT Period_ID, ai.Listing_ID, postalcode, num, street, price, p.start, p.end FROM AvailableIn AS ai
                JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                JOIN Period AS p ON p.ID = ai.Period_ID
                WHERE ai.Listing_ID = ?;
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, listing_ID);
        return preparedQuery.executeQuery();
    }

    public static ResultSet getAvailablePeriodFromStartEnd(int listing_ID, String start, String end) throws SQLException {
        String query = """
                SELECT p.*, ai.price FROM AvailableIn AS ai
                JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                JOIN Period AS p ON p.ID = ai.Period_ID
                WHERE ai.Listing_ID = ?
                AND (DATE(?) BETWEEN p.start AND p.end)
                AND (DATE(?) BETWEEN p.start AND p.end);
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, listing_ID);
        preparedQuery.setString(2, start);
        preparedQuery.setString(3, end);
        return preparedQuery.executeQuery();
    }

    public static Boolean genericCheckIfIDExists( String table, String field, int ID) throws SQLException {
        String query = """
                SELECT * FROM %s
                WHERE %s = %d
                """;
        return st.executeQuery(String.format(query, table, field, ID)).next();
    }

    public static void genericRemoveByID(String table, String field, int ID) throws SQLException {
        String query = """
                DELETE FROM %s
                WHERE %s = %d
                """;
        st.executeUpdate(String.format(query, table, field, ID));
    }

    public static void createBooking(int renter_sin, int listing_ID, String start, String end) throws SQLException {
        String query = """
                INSERT INTO Books (Renter_SIN, Listing_ID, start, end, isReserved)
                VALUES (?, ?, ?, ?, true);
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, renter_sin);
        preparedQuery.setInt(2, listing_ID);
        preparedQuery.setString(3, start);
        preparedQuery.setString(4, end);
        preparedQuery.executeUpdate();
    }

    public static void editPeriod(int ID, String start, String end) throws SQLException {
        String query = """
                UPDATE Period
                SET
                    start = ?,
                    end = ?
                WHERE ID = ?
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setString(1, start);
        preparedQuery.setString(2, end);
        preparedQuery.setInt(3, ID);
        preparedQuery.executeUpdate();
    }

    public static void createAvailablePeriod(int Listing_ID, String start, String end, double price) throws SQLException {
        String query1 = "INSERT INTO Period (start, end) VALUES (?, ?);";
        String query2 = "INSERT INTO AvailableIn (Listing_ID, Period_ID, price) VALUES (?, LAST_INSERT_ID(), ?);";
        PreparedStatement preparedQuery1 = con.prepareStatement(query1);
        preparedQuery1.setString(1, start);
        preparedQuery1.setString(2, end);
        preparedQuery1.executeUpdate();
        PreparedStatement preparedQuery2 = con.prepareStatement(query2);
        preparedQuery2.setInt(1, Listing_ID);
        preparedQuery2.setDouble(2, price);
        preparedQuery2.executeUpdate();
    }

    public static void cancelBooking(int ID, int renter_sin) throws SQLException {
        String query = """
                UPDATE Books
                SET isReserved = false
                WHERE BookingID = ? AND Renter_SIN = ?
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, ID);
        preparedQuery.setInt(2, renter_sin);
        preparedQuery.executeUpdate();

        String query2 = """
                INSERT INTO Cancelled (BookingID, date) VALUES (?, ?);
                """;
        PreparedStatement preparedQuery2 = con.prepareStatement(query2);
        preparedQuery2.setInt(1, ID);
        preparedQuery2.setString(2, Utils.getTodayString());
        preparedQuery2.executeUpdate();
    }

    public static Boolean checkIfRenterCreatedBooking(int ID, int renter_sin) throws SQLException {
        String query = """
                SELECT * FROM Books
                WHERE BookingID = ? AND Renter_SIN = ?
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, ID);
        preparedQuery.setInt(2, renter_sin);
        return preparedQuery.executeQuery().next();
    }

    public static Boolean checkIfBookingIsReserved(int ID, int renter_sin) throws SQLException {
        String query = """
                SELECT * FROM Books
                WHERE BookingID = ? AND isReserved = 1 AND Renter_SIN = ?
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, ID);
        preparedQuery.setInt(2, renter_sin);
        return preparedQuery.executeQuery().next();
    }

    public static ResultSet getRenterHistory(int renter_sin) throws SQLException {
        String query = """
                SELECT BookingID, b.Listing_ID, start, end, isReserved, a.*, ci.name, co.name
                FROM Books AS b
                JOIN LocatedIn AS li ON b.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                JOIN IsIn AS ii ON ii.Address_ID = a.ID
                JOIN City AS ci ON ii.City_ID = ci.ID
                JOIN BelongsTo AS bo on bo.City_ID = ci.ID
                JOIN Country AS co on co.ID = bo.Country_ID
                WHERE b.Renter_SIN = ?
                ORDER BY start ASC;
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, renter_sin);
        return preparedQuery.executeQuery();
    }

    public static ResultSet getPastBookings(int renter_sin) throws SQLException {
        String query = """
                SELECT BookingID, o.Host_SIN, b.Listing_ID, start, end, a.*, ci.name, co.name
                FROM Books AS b
                JOIN LocatedIn AS li ON b.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                JOIN IsIn AS ii ON ii.Address_ID = a.ID
                JOIN City AS ci ON ii.City_ID = ci.ID
                JOIN BelongsTo AS bo ON bo.City_ID = ci.ID
                JOIN Country AS co ON co.ID = bo.Country_ID
                JOIN Owns AS o ON o.Listing_ID = b.Listing_ID
                WHERE b.Renter_SIN = ? AND b.isReserved = true AND b.Renter_SIN != o.Host_SIN
                ORDER BY o.Host_SIN ASC;
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, renter_sin);
        return preparedQuery.executeQuery();
    }

    public static ResultSet searchByAddress(String address, Object obj) throws SQLException {
        if(obj.getClass() != Search.class){
            return null;
        }
        Search sObj = (Search) obj;

        String baseQuery = "SELECT DISTINCT ai.Listing_ID, a.*, ci.name, AVG(Price) as avgPrice\n" +
                           "FROM AvailableIn AS ai\n" +
                           "JOIN Period AS p ON ai.Period_ID = p.ID\n" +
                           "JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID\n" +
                           "JOIN Address AS a ON li.Address_ID = a.ID\n" +
                           "JOIN IsIn AS ii ON ii.Address_ID = a.ID\n" +
                           "JOIN City AS ci ON ii.City_ID = ci.ID\n" +
                           "JOIN BelongsTo AS bo ON bo.City_ID = ci.ID\n" +
                           "JOIN Country AS co ON co.ID = bo.Country_ID\n" +
                           "JOIN Owns AS o ON o.Listing_ID = li.Listing_ID\n" +
                           "WHERE CONCAT(num, ' ', street, ', ', ci.name, ', ', postalcode) = ?\n" +
                           "    AND EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID AND price BETWEEN ? AND ?\n" +
                           "    )\n" +
                           "    AND EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        Join Period AS p2 ON ai2.Period_ID = p2.ID\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID\n" +
                           "              AND (p2.start BETWEEN DATE(?) AND DATE(?))\n" +
                           "              AND (p2.end BETWEEN DATE(?) AND DATE(?))\n" +
                           "    )\n" +
                           "    %s\n" +
                           "GROUP BY ai.Listing_ID, ci.name\n" +
                           "%s";

        String orderByString = "";
        String amenitiesQueryString = "";
        if (sObj.priceSort.equals("ASC") || sObj.priceSort.equals("DESC")){
            orderByString += "ORDER BY avgPrice " + sObj.priceSort;
        }

        if(!sObj.amenitiesList.isEmpty()){
            amenitiesQueryString = "AND EXISTS(\n" +
                                   "    SELECT DISTINCT type\n" +
                                   "    FROM AvailableIn AS ai2\n" +
                                   "    JOIN Has AS h ON h.Listing_ID = ai2.Listing_ID\n" +
                                   "    JOIN Amenities AS amen ON h.Amenities_ID = amen.ID\n" +
                                   "    WHERE ai2.Listing_ID = ai.Listing_ID AND amen.type IN (" +
                                   Utils.repeatString("?", sObj.amenitiesList.size()) + ")\n" +
                                   ")";
        }

        baseQuery = String.format(baseQuery, amenitiesQueryString, orderByString);
        sObj.viewAllFilterOptions();
        PreparedStatement preparedQuery = con.prepareStatement(baseQuery);
        preparedQuery.setString(1, address);
        preparedQuery.setDouble(2, sObj.priceRange[0]);
        preparedQuery.setDouble(3, sObj.priceRange[1]);
        preparedQuery.setString(4, sObj.dateRange[0]);
        preparedQuery.setString(5, sObj.dateRange[1]);
        preparedQuery.setString(6, sObj.dateRange[0]);
        preparedQuery.setString(7, sObj.dateRange[1]);
        int count = 8;
        for (String amenities: sObj.amenitiesList) {
            preparedQuery.setString(count, amenities);
            count ++;
        }
        return preparedQuery.executeQuery();
    }

    public static ResultSet searchByCoordinates(double searchLat, double searchLon, Object obj) throws SQLException {
        if(obj.getClass() != Search.class){
            return null;
        }
        Search sObj = (Search) obj;

        String baseQuery = "SELECT DISTINCT ai.Listing_ID, a.*, ci.name, AVG(Price) as avgPrice,\n" +
                           "    ST_Distance_Sphere(point(?, ?),point(l.longitude, l.latitude))*0.00099978624 AS distance\n" +
                           "FROM AvailableIn AS ai\n" +
                           "JOIN Period AS p ON ai.Period_ID = p.ID\n" +
                           "JOIN Listings AS l ON l.ID = ai.Listing_ID\n" +
                           "JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID\n" +
                           "JOIN Address AS a ON li.Address_ID = a.ID\n" +
                           "JOIN IsIn AS ii ON ii.Address_ID = a.ID\n" +
                           "JOIN City AS ci ON ii.City_ID = ci.ID\n" +
                           "JOIN BelongsTo AS bo ON bo.City_ID = ci.ID\n" +
                           "JOIN Country AS co ON co.ID = bo.Country_ID\n" +
                           "JOIN Owns AS o ON o.Listing_ID = li.Listing_ID\n" +
                           "WHERE EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID AND price BETWEEN ? AND ?\n" +
                           "    )\n" +
                           "    AND EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        Join Period AS p2 ON ai2.Period_ID = p2.ID\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID\n" +
                           "              AND (p2.start BETWEEN DATE(?) AND DATE(?))\n" +
                           "              AND (p2.end BETWEEN DATE(?) AND DATE(?))\n" +
                           "    )\n" +
                           "    %s\n" +
                           "GROUP BY ai.Listing_ID, ci.name\n" +
                           "HAVING distance < ?\n" +
                           "ORDER BY distance ASC" + "%s";

        String orderByString = "";
        String amenitiesQueryString = "";
        if (sObj.priceSort.equals("ASC") || sObj.priceSort.equals("DESC")){
            orderByString += ", avgPrice " + sObj.priceSort;
        }

        if(!sObj.amenitiesList.isEmpty()){
            amenitiesQueryString = "AND EXISTS(\n" +
                                   "    SELECT DISTINCT type\n" +
                                   "    FROM AvailableIn AS ai2\n" +
                                   "    JOIN Has AS h ON h.Listing_ID = ai2.Listing_ID\n" +
                                   "    JOIN Amenities AS amen ON h.Amenities_ID = amen.ID\n" +
                                   "    WHERE ai2.Listing_ID = ai.Listing_ID AND amen.type IN (" +
                                   Utils.repeatString("?", sObj.amenitiesList.size()) + ")\n" +
                                   ")";
        }

        baseQuery = String.format(baseQuery, amenitiesQueryString, orderByString);
        sObj.viewAllFilterOptions();
        PreparedStatement preparedQuery = con.prepareStatement(baseQuery);
        preparedQuery.setDouble(1, searchLon);
        preparedQuery.setDouble(2, searchLat);
        preparedQuery.setDouble(3, sObj.priceRange[0]);
        preparedQuery.setDouble(4, sObj.priceRange[1]);
        preparedQuery.setString(5, sObj.dateRange[0]);
        preparedQuery.setString(6, sObj.dateRange[1]);
        preparedQuery.setString(7, sObj.dateRange[0]);
        preparedQuery.setString(8, sObj.dateRange[1]);
        int count = 9;
        for (String amenities: sObj.amenitiesList) {
            preparedQuery.setString(count, amenities);
            count ++;
        }
        preparedQuery.setDouble(count, sObj.distance);
        return preparedQuery.executeQuery();
    }

    public static ResultSet searchByPostalCode(String searchPostalCode, Object obj) throws SQLException {
        if(obj.getClass() != Search.class){
            return null;
        }
        Search sObj = (Search) obj;

        String baseQuery = "SELECT DISTINCT ai.Listing_ID, a.*, ci.name, AVG(Price) as avgPrice\n" +
                           "FROM AvailableIn AS ai\n" +
                           "JOIN Period AS p ON ai.Period_ID = p.ID\n" +
                           "JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID\n" +
                           "JOIN Address AS a ON li.Address_ID = a.ID\n" +
                           "JOIN IsIn AS ii ON ii.Address_ID = a.ID\n" +
                           "JOIN City AS ci ON ii.City_ID = ci.ID\n" +
                           "JOIN BelongsTo AS bo ON bo.City_ID = ci.ID\n" +
                           "JOIN Country AS co ON co.ID = bo.Country_ID\n" +
                           "JOIN Owns AS o ON o.Listing_ID = li.Listing_ID\n" +
                           "WHERE SUBSTR(postalcode, 1, 3) = SUBSTR(?, 1, 3)\n" +
                           "    AND EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID AND price BETWEEN ? AND ?\n" +
                           "    )\n" +
                           "    AND EXISTS(\n" +
                           "        SELECT *\n" +
                           "        FROM AvailableIn AS ai2\n" +
                           "        Join Period AS p2 ON ai2.Period_ID = p2.ID\n" +
                           "        WHERE ai2.Listing_ID = ai.Listing_ID\n" +
                           "              AND (p2.start BETWEEN DATE(?) AND DATE(?))\n" +
                           "              AND (p2.end BETWEEN DATE(?) AND DATE(?))\n" +
                           "    )\n" +
                           "    %s\n" +
                           "GROUP BY ai.Listing_ID, ci.name\n" +
                           "%s";

        String orderByString = "";
        String amenitiesQueryString = "";
        if (sObj.priceSort.equals("ASC") || sObj.priceSort.equals("DESC")){
            orderByString += "ORDER BY avgPrice " + sObj.priceSort;
        }

        if(!sObj.amenitiesList.isEmpty()){
            amenitiesQueryString = "AND EXISTS(\n" +
                                   "    SELECT DISTINCT type\n" +
                                   "    FROM AvailableIn AS ai2\n" +
                                   "    JOIN Has AS h ON h.Listing_ID = ai2.Listing_ID\n" +
                                   "    JOIN Amenities AS amen ON h.Amenities_ID = amen.ID\n" +
                                   "    WHERE ai2.Listing_ID = ai.Listing_ID AND amen.type IN (" +
                                   Utils.repeatString("?", sObj.amenitiesList.size()) + ")\n" +
                                   ")";
        }

        baseQuery = String.format(baseQuery, amenitiesQueryString, orderByString);
        sObj.viewAllFilterOptions();
        PreparedStatement preparedQuery = con.prepareStatement(baseQuery);
        preparedQuery.setString(1, searchPostalCode);
        preparedQuery.setDouble(2, sObj.priceRange[0]);
        preparedQuery.setDouble(3, sObj.priceRange[1]);
        preparedQuery.setString(4, sObj.dateRange[0]);
        preparedQuery.setString(5, sObj.dateRange[1]);
        preparedQuery.setString(6, sObj.dateRange[0]);
        preparedQuery.setString(7, sObj.dateRange[1]);
        int count = 8;
        for (String amenities: sObj.amenitiesList) {
            preparedQuery.setString(count, amenities);
            count ++;
        }
        return preparedQuery.executeQuery();
    }

    public static boolean checkIfAddressIsValid(String address) throws SQLException {
        String query = """
                SELECT *
                FROM AvailableIn AS ai
                JOIN Period AS p ON ai.Period_ID = p.ID
                JOIN LocatedIn AS li ON ai.Listing_ID = li.Listing_ID
                JOIN Address AS a ON li.Address_ID = a.ID
                JOIN IsIn AS ii ON ii.Address_ID = a.ID
                JOIN City AS ci ON ii.City_ID = ci.ID
                JOIN BelongsTo AS bo ON bo.City_ID = ci.ID
                JOIN Country AS co ON co.ID = bo.Country_ID
                JOIN Owns AS o ON o.Listing_ID = li.Listing_ID
                WHERE CONCAT(num, ' ', street, ', ', ci.name, ', ', postalcode) = ?
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setString(1, address);
        return preparedQuery.executeQuery().next();
    }

    public static Boolean checkIfRenterHasRentedFromHost(int host_sin, int renter_sin) throws SQLException {
        String query = """
                SELECT *
                FROM Books AS b
                JOIN Owns AS o ON b.Listing_ID = o.Listing_ID
                WHERE o.Host_SIN = ? AND b.Renter_SIN = ?;
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, host_sin);
        preparedQuery.setInt(2, renter_sin);
        return preparedQuery.executeQuery().next();
    }

    public static Boolean checkIfRenterHasBookedListing(int listing_id, int renter_sin) throws SQLException {
        String query = """
                SELECT *
                FROM Books AS b
                WHERE b.Listing_ID = ? AND b.Renter_SIN = ?;
                """;
        PreparedStatement preparedQuery = con.prepareStatement(query);
        preparedQuery.setInt(1, listing_id);
        preparedQuery.setInt(2, renter_sin);
        return preparedQuery.executeQuery().next();
    }
    public static boolean CommentsOnUser(int user1, int user2, String text) throws SQLException {
        try {
            String query = "INSERT INTO CommentsOnUser(User1_SIN, User2_SIN, text) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            ps.setString(3, text);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean RatesOnUser(int user1, int user2, int score) throws SQLException {
        try {
            if (score < 1 || score > 5) {
                System.out.println("out of bounds value for score. choose 1-5");
                return false;
            }
            String query = "INSERT INTO RatedOnUser(User1_SIN, User2_SIN, score) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, user1);
            ps.setInt(2, user2);
            ps.setInt(3, score);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public static boolean CommentsOnListing(int listing_id, int user_sin, String text) throws SQLException {
        try {
            String query = "INSERT INTO CommentsOnListing(Listing_ID, User_SIN, text) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,listing_id);
            ps.setInt(2,user_sin);
            ps.setString(3,text);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean RatesOnListing(int listing_id, int user_sin, int score) throws SQLException {
        try {
            String query = "INSERT INTO RatedOnListing(Listing_ID, User_SIN, score) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,listing_id);
            ps.setInt(2,user_sin);
            ps.setInt(3,score);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
            return false;
        }
    }

    public static void addToListings(double latitude, double longitude) throws SQLException {
        try {
            String query = "INSERT INTO Listings(latitude, longitude) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setDouble(1, latitude);
            ps.setDouble(2, longitude);
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
            return -1;
        }
    }

    public static boolean addToCountry(String country) {
        try {
            String query = "INSERT INTO Country (name) VALUES (?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, country);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean addToCity(String city) {
        try {
            String query = "INSERT INTO City (name) VALUES (?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, city);
            ps.executeUpdate();
            return true;
        } catch (SQLException e ) {
            return false;
        }
    }
    public static boolean addToAddress(String postalcode, String street, int num) {
        try {
            String query = "INSERT INTO Address (postalcode, street, num) VALUES(?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, postalcode);
            ps.setString(2, street);
            ps.setInt(3, num);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean addToType(String type) throws SQLException {
        try {
            String query = "INSERT INTO Type (type) VALUES (?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, type);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static void addToListingsType(int listings_id, int type_id) throws SQLException {
        String query = "INSERT INTO ListingsType VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, listings_id);
        ps.setInt(2, type_id);
        ps.executeUpdate();
    }
    public static void addToBelongsTo(int city_id, int country_id) throws SQLException {
        String query = "INSERT INTO BelongsTo VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, city_id);
        ps.setInt(2, country_id);
        ps.executeUpdate();
    }
    public static boolean addToAmenities(String name) throws SQLException {
        try {
            String query = "INSERT INTO Amenities(type) VALUES (?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }
    public static void addToLocatedIn(int address_id, int listing_id) throws SQLException {
        String query = "INSERT INTO LocatedIn VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, address_id);
        ps.setInt(2, listing_id);
        ps.executeUpdate();
    }
    public static void addToIsIn(int address_id, int city_id) throws SQLException {
        String query = "INSERT INTO IsIn VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, address_id);
        ps.setInt(2, city_id);
        ps.executeUpdate();
    }
    public static boolean addToPeriod(int start, int end) throws SQLException {
        try {
            String query = "INSERT INTO Period(start, end) VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, start);
            ps.setInt(2, end);
            ps.executeUpdate();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }
    public static void addToHas(int amenities_id, int listing_id) throws SQLException {
        String query = "INSERT INTO Has VALUES (?, ?)";
        PreparedStatement ps = con.prepareStatement(query);
        ps.setInt(1, amenities_id);
        ps.setInt(2, listing_id);
        ps.executeUpdate();
    }
    public static boolean addToOwns(int host_sin, int listing_id) throws SQLException {
        try {
            String query = "INSERT INTO Owns VALUES (?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, host_sin);
            ps.setInt(2, listing_id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static boolean addToAvailableIn(int listing_id, int period_id, float price) throws SQLException {
        try {
            String query = "INSERT INTO AvailableIn VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.setInt(2, period_id);
            ps.setFloat(3, price);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static int getPeriodID(int start, int end) throws SQLException {
        try {
            String query = "SELECT ID FROM Period WHERE start = ? AND end = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, start);
            ps.setInt(2, end);
            ResultSet rs = ps.executeQuery();
            rs.next();
            if (rs.next())
                return rs.getInt(1);
            else
                return -1;
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int getAddressID(String postalcode, int num) throws SQLException {
        try {
            String query = "SELECT ID FROM Address WHERE postalcode = ? AND num = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, postalcode);
            ps.setInt(2, num);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int getCountryID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Country WHERE name = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int getAmenitiesID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Amenities WHERE type = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int getCityID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM City WHERE name = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int getTypeID(String name) throws SQLException {
        try {
            String query = "SELECT ID FROM Type WHERE type = ?";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e)
        {
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
    public static ResultSet AllListingsWithPrice(int listing_id) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT AvailableIn.Period_ID, Period.start, Period.end, AvailableIn.price
                    FROM AvailableIn JOIN Period ON Period.ID = AvailableIn.Period_ID
                        JOIN Listings ON Listings.ID = AvailableIn.Listing_ID
                    WHERE Listings.ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            rs = ps.executeQuery();

            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewAllListingsWithPrice(int host_sin, int listing_id) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = AllListingsWithPrice(listing_id);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Period_ID"));
                listingData.add(listings.getString("start"));
                listingData.add(listings.getString("end"));
                listingData.add(listings.getFloat("price"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"ID", "start date", "end date", "price"},listingsList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static boolean ViewListingsHistory(int host_sin) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = ListingsHistory(host_sin);
            if (listings == null) {
                System.out.println("empty");
                return false;
            }
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Listing_ID"));
                listingData.add(listings.getInt("Renter_SIN"));
                listingData.add(listings.getString("start"));
                listingData.add(listings.getString("end"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Listing ID", "User ID", "Start of reservation", "End of reservation"},listingsList);
            return true;
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
            return  false;
        }
    }
    public static ResultSet ListingsHistory(int host_sin) {
        ResultSet rs = null;
        try {
            String lastyear = Utils.formatDateToString(Utils.addDays(Utils.getToday(), -365));
            String query = """
                    SELECT o.Listing_ID, b.Renter_SIN, b.start, b.end FROM
                    Owns AS o JOIN Books AS b ON o.Listing_ID = b.Listing_ID
                    WHERE b.isReserved = True AND b.end >= ? AND o.Listing_ID IN (SELECT Listing_ID FROM OWNS
                                                                    WHERE Host_SIN = ?);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, lastyear);
            ps.setInt(2, host_sin);
            rs = ps.executeQuery();

            return rs;
        } catch (SQLException e) {
            return rs;
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
    public static int PeriodIDToListingID(int period_id) {
        try {
            String query = """
                    SELECT Listing_ID FROM AvailableIn
                    WHERE Period_ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, period_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            else
                return -1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
    public static int BookingIDToListingID(int booking_id) {
        try {
            String query = """
                    SELECT Listing_ID FROM Books
                    WHERE BookingID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, booking_id);
            ResultSet rs = ps.executeQuery();
            if (rs.next())
                return rs.getInt(1);
            else
                return -1;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }
    public static boolean CancelBookedListing(int booking_id) {
        try {
            String query = """
                    UPDATE Books
                    set isReserved = False
                    WHERE BookingID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, booking_id);
            String today = Utils.getTodayString();
            query = """
                    INSERT INTO Cancelled VALUES(?, ?);
                    """;
            ps = con.prepareStatement(query);
            ps.setInt(1, booking_id);
            ps.setString(2, today);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
    public static void ViewGetHistory(int host_sin) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try{
            listings = GetHistory(host_sin);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Listing_ID"));
                listingData.add(listings.getString("firstname"));
                listingData.add(listings.getString("lastname"));
                listingData.add(listings.getString("start"));
                listingData.add(listings.getString("end"));
                listingData.add(listings.getBoolean("isReserved"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Listing ID", "first name", "last name", "start", "end", "Did not get cancelled"},listingsList);
        }
        catch (Exception e){
            Utils.printError("Something went wrong!", e.getMessage());
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
    public static boolean RemoveListing(int listing_id) {
        try {
            String query = """
                    DELETE FROM Listings
                    WHERE Listings.ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean isReserved (int booking_id) {
        try {
            String query = """
                    SELECT isReserved FROM Books
                    WHERE BookingID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, booking_id);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getBoolean(1);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean OwnsListing(int listing_id, int host_sin) {
        try {
            String query = """
                    SELECT ID FROM Listings
                    WHERE ID = ? AND ID IN (SELECT Listing_ID FROM Owns
                                            WHERE Host_SIN = ?);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.setInt(2, host_sin);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean MyRenter(int renter_sin, int host_sin) {
        try {
            String query = """
                    SELECT b.Renter_SIN FROM
                    Books AS b JOIN Owns AS o ON b.Listing_ID = o.Listing_ID
                    WHERE b.Renter_SIN = ? AND b.Listing_ID IN (SELECT Listing_ID FROM OWNS
                                                                    WHERE Host_SIN = ?)
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, renter_sin);
            ps.setInt(2, host_sin);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean ValidateListingID(int listing_id) {
        try {
            String query = """
                    SELECT ID FROM Listings
                    WHERE ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean ValidateUserID(int sin) {
        try {
            String query = """
                    SELECT SIN FROM User
                    WHERE SIN = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, sin);
            return ps.executeQuery().next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean UpdateAvailability (int listing_id, int period_id, int previous_period_id) {
        try {
            String query = """
                    UPDATE AvailableIn
                    SET Period_ID = ?
                    WHERE Listing_ID = ? AND Period_ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setFloat(1, period_id);
            ps.setInt(2, listing_id);
            ps.setInt(3, previous_period_id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static boolean UpdatePrice (int listing_id, int period_id, float price) {
        try {
            String query = """
                    UPDATE AvailableIn
                    SET price = ?
                    WHERE Listing_ID = ? AND Period_ID = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setFloat(1, price);
            ps.setInt(2, listing_id);
            ps.setInt(3, period_id);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static int BookedWithinAvailability(int listing_id, int period_id) throws SQLException, ParseException {
        ResultSet listings = WithinAvailability(listing_id, period_id);
        if (listings == null)
            return 0;
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        while (listings.next()) {
            ArrayList<Object> listingData = new ArrayList<Object>();
            listingData.add(listings.getString("Ostart"));
            listingData.add(listings.getString("end"));
            listingData.add(listings.getString("start"));
            listingData.add(listings.getString("Oend"));
            listingsList.add(listingData);
        }

        if (listingsList.size() == 0) { // no one has even attempted to book a place. free to update
            return 0;
        }
        SimpleDateFormat sdf = new SimpleDateFormat(Utils.sdfPattern);
        for (int i = 0; i < listingsList.size(); i++) {
            Date Ostart = sdf.parse((String) listingsList.get(i).get(0));
            Date end = sdf.parse((String) listingsList.get(i).get(1));
            Date start = sdf.parse((String) listingsList.get(i).get(2));
            Date Oend = sdf.parse((String) listingsList.get(i).get(3));
            if (Utils.IsWithinDate(Ostart, Oend, start, end))
                return -1;
        }

        return 0;
    }
    public static ResultSet WithinAvailability(int listing_id, int period_id) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT p.start AS Ostart ,p.end, b.start , b.end AS Oend FROM
                    Books AS b JOIN AvailableIn ON b.Listing_ID = AvailableIn.Listing_ID
                    JOIN Period AS p ON p.ID = AvailableIn.Period_ID
                    WHERE b.Listing_ID = ? AND p.ID = ? AND b.isReserved = True;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.setInt(2, period_id);
            rs = ps.executeQuery();
            rs.next();
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return rs;
        }
    }

    public static boolean DoesNotOverlap(int start, int end, int listing_id) {
        try {
            String query = """
                    SELECT p.start ,p.end FROM
                    AvailableIn As ai JOIN Period AS p ON ai.Period_ID = p.ID
                    WHERE ai.Listing_ID = ? AND (? > p.end OR ? < p.start);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, listing_id);
            ps.setInt(2, start);
            ps.setInt(3, end);
            ResultSet rs = ps.executeQuery();
            return !rs.next();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return false;
        }
    }
    public static int CountBookings2(String postalcode, String city) {
        try {
            String query = """
                    SELECT Count(b.BookingID) FROM
                    Books AS b JOIN LocatedIn AS l ON l.Listing_ID = b.Listing_ID
                    JOIN Address AS a ON a.ID = l.Address_ID
                    JOIN IsIn ON IsIn.Address_ID = l.Address_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    WHERE a.postalcode = ? AND City.name = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, postalcode);
            ps.setString(2, city);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static int CountBookings1(int start, int end, String city) {
        try {
            String query = """
                    SELECT Count(b.BookingID) FROM
                    Books AS b JOIN LocatedIn AS l ON l.Listing_ID = b.Listing_ID
                    JOIN IsIn ON IsIn.Address_ID = l.Address_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    WHERE b.start <= ? AND b.end >= ? AND City.name = ?;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, end);
            ps.setInt(2, start);
            ps.setString(3, city);
            ResultSet rs = ps.executeQuery();
            rs.next();
            return rs.getInt(1);
        } catch (SQLException e) {
            return -1;
        }
    }
    public static ResultSet GetHistory(int host_sin) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT o.Listing_ID, u.firstname, u.lastname, b.start, b.end, b.isReserved FROM
                    Owns AS o JOIN Books AS b ON b.Listing_ID = o.Listing_ID
                    JOIN User AS u ON u.SIN = b.Renter_SIN 
                    WHERE o.Host_SIN = ?; 
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1, host_sin);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static ResultSet CountListingsByCountryPostalcode() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT Country.name AS "country", City.name AS "city", Address.postalcode, COUNT(l.ID) AS "# of Listing" FROM
                    Listings AS l JOIN LocatedIn AS lo ON lo.Listing_ID = l.ID
                    JOIN IsIn ON IsIn.Address_ID = lo.Address_ID
                    JOIN BelongsTo ON BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country ON Country.ID = BelongsTo.Country_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    JOIN Address ON IsIn.Address_ID = Address.ID
                    GROUP BY Country.name, City.name, Address.postalcode; 
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return rs;
        }
    }
    public static void ViewCountListingsByCountryCityPostalcode() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = CountListingsByCountryPostalcode();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("country"));
                listingData.add(listings.getString("city"));
                listingData.add(listings.getString("postalcode"));
                listingData.add(listings.getString("# of Listing"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Country", "City", "Postal Code", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static ResultSet CountListingsByCountry() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT Country.name, COUNT(l.ID) AS "# of Listing" FROM
                    Listings AS l JOIN LocatedIn AS lo ON   lo.Listing_ID = l.ID
                    JOIN IsIn ON IsIn.Address_ID = lo.Address_ID
                    JOIN BelongsTo ON BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country ON Country.ID = BelongsTo.Country_ID
                    GROUP BY Country.name;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return rs;
        }
    }
    public static ResultSet RankHostsByListingsPerCity() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT u.email, City.name, COUNT(o.Listing_ID) AS "# of Listings" FROM
                    User AS u JOIN Owns AS o ON o.Host_SIN = u.SIN
                    JOIN LocatedIn ON LocatedIn.Listing_ID = o.Listing_ID
                    JOIN IsIn On IsIn.Address_ID = LocatedIn.Address_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    GROUP BY u.email, City.name
                    ORDER BY COUNT(o.Listing_ID) DESC;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return rs;
        }
    }
    public static void ViewRankHostsByListingsPerCity() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = RankHostsByListingsPerCity();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("email"));
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("# of Listing"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"User Email", "City", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static ResultSet RankHostsByListingsPerCountry() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT u.email, Country.name, COUNT(o.Listing_ID) AS "# of Listings" FROM
                    User AS u JOIN Owns AS o ON o.Host_SIN = u.SIN
                    JOIN LocatedIn ON LocatedIn.Listing_ID = o.Listing_ID
                    JOIN IsIn On IsIn.Address_ID = LocatedIn.Address_ID
                    JOIN BelongsTo on BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country on Country.ID = BelongsTo.Country_ID
                    GROUP BY u.email, Country.name
                    ORDER BY COUNT(o.Listing_ID) DESC;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewRankHostsByListingsPerCountry() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = RankHostsByListingsPerCountry();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("email"));
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("# of Listings"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"User Email", "Country", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static void ViewCountListingsByCountry() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = CountListingsByCountry();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("# of Listing"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Country", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static void ViewCountListingsByCountryCity() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = CountListingsByCountryCity();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("city"));
                listingData.add(listings.getString("# of Listing"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Country", "City", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
    public static ResultSet CommercialHosts() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT u.email, co.name, ci.name, COUNT(o.Listing_ID) AS "# of Listings" FROM
                    User AS u JOIN Owns AS o ON o.Host_SIN = u.SIN
                    JOIN LocatedIn ON LocatedIn.Listing_ID = o.Listing_ID
                    JOIN IsIn On IsIn.Address_ID = LocatedIn.Address_ID
                    JOIN BelongsTo on BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country AS co ON co.ID = BelongsTo.Country_ID
                    JOIN City AS ci ON ci.ID = IsIn.City_ID
                    GROUP BY u.email, co.name, ci.name
                    Having COUNT(o.Listing_ID) > (SELECT (SELECT Count(l.ID) FROM
                                                    Listings AS l JOIN LocatedIn ON LocatedIn.Listing_ID = l.ID
                                                    JOIN IsIn On IsIn.Address_ID = LocatedIn.Address_ID
                                                    JOIN BelongsTo on BelongsTo.City_ID = IsIn.City_ID
                                                    JOIN Country on Country.ID = BelongsTo.Country_ID
                                                    JOIN City ON City.ID = IsIn.City_ID
                                                    WHERE City.name = ci.name AND Country.name = co.name) DIV 10);
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewCommercialHosts() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = CommercialHosts();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("email"));
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("city"));
                listingData.add(listings.getString("# of Listings"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"User Email", "Country", "City", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public static ResultSet CountListingsByCountryCity() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT Country.name, City.name AS "city", COUNT(l.ID) AS "# of Listing" FROM
                    Listings AS l JOIN LocatedIn AS lo ON   lo.Listing_ID = l.ID
                    JOIN IsIn ON IsIn.Address_ID = lo.Address_ID
                    JOIN BelongsTo ON BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country ON Country.ID = BelongsTo.Country_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    GROUP BY Country.name, City.name;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }

    public static ResultSet RankRentersByBookingInPeriod(String start, String end) {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT u.email, COUNT(b.BookingID) AS "# of Bookings" FROM
                    User AS u JOIN Books AS b ON b.Renter_SIN = u.SIN
                    WHERE b.start >= ? AND b.end <= ?
                    GROUP BY u.email
                    ORDER BY COUNT(b.BookingID) DESC;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, start);
            ps.setString(2, end);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewRankRentersByBookingInPeriod(String start, String end) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = RankRentersByBookingInPeriod(start, end);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("email"));
                listingData.add(listings.getString("# of Bookings"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"User Email", "# of Bookings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public static ResultSet RankRentersByBookingInPeriodPerCity(String start, String end) {
        ResultSet rs = null;
        try {
            String today = Utils.getTodayString();
            String lastyear = Utils.formatDateToString(Utils.addDays(Utils.getToday(), -365));

            // Condition : Renters with more at least 2 bookings withing last year

            String query = """
                    SELECT u.email, City.name, COUNT(b.BookingID) AS "# of Bookings" FROM
                    User AS u JOIN Books AS b ON b.Renter_SIN = u.SIN
                    JOIN LocatedIn ON LocatedIn.Listing_ID = b.Listing_ID
                    JOIN IsIn ON IsIn.Address_ID = LocatedIn.Address_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    WHERE b.start >= ? AND b.end <= ? AND u.email IN (   SELECT u.email  FROM
                                                                     User AS u JOIN Books AS b ON b.Renter_SIN = u.SIN
                                                                      WHERE b.start >= ? AND b.end <= ?
                                                                      GROUP BY u.email
                                                                       HAVING COUNT(u.email) > 1 )
                    GROUP BY u.email, City.name
                    ORDER BY COUNT(b.BookingID) DESC;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, start);
            ps.setString(2, end);
            ps.setString(3, lastyear);
            ps.setString(4, today);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }

    public static void ViewRankRentersByBookingInPeriodPerCity(String start, String end) {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = RankRentersByBookingInPeriodPerCity(start, end);
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("email"));
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("# of Bookings"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"User Email", "City", "# of Bookings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public static ResultSet GetPopularNounPhrase() {
        ResultSet rs = null;
        try {
            String query = """
                    SELECT Country.name, City.name AS "city", COUNT(l.ID) AS "# of Listing" FROM
                    Listings AS l JOIN LocatedIn AS lo ON   lo.Listing_ID = l.ID
                    JOIN IsIn ON IsIn.Address_ID = lo.Address_ID
                    JOIN BelongsTo ON BelongsTo.City_ID = IsIn.City_ID
                    JOIN Country ON Country.ID = BelongsTo.Country_ID
                    JOIN City ON City.ID = IsIn.City_ID
                    GROUP BY Country.name, City.name;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }

    public static void ViewPopularNounPhrase() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = GetPopularNounPhrase();
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getString("name"));
                listingData.add(listings.getString("city"));
                listingData.add(listings.getString("# of Listing"));
                listingsList.add(listingData);
            }
            Utils.printTable(new String[]{"Country", "City", "# of Listings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public static ResultSet LargestCancellationsHosts() {
        ResultSet rs = null;
        try {
            String today = Utils.getTodayString();
            String lastyear = Utils.formatDateToString(Utils.addDays(Utils.getToday(), -365));

            String query = """
                    SELECT Host_SIN FROM (
                    SELECT o.Host_SIN, COUNT(o.Host_SIN) AS "cancellations" FROM
                    Owns AS o JOIN Books AS b ON b.Listing_ID = o.Listing_ID
                    JOIN Cancelled c on b.BookingID = c.BookingID
                    WHERE c.date >= ? AND c.date <= ?
                    GROUP BY o.Host_SIN
                    HAVING cancellations >= ALL (SELECT COUNT(o.Host_SIN) AS "cancellations" FROM
                    Owns AS o JOIN Books AS b ON b.Listing_ID = o.Listing_ID
                    JOIN Cancelled c on b.BookingID = c.BookingID
                    WHERE c.date >= ? AND c.date <= ?
                    GROUP BY o.Host_SIN)) r ;
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, lastyear);
            ps.setString(2, today);
            ps.setString(3, lastyear);
            ps.setString(4, today);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewLargestCancellationsHosts() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = LargestCancellationsHosts();
            if (listings == null) {
                Utils.printInfo("No cancellations across all hosts.");
                return;
            }
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Renter_SIN"));
                listingData.add(listings.getInt("cancellations"));
                listingsList.add(listingData);
            }
            int renter_sin = (int) listingsList.get(0).get(0);
            int renter_cancellations = (int) listingsList.get(0).get(1);
            Utils.printTable(new String[]{"User Email", "City", "# of Bookings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }

    public static ResultSet LargestCancellationsRenters() {
        ResultSet rs = null;
        try {
            String today = Utils.getTodayString();
            String lastyear = Utils.formatDateToString(Utils.addDays(Utils.getToday(), -365));

            String query = """
                    SELECT Renter_SIN FROM (
                    SELECT b.Renter_SIN, COUNT(b.Renter_SIN) AS "cancellations" FROM
                    Books AS b JOIN Cancelled AS c ON c.BookingID = b.BookingID
                    WHERE c.date >= ? AND c.date <= ?
                    GROUP BY b.Renter_SIN
                    HAVING cancellations >= ALL (SELECT COUNT(b.Renter_SIN) AS "cancellations" FROM
                    Books AS b JOIN Cancelled AS c ON c.BookingID = b.BookingID
                    WHERE c.date >= ? AND c.date <= ?
                    GROUP BY b.Renter_SIN)) r ;
                                                
                    """;
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, lastyear);
            ps.setString(2, today);
            ps.setString(3, lastyear);
            ps.setString(4, today);
            rs = ps.executeQuery();
            return rs;
        } catch (SQLException e) {
            return rs;
        }
    }
    public static void ViewLargestCancellationsRenters() {
        ArrayList<ArrayList<Object>> listingsList = new ArrayList<>();
        ResultSet listings = null;
        try {
            listings = LargestCancellationsRenters();
            if (listings == null) {
                Utils.printInfo("No cancellations across all renters.");
                return;
            }
            while (listings.next()) {
                ArrayList<Object> listingData = new ArrayList<Object>();
                listingData.add(listings.getInt("Renter_SIN"));
                listingData.add(listings.getInt("cancellations"));
                listingsList.add(listingData);
            }
            int renter_sin = (int) listingsList.get(0).get(0);
            int renter_cancellations = (int) listingsList.get(0).get(1);
            Utils.printTable(new String[]{"User Email", "City", "# of Bookings"}, listingsList);
        } catch (Exception e) {
            Utils.printError("Something went wrong!", e.getMessage());
        }
    }
}
