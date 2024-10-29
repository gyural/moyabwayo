package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;

public interface TimeslotRepository extends JpaRepository<Timeslot, Long> {

    //startIndex ~ endIndex-1까지 모두 1인 유저만 가져옴
    @Query("SELECT t FROM Timeslot t WHERE t.date.dateId = :dateId")
    List<Timeslot> findAllByDateId(@Param("dateId") Long dateId);
    // 특정 날짜에 해당하는 타임슬롯 조회

    // 특정 파티에 속한 타임슬롯 조회
    @Query("SELECT t FROM Timeslot t JOIN t.date d WHERE d.party.partyId = :partyId")
    List<Timeslot> findAllByPartyId(@Param("partyId") String partyId); // partyId String으로 수정

    //특정 파티 timeslot에 해당 user가 있는지 확인
    @Query("SELECT COUNT(t) > 0 FROM Timeslot t WHERE t.userEntity.userId = :userId AND t.date.party.partyId = :partyId")
    boolean existsUserInPartyTimeslot(@Param("userId") Long userId, @Param("partyId") String partyId);
}
