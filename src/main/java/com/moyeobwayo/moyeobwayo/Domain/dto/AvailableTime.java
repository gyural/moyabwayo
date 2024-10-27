package com.moyeobwayo.moyeobwayo.Domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
public class AvailableTime {
    private LocalDateTime start;
    private LocalDateTime end;
    private List<String> users;

    @Override
    public String toString() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return "[" + start.format(formatter) + ", " + end.format(formatter) + ", " + users + "]";
    }
}
