# Projekt-Kontext: Vorratshaltung App

## Ziel:
Eine Android-App zur Verwaltung von Vorräten, hauptsächlich bestehend aus Nahrungsmitteln, aber auch anderen Verbrauchsgütern.

Die App soll folgende Dinge ermöglichen:
- Verschiedene Lagerorte anlegen (z.B. Kellerregal, Vorratskammer, Küchenschublade).
- Layout pro Lagerort definieren: Anzahl an Fächern nebeneinander (`columns`) und übereinander (`rows`).
- Übersicht aller vorhandenen Artikel (Ist-Anzahl).
- Übersicht, wie viele Artikel vorhanden sein sollten (Soll-Anzahl).
- Anzeige des Mindesthaltbarkeitsdatums (MHD) (frühestes MHD pro Produkt/Lagerort).
- Automatisch generierte Einkaufsliste für fehlende Artikel (`Soll - Ist > 0`).
- Erfassung und Anzeige von Nährwerten (Kalorien, Eiweiß, Kohlenhydrate, Fett).
- Räumliche Regalansicht (Spatial Grid View) als Startbildschirm.
- Klick auf ein Regalfach öffnet die Listenansicht der darin enthaltenen Artikel.
- Schnell-Aktionen in der Listenansicht:
  - Kurzer Klick / Button (+/-): Ist-Anzahl erhöhen oder verringern.
  - Langer Klick: Soll-Anzahl oder Produktdetails anpassen.

## Mittelfristig:
- Foto-Upload nutzen, um das Regal-Layout automatisch zu bestimmen.
- Räumliche Regalansicht zeigt die vorhandenen Artikel schematisch oder mittels Fotos im Fach an.

## Zunächst Out-Of-Scope:
- Synchronisation der App über mehrere Geräte im selben WLAN (z.B. über NextCloud oder Syncthing).

---

## Architektur & Datenmodell (Room DB Schema)

### Technologiestack
- **UI**: Jetpack Compose (Material 3)
- **Architektur**: MVVM / Clean Architecture + StateFlow
- **DB**: Room Database mit Kotlin Coroutines & Flow
- **Navigation**: Jetpack Compose Navigation

### Entitäten
1. **`StorageLocation` (Lagerort)**
   - `id`: Long (PK)
   - `name`: String (z.B. "Kellerregal")
   - `rows`: Int (Anzahl Fächer übereinander)
   - `columns`: Int (Anzahl Fächer nebeneinander)
   - `type`: StorageType (SHELF, DRAWER, PANTRY)

2. **`Compartment` (Regalfach)**
   - `id`: Long (PK)
   - `storageLocationId`: Long (FK -> StorageLocation)
   - `rowIndex`: Int (0 .. rows-1)
   - `columnIndex`: Int (0 .. columns-1)
   - `label`: String? (optional)

3. **`Product` (Produkt-Stammdaten)**
   - `id`: Long (PK)
   - `name`: String (z.B. "Haltbare Milch 3.5%")
   - `barcode`: String? (EAN)
   - `targetQuantity`: Int (Soll-Anzahl)
   - `unit`: UnitType (STUECK, PACKUNG, GRAMM, LITER)
   - `calories`: Int?, `protein`: Float?, `carbohydrates`: Float?, `fat`: Float? (per 100g/Einheit)

4. **`InventoryItem` (Bestand / Charge im Fach)**
   - `id`: Long (PK)
   - `productId`: Long (FK -> Product)
   - `compartmentId`: Long (FK -> Compartment)
   - `quantity`: Int (Ist-Anzahl im Fach)
   - `expirationDate`: LocalDate? (MHD)
   - `photoUri`: String? (optional)

---

## Umsetzungsfahrplan

- [x] **Phase 1: Projekt-Setup & Datenbankschicht**
  - Room DB, Navigation Compose und KotlinX Dependencies hinzufügen.
  - Entity-Klassen, TypeConverter (LocalDate) und DAOs aufsetzen.
  - Repository-Schnittstellen und Test-Daten.

- [ ] **Phase 2: Räumliche Regalansicht (Spatial Grid View)**
  - UI-Komponente für m × n Raster (Regalfächer) in Compose.
  - Lagerort-Auswahl (Dropdown/Tabs).
  - Status-Anzeige pro Fach (Anzahl Produkte, MHD-Warnung).

- [ ] **Phase 3: Fach-Listenansicht & Schnell-Aktionen**
  - Klick auf Regalfach navigiert zur Fach-Inhaltsliste.
  - `+` / `-` Klicks zur direkten Mengenanpassung.
  - Langer Klick öffnet Soll-Anpassung / Editiermodus.

- [ ] **Phase 4: Einkaufsliste & Produktkatalog / Nährwerte**
  - Generierung der Einkaufsliste (`Soll > Ist`).
  - Nährwertansicht & Produktverwaltung.
