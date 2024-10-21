package com.moyeobwayo.moyeobwayo.Domain.dto;

import java.util.Date;

public class TimeslotResponseDTO {

    private int slot_id;
    private Date selected_start_time;
    private Date selected_end_time;
    private Long user_id;
    private String party_id;
    private int date_id;

    public TimeslotResponseDTO(int slot_id, Date selected_start_time, Date selected_end_time, Long user_id, String party_id, int date_id) {
        this.slot_id = slot_id;
        this.selected_start_time = selected_start_time;
        this.selected_end_time = selected_end_time;
        this.user_id = user_id;
        this.party_id = party_id;
        this.date_id = date_id;
    }

    public int getSlot_id() {
        return slot_id;
    }

    public void setSlot_id(int slot_id) {
        this.slot_id = slot_id;
    }

    public Date getSelected_start_time() {
        return selected_start_time;
    }

    public void setSelected_start_time(Date selected_start_time) {
        this.selected_start_time = selected_start_time;
    }

    public Date getSelected_end_time() {
        return selected_end_time;
    }

    public void setSelected_end_time(Date selected_end_time) {
        this.selected_end_time = selected_end_time;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getParty_id() {
        return party_id;
    }

    public void setParty_id(String party_id) {
        this.party_id = party_id;
    }

    public int getDate_id() {
        return date_id;
    }

    public void setDate_id(int date_id) {
        this.date_id = date_id;
    }
}