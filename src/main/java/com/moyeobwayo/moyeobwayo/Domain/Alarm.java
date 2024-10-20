package com.moyeobwayo.moyeobwayo.Domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor @AllArgsConstructor
public class Alarm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long alarmId;

    private boolean alarm_on = true;

    @OneToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id")
    @JsonIgnore  // 순환 참조 방지
    private UserEntity userEntity;

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "party_id")

    @JsonIgnore
    private Party party;
}
