package com.girlkun.services.func;

import com.girlkun.consts.ConstNpc;
import com.girlkun.data.ItemData;
import com.girlkun.models.item.Item;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcManager;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.server.ServerNotify;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.*;
import java.util.stream.Collectors;

public class CombineServiceNew {
    private static final int COST = 500000000;
    private static final byte MAX_STAR_ITEM = 16;
    private static final byte MAX_LEVEL_ITEM = 16;
    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_CHANGE_OPTION = 4;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;
    public static final int NANG_CAP_VAT_PHAM = 1000;
    public static final int NHAP_NGOC_RONG = 1001;
    public static final int NANG_CAP_BONG_TAI = 1002;
    public static final int MO_CHI_SO_BONG_TAI = 1003;
    public static final int EP_SAO_TRANG_BI = 2000;
    public static final int PHA_LE_HOA_TRANG_BI = 2001;
    public static final int TINH_AN = 2002;
    public static final int PHAP_SU_HOA = 2003;
    public static final int TAY_PHAP_SU = 2004;
    public static final int CHAN_MENH = 2005;
    public static final int CHUYEN_HOA_DO_HUY_DIET = 2006;
    public static final int CHUYEN_HOA_SKH = 525;
    public static final int GIA_HAN_VAT_PHAM = 526;
    public static final int PHAN_RA_DO_THAN_LINH = 507;
    public static final int NANG_CAP_DO_TS = 508;
    public static final int NANG_CAP_SKH_VIP = 509;
    private final Npc baHatMit;
    private final Npc whis;
    private final Npc npcthiensu64;
    private static CombineServiceNew i;

    public CombineServiceNew() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.whis = NpcManager.getNpc(ConstNpc.WHIS);
        this.npcthiensu64 = NpcManager.getNpc(ConstNpc.NPC_64);
    }

    public static CombineServiceNew gI() {
        if (i == null) {
            i = new CombineServiceNew();
        }
        return i;
    }

    public void openTabCombine(Player player, int type) {
        player.combineNew.setTypeCombine(type);
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_TAB_COMBINE);
            msg.writer().writeUTF(getTextInfoTabCombine(type));
            msg.writer().writeUTF(getTextTopTabCombine(type));
            if (player.iDMark.getNpcChose() != null) {
                msg.writer().writeShort(player.iDMark.getNpcChose().tempId);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }

    public void showInfoCombine(Player player, int[] index) {
        player.combineNew.clearItemCombine();
        for (int j : index) {
            player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(j));
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI -> {
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBi = null;
                    Item daPhaLe = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (isItemPhaLeHoa(item)) {
                            trangBi = item;
                        } else if (isDaPhaLe(item)) {
                            daPhaLe = item;
                        }
                    }
                    int star = 0; //sao pha lê đã ép
                    int starEmpty = 0; //lỗ sao pha lê
                    if (trangBi != null && daPhaLe != null) {
                        for (ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id == 102) {
                                star = io.param;
                            } else if (io.optionTemplate.id == 107) {
                                starEmpty = io.param;
                            }
                        }
                        if (star < starEmpty) {
                            player.combineNew.gemCombine = getGemEpSao(star);
                            StringBuilder npcSay = new StringBuilder(trangBi.template.name + "\n|2|");
                            for (ItemOption io : trangBi.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                            }
                            if (daPhaLe.template.type == 30) {
                                for (ItemOption io : daPhaLe.itemOptions) {
                                    npcSay.append("|7|").append(io.getOptionString()).append("\n");
                                }
                            } else {
                                npcSay.append("|7|").append(ItemService.gI().getItemOptionTemplate(getOptionDaPhaLe(daPhaLe)).name.replaceAll("#", getParamDaPhaLe(daPhaLe) + "")).append("\n");
                            }
                            npcSay.append("|1|Cần ").append(Util.numberToMoney(player.combineNew.gemCombine)).append(" ngọc");
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");

                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                    "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 trang bị có lỗ sao pha lê và 1 loại đá pha lê để ép vào", "Đóng");
                }
            }
            case PHA_LE_HOA_TRANG_BI -> {
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (isItemPhaLeHoa(item)) {
                        int star = 0;
                        for (ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 107) {
                                star = io.param;
                                break;
                            }
                        }
                        if (star < MAX_STAR_ITEM) {
                            player.combineNew.goldCombine = getGoldPhaLeHoa(star);
                            player.combineNew.gemCombine = getGemPhaLeHoa(star);
                            player.combineNew.ratioCombine = getRatioPhaLeHoa(star);

                            StringBuilder npcSay = new StringBuilder(item.template.name + "\n|2|");
                            for (ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                            }
                            npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.ratioCombine).append("%").append("\n");
                            if (player.combineNew.goldCombine <= player.inventory.gold) {
                                npcSay.append("|1|Cần ").append(Util.numberToMoney(player.combineNew.goldCombine)).append(" vàng");
                                baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                        "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                            } else {
                                npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold)).append(" vàng");
                                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                            }

                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm đã đạt tối đa sao pha lê", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể đục lỗ", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy hãy chọn 1 vật phẩm để pha lê hóa", "Đóng");
                }
            }
            case NHAP_NGOC_RONG -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 1) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        if (item != null) {
                            if (item.isNotNullItem() && item.template.id > 14 && item.template.id <= 20 && item.quantity >= 7) {
                                String npcSay = "|2|Con có muốn biến 7 viên " + item.template.name.toLowerCase() + "\n"
                                        + "thành 1 viên " + ItemService.gI().getTemplate((short) (item.template.id - 1)).name.toLowerCase();
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Đóng");
                            } else {
                                if (item.template.id == 14) {
                                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ngọc rồng 1 sao là cao nhất rồi", "Đóng");
                                } else {
                                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 7 viên " + item.template.name + " trở lên", "Đóng");
                                }
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Đã xảy ra lỗi, hãy thử thoát game vào lại", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ để 1 vật phẩm là ngọc rồng từ 2 sao đến 7 sao", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
            case MO_CHI_SO_BONG_TAI -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 3) {
                        Item bongTai = null;
                        Item manhHon = null;
                        Item daXanhLam = null;
                        for (Item item : player.combineNew.itemsCombine) {
                            if (item.isPotara()) {
                                bongTai = item;
                            }
                            if (item.isMHBT()) {
                                manhHon = item;
                            }
                            if (item.isDXL()) {
                                daXanhLam = item;
                            }
                        }
                        if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 999) {
                            player.combineNew.goldCombine = 500_000_000;
                            player.combineNew.rubyCombine = 500;
                            player.combineNew.ratioCombine = 50;
                            StringBuilder npcSay = new StringBuilder(bongTai.template.name + " \n|2|");
                            for (ItemOption io : bongTai.itemOptions) {
                                npcSay.append(io.getOptionString()).append("\n");
                            }
                            npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.ratioCombine).append("%").append("\n");
                            if (player.combineNew.goldCombine <= player.inventory.gold) {
                                if (player.combineNew.rubyCombine <= player.inventory.ruby) {
                                    npcSay.append("|1|Cần ").append(Util.numberToMoney(player.combineNew.goldCombine)).append(" vàng\n");
                                    npcSay.append("|1|Cần ").append(player.combineNew.rubyCombine).append(" hồng ngọc\n");
                                    npcSay.append("|1|Cần ").append("999 Mảnh hồn bông tai\n");
                                    npcSay.append("|1|Cần ").append("1 Đá xanh lam");
                                    baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                            "Nâng cấp", "Đóng");
                                } else {
                                    npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.rubyCombine - player.inventory.ruby)).append(" hồng ngọc");
                                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                                }
                            } else {
                                npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold)).append(" vàng");
                                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh hồn bông tai, 1 Đá xanh lam", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh hồn bông tai, 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
            case TINH_AN -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isItemAn(item)) {
                            if (item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1232 || dangusac.template.id == 1233 || dangusac.template.id == 1234) && dangusac.quantity >= 99) {
                                StringBuilder npcSay = new StringBuilder(item.template.name + "\n|2|");
                                for (ItemOption io : item.itemOptions) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                                npcSay.append("|1|Con có muốn biến trang bị ").append(item.template.name).append(" thành\n").append("trang bị Ấn không?\b|4|Đục là lên\n").append("|7|Cần 99 ").append(dangusac.template.name);
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(), "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể hóa ấn", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
            case NANG_CAP_VAT_PHAM -> {
                if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.template.type < 5)) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.template.type == 14)) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.template.id == 987)) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    Item itemDo = null;
                    Item itemDNC = null;
                    Item itemDBV = null;
                    for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                        if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                                itemDBV = player.combineNew.itemsCombine.get(j);
                                continue;
                            }
                            if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                                itemDo = player.combineNew.itemsCombine.get(j);
                            } else {
                                itemDNC = player.combineNew.itemsCombine.get(j);
                            }
                        }
                    }
                    if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                        int level = 0;
                        for (ItemOption io : itemDo.itemOptions) {
                            if (io.optionTemplate.id == 72) {
                                level = io.param;
                                break;
                            }
                        }
                        if (level < MAX_LEVEL_ITEM) {
                            player.combineNew.goldCombine = getGoldNangCapDo(level);
                            player.combineNew.ratioCombine = (float) getTileNangCapDo(level);
                            player.combineNew.countDaNangCap = getCountDaNangCapDo(level);
                            player.combineNew.countDaBaoVe = (short) getCountDaBaoVe(level);
                            StringBuilder npcSay = new StringBuilder("|2|Hiện tại " + itemDo.template.name + " (+" + level + ")\n|0|");
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                            }
                            String option = null;
                            int param = 0;
                            for (ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id == 47
                                        || io.optionTemplate.id == 6
                                        || io.optionTemplate.id == 0
                                        || io.optionTemplate.id == 7
                                        || io.optionTemplate.id == 14
                                        || io.optionTemplate.id == 22
                                        || io.optionTemplate.id == 23) {
                                    option = io.optionTemplate.name;
                                    param = io.param + (io.param * 10 / 100);
                                    break;
                                }
                            }
                            if (option != null) {
                                npcSay.append("|2|Sau khi nâng cấp (+").append(level + 1).append(")\n|7|").append(option.replaceAll("#", String.valueOf(param))).append("\n|7|Tỉ lệ thành công: ").append(player.combineNew.ratioCombine).append("%\n").append(player.combineNew.countDaNangCap > itemDNC.quantity ? "|7|" : "|1|").append("Cần ").append(player.combineNew.countDaNangCap).append(" ").append(itemDNC.template.name).append("\n").append(player.combineNew.goldCombine > player.inventory.gold ? "|7|" : "|1|").append("Cần ").append(Util.numberToMoney(player.combineNew.goldCombine)).append(" vàng");
                            }

                            String daNPC = player.combineNew.itemsCombine.size() == 3 && itemDBV != null ? String.format("\nCần tốn %s đá bảo vệ", player.combineNew.countDaBaoVe) : "";
                            if ((level == 2 || level == 4 || level == 6) && !(player.combineNew.itemsCombine.size() == 3 && itemDBV != null)) {
                                npcSay.append("\nNếu thất bại sẽ rớt xuống (+").append(level - 1).append(")");
                            }
                            if (player.combineNew.countDaNangCap > itemDNC.quantity) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay.toString(), "Còn thiếu\n" + (player.combineNew.countDaNangCap - itemDNC.quantity) + " " + itemDNC.template.name);
                            } else if (player.combineNew.goldCombine > player.inventory.gold) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay.toString(), "Còn thiếu\n" + Util.numberToMoney((player.combineNew.goldCombine - player.inventory.gold)) + " vàng");
                            } else if (player.combineNew.itemsCombine.size() == 3 && Objects.nonNull(itemDBV) && itemDBV.quantity < player.combineNew.countDaBaoVe) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay.toString(), "Còn thiếu\n" + (player.combineNew.countDaBaoVe - itemDBV.quantity) + " đá bảo vệ");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                        npcSay.toString(), "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng" + daNPC, "Từ chối");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Trang bị của ngươi đã đạt cấp tối đa", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                    }
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        break;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị và 1 loại đá nâng cấp", "Đóng");
                }
            }
            case CHAN_MENH -> {
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongTai = null;
                    Item manhVo = null;
                    int star = 0;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 1318) {
                            manhVo = item;
                        }
                        if (item.template.id >= 1300 && item.template.id <= 1308) {
                            bongTai = item;
                            star = item.template.id - 1300;
                        }
                    }
                    if (bongTai != null && bongTai.template.id == 1308) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Chân Mệnh đã đạt cấp tối đa", "Đóng");
                        return;
                    }
                    player.combineNew.diemNangCap = getPointChanMenh(star);
                    player.combineNew.daNangCap = getDNCChanMenh(star);
                    player.combineNew.tiLeNangCap = getTileChanMenh(star);
                    if (bongTai != null && manhVo != null) {
                        StringBuilder npcSay = new StringBuilder(bongTai.template.name + "\n|2|");
                        for (ItemOption io : bongTai.itemOptions) {
                            npcSay.append(io.getOptionString()).append("\n");
                        }
                        npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.tiLeNangCap).append("%").append("\n");
                        if (player.combineNew.diemNangCap <= player.inventory.event) {
                            npcSay.append("|1|Cần ").append(Util.numberToMoney(player.combineNew.diemNangCap)).append(" Điểm Săn Boss");
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                    "Nâng cấp\ncần " + player.combineNew.daNangCap + " Đá Hoàng Kim");
                        } else {
                            npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.diemNangCap - player.inventory.event)).append(" Điểm");
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                }
            }
            case NANG_CAP_BONG_TAI -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item bongtai = null;
                        Item manhvobt = null;
                        for (Item item : player.combineNew.itemsCombine) {
                            if (item.isPotara()) {
                                bongtai = item;
                            }
                            if (item.isMVBT()) {
                                manhvobt = item;
                            }
                        }
                        if (bongtai != null && manhvobt != null) {
                            int lvbt = bongtai.getLevelBongTai();
                            int countmvbt = lvbt * 1000;
                            player.combineNew.goldCombine = 500_000_000 * lvbt;
                            player.combineNew.rubyCombine = 500 * lvbt;
                            player.combineNew.ratioCombine = lvbt == 1 ? 40 : lvbt == 2 ? 30 : lvbt == 3 ? 15 : 0;
                            StringBuilder npcSay = new StringBuilder(bongtai.template.name + " \n|2|");
                            for (ItemOption io : bongtai.itemOptions) {
                                npcSay.append(io.getOptionString()).append("\n");
                            }
                            npcSay.append("|7|Tỉ lệ thành công: ").append(player.combineNew.ratioCombine).append("%").append("\n");
                            if (manhvobt.quantity >= countmvbt) {
                                if (player.combineNew.goldCombine <= player.inventory.gold) {
                                    if (player.combineNew.rubyCombine <= player.inventory.ruby) {
                                        npcSay.append("|1|Cần ").append(Util.numberToMoney(player.combineNew.goldCombine)).append(" vàng\n");
                                        npcSay.append("|1|Cần ").append(countmvbt).append(" ").append(manhvobt.template.name);
                                        baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                                "Nâng cấp\ncần " + player.combineNew.rubyCombine + " hồng ngọc");
                                    } else {
                                        npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.rubyCombine - player.inventory.ruby)).append(" hồng ngọc");
                                        baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                                    }
                                } else {
                                    npcSay.append("Còn thiếu ").append(Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold)).append(" vàng");
                                    baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay.toString(), "Đóng");
                                }

                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 1 bông tai Porata, 999 mảnh vỡ bông tai", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 1 bông tai Porata, 999 mảnh vỡ bông tai", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 1 bông tai Porata, 999 mảnh vỡ bông tai", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
            case PHAN_RA_DO_THAN_LINH -> {
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con hãy đưa ta đồ thần linh để phân rã", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 1) {
                    List<Integer> itemdov2 = new ArrayList<>(Arrays.asList(562, 564, 566));
                    int couponAdd = 0;
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (item.isNotNullItem()) {
                        if (item.template.id >= 555 && item.template.id <= 567) {
                            couponAdd = itemdov2.stream().anyMatch(t -> t == item.template.id) ? 2 : item.template.id == 561 ? 3 : 1;
                        }
                    }
                    if (couponAdd == 0) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể phân rã đồ thần linh thôi", "Đóng");
                        return;
                    }
                    String npcSay = "|2|Sau khi phân rải vật phẩm\n|7|"
                            + "Bạn sẽ nhận được : " + couponAdd + " Điểm\n"
                            + (500000000 > player.inventory.gold ? "|7|" : "|1|")
                            + "Cần " + Util.numberToMoney(500000000) + " vàng";

                    if (player.inventory.gold < 500000000) {
                        this.baHatMit.npcChat(player, "Hết tiền rồi\nẢo ít thôi con");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_PHAN_RA_DO_THAN_LINH,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(500000000) + " vàng", "Từ chối");
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể phân rã 1 lần 1 món đồ thần linh", "Đóng");
                }
            }
            case CHUYEN_HOA_DO_HUY_DIET -> {
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Con hãy đưa ta đồ Hủy diệt", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 1) {
                    int huydietok = 0;
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (item.isNotNullItem()) {
                        if (item.template.id >= 650 && item.template.id <= 662) {
                            huydietok = 1;
                        }
                    }
                    if (huydietok == 0) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa đồ Hủy diệt thôi", "Đóng");
                        return;
                    }
                    String npcSay = "|2|Sau khi chuyển hóa vật phẩm\n|7|"
                            + "Bạn sẽ nhận được : 1 " + " Phiếu Hủy diệt Tương ứng\n"
                            + (500000000 > player.inventory.gold ? "|7|" : "|1|")
                            + "Cần " + Util.numberToMoney(500000000) + " vàng";

                    if (player.inventory.gold < 500000000) {
                        this.baHatMit.npcChat(player, "Hết tiền rồi\nẢo ít thôi con");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_CHUYEN_HOA_DO_HUY_DIET,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(500000000) + " vàng", "Từ chối");
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Ta chỉ có thể chuyển hóa 1 lần 1 món đồ Hủy diệt", "Đóng");
                }
            }
            case NANG_CAP_DO_TS -> {
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 2 món Hủy Diệt bất kì và 1 món Thần Linh cùng loại", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.isCongThuc())) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh Công thức", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.template.id == 1083)) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá cầu vòng", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999)) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh thiên sứ", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                            + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isManhTS).findFirst().get().typeNameManh() + " thiên sứ tương ứng\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    if (player.inventory.gold < COST) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.whis.createOtherMenu(player, ConstNpc.MENU_NANG_CAP_DO_TS,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        return;
                    }
                    this.whis.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
            }
            case NANG_CAP_SKH_VIP -> {
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 1 món thiên sứ và 2 món SKH ngẫu nhiên", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 4) {
                    if (player.combineNew.itemsCombine.stream().noneMatch(item -> item.isNotNullItem() && item.isDHD())) {
                        this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ hủy diệt", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isSKH()).count() < 2) {
                        this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ kích hoạt ", "Đóng");
                        return;
                    }
                    Item dangusac = player.combineNew.itemsCombine.get(3);
                    if (dangusac.isNotNullItem() && dangusac.template.id == 674 && dangusac.quantity >= 1) {

                        String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                                + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().get().typeName() + " kích hoạt VIP tương ứng\n"
                                + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                        if (player.inventory.gold < COST) {
                            this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                            return;
                        }
                        this.npcthiensu64.createOtherMenu(player, ConstNpc.MENU_NANG_DOI_SKH_VIP,
                                npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                    } else {

                        this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá ngũ sắc ", "Đóng");
                    }
                } else {
                    if (player.combineNew.itemsCombine.size() > 4) {
                        this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Nguyên liệu không phù hợp", "Đóng");
                        return;
                    }
                    this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
            }
            case CHUYEN_HOA_SKH -> {
                if (player.combineNew.itemsCombine.isEmpty()) {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 3 món Thần linh", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count() < 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ Thần linh", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                            + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isDTL).findFirst().get().typeName() + " kích hoạt Thường tương ứng\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    if (player.inventory.gold < COST) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.MENU_RANDOM_SKH,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Dư vật phẩm rồi", "Đóng");
                        return;
                    }
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
            }
            case GIA_HAN_VAT_PHAM -> {
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item thegh = null;
                    Item itemGiahan = null;
                    for (Item item_ : player.combineNew.itemsCombine) {
                        if (item_.template.id == 1346) {
                            thegh = item_;
                        } else if (item_.isTrangBiHSD()) {
                            itemGiahan = item_;
                        }
                    }
                    if (thegh == null) {
                        Service.getInstance().sendThongBaoOK(player, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                        return;
                    }
                    if (itemGiahan == null) {
                        Service.getInstance().sendThongBaoOK(player, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                        return;
                    }
                    for (ItemOption itopt : itemGiahan.itemOptions) {
                        if (itopt.optionTemplate.id == 93) {
                            if (itopt.param < 0) {
                                Service.getInstance().sendThongBaoOK(player, "Trang bị này không phải trang bị có Hạn Sử Dụng");
                                return;
                            }
                        }
                    }
                    StringBuilder npcSay = new StringBuilder("Trang bị được gia hạn \"" + itemGiahan.template.name + "\"");
                    npcSay.append(itemGiahan.template.name).append("\n|2|");
                    for (ItemOption io : itemGiahan.itemOptions) {
                        npcSay.append(io.getOptionString()).append("\n");
                    }
                    npcSay.append("\n|0|Sau khi gia hạn +1 ngày\n");

                    npcSay.append("|0|Tỉ lệ thành công: 100%" + "\n");
                    if (player.inventory.gold > 200000000) {
                        npcSay.append("|2|Cần 200Tr vàng");
                        this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(),
                                "Nâng cấp", "Từ chối");

                    } else if (player.inventory.gold < 200000000) {
                        int SoVangThieu2 = (int) (200000000 - player.inventory.gold);
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn còn thiếu " + SoVangThieu2 + " vàng");
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 1 trang bị có hạn sử dụng và 1 phiếu Gia hạn");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống");
                }
            }
            case PHAP_SU_HOA -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isItemPhapSu(item)) {
                            if (item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && dangusac.template.id == 1235 && dangusac.quantity >= 1) {
                                StringBuilder npcSay = new StringBuilder(item.template.name + "\n|2|");
                                for (ItemOption io : item.itemOptions) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                                npcSay.append("|1|Con có muốn biến trang bị ").append(item.template.name).append(" thành\n").append("trang bị Pháp sư hóa không?\n").append("|7|Cần 1 ").append(dangusac.template.name);
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(), "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể hóa ấn", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
            case TAY_PHAP_SU -> {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isItemPhapSu(item)) {
                            if (item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && dangusac.template.id == 1236 && dangusac.quantity >= 1) {
                                StringBuilder npcSay = new StringBuilder(item.template.name + "\n|2|");
                                for (ItemOption io : item.itemOptions) {
                                    npcSay.append(io.getOptionString()).append("\n");
                                }
                                npcSay.append("|1|Con có muốn tẩy trang bị ").append(item.template.name).append(" về\n").append("lúc chưa Pháp sư hóa không?\n").append("|7|Cần 1 ").append(dangusac.template.name);
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay.toString(), "Làm phép", "Từ chối");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Bạn chưa bỏ đủ vật phẩm !!!", "Đóng");
                            }
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Vật phẩm này không thể thực hiện", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần bỏ đủ vật phẩm yêu cầu", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
            }
        }
    }

    public void startCombine(Player player) {
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI -> epSaoTrangBi(player);
            case PHA_LE_HOA_TRANG_BI -> phaLeHoaTrangBi(player);
            case NHAP_NGOC_RONG -> nhapNgocRong(player);
            case TINH_AN -> anTrangBi(player);
            case PHAN_RA_DO_THAN_LINH -> phanRaDTL(player);
            case CHUYEN_HOA_DO_HUY_DIET -> chuyenHoaDHD(player);
            case NANG_CAP_DO_TS -> nangCapDTS(player);
            case NANG_CAP_SKH_VIP -> nangCapSKHVIP(player);
            case CHUYEN_HOA_SKH -> randomSKH(player);
            case NANG_CAP_VAT_PHAM -> nangCapVatPham(player);
            case NANG_CAP_BONG_TAI -> nangCapBongTai(player);
            case MO_CHI_SO_BONG_TAI -> moChiSoBongTai(player);
            case PHAP_SU_HOA -> addPhapSu(player);
            case TAY_PHAP_SU -> removePhapSu(player);
            case CHAN_MENH -> nangCapChanMenh(player);
            case GIA_HAN_VAT_PHAM -> giaHanTrangBi(player);
        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();
    }

    public void moChiSoBongTai(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 3) {
                int gold = player.combineNew.goldCombine;
                if (player.inventory.gold < gold) {
                    Service.gI().sendThongBao(player, "Còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng");
                    return;
                }
                int ruby = player.combineNew.gemCombine;
                if (player.inventory.ruby < ruby) {
                    Service.gI().sendThongBao(player, "Còn thiếu " + Util.formatNumberMoney(ruby - player.inventory.ruby) + " hồng ngọc");
                    return;
                }
                Item bongTai = null;
                Item manhHon = null;
                Item daXanhLam = null;
                for (Item item : player.combineNew.itemsCombine) {
                    if (item.isPotara()) {
                        bongTai = item;
                    }
                    if (item.isMHBT()) {
                        manhHon = item;
                    }
                    if (item.isDXL()) {
                        daXanhLam = item;
                    }
                }
                if (manhHon != null && bongTai != null && daXanhLam != null && manhHon.quantity >= 999) {
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        bongTai.itemOptions.clear();
                        switch (bongTai.template.id) {
                            case 454 -> bongTai.itemOptions.add(new ItemOption(72, 1));
                            case 921 -> bongTai.itemOptions.add(new ItemOption(72, 2));
                            case 1165 -> bongTai.itemOptions.add(new ItemOption(72, 3));
                            case 1129 -> bongTai.itemOptions.add(new ItemOption(72, 4));
                        }
                        int rdUp = 0;
                        switch (player.gender) {
                            case 0, 1 -> {
                                if (Util.isTrue(80, 100)) rdUp = Util.nextInt(1, 5);
                            }
                            default -> rdUp = Util.isTrue(20, 100) ? 5 : Util.nextInt(0, 4);
                        }

                        switch (rdUp) {
                            case 0 -> bongTai.itemOptions.add(new ItemOption(50, Util.nextInt(5, 15))); // dame
                            case 1 -> bongTai.itemOptions.add(new ItemOption(14, Util.nextInt(5, 10))); // chi mang
                            case 2 -> bongTai.itemOptions.add(new ItemOption(103, Util.nextInt(5, 20))); // ki
                            case 3 -> bongTai.itemOptions.add(new ItemOption(108, Util.nextInt(5, 15))); // ne
                            case 4 -> bongTai.itemOptions.add(new ItemOption(94, Util.nextInt(5, 15))); // giap
                            case 5 -> bongTai.itemOptions.add(new ItemOption(77, Util.nextInt(5, 20))); // hp
                        }
                        sendEffectSuccessCombine(player);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    player.inventory.gold -= gold;
                    player.inventory.ruby -= ruby;
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhHon, 999);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, daXanhLam, 1);
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                    reOpenItemCombine(player);
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh hồn bông tai, 1 Đá xanh lam", "Đóng");
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh hồn bông tai, 1 Đá xanh lam", "Đóng");
            }
        } else {
            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }


    private void phanRaDTL(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            player.inventory.gold -= 500000000;
            List<Integer> itemdov2 = new ArrayList<>(Arrays.asList(562, 564, 566));
            Item item = player.combineNew.itemsCombine.get(0);
            int couponAdd = itemdov2.stream().anyMatch(t -> t == item.template.id) ? 2 : item.template.id == 561 ? 3 : 1;
            sendEffectSuccessCombine(player);
            player.inventory.coupon += couponAdd;
            this.baHatMit.npcChat(player, "Con đã nhận được " + couponAdd + " điểm");
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
            player.combineNew.itemsCombine.clear();
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        }
    }

    private void chuyenHoaDHD(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            player.inventory.gold -= 500000000;
            Item item = player.combineNew.itemsCombine.get(0);
            Item phieu = switch (item.template.id) {
                case 650, 652, 654 -> ItemService.gI().createNewItem((short) 1327);
                case 651, 653, 655 -> ItemService.gI().createNewItem((short) 1328);
                case 657, 659, 661 -> ItemService.gI().createNewItem((short) 1329);
                case 658, 660, 662 -> ItemService.gI().createNewItem((short) 1330);
                default -> ItemService.gI().createNewItem((short) 1331);
            };
            sendEffectSuccessCombine(player);
            this.baHatMit.npcChat(player, "Con đã nhận được 1 " + phieu.template.name);
            InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
            player.combineNew.itemsCombine.clear();
            InventoryServiceNew.gI().addItemBag(player, phieu);
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        }
    }

    public void nangCapDTS(Player player) {
        //check sl đồ tl, đồ hd
        // new update 2 mon huy diet + 1 mon than linh(skh theo style) +  5 manh bat ki
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ");
            return;
        }
        if (player.inventory.gold < COST) {
            Service.getInstance().sendThongBao(player, "Ảo ít thôi con...");
            return;
        }
        if (InventoryServiceNew.gI().getCountEmptyBag(player) < 1) {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
            return;
        }
        Item itemTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThuc()).findFirst().get();
        Item itemHDs = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1083).findFirst().get();
        Item itemManh = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).findFirst().get();

        player.inventory.gold -= COST;
        sendEffectSuccessCombine(player);
        short[][] itemIds = {{1048, 1051, 1054, 1057, 1060}, {1049, 1052, 1055, 1058, 1061}, {1050, 1053, 1056, 1059, 1062}}; // thứ tự td - 0,nm - 1, xd - 2

        Item itemTS = ItemService.gI().DoThienSu(itemIds[itemTL.template.gender > 2 ? player.gender : itemTL.template.gender][itemManh.typeIdManh()], itemTL.template.gender);
        InventoryServiceNew.gI().addItemBag(player, itemTS);

        InventoryServiceNew.gI().subQuantityItemsBag(player, itemTL, 1);
        InventoryServiceNew.gI().subQuantityItemsBag(player, itemManh, 999);
        InventoryServiceNew.gI().subQuantityItemsBag(player, itemHDs, 1);
        InventoryServiceNew.gI().sendItemBags(player);
        Service.getInstance().sendMoney(player);
        Service.getInstance().sendThongBao(player, "Bạn đã nhận được " + itemTS.template.name);
        player.combineNew.itemsCombine.clear();
        reOpenItemCombine(player);
    }

    public void nangCapSKHVIP(Player player) {
        // 1 thiên sứ + 2 món kích hoạt -- món đầu kh làm gốc
        if (player.combineNew.itemsCombine.size() != 4) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ hủy diệt");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isSKH()).count() != 2) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ kích hoạt");
            return;
        }
        Item dangusac = player.combineNew.itemsCombine.get(3);
        if (dangusac == null && dangusac.template.id == 674 && dangusac.quantity < 1) {
            Service.getInstance().sendThongBao(player, "Thiếu Đá ngũ sắc");
            return;
        }
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            player.inventory.gold -= COST;
            Item itemTS = player.combineNew.itemsCombine.stream().filter(Item::isDHD).findFirst().get();
            List<Item> itemSKH = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isSKH()).collect(Collectors.toList());
            CombineServiceNew.gI().sendEffectOpenItem(player, itemTS.template.iconID, itemTS.template.iconID);
            short itemId;
            if (player.gender == 3 || itemTS.template.type == 4) {
                itemId = Manager.radaSKHVip[Util.nextInt(0, 5)];
                if (player.getSession().bdPlayer > 0 && Util.isTrue(1, (int) (100 / player.getSession().bdPlayer))) {
                    itemId = Manager.radaSKHVip[6];
                }
            } else {
                itemId = Manager.doSKHVip[player.gender][itemTS.template.type][Util.nextInt(0, 5)];
                if (player.getSession().bdPlayer > 0 && Util.isTrue(1, (int) (100 / player.getSession().bdPlayer))) {
                    itemId = Manager.doSKHVip[player.gender][itemTS.template.type][6];
                }
            }
            int skhId = ItemService.gI().randomSKHId(player.gender);
            Item item;
            if (new Item(itemId).isDTL()) {
                item = Util.ratiItemTL(itemId);
                item.itemOptions.add(new Item.ItemOption(skhId, 1));
                item.itemOptions.add(new Item.ItemOption(ItemService.gI().optionIdSKH(skhId), 1));
                item.itemOptions.remove(item.itemOptions.stream().filter(itemOption -> itemOption.optionTemplate.id == 21).findFirst().get());
                item.itemOptions.add(new Item.ItemOption(21, 15));
                item.itemOptions.add(new Item.ItemOption(30, 1));
            } else {
                item = ItemService.gI().itemSKH(itemId, skhId);
            }
            InventoryServiceNew.gI().addItemBag(player, item);
            InventoryServiceNew.gI().subQuantityItemsBag(player, itemTS, 1);

            InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 1);
            itemSKH.forEach(i -> InventoryServiceNew.gI().subQuantityItemsBag(player, i, 1));
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    public void randomSKH(Player player) {
        // 1 thiên sứ + 2 món kích hoạt -- món đầu kh làm gốc
        if (player.combineNew.itemsCombine.size() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).count() != 3) {
            Service.getInstance().sendThongBao(player, "Thiếu đồ Thần linh");
            return;
        }
        Item montldau = player.combineNew.itemsCombine.get(0);
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            if (player.inventory.gold < 1) {
                Service.getInstance().sendThongBao(player, "Con cần thêm vàng để đổi...");
                return;
            }
            player.inventory.gold -= COST;
            List<Item> itemDTL = player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDTL()).collect(Collectors.toList());
            CombineServiceNew.gI().sendEffectOpenItem(player, montldau.template.iconID, montldau.template.iconID);
            short itemId;
            if (player.gender == 3 || montldau.template.type == 4) {
                itemId = Manager.radaSKHThuong[0];
            } else {
                itemId = Manager.doSKHThuong[player.gender][montldau.template.type];
            }
            int skhId = ItemService.gI().randomSKHId(player.gender);
            Item item = ItemService.gI().itemSKH(itemId, skhId);
            InventoryServiceNew.gI().addItemBag(player, item);
            itemDTL.forEach(i -> InventoryServiceNew.gI().subQuantityItemsBag(player, i, 1));
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            player.combineNew.itemsCombine.clear();
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void giaHanTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() != 2) {
            Service.getInstance().sendThongBao(player, "Thiếu nguyên liệu");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isTrangBiHSD()).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu trang bị HSD");
            return;
        }
        if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1346).count() != 1) {
            Service.getInstance().sendThongBao(player, "Thiếu Bùa Gia Hạn");
            return;
        }
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            Item thegh = player.combineNew.itemsCombine.stream().filter(item -> item.template.id == 1346).findFirst().get();
            Item tbiHSD = player.combineNew.itemsCombine.stream().filter(Item::isTrangBiHSD).findFirst().get();
            if (thegh == null) {
                Service.getInstance().sendThongBao(player, "Thiếu Bùa Gia Hạn");
                return;
            }
            if (tbiHSD == null) {
                Service.getInstance().sendThongBao(player, "Thiếu trang bị HSD");
                return;
            }
            if (tbiHSD != null) {
                for (ItemOption itopt : tbiHSD.itemOptions) {
                    if (itopt.optionTemplate.id == 93) {
                        if (itopt.param < 0 || itopt == null) {
                            Service.getInstance().sendThongBao(player, "Không Phải Trang Bị Có HSD");
                            return;
                        }
                    }
                }
            }
            if (Util.isTrue(100, 100)) {
                sendEffectSuccessCombine(player);
                for (ItemOption itopt : tbiHSD.itemOptions) {
                    if (itopt.optionTemplate.id == 93) {
                        itopt.param += 1;
                        break;
                    }
                }
            } else {
                sendEffectFailCombine(player);
            }
            InventoryServiceNew.gI().subQuantityItemsBag(player, thegh, 1);
            InventoryServiceNew.gI().sendItemBags(player);
            Service.getInstance().sendMoney(player);
            reOpenItemCombine(player);
        } else {
            Service.getInstance().sendThongBao(player, "Bạn phải có ít nhất 1 ô trống hành trang");
        }
    }

    private void epSaoTrangBi(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item trangBi = null;
            Item daPhaLe = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (isItemPhaLeHoa(item)) {
                    trangBi = item;
                } else if (isDaPhaLe(item)) {
                    daPhaLe = item;
                }
            }
            int star = 0; //sao pha lê đã ép
            int starEmpty = 0; //lỗ sao pha lê
            if (trangBi != null && daPhaLe != null) {
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : trangBi.itemOptions) {
                    if (io.optionTemplate.id == 102) {
                        star = io.param;
                        optionStar = io;
                    } else if (io.optionTemplate.id == 107) {
                        starEmpty = io.param;
                    }
                }
                if (star < starEmpty) {
                    player.inventory.gem -= gem;
                    int optionId = getOptionDaPhaLe(daPhaLe);
                    int param = getParamDaPhaLe(daPhaLe);
                    Item.ItemOption option = null;
                    for (Item.ItemOption io : trangBi.itemOptions) {
                        if (io.optionTemplate.id == optionId) {
                            option = io;
                            break;
                        }
                    }
                    if (option != null) {
                        option.param += param;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(optionId, param));
                    }
                    if (optionStar != null) {
                        optionStar.param++;
                    } else {
                        trangBi.itemOptions.add(new Item.ItemOption(102, 1));
                    }

                    InventoryServiceNew.gI().subQuantityItemsBag(player, daPhaLe, 1);
                    sendEffectSuccessCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void phaLeHoaTrangBi(Player player) {
        if (!player.combineNew.itemsCombine.isEmpty()) {
            int gold = player.combineNew.goldCombine;
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gold < gold) {
                Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            } else if (player.inventory.gem < gem) {
                Service.getInstance().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item item = player.combineNew.itemsCombine.get(0);
            if (isItemPhaLeHoa(item)) {
                int star = 0;
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 107) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (star < MAX_STAR_ITEM) {
                    player.inventory.gold -= gold;
                    player.inventory.gem -= gem;
                    byte ratio = (optionStar != null && optionStar.param > 4) ? (byte) 2 : 1;
                    if (Util.isTrue(player.combineNew.ratioCombine, 100 * ratio)) {
                        if (optionStar == null) {
                            item.itemOptions.add(new Item.ItemOption(107, 1));
                        } else {
                            optionStar.param++;
                        }
                        sendEffectSuccessCombine(player);
                        if (optionStar != null && optionStar.param >= 10) {
                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa pha lê hóa "
                                    + "thành công " + item.template.name + " lên " + optionStar.param + " sao pha lê");
                        }
                        if (optionStar != null && optionStar.param >= 1 && optionStar.param <= 2) {
                            item.itemOptions.add(new Item.ItemOption(30, 1));
                        }
                    } else {
                        sendEffectFailCombine(player);
                    }
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.getInstance().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nhapNgocRong(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                if (item != null && item.isNotNullItem() && (item.template.id > 14 && item.template.id <= 20) && item.quantity >= 7) {
                    Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1));
                    InventoryServiceNew.gI().addItemBag(player, nr);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, item, 7);
                    InventoryServiceNew.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                    sendEffectCombineDB(player, item.template.iconID);
                }
            }
        }
    }

    private void anTrangBi(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                Item dangusac = player.combineNew.itemsCombine.get(1);
                int star = 0;
                Item.ItemOption optionStar = null;
                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 34 || io.optionTemplate.id == 35 || io.optionTemplate.id == 35) {
                        star = io.param;
                        optionStar = io;
                        break;
                    }
                }
                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1232 || dangusac.template.id == 1233 || dangusac.template.id == 1234) && dangusac.quantity >= 99) {
                    if (optionStar == null) {
                        if (dangusac.template.id == 1232) {
                            item.itemOptions.add(new Item.ItemOption(34, 1));
                            sendEffectSuccessCombine(player);
                        } else if (dangusac.template.id == 1233) {
                            item.itemOptions.add(new Item.ItemOption(35, 1));
                            sendEffectSuccessCombine(player);
                        } else if (dangusac.template.id == 1234) {
                            item.itemOptions.add(new Item.ItemOption(36, 1));
                            sendEffectSuccessCombine(player);
                        }
//                    InventoryServiceNew.gI().addItemBag(player, item);
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 99);
                        InventoryServiceNew.gI().sendItemBags(player);
                        reOpenItemCombine(player);
//                    sendEffectCombineDB(player, item.template.iconID);
                    } else {
                        Service.getInstance().sendThongBao(player, "Trang bị của bạn có ấn rồi mà !!!");
                    }
                }
            }
        }
    }

    private void moChiSoBongTai2(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item linhthu = null;
            Item thangtinhthach = null;
            Item thucan = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 921) {
                    linhthu = item;
                } else if (item.template.id == 934) {
                    thangtinhthach = item;
                } else if (item.template.id == 935) {
                    thucan = item;
                }
            }
            if (linhthu != null && thangtinhthach != null && thangtinhthach.quantity >= 99) {
                player.inventory.gold -= gold;
                player.inventory.gem -= gem;
                InventoryServiceNew.gI().subQuantityItemsBag(player, thangtinhthach, 99);
                InventoryServiceNew.gI().subQuantityItemsBag(player, thucan, 1);
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    linhthu.itemOptions.clear();
                    linhthu.itemOptions.add(new Item.ItemOption(72, 2));
                    int rdUp = Util.nextInt(0, 7);
                    if (rdUp == 0) {
                        linhthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(5, 15)));
                    } else if (rdUp == 1) {
                        linhthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(5, 15)));
                    } else if (rdUp == 2) {
                        linhthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(5, 15)));
                    } else if (rdUp == 3) {
                        linhthu.itemOptions.add(new Item.ItemOption(108, Util.nextInt(5, 15)));
                    } else if (rdUp == 4) {
                        linhthu.itemOptions.add(new Item.ItemOption(94, Util.nextInt(5, 10)));
                    } else if (rdUp == 5) {
                        linhthu.itemOptions.add(new Item.ItemOption(14, Util.nextInt(5, 10)));
                    } else if (rdUp == 6) {
                        linhthu.itemOptions.add(new Item.ItemOption(80, Util.nextInt(5, 15)));
                    } else if (rdUp == 7) {
                        linhthu.itemOptions.add(new Item.ItemOption(81, Util.nextInt(5, 15)));
                    }
                    sendEffectSuccessCombine(player);
                } else {
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void moChiSoBongTai3(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item linhthu = null;
            Item thangtinhthach = null;
            Item thucan = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 1165) {
                    linhthu = item;
                } else if (item.template.id == 934) {
                    thangtinhthach = item;
                } else if (item.template.id == 935) {
                    thucan = item;
                }
            }
            if (linhthu != null && thangtinhthach != null && thangtinhthach.quantity >= 99) {
                player.inventory.gold -= gold;
                player.inventory.gem -= gem;
                InventoryServiceNew.gI().subQuantityItemsBag(player, thangtinhthach, 99);
                InventoryServiceNew.gI().subQuantityItemsBag(player, thucan, 1);
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    linhthu.itemOptions.clear();
                    linhthu.itemOptions.add(new Item.ItemOption(72, 2));
                    int rdUp = Util.nextInt(0, 7);
                    if (rdUp == 0) {
                        linhthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(10, 25)));
                    } else if (rdUp == 1) {
                        linhthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(10, 25)));
                    } else if (rdUp == 2) {
                        linhthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(10, 25)));
                    } else if (rdUp == 3) {
                        linhthu.itemOptions.add(new Item.ItemOption(108, Util.nextInt(10, 25)));
                    } else if (rdUp == 4) {
                        linhthu.itemOptions.add(new Item.ItemOption(94, Util.nextInt(8, 15)));
                    } else if (rdUp == 5) {
                        linhthu.itemOptions.add(new Item.ItemOption(14, Util.nextInt(8, 15)));
                    } else if (rdUp == 6) {
                        linhthu.itemOptions.add(new Item.ItemOption(80, Util.nextInt(10, 25)));
                    } else if (rdUp == 7) {
                        linhthu.itemOptions.add(new Item.ItemOption(81, Util.nextInt(10, 25)));
                    }
                    sendEffectSuccessCombine(player);
                } else {
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void moChiSoBongTai4(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }
            int gem = player.combineNew.gemCombine;
            if (player.inventory.gem < gem) {
                Service.gI().sendThongBao(player, "Không đủ ngọc để thực hiện");
                return;
            }
            Item linhthu = null;
            Item thangtinhthach = null;
            Item thucan = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item != null) {
                    if (item.template.id == 1129) {
                        linhthu = item;
                    } else if (item.template.id == 934) {
                        thangtinhthach = item;
                    } else if (item.template.id == 935) {
                        thucan = item;
                    }
                }
            }
            if (linhthu != null && thangtinhthach != null && thangtinhthach.quantity >= 99) {
                player.inventory.gold -= gold;
                player.inventory.gem -= gem;
                InventoryServiceNew.gI().subQuantityItemsBag(player, thangtinhthach, 99);
                InventoryServiceNew.gI().subQuantityItemsBag(player, thucan, 1);
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    linhthu.itemOptions.clear();
                    linhthu.itemOptions.add(new Item.ItemOption(72, 2));
                    int rdUp = Util.nextInt(0, 7);
                    if (rdUp == 0) {
                        linhthu.itemOptions.add(new Item.ItemOption(50, Util.nextInt(15, 35)));
                    } else if (rdUp == 1) {
                        linhthu.itemOptions.add(new Item.ItemOption(77, Util.nextInt(15, 35)));
                    } else if (rdUp == 2) {
                        linhthu.itemOptions.add(new Item.ItemOption(103, Util.nextInt(15, 35)));
                    } else if (rdUp == 3) {
                        linhthu.itemOptions.add(new Item.ItemOption(108, Util.nextInt(15, 35)));
                    } else if (rdUp == 4) {
                        linhthu.itemOptions.add(new Item.ItemOption(94, Util.nextInt(10, 20)));
                    } else if (rdUp == 5) {
                        linhthu.itemOptions.add(new Item.ItemOption(14, Util.nextInt(10, 20)));
                    } else if (rdUp == 6) {
                        linhthu.itemOptions.add(new Item.ItemOption(80, Util.nextInt(15, 35)));
                    } else if (rdUp == 7) {
                        linhthu.itemOptions.add(new Item.ItemOption(81, Util.nextInt(15, 35)));
                    }
                    sendEffectSuccessCombine(player);
                } else {
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    public void nangCapBongTai(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 2) {
                int gold = player.combineNew.goldCombine;
                if (player.inventory.gold < gold) {
                    Service.gI().sendThongBao(player, "Còn thiếu " + Util.numberToMoney(gold - player.inventory.gold) + " vàng");
                    return;
                }
                int ruby = player.combineNew.gemCombine;
                if (player.inventory.ruby < ruby) {
                    Service.gI().sendThongBao(player, "Còn thiếu " + Util.formatNumberMoney(ruby - player.inventory.ruby) + " hồng ngọc");
                    return;
                }
                Item bongtai = null;
                Item manhvobt = null;
                for (Item item : player.combineNew.itemsCombine) {
                    if (item.isPotara()) {
                        bongtai = item;
                    }
                    if (item.isMVBT()) {
                        manhvobt = item;
                    }
                }
                if (bongtai != null && manhvobt != null) {
                    int lvbt = bongtai.getLevelBongTai();
                    int countmvbt = 1000 * lvbt;
                    if (countmvbt > manhvobt.quantity) {
                        Service.gI().sendThongBao(player, "Còn thiếu " + (countmvbt - manhvobt.quantity) + " " + manhvobt.template.name);
                        return;
                    }
                    player.inventory.gold -= gold;
                    player.inventory.ruby -= ruby;
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhvobt, countmvbt);
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        bongtai.template = ItemService.gI().getTemplate(bongtai.getIDBongTaiAfterUpLevel(lvbt));
                        bongtai.itemOptions.clear();
                        bongtai.itemOptions.add(new Item.ItemOption(72, lvbt + 1));
                        sendEffectSuccessCombine(player);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                    reOpenItemCombine(player);
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh vỡ bông tai", "Đóng");
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào Bông tai Potara, 999 Mảnh vỡ bông tai", "Đóng");
            }
        } else {
            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }


    private void nangCapChanMenh(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int diem = player.combineNew.diemNangCap;
            if (player.inventory.event < diem) {
                Service.gI().sendThongBao(player, "Không đủ Điểm Săn Boss để thực hiện");
                return;
            }
            Item chanmenh = null;
            Item dahoangkim = null;
            int capbac = 0;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 1318) {
                    dahoangkim = item;
                } else if (item.template.id >= 1300 && item.template.id < 1308) {
                    chanmenh = item;
                    capbac = item.template.id - 1299;
                }
            }
            int soluongda = player.combineNew.daNangCap;
            if (dahoangkim != null && dahoangkim.quantity >= soluongda) {
                if (chanmenh != null && (chanmenh.template.id >= 1300 && chanmenh.template.id < 1308)) {
                    player.inventory.event -= diem;
                    if (Util.isTrue(player.combineNew.tiLeNangCap, 100)) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dahoangkim, soluongda);
                        chanmenh.template = ItemService.gI().getTemplate(chanmenh.template.id + 1);
                        chanmenh.itemOptions.clear();
                        chanmenh.itemOptions.add(new Item.ItemOption(50, (15 + capbac * 10)));
                        chanmenh.itemOptions.add(new Item.ItemOption(77, (20 + capbac * 10)));
                        chanmenh.itemOptions.add(new Item.ItemOption(103, (20 + capbac * 10)));
                        chanmenh.itemOptions.add(new Item.ItemOption(30, 1));
                        sendEffectSuccessCombine(player);
                    } else {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dahoangkim, soluongda);
                        sendEffectFailCombine(player);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.gI().sendMoney(player);
                    reOpenItemCombine(player);
                }
            } else {
                Service.gI().sendThongBao(player, "Không đủ Đá Hoàng Kim để thực hiện");
            }
        }
    }

    private void nangCapVatPham(Player player) {
        if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() != 1) {
                return;
            }
            if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() != 1) {
                return;//admin
            }
            Item itemDo = null;
            Item itemDNC = null;
            Item itemDBV = null;
            for (int j = 0; j < player.combineNew.itemsCombine.size(); j++) {
                if (player.combineNew.itemsCombine.get(j).isNotNullItem()) {
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.get(j).template.id == 987) {
                        itemDBV = player.combineNew.itemsCombine.get(j);
                        continue;
                    }
                    if (player.combineNew.itemsCombine.get(j).template.type < 5) {
                        itemDo = player.combineNew.itemsCombine.get(j);
                    } else {
                        itemDNC = player.combineNew.itemsCombine.get(j);
                    }
                }
            }
            if (isCoupleItemNangCapCheck(itemDo, itemDNC)) {
                int countDaNangCap = player.combineNew.countDaNangCap;
                int gold = player.combineNew.goldCombine;
                short countDaBaoVe = player.combineNew.countDaBaoVe;
                if (player.inventory.gold < gold) {
                    Service.getInstance().sendThongBao(player, "Không đủ vàng để thực hiện");
                    return;
                }

                if (itemDNC.quantity < countDaNangCap) {
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (Objects.isNull(itemDBV)) {
                        return;
                    }
                    if (itemDBV.quantity < countDaBaoVe) {
                        return;
                    }
                }

                int level = 0;
                Item.ItemOption optionLevel = null;
                for (Item.ItemOption io : itemDo.itemOptions) {
                    if (io.optionTemplate.id == 72) {
                        level = io.param;
                        optionLevel = io;
                        break;
                    }
                }
                if (level < MAX_LEVEL_ITEM) {
                    player.inventory.gold -= gold;
                    Item.ItemOption option = null;
                    Item.ItemOption option2 = null;
                    for (Item.ItemOption io : itemDo.itemOptions) {
                        if (io.optionTemplate.id == 47
                                || io.optionTemplate.id == 6
                                || io.optionTemplate.id == 0
                                || io.optionTemplate.id == 7
                                || io.optionTemplate.id == 14
                                || io.optionTemplate.id == 22
                                || io.optionTemplate.id == 23) {
                            option = io;
                        } else if (io.optionTemplate.id == 27
                                || io.optionTemplate.id == 28) {
                            option2 = io;
                        }
                    }
                    if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                        option.param += (option.param * 10 / 100);
                        if (option2 != null) {
                            option2.param += (option2.param * 10 / 100);
                        }
                        if (optionLevel == null) {
                            itemDo.itemOptions.add(new Item.ItemOption(72, 1));
                        } else {
                            optionLevel.param++;
                        }
//                        if (optionLevel != null && optionLevel.param >= 5) {
//                            ServerNotify.gI().notify("Chúc mừng " + player.name + " vừa nâng cấp "
//                                    + "thành công " + trangBi.template.name + " lên +" + optionLevel.param);
//                        }
                        sendEffectSuccessCombine(player);
                    } else {
                        if ((level == 2 || level == 4 || level == 6) && (player.combineNew.itemsCombine.size() != 3)) {
                            option.param -= (option.param * 10 / 100);
                            if (option2 != null) {
                                option2.param -= (option2.param * 10 / 100);
                            }
                            optionLevel.param--;
                        }
                        sendEffectFailCombine(player);
                    }
                    if (player.combineNew.itemsCombine.size() == 3) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, itemDBV, countDaBaoVe);
                    }
                    InventoryServiceNew.gI().subQuantityItemsBag(player, itemDNC, player.combineNew.countDaNangCap);
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void addPhapSu(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 2) {
                Item trangBi = null;
                Item daPhapSu = null;
                for (Item item : player.combineNew.itemsCombine) {
                    if (isItemPhaLeHoa(item)) {
                        trangBi = item;
                    }
                    if (isDaPhapSu(item)) {
                        daPhapSu = item;
                    }
                }
                if (trangBi != null && daPhapSu != null) {
                    Item.ItemOption optionStar = null;
                    int param = 0;
                    int level = 1;
                    short[] options = {194, 195, 196, 197};
                    int randomOption = options[new Random().nextInt(options.length)];
                    int run = 0;
                    int levelPhapSu = 0;
                    if (trangBi.itemOptions != null) {
                        optionStar = trangBi.itemOptions.stream().filter(ItemOption::isAddOptionPhapSu).findFirst().orElse(null);
                        levelPhapSu = trangBi.itemOptions.stream().filter(ItemOption::isOptionCheckedPhapSu).findFirst().map(io -> io.param).orElse(0);
                    }
                    if (trangBi.isNotNullItem() && daPhapSu.isNotNullItem() && daPhapSu.quantity >= 1) {
                        if (levelPhapSu < 6) {
                            if (optionStar == null) {
                                trangBi.itemOptions.add(randomOption == 197 ? new ItemOption(randomOption, param + 2) : new ItemOption(randomOption, param + 3));
                                trangBi.itemOptions.add(new Item.ItemOption(198, level));
                            } else {
                                if (trangBi.itemOptions != null) {
                                    for (Item.ItemOption ioo : trangBi.itemOptions) {
                                        if (ioo.isOptionCheckedPhapSu()) {
                                            ioo.param++;
                                        }
                                        if ((ioo.isAddOptionPhapSu()) && (ioo.optionTemplate.id == randomOption)) {
                                            ioo.param += randomOption == 197 ? 2 : 3;
                                            run = 1;
                                            break;
                                        } else {
                                            run = 2;
                                        }
                                    }
                                }
                                if (run == 2) {
                                    trangBi.itemOptions.add(randomOption == 197 ? new ItemOption(randomOption, param + 2) : new ItemOption(randomOption, param + 3));
                                }
                            }
                            sendEffectSuccessCombine(player);
                            InventoryServiceNew.gI().subQuantityItemsBag(player, daPhapSu, 1);
                            InventoryServiceNew.gI().sendItemBags(player);
                            reOpenItemCombine(player);
                        } else {
                            Service.getInstance().sendThongBao(player, "Pháp sư hóa đã đạt cấp cao nhất !!!");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Thiếu vật phẩm rồi !!!");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào 1 trang bị và đá pháp sư", "Đóng");
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào 1 trang bị và đá pháp sư", "Đóng");
            }
        } else {
            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public void removePhapSu(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (player.combineNew.itemsCombine.size() == 2) {
                Item.ItemOption optionStar;
                Item trangBi = null;
                Item buaTayPhapSu = null;
                for (Item item : player.combineNew.itemsCombine) {
                    if (isItemPhaLeHoa(item)) {
                        trangBi = item;
                    }
                    if (isBuaTayPhapSu(item)) {
                        buaTayPhapSu = item;
                    }
                }
                if (trangBi != null && buaTayPhapSu != null) {
                    optionStar = trangBi.itemOptions.stream().filter(ItemOption::isOptionPhapSu).findFirst().orElse(null);
                    if (optionStar == null) {
                        Service.getInstance().sendThongBao(player, "Có gì đâu mà tẩy !!!");
                        return;
                    }
                    if (trangBi.isNotNullItem() && buaTayPhapSu.isNotNullItem() && buaTayPhapSu.quantity >= 1) {
                        if (trangBi.itemOptions != null) {
                            trangBi.itemOptions.removeIf(ItemOption::isOptionPhapSu);
                        }
                        sendEffectSuccessCombine(player);
                        InventoryServiceNew.gI().subQuantityItemsBag(player, buaTayPhapSu, 1);
                        InventoryServiceNew.gI().sendItemBags(player);
                        reOpenItemCombine(player);
                    } else {
                        Service.getInstance().sendThongBao(player, "Thiếu vật phẩm rồi !!!");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào 1 trang bị và bùa tẩy pháp sư", "Đóng");
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Chỉ bỏ vào 1 trang bị và bùa tẩy pháp sư", "Đóng");
            }
        } else {
            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
        }
    }

    public void sendEffectOpenItem(Player player, short icon1, short icon2) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(OPEN_ITEM);
            msg.writer().writeShort(icon1);
            msg.writer().writeShort(icon2);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }

    public void sendEffectSuccessCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }


    public void sendEffectFailCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }

    public void reOpenItemCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(REOPEN_TAB_COMBINE);
            msg.writer().writeByte(player.combineNew.itemsCombine.size());
            for (Item it : player.combineNew.itemsCombine) {
                for (int j = 0; j < player.inventory.itemsBag.size(); j++) {
                    if (it == player.inventory.itemsBag.get(j)) {
                        msg.writer().writeByte(j);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }

    public void sendEffectCombineDB(Player player, short icon) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(CombineServiceNew.class, e);
        }
    }

    public int getPointChanMenh(int star) {
        return switch (star) {
            case 0 -> 10;
            case 1 -> 20;
            case 2 -> 30;
            case 3 -> 35;
            case 4 -> 40;
            case 5 -> 45;
            case 6 -> 50;
            case 7 -> 60;
            default -> 0;
        };
    }

    public int getDNCChanMenh(int star) {
        return switch (star) {
            case 0 -> 30;
            case 1 -> 35;
            case 2 -> 40;
            case 3 -> 45;
            case 4 -> 50;
            case 5 -> 60;
            case 6 -> 65;
            case 7 -> 80;
            default -> 0;
        };
    }

    public float getTileChanMenh(int star) {
        return switch (star) {
            case 0 -> 60f;
            case 1 -> 40f;
            case 2 -> 30f;
            case 3 -> 20f;
            case 4 -> 10f;
            case 5 -> 8f;
            case 6 -> 4f;
            case 7 -> 2f;
            default -> 0;
        };
    }

    public int getGoldPhaLeHoa(int star) {
        return switch (star) {
            case 0 -> 50_000_000;
            case 1 -> 60_000_000;
            case 2 -> 70_000_000;
            case 3 -> 80_000_000;
            case 4 -> 90_000_000;
            case 5 -> 100_000_000;
            case 6 -> 130_000_000;
            case 7 -> 150_000_000;
            default -> 0;
        };
    }

    public float getRatioPhaLeHoa(int star) {
        return switch (star) {
            case 0 -> 90;
            case 1 -> 80;
            case 2 -> 50;
            case 3 -> 40;
            case 4 -> 20;
            case 5 -> 10;
            case 6 -> 4;
            case 7 -> 2;
            default -> 0;
        };
    }

    public int getGemPhaLeHoa(int star) {
        return switch (star) {
            case 0 -> 10;
            case 1 -> 20;
            case 2 -> 30;
            case 3 -> 50;
            case 4 -> 80;
            case 5 -> 130;
            case 6 -> 210;
            case 7 -> 340;
            default -> 0;
        };
    }

    public int getGemEpSao(int star) {
        return switch (star) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 8;
            case 5 -> 13;
            case 6 -> 21;
            case 7 -> 34;
            default -> 0;
        };
    }

    public double getTileNangCapDo(int level) {
        return switch (level) {
            case 0 -> 90;
            case 1 -> 80;
            case 2 -> 50;
            case 3 -> 40;
            case 4 -> 20;
            case 5 -> 10;
            case 6 -> 4;
            case 7 -> 2;
            default -> 0;
        };
    }

    public int getCountDaNangCapDo(int level) {
        return switch (level) {
            case 0 -> 1;
            case 1 -> 2;
            case 2 -> 3;
            case 3 -> 5;
            case 4 -> 8;
            case 5 -> 13;
            case 6 -> 21;
            case 7 -> 34;
            default -> 0;
        };
    }

    public int getCountDaBaoVe(int level) {
        return level + 1;
    }

    public int getGoldNangCapDo(int level) {
        return switch (level) {
            case 0 -> 10_000_000;
            case 1 -> 15_000_000;
            case 2 -> 30_000_000;
            case 3 -> 50_000_000;
            case 4 -> 80_000_000;
            case 5 -> 120_000_000;
            case 6 -> 190_000_000;
            case 7 -> 250_000_000;
            default -> 0;
        };
    }

    public boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else
                return trangBi.template.type == 4 && daNangCap.template.id == 220;
        }
        return false;
    }

    public boolean isDaPhaLe(Item item) {
        return item != null && (item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20) || (item.template.id >= 1185 && item.template.id <= 1191));
    }

    public boolean isBuaTayPhapSu(Item item) {
        return item != null && item.template.id == 1236;
    }

    public boolean isDaPhapSu(Item item) {
        return item != null && item.template.id == 1235;
    }


    public boolean isItemPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            return (item.template.type < 5
                    || item.template.type == 32
                    || item.template.id == 1179
                    || item.template.id == 1211
                    || item.template.id == 1212
                    || item.template.id == 1213
                    || item.template.id == 1242
                    || item.template.id == 1243)
                    && !item.isTrangBiHSD();
        }
        return false;
    }

    public boolean isItemAn(Item item) {
        if (item != null && item.isNotNullItem()) {
            return item.template.id >= 1048 && item.template.id <= 1062;
        }
        return false;
    }

    public boolean isItemPhapSu(Item item) {
        if (item != null && item.isNotNullItem()) {
            return (item.template.type == 5 ||
                    item.template.type == 11 ||
                    ItemData.list_dapdo.contains((int) item.template.id)) &&
                    !item.isTrangBiHSD();
        }
        return false;
    }

    public int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).param;
        }
        return switch (daPhaLe.template.id) {
            case 20 -> 5; // +5%hp
            case 19 -> 5; // +5%ki
            case 18 -> 5; // +5%hp/30s
            case 17 -> 5; // +5%ki/30s
            case 16 -> 3; // +3%sđ
            case 15 -> 2; // +2%giáp
            case 14 -> 2; // +2%né đòn
            case 1187 -> 6; // +3%sđ
            case 1185 -> 2; // +3%sđ
            case 1190 -> 10; // +5%hp
            case 1191 -> 10; // +5%ki
            default -> -1;
        };
    }

    public int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).optionTemplate.id;
        }
        return switch (daPhaLe.template.id) {
            case 20, 1191 -> 77;
            case 19, 1190 -> 103;
            case 18 -> 80;
            case 17 -> 81;
            case 16, 1187 -> 50;
            case 15 -> 94;
            case 14 -> 108;
            case 1185 -> 14; // +3%sđ
            default -> -1;
        };
    }

    public int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        return switch (gender) {
            case 0 -> switch (type) {
                case 0 -> 0;
                case 1 -> 6;
                case 2 -> 21;
                case 3 -> 27;
                default -> -1;
            };
            case 1 -> switch (type) {
                case 0 -> 1;
                case 1 -> 7;
                case 2 -> 22;
                case 3 -> 28;
                default -> -1;
            };
            case 2 -> switch (type) {
                case 0 -> 2;
                case 1 -> 8;
                case 2 -> 23;
                case 3 -> 29;
                default -> -1;
            };
            default -> -1;
        };
    }

    public String getTextTopTabCombine(int type) {
        return switch (type) {
            case EP_SAO_TRANG_BI -> "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI -> "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case TINH_AN -> "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị Ấn";
            case MO_CHI_SO_BONG_TAI -> """
                    Ta sẽ phù phép
                    Cho bông tai Porata
                    Có 1 chỉ số ngẫu nhiên""";
            case NHAP_NGOC_RONG -> "Ta sẽ phù phép\ncho 10 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM -> "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case NANG_CAP_BONG_TAI -> """
                    Ta sẽ phù phép
                    cho Porata của ngươi
                    cấp cao hơn 1 bậc""";
            case PHAN_RA_DO_THAN_LINH -> "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case CHUYEN_HOA_DO_HUY_DIET -> "Ta sẽ phân rã \n  trang bị Hủy diệt của ngươi\nthành Phiếu hủy diệt!";
            case NANG_CAP_DO_TS -> "Ta sẽ nâng cấp \n  trang bị của người thành\n đồ thiên sứ!";
            case NANG_CAP_SKH_VIP -> "Thiên sứ nhờ ta nâng cấp \n  trang bị của người thành\n SKH VIP!";
            case CHUYEN_HOA_SKH -> "Ta sẽ chuyển hóa \n 3 món Thần linh không cần thiết\n thành SKH!";
            case PHAP_SU_HOA -> "Pháp sư hóa trang bị\nTa sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case TAY_PHAP_SU -> "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở về lúc chưa 'Pháp sư hóa'";
            case CHAN_MENH -> "Ta sẽ Nâng cấp\nChân Mệnh của ngươi\ncao hơn một bậc";
            case GIA_HAN_VAT_PHAM -> "Ta sẽ phù phép\ncho trang bị của ngươi\nthêm hạn sử dụng";
            default -> "";
        };
    }

    public String getTextInfoTabCombine(int type) {
        return switch (type) {
            case EP_SAO_TRANG_BI -> """
                    Chọn trang bị
                    (Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê
                    Chọn loại sao pha lê
                    Sau đó chọn 'Nâng cấp'
                    """;
            case PHA_LE_HOA_TRANG_BI -> "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case MO_CHI_SO_BONG_TAI -> """
                    Vào hành trang
                    Chọn bông tai Porata bất kì
                    999 nảnh hồn bông tai
                    1 đá xanh lam
                    Sau đó chọn 'Nâng cấp'
                    HP, KI sẽ random từ 5-20%
                    Dame, Né, Giáp sẽ random từ 5-15%
                    Chí Mạng sẽ random từ 5-10%
                    """;
            case TINH_AN ->
                    "Vào hành trang\nChọn 1 Trang bị THIÊN SỨ và 99 mảnh Ấn\nSau đó chọn 'Làm phép'\n--------\nTinh ấn (5 món +30%HP)\n Nhật ấn (5 món +30%KI\n Nguyệt ấn (5 món +20%SD)";
            case NHAP_NGOC_RONG -> "Vào hành trang\nChọn 7 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM -> """
                        Vào hành trang
                        Chọn trang bị
                        (Áo, quần, găng, giày hoặc rađa)
                        Chọn loại đá để nâng cấp
                        Sau đó chọn 'Nâng cấp'
                    """;
            case NANG_CAP_BONG_TAI -> """
                    Vào hành trang
                    Chọn bông tai Porata bất kì
                    999 nảnh vỡ bông tai
                    Sau đó chọn 'Nâng cấp'""";
            case PHAN_RA_DO_THAN_LINH -> """
                        Vào hành trang
                        Chọn trang bị
                        (Áo, quần, găng, giày hoặc rađa)
                        Chọn loại đá để phân rã
                        Sau đó chọn 'Phân Rã'
                    """;
            case CHUYEN_HOA_DO_HUY_DIET -> """
                    Vào hành trang
                    Chọn trang bị
                    (Áo, quần, găng, giày hoặc rađa) Hủy diệt
                    Sau đó chọn 'Chuyển hóa'""";
            case NANG_CAP_DO_TS -> """
                    Vào hành trang
                    Chọn 1 Công thức theo Hành tinh
                    kèm 1 Đá cầu vòng
                     và 999 mảnh thiên sứ
                     sẽ cho ra đồ thiên sứ từ 0-15% chỉ số
                    (Có tỉ lệ thêm dòng chỉ số ẩn)
                    Sau đó chọn 'Nâng Cấp'""";
            case NANG_CAP_SKH_VIP -> """
                    Vào hành trang
                    Chọn 1 trang bị Hủy diệt bất kì
                    Chọn tiếp ngẫu nhiên 2 món SKH thường\s
                     1 Đá ngũ sắc Đồ SKH VIP sẽ cùng loại\s
                     với đồ Hủy diệt!
                    Chọn 'Nâng Cấp'""";
            case CHUYEN_HOA_SKH -> """
                    Vào hành trang
                    Chọn 3 món Thần linh bất kì
                     Đồ SKH sẽ cùng loại\s
                     với món đầu tiên bỏ vào!
                    Chọn 'Nâng Cấp'""";
            case PHAP_SU_HOA -> """
                    Vào hành trang
                    Chọn trang bị
                    (Pet, VP đeo, Danh hiệu, Linh thú, Cải trang)
                    Chọn Đá Pháp Sư
                    Sau đó chọn 'Nâng cấp'""";
            case TAY_PHAP_SU -> """
                    Vào hành trang
                    Chọn trang bị
                    (Pet, VP đeo, Danh hiệu, Linh thú, Cải trang 'đã Pháp sư hóa')
                    Chọn Bùa Tẩy Pháp Sư
                    Sau đó chọn 'Nâng cấp'""";
            case CHAN_MENH -> """
                    Vào hành trang
                    Chọn Chân mệnh muốn nâng cấp
                    Chọn Đá Hoàng Kim
                    Sau đó chọn 'Nâng cấp'

                    Lưu ý: Khi Nâng cấp Thành công sẽ tăng 10% chỉ số của cấp trước đó""";
            case GIA_HAN_VAT_PHAM -> """
                    Vào hành trang
                    Chọn 1 trang bị có hạn sử dụng
                    Chọn thẻ gia hạn
                    Sau đó chọn 'Gia hạn'""";
            default -> "";
        };
    }
}

