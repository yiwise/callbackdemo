package com.yiwise.callback;

import com.yiwise.callback.util.AesException;
import com.yiwise.callback.util.SHA1;
import com.yiwise.callback.util.WXBizMsgCrypt;
import com.yiwise.callback.util.XMLParse;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * 发送回调请求的demo
 */
public class TestDemo {

    public static void main(String[] args) {
        testGet();
//        testPost();
    }

    /**
     * 发送post回调请求
     * 在推送消息给企业时，会对消息内容做AES加密，以XML格式POST到企业应用的URL上。
     */
    public static void testPost() {
        String postBody = "{\"username\":\"zhangsan\"}";

        String tenantId = "123";
        String token = "AIGbuK3sOV";
        String encodingAESKey = "INnZzJ/DPbUqqtU2ycEhzL8ewpsCRb9XqHBWHdDCknw";

        String timeStamp = "1678759048896";
        String nonce = "8214825565";
        String encrypt,signature;
        WXBizMsgCrypt wxBizMsgCrypt;
        try {
            wxBizMsgCrypt = new WXBizMsgCrypt(token, encodingAESKey, tenantId);
            String randomStr = wxBizMsgCrypt.getRandomStr();
            encrypt = wxBizMsgCrypt.encrypt(randomStr, postBody);
            signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);
            encrypt = URLEncoder.encode(encrypt, "UTF-8");
            signature = URLEncoder.encode(signature, "UTF-8");
        } catch (AesException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        String result = XMLParse.generate(encrypt, signature, timeStamp, nonce);
        String url = "http://localhost:8080/callback" +
                "?msg_signature=" + signature +"&timestamp=" + timeStamp + "&nonce=" + nonce;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_XML);
        HttpEntity<String> httpEntity = new HttpEntity<>(result, headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> responseEntity = restTemplate.postForEntity(url, httpEntity, String.class);
        System.out.println(responseEntity.getBody());
        if (!HttpStatus.OK.equals(responseEntity.getStatusCode())) {
            System.out.println("success");
        }
    }

    // 发送get请求 验证url
    public static void testGet() {
        String replyMsg = "验证接口验签解密是否正确";

        String tenantId = "123";
        String token = "AIGbuK3sOV";
        String encodingAESKey = "INnZzJ/DPbUqqtU2ycEhzL8ewpsCRb9XqHBWHdDCknw";

        String timeStamp = "1678759048896";
        String nonce = "8214825565";
        String encrypt,signature;
        WXBizMsgCrypt wxBizMsgCrypt;
        try {
            wxBizMsgCrypt = new WXBizMsgCrypt(token, encodingAESKey, tenantId);
            String randomStr = wxBizMsgCrypt.getRandomStr();
            encrypt = wxBizMsgCrypt.encrypt(randomStr, replyMsg);
            signature = SHA1.getSHA1(token, timeStamp, nonce, encrypt);
            encrypt = URLEncoder.encode(encrypt, "UTF-8");
            signature = URLDecoder.decode(signature, "UTF-8");
        } catch (AesException | UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }

        String url = "http://localhost:8080/callback" +
                "?msg_signature=" + signature +"&timestamp=" + timeStamp + "&nonce=" + nonce + "&echostr=" + encrypt;
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> exchange = restTemplate.getForEntity(url, String.class);
        System.out.println(exchange.getBody());
        if (replyMsg.equals(exchange.getBody())) {
            System.out.println("success");
        }
    }


}
