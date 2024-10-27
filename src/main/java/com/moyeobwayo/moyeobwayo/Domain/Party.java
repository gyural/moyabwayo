package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.UUID;

@Entity
@Getter
@Setter
public class Party {

    @Id
    private String partyId = UUID.randomUUID().toString();  // UUID를 기본 값으로 설정
    // @GeneratedValue(strategy = GenerationType.IDENTITY)
    // private int party_id;

    private int targetNum;
    private int currentNum;
    private String partyName;
    private String partyDescription;
    private Date startDate;
    private String locationName;
    @Column(name = "end_date") // jpa를 통한 삭제를 위해(카멜형으로)
    private Date endDate;
    private Date decisionDate;
    private String userId; // 새롭게 추가

    @OneToMany(mappedBy = "party")
    //@JsonIgnore  // 순환 참조 방지
    private List<Alarm> alarms;

    // @OneToMany(mappedBy = "party")
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DateEntity> dates;

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }
}
