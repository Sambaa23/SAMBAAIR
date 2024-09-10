package Updatable.GUIServer;

import Updatable.Aereo.Aereo;
import Updatable.Promozione.PromozioneAbstract;
import Updatable.Promozione.PromozionePeriodo;
import Updatable.Promozione.PromozioneVoli;
import Updatable.Volo.Volo;
import Updatable.conf.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminGUI extends JFrame {

    private static final Map<String, String> airports = new HashMap<>();
    static {
        // Aeroporti italiani principali
        airports.put("FCO", "Roma Fiumicino");
        airports.put("MXP", "Milano Malpensa");
        airports.put("LIN", "Milano Linate");
        airports.put("BLQ", "Bologna Guglielmo Marconi");
        airports.put("NAP", "Napoli Capodichino");

        // Aeroporti europei principali
        airports.put("CDG", "Parigi Charles De Gaulle");
        airports.put("LHR", "Londra Heathrow");
        airports.put("AMS", "Amsterdam Schiphol");
        airports.put("FRA", "Francoforte");
        airports.put("MAD", "Madrid Barajas");
        airports.put("BCN", "Barcellona El Prat");
        airports.put("MUC", "Monaco di Baviera");
        airports.put("ZRH", "Zurigo");

        // Aeroporti mondiali principali
        airports.put("JFK", "New York John F. Kennedy");
        airports.put("LAX", "Los Angeles");
        airports.put("ORD", "Chicago O'Hare");
        airports.put("ATL", "Atlanta Hartsfield-Jackson");
        airports.put("DXB", "Dubai");
        airports.put("HND", "Tokyo Haneda");
        airports.put("PEK", "Pechino Capital");
        airports.put("SYD", "Sydney Kingsford Smith");
    }

    private JTextField txtEmail, txtNome, txtCognome, txtIndirizzo, txtDataNascita, txtCodice, txtPuntiAccum, txtDataUltimoAcquisto;
    private JTextField txtVoloID, txtDataVolo, txtOraVolo, txtDisponibilita, txtNumeroSerialeAereo, txtModelloAereo;
    private JComboBox<String> cbPartenza, cbDestinazione;
    private JTextField txtPrenotazioneClienteEmail, txtPrenotazioneVoloID, txtPrenotazioneData;
    private JTextField txtIdPromozione, txtDescrizione, txtDataInizio, txtDataFine, txtVoliAssociati;
    private JCheckBox chkClienteFedelta, chkFedelta, chkFidelity, chkPromovoli;

    public AdminGUI() {
        setTitle("Admin GUI - Gestione Clienti, Voli, Prenotazioni e Promozioni");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Clienti", creaPannelloClienti());

        tabbedPane.add("Voli", creaPannelloVoli());

        tabbedPane.add("Prenotazioni", creaPannelloPrenotazioni());

        tabbedPane.add("Promozioni", creaPannelloPromozioni());

        add(tabbedPane);

        setVisible(true);
    }

    private JPanel creaPannelloClienti() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        panel.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtEmail, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Nome:"), gbc);
        txtNome = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtNome, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Cognome:"), gbc);
        txtCognome = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtCognome, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Indirizzo:"), gbc);
        txtIndirizzo = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtIndirizzo, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Data di nascita (gg/mm/aaaa):"), gbc);
        txtDataNascita = new JTextField(20);
        gbc.gridx = 1;
        panel.add(txtDataNascita, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        chkFidelity = new JCheckBox("Cliente Fedeltà");
        panel.add(chkFidelity, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Codice Fid:"), gbc);
        txtCodice = new JTextField(10);
        txtCodice.setEnabled(false);
        gbc.gridx = 1;
        panel.add(txtCodice, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Punti:"), gbc);
        txtPuntiAccum = new JTextField(10);
        txtPuntiAccum.setEnabled(false);
        gbc.gridx = 1;
        panel.add(txtPuntiAccum, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        panel.add(new JLabel("Data ultimo acquisto (gg/mm/aaaa):"), gbc);
        txtDataUltimoAcquisto = new JTextField(20);
        txtDataUltimoAcquisto.setEnabled(false);
        gbc.gridx = 1;
        panel.add(txtDataUltimoAcquisto, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        JButton btnAddClient = new JButton("Aggiungi Cliente");
        btnAddClient.addActionListener(e -> aggiungiCliente());
        panel.add(btnAddClient, gbc);

        gbc.gridx = 1;
        JButton btnUpdateClient = new JButton("Aggiorna Cliente");
        btnUpdateClient.addActionListener(e -> aggiornaCliente());
        panel.add(btnUpdateClient, gbc);

        gbc.gridx = 0;
        gbc.gridy = ++row;
        JButton btnDeleteClient = new JButton("Elimina Cliente");
        btnDeleteClient.addActionListener(e -> eliminaCliente());
        panel.add(btnDeleteClient, gbc);

        gbc.gridx = 1;
        JButton btnViewClients = new JButton("Visualizza Clienti");
        btnViewClients.addActionListener(e -> visualizzaClienti());
        panel.add(btnViewClients, gbc);

        chkFidelity.addItemListener(e -> {
            boolean isSelected = (e.getStateChange() == ItemEvent.SELECTED);
            txtCodice.setEnabled(isSelected);
            txtPuntiAccum.setEnabled(isSelected);
            txtDataUltimoAcquisto.setEnabled(isSelected);
        });

        return panel;
    }

    private JPanel creaPannelloVoli() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        // ID Volo
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("ID Volo:"), gbc);

        gbc.gridx = 1;
        txtVoloID = new JTextField();
        panel.add(txtVoloID, gbc);

        row++;

        // Partenza
        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Partenza:"), gbc);

        gbc.gridx = 1;
        cbPartenza = new JComboBox<>(getAirportOptions());
        cbPartenza.addActionListener(e -> updateDestinationOptions());
        panel.add(cbPartenza, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Destinazione:"), gbc);

        gbc.gridx = 1;
        cbDestinazione = new JComboBox<>(getAirportOptions());
        cbDestinazione.addActionListener(e -> updateDepartureOptions());
        panel.add(cbDestinazione, gbc);

        setDefaultAirportSelection();

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Data (gg/MM/aaaa):"), gbc);

        gbc.gridx = 1;
        txtDataVolo = new JTextField();
        panel.add(txtDataVolo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Ora (HH:MM):"), gbc);

        gbc.gridx = 1;
        txtOraVolo = new JTextField();
        panel.add(txtOraVolo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Disponibilità:"), gbc);

        gbc.gridx = 1;
        txtDisponibilita = new JTextField();
        panel.add(txtDisponibilita, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Numero Seriale Aereo:"), gbc);

        gbc.gridx = 1;
        txtNumeroSerialeAereo = new JTextField();
        panel.add(txtNumeroSerialeAereo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Modello Aereo:"), gbc);

        gbc.gridx = 1;
        txtModelloAereo = new JTextField();
        panel.add(txtModelloAereo, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10));

        JButton btnAddFlight = new JButton("Aggiungi Volo");
        btnAddFlight.addActionListener(e -> aggiungiVolo());
        buttonPanel.add(btnAddFlight);

        JButton btnUpdateFlight = new JButton("Aggiorna Volo");
        btnUpdateFlight.addActionListener(e -> aggiornaVolo());
        buttonPanel.add(btnUpdateFlight);

        JButton btnDeleteFlight = new JButton("Elimina Volo");
        btnDeleteFlight.addActionListener(e -> eliminaVolo());
        buttonPanel.add(btnDeleteFlight);

        JButton btnViewFlights = new JButton("Visualizza Voli");
        btnViewFlights.addActionListener(e -> visualizzaVoli());
        buttonPanel.add(btnViewFlights);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private void setDefaultAirportSelection() {
        String defaultDeparture = "FCO";
        String defaultDestination = "MXP";

        if (isAirportCodeAvailable(defaultDeparture) && isAirportCodeAvailable(defaultDestination)) {
            cbPartenza.setSelectedItem(defaultDeparture + " (" + airports.get(defaultDeparture) + ")");
            cbDestinazione.setSelectedItem(defaultDestination + " (" + airports.get(defaultDestination) + ")");

            updateDestinationOptions();
            updateDepartureOptions();
        }
    }

    private boolean isAirportCodeAvailable(String code) {
        return airports.containsKey(code);
    }

    private String[] getAirportOptions() {
        return airports.entrySet().stream()
                .map(entry -> entry.getKey() + " (" + entry.getValue() + ")")
                .toArray(String[]::new);
    }

    private void updateDepartureOptions() {
        String selectedDestination = getCodeFromComboBox(cbDestinazione);
        String selectedDeparture = getCodeFromComboBox(cbPartenza);

        cbPartenza.removeAllItems();
        for (Map.Entry<String, String> entry : airports.entrySet()) {
            String code = entry.getKey();
            if (!code.equals(selectedDestination)) {
                cbPartenza.addItem(code + " (" + entry.getValue() + ")");
            }
        }
        if (selectedDeparture != null && !selectedDeparture.equals(selectedDestination)) {
            cbPartenza.setSelectedItem(selectedDeparture + " (" + airports.get(selectedDeparture) + ")");
        }
    }

    private void updateDestinationOptions() {
        String selectedDeparture = getCodeFromComboBox(cbPartenza);
        String selectedDestination = getCodeFromComboBox(cbDestinazione);

        cbDestinazione.removeAllItems();
        for (Map.Entry<String, String> entry : airports.entrySet()) {
            String code = entry.getKey();
            if (!code.equals(selectedDeparture)) {
                cbDestinazione.addItem(code + " (" + entry.getValue() + ")");
            }
        }
        if (selectedDestination != null && !selectedDestination.equals(selectedDeparture)) {
            cbDestinazione.setSelectedItem(selectedDestination + " (" + airports.get(selectedDestination) + ")");
        }
    }

    private String getCodeFromComboBox(JComboBox<String> comboBox) {
        String selectedItem = (String) comboBox.getSelectedItem();
        if (selectedItem != null) {
            return selectedItem.split(" ")[0];
        }
        return null;
    }

    private JPanel creaPannelloPrenotazioni() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Email Cliente:"), gbc);

        gbc.gridx = 1;
        txtPrenotazioneClienteEmail = new JTextField();
        panel.add(txtPrenotazioneClienteEmail, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("ID Volo:"), gbc);

        gbc.gridx = 1;
        txtPrenotazioneVoloID = new JTextField();
        panel.add(txtPrenotazioneVoloID, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Data Prenotazione (gg/MM/aaaa):"), gbc);

        gbc.gridx = 1;
        txtPrenotazioneData = new JTextField();
        panel.add(txtPrenotazioneData, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Cliente Fedeltà:"), gbc);

        gbc.gridx = 1;
        chkClienteFedelta = new JCheckBox();
        panel.add(chkClienteFedelta, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        JButton btnAddBooking = new JButton("Aggiungi Prenotazione");
        btnAddBooking.addActionListener(e -> aggiungiPrenotazione());
        buttonPanel.add(btnAddBooking);

        JButton btnUpdateBooking = new JButton("Aggiorna Prenotazione");
        btnUpdateBooking.addActionListener(e -> aggiornaPrenotazione());
        buttonPanel.add(btnUpdateBooking);

        JButton btnViewBookings = new JButton("Visualizza Prenotazioni");
        btnViewBookings.addActionListener(e -> visualizzaPrenotazioni());
        buttonPanel.add(btnViewBookings);

        JButton btnDeleteBooking = new JButton("Elimina Prenotazione");
        btnDeleteBooking.addActionListener(e -> eliminaPrenotazione());
        buttonPanel.add(btnDeleteBooking);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private JPanel creaPannelloPromozioni() {
        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("ID Promozione:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtIdPromozione = new JTextField(20);
        txtIdPromozione.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(txtIdPromozione, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Descrizione:"), gbc);

        gbc.gridx = 1;
        gbc.gridwidth = 2;
        txtDescrizione = new JTextField(20);
        txtDescrizione.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(txtDescrizione, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Data Inizio:"), gbc);

        gbc.gridx = 1;
        txtDataInizio = new JTextField(20);
        txtDataInizio.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(txtDataInizio, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Data Fine:"), gbc);

        gbc.gridx = 1;
        txtDataFine = new JTextField(20);
        txtDataFine.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(txtDataFine, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        chkPromovoli = new JCheckBox("Promozione Voli");
        chkPromovoli.addActionListener(e -> {
            txtVoliAssociati.setEnabled(chkPromovoli.isSelected());
        });
        panel.add(chkPromovoli, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        panel.add(new JLabel("Voli Associati:"), gbc);

        gbc.gridx = 1;
        txtVoliAssociati = new JTextField(20);
        txtVoliAssociati.setEnabled(false); // Disabilita inizialmente
        txtVoliAssociati.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        panel.add(txtVoliAssociati, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.WEST;
        chkFedelta = new JCheckBox("Per Clienti Fedeltà");
        panel.add(chkFedelta, gbc);

        row++;

        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.CENTER;

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(2, 2, 10, 10));

        JButton btnAddPromo = new JButton("Aggiungi Promozione");
        btnAddPromo.addActionListener(e -> aggiungiPromozione());
        buttonPanel.add(btnAddPromo);

        JButton btnUpdatePromo = new JButton("Aggiorna Promozione");
        btnUpdatePromo.addActionListener(e -> aggiornaPromozione());
        buttonPanel.add(btnUpdatePromo);

        JButton btnViewPromo = new JButton("Visualizza Promozioni");
        btnViewPromo.addActionListener(e -> visualizzaPromozioni());
        buttonPanel.add(btnViewPromo);

        JButton btnDeletePromo = new JButton("Elimina Promozione");
        btnDeletePromo.addActionListener(e -> eliminaPromozione());
        buttonPanel.add(btnDeletePromo);

        panel.add(buttonPanel, gbc);

        return panel;
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|hotmail|libero)\\.com$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private LocalDate parseDateC(String dateStr) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d/M/yyyy");
        try {
            LocalDate date = LocalDate.parse(dateStr, formatter);
            if (date.isAfter(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "La data selezionata è futura, inserire una data passata.");
                return null;
            }
            return date;
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Inserire una data valida nel formato gg/mm/aaaa.");
            return null;
        }
    }

    private void aggiungiCliente() {
        String email = txtEmail.getText();
        String nome = txtNome.getText();
        String cognome = txtCognome.getText();
        String indirizzo = txtIndirizzo.getText();
        LocalDate dataNascita = parseDateC(txtDataNascita.getText());
        LocalDate dataUltimoAcquisto = null;
        int codice = -1;
        int puntiAccum = 0;

        if (chkFidelity.isSelected()) {
            dataUltimoAcquisto = parseDateC(txtDataUltimoAcquisto.getText());
            if (dataUltimoAcquisto == null) return; // Interrompe se la data è nulla
            try {
                codice = Integer.parseInt(txtCodice.getText());
                puntiAccum = Integer.parseInt(txtPuntiAccum.getText());
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "Formato numero non valido per codice o punti.");
                return;
            }
        }

        if (dataNascita == null) return;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = chkFidelity.isSelected() ?
                    "SELECT COUNT(*) FROM ClienteFedelta WHERE email = ?" :
                    "SELECT COUNT(*) FROM Cliente WHERE email = ?";

            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            rs.next();

            if (rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Cliente già presente nel database, inserirne uno nuovo!");
                return;
            }

            String sql = chkFidelity.isSelected() ?
                    "INSERT INTO ClienteFedelta (email, nome, cognome, indirizzo, data_nascita, codice, punti_accumulati, data_ultimo_acquisto) VALUES (?, ?, ?, ?, ?, ?, ?, ?)" :
                    "INSERT INTO Cliente (email, nome, cognome, indirizzo, data_nascita) VALUES (?, ?, ?, ?, ?)";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            pstmt.setString(2, nome);
            pstmt.setString(3, cognome);
            pstmt.setString(4, indirizzo);
            pstmt.setObject(5, dataNascita);

            if (chkFidelity.isSelected()) {
                pstmt.setInt(6, codice);
                pstmt.setInt(7, puntiAccum);
                pstmt.setObject(8, dataUltimoAcquisto);
            }

            pstmt.executeUpdate();
            JOptionPane.showMessageDialog(this, "Cliente " + (chkFidelity.isSelected() ? "fedeltà" : "normale") + " aggiunto con successo!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiunta del cliente.");
        }
    }

    private void eliminaCliente() {
        String email = txtEmail.getText();
        if (!validateEmail(email)) {
            JOptionPane.showMessageDialog(this, "Impossibile eliminare il cliente, formato email non valido.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM Prenotazione WHERE cliente_email = ?";
            PreparedStatement checkStmt = conn.prepareStatement(checkSql);
            checkStmt.setString(1, email);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                JOptionPane.showMessageDialog(this, "Impossibile eliminare il cliente selezionato in quanto ha effettuato una prenotazione.");
                return;
            }

            String sql;
            if (chkFidelity.isSelected()) {
                sql = "DELETE FROM ClienteFedelta WHERE email = ?";
            } else {
                sql = "DELETE FROM Cliente WHERE email = ?";
            }
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, email);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Cliente eliminato con successo!");
            } else {
                JOptionPane.showMessageDialog(this, "Cliente non trovato.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'eliminazione del cliente.");
        }
    }

    private void aggiornaCliente() {
        String email = txtEmail.getText();
        String nome = txtNome.getText();
        String cognome = txtCognome.getText();
        String indirizzo = txtIndirizzo.getText();

        if (!validateEmail(email)) {
            JOptionPane.showMessageDialog(this, "Formato email non valido.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = chkFidelity.isSelected() ?
                    "UPDATE ClienteFedelta SET nome = ?, cognome = ?, indirizzo = ?, codice = ?, punti_accumulati = ?, data_ultimo_acquisto = ? WHERE email = ?" :
                    "UPDATE Cliente SET nome = ?, cognome = ?, indirizzo = ? WHERE email = ?";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, nome);
            pstmt.setString(2, cognome);
            pstmt.setString(3, indirizzo);

            if (chkFidelity.isSelected()) {
                pstmt.setInt(4, Integer.parseInt(txtCodice.getText()));
                pstmt.setInt(5, Integer.parseInt(txtPuntiAccum.getText()));
                pstmt.setObject(6, LocalDate.parse(txtDataUltimoAcquisto.getText(), DateTimeFormatter.ofPattern("d/M/yyyy")));
                pstmt.setString(7, email);
            } else {
                pstmt.setString(4, email);
            }

            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this, "Cliente " + (chkFidelity.isSelected() ? "fedeltà" : "normale") + " aggiornato con successo!");
            } else {
                JOptionPane.showMessageDialog(this, "Cliente non trovato.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiornamento del cliente.");
        }
    }

    private void visualizzaClienti() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = chkFidelity.isSelected() ?
                    "SELECT * FROM ClienteFedelta" :
                    "SELECT * FROM Cliente";

            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder clienti = new StringBuilder();
            while (rs.next()) {
                clienti.append("Email: ").append(rs.getString("email"))
                        .append(", Nome: ").append(rs.getString("nome"))
                        .append(", Cognome: ").append(rs.getString("cognome"))
                        .append(", Indirizzo: ").append(rs.getString("indirizzo"));
                if (chkFidelity.isSelected()) {
                    clienti.append(", Codice Fid: ").append(rs.getInt("codice"))
                            .append(", Punti: ").append(rs.getInt("punti_accumulati"))
                            .append(", Data Ultimo Acquisto: ").append(rs.getDate("data_ultimo_acquisto"));
                }
                clienti.append("\n");
            }
            JOptionPane.showMessageDialog(this, clienti.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella visualizzazione dei clienti.");
        }
    }

    private void aggiungiVolo() {
        int idVolo = Integer.parseInt(txtVoloID.getText());
        String partenza = getCodeFromComboBox(cbPartenza);
        String destinazione = getCodeFromComboBox(cbDestinazione);
        int disponibilita = Integer.parseInt(txtDisponibilita.getText());
        long numeroSerialeAereo = Long.parseLong(txtNumeroSerialeAereo.getText());
        String modelloAereo = txtModelloAereo.getText();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            LocalDate dataVolo = parseDate(txtDataVolo.getText());
            LocalTime oraVolo = LocalTime.parse(txtOraVolo.getText(), timeFormatter);

            LocalDate oggi = LocalDate.now();
            LocalTime adesso = LocalTime.now();

            if (dataVolo.isBefore(oggi) || (dataVolo.isEqual(oggi) && oraVolo.isBefore(adesso))) {
                JOptionPane.showMessageDialog(this, "La data selezionata è passata, inserire una data futura.");
                return;
            }

            try (Connection conn = DatabaseConnection.getConnection()) {
                String checkAereoSql = "SELECT COUNT(*) FROM Aereo WHERE numeroSeriale = ? AND modello = ?";
                try (PreparedStatement checkAereoStmt = conn.prepareStatement(checkAereoSql)) {
                    checkAereoStmt.setLong(1, numeroSerialeAereo);
                    checkAereoStmt.setString(2, modelloAereo);
                    ResultSet rs = checkAereoStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {

                        String insertAereoSql = "INSERT INTO Aereo (numeroSeriale, modello) VALUES (?, ?)";
                        try (PreparedStatement insertAereoStmt = conn.prepareStatement(insertAereoSql)) {
                            insertAereoStmt.setLong(1, numeroSerialeAereo);
                            insertAereoStmt.setString(2, modelloAereo);
                            insertAereoStmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Nuovo aereo aggiunto alla tabella Aereo.");
                        }
                    }
                }

                String checkVoloSql = "SELECT COUNT(*) FROM Volo WHERE idVolo = ?";
                try (PreparedStatement checkVoloStmt = conn.prepareStatement(checkVoloSql)) {
                    checkVoloStmt.setInt(1, idVolo);
                    ResultSet rs = checkVoloStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(this, "Volo già presente nel database, inserirne uno nuovo!");
                        return;
                    }
                }

                String sql = "INSERT INTO Volo (idVolo, partenza, destinazione, giorno, orario, disponibilita, aereo_numeroSeriale, aereo_modello) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setInt(1, idVolo);
                pstmt.setString(2, partenza);
                pstmt.setString(3, destinazione);
                pstmt.setObject(4, dataVolo);
                pstmt.setObject(5, oraVolo);
                pstmt.setInt(6, disponibilita);
                pstmt.setLong(7, numeroSerialeAereo);
                pstmt.setString(8, modelloAereo);
                pstmt.executeUpdate();
                JOptionPane.showMessageDialog(this, "Volo aggiunto con successo!");

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore nell'aggiunta del volo.");
            }
        } catch (DateTimeParseException e) {
            JOptionPane.showMessageDialog(this, "Formato data o ora non valido. Usa il formato gg/MM/aaaa per la data e HH:mm per l'ora.");
        }
    }

    private void aggiornaVolo() {
        int idVolo = Integer.parseInt(txtVoloID.getText());
        String partenza = getCodeFromComboBox(cbPartenza);
        String destinazione = getCodeFromComboBox(cbDestinazione);
        int disponibilita = Integer.parseInt(txtDisponibilita.getText());
        long numeroSerialeAereo = Long.parseLong(txtNumeroSerialeAereo.getText());
        String modelloAereo = txtModelloAereo.getText();

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        try {
            LocalDate dataVolo = parseDate(txtDataVolo.getText());
            LocalTime oraVolo = LocalTime.parse(txtOraVolo.getText(), timeFormatter);

            if (dataVolo == null) return;

            try (Connection conn = DatabaseConnection.getConnection()) {

                String checkAereoSql = "SELECT COUNT(*) FROM Aereo WHERE numeroSeriale = ? AND modello = ?";
                try (PreparedStatement checkAereoStmt = conn.prepareStatement(checkAereoSql)) {
                    checkAereoStmt.setLong(1, numeroSerialeAereo);
                    checkAereoStmt.setString(2, modelloAereo);
                    ResultSet rs = checkAereoStmt.executeQuery();
                    if (rs.next() && rs.getInt(1) == 0) {

                        String insertAereoSql = "INSERT INTO Aereo (numeroSeriale, modello) VALUES (?, ?)";
                        try (PreparedStatement insertAereoStmt = conn.prepareStatement(insertAereoSql)) {
                            insertAereoStmt.setLong(1, numeroSerialeAereo);
                            insertAereoStmt.setString(2, modelloAereo);
                            insertAereoStmt.executeUpdate();
                            JOptionPane.showMessageDialog(this, "Nuovo aereo aggiunto alla tabella Aereo.");
                        }
                    }
                }

                String sql = "UPDATE Volo SET partenza = ?, destinazione = ?, giorno = ?, orario = ?, disponibilita = ?, aereo_numeroSeriale = ?, aereo_modello = ? WHERE idVolo = ?";
                PreparedStatement pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, partenza);
                pstmt.setString(2, destinazione);
                pstmt.setObject(3, dataVolo);
                pstmt.setObject(4, oraVolo);
                pstmt.setInt(5, disponibilita);
                pstmt.setLong(6, numeroSerialeAereo);
                pstmt.setString(7, modelloAereo);
                pstmt.setInt(8, idVolo);
                int rowsAffected = pstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Volo aggiornato con successo.");
                } else {
                    JOptionPane.showMessageDialog(this, "Nessun volo trovato con l'ID specificato.");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Errore durante l'aggiornamento del volo: " + e.getMessage());
            }
        } catch (DateTimeParseException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Formato data o ora non valido.");
        }
    }


    private void eliminaVolo() {
        int idVolo = Integer.parseInt(txtVoloID.getText());
        try (Connection conn = DatabaseConnection.getConnection()) {

            String checkVoloSql = "SELECT COUNT(*) FROM Volo WHERE idVolo = ?";
            try (PreparedStatement checkVoloStmt = conn.prepareStatement(checkVoloSql)) {
                checkVoloStmt.setInt(1, idVolo);
                ResultSet rs = checkVoloStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    String checkPrenotazioneSql = "SELECT COUNT(*) FROM Prenotazione WHERE volo_id = ?";
                    try (PreparedStatement checkPrenotazioneStmt = conn.prepareStatement(checkPrenotazioneSql)) {
                        checkPrenotazioneStmt.setInt(1, idVolo);
                        ResultSet rsPrenotazione = checkPrenotazioneStmt.executeQuery();
                        if (rsPrenotazione.next() && rsPrenotazione.getInt(1) > 0) {
                            JOptionPane.showMessageDialog(this, "Impossibile eliminare il volo selezionato in quanto presente in una prenotazione.");
                        } else {
                            String deleteVoloSql = "DELETE FROM Volo WHERE idVolo = ?";
                            try (PreparedStatement deleteVoloStmt = conn.prepareStatement(deleteVoloSql)) {
                                deleteVoloStmt.setInt(1, idVolo);
                                deleteVoloStmt.executeUpdate();
                                JOptionPane.showMessageDialog(this, "Volo eliminato con successo!");
                            }
                        }
                        rsPrenotazione.close();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Il volo da te selezionato è inesistente.");
                }
                rs.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'eliminazione del volo.");
        }
    }


    private void visualizzaVoli() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Volo";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();
            StringBuilder voli = new StringBuilder();
            while (rs.next()) {
                voli.append("ID Volo: ").append(rs.getInt("idVolo"))
                        .append(", Partenza: ").append(rs.getString("partenza"))
                        .append(", Destinazione: ").append(rs.getString("destinazione"))
                        .append(", Giorno: ").append(rs.getObject("giorno"))
                        .append(", Orario: ").append(rs.getObject("orario"))
                        .append(", Disponibilità: ").append(rs.getInt("disponibilita"))
                        .append(", Numero Serial: ").append(rs.getLong("aereo_numeroSeriale"))
                        .append(", Modello Aereo: ").append(rs.getString("aereo_modello"))
                        .append("\n");
            }
            JOptionPane.showMessageDialog(this, voli.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella visualizzazione dei voli.");
        }
    }

    private LocalDate parseDate(String dateString) throws DateTimeParseException {
        Pattern pattern = Pattern.compile("(\\d{1,2})/(\\d{1,2})/(\\d{4})");
        Matcher matcher = pattern.matcher(dateString);
        if (matcher.matches()) {
            int day = Integer.parseInt(matcher.group(1));
            int month = Integer.parseInt(matcher.group(2));
            int year = Integer.parseInt(matcher.group(3));
            return LocalDate.of(year, month, day);
        } else {
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            return LocalDate.parse(dateString, dateFormatter);
        }
    }

    private void aggiungiPrenotazione() {
        String clienteEmail = txtPrenotazioneClienteEmail.getText().trim();
        String voloID = txtPrenotazioneVoloID.getText().trim();
        String dataInput = txtPrenotazioneData.getText().trim();
        boolean isFedelta = chkClienteFedelta.isSelected();

        if (clienteEmail.isEmpty() || voloID.isEmpty() || dataInput.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di aggiungere una prenotazione.");
            return;
        }

        if ((isFedelta && !isClienteFedeltaRegistrato(clienteEmail)) || (!isFedelta && !isClienteRegistrato(clienteEmail))) {
            String msg = isFedelta ? "Per effettuare correttamente la prenotazione, inserire la mail di un cliente fedeltà esistente" : "Per effettuare correttamente la prenotazione, inserire la mail di un cliente esistente";
            JOptionPane.showMessageDialog(this, msg);
            return;
        }

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/MM/yyyy")
        };

        LocalDate dataPrenotazione = null;
        for (DateTimeFormatter formatter : formatters) {
            try {
                dataPrenotazione = LocalDate.parse(dataInput, formatter);
                break;
            } catch (DateTimeParseException ignored) {
            }
        }

        if (dataPrenotazione == null) {
            JOptionPane.showMessageDialog(this, "Formato data non valido. Usa il formato gg/MM/aaaa.");
            return;
        }

        if (!dataPrenotazione.equals(LocalDate.now())) {
            JOptionPane.showMessageDialog(this, "La data selezionata non è quella di oggi. Inserirla.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkVoloSql = "SELECT COUNT(*) FROM Volo WHERE idVolo = ?";
            try (PreparedStatement checkVoloPstmt = conn.prepareStatement(checkVoloSql)) {
                checkVoloPstmt.setString(1, voloID);
                ResultSet rs = checkVoloPstmt.executeQuery();
                if (rs.next() && rs.getInt(1) == 0) {
                    JOptionPane.showMessageDialog(this, "Per effettuare correttamente la prenotazione specificare l'id di un volo esistente.");
                    return;
                }
            }

            String checkPrenotazioneSql = isFedelta ?
                    "SELECT COUNT(*) FROM PrenotazioneFidelity WHERE cliente_email = ? AND volo_id = ?" :
                    "SELECT COUNT(*) FROM Prenotazione WHERE cliente_email = ? AND volo_id = ?";

            try (PreparedStatement checkPrenotazionePstmt = conn.prepareStatement(checkPrenotazioneSql)) {
                checkPrenotazionePstmt.setString(1, clienteEmail);
                checkPrenotazionePstmt.setString(2, voloID);
                ResultSet rs = checkPrenotazionePstmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    JOptionPane.showMessageDialog(this, "Prenotazione già presente nel database.");
                    return;
                }
            }

            String insertPrenotazioneSql = isFedelta ?
                    "INSERT INTO PrenotazioneFidelity (cliente_email, volo_id, data_prenotazione) VALUES (?, ?, ?)" :
                    "INSERT INTO Prenotazione (cliente_email, volo_id, data_prenotazione) VALUES (?, ?, ?)";

            try (PreparedStatement pstmt = conn.prepareStatement(insertPrenotazioneSql)) {
                pstmt.setString(1, clienteEmail);
                pstmt.setString(2, voloID);
                pstmt.setObject(3, dataPrenotazione);
                pstmt.executeUpdate();
            }

            TicketManagementDialog dialog = new TicketManagementDialog(this, clienteEmail, voloID, isFedelta);
            dialog.setVisible(true);

            if (dialog.BigliettiAggiunti()==0) {
                eliminaPrenotazioneBis(clienteEmail, voloID, isFedelta);
                JOptionPane.showMessageDialog(this, "Prenotazione non effettuata in quanto non è stato specificato alcun biglietto.");
            } else {
                JOptionPane.showMessageDialog(this, "Prenotazione aggiunta con successo!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiunta della prenotazione.");
        }
    }

    private void aggiornaPrenotazione() {
        String clienteEmail = txtPrenotazioneClienteEmail.getText().trim();
        String voloID = txtPrenotazioneVoloID.getText().trim();
        boolean isFedelta = chkClienteFedelta.isSelected();

        if (clienteEmail.isEmpty() || voloID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di aggiornare una prenotazione.");
            return;
        }

        if ((isFedelta && !isClienteFedeltaRegistrato(clienteEmail)) || (!isFedelta && !isClienteRegistrato(clienteEmail))) {
            String msg = isFedelta ? "Per effettuare correttamente la prenotazione, inserire la mail di un cliente fedeltà esistente" : "Per effettuare correttamente la prenotazione, inserire la mail di un cliente esistente";
            JOptionPane.showMessageDialog(this, msg);
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String checkSql = isFedelta ?
                    "SELECT COUNT(*) FROM PrenotazioneFidelity WHERE cliente_email = ? AND volo_id = ?" :
                    "SELECT COUNT(*) FROM Prenotazione WHERE cliente_email = ? AND volo_id = ?";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql)) {
                checkStmt.setString(1, clienteEmail);
                checkStmt.setString(2, voloID);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    TicketManagementDialog dialog = new TicketManagementDialog(this, clienteEmail, voloID, isFedelta);
                    dialog.setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(this, "Prenotazione non trovata.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella verifica della prenotazione.");
        }
    }

    private boolean isClienteRegistrato(String clienteEmail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Cliente WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, clienteEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella verifica dell'email del cliente.");
        }
        return false;
    }

    private boolean isClienteFedeltaRegistrato(String clienteEmail) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM ClienteFedelta WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                pstmt.setString(1, clienteEmail);
                ResultSet rs = pstmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella verifica dell'email del cliente fedeltà.");
        }
        return false;
    }

    private void eliminaPrenotazione() {
        String clienteEmail = txtPrenotazioneClienteEmail.getText().trim();
        String voloID = txtPrenotazioneVoloID.getText().trim();
        boolean isFidelity = chkClienteFedelta.isSelected(); // Checkbox per selezionare il tipo di biglietto

        if (clienteEmail.isEmpty() || voloID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di eliminare una prenotazione.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String deleteTicketsSql;
            if (isFidelity) {
                deleteTicketsSql = "DELETE FROM BigliettoFidelity WHERE cliente_fedelta_id = ? AND volo_id = ?";
            } else {
                deleteTicketsSql = "DELETE FROM Biglietto WHERE cliente_email = ? AND volo_id = ?";
            }

            try (PreparedStatement deleteTicketsPstmt = conn.prepareStatement(deleteTicketsSql)) {
                deleteTicketsPstmt.setString(1, clienteEmail);
                deleteTicketsPstmt.setString(2, voloID);
                deleteTicketsPstmt.executeUpdate();
            }

            String deletePrenotazioneSql;
            if (isFidelity) {
                deletePrenotazioneSql = "DELETE FROM PrenotazioneFidelity WHERE cliente_email = ? AND volo_id = ?";
            } else {
                deletePrenotazioneSql = "DELETE FROM Prenotazione WHERE cliente_email = ? AND volo_id = ?";
            }

            try (PreparedStatement deletePrenotazionePstmt = conn.prepareStatement(deletePrenotazioneSql)) {
                deletePrenotazionePstmt.setString(1, clienteEmail);
                deletePrenotazionePstmt.setString(2, voloID);
                int rowsAffected = deletePrenotazionePstmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this, "Prenotazione e biglietti eliminati con successo!");
                } else {
                    JOptionPane.showMessageDialog(this, "La prenotazione da te selezionata è inesistente.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'eliminazione della prenotazione.");
        }
    }

    private void eliminaPrenotazioneBis(String clienteEmail, String voloID, boolean isFidelity) {
        String deletePrenotazioneSql = isFidelity ?
                "DELETE FROM PrenotazioneFidelity WHERE cliente_email = ? AND volo_id = ?" :
                "DELETE FROM Prenotazione WHERE cliente_email = ? AND volo_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement deletePrenotazionePstmt = conn.prepareStatement(deletePrenotazioneSql)) {
            deletePrenotazionePstmt.setString(1, clienteEmail);
            deletePrenotazionePstmt.setString(2, voloID);
            deletePrenotazionePstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void visualizzaPrenotazioni() {
        boolean isFidelity = chkClienteFedelta.isSelected();

        String sql;
        if (isFidelity) {
            sql = "SELECT cliente_email, volo_id, data_prenotazione FROM PrenotazioneFidelity";
        } else {
            sql = "SELECT cliente_email, volo_id, data_prenotazione FROM Prenotazione";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            StringBuilder prenotazioni = new StringBuilder();
            while (rs.next()) {
                prenotazioni.append("Cliente Email: ").append(rs.getString("cliente_email"))
                        .append(", Volo ID: ").append(rs.getString("volo_id"))
                        .append(", Data Prenotazione: ").append(rs.getDate("data_prenotazione"))
                        .append("\n");
            }
            JOptionPane.showMessageDialog(this, prenotazioni.toString());
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella visualizzazione delle prenotazioni.");
        }
    }

    private boolean isIdPromozioneEsistente(int id) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT COUNT(*) FROM Promozione WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


    private void aggiungiPromozione() {
        String idStr = txtIdPromozione.getText().trim();
        int id = -1;
        try {
            id = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "ID non valido.");
            return;
        }

        if (isIdPromozioneEsistente(id)) {
            JOptionPane.showMessageDialog(this, "ID già esistente. Inserire un ID unico.");
            return;
        }

        String descrizione = txtDescrizione.getText().trim();
        LocalDate dataInizio = null;
        LocalDate dataFine = null;
        boolean perClientiFedelta = chkFedelta.isSelected();

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy")
        };

        String dataInizioStr = txtDataInizio.getText().trim();
        String dataFineStr = txtDataFine.getText().trim();

        try {
            for (DateTimeFormatter formatter : formatters) {
                try {
                    dataInizio = LocalDate.parse(dataInizioStr, formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }
            if (dataInizio == null) {
                JOptionPane.showMessageDialog(this, "Inserire una data di inizio promozione valida.");
                return;
            }

            for (DateTimeFormatter formatter : formatters) {
                try {
                    dataFine = LocalDate.parse(dataFineStr, formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }
            if (dataFine == null) {
                JOptionPane.showMessageDialog(this, "Inserire una data di fine promozione valida.");
                return;
            }

            if (!dataInizio.equals(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "La data selezionata non è quella di oggi. Inserirla.");
                return;
            }

            if (dataFine.isBefore(dataInizio)) {
                JOptionPane.showMessageDialog(this, "La data di fine deve essere successiva alla data di inizio.");
                return;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Errore nel formato delle date.");
            return;
        }

        Map<String, Object> risultatiVoli = parseVoliAssociati(txtVoliAssociati.getText().trim());
        List<Volo> voliAssociati = (List<Volo>) risultatiVoli.get("validi");
        List<Integer> voliInvalidi = (List<Integer>) risultatiVoli.get("invalidi");

        if (chkPromovoli.isSelected()) {
            if (!voliInvalidi.isEmpty()) {
                String message = "I seguenti voli non esistono: " + voliInvalidi.toString();
                JOptionPane.showMessageDialog(this, message);
                return;
            }
            if (voliAssociati.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Nessun volo specificato.");
                return;
            }

        }

        PromozioneAbstract promozione;
        if (chkPromovoli.isSelected()) {
            promozione = new PromozioneVoli(descrizione, dataInizio, dataFine, perClientiFedelta, voliAssociati);
        } else {
            promozione = new PromozionePeriodo(descrizione, dataInizio, dataFine, perClientiFedelta);
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            String sqlPromozione = "INSERT INTO Promozione (id, descrizione, dataInizio, dataFine, perClientiFedelta, tipo) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlPromozione)) {
                pstmt.setInt(1, id);
                pstmt.setString(2, promozione.getDescrizione());
                pstmt.setObject(3, promozione.getDataInizio());
                pstmt.setObject(4, promozione.getDataFine());
                pstmt.setBoolean(5, promozione.isPerClientiFedelta());
                pstmt.setString(6, voliAssociati.isEmpty() ? "Periodo" : "Voli");
                pstmt.executeUpdate();
            }

            if (chkPromovoli.isSelected() && !voliAssociati.isEmpty()) {
                String sqlPromozioneVoli = "INSERT INTO PromozioneVoli (promozione_id, volo_id) VALUES (?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPromozioneVoli)) {
                    for (Volo volo : voliAssociati) {
                        pstmt.setInt(1, id);
                        pstmt.setInt(2, volo.getIdVolo());
                        pstmt.addBatch();
                    }
                    pstmt.executeBatch();
                }
            }

            if (!chkPromovoli.isSelected()) {
                String sqlPromozionePeriodo = "INSERT INTO PromozionePeriodo (id, descrizione, dataInizio, dataFine, perClientiFedelta) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlPromozionePeriodo)) {
                    pstmt.setInt(1, id);
                    pstmt.setString(2, promozione.getDescrizione());
                    pstmt.setObject(3, dataInizio);
                    pstmt.setObject(4, dataFine);
                    pstmt.setBoolean(5, promozione.isPerClientiFedelta());
                    pstmt.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Promozione aggiunta con successo!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiunta della promozione.");
        }
    }

    private Map<String, Object> parseVoliAssociati(String voliAssociatiStr) {
        List<Volo> voliValidi = new ArrayList<>();
        List<Integer> voliInvalidi = new ArrayList<>();

        if (voliAssociatiStr.isEmpty()) {
            return Map.of("validi", voliValidi, "invalidi", voliInvalidi);
        }

        String[] voloIds = voliAssociatiStr.split(",");
        for (String voloIdStr : voloIds) {
            try {
                int voloId = Integer.parseInt(voloIdStr.trim());
                Volo volo = trovaVoloById(voloId);
                if (volo != null) {
                    voliValidi.add(volo);
                } else {
                    voliInvalidi.add(voloId);
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "ID volo non valido: " + voloIdStr);
            }
        }

        return Map.of("validi", voliValidi, "invalidi", voliInvalidi);
    }


    private Volo trovaVoloById(int idVolo) {
        Volo volo = null;
        Aereo aereo;

        try (Connection conn = DatabaseConnection.getConnection()) {
            // Recupera i dettagli del volo
            String sqlVolo = "SELECT * FROM Volo WHERE idVolo = ?";
            PreparedStatement pstmtVolo = conn.prepareStatement(sqlVolo);
            pstmtVolo.setInt(1, idVolo);

            ResultSet rsVolo = pstmtVolo.executeQuery();
            if (rsVolo.next()) {
                int id = rsVolo.getInt("idVolo");
                String partenza = rsVolo.getString("partenza");
                String destinazione = rsVolo.getString("destinazione");
                LocalTime orario = rsVolo.getTime("orario").toLocalTime();
                LocalDate giorno = rsVolo.getDate("giorno").toLocalDate();

                long numeroSeriale = rsVolo.getLong("aereo_numeroSeriale");
                String modello = rsVolo.getString("aereo_modello");

                aereo = findAereoByNumeroSerialeAndModello(numeroSeriale, modello);

                if (aereo != null) {
                    volo = new Volo(id, partenza, destinazione, orario, giorno, aereo);
                } else {
                    System.err.println("Aereo non trovato per il volo ID " + idVolo);
                }
            } else {
                System.err.println("Volo con ID " + idVolo + " non trovato.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore durante la ricerca del volo: " + e.getMessage());
        }

        return volo;
    }

    private Aereo findAereoByNumeroSerialeAndModello(long numeroSeriale, String modello) {
        Aereo aereo = null;

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlAereo = "SELECT * FROM Aereo WHERE numeroSeriale = ? AND modello = ?";
            PreparedStatement pstmtAereo = conn.prepareStatement(sqlAereo);
            pstmtAereo.setLong(1, numeroSeriale);
            pstmtAereo.setString(2, modello);

            ResultSet rsAereo = pstmtAereo.executeQuery();
            if (rsAereo.next()) {
                Aereo.TipoVeicolo tipoVeicolo = Aereo.TipoVeicolo.valueOf(rsAereo.getString("tipo"));
                aereo = new Aereo(numeroSeriale, modello, tipoVeicolo);
            } else {
                System.err.println("Aereo con numero di seriale " + numeroSeriale + " e modello " + modello + " non trovato.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore durante la ricerca dell'aereo: " + e.getMessage());
        }

        return aereo;
    }


    private void aggiornaPromozione() {
        String idStr = txtIdPromozione.getText().trim();
        int promozioneId;

        try {
            promozioneId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserire un ID valido.");
            return;
        }

        String descrizione = txtDescrizione.getText().trim();
        LocalDate dataInizio = null;
        LocalDate dataFine = null;
        boolean perClientiFedelta = chkFedelta.isSelected();
        boolean isPromovoli = chkPromovoli.isSelected();
        Map<String, Object> risultatiVoli = parseVoliAssociati(txtVoliAssociati.getText().trim());
        List<Volo> voliAssociati = (List<Volo>) risultatiVoli.get("validi");
        List<Integer> voliInvalidi = (List<Integer>) risultatiVoli.get("invalidi");


        if (!voliInvalidi.isEmpty()) {
            JOptionPane.showMessageDialog(this, "I seguenti voli non esistono: " + voliInvalidi.toString());
            return;
        }

        if (isPromovoli && voliAssociati.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nessun volo specificato.");
            return;
        }

        DateTimeFormatter[] formatters = {
                DateTimeFormatter.ofPattern("dd/MM/yyyy"),
                DateTimeFormatter.ofPattern("d/M/yyyy"),
                DateTimeFormatter.ofPattern("dd/M/yyyy"),
                DateTimeFormatter.ofPattern("d/MM/yyyy")
        };

        String dataInizioStr = txtDataInizio.getText().trim();
        String dataFineStr = txtDataFine.getText().trim();

        try {
            for (DateTimeFormatter formatter : formatters) {
                try {
                    dataInizio = LocalDate.parse(dataInizioStr, formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }
            if (dataInizio == null || !dataInizio.equals(LocalDate.now())) {
                JOptionPane.showMessageDialog(this, "La data di inizio deve essere uguale a quella odierna.");
                return;
            }

            for (DateTimeFormatter formatter : formatters) {
                try {
                    dataFine = LocalDate.parse(dataFineStr, formatter);
                    break;
                } catch (DateTimeParseException ignored) {}
            }
            if (dataFine == null || !dataFine.isAfter(dataInizio)) {
                JOptionPane.showMessageDialog(this, "La data di fine deve essere futura rispetto alla data di inizio.");
                return;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Formato della data non valido.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {

            // Aggiorna i dati generali della promozione
            String sqlUpdatePromozione = "UPDATE Promozione SET descrizione = ?, dataInizio = ?, dataFine = ?, perClientiFedelta = ?, tipo = ? WHERE id = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdatePromozione)) {
                pstmt.setString(1, descrizione);
                pstmt.setObject(2, dataInizio);
                pstmt.setObject(3, dataFine);
                pstmt.setBoolean(4, perClientiFedelta);
                pstmt.setString(5, isPromovoli ? "Voli" : "Periodo");
                pstmt.setInt(6, promozioneId);
                pstmt.executeUpdate();
            }

            JOptionPane.showMessageDialog(this, "Promozione aggiornata con successo!");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore durante l'aggiornamento della promozione: " + e.getMessage());
        }
    }

    private void eliminaPromozione() {
        String idStr = txtIdPromozione.getText().trim();
        int promozioneId;

        try {
            promozioneId = Integer.parseInt(idStr);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserire un ID valido.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "DELETE FROM Promozione WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, promozioneId);

            int rowsDeleted = pstmt.executeUpdate();
            if (rowsDeleted > 0) {
                JOptionPane.showMessageDialog(this, "Promozione eliminata con successo.");
            } else {
                JOptionPane.showMessageDialog(this, "Nessuna promozione trovata con l'ID specificato.");
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore durante l'eliminazione della promozione: " + e.getMessage());
        }
    }

    private void visualizzaPromozioni() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String sql = "SELECT * FROM Promozione";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            ResultSet rs = pstmt.executeQuery();

            StringBuilder sb = new StringBuilder();
            sb.append("ID\tDescrizione\tData Inizio\tData Fine\tFedeltà\tTipo\n");
            sb.append("-----------------------------------------------------------\n");

            while (rs.next()) {
                int id = rs.getInt("id");
                String descrizione = rs.getString("descrizione");
                LocalDate dataInizio = rs.getObject("dataInizio", LocalDate.class);
                LocalDate dataFine = rs.getObject("dataFine", LocalDate.class);
                boolean perClientiFedelta = rs.getBoolean("perClientiFedelta");
                String tipo = rs.getString("tipo");

                sb.append(String.format("%d\t%s\t%s\t%s\t%s\t%s\n", id, descrizione, dataInizio, dataFine, perClientiFedelta ? "Sì" : "No", tipo));
            }

            JTextArea textArea = new JTextArea(sb.toString());
            textArea.setEditable(false);
            JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "Elenco Promozioni", JOptionPane.INFORMATION_MESSAGE);
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Errore durante la visualizzazione delle promozioni: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginGUI gui = new LoginGUI();
            gui.setVisible(true);
        });
    }
}


