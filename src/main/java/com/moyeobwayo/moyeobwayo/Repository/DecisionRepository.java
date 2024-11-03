package com.moyeobwayo.moyeobwayo.Repository;

import com.moyeobwayo.moyeobwayo.Domain.Decision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DecisionRepository extends JpaRepository<Decision, Long> {
    // partyId로 Decision 객체를 조회하는 메서드
    Optional<Decision> findByPartyId(String partyId);
}
