import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MagazynDAO {
    private DatabaseHandler dbHandler;

    public MagazynDAO(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void dodajLekDoMagazynu(int lekId, int ilosc) {
        if (ilosc <= 0) throw new IllegalArgumentException("Quantity must be positive");
        String sqlCheck = "SELECT ilosc FROM magazyn WHERE lek_id = ?";
        String sqlUpdate = "UPDATE magazyn SET ilosc = ilosc + ? WHERE lek_id = ?";
        String sqlInsert = "INSERT INTO magazyn (lek_id, ilosc) VALUES (?, ?)";

        try (Connection conn = dbHandler.getDbConnection()) {
            boolean exists = false;
            try (PreparedStatement psCheck = conn.prepareStatement(sqlCheck)) {
                psCheck.setInt(1, lekId);
                ResultSet rs = psCheck.executeQuery();
                if (rs.next()) exists = true;
            }

            if (exists) {
                try (PreparedStatement psUpdate = conn.prepareStatement(sqlUpdate)) {
                    psUpdate.setInt(1, ilosc);
                    psUpdate.setInt(2, lekId);
                    psUpdate.executeUpdate();
                }
            } else {
                try (PreparedStatement psInsert = conn.prepareStatement(sqlInsert)) {
                    psInsert.setInt(1, lekId);
                    psInsert.setInt(2, ilosc);
                    psInsert.executeUpdate();
                }
            }
            System.out.println("Zaktualizowano magazyn dla leku ID=" + lekId);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean pobierzLek(int lekId, int wymaganaIlosc) {
        if (wymaganaIlosc <= 0) return false;
        String sql = "UPDATE magazyn SET ilosc = ilosc - ? WHERE lek_id = ? AND ilosc >= ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, wymaganaIlosc);
            ps.setInt(2, lekId);
            ps.setInt(3, wymaganaIlosc);
            return ps.executeUpdate() == 1;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public List<LekMagazynView> pobierzLekiZMagazynu() {
        List<LekMagazynView> lista = new ArrayList<>();

        String sql = """
        SELECT l.name, l.datawaznosci, l.opis, m.ilosc
        FROM magazyn m
        JOIN lek l ON m.lek_id = l.id
    """;

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new LekMagazynView(
                        rs.getString("name"),
                        rs.getString("datawaznosci"),
                        rs.getString("opis"),
                        rs.getInt("ilosc")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }


    public int sprawdzStan(int lekId) {
        String sql = "SELECT ilosc FROM magazyn WHERE lek_id = ?";
        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, lekId);
            ResultSet rs = ps.executeQuery();
            if(rs.next()) return rs.getInt("ilosc");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean wydajLek(int lekId, int ilosc) {
        return pobierzLek(lekId, ilosc);
    }
}
