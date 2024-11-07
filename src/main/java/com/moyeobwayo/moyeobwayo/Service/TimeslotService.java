package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeslotRequestDTO;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeslotResponseDTO;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import com.moyeobwayo.moyeobwayo.Repository.TimeslotRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimeslotService {

    private final TimeslotRepository timeslotRepository;
    private final UserEntityRepository userEntityRepository;
    private final DateEntityRepsitory dateEntityRepsitory;
    private final PartyRepository partyRepository;

    // 특정 파티에 속한 타임슬롯 조회
    public List<TimeslotResponseDTO> getTimeslotsByPartyId(String partyId) {
        List<Timeslot> timeslots = timeslotRepository.findAllByPartyId(partyId);
        return timeslots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 타임슬롯 생성
    public TimeslotResponseDTO createTimeslot(TimeslotRequestDTO dto) {
        UserEntity user = userEntityRepository.findById((long) dto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + dto.getUserId()));
        DateEntity date = dateEntityRepsitory.findById((long) dto.getDateId())
                .orElseThrow(() -> new IllegalArgumentException("날짜를 찾을 수 없습니다: " + dto.getDateId()));

        // 현재 userId에 해당하는 모든 타임슬롯 가져오기 (요청 전 상태 확인)
        List<Timeslot> userTimeslotsBefore = timeslotRepository.findTimeslotsByUserId((long) dto.getUserId());
        boolean hadAnyActiveTimeslot = userTimeslotsBefore.stream().anyMatch(ts -> ts.getByteString().contains("1")); // 기존 상태 확인

        // 타임슬롯 업데이트 또는 생성
        Optional<Timeslot> existingTimeslot = userTimeslotsBefore.stream()
                .filter(t -> t.getDate().getDateId().equals((long) dto.getDateId()))
                .findFirst();

        Timeslot timeslot;
        if (existingTimeslot.isPresent()) {
            timeslot = existingTimeslot.get();
            timeslot.setByteString(dto.getBinaryString());
        } else {
            timeslot = new Timeslot();
            timeslot.setUserEntity(user);
            timeslot.setDate(date);
            timeslot.setByteString(dto.getBinaryString());
        }

        // 변경된 타임슬롯 저장
        timeslotRepository.save(timeslot);

        // userId에 해당하는 모든 타임슬롯을 다시 가져와 변경 후 상태 확인
        List<Timeslot> userTimeslotsAfter = timeslotRepository.findTimeslotsByUserId((long) dto.getUserId());
        boolean hasAnyActiveTimeslotAfter = userTimeslotsAfter.stream().anyMatch(ts -> ts.getByteString().contains("1")); // 변경 후 상태 확인

        // current_num 업데이트 로직
        Party targetParty = date.getParty();
        if (!hadAnyActiveTimeslot && hasAnyActiveTimeslotAfter) { // 0 -> 1: current_num 증가
            targetParty.setCurrentNum(targetParty.getCurrentNum() + 1);
        } else if (hadAnyActiveTimeslot && !hasAnyActiveTimeslotAfter) { // 1 -> 0: current_num 감소
            targetParty.setCurrentNum(targetParty.getCurrentNum() - 1);
        }
        partyRepository.save(targetParty); // 변경된 파티 정보 저장

        return convertToDTO(timeslot);
    }




    private void validateByteString(String byteString, Date startTime, Date endTime) {
        Calendar startCal = Calendar.getInstance();
        startCal.setTime(startTime);
        int startHour = startCal.get(Calendar.HOUR_OF_DAY);
        int startMinute = startCal.get(Calendar.MINUTE);

        Calendar endCal = Calendar.getInstance();
        endCal.setTime(endTime);
        int endHour = endCal.get(Calendar.HOUR_OF_DAY);
        int endMinute = endCal.get(Calendar.MINUTE);

        // 시간 차이를 분 단위로 계산
        long expectedLength = ((endHour * 60 + endMinute) - (startHour * 60 + startMinute)) / 30;

        // byteString 길이 확인
        if (byteString.length() != expectedLength) {
            throw new IllegalArgumentException("binaryString의 길이가 예상 길이와 다릅니다: " + expectedLength);
        }

        // byteString이 "0"과 "1"로만 이루어져 있는지 확인
        if (!byteString.matches("[01]+")) {
            throw new IllegalArgumentException("binaryString은 0과 1로만 이루어져야 합니다.");
        }
    }
    // 타임슬롯 수정
    public TimeslotResponseDTO updateTimeslot(Timeslot timeslot, TimeslotRequestDTO dto, DateEntity date) {
        timeslot.setByteString(dto.getBinaryString());

        Date startTime = date.getParty().getStartDate();
        Date endTime = date.getParty().getEndDate();
        validateByteString(dto.getBinaryString(), startTime, endTime);

        Timeslot updatedTimeslot = timeslotRepository.save(timeslot);

        return convertToDTO(updatedTimeslot);
    }

    // 타임슬롯 삭제
    public void deleteTimeslot(Long id, Long userId, String partyId) {
        if (!timeslotRepository.existsById(id)) {
            throw new RuntimeException("타임 슬롯을 찾을 수 없습니다.");
        }

        timeslotRepository.deleteById(id);
        if (timeslotRepository.existsUserInPartyTimeslot(userId, partyId) == false) {
            //    해당 유저의 투표가 모두 사라진다면 currentNum없애기
            Party targetPary = partyRepository.findById(partyId).
                    orElseThrow(() -> new IllegalArgumentException("party is not found."));
            targetPary.setCurrentNum(targetPary.getCurrentNum() - 1);
        }
    }

    // Timeslot 객체를 TimeslotResponseDTO로 변환
    private TimeslotResponseDTO convertToDTO(Timeslot timeslot) {
        return new TimeslotResponseDTO(
                timeslot.getSlotId(),
                timeslot.getUserEntity().getUserId(),
                timeslot.getDate().getParty().getPartyId(),
                timeslot.getDate().getDateId(),
                timeslot.getByteString()
        );
    }

    public Map<String, Object> getTimeslotsByUserAndParty(Long userId, String partyId) {
        // 특정 파티와 유저에 대한 타임슬롯 조회
        List<Timeslot> timeslots = timeslotRepository.findTimeslotsByUserAndParty(userId, partyId);

        // 결과를 담을 Map 생성
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> dates = new ArrayList<>();

        // 타임슬롯의 날짜별로 binaryString 생성 및 저장
        for (Timeslot timeslot : timeslots) {
            Map<String, Object> dateInfo = new HashMap<>();
            dateInfo.put("dateId", timeslot.getDate().getDateId());
            dateInfo.put("binaryString", timeslot.getByteString());
            dates.add(dateInfo);
        }

        result.put("dates", dates);
        return result;
    }

}