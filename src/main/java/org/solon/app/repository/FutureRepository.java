package org.solon.app.repository;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solon.app.dto.Data;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class FutureRepository {

    public static void save(Data data) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação de persistência interrompida.", e);
        }
    }

    public static void saveOther(Data data) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            log.error(e.getMessage());
            Thread.currentThread().interrupt();
            throw new RuntimeException("Operação de persistência interrompida.", e);
        }
    }

}
