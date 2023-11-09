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
import com.girlkun.services.RewardService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.*;
import java.util.stream.Collectors;

public class CombineServiceNew {

    private static final int COST_DOI_VE_DOI_DO_HUY_DIET = 500000000;
    private static final int COST_DAP_DO_KICH_HOAT = 500000000;
    private static final int COST_DOI_MANH_KICH_HOAT = 500000000;

    private static final int COST = 500000000;

    private static final int TIME_COMBINE = 1500;

    private static final byte MAX_STAR_ITEM = 16;
    private static final byte MAX_LEVEL_ITEM = 16;

    private static final byte OPEN_TAB_COMBINE = 0;
    private static final byte REOPEN_TAB_COMBINE = 1;
    private static final byte COMBINE_SUCCESS = 2;
    private static final byte COMBINE_FAIL = 3;
    private static final byte COMBINE_CHANGE_OPTION = 4;
    private static final byte COMBINE_DRAGON_BALL = 5;
    public static final byte OPEN_ITEM = 6;

    public static final int EP_SAO_TRANG_BI = 500;
    public static final int PHA_LE_HOA_TRANG_BI = 501;
    public static final int CHUYEN_HOA_TRANG_BI = 502;
//    public static final int DOI_VE_HUY_DIET = 503;
//    public static final int DAP_SET_KICH_HOAT = 504;
//    public static final int DOI_MANH_KICH_HOAT = 505;
//    public static final int DOI_CHUOI_KIEM = 506;
//    public static final int DOI_LUOI_KIEM = 507;
//    public static final int DOI_KIEM_THAN = 508;
//    public static final int OPTION_PORATA = 508;

    public static final int NANG_CAP_VAT_PHAM = 510;
    public static final int NANG_CAP_BONG_TAI = 511;
    public static final int LAM_PHEP_NHAP_DA = 512;
    public static final int NHAP_NGOC_RONG = 513;
    public static final int PHAN_RA_DO_THAN_LINH = 514;
    public static final int NANG_CAP_DO_TS = 515;
    public static final int NANG_CAP_SKH_VIP = 516;
    public static final int AN_TRANG_BI = 517;
    public static final int PHAP_SU_HOA = 518;
    public static final int TAY_PHAP_SU = 519;
    public static final int MO_CHI_SO_BONG_TAI_2 = 520;
    public static final int MO_CHI_SO_BONG_TAI_3 = 521;
    public static final int MO_CHI_SO_BONG_TAI_4 = 522;

    public static final int NANG_CAP_CHAN_MENH = 523;
    public static final int CHUYEN_HOA_DO_HUY_DIET = 524;
    public static final int RANDOM_SKH = 525;
    public static final int GIA_HAN_VAT_PHAM = 526;

    private final Npc baHatMit;
    private final Npc npcwhists;
    private final Npc npcthiensu64;

    private static CombineServiceNew i;

    public CombineServiceNew() {
        this.baHatMit = NpcManager.getNpc(ConstNpc.BA_HAT_MIT);
        this.npcwhists = NpcManager.getNpc(ConstNpc.WHIS);
        this.npcthiensu64 = NpcManager.getNpc(ConstNpc.NPC_64);
    }

    public static CombineServiceNew gI() {
        if (i == null) {
            i = new CombineServiceNew();
        }
        return i;
    }

    /**
     * Mở tab đập đồ
     *
     * @param player
     * @param type kiểu đập đồ
     */
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
        }
    }

    /**
     * Hiển thị thông tin đập đồ
     *
     * @param player
     */
    public void showInfoCombine(Player player, int[] index) {
        player.combineNew.clearItemCombine();
        if (index.length > 0) {
            for (int i = 0; i < index.length; i++) {
                player.combineNew.itemsCombine.add(player.inventory.itemsBag.get(index[i]));
            }
        }
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item trangBi = null;
                    Item daPhaLe = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (isTrangBiPhaLeHoa(item)) {
                            trangBi = item;
                        } else if (isDaPhaLe(item)) {
                            daPhaLe = item;
                        }
                    }
                    int star = 0; //sao pha lê đã ép
                    int starEmpty = 0; //lỗ sao pha lê
                    if (trangBi != null && daPhaLe != null) {
                        for (Item.ItemOption io : trangBi.itemOptions) {
                            if (io.optionTemplate.id == 102) {
                                star = io.param;
                            } else if (io.optionTemplate.id == 107) {
                                starEmpty = io.param;
                            }
                        }
                        if (star < starEmpty) {
                            player.combineNew.gemCombine = getGemEpSao(star);
                            String npcSay = trangBi.template.name + "\n|2|";
                            for (Item.ItemOption io : trangBi.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            if (daPhaLe.template.type == 30) {
                                for (Item.ItemOption io : daPhaLe.itemOptions) {
                                    npcSay += "|7|" + io.getOptionString() + "\n";
                                }
                            } else {
                                npcSay += "|7|" + ItemService.gI().getItemOptionTemplate(getOptionDaPhaLe(daPhaLe)).name.replaceAll("#", getParamDaPhaLe(daPhaLe) + "") + "\n";
                            }
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.gemCombine) + " ngọc";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
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
                break;
            case PHA_LE_HOA_TRANG_BI:
                if (player.combineNew.itemsCombine.size() == 1) {
                    Item item = player.combineNew.itemsCombine.get(0);
                    if (isTrangBiPhaLeHoa(item)) {
                        int star = 0;
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 107) {
                                star = io.param;
                                break;
                            }
                        }
                        if (star < MAX_STAR_ITEM) {
                            player.combineNew.goldCombine = getGoldPhaLeHoa(star);
                            player.combineNew.gemCombine = getGemPhaLeHoa(star);
                            player.combineNew.ratioCombine = getRatioPhaLeHoa(star);

                            String npcSay = item.template.name + "\n|2|";
                            for (Item.ItemOption io : item.itemOptions) {
                                if (io.optionTemplate.id != 102) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                            if (player.combineNew.goldCombine <= player.inventory.gold) {
                                npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                                baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                        "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                            } else {
                                npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                                baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
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
                break;
            case NHAP_NGOC_RONG:
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 1) {
                        Item item = player.combineNew.itemsCombine.get(0);

                        if (item != null && item.isNotNullItem() &&   item.quantity >= 7) {
                            String npcSay = "|2|Con có muốn biến 7 " + item.template.name + " thành\n"
                                    + "1 viên " + ItemService.gI().getTemplate((short) (item.template.id - 1)).name + "\n"
                                    + "|7|Cần 7 " + item.template.name;
                            this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
                        } else {
                            this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Không có ép lên ngọc rồng 2 sao được đâu !!!", "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cần 10 viên ngọc rồng cùng sao trở lên", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hành trang cần ít nhất 1 chỗ trống", "Đóng");
                }
                break;
            case AN_TRANG_BI:
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isTrangBiAn(item)) {
                            if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1232 || dangusac.template.id == 1233 || dangusac.template.id == 1234) && dangusac.quantity >= 99) {
                                String npcSay = item.template.name + "\n|2|";
                                for (Item.ItemOption io : item.itemOptions) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                                npcSay += "|1|Con có muốn biến trang bị " + item.template.name + " thành\n"
                                        + "trang bị Ấn không?\b|4|Đục là lên\n"
                                        + "|7|Cần 99 " + dangusac.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
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
                break;
            case NANG_CAP_VAT_PHAM:
                if (player.combineNew.itemsCombine.size() >= 2 && player.combineNew.itemsCombine.size() < 4) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type < 5).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đồ nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.type == 14).count() < 1) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá nâng cấp", "Đóng");
                        break;
                    }
                    if (player.combineNew.itemsCombine.size() == 3 && player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 987).count() < 1) {
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
                        for (Item.ItemOption io : itemDo.itemOptions) {
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
                            String npcSay = "|2|Hiện tại " + itemDo.template.name + " (+" + level + ")\n|0|";
                            for (Item.ItemOption io : itemDo.itemOptions) {
                                if (io.optionTemplate.id != 72) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                            }
                            String option = null;
                            int param = 0;
                            for (Item.ItemOption io : itemDo.itemOptions) {
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
                            npcSay += "|2|Sau khi nâng cấp (+" + (level + 1) + ")\n|7|"
                                    + option.replaceAll("#", String.valueOf(param))
                                    + "\n|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%\n"
                                    + (player.combineNew.countDaNangCap > itemDNC.quantity ? "|7|" : "|1|")
                                    + "Cần " + player.combineNew.countDaNangCap + " " + itemDNC.template.name
                                    + "\n" + (player.combineNew.goldCombine > player.inventory.gold ? "|7|" : "|1|")
                                    + "Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";

                            String daNPC = player.combineNew.itemsCombine.size() == 3 && itemDBV != null ? String.format("\nCần tốn %s đá bảo vệ", player.combineNew.countDaBaoVe) : "";
                            if ((level == 2 || level == 4 || level == 6) && !(player.combineNew.itemsCombine.size() == 3 && itemDBV != null)) {
                                npcSay += "\nNếu thất bại sẽ rớt xuống (+" + (level - 1) + ")";
                            }
                            if (player.combineNew.countDaNangCap > itemDNC.quantity) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaNangCap - itemDNC.quantity) + " " + itemDNC.template.name);
                            } else if (player.combineNew.goldCombine > player.inventory.gold) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + Util.numberToMoney((player.combineNew.goldCombine - player.inventory.gold)) + " vàng");
                            } else if (player.combineNew.itemsCombine.size() == 3 && Objects.nonNull(itemDBV) && itemDBV.quantity < player.combineNew.countDaBaoVe) {
                                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                        npcSay, "Còn thiếu\n" + (player.combineNew.countDaBaoVe - itemDBV.quantity) + " đá bảo vệ");
                            } else {
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE,
                                        npcSay, "Nâng cấp\n" + Util.numberToMoney(player.combineNew.goldCombine) + " vàng" + daNPC, "Từ chối");
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
                break;
            case NANG_CAP_CHAN_MENH:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongTai = null;
                    Item manhVo = null;
                    int star = 0;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 1318) {
                            manhVo = item;
                        } else if (item.template.id >= 1300 && item.template.id <= 1308) {
                            bongTai = item;
                            star = item.template.id - 1300;
                        }
                    }
                    if (bongTai != null && bongTai.template.id == 1308) {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Chân Mệnh đã đạt cấp tối đa", "Đóng");
                        return;
                    }
                    player.combineNew.DiemNangcap = getDiemNangcapChanmenh(star);
                    player.combineNew.DaNangcap = getDaNangcapChanmenh(star);
                    player.combineNew.TileNangcap = getTiLeNangcapChanmenh(star);
                    if (bongTai != null && manhVo != null && (bongTai.template.id >= 1300 && bongTai.template.id < 1308)) {
                        String npcSay = bongTai.template.name + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.TileNangcap + "%" + "\n";
                        if (player.combineNew.DiemNangcap <= player.inventory.event) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.DiemNangcap) + " Điểm Săn Boss";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.DaNangcap + " Đá Hoàng Kim");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.DiemNangcap - player.inventory.event) + " Điểm";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Chân Mệnh và Đá Hoàng Kim", "Đóng");
                }
                break;
            case NANG_CAP_BONG_TAI:
                if (player.combineNew.itemsCombine.size() == 2) {
                    Item bongTai = null;
                    Item manhVo = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 454) {
                            bongTai = item;
                        } else if (item.template.id == 933) {
                            manhVo = item;
                        } else if (item.template.id == 921) {
                            bongTai = item;
                        } else if (item.template.id == 1165) {
                            bongTai = item;
                        }
                    }
                    if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 454) {

                        player.combineNew.goldCombine = 500000000;
                        player.combineNew.gemCombine = 1000;
                        player.combineNew.ratioCombine = 50;

                        String npcSay = "Bông tai Porata cấp 2" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 921) {

                        player.combineNew.goldCombine = 1000000000;
                        player.combineNew.gemCombine = 5000;
                        player.combineNew.ratioCombine = 20;

                        String npcSay = "Bông tai Porata cấp 3" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 1165) {

                        player.combineNew.goldCombine = 1000000000;
                        player.combineNew.gemCombine = 15000;
                        player.combineNew.ratioCombine = 10;

                        String npcSay = "Bông tai Porata cấp 4" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " Hồng ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 1, 2 hoặc 3 và X9999 Mảnh vỡ bông tai", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 1, 2 hoặc 3 và X9999 Mảnh vỡ bông tai", "Đóng");
                }
                break;
            case MO_CHI_SO_BONG_TAI_2:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item bongTai = null;
                    Item manhHon = null;
                    Item daXanhLam = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 921) {
                            bongTai = item;
                        } else if (item.template.id == 934) {
                            manhHon = item;
                        } else if (item.template.id == 935) {
                            daXanhLam = item;
                        }
                    }
                    if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 99) {

                        player.combineNew.goldCombine = 2000000000;
                        player.combineNew.gemCombine = 1000;
                        player.combineNew.ratioCombine = 50;

                        String npcSay = "Bông tai Porata cấp 2" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 2, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 2, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                }
                break;
            case MO_CHI_SO_BONG_TAI_3:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item bongTai = null;
                    Item manhHon = null;
                    Item daXanhLam = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 1165) {
                            bongTai = item;
                        } else if (item.template.id == 934) {
                            manhHon = item;
                        } else if (item.template.id == 935) {
                            daXanhLam = item;
                        }
                    }
                    if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 99) {

                        player.combineNew.goldCombine = 2000000000;
                        player.combineNew.gemCombine = 1000;
                        player.combineNew.ratioCombine = 50;

                        String npcSay = "Bông tai Porata cấp 3" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 3, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 3, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                }
                break;
            case MO_CHI_SO_BONG_TAI_4:
                if (player.combineNew.itemsCombine.size() == 3) {
                    Item bongTai = null;
                    Item manhHon = null;
                    Item daXanhLam = null;
                    for (Item item : player.combineNew.itemsCombine) {
                        if (item.template.id == 1129) {
                            bongTai = item;
                        } else if (item.template.id == 934) {
                            manhHon = item;
                        } else if (item.template.id == 935) {
                            daXanhLam = item;
                        }
                    }
                    if (bongTai != null && manhHon != null && daXanhLam != null && manhHon.quantity >= 99) {

                        player.combineNew.goldCombine = 2000000000;
                        player.combineNew.gemCombine = 1000;
                        player.combineNew.ratioCombine = 50;

                        String npcSay = "Bông tai Porata cấp 4" + "\n|2|";
                        for (Item.ItemOption io : bongTai.itemOptions) {
                            npcSay += io.getOptionString() + "\n";
                        }
                        npcSay += "|7|Tỉ lệ thành công: " + player.combineNew.ratioCombine + "%" + "\n";
                        if (player.combineNew.goldCombine <= player.inventory.gold) {
                            npcSay += "|1|Cần " + Util.numberToMoney(player.combineNew.goldCombine) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
                                    "Nâng cấp\ncần " + player.combineNew.gemCombine + " ngọc");
                        } else {
                            npcSay += "Còn thiếu " + Util.numberToMoney(player.combineNew.goldCombine - player.inventory.gold) + " vàng";
                            baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, npcSay, "Đóng");
                        }
                    } else {
                        this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                                "Cần 1 Bông tai Porata cấp 4, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                    }
                } else {
                    this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU,
                            "Cần 1 Bông tai Porata cấp 4, X99 Mảnh hồn bông tai và 1 Đá xanh lam", "Đóng");
                }
                break;
            case PHAN_RA_DO_THAN_LINH:
                if (player.combineNew.itemsCombine.size() == 0) {
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
                break;
            case CHUYEN_HOA_DO_HUY_DIET:
                if (player.combineNew.itemsCombine.size() == 0) {
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
                break;
            case NANG_CAP_DO_TS:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 2 món Hủy Diệt bất kì và 1 món Thần Linh cùng loại", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 3) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isCongThuc()).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh Công thức", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.template.id == 1083).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu đá cầu vòng", "Đóng");
                        return;
                    }
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isManhTS() && item.quantity >= 999).count() < 1) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Thiếu mảnh thiên sứ", "Đóng");
                        return;
                    }

                    String npcSay = "|2|Con có muốn đổi các món nguyên liệu ?\n|7|"
                            + "Và nhận được " + player.combineNew.itemsCombine.stream().filter(Item::isManhTS).findFirst().get().typeNameManh() + " thiên sứ tương ứng\n"
                            + "|1|Cần " + Util.numberToMoney(COST) + " vàng";

                    if (player.inventory.gold < COST) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hết tiền rồi\nẢo ít thôi con", "Đóng");
                        return;
                    }
                    this.npcwhists.createOtherMenu(player, ConstNpc.MENU_NANG_CAP_DO_TS,
                            npcSay, "Nâng cấp\n" + Util.numberToMoney(COST) + " vàng", "Từ chối");
                } else {
                    if (player.combineNew.itemsCombine.size() > 3) {
                        this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Cất đi con ta không thèm", "Đóng");
                        return;
                    }
                    this.npcwhists.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Còn thiếu nguyên liệu để nâng cấp hãy quay lại sau", "Đóng");
                }
                break;
            case NANG_CAP_SKH_VIP:
                if (player.combineNew.itemsCombine.size() == 0) {
                    this.npcthiensu64.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy đưa ta 1 món thiên sứ và 2 món SKH ngẫu nhiên", "Đóng");
                    return;
                }
                if (player.combineNew.itemsCombine.size() == 4) {
                    if (player.combineNew.itemsCombine.stream().filter(item -> item.isNotNullItem() && item.isDHD()).count() < 1) {
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
                break;
            case RANDOM_SKH:
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
                break;
            case GIA_HAN_VAT_PHAM:
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
                            if (itopt.param < 0 || itopt == null) {
                                Service.getInstance().sendThongBaoOK(player, "Trang bị này không phải trang bị có Hạn Sử Dụng");
                                return;
                            }
                        }
                    }
                    String npcSay = "Trang bị được gia hạn \"" + itemGiahan.template.name + "\"";
                    npcSay += itemGiahan.template.name + "\n|2|";
                    for (Item.ItemOption io : itemGiahan.itemOptions) {
                        npcSay += io.getOptionString() + "\n";
                    }
                    npcSay += "\n|0|Sau khi gia hạn +1 ngày\n";

                    npcSay += "|0|Tỉ lệ thành công: 100%" + "\n";
                    if (player.inventory.gold > 200000000) {
                        npcSay += "|2|Cần 200Tr vàng";
                        this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay,
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
                break;
            case PHAP_SU_HOA:
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isTrangBiPhapsu(item)) {
                            if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && dangusac.template.id == 1235 && dangusac.quantity >= 1) {
                                String npcSay = item.template.name + "\n|2|";
                                for (Item.ItemOption io : item.itemOptions) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                                npcSay += "|1|Con có muốn biến trang bị " + item.template.name + " thành\n"
                                        + "trang bị Pháp sư hóa không?\n"
                                        + "|7|Cần 1 " + dangusac.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
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
                break;
            case TAY_PHAP_SU:
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    if (player.combineNew.itemsCombine.size() == 2) {
                        Item item = player.combineNew.itemsCombine.get(0);
                        Item dangusac = player.combineNew.itemsCombine.get(1);
                        if (isTrangBiPhapsu(item)) {
                            if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && dangusac.template.id == 1236 && dangusac.quantity >= 1) {
                                String npcSay = item.template.name + "\n|2|";
                                for (Item.ItemOption io : item.itemOptions) {
                                    npcSay += io.getOptionString() + "\n";
                                }
                                npcSay += "|1|Con có muốn tẩy trang bị " + item.template.name + " về\n"
                                        + "lúc chưa Pháp sư hóa không?\n"
                                        + "|7|Cần 1 " + dangusac.template.name;
                                this.baHatMit.createOtherMenu(player, ConstNpc.MENU_START_COMBINE, npcSay, "Làm phép", "Từ chối");
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
                break;
        }
    }

    /**
     * Bắt đầu đập đồ - điều hướng từng loại đập đồ
     *
     * @param player
     */
    public void startCombine(Player player) {
        switch (player.combineNew.typeCombine) {
            case EP_SAO_TRANG_BI:
                epSaoTrangBi(player);
                break;
            case PHA_LE_HOA_TRANG_BI:
                phaLeHoaTrangBi(player);
                break;
            case CHUYEN_HOA_TRANG_BI:

                break;
            case NHAP_NGOC_RONG:
                nhapNgocRong(player);
                break;
            case AN_TRANG_BI:
                antrangbi(player);
                break;
            case PHAN_RA_DO_THAN_LINH:
                phanradothanlinh(player);
                break;
            case CHUYEN_HOA_DO_HUY_DIET:
                chuyenhoahuydiet(player);
                break;
            case NANG_CAP_DO_TS:
                openDTS(player);
                break;
            case NANG_CAP_SKH_VIP:
                openSKHVIP(player);
                break;
            case RANDOM_SKH:
                randomskh(player);
                break;
            case NANG_CAP_VAT_PHAM:
                nangCapVatPham(player);
                break;
            case NANG_CAP_BONG_TAI:
                nangCapBongTai(player);
                break;
            case MO_CHI_SO_BONG_TAI_2:
                moChiSoBongTai2(player);
            case MO_CHI_SO_BONG_TAI_3:
                moChiSoBongTai3(player);
            case MO_CHI_SO_BONG_TAI_4:
                moChiSoBongTai4(player);
            case PHAP_SU_HOA:
                phapsuhoa(player);
                break;
            case TAY_PHAP_SU:
                tayphapsu(player);
                break;
            case NANG_CAP_CHAN_MENH:
                nangCapChanMenh(player);
                break;
            case GIA_HAN_VAT_PHAM:
                GiaHanTrangBi(player);
                break;
//            case OPTION_PORATA:
//                nangCapVatPham(player);
//                break;    
        }

        player.iDMark.setIndexMenu(ConstNpc.IGNORE_MENU);
        player.combineNew.clearParamCombine();
        player.combineNew.lastTimeCombine = System.currentTimeMillis();

    }

    public void GetTrangBiKichHoathuydiet(Player player, int id) {
        Item item = ItemService.gI().createNewItem((short) id);
        int[][] optionNormal = {{127, 128}, {130, 132}, {133, 135}};
        int[][] paramNormal = {{139, 140}, {142, 144}, {136, 138}};
        int[][] optionVIP = {{129}, {131}, {134}};
        int[][] paramVIP = {{141}, {143}, {137}};
        int random = Util.nextInt(optionNormal.length);
        int randomSkh = Util.nextInt(100);
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(47, Util.nextInt(1500, 2000)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(22, Util.nextInt(100, 150)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(0, Util.nextInt(9000, 11000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(23, Util.nextInt(90, 150)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(14, Util.nextInt(15, 20)));
        }
        if (randomSkh <= 20) {//tile ra do kich hoat
            if (randomSkh <= 5) { // tile ra option vip
                item.itemOptions.add(new ItemOption(optionVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(paramVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            } else {// 
                item.itemOptions.add(new ItemOption(optionNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(paramNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            }
        }

        InventoryServiceNew.gI().addItemBag(player, item);
        InventoryServiceNew.gI().sendItemBags(player);
    }

    public void GetTrangBiKichHoatthiensu(Player player, int id) {
        Item item = ItemService.gI().createNewItem((short) id);
        int[][] optionNormal = {{127, 128}, {130, 132}, {133, 135}};
        int[][] paramNormal = {{139, 140}, {142, 144}, {136, 138}};
        int[][] optionVIP = {{129}, {131}, {134}};
        int[][] paramVIP = {{141}, {143}, {137}};
        int random = Util.nextInt(optionNormal.length);
        int randomSkh = Util.nextInt(100);
        if (item.template.type == 0) {
            item.itemOptions.add(new ItemOption(47, Util.nextInt(2000, 2500)));
        }
        if (item.template.type == 1) {
            item.itemOptions.add(new ItemOption(22, Util.nextInt(150, 200)));
        }
        if (item.template.type == 2) {
            item.itemOptions.add(new ItemOption(0, Util.nextInt(18000, 20000)));
        }
        if (item.template.type == 3) {
            item.itemOptions.add(new ItemOption(23, Util.nextInt(150, 200)));
        }
        if (item.template.type == 4) {
            item.itemOptions.add(new ItemOption(14, Util.nextInt(20, 25)));
        }
        if (randomSkh <= 20) {//tile ra do kich hoat
            if (randomSkh <= 5) { // tile ra option vip
                item.itemOptions.add(new ItemOption(optionVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(paramVIP[player.gender][0], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            } else {// 
                item.itemOptions.add(new ItemOption(optionNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(paramNormal[player.gender][random], 0));
                item.itemOptions.add(new ItemOption(30, 0));
            }
        }

        InventoryServiceNew.gI().addItemBag(player, item);
        InventoryServiceNew.gI().sendItemBags(player);
    }

    private void doiKiemThan(Player player) {
        if (player.combineNew.itemsCombine.size() == 3) {
            Item keo = null, luoiKiem = null, chuoiKiem = null;
            for (Item it : player.combineNew.itemsCombine) {
                if (it.template.id == 2015) {
                    keo = it;
                } else if (it.template.id == 2016) {
                    chuoiKiem = it;
                } else if (it.template.id == 2017) {
                    luoiKiem = it;
                }
            }
            if (keo != null && keo.quantity >= 99 && luoiKiem != null && chuoiKiem != null) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    sendEffectSuccessCombine(player);
                    Item item = ItemService.gI().createNewItem((short) 2018);
                    item.itemOptions.add(new Item.ItemOption(50, Util.nextInt(9, 15)));
                    item.itemOptions.add(new Item.ItemOption(77, Util.nextInt(8, 15)));
                    item.itemOptions.add(new Item.ItemOption(103, Util.nextInt(8, 15)));
                    if (Util.isTrue(80, 100)) {
                        item.itemOptions.add(new Item.ItemOption(93, Util.nextInt(1, 15)));
                    }
                    InventoryServiceNew.gI().addItemBag(player, item);

                    InventoryServiceNew.gI().subQuantityItemsBag(player, keo, 99);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, luoiKiem, 1);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, chuoiKiem, 1);

                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void doiChuoiKiem(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item manhNhua = player.combineNew.itemsCombine.get(0);
            if (manhNhua.template.id == 2014 && manhNhua.quantity >= 99) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    sendEffectSuccessCombine(player);
                    Item item = ItemService.gI().createNewItem((short) 2016);
                    InventoryServiceNew.gI().addItemBag(player, item);

                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhNhua, 99);

                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void doiLuoiKiem(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item manhSat = player.combineNew.itemsCombine.get(0);
            if (manhSat.template.id == 2013 && manhSat.quantity >= 99) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
                    sendEffectSuccessCombine(player);
                    Item item = ItemService.gI().createNewItem((short) 2017);
                    InventoryServiceNew.gI().addItemBag(player, item);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhSat, 99);

                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void doiManhKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 2 || player.combineNew.itemsCombine.size() == 3) {
            Item nr1s = null, doThan = null, buaBaoVe = null;
            for (Item it : player.combineNew.itemsCombine) {
                if (it.template.id == 14) {
                    nr1s = it;
                } else if (it.template.id == 2010) {
                    buaBaoVe = it;
                } else if (it.template.id >= 555 && it.template.id <= 567) {
                    doThan = it;
                }
            }

            if (nr1s != null && doThan != null) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DOI_MANH_KICH_HOAT) {
                    player.inventory.gold -= COST_DOI_MANH_KICH_HOAT;
                    int tiLe = buaBaoVe != null ? 100 : 50;
                    if (Util.isTrue(tiLe, 100)) {
                        sendEffectSuccessCombine(player);
                        Item item = ItemService.gI().createNewItem((short) 2009);
                        item.itemOptions.add(new Item.ItemOption(30, 0));
                        InventoryServiceNew.gI().addItemBag(player, item);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryServiceNew.gI().subQuantityItemsBag(player, nr1s, 1);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, doThan, 1);
                    if (buaBaoVe != null) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, buaBaoVe, 1);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            } else {
                this.baHatMit.createOtherMenu(player, ConstNpc.IGNORE_MENU, "Hãy chọn 1 trang bị thần linh và 1 viên ngọc rồng 1 sao", "Đóng");
            }
        }
    }

    private void phanradothanlinh(Player player) {
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

    private void chuyenhoahuydiet(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            player.inventory.gold -= 500000000;
            Item item = player.combineNew.itemsCombine.get(0);
            Item phieu = null;
            switch (item.template.id) {
                case 650:
                case 652:
                case 654:
                    phieu = ItemService.gI().createNewItem((short) 1327);
                    break;
                case 651:
                case 653:
                case 655:
                    phieu = ItemService.gI().createNewItem((short) 1328);
                    break;
                case 657:
                case 659:
                case 661:
                    phieu = ItemService.gI().createNewItem((short) 1329);
                    break;
                case 658:
                case 660:
                case 662:
                    phieu = ItemService.gI().createNewItem((short) 1330);
                    break;
                default:
                    phieu = ItemService.gI().createNewItem((short) 1331);
                    break;
            }
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

    public void openDTS(Player player) {
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

    public void openSKHVIP(Player player) {
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

    public void randomskh(Player player) {
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

    private void dapDoKichHoat(Player player) {
        if (player.combineNew.itemsCombine.size() == 1 || player.combineNew.itemsCombine.size() == 2) {
            Item dhd = null, dtl = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.isNotNullItem()) {
                    if (item.template.id >= 650 && item.template.id <= 662) {
                        dhd = item;
                    } else if (item.template.id >= 555 && item.template.id <= 567) {
                        dtl = item;
                    }
                }
            }
            if (dhd != null) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0 //check chỗ trống hành trang
                        && player.inventory.gold >= COST_DAP_DO_KICH_HOAT) {
                    player.inventory.gold -= COST_DAP_DO_KICH_HOAT;
                    int tiLe = dtl != null ? 100 : 50;
                    if (Util.isTrue(tiLe, 100)) {
                        sendEffectSuccessCombine(player);
                        Item item = ItemService.gI().createNewItem((short) getTempIdItemC0(dhd.template.gender, dhd.template.type));
                        RewardService.gI().initBaseOptionClothes(item.template.id, item.template.type, item.itemOptions);
                        RewardService.gI().initActivationOption(item.template.gender < 3 ? item.template.gender : player.gender, item.template.type, item.itemOptions);
                        InventoryServiceNew.gI().addItemBag(player, item);
                    } else {
                        sendEffectFailCombine(player);
                    }
                    InventoryServiceNew.gI().subQuantityItemsBag(player, dhd, 1);
                    if (dtl != null) {
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dtl, 1);
                    }
                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void doiVeHuyDiet(Player player) {
        if (player.combineNew.itemsCombine.size() == 1) {
            Item item = player.combineNew.itemsCombine.get(0);
            if (item.isNotNullItem() && item.template.id >= 555 && item.template.id <= 567) {
                if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0
                        && player.inventory.gold >= COST_DOI_VE_DOI_DO_HUY_DIET) {
                    player.inventory.gold -= COST_DOI_VE_DOI_DO_HUY_DIET;
                    Item ticket = ItemService.gI().createNewItem((short) (2001 + item.template.type));
                    ticket.itemOptions.add(new Item.ItemOption(30, 0));
                    InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
                    InventoryServiceNew.gI().addItemBag(player, ticket);
                    sendEffectOpenItem(player, item.template.iconID, ticket.template.iconID);

                    InventoryServiceNew.gI().sendItemBags(player);
                    Service.getInstance().sendMoney(player);
                    reOpenItemCombine(player);
                }
            }
        }
    }

    private void GiaHanTrangBi(Player player) {
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
                if (isTrangBiPhaLeHoa(item)) {
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
            if (isTrangBiPhaLeHoa(item)) {
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
                if (item != null && item.isNotNullItem() && (item.template.id > 16 && item.template.id <= 20) && item.quantity >= 10) {
                    Item nr = ItemService.gI().createNewItem((short) (item.template.id - 1));
                    InventoryServiceNew.gI().addItemBag(player, nr);
                    InventoryServiceNew.gI().subQuantityItemsBag(player, item, 10);
                    InventoryServiceNew.gI().sendItemBags(player);
                    reOpenItemCombine(player);
                    sendEffectCombineDB(player, item.template.iconID);
                }
            }
        }
    }

    private void antrangbi(Player player) {
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

    //    private void phanradothanlinh(Player player) {
//        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
//            if (!player.combineNew.itemsCombine.isEmpty()) {
//                Item item = player.combineNew.itemsCombine.get(0);
//                if (item != null && item.isNotNullItem() && (item.template.id > 0 && item.template.id <= 3) && item.quantity >= 1) {
//                    Item nr = ItemService.gI().createNewItem((short) (item.template.id - 78));
//                    InventoryServiceNew.gI().addItemBag(player, nr);
//                    InventoryServiceNew.gI().subQuantityItemsBag(player, item, 1);
//                    InventoryServiceNew.gI().sendItemBags(player);
//                    reOpenItemCombine(player);
//                    sendEffectCombineDB(player, item.template.iconID);
//                    Service.getInstance().sendThongBao(player, "Đã nhận được 1 điểm");
//
//                }
//            }
//        }
//    }
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
                if(item != null){
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

    private void nangCapBongTai(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int gold = player.combineNew.goldCombine;
            if (player.inventory.gold < gold) {
                Service.gI().sendThongBao(player, "Không đủ vàng để thực hiện");
                return;
            }

            int gem = player.combineNew.gemCombine;
            if (player.inventory.ruby < gem) {
                Service.gI().sendThongBao(player, "Không đủ Hồng ngọc để thực hiện");
                return;
            }

            Item bongTai = null;
            Item manhVo = null;
            for (Item item : player.combineNew.itemsCombine) {
                if (item.template.id == 454) {
                    bongTai = item;
                } else if (item.template.id == 933) {
                    manhVo = item;
                } else if (item.template.id == 921) {
                    bongTai = item;
                } else if (item.template.id == 1165) {
                    bongTai = item;
                }
            }

            if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 454) {
                Item findItemBag = InventoryServiceNew.gI().findItemBag(player, 921); //Khóa btc2
                if (findItemBag != null) {
                    Service.gI().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 2 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(921);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 921) {
                Item findItemBag = InventoryServiceNew.gI().findItemBag(player, 1165); //Khóa btc2
                if (findItemBag != null) {
                    Service.gI().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 3 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(1165);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            } else if (bongTai != null && manhVo != null && manhVo.quantity >= 9999 && bongTai.template.id == 1165) {
                Item findItemBag = InventoryServiceNew.gI().findItemBag(player, 1129); //Khóa btc2
                if (findItemBag != null) {
                    Service.gI().sendThongBao(player, "Ngươi đã có bông tai Porata cấp 4 trong hàng trang rồi, không thể nâng cấp nữa.");
                    return;
                }
                player.inventory.gold -= gold;
                player.inventory.ruby -= gem;
                if (Util.isTrue(player.combineNew.ratioCombine, 100)) {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 9999);
                    bongTai.template = ItemService.gI().getTemplate(1129);
                    sendEffectSuccessCombine(player);
                } else {
                    InventoryServiceNew.gI().subQuantityItemsBag(player, manhVo, 99);
                    sendEffectFailCombine(player);
                }
                InventoryServiceNew.gI().sendItemBags(player);
                Service.gI().sendMoney(player);
                reOpenItemCombine(player);
            }
        }
    }

    private void nangCapChanMenh(Player player) {
        if (player.combineNew.itemsCombine.size() == 2) {
            int diem = player.combineNew.DiemNangcap;
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
            int soluongda = player.combineNew.DaNangcap;
            if (dahoangkim != null && dahoangkim.quantity >= soluongda) {
                if (chanmenh != null && (chanmenh.template.id >= 1300 && chanmenh.template.id < 1308)) {
                    player.inventory.event -= diem;
                    if (Util.isTrue(player.combineNew.TileNangcap, 100)) {
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

    private void phapsuhoa(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                Item dangusac = player.combineNew.itemsCombine.get(1);
                int star = 0;
                short[] chiso = {194, 195, 196, 197};
                byte randomDo = (byte) new Random().nextInt(chiso.length);
                int lvchiso = 0;
                int cap = 1;
                Item.ItemOption optionStar = null;
                int check = chiso[randomDo];
                int run = 0;
                int lvcheck = 0;
                if(item.itemOptions != null){
                    for (Item.ItemOption io : item.itemOptions) {
                        if (io.optionTemplate.id == 194 || io.optionTemplate.id == 195 || io.optionTemplate.id == 196 || io.optionTemplate.id == 197) {
                            star = io.param;
                            optionStar = io;
                            break;
                        }
                    }
                    for (Item.ItemOption io2 : item.itemOptions) {
                        if (io2.optionTemplate.id == 198) {
                            lvcheck = io2.param;
                            break;
                        }
                    }
                }
                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1235) && dangusac.quantity >= 1) {
                    if (lvcheck < 6) {
                        if (optionStar == null) {
                            item.itemOptions.add(new Item.ItemOption(198, cap));
                            if (check == 197) {
                                item.itemOptions.add(new Item.ItemOption(check, lvchiso + 2));
                            } else {
                                item.itemOptions.add(new Item.ItemOption(check, lvchiso + 3));
                            }
                            sendEffectSuccessCombine(player);
                            InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 1);
                            InventoryServiceNew.gI().sendItemBags(player);
                            reOpenItemCombine(player);
                        } else {
                            if(item.itemOptions != null){
                                for (Item.ItemOption ioo : item.itemOptions) {
                                    if (ioo.optionTemplate.id == 198) {
                                        ioo.param++;
                                    }
                                    if ((ioo.optionTemplate.id == 194 || ioo.optionTemplate.id == 195 || ioo.optionTemplate.id == 196 || ioo.optionTemplate.id == 197) && (ioo.optionTemplate.id == check)) {
                                        if (check == 197) {
                                            ioo.param += 2;
                                        } else {
                                            ioo.param += 3;
                                        }
                                        sendEffectSuccessCombine(player);
                                        InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 1);
                                        InventoryServiceNew.gI().sendItemBags(player);
                                        reOpenItemCombine(player);
                                        run = 1;
                                        break;
                                    } else {
                                        run = 2;
                                    }
                                }
                            }
                            if (run == 2) {
                                if (check == 197) {
                                    item.itemOptions.add(new Item.ItemOption(check, lvchiso + 2));
                                } else {
                                    item.itemOptions.add(new Item.ItemOption(check, lvchiso + 3));
                                }
                                sendEffectSuccessCombine(player);
                                InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 1);
                                InventoryServiceNew.gI().sendItemBags(player);
                                reOpenItemCombine(player);
                            }
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Pháp sư hóa đã đạt cấp cao nhất !!!");
                    }
                }
            }
        }
    }

    private void tayphapsu(Player player) {
        if (InventoryServiceNew.gI().getCountEmptyBag(player) > 0) {
            if (!player.combineNew.itemsCombine.isEmpty()) {
                Item item = player.combineNew.itemsCombine.get(0);
                Item dangusac = player.combineNew.itemsCombine.get(1);
                Item.ItemOption optionStar = null;

                for (Item.ItemOption io : item.itemOptions) {
                    if (io.optionTemplate.id == 194 || io.optionTemplate.id == 195 || io.optionTemplate.id == 196 || io.optionTemplate.id == 197 || io.optionTemplate.id == 198) {
                        optionStar = io;
                        break;
                    }
                }

                if (item != null && item.isNotNullItem() && dangusac != null && dangusac.isNotNullItem() && (dangusac.template.id == 1236) && dangusac.quantity >= 1) {
                    if (optionStar == null) {
                        Service.getInstance().sendThongBao(player, "Có gì đâu mà tẩy !!!");
                    } else {

                        if (item.itemOptions != null) {

                            Iterator<ItemOption> iterator = item.itemOptions.iterator();
                            while (iterator.hasNext()) {
                                ItemOption ioo = iterator.next();
                                if (ioo.optionTemplate.id == 194 || ioo.optionTemplate.id == 195 || ioo.optionTemplate.id == 196 || ioo.optionTemplate.id == 197 || ioo.optionTemplate.id == 198) {
                                    iterator.remove();
                                }
                            }

                        }
                        //item.itemOptions.add(new Item.ItemOption(73 , 1));  
                        sendEffectSuccessCombine(player);
                        InventoryServiceNew.gI().subQuantityItemsBag(player, dangusac, 1);
                        InventoryServiceNew.gI().sendItemBags(player);
                        reOpenItemCombine(player);

                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Thiếu vật phẩm gòi !!!");
                }

            }
        }
    }

    //--------------------------------------------------------------------------
    /**
     * r
     * Hiệu ứng mở item
     *
     * @param player
     * @param icon1
     * @param icon2
     */
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
        }
    }

    /**
     * Hiệu ứng đập đồ thành công
     *
     * @param player
     */
    private void sendEffectSuccessCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_SUCCESS);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Hiệu ứng đập đồ thất bại
     *
     * @param player
     */
    private void sendEffectFailCombine(Player player) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_FAIL);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    /**
     * Gửi lại danh sách đồ trong tab combine
     *
     * @param player
     */
    private void reOpenItemCombine(Player player) {
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
        }
    }

    /**
     * Hiệu ứng ghép ngọc rồng
     *
     * @param player
     * @param icon
     */
    private void sendEffectCombineDB(Player player, short icon) {
        Message msg;
        try {
            msg = new Message(-81);
            msg.writer().writeByte(COMBINE_DRAGON_BALL);
            msg.writer().writeShort(icon);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    //--------------------------------------------------Chân mệnh/////
    private int getDiemNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 10;
            case 1:
                return 20;
            case 2:
                return 30;
            case 3:
                return 35;
            case 4:
                return 40;
            case 5:
                return 45;
            case 6:
                return 50;
            case 7:
                return 60;
        }
        return 0;
    }

    private int getDaNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 30;
            case 1:
                return 35;
            case 2:
                return 40;
            case 3:
                return 45;
            case 4:
                return 50;
            case 5:
                return 60;
            case 6:
                return 65;
            case 7:
                return 80;
        }
        return 0;
    }

    private float getTiLeNangcapChanmenh(int star) {
        switch (star) {
            case 0:
                return 60f;
            case 1:
                return 40f;
            case 2:
                return 30f;
            case 3:
                return 20f;
            case 4:
                return 10f;
            case 5:
                return 8f;
            case 6:
                return 4f;
            case 7:
                return 100f;
        }
        return 0;
    }

    //--------------------------------------------------------------------------Ratio, cost combine
    private int getGoldPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 50000000;
            case 1:
                return 60000000;
            case 2:
                return 70000000;
            case 3:
                return 80000000;
            case 4:
                return 90000000;
            case 5:
                return 100000000;
            case 6:
                return 130000000;
            case 7:
                return 150000000;
//            case 8:
//                return 50000000;
//            case 9:
//                return 70000000;
//            case 10:
//                return 90000000;
//            case 11:
//                return 120000000;
//            case 12:
//                return 180000000;
//            case 13:
//                return 200000000;
//            case 14:
//                return 210000000;
//            case 15:
//                return 230000000;
        }
        return 0;
    }

    private float getRatioPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 90;
            case 1:
                return 80;
            case 2:
                return 50;
            case 3:
                return 40;
            case 4:
                return 20;
            case 5:
                return 10;
            case 6:
                return 4;
            case 7:
                return 2;
//            case 8:
//                return 40;
//            case 9:
//                return 35;
//            case 10:
//                return 30f;
//            case 11:
//                return 20f;
//            case 12:
//                return 5f;
//            case 13:
//                return 3f;
//            case 14:
//                return 0.8f;
//            case 15:
//                return 0.4f;
        }

        return 0;
    }

    private int getGemPhaLeHoa(int star) {
        switch (star) {
            case 0:
                return 30;
            case 1:
                return 40;
            case 2:
                return 50;
            case 3:
                return 60;
            case 4:
                return 70;
            case 5:
                return 75;
            case 6:
                return 80;
            case 7:
                return 80;
//            case 8:
//                return 85;
//            case 9:
//                return 90;
//            case 10:
//                return 90;
//            case 11:
//                return 100;
//            case 12:
//                return 120;
//            case 13:
//                return 130;
//            case 14:
//                return 140;
//            case 15:
//                return 150;
        }
        return 0;
    }

    private int getGemEpSao(int star) {
        switch (star) {
            case 0:
                return 1;
            case 1:
                return 2;
            case 2:
                return 5;
            case 3:
                return 10;
            case 4:
                return 25;
            case 5:
                return 50;
            case 6:
                return 100;
            case 7:
                return 110;
//            case 8:
//                return 120;
//            case 9:
//                return 130;
//            case 10:
//                return 140;
//            case 11:
//                return 150;
//            case 12:
//                return 160;
//            case 13:
//                return 140;
//            case 14:
//                return 150;
//            case 15:
//                return 160;
        }
        return 0;
    }

    private double getTileNangCapDo(int level) {
        switch (level) {
            case 0:
                return 90;
            case 1:
                return 80;
            case 2:
                return 50;
            case 3:
                return 40;
            case 4:
                return 20;
            case 5:
                return 10;
            case 6:
                return 4;
            case 7:
                return 2;
//            case 8:
//                return 99;
//            case 9:
//                return 99;
//            case 10: // 7 sao
//                return 30;
//            case 11: // 7 sao
//                return 20;
//            case 12: // 7 sao
//                return 10;
//            case 13: // 7 sao
//                return 10;
//            case 14: // 7 sao
//                return 8;
//            case 15: // 7 sao
//                return 3;
        }
        return 0;
    }

    private int getCountDaNangCapDo(int level) {
        switch (level) {
            case 0:
                return 3;
            case 1:
                return 7;
            case 2:
                return 11;
            case 3:
                return 17;
            case 4:
                return 23;
            case 5:
                return 35;
            case 6:
                return 50;
            case 7:
                return 60;
//            case 8:
//                return 70;
//            case 9:
//                return 70;
//            case 10:
//                return 70;
//            case 11:
//                return 80;
//            case 12:
//                return 90;
//            case 13:
//                return 90;
//            case 14:
//                return 90;
//            case 15:
//                return 99;

        }
        return 0;
    }

    private int getCountDaBaoVe(int level) {
        return level + 1;
    }

    private int getGoldNangCapDo(int level) {
        switch (level) {
            case 0:
                return 10000000;
            case 1:
                return 17000000;
            case 2:
                return 30000000;
            case 3:
                return 40000000;
            case 4:
                return 70000000;
            case 5:
                return 80000000;
            case 6:
                return 100000000;
            case 7:
                return 250000000;
//            case 8:
//                return 10000000;
//            case 9:
//                return 17000000;
//            case 10:
//                return 30000000;
//            case 11:
//                return 40000000;
//            case 12:
//                return 70000000;
//            case 13:
//                return 80000000;
//            case 14:
//                return 100000000;
//            case 15:
//                return 250000000;
////                
        }
        return 0;
    }

    //--------------------------------------------------------------------------check
    private boolean isCoupleItemNangCap(Item item1, Item item2) {
        Item trangBi = null;
        Item daNangCap = null;
        if (item1 != null && item1.isNotNullItem()) {
            if (item1.template.type < 5) {
                trangBi = item1;
            } else if (item1.template.type == 14) {
                daNangCap = item1;
            }
        }
        if (item2 != null && item2.isNotNullItem()) {
            if (item2.template.type < 5) {
                trangBi = item2;
            } else if (item2.template.type == 14) {
                daNangCap = item2;
            }
        }
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isCoupleItemNangCapCheck(Item trangBi, Item daNangCap) {
        if (trangBi != null && daNangCap != null) {
            if (trangBi.template.type == 0 && daNangCap.template.id == 223) {
                return true;
            } else if (trangBi.template.type == 1 && daNangCap.template.id == 222) {
                return true;
            } else if (trangBi.template.type == 2 && daNangCap.template.id == 224) {
                return true;
            } else if (trangBi.template.type == 3 && daNangCap.template.id == 221) {
                return true;
            } else if (trangBi.template.type == 4 && daNangCap.template.id == 220) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isDaPhaLe(Item item) {
        return item != null && (item.template.type == 30 || (item.template.id >= 14 && item.template.id <= 20) || (item.template.id >= 1185 && item.template.id <= 1191));
    }

    private boolean isTrangBiPhaLeHoa(Item item) {
        if (item != null && item.isNotNullItem()) {
            if ((item.template.type < 5 || item.template.type == 32
                    || item.template.id == 1179
                    || item.template.id == 1211 || item.template.id == 1212 || item.template.id == 1213 || item.template.id == 1242 
                    || item.template.id == 1243) && !item.isTrangBiHSD()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiAn(Item item) {
        if (item != null && item.isNotNullItem()) {
            if (item.template.id >= 1048 && item.template.id <= 1062) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private boolean isTrangBiPhapsu(Item item) {
        if (item != null && item.isNotNullItem()) {
            if ((item.template.type == 5 || item.template.type == 11
                    || ItemData.list_dapdo.contains((int) item.template.id)) && !item.isTrangBiHSD()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    private int getParamDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).param;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 5; // +5%hp
            case 19:
                return 5; // +5%ki
            case 18:
                return 5; // +5%hp/30s
            case 17:
                return 5; // +5%ki/30s
            case 16:
                return 3; // +3%sđ
            case 15:
                return 2; // +2%giáp
            case 14:
                return 2; // +2%né đòn
            case 1187:
                return 6; // +3%sđ
            case 1185:
                return 2; // +3%sđ
            case 1190:
                return 10; // +5%hp
            case 1191:
                return 10; // +5%ki
            default:
                return -1;
        }
    }

    private int getOptionDaPhaLe(Item daPhaLe) {
        if (daPhaLe.template.type == 30) {
            return daPhaLe.itemOptions.get(0).optionTemplate.id;
        }
        switch (daPhaLe.template.id) {
            case 20:
                return 77;
            case 19:
                return 103;
            case 18:
                return 80;
            case 17:
                return 81;
            case 16:
                return 50;
            case 15:
                return 94;
            case 14:
                return 108;
            case 1187:
                return 50;
            case 1185:
                return 14; // +3%sđ
            case 1190:
                return 103;
            case 1191:
                return 77;
            default:
                return -1;
        }
    }

    /**
     * Trả về id item c0
     *
     * @param gender
     * @param type
     * @return
     */
    private int getTempIdItemC0(int gender, int type) {
        if (type == 4) {
            return 12;
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return 0;
                    case 1:
                        return 6;
                    case 2:
                        return 21;
                    case 3:
                        return 27;
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return 1;
                    case 1:
                        return 7;
                    case 2:
                        return 22;
                    case 3:
                        return 28;
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return 2;
                    case 1:
                        return 8;
                    case 2:
                        return 23;
                    case 3:
                        return 29;
                }
                break;
        }
        return -1;
    }

    //Trả về tên đồ c0
    private String getNameItemC0(int gender, int type) {
        if (type == 4) {
            return "Rada cấp 1";
        }
        switch (gender) {
            case 0:
                switch (type) {
                    case 0:
                        return "Áo vải 3 lỗ";
                    case 1:
                        return "Quần vải đen";
                    case 2:
                        return "Găng thun đen";
                    case 3:
                        return "Giầy nhựa";
                }
                break;
            case 1:
                switch (type) {
                    case 0:
                        return "Áo sợi len";
                    case 1:
                        return "Quần sợi len";
                    case 2:
                        return "Găng sợi len";
                    case 3:
                        return "Giầy sợi len";
                }
                break;
            case 2:
                switch (type) {
                    case 0:
                        return "Áo vải thô";
                    case 1:
                        return "Quần vải thô";
                    case 2:
                        return "Găng vải thô";
                    case 3:
                        return "Giầy vải thô";
                }
                break;
        }
        return "";
    }

    //--------------------------------------------------------------------------Text tab combine
    private String getTextTopTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở lên mạnh mẽ";
            case PHA_LE_HOA_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị pha lê";
            case AN_TRANG_BI:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở thành trang bị Ấn";
            case NHAP_NGOC_RONG:
                return "Ta sẽ phù phép\ncho 10 viên Ngọc Rồng\nthành 1 viên Ngọc Rồng cấp cao";
            case NANG_CAP_VAT_PHAM:
                return "Ta sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case NANG_CAP_BONG_TAI:
                return "Ta sẽ phù phép\ncho bông tai Porata của ngươi\nthành bongtai cấp cao hơn 1 bậc";
            case MO_CHI_SO_BONG_TAI_2:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 2 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case MO_CHI_SO_BONG_TAI_3:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 3 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case MO_CHI_SO_BONG_TAI_4:
                return "Ta sẽ phù phép\ncho bông tai Porata cấp 4 của ngươi\ncó 1 chỉ số ngẫu nhiên";
            case PHAN_RA_DO_THAN_LINH:
                return "Ta sẽ phân rã \n  trang bị của người thành điểm!";
            case CHUYEN_HOA_DO_HUY_DIET:
                return "Ta sẽ phân rã \n  trang bị Hủy diệt của ngươi\nthành Phiếu hủy diệt!";
            case NANG_CAP_DO_TS:
                return "Ta sẽ nâng cấp \n  trang bị của người thành\n đồ thiên sứ!";
            case NANG_CAP_SKH_VIP:
                return "Thiên sứ nhờ ta nâng cấp \n  trang bị của người thành\n SKH VIP!";
            case RANDOM_SKH:
                return "Ta sẽ chuyển hóa \n 3 món Thần linh không cần thiết\n thành SKH!";
            case PHAP_SU_HOA:
                return "Pháp sư hóa trang bị\nTa sẽ phù phép cho trang bị của ngươi trở lên mạnh mẽ";
            case TAY_PHAP_SU:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\ntrở về lúc chưa 'Pháp sư hóa'";
            case NANG_CAP_CHAN_MENH:
                return "Ta sẽ Nâng cấp\nChân Mệnh của ngươi\ncao hơn một bậc";
            case GIA_HAN_VAT_PHAM:
                return "Ta sẽ phù phép\ncho trang bị của ngươi\nthêm hạn sử dụng";
            default:
                return "";
        }
    }

    private String getTextInfoTabCombine(int type) {
        switch (type) {
            case EP_SAO_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa) có ô đặt sao pha lê\nChọn loại sao pha lê\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case PHA_LE_HOA_TRANG_BI:
                return "Chọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nSau đó chọn 'Nâng cấp'";
            case AN_TRANG_BI:
                return "Vào hành trang\nChọn 1 Trang bị THIÊN SỨ và 99 mảnh Ấn\nSau đó chọn 'Làm phép'\n--------\nTinh ấn (5 món +30%HP)\n Nhật ấn (5 món +30%KI\n Nguyệt ấn (5 món +20%SD)";
            case NHAP_NGOC_RONG:
                return "Vào hành trang\nChọn 10 viên ngọc cùng sao\nSau đó chọn 'Làm phép'";
            case NANG_CAP_VAT_PHAM:
                return "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để nâng cấp\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case NANG_CAP_BONG_TAI:
                return "Vào hành trang\nChọn bông tai Porata 1, 2 hoặc 3\nChọn mảnh bông tai để nâng cấp, số lượng\n9999 cái\nSau đó chọn 'Nâng cấp'\nNếu thất bại sẽ bị trừ đi 99 Mảnh bông tai\n\nSau khi thành công Bông tai của ngươi sẽ tăng 1 bậc";
            case MO_CHI_SO_BONG_TAI_2:
                return "Vào hành trang\nChọn bông tai Porata cấp 2\nChọn mảnh hồn bông tai số lượng 99 cái\nvà đá xanh lam để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case MO_CHI_SO_BONG_TAI_3:
                return "Vào hành trang\nChọn bông tai Porata cấp 3\nChọn mảnh hồn bông tai số lượng 99 cái\nvà đá xanh lam để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case MO_CHI_SO_BONG_TAI_4:
                return "Vào hành trang\nChọn bông tai Porata cấp 4\nChọn mảnh hồn bông tai số lượng 99 cái\nvà đá xanh lam để nâng cấp\nSau đó chọn 'Nâng cấp'";
            case PHAN_RA_DO_THAN_LINH:
                return "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa)\nChọn loại đá để phân rã\n"
                        + "Sau đó chọn 'Phân Rã'";
            case CHUYEN_HOA_DO_HUY_DIET:
                return "Vào hành trang\nChọn trang bị\n(Áo, quần, găng, giày hoặc rađa) Hủy diệt\n"
                        + "Sau đó chọn 'Chuyển hóa'";
            case NANG_CAP_DO_TS:
                return "Vào hành trang\nChọn 1 Công thức theo Hành tinh\nkèm 1 Đá cầu vòng\n và 999 mảnh thiên sứ\n "
                        + "sẽ cho ra đồ thiên sứ từ 0-15% chỉ số"
                        + "\n(Có tỉ lệ thêm dòng chỉ số ẩn)"
                        + "\nSau đó chọn 'Nâng Cấp'";
            case NANG_CAP_SKH_VIP:
                return "Vào hành trang\nChọn 1 trang bị Hủy diệt bất kì\nChọn tiếp ngẫu nhiên 2 món SKH thường \n 1 Đá ngũ sắc"
                        + " Đồ SKH VIP sẽ cùng loại \n với đồ Hủy diệt!"
                        + "\nChọn 'Nâng Cấp'";
            case RANDOM_SKH:
                return "Vào hành trang\nChọn 3 món Thần linh bất kì\n"
                        + " Đồ SKH sẽ cùng loại \n với món đầu tiên bỏ vào!"
                        + "\nChọn 'Nâng Cấp'";
            case PHAP_SU_HOA:
                return "Vào hành trang\nChọn trang bị\n(Pet, VP đeo, Danh hiệu, Linh thú, Cải trang)\nChọn Đá Pháp Sư\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case TAY_PHAP_SU:
                return "Vào hành trang\nChọn trang bị\n(Pet, VP đeo, Danh hiệu, Linh thú, Cải trang 'đã Pháp sư hóa')\nChọn Bùa Tẩy Pháp Sư\n"
                        + "Sau đó chọn 'Nâng cấp'";
            case NANG_CAP_CHAN_MENH:
                return "Vào hành trang\nChọn Chân mệnh muốn nâng cấp\nChọn Đá Hoàng Kim\n"
                        + "Sau đó chọn 'Nâng cấp'\n\n"
                        + "Lưu ý: Khi Nâng cấp Thành công sẽ tăng 10% chỉ số của cấp trước đó";
            case GIA_HAN_VAT_PHAM:
                return "Vào hành trang\n"
                        + "Chọn 1 trang bị có hạn sử dụng\n"
                        + "Chọn thẻ gia hạn\n"
                        + "Sau đó chọn 'Gia hạn'";
            default:
                return "";
        }
    }
}

/**
 * Code được viết bởi Hoàng Việt
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức.
 */
