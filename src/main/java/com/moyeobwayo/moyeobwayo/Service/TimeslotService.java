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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Optional;
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

        // 해당 date에 해당하는 모든 timeslot 가져오기
        List<Timeslot> timeslots = timeslotRepository.findTimeslotsByUserIdAndDateId(date.getDateId());

        // 요청받은 userId가 존재하는지 확인
        Optional<Timeslot> optionalTimeslot = timeslots.stream()
                .filter(t -> t.getUserEntity().getUserId().equals(user.getUserId()))
                .findFirst();

        if (optionalTimeslot.isPresent()) {
            System.out.println("기존 Timeslot이 존재합니다.");
            return updateTimeslot(optionalTimeslot.get(), dto, date); // 첫 번째 요소 업데이트
        } else {
            System.out.println("새로운 Timeslot 생성 중...");
            String byteString = dto.getBinaryString();

            // 시작 시간과 종료 시간을 가져오기
            Date startTime = date.getParty().getStartDate();
            Date endTime = date.getParty().getEndDate();
            // byteString 검증 메서드 호출
            validateByteString(byteString, startTime, endTime);

            Timeslot timeslot = new Timeslot();
            timeslot.setUserEntity(user);
            timeslot.setDate(date);
            timeslot.setByteString(byteString);

            // 해당 유저가 파티에 참여 중인지 확인
            boolean newUserFlag = timeslotRepository.existsUserInPartyTimeslot(user.getUserId(), date.getParty().getPartyId());
            if (newUserFlag) {
                Party targetParty = partyRepository.findById(date.getParty().getPartyId())
                        .orElseThrow(() -> new IllegalArgumentException("올바른 파티가 아닙니다."));
                targetParty.setCurrentNum(targetParty.getCurrentNum() + 1);
            }

            Timeslot createdTimeslot = timeslotRepository.save(timeslot);
            return convertToDTO(createdTimeslot);
        }
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
}
