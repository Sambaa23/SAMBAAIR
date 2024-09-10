package Updatable.GUIClient;

import Updatable.Aereo.Aereo;
import Updatable.Biglietto.Biglietto;
import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Cliente.Cliente;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Prenotazione.Prenotazione;
import Updatable.Prenotazione.PrenotazioneFidelity;
import Updatable.Volo.Volo;
import Updatable.conf.DatabaseConnection;
import server.NotificheServer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginGUIClient extends JFrame {
    private JTextField txtEmail;
    private JButton btnAccedi;
    private JButton btnRegistrati;
    private boolean isFidelityClient;

    public LoginGUIClient() {
        this("", false);
    }

    public LoginGUIClient(String email, boolean isFidelityClient) {
        this.isFidelityClient = isFidelityClient;
        setTitle("Login Cliente");
        setSize(300, 150);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(3, 2));

        JLabel lblEmail = new JLabel("Email:");
        txtEmail = new JTextField(20);
        txtEmail.setText(email);

        btnAccedi = new JButton("Accedi");
        btnRegistrati = new JButton("Registrati");

        add(lblEmail);
        add(txtEmail);
        add(btnAccedi);
        add(btnRegistrati);

        btnAccedi.addActionListener(this::handleLogin);

        btnRegistrati.addActionListener(e -> {
            if (validateEmail(txtEmail.getText())) {
                new RegistrazioneGUIClient(txtEmail.getText()).setVisible(true);
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Email non valida");
            }
        });

        setLocationRelativeTo(null);
    }

    private boolean validateEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@(gmail|hotmail|libero)\\.com$";
        Pattern pattern = Pattern.compile(emailRegex);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void handleLogin(ActionEvent e) {
        String email = txtEmail.getText().trim();
        if (email.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserisci la tua email.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            String sqlFidelity = "SELECT * FROM ClienteFedelta WHERE email = ?";
            try (PreparedStatement pstmt = conn.prepareStatement(sqlFidelity)) {
                pstmt.setString(1, email);
                ResultSet rsFidelity = pstmt.executeQuery();

                if (rsFidelity.next()) {
                    ClienteFedelta clienteFidelity = recuperaClienteFedeltaDaResultSet(rsFidelity);

                    if (haPrenotazioni(clienteFidelity.getEmail(), conn, "PrenotazioneFidelity")) {
                        clienteFidelity.setPrenotazioni(recuperaPrenotazioniFidCliente(clienteFidelity, conn));
                    }

                    int port = 50051;
                    NotificheServer server = new NotificheServer(port);

                    new Thread(() -> {
                        try {
                            server.start();
                        } catch (IOException | InterruptedException ex) {
                            ex.printStackTrace();
                        }
                    }).start();

                    Thread.sleep(1000);
                    server.registraCliente(email, true);

                    SwingUtilities.invokeLater(() -> new GUIVoliFidelity(clienteFidelity, server).setVisible(true));
                    dispose();

                } else {
                    String sqlCliente = "SELECT * FROM Cliente WHERE email = ?";
                    try (PreparedStatement pstmtCliente = conn.prepareStatement(sqlCliente)) {
                        pstmtCliente.setString(1, email);
                        ResultSet rsCliente = pstmtCliente.executeQuery();

                        if (rsCliente.next()) {
                            Cliente clienteNormale = recuperaClienteDaResultSet(rsCliente);

                            if (haPrenotazioni(clienteNormale.getEmail(), conn, "Prenotazione")) {
                                clienteNormale.setPrenotazioni(recuperaPrenotazioniCliente(clienteNormale, conn));
                            }

                            int port = 50051;
                            NotificheServer server = new NotificheServer(port);

                            new Thread(() -> {
                                try {
                                    server.start();
                                } catch (IOException | InterruptedException ex) {
                                    ex.printStackTrace();
                                }
                            }).start();

                            Thread.sleep(1000);
                            server.registraCliente(email, false);

                            SwingUtilities.invokeLater(() -> new GUIVoliNormale(clienteNormale, server).setVisible(true));
                            dispose();

                        } else {
                            JOptionPane.showMessageDialog(this, "Registrati per non perderti nessun volo su SambaAir!");
                        }
                    }
                }
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Errore nella connessione al database.");
            ex.printStackTrace();
        }
    }

    private boolean haPrenotazioni(String email, Connection conn, String tableName) throws SQLException {
        String query = "SELECT COUNT(*) FROM " + tableName + " WHERE cliente_email = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, email);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        }
        return false;
    }

    private ClienteFedelta recuperaClienteFedeltaDaResultSet(ResultSet rs) throws SQLException {
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String indirizzo = rs.getString("indirizzo");
        LocalDate dataNascita = rs.getDate("data_nascita").toLocalDate();
        int puntiAccumulati = rs.getInt("punti_accumulati");
        LocalDate dataUltimoAcquisto = rs.getDate("data_ultimo_acquisto") != null ? rs.getDate("data_ultimo_acquisto").toLocalDate() : null;

        ClienteFedelta cliente = new ClienteFedelta(rs.getString("email"), nome, cognome, indirizzo, dataNascita);
        cliente.setPuntiAccumulati(puntiAccumulati);
        cliente.setDataUltimoAcquisto(dataUltimoAcquisto);

        return cliente;
    }

    private Cliente recuperaClienteDaResultSet(ResultSet rs) throws SQLException {
        String nome = rs.getString("nome");
        String cognome = rs.getString("cognome");
        String indirizzo = rs.getString("indirizzo");
        LocalDate dataNascita = rs.getDate("data_nascita").toLocalDate();
        return new Cliente(rs.getString("email"), nome, cognome, indirizzo, dataNascita);
    }

    private LinkedList<PrenotazioneFidelity> recuperaPrenotazioniFidCliente(ClienteFedelta cliente, Connection conn) throws SQLException {
        String sqlPrenotazioniFidelity = "SELECT * FROM PrenotazioneFidelity WHERE cliente_email = ?";
        try (PreparedStatement pstmtPrenotazioni = conn.prepareStatement(sqlPrenotazioniFidelity)) {
            pstmtPrenotazioni.setString(1, cliente.getEmail());
            ResultSet rsPrenotazioni = pstmtPrenotazioni.executeQuery();

            LinkedList<PrenotazioneFidelity> prenotazioniFidelity = new LinkedList<>();
            while (rsPrenotazioni.next()) {
                int idVolo = rsPrenotazioni.getInt("volo_id");
                Volo volo = recuperaVoloDaResultSet(idVolo);

                LocalDate dataPrenotazione = rsPrenotazioni.getDate("data_prenotazione").toLocalDate();

                PrenotazioneFidelity prenotazioneFidelity = new PrenotazioneFidelity(cliente, volo, dataPrenotazione);

                prenotazioneFidelity.setBiglietti(recuperaBigliettiFidelityPrenotazione(prenotazioneFidelity, conn));

                prenotazioniFidelity.add(prenotazioneFidelity);
            }

            return prenotazioniFidelity;
        }
    }

    private LinkedList<BigliettoFidelity> recuperaBigliettiFidelityPrenotazione(PrenotazioneFidelity prenotazioneFidelity, Connection conn) throws SQLException {
        String sqlBigliettiFidelity = "SELECT * FROM BigliettoFidelity WHERE volo_id = ? AND cliente_fedelta_id = ?";
        try (PreparedStatement pstmtBiglietti = conn.prepareStatement(sqlBigliettiFidelity)) {
            pstmtBiglietti.setInt(1, prenotazioneFidelity.getVolo().getIdVolo());
            pstmtBiglietti.setString(2, prenotazioneFidelity.getCliente().getEmail());
            ResultSet rsBiglietti = pstmtBiglietti.executeQuery();

            LinkedList<BigliettoFidelity> bigliettiFidelity = new LinkedList<>();
            while (rsBiglietti.next()) {
                String nomeBeneficiario = rsBiglietti.getString("nome_beneficiario");
                String cognomeBeneficiario = rsBiglietti.getString("cognome_beneficiario");
                double prezzo = rsBiglietti.getDouble("prezzo");
                int punti = rsBiglietti.getInt("punti");
                String tariffa = rsBiglietti.getString("tariffa");
                boolean pagato = rsBiglietti.getBoolean("pagato");

                BigliettoFidelity bigliettoFidelity = new BigliettoFidelity(
                        prenotazioneFidelity.getCliente(),
                        nomeBeneficiario,
                        cognomeBeneficiario,
                        prenotazioneFidelity.getVolo(),
                        prezzo,
                        tariffa
                );
                bigliettoFidelity.setPunti(punti);
                bigliettoFidelity.setPagato(pagato);

                bigliettiFidelity.add(bigliettoFidelity);
            }

            return bigliettiFidelity;
        }
    }


    private LinkedList<Prenotazione> recuperaPrenotazioniCliente(Cliente cliente, Connection conn) throws SQLException {
        String sqlPrenotazioni = "SELECT * FROM Prenotazione WHERE cliente_email = ?";
        try (PreparedStatement pstmtPrenotazioni = conn.prepareStatement(sqlPrenotazioni)) {
            pstmtPrenotazioni.setString(1, cliente.getEmail());
            ResultSet rsPrenotazioni = pstmtPrenotazioni.executeQuery();

            LinkedList<Prenotazione> prenotazioni = new LinkedList<>();
            while (rsPrenotazioni.next()) {
                int idVolo = rsPrenotazioni.getInt("volo_id");
                Volo volo = recuperaVoloDaResultSet(idVolo);

                LocalDate dataPrenotazione = rsPrenotazioni.getDate("data_prenotazione").toLocalDate();

                Prenotazione prenotazione = new Prenotazione(cliente, volo, dataPrenotazione);

                prenotazione.setBiglietti(recuperaBigliettiPerPrenotazione(prenotazione, conn));

                prenotazioni.add(prenotazione);
            }

            return prenotazioni;
        }
    }

    private LinkedList<Biglietto> recuperaBigliettiPerPrenotazione(Prenotazione prenotazione, Connection conn) throws SQLException {
        String sqlBiglietti = "SELECT * FROM Biglietto WHERE volo_id = ? AND cliente_email = ?";
        try (PreparedStatement pstmtBiglietti = conn.prepareStatement(sqlBiglietti)) {
            pstmtBiglietti.setInt(1, prenotazione.getVolo().getIdVolo());
            pstmtBiglietti.setString(2, prenotazione.getCliente().getEmail());
            ResultSet rsBiglietti = pstmtBiglietti.executeQuery();

            LinkedList<Biglietto> biglietti = new LinkedList<>();
            while (rsBiglietti.next()) {
                String nomeBeneficiario = rsBiglietti.getString("nome_beneficiario");
                String cognomeBeneficiario = rsBiglietti.getString("cognome_beneficiario");
                double prezzo = rsBiglietti.getDouble("prezzo");
                String tariffa = rsBiglietti.getString("tariffa");
                boolean pagato = rsBiglietti.getBoolean("pagato");

                Biglietto biglietto = new Biglietto(
                        prenotazione.getCliente(),
                        nomeBeneficiario,
                        cognomeBeneficiario,
                        prenotazione.getVolo(),
                        prezzo,
                        tariffa
                );
                biglietto.setPagato(pagato);

                biglietti.add(biglietto);
            }

            return biglietti;
        }
    }

    private Volo recuperaVoloDaResultSet(int idVolo) throws SQLException {
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

    private Aereo recuperaDettagliAereo(long numeroSeriale, String modello) throws SQLException {
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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUIClient().setVisible(true));
    }
}




