import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class OldMainTest {
    public static void main(String[] args) {
        System.out.println("=== SYSTEM APTECZNY START ===");

        DatabaseHandler dbHandler = new DatabaseHandler();
        dbHandler.createTables();

        UzytkownikDAO userDao = new UzytkownikDAO(dbHandler);
        LekDAO lekDAO = new LekDAO(dbHandler);
        ReceptaDAO receptaDAO = new ReceptaDAO(dbHandler);
        MagazynDAO magazynDAO = new MagazynDAO(dbHandler);

        Lek apap = new Lek(0, "Apap", RodzajLeku.TABLETKI, "Przeciwbolowy", LocalDate.of(2026, 12, 12));
        Lek syrop = new Lek(0, "Syrop", RodzajLeku.SYROP, "Na kaszel", LocalDate.of(2025, 5, 10));

        lekDAO.zapiszLek(apap);
        lekDAO.zapiszLek(syrop);

        magazynDAO.dodajLekDoMagazynu(1, 50);
        magazynDAO.dodajLekDoMagazynu(2, 20);

        Lekarz lekarz = new Lekarz(0, "Jan", "Kowalski", "doktor1", "pass1", LocalDate.of(1970, 5, 5), "Internista");
        Pacjent pacjent = new Pacjent(0, "Adam", "Nowak", "pacjent1", "pass1", LocalDate.of(1990, 1, 1), "Alergia");
        Farmaceuta farmaceuta = new Farmaceuta(0, "Anna", "Ziele", "apteka1", "pass1", LocalDate.of(1985, 3, 15), 1);

        userDao.zapiszLekarza(lekarz);
        userDao.zapiszPacjenta(pacjent);
        userDao.zapiszFarmaceute(farmaceuta);

        Lekarz lekarzDb = userDao.znajdzLekarzaPoId(1);

        int kodRecepty = 55555;

        Lekarz lekarzStub = new Lekarz(1, null, null, null, null, null, null);
        Pacjent pacjentStub = new Pacjent(1, null, null, null, null, null, null);

        Recepta nowaRecepta = new Recepta(kodRecepty, pacjentStub, lekarzStub, LocalDate.now().plusDays(30));
        nowaRecepta.dodajLek(apap);
        nowaRecepta.dodajLek(syrop);

        receptaDAO.zapiszRecepte(nowaRecepta);
        System.out.println("Lekarz wystawil recepte #" + kodRecepty);

        System.out.println("\n--- Realizacja w aptece ---");

        Map<String, Object> receptaDb = receptaDAO.znajdzReceptePoKodzie(kodRecepty);

        if (receptaDb != null && "AKTYWNA".equals(receptaDb.get("status"))) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> leki = (List<Map<String, Object>>) receptaDb.get("leki");

            boolean czyMoznaZrealizowac = true;
            for(Map<String, Object> l : leki) {
                int idLeku = (int) l.get("id");
                if(magazynDAO.sprawdzStan(idLeku) < 1) {
                    czyMoznaZrealizowac = false;
                    System.out.println("Brak leku: " + l.get("nazwa"));
                }
            }

            if(czyMoznaZrealizowac) {
                for(Map<String, Object> l : leki) {
                    magazynDAO.pobierzLek((int) l.get("id"), 1);
                    System.out.println("Wydano: " + l.get("nazwa"));
                }
                receptaDAO.zaktualizujStatus(kodRecepty, "ZREALIZOWANA");
                System.out.println("Recepta zrealizowana pomyslnie.");
            } else {
                System.out.println("Nie mozna zrealizowac recepty.");
            }
        }

        System.out.println("\n=== KONIEC TESTU ===");
    }
}