import java.time.LocalDate;

public class Lekarz extends Uzytkownik {
    private String specjalizacja;

    public Lekarz(int id, String imie, String nazwisko, String login, String haslo, LocalDate dataUrodzenia, String specjalizacja) {
        super(id, imie, nazwisko, login, haslo, dataUrodzenia);
        this.specjalizacja = specjalizacja;
    }

    public String getSpecjalizacja() {
        return specjalizacja;
    }
}