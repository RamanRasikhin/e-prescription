import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.UUID;

public class AuthService {

    private final Map<String, UserSession> activeSessions = new ConcurrentHashMap<>();
    private final DatabaseHandler dbHandler;

    public AuthService() { this(new DatabaseHandler()); }

    public AuthService(DatabaseHandler dbHandler) { this.dbHandler = dbHandler; }

    public static class UserSession {
        public final int userId;
        public final String role;

        public UserSession(int userId, String role) {
            this.userId = userId;
            this.role = role;
        }
    }

    public String login(String login, String haslo) {
        String sql = "SELECT id, rola FROM uzytkownik WHERE login = ? AND haslo = ?";

        try (Connection conn = dbHandler.getDbConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, login);
            ps.setString(2, haslo);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("id");
                String role = rs.getString("rola");

                String token = UUID.randomUUID().toString();
                activeSessions.put(token, new UserSession(id, role));
                
                return token;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null; 
    }

    public boolean hasAccess(String token, String requiredRole) {
        if (token == null || !activeSessions.containsKey(token)) {
            return false;
        }
        String userRole = activeSessions.get(token).role;
        if ("ANY".equals(requiredRole)) return true; 
        return userRole.equals(requiredRole);
    }

    public int getUserId(String token) {
        if (token != null && activeSessions.containsKey(token)) {
            return activeSessions.get(token).userId;
        }
        return -1;
    }
    
    public void logout(String token) {
        activeSessions.remove(token);
    }
}