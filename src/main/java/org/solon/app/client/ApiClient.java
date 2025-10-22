package org.solon.app.client;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solon.app.dto.DataDomain;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiClient {

    public static void call(DataDomain data) {
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Chamada de API interrompida.", e);
        }
    }

}
