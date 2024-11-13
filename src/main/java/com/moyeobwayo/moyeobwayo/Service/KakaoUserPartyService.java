package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class KakaoUserPartyService {

    private final UserEntityRepository userEntityRepository;

    @Autowired
    public KakaoUserPartyService(UserEntityRepository userEntityRepository) {
        this.userEntityRepository = userEntityRepository;
    }

    /**
     *
     * @param kakaoUserId
     * @return
     */
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

        /*
        파티가 중복되는 문제가 발생한다고 했는데 로직 상 그럴만한 부분을 찾지 못해 Set으로 변경해서
        중복 제거하고 다시 ArrayList로 변환시키는 방법을 사용했습니다.(donggeun)
         */
        Set<Party> partySet = new HashSet<>(partyList);
        List<Party> distinctPartyList = new ArrayList<>(partySet);


        return distinctPartyList;
        // return partyList;

    }
}
