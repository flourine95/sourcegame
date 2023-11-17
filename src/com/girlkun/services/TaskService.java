package com.girlkun.services;

import com.girlkun.consts.ConstMob;
import com.girlkun.consts.ConstNpc;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.consts.ConstTask;
import com.girlkun.models.boss.Boss;
import com.girlkun.models.boss.BossID;
import com.girlkun.models.item.Item;
import com.girlkun.models.map.ItemMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.player.Player;
import com.girlkun.models.task.SideTaskTemplate;
import com.girlkun.models.task.SubTaskMain;
import com.girlkun.models.task.TaskMain;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

import static com.girlkun.models.player.Inventory.LIMIT_GOLD;


public class TaskService {
    private static final byte MEMBER_DO_TASK_TOGETHER = 1;

    private static com.girlkun.services.TaskService i;

    public static com.girlkun.services.TaskService gI() {
        if (i == null) {
            i = new com.girlkun.services.TaskService();
        }
        return i;
    }

    public TaskMain getTaskMainById(Player player, int id) {
        for (TaskMain tm : Manager.TASKS) {
            if (tm.id == id) {
                TaskMain newTaskMain = new TaskMain(tm);
                newTaskMain.detail = transformName(player, newTaskMain.detail);
                for (SubTaskMain stm : newTaskMain.subTasks) {
                    stm.mapId = (short) transformMapId(player, stm.mapId);
                    stm.npcId = (byte) transformNpcId(player, stm.npcId);
                    stm.notify = transformName(player, stm.notify);
                    stm.name = transformName(player, stm.name);
                }
                return newTaskMain;
            }
        }
        return player.playerTask.taskMain;
    }

    //gửi thông tin nhiệm vụ chính
    public void sendTaskMain(Player player) {
        Message msg;
        try {
            msg = new Message(40);
            msg.writer().writeShort(player.playerTask.taskMain.id);
//            msg.writer().writeShort(12);
            msg.writer().writeByte(player.playerTask.taskMain.index);
//            msg.writer().writeUTF(player.playerTask.taskMain.name);
            msg.writer().writeUTF(player.playerTask.taskMain.name + "[" + player.playerTask.taskMain.id + "]");
            msg.writer().writeUTF(player.playerTask.taskMain.detail);
            msg.writer().writeByte(player.playerTask.taskMain.subTasks.size());
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeUTF(stm.name);
                msg.writer().writeByte(stm.npcId);
                msg.writer().writeShort(stm.mapId);
                msg.writer().writeUTF(stm.notify);
            }
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            for (SubTaskMain stm : player.playerTask.taskMain.subTasks) {
                msg.writer().writeShort(stm.maxCount);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        }
    }

    //chuyển sang task mới
    public void sendNextTaskMain(Player player) {
        rewardDoneTask(player);
        player.playerTask.taskMain = TaskService.gI().getTaskMainById(player, player.playerTask.taskMain.id + 1);
        sendTaskMain(player);
        Service.gI().sendThongBao(player, "Nhiệm vụ tiếp theo của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    //số lượng đã hoàn thành
    public void sendUpdateCountSubTask(Player player) {
        Message msg;
        try {
            msg = new Message(43);
            msg.writer().writeShort(player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        }
    }

    //chuyển sub task tiếp theo
    public void sendNextSubTask(Player player) {
        Message msg;
        try {
            msg = new Message(41);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(TaskService.class, e);
        }
    }

    //gửi thông tin nhiệm vụ hiện tại
    public void sendInfoCurrentTask(Player player) {
        Service.gI().sendThongBao(player, "Nhiệm vụ hiện tại của bạn là "
                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).name);
    }

    public boolean checkDoneTaskTalkNpc(Player player, Npc npc) {
        return switch (npc.tempId) {
            case ConstNpc.QUY_LAO_KAME -> player.gender == ConstPlayer.TRAI_DAT && (doneTask(player, ConstTask.TASK_9_1)
                    || doneTask(player, ConstTask.TASK_10_2)
                    || doneTask(player, ConstTask.TASK_11_3)
                    || doneTask(player, ConstTask.TASK_12_2)
                    || doneTask(player, ConstTask.TASK_13_1)
                    || doneTask(player, ConstTask.TASK_14_3)
                    || doneTask(player, ConstTask.TASK_15_3)
                    || doneTask(player, ConstTask.TASK_16_3));
            case ConstNpc.TRUONG_LAO_GURU -> player.gender == ConstPlayer.NAMEC && (doneTask(player, ConstTask.TASK_9_1)
                    || doneTask(player, ConstTask.TASK_10_2)
                    || doneTask(player, ConstTask.TASK_11_3)
                    || doneTask(player, ConstTask.TASK_12_2)
                    || doneTask(player, ConstTask.TASK_13_1)
                    || doneTask(player, ConstTask.TASK_14_3)
                    || doneTask(player, ConstTask.TASK_15_3)
                    || doneTask(player, ConstTask.TASK_16_3));
            case ConstNpc.VUA_VEGETA -> player.gender == ConstPlayer.XAYDA && (doneTask(player, ConstTask.TASK_9_1)
                    || doneTask(player, ConstTask.TASK_10_2)
                    || doneTask(player, ConstTask.TASK_11_3)
                    || doneTask(player, ConstTask.TASK_12_2)
                    || doneTask(player, ConstTask.TASK_13_1)
                    || doneTask(player, ConstTask.TASK_14_3)
                    || doneTask(player, ConstTask.TASK_15_3)
                    || doneTask(player, ConstTask.TASK_16_3));
            case ConstNpc.ONG_GOHAN, ConstNpc.ONG_MOORI, ConstNpc.ONG_PARAGUS -> (doneTask(player, ConstTask.TASK_0_2)
                    || doneTask(player, ConstTask.TASK_0_5)
                    || doneTask(player, ConstTask.TASK_1_1)
                    || doneTask(player, ConstTask.TASK_2_1)
                    || doneTask(player, ConstTask.TASK_3_2)
                    || doneTask(player, ConstTask.TASK_4_1)
                    || doneTask(player, ConstTask.TASK_5_3)
                    || doneTask(player, ConstTask.TASK_8_2)
                    || doneTask(player, ConstTask.TASK_12_1));
            case ConstNpc.DR_DRIEF, ConstNpc.CARGO, ConstNpc.CUI -> (doneTask(player, ConstTask.TASK_6_1)
                    || doneTask(player, ConstTask.TASK_7_2)
                    || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_17_1)
                    || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_18_5)
                    || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_19_3)
                    || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_20_6)
                    || player.zone.map.mapId == 19 && doneTask(player, ConstTask.TASK_21_4));
            case ConstNpc.BUNMA, ConstNpc.DENDE, ConstNpc.APPULE -> doneTask(player, ConstTask.TASK_8_1);
            case ConstNpc.BUNMA_TL -> (doneTask(player, ConstTask.TASK_22_0)
                    || doneTask(player, ConstTask.TASK_22_4)
                    || doneTask(player, ConstTask.TASK_23_4)
                    || doneTask(player, ConstTask.TASK_24_4)
                    || doneTask(player, ConstTask.TASK_25_5)
                    || doneTask(player, ConstTask.TASK_26_4)
                    || doneTask(player, ConstTask.TASK_27_4)
                    || doneTask(player, ConstTask.TASK_28_5)
                    || doneTask(player, ConstTask.TASK_31_4)
                    || doneTask(player, ConstTask.TASK_29_4)
                    || doneTask(player, ConstTask.TASK_30_5));
            default -> false;
        };
    }

    //kiểm tra hoàn thành nhiệm vụ gia nhập bang hội
    public void checkDoneTaskJoinClan(Player player) {
        if (player.isPl()) {
            doneTask(player, ConstTask.TASK_13_0);
        }
    }

    //kiểm tra hoàn thành nhiệm vụ lấy item từ rương
    public void checkDoneTaskGetItemBox(Player player) {
        if (player.isPl()) {
            doneTask(player, ConstTask.TASK_0_3);
        }
    }

    //kiểm tra hoàn thành nhiệm vụ sức mạnh
    public void checkDoneTaskPower(Player player, long power) {
        if (player.isPl()) {
            if (power >= 20000) {
                doneTask(player, ConstTask.TASK_5_0);
            }
            if (power >= 35000) {
                doneTask(player, ConstTask.TASK_5_1);
            }
            if (power >= 60000) {
                doneTask(player, ConstTask.TASK_5_2);
            }
            if (power >= 200000) {
                doneTask(player, ConstTask.TASK_10_0);
            }
            if (power >= 600000000) {
                doneTask(player, ConstTask.TASK_20_0);
            }
            if (power >= 2000000000L) {
                doneTask(player, ConstTask.TASK_21_0);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi player sử dụng tiềm năng
    public void checkDoneTaskUseTiemNang(Player player) {
        if (player.isPl()) {
            doneTask(player, ConstTask.TASK_3_0);
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi vào map nào đó
    public void checkDoneTaskGoToMap(Player player, Zone zoneJoin) {
        if (player.isPl()) {
            switch (zoneJoin.map.mapId) {
                case 39, 40, 41 -> {
                    if (player.location.x >= 635) {
                        doneTask(player, ConstTask.TASK_0_0);
                    }
                }
                case 21, 22, 23 -> {
                    doneTask(player, ConstTask.TASK_0_1);
                    doneTask(player, ConstTask.TASK_12_0);
                }
                case 24, 25, 26 -> doneTask(player, ConstTask.TASK_6_0);
                case 3, 11, 17 -> doneTask(player, ConstTask.TASK_7_0);
                case 0, 7, 14 -> doneTask(player, ConstTask.TASK_8_0);
                case 5, 13, 20 -> doneTask(player, ConstTask.TASK_9_0);
                case 19 -> doneTask(player, ConstTask.TASK_17_0);
                case 93 -> doneTask(player, ConstTask.TASK_22_1);
                case 97 -> doneTask(player, ConstTask.TASK_23_0);
                case 100 -> doneTask(player, ConstTask.TASK_24_0);
//                case 52 -> doneTask(player, ConstTask.TASK_29_0);
                case 103 -> doneTask(player, ConstTask.TASK_28_0);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi nhặt item
    public void checkDoneTaskPickItem(Player player, ItemMap item) {
        if (player.isPl() && item != null && item.itemTemplate != null) {
            switch (item.itemTemplate.id) {
                case 73 -> doneTask(player, ConstTask.TASK_2_0);
                case 78 -> {
                    doneTask(player, ConstTask.TASK_3_1);
                    Service.gI().sendFlagBag(player);
                }
                case 380 -> doneTask(player, ConstTask.TASK_26_3);
                case 15 -> doneTask(player, ConstTask.TASK_27_3);
//                case 16 -> {
//                    doneTask(player, ConstTask.TASK_28_3);
//                    Service.getInstance().sendFlagBag(player);
//                }
                case 992 -> doneTask(player, ConstTask.TASK_31_0);
                case 865 -> doneTask(player, ConstTask.TASK_31_1);
                case 874 -> doneTask(player, ConstTask.TASK_31_2);
                case 725 -> doneTask(player, ConstTask.TASK_31_3);
                case 861 -> {
                    if (player.isPl() && item.itemTemplate.id == 861) {
                        player.achievement.plusCount(15);
                    }
                }
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ kết bạn
    public void checkDoneTaskMakeFriend(Player player, Player friend) {
        if (player.isPl()) {
            switch (friend.gender) {
                case ConstPlayer.TRAI_DAT:
                    doneTask(player, ConstTask.TASK_11_0);
                    doneTask(player, ConstTask.TASK_27_0);
                    break;
                case ConstPlayer.NAMEC:
                    doneTask(player, ConstTask.TASK_11_1);
                    doneTask(player, ConstTask.TASK_27_1);
                    break;
                case ConstPlayer.XAYDA:
                    doneTask(player, ConstTask.TASK_11_2);
                    doneTask(player, ConstTask.TASK_27_2);
                    break;
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi xác nhận menu npc nào đó
    public void checkDoneTaskConfirmMenuNpc(Player player, Npc npc, byte select) {
        if (player.isPl()) {
            if (npc.tempId == ConstNpc.DAU_THAN) {
                if (player.iDMark.getIndexMenu() == ConstNpc.MAGIC_TREE_NON_UPGRADE_LEFT_PEA ||
                        player.iDMark.getIndexMenu() == ConstNpc.MAGIC_TREE_NON_UPGRADE_FULL_PEA) {
                    if (select == 0) {
                        doneTask(player, ConstTask.TASK_0_4);
                    }
                }
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi tiêu diệt được boss
    public void checkDoneTaskKillBoss(Player player, Boss boss) {
        if (player != null && player.isPl()) {
            switch ((int) boss.id) {
                case BossID.KUKU -> doneTask(player, ConstTask.TASK_19_0);
                case BossID.MAP_DAU_DINH -> doneTask(player, ConstTask.TASK_19_1);
                case BossID.RAMBO -> doneTask(player, ConstTask.TASK_19_2);
                case BossID.SO_4 -> doneTask(player, ConstTask.TASK_20_1);
                case BossID.SO_3 -> doneTask(player, ConstTask.TASK_20_2);
                case BossID.SO_2 -> doneTask(player, ConstTask.TASK_20_3);
                case BossID.SO_1 -> doneTask(player, ConstTask.TASK_20_4);
                case BossID.TIEU_DOI_TRUONG -> doneTask(player, ConstTask.TASK_20_5);
                case BossID.FIDE -> {
                    switch (boss.currentLevel) {
                        case 0 -> doneTask(player, ConstTask.TASK_21_1);
                        case 1 -> doneTask(player, ConstTask.TASK_21_2);
                        case 2 -> doneTask(player, ConstTask.TASK_21_3);
                    }
                }
                case BossID.ANDROID_19 -> doneTask(player, ConstTask.TASK_22_2);
                case BossID.DR_KORE -> doneTask(player, ConstTask.TASK_22_3);
                case BossID.POC -> doneTask(player, ConstTask.TASK_23_1);
                case BossID.PIC -> doneTask(player, ConstTask.TASK_23_2);
                case BossID.KING_KONG -> doneTask(player, ConstTask.TASK_23_3);
                case BossID.XEN_BO_HUNG -> {
                    switch (boss.currentLevel) {
                        case 0 -> doneTask(player, ConstTask.TASK_24_1);
                        case 1 -> doneTask(player, ConstTask.TASK_24_2);
                        case 2 -> doneTask(player, ConstTask.TASK_24_3);
                    }
                }
                case BossID.TDST -> {
                    switch (boss.currentLevel) {
                        case 0 -> doneTask(player, ConstTask.TASK_20_1);
                        case 1 -> doneTask(player, ConstTask.TASK_20_2);
                        case 2 -> doneTask(player, ConstTask.TASK_20_3);
                        case 3 -> doneTask(player, ConstTask.TASK_20_4);
                        case 4 -> doneTask(player, ConstTask.TASK_20_5);
                    }
                }
                case BossID.XEN_CON_1 -> doneTask(player, ConstTask.TASK_28_1);
                case BossID.SIEU_BO_HUNG -> {
                    switch (boss.currentLevel) {
                        case 0 -> doneTask(player, ConstTask.TASK_28_2);
                        case 1 -> doneTask(player, ConstTask.TASK_28_3);
                        case 2 -> doneTask(player, ConstTask.TASK_28_4);
                    }
                }
                case BossID.DORAEMON -> doneTask(player, ConstTask.TASK_30_2);
                case BossID.NOBITA -> doneTask(player, ConstTask.TASK_30_3);
                case BossID.XUKA -> doneTask(player, ConstTask.TASK_30_0);
                case BossID.CHAIEN -> doneTask(player, ConstTask.TASK_30_1);
                case BossID.XEKO -> doneTask(player, ConstTask.TASK_30_4);
            }
        }
    }

    //kiểm tra hoàn thành nhiệm vụ khi giết được quái
    public void checkDoneTaskKillMob(Player player, Mob mob) {
        if (player != null && player.isPl() && player.zone != null) {
            switch (mob.tempId) {
                case ConstMob.MOC_NHAN -> doneTask(player, ConstTask.TASK_1_0);
                case ConstMob.KHUNG_LONG_ME, ConstMob.LON_LOI_ME, ConstMob.QUY_DAT_ME ->
                        doneTask(player, ConstTask.TASK_4_0);
                case ConstMob.THAN_LAN_BAY, ConstMob.PHI_LONG, ConstMob.QUY_BAY -> doneTask(player, ConstTask.TASK_7_1);
                case ConstMob.OC_MUON_HON, ConstMob.OC_SEN, ConstMob.HEO_XAYDA_ME ->
                        doneTask(player, ConstTask.TASK_10_1);
                case ConstMob.HEO_RUNG, ConstMob.HEO_DA_XANH, ConstMob.HEO_XAYDA -> {
                    if (player.clan != null) {
                        List<Player> list = new ArrayList<>();
                        List<Player> playersMap = player.zone.getPlayers();
                        for (Player pl : playersMap) {
                            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                                list.add(pl);
                            }
                        }
                        if (list.size() >= MEMBER_DO_TASK_TOGETHER) {
                            for (Player pl : list) {
                                switch (mob.tempId) {
                                    case ConstMob.HEO_RUNG -> doneTask(pl, ConstTask.TASK_14_0);
                                    case ConstMob.HEO_DA_XANH -> doneTask(pl, ConstTask.TASK_14_1);
                                    case ConstMob.HEO_XAYDA -> doneTask(pl, ConstTask.TASK_14_2);
                                }
                            }
                        }
                    }
                }
                case ConstMob.BULON, ConstMob.UKULELE, ConstMob.QUY_MAP -> {
                    if (player.clan != null) {
                        List<Player> list = new ArrayList<>();
                        List<Player> playersMap = player.zone.getPlayers();
                        for (Player pl : playersMap) {
                            if (pl != null && pl.clan != null && pl.clan.equals(player.clan)) {
                                list.add(pl);
                            }
                        }
                        if (list.size() >= MEMBER_DO_TASK_TOGETHER) {
                            for (Player pl : list) {
                                switch (mob.tempId) {
                                    case ConstMob.BULON -> doneTask(pl, ConstTask.TASK_15_0);
                                    case ConstMob.UKULELE -> doneTask(pl, ConstTask.TASK_15_1);
                                    case ConstMob.QUY_MAP -> doneTask(pl, ConstTask.TASK_15_2);
                                }
                            }
                        }
                    }
                }
                case ConstMob.TAMBOURINE -> doneTask(player, ConstTask.TASK_16_0);
                case ConstMob.DRUM -> doneTask(player, ConstTask.TASK_16_1);
                case ConstMob.AKKUMAN -> doneTask(player, ConstTask.TASK_16_2);
                case ConstMob.NAPPA -> doneTask(player, ConstTask.TASK_18_0);
                case ConstMob.SOLDIER -> doneTask(player, ConstTask.TASK_18_1);
                case ConstMob.APPULE -> doneTask(player, ConstTask.TASK_18_2);
                case ConstMob.RASPBERRY -> doneTask(player, ConstTask.TASK_18_3);
                case ConstMob.THAN_LAN_XANH -> doneTask(player, ConstTask.TASK_18_4);
                case ConstMob.XEN_CON_CAP_1 -> doneTask(player, ConstTask.TASK_25_0);
                case ConstMob.XEN_CON_CAP_2 -> doneTask(player, ConstTask.TASK_25_1);
                case ConstMob.XEN_CON_CAP_3 -> doneTask(player, ConstTask.TASK_25_2);
                case ConstMob.XEN_CON_CAP_4 -> doneTask(player, ConstTask.TASK_25_3);
                case ConstMob.XEN_CON_CAP_5 -> doneTask(player, ConstTask.TASK_25_4);
                case ConstMob.XEN_CON_CAP_6 -> doneTask(player, ConstTask.TASK_26_0);
                case ConstMob.XEN_CON_CAP_7 -> doneTask(player, ConstTask.TASK_26_1);
                case ConstMob.XEN_CON_CAP_8 -> {
                    doneTask(player, ConstTask.TASK_26_2);
                    if (player.nhiemvuChienthan.tasknow == 1) {
                        player.nhiemvuChienthan.dalamduoc++;
                    }
                }
                case ConstMob.TAI_TIM -> doneTask(player, ConstTask.TASK_29_0);
                case ConstMob.ABO -> doneTask(player, ConstTask.TASK_29_1);
                case ConstMob.KADO -> doneTask(player, ConstTask.TASK_29_2);
                case ConstMob.DA_XANH -> doneTask(player, ConstTask.TASK_29_3);
            }
        }
    }

    //xong nhiệm vụ nào đó
    private boolean doneTask(Player player, int idTaskCustom) {
        if (TaskService.gI().isCurrentTask(player, idTaskCustom)) {
            this.addDoneSubTask(player, 1);
            switch (idTaskCustom) {
                case ConstTask.TASK_0_0 ->
                        NpcService.gI().createTutorial(player, -1, transformName(player, "Làm tốt lắm..\n"
                                + "Bây giờ bạn hãy vào nhà ông %2 bên phải để nhận nhiệm vụ mới nhé"));
                case ConstTask.TASK_0_1 ->
                        NpcService.gI().createTutorial(player, -1, transformName(player, "Ông %2 đang đứng đợi kìa\n"
                                + "Hãy nhấn 2 lần vào để nói chuyện"));
                case ConstTask.TASK_0_2 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Con vừa đi đâu về đó?
                                Con hãy đến rương đồ để lấy rađa..
                                ..sau đó thu hoạch hết đậu trên cây đậu thần đằng kia!""");
                case ConstTask.TASK_0_3, ConstTask.TASK_0_4, ConstTask.TASK_3_1, ConstTask.TASK_5_1, ConstTask.TASK_5_2, ConstTask.TASK_6_0, ConstTask.TASK_7_1, ConstTask.TASK_9_0, ConstTask.TASK_10_0, ConstTask.TASK_10_1, ConstTask.TASK_11_0, ConstTask.TASK_11_1, ConstTask.TASK_11_2, ConstTask.TASK_14_1, ConstTask.TASK_14_2, ConstTask.TASK_15_0, ConstTask.TASK_15_2, ConstTask.TASK_15_1, ConstTask.TASK_16_1, ConstTask.TASK_16_2, ConstTask.TASK_18_1, ConstTask.TASK_18_2, ConstTask.TASK_18_3, ConstTask.TASK_18_4, ConstTask.TASK_19_1, ConstTask.TASK_19_2, ConstTask.TASK_20_1, ConstTask.TASK_20_2, ConstTask.TASK_20_3, ConstTask.TASK_20_4, ConstTask.TASK_20_5, ConstTask.TASK_21_1, ConstTask.TASK_21_2, ConstTask.TASK_21_3, ConstTask.TASK_22_1, ConstTask.TASK_22_2, ConstTask.TASK_22_3, ConstTask.TASK_23_1, ConstTask.TASK_23_2, ConstTask.TASK_23_3, ConstTask.TASK_24_1, ConstTask.TASK_24_2, ConstTask.TASK_24_3, ConstTask.TASK_25_1, ConstTask.TASK_25_2, ConstTask.TASK_25_3, ConstTask.TASK_25_0, ConstTask.TASK_25_4, ConstTask.TASK_26_0, ConstTask.TASK_26_1, ConstTask.TASK_26_2, ConstTask.TASK_26_3, ConstTask.TASK_27_1, ConstTask.TASK_27_2, ConstTask.TASK_27_3, ConstTask.TASK_28_1, ConstTask.TASK_28_0, ConstTask.TASK_28_2, ConstTask.TASK_28_3, ConstTask.TASK_28_4, ConstTask.TASK_27_0, ConstTask.TASK_29_1, ConstTask.TASK_29_2, ConstTask.TASK_29_3, ConstTask.TASK_29_0, ConstTask.TASK_30_0, ConstTask.TASK_30_1, ConstTask.TASK_30_2, ConstTask.TASK_30_3, ConstTask.TASK_30_4, ConstTask.TASK_31_1, ConstTask.TASK_31_3, ConstTask.TASK_31_0, ConstTask.TASK_31_2, ConstTask.TASK_2_0, ConstTask.TASK_3_0, ConstTask.TASK_5_0, ConstTask.TASK_7_0, ConstTask.TASK_12_0, ConstTask.TASK_13_0, ConstTask.TASK_14_0, ConstTask.TASK_16_0, ConstTask.TASK_17_0, ConstTask.TASK_18_0, ConstTask.TASK_19_0, ConstTask.TASK_20_0, ConstTask.TASK_21_0, ConstTask.TASK_23_0, ConstTask.TASK_24_0, ConstTask.TASK_8_0 -> {
                }
                case ConstTask.TASK_0_5 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Tốt lắm, rađa sẽ giúp con thấy được lượng máu và thể lực ở bên góc trái
                                Bây giờ con hãy đi luyện tập
                                Con hãy ra %1, ở đó có những con mộc nhân cho con luyện tập dó
                                Hãy đốn ngã 5 con mộc nhân cho ông""");
                case ConstTask.TASK_1_0 -> {
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount + " mộc nhân");
                    }
                }
                case ConstTask.TASK_1_1 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Thể lực của con cũng khá tốt
                                Con à, dạo gần đây dân làng của chúng ta gặp phải vài chuyện
                                Bên cạnh làng ta đột nhiên xuất hiện lũ quái vật
                                Nó tàn sát dân làng và phá hoại nông sản làng ta
                                Con hãy tìm đánh chúng và đem về đây 10 cái đùi gà, 2 ông cháu mình sẽ để dành ăn dần
                                Đây là tấm bản đồ của vùng này, con hãy xem để tìm đến %3
                                Con có thể sử dụng đậu thần khi hết HP hoặc KI, bằng cách nhấn vào nút có hình trái tim bên góc phải dưới màn hình
                                Nhanh lên, ông đói lắm rồi""");

                //--------------------------------------------------------------
                case ConstTask.TASK_2_1 -> {
                    try {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, InventoryServiceNew.gI().findItemBag(player, 73), 10);
                    } catch (Exception e) {
                        Logger.logException(TaskService.class, e);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.gI().dropItemMapForMe(player, player.zone.getItemMapByTempId(74));
                    npcSay(player, ConstTask.NPC_NHA,
                            """
                                    Tốt lắm, đùi gà đây rồi, haha. Ông sẽ nướng tại đống lửa gần kia con có thể ăn bất cứ lúc nào nếu muốn
                                    À cháu này, vừa nãy ông có nghe thấy 1 tiếng động lớn, hình như có 1 vật thể rơi tại %5, con hãy đến kiểm tra xem
                                    Con cũng có thể dùng tiềm năng bản thân để nâng HP, KI hoặc sức đánh""");
                }
                case ConstTask.TASK_3_2 -> {
                    try {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, InventoryServiceNew.gI().findItemBag(player, 78), 1);
                    } catch (Exception e) {
                        Logger.logException(TaskService.class, e);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.gI().sendFlagBag(player);
                    npcSay(player, ConstTask.NPC_NHA,
                            """
                                    Có em bé trong phi thuyền rơi xuống à, ông cứ tưởng là sao băng chứ
                                    Ông sẽ đặt tên cho em nó là Goku, từ giờ nó sẽ là thành viên trong gia đình ta
                                    Nãy ông mới nhận được tin có bầy mãnh thú xuất hiện tại Trạm phi thuyền
                                    Bọn chúng vừa đổ bộ xuống trái đất để trả thù việc con sát hại con chúng
                                    Con hãy đi tiêu diệt chúng để giúp dân làng tại đó luôn nhé""");
                }
                case ConstTask.TASK_4_0 -> {
                    if (isCurrentTask(player, idTaskCustom)) {
                        Service.gI().sendThongBao(player, "Bạn đánh được "
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count + "/"
                                + player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount
                                + transformName(player, " %4 mẹ"));
                    }
                }
                case ConstTask.TASK_4_1 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Ông rất tự hào về con
                                Ông cho con cuốn bí kíp này để nâng cao võ học
                                Hãy dùng sức mạnh của mình trừ gian diệt ác bảo vệ dân lành con nhé
                                Bây giờ con hãy đi tập luyện đi, khi nào mạnh hơn thì quay về đây ông giao cho nhiệm vụ mới
                                Đi đi..""");
                case ConstTask.TASK_5_3 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Con bây giờ là người khỏe nhất vùng này rồi. Con có thể tới trạm tàu vũ trụ
                                gặp %7, cậu ấy có thể đưa con tới bất cứ nơi nào
                                Con hãy tới chào hỏi cậu đi""");
                case ConstTask.TASK_6_1 -> npcSay(player, ConstTask.NPC_TTVT,
                        "Ôi, tôi chào cậu " + player.name + ", tôi nghe danh cậu đã lâu trong vùng này mà\n"
                                + "tới bây giờ mới có thể gặp\n"
                                + "Cậu giúp tôi điều này với, đứa nhỏ nhà tôi nó lên đường đi tìm cái thứ\n"
                                + "gọi là ngọc rồng gì đó,..\n"
                                + ".. hồi nãy có người báo tôi rằng trên đường bé gặp chuyện không may, bé nó bị bọn %9 bắt\n"
                                + "Cậu hãy đi cứu nó giúp tôi với, bé nó tên là %8.. rất cám ơn cậu và hứa\n"
                                + "sẽ tặng cậu 1 thứ xứng đáng");
                case ConstTask.TASK_7_2 -> npcSay(player, ConstTask.NPC_TTVT,
                        "Ôi, tôi thật sự cám ơn cậu, " + player.name + ", con bé đã bình an trở về\n"
                                + "Để báo đáp ơn này, tôi sẽ miễn phí cho cậu đi phi thuyền của tôi, cậu có thể tới bất kỳ nơi nào cậu muốn\n"
                                + "Cứ lúc nào cần hãy đến đây nhé..\n"
                                + "..à mà bé nhà tôi nó cũng đang đứng ở trước %1 đó, nó có bán vài vật phẩm ở đó\n"
                                + "Cậu hãy tới trò chuyện với bé nó nhé..");
                case ConstTask.TASK_8_1 -> {
                    Item capsule = ItemService.gI().createNewItem((short) 193, 75);
                    InventoryServiceNew.gI().addItemBag(player, capsule);
                    npcSay(player, ConstTask.NPC_SHOP_LANG,
                            """
                                    Hiện tại em vẫn khỏe anh ạ, hơi bị trầy xước tí thôi nhưng không sao
                                    Em thực sự cảm ơn anh đã cứu em, nếu không có anh thì giờ này cũng không biết em sẽ thế nào nữa
                                    À em có cái món này, tuy nó không quá giá trị nhưng em mong anh nhận cho em vui""");
                }
                case ConstTask.TASK_8_2 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Cháu trai của ông, con làm ông tự hào lắm. Con đã biết dùng sức mạnh của mình để giúp kẻ yếu
                                Bây giờ con đã trưởng thành thực sự rồi, ông sẽ bàn giao con lại cho %10 - người bạn lâu ngày không gặp của ông
                                Con hãy tìm đường tới %11 và gửi lời chào của ông tới lão ấy nhé
                                Đi đi con...""");
                case ConstTask.TASK_9_1 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Chào cậu bé, cháu có phải cháu nội ông %2 phải không?
                                Ta cũng đã gặp cháu 1 lần hồi cháu còn bé xíu à
                                Bây giờ cháu muốn ta nhận cháu làm đệ tử à? Ta cũng không biết thực lực của cháu hiện tại như nào nữa
                                Cháu bé hãy đi đánh mấy con %12 ở quanh đây thể hiện tài năng và ta sẽ coi như đó là học phí nhé""");
                case ConstTask.TASK_10_2 -> {
                    Item skill2 = ItemService.gI().createNewItem((short) (player.gender == 0 ? 94 : player.gender == 1 ? 101 : 108), 1);
                    InventoryServiceNew.gI().addItemBag(player, skill2);
                    npcSay(player, ConstTask.NPC_QUY_LAO,
                            """
                                    Tốt lắm, bây giờ con đã chính thức trở thành đệ tử của ta
                                    Ta sẽ dạy con 1 tuyệt chiêu đặc biệt của ta
                                    Bây giờ con hãy đi kết bạn với những người xung quanh đây đi, thêm 1 người bạn bớt 1 kẻ thù mà con
                                    Mà lưu ý là tránh kết bạn với những người có bang hội nhé, họ không là kẻ thù cũng không nên là bạn""");
                }
                case ConstTask.TASK_11_3 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Giờ đây xã giao của con đã tiến bộ hơn rất nhiều rồi
                                Bây giờ con hãy về nhà xin ông %2 rằng con sẽ vào bang hội nhé
                                Ta sợ lão ấy không đồng ý lại quay sang trách móc cái thân già này..
                                Đi đi con, nói khéo lão ấy nhé.""");
                case ConstTask.TASK_12_1 -> npcSay(player, ConstTask.NPC_NHA,
                        """
                                Con muốn tham gia vào bang hội á? Haizz, cái lão già này lại dạy hư cháu ông rồi
                                Con muốn thì cũng được thôi, nhưng con phải biết lựa chọn được bang hội nào tốt đấy nhé..
                                ..xã hội này có nhiều thành phần lắm, cũng chỉ vì an nguy của con nên ông chỉ biết dặn dò vậy
                                Chúc con may mắn trên con đường con chọn, mà luôn nhớ rằng con phải là 1 công dân tốt đấy nhé..""");
                case ConstTask.TASK_12_2 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Cuối cùng lão ấy cũng đồng ý rồi à? Tốt lắm
                                Bây giờ con hãy cùng những người bạn con vừa kết bạn tạo thành 1 bang hội đi nhé
                                Khi nào đủ 5 thành viên bang hãy tới đây ta sẽ giao nhiệm vụ cho tất cả các con""");
                case ConstTask.TASK_13_1 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Tốt lắm, con đã có những người đồng đội kề vai sát cánh rồi
                                Bây giờ con và 3 người họ hãy thể hiện tinh thần đoàn kết đi nào
                                Cách phối hợp nhau làm nhiệm vụ, cách cư xử với nhau đó là hiện thân của tâm tính mỗi người
                                Các con hãy đối nhân xử thế với nhau, hãy cùng hợp sức tiêu diệt lũ quái vật nhé""");
                case ConstTask.TASK_14_3 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Giỏi lắm các con!
                                ...Hiện tại có vài chủng quái vật mới đổ bộ lên hành tinh chúng ta
                                Con hãy cùng 3 người trong bang lên đường tiêu diệt chúng nhé
                                Dân chúng đặt niềm tin vào các con hết đấy..
                                Đi đi...""");
                case ConstTask.TASK_15_3 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Giỏi lắm các con
                                Còn 1 vài con quái vật đầu sỏ nữa
                                Con hãy tiêu diệt nốt chúng đi nhé..""");
                case ConstTask.TASK_16_3 -> npcSay(player, ConstTask.NPC_QUY_LAO,
                        """
                                Con thực sự làm ta ngạc nhiên đấy, không uổng công ta truyền dạy võ công
                                Bên ngoài còn rất nhiều kẻ thù nguy hiểm, nên con phải không ngừng luyện tập nhé
                                Lại có chuyện xảy ra rồi, Cui - một người họ hàng xa của họ hàng ta - đang gặp chuyện
                                Con hãy tới thành phố Vegeta hỏi thăm tình hình cậu ta nhé! Đi đi con..""");
                case ConstTask.TASK_17_1 -> npcSay(player, ConstNpc.CUI,
                        """
                                Chào cậu, cậu là đệ tử của %10 phải không
                                Bọn người ngoài hành tinh cầm đầu bởi tên Fide đã và đang đổ bộ vào quê hương của tôi..
                                ..chúng tàn sát hết dân lành và hủy hoại quê hương chúng tôi
                                Cậu hãy giúp tôi 1 tay tiêu diệt bọn chúng nhé"""); //need retext
                case ConstTask.TASK_18_5 -> npcSay(player, ConstNpc.CUI,
                        """
                                Cảm ơn cậu đã hỗ trợ tôi tiêu diệt bọn lính tay sai Fide
                                3 tên cầm đầu chúng đang tức giận lắm, tôi thì không đủ mạnh để chống lại bọn chúng
                                ...""");
                case ConstTask.TASK_19_3 -> npcSay(player, ConstNpc.CUI,
                        """
                                Cảm ơn cậu đã tiêu diệt giúp tôi lũ đệ tử của Fide
                                Dưới trướng Fide còn có 1 đội gồm 5 thành viên được chúng gọi là Tiều Đội Sát Thủ
                                Chúng rất mạnh và rất trung thành với tên Fide
                                Bọn chúng vừa được cử tới đi trả thù cho 3 tên đệ tử cậu vừa tiêu diệt
                                Hãy chống lại bọn chúng giúp tôi nhé....""");
                case ConstTask.TASK_20_6 -> npcSay(player, ConstNpc.CUI,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_21_4 -> npcSay(player, ConstNpc.CUI,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");

                //--------------------------------------------------------------
                case ConstTask.TASK_22_0 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_22_4 -> {
                    npcSay(player, ConstNpc.BUNMA_TL,
                            """
                                    Độc Quyền Bởi Nro TaBi
                                    Độc Quyền Bởi Nro TaBi
                                    Độc Quyền Bởi Nro TaBi""");
                    npcSay(player, ConstNpc.BUNMA_TL,
                            """
                                    Độc Quyền Bởi Nro TaBi
                                    Độc Quyền Bởi Nro TaBi
                                    Độc Quyền Bởi Nro TaBi""");
                }
                case ConstTask.TASK_23_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_24_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_25_5 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_26_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_27_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_28_5 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_29_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_30_5 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
                case ConstTask.TASK_31_4 -> npcSay(player, ConstNpc.BUNMA_TL,
                        """
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi
                                Độc Quyền Bởi Nro TaBi""");
            }
            InventoryServiceNew.gI().sendItemBags(player);
            return true;
        }
        return false;
    }

    private void npcSay(Player player, int npcId, String text) {
        npcId = transformNpcId(player, npcId);
        text = transformName(player, text);
        int avatar = NpcService.gI().getAvatar(npcId);
        NpcService.gI().createTutorial(player, avatar, text);
    }

    //Thưởng nhiệm vụ
    private void rewardDoneTask(Player player) {
        if (player.playerTask.taskMain.id > 0 && player.playerTask.taskMain.id < 25) {
            Service.gI().addSMTN(player, (byte) 2, 500 * (player.playerTask.taskMain.id + 1), false);
            player.inventory.gold += (player.playerTask.taskMain.id < 5 && player.playerTask.taskMain.id >= 0) ? 100000 * (player.playerTask.taskMain.id + 1) : 500000;
            if (player.inventory.gold > LIMIT_GOLD) {
                player.inventory.gold = LIMIT_GOLD;
            }
            Service.gI().sendMoney(player);
        }
    }

    // vd: pem đc 1 mộc nhân -> +1 mộc nhân vào nv hiện tại
    private void addDoneSubTask(Player player, int numDone) {
        player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count += numDone;
        if (player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).count
                >= player.playerTask.taskMain.subTasks.get(player.playerTask.taskMain.index).maxCount) {
            player.playerTask.taskMain.index++;
            if (player.playerTask.taskMain.index >= player.playerTask.taskMain.subTasks.size()) {
                this.sendNextTaskMain(player);
            } else {
                this.sendNextSubTask(player);
            }
        } else {
            this.sendUpdateCountSubTask(player);
        }
    }

    private int transformMapId(Player player, int id) {
        if (id == ConstTask.MAP_NHA) {
            return (short) (player.gender + 21);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 1 : (player.gender == ConstPlayer.NAMEC
                    ? 8 : 15);
        } else if (id == ConstTask.MAP_VACH_NUI) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 39 : (player.gender == ConstPlayer.NAMEC
                    ? 40 : 41);
        } else if (id == ConstTask.MAP_200) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 2 : (player.gender == ConstPlayer.NAMEC
                    ? 9 : 16);
        } else if (id == ConstTask.MAP_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 24 : (player.gender == ConstPlayer.NAMEC
                    ? 25 : 26);
        } else if (id == ConstTask.MAP_QUAI_BAY_600) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 3 : (player.gender == ConstPlayer.NAMEC
                    ? 11 : 17);
        } else if (id == ConstTask.MAP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 0 : (player.gender == ConstPlayer.NAMEC
                    ? 7 : 14);
        } else if (id == ConstTask.MAP_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? 5 : (player.gender == ConstPlayer.NAMEC
                    ? 13 : 20);
        }
        return id;
    }

    private int transformNpcId(Player player, int id) {
        if (id == ConstTask.NPC_NHA) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.ONG_GOHAN : (player.gender == ConstPlayer.NAMEC
                    ? ConstNpc.ONG_MOORI : ConstNpc.ONG_PARAGUS);
        } else if (id == ConstTask.NPC_TTVT) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.DR_DRIEF : (player.gender == ConstPlayer.NAMEC
                    ? ConstNpc.CARGO : ConstNpc.CUI);
        } else if (id == ConstTask.NPC_SHOP_LANG) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.BUNMA : (player.gender == ConstPlayer.NAMEC
                    ? ConstNpc.DENDE : ConstNpc.APPULE);
        } else if (id == ConstTask.NPC_QUY_LAO) {
            return player.gender == ConstPlayer.TRAI_DAT
                    ? ConstNpc.QUY_LAO_KAME : (player.gender == ConstPlayer.NAMEC
                    ? ConstNpc.TRUONG_LAO_GURU : ConstNpc.VUA_VEGETA);
        }
        return id;
    }

    //replate %1 %2 -> chữ
    private String transformName(Player player, String text) {
        byte gender = player.gender;
        text = text.replaceAll(ConstTask.TEN_NPC_QUY_LAO, gender == ConstPlayer.TRAI_DAT
                ? "Quy Lão Kame" : (gender == ConstPlayer.NAMEC
                ? "Trưởng lão Guru" : "Vua Vegeta"));
        text = text.replaceAll(ConstTask.TEN_MAP_QUY_LAO, gender == ConstPlayer.TRAI_DAT
                ? "Đảo Kamê" : (gender == ConstPlayer.NAMEC
                ? "Đảo Guru" : "Vách núi đen"));
        text = text.replaceAll(ConstTask.TEN_QUAI_3000, gender == ConstPlayer.TRAI_DAT
                ? "ốc mượn hồn" : (gender == ConstPlayer.NAMEC
                ? "ốc sên" : "heo Xayda mẹ"));
        text = text.replaceAll(ConstTask.TEN_LANG, gender == ConstPlayer.TRAI_DAT
                ? "Làng Aru" : (gender == ConstPlayer.NAMEC
                ? "Làng Mori" : "Làng Kakarot"));
        text = text.replaceAll(ConstTask.TEN_NPC_NHA, gender == ConstPlayer.TRAI_DAT
                ? "Ông Gôhan" : (gender == ConstPlayer.NAMEC
                ? "Ông Moori" : "Ông Paragus"));
        text = text.replaceAll(ConstTask.TEN_QUAI_200, gender == ConstPlayer.TRAI_DAT
                ? "khủng long" : (gender == ConstPlayer.NAMEC
                ? "lợn lòi" : "quỷ đất"));
        text = text.replaceAll(ConstTask.TEN_MAP_200, gender == ConstPlayer.TRAI_DAT
                ? "Đồi hoa cúc" : (gender == ConstPlayer.NAMEC
                ? "Đồi nấm tím" : "Đồi hoang"));
        text = text.replaceAll(ConstTask.TEN_VACH_NUI, gender == ConstPlayer.TRAI_DAT
                ? "Vách núi Aru" : (gender == ConstPlayer.NAMEC
                ? "Vách núi Moori" : "Vách núi Kakarot"));
        text = text.replaceAll(ConstTask.TEN_MAP_500, gender == ConstPlayer.TRAI_DAT
                ? "Thung lũng tre" : (gender == ConstPlayer.NAMEC
                ? "Thị trấn Moori" : "Làng Plane"));
        text = text.replaceAll(ConstTask.TEN_NPC_TTVT, gender == ConstPlayer.TRAI_DAT
                ? "Dr. Brief" : (gender == ConstPlayer.NAMEC
                ? "Cargo" : "Cui"));
        text = text.replaceAll(ConstTask.TEN_QUAI_BAY_600, gender == ConstPlayer.TRAI_DAT
                ? "thằn lằn bay" : (gender == ConstPlayer.NAMEC
                ? "phi long" : "quỷ bay"));
        text = text.replaceAll(ConstTask.TEN_NPC_SHOP_LANG, gender == ConstPlayer.TRAI_DAT
                ? "Bunma" : (gender == ConstPlayer.NAMEC
                ? "Dende" : "Appule"));
        return text;
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            for (int j = 0; j <= 10; j++) {
                System.out.println("case ConstTask.TASK_" + i + "_" + j + ":");
                System.out.println("return player.playerTask.taskMain.id == " + i + " && player.playerTask.taskMain.index == " + j + ";");
            }
        }
    }

    private boolean isCurrentTask(Player player, int idTaskCustom) {
        return idTaskCustom == (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1;
    }

    public int getIdTask(Player player) {
        if (player.isPet || player.isTrieuHoiPet || player.isBoss || player.playerTask == null || player.playerTask.taskMain == null) {
            return -1;
        }
        return (player.playerTask.taskMain.id << 10) + player.playerTask.taskMain.index << 1;
    }

    //--------------------------------------------------------------------------
    public SideTaskTemplate getSideTaskTemplateById(int id) {
        if (id != -1) {
            return Manager.SIDE_TASKS_TEMPLATE.get(id);
        }
        return null;
    }

    public void changeSideTask(Player player, byte level) {
        if (player.playerTask.sideTask.leftTask > 0) {
            player.playerTask.sideTask.reset();
            SideTaskTemplate temp;
            if (player.playerTask.taskMain.id <= 16) temp = Manager.SIDE_TASKS_TEMPLATE.get(Util.nextInt(0, 26));
            else if (player.playerTask.taskMain.id <= 21) temp = Manager.SIDE_TASKS_TEMPLATE.get(Util.nextInt(27, 45));
            else temp = Manager.SIDE_TASKS_TEMPLATE.get(Util.nextInt(46, 57));
            player.playerTask.sideTask.template = temp;
            player.playerTask.sideTask.maxCount = Util.nextInt(temp.count[level][0], temp.count[level][1]);
            player.playerTask.sideTask.leftTask--;
            player.playerTask.sideTask.level = level;
            player.playerTask.sideTask.receivedTime = System.currentTimeMillis();
            Service.gI().sendThongBao(player, "Bạn nhận được nhiệm vụ: " + player.playerTask.sideTask.getName());
        } else {
            Service.gI().sendThongBao(player,
                    "Bạn đã nhận hết nhiệm vụ hôm nay. Hãy chờ tới ngày mai rồi nhận tiếp");
        }
    }

    public String getString(Player player) {
        return "Nhiệm vụ hiện tại: " + player.playerTask.sideTask.getName() + " ("
                + player.playerTask.sideTask.getLevel() + ")"
                + "\nHiện tại đã hoàn thành: " + player.playerTask.sideTask.count + "/"
                + player.playerTask.sideTask.maxCount + " ("
                + player.playerTask.sideTask.getPercentProcess() + "%)\nSố nhiệm vụ còn lại trong ngày: "
                + player.playerTask.sideTask.leftTask + "/" + ConstTask.MAX_SIDE_TASK
                + "\n Số thỏi vàng khóa nhận được từ nhiệm vụ này: " + getGoldRewardString(player);
    }

    public String getGoldRewardString(Player player) {
        return switch (player.playerTask.sideTask.level) {
            case ConstTask.EASY -> "1-3";
            case ConstTask.NORMAL -> "3-5";
            case ConstTask.HARD -> "5-7";
            case ConstTask.VERY_HARD -> "7-9";
            case ConstTask.HELL -> "9-15";
            default -> "0";
        };
    }

    public int getGoldReward(Player player) {
        return switch (player.playerTask.sideTask.level) {
            case ConstTask.EASY -> Util.nextInt(1, 3);
            case ConstTask.NORMAL -> Util.nextInt(3, 5);
            case ConstTask.HARD -> Util.nextInt(5, 7);
            case ConstTask.VERY_HARD -> Util.nextInt(7, 9);
            case ConstTask.HELL -> Util.nextInt(9, 15);
            default -> 0;
        };
    }

    public void removeSideTask(Player player) {
        Service.gI().sendThongBao(player, "Bạn vừa hủy bỏ nhiệm vụ " + player.playerTask.sideTask.getName());
        player.playerTask.sideTask.reset();
    }

    public void paySideTask(Player player) {
        if (player.playerTask.sideTask.template != null) {
            if (player.playerTask.sideTask.isDone()) {
                int goldReward = getGoldReward(player);
                Item thoivang = ItemService.gI().createNewItem((short) 457);
                thoivang.quantity = goldReward;
                thoivang.itemOptions.add(new Item.ItemOption(30, 1));
                InventoryServiceNew.gI().addItemBag(player, thoivang);
                Service.gI().sendThongBao(player, "Bạn nhận được "
                        + goldReward + " thỏi vàng");
                player.playerTask.sideTask.reset();
            } else {
                Service.gI().sendThongBao(player, "Bạn chưa hoàn thành nhiệm vụ");
            }
        }
    }

    public void checkDoneSideTaskKillMob(Player player, Mob mob) {
        if (player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 0 && mob.tempId == ConstMob.KHUNG_LONG)
                    || (player.playerTask.sideTask.template.id == 1 && mob.tempId == ConstMob.LON_LOI)
                    || (player.playerTask.sideTask.template.id == 2 && mob.tempId == ConstMob.QUY_DAT)
                    || (player.playerTask.sideTask.template.id == 3 && mob.tempId == ConstMob.KHUNG_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 4 && mob.tempId == ConstMob.LON_LOI_ME)
                    || (player.playerTask.sideTask.template.id == 5 && mob.tempId == ConstMob.QUY_DAT_ME)
                    || (player.playerTask.sideTask.template.id == 6 && mob.tempId == ConstMob.THAN_LAN_BAY)
                    || (player.playerTask.sideTask.template.id == 7 && mob.tempId == ConstMob.PHI_LONG)
                    || (player.playerTask.sideTask.template.id == 8 && mob.tempId == ConstMob.QUY_BAY)
                    || (player.playerTask.sideTask.template.id == 9 && mob.tempId == ConstMob.THAN_LAN_ME)
                    || (player.playerTask.sideTask.template.id == 10 && mob.tempId == ConstMob.PHI_LONG_ME)
                    || (player.playerTask.sideTask.template.id == 11 && mob.tempId == ConstMob.QUY_BAY_ME)
                    || (player.playerTask.sideTask.template.id == 12 && mob.tempId == ConstMob.HEO_RUNG)
                    || (player.playerTask.sideTask.template.id == 13 && mob.tempId == ConstMob.HEO_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 14 && mob.tempId == ConstMob.HEO_XAYDA)
                    || (player.playerTask.sideTask.template.id == 15 && mob.tempId == ConstMob.OC_MUON_HON)
                    || (player.playerTask.sideTask.template.id == 16 && mob.tempId == ConstMob.OC_SEN)
                    || (player.playerTask.sideTask.template.id == 17 && mob.tempId == ConstMob.HEO_XAYDA_ME)
                    || (player.playerTask.sideTask.template.id == 18 && mob.tempId == ConstMob.KHONG_TAC)
                    || (player.playerTask.sideTask.template.id == 19 && mob.tempId == ConstMob.QUY_DAU_TO)
                    || (player.playerTask.sideTask.template.id == 20 && mob.tempId == ConstMob.QUY_DIA_NGUC)
                    || (player.playerTask.sideTask.template.id == 21 && mob.tempId == ConstMob.HEO_RUNG_ME)
                    || (player.playerTask.sideTask.template.id == 22 && mob.tempId == ConstMob.HEO_XANH_ME)
                    || (player.playerTask.sideTask.template.id == 23 && mob.tempId == ConstMob.ALIEN)
                    || (player.playerTask.sideTask.template.id == 24 && mob.tempId == ConstMob.TAMBOURINE)
                    || (player.playerTask.sideTask.template.id == 25 && mob.tempId == ConstMob.DRUM)
                    || (player.playerTask.sideTask.template.id == 26 && mob.tempId == ConstMob.AKKUMAN)
                    || (player.playerTask.sideTask.template.id == 27 && mob.tempId == ConstMob.NAPPA)
                    || (player.playerTask.sideTask.template.id == 28 && mob.tempId == ConstMob.SOLDIER)
                    || (player.playerTask.sideTask.template.id == 29 && mob.tempId == ConstMob.APPULE)
                    || (player.playerTask.sideTask.template.id == 30 && mob.tempId == ConstMob.RASPBERRY)
                    || (player.playerTask.sideTask.template.id == 31 && mob.tempId == ConstMob.THAN_LAN_XANH)
                    || (player.playerTask.sideTask.template.id == 32 && mob.tempId == ConstMob.QUY_DAU_NHON)
                    || (player.playerTask.sideTask.template.id == 33 && mob.tempId == ConstMob.QUY_DAU_VANG)
                    || (player.playerTask.sideTask.template.id == 34 && mob.tempId == ConstMob.QUY_DA_TIM)
                    || (player.playerTask.sideTask.template.id == 35 && mob.tempId == ConstMob.QUY_GIA)
                    || (player.playerTask.sideTask.template.id == 36 && mob.tempId == ConstMob.CA_SAU)
                    || (player.playerTask.sideTask.template.id == 37 && mob.tempId == ConstMob.DOI_DA_XANH)
                    || (player.playerTask.sideTask.template.id == 38 && mob.tempId == ConstMob.QUY_CHIM)
                    || (player.playerTask.sideTask.template.id == 39 && mob.tempId == ConstMob.LINH_DAU_TROC)
                    || (player.playerTask.sideTask.template.id == 40 && mob.tempId == ConstMob.LINH_TAI_DAI)
                    || (player.playerTask.sideTask.template.id == 41 && mob.tempId == ConstMob.LINH_VU_TRU)
                    || (player.playerTask.sideTask.template.id == 42 && mob.tempId == ConstMob.KHI_LONG_DEN)
                    || (player.playerTask.sideTask.template.id == 43 && mob.tempId == ConstMob.KHI_GIAP_SAT)
                    || (player.playerTask.sideTask.template.id == 44 && mob.tempId == ConstMob.KHI_LONG_DO)
                    || (player.playerTask.sideTask.template.id == 45 && mob.tempId == ConstMob.KHI_LONG_VANG)
                    || (player.playerTask.sideTask.template.id == 46 && mob.tempId == ConstMob.XEN_CON_CAP_1)
                    || (player.playerTask.sideTask.template.id == 47 && mob.tempId == ConstMob.XEN_CON_CAP_2)
                    || (player.playerTask.sideTask.template.id == 48 && mob.tempId == ConstMob.XEN_CON_CAP_3)
                    || (player.playerTask.sideTask.template.id == 49 && mob.tempId == ConstMob.XEN_CON_CAP_4)
                    || (player.playerTask.sideTask.template.id == 50 && mob.tempId == ConstMob.XEN_CON_CAP_5)
                    || (player.playerTask.sideTask.template.id == 51 && mob.tempId == ConstMob.XEN_CON_CAP_6)
                    || (player.playerTask.sideTask.template.id == 52 && mob.tempId == ConstMob.XEN_CON_CAP_7)
                    || (player.playerTask.sideTask.template.id == 53 && mob.tempId == ConstMob.XEN_CON_CAP_8)
                    || (player.playerTask.sideTask.template.id == 54 && mob.tempId == ConstMob.TAI_TIM)
                    || (player.playerTask.sideTask.template.id == 55 && mob.tempId == ConstMob.ABO)
                    || (player.playerTask.sideTask.template.id == 56 && mob.tempId == ConstMob.KADO)
                    || (player.playerTask.sideTask.template.id == 57 && mob.tempId == ConstMob.DA_XANH)) {
                player.playerTask.sideTask.count++;
                notifyProcessSideTask(player);
            }
        }
    }

    public void checkDoneSideTaskPickItem(Player player, ItemMap item) {
        if (player.playerTask.sideTask.template != null) {
            if ((player.playerTask.sideTask.template.id == 58 && item.itemTemplate.type == 9)) {
                player.playerTask.sideTask.count += item.quantity;
                notifyProcessSideTask(player);
            }
        }
    }

    private void notifyProcessSideTask(Player player) {
        int percentDone = player.playerTask.sideTask.getPercentProcess();
        boolean notify = false;
        if (percentDone != 100) {
            if (!player.playerTask.sideTask.notify90 && percentDone >= 90) {
                player.playerTask.sideTask.notify90 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify80 && percentDone >= 80) {
                player.playerTask.sideTask.notify80 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify70 && percentDone >= 70) {
                player.playerTask.sideTask.notify70 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify60 && percentDone >= 60) {
                player.playerTask.sideTask.notify60 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify50 && percentDone >= 50) {
                player.playerTask.sideTask.notify50 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify40 && percentDone >= 40) {
                player.playerTask.sideTask.notify40 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify30 && percentDone >= 30) {
                player.playerTask.sideTask.notify30 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify20 && percentDone >= 20) {
                player.playerTask.sideTask.notify20 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify10 && percentDone >= 10) {
                player.playerTask.sideTask.notify10 = true;
                notify = true;
            } else if (!player.playerTask.sideTask.notify0 && percentDone >= 0) {
                player.playerTask.sideTask.notify0 = true;
                notify = true;
            }
            if (notify) {
                Service.gI().sendThongBao(player, "Nhiệm vụ: "
                        + player.playerTask.sideTask.getName() + " đã hoàn thành: "
                        + player.playerTask.sideTask.count + "/" + player.playerTask.sideTask.maxCount + " ("
                        + percentDone + "%)");
            }
        } else {
            Service.gI().sendThongBao(player, "Chúc mừng bạn đã hoàn thành nhiệm vụ, "
                    + "bây giờ hãy quay về Bò Mộng trả nhiệm vụ.");
        }
    }
}
    