package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.annotation.PostConstruct;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

import java.awt.print.PrinterJob;
import java.text.SimpleDateFormat;
import java.util.*;

@Entity
@Getter
@Setter
public class Party {

    @Id
    // private String partyId = UUID.randomUUID().toString();  // UUID를 기본 값으로 설정
    private String partyId = generatePartyId();

    private int targetNum;
    private int currentNum;
    private String partyName;
    private String partyDescription;
    private Date startDate;
    private String locationName;

    @Column(name = "end_date") // jpa를 통한 삭제를 위해(카멜형으로)
    private Date endDate;

    private boolean decisionDate; // boolean으로 변경

    private String userId; // 새롭게 추가

    private boolean messageSend = false; // 메세지 전송 여부
    @OneToMany(mappedBy = "party")
    //@JsonIgnore  // 순환 참조 방지
    private List<Alarm> alarms;


    // @OneToMany(mappedBy = "party")
    // ----------------Lazy Loading의 원흉----------------
    @OneToMany(mappedBy = "party", cascade = CascadeType.ALL, orphanRemoval = true)
    // private List<DateEntity> dates;
    private Set<DateEntity> dates; // 24.11.22) 변경: List -> Set : 기능 문제시 삭제 후 위 코드 사용
    // --------------------------------------------------

    @PostConstruct
    public void init() {
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    private String generatePartyId() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        String currentDateTime = dateFormat.format(new Date()); // 현재 날짜 및 시간
        String uuid = UUID.randomUUID().toString(); // UUID 생성
        return currentDateTime + uuid; // 날짜-시간 + UUID 조합
    }
}
