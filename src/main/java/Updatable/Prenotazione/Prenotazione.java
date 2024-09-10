package Updatable.Prenotazione;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedList;
import Updatable.Biglietto.Biglietto;
import Updatable.Cliente.Cliente;
import Updatable.Pagamento.Pagamento;
import Updatable.Volo.Volo;
import jakarta.persistence.*;
import java.util.Objects;
import java.util.Scanner;

@Entity
@IdClass(PrenotazioneID.class)
public class Prenotazione extends PrenotazioneAbstract implements Cloneable{
    @Id @OneToOne private Cliente cliente;
    @Id @OneToOne private Volo volo;

    @OneToMany(mappedBy = "Prenotazione", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedList<Biglietto> biglietti;
    private LocalDate dataPrenotazione;

    public Prenotazione() {
        this.biglietti = new LinkedList<>();
    }//costruttore di default

    public Prenotazione(Cliente cliente, Volo volo) {
        this.cliente = cliente;
        this.volo = volo;
        this.biglietti = new LinkedList<>();
    }

    public Prenotazione(Cliente cliente, Volo volo, LocalDate dataPrenotazione) {
        this.cliente = cliente;
        this.volo = volo;
        this.biglietti = new LinkedList<>();
        this.dataPrenotazione = dataPrenotazione;
    }//Costruttore Normale

    public void aggiungiBiglietto(Biglietto biglietto) {
        if (biglietto.getCliente().getEmail().equals(this.cliente.getEmail()) && biglietto.getVolo().getIdVolo() == this.volo.getIdVolo()) {
            this.biglietti.add(biglietto);
        } else {
            throw new IllegalArgumentException("Il biglietto non riguarda la prenotazione del cliente " + this.cliente.getEmail() + " del volo " + this.volo.getIdVolo());
        }
    }//aggiungiBiglietto

    public boolean isScaduta() {
        LocalDate oggi = LocalDate.now();
        LocalTime adesso = LocalTime.now();

        LocalTime orarioVolo = volo.getOrario();
        if (oggi.isBefore(volo.dataVolo().minusDays(3)) || (oggi.isEqual(volo.dataVolo().minusDays(3)) && adesso.isBefore(orarioVolo))) {
            return false;
        }
        else{
            return true;
        }
    }

    public Cliente getCliente() {
        return cliente;
    }

    public Volo getVolo() {
        return volo;
    }

    public LinkedList<Biglietto> getBiglietti() {
        return biglietti;
    }

    public void setBiglietti(LinkedList<Biglietto> biglietti) {
        this.biglietti = biglietti;
    }

    public LocalDate getDataPrenotazione() {
        return dataPrenotazione;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Updatable.Prenotazione.Prenotazione that)) return false;
        return Objects.equals(cliente, that.cliente) && Objects.equals(volo, that.volo);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(cliente, volo);
    }//hashCode

    @Override
    public String toString() {
        return "Prenotazione{" +
                "cliente=" + cliente +
                ", volo=" + volo +
                ", biglietti=" + biglietti +
                ", dataPrenotazione=" + dataPrenotazione +
                '}';
    }//toString

    @Override
    public Prenotazione clone() {
        try {
            Prenotazione cloned = (Prenotazione) super.clone();
            cloned.biglietti = new LinkedList<>();
            for (Biglietto b : this.biglietti) {
                cloned.biglietti.add(b.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }//clone
}//Prenotazione
