import java.sql.*;

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
        String query = "INSERT INTO Listings(latitude, longitude) VALUES (%f, %f)";
        query = String.format(query, latitude, longitude);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
    }

    public static Integer getRecentID() throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
        return rs.getInt(1);
    }

    public static void addToCountry(String country) throws SQLException {
        String query = "INSERT INTO Country (name) VALUES ('%s')";
        query = String.format(query, country);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
    }
    public static void addToCity(String city) throws SQLException {
        String query = "INSERT INTO City (name) VALUES ('%s')";
        query = String.format(query, city);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
    }
    public static void addToAddress(String postalcode, String street, int num) throws SQLException {
        String query = "INSERT INTO Address VALUES ('%s', '%s', %d)";
        query = String.format(query, postalcode, street, num);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
    }
    public static void addToType(String type) throws SQLException {
        String query = "INSERT INTO Type (type) VALUES ('%s')";
        query = String.format(query, type);
        PreparedStatement ps = con.prepareStatement(query);
        ResultSet rs = ps.executeQuery();
    }
}
