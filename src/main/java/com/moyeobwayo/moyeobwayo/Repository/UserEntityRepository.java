package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserEntityRepository extends JpaRepository<UserEntity, Long> {
    List<UserEntity> findUserEntitiesByParty(Party party);

    @Query("SELECT u.kakaoProfile.kakaoUserId FROM UserEntity u WHERE u.party.partyId = :partyId")
    Optional<Long> findKakaoIDByPartyId(@Param("partyId") String partyId); // ★ partyId가 String으로 처리됨

    // ★ 파티 ID가 String으로 처리되도록 변경
    @Query("SELECT u FROM UserEntity u WHERE u.userName = :userName AND u.party.partyId = :partyId")
    Optional<UserEntity> findUserInSameParty(@Param("userName") String userName, @Param("partyId") String partyId);

    // ★ 파티 ID가 String으로 처리되도록 변경
    @Query("SELECT u FROM UserEntity u WHERE u.userId = :currentUserId")
    Optional<UserEntity> findByIdAndPartyId(@Param("currentUserId") int currentUserId);

    // 알림톡 관련 : 파티 생성자의 이름 찾아서 전화번호, 국가번호 조회 및 가공
    @Query("SELECT u FROM UserEntity u WHERE u.userName = :userName")
    Optional<UserEntity> findByUserName(@Param("userName") String userName);

    List<UserEntity> findUserEntitiesByKakaoProfile_KakaoUserId(Long kakaoUserId);

    Optional<UserEntity> findByKakaoProfile_KakaoUserId(Long kakaoUserId);

    List<UserEntity> findAllByParty_PartyId(String partyId);
}
