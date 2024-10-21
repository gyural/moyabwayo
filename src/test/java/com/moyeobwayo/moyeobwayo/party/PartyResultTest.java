package com.moyeobwayo.moyeobwayo.party;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import com.moyeobwayo.moyeobwayo.Repository.PartyStringIdRepository;  // !!!!!!!!!!! 수정
import com.moyeobwayo.moyeobwayo.Repository.TimeslotRepository;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import com.moyeobwayo.moyeobwayo.Service.PartyService;
import com.moyeobwayo.moyeobwayo.Domain.dto.AvailableTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class PartyResultTest {

    @Autowired
    private PartyService partyService;

    @Autowired
    private PartyStringIdRepository partyStringIdRepository;  // 수정

    @Autowired
    private UserEntityRepository userRepository;

    @Autowired
    private TimeslotRepository timeslotRepository;

    @Autowired
    private DateEntityRepsitory dateEntityRepsitory;

    @Test
    @Transactional
    public void 파티결과테스트() {
        // Step 1: Create a party
        Party party = new Party();
        party.setPartyName("Test Party");
        party.setPartyDescription("This is a test party.");
        party.setTargetNum(4);
        party.setCurrentNum(0);
        party.setStartDate(new Date());
        party.setEndDate(new Date(System.currentTimeMillis() + 86400000)); // 1 day later

        // Save the party
        Party savedParty = partyStringIdRepository.save(party);  // 수정

        // Step 2: Create dates for the party
        List<DateEntity> dateEntities = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            DateEntity dateEntity = new DateEntity();
            dateEntity.setSelected_date(new Date(System.currentTimeMillis() + (i * 86400000L)));
            dateEntity.setParty(savedParty);
            dateEntities.add(dateEntity);
        }

        // Save dates and set the relationship in the party
        dateEntityRepsitory.saveAll(dateEntities);
        savedParty.setDates(dateEntities);
        partyStringIdRepository.save(savedParty);  // 수정

        // Step 3: Create users
        UserEntity user1 = new UserEntity();
        user1.setUserName("user1");
        UserEntity user2 = new UserEntity();
        user2.setUserName("user2");
        UserEntity user3 = new UserEntity();
        user3.setUserName("user3");
        UserEntity user4 = new UserEntity();
        user4.setUserName("user4");

        // Save users
        userRepository.save(user1);
        userRepository.save(user2);
        userRepository.save(user3);
        userRepository.save(user4);

        // Step 4: Create timeslots for users
        Timeslot timeslot1 = new Timeslot();
        timeslot1.setSelectedStartTime(new Date());
        timeslot1.setSelectedEndTime(new Date(System.currentTimeMillis() + 3600000)); // 1 hour
        timeslot1.setDate(dateEntities.get(0));
        timeslot1.setUserEntity(user1);

        Timeslot timeslot2 = new Timeslot();
        timeslot2.setSelectedStartTime(new Date());
        timeslot2.setSelectedEndTime(new Date(System.currentTimeMillis() + 7200000)); // 2 hours
        timeslot2.setDate(dateEntities.get(1));
        timeslot2.setUserEntity(user2);

        // Save timeslots
        timeslotRepository.save(timeslot1);
        timeslotRepository.save(timeslot2);

        // Step 5: Call the service method to get the available times
        List<AvailableTime> availableTimes = partyService.findAvailableTimesForParty(savedParty.getPartyId());  // 수정

        // Step 6: Assertions to verify the results
        assertThat(availableTimes).isNotEmpty(); // 비어 있지 않은지
        assertThat(availableTimes.get(0).getUsers()).contains("user1", "user2"); // 두 개 다 포함하는지
    }

}
