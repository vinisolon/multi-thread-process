package org.solon.app.rest;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.solon.app.dto.DataDomain;
import org.solon.app.dto.DataRequest;
import org.solon.app.enums.DataStatus;
import org.solon.app.service.FutureService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProcessController {
    private final FutureService service;

    @PostMapping("/process")
    public void process(@Valid @RequestBody DataRequest request) {
        var dataset = generateRandomData(request.getDataSize());
        var start = System.currentTimeMillis();

        service.process_v1(dataset);

        var finish = System.currentTimeMillis();
        var processTime = (finish - start) / 1000.0;
        log.info("FUTURE -> size={}, time={}", request.getDataSize(), processTime);
    }

    private static final String[] TYPES = {"A", "B", "C"};
    private static final Random RANDOM = new Random();

    public static List<DataDomain> generateRandomData(long size) {
        return LongStream.rangeClosed(1, size)
                .mapToObj(code -> {
                    String randomType = TYPES[RANDOM.nextInt(TYPES.length)];
                    boolean randomCallApi = RANDOM.nextBoolean();
                    return new DataDomain(null, code, randomType, DataStatus.PENDING, randomCallApi);
                })
                .toList();
    }

}
