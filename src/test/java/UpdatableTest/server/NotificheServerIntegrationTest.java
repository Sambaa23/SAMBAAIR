package UpdatableTest.server;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import main.proto.*;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.NotificheServer;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NotificheServerIntegrationTest {

    private Server server;
    private ManagedChannel channel;
    private NotificheServiceGrpc.NotificheServiceBlockingStub blockingStub;

    @BeforeEach
    public void setUp() throws IOException {
        // Avvia il server gRPC
        NotificheServer notificheServer = new NotificheServer(50051);
        server = ServerBuilder.forPort(50051)
                .addService(notificheServer.new NotificheServiceImpl())
                .build()
                .start();

        // Configura il canale e lo stub del client
        channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();
        blockingStub = NotificheServiceGrpc.newBlockingStub(channel);
    }

    @AfterEach
    public void tearDown() throws InterruptedException {
        // Arresta il server e chiudi il canale
        if (server != null) {
            server.shutdown().awaitTermination();
        }
        if (channel != null) {
            channel.shutdown();
        }
    }

    @Test
    public void testRegisterObserver() {
        // Prepara la richiesta
        RegisterObserverRequest request = RegisterObserverRequest.newBuilder()
                .setClientId("cliente@example.com")
                .setObserverAddress("localhost")
                .build();

        // Invoca il metodo gRPC
        RegisterObserverResponse response = blockingStub.registerObserver(request);

        // Verifica la risposta
        assertEquals("Observer registered", response.getStatus());
    }

    @Test
    public void testRiceviNotificaPromozione() {
        // Prepara la richiesta
        NotificaPromozioneRequest request = NotificaPromozioneRequest.newBuilder()
                .setClientId("cliente@example.com")
                .setMessage("Test promozione")
                .build();

        // Invoca il metodo gRPC
        NotificaPromozioneResponse response = blockingStub.riceviNotificaPromozione(request);

        // Verifica la risposta
        assertEquals("Notifica di promozione inviata", response.getStatus());
    }

    @Test
    public void testNotificaInattivitaCliente() {
        // Prepara la richiesta
        NotificaInattivitaRequest request = NotificaInattivitaRequest.newBuilder()
                .setClientId("cliente@example.com")
                .setMessage("Test inattività")
                .build();

        // Invoca il metodo gRPC
        NotificaInattivitaResponse response = blockingStub.notificaInattivitaCliente(request);

        // Verifica la risposta
        assertEquals("Cliente attivo, nessuna notifica inviata", response.getStatus());
    }

    @Test
    public void testNotificaScadenzaPrenotazione() {
        // Prepara la richiesta
        String scadenzaStr = LocalDateTime.now().plusDays(4).toString();
        NotificaScadenzaRequest request = NotificaScadenzaRequest.newBuilder()
                .setClientId("cliente@example.com")
                .setScadenza(scadenzaStr)
                .build();

        // Invoca il metodo gRPC
        NotificaScadenzaResponse response = blockingStub.notificaScadenzaPrenotazione(request);

        // Verifica la risposta
        assertEquals("Notifica di scadenza inviata", response.getStatus());
    }
}

