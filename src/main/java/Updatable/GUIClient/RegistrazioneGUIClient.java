package Updatable.GUIClient;

import Updatable.conf.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RegistrazioneGUIClient extends JFrame {
    private JTextField txtEmail;
    private JTextField txtNome;
    private JTextField txtCognome;
    private JTextField txtIndirizzo;
    private JTextField txtDataNascita;
    private JButton btnRegistrati;
    private JCheckBox chkFidelity;
    private boolean isFidelity;

    public RegistrazioneGUIClient(String emailPrecedente) {
        setTitle("Registrazione Cliente");
        setSize(400, 350);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        panel.setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int row = 0;

        gbc.gridx = 0;
        gbc.gridy = row;
        panel.add(new JLabel("Email:"), gbc);
        txtEmail = new JTextField(20);
        txtEmail.setText(emailPrecedente);
        txtEmail.setEnabled(false);
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


        btnRegistrati = new JButton("Registrati");
        gbc.gridx = 1;
        gbc.gridy = ++row;
        panel.add(btnRegistrati, gbc);

        chkFidelity.addItemListener(e -> {
            isFidelity = (e.getStateChange() == ItemEvent.SELECTED);
        });

        btnRegistrati.addActionListener(e -> handleRegistrazione());

        add(panel);
    }

    private void handleRegistrazione() {
        String email = txtEmail.getText().trim();
        String nome = txtNome.getText().trim();
        String cognome = txtCognome.getText().trim();
        String indirizzo = txtIndirizzo.getText().trim();
        LocalDate dataNascita;
        int codice = 0;
        int puntiAccumulati = 0;
        boolean fedelta = isFidelity;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        try {
            dataNascita = LocalDate.parse(txtDataNascita.getText().trim(), formatter);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Formato della data o dati non validi.");
            return;
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);

            if (fedelta) {
                codice = getNextFidelityCode(conn);

                String sqlInsertFedelta = "INSERT INTO ClienteFedelta (email, nome, cognome, indirizzo, data_nascita, codice, punti_accumulati) VALUES (?, ?, ?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertFedelta)) {
                    pstmt.setString(1, email);
                    pstmt.setString(2, nome);
                    pstmt.setString(3, cognome);
                    pstmt.setString(4, indirizzo);
                    pstmt.setObject(5, dataNascita);
                    pstmt.setInt(6, codice);
                    pstmt.setInt(7, puntiAccumulati);
                    pstmt.executeUpdate();
                }
            } else {
                String sqlInsertCliente = "INSERT INTO Cliente (email, nome, cognome, indirizzo, data_nascita) VALUES (?, ?, ?, ?, ?)";
                try (PreparedStatement pstmt = conn.prepareStatement(sqlInsertCliente)) {
                    pstmt.setString(1, email);
                    pstmt.setString(2, nome);
                    pstmt.setString(3, cognome);
                    pstmt.setString(4, indirizzo);
                    pstmt.setObject(5, dataNascita);
                    pstmt.executeUpdate();
                }
            }

            conn.commit();
            JOptionPane.showMessageDialog(this, "Registrazione avvenuta con successo!");
            new LoginGUIClient(email, fedelta).setVisible(true);
            dispose();

        } catch (SQLException ex) {
            ex.printStackTrace();
            try {
                Connection conn = DatabaseConnection.getConnection();
                conn.rollback();
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
            JOptionPane.showMessageDialog(this, "Errore durante la registrazione.");
        }
    }

    private int getNextFidelityCode(Connection conn) throws SQLException {
        int newCode = 1;
        String sql = "SELECT COALESCE(MAX(codice), 0) AS maxCode FROM ClienteFedelta";
        try (PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            if (rs.next()) {
                newCode = rs.getInt("maxCode") + 1;
            }
        }
        return newCode;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginGUIClient().setVisible(true));
    }
}



