package Updatable.Biglietto;

import Updatable.Cliente.ClienteFedelta;
import Updatable.Volo.Volo;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Objects;

import jakarta.persistence.*;

@Entity
public class BigliettoFidelity extends BigliettoAbstract implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @OneToOne private ClienteFedelta cliente;
    private String nomeBeneficiario;
    private String cognomeBeneficiario;
    @OneToOne private Volo volo;
    private double prezzo;
    private int punti;
    private String tariffa;
    private boolean pagato;

    public BigliettoFidelity() {}//costruttore di default

    public BigliettoFidelity(ClienteFedelta cliente, String nomeBeneficiario, String cognomeBeneficiario, Volo volo, double prezzo, String tariffa) {
        this.cliente = cliente.clone();
        this.nomeBeneficiario = nomeBeneficiario;
        this.cognomeBeneficiario = cognomeBeneficiario;
        this.volo = volo.clone();
        this.prezzo = prezzo;
        this.punti = volo.getPunti();
        this.tariffa = tariffa;
        this.pagato = false;
    }//costruttore normale

    // Aggiungi un costruttore che accetta l'ID
    public BigliettoFidelity(int idBiglietto, ClienteFedelta cliente, String nomeBeneficiario, String cognomeBeneficiario, Volo volo, double prezzo, String tariffa) {
        this.id = idBiglietto;
        this.cliente = cliente;
        this.nomeBeneficiario = nomeBeneficiario;
        this.cognomeBeneficiario = cognomeBeneficiario;
        this.volo = volo;
        this.prezzo = prezzo;
        this.tariffa = tariffa;
    }

    public int getId() {
        return id;
    }

    public ClienteFedelta getCliente() {
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

    public int getPunti() {
        return punti;
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

    public void setPunti(int punti) {
        this.punti = punti;
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
    }

    public void modificaDataOrario(Volo nuovoVolo) {
        this.volo = nuovoVolo.clone();
    }//modificaDataOrario

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BigliettoFidelity that = (BigliettoFidelity) o;
        return Double.compare(that.prezzo, prezzo) == 0 &&
                Double.compare(that.punti, punti) == 0 &&
                Objects.equals(cliente, that.cliente) &&
                Objects.equals(nomeBeneficiario, that.nomeBeneficiario) &&
                Objects.equals(cognomeBeneficiario, that.cognomeBeneficiario) &&
                Objects.equals(volo, that.volo) &&
                Objects.equals(tariffa, that.tariffa);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), cliente, nomeBeneficiario, cognomeBeneficiario, volo, prezzo, punti, tariffa);
    }//hashCode

    @Override
    public String toString() {
        return "BigliettoFidelity{" +
                "id=" + id +
                ", cliente=" + cliente +
                ", nomeBeneficiario='" + nomeBeneficiario + '\'' +
                ", cognomeBeneficiario='" + cognomeBeneficiario + '\'' +
                ", volo=" + volo +
                ", prezzo=" + prezzo +
                ", punti=" + punti +
                '}';
    }//toString
    @Override
    public BigliettoFidelity clone() {
        try {
            BigliettoFidelity cloned = (BigliettoFidelity) super.clone();
            cloned.cliente = this.cliente.clone();
            cloned.volo = this.volo.clone();
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }//clone
}//BigliettoFidelity


