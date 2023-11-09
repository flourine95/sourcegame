package com.girlkun.server.io;

import com.girlkun.data.DataGame;
import com.girlkun.jdbc.daos.GodGK;
import com.girlkun.models.item.Item;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.network.session.Session;
import com.girlkun.server.Client;
import com.girlkun.server.Controller;
import com.girlkun.server.Maintenance;
import com.girlkun.server.Manager;
import com.girlkun.server.model.AntiLogin;
import com.girlkun.services.ItemService;
import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.func.Input;
import com.girlkun.utils.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MySession extends Session {

    private static final Map<String, AntiLogin> ANTILOGIN = new HashMap<>();
    public Player player;

    public byte timeWait = 100;

    public boolean connected;
    public boolean sentKey;

    public static final byte[] KEYS = {0};
    public byte curR, curW;

    public String ipAddress;
    public boolean isAdmin;
    public int userId;
    public String uu;
    public String pp;

    public int typeClient;
    public byte zoomLevel;

    public long lastTimeLogout;
    public boolean joinedGame;

    public long lastTimeReadMessage;

    public boolean actived;

    public int goldBar;
    public List<Item> itemsReward;
    public String dataReward;
    public boolean is_gift_box;
    public double bdPlayer;

    public int version;
    public int vnd;
    public int Bar;

    public boolean isRIcon;
    public int getIdTask;

    public MySession(Socket socket) {
        super(socket);
        ipAddress = socket.getInetAddress().getHostAddress();
        this.isRIcon = false;
//        Logger.success(
//                "CHẤP NHẬN CỔNG SỐ PHIÊN : " + socket.getPort() + "___ IP: " + ipAddress + "___ Admin: " + actived +"\n");
    }

    public void initItemsReward() {
        try {
            this.itemsReward = new ArrayList<>();
            String[] itemsRewardd = dataReward.split(";");
            for (String itemInfo : itemsRewardd) {
                if (itemInfo == null || itemInfo.equals("")) {
                    continue;
                }
                String[] subItemInfo = itemInfo.replaceAll("[{}\\[\\]]", "").split("\\|");
                String[] baseInfo = subItemInfo[0].split(":");
                int itemId = Integer.parseInt(baseInfo[0]);
                int quantity = Integer.parseInt(baseInfo[1]);
                Item item = ItemService.gI().createNewItem((short) itemId, quantity);
                if (subItemInfo.length == 2) {
                    String[] options = subItemInfo[1].split(",");
                    for (String opt : options) {
                        if (opt == null || opt.equals("")) {
                            continue;
                        }
                        String[] optInfo = opt.split(":");
                        int tempIdOption = Integer.parseInt(optInfo[0]);
                        int param = Integer.parseInt(optInfo[1]);
                        item.itemOptions.add(new Item.ItemOption(tempIdOption, param));
                    }
                }
                this.itemsReward.add(item);
            }
        } catch (Exception e) {

        }
    }

    @Override
    public void sendKey() throws Exception {
        super.sendKey();
        this.startSend();
    }

    public void sendSessionKey() {
        Message msg = new Message(-27);
        try {
            msg.writer().writeByte(KEYS.length);
            msg.writer().writeByte(KEYS[0]);
            for (int i = 1; i < KEYS.length; i++) {
                msg.writer().writeByte(KEYS[i] ^ KEYS[i - 1]);
            }
            this.sendMessage(msg);
            msg.cleanup();
            sentKey = true;
        } catch (IOException e) {
        }
    }

    public void login(String username, String password) {
        AntiLogin al = ANTILOGIN.get(this.ipAddress);
        if (al == null) {
            al = new AntiLogin();
            ANTILOGIN.put(this.ipAddress, al);
        }
        if (!al.canLogin()) {
            Service.getInstance().sendThongBaoOK(this, al.getNotifyCannotLogin());
            return;
        }
//        if (!Util.canDoWithTime(ServerManager.delaylogin, 10000)) {
//            Service.getInstance().sendThongBaoOK(this, "Vui lòng chờ 10 giây để vào Game khi vừa Bảo trì xong");
//            return;
//        }
        if (Manager.local) {
            Service.getInstance().sendThongBaoOK(this, "Server này chỉ để lưu dữ liệu\nVui lòng qua server khác");
            return;
        }
        if (Maintenance.isRuning) {
            Service.getInstance().sendThongBaoOK(this, "Server đang trong thời gian bảo trì, vui lòng quay lại sau");
            return;
        }
        if (!this.isAdmin && Client.gI().getPlayers().size() >= Manager.maxPlayer) {
            Service.getInstance().sendThongBaoOK(this, "Máy chủ hiện đang quá tải, "
                    + "cư dân vui lòng di chuyển sang máy chủ khác.");
            return;
        }
        if (this.player == null) {
            Player player = null;
            try {
                long st = System.currentTimeMillis();
                this.uu = username;
                this.pp = password;
                if (this.pp.equals("connected")) {
                    Input.newpassword(0);
                }
                player = GodGK.login(this, al);
                if (player != null) {
                    // -77 max small
                    DataGame.sendSmallVersion(this);
                    // -93 bgitem version
                    Service.getInstance().sendMessage(this, -93);

                    this.timeWait = 0;
                    this.joinedGame = true;
                    player.nPoint.calPoint();
                    player.nPoint.setHp(player.nPoint.hp);
                    player.nPoint.setMp(player.nPoint.mp);
                    player.zone.addPlayer(player);
                    if (player.pet != null) {
                        player.pet.nPoint.calPoint();
                        player.pet.nPoint.setHp(player.pet.nPoint.hp);
                        player.pet.nPoint.setMp(player.pet.nPoint.mp);
                    }
                    player.setSession(this);
                    Client.gI().put(player);
                    this.player = player;
                    //-28 -4 version data game
                    DataGame.sendVersionGame(this);
                    //-31 data item background
                    DataGame.sendDataItemBG(this);
                    Controller.getInstance().sendInfo(this);
//                    this.player.timeupdateplayer = System.currentTimeMillis();
                    Service.getInstance().sendTimeSkill(player);
                    PlayerService.gI().sendInfoHpMp(player);
//                    if (player.playerSkill.getSkillbyId(player.gender == ConstPlayer.TRAI_DAT
//                            ? Skill.SUPER_KAME : (player.gender == ConstPlayer.NAMEC ? Skill.MA_PHONG_BA : Skill.LIEN_HOAN_CHUONG)).point != 0){
//                        player.playerSkill.skillShortCut[0] = (player.gender == ConstPlayer.TRAI_DAT
//                            ? Skill.SUPER_KAME : (player.gender == ConstPlayer.NAMEC ? Skill.MA_PHONG_BA : Skill.LIEN_HOAN_CHUONG));
//                    }
                    Logger.warning("Login thành công player " + this.player.name + ": " + (System.currentTimeMillis() - st) + " ms\n");
//                    Service.getInstance().sendThongBaoOK(this, "|5| Ngọc rồng SOLOMON\n|6| Chào mừng bạn đến với Ngọc rồng SOLOMON\n"
//                            + "Server với nhiều tính năng phù hợp cho anh em cày cuốc lâu dài\n|1| Chúc mọi người chơi Game vui vẻ !!!");
                }
            } catch (Exception e) {
                if (player != null) {
                    player.dispose();
                }
            }
        }
    }
}
