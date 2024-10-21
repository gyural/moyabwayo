package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeslotRequestDTO;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeslotResponseDTO;
import com.moyeobwayo.moyeobwayo.Repository.TimeslotRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TimeslotService {

    private final TimeslotRepository timeslotRepository;
    private final UserEntityRepository userEntityRepository;
    private final DateEntityRepsitory dateEntityRepsitory;

    public TimeslotService(TimeslotRepository timeslotRepository, UserEntityRepository userEntityRepository, DateEntityRepsitory dateEntityRepsitory) {
        this.timeslotRepository = timeslotRepository;
        this.userEntityRepository = userEntityRepository;
        this.dateEntityRepsitory = dateEntityRepsitory;
    }

    // 특정 파티에 속한 타임슬롯 조회
    public List<TimeslotResponseDTO> getTimeslotsByPartyId(String partyId) {
        List<Timeslot> timeslots = timeslotRepository.findAllByPartyId(partyId);
        return timeslots.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    // 타임슬롯 생성
    public TimeslotResponseDTO createTimeslot(TimeslotRequestDTO dto) {
        UserEntity user = userEntityRepository.findById((long) dto.getUser_id())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + dto.getUser_id()));
        DateEntity date = dateEntityRepsitory.findById(dto.getDate_id())
                .orElseThrow(() -> new IllegalArgumentException("날짜를 찾을 수 없습니다: " + dto.getDate_id()));

        Timeslot timeslot = new Timeslot();
        timeslot.setSelectedStartTime(dto.getSelected_start_time());
        timeslot.setSelectedEndTime(dto.getSelected_end_time());
        timeslot.setUserEntity(user);
        timeslot.setDate(date);
        //해당 유저가 파티에 참여인원으로 있는지 확인해야함
        Timeslot createdTimeslot = timeslotRepository.save(timeslot);
        return convertToDTO(createdTimeslot);
    }

    // 타임슬롯 수정
    public TimeslotResponseDTO updateTimeslot(int id, Date selectedStartTime, Date selectedEndTime) {
        Timeslot existingTimeslot = timeslotRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("타임 슬롯을 찾을 수 없습니다."));

        if (selectedStartTime.after(selectedEndTime)) {
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        existingTimeslot.setSelectedStartTime(selectedStartTime);
        existingTimeslot.setSelectedEndTime(selectedEndTime);

        Timeslot updatedTimeslot = timeslotRepository.save(existingTimeslot);
        return convertToDTO(updatedTimeslot);
    }

    // 타임슬롯 삭제
    public void deleteTimeslot(int id) {
        if (!timeslotRepository.existsById(id)) {
            throw new RuntimeException("타임 슬롯을 찾을 수 없습니다.");
        }
        timeslotRepository.deleteById(id);
    }

    // Timeslot 객체를 TimeslotResponseDTO로 변환
    private TimeslotResponseDTO convertToDTO(Timeslot timeslot) {
        return new TimeslotResponseDTO(
                timeslot.getSlotId(),
                timeslot.getSelectedStartTime(),
                timeslot.getSelectedEndTime(),
                timeslot.getUserEntity().getUserId(),
                timeslot.getDate().getParty().getPartyId(),
                timeslot.getDate().getDateId()
        );
    }
}
