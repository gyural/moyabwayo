package com.moyeobwayo.moyeobwayo.Date;

import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Repository.DateEntityRepsitory;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Calendar;
import java.util.Date;
import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class DateEntityRepositoryTest {

    @Autowired
    private DateEntityRepsitory dateEntityRepsitory;

    @Autowired
    private PartyRepository partyRepository;

    private Party testParty;

    @BeforeEach
    public void setup() {
        // Set up a test party entity
        testParty = new Party();
        testParty.setPartyName("Test Party");
        partyRepository.save(testParty);

        // Create a DateEntity with a specific date
        DateEntity dateEntity = new DateEntity();
        dateEntity.setSelected_date(getDateWithoutTime(2, 2, 1)); // 1일의 날짜
        dateEntity.setParty(testParty);
        dateEntityRepsitory.save(dateEntity);
    }

    @Test
    public void testFindDateIdByPartyAndSelectedDate() {
        // Given
        Date selectedDate = getDateWithoutTime(2, 0, 1); // 1일의 날짜 (시간 다름)

        // When
        Integer dateId = dateEntityRepsitory.findDateIdByPartyAndSelectedDate(testParty.getPartyId(), selectedDate);
        // Then
        assertThat(dateId).isNotNull();
    }

    public static Date getDateWithoutTime(int day, int hour, int month) {
        // 현재 날짜를 기준으로 Calendar 객체 생성
        Calendar cal = Calendar.getInstance();

        // 특정 날짜 설정
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.MONTH, month - 1); // 0부터 시작하므로 -1
        cal.set(Calendar.YEAR, cal.get(Calendar.YEAR)); // 현재 연도 유지
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        // Calendar 객체에서 Date 객체로 변환
        return new Date(cal.getTimeInMillis());
    }
}