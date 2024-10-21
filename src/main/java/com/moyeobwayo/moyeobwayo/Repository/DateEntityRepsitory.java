package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;

public interface DateEntityRepsitory extends JpaRepository<DateEntity, Integer> {
    @Query("SELECT d.dateId FROM DateEntity d WHERE d.party.partyId = :partyId AND FUNCTION('DATE', d.selected_date) = FUNCTION('DATE', :selectedDate)")
    Integer findDateIdByPartyAndSelectedDate(@Param("partyId") String partyId, @Param("selectedDate") Date selectedDate);
}
