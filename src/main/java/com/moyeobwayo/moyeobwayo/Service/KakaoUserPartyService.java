package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class KakaoUserPartyService {

    private final UserEntityRepository userEntityRepository;

    @Autowired
    public KakaoUserPartyService(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    public List<Party> getPartyByKakaoUserId(Long kakaoUserId) {
        // kakao_user_id로 UserEntity 조회
        List<UserEntity> userEntity = userEntityRepository.findUserEntitiesByKakaoProfile_KakaoUserId(kakaoUserId);


        if (userEntity.isEmpty()) {
            return null;
        }
        List<Party> partyList = new ArrayList<>();
        for (UserEntity userEntity1 : userEntity) {
            partyList.add(userEntity1.getParty());

        }
        return partyList;

    }
}
