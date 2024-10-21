package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;
import java.util.TimeZone;

@Entity
@Getter
@Setter
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;

    private Date selectedStartTime;
    private Date selectedEndTime;

    @ManyToOne
    @JoinColumn(name = "date_id")
    @JsonIgnore  // 순환 참조 방지
    private DateEntity date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @JsonIgnore  // 순환 참조 방지
    private UserEntity userEntity;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
