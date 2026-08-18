<div align="center">
  <img src="https://raw.githubusercontent.com/tandpfun/skill-icons/main/icons/AndroidLight.svg" width="80" height="80" alt="BareBudget Logo" />
  
  # 💸 BareBudget
  
  **Frictionless Personal Finance & Survival Runway Tracker**
  
  *Kelola keuangan bulanan, hitung tanggal bertahan hidup finansialmu (Runway/Death Day), catat pengeluaran tanpa ribet dengan OCR, dan capai target tabungan.*

  ---

  [![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?style=for-the-badge&logo=kotlin&logoColor=white)](https://kotlinlang.org/)
  [![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/jetpack/compose)
  [![Go Fiber](https://img.shields.io/badge/Backend-Go%20Fiber-00ADD8?style=for-the-badge&logo=go&logoColor=white)](https://gofiber.io/)
  [![PostgreSQL](https://img.shields.io/badge/Database-PostgreSQL-336791?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
  [![Room DB](https://img.shields.io/badge/Local%20Cache-Room%20SQLite-3DDC84?style=for-the-badge&logo=sqlite&logoColor=white)](https://developer.android.com/training/data-storage/room)
  [![Firebase Auth](https://img.shields.io/badge/Auth-Firebase%20%26%20Google-FFCA28?style=for-the-badge&logo=firebase&logoColor=black)](https://firebase.google.com/)

</div>

---

## 📖 Tentang BareBudget

**BareBudget** adalah aplikasi finansial minimalis, cepat, dan modern yang dirancang untuk menjawab pertanyaan paling krusial setiap orang di tengah bulan:
> *"Dengan gaya hidup belanja saat ini, sampai tanggal berapa sisa uangku bisa bertahan hidup?"*

Dengan konsep **Offline-First**, BareBudget dapat digunakan langsung tanpa login (**Guest Mode**) dan secara cerdas dapat dimigrasikan ke **Google Cloud Account** kapan saja tanpa kehilangan data historis.

---

## ✨ Fitur Unggulan

### 1. 🧮 Financial Runway & "Estimated Death Day"
* Menghitung kecepatan pengeluaran harian (*burn rate*).
* Memprediksi tanggal persis kapan saldo budget Anda akan habis di bulan berjalan.
* Memberikan pesan status real-time (*Aman*, *Waspada*, atau *Kritis*).

### 2. 🌊 Floating Bottom Navigation Bar & Modern UI
* Navigasi melayang (*island floating bar*) modern dengan animasi *spring* yang responsif.
* Tombol tengah mencolok (**Quick Log**) untuk pencatatan transaksi dalam hitungan detik.
* Desain warna pastel lembut (*Mint, Coral, Lavender, Ochre*) yang ramah di mata untuk mode terang maupun gelap.

### 3. 🎯 Savings Goals & Pockets (Sinking Funds)
* Buat target tabungan (Dana Darurat, Liburan, Beli Gadget, dll).
* Progress bar visual interaktif lengkap dengan persentase dan sisa dana.
* Dialog khusus untuk **Setor Tabungan (Deposit)** atau **Tarik Dana (Withdraw)** secara fleksibel.

### 4. 🔁 Due Bills & Recurring Subscriptions
* Catat tagihan berkala (WiFi, Kos, Netflix, Listrik, PayLater).
* **Auto-Rollover**: Ketika tagihan berulang ditandai **PAID/Lunas**, sistem otomatis menjadwalkan tagihan untuk periode berikutnya (*Weekly, Monthly, Yearly*).

### 5. 👥 Smart Split Bill Calculator
* Kalkulator patungan cerdas di layar transaksi.
* Hitung bagi rata (*Equal Split*) lengkap dengan kalkulasi persentase pajak PB1 (10%) & service fee (5%).
* **1-Click Share to WhatsApp**: Bagikan rincian patungan siap kirim langsung ke teman atau grup WhatsApp.

### 6. 📷 ML Kit Receipt OCR (Snap Ledger)
* Pindai struk belanja fisik langsung dengan kamera Android.
* Deteksi otomatis nama merchant, total nominal, dan saran kategori belanja.

### 7. 🔒 Offline-First Architecture & Smart Sync
* Bekerja 100% secara offline menggunakan **Room SQLite Database**.
* Otomatis melakukan sinkronisasi dua arah (*Two-Way Sync*) dengan backend REST API Go saat perangkat terhubung ke internet.
* **Guest to Google Migration**: Mulai instan sebagai *Guest*, migrasikan seluruh data lokal ke Google Account saat login.

---

## 🏛️ Arsitektur & Tech Stack

```
BareBudget/
├── app/                        # Android Native Client (Kotlin + Jetpack Compose)
│   ├── src/main/java/com/ssajudn/barebudget/
│   │   ├── data/
│   │   │   ├── auth/           # Firebase Authentication & Credential Manager
│   │   │   ├── local/          # Room DB (Entities, DAOs) & UserSessionManager
│   │   │   ├── model/          # DTOs & Domain Models
│   │   │   ├── network/        # Retrofit Client & ApiService
│   │   │   └── repository/     # Offline-First BudgetRepository
│   │   ├── ui/
│   │   │   ├── analytics/      # Financial Breakdown & Category Charts
│   │   │   ├── bills/          # Due Bills & Recurring Subscriptions
│   │   │   ├── budget/         # Monthly Spending Target Setup
│   │   │   ├── components/     # CustomDialog, CustomToastHost, FloatingNavBar
│   │   │   ├── dashboard/      # Financial Runway Card & Recent Feeds
│   │   │   ├── goals/          # Savings Goals & Pockets
│   │   │   ├── navigation/     # AppNavigation & Screen Routes
│   │   │   ├── onboarding/     # Onboarding & Auth Screens
│   │   │   ├── theme/          # Custom Pastel Palette & Typography
│   │   │   └── transaction/    # Add Expense & Split Bill BottomSheet
│   │   └── utils/              # CurrencyFormatter, DateUtils, ReceiptParser
│   └── build.gradle.kts
│
└── server/                     # High-Performance REST API (Go + Fiber)
    ├── cmd/api/main.go         # Server entrypoint & Route Handlers
    ├── internal/
    │   ├── config/             # Environment configurations
    │   ├── database/           # PostgreSQL connection & GORM AutoMigrate
    │   ├── handler/            # HTTP Request Handlers
    │   ├── middleware/         # Auth & Header Middlewares
    │   ├── models/             # Database Models & Entities
    │   ├── repository/         # Database Queries & GORM Operations
    │   └── service/            # Business Logic & Runway Calculations
    └── go.mod
```

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
```

---

## 🎨 Shared Design System

BareBudget menggunakan komponen kustom modern tanpa bergantung penuh pada default theme Material:
* **`CustomDialog`**: Dialog kartu elegan dengan border halus, header badge, dan tombol pill kustom.
* **`CustomConfirmDeleteDialog`**: Modal konfirmasi tindakan destruktif dengan warna aksen *Pastel Coral*.
* **`CustomToastHost`**: Notifikasi in-app melayang (*Top Floating Banner*) dengan transisi halus.
* **`FloatingBottomNavBar`**: Navigasi pulau melayang dengan elevation shadow lembut.

---

## 📄 Lisensi

Distributed under the **MIT License**. Lihat `LICENSE` untuk informasi lebih lanjut.

<div align="center">
  <sub>Dibangun dengan ❤️ oleh <a href="https://github.com/ssajudn">ssajudn</a> untuk kebebasan finansial yang lebih sehat.</sub>
</div>
