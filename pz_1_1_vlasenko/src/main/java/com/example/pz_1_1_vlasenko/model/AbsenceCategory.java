package com.example.pz_1_1_vlasenko.model;

import lombok.Getter;
import java.util.Arrays;

@Getter
public enum AbsenceCategory {
    SICK("хворий", "х", "хв"),
    MEDICAL("санчастина", "сч"),
    BUSINESS_TRIP("відрядження", "вд"),
    VACATION("відпустка", "вп"),
    PRESENT("присутній", ""); // Порожня клітинка

    private final String title;
    private final String[] codes;

    AbsenceCategory(String title, String... codes) {
        this.title = title;
        this.codes = codes;
    }

    public static AbsenceCategory parse(String cellValue) {
        if (cellValue == null || cellValue.trim().isEmpty()) {
            return PRESENT;
        }
        String normalized = cellValue.trim().toLowerCase();

        return Arrays.stream(values())
                .filter(cat -> Arrays.asList(cat.codes).contains(normalized) || cat.title.equals(normalized))
                .findFirst()
                .orElse(PRESENT);
    }
}
