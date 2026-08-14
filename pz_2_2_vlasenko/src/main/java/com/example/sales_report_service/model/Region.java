package com.example.sales_report_service.model;

/**
 * Регіон, у якому здійснено продаж.
 * displayName — назва українською для відображення у PDF-звітах.
 */

public enum Region {

    KYIV("Київ"),
    LVIV("Львів"),
    ODESA("Одеса"),
    KHARKIV("Харків"),
    DNIPRO("Дніпро");

    private final String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
