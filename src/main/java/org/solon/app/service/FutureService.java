package org.solon.app.service;

import lombok.extern.slf4j.Slf4j;
import org.solon.app.client.ApiClient;
import org.solon.app.dto.DataDomain;
import org.solon.app.entity.DataEntity;
import org.solon.app.enums.DataStatus;
import org.solon.app.repository.DataRepository;
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
    private final DataRepository repository;

    public FutureService(@Qualifier("virtualTaskExecutor") TaskExecutor virtualTaskExecutor,
                         PlatformTransactionManager transactionManager,
                         DataRepository repository) {
        this.virtualTaskExecutor = virtualTaskExecutor;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.repository = repository;
    }

    public void process_v1(List<DataDomain> dataset) {

        var futures = dataset.stream()
                .map(data -> CompletableFuture
                        // T1: Inicia o processo principal na Virtual Thread
                        .supplyAsync(() -> process(data), virtualTaskExecutor)

                        // T2: Finaliza com sucesso
                        .thenAccept(item -> log.info("PROCESSED={}", item.getCode()))

                        // T3: Tratamento de erro (compensação paralela)
                        .exceptionally(e -> {
                            log.error("FAILED={} REASON={}", data.getCode(), e.getMessage());
                            // Dispara o registro de falha (T3) de forma assíncrona/paralela
                            handle(data);
                            return null; // CHAVE: Recupera o Future para não interromper o allOf()
                        })
                )
                .toList();

        // Espera que todos os itens do lote terminem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
    }

    private DataDomain process(DataDomain data) {
        return transactionTemplate.execute(status -> {
            try {
                save(data, DataStatus.PROCESSING);

                throwException333(data);

                if (data.getType().equals("A")) {
                    save(data, DataStatus.PROCESSED); // Simula escrita no DB (I/O)
                } else if (data.getType().equals("B")) {
                    if (data.shouldCallApi()) {
                        ApiClient.call(data); // Simula chamada client externo (I/O)
                    }
                    save(data, DataStatus.PROCESSED);
                } else {
                    ApiClient.call(data); // Simula chamada client (I/O)
                    save(data, DataStatus.PROCESSED); // Simula escrita no DB (I/O)
                }

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

    private void save(DataDomain data, DataStatus status) {
        data.setStatus(status);
        repository.save(new DataEntity(data));
    }

    private void throwException1000(DataDomain data) {
        if (data.getCode() % 1000 == 0) {
            throw new RuntimeException("FAIL 1000");
        }
    }

    private void throwException333(DataDomain data) {
        if (data.getCode() % 333 == 0) {
            throw new RuntimeException("FAIL 333");
        }
    }

    private void handle(DataDomain data) {
        CompletableFuture.runAsync(() ->
                // Transação T3: Nova transação isolada para registrar o FAIL
                transactionTemplate.executeWithoutResult(status ->
                        save(data, DataStatus.FAILED)), virtualTaskExecutor);
    }

}
