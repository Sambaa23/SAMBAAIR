package Updatable.GUIClient;

import Updatable.Aereo.Aereo;
import Updatable.Aereo.AereoFactoryImpl;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Pagamento.Pagamento;
import Updatable.Prenotazione.PrenotazioneFidelity;
import Updatable.Volo.VoliDisponibili;
import Updatable.Volo.Volo;
import Updatable.conf.DatabaseConnection;
import Updatable.Biglietto.BigliettoFidelity;
import client.NotificheClient;
import server.NotificheServer;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GUIVoliFidelity extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel panelVoli;
    private JPanel panelBiglietti;
    private JPanel panelPosta;
    private JComboBox<String> cboTariffa;
    private List<Volo> voliDisponibili;
    private List<Aereo> aereiDisponibili;
    private ClienteFedelta cliente;
    private List<BigliettoFidelity> bigliettiCliente;
    private List<String> notifiche;
    private static NotificheServer server;
    private static final Map<String, List<String>> cittaDestinazione = new HashMap<>();

    static {
        List<String> aeroportiEuropei = Arrays.asList(
                "FCO", "MXP", "LIN", "BLQ", "NAP",
                "CDG", "LHR", "AMS", "FRA", "MAD", "BCN", "MUC", "ZRH"
        );
        cittaDestinazione.put("EUR", aeroportiEuropei);
    }

    public GUIVoliFidelity(ClienteFedelta cliente, NotificheServer server) {
        this.cliente = cliente;
        GUIVoliFidelity.server = server;
        this.notifiche = new ArrayList<>();

        setTitle("Gestione Voli e Biglietti - Fedeltà");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Creazione del JTabbedPane
        tabbedPane = new JTabbedPane();

        // Pannello per i voli
        panelVoli = new JPanel();
        panelVoli.setLayout(new BoxLayout(panelVoli, BoxLayout.Y_AXIS));
        JScrollPane scrollPaneVoli = new JScrollPane(panelVoli);
        tabbedPane.addTab("Voli Disponibili", scrollPaneVoli);

        // Pannello per i biglietti
        panelBiglietti = new JPanel();
        panelBiglietti.setLayout(new BoxLayout(panelBiglietti, BoxLayout.Y_AXIS));
        JScrollPane scrollPaneBiglietti = new JScrollPane(panelBiglietti);
        tabbedPane.addTab("I Miei Biglietti Acquistati", scrollPaneBiglietti);

        // Pannello per la posta
        panelPosta = new JPanel();
        panelPosta.setLayout(new BoxLayout(panelPosta, BoxLayout.Y_AXIS));
        JScrollPane scrollPanePosta = new JScrollPane(panelPosta);
        tabbedPane.addTab("Posta", scrollPanePosta);


        add(tabbedPane, BorderLayout.CENTER);


        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            if (selectedIndex == 2) {
                aggiornaNotifiche();
            } else if (selectedIndex == 1) {
                visualizzaBigliettiCliente();
            }
        });

        int numVoliEsistenti = contaVoliNelDatabase();
        if (numVoliEsistenti < 10) {
            VoliDisponibili voliDisponibiliClass = new VoliDisponibili(new AereoFactoryImpl(), 10, 50 - numVoliEsistenti);
            this.voliDisponibili = voliDisponibiliClass.getVoliDisponibili();
            this.aereiDisponibili = voliDisponibiliClass.getAereiDisponibili();

            int nextIdVolo = getNextVoloId();
            for (int i = 0; i < voliDisponibili.size(); i++) {
                Volo volo = voliDisponibili.get(i);
                Aereo aereo = volo.getAereo();
                Aereo.TipoVeicolo tipoVeicolo = aereo.getTipo();

                Random rand = new Random();
                int disponibilita = switch (tipoVeicolo) {
                    case NAZIONALE -> rand.nextInt(180) + 1;
                    case INTERNAZIONALE -> rand.nextInt(300) + 1;
                    case INTERCONTINENTALE -> rand.nextInt(600) + 1;
                };

                volo = new Volo(nextIdVolo + i, volo.getPartenza(), volo.getDestinazione(), volo.getOrario(), volo.getGiorno(), volo.getAereo());
                volo.setDisponibilita(disponibilita);
                voliDisponibili.set(i, volo);
            }

            inserisciAereiNelDatabase();
            inserisciVoliNelDatabase();
        }

        caricaVoliDalDatabase();
        setLocationRelativeTo(null);

        if (cliente.getPuntiAccumulati() >= 10000) {
            mostraPremioFedelta();
        }
    }

    private void mostraPremioFedelta() {
        int result = JOptionPane.showConfirmDialog(this, "Complimenti, hai vinto un biglietto omaggio per una meta europea! Clicca OK per scegliere dove andare!", "Premio Fedeltà", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            mostraVoliDisponibiliEuropei();
        }
    }

    private void mostraVoliDisponibiliEuropei() {
        JFrame frameVoli = new JFrame("Voli Disponibili per l'Europa");
        frameVoli.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        frameVoli.setSize(600, 500);

        List<Volo> voliEuropei = caricaVoliEuropeiDalDatabase();

        if (voliEuropei.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Non ci sono voli disponibili per mete europee al momento.");
            frameVoli.dispose();
            return;
        }

        int row = 0;
        for (Volo volo : voliEuropei) {
            JButton btnPrenotaVolo = new JButton("Prenota Biglietto per " + volo.getPartenza() + " -> " + volo.getDestinazione());
            btnPrenotaVolo.addActionListener(e -> {
                prenotaBigliettoOmaggio(volo);
                JOptionPane.showMessageDialog(frameVoli, "Biglietto prenotato con successo! I tuoi punti fedeltà sono stati aggiornati.");
                frameVoli.dispose();
            });

            gbc.gridy = row++;
            frameVoli.add(btnPrenotaVolo, gbc);
        }

        frameVoli.setVisible(true);
        frameVoli.setLocationRelativeTo(this);
    }

    private List<Volo> caricaVoliEuropeiDalDatabase() {
        List<Volo> voliEuropei = new ArrayList<>();

        List<Volo> tuttiIVoli = caricaVoliDalDatabaseBis();

        for (Volo volo : tuttiIVoli) {
            if (cittaDestinazione.get("EUR").contains(volo.getPartenza()) &&
                    cittaDestinazione.get("EUR").contains(volo.getDestinazione())) {
                voliEuropei.add(volo);
            }
        }

        return voliEuropei;
    }

    private List<Volo> caricaVoliDalDatabaseBis() {
        List<Volo> voli = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Volo";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Aereo aereo = recuperaDettagliAereo(rs.getLong("aereo_numeroSeriale"), rs.getString("aereo_modello"));

                    Volo volo = new Volo(
                            rs.getInt("idVolo"),
                            rs.getString("partenza"),
                            rs.getString("destinazione"),
                            rs.getTime("orario").toLocalTime(),
                            rs.getDate("giorno").toLocalDate(),
                            aereo != null ? aereo : new Aereo(0, "Sconosciuto", Aereo.TipoVeicolo.NAZIONALE) // Gestione di aereo null
                    );

                    volo.setDisponibilita(rs.getInt("disponibilita"));

                    if (!isVoloScaduto(volo)) {
                        voli.add(volo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return voli;
    }

    private void prenotaBigliettoOmaggio(Volo volo) {
        JPanel panel = new JPanel(new GridLayout(3, 2));
        JTextField txtNomeBeneficiario = new JTextField();
        JTextField txtCognomeBeneficiario = new JTextField();
        cboTariffa = new JComboBox<>(new String[]{"economy", "premium", "business"});

        panel.add(new JLabel("Nome Beneficiario:"));
        panel.add(txtNomeBeneficiario);
        panel.add(new JLabel("Cognome Beneficiario:"));
        panel.add(txtCognomeBeneficiario);
        panel.add(new JLabel("Tariffa:"));
        panel.add(cboTariffa);

        int result = JOptionPane.showConfirmDialog(null, panel, "Inserisci i dettagli del beneficiario", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String nomeBeneficiario = txtNomeBeneficiario.getText().trim();
            String cognomeBeneficiario = txtCognomeBeneficiario.getText().trim();
            String tariffa = (String) cboTariffa.getSelectedItem();

            if (nomeBeneficiario.isEmpty() || cognomeBeneficiario.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Compila tutti i campi.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            PrenotazioneFidelity prenotazione = new PrenotazioneFidelity(cliente, volo, LocalDate.now());
            BigliettoFidelity bigliettoOmaggio = new BigliettoFidelity(cliente, nomeBeneficiario, cognomeBeneficiario, volo,0.0, tariffa);
            prenotazione.getBiglietti().add(bigliettoOmaggio);
            completaPrenotazioneBigliettoOmaggio(prenotazione, bigliettoOmaggio);
            cliente.getPrenotazioni().add(prenotazione);

            JOptionPane.showMessageDialog(this, "Hai prenotato con successo il tuo biglietto omaggio!");

        }
    }

    private void completaPrenotazioneBigliettoOmaggio(PrenotazioneFidelity prenotazione, BigliettoFidelity bigliettoOmaggio) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String insertPrenotazioneQuery = "INSERT INTO PrenotazioneFidelity (cliente_email, volo_id, data_prenotazione) VALUES (?, ?, ?)";
                try (PreparedStatement prenotazioneStmt = connection.prepareStatement(insertPrenotazioneQuery)) {
                    prenotazioneStmt.setString(1, prenotazione.getCliente().getEmail());
                    prenotazioneStmt.setInt(2, prenotazione.getVolo().getIdVolo());
                    prenotazioneStmt.setDate(3, new Date(System.currentTimeMillis()));
                    prenotazioneStmt.executeUpdate();
                }

                String insertBigliettoQuery = "INSERT INTO BigliettoFidelity (cliente_fedelta_id, volo_id, nome_beneficiario, cognome_beneficiario, prezzo, punti, tariffa, pagato) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement bigliettoStmt = connection.prepareStatement(insertBigliettoQuery)) {
                    bigliettoStmt.setString(1, cliente.getEmail());
                    bigliettoStmt.setInt(2, bigliettoOmaggio.getVolo().getIdVolo());
                    bigliettoStmt.setString(3, bigliettoOmaggio.getNomeBeneficiario());
                    bigliettoStmt.setString(4, bigliettoOmaggio.getCognomeBeneficiario());
                    bigliettoStmt.setDouble(5, 0.0);
                    bigliettoStmt.setInt(6, 0);
                    bigliettoStmt.setString(7, "Economy");
                    bigliettoStmt.setBoolean(8, true);
                    bigliettoStmt.executeUpdate();
                }

                int puntiTotali = cliente.getPuntiAccumulati()-10000;
                cliente.setPuntiAccumulati(puntiTotali);
                cliente.setDataUltimoAcquisto(LocalDate.now());

                String updatePuntiClienteQuery = "UPDATE ClienteFedelta SET punti_accumulati = ?, data_ultimo_acquisto = ? WHERE email = ?";
                try (PreparedStatement updatePuntiStmt = connection.prepareStatement(updatePuntiClienteQuery)) {
                    updatePuntiStmt.setInt(1, puntiTotali);
                    updatePuntiStmt.setDate(2, new Date(System.currentTimeMillis()));
                    updatePuntiStmt.setString(3, cliente.getEmail());
                    updatePuntiStmt.executeUpdate();
                }

                int nuovaDisponibilita = bigliettoOmaggio.getVolo().getDisponibilita() - 1;
                bigliettoOmaggio.getVolo().setDisponibilita(nuovaDisponibilita);

                String updateDisponibilitaVoloQuery = "UPDATE Volo SET disponibilita = ? WHERE idVolo = ?";
                try (PreparedStatement updateVoloStmt = connection.prepareStatement(updateDisponibilitaVoloQuery)) {
                    updateVoloStmt.setInt(1, nuovaDisponibilita);
                    updateVoloStmt.setInt(2, bigliettoOmaggio.getVolo().getIdVolo());
                    updateVoloStmt.executeUpdate();
                }

                connection.commit();

            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore nella prenotazione del biglietto omaggio.", "Errore", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella connessione al database.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void aggiornaNotifiche() {
        SwingUtilities.invokeLater(() -> {
            ConcurrentLinkedQueue<String> notificheQueue = server.getNotifiche(cliente.getEmail());
            if (notificheQueue != null) {
                notifiche.clear();
                notifiche.addAll(notificheQueue);
                visualizzaNotifiche();

                NotificheClient client = new NotificheClient("localhost", 50051);
                try {
                    client.riceviNotificheScadenzaPrenotazione(cliente.getEmail(), notificheQueue);
                    client.riceviNotificheInattivita(cliente.getEmail(), "Test di inattività");
                    client.riceviNotifichePromozione(cliente.getEmail(), "Test di promozioni");
                } finally {
                    try {
                        client.shutdown();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                System.out.println("Nessuna notifica trovata per: " + cliente.getEmail());
            }
        });
    }

    private void visualizzaNotifiche() {
        panelPosta.removeAll();
        if (notifiche.isEmpty()) {
            panelPosta.add(new JLabel("Nessuna notifica disponibile."));
        } else {
            for (String notifica : notifiche) {
                panelPosta.add(new JLabel(notifica));
                if (notifica.equals("Caro cliente, non hai acquistato biglietti da più di due anni. Il tuo stato di fedeltà è stato revocato.")){
                    JOptionPane.showMessageDialog(null, "Il tuo stato di fedeltà è stato revocato. Riavvia la gui e accedi come cliente normale.");
                    dispose();
                }
            }
        }
        panelPosta.revalidate();
        panelPosta.repaint();
    }

    private int getNextVoloId() {
        int nextId = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COALESCE(MAX(idVolo), 0) + 1 AS nextId FROM Volo";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    nextId = rs.getInt("nextId");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return nextId;
    }

    private int contaVoliNelDatabase() {
        int count = 0;
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) AS numVoli FROM Volo";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    count = rs.getInt("numVoli");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return count;
    }

    private void inserisciAereiNelDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Aereo (numeroSeriale, modello, tipo) VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE numeroSeriale = numeroSeriale";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Aereo aereo : aereiDisponibili) {
                    pstmt.setLong(1, aereo.getNumeroSeriale());
                    pstmt.setString(2, aereo.getModello());
                    pstmt.setString(3, aereo.getTipo().name());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void inserisciVoliNelDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "INSERT INTO Volo (idVolo, partenza, destinazione, orario, giorno, disponibilita, aereo_numeroSeriale, aereo_modello) VALUES (?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE idVolo = idVolo";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                for (Volo volo : voliDisponibili) {
                    pstmt.setInt(1, volo.getIdVolo());
                    pstmt.setString(2, volo.getPartenza());
                    pstmt.setString(3, volo.getDestinazione());
                    pstmt.setTime(4, java.sql.Time.valueOf(volo.getOrario()));
                    pstmt.setDate(5, java.sql.Date.valueOf(volo.getGiorno()));
                    pstmt.setInt(6, volo.getDisponibilita());
                    pstmt.setLong(7, volo.getAereo().getNumeroSeriale());
                    pstmt.setString(8, volo.getAereo().getModello());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void aggiungiVoloAllaGUI(Volo volo) {
        JPanel panelVolo = new JPanel();
        panelVolo.setLayout(new FlowLayout(FlowLayout.LEFT));

        JLabel lblVolo = new JLabel(volo.getIdVolo() + " - " + volo.getPartenza() + " -> " + volo.getDestinazione() +
                " | Data: " + volo.getGiorno() +
                " | Ora: " + volo.getOrario() +
                " | Disponibilità: " + volo.getDisponibilita() +
                " | Aereo: " + volo.getAereo().getModello());
        JButton btnPrenota = new JButton("Prenota Biglietto/i");

        btnPrenota.addActionListener(e -> prenotaVolo(volo));

        panelVolo.add(lblVolo);
        panelVolo.add(btnPrenota);

        panelVoli.add(panelVolo);
        panelVoli.add(Box.createRigidArea(new Dimension(0, 10)));  // Spazio tra le righe
    }

    private void caricaVoliDalDatabase() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Volo";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                ResultSet rs = pstmt.executeQuery();
                while (rs.next()) {
                    Aereo aereo = recuperaDettagliAereo(rs.getLong("aereo_numeroSeriale"), rs.getString("aereo_modello"));

                    Volo volo = new Volo(
                            rs.getInt("idVolo"),
                            rs.getString("partenza"),
                            rs.getString("destinazione"),
                            rs.getTime("orario").toLocalTime(),
                            rs.getDate("giorno").toLocalDate(),
                            aereo != null ? aereo : new Aereo(0, "Sconosciuto", Aereo.TipoVeicolo.NAZIONALE) // Gestione di aereo null
                    );

                    volo.setDisponibilita(rs.getInt("disponibilita"));

                    if (!isVoloScaduto(volo)) {
                        aggiungiVoloAllaGUI(volo);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean isVoloScaduto(Volo volo) {
        PrenotazioneFidelity prenotazione = new PrenotazioneFidelity(cliente, volo);
        return prenotazione.isScaduta();
    }


    private void visualizzaBigliettiCliente() {
        panelBiglietti.removeAll();
        bigliettiCliente = recuperaBigliettiCliente();

        DefaultListModel<BigliettoFidelity> listModel = new DefaultListModel<>();
        for (BigliettoFidelity biglietto : bigliettiCliente) {
            listModel.addElement(biglietto);
        }

        JList<BigliettoFidelity> listBiglietti = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listBiglietti);

        JButton btnModificaBiglietto = new JButton("Modifica Data/Ora Biglietto");
        btnModificaBiglietto.addActionListener(e -> {
            BigliettoFidelity bigliettoSelezionato = listBiglietti.getSelectedValue();
            if (bigliettoSelezionato != null) {
                mostraDialogoModificaBiglietto(bigliettoSelezionato);
            } else {
                JOptionPane.showMessageDialog(this, "Seleziona un biglietto per modificarlo.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });

        panelBiglietti.setLayout(new BorderLayout());
        panelBiglietti.add(scrollPane, BorderLayout.CENTER);
        panelBiglietti.add(btnModificaBiglietto, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void mostraDialogoModificaBiglietto(BigliettoFidelity bigliettoSelezionato) {
        JPanel panel = new JPanel(new GridLayout(4, 2));

        JTextField txtNuovaData = new JTextField();
        JTextField txtNuovoOrario = new JTextField();

        panel.add(new JLabel("Nuova Data (YYYY-MM-DD):"));
        panel.add(txtNuovaData);
        panel.add(new JLabel("Nuovo Orario (HH:MM):"));
        panel.add(txtNuovoOrario);

        int result = JOptionPane.showConfirmDialog(null, panel, "Modifica Data e Orario", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            try {
                String nuovaData = txtNuovaData.getText().trim();
                String nuovoOrario = txtNuovoOrario.getText().trim();

                LocalDate data = LocalDate.parse(nuovaData);
                LocalTime orario = LocalTime.parse(nuovoOrario);
                bigliettoSelezionato.getVolo().setOrario(orario);
                bigliettoSelezionato.getVolo().setGiorno(data);

                cliente.richiediModificaBiglietto(bigliettoSelezionato, bigliettoSelezionato.getVolo());

                if (mostraDialogoPagamento(bigliettoSelezionato)) {
                    aggiornaBigliettoInDatabase(bigliettoSelezionato.getVolo());
                    JOptionPane.showMessageDialog(null, "Modifica del biglietto e pagamento avvenuti con successo.");
                    visualizzaBigliettiCliente();
                } else {
                    JOptionPane.showMessageDialog(null, "Pagamento non riuscito. Modifica del biglietto annullata.", "Errore", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Errore durante l'inserimento della data o dell'orario. Verifica i dati inseriti.", "Errore", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private boolean mostraDialogoPagamento(BigliettoFidelity bigliettoSelezionato) {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        JTextField txtNumeroCarta = new JTextField();
        JTextField txtScadenza = new JTextField();
        JTextField txtCVV = new JTextField();

        panel.add(new JLabel("Numero Carta:"));
        panel.add(txtNumeroCarta);
        panel.add(new JLabel("Data di Scadenza (MM/YY):"));
        panel.add(txtScadenza);
        panel.add(new JLabel("CVV:"));
        panel.add(txtCVV);

        int result = JOptionPane.showConfirmDialog(null, panel, "Inserisci i dettagli di pagamento", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            String numeroCarta = txtNumeroCarta.getText().trim();
            String scadenza = txtScadenza.getText().trim();
            String cvv = txtCVV.getText().trim();
            double importoTotale = bigliettoSelezionato.calcolaTassaModifica();
            Pagamento pagamento = new Pagamento(numeroCarta, scadenza, cvv, cliente.getNome() + " " + cliente.getCognome());

            if (pagamento.validaPagamento(importoTotale)) {
                JOptionPane.showMessageDialog(null, "Pagamento effettuato con successo!");
                return true;
            } else {
                JOptionPane.showMessageDialog(null, "Pagamento fallito. Si prega di riprovare.");
                return false;
            }
        }
        return false;
    }

    private void aggiornaBigliettoInDatabase(Volo nuovoVolo) {
        try (Connection connection = DatabaseConnection.getConnection()) {
            connection.setAutoCommit(false);

            try {
                String updateVoloQuery = "UPDATE Volo SET orario = ?, giorno = ? WHERE idVolo = ?";
                try (PreparedStatement updateVoloStmt = connection.prepareStatement(updateVoloQuery)) {
                    updateVoloStmt.setTime(1, Time.valueOf(nuovoVolo.getOrario()));
                    updateVoloStmt.setDate(2, Date.valueOf(nuovoVolo.getGiorno()));
                    updateVoloStmt.setInt(3, nuovoVolo.getIdVolo());
                    updateVoloStmt.executeUpdate();
                }
                connection.commit();
                System.out.println("Biglietto e volo aggiornati nel database con successo!");
            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Errore durante l'aggiornamento del database: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
    }

    private List<BigliettoFidelity> recuperaBigliettiCliente() {
        List<BigliettoFidelity> biglietti = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connessione al database non riuscita.");
            }

            String query = "SELECT * FROM BigliettoFidelity WHERE cliente_fedelta_id = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, cliente.getEmail());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int idBiglietto = rs.getInt("id"); // Recupera l'ID del biglietto
                    int idVolo = rs.getInt("volo_id");
                    Volo volo = recuperaDettagliVolo(idVolo);

                    BigliettoFidelity biglietto = new BigliettoFidelity(
                            idBiglietto, // Passa l'ID del biglietto al costruttore
                            cliente,
                            rs.getString("nome_beneficiario"),
                            rs.getString("cognome_beneficiario"),
                            volo,
                            rs.getDouble("prezzo"),
                            rs.getString("tariffa")
                    );
                    biglietti.add(biglietto);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return biglietti;
    }


    private Volo recuperaDettagliVolo(int idVolo) {
        Volo volo = null;
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connessione al database non riuscita.");
            }

            String query = "SELECT * FROM Volo WHERE idVolo = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setInt(1, idVolo);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Aereo aereo = recuperaDettagliAereo(rs.getLong("aereo_numeroSeriale"), rs.getString("aereo_modello"));

                    volo = new Volo(
                            rs.getInt("idVolo"),
                            rs.getString("partenza"),
                            rs.getString("destinazione"),
                            rs.getTime("orario").toLocalTime(),
                            rs.getDate("giorno").toLocalDate(),
                            aereo
                    );
                    volo.setDisponibilita(rs.getInt("disponibilita"));
                } else {
                    System.out.println("Volo non trovato: " + idVolo);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return volo;
    }

    private Aereo recuperaDettagliAereo(long numeroSeriale, String modello) {
        Aereo aereo = null;
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connessione al database non riuscita.");
            }

            String query = "SELECT * FROM Aereo WHERE numeroSeriale = ? AND modello = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, numeroSeriale);
                stmt.setString(2, modello);
                ResultSet rs = stmt.executeQuery();

                if (rs.next()) {
                    Aereo.TipoVeicolo tipo = Aereo.TipoVeicolo.valueOf(rs.getString("tipo"));

                    aereo = new Aereo(
                            rs.getLong("numeroSeriale"),
                            rs.getString("modello"),
                            tipo
                    );
                } else {
                    System.out.println("Aereo non trovato: " + numeroSeriale + ", " + modello);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return aereo;
    }

    private void prenotaVolo(Volo volo) {
        try {
            PrenotazioneFidelity prenotazione = new PrenotazioneFidelity(cliente, volo);
            new PrenotazioneFidelityGUI(prenotazione).setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Prenotazione Esistente", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}


