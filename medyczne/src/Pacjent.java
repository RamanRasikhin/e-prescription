import java.time.LocalDate;

public class Pacjent extends Uzytkownik {
    private String istotneInformacjeMedyczne;

    public Pacjent(int id, String imie, String nazwisko, String login, String haslo, LocalDate dataUrodzenia, String info) {
        super(id, imie, nazwisko, login, haslo, dataUrodzenia);
        this.istotneInformacjeMedyczne = info;
    }

    public String getIstotneInformacjeMedyczne() {
        return istotneInformacjeMedyczne;
    }
}