package Updatable.Promozione;

import java.time.LocalDate;

import Updatable.Volo.Volo;
import jakarta.persistence.*;

@Entity
public abstract class PromozioneAbstract {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String descrizione;
    private LocalDate dataInizio;
    private LocalDate dataFine;
    private boolean perClientiFedelta;

    protected PromozioneAbstract() {
        this.descrizione = "";
        this.dataInizio = LocalDate.now();
        this.dataFine = LocalDate.now();
        this.perClientiFedelta = false;
    }//Costruttore di default

    public PromozioneAbstract(String descrizione, LocalDate dataInizio, LocalDate dataFine, boolean perClientiFedelta) {
        this.descrizione = descrizione;
        this.dataInizio = dataInizio;
        this.dataFine = dataFine;
        this.perClientiFedelta = perClientiFedelta;
    }//Costruttore normale

    public abstract boolean isApplicable(Volo volo, LocalDate data);

    public String getDescrizione() {
        return descrizione;
    }

    public LocalDate getDataInizio() {
        return dataInizio;
    }

    public LocalDate getDataFine() {
        return dataFine;
    }

    public boolean isPerClientiFedelta() {
        return perClientiFedelta;
    }
}//PromozioneAbstract
