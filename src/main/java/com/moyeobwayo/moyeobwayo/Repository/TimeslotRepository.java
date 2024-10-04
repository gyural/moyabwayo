package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Integer> {
    @Query("SELECT t.userEntity FROM Timeslot t WHERE t.date.date_id = :dateId AND :selectedTime BETWEEN t.selected_start_time AND t.selected_end_time")
    List<UserEntity> findUsersByDateAndTime(
            @Param("dateId") int dateId,
            @Param("selectedTime") Date selectedTime);

    List<Timeslot> findAllByDate(DateEntity date); // 임시 추가(심동근)

    // 특정 파티에 속한 타임슬롯을 조회하는 쿼리 추가
    @Query("SELECT t FROM Timeslot t WHERE t.date.party.party_id = :partyId")
    List<Timeslot> findByPartyId(@Param("partyId") int partyId);
}
