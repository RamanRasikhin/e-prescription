import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReceptaDAO {
    private DatabaseHandler dbHandler;

    public ReceptaDAO(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    private Integer getLekarzId(int uzytkownikId) throws SQLException {
        String sql = "SELECT id FROM lekarz WHERE uzytkownik_id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uzytkownikId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    private Integer getPacjentId(int uzytkownikId) throws SQLException {
        String sql = "SELECT id FROM pacjent WHERE uzytkownik_id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uzytkownikId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        }
        return null;
    }

    public List<ReceptaView> getReceptyLekarza(int lekarzId) {
        List<ReceptaView> lista = new ArrayList<>();

        String sql = """
        SELECT r.kod_recepty,
               r.data_waznosci,
               r.status,
               COUNT(rl.lek_id) AS liczbaLekow
        FROM recepta r
        LEFT JOIN recepta_lek rl ON r.kod_recepty = rl.kod_recepty
        WHERE r.lekarz_id = ?
        GROUP BY r.kod_recepty
    """;

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, lekarzId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new ReceptaView(
                        rs.getInt("kod_recepty"),
                        rs.getString("data_waznosci"),
                        rs.getString("status"),
                        rs.getInt("liczbaLekow")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public List<ReceptaView> getReceptyPacjenta(int pacjentId) {
        List<ReceptaView> lista = new ArrayList<>();

        // DEBUG
        System.out.println("DEBUG getReceptyPacjenta: pacjentId = " + pacjentId);

        String sql = """
    SELECT r.kod_recepty,
           r.data_waznosci,
           r.status,
           COUNT(rl.lek_id) AS liczbaLekow
    FROM recepta r
    LEFT JOIN recepta_lek rl ON r.kod_recepty = rl.kod_recepty
    WHERE r.pacjent_id = ?
    GROUP BY r.kod_recepty
    """;

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, pacjentId);
            ResultSet rs = ps.executeQuery();

            int count = 0;
            while (rs.next()) {
                count++;
                lista.add(new ReceptaView(
                        rs.getInt("kod_recepty"),
                        rs.getString("data_waznosci"),
                        rs.getString("status"),
                        rs.getInt("liczbaLekow")
                ));
                System.out.println("  Znaleziono receptę: " + rs.getInt("kod_recepty"));
            }

            System.out.println("  Łącznie recept: " + count);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean czyKodReceptyZajety(int kodRecepty) {
        String sql = "SELECT 1 FROM recepta WHERE kod_recepty = ? LIMIT 1";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kodRecepty);
            ResultSet rs = ps.executeQuery();

            return rs.next(); // jeśli rs.next() == true, oznacza że kod już istnieje

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false; // w razie błędu traktujemy jako "nie zajęty"
    }

    public int getLekarzIdByUserId(int uzytkownikId) {
        String sql = "SELECT id FROM lekarz WHERE uzytkownik_id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uzytkownikId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getPacjentIdByUserId(int uzytkownikId) {
        String sql = "SELECT id FROM pacjent WHERE uzytkownik_id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, uzytkownikId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("id");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }


    public void zapiszRecepte(Recepta recepta) {
        String sqlRecepta = "INSERT INTO recepta (kod_recepty, lekarz_id, pacjent_id, data_waznosci, status) VALUES (?, ?, ?, ?, ?)";
        String sqlLeki = "INSERT INTO recepta_lek (kod_recepty, lek_id) VALUES (?, ?)";

        try (Connection conn = dbHandler.getDbConnection()) {
            conn.setAutoCommit(false);

            Integer lekarzDbId = getLekarzId(recepta.getLekarzWypisujacy().getId());
            Integer pacjentDbId = getPacjentId(recepta.getPacjent().getId());

            if (lekarzDbId == null || pacjentDbId == null) {
                conn.rollback();
                return;
            }

            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlRecepta)) {
                    ps.setInt(1, recepta.getKod());
                    ps.setInt(2, lekarzDbId);
                    ps.setInt(3, pacjentDbId);
                    ps.setString(4, recepta.getDataWaznosci().toString());
                    ps.setString(5, "AKTYWNA");
                    ps.executeUpdate();
                }

                try (PreparedStatement psLeki = conn.prepareStatement(sqlLeki)) {
                    for (Lek lek : recepta.getListaLekow()) {
                        psLeki.setInt(1, recepta.getKod());
                        psLeki.setInt(2, lek.getId());
                        psLeki.addBatch();
                    }
                    psLeki.executeBatch();
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

    public void zaktualizujStatus(int kodRecepty, String status) {
        String sql = "UPDATE recepta SET status = ? WHERE kod_recepty = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2, kodRecepty);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<Map<String, Object>> getReceptyPacjentaAPI(int uzytkownikId) {
        List<Map<String, Object>> wyniki = new ArrayList<>();

        String sql = "SELECT r.kod_recepty, r.data_waznosci, r.status, doc.nazwisko as lekarz, " +
                     "GROUP_CONCAT(l.name, ', ') as lista_lekow " +
                     "FROM recepta r " +
                     "JOIN pacjent p ON r.pacjent_id = p.id " +
                     "JOIN lekarz doc_t ON r.lekarz_id = doc_t.id " +
                     "JOIN uzytkownik doc ON doc_t.uzytkownik_id = doc.id " +
                     "JOIN recepta_lek rl ON r.kod_recepty = rl.kod_recepty " +
                     "JOIN lek l ON rl.lek_id = l.id " +
                     "WHERE p.uzytkownik_id = ? " +
                     "GROUP BY r.kod_recepty";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, uzytkownikId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                row.put("kod", rs.getInt("kod_recepty"));
                row.put("lekarz", rs.getString("lekarz"));
                row.put("waznosc", rs.getString("data_waznosci"));
                row.put("status", rs.getString("status"));
                row.put("leki", rs.getString("lista_lekow"));
                wyniki.add(row);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return wyniki;
    }

    public Map<String, Object> znajdzReceptePoKodzie(int kod) {
        String sqlInfo = "SELECT r.kod_recepty, r.status, r.data_waznosci, " +
                         "p_user.imie, p_user.nazwisko " +
                         "FROM recepta r " +
                         "JOIN pacjent p ON r.pacjent_id = p.id " +
                         "JOIN uzytkownik p_user ON p.uzytkownik_id = p_user.id " +
                         "WHERE r.kod_recepty = ?";
        
        String sqlLeki = "SELECT l.id, l.name FROM recepta_lek rl " +
                         "JOIN lek l ON rl.lek_id = l.id WHERE rl.kod_recepty = ?";

        try (Connection conn = dbHandler.getDbConnection()) {
            Map<String, Object> result = new HashMap<>();
            
            try (PreparedStatement ps = conn.prepareStatement(sqlInfo)) {
                ps.setInt(1, kod);
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    result.put("kod", rs.getInt("kod_recepty"));
                    result.put("pacjent", rs.getString("imie") + " " + rs.getString("nazwisko"));
                    result.put("status", rs.getString("status"));
                    result.put("waznosc", rs.getString("data_waznosci"));
                } else {
                    return null;
                }
            }

            List<Map<String, Object>> lekiList = new ArrayList<>();
            try (PreparedStatement ps = conn.prepareStatement(sqlLeki)) {
                ps.setInt(1, kod);
                ResultSet rs = ps.executeQuery();
                while(rs.next()) {
                    Map<String, Object> lekMap = new HashMap<>();
                    lekMap.put("id", rs.getInt("id"));
                    lekMap.put("nazwa", rs.getString("name"));
                    lekiList.add(lekMap);
                }
            }
            result.put("leki", lekiList);
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<Lek, Integer> getLekiZRecepty(int kod) {
        Map<Lek, Integer> map = new HashMap<>();

        String sql = """
        SELECT l.id, l.name, l.opis, l.rodzaj, l.datawaznosci,
               COUNT(*) as ilosc
        FROM recepta_lek rl
        JOIN lek l ON rl.lek_id = l.id
        WHERE rl.kod_recepty = ?
        GROUP BY l.id
    """;

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, kod);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Lek lek = new Lek(
                        rs.getInt("id"),
                        rs.getString("name"),
                        RodzajLeku.valueOf(rs.getString("rodzaj")),
                        rs.getString("opis"),
                        LocalDate.parse(rs.getString("datawaznosci"))
                );
                map.put(lek, rs.getInt("ilosc"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }


    public void uniewaznijRecepte(int kod) {
        zaktualizujStatus(kod, "ANULOWANA");
    }
}