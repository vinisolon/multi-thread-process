package org.solon.app.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.solon.app.dto.DataDomain;
import org.solon.app.enums.DataStatus;

import java.util.List;
import java.util.Random;
import java.util.stream.LongStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataFactory {

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
