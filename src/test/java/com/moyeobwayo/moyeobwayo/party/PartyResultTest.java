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
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.text.SimpleDateFormat;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class PartyResultTest {
    @Autowired
    private MockMvc mockMvc;

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

    @MockBean
    private PartyService mockPartyService;
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
    @Test
    public void testGetPartyWithSortedDates() throws Exception {
        MockitoAnnotations.openMocks(this);
        Party mockParty = new Party();
        mockParty.setPartyName("Test Party");

        // 오름차순으로 정렬된 DateEntity 생성
        // 현재 날짜 기준으로 내일, 1주일 뒤, 1달 뒤 날짜 생성
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        // 내일 날짜
        calendar.add(Calendar.DATE, 1);
        DateEntity date1 = new DateEntity();
        date1.setSelected_date(calendar.getTime()); // 내일
        // 1주일 뒤 날짜
        calendar.add(Calendar.DATE, 6); // 1주일 후
        DateEntity date2 = new DateEntity();
        date2.setSelected_date(calendar.getTime());
        // 1달 뒤 날짜
        calendar.add(Calendar.MONTH, 1); // 1달 후
        DateEntity date3 = new DateEntity();
        date3.setSelected_date(calendar.getTime());

        List<DateEntity> sortedDates = Arrays.asList(date2, date1, date3);
        mockParty.setDates(sortedDates);

        // Service 호출 시 Mock 데이터를 반환하도록 설정
        when(mockPartyService.findPartyById("1")).thenReturn(mockParty);

        // SimpleDateFormat을 사용하여 날짜를 응답의 형식에 맞게 변환
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String formattedDate1 = dateFormat.format(date1.getSelected_date());
        String formattedDate2 = dateFormat.format(date2.getSelected_date());
        String formattedDate3 = dateFormat.format(date3.getSelected_date());
        // GET 요청을 통해 검증: dates가 오름차순으로 정렬되었는지 확인
        mockMvc.perform(get("/api/v1/party/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.party.dates[0].selected_date", is(formattedDate1)))
                .andExpect(jsonPath("$.party.dates[1].selected_date", is(formattedDate2)))
                .andExpect(jsonPath("$.party.dates[2].selected_date", is(formattedDate3)));
    }
}
