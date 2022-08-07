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
}
