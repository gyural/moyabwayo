package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
public interface PartyRepository extends JpaRepository<Party, String> {
    List<Party> findByEndDateBefore(Date date);

    @Query("SELECT p FROM Party p JOIN FETCH p.dates WHERE p.partyId = :partyId")
    Optional<Party> findPartyWithDates(@Param("partyId") String partyId);

    @Query("SELECT p FROM Party p LEFT JOIN FETCH p.dates WHERE p.partyId = :partyId")
    Optional<Party> findByIdWithDates(@Param("partyId") String partyId);

    // Lazy Loading 해결용
    @Query("SELECT p FROM Party p " +
            "LEFT JOIN FETCH p.dates d " +
            "LEFT JOIN FETCH d.timeslots " +
            "WHERE p.partyId = :partyId")
    Optional<Party> findByIdWithDatesAndTimeslots(@Param("partyId") String partyId);
}
