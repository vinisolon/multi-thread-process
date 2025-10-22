package org.solon.app.service;

import lombok.extern.slf4j.Slf4j;
import org.solon.app.client.ApiClient;
import org.solon.app.dto.Data;
import org.solon.app.repository.FutureRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class FutureService {

    private final TaskExecutor virtualTaskExecutor;
    private final TransactionTemplate transactionTemplate;

    private static final String SUCCESS = "SUCCESS";

    public FutureService(@Qualifier("virtualTaskExecutor") TaskExecutor virtualTaskExecutor,
                         PlatformTransactionManager transactionManager) {
        this.virtualTaskExecutor = virtualTaskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    public void process_v1(List<Data> dataset) {

        var futures = dataset.stream()
                .map(data -> CompletableFuture
                        // T1: Inicia o processo principal na Virtual Thread
                        .supplyAsync(() -> process(data), virtualTaskExecutor)

                        // T2: Finaliza com sucesso
                        .thenAccept(item -> log.info("DATA={} PROCESSED", item.getCode()))

                        // T3: Tratamento de erro (compensação paralela)
                        .exceptionally(ex -> {
                            Throwable originalEx = ex.getCause() != null ? ex.getCause() : ex;
                            log.error("DATA={}, FALHA={}", data.getCode(), originalEx.getMessage());
                            // Dispara o registro de falha (T3) de forma assíncrona/paralela
                            handle(data);
                            return null; // CHAVE: Recupera o Future para não interromper o allOf()
                        })
                )
                .toList();

        // Espera que todos os itens do lote terminem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private Data process(Data data) {
        return transactionTemplate.execute(status -> {
            try {
                save(data, "PROCESSING");

                throwException333(data);

                if (data.getType().equals("A")) {
                    saveOther(data, SUCCESS);
                } else if (data.getType().equals("B")) {
                    if (data.shouldCallApi()) {
                        ApiClient.call(data); // Simula a escrita da falha no DB (I/O)
                    }
                } else {
                    ApiClient.call(data); // Simula a escrita da falha no DB (I/O)
                }

                save(data, SUCCESS);

                throwException1000(data);

                return data;
            } catch (Exception e) {
                log.error(e.getMessage());
                // Se falhar (ex: na chamada da API ou no save)
                status.setRollbackOnly(); // Garante o ROLLBACK da T1
                // Propaga o erro para o .exceptionally
                throw e;
            }
        });
    }

    private void throwException1000(Data data) {
        if (data.getCode() % 1000 == 0) {
            throw new RuntimeException("FAIL 1000");
        }
    }

    private void throwException333(Data data) {
        if (data.getCode() % 333 == 0) {
            throw new RuntimeException("FAIL 333");
        }
    }

    private void save(Data data, String status) {
        data.setStatus(status);
        FutureRepository.save(data); // Simula a escrita da falha no DB (I/O)
    }

    private void saveOther(Data data, String status) {
        data.setStatus(status);
        FutureRepository.saveOther(data); // Simula a escrita da falha no DB (I/O)
    }

    private void handle(Data data) {
        CompletableFuture.runAsync(() -> {
            // Transação T3: Nova transação isolada para registrar o FAIL
            transactionTemplate.executeWithoutResult(status -> {
                log.error("DATA={} FAIL", data.getType());
                saveOther(data, "FAIL"); // Simula a escrita da falha no DB (I/O)
            });
        }, virtualTaskExecutor);
    }

}
