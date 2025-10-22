package org.solon.app.dto;

import lombok.AllArgsConstructor;
import org.solon.app.enums.DataStatus;

@lombok.Data
@AllArgsConstructor
public class DataDomain {

    private Long id;
    private Long code;
    private String type;
    private DataStatus status;
    private Boolean callApi;

    public boolean shouldCallApi() {
        return this.getCallApi();
    }

}
