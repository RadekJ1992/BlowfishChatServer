package manager;

import model.Message;
import model.User;

import javax.xml.stream.Location;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO obsługujące połączenie aplikacji z bazą danych i modyfikację jej zawartości
 *
 * Klasa implementuje wzorzec singletona bez leniwej inicjalizacji, obiekt tworzony jest w momencie tworzenia całej klasy
 * Created by radoslawjarzynka on 11.05.15.
 */
public class DBManager {

    private static volatile DBManager instance = new DBManager();

    private static final String DATABASE_LOCATION = "jdbc:sqlite:blowfish_chat.db";

    private SecureRandom random = new SecureRandom();

    // private constructor
    private DBManager() {
        Connection c;
        Statement stmt1;
        Statement stmt2;
        Statement stmt3;
        try {
            //utworzenie bazy danych
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);
            System.out.println("Opened database successfully");

            stmt1 = c.createStatement();
            String sql1 = "CREATE TABLE IF NOT EXISTS USERS " +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT    NOT NULL," +
                    " USERNAME      TEXT NOT NULL, " +
                    " PASSWORD      TEXT NOT NULL, " +
                    " TOKEN         TEXT)";
            stmt1.executeUpdate(sql1);
            stmt1.close();

            stmt2 = c.createStatement();
            String sql2= "CREATE TABLE IF NOT EXISTS MESSAGES" +
                    "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
                    "USER_ID INTEGER NOT NULL," +
                    "SENDER_ID INTEGER NOT NULL," +
                    "MESSAGE TEXT," +
                    "FOREIGN KEY(USER_ID) REFERENCES USERS(ID) ON UPDATE CASCADE) ";
            stmt2.executeUpdate(sql2);
            stmt2.close();

            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        System.out.println("Tables created successfully");
    }

    /**
     * Getter instancji singletona
     * @return
     */
    public static DBManager getInstance() {
        return instance;
    }

    public void addNewUser(User user) {
        Connection c;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            String token = generateToken();
            c = DriverManager.getConnection(DATABASE_LOCATION);
            String sql = "INSERT INTO USERS (USERNAME, PASSWORD, TOKEN) " +
                    "VALUES (?,?,?);";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPassword());
            stmt.setString(3, token);
            stmt.execute();
            ResultSet rs = stmt.getGeneratedKeys();
            if (rs != null && rs.next()) {
                user.setId(rs.getInt(1));
            }
            stmt.close();
            c.close();
            user.setToken(token);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public User getUserById(Integer id) {
        Connection c;
        PreparedStatement preparedStatement = null;
        User user = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);

            String selectSQL = "SELECT ID, USERNAME, PASSWORD, TOKEN FROM USERS WHERE ID = ?";
            preparedStatement = c.prepareStatement(selectSQL);
            preparedStatement.setInt(1, id);
            ResultSet rs = preparedStatement.executeQuery();

            if ( rs.first() ) {
                user = new User();
                user.setId(rs.getInt("ID"));;
                user.setUsername(rs.getString("USERNAME"));;
                user.setPassword(rs.getString("PASSWORD"));;
                user.setToken(rs.getString("TOKEN"));;
            }
            rs.close();
            preparedStatement.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return user;
    }

    public User getUserByUsername(String username) {
        Connection c;
        PreparedStatement preparedStatement = null;
        User user = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);

            String selectSQL = "SELECT ID, USERNAME, PASSWORD, TOKEN FROM USERS WHERE NAME = ?";
            preparedStatement = c.prepareStatement(selectSQL);
            preparedStatement.setString(1, username);
            ResultSet rs = preparedStatement.executeQuery();

            if ( rs.first() ) {
                user = new User();
                user.setId(rs.getInt("ID"));;
                user.setUsername(rs.getString("USERNAME"));;
                user.setPassword(rs.getString("PASSWORD"));;
                user.setToken(rs.getString("TOKEN"));;
            }
            rs.close();
            preparedStatement.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return user;
    }

    public String generateTokenForUser(User user) {
        Connection c;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);
            int userID = user.getId();
            String token = generateToken();
            String sql = "UPDATE USERS SET TOKEN = ? WHERE USER_ID = ?;";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, token);
            stmt.setInt(2, userID);
            stmt.executeUpdate();
            stmt.close();
            c.close();
            user.setToken(token);
            return token;
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return null;
    }

    public void removeUserToken(User user) {
        Connection c;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);
            int userID = user.getId();
            String sql = "UPDATE USERS SET TOKEN = ? WHERE USER_ID = ?;";
            stmt = c.prepareStatement(sql);
            stmt.setString(1, null);
            stmt.setInt(2, userID);
            stmt.executeUpdate();
            stmt.close();
            c.close();
            user.setToken(null);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public boolean isTokenValid(User user, String token) {
        Connection c;
        PreparedStatement preparedStatement = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);

            String selectSQL = "SELECT TOKEN FROM USERS WHERE ID = ?";
            preparedStatement = c.prepareStatement(selectSQL);
            preparedStatement.setInt(1, user.getId());
            ResultSet rs = preparedStatement.executeQuery();

            String _token = null;
            if ( rs.first() ) {
                _token = rs.getString("TOKEN");
            }
            rs.close();
            preparedStatement.close();
            c.close();
            return token.equals(_token);
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return false;
    }

    public void insertNewMessage(Message message) {
        Connection c;
        PreparedStatement stmt = null;
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);
            int userId = message.getUserId();
            int senderId = message.getSenderId();
            String msg = message.getMessage();
            String sql = "INSERT INTO MESSAGES (USER_ID, SENDER_ID, MESSAGE) " +
                    "VALUES (?,?,?);";
            stmt = c.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setInt(2, senderId);
            stmt.setString(3, msg);
            stmt.executeUpdate();
            stmt.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
    }

    public List<Message> getUserMessages(User user) {
        Connection c;
        PreparedStatement preparedStatement = null;
        List<Message> result = new ArrayList<Message>();
        try {
            Class.forName("org.sqlite.JDBC");
            c = DriverManager.getConnection(DATABASE_LOCATION);

            String selectSQL = "SELECT USER_ID, SENDER_ID, MESSAGE FROM MESSAGES WHERE USER_ID = ? order by ID DESC";
            preparedStatement = c.prepareStatement(selectSQL);
            preparedStatement.setInt(1, user.getId());
            ResultSet rs = preparedStatement.executeQuery();

            while ( rs.next() ) {
                int userId = rs.getInt("USER_ID");
                int senderId = rs.getInt("SENDER_ID");
                String msg = rs.getString("MESSAGE");
                result.add(new Message(userId, senderId, msg));
            }
            rs.close();
            preparedStatement.close();
            c.close();
        } catch ( Exception e ) {
            e.printStackTrace();
        }
        return result;
    }

    private String generateToken() {
        return new BigInteger(130, random).toString(32);
    }
}
