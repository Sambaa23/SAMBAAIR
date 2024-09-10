package Updatable.Prenotazione;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;

import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Volo.Volo;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@IdClass(PrenotazioneID.class)
public class PrenotazioneFidelity extends PrenotazioneAbstract implements Cloneable{
    @Id @OneToOne private ClienteFedelta cliente;
    @Id @OneToOne private Volo volo;
    @OneToMany(mappedBy = "PrenotazioneFidelity", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedList<BigliettoFidelity> biglietti;
    private LocalDate dataPrenotazione;

    public PrenotazioneFidelity() {
        this.biglietti = new LinkedList<>();
    }//costruttore di default

    public PrenotazioneFidelity(ClienteFedelta cliente, Volo volo) {
        this.cliente = cliente;
        this.volo = volo;
        this.biglietti = new LinkedList<>();
    }

    public PrenotazioneFidelity(ClienteFedelta cliente, Volo volo, LocalDate dataPrenotazione) {
        this.cliente = cliente;
        this.volo = volo;
        this.biglietti = new LinkedList<>();
        this.dataPrenotazione = dataPrenotazione;
    }//Costruttore Normale

    public void aggiungiBiglietto(BigliettoFidelity biglietto) {
        if (biglietto.getCliente().getEmail().equals(this.cliente.getEmail()) &&
                biglietto.getVolo().getIdVolo() == this.volo.getIdVolo()) {
            this.biglietti.add(biglietto);
        } else {
            throw new IllegalArgumentException("Il biglietto non corrisponde alla prenotazione del cliente.");
        }
    }

    public boolean isScaduta() {
        LocalDate oggi = LocalDate.now();
        LocalTime adesso = LocalTime.now();

        LocalDate dataVolo = volo.getGiorno();
        LocalTime orarioVolo = volo.getOrario();

        if (oggi.isBefore(volo.dataVolo().minusDays(3)) || (oggi.isEqual(volo.dataVolo().minusDays(3)) && adesso.isBefore(orarioVolo))) {
            return false;
        }
        else{
            return true;
        }
    }

    public ClienteFedelta getCliente() {
        return cliente;
    }

    public Volo getVolo() {
        return volo;
    }

    public LinkedList<BigliettoFidelity> getBiglietti() {
        return biglietti;
    }

    public void setBiglietti(LinkedList<BigliettoFidelity> biglietti) {
        this.biglietti = biglietti;
    }

    public LocalDate getDataPrenotazione() {
        return dataPrenotazione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Updatable.Prenotazione.PrenotazioneFidelity that)) return false;
        return Objects.equals(cliente, that.cliente) && Objects.equals(volo, that.volo);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(cliente, volo);
    }//hashCode

    @Override
    public String toString() {
        return "PrenotazioneFidelity{" +
                "cliente=" + cliente +
                ", volo=" + volo +
                ", biglietti=" + biglietti +
                ", dataPrenotazione=" + dataPrenotazione +
                '}';
    }//toString
    @Override
    public PrenotazioneFidelity clone() {
        try {
            PrenotazioneFidelity cloned = (PrenotazioneFidelity) super.clone();
            cloned.biglietti = new LinkedList<>();
            for (BigliettoFidelity b : this.biglietti) {
                cloned.biglietti.add(b.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }//clone
}//PrenotazioneFidelity
