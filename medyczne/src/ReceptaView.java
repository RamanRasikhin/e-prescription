public class ReceptaView {

    private final int kodRecepty;
    private final String dataWaznosci;
    private final String status;
    private final int liczbaLekow;

    public ReceptaView(int kodRecepty, String dataWaznosci, String status, int liczbaLekow) {
        this.kodRecepty = kodRecepty;
        this.dataWaznosci = dataWaznosci;
        this.status = status;
        this.liczbaLekow = liczbaLekow;
    }

    public int getKodRecepty() {
        return kodRecepty;
    }

    public String getDataWaznosci() {
        return dataWaznosci;
    }

    public String getStatus() {
        return status;
    }

    public int getLiczbaLekow() {
        return liczbaLekow;
    }
}
