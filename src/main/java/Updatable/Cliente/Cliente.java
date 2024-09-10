package Updatable.Cliente;

import java.time.LocalDate;
import java.util.*;

import Updatable.Prenotazione.Prenotazione;
import Updatable.Biglietto.Biglietto;
import Updatable.Volo.Volo;
import jakarta.persistence.*;

@Entity
public class Cliente extends ClienteAbstract implements Cloneable {
    @Id private String email;
    private String nome;
    private String cognome;
    private String indirizzo;
    private LocalDate dataNascita;
    @OneToMany(mappedBy = "Cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private LinkedList<Prenotazione> prenotazioni;

    public Cliente() {
        this.prenotazioni = new LinkedList<>();
    } // costruttore di default

    public Cliente(String email, String nome, String cognome, String indirizzo, LocalDate dataNascita) {
        this.email = email;
        this.nome = nome;
        this.cognome = cognome;
        this.indirizzo = indirizzo;
        this.dataNascita = dataNascita;
        this.prenotazioni = new LinkedList<>();
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

    public LinkedList<Prenotazione> getPrenotazioni() {
        return prenotazioni;
    }

    public void setPrenotazioni(LinkedList<Prenotazione> prenotazioni) {
        this.prenotazioni = prenotazioni;
    }

    public void aggiungiPrenotazione(Prenotazione prenotazione) {
        for (Prenotazione p : prenotazioni) {
            if (p.getVolo().getIdVolo() == prenotazione.getVolo().getIdVolo()) {
                throw new IllegalArgumentException("Il cliente ha già una prenotazione per questo volo");
            }
        }
        this.prenotazioni.add(prenotazione);
    }//aggiungiPrenotazione

    public void richiediModificaBiglietto(Biglietto biglietto, Volo nuovoVolo) {
        biglietto.modificaDataOrario(nuovoVolo, this.nome, this.cognome);
    }//richiediModificaBiglietto

    public void acquistaBiglietto(Prenotazione prenotazione, String nomeBeneficiario, String cognomeBeneficiario, double prezzo, String tariffa) {

        Biglietto biglietto = new Biglietto(this, nomeBeneficiario, cognomeBeneficiario, prenotazione.getVolo(), prezzo, tariffa);
        prenotazione.aggiungiBiglietto(biglietto);

    }//acquistaBiglietti

    public Cliente clone() {
        try {
            Cliente cloned = (Cliente) super.clone();
            cloned.prenotazioni = new LinkedList<>();
            for (Prenotazione p : this.prenotazioni) {
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
        Cliente cliente = (Cliente) o;
        return Objects.equals(email, cliente.email) &&
                Objects.equals(nome, cliente.nome) &&
                Objects.equals(cognome, cliente.cognome) &&
                Objects.equals(indirizzo, cliente.indirizzo) &&
                Objects.equals(dataNascita, cliente.dataNascita);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(email, nome, cognome, indirizzo, dataNascita);
    }// hashCode

    @Override
    public String toString() {
        return "Cliente{" +
                "email='" + email + '\'' +
                ", nome='" + nome + '\'' +
                ", cognome='" + cognome + '\'' +
                ", indirizzo='" + indirizzo + '\'' +
                ", dataNascita=" + dataNascita +
                ", prenotazioni=" + prenotazioni +
                '}';
    } // toString
} // Cliente
