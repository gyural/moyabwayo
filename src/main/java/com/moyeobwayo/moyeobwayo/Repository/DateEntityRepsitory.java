package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface DateEntityRepsitory extends JpaRepository<DateEntity, Long> {
    @Query("SELECT d.dateId FROM DateEntity d WHERE d.party.partyId = :partyId AND FUNCTION('DATE', d.selected_date) = FUNCTION('DATE', :selectedDate)")
    Integer findDateIdByPartyAndSelectedDate(@Param("partyId") String partyId, @Param("selectedDate") Date selectedDate);

    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("SELECT d.party FROM DateEntity d WHERE d.dateId = :dateId")
    Party findPartyByDateIdWithLock(@Param("dateId") Long dateId);
}
