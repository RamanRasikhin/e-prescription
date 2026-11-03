import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public class Lek {
    private int id;
    private String nazwa;
    private RodzajLeku rodzaj;
    private String opis;
    private LocalDate dataWaznosci;

    public Lek(int id, String nazwa, RodzajLeku rodzaj, String opis, LocalDate dataWaznosci) {
        this.id = id;
        this.nazwa = nazwa;
        this.rodzaj = rodzaj;
        this.opis = opis;
        this.dataWaznosci = dataWaznosci;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Lek lek = (Lek) o;
        return id == lek.id;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public long wyswietlIleDniDoPrzeterminowania() {
        return ChronoUnit.DAYS.between(LocalDate.now(), dataWaznosci);
    }

    public String toString() {
        return nazwa + " (" + rodzaj + ")";
    }

    public int getId() { return id; }
    public String getNazwa() { return nazwa; }
    public RodzajLeku getRodzaj() { return rodzaj; }
    public String getOpis() { return opis; }
    public LocalDate getDataWaznosci() { return dataWaznosci; }
}