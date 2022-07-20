package net.fero.manejd.database;

import net.fero.manejd.structure.Search;
import net.fero.manejd.utils.ConsoleColors;
import net.fero.manejd.utils.Hashing;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlStore {
    private static Connection conn = null;
    private final static Logger log = LoggerFactory.getLogger(SqlStore.class);

    static {
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:manejd.db");

            log.info("Connected to Sql lite");
            PreparedStatement preparedStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS secrets (master_password TEXT NOT NULL PRIMARY KEY)");
            preparedStatement.execute();

            preparedStatement = conn.prepareStatement("CREATE TABLE IF NOT EXISTS entries (site_name TEXT NOT NULL, " +
                    "site_url TEXT NOT NULL PRIMARY KEY, email TEXT, username TEXT, password TEXT NOT NULL)");

            preparedStatement.execute();

            log.info("Initialized Tables");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection getConn() {
        return conn;
    }

    public static void addSecret(String hashed) {
        try (PreparedStatement stmt = conn.prepareStatement("INSERT OR IGNORE INTO secrets VALUES (?)")) {
            stmt.setString(1, hashed);

            stmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static boolean validatePassword(String mp) {
        try (PreparedStatement stmt = conn.prepareStatement("SELECT * FROM secrets WHERE master_password=?")) {
            stmt.setString(1, mp);

            ResultSet resultSet = stmt.executeQuery();

            if(!resultSet.next()) {
                return false;
            }

            String master_password = resultSet.getString("master_password");

            return master_password.equals(mp);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static boolean hasConfigured() {
        try(PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(master_password) as total FROM secrets")) {
            ResultSet resultSet = stmt.executeQuery();

            resultSet.next();

            return resultSet.getInt("total") > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static List<Search> searchEntries(Search search) {
        StringBuilder sql = new StringBuilder("SELECT * FROM entries");

        if(!search.isEmpty()) {
            sql.append(" WHERE ");

            HashMap<String, String> asList = search.getAsMap();

            for(Map.Entry<String, String> entry : asList.entrySet()) {
                if(entry.getValue() == null) continue;

                sql.append(entry.getKey()).append("='").append(entry.getValue()).append("' AND ");
            }

            sql = new StringBuilder(sql.substring(0, sql.length() - 5));
        }

        try(PreparedStatement stmt = conn.prepareStatement(sql.toString())) {

            ResultSet resultSet = stmt.executeQuery();

            if(!resultSet.next()) {
                System.out.println();
                System.out.println(ConsoleColors.YELLOW + "No results found" + ConsoleColors.RESET);
                return null;
            }

            List<Search> entries = new ArrayList<>();

            do {
                entries.add(new Search(
                        resultSet.getString("site_name"),
                        resultSet.getString("site_url"),
                        resultSet.getString("email"),
                        resultSet.getString("username"),
                        resultSet.getString("password"))
                );
            } while (resultSet.next());

            return entries;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void addEntry(Search search, String password, String mp) {
        if(checkExists(search)) {
            System.out.println(ConsoleColors.RED + "Entry with these details already exists" + ConsoleColors.RESET);
            return;
        }
        try {
            String masterKey = Hashing.computeMasterKey(mp);

            String aesEncrypted = Hashing.aesEncrypt(password, masterKey);

            PreparedStatement stmt = conn.prepareStatement("INSERT OR IGNORE INTO entries VALUES (?, ?, ?, ?, ?)");
            stmt.setString(1, search.siteName);
            stmt.setString(2, search.siteUrl);
            stmt.setString(3, search.email);
            stmt.setString(4, search.username);
            stmt.setString(5, aesEncrypted);

            stmt.execute();

            System.out.println();
            System.out.println(ConsoleColors.GREEN + "Entry Added Successfully !" + ConsoleColors.RESET);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean checkExists(Search search) {
        try(PreparedStatement stmt = conn.prepareStatement("SELECT * FROM entries WHERE site_name=? AND site_url=? AND email=? AND username=?")) {
            stmt.setString(1, search.siteName);
            stmt.setString(2, search.siteUrl);
            stmt.setString(3, search.email != null ? search.email : "");
            stmt.setString(4, search.username);

            ResultSet resultSet = stmt.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
