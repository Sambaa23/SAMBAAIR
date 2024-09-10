package Updatable.GUIClient;

import Updatable.Aereo.Aereo;
import Updatable.Aereo.AereoFactoryImpl;
import Updatable.Biglietto.Biglietto;
import Updatable.Cliente.Cliente;
import Updatable.Pagamento.Pagamento;
import Updatable.Prenotazione.Prenotazione;
import Updatable.Volo.VoliDisponibili;
import Updatable.Volo.Volo;
import Updatable.conf.DatabaseConnection;
import client.NotificheClient;
import server.NotificheServer;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

public class GUIVoliNormale extends JFrame {
    private JTabbedPane tabbedPane;
    private JPanel panelVoli;
    private JPanel panelBiglietti;
    private List<Volo> voliDisponibili;
    private List<Aereo> aereiDisponibili;
    private Cliente cliente;
    private List<Biglietto> bigliettiCliente;
    private static NotificheServer server;
    private List<String> notifiche;
    private JPanel panelPosta;

    public GUIVoliNormale(Cliente cliente, NotificheServer server) {
        this.cliente = cliente;
        GUIVoliNormale.server = server;
        this.notifiche = new ArrayList<>();


        setTitle("Gestione Voli e Biglietti - Normale");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        tabbedPane = new JTabbedPane();

        panelVoli = new JPanel();
        panelVoli.setLayout(new BoxLayout(panelVoli, BoxLayout.Y_AXIS));
        JScrollPane scrollPaneVoli = new JScrollPane(panelVoli);
        tabbedPane.addTab("Voli Disponibili", scrollPaneVoli);

        panelBiglietti = new JPanel();
        panelBiglietti.setLayout(new BoxLayout(panelBiglietti, BoxLayout.Y_AXIS));
        JScrollPane scrollPaneBiglietti = new JScrollPane(panelBiglietti);
        tabbedPane.addTab("I Miei Biglietti Acquistati", scrollPaneBiglietti);

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
            List<Volo> voliDisponibili = voliDisponibiliClass.getVoliDisponibili();
            List<Aereo> aereiDisponibili = voliDisponibiliClass.getAereiDisponibili();

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
                System.out.println("Nessuna notifica trovata per: " + cliente.getEmail()); // Debug
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
            }
        }
        panelPosta.revalidate();
        panelPosta.repaint();
    }

    // Conta i voli nel database
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

    // Inserisci gli aerei nel database
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
        panelVoli.add(Box.createRigidArea(new Dimension(0, 10)));
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
        Prenotazione prenotazione = new Prenotazione(cliente, volo);
        return prenotazione.isScaduta();
    }

    private Aereo recuperaDettagliAereo(long numeroSeriale, String modello) {
        Aereo aereo = null;
        try (Connection connection = DatabaseConnection.getConnection()) {
            String query = "SELECT * FROM Aereo WHERE numeroSeriale = ? AND modello = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setLong(1, numeroSeriale);  // Impostiamo il numero seriale
                stmt.setString(2, modello);      // Impostiamo il modello
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


    private void visualizzaBigliettiCliente() {
        panelBiglietti.removeAll();

        bigliettiCliente = recuperaBigliettiCliente();

        DefaultListModel<Biglietto> listModel = new DefaultListModel<>();
        for (Biglietto biglietto : bigliettiCliente) {
            listModel.addElement(biglietto);
        }
        JList<Biglietto> listBiglietti = new JList<>(listModel);
        JScrollPane scrollPane = new JScrollPane(listBiglietti);

        JButton btnModificaBiglietto = new JButton("Modifica Data/Ora Biglietto");
        btnModificaBiglietto.addActionListener(e -> {
            Biglietto bigliettoSelezionato = listBiglietti.getSelectedValue();
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

    private void mostraDialogoModificaBiglietto(Biglietto bigliettoSelezionato) {
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

    private boolean mostraDialogoPagamento(Biglietto bigliettoSelezionato) {
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
    private List<Biglietto> recuperaBigliettiCliente() {
        List<Biglietto> biglietti = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection()) {
            if (connection == null) {
                throw new SQLException("Connessione al database non riuscita.");
            }

            String query = "SELECT * FROM Biglietto WHERE cliente_email = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, cliente.getEmail());
                ResultSet rs = stmt.executeQuery();

                while (rs.next()) {
                    int idBiglietto = rs.getInt("id");
                    int idVolo = rs.getInt("volo_id");
                    Volo volo = recuperaDettagliVolo(idVolo);

                    Biglietto biglietto = new Biglietto(
                            idBiglietto,
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
                            aereo // Passiamo l'oggetto Aereo qui
                    );
                    volo.setDisponibilita(rs.getInt("disponibilita"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return volo;
    }

    private void prenotaVolo(Volo volo) {
        try {
            Prenotazione prenotazione = new Prenotazione(cliente, volo);
            new PrenotazioneNormaleGUI(prenotazione).setVisible(true);

        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Prenotazione Esistente", JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}
