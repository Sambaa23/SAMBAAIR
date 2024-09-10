package Updatable.Biglietto;
import Updatable.Cliente.Cliente;
import Updatable.Pagamento.Pagamento;
import Updatable.Volo.Volo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;
import java.util.Scanner;

import jakarta.persistence.*;

@Entity
public class Biglietto extends BigliettoAbstract implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne private Cliente cliente;
    private String nomeBeneficiario;
    private String cognomeBeneficiario;
    @OneToOne private Volo volo;
    private double prezzo;
    private String tariffa;
    private boolean pagato;
    public Biglietto() {}//costruttore di default

    public Biglietto(Cliente cliente, String nomeBeneficiario, String cognomeBeneficiario, Volo volo, double prezzo, String tariffa) {
        this.cliente = cliente.clone();
        this.nomeBeneficiario = nomeBeneficiario;
        this.cognomeBeneficiario = cognomeBeneficiario;
        this.volo = volo.clone();
        this.prezzo = prezzo;
        this.tariffa = tariffa;
        this.pagato = false;
    }//costruttore normale

    public Biglietto(int idBiglietto, Cliente cliente, String nomeBeneficiario, String cognomeBeneficiario, Volo volo, double prezzo, String tariffa) {
        this.id = idBiglietto;
        this.cliente = cliente;
        this.nomeBeneficiario = nomeBeneficiario;
        this.cognomeBeneficiario = cognomeBeneficiario;
        this.volo = volo;
        this.prezzo = prezzo;
        this.tariffa = tariffa;
    }//costruttore normale 2

    public int getId() {
        return id;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public String getNomeBeneficiario() {
        return nomeBeneficiario;
    }

    public String getCognomeBeneficiario() {
        return cognomeBeneficiario;
    }

    public Volo getVolo() {
        return volo;
    }

    public double getPrezzo() {
        return prezzo;
    }

    public LocalTime getOrario(){
        return volo.getOrario();
    }

    public LocalDate getData(){
        return volo.dataVolo();
    }

    public LocalDateTime getDatetime(){
        return volo.dataOraVolo();
    }

    public String getTariffa() {
        return tariffa;
    }

    public void setPagato(boolean pagato) {
        this.pagato = pagato;
    }

    public double calcolaTassaModifica() {
        switch (tariffa.toLowerCase()) {
            case "economy":
                return 20.0;
            case "premium":
                return 10.0;
            case "business":
                return 5.0;
            default:
                return 0.0;
        }
    }//calcolaTassaModifica

    public void modificaDataOrario(Volo nuovoVolo, String nome, String cognome) {
        this.volo = nuovoVolo.clone();
    }//modificaDataOrario

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Biglietto)) return false;
        Biglietto biglietto = (Biglietto) o;
        return id == biglietto.id &&
                Double.compare(biglietto.prezzo, prezzo) == 0 &&
                Objects.equals(cliente, biglietto.cliente) &&
                Objects.equals(nomeBeneficiario, biglietto.nomeBeneficiario) &&
                Objects.equals(cognomeBeneficiario, biglietto.cognomeBeneficiario) &&
                Objects.equals(volo, biglietto.volo) &&
                Objects.equals(tariffa, biglietto.tariffa);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(id, cliente, nomeBeneficiario, cognomeBeneficiario, volo, prezzo, tariffa);
    }//hashCode

    @Override
    public String toString() {
        return "Biglietto{" +
                "id=" + id +
                ", nomeBeneficiario='" + nomeBeneficiario + '\'' +
                ", cognomeBeneficiario='" + cognomeBeneficiario + '\'' +
                ", orario=" + volo.orarioVolo() +
                ", data=" + volo.dataVolo() +
                ", prezzo=" + prezzo +
                '}';
    }//toString

    @Override
    public Biglietto clone() {
        try {
            Biglietto cloned = (Biglietto) super.clone();
            cloned.cliente = this.cliente.clone();
            cloned.volo = this.volo.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }//clone
}//Biglietto
