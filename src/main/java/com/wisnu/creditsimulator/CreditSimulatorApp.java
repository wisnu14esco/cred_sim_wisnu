package com.wisnu.creditsimulator;

import com.wisnu.creditsimulator.client.HttpLoanApplicationClient;
import com.wisnu.creditsimulator.client.LoanApplicationClient;
import com.wisnu.creditsimulator.controller.CommandRegistry;
import com.wisnu.creditsimulator.controller.CreditSimulatorController;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.controller.commands.CalculateCommand;
import com.wisnu.creditsimulator.controller.commands.ExitCommand;
import com.wisnu.creditsimulator.controller.commands.LoadCommand;
import com.wisnu.creditsimulator.controller.commands.SheetCommand;
import com.wisnu.creditsimulator.controller.commands.ShowCommand;
import com.wisnu.creditsimulator.controller.commands.ViewCommand;
import com.wisnu.creditsimulator.repository.JsonFileSheetRepository;
import com.wisnu.creditsimulator.repository.SheetRepository;
import com.wisnu.creditsimulator.service.InstallmentCalculator;
import com.wisnu.creditsimulator.service.LoanSimulationService;
import com.wisnu.creditsimulator.service.LoanValidator;
import com.wisnu.creditsimulator.service.YearOnYearInterestPolicy;
import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.SimulationFormatter;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Clock;
import java.util.Map;

/**
 * Application entry point.
 *
 * credit_simulator               interactive mode
 * credit_simulator inputs.txt    reads commands and answers from a file, one per line
 *
 * Environment variables:
 *   - CREDIT_SIMULATOR_API_URL : web service used by "load"
 *   - CREDIT_SIMULATOR_SHEETS  : JSON file where sheets are stored (default: ~/.credit_simulator/sheets.json)
 */
public final class CreditSimulatorApp {

    static final String API_URL_ENV = "CREDIT_SIMULATOR_API_URL";
    static final String SHEETS_ENV = "CREDIT_SIMULATOR_SHEETS";

    private static final String USAGE = "Usage: credit_simulator [file_inputs.txt]";

    private CreditSimulatorApp() {
    }

    public static void main(String[] args) {
        System.exit(run(args, System.in, System.out, System.err, System.getenv()));
    }

    static int run(String[] args, InputStream stdin, PrintStream out, PrintStream err, Map<String, String> env) {
        if (args.length > 1) {
            err.println(USAGE);
            return 2;
        }
        if (args.length == 1 && ("-h".equals(args[0]) || "--help".equals(args[0]))) {
            out.println(USAGE);
            return 0;
        }

        boolean interactive = args.length == 0;
        try (BufferedReader reader = interactive
                ? new BufferedReader(new InputStreamReader(stdin, StandardCharsets.UTF_8))
                : Files.newBufferedReader(Path.of(args[0]), StandardCharsets.UTF_8)) {
            ConsoleView view = new ConsoleView(reader, out, interactive);
            LoanApplicationClient client = new HttpLoanApplicationClient(apiUri(env));
            SheetRepository sheets = new JsonFileSheetRepository(sheetsFile(env));
            return createController(view, client, sheets, Clock.systemDefaultZone()).run();
        } catch (NoSuchFileException e) {
            err.println("Error: file input tidak ditemukan: " + args[0]);
            return 2;
        } catch (IOException e) {
            err.println("Error: gagal membaca input: " + e.getMessage());
            return 2;
        } catch (IllegalArgumentException e) {
            err.println("Error: konfigurasi tidak valid: " + e.getMessage());
            return 2;
        }
    }

    /** Wires the MVC components together. */
    public static CreditSimulatorController createController(ConsoleView view, LoanApplicationClient client,
                                                             SheetRepository sheets, Clock clock) {
        LoanSimulationService service = new LoanSimulationService(
                new LoanValidator(clock), new InstallmentCalculator(new YearOnYearInterestPolicy()));
        SimulationFormatter formatter = new SimulationFormatter();
        SimulatorSession session = new SimulatorSession();

        CommandRegistry registry = new CommandRegistry();
        registry.register(new CalculateCommand(view, service, formatter, session))
                .register(new LoadCommand(view, client, service, formatter, session))
                .register(new ViewCommand(view, formatter, session))
                .register(new SheetCommand(view, sheets, service, formatter, session))
                .register(new ShowCommand(registry, view))
                .register(new ExitCommand(view, session));

        return new CreditSimulatorController(view, registry, session);
    }

    private static URI apiUri(Map<String, String> env) {
        String url = env.get(API_URL_ENV);
        return url == null || url.isBlank() ? HttpLoanApplicationClient.DEFAULT_URI : URI.create(url.trim());
    }

    private static Path sheetsFile(Map<String, String> env) {
        String path = env.get(SHEETS_ENV);
        if (path != null && !path.isBlank()) {
            return Path.of(path.trim());
        }
        return Path.of(System.getProperty("user.home"), ".credit_simulator", "sheets.json");
    }
}
