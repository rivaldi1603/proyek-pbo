# Spring v4.0 Proyek Starter

## Logs

### 04-12-2025

- Memperbarui kebutuhan paket


## Syntax

### Melakukan Instal Ulang Kebutuhan Paket

command: `mvn clean install`

#### Windows: melakukan build ulang proyek dan membuka hasil laporan
command with open jacoco: `mvn clean test; start target\site\jacoco\index.html`

#### Mac: melakukan build ulang proyek dan membuka hasil laporan
command with open jacoco: `mvn clean test && open target\site\jacoco\index.html`

#### Linux: melakukan build ulang proyek dan membuka hasil laporan
command with open jacoco: `mvn clean test && xdg-open target\site\jacoco\index.html`

### Menjalankan Aplikasi

Command: `mvn spring-boot:run`

URL: http://localhost:8080

### Menjalankan Test Covertage

pre-command: `mvn clean install`

command: `./mvnw test jacoco:report`

command-check: `./mvnw clean test jacoco:check`

## Purpose

Proyek ini dibuat untuk tujuan **Pendidikan**.


