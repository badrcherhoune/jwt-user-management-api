package com.challenge.users.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BatchImportResult {
    private int total;
    private int imported;
    private int skipped;
}
