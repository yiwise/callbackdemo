package com.yiwise.callback;


import com.yiwise.callback.util.WXBizMsgCrypt;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.net.URLDecoder;

/**
 * 接收回调请求的demo
 * http状态返回200表示回调成功。 验签、解密过程中出现异常建议直接抛出。
 */
public class CallbackDemo {

    public static String token = "AIGbuK3sOV";
    // 为空字符 暂无实际意义
    public static String receiveid = "";
    public static String encodingAesKey = "INnZzJ/DPbUqqtU2ycEhzL8ewpsCRb9XqHBWHdDCknw";

    /**
     * get 请求  验签.解密 返回解密后明文
     *
     * @param msgSignature 加密
     * @param timestamp    时间戳
     * @param nonce        随机
     * @param echostr      .
     * @throws Exception
     * 接收到该请求时，企业应
     * 1.解析出Get请求的参数，
     * 包括消息体签名(msg_signature)，时间戳(timestamp)，随机数字串(nonce)以及一知推送过来的随机加密字符串(echostr),
     * 这一步注意作URL解码。
     * 2.验证消息体签名的正确性
     * 3.解密出echostr原文，将原文当作Get请求的response，返回给一知
     * 第2，3步可以用一知提供的库函数VerifyURL来实现。
     */
    @GetMapping(value = "/callback")
    public String reveiceMsg(@RequestParam(name = "msg_signature")String msgSignature,
                             @RequestParam(name = "timestamp")String timestamp,
                             @RequestParam(name = "nonce")String nonce,
                             @RequestParam(name = "echostr")String echostr) throws Exception {
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token, encodingAesKey, receiveid);
        // 参数值需要做Urldecode处理
        // 参数值做Urldecode处理，很多客户对接时候出现过问题 这里详细描述一下
        // 由于环境不同，有些项目可能做过全局的编码处理，这里可以参考echostr的最后两位参数
        // 如果最后两位接收到的是两个等于号 "==" 表示已经解码过了 不需要再 Urldecode
        // 如果最后两位是 %3D%3D 表示需要Urldecode一次 转换成 ==
        // 如果最后两位是 %253D%253D 表示需要Urldecode俩次 转换成 ==
        String signature = URLDecoder.decode(msgSignature, "UTF-8");
        String echostrDecode = URLDecoder.decode(echostr, "UTF-8");
        String sEchoStr = wxcpt.VerifyURL(signature, timestamp, nonce, echostrDecode);
        //必须要返回解密之后的明文
        if (sEchoStr != null &&  "".equals(sEchoStr)) {
            System.out.println("URL验证失败");
        } else {
            System.out.println("验证成功!");
        }
        return sEchoStr;
    }


    /**
     *
     * @param msgSignature 签名
     * @param timestamp 时间戳
     * @param nonce 随机值
     * @param xml 加密后封装成xml
     * @return
     * @throws Exception
     */
    @PostMapping(value = "/callback", consumes = MediaType.APPLICATION_XML_VALUE)
    public String callback2(@RequestParam(name = "msg_signature") final String msgSignature,
                            @RequestParam(name = "timestamp") final String timestamp,
                            @RequestParam(name = "nonce") final String nonce,
                            @RequestBody String xml) throws Exception {
        String signature = URLDecoder.decode(msgSignature, "UTF-8");
        String xmlDecode = URLDecoder.decode(xml,"UTF-8");
        WXBizMsgCrypt wxcpt = new WXBizMsgCrypt(token, encodingAesKey, receiveid);
        // 验签、解密
        String sMsg = wxcpt.DecryptMsg(signature, timestamp, nonce, xmlDecode);
        System.out.println(sMsg);
        // 然后去操作你的业务逻辑  sMsg是Json格式 具体参数参考回调业务文档

        // 根据业务返回信息
        return "success";
    }


}
