package com.wisnu.creditsimulator.repository;

import com.wisnu.creditsimulator.json.JsonException;
import com.wisnu.creditsimulator.json.JsonParser;
import com.wisnu.creditsimulator.json.JsonWriter;
import com.wisnu.creditsimulator.json.LoanApplicationJson;
import com.wisnu.creditsimulator.model.LoanApplication;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Stores sheets in a single JSON file:
 *
 * { "sheets": { "mobil-baru": { "vehicleType": "Mobil", ... } } }
 *
 * The file is re-read on every operation, so several runs of the application share the same sheets.
 */
public class JsonFileSheetRepository implements SheetRepository {

    private static final String SHEETS = "sheets";

    private final Path file;

    public JsonFileSheetRepository(Path file) {
        this.file = Objects.requireNonNull(file, "file");
    }

    public Path file() {
        return file;
    }

    @Override
    public void save(String name, LoanApplication application) {
        Map<String, LoanApplication> sheets = readAll();
        sheets.put(name, application);
        writeAll(sheets);
    }

    @Override
    public Optional<LoanApplication> find(String name) {
        return Optional.ofNullable(readAll().get(name));
    }

    @Override
    public List<String> names() {
        return List.copyOf(readAll().keySet());
    }

    @Override
    public boolean delete(String name) {
        Map<String, LoanApplication> sheets = readAll();
        if (sheets.remove(name) == null) {
            return false;
        }
        writeAll(sheets);
        return true;
    }

    private Map<String, LoanApplication> readAll() {
        Map<String, LoanApplication> sheets = new LinkedHashMap<>();
        if (!Files.exists(file)) {
            return sheets;
        }
        try {
            Map<String, Object> root = JsonParser.parseObject(Files.readString(file, StandardCharsets.UTF_8));
            Object stored = root.get(SHEETS);
            if (stored == null) {
                return sheets;
            }
            if (!(stored instanceof Map)) {
                throw new JsonException("Field '" + SHEETS + "' harus berupa object");
            }
            for (Map.Entry<?, ?> entry : ((Map<?, ?>) stored).entrySet()) {
                if (!(entry.getValue() instanceof Map)) {
                    throw new JsonException("Sheet '" + entry.getKey() + "' harus berupa object");
                }
                sheets.put((String) entry.getKey(), LoanApplicationJson.fromMap((Map<?, ?>) entry.getValue()));
            }
            return sheets;
        } catch (IOException e) {
            throw new SheetStorageException("Gagal membaca file sheet " + file + ": " + e.getMessage(), e);
        } catch (JsonException | IllegalArgumentException e) {
            throw new SheetStorageException("File sheet " + file + " rusak: " + e.getMessage(), e);
        }
    }

    private void writeAll(Map<String, LoanApplication> sheets) {
        Map<String, Object> stored = new LinkedHashMap<>();
        sheets.forEach((name, application) -> stored.put(name, LoanApplicationJson.toMap(application)));
        Map<String, Object> root = new LinkedHashMap<>();
        root.put(SHEETS, stored);

        try {
            Path directory = file.toAbsolutePath().getParent();
            Files.createDirectories(directory);
            Path temp = Files.createTempFile(directory, "sheets", ".tmp");
            Files.writeString(temp, JsonWriter.write(root) + System.lineSeparator(), StandardCharsets.UTF_8);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException e) {
            throw new SheetStorageException("Gagal menyimpan file sheet " + file + ": " + e.getMessage(), e);
        }
    }
}
