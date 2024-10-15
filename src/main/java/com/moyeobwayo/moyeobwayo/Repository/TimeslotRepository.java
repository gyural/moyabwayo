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

    // 특정 날짜와 시간에 해당하는 사용자를 조회
    @Query("SELECT t.userEntity FROM Timeslot t WHERE t.date.date_id = :dateId AND :selectedTime BETWEEN t.selected_start_time AND t.selected_end_time")
    List<UserEntity> findUsersByDateAndTime(
            @Param("dateId") int dateId,
            @Param("selectedTime") Date selectedTime);

    // 특정 날짜에 해당하는 타임슬롯 조회
    List<Timeslot> findAllByDate(DateEntity date);

    // 특정 파티에 속한 타임슬롯 조회
    @Query("SELECT t FROM Timeslot t JOIN t.date d WHERE d.party.partyId = :partyId")
    List<Timeslot> findAllByPartyId(@Param("partyId") String partyId); // partyId String으로 수정
}
