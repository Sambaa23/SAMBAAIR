package client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import main.proto.*;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NotificheClient {
    private final ManagedChannel channel;
    private final NotificheServiceGrpc.NotificheServiceStub stub;

    public NotificheClient(String host, int port) {
        this(ManagedChannelBuilder.forAddress(host, port).usePlaintext());
    }

    public NotificheClient(ManagedChannelBuilder<?> channelBuilder) {
        channel = channelBuilder.build();
        stub = NotificheServiceGrpc.newStub(channel);
    }

    public void riceviNotifichePromozione(String clientId, String message) {
        StreamObserver<NotificaPromozioneResponse> responseObserver = new StreamObserver<NotificaPromozioneResponse>() {
            @Override
            public void onNext(NotificaPromozioneResponse value) {
                System.out.println("Notifica di promozione ricevuta: " + value.getStatus());

                if (value.getStatus().equals("SUCCESSO")) {
                    System.out.println("Notifica consegnata correttamente al cliente: " + clientId);
                }
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Errore nella ricezione della notifica di promozione per il cliente " + clientId + ": " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Ricezione della notifica di promozione completata per il cliente: " + clientId);
            }
        };


        try {
            stub.riceviNotificaPromozione(NotificaPromozioneRequest.newBuilder()
                    .setClientId(clientId)
                    .setMessage(message)
                    .build(), responseObserver);
        } catch (Exception e) {
            System.err.println("Errore durante l'invio della richiesta di notifica: " + e.getMessage());
        }
    }


    public void riceviNotificheInattivita(String clientId, String message) {
        StreamObserver<NotificaInattivitaResponse> responseObserver = new StreamObserver<NotificaInattivitaResponse>() {
            @Override
            public void onNext(NotificaInattivitaResponse value) {
                SwingUtilities.invokeLater(() -> {
                    System.out.println("Notifica di inattività ricevuta: " + value.getStatus());
                });
            }

            @Override
            public void onError(Throwable t) {
                System.err.println("Errore nella ricezione della notifica di inattività: " + t.getMessage());
            }

            @Override
            public void onCompleted() {
                System.out.println("Ricezione della notifica di inattività completata");
            }
        };

        stub.notificaInattivitaCliente(NotificaInattivitaRequest.newBuilder()
                .setClientId(clientId)
                .setMessage(message)
                .build(), responseObserver);
    }


    public void riceviNotificheScadenzaPrenotazione(String clientmail, ConcurrentLinkedQueue<String> notifiche) {
        List<Pattern> patterns = Arrays.asList(
                Pattern.compile("\\d{4}-\\d{2}-\\d{2}T\\d{2}:\\d{2}"),
                Pattern.compile("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}"),
                Pattern.compile("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}")
        );

        for (String notifica : notifiche) {
            boolean dataTrovata = false;

            for (Pattern pattern : patterns) {
                Matcher matcher = pattern.matcher(notifica);
                if (matcher.find()) {
                    dataTrovata = true;
                    String dataDiScadenza = matcher.group();
                    if (pattern.pattern().equals("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}")) {
                        dataDiScadenza = dataDiScadenza.replace(" ", "T") + ":00";
                    } else if (pattern.pattern().equals("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}")) {
                        dataDiScadenza = dataDiScadenza.replace(" ", "T") + ":00";
                    } else {
                        dataDiScadenza += ":00";
                    }

                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                            pattern.pattern().equals("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}") ?
                                    "dd/MM/yyyy'T'HH:mm:ss" : pattern.pattern().equals("\\d{2}-\\d{2}-\\d{4} \\d{2}:\\d{2}") ?
                                    "dd-MM-yyyy'T'HH:mm:ss" : "yyyy-MM-dd'T'HH:mm:ss"
                    );

                    try {
                        LocalDateTime scadenza = LocalDateTime.parse(dataDiScadenza, formatter);

                        NotificaScadenzaRequest request = NotificaScadenzaRequest.newBuilder()
                                .setClientId(clientmail)
                                .setScadenza(scadenza.toString())
                                .build();

                        // Chiamata gRPC
                        stub.notificaScadenzaPrenotazione(request, new StreamObserver<NotificaScadenzaResponse>() {
                            @Override
                            public void onNext(NotificaScadenzaResponse value) {
                                System.out.println("Notifica di scadenza della prenotazione ricevuta: " + value.getStatus());
                            }

                            @Override
                            public void onError(Throwable t) {
                                System.err.println("Errore nella ricezione della notifica di scadenza della prenotazione: " + t.getMessage());
                            }

                            @Override
                            public void onCompleted() {
                                System.out.println("Ricezione della notifica di scadenza della prenotazione completata");
                            }
                        });
                    } catch (DateTimeParseException e) {
                        System.err.println("Formato della data di scadenza non valido: " + notifica);
                    }
                    break;
                }
            }
        }
    }

    public void shutdown() throws InterruptedException {
        if (channel != null) {
            channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}





