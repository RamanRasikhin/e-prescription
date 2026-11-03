import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Recepta {
    private int kod;
    private Pacjent pacjent;
    private Lekarz lekarzWypisujacy;
    private LocalDate dataWaznosci;
    private List<Lek> listaLekow;
    private String status;

    public Recepta(int kod, Pacjent pacjent, Lekarz lekarz, LocalDate dataWaznosci) {
        this.kod = kod;
        this.pacjent = pacjent;
        this.lekarzWypisujacy = lekarz;
        this.dataWaznosci = dataWaznosci;
        this.listaLekow = new ArrayList<>();
        this.status = "AKTYWNA";
    }

    public void dodajLek(Lek lek) {
        listaLekow.add(lek);
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getKod() { return kod; }
    public Pacjent getPacjent() { return pacjent; }
    public Lekarz getLekarzWypisujacy() { return lekarzWypisujacy; }
    public LocalDate getDataWaznosci() { return dataWaznosci; }
    public List<Lek> getListaLekow() { return listaLekow; }
    public String getStatus() { return status; }
}