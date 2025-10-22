package org.solon.app.dto;

import lombok.AllArgsConstructor;

@lombok.Data
@AllArgsConstructor
public class Data {

    private int code;
    private String type;
    private String status;
    private Boolean callApi;

    public boolean shouldCallApi() {
        return this.getCallApi();
    }

}
