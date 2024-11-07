package com.moyeobwayo.moyeobwayo.Service;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
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
            @Value("${NCP_SECRET_KEY}") String plusFriendId) {
        this.serviceID = serviceID;
        this.ncpAccessKey = ncpAccessKey;
        this.ncpSecretKey = ncpSecretKey;
        this.plusFriendId = plusFriendId;
    }

    public void sendAlimTalk(String to, String templateCode, String content) {
        String alimTalkSendRequestUrl = "https://sens.apigw.ntruss.com/alimtalk/v2/services/" + serviceID + "/messages";
        String alimTalkSignatureRequestUrl = "/alimtalk/v2/services/" + serviceID + "/messages";
        CloseableHttpClient httpClient = null;
        try {
            String[] signatureArray =
                    makePostSignature(ncpAccessKey, ncpSecretKey, alimTalkSignatureRequestUrl);

            // http 통신 객체 생성
            httpClient = HttpClients.createDefault(); // http client 생성
            HttpPost httpPost = new HttpPost(alimTalkSendRequestUrl); // post 메서드와 URL 설정

            // 헤더 설정
            httpPost.setHeader("Content-Type", "application/json; charset=UTF-8");
            httpPost.setHeader("x-ncp-iam-access-key", ncpAccessKey);
            httpPost.setHeader("x-ncp-apigw-timestamp", signatureArray[0]);
            httpPost.setHeader("x-ncp-apigw-signature-v2", signatureArray[1]);

            // body 설정
            JSONObject msgObj = new JSONObject();
            msgObj.put("plusFriendId", plusFriendId);
            msgObj.put("templateCode", templateCode);

            JSONObject messages = new JSONObject();
            messages.put("to", to);
            messages.put("content", content);

            JSONArray messageArray = new JSONArray();
            messageArray.put(messages);
            msgObj.put("messages", messageArray);

            // api 전송 값 http 객체에 담기
            httpPost.setEntity(new StringEntity(msgObj.toString(), "UTF-8"));
            // api 호출
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

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }

    public String[] makeGetSignature(String accessKey, String secretKey, String url) {
        String[] result = new String[2];
        try {
            String timeStamp = String.valueOf(Instant.now().toEpochMilli()); // current timestamp (epoch)
            String space = " "; // space
            String newLine = "\n"; // new line
            String method = "GET"; // method

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
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return result;
    }
}
