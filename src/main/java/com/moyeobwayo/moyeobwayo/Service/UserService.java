package com.moyeobwayo.moyeobwayo.Service;

import com.moyeobwayo.moyeobwayo.Domain.Alarm;
import com.moyeobwayo.moyeobwayo.Domain.KakaoProfile;
import com.moyeobwayo.moyeobwayo.Domain.Party;
import com.moyeobwayo.moyeobwayo.Domain.UserEntity;
import com.moyeobwayo.moyeobwayo.Domain.response.UserResponse;
import com.moyeobwayo.moyeobwayo.Repository.AlarmRepository;
import com.moyeobwayo.moyeobwayo.Repository.KakaoProfileRepository;
import com.moyeobwayo.moyeobwayo.Repository.PartyRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import com.moyeobwayo.moyeobwayo.Repository.UserEntityRepository;

import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserEntityRepository userRepository;
    private final PartyRepository partyRepository;
    private final AlarmRepository alarmRepository; // 알람 리포지토리 추가
    private final KakaoProfileRepository kakaoProfileRepository;

    public UserService(UserEntityRepository userRepository, PartyRepository partyRepository, AlarmRepository alarmRepository, KakaoProfileRepository kakaoProfileRepository) {
        this.userRepository = userRepository;
        this.partyRepository = partyRepository;
        this.alarmRepository = alarmRepository;
        this.kakaoProfileRepository = kakaoProfileRepository;
    }

    // 사용자 로그인 및 등록 처리
    public Optional<UserResponse> loginOrRegister(String userName, String password, String partyId, boolean isKakao, long kakaoUserId) {
        // 파티 ID로 해당 파티 조회
        Optional<Party> partyOptional = partyRepository.findById(partyId);
        if (partyOptional.isEmpty()) {
            return Optional.empty(); // 해당 파티가 존재하지 않음
        }
        Party party = partyOptional.get(); // 파티가 존재하면 파티 객체를 가져옴

        // 기존 사용자 확인
        Optional<UserEntity> existingUser = userRepository.findUserInSameParty(userName + "_" + kakaoUserId, partyId);
        if (existingUser.isPresent()) {
            // 기존 사용자가 있으면 비밀번호 확인 후 로그인 처리
            UserEntity user = existingUser.get();
            if (password == null || password.equals(user.getPassword())) {
                return Optional.of(new UserResponse(user, "기존 회원 로그인이 완료되었습니다."));
            } else {
                return Optional.empty(); // 비밀번호 불일치로 로그인 실패
            }
        }

        // 사용자가 없으면 새로 회원가입 (회원가입 기능)
        UserEntity newUser = new UserEntity();
        newUser.setUserName(userName + "_" + kakaoUserId); // 카카오 유저 ID를 붙임
        newUser.setPassword(password);
        newUser.setParty(party);
        KakaoProfile kakaoProfile = kakaoProfileRepository.findById(kakaoUserId).orElse(null);
        newUser.setKakaoProfile(kakaoProfile);
        // 새로운 사용자는 DB에 저장.
        newUser = userRepository.save(newUser);

        if (isKakao) {
            Alarm newAlarm = new Alarm();
            newAlarm.setUserEntity(newUser);
            newAlarm.setParty(party);
            newAlarm.setAlarm_on(true);

            alarmRepository.save(newAlarm);
        }

        return Optional.of(new UserResponse(newUser, "신규 회원이 데이터베이스에 저장되었습니다."));
    }
}