package Updatable.GUIServer;

import Updatable.conf.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TicketManagementDialog extends JDialog {

    private JTextField txtClienteEmail;
    private JTextField txtVoloID;
    private JTextField txtNomeBeneficiario;
    private JTextField txtCognomeBeneficiario;
    private JTextField txtPrezzo;
    private JComboBox<String> cboTariffa;
    private JTextField txtPunti;
    private JButton btnAggiungi;
    private JButton btnAggiorna;
    private JButton btnElimina;
    private JButton btnVisualizza;
    private boolean isFedelta;

    private JPanel panelBiglietto;
    private int bigliettiAggiunti = 0;

    public TicketManagementDialog(JFrame parent, String clienteEmail, String voloID, boolean isFedelta) {
        super(parent, "Gestione Biglietti", true);
        this.isFedelta = isFedelta;
        setLayout(new BorderLayout());

        JPanel panelDettagli = new JPanel(new GridLayout(0, 2));
        add(panelDettagli, BorderLayout.NORTH);

        panelDettagli.add(new JLabel("Email Cliente:"));
        txtClienteEmail = new JTextField(clienteEmail);
        txtClienteEmail.setEditable(false);
        panelDettagli.add(txtClienteEmail);

        panelDettagli.add(new JLabel("ID Volo:"));
        txtVoloID = new JTextField(voloID);
        txtVoloID.setEditable(false);
        panelDettagli.add(txtVoloID);

        panelDettagli.add(new JLabel("Nome Beneficiario:"));
        txtNomeBeneficiario = new JTextField();
        panelDettagli.add(txtNomeBeneficiario);

        panelDettagli.add(new JLabel("Cognome Beneficiario:"));
        txtCognomeBeneficiario = new JTextField();
        panelDettagli.add(txtCognomeBeneficiario);

        panelDettagli.add(new JLabel("Prezzo:"));
        txtPrezzo = new JTextField();
        panelDettagli.add(txtPrezzo);

        panelDettagli.add(new JLabel("Tariffa:"));
        cboTariffa = new JComboBox<>(new String[] {"economy", "premium", "business"});
        panelDettagli.add(cboTariffa);

        JPanel panelPulsanti = new JPanel();
        add(panelPulsanti, BorderLayout.SOUTH);

        btnAggiungi = new JButton("Aggiungi Biglietto");
        btnAggiorna = new JButton("Aggiorna Biglietto");
        btnElimina = new JButton("Elimina Biglietto");
        btnVisualizza = new JButton("Visualizza Biglietti");

        panelPulsanti.add(btnAggiungi);
        panelPulsanti.add(btnAggiorna);
        panelPulsanti.add(btnElimina);
        panelPulsanti.add(btnVisualizza);

        panelBiglietto = new JPanel(new GridLayout(1, 1));
        add(panelBiglietto, BorderLayout.CENTER);

        aggiornaPannelloBiglietto();

        btnAggiungi.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aggiungiBiglietto();
            }
        });

        btnAggiorna.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                aggiornaBiglietto();
            }
        });

        btnElimina.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                eliminaBiglietto();
            }
        });

        btnVisualizza.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizzaBiglietti();
            }
        });

        pack();
        setLocationRelativeTo(parent);
    }

    private void aggiornaPannelloBiglietto() {
        panelBiglietto.removeAll();

        if (isFedelta) {
            txtPunti = new JTextField();
            panelBiglietto.setLayout(new GridLayout(0, 2));
            panelBiglietto.add(new JLabel("Punti:"));
            panelBiglietto.add(txtPunti);
        } else {
            txtPunti = null;
            panelBiglietto.setLayout(new GridLayout(0, 1));
        }

        panelBiglietto.revalidate();
        panelBiglietto.repaint();
    }

    public int BigliettiAggiunti() {
        return bigliettiAggiunti;
    }

    private void aggiungiBiglietto() {
        String clienteEmail = txtClienteEmail.getText().trim();
        String voloID = txtVoloID.getText().trim();
        String nomeBeneficiario = txtNomeBeneficiario.getText().trim();
        String cognomeBeneficiario = txtCognomeBeneficiario.getText().trim();
        String prezzoText = txtPrezzo.getText().trim();
        String tariffa = (String) cboTariffa.getSelectedItem();
        boolean isFidelity = isFedelta;

        if (clienteEmail.isEmpty() || voloID.isEmpty() || nomeBeneficiario.isEmpty() || cognomeBeneficiario.isEmpty() || prezzoText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di aggiungere un biglietto.");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserire un valore numerico valido per il prezzo.");
            return;
        }

        String sql;
        if (isFidelity) {
            sql = "INSERT INTO BigliettoFidelity (cliente_fedelta_id, volo_id, nome_beneficiario, cognome_beneficiario, prezzo, tariffa, punti, pagato) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        } else {
            sql = "INSERT INTO Biglietto (cliente_email, volo_id, nome_beneficiario, cognome_beneficiario, prezzo, tariffa, pagato) VALUES (?, ?, ?, ?, ?, ?, ?)";
        }

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            int paramIndex = 1;
            pstmt.setString(paramIndex++, clienteEmail);
            pstmt.setString(paramIndex++, voloID);
            pstmt.setString(paramIndex++, nomeBeneficiario);
            pstmt.setString(paramIndex++, cognomeBeneficiario);
            pstmt.setDouble(paramIndex++, prezzo);
            pstmt.setString(paramIndex++, tariffa);
            pstmt.setBoolean(paramIndex++, false);

            if (isFidelity) {
                int punti;
                try {
                    punti = Integer.parseInt(txtPunti.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Inserire un valore numerico valido per i punti.");
                    return;
                }
                pstmt.setInt(paramIndex++, punti);
            }

            pstmt.executeUpdate();
            bigliettiAggiunti ++;
            JOptionPane.showMessageDialog(this, "Biglietto aggiunto con successo!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiunta del biglietto.");
        }
    }


    private void aggiornaBiglietto() {
        String clienteEmail = txtClienteEmail.getText().trim();
        String voloID = txtVoloID.getText().trim();
        String nomeBeneficiario = txtNomeBeneficiario.getText().trim();
        String cognomeBeneficiario = txtCognomeBeneficiario.getText().trim();
        String prezzoText = txtPrezzo.getText().trim();
        String tariffa = (String) cboTariffa.getSelectedItem();
        boolean isFidelity = isFedelta;

        if (clienteEmail.isEmpty() || voloID.isEmpty() || nomeBeneficiario.isEmpty() || cognomeBeneficiario.isEmpty() || prezzoText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di aggiornare un biglietto.");
            return;
        }

        double prezzo;
        try {
            prezzo = Double.parseDouble(prezzoText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Inserire un valore numerico valido per il prezzo.");
            return;
        }

        String sql;
        if (isFidelity) {
            sql = "UPDATE BigliettoFidelity SET nome_beneficiario = ?, cognome_beneficiario = ?, prezzo = ?, tariffa = ?, punti = ? WHERE cliente_fedelta_id = ? AND volo_id = ?";
        } else {
            sql = "UPDATE Biglietto SET nome_beneficiario = ?, cognome_beneficiario = ?, prezzo = ?, tariffa = ? WHERE cliente_email = ? AND volo_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            int paramIndex = 1;
            pstmt.setString(paramIndex++, nomeBeneficiario);
            pstmt.setString(paramIndex++, cognomeBeneficiario);
            pstmt.setDouble(paramIndex++, prezzo);
            pstmt.setString(paramIndex++, tariffa);

            if (isFidelity) {
                int punti;
                try {
                    punti = Integer.parseInt(txtPunti.getText().trim());
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(this, "Inserire un valore numerico valido per i punti.");
                    return;
                }
                pstmt.setInt(paramIndex++, punti);
                pstmt.setString(paramIndex++, clienteEmail);
            } else {
                pstmt.setString(paramIndex++, clienteEmail);
            }

            pstmt.setString(paramIndex++, voloID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Biglietto aggiornato con successo!");
            } else {
                JOptionPane.showMessageDialog(this, "Nessun biglietto trovato per aggiornare.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'aggiornamento del biglietto.");
        }
    }

    private void eliminaBiglietto() {
        String clienteEmail = txtClienteEmail.getText().trim();
        String voloID = txtVoloID.getText().trim();
        boolean isFidelity = isFedelta;

        if (clienteEmail.isEmpty() || voloID.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Inserire tutti i campi prima di eliminare un biglietto.");
            return;
        }

        String sql;
        if (isFidelity) {
            sql = "DELETE FROM BigliettoFidelity WHERE cliente_fedelta_id = ? AND volo_id = ?";
        } else {
            sql = "DELETE FROM Biglietto WHERE cliente_email = ? AND volo_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, clienteEmail);
            pstmt.setString(2, voloID);

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                JOptionPane.showMessageDialog(this, "Biglietto eliminato con successo!");
            } else {
                JOptionPane.showMessageDialog(this, "Nessun biglietto trovato per eliminare.");
            }
            bigliettiAggiunti --;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nell'eliminazione del biglietto.");
        }
    }


    private void visualizzaBiglietti() {
        String clienteEmail = txtClienteEmail.getText().trim();
        String voloID = txtVoloID.getText().trim();
        boolean isFidelity = isFedelta;

        String sql;
        if (isFidelity) {
            sql = "SELECT * FROM BigliettoFidelity WHERE cliente_fedelta_id = ? AND volo_id = ?";
        } else {
            sql = "SELECT * FROM Biglietto WHERE cliente_email = ? AND volo_id = ?";
        }

        try (Connection conn = DatabaseConnection.getConnection()) {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, clienteEmail);
            pstmt.setString(2, voloID);
            ResultSet rs = pstmt.executeQuery();

            JPanel panelVisualizzazione = new JPanel();
            panelVisualizzazione.setLayout(new BoxLayout(panelVisualizzazione, BoxLayout.Y_AXIS));

            while (rs.next()) {
                String info = String.format("Nome: %s, Cognome: %s, Prezzo: %.2f, Tariffa: %s",
                        rs.getString("nome_beneficiario"),
                        rs.getString("cognome_beneficiario"),
                        rs.getDouble("prezzo"),
                        rs.getString("tariffa"));

                if (isFidelity) {
                    info += String.format(", Punti: %d",
                            rs.getInt("punti"));
                }

                JLabel lblInfo = new JLabel(info);
                panelVisualizzazione.add(lblInfo);
            }

            JScrollPane scrollPane = new JScrollPane(panelVisualizzazione);
            JOptionPane.showMessageDialog(this, scrollPane, "Visualizzazione Biglietti", JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Errore nella visualizzazione dei biglietti.");
        }
    }
}

