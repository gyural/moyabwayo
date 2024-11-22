package com.moyeobwayo.moyeobwayo.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.moyeobwayo.moyeobwayo.Domain.Party;
//import jdk.internal.org.jline.utils.InfoCmp;
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

public class kakaotalkalarmService {
    private final String serviceID;
    private final String ncpAccessKey;
    private final String ncpSecretKey;
    private final String plusFriendId;

    public kakaotalkalarmService(
            @Value("${NCP_SERVICE_ID}") String serviceID,
            @Value("${NCP_ACCESS_KEY}") String ncpAccessKey,
            @Value("${NCP_SECRET_KEY}") String ncpSecretKey,
            @Value("${NCP_PLUS_FRIEND_ID}") String plusFriendId) {
        this.serviceID = serviceID;
        this.ncpAccessKey = ncpAccessKey;
        this.ncpSecretKey = ncpSecretKey;
        this.plusFriendId = plusFriendId;
    }

    public void sendAlimTalk(String to, String templateCode, String content, JSONArray buttons) {
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


            // 두 개 이상의 항목을 가진 리스트를 추가하여 오류 해결
            JSONArray list = new JSONArray();
            list.put(new JSONObject().put("title", "모임 시간").put("description", "test"));
            list.put(new JSONObject().put("title", "모임 장소").put("description", "서울 강남구"));



            // 버튼 추가
            messages.put("buttons", buttons);

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

    // party 객체로 받아서 사용하던 파티 id와 파티 생성자 이름을 string으로 받아서 넘겨준다.
    public void sendVotingCompletionAlimTalk(String partyName, String partyLeaderName, List<String> topTimeSlots, String to) throws JSONException {

        // 상위 3개의 시간대를 가져옵니다. (topTimeSlots 리스트에 시간대가 들어있음)
        String topTimeSlot1 = topTimeSlots.size() > 0 ? topTimeSlots.get(0) : "시간대 없음";
        String topTimeSlot2 = topTimeSlots.size() > 1 ? topTimeSlots.get(1) : "시간대 없음";
        String topTimeSlot3 = topTimeSlots.size() > 2 ? topTimeSlots.get(2) : "시간대 없음";

        // 메시지 내용 생성
        String content = String.format(
                "✨ [투표 완료 알림] ✨\n" +
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
        button.put("linkMobile", "https://www.moyeobwayo.com/");
        button.put("linkPc", "https://www.moyeobwayo.com/");

        // 버튼 배열 생성
        JSONArray buttons = new JSONArray();
        buttons.put(button);

        // 알림 메시지 템플릿 코드 설정
        // templateCode : moyeobwayobasic -> moyeobwayobasic1 변경
        String templateCode = "moyeobwayobasic1";  // Naver Cloud Platform SENS에서 설정한 템플릿 코드

        // 메시지 전송
        sendAlimTalk(
                to, // 전화번호를 직접 전달
                templateCode, // 템플릿 코드
                content, // 메시지 내용
                buttons // 버튼 배열 추가
        );
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

//    public String[] makeGetSignature(String accessKey, String secretKey, String url) {
//        String[] result = new String[2];
//        try {
//            String timeStamp = String.valueOf(Instant.now().toEpochMilli()); // current timestamp (epoch)
//            String space = " "; // space
//            String newLine = "\n"; // new line
//            String method = "GET"; // method
//
//            String message =
//                    new StringBuilder()
//                            .append(method)
//                            .append(space)
//                            .append(url)
//                            .append(newLine)
//                            .append(timeStamp)
//                            .append(newLine)
//                            .append(accessKey)
//                            .toString();
//
//            SecretKeySpec signingKey = new SecretKeySpec(secretKey.getBytes("UTF-8"), "HmacSHA256");
//            Mac mac = Mac.getInstance("HmacSHA256");
//            mac.init(signingKey);
//
//            byte[] rawHmac = mac.doFinal(message.getBytes("UTF-8"));
//            String encodeBase64String = Base64.encodeBase64String(rawHmac);
//
//            result[0] = timeStamp;
//            result[1] = encodeBase64String;
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//        return result;
    }

