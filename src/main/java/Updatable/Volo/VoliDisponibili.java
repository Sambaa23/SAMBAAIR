package Updatable.Volo;

import Updatable.Aereo.Aereo;
import Updatable.Aereo.AereoFactoryImpl;
import Updatable.Promozione.PromozioneAbstract;
import Updatable.Promozione.PromozionePeriodo;
import Updatable.Promozione.PromozioneVoli;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

public class VoliDisponibili extends VoliDisponibiliAbstract {
    private List<Volo> voliDisponibili;
    private List<Aereo> aereiDisponibili;
    private List<PromozioneAbstract> promozioni;

    public VoliDisponibili(AereoFactoryImpl factory, int numAerei, int numero) {
        this.voliDisponibili = new ArrayList<>();
        this.aereiDisponibili = generaAereiDisponibili(factory, numAerei);
        this.promozioni = new ArrayList<>();
        generaVoliCasuali(numero);
    }

    public List<Volo> getVoliDisponibili() {
        return voliDisponibili;
    }

    public List<Aereo> getAereiDisponibili() {
        return aereiDisponibili;
    }

    private List<Aereo> generaAereiDisponibili(AereoFactoryImpl factory, int numAerei) {
        List<Aereo> aerei = new ArrayList<>();
        for (int i = 0; i < numAerei; i++) {
            Aereo aereo = factory.creaAereo();
            aerei.add(aereo);
        }
        return aerei;
    }//generaAereiDisponibili

    private void generaVoliCasuali(int numero) {
        Random rand = new Random();
        for (int i = 0; i < numero; i++) {
            List<String> airportList = new ArrayList<>(Volo.getAirportCoordinates().keySet());
            String partenza = airportList.get(rand.nextInt(airportList.size()));
            String destinazione;
            do {
                destinazione = airportList.get(rand.nextInt(airportList.size()));
            } while (destinazione.equals(partenza));

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime dataOra = now.plusMinutes(rand.nextInt(259200));
            LocalTime orario = dataOra.toLocalTime();
            LocalDate giorno = dataOra.toLocalDate();

            List<Aereo> aerei = getAereiDisponibili();
            Aereo aereo = aerei.get(rand.nextInt(aerei.size()));
            Volo volo = new Volo(partenza, destinazione, orario, giorno, aereo);

            voliDisponibili.add(volo);

            if (rand.nextDouble() < 0.4) {
                PromozioneAbstract promozione = new PromozioneVoli("Sconto del 15% sul volo", LocalDate.now().plusDays(rand.nextInt(30)), LocalDate.now().plusDays(rand.nextInt(60)), true, List.of(volo));
                promozioni.add(promozione);
            }

            if (rand.nextDouble() < 0.3) {
                PromozioneAbstract promozione = new PromozioneVoli("Sconto del 20% sul volo", LocalDate.now().plusDays(rand.nextInt(30)), LocalDate.now().plusDays(rand.nextInt(60)), true, List.of(volo));
                promozioni.add(promozione);
            }

            if (rand.nextDouble() < 0.2) {
                PromozioneAbstract promozione = new PromozionePeriodo("Offerta estiva", LocalDate.of(LocalDate.now().getYear(), 6, 1), LocalDate.of(LocalDate.now().getYear(), 8, 31), false);
                promozioni.add(promozione);
            }
        }
    }//generaVoliCasuali
}//VoliDisponibili


