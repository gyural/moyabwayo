package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.*;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeSlot;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCompleteRequest;
import com.moyeobwayo.moyeobwayo.Domain.request.party.PartyCreateRequest;
import com.moyeobwayo.moyeobwayo.Domain.response.PartyCompleteResponse;
import com.moyeobwayo.moyeobwayo.Domain.dto.PartyDTO;
import com.moyeobwayo.moyeobwayo.Repository.*;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.LazyInitializationException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.moyeobwayo.moyeobwayo.Domain.dto.AvailableTime;
import com.moyeobwayo.moyeobwayo.Repository.DecisionRepository; // 추가됨

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;

// *****************************
// 알림톡 관련
//import org.springframework.boot.configurationprocessor.json.JSONArray;
//import org.springframework.boot.configurationprocessor.json.JSONException;
//import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.transaction.annotation.Transactional; // lazy 로딩 문제 해결용

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
// *****************************

@Service
@RequiredArgsConstructor
public class PartyService {
    private final PartyStringIdRepository partyStringIdRepository;
    private final PartyRepository partyRepository;
    private final UserEntityRepository userRepository;
    private final TimeslotRepository timeslotRepository;
    private final DateEntityRepsitory dateEntityRepsitory;
    private final KakaoUserService kakaoUserService;
    private final AlarmRepository alarmRepository;
    private final DecisionRepository decisionRepository;
    private final kakaotalkalarmService kakaotalkalarmService;

    // *****************************
    // 알림톡 관련

    // 스케줄러 초기화
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /**
     * 알림톡 예약 전송 10분 (테스트 3초)
     */
    public void scheduleAlimTalk(Party party) {
        scheduler.schedule(() -> {
            try {
                sendAlimTalkToPartyCreator(party.getPartyId());
            } catch (Exception e) {
                System.err.println("알림톡 전송 실패: " + e.getMessage());
            }
        }, 10 * 60, TimeUnit.SECONDS);
        //}, 10, TimeUnit.MINUTES); // 10분 후 실행
    }

    /**
     * 알림톡 전송 및 message_send 업데이트
     */
    public void sendAlimTalkToPartyCreator(String partyId) {

        try {
            // Party 및 UserEntity 로드 (파티 생성자 조회)
            Party party = partyRepository.findByIdWithDatesAndTimeslots(partyId)
                    .orElseThrow(() -> new IllegalArgumentException("Party not found with ID: " + partyId));

            // 24.11.22) **변경된 부분: Set을 List로 변환**
            List<DateEntity> dateList = new ArrayList<>(party.getDates()); // Set -> List 변환
            if (dateList.isEmpty()) {
                throw new IllegalArgumentException("No dates found for party: " + partyId);
            }

            UserEntity partyCreator = userRepository.findByUserNameAndParty_PartyId(party.getUserId(), partyId)
                    .orElseThrow(() -> new IllegalArgumentException("파티 생성자를 찾을 수 없습니다: " + party.getUserId()));


            // KakaoProfile 로드 및 전화번호 확인
            KakaoProfile kakaoProfile = partyCreator.getKakaoProfile();
            if (kakaoProfile == null || kakaoProfile.getPhoneNumber() == null) {
                throw new IllegalArgumentException("파티 생성자의 전화번호를 찾을 수 없습니다.");
            }

            // 전화번호 가공
            String phoneNumber = formatPhoneNumber(kakaoProfile.getCountryCode(), kakaoProfile.getPhoneNumber());

            // 파티 이름 및 생성자 이름 정의
//            String partyName = party.getPartyName();       // 파티 이름
//            String partyLeaderName = party.getUserId();    // 파티 생성자 이름


            // 1. 모든 가능한 시간대 가져오기
            List<AvailableTime> availableTimes = findAvailableTimesForParty(party);

            // 2. 상위 3개 시간대 추출
            List<String> topTimeSlots = availableTimes.stream()
                    .limit(3) // 상위 3개만 선택 (limit(n) : 리스트에 요소가 n개보다 적다면 남은 요소를 그대로 반환)
                    .map(availableTime -> String.format("%s - %s",
                            availableTime.getStart().toString(),
                            availableTime.getEnd().toString())) // 시간대 이름 생성
                    .collect(Collectors.toList());

            // 시간대 전송 테스트용 더미데이터 리스트
            // List<String> topTimeSlots = List.of("시간대 1", "시간대 2", "시간대 3");

            // 3. 알림톡 전송
            try {
                kakaotalkalarmService.sendVotingCompletionAlimTalk(
                        party.getPartyId(),
                        party.getPartyName(),
                        party.getUserId(),
                        topTimeSlots,
                        phoneNumber
                );
                // party : 현재 파티 객체
                // topTimeSlots : 시간대 3개 슬라이싱해서 배열로 넘기기 (findAvailableTimesForParty 함수 호출 후 시간대 리스트 받아와서 진행)
                // -> 일단 빈배열 더미데이터로 진행
                // phoneNumber : 파티 생성자의 전화번호
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // message_send 업데이트
            party.setMessageSend(true);
            partyRepository.save(party);

            System.out.println("알림톡 전송 완료: Party ID: " + partyId);

        } catch (LazyInitializationException e) {
            System.err.println("LazyInitializationException 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            System.err.println("sendAlimTalkToPartyCreator 실행 중 예외 발생: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    // 전화번호 형식 변환(가공)
    // db 에 저장된 형태 : '10-1234-5678' -> 변환 : '01012345678'
    private String formatPhoneNumber(String countryCode, String phoneNumber) {
        if (countryCode == null || phoneNumber == null) {
            throw new IllegalArgumentException("전화번호 또는 국가 코드가 null입니다.");
        }
        // 하이픈 제거 및 숫자만 추출
        String formattedNumber = phoneNumber.replaceAll("[^0-9]", "");

        // 전화번호의 첫 자리가 0이 아니면 0 추가
        if (!formattedNumber.startsWith("0")) {
            formattedNumber = "0" + formattedNumber;
        }

        return formattedNumber; // 형식 예) 01012345678
    }

    // *****************************


    public void updateAlarmStatus(String partyId, String alarmStatus) {
        // partyId로 Alarm 객체 조회
        Optional<Alarm> alarmOptional = alarmRepository.findAlarmByParty_PartyId(partyId);

        if (alarmOptional.isPresent()) {
            Alarm alarm = alarmOptional.get();

            // "on" 또는 "off" 값에 따라 알람 상태를 설정
            if ("on".equalsIgnoreCase(alarmStatus)) {
                alarm.setAlarm_on(true);
            } else if ("off".equalsIgnoreCase(alarmStatus)) {
                alarm.setAlarm_on(false);
            } else {
                throw new IllegalArgumentException("Invalid alarm status value: " + alarmStatus);
            }

            // 변경 사항 저장
            alarmRepository.save(alarm);
        } else {
            throw new EntityNotFoundException("Alarm for Party with ID " + partyId + " not found.");
        }
    }

    /**
     * POST api/v1/party/complete/{id}
     * 일정 확정
     * @param id
     * @param partyCompleteRequest
     * @return
     */
    public ResponseEntity<?> partyComplete(String id, PartyCompleteRequest partyCompleteRequest){
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

        Party party = partyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Error: Party not found with id " + id));

        // Start and end time 설정
        Date startTime = partyCompleteRequest.getCompleteTime();
        Date endTime = partyCompleteRequest.getEndTime();
        String locationName = partyCompleteRequest.getLocationName() != null ? partyCompleteRequest.getLocationName() : "미정";

        // !!!!!!!!!!!!! 추가 됨 !!!!!!!!!!!!!!!!!!!!
        List<String> possibleUsersName = partyCompleteRequest.getUsers();
        List<String> possibleUsersId = partyCompleteRequest.getUsersId();

        // 파티에 속한 모든 유저 조회
        List<UserEntity> partyUsers = userRepository.findAllByParty_PartyId(id);

        // 가능한 유저 ID와 비교하여 불가능한 유저 필터링
        List<String> impossibleUsers = partyUsers.stream()
                .filter(user -> !possibleUsersId.contains(user.getUserId().toString()))
                .map(UserEntity::getUserName)
                .collect(Collectors.toList());


        // 새로운 Decision 엔티티 생성 및 설정
        Decision decision = new Decision();
        decision.setPartyId(id);
        decision.setStartTime(startTime);
        decision.setEndTime(endTime);
        decision.setPossibleUsers(possibleUsersName);
        decision.setImpossibleUsers(impossibleUsers); // 불가능한 유저 리스트 추가
        decisionRepository.save(decision);
        // !!!!!!!!!!!!! 추가 됨 !!!!!!!!!!!!!!!!!!!!

        // 확정 시간 DB 반영
        // **수정**: decisionDate을 boolean로 설정
        party.setDecisionDate(true); // 파티 확정을 의미하는 boolean 값 설정
        party.setLocationName(locationName);
        partyRepository.save(party);

        try {
            List<UserEntity> possibleUsers = getPossibleUsers(party, startTime, endTime, partyCompleteRequest.getDateId());
            // 메시지 전송
            System.out.println("possibleUsers");
            System.out.println(possibleUsers);
            Map<String, String> userMessageResponse = kakaoUserService.sendKakaoCompletMesage(possibleUsers, party, startTime);
            // 카카오 메시지를 보낼 수 없는 유저들 추가
            for (UserEntity user : possibleUsers) {
                // 메시지를 보낸 유저만 map에 포함되어 있으므로, 포함되지 않은 유저에게 "카카오 유저가 아님"을 추가
                userMessageResponse.putIfAbsent(user.getUserName(), "카카오 유저가 아님");
            }

            PartyCompleteResponse response = new PartyCompleteResponse(party, userMessageResponse);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
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
    public ResponseEntity<?> validatePartyExist(String id) {
        Party party = partyRepository.findById(id).orElse(null);
        if (party == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Error: Party not found"));
        }
        return null;  // 파티가 존재하면 null 반환
    }
    // 파티내의 목표시간에 가능한 유저리스트 반환
    public List<UserEntity> getPossibleUsers(Party party, Date targetDate, Date endDate, Long dateId) throws Exception {
        // DateID 조회
        Integer targetDateID = dateEntityRepsitory.findDateIdByPartyAndSelectedDate(party.getPartyId(), targetDate);  // 이제 String으로 처리
        if (targetDateID == null) {

            return new ArrayList<>();  // 빈 배열 반환
        }
        // 특정 시간 범위 안에 있는 UserEntity 조회
        int partyStartMinutes = party.getStartDate().getHours() * 60 + party.getStartDate().getMinutes();
        int descisionStartMinutes = targetDate.getHours() * 60 + targetDate.getMinutes();
        int descisionEndMinutes = endDate.getHours() * 60 + endDate.getMinutes();

        // 시작 및 종료 인덱스 계산 (30분 단위로 인덱스 변환)
        int startIndex = (descisionStartMinutes - partyStartMinutes) / 30;
        int endIndex = (descisionEndMinutes - partyStartMinutes) / 30;
        // 애내의 시간 차이 x2 가 byteString의 총길이이고 이때 targetDate ~ endDate -1 인덱스룰 구해야해
        List<Timeslot> timeslots = timeslotRepository.findAllByDateId(dateId);

        return  filterUsersByByteString(timeslots, startIndex, endIndex);
    }
    public List<UserEntity> filterUsersByByteString(List<Timeslot> timeslots, int startIndex, int endIndex) {
        List<UserEntity> filteredUsers = new ArrayList<>();
        for (Timeslot timeslot : timeslots) {
            String byteString = timeslot.getByteString();
            // 특정 범위가 모두 '1'인지 확인
            if (byteString.substring(startIndex, endIndex).equals("1".repeat(endIndex - startIndex))) {
                filteredUsers.add(timeslot.getUserEntity());
            }
        }
        return filteredUsers;
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
            if(partyCreateRequest.getParticipants()< 0 ||
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
            party.setTargetNum(partyCreateRequest.getParticipants());
            party.setPartyName(partyCreateRequest.getPartyTitle());
            party.setPartyDescription(partyCreateRequest.getPartyDescription());
            party.setStartDate(partyCreateRequest.getStartTime());
            party.setEndDate(partyCreateRequest.getEndTime());
            //**수정**
            party.setDecisionDate(false);  // 파티 생성 시 확정되지 않음을 의미하는 false 값 설정
            party.setUserId(partyCreateRequest.getUserId());

            party= partyRepository.save(party); // db에 저장 후 저장된 객체 반환(자동 생성된 id를 가져오기 위해)

            // 방금 생성한 Party 테이블 튜플의 pk 가져오기
            String party_id = party.getPartyId();

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

    public Party findPartyById(String partyId) {
        return partyStringIdRepository.findById(partyId)
                .orElseThrow(() -> new IllegalArgumentException("Party not found"));
    }


    /**
     * 특정 파티의 가용 여부 높은 시간을 찾는 메서드
     * @param party
     * @return List<AvailableTime>
     */
    public List<AvailableTime> findAvailableTimesForParty(Party party) {  // !!!!!!!!!!! 수정
        try {
            // 1. Party 객체 찾기
            // Party party = partyStringIdRepository.findById(partyId)  // !!!!!!!!!!! 수정
            //        .orElseThrow(() -> new IllegalArgumentException("Party not found"));
            ZoneId zoneId = ZoneId.systemDefault();
            LocalDateTime partyStartTime = LocalDateTime.ofInstant(party.getStartDate().toInstant(), ZoneId.systemDefault());
            LocalDateTime partyEndTime = LocalDateTime.ofInstant(party.getEndDate().toInstant(), ZoneId.systemDefault());
            // 2. Party와 연결된 모든 DateEntity의 Timeslot 가져오기

            List<TimeSlot> timeSlots = new ArrayList<>();

            // 24.11.22) **추가된 부분: Set을 List로 변환**
            List<DateEntity> dateList = new ArrayList<>(party.getDates()); // Set -> List 변환

            // 24.11.22) Optional: 정렬이 필요한 경우 (날짜 기준으로 정렬)
            dateList.sort(Comparator.comparing(DateEntity::getSelected_date));

            // 24.11.22) 기존 코드: for (DateEntity date : party.getDates()) {
            for (DateEntity date : dateList) {  // 변경된 부분: Set 대신 List 사용

                // date에서 날짜를 가져오고, party에서 시작 및 종료 시간을 가져옴
                LocalDateTime dateStart = LocalDateTime.ofInstant(date.getSelected_date().toInstant(), zoneId);

                // party의 날짜와 시간 합치기
                LocalDateTime startTime = dateStart.withHour(partyStartTime.getHour()).withMinute(partyStartTime.getMinute());
                LocalDateTime endTime = dateStart.withHour(partyEndTime.getHour()).withMinute(partyEndTime.getMinute());

                // 종료 시간이 00:00인지 확인하고 하루를 추가
                if (endTime.getHour() == 0 && endTime.getMinute() == 0) {
                    endTime = endTime.plusDays(1);
                }

                List<Timeslot> slots = new ArrayList<>(date.getTimeslots()); // Set -> List 변환
                // 24.11.22) Optional: 정렬이 필요한 경우 (slotId 기준으로 정렬)
                slots.sort(Comparator.comparing(Timeslot::getSlotId));

                for (Timeslot slot : slots) {
                    // 비트스트링을 통해 30분 단위로 시간 계산
                    String byteString = slot.getByteString();
                    int intervalMinutes = 30;

                    LocalDateTime currentSlotStart = startTime;
                    boolean inAvailableRange = false;
                    LocalDateTime rangeStart = null;

                    for (int i = 0; i < byteString.length(); i++) {
                        LocalDateTime currentTimePoint = currentSlotStart.plusMinutes(i * intervalMinutes);

                        // 현재 date와 파티의 시작/종료 시간 사이에서 유효한 범위 체크
                        if (currentTimePoint.isBefore(dateStart) || currentTimePoint.isAfter(endTime)) {
                            continue;
                        }

                        // 비트가 '1'일 때 사용 가능 시간으로 처리
                        if (byteString.charAt(i) == '1') {
                            if (!inAvailableRange) {
                                rangeStart = currentTimePoint;
                                inAvailableRange = true;
                            }
                        } else {
                            // 비트가 '0'이면 현재 범위가 끝났음을 기록하고 추가
                            if (inAvailableRange) {
                                timeSlots.add(new TimeSlot(
                                        slot.getUserEntity().getUserId(),          // userId 설정
                                        slot.getUserEntity().getUserName(),        // userName 설정
                                        rangeStart,
                                        currentTimePoint  // 종료 시간은 현재 30분 후
                                ));

                                inAvailableRange = false;
                            }
                        }
                    }

                    // 만약 마지막 구간이 1로 끝났다면 종료 시간까지의 구간 추가
                    if (inAvailableRange) {
                        timeSlots.add(new TimeSlot(
                                slot.getUserEntity().getUserId(),       // userId 설정
                                slot.getUserEntity().getUserName(),     // userName 설정
                                rangeStart,
                                endTime  // 마지막 종료 시간을 파티 종료 시간으로 설정
                        ));
                    }
                }
            }

            // 3. 가능한 시간대 찾기
            return findAvailableTimes(timeSlots);
        } catch (LazyInitializationException e) {
            // Lazy Loading 예외 발생 시 로그 출력
            System.err.println("LazyInitializationException 발생!");
            System.err.println("예외 메시지: " + e.getMessage());
            e.printStackTrace();
            throw e; // 예외를 다시 던져 호출자에게 전달
        }
    }


    /**
     * 주어진 TimeSlot 리스트를 이용하여 가능한 시간을 찾는 메서드
     * @param timeSlots
     * @return List<AvailableTime>
     */
    public List<AvailableTime> findAvailableTimes(List<TimeSlot> timeSlots) {
        Set<LocalDateTime> timePoints = new HashSet<>();
        for (TimeSlot slot : timeSlots) {
            timePoints.add(slot.getStart());
            timePoints.add(slot.getEnd());
        }

        List<LocalDateTime> sortedTimePoints = new ArrayList<>(timePoints);
        Collections.sort(sortedTimePoints);

        List<AvailableTime> availableTimes = new ArrayList<>();
        for (int i = 0; i < sortedTimePoints.size() - 1; i++) {
            LocalDateTime start = sortedTimePoints.get(i);
            LocalDateTime end = sortedTimePoints.get(i + 1);
            List<Map<String, Object>> usersAvailable = new ArrayList<>();

            for (TimeSlot slot : timeSlots) {
                if (!slot.getStart().isAfter(start) && !slot.getEnd().isBefore(end)) {
                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("userId", slot.getUserId());       // 사용자 ID 추가
                    userMap.put("userName", slot.getUserName());   // 사용자 이름 추가
                    usersAvailable.add(userMap);
                }
            }

            if (!usersAvailable.isEmpty()) {
                availableTimes.add(new AvailableTime(start, end, usersAvailable));
            }
        }

        availableTimes.sort((a, b) -> b.getUsers().size() - a.getUsers().size());
        return availableTimes;
    }


    /**
     * 만료된 파티를 삭제하는 메서드(url을 통해 접근하지 않기에 컨트롤러 없음)
     */
//    public void deleteExpiredParties() {
//        LocalDateTime currentDateTime = LocalDateTime.now(); // 현재 시간을 LocalDateTime으로 가져오기
//        Date currentDate = Date.from(currentDateTime.atZone(ZoneId.systemDefault()).toInstant()); // LocalDateTime을 Date로 변환
//
//        System.out.println("현재 시간: " + currentDate);
//        List<Party> expiredParties = partyRepository.findByEndDateBefore(currentDate);
//
//        if (!expiredParties.isEmpty()) {
//            partyRepository.deleteAll(expiredParties);
//            System.out.println(expiredParties.size() + "개의 만료된 파티를 삭제했습니다.");
//        } else {
//            System.out.println("삭제할 만료된 파티가 없습니다.");
//
//            // 모든 파티의 end_date를 출력
//            List<Party> allParties = partyRepository.findAll();
//            for (Party party : allParties) {
//                System.out.println("Party ID: " + party.getParty_id() + ", End Date: " + party.getEndDate());
//            }
//        }
//    }

    /**
     * 파티 정보 수정
     * @param partyId
     * @param partyUpdateRequest
     * @return
     */
    public ResponseEntity<?> updateParty(String partyId, PartyCreateRequest partyUpdateRequest) {
        try {
            // 1. 파티 존재 여부 확인
            Party existingParty = partyStringIdRepository.findById(partyId) // String 타입 partyId를 처리
                    .orElseThrow(() -> new IllegalArgumentException("Error: Party not found"));


            // 2. 수정할 필드 업데이트
            existingParty.setTargetNum(partyUpdateRequest.getParticipants());
            existingParty.setPartyName(partyUpdateRequest.getPartyTitle());
            existingParty.setPartyDescription(partyUpdateRequest.getPartyDescription());
            existingParty.setStartDate(partyUpdateRequest.getStartTime());
            existingParty.setEndDate(partyUpdateRequest.getEndTime());

            // **수정**: boolean 타입에 맞게 변경된 decisionDate 설정
            existingParty.setDecisionDate(partyUpdateRequest.isDecisionDate()); // boolean 값으로 설정

            existingParty.setUserId(partyUpdateRequest.getUserId());

            // 3. DateEntity 업데이트 (기존 리스트를 제거 후 새로 추가)
            // List<DateEntity> existingDates = existingParty.getDates();
            List<DateEntity> existingDates = new ArrayList<>(existingParty.getDates()); // 24.11.22) 에러 시 위 코드 사용, 이건 삭제

            dateEntityRepsitory.deleteAll(existingDates); // 기존 날짜 리스트 삭제

            List<DateEntity> newDates = new ArrayList<>();
            for (Date date : partyUpdateRequest.getDates()) {
                DateEntity dateEntity = new DateEntity();
                dateEntity.setSelected_date(date);
                dateEntity.setParty(existingParty);
                newDates.add(dateEntity);
            }
            dateEntityRepsitory.saveAll(newDates); // 새로운 날짜 리스트 저장

            // 4. 파티 정보 저장 (업데이트)
            partyRepository.save(existingParty);

            // 5. 수정된 파티 정보 반환
            return ResponseEntity.ok(existingParty);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }


    public void disconnectUserFromParty(String partyId) {
        // 파티 ID로 KakaoUserId 조회
        Optional<Long> kakaoUserIdOptional = userRepository.findKakaoIDByPartyId(partyId);

        if (kakaoUserIdOptional.isPresent()) {
            Long kakaoUserId = kakaoUserIdOptional.get();

            // KakaoUserId로 UserEntity 조회
            Optional<UserEntity> userOptional = userRepository.findByKakaoProfile_KakaoUserId(kakaoUserId);

            if (userOptional.isPresent()) {
                UserEntity userEntity = userOptional.get();
                // 파티와의 연결을 해제
                userEntity.setParty(null);
                // 카카오 프로필과의 연결을 해제 (필요하다면)
                userEntity.setKakaoProfile(null);
                // 업데이트된 정보를 저장
                userRepository.save(userEntity);
            } else {
                throw new EntityNotFoundException("User with kakaoUserId " + kakaoUserId + " not found.");
            }
        } else {
            throw new EntityNotFoundException("User with partyId " + partyId + " not found.");
        }
    }

    /**
     * 특정 파티에 속하는 모든 유저 정보를 반환
     * @param partyId 파티 ID
     * @return List<UserEntity>
     */
    public List<UserEntity> findUsersByPartyId(String partyId) {
        // 파티 ID를 통해 해당 파티에 속한 모든 유저를 조회
        return userRepository.findAllByParty_PartyId(partyId);
    }

}
