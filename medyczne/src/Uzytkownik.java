import java.time.LocalDate;
import java.time.Period;

public abstract class Uzytkownik {
    protected int id;
    protected String imie;
    protected String nazwisko;
    protected String login;
    protected String haslo;
    protected LocalDate dataUrodzenia;

    public Uzytkownik(int id, String imie, String nazwisko, String login, String haslo, LocalDate dataUrodzenia) {
        this.id = id;
        this.imie = imie;
        this.nazwisko = nazwisko;
        this.login = login;
        this.haslo = haslo;
        this.dataUrodzenia = dataUrodzenia;
    }

    public String toString(){
        return imie + " " + nazwisko + " (" + login + ")";
    }

    public String getImie() { return imie; }
    public String getNazwisko() { return nazwisko; }
    public String getLogin() { return login; }
    public LocalDate getDataUrodzenia() { return dataUrodzenia; }

    public int pokazWiek() {
        return Period.between(dataUrodzenia, LocalDate.now()).getYears();
    }

    public boolean zaloguj(String podanyLogin, String podaneHaslo) {
        return this.login.equals(podanyLogin) && this.haslo.equals(podaneHaslo);
    }

    public int getId() {
        return id;
    }

    public String getHaslo() {
        return haslo;
    }
}