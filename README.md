# credit_simulator

Console application that calculates monthly installments for a vehicle loan (Mobil/Motor).

**Language:** Java 17, built with Maven. No runtime libraries; JUnit 5 is used for tests only.

## Requirements

- Java 17+
- Maven 3.8+ (the launcher builds the jar automatically on first run)

## How to run

```bash
./credit_simulator                    # interactive mode
./credit_simulator file_inputs.txt    # read commands from a file
```

`bin/credit_simulator` works the same way.

## Step by step

1. Run `./credit_simulator`.
2. Type `show` to list all commands.
3. Type `calc` and answer the questions:
   - Jenis kendaraan: `Mobil` or `Motor` (case ignored)
   - Kondisi kendaraan: `Baru` or `Bekas` (case ignored)
   - Tahun kendaraan: 4 digits; `Baru` cannot be older than last year
   - Jumlah pinjaman: up to 1,000,000,000
   - Tenor: 1–6 years
   - Jumlah DP: at least 35% (Baru) or 25% (Bekas) of the loan
4. The monthly installment is shown per year:
   ```
   tahun 1 : Rp. 2,250,000.00/bln , Suku Bunga : 8%
   tahun 2 : Rp. 2,432,250.00/bln , Suku Bunga : 8,1%
   tahun 3 : Rp. 2,641,423.50/bln , Suku Bunga : 8,6%
   ```
5. Type `sheet save <nama>` to save it, and `sheet switch <nama>` to go back to it later.
6. Type `load` to load an existing calculation from the web service.
7. Type `exit` to quit.

## Commands

| Command               | Description                                    |
|-----------------------|------------------------------------------------|
| `calc`                | Calculate a new loan                           |
| `load`                | Load a calculation from the web service        |
| `view`                | Show the current calculation again             |
| `sheet save [nama]`   | Save the current calculation as a sheet        |
| `sheet switch <nama>` | Switch to a saved sheet                        |
| `sheet list`          | List saved sheets                              |
| `sheet delete <nama>` | Delete a sheet                                 |
| `show`                | Show all commands                              |
| `exit`                | Quit                                           |

Sheets are saved in `~/.credit_simulator/sheets.json`.

## Input file

One command or answer per line, the same as typing interactively. Lines starting with `#` are
ignored. Example ([file_inputs.txt](file_inputs.txt)):

```text
calc
Mobil
Bekas
2020
100000000
3
25000000
exit
```

## Calculation

Based on [docs/Rumus.xlsx](docs/Rumus.xlsx):

- Principal = loan amount − down payment
- Each year: interest is added to the remaining principal, then divided by the remaining months
- Base rate: Mobil 8%, Motor 9%; it rises +0.1% and +0.5% in alternating years (8% → 8.1% → 8.6% → …)

## Design

MVC with Factory and Command patterns (`src/main/java/com/wisnu/creditsimulator/`):

- `model/`: `Vehicle` → `Car`, `Motorcycle`, created by `VehicleFactory`
- `service/`: interest policy, installment calculator, validator
- `controller/`: main loop and one `Command` class per menu entry
- `view/`: console input/output and result formatting
- `client/`: web service client (JDK `HttpClient`)
- `repository/`: sheet storage
- `json/`: small JSON parser/writer

## Tests

```bash
mvn test
```

## Docker

```bash
docker build -t credit_simulator .
docker run --rm -it credit_simulator
docker run --rm -v "$PWD/file_inputs.txt:/input/file_inputs.txt:ro" credit_simulator /input/file_inputs.txt
```

## CI/CD

GitHub Actions ([.github/workflows/ci.yml](.github/workflows/ci.yml)) builds and tests every push,
then publishes the Docker image to Docker Hub. It needs the repository secrets
`DOCKERHUB_USERNAME` and `DOCKERHUB_TOKEN`.
