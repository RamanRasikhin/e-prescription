import javafx.application.Application;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class  Main extends Application {

    static DatabaseHandler dbHandler = new DatabaseHandler();

    static UzytkownikDAO userDao = new UzytkownikDAO(dbHandler);
    static LekDAO lekDAO = new LekDAO(dbHandler);
    static ReceptaDAO receptaDAO = new ReceptaDAO(dbHandler);
    static MagazynDAO magazynDAO = new MagazynDAO(dbHandler);

    private Scene createPacjentPanel(Stage stage, int id) {

        int pacjentId = receptaDAO.getPacjentIdByUserId(id);
        if (pacjentId == -1) {

            return new Scene(new Label("Błąd: nie znaleziono pacjenta"), 300, 200);
        }

        Label title = new Label("Panel Pacjenta");
        title.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold;");

        // ===== TABELA =====
        TableView<ReceptaView> table = new TableView<>();

        TableColumn<ReceptaView, Integer> colKod =
                new TableColumn<>("Kod recepty");
        colKod.setCellValueFactory(
                new PropertyValueFactory<>("kodRecepty"));

        TableColumn<ReceptaView, String> colData =
                new TableColumn<>("Data ważności");
        colData.setCellValueFactory(
                new PropertyValueFactory<>("dataWaznosci"));

        TableColumn<ReceptaView, String> colStatus =
                new TableColumn<>("Status");
        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn<ReceptaView, Integer> colLeki =
                new TableColumn<>("Liczba leków");
        colLeki.setCellValueFactory(
                new PropertyValueFactory<>("liczbaLekow"));

        table.getColumns().addAll(colKod, colData, colStatus, colLeki);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        MenuItem copyItem = new MenuItem("Kopiuj kod recepty");

        copyItem.setOnAction(e -> {
            ReceptaView selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(selected.getKodRecepty()));
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        ContextMenu contextMenu = new ContextMenu(copyItem);
        table.setContextMenu(contextMenu);


        // ===== DANE =====
        table.getItems().addAll(
                receptaDAO.getReceptyPacjenta(pacjentId)
        );

        // ===== ODŚWIEŻ =====
        Button refreshButton = new Button("Odśwież");
        refreshButton.setOnAction(e -> {
            table.getItems().clear();
            table.getItems().addAll(
                    receptaDAO.getReceptyPacjenta(pacjentId)
            );
        });

        Button logoutButton = new Button("Wyloguj");
        logoutButton.setOnAction(e ->
                stage.setScene(createLoginScene(stage))
        );

        HBox buttons = new HBox(10, refreshButton);
        buttons.setAlignment(Pos.CENTER);

        HBox topBar = new HBox(logoutButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));

        VBox main = new VBox(15, title, table, buttons);
        main.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(main);

        return new Scene(root, 700, 500);
    }

    private Scene createLekarzPanel(Stage stage, int id) {

        int lekarzId = receptaDAO.getLekarzIdByUserId(id);
        if (lekarzId == -1) {
            return new Scene(new Label("Błąd: nie znaleziono lekarza"), 300, 200);
        }

        Label title = new Label("Panel Lekarza");
        title.setStyle("-fx-font-size: 18pt; -fx-font-weight: bold;");

        // ===== TABELA =====
        TableView<ReceptaView> table = new TableView<>();

        TableColumn<ReceptaView, Integer> colKod =
                new TableColumn<>("Kod recepty");
        colKod.setCellValueFactory(
                new PropertyValueFactory<>("kodRecepty"));

        TableColumn<ReceptaView, String> colData =
                new TableColumn<>("Data ważności");
        colData.setCellValueFactory(
                new PropertyValueFactory<>("dataWaznosci"));

        TableColumn<ReceptaView, String> colStatus =
                new TableColumn<>("Status");
        colStatus.setCellValueFactory(
                new PropertyValueFactory<>("status"));

        TableColumn<ReceptaView, Integer> colLeki =
                new TableColumn<>("Liczba leków");
        colLeki.setCellValueFactory(
                new PropertyValueFactory<>("liczbaLekow"));

        table.getColumns().addAll(colKod, colData, colStatus, colLeki);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        MenuItem copyItem = new MenuItem("Kopiuj kod recepty");

        copyItem.setOnAction(e -> {
            ReceptaView selected = table.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ClipboardContent content = new ClipboardContent();
                content.putString(String.valueOf(selected.getKodRecepty()));
                Clipboard.getSystemClipboard().setContent(content);
            }
        });

        ContextMenu contextMenu = new ContextMenu(copyItem);
        table.setContextMenu(contextMenu);


        // ===== DANE =====
        table.getItems().addAll(
                receptaDAO.getReceptyLekarza(lekarzId)
        );

        // ===== ODŚWIEŻ =====
        Button refreshButton = new Button("Odśwież");
        refreshButton.setOnAction(e -> {
            table.getItems().clear();
            table.getItems().addAll(
                    receptaDAO.getReceptyLekarza(lekarzId)
            );
        });

        Button receptaButton = new Button("Nowa recepta");
        receptaButton.setOnAction(e -> {
            createReceptaWindow(stage, lekarzId);
        });

        Button uniewaznijButton = new Button("Unieważnij");
        uniewaznijButton.setOnAction(e -> {

            var selected = table.getSelectionModel().getSelectedItems();

            if (selected.isEmpty()) {
                new Alert(Alert.AlertType.WARNING,
                        "Zaznacz przynajmniej jedną receptę.")
                        .showAndWait();
                return;
            }

            Alert confirm = new Alert(
                    Alert.AlertType.CONFIRMATION,
                    "Czy na pewno chcesz unieważnić zaznaczone recepty?",
                    ButtonType.YES,
                    ButtonType.NO
            );

            confirm.showAndWait();

            if (confirm.getResult() != ButtonType.YES) {
                return;
            }

            for (ReceptaView rv : selected) {
                receptaDAO.uniewaznijRecepte(rv.getKodRecepty());
            }

            // odśwież tabelę
            table.getItems().clear();
            table.getItems().addAll(
                    receptaDAO.getReceptyLekarza(lekarzId)
            );
        });


        Button logoutButton = new Button("Wyloguj");
        logoutButton.setOnAction(e ->
                stage.setScene(createLoginScene(stage))
        );

        HBox buttons = new HBox(10, refreshButton, receptaButton, uniewaznijButton);
        buttons.setAlignment(Pos.CENTER);

        HBox topBar = new HBox(logoutButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));

        VBox main = new VBox(15, title, table, buttons);
        main.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(main);

        return new Scene(root, 700, 500);
    }

    private Scene createFarmaceutaPanel(Stage stage, int id) {
        Label title = new Label("Panel farmaceuty");
        title.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        // ===== TABELA =====
        TableView<LekMagazynView> table = new TableView<>();

        TableColumn<LekMagazynView, String> colNazwa =
                new TableColumn<>("Nazwa");
        colNazwa.setCellValueFactory(
                new PropertyValueFactory<>("nazwa"));

        TableColumn<LekMagazynView, String> colData =
                new TableColumn<>("Data ważności");
        colData.setCellValueFactory(
                new PropertyValueFactory<>("dataWaznosci"));

        TableColumn<LekMagazynView, String> colOpis =
                new TableColumn<>("Opis");
        colOpis.setCellValueFactory(
                new PropertyValueFactory<>("opis"));

        TableColumn<LekMagazynView, Integer> colIlosc =
                new TableColumn<>("Ilość");
        colIlosc.setCellValueFactory(
                new PropertyValueFactory<>("ilosc"));

        table.getColumns().addAll(colNazwa, colData, colOpis, colIlosc);
        table.getItems().addAll(magazynDAO.pobierzLekiZMagazynu());
        table.setPrefHeight(250);


        TextField kodField = new TextField();
        kodField.setPromptText("Kod recepty");
        Label wynikLabel = new Label();

        Button refreshButton = new Button("Odśwież");
        refreshButton.setOnAction(e -> {
            table.getItems().clear();
            table.getItems().addAll(magazynDAO.pobierzLekiZMagazynu());
        });

        Button sprawdzBtn = new Button("Sprawdź receptę");
        sprawdzBtn.setOnAction(e -> {
            try {
                int kod = Integer.parseInt(kodField.getText());

                ReceptaDAO receptaDAO = new ReceptaDAO(dbHandler);
                var recepta = receptaDAO.znajdzReceptePoKodzie(kod);

                Map<String, Object> receptaFar = receptaDAO.znajdzReceptePoKodzie(kod);

                if (receptaFar == null) {
                    wynikLabel.setText("❌ Brak recepty");
                    return;
                }

                String status = (String) receptaFar.get("status");
                LocalDate dataWaznosci = LocalDate.parse((String) receptaFar.get("waznosc"));

// 🔒 STATUS
                if (!"AKTYWNA".equalsIgnoreCase(status)) {
                    wynikLabel.setText("🚫 Recepta jest " + status.toLowerCase());
                    wynikLabel.setStyle("-fx-text-fill: red;");
                    return;
                }

// ⏰ DATA
                if (dataWaznosci.isBefore(LocalDate.now())) {
                    wynikLabel.setText("⏰ Recepta jest przeterminowana");
                    wynikLabel.setStyle("-fx-text-fill: red;");
                    return;
                }

// ✅ OK
                createRealizacjaRecepty(stage, kod);


                // ✅ WSZYSTKO OK → REALIZACJA
                createRealizacjaRecepty(stage, kod);

            } catch (NumberFormatException ex) {
                wynikLabel.setText("⚠️ Nieprawidłowy kod");
                wynikLabel.setStyle("-fx-text-fill: orange;");
            }
        });


        Button logoutButton = new Button("Wyloguj");
        logoutButton.setOnAction(e ->
                stage.setScene(createLoginScene(stage))
        );

        VBox receptaBox = new VBox(10, kodField, sprawdzBtn, wynikLabel);
        receptaBox.setPadding(new Insets(10));

        HBox topBar = new HBox(logoutButton);
        topBar.setAlignment(Pos.TOP_RIGHT);
        topBar.setPadding(new Insets(10));

        HBox tableControls = new HBox(10, refreshButton);
        tableControls.setAlignment(Pos.CENTER_LEFT);

        VBox main = new VBox(15, title, table, tableControls, new Separator(), receptaBox);
        main.setPadding(new Insets(20));

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(main);

        return new Scene(root, 700, 500);
    }

    private Scene createLoginScene(Stage stage) {
        Label titleLabel = new Label("WITAMY! - zaloguj się do swojego profilu");
        Label loginLabel = new Label("Login:");
        Label passwordLabel = new Label("Hasło:");
        Label messageLabel = new Label();

        TextField loginField = new TextField();
        PasswordField passwordField = new PasswordField();
        Button loginButton = new Button("Zaloguj");

        loginButton.setOnAction(e -> {
            String login = loginField.getText();
            String password = passwordField.getText();

            UserResult result = userDao.znajdzUzytkownikaLogowanie(login, password);

            if (result != null) {
                messageLabel.setText("Zalogowano poprawnie ✅");
                messageLabel.setStyle("-fx-text-fill: green;");

                switch (result.getRola()) {
                    case "LEKARZ":
                        stage.setScene(createLekarzPanel(stage, result.getId()));
                        break;
                    case "PACJENT":
                        stage.setScene(createPacjentPanel(stage, result.getId()));
                        break;
                    case "FARMACEUTA":
                        stage.setScene(createFarmaceutaPanel(stage, result.getId()));
                        break;
                }

            } else {
                messageLabel.setText("Błędny login lub hasło ❌");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        GridPane grid = new GridPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setAlignment(Pos.CENTER);

        grid.add(titleLabel, 0, 0, 2, 1);
        grid.add(loginLabel, 0, 1);
        grid.add(loginField, 1, 1);
        grid.add(passwordLabel, 0, 2);
        grid.add(passwordField, 1, 2);
        grid.add(loginButton, 1, 3);
        grid.add(messageLabel, 1, 4);

        return new Scene(grid, 500, 400);
    }

    public void createReceptaWindow(Stage ownerStage, int lekarzId) {

        int lekarzUserId = UzytkownikDAO.getUserIdByLekarzId(lekarzId);

        Stage stage = new Stage();
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initOwner(ownerStage);
        stage.setTitle("Tworzenie nowej recepty");

        // ===== Pacjent =====
        TextField imieField = new TextField();
        imieField.setPromptText("Imię pacjenta");
        TextField nazwiskoField = new TextField();
        nazwiskoField.setPromptText("Nazwisko pacjenta");

        // ===== Lista leków =====
        ListView<Lek> lekListView = new ListView<>();
        lekListView.setItems(FXCollections.observableArrayList(lekDAO.pobierzWszystkieLeki()));
        lekListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // ===== Przycisk zapisania =====
        Button zapiszBtn = new Button("Zapisz receptę");
        Label statusLabel = new Label();

        zapiszBtn.setOnAction(e -> {
            try {
                String imie = imieField.getText().trim();
                String nazwisko = nazwiskoField.getText().trim();

                if (imie.isEmpty() || nazwisko.isEmpty()) {
                    statusLabel.setText("⚠️ Wpisz imię i nazwisko pacjenta");
                    return;
                }

                // Szukaj pacjenta - POPRAWIONE: użyj equals() zamiast ==
                UserResult pacjentResult = UzytkownikDAO.znajdzUzytkownikowPoImieniuINazwisku(imie, nazwisko);
                if (pacjentResult != null && "PACJENT".equals(pacjentResult.getRola())) {
                    Pacjent pacjent = UzytkownikDAO.znajdzPacjentaPoId(pacjentResult.getId());

                    List<Lek> wybraneLeki = new ArrayList<>(lekListView.getSelectionModel().getSelectedItems());
                    if (wybraneLeki.isEmpty()) {
                        statusLabel.setText("⚠️ Wybierz przynajmniej jeden lek");
                        return;
                    }

                    Random rand = new Random();
                    int kod;
                    do {
                        kod = rand.nextInt(1000000) + 1000000;
                    } while (receptaDAO.czyKodReceptyZajety(kod));

                    // Tworzymy receptę - POPRAWIONE: użyj lekarzUserId zamiast lekarzId
                    Lekarz lekarz = UzytkownikDAO.znajdzLekarzaPoId(lekarzUserId);
                    if (lekarz == null) {
                        statusLabel.setText("❌ Błąd: nie znaleziono lekarza");
                        return;
                    }

                    Recepta recepta = new Recepta(
                            kod,
                            pacjent,
                            lekarz,
                            LocalDate.now().plusDays(30)
                    );

                    for (Lek l : wybraneLeki) {
                        recepta.dodajLek(l);
                    }

                    receptaDAO.zapiszRecepte(recepta);
                    statusLabel.setText("✅ Recepta zapisana");
                    stage.close();

                } else {
                    statusLabel.setText("❌ Nie znaleziono pacjenta o podanych danych");
                }

            } catch (NumberFormatException ex) {
                statusLabel.setText("⚠️ Niepoprawny kod recepty");
            }
        });

        VBox layout = new VBox(10,
                new Label("Imię pacjenta:"), imieField,
                new Label("Nazwisko pacjenta:"), nazwiskoField,
                new Label("Wybierz leki:"),
                new Label("📌 Zaznacz wiele leków używając CTRL + klik"), lekListView,
                zapiszBtn,
                statusLabel
        );
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 400, 500));
        stage.show();
    }

    public void createRealizacjaRecepty(Stage owner, int kodRecepty) {

        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Realizacja recepty " + kodRecepty);

        TableView<LekRealizacjaRow> table = new TableView<>();

        TableColumn<LekRealizacjaRow, String> colNazwa =
                new TableColumn<>("Lek");
        colNazwa.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getNazwa()));

        TableColumn<LekRealizacjaRow, Integer> colNaRecepcie =
                new TableColumn<>("Na recepcie");
        colNaRecepcie.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getIloscNaRecepcie()).asObject());

        TableColumn<LekRealizacjaRow, Integer> colNaMagazynie =
                new TableColumn<>("Na magazynie");
        colNaMagazynie.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getIloscNaMagazynie()).asObject());

        TableColumn<LekRealizacjaRow, Boolean> colWydaj =
                new TableColumn<>("Wydać");
        colWydaj.setCellValueFactory(c -> c.getValue().wydacProperty());
        colWydaj.setCellFactory(CheckBoxTableCell.forTableColumn(colWydaj));

        table.getColumns().addAll(colNazwa, colNaRecepcie, colNaMagazynie, colWydaj);
        table.setEditable(true);

        Map<Lek, Integer> leki = receptaDAO.getLekiZRecepty(kodRecepty);
        List<LekRealizacjaRow> rows = new ArrayList<>();

        for (var entry : leki.entrySet()) {
            Lek lek = entry.getKey();
            int naRecepcie = entry.getValue();
            int naMagazynie = magazynDAO.sprawdzStan(lek.getId());
            rows.add(new LekRealizacjaRow(lek, naRecepcie, naMagazynie));
        }

        table.setItems(FXCollections.observableArrayList(rows));

        Button realizujBtn = new Button("Zrealizuj");
        realizujBtn.setOnAction(e -> {

            for (LekRealizacjaRow row : table.getItems()) {
                if (row.isWydac()) {
                    boolean success = magazynDAO.wydajLek(
                            row.getLek().getId(),
                            row.getIloscNaRecepcie()
                    );

                    if (!success) {
                        new Alert(Alert.AlertType.ERROR,
                                "Brak leku: " + row.getNazwa())
                                .showAndWait();
                        return;
                    }
                }
            }

            receptaDAO.zaktualizujStatus(kodRecepty, "ZREALIZOWANA");
            stage.close();
        });

        VBox layout = new VBox(10, table, realizujBtn);
        layout.setPadding(new Insets(15));

        stage.setScene(new Scene(layout, 600, 400));
        stage.show();
    }


    @Override
    public void start(Stage stage) {
        stage.setTitle("System Zarządzania Receptami");
        stage.setScene(createLoginScene(stage));
        stage.show();
    }



    public static void main(String[] args) {

        dbHandler.createTables();

        Lekarz lekarz = new Lekarz(1, "Jan", "Kowalski", "lekarz", "pass1", LocalDate.of(1980, 5, 20), "Kardiolog");
        userDao.zapiszLekarza(lekarz);

        Pacjent pacjent = new Pacjent(2, "Adam", "Nowak", "pacjent", "pass1", LocalDate.of(1995, 3, 15), "Alergia");
        userDao.zapiszPacjenta(pacjent);

        Farmaceuta farmaceuta = new Farmaceuta(3, "Piotr", "Wisniewski", "farmaceuta", "pass1", LocalDate.of(1975, 12, 12), 1);
        userDao.zapiszFarmaceute(farmaceuta);

        launch(args);
    }
}
