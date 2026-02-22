import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBHelper {

    private String userName = "root";
    private String password = "qazwsxedc";
    private String dbUrl = "jdbc:mysql://localhost:3306/depo_yonetim";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(dbUrl, userName, password);
    }
}