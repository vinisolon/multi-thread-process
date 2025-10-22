package org.solon.app.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;

@lombok.Data
@AllArgsConstructor
public class DataRequest {
    @NotNull
    private Long dataSize;
}
