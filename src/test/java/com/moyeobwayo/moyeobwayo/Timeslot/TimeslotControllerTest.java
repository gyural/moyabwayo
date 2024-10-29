package com.moyeobwayo.moyeobwayo.Timeslot;

import com.moyeobwayo.moyeobwayo.Controller.TimeslotController;
import com.moyeobwayo.moyeobwayo.Domain.Timeslot;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.DateEntity;
import com.moyeobwayo.moyeobwayo.Service.TimeslotService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TimeslotController.class)
public class TimeslotControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TimeslotService timeslotService;

    @Test
    public void testCreateTimeslot() throws Exception {
        Timeslot timeslot = new Timeslot();
        timeslot.setSlotId(1);

        UserEntity userEntity = new UserEntity();
        userEntity.setUserId(9L);
        timeslot.setUserEntity(userEntity);

        DateEntity dateEntity = new DateEntity();
        dateEntity.setDateId(2);
        timeslot.setDate(dateEntity);

        //Mockito.when(timeslotService.createTimeslot(Mockito.any(Timeslot.class))).thenReturn(timeslot);

        mockMvc.perform(post("/api/v1/timeslots")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"selected_start_time\": \"2024-10-02T14:00:00\", \"selected_end_time\": \"2024-10-02T16:00:00\", \"userEntity\": { \"user_id\": 9 }, \"date\": { \"date_id\": 2 } }"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.user_id").value(9))
                .andExpect(jsonPath("$.date_id").value(2));
    }
}
