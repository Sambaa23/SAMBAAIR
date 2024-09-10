package server;

import Updatable.conf.DatabaseConnection;
import com.google.protobuf.Empty;
import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.stub.StreamObserver;
import main.proto.*;

import java.io.IOException;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeParseException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.*;

public class NotificheServer {
    private final int port;
    private final Map<String, LocalDate> ultimoAcquistoCliente;
    private final Map<String, LocalDateTime> prenotazioniCliente;
    private final Map<String, String> observerAddresses;
    private final ExecutorService executorService;
    private final ScheduledExecutorService scheduler;
    private final Set<String> clientiRegistrati = ConcurrentHashMap.newKeySet();
    private boolean notificheAvviate = false;
    private final ConcurrentMap<String, ConcurrentLinkedQueue<String>> notifichePerCliente = new ConcurrentHashMap<>();

    public NotificheServer(int port) {
        this.port = port;
        this.ultimoAcquistoCliente = new ConcurrentHashMap<>();
        this.prenotazioniCliente = new ConcurrentHashMap<>();
        this.observerAddresses = new ConcurrentHashMap<>();
        this.executorService = Executors.newCachedThreadPool();
        this.scheduler = Executors.newScheduledThreadPool(1);
    }

    public void start() throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(port)
                .addService(new NotificheServiceImpl())
                .build()
                .start();
        System.out.println("Server started, listening on port " + port);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.err.println("Shutting down gRPC server since JVM is shutting down");
            NotificheServer.this.stop();
            System.err.println("Server shut down");
        }));
        blockUntilShutdown(server);
    }

    public synchronized void registraCliente(String email, boolean fedelta) {
        notifichePerCliente.putIfAbsent(email, new ConcurrentLinkedQueue<>());
        clientiRegistrati.add(email);
        if (!notificheAvviate) {
            startScheduledTasks(fedelta);
            notificheAvviate = true;
        }
    }

    private void startScheduledTasks(boolean fedelta) {
        scheduler.scheduleAtFixedRate(this::controllaEInviaNotifiche, 0, 10, TimeUnit.MINUTES);
        if (fedelta){
            scheduler.scheduleAtFixedRate(this::controllaInattivitaClienti, 0, 1, TimeUnit.DAYS);
            scheduler.scheduleAtFixedRate(this::inviaPromozioniFedelta, 0, 7, TimeUnit.DAYS);
        }
        else{
            scheduler.scheduleAtFixedRate(this::inviaPromozioniATuttiClienti, 0, 30, TimeUnit.DAYS);
        }
    }

    private synchronized void controllaEInviaNotifiche() {
        ZonedDateTime oraCorrenteZoned = ZonedDateTime.now(ZoneId.of("Europe/Rome"));
        LocalDateTime oraCorrente = oraCorrenteZoned.toLocalDateTime();

        System.out.println("Ora corrente: " + oraCorrente);

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlVoli = "SELECT v.idVolo, v.giorno, v.orario FROM Volo v";
            try (PreparedStatement pstmtVoli = conn.prepareStatement(sqlVoli)) {
                ResultSet rsVoli = pstmtVoli.executeQuery();

                while (rsVoli.next()) {
                    int idVolo = rsVoli.getInt("idVolo");
                    LocalDate giornoVolo = rsVoli.getDate("giorno").toLocalDate();
                    Time orarioVolo = rsVoli.getTime("orario");
                    LocalDateTime dataVolo = LocalDateTime.of(giornoVolo, orarioVolo.toLocalTime());

                    LocalDateTime dataVoloMeno4Giorni = dataVolo.minusDays(4);
                    LocalDateTime dataVoloMeno3Giorni = dataVolo.minusDays(3);

                    if (!oraCorrente.isBefore(dataVoloMeno4Giorni) && oraCorrente.isBefore(dataVoloMeno3Giorni)) {
                        String sqlPrenotazioni = "SELECT COUNT(*) FROM PrenotazioneFidelity p WHERE p.volo_id = ?";
                        try (PreparedStatement pstmtPrenotazioni = conn.prepareStatement(sqlPrenotazioni)) {
                            pstmtPrenotazioni.setInt(1, idVolo);
                            ResultSet rsPrenotazioni = pstmtPrenotazioni.executeQuery();
                            rsPrenotazioni.next();
                            int prenotazioniCount = rsPrenotazioni.getInt(1);

                            if (prenotazioniCount == 0) {
                                String messaggio = "Attenzione! Mancano meno di 24 ore alla possibilità di prenotare un biglietto per il volo con ID: "
                                        + idVolo + ", previsto per il " + dataVolo;

                                for (String email : clientiRegistrati) {
                                    inviaNotifica(email, messaggio);
                                    notifyObservers(email, messaggio);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private synchronized void controllaInattivitaClienti() {
        LocalDate oggi = LocalDate.now();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT email, nome, cognome, indirizzo, data_nascita, data_ultimo_acquisto FROM clientefedelta";
            try (PreparedStatement pstmt = conn.prepareStatement(sql);
                 ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {
                    String email = rs.getString("email");
                    String nome = rs.getString("nome");
                    String cognome = rs.getString("cognome");
                    String indirizzo = rs.getString("indirizzo");
                    LocalDate dataNascita = rs.getDate("data_nascita").toLocalDate();
                    LocalDate ultimoAcquisto = rs.getDate("data_ultimo_acquisto").toLocalDate();
                    ultimoAcquistoCliente.put(email, ultimoAcquisto);

                    if (oggi.minusYears(2).isAfter(ultimoAcquisto)) {
                        String messaggio = "Caro cliente, non hai acquistato biglietti da più di due anni. Il tuo stato di fedeltà è stato revocato.";
                        inviaNotifica(email, messaggio);

                        String sqlTrasferisci = "INSERT INTO cliente (email, nome, cognome, indirizzo, data_nascita) VALUES (?, ?, ?, ?, ?)";
                        try (PreparedStatement pstmtTrasferisci = conn.prepareStatement(sqlTrasferisci)) {
                            pstmtTrasferisci.setString(1, email);
                            pstmtTrasferisci.setString(2, nome);
                            pstmtTrasferisci.setString(3, cognome);
                            pstmtTrasferisci.setString(4, indirizzo);
                            pstmtTrasferisci.setDate(5, Date.valueOf(dataNascita)); // Corretto l'indice
                            pstmtTrasferisci.executeUpdate();
                        }

                        String sqlRimuovi = "DELETE FROM clientefedelta WHERE email = ?";
                        try (PreparedStatement pstmtRimuovi = conn.prepareStatement(sqlRimuovi)) {
                            pstmtRimuovi.setString(1, email);
                            pstmtRimuovi.executeUpdate();
                        }

                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void inviaPromozioniFedelta() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlPromozioni = "SELECT id, descrizione, dataInizio, dataFine, tipo FROM Promozione WHERE perClientiFedelta = ?";
            try (PreparedStatement pstmtPromozioni = conn.prepareStatement(sqlPromozioni)) {
                pstmtPromozioni.setBoolean(1, true);
                try (ResultSet rsPromozioni = pstmtPromozioni.executeQuery()) {

                    while (rsPromozioni.next()) {
                        int id = rsPromozioni.getInt("id");
                        String descrizione = rsPromozioni.getString("descrizione");
                        LocalDate dataInizio = rsPromozioni.getDate("dataInizio").toLocalDate();
                        LocalDate dataScadenza = rsPromozioni.getDate("dataFine").toLocalDate();

                        String messaggio = "Promozione: " + id + "\n" +
                                "Descrizione: " + descrizione + "\n" +
                                "Valido dal: " + dataInizio + "\n" +
                                "al: " + dataScadenza;

                        for (String email : clientiRegistrati) {
                            inviaNotifica(email, messaggio);
                            notifyObservers(email, messaggio);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public synchronized void inviaPromozioniATuttiClienti() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlPromozioni = "SELECT id, descrizione, dataInizio, dataFine, tipo FROM Promozione WHERE perClientiFedelta = ?";
            try (PreparedStatement pstmtPromozioni = conn.prepareStatement(sqlPromozioni)) {
                pstmtPromozioni.setBoolean(1, false);
                try (ResultSet rsPromozioni = pstmtPromozioni.executeQuery()) {

                    while (rsPromozioni.next()) {
                        int id = rsPromozioni.getInt("id");
                        String descrizione = rsPromozioni.getString("descrizione");
                        LocalDate dataInizio = rsPromozioni.getDate("dataInizio").toLocalDate();
                        LocalDate dataScadenza = rsPromozioni.getDate("dataFine").toLocalDate();

                        String messaggio = "Promozione: " + id + "\n" +
                                "Descrizione: " + descrizione + "\n" +
                                "Valido dal: " + dataInizio + "\n" +
                                "al: " + dataScadenza;

                        try (PreparedStatement pstmtClienti = conn.prepareStatement("SELECT email FROM Cliente")) {
                            try (ResultSet rsClienti = pstmtClienti.executeQuery()) {
                                while (rsClienti.next()) {
                                    String email = rsClienti.getString("email");
                                    inviaNotifica(email, messaggio);
                                    notifyObservers(email, messaggio);
                                }
                            }
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    public synchronized void inviaNotifica(String email, String messaggio) {
        if (clientiRegistrati.contains(email)) {
            ConcurrentLinkedQueue<String> notifiche = notifichePerCliente.get(email);
            if (notifiche != null) {
                notifiche.add(messaggio);
            }
        }
    }

    public ConcurrentLinkedQueue<String> getNotifiche(String email) {
        return notifichePerCliente.get(email);
    }


    private void stop() {
        if (executorService != null) {
            executorService.shutdown();
        }
        if (scheduler != null) {
            scheduler.shutdown();
        }
    }

    private void blockUntilShutdown(Server server) throws InterruptedException {
        server.awaitTermination();
    }

    private void notifyObservers(String clientmail, String message) {
        String observerAddress = observerAddresses.get(clientmail);
        if (observerAddress != null) {
            ManagedChannel channel = ManagedChannelBuilder.forAddress(observerAddress, 50052).usePlaintext().build();
            ObserverServiceGrpc.ObserverServiceStub stub = ObserverServiceGrpc.newStub(channel);
            Notification notification = Notification.newBuilder().setClientId(clientmail).setMessage(message).build();
            stub.receiveNotification(notification, new StreamObserver<Empty>() {
                @Override
                public void onNext(Empty value) {}

                @Override
                public void onError(Throwable t) {
                    System.err.println("Errore nella notifica agli observer: " + t.getMessage());
                }

                @Override
                public void onCompleted() {
                    channel.shutdown();
                }
            });
        }
    }

    public class NotificheServiceImpl extends NotificheServiceGrpc.NotificheServiceImplBase {
        @Override
        public void registerObserver(RegisterObserverRequest request, StreamObserver<RegisterObserverResponse> responseObserver) {
            String clientmail = request.getClientId();
            String observerAddress = request.getObserverAddress();
            observerAddresses.put(clientmail, observerAddress);
            responseObserver.onNext(RegisterObserverResponse.newBuilder().setStatus("Observer registered").build());
            responseObserver.onCompleted();
        }

        @Override
        public void riceviNotificaPromozione(NotificaPromozioneRequest request, StreamObserver<NotificaPromozioneResponse> responseObserver) {
            String clientmail = request.getClientId();
            String message = request.getMessage();
            System.out.println("Notifica di promozione ricevuta per il cliente con email " + clientmail + ": " + message);

            try {
                executorService.submit(() -> {
                    try {
                        inviaNotifica(clientmail, message);
                        notifyObservers(clientmail, message);
                    } catch (Exception e) {
                        System.err.println("Errore durante l'invio della notifica: " + e.getMessage());
                    }
                });

                responseObserver.onNext(NotificaPromozioneResponse.newBuilder().setStatus("Notifica di promozione inviata").build());
            } catch (Exception e) {
                System.err.println("Errore nella ricezione della richiesta di notifica: " + e.getMessage());
                responseObserver.onNext(NotificaPromozioneResponse.newBuilder().setStatus("Errore nell'invio della notifica").build());
            } finally {
                responseObserver.onCompleted();
            }
        }


        @Override
        public void notificaInattivitaCliente(NotificaInattivitaRequest request, StreamObserver<NotificaInattivitaResponse> responseObserver) {
            String clientmail = request.getClientId();
            String message = request.getMessage();
            System.out.println("Notifica di inattività ricevuta per il cliente con email " + clientmail + ": " + message);

            LocalDate ultimoAcquisto = ultimoAcquistoCliente.get(clientmail);
            if (ultimoAcquisto != null && LocalDate.now().minusYears(2).isAfter(ultimoAcquisto)) {
                executorService.submit(() -> inviaNotifica(clientmail, message));
                notifyObservers(clientmail, message);
                responseObserver.onNext(NotificaInattivitaResponse.newBuilder().setStatus("Notifica di inattività inviata").build());
            } else {
                responseObserver.onNext(NotificaInattivitaResponse.newBuilder().setStatus("Cliente attivo, nessuna notifica inviata").build());
            }
            responseObserver.onCompleted();
        }

        @Override
        public void notificaScadenzaPrenotazione(NotificaScadenzaRequest request, StreamObserver<NotificaScadenzaResponse> responseObserver) {
            String clientmail = request.getClientId();
            String scadenzaStr = request.getScadenza();

            LocalDateTime scadenza;
            try {
                scadenza = LocalDateTime.parse(scadenzaStr);
            } catch (DateTimeParseException e) {
                System.err.println("Formato della data di scadenza non valido: " + scadenzaStr);
                responseObserver.onNext(NotificaScadenzaResponse.newBuilder().setStatus("Data di scadenza non valida").build());
                responseObserver.onCompleted();
                return;
            }

            LocalDateTime oraCorrente = LocalDateTime.now();
            System.out.println("Notifica di scadenza della prenotazione ricevuta per il cliente " + clientmail + " entro " + scadenza);

            if (oraCorrente.isBefore(scadenza) && oraCorrente.plusDays(4).toLocalDate().equals(scadenza.toLocalDate())) {
                String messaggio = "Attenzione, mancano solo 24 ore alla scadenza per prenotare il volo. Affrettati";

                executorService.submit(() -> inviaNotifica(clientmail, messaggio));
                notifyObservers(clientmail, messaggio);

                responseObserver.onNext(NotificaScadenzaResponse.newBuilder().setStatus("Notifica di scadenza inviata").build());
            } else if (oraCorrente.isAfter(scadenza)) {
                responseObserver.onNext(NotificaScadenzaResponse.newBuilder().setStatus("Prenotazione scaduta").build());
            } else {
                responseObserver.onNext(NotificaScadenzaResponse.newBuilder().setStatus("La prenotazione non è ancora in scadenza").build());
            }
            responseObserver.onCompleted();
        }
    }
}







