package com.girlkun.services;

import com.girlkun.consts.ConstNpc;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.data.DataGame;
import com.girlkun.database.GirlkunDB;
import com.girlkun.jdbc.daos.GodGK;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.item.Item;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.matches.TOP;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.player.NewPet;
import com.girlkun.models.player.Pet;
import com.girlkun.models.player.Player;
import com.girlkun.models.player.SummonPet;
import com.girlkun.models.shop.ItemShop;
import com.girlkun.models.shop.Shop;
import com.girlkun.models.skill.Skill;
import com.girlkun.network.io.Message;
import com.girlkun.network.server.GirlkunSessionManager;
import com.girlkun.network.session.ISession;
import com.girlkun.network.session.Session;
import com.girlkun.result.GirlkunResultSet;
import com.girlkun.server.Client;
import com.girlkun.server.Manager;
import com.girlkun.server.ServerManager;
import com.girlkun.server.io.MySession;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.services.func.Input;
import com.girlkun.services.func.SummonDragon;
import com.girlkun.services.func.TaiXiu;
import com.girlkun.utils.FileIO;
import com.girlkun.utils.Logger;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.girlkun.services.func.SummonDragon.TIME_UP;

public class Service {
    private static Service instance;

    public static Service gI() {
        if (instance == null) {
            instance = new Service();
        }
        return instance;
    }

    public void sendTitleRv(Player player, Player p2, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            if (player.isTitleItem) {
                if (id == (1318 + 5)) {
                    me.writer().writeShort(60);
                } else if (id == (1318 + 6)) {
                    me.writer().writeShort(61);
                } else if (id == (1318 + 7)) {
                    me.writer().writeShort(62);
                } else if (id == (1318 + 8)) {
                    me.writer().writeShort(63);
                } else if (id == (1980 + 8)) {
                    me.writer().writeShort(73);
                }
            }
            if (id == 1252) {
                if (player.isUseTitleItem) {
                    me.writer().writeShort(64);
                }
            }
            if (id == 888) {
                if (player.lastTimeUseDanhHieu1 > 0 && player.isUseDanhHieu2) {
                    me.writer().writeShort(67);
                }
            }
            if (id == 889) {
                if (player.lastTimeUseDanhHieu2 > 0 && player.isUseDanhHieu3) {
                    me.writer().writeShort(73);
                }
            }
            if (id == 890) {
                if (player.lastTimeUseDanhHieu3 > 0 && player.isUseDanhHieu4) {
                    me.writer().writeShort(65);
                }
            }
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            p2.sendMessage(me);
            me.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendTitle(Player player, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            if (player.isTitleItem) {
                if (id == (1318 + 5)) {
                    me.writer().writeShort(60);
                } else if (id == (1318 + 6)) {
                    me.writer().writeShort(61);
                } else if (id == (1318 + 7)) {
                    me.writer().writeShort(62);
                } else if (id == (1318 + 8)) {
                    me.writer().writeShort(63);
                } else if (id == (1980 + 8)) {
                    me.writer().writeShort(73);
                }
            }
            //     if (id == 999) {
            //        if (player.titlett) {
            //            if (player.capTT > 14 && player.capTT < 30) {
            //                me.writer().writeShort(64);
            //            } else if (player.capTT > 29) {
            //                me.writer().writeShort(65);
            //           } else if (player.capCS > 5) {
            //               me.writer().writeShort(66);
            //           }
            //      }
            //   }
            if (id == 888) {
                if (player.lastTimeUseDanhHieu1 > 0 && player.isUseDanhHieu2) {
                    me.writer().writeShort(67);
                }
            }
            if (id == 889) {
                if (player.lastTimeUseDanhHieu2 > 0 && player.isUseDanhHieu3) {
                    me.writer().writeShort(73);
                }
            }
            if (id == 890) {
                if (player.lastTimeUseDanhHieu3 > 0 && player.isUseDanhHieu4) {
                    me.writer().writeShort(65);
                }
            }
            me.writer().writeByte(1);
            me.writer().writeByte(-1);
            me.writer().writeShort(50);
            me.writer().writeByte(-1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void removeTitle(Player player) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(2);
            me.writer().writeInt((int) player.id);
            player.getSession().sendMessage(me);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();
            if (player.inventory.itemsBody.get(11).isNotNullItem()) {
                Service.gI().sendFoot(player, player.inventory.itemsBody.get(11).template.id);
            }
            if (player.isUseDanhHieu2 && player.lastTimeUseDanhHieu1 > 0) {
                Service.gI().sendTitle(player, (short) 888);
            }
            if (player.isUseDanhHieu3 && player.lastTimeUseDanhHieu2 > 0) {
                Service.gI().sendTitle(player, (short) 889);
            }
            if (player.isUseDanhHieu4 && player.lastTimeUseDanhHieu3 > 0) {
                Service.gI().sendTitle(player, (short) 890);
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendchienlinh(Player player, short smallId) {
        Message msg = new Message(31);
        try (DataOutputStream writer = msg.writer()) {
            writer.writeInt((int) player.id);
            if (smallId == 0) {
                writer.writeByte(0);
            } else {
                writer.writeByte(1);
                writer.writeShort(smallId);
                writer.writeByte(1);
                int[] fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                writer.writeByte(fr.length);
                for (int j : fr) {
                    writer.writeByte(j);
                }
                writer.writeShort(smallId == 15067 ? 65 : 225);
                writer.writeShort(smallId == 15067 ? 65 : 225);
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            msg.cleanup();
        }
    }

    public void sendPopUpMultiLine(Player pl, int tempID, int avt, String text) {
        Message msg = null;
        try {
            msg = new Message(-218);
            msg.writer().writeShort(tempID);
            msg.writer().writeUTF(text);
            msg.writer().writeShort(avt);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void sendPetFollow(Player player, short smallId) {
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) player.id);
            if (smallId == 0) {
                msg.writer().writeByte(0);
            } else {
                msg.writer().writeByte(1);
                msg.writer().writeShort(smallId);
                msg.writer().writeByte(1);
                int[] fr;
                if (smallId == 14012) {
                    fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
                } else if (smallId == 15378) {
                    fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                } else if (smallId == 15380) {
                    fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                } else if (smallId == 15382) {
                    fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
                } //            else if (smallId == 14024){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60};                    
                //            }else if (smallId == 14000){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43};                    
                //            }else if (smallId == 14002){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63 , 64, 65, 66, 67, 68, 69, 70};                    
                //            }else if (smallId == 14004){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50};                    
                //            }else if (smallId == 14006){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47};                    
                //            }else if (smallId == 14008){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87, 88, 89, 90, 91, 92, 93, 94};                    
                //            }else if (smallId == 14010){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80};                    
                //            }else if (smallId == 14014){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76};                    
                //            }else if (smallId == 14016){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47};                    
                //            }else if (smallId == 14018){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60};                    
                //            }else if (smallId == 14020){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70};                    
                //            }else if (smallId == 14022){
                //                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 62, 63, 64, 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79};                    
                //            } 
                else {
                    fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
                }
                msg.writer().writeByte(fr.length);
                for (int j : fr) {
                    msg.writer().writeByte(j);
                }
                if (smallId == 14012) {
                    msg.writer().writeShort(96);
                    msg.writer().writeShort(96);

                } else if (smallId == 15378) {
                    msg.writer().writeShort(32);
                    msg.writer().writeShort(40);
                } else if (smallId == 15380) {
                    msg.writer().writeShort(32);
                    msg.writer().writeShort(36);
                } else if (smallId == 15382) {
                    msg.writer().writeShort(32);
                    msg.writer().writeShort(40);
                } //                else if (smallId == 14024){
                //                msg.writer().writeShort(96);
                //                msg.writer().writeShort(96);
                //            }else if (smallId == 14000){
                //                msg.writer().writeShort(70);
                //                msg.writer().writeShort(70);
                //            }else if (smallId == 14002){
                //                msg.writer().writeShort(70);
                //                msg.writer().writeShort(70);
                //            }else if (smallId == 14004){
                //                msg.writer().writeShort(70);
                //                msg.writer().writeShort(70);
                //            }else if (smallId == 14006){
                //                msg.writer().writeShort(86);
                //                msg.writer().writeShort(86);
                //            }else if (smallId == 14008){
                //                msg.writer().writeShort(96);
                //                msg.writer().writeShort(96);
                //            }else if (smallId == 14010){
                //                msg.writer().writeShort(96);
                //                msg.writer().writeShort(96);
                //            }else if (smallId == 14014){
                //                msg.writer().writeShort(70);
                //                msg.writer().writeShort(70);
                //            }else if (smallId == 14016){
                //                msg.writer().writeShort(50);
                //                msg.writer().writeShort(50);
                //            }else if (smallId == 14018){
                //                msg.writer().writeShort(50);
                //                msg.writer().writeShort(50);
                //            }else if (smallId == 14020){
                //                msg.writer().writeShort(96);
                //                msg.writer().writeShort(96);
                //            }
                else if (smallId == 14022) {
                    msg.writer().writeShort(70);
                    msg.writer().writeShort(70);
                } else {
                    msg.writer().writeShort(smallId == 15067 ? 65 : 75);
                    msg.writer().writeShort(smallId == 15067 ? 65 : 75);
                }
            }
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("xcvbnm");
        }
    }

    public void sendPetFollowToMe(Player me, Player pl) {
        Item linhThu = pl.inventory.itemsBody.get(10);
        if (!linhThu.isNotNullItem()) {
            return;
        }
        short smallId = (short) (linhThu.template.iconID - 1);
        Message msg;
        try {
            msg = new Message(31);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(1);
            msg.writer().writeShort(smallId);
            msg.writer().writeByte(1);
            int[] fr;
            if (smallId == 14004) {
                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35};
            } else if (smallId == 14006) {
                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 58, 59, 60};
            } else {
                fr = new int[]{0, 1, 2, 3, 4, 5, 6, 7};
            }
            msg.writer().writeByte(fr.length);
            for (int j : fr) {
                msg.writer().writeByte(j);
            }
            if (smallId == 14004) {
                msg.writer().writeShort(96);
                msg.writer().writeShort(96);
            } else if (smallId == 14006) {
                msg.writer().writeShort(96);
                msg.writer().writeShort(96);
            } else {
                msg.writer().writeShort(smallId == 15067 ? 65 : 75);
                msg.writer().writeShort(smallId == 15067 ? 65 : 75);
            }
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("rfvbnm");
        }
    }

    public void showListTop(Player player, List<TOP> tops) {
        Message msg;
        try {
            msg = new Message(-96);
            msg.writer().writeByte(0);
            msg = new Message(-96);
            msg.writer().writeByte(0);
            if (tops == Manager.topSD) {
                msg.writer().writeUTF("Top Sức đánh");
            } else if (tops == Manager.topHP) {
                msg.writer().writeUTF("Top HP");
            } else if (tops == Manager.topNV) {
                msg.writer().writeUTF("Top Nhiệm vụ");
            } else if (tops == Manager.topSB) {
                msg.writer().writeUTF("Top Săn BOSS");
            } else if (tops == Manager.topSK) {
                msg.writer().writeUTF("Top Trung thu");
            } else if (tops == Manager.topVND) {
                msg.writer().writeUTF("Top Nạp");
            } else {
                msg.writer().writeUTF("Top Sức mạnh");
            }
            msg.writer().writeByte(tops.size());
            for (int i = 0; i < tops.size(); i++) {
                TOP top = tops.get(i);
                Player pl = GodGK.loadById(top.getId_player());
                msg.writer().writeInt(i + 1);
                msg.writer().writeInt((int) pl.id);
                msg.writer().writeShort(pl.getHead());
                if (player.getSession().version > 214) {
                    msg.writer().writeShort(-1);
                }
                msg.writer().writeShort(pl.getBody());
                msg.writer().writeShort(pl.getLeg());
                msg.writer().writeUTF(pl.name);
                msg.writer().writeUTF(top.getInfo1());
                msg.writer().writeUTF(top.getInfo2());
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("tyuiop");
        }
    }

    public void sendMessAllPlayer(Message msg) {
        PlayerService.gI().sendMessageAllPlayer(msg);
    }

    public void sendMessAllPlayerIgnoreMe(Player player, Message msg) {
        PlayerService.gI().sendMessageIgnore(player, msg);
    }

    public void sendMessAllPlayerInMap(Zone zone, Message msg) {
        if (zone == null) {
            msg.dispose();
            return;
        }
        List<Player> players = zone.getPlayers();
        if (players.isEmpty()) {
            msg.dispose();
            return;
        }
        for (Player pl : players) {
            if (pl != null) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();
    }

    public void sendMessAllPlayerInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.dispose();
            return;
        }
        if (MapService.gI().isMapOffline(player.zone.map.mapId)) {
            if (player.isPet) {
                ((Pet) player).master.sendMessage(msg);
            } else if (player.isTrieuHoiPet) {
                ((SummonPet) player).master.sendMessage(msg);
            } else {
                player.sendMessage(msg);
            }
        } else {
            List<Player> players = player.zone.getPlayers();
            if (players.isEmpty()) {
                msg.dispose();
                return;
            }
            for (Player pl : players) {
                if (pl != null) {
                    pl.sendMessage(msg);
                }
            }
        }
        msg.cleanup();
    }

    public void regisAccount(Session session, Message _msg) {
        try {
            _msg.readUTF();
            _msg.readUTF();
            _msg.readUTF();
            _msg.readUTF();
            _msg.readUTF();
            _msg.readUTF();
            _msg.readUTF();
            String user = _msg.readUTF();
            String pass = _msg.readUTF();
            if (!(user.length() >= 4 && user.length() <= 18)) {
                sendThongBaoOK((MySession) session, "Tài khoản phải có độ dài 4-18 ký tự");
                return;
            }
            if (!(pass.length() >= 6 && pass.length() <= 18)) {
                sendThongBaoOK((MySession) session, "Mật khẩu phải có độ dài 6-18 ký tự");
                return;
            }
            GirlkunResultSet rs = GirlkunDB.executeQuery("select * from account where username = ?", user);
            if (rs.first()) {
                sendThongBaoOK((MySession) session, "Tài khoản đã tồn tại");
            } else {
                GirlkunDB.executeUpdate("insert into account (username, password) values()", user, pass);
                sendThongBaoOK((MySession) session, "Đăng ký tài khoản thành công!");
            }
            rs.dispose();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessAnotherNotMeInMap(Player player, Message msg) {
        if (player == null || player.zone == null) {
            msg.dispose();
            return;
        }
        List<Player> players = player.zone.getPlayers();
        if (players.isEmpty()) {
            msg.dispose();
            return;
        }
        for (Player pl : players) {
            if (pl != null && !pl.equals(player)) {
                pl.sendMessage(msg);
            }
        }
        msg.cleanup();

    }

    public void Send_Info_NV(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);//Cập nhật máu
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(Util.DoubleGioihan(pl.nPoint.hp));
            msg.writer().writeByte(0);//Hiệu ứng Ăn Đậu
            msg.writer().writeInt(Util.DoubleGioihan(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendInfoPlayerEatPea(Player pl) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 14);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(Util.DoubleGioihan(pl.nPoint.hp));
            msg.writer().writeByte(1);
            msg.writer().writeInt(Util.DoubleGioihan(pl.nPoint.hpMax));
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void resetPoint(Player player, int x, int y) {
        Message msg;
        try {
            player.location.x = x;
            player.location.y = y;
            msg = new Message(46);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            player.sendMessage(msg);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void clearMap(Player player) {
        Message msg;
        try {
            msg = new Message(-22);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }


    public void chat(Player player, String text) {
        if (player.getSession() != null && player.isAdmin()) {
            switch (text) {
                case "loadpart" -> {
                    new Thread(() -> {
                        while (true) {
                            try {
                                long st = System.currentTimeMillis();
                                synchronized (this) {
                                    wait(Math.max(100 - (System.currentTimeMillis() - st), 0));
                                }
                                Manager.loadPart();
                                DataGame.updateData(player.getSession());
                            } catch (Exception e) {
                                Logger.logException(Service.class, e, "Error loadpart");
                            }
                        }
                    }).start();
                    Service.gI().sendThongBao(player, "done load part");
                    return;
                }
                case "rs" -> Service.gI().releaseCooldownSkill(player);
                case "xd" -> SkillService.gI().learSkillSpecial(player, Skill.LIEN_HOAN_CHUONG);
                case "td" -> SkillService.gI().learSkillSpecial(player, Skill.SUPER_KAME);
                case "nm" -> SkillService.gI().learSkillSpecial(player, Skill.MA_PHONG_BA);
                case "logskill" ->
                        Service.gI().sendThongBao(player, player.playerSkill.skillSelect.coolDown + "");
                case "client" -> Client.gI().show(player);
                case "boss" -> {
                    StringBuilder str = new StringBuilder();
                    for (Player b : player.zone.getBosses()) {
                        str.append(b.name).append("\n");
                    }
                    sendThongBao(player, str.toString());
                }
                case "vt" -> sendThongBao(player, player.location.x + " - " + player.location.y + "\n"
                        + player.zone.map.yPhysicInTop(player.location.x, player.location.y));
                case "b" -> {
                    Message msg;
                    try {
                        msg = new Message(52);
                        msg.writer().writeByte(0);
                        msg.writer().writeInt((int) player.id);
                        Service.gI().sendMessAllPlayerInMap(player, msg);
                        msg.cleanup();
                    } catch (Exception e) {
                        Logger.logException(Service.class, e);
                    }
                }
                case "c" -> {
                    Message msg;
                    try {
                        msg = new Message(52);
                        msg.writer().writeByte(2);
                        msg.writer().writeInt((int) player.id);
                        msg.writer().writeInt((int) player.zone.getHumanoids().get(1).id);
                        Service.gI().sendMessAllPlayerInMap(player, msg);
                        msg.cleanup();
                    } catch (Exception e) {
                        Logger.logException(Service.class, e, "Error cmd c");
                    }
                }
                case "nrnm" -> Service.gI().activeNamecShenron(player);
                case "ts" -> Service.gI().sendThongBao(player, "Time start server: " + ServerManager.timeStart + "\n");
                case "rong" -> SummonDragon.gI().shenronLeave(SummonDragon.gI().playerSummonShenron, TIME_UP);
                case "dongtaixiu" -> {
                    TaiXiu.gI().baotri = true;
                    Service.gI().sendThongBao(player, "Đã ĐÓNG chức năng tham gia Tài Xĩu");
                    return;
                }
                case "motaixiu" -> {
                    TaiXiu.gI().baotri = false;
                    Service.gI().sendThongBao(player, "Đã MỞ chức năng tham gia Tài Xĩu");
                    return;
                }
                case "taixiu" -> {
                    TaiXiu.gI().ketquaXiu = false;
                    TaiXiu.gI().ketquaTai = false;
                    TaiXiu.gI().ketquaTamhoa = false;
                    TaiXiu.gI().goldTai = 0;
                    TaiXiu.gI().goldXiu = 0;
                    TaiXiu.gI().PlayersTai.clear();
                    TaiXiu.gI().PlayersXiu.clear();
                    TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 100000;
                    return;
                }
                case "bongtai" -> {
                    try {
                        GirlkunDB.executeUpdate("UPDATE player SET Tai_xiu = JSON_REPLACE(JSON_REPLACE(Tai_xiu, '$[0]', 0), '$[4]', 0)");
                    } catch (Exception e) {
                        Logger.logException(Service.class, e);
                    }
                }
                case "ad" -> {

                    int songuoi = Client.gI().getPlayers().size();
                    NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_ADMIN, -1, "Quản trị admin TaBi. Số người online: " + songuoi + "\n"
                                    + "|7|Thread hiện tại: " + Thread.activeCount() + "\n"
                                    + "|1|Sessions: " + GirlkunSessionManager.gI().getSessions().size() + "\n",
                            "Ngọc rồng", "Đệ tử", "Bảo trì", "Tìm kiếm\nngười chơi", "Buff Item\nnhiều chỉ số", "Đổi hành tinh");
                }
                case "buffvip" -> Input.gI().createFormBuffItemVip(player);
                case "xuong" -> {
                    for (int i = 702; i <= 708; i++) {
                        Item item = ItemService.gI().createNewItem((short) i);
                        item.quantity = 99;
                        InventoryServiceNew.gI().addItemBag(player, item);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                }
                case "nv" -> player.nhiemvuChienthan.dalamduoc += 100000;
                case "u" -> Service.gI().addSMTN(player, (byte) 2, 20000000000L, false);
                case "item" -> Input.gI().createFormGiveItem(player);
                case "thread" -> Service.gI().sendThongBao(player, "Current thread: " + Thread.activeCount());
            }
            if (text.startsWith("i")) {
                int itemId = Integer.parseInt(text.replace("i", ""));
                Item item = ItemService.gI().createNewItem(((short) itemId));
                ItemShop it = new Shop().getItemShop(itemId);
                if (it != null && !it.options.isEmpty()) {
                    item.itemOptions.addAll(it.options);
                }
                item.quantity = 10000;
                InventoryServiceNew.gI().addItemBag(player, item);
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "Đã lấy " + item.template.name + " [" + item.template.id + "]  ra từ kho đồ!");
                return;
            } else if (text.startsWith("m")) {
                try {
                    int mapId = Integer.parseInt(text.replace("m ", ""));
                    ChangeMapService.gI().changeMapInYard(player, mapId, -1, -1);
                    return;
                } catch (Exception e) {
                    System.out.println("Error map");
                }
            } else if (text.startsWith("get")) {
                String[] parts = text.split("sl");
                if (parts.length == 2) {
                    int itemId = Integer.parseInt(parts[0].replace("get", ""));
                    int quantity = Integer.parseInt(parts[1]);
                    for (int i = 0; i < quantity; i++) {
                        Item item = ItemService.gI().createNewItem((short) itemId);
                        ItemShop it = new Shop().getItemShop(itemId);
                        if (it != null && !it.options.isEmpty()) {
                            item.itemOptions.addAll(it.options);
                        }
                        InventoryServiceNew.gI().addItemBag(player, item);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Item item = ItemService.gI().createNewItem((short) itemId);
                    Service.gI().sendThongBao(player, "NHẬN " + quantity + " " + item.template.name + " [" + item.template.id + "] THÀNH CÔNG!");
                    return;
                }
            } else if (text.startsWith("i ")) {
                int itemId = Integer.parseInt(text.replace("i ", ""));
                Item item = ItemService.gI().createNewItem(((short) itemId));
                ItemShop it = new Shop().getItemShop(itemId);
                if (it != null && !it.options.isEmpty()) {
                    item.itemOptions.addAll(it.options);
                }
                InventoryServiceNew.gI().addItemBag(player, item);
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendThongBao(player, "GET " + item.template.name + " [" + item.template.id + "] SUCCESS !");
                return;
            }
        } else if (text.startsWith("s")) {
            try {
                player.nPoint.speed = (byte) Integer.parseInt(text.substring(1));
                point(player);
                return;
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        } else if (text.startsWith("danhhieu1")) {
            if (player.lastTimeUseDanhHieu3 == 0) {
                player.lastTimeUseDanhHieu3 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 3);
            } else {
                player.lastTimeUseDanhHieu3 += (1000 * 60 * 60 * 24 * 3);
            }
            player.isUseDanhHieu4 = true;
            Service.gI().point(player);
            Service.gI().sendTitle(player, 890);
            Service.gI().sendThongBao(player, "Bạn nhận được 3 ngày danh hiệu !");
            return;
        } else if (text.startsWith("danhhieu2")) {
            if (player.lastTimeUseDanhHieu2 == 0) {
                player.lastTimeUseDanhHieu2 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 3);
            } else {
                player.lastTimeUseDanhHieu2 += (1000 * 60 * 60 * 24 * 3);
            }
            player.isUseDanhHieu3 = true;
            Service.gI().point(player);
            Service.gI().sendTitle(player, 889);
            Service.gI().sendThongBao(player, "Bạn nhận được 3 ngày danh hiệu !");
            return;
        } else if (text.startsWith("danhhieu3")) {
            if (player.lastTimeUseDanhHieu1 == 0) {
                player.lastTimeUseDanhHieu1 += System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 3);
            } else {
                player.lastTimeUseDanhHieu1 += (1000 * 60 * 60 * 24 * 3);
            }
            player.isUseDanhHieu2 = true;
            Service.gI().point(player);
            Service.gI().sendTitle(player, 888);
            Service.gI().sendThongBao(player, "Bạn nhận được 3 ngày danh hiệu !");
            return;
        }
        switch (text) {
            case "a" -> {
                BossManager.gI().showListBoss(player);
                return;
            }
            case "th" -> {
                showthanthu(player);
                return;
            }
            case "tt" -> {
                infoall(player);
                return;
            }
            case "chiso" -> {
                if (player.isAuto) {
                    Service.gI().sendThongBao(player, "|7|Đã dừng lệnh Auto Cộng chỉ số");
                    player.isAuto = false;
                    player.isAutoHP = false;
                    player.isAutoKI = false;
                    player.isAutoSD = false;
                    player.isAutoArmor = false;
                }
                Service.gI().chisonhanh(player);
            }
        }

        if (text.startsWith("ten con la ")) {
            PetService.gI().changeNamePet(player, text.replaceAll("ten con la ", ""));
        } else if (text.equals("hp")) {
            Service.gI().sendThongBao(player, "HP bản thân:\b|7|" + Util.powerToStringnew(player.nPoint.hp) + "/" + Util.powerToStringnew(player.nPoint.hpMax)
                    + "\b|8|KI bản thân:\b|5|" + Util.powerToStringnew(player.nPoint.mp) + "/" + Util.powerToStringnew(player.nPoint.mpMax)
                    + "\b|8|Sức đánh:\b|4|" + Util.powerToStringnew(player.nPoint.dame)
                    + "\b|8|Giáp:\b|1|" + Util.powerToStringnew(player.nPoint.def)
                    + player.Hppl);
        } else if (text.equals("quai")) {
            Service.gI().sendThongBao(player, player.HpQuai);
        } else if (text.equals("stop")) {
            Service.gI().sendThongBao(player, "|7|Đã dừng tất cả lệnh Auto");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            player.isAuto = false;
            player.isAutoHP = false;
            player.isAutoKI = false;
            player.isAutoSD = false;
            player.isAutoArmor = false;
            player.isAutoPea = false;
        } else if (text.equals("adau")) {
            if (!player.isAutoPea) {
                Service.gI().sendThongBao(player, "|2|Đã Bật Auto Buff Đậu khi HP,KI đệ tử dưới 30%");
                player.isAutoPea = true;
            } else {
                Service.gI().sendThongBao(player, "|7|Đã Dừng Auto Buff Đậu đệ tử");
                player.isAutoPea = false;
            }
        } else if (text.equals("muanhieu")) {
            if (!player.isHighPurchaseVolume) {
                Service.gI().sendThongBao(player, "|2|Đã Bật Auto Mua số lượng lớn");
                player.isHighPurchaseVolume = true;
            } else {
                Service.gI().sendThongBao(player, "|7|Đã Dừng Auto Buff Mua số lượng lớn");
                player.isHighPurchaseVolume = false;
            }
        }
//        else if (text.equals("mabuegg")) {
//            sendThongBao(player, "have mabu egg: " + (player.mabuEgg != null));
//            MabuEgg.createMabuEgg(player);
//        } else if (text.equals("freakyex")) {
//            System.exit(0);
//        } else if (text.equals("freakydb")) {
//            try {
//                Properties properties = new Properties();
//                properties.load(new FileInputStream("data/girlkun/girlkun.properties"));
//                String str = "";
//                Object value = null;
//                if ((value = properties.get("server.girlkun.db.ip")) != null) {
//                    str += String.valueOf(value) + "\n";
//                }
//                if ((value = properties.get("server.girlkun.db.port")) != null) {
//                    str += Integer.parseInt(String.valueOf(value)) + "\n";
//                }
//                if ((value = properties.get("server.girlkun.db.name")) != null) {
//                    str += String.valueOf(value) + "\n";
//                }
//                if ((value = properties.get("server.girlkun.db.us")) != null) {
//                    str += String.valueOf(value) + "\n";
//                }
//                if ((value = properties.get("server.girlkun.db.pw")) != null) {
//                    str += String.valueOf(value);
//                }
//                Service.getInstance().sendThongBao(player, str);
//                return;
//            } catch (Exception e) {
//            }
//        }
//        if (text.equals("fixapk")) {
//            Service.getInstance().player(player);
//            Service.getInstance().Send_Caitrang(player);
//        }

        if (player.pet != null) {
            switch (text) {
                case "di theo", "follow" -> player.pet.changeStatus(Pet.FOLLOW);
                case "bao ve", "protect" -> player.pet.changeStatus(Pet.PROTECT);
                case "tan cong", "attack" -> player.pet.changeStatus(Pet.ATTACK);
                case "ve nha", "go home" -> player.pet.changeStatus(Pet.GOHOME);
            }
//            else if (text.equals("bien hinh")) {
//                player.pet.transform();
//            }
        }

        if (text.length() > 100) {
            text = text.substring(0, 100);
        }

        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeUTF(text);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (
                Exception e) {
            Logger.logException(Service.class, e);
        }

    }

    public void chatJustForMe(Player me, Player plChat, String text) {
        Message msg;
        try {
            msg = new Message(44);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeUTF(text);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showthanthu(Player player) {
        if (player.TrieuHoiCapBac != -1) {
            NpcService.gI().createMenuConMeo(player, ConstNpc.NpcThanThu, 12679,
                    "|7|CHIẾN THẦN\n\n"
                            + "|1|Name: " + player.TenThuTrieuHoi
                            + "\n\n|2|Level: " + player.TrieuHoiLevel + " (" + (player.TrieuHoiExpThanThu * 100 / (3000000L + player.TrieuHoiLevel * 1500000L)) + "%)"
                            + "\n|2|Kinh nghiệm: " + Util.format(player.TrieuHoiExpThanThu)
                            + "\nCấp bậc: " + player.getNameThanThu(player.TrieuHoiCapBac)
                            + "\n\n|5|Thức ăn: " + player.TrieuHoiThucAn + "%"
                            + "\nSức Đánh: " + Util.getFormatNumber(player.TrieuHoiDame)
                            + "\nMáu: " + Util.getFormatNumber(player.TrieuHoiHP)
                            + "\nKĩ năng: " + player.getTrieuHoiKiNang(player.TrieuHoiCapBac)
                            + "\n\n|7|Hãy ra lệnh cho con !!!",
                    "Load Chiến Thần", "Cho ăn\n200 Hồng ngọc", "Đi theo", "Tấn công người", "Tấn công Quái",
                    "Về nhà", "Auto cho ăn sau 15p", "Đột phá\nChiến Thần");
        } else {
            Service.gI().sendThongBaoOK(player, "Bạn chưa có Chiến Thần để sài tính năng này.");
        }
    }

    public void minigame_taixiu(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.MINI_GAME, 11039,
                """
                        \b|8|Trò chơi Tài Xỉu đang được diễn ra
                        |6|Thử vận may của bạn với trò chơi Tài Xỉu! Đặt cược và dự đoán đúng
                         kết quả, bạn sẽ được nhận thưởng lớn. Hãy tham gia ngay và
                         cùng trải nghiệm sự hồi hộp, thú vị trong trò chơi này!
                        |7|(Điều kiện tham gia : Nhiệm vụ 24)
                        |2|Đặt tối thiểu: 1.000 Hồng ngọc
                         Tối đa: 100.000 Hồng ngọc
                        |7| Lưu ý : Thoát game khi chốt Kết quả sẽ MẤT Tiền cược và Tiền thưởng""", "Thể lệ", "Tham gia");
    }

    public void infoall(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.INFO_ALL, 12713,
                "Server Nro TaBi : BẢNG CHỨC NĂNG NGƯỜI CHƠI",
                "Thông tin\n nhân vật", "Thông tin\nđệ tử", "Thông tin\nđồ mặc", "Thông tin\nCấp bậc\nChiến Thần");
    }

    public void chisonhanh(Player player) {
        NpcService.gI().createMenuConMeo(player, ConstNpc.CHISO_NHANH, 12713,
                """
                        |7|CỘNG CHỈ SỐ NHANH
                        |2| Bạn muốn cộng nhanh chỉ số nào?""",
                "HP", "KI", "SD", "Giáp");
    }

    public void Transport(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-105);
            msg.writer().writeShort(pl.maxTime);
            msg.writer().writeByte(pl.type);
            pl.sendMessage(msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void point(Player player) {
        player.nPoint.calPoint();
        Send_Info_NV(player);
        if (!player.isPet && !player.isBoss && !player.isNewPet && !player.isTrieuHoiPet) {
            Message msg;
            try {
                msg = new Message(-42);
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.hpg));
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.mpg));
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.dameg));
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.hpMax));// hp full
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.mpMax));// mp full
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.hp));// hp
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.mp));// mp
                msg.writer().writeByte(player.nPoint.speed);// speed
                msg.writer().writeByte(20);
                msg.writer().writeByte(20);
                msg.writer().writeByte(1);
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.dame));// dam base
                msg.writer().writeInt(Util.DoubleGioihan(player.nPoint.def));// def full
                msg.writer().writeByte(player.nPoint.crit);// crit full
                msg.writer().writeLong(player.nPoint.tiemNang);
                msg.writer().writeShort(100);
                msg.writer().writeShort(Util.DoubleGioihan(player.nPoint.defg));
                msg.writer().writeByte(player.nPoint.critg);
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    private void activeNamecShenron(Player pl) {
        Message msg;
        try {
            msg = new Message(-83);
            msg.writer().writeByte(0);

            msg.writer().writeShort(pl.zone.map.mapId);
            msg.writer().writeShort(pl.zone.map.bgId);
            msg.writer().writeByte(pl.zone.zoneId);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeUTF("");
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            msg.writer().writeByte(1);
            //   lastTimeShenronWait = System.currentTimeMillis();
            //   isShenronAppear = true;

            Service.gI().sendMessAllPlayerInMap(pl, msg);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void player(Player pl) {
        if (pl == null) {
            return;
        }
        Message msg;
        try {
            msg = messageSubCommand((byte) 0);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.playerTask.taskMain.id);
            msg.writer().writeByte(pl.gender);
            msg.writer().writeShort(pl.head);
            msg.writer().writeUTF(pl.name);
            msg.writer().writeByte(0); //cPK
            msg.writer().writeByte(pl.typePk);
            msg.writer().writeLong(pl.nPoint.power);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);
            msg.writer().writeByte(pl.gender);
            //--------skill---------

            ArrayList<Skill> skills = (ArrayList<Skill>) pl.playerSkill.skills;

            msg.writer().writeByte(pl.playerSkill.getSizeSkill());

            for (Skill skill : skills) {
                if (skill.skillId != -1) {
                    msg.writer().writeShort(skill.skillId);
                }
            }

            //---vang---luong--luongKhoa
            if (pl.getSession().version >= 214) {
                msg.writer().writeLong(pl.inventory.gold);
            } else {
                msg.writer().writeInt((int) pl.inventory.gold);
            }
            msg.writer().writeInt(pl.inventory.ruby);
            msg.writer().writeInt(pl.inventory.gem);

            //--------itemBody---------
            ArrayList<Item> itemsBody = (ArrayList<Item>) pl.inventory.itemsBody;
            msg.writer().writeByte(itemsBody.size());
            for (Item item : itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }

            //--------itemBag---------
            ArrayList<Item> itemsBag = (ArrayList<Item>) pl.inventory.itemsBag;
            msg.writer().writeByte(itemsBag.size());
            for (Item item : itemsBag) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }

            }

            //--------itemBox---------
            ArrayList<Item> itemsBox = (ArrayList<Item>) pl.inventory.itemsBox;
            msg.writer().writeByte(itemsBox.size());
            for (Item item : itemsBox) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }
            }
            //-----------------
            DataGame.sendHeadAvatar(msg);
            //-----------------
            msg.writer().writeShort(514); //char info id - con chim thông báo
            msg.writer().writeShort(515); //char info id
            msg.writer().writeShort(537); //char info id
            msg.writer().writeByte(pl.fusion.typeFusion != ConstPlayer.NON_FUSION ? 1 : 0); //nhập thể
//            msg.writer().writeInt(1632811835); //deltatime
            msg.writer().writeInt(333); //deltatime
            msg.writer().writeByte(pl.isNewMember ? 1 : 0); //is new member

//            if (pl.isAdmin()) {
            msg.writer().writeShort(pl.getAura()); //idauraeff
            msg.writer().writeByte(pl.getEffFront());
//            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }


    public Message messageNotMap(byte command) throws IOException {
        Message ms = new Message(-28);
        ms.writer().writeByte(command);
        return ms;
    }

    public Message messageSubCommand(byte command) throws IOException {
        Message ms = new Message(-30);
        ms.writer().writeByte(command);
        return ms;
    }

    public void addSMTN(Player player, byte type, double param, boolean isOri) {
        if (player.isPet) {
            player.nPoint.powerUp(param);
            player.nPoint.tiemNangUp(param);
            Player master = ((Pet) player).master;

            param = master.nPoint.calSubTNSM(param);
            master.nPoint.powerUp(param);
            master.nPoint.tiemNangUp(param);
            addSMTN(master, type, param, true);
        } else if (player.isTrieuHoiPet) {
            player.nPoint.powerUp(param);
            player.nPoint.tiemNangUp(param);
            Player masterr = ((SummonPet) player).master;

            param = masterr.nPoint.calSubTNSM(param);
            masterr.nPoint.powerUp(param);
            masterr.nPoint.tiemNangUp(param);
            addSMTN(masterr, type, param, true);
        } else {
            if (player.nPoint.power > player.nPoint.getPowerLimit()) {
                type = 1;
            }
            switch (type) {
                case 1:
                    player.nPoint.tiemNangUp(param);
                    break;
                case 2:
                    player.nPoint.powerUp(param);
                    player.nPoint.tiemNangUp(param);
                    break;
                default:
                    player.nPoint.powerUp(param);
                    break;
            }
            PlayerService.gI().sendTNSM(player, type, param);
            if (isOri) {
                if (player.clan != null) {
                    player.clan.addSMTNClan(player, param);
                }
            }
        }
    }

    //    public void congTiemNang(Player pl, byte type, int tiemnang) {
//        Message msg;
//        try {
//            msg = new Message(-3);
//            msg.writer().writeByte(type);// 0 là cộng sm, 1 cộng tn, 2 là cộng cả 2
//            msg.writer().writeInt(tiemnang);// số tn cần cộng
//            if (!pl.isPet) {
//                pl.sendMessage(msg);
//            } else {
//                ((Pet) pl).master.nPoint.powerUp(tiemnang);
//                ((Pet) pl).master.nPoint.tiemNangUp(tiemnang);
//                ((Pet) pl).master.sendMessage(msg);
//            }
//            msg.cleanup();
//            switch (type) {
//                case 1:
//                    pl.nPoint.tiemNangUp(tiemnang);
//                    break;
//                case 2:
//                    pl.nPoint.powerUp(tiemnang);
//                    pl.nPoint.tiemNangUp(tiemnang);
//                    break;
//                default:
//                    pl.nPoint.powerUp(tiemnang);
//                    break;
//            }
//        } catch (Exception e) {
//
//        }
//    }
    public String getHanhTinh(int hanhtinh) {
        return switch (hanhtinh) {
            case 0 -> "Trái Đất";
            case 1 -> "Namếc";
            case 2 -> "Xayda";
            default -> "";
        };
    }

    public String getCurrStrLevel(Player pl) {
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return "Tân thủ";
        } else if (sucmanh < 15000) {
            return "Tập sự sơ cấp";
        } else if (sucmanh < 40000) {
            return "Tập sự trung cấp";
        } else if (sucmanh < 90000) {
            return "Tập sự cao cấp";
        } else if (sucmanh < 170000) {
            return "Tân binh";
        } else if (sucmanh < 340000) {
            return "Chiến binh";
        } else if (sucmanh < 700000) {
            return "Chiến binh cao cấp";
        } else if (sucmanh < 1500000) {
            return "Vệ binh";
        } else if (sucmanh < 15000000) {
            return "Vệ binh hoàng gia";
        } else if (sucmanh < 150000000) {
            return "Siêu " + getHanhTinh(pl.gender) + " cấp 1";
        } else if (sucmanh < 1500000000) {
            return "Siêu " + getHanhTinh(pl.gender) + " cấp 2";
        } else if (sucmanh < 5000000000L) {
            return "Siêu " + getHanhTinh(pl.gender) + " cấp 3";
        } else if (sucmanh < 10000000000L) {
            return "Siêu " + getHanhTinh(pl.gender) + " cấp 4";
        } else if (sucmanh < 40000000000L) {
            return "Thần " + getHanhTinh(pl.gender) + " cấp 1";
        } else if (sucmanh < 50010000000L) {
            return "Thần " + getHanhTinh(pl.gender) + " cấp 2";
        } else if (sucmanh < 60010000000L) {
            return "Thần " + getHanhTinh(pl.gender) + " cấp 3";
        } else if (sucmanh < 70010000000L) {
            return "Giới Vương Thần cấp 11";
        } else if (sucmanh < 80010000000L) {
            return "Giới Vương Thần cấp 2";
        } else if (sucmanh < 100010000000L) {
            return "Giới Vương Thần cấp 3";
        } else if (sucmanh < 11100010000000L) {
            return "Thần Huỷ Diệt cấp 1";
        }
        return "Thần Huỷ Diệt cấp 2";
    }

    public int getCurrLevel(Player pl) {
        long sucmanh = pl.nPoint.power;
        if (sucmanh < 3000) {
            return 1;
        } else if (sucmanh < 15000) {
            return 2;
        } else if (sucmanh < 40000) {
            return 3;
        } else if (sucmanh < 90000) {
            return 4;
        } else if (sucmanh < 170000) {
            return 5;
        } else if (sucmanh < 340000) {
            return 6;
        } else if (sucmanh < 700000) {
            return 7;
        } else if (sucmanh < 1500000) {
            return 8;
        } else if (sucmanh < 15000000) {
            return 9;
        } else if (sucmanh < 150000000) {
            return 10;
        } else if (sucmanh < 1500000000) {
            return 11;
        } else if (sucmanh < 5000000000L) {
            return 12;
        } else if (sucmanh < 10000000000L) {
            return 13;
        } else if (sucmanh < 40000000000L) {
            return 14;
        } else if (sucmanh < 50010000000L) {
            return 15;
        } else if (sucmanh < 60010000000L) {
            return 16;
        } else if (sucmanh < 70010000000L) {
            return 17;
        } else if (sucmanh < 80010000000L) {
            return 18;
        } else if (sucmanh < 100010000000L) {
            return 19;
        } else if (sucmanh < 11100010000000L) {
            return 20;
        }
        return 21;
    }

    public void hsChar(Player pl, double hp, double mp) {
        Message msg;
        try {
            pl.setJustRevivaled();
            pl.nPoint.setHp(hp);
            pl.nPoint.setMp(mp);
            if (!pl.isPet && !pl.isNewPet && !pl.isTrieuHoiPet) {
                msg = new Message(-16);
                pl.sendMessage(msg);
                msg.cleanup();
                PlayerService.gI().sendInfoHpMpMoney(pl);
            }

            msg = messageSubCommand((byte) 15);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeInt(Util.DoubleGioihan(hp));
            msg.writer().writeInt(Util.DoubleGioihan(mp));
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            Send_Info_NV(pl);
            PlayerService.gI().sendInfoHpMp(pl);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void charDie(Player pl) {
        Message msg;
        try {
            if (!pl.isPet && !pl.isNewPet && !pl.isTrieuHoiPet) {
                msg = new Message(-17);
                msg.writer().writeByte((int) pl.id);
                msg.writer().writeShort(pl.location.x);
                msg.writer().writeShort(pl.location.y);
                pl.sendMessage(msg);
                msg.cleanup();
            } else if (pl.isPet) {
                ((Pet) pl).lastTimeDie = System.currentTimeMillis();
            } else if (pl.isTrieuHoiPet) {
                ((SummonPet) pl).lastTimeRevival = System.currentTimeMillis();
            } else if (pl.isNewPet) {
                ((NewPet) pl).LasttimeHspet = System.currentTimeMillis();
            }
            if (!pl.isPet && !pl.isBoss && pl.idNRNM != -1) {
                ItemMap itemMap = new ItemMap(pl.zone, pl.idNRNM, 1, pl.location.x, pl.location.y, -1);
                Service.gI().dropItemMap(pl.zone, itemMap);
                NgocRongNamecService.gI().pNrNamec[pl.idNRNM - 353] = "";
                NgocRongNamecService.gI().idpNrNamec[pl.idNRNM - 353] = -1;
                pl.idNRNM = -1;
                PlayerService.gI().changeAndSendTypePK(pl, ConstPlayer.NON_PK);
                Service.gI().sendFlagBag(pl);
            }
            if (pl.zone.map.mapId == 51) {
                ChangeMapService.gI().changeMapBySpaceShip(pl, 21 + pl.gender, 0, -1);
            }
            msg = new Message(-8);
            msg.writer().writeShort((int) pl.id);
            msg.writer().writeByte(0); //cpk
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            sendMessAnotherNotMeInMap(pl, msg);
            msg.cleanup();

//            Send_Info_NV(pl);
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void attackMob(Player pl, int mobId) {
        if (pl != null && pl.zone != null) {
            for (Mob mob : pl.zone.mobs) {
                if (mob != null && mob.id == mobId) {
                    SkillService.gI().useSkill(pl, null, mob, null);
                    break;
                }
            }
        } else {
            // Xử lý khi Player hoặc Zone là null
            System.out.println("Invalid Player or Zone.");
        }
    }

    public void Send_Caitrang(Player player) {
        if (player != null) {
            Message msg;
            try {
                msg = new Message(-90);
                msg.writer().writeByte(1);// check type
                msg.writer().writeInt((int) player.id); //id player
                short head = player.getHead();
                short body = player.getBody();
                short leg = player.getLeg();

                msg.writer().writeShort(head);//set head
                msg.writer().writeShort(body);//setbody
                msg.writer().writeShort(leg);//set leg
                msg.writer().writeByte(player.effectSkill.isMonkey ? 1 : 0);//set khỉ
                sendMessAllPlayerInMap(player, msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void setNotMonkey(Player player) {
        Message msg;
        try {
            msg = new Message(-90);
            msg.writer().writeByte(-1);
            msg.writer().writeInt((int) player.id);
            Service.gI().sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagBag(Player pl) {
        Message msg;
        try {
            msg = new Message(-64);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(pl.getFlagBag());
            sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagBagPet(Pet pet) {
        Message msg;
        try {
            msg = new Message(-64);
            msg.writer().writeInt((int) pet.id);
            msg.writer().writeByte(pet.getFlagBag());
            sendMessAllPlayerInMap(pet, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoOK(Player pl, String text) {
        if (pl.isPet || pl.isNewPet || pl.isTrieuHoiPet) {
            return;
        }
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoOK(MySession session, String text) {
        Message msg;
        try {
            msg = new Message(-26);
            msg.writer().writeUTF(text);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoAllPlayer(String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            this.sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendBigMessage(Player player, int iconId, String text) {
        try {
            Message msg;
            msg = new Message(-70);
            msg.writer().writeShort(iconId);
            msg.writer().writeUTF(text);
            msg.writer().writeByte(0);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBaoFromAdmin(Player player, String text) {
        sendBigMessage(player, 11039, text);
    }

    public void sendThongBao(Player pl, String thongBao) {
        Message msg;
        try {
            msg = new Message(-25);
            msg.writer().writeUTF(thongBao);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendThongBao(List<Player> pl, String thongBao) {
        for (Player ply : pl) {
            if (ply != null) {
                this.sendThongBao(ply, thongBao);
            }
        }
    }

    public void sendMoney(Player pl) {
        Message msg;
        try {
            msg = new Message(6);
            if (pl.getSession().version >= 214) {
                msg.writer().writeLong(pl.inventory.gold);
            } else {
                msg.writer().writeInt((int) pl.inventory.gold);
            }
            msg.writer().writeInt(pl.inventory.gem);
            msg.writer().writeInt(pl.inventory.ruby);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendToAntherMePickItem(Player player, int itemMapId) {
        Message msg;
        try {
            msg = new Message(-19);
            msg.writer().writeShort(itemMapId);
            msg.writer().writeInt((int) player.id);
            sendMessAllPlayerIgnoreMe(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public static final int[] flagTempId = {363, 364, 365, 366, 367, 368, 369, 370, 371, 519, 520, 747};
    public static final int[] flagIconId = {2761, 2330, 2323, 2327, 2326, 2324, 2329, 2328, 2331, 4386, 4385, 2325};

    public void openFlagUI(Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(0);
            msg.writer().writeByte(flagTempId.length);
            for (int j : flagTempId) {
                msg.writer().writeShort(j);
                msg.writer().writeByte(1);
                switch (j) {
                    case 363:
                        msg.writer().writeByte(73);
                        msg.writer().writeShort(0);
                        break;
                    case 371:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(10);
                        break;
                    default:
                        msg.writer().writeByte(88);
                        msg.writer().writeShort(5);
                        break;
                }
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void sendFoot(Player player, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 1300:
                    me.writer().writeShort(74);
                    break;
                case 1301:
                    me.writer().writeShort(75);
                    break;
                case 1302:
                    me.writer().writeShort(76);
                    break;
                case 1303:
                    me.writer().writeShort(77);
                    break;
                case 1304:
                    me.writer().writeShort(78);
                    break;
                case 1305:
                    me.writer().writeShort(79);
                    break;
                case 1306:
                    me.writer().writeShort(80);
                    break;
                case 1307:
                    me.writer().writeShort(81);
                    break;
                case 1308:
                    me.writer().writeShort(82);
                    break;
                default:
                    break;
            }
            me.writer().writeByte(0);
            me.writer().writeByte(-1);
            me.writer().writeShort(1);
            me.writer().writeByte(-1);
            this.sendMessAllPlayerInMap(player, me);
            me.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFootRv(Player player, Player p2, int id) {
        Message me;
        try {
            me = new Message(-128);
            me.writer().writeByte(0);
            me.writer().writeInt((int) player.id);
            switch (id) {
                case 1300 -> me.writer().writeShort(74);
                case 1301 -> me.writer().writeShort(75);
                case 1302 -> me.writer().writeShort(76);
                case 1303 -> me.writer().writeShort(77);
                case 1304 -> me.writer().writeShort(78);
                case 1305 -> me.writer().writeShort(79);
                case 1306 -> me.writer().writeShort(80);
                case 1307 -> me.writer().writeShort(81);
                case 1308 -> me.writer().writeShort(82);
                default -> {
                }
            }

            me.writer().writeByte(0);
            me.writer().writeByte(-1);
            me.writer().writeShort(1);
            me.writer().writeByte(-1);
            p2.sendMessage(me);
            me.cleanup();
        } catch (Exception e) {
            System.out.println("77777");
        }
    }

    public void changeFlag(Player pl, int index) {
        Message msg;
        try {
            pl.cFlag = (byte) index;
            msg = new Message(-103);
            msg.writer().writeByte(1);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(index);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(index);
            msg.writer().writeShort(flagIconId[index]);
            Service.gI().sendMessAllPlayerInMap(pl, msg);
            msg.cleanup();

            if (pl.pet != null) {
                pl.pet.cFlag = (byte) index;
                msg = new Message(-103);
                msg.writer().writeByte(1);
                msg.writer().writeInt((int) pl.pet.id);
                msg.writer().writeByte(index);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();

                msg = new Message(-103);
                msg.writer().writeByte(2);
                msg.writer().writeByte(index);
                msg.writer().writeShort(flagIconId[index]);
                Service.gI().sendMessAllPlayerInMap(pl.pet, msg);
                msg.cleanup();
            }
            if (pl.thuTrieuHoi != null) {
                pl.thuTrieuHoi.cFlag = (byte) index;
                msg = new Message(-103);
                msg.writer().writeByte(1);
                msg.writer().writeInt((int) pl.thuTrieuHoi.id);
                msg.writer().writeByte(index);
                Service.gI().sendMessAllPlayerInMap(pl.thuTrieuHoi, msg);
                msg.cleanup();
                msg = new Message(-103);
                msg.writer().writeByte(2);
                msg.writer().writeByte(index);
                msg.writer().writeShort(flagIconId[index]);
                Service.gI().sendMessAllPlayerInMap(pl.thuTrieuHoi, msg);
                msg.cleanup();
            }
            if (pl.iDMark != null) {
                pl.iDMark.setLastTimeChangeFlag(System.currentTimeMillis());
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendFlagPlayerToMe(Player me, Player pl) {
        Message msg;
        try {
            msg = new Message(-103);
            msg.writer().writeByte(2);
            msg.writer().writeByte(pl.cFlag);
            msg.writer().writeShort(flagIconId[pl.cFlag]);
            me.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void chooseFlag(Player pl, int index) {
        if (MapService.gI().isMapBlackBallWar(pl.zone.map.mapId) || MapService.gI().isMapMaBu(pl.zone.map.mapId)) {
            sendThongBao(pl, "Không thể đổi cờ lúc này!");
            return;
        }
        if (pl.isBeQuynh) {
            sendThongBao(pl, "|7|Không thể đổi cờ khi đang Hộ tống!");
            return;
        }
        if (Util.canDoWithTime(pl.iDMark.getLastTimeChangeFlag(), 60000)) {
            changeFlag(pl, index);
        } else {
            sendThongBao(pl, "Không thể đổi cờ lúc này! Vui lòng đợi " + TimeUtil.getTimeLeft(pl.iDMark.getLastTimeChangeFlag(), 60) + " nữa!");
        }
    }

    public void attackPlayer(Player pl, int idPlAnPem) {
        SkillService.gI().useSkill(pl, pl.zone.getPlayerInMap(idPlAnPem), null, null);
    }

    public void releaseCooldownSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                skill.coolDown = 0;
                msg.writer().writeShort(skill.skillId);
                int leftTime = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (leftTime < 0) {
                    leftTime = 0;
                }
                msg.writer().writeInt(leftTime);
            }
            pl.sendMessage(msg);
            pl.nPoint.setMp(pl.nPoint.mpMax);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendTimeSkill(Player pl) {
        Message msg;
        try {
            msg = new Message(-94);
            for (Skill skill : pl.playerSkill.skills) {
                msg.writer().writeShort(skill.skillId);
                int timeLeft = (int) (skill.lastTimeUseThisSkill + skill.coolDown - System.currentTimeMillis());
                if (timeLeft < 0) {
                    timeLeft = 0;
                }
                msg.writer().writeInt(timeLeft);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void dropItemMap(Zone zone, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            msg.writer().flush();
            for (byte i = 0; i < zone.players.size(); i++) {
                if (zone.players.get(i) != null) {
                    zone.players.get(i).session.sendMessage(msg);
                }
            }
//            sendMessAllPlayerInMap(zone, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void dropItemMapForMe(Player player, ItemMap item) {
        Message msg;
        try {
            msg = new Message(68);
            msg.writer().writeShort(item.itemMapId);
            msg.writer().writeShort(item.itemTemplate.id);
            msg.writer().writeShort(item.x);
            msg.writer().writeShort(item.y);
            msg.writer().writeInt(3);//
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void showInfoPet(Player pl) {
        if (pl != null && pl.pet != null) {
            Message msg;
            try {
                msg = new Message(-107);
                msg.writer().writeByte(2);
                msg.writer().writeShort(pl.pet.getAvatar());
                msg.writer().writeByte(pl.pet.inventory.itemsBody.size());

                for (Item item : pl.pet.inventory.itemsBody) {
                    if (!item.isNotNullItem()) {
                        msg.writer().writeShort(-1);
                    } else {
                        msg.writer().writeShort(item.template.id);
                        msg.writer().writeInt(item.quantity);
                        msg.writer().writeUTF(item.getInfo());
                        msg.writer().writeUTF(item.getContent());

                        int countOption = item.itemOptions.size();
                        msg.writer().writeByte(countOption);
                        for (ItemOption iop : item.itemOptions) {
                            msg.writer().writeByte(iop.optionTemplate.id);
                            msg.writer().writeShort(iop.param);
                        }
                    }
                }

                msg.writer().writeInt(Util.DoubleGioihan(pl.pet.nPoint.hp)); //hp
                msg.writer().writeInt(Util.DoubleGioihan(pl.pet.nPoint.hpMax)); //hpfull
                msg.writer().writeInt(Util.DoubleGioihan(pl.pet.nPoint.mp)); //mp
                msg.writer().writeInt(Util.DoubleGioihan(pl.pet.nPoint.mpMax)); //mpfull
                msg.writer().writeInt(Util.DoubleGioihan(pl.pet.nPoint.dame)); //damefull
                msg.writer().writeUTF(pl.pet.name); //name
                msg.writer().writeUTF(getCurrStrLevel(pl.pet)); //curr level
                msg.writer().writeLong(pl.pet.nPoint.power); //power
                msg.writer().writeLong(pl.pet.nPoint.tiemNang); //tiềm năng
                msg.writer().writeByte(pl.pet.getStatus()); //status
                msg.writer().writeShort(pl.pet.nPoint.stamina); //stamina
                msg.writer().writeShort(pl.pet.nPoint.maxStamina); //stamina full
                msg.writer().writeByte(pl.pet.nPoint.crit); //crit
                msg.writer().writeShort(Util.DoubleGioihan(pl.pet.nPoint.def)); //def
//                int sizeSkill = pl.pet.playerSkill.skills.size();
                msg.writer().writeByte(4); //counnt pet skill
                for (int i = 0; i < pl.pet.playerSkill.skills.size(); i++) {
                    if (pl.pet.playerSkill.skills.get(i).skillId != -1) {
                        msg.writer().writeShort(pl.pet.playerSkill.skills.get(i).skillId);
                    } else {
                        switch (i) {
                            case 1:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 150tr để mở");
                                break;
                            case 2:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 1tỷ5 để mở");
                                break;
                            default:
                                msg.writer().writeShort(-1);
                                msg.writer().writeUTF("Cần đạt sức mạnh 20tỷ\nđể mở");
                                break;
                        }
                    }
                }
                pl.sendMessage(msg);
                msg.cleanup();

            } catch (Exception e) {
                Logger.logException(Service.class, e);
            }
        }
    }

    public void sendSpeedPlayer(Player pl, int speed) {
        Message msg;
        try {
            msg = Service.gI().messageSubCommand((byte) 8);
            msg.writer().writeInt((int) pl.id);
            msg.writer().writeByte(speed != -1 ? speed : pl.nPoint.speed);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setPos(Player player, int x, int y) {
        player.location.x = x;
        player.location.y = y;
        Message msg;
        try {
            msg = new Message(123);
            msg.writer().writeInt((int) player.id);
            msg.writer().writeShort(x);
            msg.writer().writeShort(y);
            msg.writer().writeByte(1);
            sendMessAllPlayerInMap(player, msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void getPlayerMenu(Player player, int playerId) {
        Message msg;
        try {
            msg = new Message(-79);
            Player pl = player.zone.getPlayerInMap(playerId);
            if (pl != null) {
                msg.writer().writeInt(playerId);
                msg.writer().writeLong(pl.nPoint.power);
                msg.writer().writeUTF(Service.gI().getCurrStrLevel(pl));
                player.sendMessage(msg);
            }
            msg.cleanup();
            if (player.isAdmin()) {
                SubMenuService.gI().showMenuForAdmin(player);
            }
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void hideWaitDialog(Player pl) {
        Message msg;
        try {
            msg = new Message(-99);
            msg.writer().writeByte(-1);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void chatPrivate(Player plChat, Player plReceive, String text) {
        Message msg;
        try {
            msg = new Message(92);
            msg.writer().writeUTF(plChat.name);
            msg.writer().writeUTF("|5|" + text);
            msg.writer().writeInt((int) plChat.id);
            msg.writer().writeShort(plChat.getHead());
            msg.writer().writeShort(-1);
            msg.writer().writeShort(plChat.getBody());
            msg.writer().writeShort(plChat.getFlagBag()); //bag
            msg.writer().writeShort(plChat.getLeg());
            msg.writer().writeByte(1);
            plChat.sendMessage(msg);
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void changePassword(Player player, String oldPass, String newPass, String rePass) {
        if (player.getSession().pp.equals(oldPass)) {
            if (newPass.length() >= 6) {
                if (newPass.equals(rePass)) {
                    if (newPass.equals("newpassword")) {
                        Input.newpassword(0);
                    }
                    player.getSession().pp = newPass;
                    try {
                        GirlkunDB.executeUpdate("update account set password = ? where id = ? and username = ?",
                                rePass, player.getSession().userId, player.getSession().uu);
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thành công!");
                    } catch (Exception ex) {
                        Service.gI().sendThongBao(player, "Đổi mật khẩu thất bại!");
                        Logger.logException(Service.class, ex);
                    }
                } else {
                    Service.gI().sendThongBao(player, "Mật khẩu nhập lại không đúng!");
                }
            } else {
                Service.gI().sendThongBao(player, "Mật khẩu ít nhất 6 ký tự!");
            }
        } else {
            Service.gI().sendThongBao(player, "Mật khẩu cũ không đúng!");
        }
    }

    public void switchToCreateChar(MySession session) {
        Message msg;
        try {
            msg = new Message(2);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void sendCaption(MySession session, byte gender) {
        Message msg;
        try {
            msg = new Message(-41);
            msg.writer().writeByte(Manager.CAPTIONS.size());
            for (String caption : Manager.CAPTIONS) {
                msg.writer().writeUTF(caption.replaceAll("%1", gender == ConstPlayer.TRAI_DAT ? "Trái đất"
                        : (gender == ConstPlayer.NAMEC ? "Namếc" : "Xayda")));
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void sendHavePet(Player player) {
        Message msg;
        try {
            msg = new Message(-107);
            msg.writer().writeByte(player.pet == null ? 0 : 1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);

        }
    }

    public void sendWaitToLogin(MySession session, int secondsWait) {
        Message msg;
        try {
            msg = new Message(122);
            msg.writer().writeShort(secondsWait);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessage(MySession session, int cmd, String path) {
        Message msg;
        try {
            msg = new Message(cmd);
            msg.writer().write(Objects.requireNonNull(FileIO.readFile(path)));
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void sendMessage(MySession session, int cmd) {
        Message msg;
        try {
            msg = new Message(cmd);
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void createItemMap(Player player, int tempId) {
        ItemMap itemMap = new ItemMap(player.zone, tempId, 1, player.location.x, player.location.y, player.id);
        dropItemMap(player.zone, itemMap);
    }

    public void sendNangDong(Player player) {
        Message msg;
        try {
            msg = new Message(-97);
            msg.writer().writeInt(100);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void setClientType(MySession session, Message msg) {
        try {
            session.typeClient = (msg.reader().readByte());//client_type
            session.zoomLevel = msg.reader().readByte();//zoom_level
            msg.reader().readBoolean();//is_gprs
            msg.reader().readInt();//width
            msg.reader().readInt();//height
            msg.reader().readBoolean();//is_qwerty
            msg.reader().readBoolean();//is_touch
            String platform = msg.reader().readUTF();
            String[] arrPlatform = platform.split("\\|");
            session.version = Integer.parseInt(arrPlatform[1].replaceAll("\\.", ""));
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        } finally {
            msg.cleanup();
        }
        DataGame.sendLinkIP(session);
    }

    public void dropVeTinh(Player pl, Item item, Zone map, int x, int y) {
        ItemMap itemMap = new ItemMap(map, item.template, item.quantity, x, y, pl.id);
        itemMap.options = item.itemOptions;
        map.addItem(itemMap);
        Message msg = null;
        try {
            msg = new Message(68);
            msg.writer().writeShort(itemMap.itemMapId);
            msg.writer().writeShort(itemMap.itemTemplate.id);
            msg.writer().writeShort(itemMap.x);
            msg.writer().writeShort(itemMap.y);
            msg.writer().writeInt(-2);
            msg.writer().writeShort(200);
            sendMessAllPlayerInMap(map, msg);
        } catch (IOException e) {
            Logger.logException(Service.class, e);
        } finally {
            if (msg != null) {
                msg.cleanup();
            }
        }
    }

    public void stealMoney(Player pl, int stealMoney) {
        Message msg;
        try {
            msg = new Message(95);
            msg.writer().writeInt(stealMoney);
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e);
        }
    }

    public void switchToRegisterScr(ISession session) {
        Message message;
        try {
            message = new Message(42);
            message.writeByte(0);
            session.sendMessage(message);
            message.cleanup();
        } catch (Exception e) {
            Logger.logException(Service.class, e, "Error Register");
        }
    }
}

