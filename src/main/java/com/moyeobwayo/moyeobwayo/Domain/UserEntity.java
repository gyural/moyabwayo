package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;

@Entity
@Getter
@Setter
@Table(
        name = "user_entity",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"kakao_user_id", "party_id"})
        }
)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "kakao_user_id", nullable = true) // 비어있어도 가능하도록 설정
    @JsonIgnore
    private KakaoProfile kakaoProfile; // 해당 table의 기본키를 참조

    private String userName;
    private String password;

    @OneToOne(mappedBy = "userEntity")
    @JsonIgnore  // 순환 참조 방지
    private Alarm alarm;

    //양방향 관계 필요 없고 무한루프를 유발함
    @OneToMany(mappedBy = "userEntity")
    @JsonIgnore // Timeslot에서 UserEntity를 참조할 때 무시
    private List<Timeslot> timeslots;

    @JsonProperty("party")
    @ManyToOne
    @JoinColumn(name="partyId", nullable = true)
    @JsonIgnore
    private Party party;


}
