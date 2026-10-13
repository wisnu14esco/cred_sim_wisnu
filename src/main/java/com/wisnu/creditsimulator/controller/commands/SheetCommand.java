package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.CommandException;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.repository.SheetRepository;
import com.wisnu.creditsimulator.service.LoanSimulationService;
import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.SimulationFormatter;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

/**
 * sheet: saves the current calculation under a name and switches between saved sheets.
 */
public class SheetCommand implements Command {

    private static final Pattern SHEET_NAME = Pattern.compile("[A-Za-z0-9_.-]{1,40}");

    private final ConsoleView view;
    private final SheetRepository repository;
    private final LoanSimulationService service;
    private final SimulationFormatter formatter;
    private final SimulatorSession session;

    public SheetCommand(ConsoleView view, SheetRepository repository, LoanSimulationService service,
                        SimulationFormatter formatter, SimulatorSession session) {
        this.view = view;
        this.repository = repository;
        this.service = service;
        this.formatter = formatter;
        this.session = session;
    }

    @Override
    public String name() {
        return "sheet";
    }

    @Override
    public List<Usage> usages() {
        return List.of(
                new Usage("sheet save [nama]", "Simpan perhitungan saat ini ke sheet (default: sheet aktif)"),
                new Usage("sheet switch <nama>", "Pindah ke sheet lain dan tampilkan hasilnya"),
                new Usage("sheet list", "Tampilkan semua sheet yang tersimpan"),
                new Usage("sheet delete <nama>", "Hapus sheet"));
    }

    @Override
    public void execute(List<String> args) {
        if (args.isEmpty()) {
            throw new CommandException("Gunakan: sheet save|switch|list|delete [nama]");
        }
        String action = args.get(0).toLowerCase(Locale.ROOT);
        List<String> rest = args.subList(1, args.size());
        switch (action) {
            case "save":
                save(rest.isEmpty() ? session.activeSheet() : sheetName(rest));
                break;
            case "switch":
                switchTo(sheetName(rest));
                break;
            case "list":
                list();
                break;
            case "delete":
                delete(sheetName(rest));
                break;
            default:
                throw new CommandException("Sub-perintah sheet '" + action + "' tidak dikenal. "
                        + "Gunakan: save, switch, list, delete");
        }
    }

    private void save(String name) {
        LoanSimulation simulation = session.currentSimulation()
                .orElseThrow(() -> new CommandException("Belum ada perhitungan untuk disimpan. "
                        + "Jalankan 'calc' atau 'load' dulu."));
        repository.save(name, simulation.application());
        session.setActiveSheet(name);
        view.println("Perhitungan disimpan ke sheet '" + name + "'.");
    }

    private void switchTo(String name) {
        LoanApplication application = repository.find(name)
                .orElseThrow(() -> new CommandException("Sheet '" + name + "' tidak ditemukan. "
                        + "Lihat daftar dengan 'sheet list'."));
        LoanSimulation simulation = service.recalculate(application);
        session.setCurrentSimulation(simulation);
        session.setActiveSheet(name);

        view.println("Pindah ke sheet '" + name + "'.");
        view.println();
        view.printLines(formatter.format(simulation));
    }

    private void list() {
        List<String> names = repository.names();
        if (names.isEmpty()) {
            view.println("Belum ada sheet tersimpan. Simpan dengan 'sheet save <nama>'.");
            return;
        }
        view.println("Sheet tersimpan (* = aktif):");
        for (String name : names) {
            view.println((name.equals(session.activeSheet()) ? "  * " : "    ") + name);
        }
    }

    private void delete(String name) {
        if (!repository.delete(name)) {
            throw new CommandException("Sheet '" + name + "' tidak ditemukan.");
        }
        view.println("Sheet '" + name + "' dihapus.");
    }

    private static String sheetName(List<String> args) {
        if (args.size() != 1) {
            throw new CommandException("Masukkan satu nama sheet, contoh: sheet switch mobil-baru");
        }
        String name = args.get(0);
        if (!SHEET_NAME.matcher(name).matches()) {
            throw new CommandException("Nama sheet hanya boleh huruf, angka, '-', '_' atau '.' (maks 40 karakter)");
        }
        return name;
    }
}
