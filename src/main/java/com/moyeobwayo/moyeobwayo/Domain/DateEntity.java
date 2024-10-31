package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.moyeobwayo.moyeobwayo.Domain.dto.TimeslotUserDTO;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.TimeZone;

@Entity
@Getter
@Setter
public class DateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long dateId;

    private java.util.Date selected_date;

    @ManyToOne
    @JoinColumn(name = "party_id")
    @JsonIgnore  // Party를 직렬화에서 제외하여 순환 참조 방지
    private Party party;

    // @OneToMany(mappedBy = "date")
    @OneToMany(mappedBy = "date", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Timeslot> timeslots;

    // !!!!!!!!!!!!!!!!!!!!! 추가됨 !!!!!!!!!!!!!!!!!!!!!!!
    @Transient
    private List<TimeslotUserDTO> convertedTimeslots;  // JSON 응답에 사용할 타임슬롯 변환 데이터
    // !!!!!!!!!!!!!!!!!!!!! 추가됨 !!!!!!!!!!!!!!!!!!!!!!!

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
