import java.sql.*;

public class App {
    public static void main(String[] args){
        MySQLObj sqlObj = new MySQLObj();

        // test query
        try{
            ResultSet testRs = sqlObj.testQuery();
            while(testRs.next()){
                System.out.println(testRs.getInt("a"));
            }
        }catch (Exception e){
            e.printStackTrace();
        }

        sqlObj.closeConnection(); // closes sql connection, keep this in the bottom
    }
}
