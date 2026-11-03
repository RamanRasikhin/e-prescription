public class UserResult {
    private final int id;
    private final String rola;

    public UserResult(int id, String rola) {
        this.id = id;
        this.rola = rola;
    }

    public int getId() {
        return id;
    }

    public String getRola() {
        return rola;
    }
}
