package lee.code.locker.database;

import lee.code.locker.GoldmanLocker;
import lombok.SneakyThrows;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SQLite {

    private Connection connection;
    private Statement statement;

    public void connect() {
        GoldmanLocker plugin = GoldmanLocker.getPlugin();
        connection = null;

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }
            File dbFile = new File(plugin.getDataFolder(), "database.db");
            if (!dbFile.exists()) {
                dbFile.createNewFile();
            }
            String url = "jdbc:sqlite:" + dbFile.getPath();

            connection = DriverManager.getConnection(url);
            statement = connection.createStatement();

        } catch (IOException | SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void disconnect() {
        try {
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public void update(String sql) {
        try {
            statement.execute(sql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }

    public ResultSet getResult(String sql) {
        try {
            return statement.executeQuery(sql);
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
        return null;
    }

    public void loadTables() {
        //chunk table
        update("CREATE TABLE IF NOT EXISTS locks (" +
                "`location` varchar PRIMARY KEY," +
                "`owner` varchar NOT NULL," +
                "`trusted` varchar NOT NULL" +
                ");");
    }

    //table

    @SneakyThrows
    public void createLock(String location, UUID owner, String trusted) {

        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        if (rs.next()) update("DELETE FROM locks WHERE location = '" + location + "';");

        update("INSERT INTO locks (location, owner, trusted) VALUES( '" + location + "','" + owner + "','" + trusted + "');");
    }

    @SneakyThrows
    public boolean isLocked(String location) {
        ResultSet rs = getResult("SELECT location FROM locks WHERE location = '" + location + "';");
        return rs.next();
    }

    @SneakyThrows
    public boolean isLockOwner(String location, UUID owner) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        if (rs.next()) {
            return UUID.fromString(rs.getString("owner")).equals(owner);
        }
        return false;
    }

    @SneakyThrows
    public UUID getLockOwner(String location) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        return UUID.fromString(rs.getString("owner"));
    }

    public void removeLock(String location) {
        update("DELETE FROM locks WHERE location = '" + location + "';");
    }

    @SneakyThrows
    public List<String> getTrustedToLock(String location) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        if (rs.next()) {
            String trusted = rs.getString("trusted");
            if (!trusted.equals("n")) {
                List<String> players = new ArrayList<>();
                String[] splitUUIDs = StringUtils.split(trusted, ',');
                for (String uuid : splitUUIDs) players.add(Bukkit.getOfflinePlayer(UUID.fromString(uuid)).getName());
                return players;
            }
        }
        return Collections.singletonList("");
    }

    @SneakyThrows
    public boolean isLockTrusted(String location, UUID trusted) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        if (rs.next()) {
            if (rs.getString("trusted").equals("n")) {
                return false;
            } else {
                String players = rs.getString("trusted");
                String[] split = StringUtils.split(players, ',');
                for (String player : split) {
                    if (UUID.fromString(player).equals(trusted)) return true;
                }
            }
        }
        return false;
    }

    @SneakyThrows
    public void removeLockTrusted(String location, String untrust) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");
        String players = rs.getString("trusted");
        String[] split = StringUtils.split(players, ',');
        List<String> playerList = new ArrayList<>();

        for (String trusted : split) {
            if (!Bukkit.getOfflinePlayer(UUID.fromString(trusted)).getName().equals(untrust)) playerList.add(trusted);
        }

        if (!playerList.isEmpty()) {
            String trusted = StringUtils.join(playerList, ",");
            update("UPDATE locks SET trusted ='" + trusted + "' WHERE location ='" + location + "';");
        } else update("UPDATE locks SET trusted ='n' WHERE location ='" + location + "';");
    }

    @SneakyThrows
    public void addLockTrusted(String location, UUID trust) {
        ResultSet rs = getResult("SELECT * FROM locks WHERE location = '" + location + "';");

        if (!rs.getString("trusted").equals("n")) {
            String trusted = rs.getString("trusted") + "," + trust;
            update("UPDATE locks SET trusted ='" + trusted + "' WHERE location ='" + location + "';");

        } else update("UPDATE locks SET trusted ='" + trust + "' WHERE location ='" + location + "';");
    }
}
