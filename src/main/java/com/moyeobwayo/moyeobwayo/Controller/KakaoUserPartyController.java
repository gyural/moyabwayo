package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.request.GetMeetListByKakaoIdRequest;
import com.moyeobwayo.moyeobwayo.Service.KakaoUserPartyService;
import com.moyeobwayo.moyeobwayo.Service.PartyService;
import com.moyeobwayo.moyeobwayo.Service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;



@RestController
@RequestMapping("api/v1/kakaouser")
public class KakaoUserPartyController {

    private final KakaoUserPartyService kakaoUserPartyService;
    private final PartyService partyService;

    public KakaoUserPartyController(KakaoUserPartyService kakaoUserPartyService, PartyService partyService) {
        this.kakaoUserPartyService = kakaoUserPartyService;
        this.partyService = partyService;
    }

    /**
     * POST /api/v1/kakaouser/meetlist
     * 카카오 사용자 id로 파티 조회
     * @param reqData
     * @return
     */
    @PostMapping("/meetlist")
    public ResponseEntity<?> getPartyByKakaoUserId(@RequestBody GetMeetListByKakaoIdRequest reqData) {
        try {
            // 서비스에서 kakao_user_id로 Party 조회
            Pageable pageable = PageRequest.of(reqData.getPage() - 1, reqData.getSize());
            List<Party> parties = kakaoUserPartyService.getPartyByKakaoUserId(reqData.getKakaoUserId(), pageable);
            return ResponseEntity.ok(parties);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("서버 오류: " + e.getMessage());
        }
    }

    @Operation(
            summary = "Delete user from party by party ID",
            description = "Disconnects the user associated with the given party ID."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "User disconnected from the party successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(type = "string"))
            ),

    })
    @DeleteMapping("/meetlist/{id}")
    public ResponseEntity<?> deletePartyByKakaoUserId(@PathVariable String id) {
        try {
            // 파티 ID를 사용하여 사용자와 파티 간의 연결 끊기
            partyService.disconnectUserFromParty(id);
            return ResponseEntity.ok("User disconnected from party successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error disconnecting user from party: " + e.getMessage());
        }
    }

    @PostMapping("/meetlist/alarm/{partyId}")
    public ResponseEntity<?> updateAlarmStatus(@PathVariable String partyId, @RequestBody Map<String, String> requestBody) {
        try {
            // 요청 바디에서 "alarm" 값을 가져옴
            String alarmStatus = requestBody.get("alarm");
            partyService.updateAlarmStatus(partyId, alarmStatus); // 인스턴스를 통해 메서드 호출
            return ResponseEntity.ok("Alarm status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error updating alarm status: " + e.getMessage());
        }
    }
}





