package org.solon.app.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.solon.app.dto.Data;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.solon.app.util.DataFactory.generateRandomData;

@Slf4j
@ExtendWith(MockitoExtension.class)
class FutureServiceTest {

    // 1. Mock do TransactionTemplate: Usado para simular o comportamento de execute()
    @Mock
    private TransactionTemplate transactionTemplate;

    // 2. Mock do PlatformTransactionManager: Necessário para que o CONSTRUTOR do serviço não quebre
    @Mock
    private PlatformTransactionManager transactionManager;

    // 3. Mock do TransactionStatus: O objeto passado para o lambda que contém a chamada status.setRollbackOnly()
    @Mock
    private TransactionStatus transactionStatus;

    // 4. O serviço será construído, mas as dependências finais serão ajustadas manualmente
    @InjectMocks
    private FutureService service;

    private static final int DATA_SIZE = 10_000;

    // Inicializa o Executor real de Virtual Threads
    // Campo para manter o executor real
    private final TaskExecutor virtualTaskExecutor = createVirtualThreadExecutor();

    // Método auxiliar para criar o Executor de Virtual Threads (Java 21+)
    private TaskExecutor createVirtualThreadExecutor() {
        return new TaskExecutorAdapter(Executors.newVirtualThreadPerTaskExecutor());
    }

    @BeforeEach
    void setup() {
        // 1. Inicializamos o serviço manualmente.
        //    - Passamos o Executor real.
        //    - Passamos o mock do PlatformTransactionManager (para que o construtor do TransactionTemplate não lance NPE).
        this.service = new FutureService(virtualTaskExecutor, this.transactionManager);

        // 2. Usamos Reflexão para garantir que o campo 'transactionTemplate' no *serviço* seja substituído pelo nosso *mock*.
        //    Isso é necessário porque o construtor do serviço cria um TransactionTemplate *real*
        //    e nós queremos usar nosso mock para interceptar chamadas.
        try {
            java.lang.reflect.Field field = FutureService.class.getDeclaredField("transactionTemplate");
            field.setAccessible(true);
            field.set(service, transactionTemplate);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao setar o TransactionTemplate mockado: " + e.getMessage());
        }

        // --- Configuração do MOCK do TransactionTemplate (ESSENCIAL) ---

        // Simula o método execute(): intercepta o call, executa o lambda, e fornece o mock de status.
        doAnswer(invocation -> {
            // Capturamos o callback (o lambda do método process())
            // Usamos @SuppressWarnings para resolver o aviso 'unchecked assignment'
            @SuppressWarnings("unchecked")
            TransactionCallback<Data> callback = (TransactionCallback<Data>) invocation.getArgument(0);

            // Executamos o callback, passando o nosso mock de TransactionStatus, resolvendo o NPE.
            return callback.doInTransaction(transactionStatus);

            // Aplicamos este comportamento ao mock do TransactionTemplate
        }).when(transactionTemplate).execute(any(TransactionCallback.class));

        // 4. Simulação de setRollbackOnly()
        // Garantimos que a chamada status.setRollbackOnly() não lance exceção
        lenient().doNothing().when(transactionStatus).setRollbackOnly();
    }

    @Test
    void test() {
        var dataset = generateRandomData(DATA_SIZE);
        assertDoesNotThrow(() -> {
            var start = System.currentTimeMillis();
            service.process_v1(dataset);
            var finish = System.currentTimeMillis();
            var processTime = (finish - start) / 1000.0;
            log.info("FUTURE -> size={}, time={}", DATA_SIZE, processTime);
        });
    }

}