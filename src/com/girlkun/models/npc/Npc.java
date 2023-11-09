package com.girlkun.models.npc;

import com.girlkun.consts.ConstNpc;
import com.girlkun.models.map.Map;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.services.MapService;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

public abstract class Npc implements IAtionNpc {

    public int mapId;
    public Map map;

    public int status;

    public int cx;

    public int cy;

    public int tempId;

    public int avartar;

    public BaseMenu baseMenu;

    protected Npc(int mapId, int status, int cx, int cy, int tempId, int avartar) {
        this.map = MapService.gI().getMapById(mapId);
        this.mapId = mapId;
        this.status = status;
        this.cx = cx;
        this.cy = cy;
        this.tempId = tempId;
        this.avartar = avartar;
        Manager.NPCS.add(this);
    }

    public void initBaseMenu(String text) {
        text = text.substring(1);
        String[] data = text.split("\\|");
        baseMenu = new BaseMenu();
        baseMenu.npcId = tempId;
        baseMenu.npcSay = data[0].replaceAll("<>", "\n");
        baseMenu.menuSelect = new String[data.length - 1];
        for (int i = 0; i < baseMenu.menuSelect.length; i++) {
            baseMenu.menuSelect[i] = data[i + 1].replaceAll("<>", "\n");
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String... menuSelect) {
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("zzzzz");
        }
    }

    public void createOtherMenu(Player player, int indexMenu, String npcSay, String[] menuSelect, Object object) {
        NpcFactory.PLAYERID_OBJECT.put(player.id, object);
        Message msg;
        try {
            player.iDMark.setIndexMenu(indexMenu);
            msg = new Message(32);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(npcSay);
            msg.writer().writeByte(menuSelect.length);
            for (String menu : menuSelect) {
                msg.writer().writeUTF(menu);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("xxxxx");
        }
    }

    @Override
    public void openBaseMenu(Player player) {
        if (canOpenNpc(player)) {
            player.iDMark.setIndexMenu(ConstNpc.BASE_MENU);
            try {
                if (baseMenu != null) {
                    baseMenu.openMenu(player);
                } else {
                    Message msg;
                    msg = new Message(32);
                    msg.writer().writeShort(tempId);
                    msg.writer().writeUTF("Cậu muốn gì ở tôi?");
                    msg.writer().writeByte(1);
                    msg.writer().writeUTF("Không");
                    player.sendMessage(msg);
                    msg.cleanup();
                }
            } catch (Exception e) {
                Logger.logException(Npc.class, e);
            }
        }
    }

    public void npcChat(Player player, String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void npcChat(String text) {
        Message msg;
        try {
            msg = new Message(124);
            msg.writer().writeShort(tempId);
            msg.writer().writeUTF(text);
            for (Zone zone : map.zones) {
                Service.getInstance().sendMessAllPlayerInMap(zone, msg);
            }
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public boolean canOpenNpc(Player player) {
        if (player.highPurchaseCount == 0) {
            if (player.isBeQuynh == false) {
                if (this.tempId == ConstNpc.DAU_THAN) {
                    if (player.zone.map.mapId == 21
                            || player.zone.map.mapId == 22
                            || player.zone.map.mapId == 23) {
                        return true;
                    } else {
                        Service.getInstance().hideWaitDialog(player);
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                        return false;
                    }
                }
                if (player.zone.map.mapId == this.mapId
                        && Util.getDistance(this.cx, this.cy, player.location.x, player.location.y) <= 60) {
                    player.iDMark.setNpcChose(this);
                    return true;
                } else {
                    Service.getInstance().hideWaitDialog(player);
                    Service.getInstance().sendThongBao(player, "Không thể thực hiện khi đứng quá xa");
                    return false;
                }
            } else {
                Service.getInstance().hideWaitDialog(player);
                Service.getInstance().sendThongBao(player, "|7|Không thể thực hiện khi đang Hộ tống");
                return false;
            }
        } else {
            Service.getInstance().hideWaitDialog(player);
            Service.getInstance().sendThongBao(player, "|7|Không thể thực hiện khi đang Auto mua Vật phẩm");
            return false;
        }
    }

}
