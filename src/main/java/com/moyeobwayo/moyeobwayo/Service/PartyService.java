package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeSlot;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCompleteRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCreateRequest;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import com.moyeobwayo.moyeobwayo.Repository.TimeslotRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.moyeobwayo.moyeobwayo.Domain.dto.AvailableTime;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.*;
import java.util.Map;

@Service
public class PartyService {
    private PartyRepository partyRepository;
    private UserEntityRepository userRepository;
    private TimeslotRepository timeslotRepository;
    private DateEntityRepsitory dateEntityRepsitory;
    private KakaoUserService kakaoUserService;

    /**
     * 의존성 주입
     * @param partyRepository
     * @param userRepository
     * @param timeslotRepository
     * @param dateEntityRepsitory
     * @param kakaoUserService
     */

    public PartyService(PartyRepository partyRepository,
                        UserEntityRepository userRepository,
                        TimeslotRepository timeslotRepository,
                        DateEntityRepsitory dateEntityRepsitory,
                        KakaoUserService kakaoUserService) {
        this.partyRepository = partyRepository;
        this.userRepository = userRepository;
        this.timeslotRepository = timeslotRepository;
        this.dateEntityRepsitory = dateEntityRepsitory;
        this.kakaoUserService = kakaoUserService;
    }

    /**
     * POST api/v1/party/complete/{id}
     * 일정 확정
     * @param id
     * @param partyCompleteRequest
     * @return
     */
    public ResponseEntity<?> partyComplete(int id, PartyCompleteRequest partyCompleteRequest){
        // 1. 필수값 검증
        ResponseEntity<?> validationResponse = validateRequireValues(partyCompleteRequest);
        if (validationResponse != null) {
            return validationResponse;
        }

        // 2. 파티 존재 검증
        ResponseEntity<?> partyValidationResponse = validatePartyExist(id);
        if (partyValidationResponse != null) {
            return partyValidationResponse;
        }

        Party party = partyRepository.findById(id).orElse(null);
        List<UserEntity> targetUsers = userRepository.findUserEntitiesByParty(party);
        if(targetUsers.size() > 0){
            // kakaoUser라면 리마인드 메시지 전송
            Date reqDate = partyCompleteRequest.getCompleteTime();
            String locationName = partyCompleteRequest.getLocationName() != null ? partyCompleteRequest.getLocationName() : "미정";
            // 확정 시간 DB 반영
            party.setDecision_date(reqDate);
            party.setLocation_name(locationName);
            try {
                List<UserEntity> possibleUsers = getPossibleUsers(party, reqDate);
                // 메시지 전송
                sendCompleteMsg(possibleUsers, party, reqDate);
                partyRepository.save(party);
                return ResponseEntity.ok(party);
            } catch (Exception e) {
                return ResponseEntity.badRequest().body("Error: " + e.getMessage());
            }
        }else{
            // 확정 시간 DB 반영
            Date reqDate = partyCompleteRequest.getCompleteTime();
            party.setDecision_date(reqDate);
            return ResponseEntity.ok(party);
        }
    }

    // 필수값 검증 모듈
    public ResponseEntity<?> validateRequireValues(PartyCompleteRequest partyCompleteRequest) {
        if (partyCompleteRequest.getUserId() == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: User ID is required."));
        }
        if (partyCompleteRequest.getCompleteTime() == null) {
            return ResponseEntity.badRequest().body(Map.of("message",  "Complete time is required."));
        }
        return null;  // 검증 통과 시 null 반환
    }

    // 파티 존재 검증 모듈
    public ResponseEntity<?> validatePartyExist(int id) {
        Party party = partyRepository.findById(id).orElse(null);
        if (party == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Party not found"));
        }
        return null;  // 파티가 존재하면 null 반환
    }
    // 파티내의 목표시간에 가능한 유저리스트 반환
    public List<UserEntity> getPossibleUsers( Party party, Date targetDate) {
        // DateID 조회
        Integer targetDateID = dateEntityRepsitory.findDateIdByPartyAndSelectedDate(party.getParty_id(), targetDate);
        if (targetDateID == null) {
            return new ArrayList<UserEntity>();  // 빈 배열 반환
        }
        // 특정 시간 범위 안에 있는 UserEntity 조회
        return timeslotRepository.findUsersByDateAndTime(targetDateID, targetDate);
    }
    public void sendCompleteMsg (List<UserEntity> targetUsers, Party party, Date completeDate){
        // kakao 확정 메시지 보내기
        kakaoUserService.sendKakaoCompletMesage(targetUsers, party, completeDate);
    }


    /**
     * POST api/v1/party/create
     * 파티 생성
     * @param partyCreateRequest
     * @return
     */
    public ResponseEntity<?> partyCreate(PartyCreateRequest partyCreateRequest){
        try{
            // 필수 값 검증(값이 정상적으로 전달되었는지 검증), partyDescription은 null 혹은 empty로 와도 가능하게 함.
            if(partyCreateRequest.getParticipants()<=0 ||
                    partyCreateRequest.getPartyTitle()==null || partyCreateRequest.getPartyTitle().isEmpty() ||
                    partyCreateRequest.getStartTime()==null || partyCreateRequest.getEndTime()==null ||
                    partyCreateRequest.getDates()==null || partyCreateRequest.getDates().isEmpty()){

                return ResponseEntity.badRequest().body("Error: 필요한 값이 전부 넘어오지 않음");
            }

            // 파티 시간 유효성 검증(startTime, endTime이 타당한지)
            if(!partyCreateRequest.getStartTime().before(partyCreateRequest.getEndTime())){
                return ResponseEntity.badRequest().body("Error: 시작 시간보다 종료 시간이 더 빠름");
            }

            // 파티 객체 생성 및 Party 테이블에 삽입
            Party party = new Party();
            party.setTarget_num(partyCreateRequest.getParticipants());
            party.setParty_name(partyCreateRequest.getPartyTitle());
            party.setParty_description(partyCreateRequest.getPartyDescription());
            party.setStart_date(partyCreateRequest.getStartTime());
            party.setEndDate(partyCreateRequest.getEndTime());
            party.setDecision_date(partyCreateRequest.getDecisionDate());
            party= partyRepository.save(party); // db에 저장 후 저장된 객체 반환(자동 생성된 id를 가져오기 위해)

            // 방금 생성한 Party 테이블 튜플의 pk 가져오기
            int party_id = party.getParty_id();

            // Party의 pk와 List<Date>를 이용하여 date_entity 테이블에 삽입
            List<DateEntity> dateEntities = new ArrayList<>();
            for(Date date: partyCreateRequest.getDates()){
                DateEntity dateEntity = new DateEntity();
                dateEntity.setSelected_date(date);
                dateEntity.setParty(party);

                dateEntities.add(dateEntity);
            }
            dateEntityRepsitory.saveAll(dateEntities); // db에 저장

            // 모든 과정이 정상적으로 수행되었다면, status(200) 반환
            return ResponseEntity.ok(party);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * 특정 파티의 가용 여부 높은 시간을 찾는 메서드
     * @param partyId
     * @return List<AvailableTime>
     */
    public List<AvailableTime> findAvailableTimesForParty(int partyId) {
        // 1. Party 객체 찾기
        Party party = partyRepository.findById(partyId).orElseThrow(() -> new IllegalArgumentException("Party not found"));

        // 2. Party와 연결된 모든 DateEntity의 Timeslot 가져오기
        List<TimeSlot> timeSlots = new ArrayList<>();
        for (DateEntity date : party.getDates()) {
            List<Timeslot> timeslots = timeslotRepository.findAllByDate(date);
            for (Timeslot slot : timeslots) {
                // Convert Timeslot to TimeSlot DTO
                LocalDateTime start = slot.getSelected_start_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                LocalDateTime end = slot.getSelected_end_time().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                timeSlots.add(new TimeSlot(slot.getUserEntity().getUser_name(), start, end));
            }
        }

        // 3. 가능한 시간대 찾기
        return findAvailableTimes(timeSlots);
    }

    /**
     * 주어진 TimeSlot 리스트를 이용하여 가능한 시간을 찾는 메서드
     * @param timeSlots
     * @return List<AvailableTime>
     */
    public List<AvailableTime> findAvailableTimes(List<TimeSlot> timeSlots) {
        // 모든 시작 및 종료 시간 추출
        Set<LocalDateTime> timePoints = new HashSet<>();
        for (TimeSlot slot : timeSlots) {
            timePoints.add(slot.getStart());
            timePoints.add(slot.getEnd());
        }

        // 시간 순서대로 정렬
        List<LocalDateTime> sortedTimePoints = new ArrayList<>(timePoints);
        Collections.sort(sortedTimePoints);

        // 가능한 모든 시간 범위 생성
        List<AvailableTime> availableTimes = new ArrayList<>();
        for (int i = 0; i < sortedTimePoints.size() - 1; i++) {
            LocalDateTime start = sortedTimePoints.get(i);
            LocalDateTime end = sortedTimePoints.get(i + 1);
            List<String> usersAvailable = new ArrayList<>();

            // 각 사용자에 대해 해당 시간 범위에 가능한지 확인
            for (TimeSlot slot : timeSlots) {
                if (!slot.getStart().isAfter(start) && !slot.getEnd().isBefore(end)) {
                    usersAvailable.add(slot.getUser());
                }
            }

            if (!usersAvailable.isEmpty()) {
                availableTimes.add(new AvailableTime(start, end, usersAvailable));
            }
        }

        // 가능한 사용자 수에 따라 정렬
        availableTimes.sort((a, b) -> b.getUsers().size() - a.getUsers().size());

        return availableTimes;
    }

    /**
     * 만료된 파티를 삭제하는 메서드(url을 통해 접근하지 않기에 컨트롤러 없음)
     */

    public void deleteExpiredParties() {
        LocalDateTime currentDateTime = LocalDateTime.now(); // 현재 시간을 LocalDateTime으로 가져오기
        Date currentDate = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant()); // LocalDateTime을 Date로 변환

        System.out.println("현재 시간: " + currentDate);
        List<Party> expiredParties = partyRepository.findByEndDateBefore(currentDate);

        if (!expiredParties.isEmpty()) {
            partyRepository.deleteAll(expiredParties);
            System.out.println(expiredParties.size() + "개의 만료된 파티를 삭제했습니다.");
        } else {
            System.out.println("삭제할 만료된 파티가 없습니다.");

            // 모든 파티의 end_date를 출력
            List<Party> allParties = partyRepository.findAll();
            for (Party party : allParties) {
                System.out.println("Party ID: " + party.getParty_id() + ", End Date: " + party.getEndDate());
            }
        }
    }

}
