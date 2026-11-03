PRAGMA foreign_keys = ON;

CREATE TABLE uzytkownik (
    id TEXT PRIMARY KEY,
    imie TEXT,
    nazwisko TEXT,
    data_urodzenia TEXT,
    haslo TEXT,
    rola TEXT
);

CREATE TABLE farmaceuta (
    id TEXT PRIMARY KEY,
    uzytkownik_id TEXT NOT NULL,
    apteka_id INTEGER,
    FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id)
);

CREATE UNIQUE INDEX farmaceuta_uzytkownik_idx
    ON farmaceuta(uzytkownik_id);

CREATE TABLE lekarz (
    id TEXT PRIMARY KEY,
    uzytkownik_id TEXT NOT NULL,
    specjalizacja TEXT,
    FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id)
);

CREATE UNIQUE INDEX lekarz_uzytkownik_idx
    ON lekarz(uzytkownik_id);

CREATE TABLE pacjent (
    id TEXT PRIMARY KEY,
    uzytkownik_id TEXT NOT NULL,
    istotneinformacjamedyczna TEXT,
    FOREIGN KEY (uzytkownik_id) REFERENCES uzytkownik(id)
);

CREATE UNIQUE INDEX pacjent_uzytkownik_idx
    ON pacjent(uzytkownik_id);

CREATE TABLE lek (
    id TEXT PRIMARY KEY,
    name TEXT,
    opis TEXT,
    datawaznosci TEXT,
    rodzaj TEXT
);

CREATE TABLE recepta (
    id TEXT PRIMARY KEY,
    kod_recepty INTEGER, 
    lekarz_id TEXT NOT NULL,
    pacjent_id TEXT NOT NULL,
    lek_id TEXT NOT NULL,
    data_waznosci TEXT, 
    status TEXT,
    FOREIGN KEY (lekarz_id) REFERENCES lekarz(id),
    FOREIGN KEY (pacjent_id) REFERENCES pacjent(id),
    FOREIGN KEY (lek_id) REFERENCES lek(id)
);

