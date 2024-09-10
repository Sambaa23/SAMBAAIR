package Updatable.Promozione;

import Updatable.Volo.Volo;

import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "PromozionePeriodo")
public class PromozionePeriodo extends PromozioneAbstract {
    public PromozionePeriodo(String descrizione, LocalDate dataInizio, LocalDate dataFine, boolean perClientiFedelta) {
        super(descrizione, dataInizio, dataFine, perClientiFedelta);
    }

    @Override
    public boolean isApplicable(Volo volo, LocalDate data) {
        return !data.isBefore(getDataInizio()) && !data.isAfter(getDataFine());
    }
}


