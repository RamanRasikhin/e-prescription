import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class PrescriptionWorkflowTest {
    @TempDir Path directory;
    DatabaseHandler database;
    UzytkownikDAO users;
    ReceptaDAO prescriptions;
    MagazynDAO stock;
    Lek first, second;
    int doctor, patient;

    @BeforeEach void setUp() {
        database = new DatabaseHandler(directory.resolve("test.db").toString());
        database.createTables();
        users = new UzytkownikDAO(database);
        users.zapiszLekarza(new Lekarz(0,"Demo","Doctor","doctor","demo-password",LocalDate.of(1980,1,1),"Demo"));
        users.zapiszPacjenta(new Pacjent(0,"Demo","Patient","patient","demo-password",LocalDate.of(1990,1,1),"Synthetic"));
        doctor = users.znajdzUzytkownikaLogowanie("doctor","demo-password").getId();
        patient = users.znajdzUzytkownikaLogowanie("patient","demo-password").getId();
        LekDAO medicines = new LekDAO(database);
        medicines.zapiszLek(new Lek(0,"First demo medicine",RodzajLeku.TABLETKI,"Synthetic",LocalDate.now().plusYears(1)));
        medicines.zapiszLek(new Lek(0,"Second demo medicine",RodzajLeku.TABLETKI,"Synthetic",LocalDate.now().plusYears(1)));
        first = medicines.pobierzWszystkieLeki().get(0);
        second = medicines.pobierzWszystkieLeki().get(1);
        stock = new MagazynDAO(database);
        stock.dodajLekDoMagazynu(first.getId(),5);
        stock.dodajLekDoMagazynu(second.getId(),2);
        prescriptions = new ReceptaDAO(database);
    }

    void issue(int code, LocalDate expiry, Lek... medicines) {
        Recepta prescription = new Recepta(code,
            new Pacjent(patient,null,null,null,null,null,null),
            new Lekarz(doctor,null,null,null,null,null,null),expiry);
        for(Lek medicine:medicines) prescription.dodajLek(medicine);
        prescriptions.zapiszRecepte(prescription);
    }

    @Test void issuedPrescriptionAppearsInPatientHistory() {
        issue(12345,LocalDate.now().plusDays(7),first);
        assertEquals(1,prescriptions.getReceptyPacjentaAPI(patient).size());
        assertNotNull(prescriptions.znajdzReceptePoKodzie(12345));
    }

    @Test void zeroAndNegativeQuantitiesCannotIncreaseStock() {
        assertFalse(stock.pobierzLek(first.getId(),-3));
        assertFalse(stock.wydajLek(first.getId(),0));
        assertEquals(5,stock.sprawdzStan(first.getId()));
        assertThrows(IllegalArgumentException.class,()->stock.dodajLekDoMagazynu(first.getId(),-2));
    }

    @Test void fulfillmentUsesFullQuantitiesAndChangesStatus() {
        issue(12345,LocalDate.now().plusDays(7),first,first,second);
        assertTrue(prescriptions.zrealizujRecepte(12345));
        assertEquals(3,stock.sprawdzStan(first.getId()));
        assertEquals(1,stock.sprawdzStan(second.getId()));
        assertEquals("ZREALIZOWANA",prescriptions.znajdzReceptePoKodzie(12345).get("status"));
    }

    @Test void insufficientStockRollsBackTheWholePrescription() {
        issue(12345,LocalDate.now().plusDays(7),first,second,second,second);
        assertFalse(prescriptions.zrealizujRecepte(12345));
        assertEquals(5,stock.sprawdzStan(first.getId()));
        assertEquals(2,stock.sprawdzStan(second.getId()));
        assertEquals("AKTYWNA",prescriptions.znajdzReceptePoKodzie(12345).get("status"));
    }

    @Test void prescriptionCannotBeDispensedTwice() {
        issue(12345,LocalDate.now().plusDays(7),first);
        assertTrue(prescriptions.zrealizujRecepte(12345));
        assertFalse(prescriptions.zrealizujRecepte(12345));
        assertEquals(4,stock.sprawdzStan(first.getId()));
    }

    @Test void expiredPrescriptionDoesNotChangeStock() {
        issue(12345,LocalDate.now().minusDays(1),first);
        assertFalse(prescriptions.zrealizujRecepte(12345));
        assertEquals(5,stock.sprawdzStan(first.getId()));
    }

    @Test void emptyOrUnknownPrescriptionCannotBeFulfilled() {
        issue(12345,LocalDate.now().plusDays(7));
        assertFalse(prescriptions.zrealizujRecepte(12345));
        assertFalse(prescriptions.zrealizujRecepte(99999));
    }

    @Test void sessionsEnforceRolesAndLogout() {
        AuthService auth = new AuthService(database);
        assertNull(auth.login("doctor","incorrect"));
        String token = auth.login("doctor","demo-password");
        assertNotNull(token);
        assertTrue(auth.hasAccess(token,"LEKARZ"));
        assertFalse(auth.hasAccess(token,"PACJENT"));
        auth.logout(token);
        assertFalse(auth.hasAccess(token,"ANY"));
    }
}
