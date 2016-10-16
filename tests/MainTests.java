import com.company.Hurricane;
import com.company.Main;
import com.company.User;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import static org.junit.Assert.assertTrue;

/**
 * Created by VeryBarry on 10/16/16.
 */
public class MainTests {
    public Connection startConnection () throws SQLException {
        Connection conn = DriverManager.getConnection("jdbc:h2:mem:test");
        Main.createTables(conn);
        return conn;
    }
    @Test
    public void testInsertSelectUser() throws SQLException {
        Connection conn = startConnection();
        Main.insertUser(conn,"Barry","1234");
        ArrayList<User> userArray = Main.selectUser(conn);
        conn.close();
        assertTrue(userArray != null);
        ArrayList<User> userTest = Main.selectUser(conn);
        assertTrue(!userTest.isEmpty());
    }
    @Test
    public void testInsertSelectHurricane() throws SQLException {
        Connection conn = startConnection();
        Main.insertHurricane(conn, "Hugo", "CHS", 4, "www.hurricane.com", 0);
        Main.insertHurricane(conn, "Floyd", "MTB", 3, "www.hurricane.com", 0);
        Main.insertHurricane(conn, "Matthew", "CHS", 2, "www.hurricane.com", 0);
        ArrayList<Hurricane> hurricanes = Main.selectHurricane(conn);
        assertTrue(!hurricanes.isEmpty());
    }
}
