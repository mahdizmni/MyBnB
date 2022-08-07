import java.sql.*;

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

        String baseQuery = """
                SELECT DISTINCT ai.Listing_ID, a.*, ci.name, AVG(Price) as avgPrice
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
                    AND EXISTS(
                        SELECT *
                        FROM AvailableIn AS ai2
                        WHERE ai2.Listing_ID = ai.Listing_ID AND price BETWEEN ? AND ?
                    )
                    AND EXISTS(
                        SELECT *
                        FROM AvailableIn AS ai2
                        Join Period AS p2 ON ai2.Period_ID = p2.ID
                        WHERE ai2.Listing_ID = ai.Listing_ID
                              AND (p2.start BETWEEN DATE(?) AND DATE(?))
                              AND (p2.end BETWEEN DATE(?) AND DATE(?))
                    )
                GROUP BY ai.Listing_ID, ci.name
                ORDER BY avgPrice ASC;
                """;

        Search sObj = (Search) obj;
        sObj.viewAllFilterOptions();
        PreparedStatement preparedQuery = con.prepareStatement(baseQuery);
        preparedQuery.setString(1, address);
        preparedQuery.setDouble(2, sObj.priceRange[0]);
        preparedQuery.setDouble(3, sObj.priceRange[1]);
        preparedQuery.setString(4, sObj.dateRange[0]);
        preparedQuery.setString(5, sObj.dateRange[1]);
        preparedQuery.setString(6, sObj.dateRange[0]);
        preparedQuery.setString(7, sObj.dateRange[1]);
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
            ps.setInt(1,user1);
            ps.setInt(2,user2);
            ps.setString(3,text);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
            return false;
        }
    }

    public static boolean RatesOnUser(int user1, int user2, int score) throws SQLException {
        try {
            String query = "INSERT INTO RatedOnUser(User1_SIN, User2_SIN, score) VALUES (?, ?, ?)";
            PreparedStatement ps = con.prepareStatement(query);
            ps.setInt(1,user1);
            ps.setInt(2,user2);
            ps.setInt(3,score);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            //  System.out.println(e.getMessage());
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

}
