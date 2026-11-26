package tn.esprit.autoloc.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.autoloc.domain.Reservation;

public interface IReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByDateDebutAndDateFin(LocalDate dateDebut, LocalDate dateFin);

    @Query("""
            SELECT r FROM Reservation r
            JOIN FETCH r.vehicule
            WHERE r.dateFin < :today
              AND r.statut NOT IN (tn.esprit.autoloc.domain.StatutReservation.ANNULEE,
                                   tn.esprit.autoloc.domain.StatutReservation.TERMINEE)
            """)
    List<Reservation> findReservationsExpirees(@Param("today") LocalDate today);
}