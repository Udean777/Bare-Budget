<div align="center">
  <img src="art/app_logo.png" width="120" height="120" alt="BareBudget Logo" style="border-radius: 24px;" />
  
  # BareBudget
  
  **Frictionless Personal Finance & Survival Runway Tracker**
  
  *Kelola keuangan bulanan, hitung tanggal bertahan hidup finansialmu (Runway/Death Day), kelola multi-dompet, dan capai target tabungan.*

  ---

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Material Design 3](https://img.shields.io/badge/UI-Material%20Design%203-7B1FA2?style=for-the-badge&logo=materialdesign&logoColor=white)](https://m3.material.io/)
  [![Go Fiber](https://img.shields.io/badge/Backend-Go%20Fiber-00ADD8?style=for-the-badge&logo=go&logoColor=white)](https://gofiber.io/)
  [![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Room DB](https://img.shields.io/badge/Local%20Cache-Room%20SQLite-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![Firebase Auth](https://img.shields.io/badge/Auth-Firebase%20%26%20Google-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)

</div>

---

## 📖 Tentang BareBudget

**BareBudget** adalah aplikasi personal finance minimalis, cepat, dan modern yang dirancang untuk menjawab pertanyaan krusial di tengah bulan:
> *"Dengan gaya hidup belanja saat ini, sampai tanggal berapa sisa uangku bisa bertahan hidup?"*

Seluruh antarmuka aplikasi dibangun **100% menggunakan Material Design 3 (M3)** dengan Jetpack Compose, menghasilkan visual yang konsisten, modern, adaptif, serta mendukung penuh personalisasi tema dinamis (*Material You*).

Dengan arsitektur **Offline-First**, BareBudget dapat langsung digunakan seketika tanpa login (**Guest Mode**) dan dapat ditautkan ke **Google Cloud Account** kapan saja untuk sinkronisasi tanpa kehilangan data historis.

---

## ✨ Fitur Unggulan

### 1. 🧮 Financial Runway & "Estimated Death Day"
* Menghitung kecepatan pengeluaran harian (*burn rate*).
* Memprediksi tanggal persis kapan saldo budget Anda akan habis di bulan berjalan.
* Memberikan indikator status kesehatan keuangan secara real-time (*Aman*, *Waspada*, atau *Kritis*).
* **Monthly Budget Lock & Audit**: Budget bulanan hanya dapat diset 1x per bulan untuk menjaga disiplin keuangan dan konsistensi audit (*expenses only*, tidak bercampur dengan alokasi tagihan/bills).
* **Budget Gate**: Transaksi *expense* dan *income* hanya dapat dicatat setelah anggaran bulan berjalan diset — transfer antar dompet tetap bebas. Layar catat transaksi menampilkan banner pengingat beserta pintasan langsung ke layar anggaran.

### 2. 🔄 Multi-Wallet Transfer, Smart Switch & Arus Kas Real-Time
* Lacak transaksi harian dengan kategori lengkap: **Pemasukan (Income)**, **Pengeluaran (Expense)**, dan **Transfer Antar Dompet**.
* **Layar & Tab Navigasi "Transfer" Khusus**: Menu transfer ditempatkan di tengah *Bottom Navigation Bar* untuk akses instan transfer antar dompet (Tunai, BCA, Mandiri, GoPay, OVO, ShopeePay, dll).
* **Smart Switch & 1-Tap Wallet Swap**: Pencegahan otomatis pemilihan dompet asal dan tujuan yang sama dengan sistem *smart switch* cerdas dan tombol *swap* instan.
* **Wallet CRUD Lengkap**: Tambah, lihat saldo, **edit** (nama & warna), dan hapus dompet. Dompet default *"Uang Tunai"* otomatis tersedia saat pertama kali digunakan; semua transaksi wajib melewati dompet.
* Kalkulasi total kekayaan bersih (**Net Worth**) otomatis dan pembaruan saldo real-time pada seluruh layar tanpa perlu refresh manual.

### 3. 🎯 Smart Savings Goals & Pockets (With Wallet Integration)
* Buat dan kelola pos target tabungan (Dana Darurat, Liburan, Gadget, Kendaraan, dll).
* **Integrasi Saldo Dompet (Deposit & Withdraw)**:
  * **Setor (Deposit)**: Memotong saldo dompet pilihan & mencatat pengeluaran alokasi tabungan.
  * **Tarik (Withdraw)**: Mengurangi saldo target & mengembalikan saldo ke dompet pilihan.
* **Smart Calculator & Color Picker**: Kalkulasi otomatis rekomendasi nominal tabungan per bulan/hari untuk mencapai target tepat waktu serta pemilih warna aksen kartu.
* Visualisasi progress bar interaktif dengan persentase dan badge status (*Tercapai 100%*, *Mendekati Deadline*, *On Track*).

### 4. 📋 Due Bills, Recurring Subscriptions & Refund System
* Catat dan pantau pengingat tagihan berkala (WiFi, Kos, Listrik, Streaming, PayLater: Shopee, Kredivo, GoPay, atau custom).
* **Dropdown Penyedia & Custom Icon**: Pilihan penyedia tagihan lengkap dengan logo resmi dan dukungan unggah ikon kustom dengan persistensi storage lokal.
* **Auto-Rollover**: Ketika tagihan ditandai **Lunas (PAID)**, sistem otomatis menjadwalkan tagihan untuk periode berikutnya (*Weekly, Monthly, Yearly*).
* **Refund Batal Bayar**: Saat status tagihan lunas dikembalikan ke **Belum Lunas**, saldo dompet yang digunakan sebelumnya otomatis dikembalikan (*refund*) dan dicatat sebagai transaksi koreksi tanpa merusak audit pengeluaran.

### 5. ⚡ Modern Quick Action Bottom Sheets & Responsive Dialogs
* Tap pada kartu Tagihan atau Target Tabungan untuk membuka **Modal Bottom Sheet Quick Action** yang lega dan informatif.
* Rincian tagihan/target lengkap dengan tombol aksi berbasis rounded surface card dan deskripsi penjelasan yang intuitif.
* **Scrollable Adaptive Dialogs**: Form input dan modal dialog yang responsif dan aman di berbagai resolusi layar.

### 6. 🌐 Multi-Language & Seamless In-App Locale Switching
* **Dukungan Dua Bahasa Penuh**: Tersedia dalam Bahasa Indonesia dan English secara komprehensif di seluruh modul (Onboarding, Dashboard, Tagihan, Transfer, Target, Anggaran Bulanan, Analitik, dan Pengaturan).
* **Clean Capsule Selector di Onboarding**: Pemilih bahasa ringkas dan elegan langsung pada Slide 1 Onboarding.
* **Zero-Blink Smooth Switching**: Konfigurasi per-app language modern berbasis `android:configChanges` yang memastikan pergantian bahasa terjadi seketika (*in-place recomposition*) tanpa kedipan hitam atau restart activity.

### 7. 👥 Smart Split Bill Calculator
* Kalkulator patungan cerdas langsung di dalam aplikasi.
* Hitung pembagian rata (*Equal Split*) lengkap dengan opsi penyesuaian pajak restoran (PB1 10%) dan service charge (5%).
* **1-Click Share to WhatsApp**: Rincian tagihan siap kirim langsung ke teman atau grup pesan singkat.

### 8. 🎨 Material 3 Expressive Design & Advanced Filtering
* **MaterialKolor Expressive Palette**: Warna antarmuka dinamis dan ekspresif dengan variasi tonal kontras tinggi.
* **Signature Asymmetric & Squircle Cards**: Kartu *Financial Runway* asimetris (`AppShapes.AsymmetricHero`), kartu dompet bergaya *Physical Debit Card*, dan kartu item berbentuk *Squircle* dengan `crispBorder (0.8.dp)`.
* **Floating Pill Navigation Bar**: Navigasi bawah melayang berbentuk kapsul (*floating pill*) dengan animasi transisi pegas (*spring physics*).
* **Advanced Bottom Sheet & In-Screen Search Filters**:
  - *All Transactions*: Single-line search bar + modal bottom sheet filter (Tipe Transaksi, Kategori, Dompet) dengan *draft state* aman.
  - *Due Bills*: Single-line search bar + segmented button status filter (`Semua`, `Belum Lunas`, `Lunas`).
  - *Savings Goals*: Single-line search bar + segmented button tab status (`Semua`, `Aktif`, `Tercapai`).

### 9. 📦 Offline JSON Backup & Restore
* Ekspor seluruh data transaksi, dompet, tagihan, dan target tabungan ke file `.json` lokal sebagai salinan cadangan.
* Impor kembali data JSON kapan saja dengan pemulihan database Room instan.

### 10. 🚀 Animated Splash Screen, Illustrated Onboarding & Interactive Tour Guide
* **Branded Splash Screen**: Transisi *fade & spring scale* yang mulus serta dukungan penuh *Android 12+ SplashScreen API*.
* **3D Vector Illustrated Onboarding**: Alur pengenalan aplikasi interaktif dengan ilustrasi *semi-3D cartoonish* yang modern dan ramah pengguna.
* **Multi-Screen Tour Guide (Coach Marks)**: Tur interaktif 7 langkah yang mengikuti alur inti aplikasi (*budget → dompet → catat transaksi → fitur pendukung*) dengan efek *spotlight* pada elemen target, tooltip berisi penjelasan, progres dots, tombol lewati/lanjut, navigasi layar otomatis antar langkah, dan dukungan dua bahasa. Berjalan otomatis sekali untuk user baru dan dapat diputar ulang dari menu Pengaturan.

### 11. 🔒 Clean Architecture & Offline-First Cloud Sync
* **Multi-module ter-enforce**: `:domain` pure Kotlin (tanpa Android/Room/Retrofit), `:data` Android library (Room/Retrofit/Firebase/WorkManager), `:presentation` Compose UI library + ViewModels (MaterialKolor, Compose Navigation, Hilt), `:app` composition root — `app → presentation → data → domain`. Guard compile-time terisolasi bersih.
* **DTO terpisah**: `data/network/dto/` (`WalletDto`, `TransactionDto`, `GoalDto`, `DueBillDto`) dengan `Gson(LOWER_CASE_WITH_UNDERSCORES)` + `@SerializedName`, `ApiService` `PATCH` konsisten backend, `RemoteDataSource` mapping `dto.toDomain()` — perubahan domain tidak pecahkan kontrak API. `ApiContractTest` MockWebServer verifikasi snake_case.
* **Outbox + WorkManager**: `Room outbox` (`PENDING/IN_FLIGHT/DONE/FAILED_RETRYABLE`) + `MIGRATION_8_9`, `withTransaction` atomik (saldo+transaksi+goal/bill), `OutboxWorker` `@HiltWorker` + `OutboxScheduler` `WorkManager` (`NetworkType.CONNECTED`, backoff `2^attempts*30s`), `idempotency_keys` Postgres (`user_id,key` PK) — retry tanpa duplikasi, guest skip.
* **UiEffect terstandar**: `UiEffect.ShowSnackbar/Navigate/PopBackStack` + `OperationState Idle/Loading/Success/Error` via `Channel(BUFFERED)` — one-shot tidak re-emit saat rotasi, `Button(enabled = !isOperationLoading)` cegah double-tap.
* **Auth & isolasi**: `TokenVerifier` (`auth/verifier.go`) verifikasi Firebase ID token (dev `dev-user-123` hanya non-prod), `ownerId` di 5 tabel + `signOut` `clearAllTables` — cegah lintas akun, `MigrationRepository` validasi `isSuccessful` sebelum `clearAllTables`.
* 100% offline via **Room SQLite**, two-way sync saat online, **Guest→Google migration** aman (validasi partial failure).

---

## 🏛️ Arsitektur & Tech Stack

```
BareBudget/
├── domain/                     # Pure Kotlin JVM — entities, repository ports, use-cases, AppTheme
│   └── src/main/java/com/ssajudn/barebudget/domain/
│       ├── model/              # Wallet, Transaction, Goal, DueBill, Budget, AppTheme, DomainModels
│       ├── repository/         # WalletRepository, TransactionRepository, GoalRepository, DueBillRepository, BudgetRepository
│       ├── usecase/            # GetDashboardSummary, GetCashflow/NetWorth, PayDueBill
│       └── error/              # AppException (typed)
├── data/                       # Android Library — Room, Retrofit, Firebase, WorkManager (→ :domain)
│   ├── src/main/java/com/ssajudn/barebudget/data/
│   │   ├── auth/               # AuthManager (Firebase), verifier
│   │   ├── datasource/local/   # LocalDataSource + withTransaction + ownerId
│   │   ├── datasource/remote/  # RemoteDataSource (DTO→domain mapping)
│   │   ├── local/room/         # Entities, Daos, AppDatabase v9 (outbox), OutboxEntity/Dao
│   │   ├── sync/               # OutboxWorker (@HiltWorker), OutboxScheduler (WorkManager)
│   │   ├── network/            # ApiClient, ApiService (DTO), dto/ (Wallet/Transaction/Goal/DueBillDto)
│   │   ├── repository/         # *RepositoryImpl (isGuestMode routing), DomainMappers, MigrationRepositoryImpl
│   │   ├── service/            # WalletBalanceService (single writer)
│   │   └── utils/              # AppConfig (BuildConfig data), DateUtils
│   └── schemas/                # Room schema 8.json, 9.json (outbox)
├── presentation/               # Android Library — Jetpack Compose UI, ViewModels, Navigation (→ :domain, :data)
│   └── src/main/java/com/ssajudn/barebudget/
│       ├── ui/
│       │   ├── analytics/      # Financial Breakdown & Category Charts
│       │   ├── bills/          # Due Bills, 1-Line Search, Segmented Filter & Refund System
│       │   ├── budget/         # Monthly Spending Target & Locked Budget UI
│       │   ├── components/     # Squircle Dialogs, Floating Pill Navigation Bar, StateViews
│       │   ├── dashboard/      # Asymmetric Financial Runway Card & Quick Actions
│       │   ├── goals/          # Savings Goals, Pockets, 1-Line Search & Smart Calculator
│       │   ├── navigation/     # AppNavigation & TopLevelDestinations
│       │   ├── onboarding/     # AuthScreen, 3D Illustrated Onboarding
│       │   ├── settings/       # Appearance (MaterialKolor Expressive), JSON Backup/Restore, Profile
│       │   ├── splash/         # Animated Branded Splash Screen
│       │   ├── theme/          # AppShapes (AsymmetricHero, Squircle, Pill), crispBorder, Theme.kt
│       │   ├── tour/           # TourScript, TourOverlay (spotlight coach marks), TourRegistry
│       │   ├── transaction/    # Add Expense, TransferScreen, AllTransactions (Search & Filter BottomSheet)
│       │   └── wallets/        # Physical Debit-Style Wallet Cards
│       └── utils/              # CurrencyFormatter, CurrencyVisualTransformation, DateUtils (UI)
├── app/                        # Android Application — Composition Root & Application Entry (→ :presentation)
│   └── src/main/java/com/ssajudn/barebudget/
│       └── BareBudgetApplication.kt # Hilt + HiltWorkerFactory (WorkManager)
└── server/                     # Go + Fiber + GORM + PostgreSQL
    ├── cmd/api/main.go         # Fiber + AuthMiddlewareWithVerifier + route PATCH
    ├── internal/
    │   ├── auth/               # TokenVerifier (Firebase ID token, dev fallback non-prod)
    │   ├── config/             # ENV, CORS, IsProduction
    │   ├── database/           # postgres.go AutoMigrate User/Wallet/Transaction/DueBill/Budget/Goal/IdempotencyKey
    │   ├── handler/            # HTTP Handlers (DTO vs GORM model terpisah)
    │   ├── middleware/         # AuthMiddlewareWithVerifier (Bearer → verified UID)
    │   ├── models/             # GORM entities + IdempotencyKey (user_id,key PK)
    │   ├── repository/         # Transactional queries (CreateTransaction, UpdateDueBillStatus, DepositToGoal)
    │   └── service/            # Business logic
    └── go.mod                  # go 1.23, Dockerfile golang:1.23-alpine
```

**Modern Stack & Enterprise Utilities:** Hilt Work (`hilt-work`), WorkManager (`work-runtime-ktx`), MockWebServer (`mockwebserver`), Gson `LOWER_CASE_WITH_UNDERSCORES`, Room `withTransaction`, `Channel<UiEffect>`.

---

## 🚀 Panduan Menjalankan Project

### 1. Menjalankan Backend Server (Go + PostgreSQL)

Pastikan Anda telah menginstal **Go (1.23+)** dan **PostgreSQL**.

```bash
# Masuk ke direktori server
cd server

# Salin konfigurasi environment
cp .env.example .env

# Jalankan server API (default port: 8080)
go run cmd/api/main.go
```

> **Tips Docker**: Anda juga dapat menjalankan database PostgreSQL menggunakan Docker:
> ```bash
> docker run --name barebudget-postgres -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -e POSTGRES_DB=barebudget -p 5432:5432 -d postgres:16-alpine
> ```

---

### 2. Menjalankan Aplikasi Android

1. Buka root folder project di **Android Studio**.
2. Pastikan file `google-services.json` Anda telah berada di direktori `app/` untuk Firebase Auth.
3. Jalankan emulator Android atau hubungkan perangkat fisik.
4. Klik **Run 'app'** (`Shift + F10`) atau compile melalui CLI:

```bash
./gradlew installDebug
# atau multi-module assemble
./gradlew :domain:build :data:assembleDebug :app:assembleDebug
```

### 3. Menjalankan Test & Verifikasi

```bash
# Unit test domain + data (termasuk ApiContractTest MockWebServer)
./gradlew :domain:test :data:testDebugUnitTest --tests "*ApiContractTest*"

# Verifikasi guard multi-module (domain tidak import Android)
./gradlew :domain:check

# Outbox manual: airplane → buat 3 transaksi → matikan airplane → cek WorkManager log "outbox_sync" sukses
```

---

## 🎨 Material 3 Design System

BareBudget menerapkan panduan desain **Material Design 3 (M3)** secara menyeluruh:
* **`M3 Dynamic Theming`**: Menggunakan palet warna tonal adaptif (*Tonal Spot / Dynamic Colors*) yang mengikuti palet Material You Android 12+.
* **`M3 Navigation Bar`**: Navigasi bottom bar resmi Material 3 dengan *pill indicator* aktif dan transisi halus.
* **`M3 Elevated & Outlined Cards`**: Pengelompokan informasi keuangan dengan hirarki elevasi permukaan yang jelas.
* **`M3 Expressive Typography & Shapes`**: Bentuk sudut membulat ekspresif (*extra large shapes*) yang ergonomis untuk perangkat mobile.

---

## 📄 Lisensi

Distributed under the **MIT License**. Lihat `LICENSE` untuk informasi lebih lanjut.

<div align="center">
  <sub>Dibangun dengan ❤️ oleh <a href="https://github.com/Udean777">Udean777</a> untuk kebebasan finansial yang lebih sehat.</sub>
</div>
