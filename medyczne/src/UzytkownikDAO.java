import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class UzytkownikDAO {
    private static DatabaseHandler dbHandler;

    public UzytkownikDAO(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void zapiszLekarza(Lekarz lekarz) {
        String sqlUser = "INSERT OR IGNORE INTO uzytkownik (imie, nazwisko, login, data_urodzenia, haslo, rola) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlDoc = "INSERT OR IGNORE INTO lekarz (uzytkownik_id, specjalizacja) VALUES (?, ?)";

        try (Connection conn = dbHandler.getDbConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, lekarz.getImie());
                    psUser.setString(2, lekarz.getNazwisko());
                    psUser.setString(3, lekarz.getLogin());
                    psUser.setString(4, lekarz.getDataUrodzenia().toString());
                    psUser.setString(5, lekarz.getHaslo());
                    psUser.setString(6, "LEKARZ");
                    psUser.executeUpdate();
                    
                    ResultSet rs = psUser.getGeneratedKeys();
                    if(rs.next()) userId = rs.getInt(1);
                }

                if(userId != -1) {
                    try (PreparedStatement psDoc = conn.prepareStatement(sqlDoc)) {
                        psDoc.setInt(1, userId);
                        psDoc.setString(2, lekarz.getSpecjalizacja());
                        psDoc.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void zapiszPacjenta(Pacjent pacjent) {
        String sqlUser = "INSERT OR IGNORE INTO uzytkownik (imie, nazwisko, login, data_urodzenia, haslo, rola) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlPat = "INSERT OR IGNORE INTO pacjent (uzytkownik_id, istotneinformacjamedyczna) VALUES (?, ?)";

        try (Connection conn = dbHandler.getDbConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, pacjent.getImie());
                    psUser.setString(2, pacjent.getNazwisko());
                    psUser.setString(3, pacjent.getLogin());
                    psUser.setString(4, pacjent.getDataUrodzenia().toString());
                    psUser.setString(5, pacjent.getHaslo());
                    psUser.setString(6, "PACJENT");
                    psUser.executeUpdate();
                    ResultSet rs = psUser.getGeneratedKeys();
                    if(rs.next()) userId = rs.getInt(1);
                }

                if(userId != -1) {
                    try (PreparedStatement psPat = conn.prepareStatement(sqlPat)) {
                        psPat.setInt(1, userId);
                        psPat.setString(2, pacjent.getIstotneInformacjeMedyczne());
                        psPat.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void zapiszFarmaceute(Farmaceuta farmaceuta) {
        String sqlUser = "INSERT OR IGNORE INTO uzytkownik (imie, nazwisko, login, data_urodzenia, haslo, rola) VALUES (?, ?, ?, ?, ?, ?)";
        String sqlPharm = "INSERT OR IGNORE INTO farmaceuta (uzytkownik_id, apteka_id) VALUES (?, ?)";

        try (Connection conn = dbHandler.getDbConnection()) {
            conn.setAutoCommit(false);
            try {
                int userId = -1;
                try (PreparedStatement psUser = conn.prepareStatement(sqlUser, PreparedStatement.RETURN_GENERATED_KEYS)) {
                    psUser.setString(1, farmaceuta.getImie());
                    psUser.setString(2, farmaceuta.getNazwisko());
                    psUser.setString(3, farmaceuta.getLogin());
                    psUser.setString(4, farmaceuta.getDataUrodzenia().toString());
                    psUser.setString(5, farmaceuta.getHaslo());
                    psUser.setString(6, "FARMACEUTA");
                    psUser.executeUpdate();
                    ResultSet rs = psUser.getGeneratedKeys();
                    if(rs.next()) userId = rs.getInt(1);
                }
                if(userId != -1) {
                    try (PreparedStatement psPharm = conn.prepareStatement(sqlPharm)) {
                        psPharm.setInt(1, userId);
                        psPharm.setInt(2, farmaceuta.getAptekaId());
                        psPharm.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static int getUserIdByLekarzId(int lekarzId) {
        String sql = "SELECT uzytkownik_id FROM lekarz WHERE id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lekarzId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uzytkownik_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static int getUserIdByPacjentId(int pacjentId) {
        String sql = "SELECT uzytkownik_id FROM pacjent WHERE id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, pacjentId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("uzytkownik_id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public UserResult znajdzUzytkownikaLogowanie(String login, String haslo) {
        String sql = "SELECT id, rola FROM uzytkownik WHERE login = ? AND haslo = ?";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, haslo);

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new UserResult(
                        rs.getInt("id"),
                        rs.getString("rola")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static UserResult znajdzUzytkownikowPoImieniuINazwisku(String imie, String nazwisko) {

        String sql = """
        SELECT id, rola
        FROM uzytkownik
        WHERE imie LIKE ? AND nazwisko LIKE ?
    """;

        UserResult wynik = null;
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + imie + "%");
            ps.setString(2, "%" + nazwisko + "%");

            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                wynik = new UserResult(
                        rs.getInt("id"),
                        rs.getString("rola")
                ); //TODO: a co jak więcej?
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return wynik;
    }



    public static Lekarz znajdzLekarzaPoId(int id) {
        String sql = "SELECT u.id, u.imie, u.nazwisko, u.login, u.haslo, u.data_urodzenia, l.specjalizacja " +
                     "FROM uzytkownik u " +
                     "JOIN lekarz l ON u.id = l.uzytkownik_id " +
                     "WHERE u.id = ?";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Lekarz(
                        rs.getInt("id"),
                        rs.getString("imie"),
                        rs.getString("nazwisko"),
                        rs.getString("login"),
                        rs.getString("haslo"),
                        LocalDate.parse(rs.getString("data_urodzenia")),
                        rs.getString("specjalizacja")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Pacjent znajdzPacjentaPoId(int id) {
        String sql = "SELECT u.id, u.imie, u.nazwisko, u.login, u.haslo, u.data_urodzenia, p.istotneinformacjamedyczna " +
                "FROM uzytkownik u " +
                "JOIN pacjent p ON u.id = p.uzytkownik_id " +
                "WHERE u.id = ?";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Pacjent(
                        rs.getInt("id"),
                        rs.getString("imie"),
                        rs.getString("nazwisko"),
                        rs.getString("login"),
                        rs.getString("haslo"),
                        LocalDate.parse(rs.getString("data_urodzenia")),
                        rs.getString("istotneinformacjamedyczna")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}