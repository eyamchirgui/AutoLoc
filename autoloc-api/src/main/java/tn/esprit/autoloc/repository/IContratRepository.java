package tn.esprit.autoloc.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.autoloc.domain.Contrat;

public interface IContratRepository extends JpaRepository<Contrat, Long> {

    @Query("""
            SELECT c FROM Contrat c
            JOIN FETCH c.reservation r
            JOIN FETCH r.client
            JOIN FETCH r.vehicule
            WHERE c.valide = true
              AND r.statut NOT IN (tn.esprit.autoloc.domain.StatutReservation.ANNULEE,
                                   tn.esprit.autoloc.domain.StatutReservation.TERMINEE)
              AND r.dateFin BETWEEN :today AND :horizon
            """)
    List<Contrat> findContratsArrivantAEcheance(@Param("today") LocalDate today,
                                                @Param("horizon") LocalDate horizon);
}