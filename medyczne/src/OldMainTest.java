import java.time.LocalDate;

public class OldMainTest {
    public static void main(String[] args) {
        System.out.println("=== SYSTEM APTECZNY START ===");

        DatabaseHandler dbHandler = new DatabaseHandler();
        dbHandler.createTables();

        UzytkownikDAO userDao = new UzytkownikDAO(dbHandler);
        LekDAO lekDAO = new LekDAO(dbHandler);
        ReceptaDAO receptaDAO = new ReceptaDAO(dbHandler);
        MagazynDAO magazynDAO = new MagazynDAO(dbHandler);

        Lek apap = new Lek(0, "Apap", RodzajLeku.TABLETKI, "Przeciwbolowy", LocalDate.now().plusYears(1));
        Lek syrop = new Lek(0, "Syrop", RodzajLeku.SYROP, "Na kaszel", LocalDate.now().plusYears(1));

        lekDAO.zapiszLek(apap);
        lekDAO.zapiszLek(syrop);

        apap = lekDAO.pobierzWszystkieLeki().get(0);
        syrop = lekDAO.pobierzWszystkieLeki().get(1);
        magazynDAO.dodajLekDoMagazynu(apap.getId(), 50);
        magazynDAO.dodajLekDoMagazynu(syrop.getId(), 20);

        Lekarz lekarz = new Lekarz(0, "Jan", "Kowalski", "doktor1", "pass1", LocalDate.of(1970, 5, 5), "Internista");
        Pacjent pacjent = new Pacjent(0, "Adam", "Nowak", "pacjent1", "pass1", LocalDate.of(1990, 1, 1), "Alergia");
        Farmaceuta farmaceuta = new Farmaceuta(0, "Anna", "Ziele", "apteka1", "pass1", LocalDate.of(1985, 3, 15), 1);

        userDao.zapiszLekarza(lekarz);
        userDao.zapiszPacjenta(pacjent);
        userDao.zapiszFarmaceute(farmaceuta);

        int doctorId = userDao.znajdzUzytkownikaLogowanie("doktor1", "pass1").getId();
        int patientId = userDao.znajdzUzytkownikaLogowanie("pacjent1", "pass1").getId();
        int kodRecepty = 55555;
        Lekarz lekarzStub = new Lekarz(doctorId, null, null, null, null, null, null);
        Pacjent pacjentStub = new Pacjent(patientId, null, null, null, null, null, null);

        Recepta nowaRecepta = new Recepta(kodRecepty, pacjentStub, lekarzStub, LocalDate.now().plusDays(30));
        nowaRecepta.dodajLek(apap);
        nowaRecepta.dodajLek(syrop);

        receptaDAO.zapiszRecepte(nowaRecepta);
        System.out.println("Lekarz wystawil recepte #" + kodRecepty);

        System.out.println("\n--- Realizacja w aptece ---");

        if (!receptaDAO.zrealizujRecepte(kodRecepty)) {
            throw new IllegalStateException("Nie mozna zrealizowac recepty.");
        }
        System.out.println("Recepta zrealizowana pomyslnie.");

        System.out.println("\n=== KONIEC TESTU ===");
    }
}