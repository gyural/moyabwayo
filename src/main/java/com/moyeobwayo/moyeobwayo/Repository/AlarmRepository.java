package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AlarmRepository extends JpaRepository<Alarm, Long> {
    Optional<Alarm> findAlarmByParty_PartyId(String partyId);

    @Query("SELECT a FROM Alarm a WHERE a.party.partyId = :partyId AND a.userEntity.kakaoProfile.kakaoUserId = :kakaoUserId")
    Optional<Alarm> findAlarmByPartyIdAndKakaoUserId(@Param("partyId") String partyId, @Param("kakaoUserId") String kakaoUserId);}
