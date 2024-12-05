package com.moyeobwayo.moyeobwayo.Service;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration

public class KakaotalkalarmService {
    private final String serviceID;
    private final String ncpAccessKey;
    private final String ncpSecretKey;
    private final String plusFriendId;
    private final String templateCodeVoteComplete;
    private final String templateCodePartyComplete;
    private final String templateCodePartyRemind;
    private final UtilService utilService;

    public KakaotalkalarmService(
            @Value("${NCP_SERVICE_ID}") String serviceID,
            @Value("${NCP_ACCESS_KEY}") String ncpAccessKey,
            @Value("${NCP_SECRET_KEY}") String ncpSecretKey,
            @Value("${NCP_PLUS_FRIEND_ID}") String plusFriendId,
            @Value("${NCP_TEMPLATE_CODE_VOTE_COMPLETE}") String templateCodeVoteComplete,
            @Value("${NCP_TEMPLATE_CODE_PARTY_COMPLETE}") String templateCodePartyComplete,
            @Value("${NCP_TEMPLATE_CODE_PARTY_REMIND}") String templateCodePartyRemind,
            UtilService utilService) {
        this.serviceID = serviceID;
        this.ncpAccessKey = ncpAccessKey;
        this.ncpSecretKey = ncpSecretKey;
        this.plusFriendId = plusFriendId;
        this.templateCodeVoteComplete = templateCodeVoteComplete;
        this.templateCodePartyComplete = templateCodePartyComplete;
        this.templateCodePartyRemind = templateCodePartyRemind;
        this.utilService = utilService;
    }

    public void sendAlimTalk(String to,
                             String templateCode,
                             String content,
                             JSONArray buttons,
                             boolean isReservedMessage,
                             String targetDateTime

    ) {
        String alimTalkSendRequestUrl = "https://sens.apigw.ntruss.com/alimtalk/v2/services/" + serviceID + "/messages";
        String alimTalkSignatureRequestUrl = "/alimtalk/v2/services/" + serviceID + "/messages";
        CloseableHttpClient httpClient = null;

        try {
            String[] signatureArray = makePostSignature(ncpAccessKey, ncpSecretKey, alimTalkSignatureRequestUrl);

            // http 통신 객체 생성
            httpClient = HttpClients.createDefault(); // http client 생성
            HttpPost httpPost = new HttpPost(alimTalkSendRequestUrl); // post 메서드와 URL 설정

            // 헤더 설정
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setHeader("x-ncp-iam-access-key", ncpAccessKey);
            httpPost.setHeader("x-ncp-apigw-timestamp", signatureArray[0]);
            httpPost.setHeader("x-ncp-apigw-signature-v2", signatureArray[1]);

            // 메시지 객체 구성
            JSONObject msgObj = new JSONObject();
            msgObj.put("plusFriendId", plusFriendId);
            msgObj.put("templateCode", templateCode);



            // 메시지 내용 구성
            JSONObject messages = new JSONObject();
            messages.put("countryCode", "82");  // 국가 코드
            messages.put("to", to); // 전화번호
            messages.put("content", content);  // 메시지 내용

            //reserved Time 필드 등록
            if (isReservedMessage){
                //reserveTime이
                msgObj.put("reserveTime", targetDateTime);
            }
            // 두 개 이상의 항목을 가진 리스트를 추가하여 오류 해결
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("title", "모임 시간").put("description", "test"));
            list.put(new JSONObject().put("title", "모임 장소").put("description", "서울 강남구"));

            // 버튼 추가
            if (buttons != null) {
                messages.put("buttons", buttons);
            }

            // 메시지 객체 배열에 메시지 추가
            JSONArray messageArray = new JSONArray();
            messageArray.put(messages);

            // 메시지 배열을 메시지 객체에 포함
            msgObj.put("messages", messageArray);

            // 예약 시간과 타임존 설정 (필요시 추가)

            // 바디 출력
            System.out.println("Request Body:");
            System.out.println(msgObj.toString());

            // API 전송 값 http 객체에 담기
            httpPost.setEntity(new StringEntity(msgObj.toString(), "UTF-8"));

            // API 호출
            CloseableHttpResponse httpResponse = httpClient.execute(httpPost);

            // 응답 결과
            String result = EntityUtils.toString(httpResponse.getEntity(), "UTF-8");
            System.out.println(result);

        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (httpClient != null) {
                    httpClient.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void sendVotingCompletionAlimTalk(
            String partyId,
            String partyName,
            String partyLeaderName,
            List<String> topTimeSlots,
            String to) throws JSONException {

        partyLeaderName = partyLeaderName.contains("(")
                ? partyLeaderName.substring(0, partyLeaderName.indexOf("(")).trim()
                : partyLeaderName;

        String topTimeSlot1 = topTimeSlots.size() > 0 ? formatTimeSlot(topTimeSlots.get(0)) : "시간대 없음";
        String topTimeSlot2 = topTimeSlots.size() > 1 ? formatTimeSlot(topTimeSlots.get(1)) : "시간대 없음";
        String topTimeSlot3 = topTimeSlots.size() > 2
                ? formatTimeSlot(topTimeSlots.get(2)) + "\nhttps://www.moyeobwayo.com/meeting/" + partyId
                : "시간대 없음\nhttps://www.moyeobwayo.com/meeting/" + partyId;

        String content = String.format(
                        "✨ [투표 완료 알림] ✨\n" +
                        "%s 모임의 투표가 완료되었습니다! 🎉\n" +
                        "\n" +
                        "파티장 %s 님이 개설한 모임이 투표 완료 되었어요. 🎈\n" +
                        "참여가 가장 많은 시간대 3가지를 알려드립니다:\n" +
                        "\n" +
                        "1. 🕒 %s\n" +
                        "2. 🕒 %s\n" +
                        "3. 🕒 %s\n" +
                        "\n" +
                        "자세한 일정은 아래 버튼을 통해 확인해 주세요! 📅\n" +
                        "\n" +
                        "👇 지금 바로 확인하기 👇\n" +
                        "[모임 확인하기]",
                partyName, partyLeaderName, topTimeSlot1, topTimeSlot2, topTimeSlot3
        );

        // 버튼 생성 (모임 확인하기 버튼)
        JSONObject button = new JSONObject();
        button.put("type", "WL"); // 변경된 부분
        button.put("name", "모여봐요");
        button.put("linkMobile", "https://www.moyeobwayo.com/"+partyId);
        button.put("linkPc", "https://www.moyeobwayo.com/"+partyId);

        // 버튼 배열 생성
        JSONArray buttons = new JSONArray();
        buttons.put(button);

        // 메시지 전송
        sendAlimTalk(
                to, // 전화번호를 직접 전달
                templateCodeVoteComplete, // 템플릿 코드
                content, // 메시지 내용
                buttons, // 버튼 배열 추가
                true, // 예약 메시지 O
                GetDelayFormatTime(11)
        );
    }
    // 파티 확정시 리마인드 알람
    public boolean sendPartyCompletionAlimTalk(
            String partyId,
            String partyName,
            String partyLeaderName,
            Date targetDateTime,
            int possibleNum,
            int impossibleNum,
            String to) throws JSONException {

        partyLeaderName = partyLeaderName.contains("(")
                ? partyLeaderName.substring(0, partyLeaderName.indexOf("(")).trim()
                : partyLeaderName;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일");
        String targetDate = dateFormat.format(targetDateTime);
        // 시간 부분: "HH시 mm분"
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분");
        String targetTime = timeFormat.format(targetDateTime);

        String content = String.format(
                "[모여봐요] 📅 모임이 확정되었어요!\n" +
                        "안녕하세요! 🎉 드디어 모임 일정이 확정되었습니다. 아래 내용을 확인해주세요!\n\n" +
                        "✅ 확정된 모임 정보\n" +
                        "• 모임 이름: %s\n" +  // partyName
                        "• 모임 이름: %s\n" +      // partyLeaderName
                        "• 날짜: %s\n" +        // targetDate (예: 모임 날짜)
                        "• 시간: %s\n\n" +      // targetTime (예: 모임 시간)

                        "📊 참여 현황\n" +
                        "• 참여 가능 인원: %s명\n" +  // possibleNum (참여 가능한 인원 수)
                        "• 참여 불가능 인원: %s명\n\n" +  // notPossibleNum (참여 불가능 인원 수)

                        "⏰ 리마인드 알림\n" +
                        "모임 당일 [1시간 전] 다시 한번 알림을 드릴게요! 잊지 말고 준비해주세요 😊\n\n" +

                        "📍모임 세부 정보 확인 및 참여 관리:\n" +
                        "%s\n\n" +  // partyURL (모임 세부 정보 URL)

                        "모임과 관련해 궁금한 점이 있다면 언제든 알려주세요.\n" +
                        "그럼 모임 날 뵙겠습니다! 🎈\n\n" +
                        "“모여봐요” 팀 드림",
                partyName, partyLeaderName, targetDate, targetTime, possibleNum, impossibleNum,
                "https://www.moyeobwayo.com/meeting/"+partyId
        );

        try{
            // 메시지 전송
            sendAlimTalk(
                    to, // 전화번호를 직접 전달
                    templateCodePartyComplete, // 템플릿 코드
                    content, // 메시지 내용
                    null, // 버튼 배열 추가
                    false, // 예약 메시지 X
                    GetDelayFormatTime(11)
            );
            // 리마인드 알람 예약 등록
            int subtractminutes  = 25;
            ReservePartyReminderAlimTalk(
                    partyId, partyName, partyLeaderName, targetDateTime,
                    possibleNum, impossibleNum, subtractminutes, to);
            return true;
        } catch (Exception e){
            System.out.println(e);
            return false;
        }
    }
    //파티 확정시간 N시간전 보내는 리마인드 알림 등록하기
    public void ReservePartyReminderAlimTalk(String partyId,
                                             String partyName,
                                             String partyLeaderName,
                                             Date targetDateTime,
                                             int possibleNum,
                                             int impossibleNum,
                                             int subtractMinutes, // 몇분전에 예약을 할건지 정하기
                                             String to) throws JSONException
    {
        // 리마인드 시간보다 현재시간이 이르다면 알림을 전송할 수 업으므로 종료
        if (utilService.isTimeEarlierThanNow(targetDateTime, subtractMinutes)){
            return;
        }
        partyLeaderName = partyLeaderName.contains("(")
                ? partyLeaderName.substring(0, partyLeaderName.indexOf("(")).trim()
                : partyLeaderName;

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM월 dd일");
        String targetDate = dateFormat.format(targetDateTime);
        // 시간 부분: "HH시 mm분"
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH시 mm분");
        String targetTime = timeFormat.format(targetDateTime);

        String content = String.format(
                "[모여봐요] ⏰ 모임이 곧 시작됩니다!\n" +
                        "안녕하세요! 🎉 드디어 기다리던 모임 시간이 가까워졌습니다. 아래 정보를 다시 한번 확인해주세요!\n\n" +

                        "✅ 모임 세부 정보\n" +
                        "• 모임 이름: %s\n" +  // partyName
                        "• 모임장 이름: %s\n" +      // partyLeaderName
                        "• 날짜: %s\n" +        // targetDate (예: 모임 날짜)
                        "• 시간: %s\n\n" +      // targetTime (예: 모임 시간)

                        "📊 현재 참여 현황\n" +
                        "• 참여 가능 인원: %s명\n" +  // possibleNum (참여 가능한 인원 수)
                        "• 참여 불가능 인원: %s명\n\n" +  // impossibleNum (참여 불가능 인원 수)

                        "📍모임 세부 정보 확인 및 참여 관리:\n" +
                        "%s\n\n" +  // partyURL (모임 세부 정보 URL)

                        "모임 시작 전 필요한 준비물을 챙기고, 즐거운 시간을 보내세요!\n" +
                        "궁금한 점이 있다면 언제든 알려주세요. 😊\n\n" +

                        "“모여봐요” 팀 드림",
                partyName, partyLeaderName, targetDate, targetTime, possibleNum, impossibleNum,
                "https://www.moyeobwayo.com/meeting/" + partyId
        );

        try{
            // 메시지 전송
            sendAlimTalk(
                    to, // 전화번호를 직접 전달
                    templateCodePartyRemind, // 템플릿 코드
                    content, // 메시지 내용
                    null, // 버튼 배열 추가
                    true, // 예약 메시지 O
                    utilService.subtractMinutesFromCompleteTime(targetDateTime, subtractMinutes)
            );
        } catch (Exception e){
            System.out.println(e);
        }
    }
    private static String GetDelayFormatTime(int delayTimeInMinutes){
        LocalDateTime reserveTime = LocalDateTime.now().plusMinutes(delayTimeInMinutes);
        // 2. 포맷 지정
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        // 3. reserveTime을 지정된 포맷으로 문자열 변환
        String formattedTime = reserveTime.format(formatter);
        return formattedTime;
    }

    private static String formatTimeSlot(String timeslot) {
        // Split the timeslot into start and end times
        String[] parts = timeslot.split(" - ");
        if (parts.length != 2) {
            return "Invalid timeslot format";
        }

        // Parse the start and end times
        LocalDateTime startTime = LocalDateTime.parse(parts[0]);
        LocalDateTime endTime = LocalDateTime.parse(parts[1]);

        // Formatters for start time and end time
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일 (E)", Locale.KOREAN);
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        // Construct the formatted string
        return startTime.format(dateFormatter) + " "
                + startTime.format(timeFormatter) + " ~ "
                + endTime.format(timeFormatter);
    }

    public String[] makePostSignature(String accessKey, String secretKey, String url) {
        String[] result = new String[2];
        try {
            String timeStamp = String.valueOf(Instant.now().toEpochMilli()); // current timestamp (epoch)
            String space = " "; // space
            String newLine = "\n"; // new line
            String method = "POST"; // method
            String message =
                    new StringBuilder()
                            .append(method)
                            .append(space)
                            .append(url)
                            .append(newLine)
                            .append(timeStamp)
                            .append(newLine)
                            .append(accessKey)
                            .toString();

            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);

            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
            String encodeBase64String = Base64.encodeBase64String(rawHmac);

            result[0] = timeStamp;
            result[1] = encodeBase64String;

            System.out.println("Timestamp: " + timeStamp);
            System.out.println("Signature: " + encodeBase64String);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
    }

