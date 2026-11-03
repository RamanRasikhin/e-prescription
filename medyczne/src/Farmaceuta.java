import java.time.LocalDate;

public class Farmaceuta extends Uzytkownik {
    private int aptekaId;

    public Farmaceuta(int id, String imie, String nazwisko, String login, String haslo, LocalDate dataUrodzenia, int aptekaId) {
        super(id, imie, nazwisko, login, haslo, dataUrodzenia);
        this.aptekaId = aptekaId;
    }

    public int getAptekaId() {
        return aptekaId;
    }
}