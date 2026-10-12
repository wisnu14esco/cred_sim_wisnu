package com.wisnu.creditsimulator.view;

import com.wisnu.creditsimulator.model.LoanApplication;
import com.wisnu.creditsimulator.model.LoanSimulation;
import com.wisnu.creditsimulator.model.Vehicle;
import com.wisnu.creditsimulator.model.YearlyInstallment;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SimulationFormatter {

    public List<String> format(LoanSimulation simulation) {
        List<String> lines = new ArrayList<>(summary(simulation.application()));
        lines.add("");
        lines.add("Cicilan per bulan:");
        for (YearlyInstallment installment : simulation.installments()) {
            lines.add(String.format("tahun %d : %s/bln , Suku Bunga : %s",
                    installment.year(), money(installment.monthlyInstallment()), percent(installment.interestRate())));
        }
        return lines;
    }

    public List<String> summary(LoanApplication application) {
        Vehicle vehicle = application.vehicle();
        return List.of(
                "Kendaraan       : " + vehicle.type().displayName() + " " + vehicle.condition().displayName()
                        + " (" + vehicle.year() + ")",
                "Jumlah Pinjaman : " + money(application.totalLoanAmount()),
                "Jumlah DP       : " + money(application.downPayment()),
                "Pokok Pinjaman  : " + money(application.principal()),
                "Tenor           : " + application.tenureYears() + " tahun");
    }

    /** Formats money as "Rp. 1,234,567.89". */
    public static String money(BigDecimal amount) {
        return String.format(Locale.US, "Rp. %,.2f", amount.setScale(2, RoundingMode.HALF_UP));
    }

    /** Formats a rate fraction as a percentage with a decimal comma: 0.081 -> "8,1%". */
    public static String percent(BigDecimal rate) {
        return rate.movePointRight(2).stripTrailingZeros().toPlainString().replace('.', ',') + "%";
    }
}
