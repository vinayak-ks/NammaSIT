package theliars.nammasit.socket.udp;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import theliars.nammasit.ActivitiesManager;
import theliars.nammasit.BaseApplication;
import theliars.nammasit.activity.message.ChatActivity;
import theliars.nammasit.bean.Entity;
import theliars.nammasit.bean.Message;
import theliars.nammasit.bean.Users;
import theliars.nammasit.socket.tcp.TcpService;
import theliars.nammasit.sql.SqlDBOperate;
import theliars.nammasit.util.ImageUtils;
import theliars.nammasit.util.LogUtils;
import theliars.nammasit.util.SessionUtils;

public class UDPMessageListener implements Runnable {

    private static final String TAG = "SZU_UDPMessageListener";

    private static final int POOL_SIZE = 5;
    private static final int BUFFERLENGTH = 1024; //

    private static byte[] sendBuffer = new byte[BUFFERLENGTH];
    private static byte[] receiveBuffer = new byte[BUFFERLENGTH];

    private HashMap<String, String> mLastMsgCache;
    private ArrayList<Users> mUnReadPeopleList; //
    private HashMap<String, Users> mOnlineUsers; // ，

    private String BROADCASTIP;
    private Thread receiveUDPThread;
    private boolean isThreadRunning;
    private List<OnNewMsgListener> mListenerList;

    private Users mLocalUser; //
    private SqlDBOperate mDBOperate;

    private static ExecutorService executor;
    private static DatagramSocket UDPSocket;
    private static DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;

    private static Context mContext;
    private static UDPMessageListener instance;

    private UDPMessageListener() {
        BROADCASTIP = "255.255.255.255";
        // BROADCASTIP = WifiUtils.getBroadcastAddress();

        mDBOperate = new SqlDBOperate(mContext);
        mListenerList = new ArrayList<UDPMessageListener.OnNewMsgListener>();
        mOnlineUsers = new LinkedHashMap<String, Users>();
        mLastMsgCache = new HashMap<String, String>();
        mUnReadPeopleList = new ArrayList<Users>();

        int cpuNums = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cpuNums * POOL_SIZE); //
    }


    public static UDPMessageListener getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            instance = new UDPMessageListener();
        }
        return instance;
    }

    @Override
    public void run() {
        while (isThreadRunning) {

            try {
                UDPSocket.receive(receiveDatagramPacket);
            }
            catch (IOException e) {
                isThreadRunning = false;
                receiveDatagramPacket = null;
                if (UDPSocket != null) {
                    UDPSocket.close();
                    UDPSocket = null;
                }
                receiveUDPThread = null;
                LogUtils.e(TAG, "UDP数据包接收失败！线程停止");
                e.printStackTrace();
                break;
            }

            if (receiveDatagramPacket.getLength() == 0) {
                LogUtils.e(TAG, "无法接收UDP数据或者接收到的UDP数据为空");
                continue;
            }

            String UDPListenResStr = "";
            try {
                UDPListenResStr = new String(receiveBuffer, 0, receiveDatagramPacket.getLength(),
                        "gbk");
            }
            catch (UnsupportedEncodingException e) {
                LogUtils.e(TAG, "系统不支持GBK编码");
            }

            IPMSGProtocol ipmsgRes = new IPMSGProtocol(UDPListenResStr);
            int commandNo = ipmsgRes.getCommandNo();
            String senderIMEI = ipmsgRes.getSenderIMEI();
            String senderIp = receiveDatagramPacket.getAddress().getHostAddress();

            if (BaseApplication.isDebugmode) {
                processMessage(commandNo, ipmsgRes, senderIMEI, senderIp);
            }
            else {
                if (!SessionUtils.isLocalUser(senderIMEI)) {
                    processMessage(commandNo, ipmsgRes, senderIMEI, senderIp);
                }
            }

            if (receiveDatagramPacket != null) {
                receiveDatagramPacket.setLength(BUFFERLENGTH);
            }

        }

        receiveDatagramPacket = null;
        if (UDPSocket != null) {
            UDPSocket.close();
            UDPSocket = null;
        }
        receiveUDPThread = null;

    }

    public void processMessage(int commandNo, IPMSGProtocol ipmsgRes, String senderIMEI,
            String senderIp) {
        TcpService tcpService;
        switch (commandNo) {


            case IPMSGConst.IPMSG_BR_ENTRY: {
                LogUtils.i(TAG, "收到上线通知");
                addUser(ipmsgRes);
                sendUDPdata(IPMSGConst.IPMSG_ANSENTRY, receiveDatagramPacket.getAddress(),
                        mLocalUser);
                LogUtils.i(TAG, "成功发送上线应答");
            }
                break;


            case IPMSGConst.IPMSG_ANSENTRY: {
                LogUtils.i(TAG, "收到上线应答");
                addUser(ipmsgRes);
            }
                break;


            case IPMSGConst.IPMSG_BR_EXIT: {
                removeOnlineUser(senderIMEI, 1);
                LogUtils.i(TAG, "成功删除imei为" + senderIMEI + "的用户");
            }
                break;

            case IPMSGConst.IPMSG_REQUEST_IMAGE_DATA:
                LogUtils.i(TAG, "收到IMAGE发送请求");

                tcpService = TcpService.getInstance(mContext);
                tcpService.setSavePath(BaseApplication.IMAG_PATH);
                tcpService.startReceive();
                sendUDPdata(IPMSGConst.IPMSG_CONFIRM_IMAGE_DATA, senderIp);
                break;

            case IPMSGConst.IPMSG_REQUEST_VOICE_DATA:
                LogUtils.i(TAG, "收到VOICE发送请求");

                tcpService = TcpService.getInstance(mContext);
                tcpService.setSavePath(BaseApplication.VOICE_PATH);
                tcpService.startReceive();
                sendUDPdata(IPMSGConst.IPMSG_CONFIRM_VOICE_DATA, senderIp);
                break;

            case IPMSGConst.IPMSG_SENDMSG: {
                LogUtils.i(TAG, "收到MSG消息");
                Message msg = (Message) ipmsgRes.getAddObject();

                switch (msg.getContentType()) {
                    case TEXT:
                        sendUDPdata(IPMSGConst.IPMSG_RECVMSG, senderIp, ipmsgRes.getPacketNo());
                        break;

                    case IMAGE:
                        LogUtils.i(TAG, "收到图片信息");
                        msg.setMsgContent(BaseApplication.IMAG_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        String THUMBNAIL_PATH = BaseApplication.THUMBNAIL_PATH + File.separator
                                + msg.getSenderIMEI();

                        LogUtils.d(TAG, "缩略图文件夹路径:" + THUMBNAIL_PATH);
                        LogUtils.d(TAG, "图片文件路径:" + msg.getMsgContent());

                        ImageUtils.createThumbnail(mContext, msg.getMsgContent(), THUMBNAIL_PATH
                                + File.separator);
                        break;

                    case VOICE:
                        LogUtils.i(TAG, "收到录音信息");
                        msg.setMsgContent(BaseApplication.VOICE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        LogUtils.d(TAG, "文件路径:" + msg.getMsgContent());
                        break;

                    case FILE:
                        LogUtils.i(TAG, "收到文件 发送请求");
                        tcpService = TcpService.getInstance(mContext);
                        tcpService.setSavePath(BaseApplication.FILE_PATH);
                        tcpService.startReceive();
                        sendUDPdata(IPMSGConst.IPMSG_CONFIRM_FILE_DATA, senderIp);
                        msg.setMsgContent(BaseApplication.FILE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        LogUtils.d(TAG, "文件路径:" + msg.getMsgContent());
                        break;
                }


                mDBOperate.addChattingInfo(senderIMEI, SessionUtils.getIMEI(), msg.getSendTime(),
                        msg.getMsgContent(), msg.getContentType());

                //
                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;
                pMessage.obj = msg;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v == null) {
                    addUnReadPeople(getOnlineUser(senderIMEI)); //
                    for (int i = 0; i < mListenerList.size(); i++) {
                        android.os.Message pMsg = new android.os.Message();
                        pMsg.what = IPMSGConst.IPMSG_RECVMSG;
                        mListenerList.get(i).processMessage(pMsg);
                    }
                }
                else {
                    v.processMessage(pMessage);
                }

                addLastMsgCache(senderIMEI, msg); //
                BaseApplication.playNotification();

            }
                break;

            default:
                LogUtils.i(TAG, "收到命令：" + commandNo);

                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v != null) {
                    v.processMessage(pMessage);
                }

                break;

        } // End of switch
    }

    /**  **/
    public void connectUDPSocket() {
        try {
            //
            if (UDPSocket == null)
                UDPSocket = new DatagramSocket(IPMSGConst.PORT);
            LogUtils.i(TAG, "connectUDPSocket() 绑定端口成功");

            //
            if (receiveDatagramPacket == null)
                receiveDatagramPacket = new DatagramPacket(receiveBuffer, BUFFERLENGTH);

            startUDPSocketThread();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /**  **/
    public void startUDPSocketThread() {
        if (receiveUDPThread == null) {
            receiveUDPThread = new Thread(this);
            receiveUDPThread.start();
        }
        isThreadRunning = true;
        LogUtils.i(TAG, "startUDPSocketThread() 线程启动成功");
    }

    /**  **/
    public void stopUDPSocketThread() {
        isThreadRunning = false;
        if (receiveUDPThread != null)
            receiveUDPThread.interrupt();
        receiveUDPThread = null;
        instance = null; //
        LogUtils.i(TAG, "stopUDPSocketThread() 线程停止成功");
    }

    public void addMsgListener(OnNewMsgListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeMsgListener(OnNewMsgListener listener) {
        this.mListenerList.remove(listener);
    }

    /**  **/
    public void notifyOnline() {
        //
        mLocalUser = SessionUtils.getLocalUserInfo();
        sendUDPdata(IPMSGConst.IPMSG_BR_ENTRY, BROADCASTIP, mLocalUser);
        LogUtils.i(TAG, "notifyOnline() 上线通知成功");
    }

    /**  **/
    public void notifyOffline() {
        sendUDPdata(IPMSGConst.IPMSG_BR_EXIT, BROADCASTIP);
        LogUtils.i(TAG, "notifyOffline() 下线通知成功");
    }

    /**  **/
    public void refreshUsers() {
        removeOnlineUser(null, 0); //
        notifyOnline();
    }


    private void addUser(IPMSGProtocol paramIPMSGProtocol) {
        String receiveIMEI = paramIPMSGProtocol.getSenderIMEI();
        if (BaseApplication.isDebugmode) {
            Users newUser = (Users) paramIPMSGProtocol.getAddObject();
            addOnlineUser(receiveIMEI, newUser);
            mDBOperate.addUserInfo(newUser);
        }
        else {
            if (!SessionUtils.isLocalUser(receiveIMEI)) {
                Users newUser = (Users) paramIPMSGProtocol.getAddObject();
                addOnlineUser(receiveIMEI, newUser);
                mDBOperate.addUserInfo(newUser);
            }
        }
        LogUtils.i(TAG, "成功添加imei为" + receiveIMEI + "的用户");

    }


    public static void sendUDPdata(int commandNo, String targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP, Object addData) {
        sendUDPdata(commandNo, targetIP.getHostAddress(), addData);
    }

    public static void sendUDPdata(int commandNo, String targetIP, Object addData) {
        IPMSGProtocol ipmsgProtocol = null;
        String imei = SessionUtils.getIMEI();

        if (addData == null) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo);
        }
        else if (addData instanceof Entity) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo, (Entity) addData);
        }
        else if (addData instanceof String) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo, (String) addData);
        }
        sendUDPdata(ipmsgProtocol, targetIP);
    }

    public static void sendUDPdata(final IPMSGProtocol ipmsgProtocol, final String targetIP) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    InetAddress targetAddr = InetAddress.getByName(targetIP); // 目的地址
                    sendBuffer = ipmsgProtocol.getProtocolJSON().getBytes("gbk");
                    sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                            targetAddr, IPMSGConst.PORT);
                    UDPSocket.send(sendDatagramPacket);
                    LogUtils.i(TAG, "sendUDPdata() 数据发送成功");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "sendUDPdata() 发送UDP数据包失败");
                }

            }
        });

    }

    public synchronized void addOnlineUser(String paramIMEI, Users paramObject) {
        mOnlineUsers.put(paramIMEI, paramObject);

        for (int i = 0; i < mListenerList.size(); i++) {
            android.os.Message pMsg = new android.os.Message();
            pMsg.what = IPMSGConst.IPMSG_BR_ENTRY;
            mListenerList.get(i).processMessage(pMsg);
        }

        LogUtils.d(TAG, "addUser | OnlineUsersNum：" + mOnlineUsers.size());
    }

    public Users getOnlineUser(String paramIMEI) {
        return mOnlineUsers.get(paramIMEI);
    }


    public void removeOnlineUser(String paramIMEI, int paramtype) {
        if (paramtype == 1) {
            mOnlineUsers.remove(paramIMEI);
            for (int i = 0; i < mListenerList.size(); i++) {
                android.os.Message pMsg = new android.os.Message();
                pMsg.what = IPMSGConst.IPMSG_BR_EXIT;
                mListenerList.get(i).processMessage(pMsg);
            }

        }
        else if (paramtype == 0) {
            mOnlineUsers.clear();
        }

        LogUtils.d(TAG, "removeUser | OnlineUsersNum：" + mOnlineUsers.size());
    }

    public HashMap<String, Users> getOnlineUserMap() {
        return mOnlineUsers;
    }


    public void addLastMsgCache(String paramIMEI, Message msg) {
        StringBuffer content = new StringBuffer();
        switch (msg.getContentType()) {
            case FILE:
                content.append("<FILE>: ").append(msg.getMsgContent());
                break;
            case IMAGE:
                content.append("<IMAGE>: ").append(msg.getMsgContent());
                break;
            case VOICE:
                content.append("<VOICE>: ").append(msg.getMsgContent());
                break;
            default:
                content.append(msg.getMsgContent());
                break;
        }
        if (msg.getMsgContent().isEmpty()) {
            content.append(" ");
        }
        mLastMsgCache.put(paramIMEI, content.toString());
    }


    public String getLastMsgCache(String paramIMEI) {
        return mLastMsgCache.get(paramIMEI);
    }


    public void removeLastMsgCache(String paramIMEI) {
        mLastMsgCache.remove(paramIMEI);
    }

    public void clearMsgCache() {
        mLastMsgCache.clear();
    }

    public void clearUnReadMessages() {
        mUnReadPeopleList.clear();
    }


    public void addUnReadPeople(Users people) {
        if (!mUnReadPeopleList.contains(people))
            mUnReadPeopleList.add(people);
    }


    public ArrayList<Users> getUnReadPeopleList() {
        return mUnReadPeopleList;
    }


    public int getUnReadPeopleSize() {
        return mUnReadPeopleList.size();
    }


    public void removeUnReadPeople(Users people) {
        if (mUnReadPeopleList.contains(people))
            mUnReadPeopleList.remove(people);
    }


    public interface OnNewMsgListener {
        public void processMessage(android.os.Message pMsg);
    }

}