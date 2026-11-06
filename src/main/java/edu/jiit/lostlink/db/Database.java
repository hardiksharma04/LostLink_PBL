package edu.jiit.lostlink.db;

import java.nio.file.*;
import java.sql.*;

public final class Database {
    private static final String URL;
    static {
        try {
            Path dir = Path.of(System.getProperty("user.home"), ".lostlink");
            Files.createDirectories(dir);
            URL = "jdbc:sqlite:" + dir.resolve("lostlink.db").toAbsolutePath();
        } catch (Exception e) { throw new ExceptionInInitializerError(e); }
    }
    private Database() {}
    public static Connection connect() throws SQLException {
        Connection c=DriverManager.getConnection(URL);
        try(Statement s=c.createStatement()){s.execute("PRAGMA foreign_keys=ON");}
        return c;
    }
    public static void initialize() throws SQLException {
        try(Connection c=connect(); Statement s=c.createStatement()) {
            s.executeUpdate("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, email TEXT NOT NULL UNIQUE COLLATE NOCASE, salt TEXT NOT NULL, password_hash TEXT NOT NULL, is_admin INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE TABLE IF NOT EXISTS reports (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL REFERENCES users(id), type TEXT NOT NULL CHECK(type IN ('Lost','Found')), name TEXT NOT NULL, category TEXT NOT NULL, colour TEXT NOT NULL, item_date TEXT NOT NULL, location TEXT NOT NULL, description TEXT NOT NULL, contact TEXT NOT NULL, status TEXT NOT NULL DEFAULT 'Open' CHECK(status IN ('Open','Matched','Returned')), verified INTEGER NOT NULL DEFAULT 0, created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP)");
            s.executeUpdate("CREATE INDEX IF NOT EXISTS reports_type_status ON reports(type,status)");
        }
    }
    public static boolean hasAdministrator() throws SQLException {
        try(Connection c=connect();Statement s=c.createStatement();ResultSet r=s.executeQuery("SELECT EXISTS(SELECT 1 FROM users WHERE is_admin=1)")) {
            return r.next() && r.getInt(1)==1;
        }
    }
}
