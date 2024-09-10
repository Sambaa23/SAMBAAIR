package Updatable.GUIClient;

import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Cliente.EstrattoConto;
import Updatable.Pagamento.Pagamento;
import Updatable.Prenotazione.PrenotazioneFidelity;
import Updatable.conf.DatabaseConnection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrenotazioneFidelityGUI extends JFrame {
    private PrenotazioneFidelity prenotazione;
    private List<JFrame> finestreInserimentoBiglietti;
    private JFrame frameInserimentoBiglietti;
    private JTextField txtNomeBeneficiario;
    private JTextField txtCognomeBeneficiario;
    private JComboBox<String> cboTariffa;
    private int numeroBiglietti;
    private int bigliettiInseriti = 0;

    public PrenotazioneFidelityGUI(PrenotazioneFidelity prenotazione) {
        this.prenotazione=prenotazione;
        this.finestreInserimentoBiglietti = new ArrayList<>();

        setTitle("Prenotazione Fedeltà");
        setSize(400, 300);
        setLayout(new GridLayout(7, 2));

        add(new JLabel("Volo: " + prenotazione.getVolo().getPartenza() + " -> " + prenotazione.getVolo().getDestinazione()));
        add(new JLabel("Cliente Fedeltà: " + prenotazione.getCliente().getEmail()));

        JButton btnPrenotaBiglietto = new JButton("Prenota Biglietto");
        btnPrenotaBiglietto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                mostraDialogoNumeroBiglietti();
            }
        });

        add(btnPrenotaBiglietto);

        JButton btnVisualizzaEstrattoConto = new JButton("Visualizza Estratto Conto");
        btnVisualizzaEstrattoConto.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                visualizzaEstrattoConto();
            }
        });
        add(btnVisualizzaEstrattoConto);

        setVisible(true);

        setLocationRelativeTo(null);
    }

    private void mostraDialogoNumeroBiglietti() {
        String input = JOptionPane.showInputDialog(this, "Quanti biglietti vuoi acquistare?");
        if (input == null || input.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Devi inserire un numero di biglietti.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            numeroBiglietti = Integer.parseInt(input);
            if (numeroBiglietti <= 0) {
                JOptionPane.showMessageDialog(this, "Devi inserire almeno un biglietto.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (numeroBiglietti > prenotazione.getVolo().getDisponibilita()) {
                JOptionPane.showMessageDialog(this, "Il numero di biglietti da te selezionato supera la disponibilità del volo.", "Errore", JOptionPane.ERROR_MESSAGE);
                return;
            }

            bigliettiInseriti = 0;
            mostraScreenInserimentoDettagli();
            dispose();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Inserisci un numero valido.", "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostraScreenInserimentoDettagli() {
        JPanel panel = new JPanel(new GridLayout(4, 2));
        txtNomeBeneficiario = new JTextField();
        txtCognomeBeneficiario = new JTextField();
        cboTariffa = new JComboBox<>(new String[]{"economy", "premium", "business"});

        panel.add(new JLabel("Nome Beneficiario:"));
        panel.add(txtNomeBeneficiario);
        panel.add(new JLabel("Cognome Beneficiario:"));
        panel.add(txtCognomeBeneficiario);
        panel.add(new JLabel("Tariffa:"));
        panel.add(cboTariffa);

        JButton btnProcedi = new JButton("Procedi");
        btnProcedi.addActionListener(e -> gestisciPrenotazione());

        panel.add(btnProcedi);

        frameInserimentoBiglietti = new JFrame("Inserisci Dettagli Biglietti");
        frameInserimentoBiglietti.setSize(400, 200);
        frameInserimentoBiglietti.add(panel);
        frameInserimentoBiglietti.setLocationRelativeTo(this);
        frameInserimentoBiglietti.setVisible(true);

        finestreInserimentoBiglietti.add(frameInserimentoBiglietti);
    }

    private void gestisciPrenotazione() {
        String nomeBeneficiario = txtNomeBeneficiario.getText().trim();
        String cognomeBeneficiario = txtCognomeBeneficiario.getText().trim();
        String tariffa = (String) cboTariffa.getSelectedItem();
        double prezzo = calcolaPrezzoBiglietto(tariffa);

        if (nomeBeneficiario.isEmpty() || cognomeBeneficiario.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Compila tutti i campi.", "Errore", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            prenotazione.getCliente().acquistaBiglietto(prenotazione, nomeBeneficiario, cognomeBeneficiario, prezzo, tariffa);
            bigliettiInseriti++;
            if (bigliettiInseriti >= numeroBiglietti) {
                mostraScreenPagamento();
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Biglietto " + bigliettiInseriti + " aggiunto. Inserisci i dettagli del prossimo biglietto.");
                mostraScreenInserimentoDettagli();

            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void mostraScreenPagamento() {
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

            double importoTotale = calcolaImportoTotale();

            Pagamento pagamento = new Pagamento(numeroCarta, scadenza, cvv, prenotazione.getCliente().getNome() + " " + prenotazione.getCliente().getCognome());

            if (pagamento.validaPagamento(importoTotale)) {
                JOptionPane.showMessageDialog(this, "Pagamento effettuato con successo!");

                completaPrenotazione();
                chiudiTutteLeFinestre();

            } else {
                JOptionPane.showMessageDialog(this, "Pagamento fallito. Si prega di riprovare.");
            }
        }
    }

    private void chiudiTutteLeFinestre() {
        for (JFrame frame : finestreInserimentoBiglietti) {
            frame.dispose();
        }
        finestreInserimentoBiglietti.clear();
    }

    private double calcolaImportoTotale() {
        double importoTotale = 0.0;
        for (PrenotazioneFidelity prenotazione : prenotazione.getCliente().getPrenotazioni()) {
            for (BigliettoFidelity biglietto : prenotazione.getBiglietti()) {
                importoTotale += biglietto.getPrezzo();
            }
        }
        return importoTotale;
    }


    private void completaPrenotazione() {
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
                        for (BigliettoFidelity biglietto : prenotazione.getBiglietti()) {
                            bigliettoStmt.setString(1, biglietto.getCliente().getEmail());
                            bigliettoStmt.setInt(2, biglietto.getVolo().getIdVolo());
                            bigliettoStmt.setString(3, biglietto.getNomeBeneficiario());
                            bigliettoStmt.setString(4, biglietto.getCognomeBeneficiario());
                            bigliettoStmt.setDouble(5, biglietto.getPrezzo());
                            bigliettoStmt.setInt(6, biglietto.getPunti());
                            bigliettoStmt.setString(7, biglietto.getTariffa());
                            bigliettoStmt.setBoolean(8, true);
                            bigliettoStmt.executeUpdate();
                        }
                }
                prenotazione.getCliente().aggiungiPrenotazione(prenotazione);

                int puntiTotali = prenotazione.getCliente().getPuntiAccumulati() + calcolaPuntiFedelta();
                prenotazione.getCliente().setPuntiAccumulati(puntiTotali);
                prenotazione.getCliente().setDataUltimoAcquisto(LocalDate.now());

                String updatePuntiClienteQuery = "UPDATE ClienteFedelta SET punti_accumulati = ?, data_ultimo_acquisto = ? WHERE email = ?";
                try (PreparedStatement updatePuntiStmt = connection.prepareStatement(updatePuntiClienteQuery)) {
                    updatePuntiStmt.setInt(1, puntiTotali);
                    updatePuntiStmt.setDate(2, new Date(System.currentTimeMillis()));
                    updatePuntiStmt.setString(3, prenotazione.getCliente().getEmail());
                    updatePuntiStmt.executeUpdate();
                }

                int nuovaDisponibilita = prenotazione.getVolo().getDisponibilita() - numeroBiglietti;
                prenotazione.getVolo().setDisponibilita(nuovaDisponibilita);

                String updateDisponibilitaVoloQuery = "UPDATE Volo SET disponibilita = ? WHERE idVolo = ?";
                try (PreparedStatement updateVoloStmt = connection.prepareStatement(updateDisponibilitaVoloQuery)) {
                    updateVoloStmt.setInt(1, nuovaDisponibilita);
                    updateVoloStmt.setInt(2, prenotazione.getVolo().getIdVolo());
                    updateVoloStmt.executeUpdate();
                }


                connection.commit();
                System.out.println("Prenotazione e biglietti inseriti nel database con successo!");
            } catch (SQLException e) {
                connection.rollback();
                System.err.println("Errore durante l'inserimento nel database: " + e.getMessage());
            }
        } catch (SQLException e) {
            System.err.println("Errore di connessione al database: " + e.getMessage());
        }
    }

    private int calcolaPuntiFedelta() {
        int puntiTotali = 0;
        for (BigliettoFidelity biglietto : prenotazione.getBiglietti()) {
            puntiTotali += biglietto.getVolo().calcolaPunti();
        }
        return puntiTotali;
    }

    private double calcolaPrezzoBiglietto(String tariffa) {
        switch (tariffa) {
            case "premium":
                return 300.0;
            case "business":
                return 500.0;
            default:
                return 150.0;
        }
    }

    private void visualizzaEstrattoConto() {

        EstrattoConto estrattoConto = prenotazione.getCliente().generaEstrattoConto();
        List<String> estratto = estrattoConto.getEstratto();

        StringBuilder estrattoStringa = new StringBuilder();
        for (String riga : estratto) {
            estrattoStringa.append(riga).append("\n");
        }

        JTextArea textArea = new JTextArea(estrattoStringa.toString());
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 300));

        JOptionPane.showMessageDialog(this, scrollPane, "Estratto Conto Fedeltà", JOptionPane.INFORMATION_MESSAGE);
    }
}




