package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Service.kakaotalkalarmService;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/alimtalk")
public class sendAlarmTalkController {

    private final kakaotalkalarmService kakaotalkalarmService;

    // Constructor-based injection ensures the service is correctly initialized
    @Autowired
    public sendAlarmTalkController(kakaotalkalarmService kakaotalkalarmService) {
        this.kakaotalkalarmService = kakaotalkalarmService;
    }

}
