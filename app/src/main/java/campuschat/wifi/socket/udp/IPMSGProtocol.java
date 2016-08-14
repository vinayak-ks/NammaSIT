package campuschat.wifi.socket.udp;

import campuschat.wifi.bean.Entity;
import campuschat.wifi.bean.Message;
import campuschat.wifi.bean.Users;
import campuschat.wifi.util.JsonUtils;
import campuschat.wifi.util.LogUtils;

import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;


import com.alibaba.fastjson.annotation.JSONField;


public class IPMSGProtocol {
    private static final String TAG = "SZU_IPMSGPProtocol";
    private static final String PACKETNO = "packetNo";
    private static final String COMMANDNO = "commandNo";
    private static final String ADDOBJECT = "addObject";
    private static final String ADDSTR = "addStr";
    private static final String ADDTYPE = "addType";

    private String packetNo;
    private String senderIMEI;
    private int commandNo;
    private ADDITION_TYPE addType;
    private Entity addObject;
    private String addStr;

    public IPMSGProtocol() {
        this.packetNo = getSeconds();
    }

    public enum ADDITION_TYPE {
        USER, MSG, STRING
    }


    public IPMSGProtocol(String paramProtocolJSON) {
        try {
            JSONObject protocolJSON = new JSONObject(paramProtocolJSON);
            packetNo = protocolJSON.getString(PACKETNO);
            commandNo = protocolJSON.getInt(COMMANDNO);
            senderIMEI = protocolJSON.getString(Users.IMEI);
            if (protocolJSON.has(ADDTYPE)) {
                String addJSONStr = null;
                if (protocolJSON.has(ADDOBJECT)) {
                    addJSONStr = protocolJSON.getString(ADDOBJECT);
                }
                else if (protocolJSON.has(ADDSTR)) {
                    addJSONStr = protocolJSON.getString(ADDSTR);
                }
                switch (ADDITION_TYPE.valueOf(protocolJSON.getString(ADDTYPE))) {
                    case USER:
                        addObject = JsonUtils.getObject(addJSONStr, Users.class);
                        break;

                    case MSG:
                        addObject = JsonUtils.getObject(addJSONStr, Message.class);
                        break;

                    case STRING:
                        addStr = addJSONStr;
                        break;

                    default:
                        break;
                }

            }
        }
        catch (JSONException e) {
            e.printStackTrace();
            LogUtils.e(TAG, "非标准JSON文本");
        }
    }

    public IPMSGProtocol(String paramSenderIMEI, int paramCommandNo, Entity paramObject) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addObject = paramObject;
        if (paramObject instanceof Message) {
            this.addType = ADDITION_TYPE.MSG;
        }
        else if (paramObject instanceof Users) {
            this.addType = ADDITION_TYPE.USER;
        }
    }

    public IPMSGProtocol(String paramSenderIMEI, int paramCommandNo, String paramStr) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
        this.addStr = paramStr;
        this.addType = ADDITION_TYPE.STRING;
    }

    public IPMSGProtocol(String paramSenderIMEI, int paramCommandNo) {
        super();
        this.packetNo = getSeconds();
        this.senderIMEI = paramSenderIMEI;
        this.commandNo = paramCommandNo;
    }

    @JSONField(name = PACKETNO)
    public String getPacketNo() {
        return this.packetNo;
    }

    public void setPacketNo(String paramPacketNo) {
        this.packetNo = paramPacketNo;
    }

    @JSONField(name = Users.IMEI)
    public String getSenderIMEI() {
        return this.senderIMEI;
    }

    public void setSenderIMEI(String paramSenderIMEI) {
        this.senderIMEI = paramSenderIMEI;
    }

    @JSONField(name = ADDTYPE)
    public ADDITION_TYPE getAddType() {
        return this.addType;
    }

    public void setAddType(ADDITION_TYPE paramType) {
        this.addType = paramType;
    }

    @JSONField(name = COMMANDNO)
    public int getCommandNo() {
        return this.commandNo;
    }

    public void setCommandNo(int paramCommandNo) {
        this.commandNo = paramCommandNo;
    }

    @JSONField(name = ADDOBJECT)
    public Entity getAddObject() {
        return this.addObject;
    }

    public void setAddObject(Entity paramObject) {
        this.addObject = paramObject;
    }

    @JSONField(name = ADDSTR)
    public String getAddStr() {
        return this.addStr;
    }

    public void setAddStr(String paramStr) {
        this.addStr = paramStr;
    }


    @JSONField(serialize = false)
    public String getProtocolJSON() {
        return JsonUtils.createJsonString(this);
    }


    @JSONField(serialize = false)
    private String getSeconds() {
        Date nowDate = new Date();
        return Long.toString(nowDate.getTime());
    }

}
