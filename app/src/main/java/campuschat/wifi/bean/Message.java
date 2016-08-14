package campuschat.wifi.bean;

import com.alibaba.fastjson.annotation.JSONField;


public class Message extends Entity {

    private String senderIMEI;
    private String sendTime;
    private String MsgContent;
    private CONTENT_TYPE contentType;
    private int percent;

    public Message() {
    }

    public Message(String paramSenderIMEI, String paramSendTime, String paramMsgContent,
            CONTENT_TYPE paramContentType) {
        this.senderIMEI = paramSenderIMEI;
        this.sendTime = paramSendTime;
        this.MsgContent = paramMsgContent;
        this.contentType = paramContentType;
    }


    public enum CONTENT_TYPE {
        TEXT, IMAGE, FILE, VOICE;
    }



    public String getSenderIMEI() {
        return senderIMEI;
    }


    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }


    public CONTENT_TYPE getContentType() {
        return contentType;
    }


    public void setContentType(CONTENT_TYPE paramContentType) {
        this.contentType = paramContentType;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String paramSendTime) {
        this.sendTime = paramSendTime;
    }


    public String getMsgContent() {
        return MsgContent;
    }


    public void setMsgContent(String paramMsgContent) {
        this.MsgContent = paramMsgContent;
    }


    public Message clone() {
        return new Message(senderIMEI, sendTime, MsgContent, contentType);
    }

    @JSONField(serialize = false)
    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

}
