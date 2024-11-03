package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.Decision;
import com.moyeobwayo.moyeobwayo.Repository.DecisionRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/decision")
public class DecisionController {

    private final DecisionRepository decisionRepository;

    public DecisionController(DecisionRepository decisionRepository) {
        this.decisionRepository = decisionRepository;
    }

    // 특정 party_id에 해당하는 Decision 데이터를 가져오는 엔드포인트
    @GetMapping("/{id}")
    public ResponseEntity<?> getDecisionByPartyId(@PathVariable("id") String partyId) {
        Optional<Decision> decision = decisionRepository.findByPartyId(partyId);

        if (decision.isPresent()) {
            // 데이터가 있으면 OK 상태로 반환
            return ResponseEntity.ok(decision.get());
        } else {
            // 데이터가 없으면 404 Not Found 반환
            return ResponseEntity.status(404).body("Error: Decision not found for partyId: " + partyId);
        }
    }
}
