package com.moyeobwayo.moyeobwayo.Domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import com.moyeobwayo.moyeobwayo.Domain.converter.StringListConverter;
import java.util.Date;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Decision {

    @Id
    @Column(name = "party_id", length = 255, nullable = false)
    private String partyId;

    @Column(name = "start_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date startTime;

    @Column(name = "end_time", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date endTime;

    @Convert(converter = StringListConverter.class)
    @Column(name = "possible_users", columnDefinition = "TEXT")
    private List<String> possibleUsers;

    @Convert(converter = StringListConverter.class)
    @Column(name = "impossible_users", columnDefinition = "TEXT")
    private List<String> impossibleUsers;
}
