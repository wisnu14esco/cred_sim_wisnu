package com.wisnu.creditsimulator.controller.commands;

import com.wisnu.creditsimulator.controller.Command;
import com.wisnu.creditsimulator.controller.SimulatorSession;
import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.model.VehicleCondition;
import com.wisnu.creditsimulator.model.VehicleFactory;
import com.wisnu.creditsimulator.model.VehicleType;
import com.wisnu.creditsimulator.service.LoanSimulationService;
import com.wisnu.creditsimulator.service.LoanValidator;
import com.wisnu.creditsimulator.view.ConsoleView;
import com.wisnu.creditsimulator.view.SimulationFormatter;
import java.math.BigDecimal;
import java.util.List;

/**
 * calc: asks for the vehicle and loan data step by step, then shows the monthly installments.
 */
public class CalculateCommand implements Command {

    private final ConsoleView view;
    private final LoanSimulationService service;
    private final SimulationFormatter formatter;
    private final SimulatorSession session;

    public CalculateCommand(ConsoleView view, LoanSimulationService service, SimulationFormatter formatter,
                            SimulatorSession session) {
        this.view = view;
        this.service = service;
        this.formatter = formatter;
        this.session = session;
    }

    @Override
    public String name() {
        return "calc";
    }

    @Override
    public List<Usage> usages() {
        return List.of(new Usage("calc", "Hitung cicilan baru (input data kendaraan dan pinjaman)"));
    }

    @Override
    public void execute(List<String> args) {
        LoanValidator validator = service.validator();

        VehicleType type = view.ask("Jenis kendaraan (Motor|Mobil)", validator::parseVehicleType);
        VehicleCondition condition = view.ask("Kondisi kendaraan (Baru|Bekas)", validator::parseVehicleCondition);
        int year = view.ask("Tahun kendaraan (4 digit)", raw -> validator.parseVehicleYear(raw, condition));
        BigDecimal loanAmount = view.ask("Jumlah pinjaman total (maks 1.000.000.000)", validator::parseLoanAmount);
        int tenure = view.ask("Tenor pinjaman (1-6 tahun)", validator::parseTenure);
        BigDecimal downPayment = view.ask("Jumlah DP (min " + percent(condition) + "% dari pinjaman)",
                raw -> validator.parseDownPayment(raw, loanAmount, condition));

        LoanApplication application = new LoanApplication(
                VehicleFactory.create(type, condition, year), loanAmount, tenure, downPayment);
        LoanSimulation simulation = service.simulate(application);
        session.setCurrentSimulation(simulation);

        view.println();
        view.printLines(formatter.format(simulation));
    }

    private static String percent(VehicleCondition condition) {
        return condition.minimumDownPaymentRatio().movePointRight(2).stripTrailingZeros().toPlainString();
    }
}
