package Updatable.Promozione;

import java.time.LocalDate;
import java.util.List;

import Updatable.Volo.Volo;
import javax.persistence.*;

@Entity
@Table(name = "PromozioneVoli")
public class PromozioneVoli extends PromozioneAbstract {
    @ManyToMany
    @JoinTable(
            name = "PromozioneVoli",
            joinColumns = @JoinColumn(name = "promozione_id"),
            inverseJoinColumns = @JoinColumn(name = "volo_id")
    )
    private List<Volo> voliAssociati;

    public PromozioneVoli(String descrizione, LocalDate dataInizio, LocalDate dataFine, boolean perClientiFedelta, List<Volo> voliAssociati) {
        super(descrizione, dataInizio, dataFine, perClientiFedelta);
        this.voliAssociati = voliAssociati;
    }

    @Override
    public boolean isApplicable(Volo volo, LocalDate data) {
        return voliAssociati.contains(volo) && !data.isBefore(getDataInizio()) && !data.isAfter(getDataFine());
    }

    public List<Volo> getVoliAssociati() {
        return voliAssociati;
    }

    public void setVoliAssociati(List<Volo> voliAssociati) {
        this.voliAssociati = voliAssociati;
    }
}

