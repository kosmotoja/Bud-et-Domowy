# 💰 Budżet Domowy (Home Budget App)

<p align="center">
  <img width="280" alt="App Preview" src="https://github.com/user-attachments/assets/42c2c96c-8771-4049-80e6-5113bf7dadd3" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Platform-Android-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Platform">
  <img src="https://img.shields.io/badge/Framework-Jetpack_Compose-4285F4?style=for-the-badge&logo=jetpackcompose&logoColor=white" alt="Jetpack Compose">
  <img src="https://img.shields.io/badge/Database-Room_%2F_SQLite-003B57?style=for-the-badge&logo=sqlite&logoColor=white" alt="SQLite">
  <img src="https://img.shields.io/badge/Status-BUILD_SUCCESSFUL-success?style=for-the-badge" alt="Status">
</p>

Nowoczesna, w pełni lokalna aplikacja na system Android służąca do kompleksowej kontroli budżetu domowego i śledzenia wydatków. Stworzona w technologii **Jetpack Compose** z zachowaniem najwyższych standardów UI/UX w stylistyce **Neon Dark Mode**.

---

## 🌟 Zrealizowane funkcjonalności

### 1. Zarządzanie Przychodem (Zarobki)
* **Ręczna edycja pensji:** Możliwość podania kwoty wypłaty dla wybranego miesiąca.
* **Pamięć kwoty domyślnej:** Zapisanie pensji automatycznie ustala tę kwotę jako domyślną dla nowo tworzonych miesięcy, eliminując konieczność wpisywania jej za każdym razem.

### 2. Kategoryzacja i Śledzenie Wydatków
Aplikacja posiada 7 predefiniowanych kategorii z dedykowanymi neonowymi barwami i ikonami:
* 🟣 **Czynsz** (`#6366f1` Indygo)
* 🟡 **Prąd** (`#f59e0b` Ciepły bursztyn)
* 🌸 **Gaz** (`#ec4899` Malinowy róż)
* 🟢 **Jedzenie** (`#10b981` Żywa zieleń)
* 🔴 **Raty** (`#ef4444` Klasyczna czerwień)
* 🔵 **Media** (`#06b6d4` Błękit morski)
* 🟣 **Inne** (`#a855f7` Jasny fiolet)

* **Szybkie dopisywanie:** Wprowadzanie kwoty z selektorem kategorii oraz szybkimi kafelkami nominałów (`+20 zł`, `+50 zł`, `+100 zł`, `+200 zł`).
* **Zerowanie kategorii:** Przycisk krzyżyka (`×`) przy każdej kategorii z możliwością natychmiastowego zresetowania kosztów w danej grupie do 0 zł.

### 3. Niezależne Oszczędności (Skarbonka)
* Świadoma deklaracja oszczędności (niezależna od resztek z pensji).
* **Dwa tryby pracy:**
  * **Ustaw:** Ustalenie sztywnej kwoty odłożonej w danym miesiącu.
  * **+ Dopłać:** Stopniowe dorzucanie drobnych kwot do wirtualnej skarbonki.

### 4. Inteligentny Bilans Płynności („Wolne środki”)
* Przeliczanie wzoru w czasie rzeczywistym:  
  $$\text{Wolne środki} = \text{Zarobki} - \text{Wydatki} - \text{Oszczędności}$$
* **Wskaźnik bezpieczeństwa:** Świecący błękit/cyan (`#06b6d4`) przy zachowaniu płynności finansowej oraz automatyczna zmiana na koralową czerwień (`#f43f5e`) z ostrzeżeniem o deficycie przy przekroczeniu budżetu.

### 5. Wielomiesięczny Kalendarz i Historia
* Belka nawigacyjna z przyciskami dotykowymi `◀` oraz `▶`.
* Rozwijane menu wyboru miesiąca (od 12 miesięcy wstecz do 6 miesięcy w przód).
* Przycisk **„Dzisiaj”**, który pojawia się przy przeglądaniu historii lub planowaniu przyszłości, umożliwiając powrót jednym kliknięciem.
* Niezależne bazy dla każdego miesiąca z podglądem trendu ostatnich miesięcy na dole ekranu.

### 6. Prywatność i Bezpieczeństwo (Local-First & Offline)
* Wszystkie dane są zapisywane wyłącznie lokalnie w bazie **Room (SQLite)** na urządzeniu użytkownika. 
* Brak wymogu logowania, rejestracji oraz zewnętrznych serwerów (100% prywatności).

---

## 🎨 Styl i Interfejs (UI/UX)

* **Neon Dark Mode:** Głębokie tło (`#0e101a` / `#11131e`), panele w kolorze ciemnego antracytu (`#181b2a`) ze świetlistymi obramowaniami.
* **Siatka metryk 2×2:** Duże, czytelne kafelki (Zarobki, Wydatki, Oszczędności, Wolne środki) z ikonami ołówka (`✎`) przekierowującymi natychmiast do edycji.
* **Wielobarwny wykres Donut:** Rysowany płynnie za pomocą `Jetpack Compose Canvas` z zaokrąglonymi segmentami i estetycznie wyśrodkowaną sumą wydatków.
* **Paski postępu:** Animowane mini-wskaźniki procentowego udziału każdej kategorii w łącznych wydatkach.
* **Dotyk i Haptyka:** Przyciski dostosowane do obsługi kciukiem ze sprzężeniem haptycznym przy kliknięciach.
* **Widżet na pulpit:** Gotowy komponent `Android AppWidget` z podglądem wolnych środków, pensji, wydatków i oszczędności wprost z ekranu głównego telefonu.
* **Adaptacyjna ikona:** Dedykowana ikona z neonowym symbolem portfela i skarbonki na ciemnogranatowym tle.

---

## 🛠️ Status Projektu

* Wszystkie testy jednostkowe oraz proces kompilacji aplikacji zakończyły się pełnym sukcesem (`BUILD SUCCESSFUL`). 
* Aplikacja jest w pełni sprawna i gotowa do użytku.
