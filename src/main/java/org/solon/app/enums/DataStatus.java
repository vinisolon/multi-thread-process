package org.solon.app.enums;

import lombok.ToString;

@ToString
public enum DataStatus {
    PENDING,
    PROCESSING,
    PROCESSED,
    FAILED
}
