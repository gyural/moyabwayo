package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Date;
import java.util.TimeZone;

@Entity
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class Timeslot {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int slotId;

    private Date selectedStartTime;
    private Date selectedEndTime;
    private String byteString;

    @ManyToOne
    @JoinColumn(name = "date_id")
    @JsonIgnore  // 순환 참조 방지
    private DateEntity date;

    @ManyToOne
    @JoinColumn(name = "user_id")
    //@JsonIgnore  // 순환 참조 방지
    private UserEntity userEntity;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
