package UpdatableTest.Cliente;

import static org.junit.jupiter.api.Assertions.*;

import Updatable.Aereo.Aereo;
import Updatable.Biglietto.BigliettoFidelity;
import Updatable.Cliente.ClienteFedelta;
import Updatable.Prenotazione.PrenotazioneFidelity;
import Updatable.Volo.Volo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import java.time.LocalTime;

public class ClienteFedeltaTest {

    private ClienteFedelta clienteFedelta;
    private Volo volo;
    private Aereo aereo;
    private PrenotazioneFidelity prenotazione;

    @BeforeEach
    void setUp() {
        aereo = new Aereo(123456789L, "Boeing 747", 450, Aereo.TipoVeicolo.INTERNAZIONALE);
        volo = new Volo("NAP", "LAX", LocalTime.of(16, 17), LocalDate.of(2024, 5, 21), aereo);
        clienteFedelta = new ClienteFedelta("email@test.com", "Mario", "Rossi", "Via Roma 1", LocalDate.of(1980, 5, 15));
        prenotazione = new PrenotazioneFidelity(clienteFedelta, volo);
    }

    @Test
    void testGetEmail() {
        assertEquals("email@test.com", clienteFedelta.getEmail());
    }

    @Test
    void testGetNome() {
        assertEquals("Mario", clienteFedelta.getNome());
    }

    @Test
    void testGetCognome() {
        assertEquals("Rossi", clienteFedelta.getCognome());
    }

    @Test
    void testGetIndirizzo() {
        assertEquals("Via Roma 1", clienteFedelta.getIndirizzo());
    }

    @Test
    void testGetDataNascita() {
        assertEquals(LocalDate.of(1980, 5, 15), clienteFedelta.getDataNascita());
    }

    @Test
    void testRichiediModificaBiglietto() {
        BigliettoFidelity biglietto = new BigliettoFidelity(clienteFedelta, "Luca", "Bianchi", volo, 150.0, "economy");
        Volo nuovoVolo = new Volo("NAP", "LAX", LocalTime.of(15, 15), LocalDate.of(2024, 4, 30), aereo);
        clienteFedelta.richiediModificaBiglietto(biglietto, nuovoVolo);
        assertEquals(nuovoVolo, biglietto.getVolo());
    }

    @Test
    void testAggiungiPrenotazione() {
        PrenotazioneFidelity prenotazioneFidelity = new PrenotazioneFidelity(clienteFedelta, volo);
        clienteFedelta.aggiungiPrenotazione(prenotazioneFidelity);
        assertEquals(1, clienteFedelta.getPrenotazioni().size());
        assertThrows(IllegalArgumentException.class, () -> clienteFedelta.aggiungiPrenotazione(prenotazioneFidelity));
    }

    @Test
    void testAcquistaBiglietto() {
        // Acquisto biglietto
        clienteFedelta.acquistaBiglietto(prenotazione, "Luca", "Bianchi", 200.0, "premium");

        // Verifica che il biglietto sia stato aggiunto alla prenotazione
        assertEquals(1, prenotazione.getBiglietti().size());
        BigliettoFidelity biglietto = prenotazione.getBiglietti().get(0);

        // Verifica che i dati del biglietto siano corretti
        assertEquals("Luca", biglietto.getNomeBeneficiario());
        assertEquals("Bianchi", biglietto.getCognomeBeneficiario());
        assertEquals(200.0, biglietto.getPrezzo());
        assertEquals(clienteFedelta, biglietto.getCliente());
        assertEquals(volo, biglietto.getVolo());
        assertEquals("premium", biglietto.getTariffa());
    }

    @Test
    void testClone() {
        ClienteFedelta clone = clienteFedelta.clone();
        assertEquals(clienteFedelta, clone);
        assertNotSame(clienteFedelta, clone);
    }
}

