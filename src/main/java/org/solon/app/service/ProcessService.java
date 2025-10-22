package org.solon.app.service;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solon.app.dto.Data;

@Slf4j
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ProcessService {

//    public static void process(Data data) {
//        try {
//            data.setStatus("PROCESSING");
//            if (data.getType().equals("A")) {
//                saveWihoutCallApi(data);
//            } else if (data.getType().equals("B")) {
//                routeToSave(data);
//            } else {
//                saveAndCallApi(data);
//            }
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            handle(data);
//        }
//    }
//
//    private static void saveWihoutCallApi(Data data) {
//        data.setStatus("COMPLETED");
//        save(data);
//    }
//
//    private static void routeToSave(Data data) {
//        if (data.shouldCallApi()) {
//            callApi();
//        }
//        data.setStatus("COMPLETED");
//        save(data);
//    }
//
//    private static void saveAndCallApi(Data data) {
//        callApi();
//        data.setStatus("COMPLETED");
//        save(data);
//    }
//
//    private static void save(Data data) {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException e) {
//            log.error(e.getMessage());
//        }
//    }
//
//    private static void callApi() {
//        try {
//            Thread.sleep(3000);
//        } catch (InterruptedException e) {
//            log.error(e.getMessage());
//        }
//    }
//
//    private static void handle(Data data) {
//        data.setStatus("FAIL");
//        save(data);
//    }

}
