package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCompleteRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCreateRequest;
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

import java.util.List;

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
                    content = @Content(schema = @Schema(implementation = Party.class))),  // Party 객체를 반환
            @ApiResponse(responseCode = "400", description = "Bad request, invalid data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),  // 에러 응답 정의
            @ApiResponse(responseCode = "404", description = "Party not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/complete/{id}")  // URL에서 id를 경로 변수로 받음
    public ResponseEntity<?> completeParty(@PathVariable("id") int id, @RequestBody PartyCompleteRequest partyCompleteRequest) {
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
    @GetMapping("/{id}")
    public ResponseEntity<?> getParty(@PathVariable int id) {
        List<AvailableTime> availableTimes = partyService.findAvailableTimesForParty(id);
        return ResponseEntity.ok(availableTimes);
    }

}
