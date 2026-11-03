public class LekMagazynView {
    private final String nazwa;
    private final String dataWaznosci;
    private final String opis;
    private final int ilosc;

    public LekMagazynView(String nazwa, String dataWaznosci, String opis, int ilosc) {
        this.nazwa = nazwa;
        this.dataWaznosci = dataWaznosci;
        this.opis = opis;
        this.ilosc = ilosc;
    }

    public String getNazwa() {
        return nazwa;
    }

    public String getDataWaznosci() {
        return dataWaznosci;
    }

    public String getOpis() { return opis; }

    public int getIlosc() {
        return ilosc;
    }
}
