import java.sql.*;

public class MySQLObj {
    private final String uriDb = "jdbc:mysql://localhost:3306/c43bnb";
    private final String username = "root";
    private final String password = "";
    public Connection con;
    public Statement st;

    public MySQLObj(){
        try{
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.con = DriverManager.getConnection(uriDb, username, password);
            this.st = this.con.createStatement();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        try {
            this.st.close();
            this.con.close();
        }
        catch (Exception e){
            e.printStackTrace();
        }
    }

    public ResultSet testQuery() throws SQLException {
        String query = "SELECT * FROM test";
        return st.executeQuery(query);
    }

}
