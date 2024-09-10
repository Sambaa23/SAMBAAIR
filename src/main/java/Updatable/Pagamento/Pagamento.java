package Updatable.Pagamento;

import jakarta.persistence.*;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Random;

@Entity
public class Pagamento {
    @Id private String numeroCarta;
    private String dataScadenza;
    private String cvv;
    private String intestatario;
    private double saldoDisponibile;

    public Pagamento(){}


    public Pagamento(String numeroCarta, String dataScadenza, String cvv, String intestatario) {
        this.numeroCarta = numeroCarta;
        this.dataScadenza = dataScadenza;
        this.cvv = cvv;
        this.intestatario = intestatario;
        Random random = new Random();
        this.saldoDisponibile= random.nextDouble() * 20000000;
    }//Costruttore normale

    public boolean validaPagamento(double importo) {
        // Verifica della lunghezza del numero di carta
        if (numeroCarta.length() != 16) {
            System.out.println("Numero di carta incorretto");
            return false;
        }

        YearMonth dataScadenzaParsed = null;
        DateTimeFormatter formatterMMYY = DateTimeFormatter.ofPattern("MM/yy");
        DateTimeFormatter formatterMYY = DateTimeFormatter.ofPattern("M/yy");

        try {
            dataScadenzaParsed = YearMonth.parse(dataScadenza, formatterMMYY);
        } catch (DateTimeParseException e1) {
            try {
                dataScadenzaParsed = YearMonth.parse(dataScadenza, formatterMYY);
            } catch (DateTimeParseException e2) {
                System.out.println("Data di scadenza incorretta");
                return false;
            }
        }

        if (dataScadenzaParsed.isBefore(YearMonth.now())) {
            System.out.println("Carta scaduta");
            return false;
        }

        if (cvv.length() != 3) {
            System.out.println("CVV incorretto");
            return false;
        }

        if (saldoDisponibile < importo) {
            System.out.println("Saldo insufficiente");
            return false;
        }

        saldoDisponibile -= importo;
        return true;
    }//validaPagamento
}//Pagamento
