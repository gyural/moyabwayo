package com.moyeobwayo.moyeobwayo.Controller;

import com.moyeobwayo.moyeobwayo.Service.KakaotalkalarmService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/alimtalk")
public class sendAlarmTalkController {

    private final KakaotalkalarmService kakaotalkalarmService;

    // Constructor-based injection ensures the service is correctly initialized
    @Autowired
    public sendAlarmTalkController(KakaotalkalarmService kakaotalkalarmService) {
        this.kakaotalkalarmService = kakaotalkalarmService;
    }

}
