package org.solon.app.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import org.solon.app.dto.DataDomain;
import org.solon.app.enums.DataStatus;

@lombok.Data
@AllArgsConstructor
@Entity
public class DataEntity {
    @Id
    @GeneratedValue
    private Long id;
    @Enumerated(EnumType.STRING)
    private DataStatus status;

    public DataEntity(DataDomain domain) {
        this.id = null;
        this.status = domain.getStatus();
    }
}
