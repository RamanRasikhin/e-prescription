import fi.iki.elonen.NanoHTTPD;
import com.google.gson.Gson;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerAPI extends NanoHTTPD {

    private final Gson gson = new Gson();

    private final DatabaseHandler databaseHandler = new DatabaseHandler();
    private final AuthService authService = new AuthService();
    private final ReceptaDAO receptaDAO = new ReceptaDAO(databaseHandler);
    private final LekDAO lekDAO = new LekDAO(databaseHandler);
    private final UzytkownikDAO uzytkownikDAO = new UzytkownikDAO(databaseHandler);
    private final MagazynDAO magazynDAO = new MagazynDAO(databaseHandler);

    public ServerAPI() throws IOException {
        super(8080);
        start(NanoHTTPD.SOCKET_READ_TIMEOUT, false);
        System.out.println("\nServer running on http://localhost:8080/ \n");
    }

    public static void main(String[] args) {
        DatabaseHandler db = new DatabaseHandler();
        db.createTables();

        UzytkownikDAO userDAO = new UzytkownikDAO(db);
        LekDAO lekDAO = new LekDAO(db);
        MagazynDAO magDAO = new MagazynDAO(db);

        Lekarz lekarz = new Lekarz(0, "Jan", "Kowalski", "doc1", "pass1", LocalDate.of(1980, 5, 20), "Kardiolog");
        userDAO.zapiszLekarza(lekarz);

        Pacjent pacjent = new Pacjent(0, "Adam", "Nowak", "pat1", "pass1", LocalDate.of(1995, 3, 15), "Alergia");
        userDAO.zapiszPacjenta(pacjent);

        Farmaceuta farmaceuta = new Farmaceuta(0, "Piotr", "Wisniewski", "apt1", "pass1", LocalDate.of(1975, 12, 12), 1);
        userDAO.zapiszFarmaceute(farmaceuta);

        Lek lek = new Lek(0, "Apap", RodzajLeku.TABLETKI, "Bol glowy", LocalDate.of(2025, 10, 10));
        lekDAO.zapiszLek(lek);
        magDAO.dodajLekDoMagazynu(1, 100);

        try {
            new ServerAPI();
            System.in.read();
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        Method method = session.getMethod();

        if (Method.POST.equals(method) && "/api/login".equals(uri)) {
            try {
                Map<String, String> body = parseBody(session);
                String token = authService.login(body.get("login"), body.get("password"));

                if (token != null) {
                    Map<String, String> result = new HashMap<>();
                    result.put("token", token);
                    return newJsonResponce(Response.Status.OK, result);
                } else {
                    return newJsonResponce(Response.Status.UNAUTHORIZED, "Login failed");
                }
            } catch (Exception e) {
                return newJsonResponce(Response.Status.INTERNAL_ERROR, e.getMessage());
            }
        }

        String token = session.getHeaders().get("authorization");
        if (token == null) {
             return newJsonResponce(Response.Status.UNAUTHORIZED, "Missing token");
        }

        if (Method.GET.equals(method) && "/api/pacjent/recepty".equals(uri)) {
            if (!authService.hasAccess(token, "PACJENT")) return accessDenied();

            int uzytkownikId = authService.getUserId(token);
            List<Map<String, Object>> recepty = receptaDAO.getReceptyPacjentaAPI(uzytkownikId);

            return newJsonResponce(Response.Status.OK, recepty);
        }

        if (Method.POST.equals(method) && "/api/lekarz/wystaw".equals(uri)) {
            if (!authService.hasAccess(token, "LEKARZ")) return accessDenied();

            try {
                Map<String, String> body = parseBody(session);

                int pacjentUserId = Integer.parseInt(body.get("pacjentId"));
                String lekIdsRaw = body.get("lekIds");
                int kodRecepty = Integer.parseInt(body.get("kodRecepty"));

                String[] ids = lekIdsRaw.split(",");
                List<Lek> leki = new ArrayList<>();
                for(String id : ids) {
                    Lek l = lekDAO.pobierzLek(Integer.parseInt(id.trim()));
                    if(l != null) leki.add(l);
                }

                if (leki.isEmpty()) return newJsonResponce(Response.Status.BAD_REQUEST, "No valid meds found");

                int lekarzUserId = authService.getUserId(token);

                Lekarz lekarzStub = new Lekarz(lekarzUserId, null, null, null, null, null, null);
                Pacjent pacjentStub = new Pacjent(pacjentUserId, null, null, null, null, null, null);

                Recepta recepta = new Recepta(kodRecepty, pacjentStub, lekarzStub, LocalDate.now().plusDays(30));
                for(Lek l : leki) {
                    recepta.dodajLek(l);
                }

                receptaDAO.zapiszRecepte(recepta);

                return newJsonResponce(Response.Status.OK, "Prescription created");

            } catch (Exception e) {
                e.printStackTrace();
                return newJsonResponce(Response.Status.INTERNAL_ERROR, e.getMessage());
            }
        }

        if (Method.GET.equals(method) && "/api/aptekarz/sprawdz".equals(uri)) {
            if (!authService.hasAccess(token, "FARMACEUTA")) return accessDenied();

            String kodParam = session.getParms().get("kod");
            if (kodParam == null) return newJsonResponce(Response.Status.BAD_REQUEST, "Missing kod");

            try {
                Map<String, Object> r = receptaDAO.znajdzReceptePoKodzie(Integer.parseInt(kodParam));
                if (r != null) {
                    return newJsonResponce(Response.Status.OK, r);
                } else {
                    return newJsonResponce(Response.Status.NOT_FOUND, "Not found");
                }
            } catch (NumberFormatException e) {
                return newJsonResponce(Response.Status.BAD_REQUEST, "Invalid format");
            }
        }

        if (Method.POST.equals(method) && "/api/aptekarz/realizuj".equals(uri)) {
            if (!authService.hasAccess(token, "FARMACEUTA")) return accessDenied();

            try {
                Map<String, String> body = parseBody(session);
                int kod = Integer.parseInt(body.get("kodRecepty"));

                Map<String, Object> rCheck = receptaDAO.znajdzReceptePoKodzie(kod);

                if (rCheck == null || !"AKTYWNA".equals(rCheck.get("status"))) {
                    return newJsonResponce(Response.Status.BAD_REQUEST, "Invalid prescription status");
                }

                @SuppressWarnings("unchecked")
                List<Map<String, Object>> leki = (List<Map<String, Object>>) rCheck.get("leki");

                for(Map<String, Object> l : leki) {
                    int lekId = (int) l.get("id");
                    int stan = magazynDAO.sprawdzStan(lekId);
                    if (stan < 1) {
                        return newJsonResponce(Response.Status.BAD_REQUEST, "Out of stock: " + l.get("nazwa"));
                    }
                }

                for(Map<String, Object> l : leki) {
                    magazynDAO.pobierzLek((int) l.get("id"), 1);
                }

                receptaDAO.zaktualizujStatus(kod, "ZREALIZOWANA");

                return newJsonResponce(Response.Status.OK, "Prescription realized");

            } catch (Exception e) {
                return newJsonResponce(Response.Status.INTERNAL_ERROR, e.getMessage());
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/plain", "Not Found");
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> parseBody(IHTTPSession session) throws IOException, ResponseException {
        Map<String, String> files = new HashMap<>();
        session.parseBody(files);
        String json = files.get("postData");

        if (json == null) return new HashMap<>();
        return gson.fromJson(json, Map.class);
    }

    private Response newJsonResponce(Response.Status status, Object data) {
        String json = gson.toJson(data);
        return newFixedLengthResponse(status, "application/json", json);
    }

    private Response accessDenied() {
        return newJsonResponce(Response.Status.UNAUTHORIZED, "Access Denied");
    }
}