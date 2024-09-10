package Updatable.Volo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public interface VoloInterface {
    LocalTime orarioVolo();
    LocalDate dataVolo();
    LocalDateTime dataOraVolo();
    int disponibilitaVolo();
}//VoloInterface
