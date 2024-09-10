package Updatable.Aereo;

import jakarta.persistence.*;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

@Entity
@IdClass(AereoId.class)
public class Aereo implements Cloneable{
    @Id
    private long numeroSeriale;
    @Id private String modello;
    @OneToMany(mappedBy = "Aereo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posto> posti;
    private TipoVeicolo tipo;

    public enum TipoVeicolo {
        NAZIONALE,
        INTERNAZIONALE,
        INTERCONTINENTALE
    }

    public Aereo(long numeroSeriale, String modello, int p, TipoVeicolo tipo) {
        this.numeroSeriale = numeroSeriale;
        this.modello = modello;
        this.tipo = tipo;
        this.posti = creaListaPosti(p);
    }//costruttore normale 1

    public Aereo(long numeroSeriale, String modello, TipoVeicolo tipo) {
        this.numeroSeriale = numeroSeriale;
        this.modello = modello;
        this.posti = new LinkedList<>();
        this.tipo = tipo;
    }//costruttore normale 2

    public Aereo() {}

    private List<Posto> creaListaPosti(int p) {
        List<Posto> posti = new LinkedList<>();
        char nomeUltPosto = getNameLastPos();
        for (int fila = 1; fila <= p; fila++) {
            for (char j = 'a'; j <= nomeUltPosto; j++) {
                Posto pos = new Posto(this, fila, j);
                posti.add(pos);
            }
        }
        return posti;
    }//creaListaPosti

    public int getPosti() {
        if (posti == null) {
            return 0;
        }
        Iterator<Posto> it = posti.iterator();
        int cont = 0;
        while (it.hasNext()) {
            cont++;
            it.next();
        }
        return cont;
    }//getPosti

    private char getNameLastPos() {
        char c;
        switch (tipo) {
            case INTERCONTINENTALE -> c= 'j';
            case INTERNAZIONALE, NAZIONALE -> c= 'f';
            default -> throw new IllegalStateException("Unexpected value: " + tipo);
        }
        return c;
    }//getNameLastPos

    // Getters
    public long getNumeroSeriale() {
        return numeroSeriale;
    }

    public String getModello() {
        return modello;
    }

    public List<Posto> getP() {
        return posti;
    }

    public TipoVeicolo getTipo() {
        return tipo;
    }

    // equals, hashCode, toString
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Aereo)) return false;
        Aereo veicolo = (Aereo) o;
        return numeroSeriale == veicolo.numeroSeriale && Objects.equals(modello, veicolo.modello);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(numeroSeriale, modello);
    }//hashCode

    @Override
    public String toString() {
        return "Veicolo{" + "SN=" + numeroSeriale + ", modello='" + modello + '\'' + '}';
    }//toString

    @Override
    public Aereo clone() {
        try {
            Aereo cloned = (Aereo) super.clone();
            cloned.posti = new LinkedList<>();
            for (Posto posto : this.posti) {
                cloned.posti.add(posto.clone());
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e); // Gestione delle eccezioni
        }
    }//Clone
}//Aereo