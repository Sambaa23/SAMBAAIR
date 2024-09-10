package Updatable.Volo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import jakarta.persistence.*;
import Updatable.Aereo.Aereo;

@Entity
public class Volo extends VoloAbstract implements Cloneable{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idVolo;
    protected final String partenza;
    protected final String destinazione;
    protected LocalTime orario;
    protected LocalDate giorno;
    protected int disponibilita;
    @OneToOne protected Aereo aereo;

    private static final Map<String, double[]> airportCoordinates = new HashMap<>();

    static {
        // Aeroporti italiani principali
        airportCoordinates.put("FCO", new double[]{41.8003, 12.2389}); // Roma Fiumicino
        airportCoordinates.put("MXP", new double[]{45.6301, 8.7281});  // Milano Malpensa
        airportCoordinates.put("LIN", new double[]{45.4451, 9.2776});  // Milano Linate
        airportCoordinates.put("BLQ", new double[]{44.5312, 11.2887}); // Bologna Guglielmo Marconi
        airportCoordinates.put("NAP", new double[]{40.8848, 14.2908}); // Napoli Capodichino

        // Aeroporti europei principali
        airportCoordinates.put("CDG", new double[]{49.0097, 2.5479});  // Parigi Charles De Gaulle
        airportCoordinates.put("LHR", new double[]{51.4700, -0.4543}); // Londra Heathrow
        airportCoordinates.put("AMS", new double[]{52.3086, 4.7639});  // Amsterdam Schiphol
        airportCoordinates.put("FRA", new double[]{50.0379, 8.5622});  // Francoforte
        airportCoordinates.put("MAD", new double[]{40.4936, -3.5668}); // Madrid Barajas
        airportCoordinates.put("BCN", new double[]{41.2974, 2.0833});  // Barcellona El Prat
        airportCoordinates.put("MUC", new double[]{48.3538, 11.7861}); // Monaco di Baviera
        airportCoordinates.put("ZRH", new double[]{47.4647, 8.5492});  // Zurigo

        // Aeroporti mondiali principali
        airportCoordinates.put("JFK", new double[]{40.6413, -73.7781}); // New York John F. Kennedy
        airportCoordinates.put("LAX", new double[]{33.9416, -118.4085}); // Los Angeles
        airportCoordinates.put("ORD", new double[]{41.9742, -87.9073}); // Chicago O'Hare
        airportCoordinates.put("ATL", new double[]{33.6407, -84.4277}); // Atlanta Hartsfield-Jackson
        airportCoordinates.put("DXB", new double[]{25.2532, 55.3657});  // Dubai
        airportCoordinates.put("HND", new double[]{35.5494, 139.7798}); // Tokyo Haneda
        airportCoordinates.put("PEK", new double[]{40.0801, 116.5846}); // Pechino Capital
        airportCoordinates.put("SYD", new double[]{-33.9399, 151.1753}); // Sydney Kingsford Smith
    }

    public Volo() {
        this.idVolo = -1;
        this.partenza = "";
        this.destinazione = "";
        this.orario = LocalTime.MIDNIGHT;
        this.giorno = LocalDate.now();
        this.disponibilita = 0;
        this.aereo = null;
    }

    public Volo(String partenza, String destinazione, LocalTime orario, LocalDate giorno, Aereo aereo){
        this.partenza = partenza;
        this.destinazione = destinazione;
        this.giorno = LocalDate.of(giorno.getYear(),giorno.getMonth(),giorno.getDayOfMonth());
        this.orario = LocalTime.of(orario.getHour(), orario.getMinute());
        this.disponibilita = aereo.getPosti();
        this.aereo = aereo;
    }

    public Volo(int idVolo, String partenza, String destinazione, LocalTime orario, LocalDate giorno, Aereo aereo) {
        this.idVolo=idVolo;
        this.partenza = partenza;
        this.destinazione = destinazione;
        this.orario = orario;
        this.giorno = giorno;
        this.disponibilita = aereo.getPosti();
        this.aereo = aereo;
    }

    public int getIdVolo() {
        return idVolo;
    }

    public String getPartenza() {
        return partenza;
    }

    public String getDestinazione() {
        return destinazione;
    }

    public LocalTime getOrario() {
        return orario;
    }

    public LocalDate getGiorno() {
        return giorno;
    }

    public int getDisponibilita() {
        return disponibilita;
    }

    @Override
    public LocalTime orarioVolo(){
        return getOrario();
    }

    @Override
    public LocalDate dataVolo() {
        return getGiorno();
    }

    @Override
    public LocalDateTime dataOraVolo() {
        LocalDate d = getGiorno();
        LocalTime o = getOrario();
        return LocalDateTime.of(d,o);
    }

    @Override
    public int disponibilitaVolo() {
        return getDisponibilita();
    }

    public int getPunti() {
        return calcolaPunti();
    }

    public Aereo getAereo() {
        return aereo;
    }

    public static Map<String, double[]> getAirportCoordinates() {
        return airportCoordinates;
    }//getAirportCoordinates

    private double[] getCoordinate(String airportCode) {
        double[] coords = airportCoordinates.get(airportCode);
        if (coords == null) {
            throw new IllegalArgumentException("Le coordinate per l'aeroporto " + airportCode + " non sono disponibili.");
        }
        return coords;
    }//getCoordinate

    public void setOrario(LocalTime orario) {
        this.orario = orario;
    }

    public void setGiorno(LocalDate giorno) {
        this.giorno = giorno;
    }

    public void setDisponibilita(int disponibilita) {
        this.disponibilita = disponibilita;
    }

    public void setAereo(Aereo aereo) {
        this.aereo = aereo;
    }

    private double calcolaDistanza(String city1, String city2) {
        double[] coord1 = getCoordinate(city1);
        double[] coord2 = getCoordinate(city2);

        final int R = 6371;//raggio Terra

        double latDistance = Math.toRadians(coord2[0] - coord1[0]);
        double lonDistance = Math.toRadians(coord2[1] - coord1[1]);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(coord1[0])) * Math.cos(Math.toRadians(coord2[0]))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c;
    }//calcolaDistanza

    public int calcolaPunti() {
        double distanza = calcolaDistanza(partenza, destinazione);
        return (int) Math.floor(distanza);
    }//calcolaPunti

    public Volo clone() {
        try {
            Volo cloned = (Volo) super.clone();
            if (this.aereo != null) {
                cloned.aereo = this.aereo.clone();
            }
            return cloned;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError(e);
        }
    }//clone

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Volo volo = (Volo) o;
        return idVolo == volo.idVolo &&
                disponibilita == volo.disponibilita &&
                Objects.equals(partenza, volo.partenza) &&
                Objects.equals(destinazione, volo.destinazione) &&
                Objects.equals(orario, volo.orario) &&
                Objects.equals(giorno, volo.giorno) &&
                Objects.equals(aereo, volo.aereo);
    }//equals

    @Override
    public int hashCode() {
        return Objects.hash(idVolo, partenza, destinazione, orario, giorno, disponibilita, aereo);
    }//hashCode

    public String toString(){
        return "Compagnia SambaAir, volo "+idVolo+", "+partenza+"-"+destinazione+", "+giorno+" "+orario+", disponibilità: "+disponibilita;
    }//toString
}//Volo
