package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.dto.PartyResponseDTO;
import com.moyeobwayo.moyeobwayo.Domain.dto.PartyUserResponseDTO;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCompleteRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCreateRequest;
import com.moyeobwayo.moyeobwayo.Domain.response.PartyCompleteResponse;
import com.moyeobwayo.moyeobwayo.Service.PartyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import com.moyeobwayo.moyeobwayo.Domain.dto.AvailableTime;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("api/v1/party")
public class PartyController {

    private final PartyService partyService;

    public PartyController(PartyService partyService) {
        this.partyService = partyService;
    }


    @Operation(summary = "Complete party", description = "Completes the party with the given ID and request details.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Party completed successfully",
                    content = @Content(schema = @Schema(implementation = PartyCompleteResponse.class))),  // Party 객체를 반환
            @ApiResponse(responseCode = "400", description = "Bad request, invalid data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),  // 에러 응답 정의
            @ApiResponse(responseCode = "404", description = "Party not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/complete/{id}")  // URL에서 id를 경로 변수로 받음
    public ResponseEntity<?> completeParty(@PathVariable("id") String id, @RequestBody PartyCompleteRequest partyCompleteRequest) {
        return partyService.partyComplete(id, partyCompleteRequest);
    }

    /**
     * 파티 생성
     * POST api/v1/party/create
     * @param partyCreateRequest
     * @return
     */
    @PostMapping("/create")
    public ResponseEntity<?> createParty(@RequestBody PartyCreateRequest partyCreateRequest) {
        return partyService.partyCreate(partyCreateRequest);
    }

    /**
     * 지정된 파티의 가능 여부 높은 시간 출력
     * GET api/v1/party/{id}
     * @param id
     * @return
     */
//    @GetMapping("/{id}")
//    public ResponseEntity<?> getParty(@PathVariable int id) {
//        List<AvailableTime> availableTimes = partyService.findAvailableTimesForParty(id);
//        return ResponseEntity.ok(availableTimes);
//    }
    @GetMapping("/{id}")
    public ResponseEntity<PartyResponseDTO> getParty(@PathVariable("id") String id) {
        PartyResponseDTO partyResponseDTO = new PartyResponseDTO();
        Party party = partyService.findPartyById(id);
        List<AvailableTime> availableTimes = partyService.findAvailableTimesForParty(party);

        // 3. 두 데이터를 하나의 Map에 담기
        partyResponseDTO.setParty(party);  // 파티 정보 추가
        partyResponseDTO.setAvailableTime(availableTimes);  // 우선순위 시간 정보 추가

        // 4. Map을 JSON으로 반환
        return ResponseEntity.ok(partyResponseDTO);
    }

    /**
     * 파티 수정
     * PUT api/v1/party/update/{partyId}
     * @param partyId
     * @param partyUpdateRequest
     * @return
     */
    @PutMapping("/update/{partyId}")
    public ResponseEntity<?> updateParty(@PathVariable String partyId, @RequestBody PartyCreateRequest partyUpdateRequest) {
        return partyService.updateParty(partyId, partyUpdateRequest);
    }

    /**
     * 파티에 참여하는 유저들의 정보 가져오기
     * (모든 유저들의 정보를 건네주고, 그 중 카카오 유저인 경우에는 해당 카카오 정보만 전달)
     * (일반 유저는 사진을 null값으로 전달해주고, 카카오 유저는 카카오 유저 테이블의 사진을 전달)
     * GET api/v1/party/{partyId}/users
     */
    @GetMapping("/{partyId}/users")
    public ResponseEntity<List<PartyUserResponseDTO>> getPartyUsers(@PathVariable String partyId) {
        // 1. 파티의 유저 목록 가져오기
        List<UserEntity> users = partyService.findUsersByPartyId(partyId);

        // 2. 유저 정보를 PartyUserResponseDTO로 변환
        List<PartyUserResponseDTO> userResponses = users.stream().map(user -> {
            String profileImage = null;
            if (user.getKakaoProfile() != null) {
                profileImage = user.getKakaoProfile().getProfile_image(); // 카카오 유저는 이미지 제공
            }
            return new PartyUserResponseDTO(user.getUserId(), user.getUserName(), profileImage);
        }).toList();

        // 3. 유저 정보 리스트 반환
        return ResponseEntity.ok(userResponses);
    }



}
