import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class LekDAO {
    private DatabaseHandler dbHandler;

    public LekDAO(DatabaseHandler dbHandler) {
        this.dbHandler = dbHandler;
    }

    public void zapiszLek(Lek lek) {
        String sql = "INSERT OR IGNORE INTO lek (name, opis, datawaznosci, rodzaj) VALUES (?, ?, ?, ?)";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, lek.getNazwa());
            ps.setString(2, lek.getOpis());
            ps.setString(3, lek.getDataWaznosci().toString());
            ps.setString(4, lek.getRodzaj().toString());

            ps.executeUpdate();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Lek pobierzLek(int id) {
        String sql = "SELECT * FROM lek WHERE id = ?";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Lek(
                        rs.getInt("id"),
                        rs.getString("name"),
                        RodzajLeku.valueOf(rs.getString("rodzaj")),
                        rs.getString("opis"),
                        LocalDate.parse(rs.getString("datawaznosci"))
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Lek> pobierzWszystkieLeki() {
        List<Lek> lista = new ArrayList<>();
        String sql = "SELECT * FROM lek";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(new Lek(
                        rs.getInt("id"),
                        rs.getString("name"),
                        RodzajLeku.valueOf(rs.getString("rodzaj")),
                        rs.getString("opis"),
                        LocalDate.parse(rs.getString("datawaznosci"))
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

}