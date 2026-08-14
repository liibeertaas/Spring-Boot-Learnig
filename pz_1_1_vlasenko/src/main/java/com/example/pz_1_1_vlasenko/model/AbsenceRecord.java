package com.example.pz_1_1_vlasenko.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDate;

@Data
@AllArgsConstructor
public class AbsenceRecord {
    private String group;
    private String fullName;
    private LocalDate date;
    private AbsenceCategory category;
}
