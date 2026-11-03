import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseHandler {
    private final String connectionString;

    public DatabaseHandler() {
        this(System.getProperty("recepty.db", "apteka.db"));
    }

    public DatabaseHandler(String databaseFile) {
        this.connectionString = "jdbc:sqlite:" + databaseFile;
    }

    public Connection getDbConnection() throws SQLException {
        return DriverManager.getConnection(connectionString);
    }

    public void createTables() {
        String[] sqlStatements = {
            "CREATE TABLE IF NOT EXISTS uzytkownik (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "imie TEXT, " +
                "nazwisko TEXT, " +
                "login TEXT UNIQUE NOT NULL, " +
                "data_urodzenia TEXT, " +
                "haslo TEXT, " +
                "rola TEXT)",

            "CREATE TABLE IF NOT EXISTS lekarz (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uzytkownik_id INTEGER NOT NULL UNIQUE, " +
                "specjalizacja TEXT, " +
                "FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id))",

            "CREATE TABLE IF NOT EXISTS pacjent (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uzytkownik_id INTEGER NOT NULL UNIQUE, " +
                "istotneinformacjamedyczna TEXT, " +
                "FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id))",

            "CREATE TABLE IF NOT EXISTS farmaceuta (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "uzytkownik_id INTEGER NOT NULL UNIQUE, " +
                "apteka_id INTEGER, " +
                "FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id))",

            "CREATE TABLE IF NOT EXISTS lek (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "opis TEXT, " +
                "datawaznosci TEXT, " +
                "rodzaj TEXT)",

            "CREATE TABLE IF NOT EXISTS magazyn (" +
                "lek_id INTEGER PRIMARY KEY, " +
                "ilosc INTEGER DEFAULT 0, " +
                "FOREIGN KEY (lek_id) REFERENCES lek(id))",

            "CREATE TABLE IF NOT EXISTS recepta (" +
                "kod_recepty INTEGER PRIMARY KEY, " + 
                "lekarz_id INTEGER NOT NULL, " +
                "pacjent_id INTEGER NOT NULL, " +
                "data_waznosci TEXT, " +
                "status TEXT, " +
                "FOREIGN KEY (lekarz_id) REFERENCES lekarz(id), " +
                "FOREIGN KEY (pacjent_id) REFERENCES pacjent(id))",

            "CREATE TABLE IF NOT EXISTS recepta_lek (" +
                "kod_recepty INTEGER, " +
                "lek_id INTEGER, " +
                "FOREIGN KEY (kod_recepty) REFERENCES recepta(kod_recepty), " +
                "FOREIGN KEY (lek_id) REFERENCES lek(id))"
        };

        try (Connection conn = getDbConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : sqlStatements) {
                stmt.execute(sql);
            }
            System.out.println("Baza danych zaktualizowana pomyslnie.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}