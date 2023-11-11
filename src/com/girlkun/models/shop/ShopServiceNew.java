package com.girlkun.models.shop;

import com.girlkun.consts.ConstNpc;
import com.girlkun.data.ItemData;
import com.girlkun.models.item.Item;
import com.girlkun.models.player.Inventory;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.server.Manager;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.NpcService;
import com.girlkun.services.Service;
import com.girlkun.services.func.Input;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.List;

public class ShopServiceNew {

    private static final byte COST_GOLD = 0;
    private static final byte COST_GEM = 1;
    private static final byte COST_ITEM_SPEC = 2;
    private static final byte COST_RUBY = 3;
    private static final byte COST_COUPON = 4;
    private static final byte COST_EVENT = 5;

    private static final byte NORMAL_SHOP = 6;
    private static final byte SPEC_SHOP = 7;
    private static ShopServiceNew I;

    public static ShopServiceNew gI() {
        if (ShopServiceNew.I == null) {
            ShopServiceNew.I = new ShopServiceNew();
        }
        return ShopServiceNew.I;
    }

    public void opendShop(Player player, String tagName, boolean allGender) {
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            openShopType4(player, tagName, player.inventory.itemsBoxCrackBall);
            return;
        } else if (tagName.equals("ITEMS_REWARD")) {
            player.getSession().initItemsReward();
            return;
        }
        try {
            Shop shop = this.getShop(tagName);
            shop = this.resolveShop(player, shop, allGender);
            switch (shop.typeShop) {
                case NORMAL_SHOP -> openShopType0(player, shop);
                case SPEC_SHOP -> openShopType3(player, shop);
            }
        } catch (Exception e) {
            Logger.logException(ShopServiceNew.class, e);
            Service.getInstance().sendThongBao(player, e.getMessage());
        }
    }

    private Shop getShop(String tagName) throws Exception {
        for (Shop s : Manager.SHOPS) {
            if (s.tagName != null && s.tagName.equals(tagName)) {
                return s;
            }
        }
        throw new Exception("Shop " + tagName + " không tồn tại!");
    }

    private Shop resolveShop(Player player, Shop shop, boolean allGender) {
        if (shop.tagName != null && (shop.tagName.equals("BUA_1H")
                || shop.tagName.equals("BUA_8H") || shop.tagName.equals("BUA_1M"))) {
            return this.resolveShopBua(player, new Shop(shop));
        }
        return allGender ? new Shop(shop) : new Shop(shop, player.gender);
    }

    private Shop resolveShopBua(Player player, Shop s) {
        for (TabShop tabShop : s.tabShops) {
            for (ItemShop item : tabShop.itemShops) {
                long min = switch (item.temp.id) {
                    case 213 -> {
                        long timeTriTue = player.charms.tdTriTue;
                        long current = System.currentTimeMillis();
                        yield (timeTriTue - current) / 60000;
                    }
                    case 214 -> (player.charms.tdManhMe - System.currentTimeMillis()) / 60000;
                    case 215 -> (player.charms.tdDaTrau - System.currentTimeMillis()) / 60000;
                    case 216 -> (player.charms.tdOaiHung - System.currentTimeMillis()) / 60000;
                    case 217 -> (player.charms.tdBatTu - System.currentTimeMillis()) / 60000;
                    case 218 -> (player.charms.tdDeoDai - System.currentTimeMillis()) / 60000;
                    case 219 -> (player.charms.tdThuHut - System.currentTimeMillis()) / 60000;
                    case 522 -> (player.charms.tdDeTu - System.currentTimeMillis()) / 60000;
                    case 671 -> (player.charms.tdTriTue3 - System.currentTimeMillis()) / 60000;
                    case 672 -> (player.charms.tdTriTue4 - System.currentTimeMillis()) / 60000;
                    default -> 0;
                };
                if (min > 0) {
                    item.options.clear();
                    if (min >= 1440) {
                        item.options.add(new Item.ItemOption(63, (int) min / 1440));
                    } else if (min >= 60) {
                        item.options.add(new Item.ItemOption(64, (int) min / 60));
                    } else {
                        item.options.add(new Item.ItemOption(65, (int) min));
                    }
                }
            }
        }
        return s;
    }

    private void openShopType0(Player player, Shop shop) {
        if (shop != null) {
            player.iDMark.setShopOpen(shop);
            player.iDMark.setTagNameShop(shop.tagName);
            Message msg;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(NORMAL_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        if (itemShop.typeSell == COST_GOLD) {
                            msg.writer().writeInt(itemShop.cost);
                            msg.writer().writeInt(0);
                        } else if (itemShop.typeSell == COST_GEM) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_RUBY) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_COUPON) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        } else if (itemShop.typeSell == COST_EVENT) {
                            msg.writer().writeInt(0);
                            msg.writer().writeInt(itemShop.cost);
                        }
                        msg.writer().writeByte(itemShop.options.size());
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(ShopServiceNew.class, e);
            }
        } else {
            Logger.error("Shop null");
        }
    }

    private void openShopType3(Player player, Shop shop) {
        if (shop != null) {
            player.iDMark.setShopOpen(shop);
            player.iDMark.setTagNameShop(shop.tagName);
            Message msg;
            try {
                msg = new Message(-44);
                msg.writer().writeByte(SPEC_SHOP);
                msg.writer().writeByte(shop.tabShops.size());
                for (TabShop tab : shop.tabShops) {
                    msg.writer().writeUTF(tab.name);
                    msg.writer().writeByte(tab.itemShops.size());
                    for (ItemShop itemShop : tab.itemShops) {
                        msg.writer().writeShort(itemShop.temp.id);
                        msg.writer().writeShort(itemShop.iconSpec);
                        msg.writer().writeInt((InventoryServiceNew.gI().findItemBag(player, 459) != null && itemShop.iconSpec == 7743) ? itemShop.cost / 2 : itemShop.cost);
                        msg.writer().writeByte(itemShop.options.size());
                        for (Item.ItemOption option : itemShop.options) {
                            msg.writer().writeByte(option.optionTemplate.id);
                            msg.writer().writeShort(option.param);
                        }
                        msg.writer().writeByte(itemShop.isNew ? 1 : 0);
                        if (itemShop.temp.type == 5) {
                            msg.writer().writeByte(1);
                            msg.writer().writeShort(itemShop.temp.head);
                            msg.writer().writeShort(itemShop.temp.body);
                            msg.writer().writeShort(itemShop.temp.leg);
                            msg.writer().writeShort(-1);
                        } else {
                            msg.writer().writeByte(0);
                        }
                    }
                }
                player.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
                Logger.logException(ShopServiceNew.class, e);
            }
        } else {
            Logger.error("Shop null");
        }
    }

    private void openShopType4(Player player, String tagName, List<Item> items) {
        if (items == null) {
            return;
        }
        player.iDMark.setTagNameShop(tagName);
        Message msg;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(4);
            msg.writer().writeByte(1);
            msg.writer().writeUTF("Phần\nthưởng");
            msg.writer().writeByte(items.size());
            for (Item item : items) {
                msg.writer().writeShort(item.template.id);
                msg.writer().writeUTF("\n|7|Chào mừng đến với Ngọc rồng Tabi");
                msg.writer().writeByte(item.itemOptions.size() + 1);
                for (Item.ItemOption io : item.itemOptions) {
                    msg.writer().writeByte(io.optionTemplate.id);
                    msg.writer().writeShort(io.param);
                }
                //số lượng
                msg.writer().writeByte(31);
                msg.writer().writeShort(item.quantity);
                //
                msg.writer().writeByte(1);
                if (item.template.type == 5) {
                    msg.writer().writeByte(1);
                    msg.writer().writeShort(item.template.head);
                    msg.writer().writeShort(item.template.body);
                    msg.writer().writeShort(item.template.leg);
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeByte(0);
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            System.out.println("bbbbb");
        }
    }

    public boolean VatphamKoMuanhieu(Player player, int id) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop it = shop.getItemShop(id);
        return it != null && it.temp.isUpToUp;
    }

    public void takeItem(Player player, byte type, int tempId) {
        String tagName = player.iDMark.getTagNameShop();
        player.idItemHighPurchase = tempId;
        if (tagName == null || tagName.isEmpty()) {
            return;
        }
        if (tagName.equals("ITEMS_LUCKY_ROUND")) {
            int index = -1;
            for (Item ITEM : player.inventory.itemsBoxCrackBall) {
                if (ITEM.template.id == tempId) {
                    index = player.inventory.itemsBoxCrackBall.indexOf(ITEM);
                }
            }
            if (index >= 0) {
                getItemSideBoxLuckyRound(player, player.inventory.itemsBoxCrackBall, type, index);
            } else {
                Service.getInstance().sendThongBao(player, "Không tìm thấy item");
            }
            return;
        }
        if (tagName.equals("ITEMS_REWARD")) {
            return;
        }
        if (player.iDMark.getShopOpen() == null) {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (tagName.equals("BUA_1H") || tagName.equals("BUA_8H") || tagName.equals("BUA_1M")) {
            buyItemBua(player, tempId);
        } else {
            if (VatphamKoMuanhieu(player, tempId) && player.isHighPurchaseVolume) {
                if (player.highPurchaseCount == 0 && player.idItemHighPurchase != -1) {
                    Input.gI().muanhieu(player);
                }
            } else {
                buyItem(player, tempId);
            }
        }
        Service.getInstance().sendMoney(player);
    }

    private boolean subMoneyByItemShop(Player player, ItemShop is) {
        int gold = 0;
        int gem = 0;
        int ruby = 0;
        int coupon = 0;
        int event = 0;
        switch (is.typeSell) {
            case COST_GOLD -> gold = is.cost;
            case COST_GEM -> gem = is.cost;
            case COST_RUBY -> ruby = is.cost;
            case COST_COUPON -> coupon = is.cost;
            case COST_EVENT -> event = is.cost;
        }
        if (player.inventory.gold < gold) {
            Service.getInstance().sendThongBao(player, "Bạn không có đủ vàng");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return false;
        } else if (player.inventory.gem < gem) {
            Service.getInstance().sendThongBao(player, "Bạn không có đủ ngọc");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return false;
        } else if (player.inventory.ruby < ruby) {
            Service.getInstance().sendThongBao(player, "Bạn không có đủ hồng ngọc");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return false;
        } else if (player.inventory.coupon < coupon) {
            Service.getInstance().sendThongBao(player, "Bạn không có đủ điểm");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return false;
        } else if (player.inventory.event < event) {
            Service.getInstance().sendThongBao(player, "Bạn không có đủ điểm săn Boss");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return false;
        }
        player.inventory.gold -= is.temp.gold;
        player.inventory.gem -= is.temp.gem;
        player.inventory.ruby -= ruby;
        player.inventory.coupon -= coupon;
        player.inventory.event -= event;
        return true;
    }

    /**
     * Mua bùa
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    private void buyItemBua(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            return;
        }
        if (!subMoneyByItemShop(player, is)) {
            return;
        }
        InventoryServiceNew.gI().addItemBag(player, ItemService.gI().createItemFromItemShop(is));
        InventoryServiceNew.gI().sendItemBags(player);
        opendShop(player, shop.tagName, true);
    }

    /**
     * Mua vật phẩm trong cửa hàng
     *
     * @param player người chơi
     * @param itemTempId id template vật phẩm
     */
    public void buyItem(Player player, int itemTempId) {
        Shop shop = player.iDMark.getShopOpen();
        ItemShop is = shop.getItemShop(itemTempId);
        if (is == null) {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return;
        }
        if (InventoryServiceNew.gI().getCountEmptyBag(player) == 0) {
            Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
            player.highPurchaseCount = 0;
            player.idItemHighPurchase = -1;
            return;
        }
        if (shop.typeShop == ShopServiceNew.NORMAL_SHOP) {
            if (!subMoneyByItemShop(player, is)) {
                return;
            }
        } else if (shop.typeShop == ShopServiceNew.SPEC_SHOP) {
            if (!this.subIemByItemShop(player, is)) {
                return;
            }
        }
        Item item = ItemService.gI().createItemFromItemShop(is);
        InventoryServiceNew.gI().addItemBag(player, item);
        InventoryServiceNew.gI().sendItemBags(player);
        Service.getInstance().sendThongBao(player, "Mua thành công " + is.temp.name);
    }

    private void _________________Bán_vật_phẩm______________________________() {
        //**********************************************************************
    }

    private boolean subIemByItemShop(Player pl, ItemShop itemShop) {
        boolean isBuy = false;
        short itSpec = ItemService.gI().getItemIdByIcon((short) itemShop.iconSpec);
        int buySpec = (InventoryServiceNew.gI().findItemBag(pl, 459) != null) ? itemShop.cost / 2 : itemShop.cost;
        Item itS = ItemService.gI().createNewItem(itSpec);
        switch (itS.template.id) {
            case 76:
            case 188:
            case 189:
            case 190:
                if (itemShop.id == 457 && pl.inventory.gold >= 600000000) {
                    pl.inventory.gold -= 600000000;
                    isBuy = true;
                } else if (pl.inventory.gold >= buySpec) {
                    pl.inventory.gold -= buySpec;
                    isBuy = true;
                } else {
                    Service.getInstance().sendThongBao(pl, "Bạn Không Đủ Vàng Để Mua Vật Phẩm");
                    pl.highPurchaseCount = 0;
                    pl.idItemHighPurchase = -1;
                    isBuy = false;
                }
                break;
            case 861:
                if (itemShop.tabShop.shop.tagName.equals("BILL")) {
                    for (Item i : pl.inventory.itemsBag) {
                        if (i.template != null) {
                            if (ItemData.list_thuc_an.contains((int) i.template.id)) {
                                if (InventoryServiceNew.gI().findItemBag(pl, i.template.id).quantity < 99) {
                                    Service.getInstance().sendThongBao(pl, "Không đủ số lượng thức ăn");
                                    pl.highPurchaseCount = 0;
                                    pl.idItemHighPurchase = -1;
                                    return false;
                                } else {
                                    if (pl.inventory.ruby >= buySpec) {
                                        pl.inventory.ruby -= buySpec;
                                        InventoryServiceNew.gI().subQuantityItemsBag(pl, InventoryServiceNew.gI().findItemBag(pl, i.template.id), 99);
//                                    InventoryServiceNew.gI().subQuantityItemsBag(pl, InventoryServiceNew.gI().findItemBag(pl, itSpec), buySpec);
                                        InventoryServiceNew.gI().sendItemBags(pl);
                                        Service.getInstance().sendMoney(pl);
                                        return true;
                                    } else {
                                        Service.getInstance().sendThongBao(pl, "Bạn Không Đủ Hồng Ngọc Để Mua Vật Phẩm");
                                        pl.highPurchaseCount = 0;
                                        pl.idItemHighPurchase = -1;
                                        return false;
                                    }
                                }
                            }
                        }

                    }
                    Service.getInstance().sendThongBao(pl, "Không tìm thấy thức ăn");
                    pl.highPurchaseCount = 0;
                    pl.idItemHighPurchase = -1;
                    return false;
                }

                for (Item i : pl.inventory.itemsBag) {
                    if (i.template != null) {
                        if (ItemData.phieu.contains((int) i.template.id)) {
                            if (InventoryServiceNew.gI().findItemBag(pl, i.template.id).quantity > 0) {
                                if (pl.inventory.ruby >= buySpec) {
                                    InventoryServiceNew.gI().subQuantityItemsBag(pl, InventoryServiceNew.gI().findItemBag(pl, i.template.id), 1);
                                    InventoryServiceNew.gI().sendItemBags(pl);
                                    Service.getInstance().sendMoney(pl);
                                } else {
                                    Service.getInstance().sendThongBao(pl, "Bạn Không Đủ Hồng Ngọc Để Mua Vật Phẩm");
                                    isBuy = false;
                                    pl.highPurchaseCount = 0;
                                    pl.idItemHighPurchase = -1;
                                }
                            } else {
                                Service.getInstance().sendThongBao(pl, "Bạn Không Đủ Phiếu giảm giá Để Mua Vật Phẩm");
                                isBuy = false;
                                pl.highPurchaseCount = 0;
                                pl.idItemHighPurchase = -1;
                            }
                        }
                    }
                }
                if (pl.inventory.ruby >= buySpec) {
                    pl.inventory.ruby -= buySpec;
                    Service.getInstance().sendMoney(pl);
                    isBuy = true;
                } else {
                    Service.getInstance().sendThongBao(pl, "Bạn Không Đủ Hồng Ngọc Để Mua Vật Phẩm");
                    isBuy = false;
                    pl.highPurchaseCount = 0;
                    pl.idItemHighPurchase = -1;
                }
                break;
            default:
                if (InventoryServiceNew.gI().findItemBag(pl, itSpec) == null || !InventoryServiceNew.gI().findItemBag(pl, itSpec).isNotNullItem()) {
                    Service.getInstance().sendThongBao(pl, "Không tìm thấy " + itS.template.name);
                    isBuy = false;
                    pl.highPurchaseCount = 0;
                    pl.idItemHighPurchase = -1;
                } else if (InventoryServiceNew.gI().findItemBag(pl, itSpec).quantity < buySpec) {
                    Service.getInstance().sendThongBao(pl, "Bạn không có đủ " + buySpec + " " + itS.template.name);
                    isBuy = false;
                    pl.highPurchaseCount = 0;
                    pl.idItemHighPurchase = -1;
                } else {
                    InventoryServiceNew.gI().subQuantityItemsBag(pl, InventoryServiceNew.gI().findItemBag(pl, itSpec), buySpec);
                    isBuy = true;
                }
                break;
        }
        return isBuy;
    }

    public void showConfirmSellItem(Player pl, int where, int index) {
        if (index == 2 || index == 1) {
            return;
        }
        Item item = null;
        if (where == 0) {
            item = pl.inventory.itemsBody.get(index);
        } else {
            if (pl.getSession().version < 220) {
                index -= (pl.inventory.itemsBody.size() - 7);
            }
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null && item.isNotNullItem() && item.template.id == 457) {
            NpcService.gI().createMenuConMeo(pl, ConstNpc.BAN_NHIEU_THOI_VANG, 7710,
                    "|7|BÁN NHIỀU THỎI VÀNG\n"
                            + "|-1|Theo nguyện vọng góp ý từ các chiến binh, ta được Admin Ngọc Rồng Tabi\n"
                            + "giao cho trọng trách hỗ trợ Bán thỏi vàng số lượng nhiều\n"
                            + "|0|Ngươi muốn bán số lượng bao nhiêu Thỏi vàng?",
                    "Bán X1\nThỏi vàng\n(500 Triệu)", "Bán X5\nThỏi vàng\n(2 Tỷ 5)", "Bán X10\nThỏi vàng\n(5 Tỷ)",
                    "Bán X100\nThỏi vàng\n(50 Tỷ)");
            return;
        }
        if (item != null && item.isNotNullItem()) {
            int quantity = item.quantity;
            int cost = item.template.gold;
            if (item.template.id == 457) {
                quantity = 1;
            } else {
                cost /= 4;
            }
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            String text = "Bạn có muốn bán\nx" + quantity
                    + " " + item.template.name + "\nvới giá là " + Util.numberToMoney(cost) + " vàng?";
            Message msg = new Message(7);
            try {
                msg.writer().writeByte(where);
                msg.writer().writeShort(index);
                msg.writer().writeUTF(text);
                pl.sendMessage(msg);
                msg.cleanup();
            } catch (Exception e) {
            }
        }
    }

    public void sellItem(Player pl, int where, int index) {
        Item item = null;
        if (where == 0) {
            item = pl.inventory.itemsBody.get(index);
        } else {
            item = pl.inventory.itemsBag.get(index);
        }
        if (item != null) {
            int quantity = item.quantity;
            int cost = item.template.gold;
            if (item.template.id == 1132) {
                Service.getInstance().sendThongBao(pl, "Không thể bán vật phẩm này");
                return;
            }
            if (item.template.id == 457) {
                quantity = 1;
            } else {
                cost /= 4;
            }
            if (cost == 0) {
                cost = 1;
            }
            cost *= quantity;

            if (pl.inventory.gold + cost > Inventory.LIMIT_GOLD) {
                Service.getInstance().sendThongBao(pl, "Vàng sau khi bán vượt quá giới hạn");
                return;
            }
            pl.inventory.gold += cost;
            Service.getInstance().sendMoney(pl);
            Service.getInstance().sendThongBao(pl, "Đã bán " + item.template.name
                    + " thu được " + Util.numberToMoney(cost) + " vàng");
            if (item.template.type >= 0 && item.template.type <= 4) {
                pl.achievement.plusCount(10);
            }
            if (item.template.id == 457) {
                pl.achievement.plusCount(13);
            }
            if (where == 0) {
                InventoryServiceNew.gI().subQuantityItemsBody(pl, item, quantity);
                InventoryServiceNew.gI().sendItemBody(pl);
                Service.getInstance().Send_Caitrang(pl);
            } else {
                InventoryServiceNew.gI().subQuantityItemsBag(pl, item, quantity);
                InventoryServiceNew.gI().sendItemBags(pl);
            }
        } else {
            Service.getInstance().sendThongBao(pl, "Không thể thực hiện");
        }
    }

    private void getItemSideBoxLuckyRound(Player player, List<Item> items, byte type, int index) {
        if (items == null || items.isEmpty()) {
            return;
        }
        Item item = items.get(index);
        switch (type) {
            case 0: //nhận
                if (item.isNotNullItem()) {
                    if (InventoryServiceNew.gI().getCountEmptyBag(player) != 0) {
                        InventoryServiceNew.gI().addItemBag(player, item);
                        Service.getInstance().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        InventoryServiceNew.gI().sendItemBags(player);
                        items.remove(index);
                    } else {
                        Service.getInstance().sendThongBao(player, "Hành trang đã đầy");
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                }
                break;
            case 1: //xóa
                items.remove(index);
                Service.getInstance().sendThongBao(player, "Xóa vật phẩm thành công");
                break;
            case 2: //nhận hết
                for (int i = items.size() - 1; i >= 0; i--) {
                    item = items.get(i);
                    if (InventoryServiceNew.gI().addItemBag(player, item)) {
                        Service.getInstance().sendThongBao(player,
                                "Bạn nhận được " + (item.template.id == 189
                                        ? Util.numberToMoney(item.quantity) + " vàng" : item.template.name));
                        items.remove(i);
                    }
                }
                InventoryServiceNew.gI().sendItemBags(player);
                break;
        }
        openShopType4(player, player.iDMark.getTagNameShop(), items);
    }
}

