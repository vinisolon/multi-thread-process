package org.solon.app.util;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.solon.app.dto.Data;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class DataFactory {

    private static final String[] TYPES = {"A", "B", "C"};
    private static final Random RANDOM = new Random();

    public static List<Data> generateRandomData(int size) {
        return IntStream.rangeClosed(1, size)
                .mapToObj(code -> {
                    String randomType = TYPES[RANDOM.nextInt(TYPES.length)];
                    boolean randomCallApi = RANDOM.nextBoolean();
                    return new Data(code, randomType, "TO_PROCESS", randomCallApi);
                })
                .toList();
    }
}
