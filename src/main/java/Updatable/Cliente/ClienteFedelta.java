package Updatable.Cliente;

import Updatable.Biglietto.BigliettoFidelity;
import Updatable.GUIClient.GUIVoliFidelity;
import Updatable.Prenotazione.PrenotazioneFidelity;
import Updatable.Promozione.PromozioneAbstract;
import Updatable.Volo.Volo;
import java.time.LocalDate;
import java.util.*;
import jakarta.persistence.*;
import client.NotificheClient;

@Entity
public class ClienteFedelta extends ClienteAbstract implements Cloneable{
    @Id private String email;
    private String nome;
    private String cognome;
    private String indirizzo;
    private LocalDate dataNascita;
    @OneToMany(mappedBy = "ClienteFedelta", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedList<PrenotazioneFidelity> prenotazioni = new LinkedList<>();
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int codice;
    private int puntiAccumulati;
    private LocalDate dataUltimoAcquisto;

    public ClienteFedelta() {
        this.prenotazioni = new LinkedList<>();
        this.puntiAccumulati = 0;
    } // costruttore di default

    public ClienteFedelta(String email, String nome, String cognome, String indirizzo, LocalDate dataNascita) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.indirizzo = indirizzo;
        this.dataNascita = dataNascita;
        this.puntiAccumulati = 0;
    } // Costruttore normale

    public String getEmail() {
        return email;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public LocalDate getDataNascita() {
        return dataNascita;
    }

    public LinkedList<PrenotazioneFidelity> getPrenotazioni() {
        return prenotazioni;
    }

    public void setPrenotazioni(LinkedList<PrenotazioneFidelity> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }

    public int getCodice() {
        return codice;
    }

    public int getPuntiAccumulati() {
        return puntiAccumulati;
    }

    public void setPuntiAccumulati(int puntiAccumulati) {
        this.puntiAccumulati = puntiAccumulati;
    }

    public LocalDate getDataUltimoAcquisto() {
        return dataUltimoAcquisto;
    }

    public void setDataUltimoAcquisto(LocalDate dataUltimoAcquisto) {
        this.dataUltimoAcquisto = dataUltimoAcquisto;
    }

    public void aggiungiPrenotazione(PrenotazioneFidelity prenotazione) {
        for (PrenotazioneFidelity p : prenotazioni) {
            if (p.getVolo().getIdVolo() == prenotazione.getVolo().getIdVolo()) {
                throw new IllegalArgumentException("Il cliente ha già una prenotazione per questo volo");
            }
        }
        this.prenotazioni.add(prenotazione);
    }//aggiungiPrenotazione

    public void richiediModificaBiglietto(BigliettoFidelity biglietto, Volo nuovoVolo) {
        biglietto.modificaDataOrario(nuovoVolo);
    }//richiediModificaBiglietto

    public void rimuoviPrenotazione(PrenotazioneFidelity prenotazione) {
        this.prenotazioni.remove(prenotazione);
    }//rimuoviPrenotazione

    public void acquistaBiglietto(PrenotazioneFidelity prenotazione, String nomeBeneficiario, String cognomeBeneficiario, double prezzo, String tariffa) {

        BigliettoFidelity biglietto = new BigliettoFidelity(this, nomeBeneficiario, cognomeBeneficiario, prenotazione.getVolo(), prezzo, tariffa);
        prenotazione.aggiungiBiglietto(biglietto);

    }//acquistaBiglietti

    public EstrattoConto generaEstrattoConto() {
        return new EstrattoConto(this);
    }//EstrattoConto


    public ClienteFedelta clone() {
        try {
            ClienteFedelta cloned = (ClienteFedelta) super.clone();
            cloned.prenotazioni = new LinkedList<>();
            for (PrenotazioneFidelity p : this.prenotazioni) {
                cloned.prenotazioni.add(p.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError();
        }
    }// clone

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClienteFedelta that = (ClienteFedelta) o;
        return codice == that.codice &&
                puntiAccumulati == that.puntiAccumulati &&
                Objects.equals(email, that.email) &&
                Objects.equals(nome, that.nome) &&
                Objects.equals(cognome, that.cognome) &&
                Objects.equals(indirizzo, that.indirizzo) &&
                Objects.equals(dataNascita, that.dataNascita) &&
                Objects.equals(dataUltimoAcquisto, that.dataUltimoAcquisto) &&
                Objects.equals(prenotazioni, that.prenotazioni);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email, nome, cognome, indirizzo, dataNascita, codice, puntiAccumulati, dataUltimoAcquisto, prenotazioni);
    }//hashCode

    public String toString() {
        return "Cliente {"+nome+" "+cognome+"}";
    }//toString
}//ClienteFedelta
