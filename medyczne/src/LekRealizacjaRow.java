import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

public class LekRealizacjaRow {

    private final Lek lek;
    private final int iloscNaRecepcie;
    private final int iloscNaMagazynie;
    private final BooleanProperty wydac = new SimpleBooleanProperty(false);

    public LekRealizacjaRow(Lek lek, int iloscNaRecepcie, int iloscNaMagazynie) {
        this.lek = lek;
        this.iloscNaRecepcie = iloscNaRecepcie;
        this.iloscNaMagazynie = iloscNaMagazynie;
    }

    public Lek getLek() { return lek; }
    public String getNazwa() { return lek.getNazwa(); }
    public int getIloscNaRecepcie() { return iloscNaRecepcie; }
    public int getIloscNaMagazynie() { return iloscNaMagazynie; }

    public BooleanProperty wydacProperty() { return wydac; }
    public boolean isWydac() { return wydac.get(); }
}
