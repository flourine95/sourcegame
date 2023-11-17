package com.girlkun.services.func;

import com.girlkun.consts.ConstMap;
import com.girlkun.consts.ConstNpc;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.card.Card;
import com.girlkun.models.card.RadarCard;
import com.girlkun.models.card.RadarService;
import com.girlkun.models.item.Item;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.map.Zone;
import com.girlkun.models.player.Inventory;
import com.girlkun.models.player.Player;
import com.girlkun.models.skill.Skill;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.server.io.MySession;
import com.girlkun.services.*;
import com.girlkun.utils.Logger;
import com.girlkun.utils.SkillUtil;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import java.util.Date;
import java.util.Random;

public class UseItem {

    private static final int ITEM_BOX_TO_BODY_OR_BAG = 0;
    private static final int ITEM_BAG_TO_BOX = 1;
    private static final int ITEM_BODY_TO_BOX = 3;
    private static final int ITEM_BAG_TO_BODY = 4;
    private static final int ITEM_BODY_TO_BAG = 5;
    private static final int ITEM_BAG_TO_PET_BODY = 6;
    private static final int ITEM_BODY_PET_TO_BAG = 7;

    private static final byte DO_USE_ITEM = 0;
    private static final byte DO_THROW_ITEM = 1;
    private static final byte ACCEPT_THROW_ITEM = 2;
    private static final byte ACCEPT_USE_ITEM = 3;
    public static final int[][][] LIST_ITEM_CLOTHES = {
            // áo , quần , găng ,giày,rada
            //td -> nm -> xd
            {{0, 33, 3, 34, 136, 137, 138, 139, 230, 231, 232, 233, 555}, {6, 35, 9, 36, 140, 141, 142, 143, 242, 243, 244, 245, 556}, {21, 24, 37, 38, 144, 145, 146, 147, 254, 255, 256, 257, 562}, {27, 30, 39, 40, 148, 149, 150, 151, 266, 267, 268, 269, 563}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
            {{1, 41, 4, 42, 152, 153, 154, 155, 234, 235, 236, 237, 557}, {7, 43, 10, 44, 156, 157, 158, 159, 246, 247, 248, 249, 558}, {22, 46, 25, 45, 160, 161, 162, 163, 258, 259, 260, 261, 564}, {28, 47, 31, 48, 164, 165, 166, 167, 270, 271, 272, 273, 565}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}},
            {{2, 49, 5, 50, 168, 169, 170, 171, 238, 239, 240, 241, 559}, {8, 51, 11, 52, 172, 173, 174, 175, 250, 251, 252, 253, 560}, {23, 53, 26, 54, 176, 177, 178, 179, 262, 263, 264, 265, 566}, {29, 55, 32, 56, 180, 181, 182, 183, 274, 275, 276, 277, 567}, {12, 57, 58, 59, 184, 185, 186, 187, 278, 279, 280, 281, 561}}
    };

    private static UseItem instance;

    private UseItem() {

    }

    public static UseItem gI() {
        if (instance == null) {
            instance = new UseItem();
        }
        return instance;
    }

    public void getItem(MySession session, Message msg) {
        Player player = session.player;
        TransactionService.gI().cancelTrade(player);
        try {
            int type = msg.reader().readByte();
            int index = msg.reader().readByte();
            if (index == -1) {
                return;
            }
            switch (type) {
                case ITEM_BOX_TO_BODY_OR_BAG -> {
                    InventoryServiceNew.gI().itemBoxToBodyOrBag(player, index);
                    TaskService.gI().checkDoneTaskGetItemBox(player);
                }
                case ITEM_BAG_TO_BOX -> InventoryServiceNew.gI().itemBagToBox(player, index);
                case ITEM_BODY_TO_BOX -> InventoryServiceNew.gI().itemBodyToBox(player, index);
                case ITEM_BAG_TO_BODY -> InventoryServiceNew.gI().itemBagToBody(player, index);
                case ITEM_BODY_TO_BAG -> InventoryServiceNew.gI().itemBodyToBag(player, index);
                case ITEM_BAG_TO_PET_BODY -> InventoryServiceNew.gI().itemBagToPetBody(player, index);
                case ITEM_BODY_PET_TO_BAG -> InventoryServiceNew.gI().itemPetBodyToBag(player, index);
            }
            player.setClothes.setup();
            if (player.pet != null) {
                player.pet.setClothes.setup();
            }
            player.setClanMember();
            Service.gI().point(player);
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    public void doItem(Player player, Message _msg) {
        TransactionService.gI().cancelTrade(player);
        Message msg;
        byte type;
        try {
            type = _msg.reader().readByte();
            int where = _msg.reader().readByte();
            int index = _msg.reader().readByte();
            switch (type) {
                case DO_USE_ITEM -> {
                    if (player != null && player.inventory != null) {
                        if (index != -1) {
                            Item item = player.inventory.itemsBag.get(index);
                            if (item.isNotNullItem()) {
                                if (item.template.type == 7) {
                                    msg = new Message(-43);
                                    msg.writer().writeByte(type);
                                    msg.writer().writeByte(where);
                                    msg.writer().writeByte(index);
                                    msg.writer().writeUTF("Bạn chắc chắn học " + player.inventory.itemsBag.get(index).template.name + "?");
                                    player.sendMessage(msg);
                                } else {
                                    UseItem.gI().useItem(player, item, index);
                                }
                            }
                        } else {
                            this.eatPea(player);
                        }
                    }
                }
                case DO_THROW_ITEM -> {
                    if (!(player.zone.map.mapId == 21 || player.zone.map.mapId == 22 || player.zone.map.mapId == 23)) {
                        Item item;
                        if (where == 0) {
                            item = player.inventory.itemsBody.get(index);
                        } else {
                            item = player.inventory.itemsBag.get(index);
                        }
                        msg = new Message(-43);
                        msg.writer().writeByte(type);
                        msg.writer().writeByte(where);
                        msg.writer().writeByte(index);
                        msg.writer().writeUTF("Bạn chắc chắn muốn vứt " + item.template.name + "?");
                        player.sendMessage(msg);
                    } else {
                        Service.gI().sendThongBao(player, "Không thể thực hiện");
                    }
                }
                case ACCEPT_THROW_ITEM -> {
                    InventoryServiceNew.gI().throwItem(player, where, index);
                    Service.gI().point(player);
                    InventoryServiceNew.gI().sendItemBags(player);
                }
                case ACCEPT_USE_ITEM -> UseItem.gI().useItem(player, player.inventory.itemsBag.get(index), index);
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useItem(Player pl, Item item, int indexBag) {
        if (item.template.strRequire <= pl.nPoint.power) {
            switch (item.template.type) {
                case 6 -> this.eatPea(pl);
                case 7 -> learnSkill(pl, item);
                case 11 -> {
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendFlagBag(pl);
                }
                case 12 -> controllerCallRongThan(pl, item);
                case 23, 24 -> InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                case 33 -> useCard(pl, item);
                case 72 -> {
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendPetFollow(pl, (short) (item.template.iconID - 1));
                }
                case 74 -> {
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendFoot(pl, item.template.id);
                }
                case 75 -> {
                    InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                    Service.gI().sendchienlinh(pl, (short) (item.template.iconID - 1));
                }
                default -> {
                    switch (item.template.id) {
                        case 457 -> {
                            pl.inventory.gold += 500000000;
                            Service.gI().sendMoney(pl);
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            InventoryServiceNew.gI().sendItemBags(pl);
                            pl.achievement.plusCount(13);
                        }
                        case 1132 -> Input.gI().tangHongNgoc(pl);
                        case 992 -> {
                            pl.type = 1;
                            pl.maxTime = 5;
                            Service.gI().Transport(pl);
                            Service.gI().clearMap(pl);
                        }
                        case 1322 -> {
                            pl.type = 1;
                            pl.maxTime = 6;
                            Service.gI().Transport(pl);
                            Service.gI().clearMap(pl);
                        }
                        case 361 -> {
                            if (pl.idNRNM != -1) {
                                Service.gI().sendThongBao(pl, "Không thể thực hiện");
                                return;
                            }
                            pl.idGo = (short) Util.nextInt(0, 6);
                            NpcService.gI().createMenuConMeo(pl, ConstNpc.CONFIRM_TELE_NAMEC, -1, "1 Sao (" + NgocRongNamecService.gI().getDis(pl, 0, (short) 353) + " m)\n2 Sao (" + NgocRongNamecService.gI().getDis(pl, 1, (short) 354) + " m)\n3 Sao (" + NgocRongNamecService.gI().getDis(pl, 2, (short) 355) + " m)\n4 Sao (" + NgocRongNamecService.gI().getDis(pl, 3, (short) 356) + " m)\n5 Sao (" + NgocRongNamecService.gI().getDis(pl, 4, (short) 357) + " m)\n6 Sao (" + NgocRongNamecService.gI().getDis(pl, 5, (short) 358) + " m)\n7 Sao (" + NgocRongNamecService.gI().getDis(pl, 6, (short) 359) + " m)", "Đến ngay\nViên " + (pl.idGo + 1) + " Sao\n50 ngọc", "Kết thức");
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            InventoryServiceNew.gI().sendItemBags(pl);
                        }
                        case 942 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 966, 967, 968);
                            Service.gI().point(pl);
                        }
                        case 943 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 969, 970, 971);
                            Service.gI().point(pl);
                        }
                        case 944 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 972, 973, 974);
                            Service.gI().point(pl);
                        }
                        case 967 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1050, 1051, 1052);
                            Service.gI().point(pl);
                        }
                        case 1107 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1183, 1184, 1185);
                            Service.gI().point(pl);
                        }
                        case 1140 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1285, 1286, 1287);
                            Service.gI().point(pl);
                        }
                        case 1133 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1261, 1262, 1263);
                            Service.gI().point(pl);
                        }
                        case 1180 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1270, 1271, 1272);
                            Service.gI().point(pl);
                        }
                        case 1181 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1273, 1274, 1275);
                            Service.gI().point(pl);
                        }
                        case 1196 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1294, 1295, 1296);
                            Service.gI().point(pl);
                        }
                        case 1197 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1297, 1298, 1299);
                            Service.gI().point(pl);
                        }
                        case 1198 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1300, 1301, 1302);
                            Service.gI().point(pl);
                        }
                        case 1221 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1333, 1334, 1335);
                            Service.gI().point(pl);
                        }
                        case 1222 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1336, 1337, 1338);
                            Service.gI().point(pl);
                        }
                        case 1223 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1339, 1340, 1341);
                            Service.gI().point(pl);
                        }
                        case 1229 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1345, 1346, 1347);
                            Service.gI().point(pl);
                        }
                        case 1230 -> {
                            if (pl.newpet != null) {
                                ChangeMapService.gI().exitMap(pl.newpet);
                                pl.newpet.dispose();
                                pl.newpet = null;
                            }
                            InventoryServiceNew.gI().itemBagToBody(pl, indexBag);
                            PetService.Pet2(pl, 1348, 1349, 1350);
                            Service.gI().point(pl);
                        }
                        case 211, 212 -> eatGrapes(pl, item);
                        case 1105 -> UseItem.gI().hopTS(pl, item);
                        case 342, 343, 344, 345 -> {
                            if (pl.zone.items.stream().filter(it -> it != null && it.itemTemplate.type == 22).count() < 5) {
                                Service.gI().dropVeTinh(pl, item, pl.zone, pl.location.x, pl.location.y);
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            } else {
                                Service.gI().sendThongBao(pl, "Đặt ít thôi con");
                            }
                        }
                        case 380 -> openCSKB(pl, item);
                        case 1296 -> mayDoBoss(pl);
                        case 1029 -> Input.gI().TAOPET(pl);
                        case 668 -> hopQuaTanThu(pl, item);
                        case 1334 -> hopThanLinh(pl, item);
                        case 722 -> openCSH(pl, item);
                        case 570 -> openWoodChest(pl, item);
                        case 381, 382, 383, 384, 385, 379, 1201, 663, 664, 665, 666, 667, 579, 1099, 1100, 1101, 1102, 1103, 899, 1317, 465, 466, 472, 473 ->
                                useItemTime(pl, item);
                        case 521 -> useTDLT(pl, item);
                        case 454 -> UseItem.gI().usePorata(pl);
                        case 921 -> UseItem.gI().usePorata2(pl);
                        case 1165 -> UseItem.gI().usePorata3(pl);
                        case 1129 -> UseItem.gI().usePorata4(pl);
                        case 193 -> {
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            openCapsuleUI(pl);
                        }
                        case 194 -> openCapsuleUI(pl);
                        case 1241 -> changeSkillPet4(pl, item);
                        case 401 -> changePet(pl, item);
                        case 1108 -> changePetBerus(pl, item);
                        case 543 -> {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) == 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                int[] pet = new int[]{942, 1180, 1181, 1196, 1197, 1198, 1107, 1140};
                                int randomPet = new Random().nextInt(pet.length);
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                                Item linhThu = ItemService.gI().createNewItem((short) pet[randomPet]);
                                linhThu.itemOptions.add(new ItemOption(50, Util.nextInt(10, 25)));
                                linhThu.itemOptions.add(new ItemOption(77, Util.nextInt(15, 30)));
                                linhThu.itemOptions.add(new ItemOption(103, Util.nextInt(15, 30)));
                                linhThu.itemOptions.add(new ItemOption(80, Util.nextInt(3, 10)));
                                linhThu.itemOptions.add(new ItemOption(81, Util.nextInt(3, 10)));
                                if (Util.isTrue(98, 100)) {
                                    linhThu.itemOptions.add(new ItemOption(93, Util.nextInt(3, 5)));
                                }
                                linhThu.itemOptions.add(new ItemOption(30, 1));
                                InventoryServiceNew.gI().addItemBag(pl, linhThu);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được Pet " + linhThu.template.name);
                            }
                        }
                        case 542 -> changePetPic(pl, item);
                        case 402, 403, 404, 759 -> upSkillPet(pl, item);
                        case 2000, 2001, 2002 -> UseItem.gI().itemSKH(pl, item);
                        case 2003, 2004, 2005 -> UseItem.gI().itemDHD(pl, item);
                        case 736 -> ItemService.gI().OpenItem736(pl, item);
                        case 1237 -> openPhapSu(pl, item);
                        case 1335, 1336, 1337 -> banhTrungThu(pl, item);
                        case 1342 -> hopTrungThu(pl, item);
                        case 987 ->
                                Service.gI().sendThongBao(pl, "Bảo vệ trang bị không bị rớt cấp"); //đá bảo vệ
                        case 2006 -> Input.gI().createFormChangeNameByItem(pl);
                        case 2028 -> {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) == 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                int[] pet = new int[]{2019, 2020, 2021, 2022, 2023, 2024, 2025, 2026, 2033, 2034, 2036, 2037, 2038, 2039, 2040};
                                int randomPet = new Random().nextInt(pet.length);
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                                Item linhThu = ItemService.gI().createNewItem((short) pet[randomPet]);
                                linhThu.itemOptions.add(new ItemOption(50, Util.nextInt(15, 30)));
                                linhThu.itemOptions.add(new ItemOption(77, Util.nextInt(20, 40)));
                                linhThu.itemOptions.add(new ItemOption(103, Util.nextInt(20, 40)));
                                linhThu.itemOptions.add(new ItemOption(95, Util.nextInt(5, 8)));
                                linhThu.itemOptions.add(new ItemOption(96, Util.nextInt(5, 8)));
                                InventoryServiceNew.gI().addItemBag(pl, linhThu);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được Linh thú " + linhThu.template.name);
                            }
                        }
                        case 2027 -> {
                            if (InventoryServiceNew.gI().getCountEmptyBag(pl) == 0) {
                                Service.gI().sendThongBao(pl, "Hành trang không đủ chỗ trống");
                            } else {
                                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                                Item linhThu = ItemService.gI().createNewItem((short) Util.nextInt(1273, 1295));
                                linhThu.itemOptions.add(new ItemOption(50, Util.nextInt(30, 60)));
                                linhThu.itemOptions.add(new ItemOption(77, Util.nextInt(40, 70)));
                                linhThu.itemOptions.add(new ItemOption(103, Util.nextInt(40, 70)));
                                linhThu.itemOptions.add(new ItemOption(95, Util.nextInt(7, 15)));
                                linhThu.itemOptions.add(new ItemOption(96, Util.nextInt(7, 15)));
                                linhThu.itemOptions.add(new ItemOption(30, 1));
                                InventoryServiceNew.gI().addItemBag(pl, linhThu);
                                InventoryServiceNew.gI().sendItemBags(pl);
                                Service.gI().sendThongBao(pl, "Chúc mừng bạn nhận được Linh thú " + linhThu.template.name);
                            }
                        }
                    }
                }
            }
            InventoryServiceNew.gI().sendItemBags(pl);
        } else {
            Service.gI().sendThongBaoOK(pl, "Sức mạnh không đủ yêu cầu");
        }
    }


    public void useCard(Player pl, Item item) {
        RadarCard radarTemplate = RadarService.gI().RADAR_TEMPLATE.stream().filter(c -> c.Id == item.template.id)
                .findFirst().orElse(null);
        if (radarTemplate == null) {
            return;
        }
        if (radarTemplate.Require != -1) {
            RadarCard radarRequireTemplate = RadarService.gI().RADAR_TEMPLATE.stream()
                    .filter(r -> r.Id == radarTemplate.Require).findFirst().orElse(null);
            if (radarRequireTemplate == null) {
                return;
            }
        }
        Card card = pl.cards.stream().filter(r -> r.Id == item.template.id).findFirst().orElse(null);
        if (card == null) {
            Card newCard = new Card(item.template.id, (byte) 1, radarTemplate.Max, (byte) -1, radarTemplate.Options);
            pl.cards.add(newCard);
            RadarService.gI().RadarSetAmount(pl, newCard.Id, newCard.Amount, newCard.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, newCard.Id, newCard.Level);
        } else {
            if (card.Level >= 2) {
                Service.gI().sendThongBao(pl, "Thẻ này đã đạt cấp tối đa");
                return;
            }
            card.Amount++;
            if (card.Amount >= card.MaxAmount) {
                card.Amount = 0;
                if (card.Level == -1) {
                    card.Level = 1;
                } else {
                    card.Level++;
                }
                Service.gI().point(pl);
            }
            RadarService.gI().RadarSetAmount(pl, card.Id, card.Amount, card.MaxAmount);
            RadarService.gI().RadarSetLevel(pl, card.Id, card.Level);
        }
        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
        InventoryServiceNew.gI().sendItemBags(pl);
    }

    private void changePet(Player player, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBody(player.pet) == 8) {
            if (player.pet != null) {
                int gender = player.pet.gender + 1;
                if (gender > 2) {
                    gender = 0;
                }
                PetService.gI().changeNormalPet(player, gender);
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.gI().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void changePetBerus(Player player, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBody(player.pet) == 8) {
            if (player.pet != null) {
                int gender = player.pet.gender;
                PetService.gI().changeBerusPet(player, gender);
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.gI().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void changePetMabu(Player player, Item item) {
        if (player.pet != null) {
            int gender = player.pet.gender;
            PetService.gI().changeMabuPet(player, gender);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
        } else {
            Service.gI().sendThongBao(player, "Không thể thực hiện");
        }
    }

    private void changePetPic(Player player, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBody(player.pet) == 8) {
            if (player.pet != null) {
                int gender = player.pet.gender;
                PetService.gI().changePicPet(player, gender);
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Không thể thực hiện");
            }
        } else {
            Service.gI().sendThongBao(player, "Vui lòng tháo hết đồ đệ tử");
        }
    }

    private void openPhieuCaiTrangHaiTac(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            Item ct = ItemService.gI().createNewItem((short) Util.nextInt(618, 626));
            ct.itemOptions.add(new ItemOption(147, 3));
            ct.itemOptions.add(new ItemOption(77, 3));
            ct.itemOptions.add(new ItemOption(103, 3));
            ct.itemOptions.add(new ItemOption(149, 0));
            if (item.template.id == 2006) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(1, 7)));
            } else if (item.template.id == 2007) {
                ct.itemOptions.add(new ItemOption(93, Util.nextInt(7, 30)));
            }
            InventoryServiceNew.gI().addItemBag(pl, ct);
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, item.template.iconID, ct.template.iconID);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void eatGrapes(Player pl, Item item) {
        int percentCurrentStatima = pl.nPoint.stamina * 100 / pl.nPoint.maxStamina;
        if (percentCurrentStatima > 50) {
            Service.gI().sendThongBao(pl, "Thể lực vẫn còn trên 50%");
            return;
        } else if (item.template.id == 211) {
            pl.nPoint.stamina = pl.nPoint.maxStamina;
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 100%");
        } else if (item.template.id == 212) {
            pl.nPoint.stamina += (short) (pl.nPoint.maxStamina * 20 / 100);
            Service.gI().sendThongBao(pl, "Thể lực của bạn đã được hồi phục 20%");
        }
        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
        InventoryServiceNew.gI().sendItemBags(pl);
        PlayerService.gI().sendCurrentStamina(pl);
    }

    private void openCSKB(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] temp = {76, 188, 189, 190, 381, 382, 383, 384, 385};
            int[][] gold = {{5000, 20000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (index <= 3) {
                pl.inventory.gold += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 930;
            } else {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);

            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public boolean mayDoBoss(Player pl) {
        try {
            BossManager.gI().doBossMember(pl);
            return true;
        } catch (Exception e) {
            Logger.logException(UseItem.class, e, "Error player: " + pl.name);
        }
        return false;
    }

    private void changeSkillPet4(Player pl, Item item) {
        if (pl.pet != null) {
            if (pl.pet.nPoint.power > 20000000000L) {
                if (pl.pet.playerSkill.skills.get(2).skillId != -1) {
                    pl.pet.openSkill4();
                    Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                    InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    InventoryServiceNew.gI().sendItemBags(pl);
                } else {
                    Service.gI().sendThongBao(pl, "Ít nhất đệ tử ngươi phải có chiêu 3 chứ!");
                }
            } else {
                Service.gI().sendThongBao(pl, "Yêu cầu đệ tử có skill 4");
            }
        } else {
            Service.gI().sendThongBao(pl, "Ngươi làm gì có đệ tử?");
        }
    }

    private void openPhapSu(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] manh = {1232, 1233, 1234};
            short da = 1235;
            short bua = 1236;
            short[] rac = {579, 1201, 15};
            byte index = (byte) Util.nextInt(0, manh.length - 1);
            byte index2 = (byte) Util.nextInt(0, rac.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(35, 100)) {
                Item it = ItemService.gI().createNewItem(rac[index2]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else if (Util.isTrue(13, 100)) {
                Item it = ItemService.gI().createNewItem(da);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else if (Util.isTrue(3, 100)) {
                Item it = ItemService.gI().createNewItem(bua);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            } else {
                Item it = ItemService.gI().createNewItem(manh[index]);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void banhTrungThu(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] rac = {579, 1201, 899, 1099, 1100, 1101, 1102};
            int[][] gold = {{10000000, 30000000}};
            byte index2 = (byte) Util.nextInt(0, rac.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (Util.isTrue(60, 100)) {
                int vang = Util.nextInt(gold[0][0], gold[0][1]);
                pl.inventory.gold += vang;
                if (pl.inventory.gold > Inventory.LIMIT_GOLD) {
                    pl.inventory.gold = Inventory.LIMIT_GOLD;
                }
                icon[1] = 930;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + Util.format(vang) + " Vàng");
                if (item.template.id == 1335 || item.template.id == 1336) {
                    pl.nhsPoint += 2;
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "|4|Bạn nhận được 2 Điểm Sự kiện");
                } else {
                    pl.nhsPoint += 5;
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "|4|Bạn nhận được 5 Điểm Sự kiện");
                }
            } else {
                Item it = ItemService.gI().createNewItem(rac[index2]);
                if (item.template.id == 1337) {
                    it.quantity = 2;
                }
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
                if (item.template.id == 1335 || item.template.id == 1336) {
                    pl.nhsPoint += 2;
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "|4|Bạn nhận được 2 Điểm Sự kiện");
                } else {
                    pl.nhsPoint += 5;
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "|4|Bạn nhận được 5 Điểm Sự kiện");
                }
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void hopTrungThu(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] rac = {1333, 1344, 1345};
            byte index2 = (byte) Util.nextInt(0, rac.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            Item it = ItemService.gI().createNewItem(rac[index2]);
//            System.out.println("    it    " + it.template.id);
            if (it.template.id == 1345) {
                it.itemOptions.add(new ItemOption(50, Util.nextInt(20, 50)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(25, 60)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(25, 60)));
                it.itemOptions.add(new ItemOption(14, Util.nextInt(5, 15)));
                if (Util.isTrue(30, 100)) {
                    it.itemOptions.add(new ItemOption(100, Util.nextInt(100, 1000)));
                    it.itemOptions.add(new ItemOption(101, Util.nextInt(100, 500)));
                }
            } else {
                it.itemOptions.add(new ItemOption(50, Util.nextInt(15, 40)));
                it.itemOptions.add(new ItemOption(77, Util.nextInt(20, 50)));
                it.itemOptions.add(new ItemOption(103, Util.nextInt(20, 50)));
                it.itemOptions.add(new ItemOption(14, Util.nextInt(2, 10)));
                it.itemOptions.add(new ItemOption(95, Util.nextInt(2, 10)));
                it.itemOptions.add(new ItemOption(96, Util.nextInt(2, 10)));
            }
            if (Util.isTrue(99, 100)) {
                it.itemOptions.add(new ItemOption(93, Util.nextInt(1, 5)));
            }
            it.itemOptions.add(new ItemOption(30, 0));
            InventoryServiceNew.gI().addItemBag(pl, it);
            icon[1] = it.template.iconID;
            Service.gI().sendThongBao(pl, "Bạn đã nhận được " + it.template.name);
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    public void hopQuaTanThu(Player player, Item item) {

        if (player.gender == 0) {
            Item itemReward = ItemService.gI().createNewItem((short) 0);
            Item itemReward1 = ItemService.gI().createNewItem((short) 6);
            Item itemReward2 = ItemService.gI().createNewItem((short) 12);
            Item itemReward3 = ItemService.gI().createNewItem((short) 21);
            Item itemReward4 = ItemService.gI().createNewItem((short) 27);
            itemReward.quantity = 1;
            itemReward1.quantity = 1;
            itemReward2.quantity = 1;
            itemReward3.quantity = 1;
            itemReward4.quantity = 1;
            if (InventoryServiceNew.gI().getCountEmptyBag(player) > 4) {
                itemReward.itemOptions.add(new ItemOption(47, 5));
                itemReward1.itemOptions.add(new ItemOption(7, 30));
                itemReward2.itemOptions.add(new ItemOption(14, 1));
                itemReward3.itemOptions.add(new ItemOption(0, 5));
                itemReward4.itemOptions.add(new ItemOption(6, 30));

                itemReward.itemOptions.add(new ItemOption(107, 12));
                itemReward1.itemOptions.add(new ItemOption(107, 12));
                itemReward2.itemOptions.add(new ItemOption(107, 12));
                itemReward3.itemOptions.add(new ItemOption(107, 12));
                itemReward4.itemOptions.add(new ItemOption(107, 12));

                itemReward.itemOptions.add(new ItemOption(30, 1));
                itemReward1.itemOptions.add(new ItemOption(30, 1));
                itemReward2.itemOptions.add(new ItemOption(30, 1));
                itemReward3.itemOptions.add(new ItemOption(30, 1));
                itemReward4.itemOptions.add(new ItemOption(30, 1));

                InventoryServiceNew.gI().addItemBag(player, itemReward);
                InventoryServiceNew.gI().addItemBag(player, itemReward1);
                InventoryServiceNew.gI().addItemBag(player, itemReward2);
                InventoryServiceNew.gI().addItemBag(player, itemReward3);
                InventoryServiceNew.gI().addItemBag(player, itemReward4);

                Service.gI().sendThongBao(player, "Bạn đã nhận được set đồ 10 sao !");
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
            }
        } else if (player.gender == 1) {
            Item itemReward = ItemService.gI().createNewItem((short) 1);
            Item itemReward1 = ItemService.gI().createNewItem((short) 7);
            Item itemReward2 = ItemService.gI().createNewItem((short) 12);
            Item itemReward3 = ItemService.gI().createNewItem((short) 22);
            Item itemReward4 = ItemService.gI().createNewItem((short) 28);
            itemReward.quantity = 1;
            itemReward1.quantity = 1;
            itemReward2.quantity = 1;
            itemReward3.quantity = 1;
            itemReward4.quantity = 1;
            if (InventoryServiceNew.gI().getCountEmptyBag(player) > 4) {

                itemReward.itemOptions.add(new ItemOption(47, 5));
                itemReward1.itemOptions.add(new ItemOption(7, 30));
                itemReward2.itemOptions.add(new ItemOption(14, 1));
                itemReward3.itemOptions.add(new ItemOption(0, 5));
                itemReward4.itemOptions.add(new ItemOption(6, 30));

                itemReward.itemOptions.add(new ItemOption(107, 12));
                itemReward1.itemOptions.add(new ItemOption(107, 12));
                itemReward2.itemOptions.add(new ItemOption(107, 12));
                itemReward3.itemOptions.add(new ItemOption(107, 12));
                itemReward4.itemOptions.add(new ItemOption(107, 12));

                itemReward.itemOptions.add(new ItemOption(30, 1));
                itemReward1.itemOptions.add(new ItemOption(30, 1));
                itemReward2.itemOptions.add(new ItemOption(30, 1));
                itemReward3.itemOptions.add(new ItemOption(30, 1));
                itemReward4.itemOptions.add(new ItemOption(30, 1));

                InventoryServiceNew.gI().addItemBag(player, itemReward);
                InventoryServiceNew.gI().addItemBag(player, itemReward1);
                InventoryServiceNew.gI().addItemBag(player, itemReward2);
                InventoryServiceNew.gI().addItemBag(player, itemReward3);
                InventoryServiceNew.gI().addItemBag(player, itemReward4);

                Service.gI().sendThongBao(player, "Bạn đã nhận được set đồ 10 sao !");
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
            }
        } else {
            Item itemReward = ItemService.gI().createNewItem((short) 2);
            Item itemReward1 = ItemService.gI().createNewItem((short) 8);
            Item itemReward2 = ItemService.gI().createNewItem((short) 12);
            Item itemReward3 = ItemService.gI().createNewItem((short) 23);
            Item itemReward4 = ItemService.gI().createNewItem((short) 29);
            itemReward.quantity = 1;
            itemReward1.quantity = 1;
            itemReward2.quantity = 1;
            itemReward3.quantity = 1;
            itemReward4.quantity = 1;
            if (InventoryServiceNew.gI().getCountEmptyBag(player) > 4) {
                itemReward.itemOptions.add(new ItemOption(47, 5));
                itemReward1.itemOptions.add(new ItemOption(7, 30));
                itemReward2.itemOptions.add(new ItemOption(14, 1));
                itemReward3.itemOptions.add(new ItemOption(0, 5));
                itemReward4.itemOptions.add(new ItemOption(6, 30));

                itemReward.itemOptions.add(new ItemOption(107, 12));
                itemReward1.itemOptions.add(new ItemOption(107, 12));
                itemReward2.itemOptions.add(new ItemOption(107, 12));
                itemReward3.itemOptions.add(new ItemOption(107, 12));
                itemReward4.itemOptions.add(new ItemOption(107, 12));

                itemReward.itemOptions.add(new ItemOption(30, 1));
                itemReward1.itemOptions.add(new ItemOption(30, 1));
                itemReward2.itemOptions.add(new ItemOption(30, 1));
                itemReward3.itemOptions.add(new ItemOption(30, 1));
                itemReward4.itemOptions.add(new ItemOption(30, 1));

                InventoryServiceNew.gI().addItemBag(player, itemReward);
                InventoryServiceNew.gI().addItemBag(player, itemReward1);
                InventoryServiceNew.gI().addItemBag(player, itemReward2);
                InventoryServiceNew.gI().addItemBag(player, itemReward3);
                InventoryServiceNew.gI().addItemBag(player, itemReward4);

                Service.gI().sendThongBao(player, "Bạn đã nhận được set đồ 10 sao !");
                InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                InventoryServiceNew.gI().sendItemBags(player);
            } else {
                Service.gI().sendThongBao(player, "Bạn phải có ít nhất 5 ô trống hành trang");
            }
        }

    }

    public void hopThanLinh(Player player, Item item) {
        byte randomDo = (byte) new Random().nextInt(Manager.itemIds_TL.length);
        Item thanlinh = Util.randomthanlinh(Manager.itemIds_TL[randomDo]);
        short[] icon = new short[2];
        icon[0] = item.template.iconID;
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 1) {
            InventoryServiceNew.gI().addItemBag(player, thanlinh);
            Service.gI().sendThongBao(player, "Bạn đã nhận được " + thanlinh.template.name);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
            icon[1] = thanlinh.template.iconID;
            InventoryServiceNew.gI().sendItemBags(player);
            CombineServiceNew.gI().sendEffectOpenItem(player, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void openCSH(Player pl, Item item) {
        if (InventoryServiceNew.gI().getCountEmptyBag(pl) > 0) {
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            icon[1] = 7743;
            pl.inventory.ruby += Util.nextInt(70, 150);
            if (pl.inventory.ruby > 2000000000) {
                pl.inventory.ruby = 2000000000;
            }
            PlayerService.gI().sendInfoHpMpMoney(pl);
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
            InventoryServiceNew.gI().sendItemBags(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);
        } else {
            Service.gI().sendThongBao(pl, "Hàng trang đã đầy");
        }
    }

    private void openWoodChest(Player pl, Item item) {
        int time = (int) TimeUtil.diffDate(new Date(), new Date(item.createTime), TimeUtil.DAY);
        if (time != 0) {
            Item itemReward;
            int param = pl.inventory.getParam(item, 72);
            short[] temp = {1079, 722};
            int[][] gold = {{1000, 2000}};
            byte index = (byte) Util.nextInt(0, temp.length - 1);
            short[] icon = new short[2];
            icon[0] = item.template.iconID;
            if (param < 9) {
                pl.inventory.ruby += Util.nextInt(gold[0][0], gold[0][1]);
                if (pl.inventory.ruby > 2000000000) {
                    pl.inventory.ruby = 2000000000;
                }
                PlayerService.gI().sendInfoHpMpMoney(pl);
                icon[1] = 7743;
            } else if (param == 9 || param == 10) {
                itemReward = ItemService.gI().createNewItem((short) 861);
                itemReward.quantity = Util.nextInt(2000, 5000);
                InventoryServiceNew.gI().addItemBag(pl, itemReward);
                icon[1] = itemReward.template.iconID;
            }
            if (param == 11) {
                Item it = ItemService.gI().createNewItem(temp[index]);
                it.quantity = Util.nextInt(50, 100);
                it.itemOptions.add(new ItemOption(73, 0));
                InventoryServiceNew.gI().addItemBag(pl, it);
                icon[1] = it.template.iconID;
            }
            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
//        pl.inventory.addGold(gold);
//        InventoryServiceNew.gI().sendItemBags(pl);
            PlayerService.gI().sendInfoHpMpMoney(pl);
            CombineServiceNew.gI().sendEffectOpenItem(pl, icon[0], icon[1]);

        } else {
            Service.gI().sendThongBao(pl, "Vui lòng đợi 24h");
        }
    }

    private int randClothes(int level) {
        return LIST_ITEM_CLOTHES[Util.nextInt(0, 2)][Util.nextInt(0, 4)][level - 1];
    }

    private void useItemTime(Player pl, Item item) {
        switch (item.template.id) {
            case 382 -> {
                pl.itemTime.lastTimeBoHuyet = System.currentTimeMillis();
                pl.itemTime.isUseBoHuyet = true;
            }
            case 383 -> {
                pl.itemTime.lastTimeBoKhi = System.currentTimeMillis();
                pl.itemTime.isUseBoKhi = true;
            }
            case 384 -> {
                pl.itemTime.lastTimeGiapXen = System.currentTimeMillis();
                pl.itemTime.isUseGiapXen = true;
            }
            case 381 -> {
                pl.itemTime.lastTimeCuongNo = System.currentTimeMillis();
                pl.itemTime.isUseCuongNo = true;
                Service.gI().point(pl);
            }
            case 385 -> {
                pl.itemTime.lastTimeAnDanh = System.currentTimeMillis();
                pl.itemTime.isUseAnDanh = true;
            }
            case 379 -> {
                pl.itemTime.lastTimeUseMayDo = System.currentTimeMillis();
                pl.itemTime.isUseMayDo = true;
            }
            case 1317 -> {
                pl.itemTimesieucap.lastTimeUseXiMuoi = System.currentTimeMillis();
                pl.itemTimesieucap.isUseXiMuoi = true;
                Service.gI().point(pl);
            }
            case 1099 -> {
                pl.itemTimesieucap.lastTimeCuongNo3 = System.currentTimeMillis();
                pl.itemTimesieucap.isUseCuongNo3 = true;
                Service.gI().point(pl);
            }
            case 1100 -> {
                pl.itemTimesieucap.lastTimeBoHuyet3 = System.currentTimeMillis();
                pl.itemTimesieucap.isUseBoHuyet3 = true;
            }
            case 1102 -> {
                pl.itemTimesieucap.lastTimeBoKhi3 = System.currentTimeMillis();
                pl.itemTimesieucap.isUseBoKhi3 = true;
            }
            case 1101 -> {
                pl.itemTimesieucap.lastTimeGiapXen3 = System.currentTimeMillis();
                pl.itemTimesieucap.isUseGiapXen3 = true;
            }
            case 1103 -> {
                pl.itemTimesieucap.lastTimeAnDanh3 = System.currentTimeMillis();
                pl.itemTimesieucap.isUseAnDanh3 = true;
            }
            case 899 -> {
                pl.itemTimesieucap.lastTimeKeo = System.currentTimeMillis();
                pl.itemTimesieucap.isKeo = true;
            }
            case 465, 466, 472, 473 -> {
                pl.itemTimesieucap.lastTimeUseBanh = System.currentTimeMillis();
                pl.itemTimesieucap.isUseTrungThu = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTimesieucap.iconBanh);
                pl.itemTimesieucap.iconBanh = item.template.iconID;
            }
            case 663, 664, 665, 666, 667 -> {
                pl.itemTimesieucap.lastTimeMeal = System.currentTimeMillis();
                pl.itemTimesieucap.isEatMeal = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTimesieucap.iconMeal);
                pl.itemTimesieucap.iconMeal = item.template.iconID;
            }
            case 579 -> {
                pl.itemTime.lastTimeDuoikhi = System.currentTimeMillis();
                pl.itemTime.isDuoikhi = true;
                ItemTimeService.gI().removeItemTime(pl, pl.itemTime.iconDuoi);
                pl.itemTime.iconDuoi = item.template.iconID;
            }
            case 1201 -> {
                pl.itemTime.lastTimeUseMayDo2 = System.currentTimeMillis();
                pl.itemTime.isUseMayDo2 = true;
            }
        }
        Service.gI().point(pl);
        ItemTimeService.gI().sendAllItemTime(pl);
        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
        InventoryServiceNew.gI().sendItemBags(pl);
    }

    private void controllerCallRongThan(Player pl, Item item) {
        int tempId = item.template.id;
        if (tempId >= SummonDragon.NGOC_RONG_1_SAO && tempId <= SummonDragon.NGOC_RONG_7_SAO) {
            if (tempId == SummonDragon.NGOC_RONG_1_SAO || tempId == SummonDragon.NGOC_RONG_2_SAO) {
                SummonDragon.gI().openMenuSummonShenron(pl, (byte) (tempId - 13));
            } else {
                NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_SUMMON_DRAGON,
                        -1, "Bạn chỉ có thể gọi rồng từ ngọc 2 sao, 1 sao",
                        "Hướng\ndẫn thêm\n(mới)",
                        "OK");
            }
        }
        if (tempId >= GoiRongXuong.XUONG_1_SAO && tempId <= GoiRongXuong.XUONG_7_SAO) {
            if (tempId == GoiRongXuong.XUONG_1_SAO) {
                GoiRongXuong.gI().openMenuRongXuong(pl, (byte) (tempId - 701));
            } else {
                NpcService.gI().createMenuConMeo(pl, ConstNpc.TUTORIAL_RONG_XUONG,
                        -1, "Bạn chỉ có thể gọi rồng từ ngọc 1 sao", "Hướng\ndẫn thêm\n(mới)", "OK");
            }
        }
    }

    private void learnSkill(Player pl, Item item) {
        Message msg;
        try {
            if (item.template.gender == pl.gender || item.template.gender == 3) {
                String[] subName = item.template.name.split("");
                byte level = Byte.parseByte(subName[subName.length - 1]);
                Skill curSkill = SkillUtil.getSkillByItemID(pl, item.template.id);
                if (curSkill == null) {
                    Service.gI().sendThongBao(pl, "Đã xảy ra lỗi, không tìm thấy kỹ năng");
                    return;
                }
                if (curSkill.point == 7) {
                    Service.gI().sendThongBao(pl, "Kỹ năng đã đạt tối đa!");
                } else {
                    if (curSkill.point == 0) {
                        if (level == 1) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 23);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Skill skillNeed = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            Service.gI().sendThongBao(pl, "Vui lòng học " + skillNeed.template.name + " cấp " + skillNeed.point + " trước!");
                        }
                    } else {
                        if (curSkill.point + 1 == level) {
                            curSkill = SkillUtil.createSkill(SkillUtil.getTempSkillSkillByItemID(item.template.id), level);
                            SkillUtil.setSkill(pl, curSkill);
                            InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                            msg = Service.gI().messageSubCommand((byte) 62);
                            msg.writer().writeShort(curSkill.skillId);
                            pl.sendMessage(msg);
                            msg.cleanup();
                        } else {
                            Service.gI().sendThongBao(pl, "Vui lòng học " + curSkill.template.name + " cấp " + (curSkill.point + 1) + " trước!");
                        }
                    }
                    InventoryServiceNew.gI().sendItemBags(pl);
                }
            } else {
                Service.gI().sendThongBao(pl, "Không thể thực hiện");
            }
        } catch (Exception e) {
            Logger.logException(UseItem.class, e);
        }
    }

    private void useTDLT(Player pl, Item item) {
        if (pl.itemTime.isUseTDLT) {
            ItemTimeService.gI().turnOffTDLT(pl, item);
        } else {
            ItemTimeService.gI().turnOnTDLT(pl, item);
        }
    }

    private void usePorata2(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion2(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata3(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion3(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata4(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion4(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void usePorata(Player pl) {
        if (pl.pet == null || pl.fusion.typeFusion == 4) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        } else {
            if (pl.fusion.typeFusion == ConstPlayer.NON_FUSION) {
                pl.pet.fusion(true);
            } else {
                pl.pet.unFusion();
            }
        }
    }

    private void openCapsuleUI(Player pl) {
        pl.iDMark.setTypeChangeMap(ConstMap.CHANGE_CAPSULE);
        ChangeMapService.gI().openChangeMapTab(pl);
    }

    public void choseMapCapsule(Player pl, int index) {
        int zoneId = -1;
        if (index < 0) {
            return;
        }
        Zone zoneChose = pl.mapCapsule.get(index);
        //Kiểm tra số lượng người trong khu

        if (zoneChose.getNumOfPlayers() > 30
                || MapService.gI().isMapDoanhTrai(zoneChose.map.mapId)
                || MapService.gI().isMapMaBu(zoneChose.map.mapId)
                || MapService.gI().isMapHuyDiet(zoneChose.map.mapId)
                || MapService.gI().isMapBanDoKhoBau(zoneChose.map.mapId)
                || MapService.gI().isMapKhiGas(zoneChose.map.mapId)) {
            Service.gI().sendThongBao(pl, "Hiện tại không thể vào được khu!");
            return;
        }
        if (index != 0 || zoneChose.map.mapId == 21
                || zoneChose.map.mapId == 22
                || zoneChose.map.mapId == 23) {
            pl.mapBeforeCapsule = pl.zone;
        } else {
            zoneId = pl.mapBeforeCapsule != null ? pl.mapBeforeCapsule.zoneId : -1;
            pl.mapBeforeCapsule = null;
        }
        ChangeMapService.gI().changeMapBySpaceShip(pl, pl.mapCapsule.get(index).map.mapId, zoneId, -1);
    }

    public void eatPea(Player player) {
        Item pea = null;
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.type == 6) {
                pea = item;
                break;
            }
        }
        if (pea != null) {
            int hpKiHoiPhuc = 0;
            int lvPea = Integer.parseInt(pea.template.name.substring(13));
            for (Item.ItemOption io : pea.itemOptions) {
                if (io.optionTemplate.id == 2) {
                    hpKiHoiPhuc = io.param * 1000;
                    break;
                }
                if (io.optionTemplate.id == 48) {
                    hpKiHoiPhuc = io.param;
                    break;
                }
            }
            player.nPoint.setHp(player.nPoint.hp + hpKiHoiPhuc);
            player.nPoint.setMp(player.nPoint.mp + hpKiHoiPhuc);
            PlayerService.gI().sendInfoHpMp(player);
            Service.gI().sendInfoPlayerEatPea(player);
            if (player.pet != null && player.zone.equals(player.pet.zone) && !player.pet.isDie()) {
                int statima = 100 * lvPea;
                player.pet.nPoint.stamina += (short) statima;
                if (player.pet.nPoint.stamina > player.pet.nPoint.maxStamina) {
                    player.pet.nPoint.stamina = player.pet.nPoint.maxStamina;
                }
                player.pet.nPoint.setHp(player.pet.nPoint.hp + hpKiHoiPhuc);
                player.pet.nPoint.setMp(player.pet.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.pet);
                Service.gI().chatJustForMe(player, player.pet, "Cảm ơn sư phụ đã cho con đậu thần");
            }
            if (player.thuTrieuHoi != null && player.zone.equals(player.thuTrieuHoi.zone) && !player.thuTrieuHoi.isDie()) {
                player.thuTrieuHoi.nPoint.setHp(player.thuTrieuHoi.nPoint.hp + hpKiHoiPhuc);
                player.thuTrieuHoi.nPoint.setMp(player.thuTrieuHoi.nPoint.mp + hpKiHoiPhuc);
                Service.gI().sendInfoPlayerEatPea(player.thuTrieuHoi);
                Service.gI().chatJustForMe(player, player.thuTrieuHoi, "Đa tạ Chủ Thượng");
            }

            InventoryServiceNew.gI().subQuantityItemsBag(player, pea, 1);
            InventoryServiceNew.gI().sendItemBags(player);
        }
    }

    private void upSkillPet(Player pl, Item item) {
        if (pl.pet == null) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
            return;
        }
        try {
            switch (item.template.id) {
                case 402 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 0)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 403 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 1)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 404 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 2)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
                case 759 -> {
                    if (SkillUtil.upSkillPet(pl.pet.playerSkill.skills, 3)) {
                        Service.gI().chatJustForMe(pl, pl.pet, "Cảm ơn sư phụ");
                        InventoryServiceNew.gI().subQuantityItemsBag(pl, item, 1);
                    } else {
                        Service.gI().sendThongBao(pl, "Không thể thực hiện");
                    }
                }
            }
        } catch (Exception e) {
            Service.gI().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void itemSKH(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày", "Rada", "Từ Chối");
    }

    private void itemDHD(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Hãy chọn một món quà", "Áo", "Quần", "Găng", "Giày", "Rada", "Từ Chối");
    }

    private void hopTS(Player pl, Item item) {
        NpcService.gI().createMenuConMeo(pl, item.template.id, -1, "Chọn hành tinh của mày đi", "Set trái đất", "Set namec", "Set xayda", "Từ chổi");
    }

}


