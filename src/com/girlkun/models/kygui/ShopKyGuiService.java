package com.girlkun.models.kygui;

import com.girlkun.consts.ConstNpc;
import com.girlkun.models.item.Item;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.NpcService;
import com.girlkun.services.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * author BTH
 */
public class ShopKyGuiService {

    private static ShopKyGuiService instance;

    public static ShopKyGuiService gI() {
        if (instance == null) {
            instance = new ShopKyGuiService();
        }
        return instance;
    }

    private List<ItemKyGui> getItemKyGui(Player pl, byte tab, byte... max) {
        List<ItemKyGui> its = new ArrayList<>();
        List<ItemKyGui> listSort = new ArrayList<>();
        List<ItemKyGui> listSort2 = new ArrayList<>();
        ShopKyGuiManager.gI().listItem.stream().filter((it) -> (it != null && it.tab == tab && !it.isBuy && it.player_sell != pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.isUpTop, Comparator.reverseOrder())).forEach(i -> listSort.add(i));
        if (max.length == 2) {
            if (listSort.size() > max[1]) {
                for (int i = max[0]; i < max[1]; i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            } else {
                for (int i = max[0]; i <= max[0]; i++) {
                    if (listSort.get(i) != null) {
                        listSort2.add(listSort.get(i));
                    }
                }
            }
            return listSort2;
        }
        if (max.length == 1 && listSort.size() > max[0]) {
            for (int i = 0; i < max[0]; i++) {
                if (listSort.get(i) != null) {
                    listSort2.add(listSort.get(i));
                }
            }
            return listSort2;
        }
        return listSort;
    }

    private List<ItemKyGui> getItemKyGui() {
        List<ItemKyGui> its = new ArrayList<>();
        List<ItemKyGui> listSort = new ArrayList<>();
        ShopKyGuiManager.gI().listItem.stream().filter((it) -> (it != null && !it.isBuy)).forEachOrdered((it) -> {
            its.add(it);
        });
        its.stream().filter(i -> i != null).sorted(Comparator.comparing(i -> i.isUpTop, Comparator.reverseOrder())).forEach(i -> listSort.add(i));
        return listSort;
    }

    public void buyItem(Player pl, int id) {
        ItemKyGui it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã có người khác mua");
            openShopKyGui(pl);
            return;
        }
        if (it.player_sell == pl.id) {
            Service.gI().sendThongBao(pl, "Không thể mua vật phẩm do chính bạn bán");
            openShopKyGui(pl);
            return;
        }
        boolean isBuy = false;
        if (it.goldSell > 0) {
            if (pl.inventory.gold >= it.goldSell) {
                pl.inventory.gold -= it.goldSell;
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ vàng để mua vật phẩm này!");
                isBuy = false;
                openShopKyGui(pl);
            }
        } else if (it.gemSell > 0) {
            if (pl.inventory.ruby >= it.gemSell) {
                pl.inventory.ruby-= it.gemSell;
                isBuy = true;
            } else {
                Service.gI().sendThongBao(pl, "Bạn không đủ hồng ngọc để mua vật phẩm này!");
                isBuy = false;
                openShopKyGui(pl);
            }
        }
        Service.gI().sendMoney(pl);
        if (isBuy) {
            Item item = ItemService.gI().createNewItem(it.itemId);
            item.quantity = it.quantity;
            item.itemOptions.addAll(it.options);
            it.isBuy = true;
            if (it.isBuy) {
                InventoryServiceNew.gI().addItemBag(pl, item);
                InventoryServiceNew.gI().sendItemBags(pl);
                Service.gI().sendThongBao(pl, "Bạn đã nhận được " + item.template.name);
                openShopKyGui(pl);
            }
        }
    }

    public ItemKyGui getItemBuy(int id) {
        for (ItemKyGui it : getItemKyGui()) {
            if (it != null && it.id == id) {
                return it;
            }
        }
        return null;
    }

    public ItemKyGui getItemBuy(Player pl, int id) {
        for (ItemKyGui it : ShopKyGuiManager.gI().listItem) {
            if (it != null && it.id == id && it.player_sell == pl.id) {
                return it;
            }
        }
        return null;
    }

    public void openShopKyGui(Player pl, byte index, int page) {
        if (page > getItemKyGui(pl, index).size()) {
            return;
        }
        Message msg = null;
        try {
            msg = new Message(-100);
            msg.writer().writeByte(index);
            List<ItemKyGui> items = getItemKyGui(pl, index);
            List<ItemKyGui> itemsSend = getItemKyGui(pl, index, (byte) (page * 100), (byte) (page * 100 + 100));
            byte tab = (byte) (items.size() / 100 > 0 ? items.size() / 100 : 1);
            msg.writer().writeByte(tab); // max page
            msg.writer().writeByte(page);
            msg.writer().writeByte(itemsSend.size());
            for (int j = 0; j < itemsSend.size(); j++) {
                ItemKyGui itk = itemsSend.get(j);
                Item it = ItemService.gI().createNewItem(itk.itemId);
                it.itemOptions.clear();
                if (itk.options.isEmpty()) {
                    it.itemOptions.add(new ItemOption(73, 0));
                } else {
                    it.itemOptions.addAll(itk.options);
                }
                msg.writer().writeShort(it.template.id);
                msg.writer().writeShort(itk.id);
                msg.writer().writeInt(itk.goldSell);
                msg.writer().writeInt(itk.gemSell);
                msg.writer().writeByte(0); // buy type
                if (pl.getSession().version >= 222) {
                    msg.writer().writeInt(itk.quantity);
                } else {
                    msg.writer().writeByte(itk.quantity);
                }
                msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                msg.writer().writeByte(it.itemOptions.size());
                for (int a = 0; a < it.itemOptions.size(); a++) {
                    msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                    msg.writer().writeShort(it.itemOptions.get(a).param);
                }
                msg.writer().writeByte(0);
                msg.writer().writeByte(0);
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("fffff");
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }

    public void upItemToTop(Player pl, int id) {
        ItemKyGui it = getItemBuy(id);
        if (it == null || it.isBuy) {
            Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được người khác mua");
            openShopKyGui(pl);
            return;
        }
        if (it.player_sell != pl.id) {
            Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
            openShopKyGui(pl);
            return;
        }
        pl.iDMark.setIdItemUpTop(id);
        NpcService.gI().createMenuConMeo(pl, ConstNpc.UP_TOP_ITEM, -1, "Bạn có muốn đưa vật phẩm ['" + ItemService.gI().createNewItem(it.itemId).template.name + "'] của bản thân lên trang đầu?\nYêu cầu 50 hồng ngọc.", "Đồng ý", "Từ Chối");
    }

    
    public void claimOrDel(Player pl, byte action, int id) {
        ItemKyGui it = getItemBuy(pl, id);
        switch (action) {
            case 1: // hủy vật phẩm
                if (it == null || it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc đã được bán");
                    openShopKyGui(pl);
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                Item item = ItemService.gI().createNewItem(it.itemId);
                item.quantity = it.quantity;
                item.itemOptions.addAll(it.options);
                if (ShopKyGuiManager.gI().listItem.remove(it)) {
                    InventoryServiceNew.gI().addItemBag(pl, item);
                    InventoryServiceNew.gI().sendItemBags(pl);
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "Hủy bán vật phẩm thành công");
                    openShopKyGui(pl);
                }
                break;
            case 2: // nhận tiền
                if (it == null || !it.isBuy) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không tồn tại hoặc chưa được bán");
                    openShopKyGui(pl);
                    return;
                }
                if (it.player_sell != pl.id) {
                    Service.gI().sendThongBao(pl, "Vật phẩm không thuộc quyền sở hữu");
                    openShopKyGui(pl);
                    return;
                }
                if (it.goldSell > 0) {
                    pl.inventory.gold += it.goldSell - it.goldSell * 5 / 100;
                } else if (it.gemSell > 0) {
                    pl.inventory.ruby += it.gemSell - it.gemSell * 5 / 100;
                }
                if (ShopKyGuiManager.gI().listItem.remove(it)) {
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "Bạn đã bán vật phẩm thành công");
                    openShopKyGui(pl);
                }
                break;
                
        }
    }

    public List<ItemKyGui> getItemCanKiGui(Player pl) {
        List<ItemKyGui> its = new ArrayList<>();
        ShopKyGuiManager.gI().listItem.stream().filter((it) -> (it != null && it.player_sell == pl.id)).forEachOrdered((it) -> {
            its.add(it);
        });
        pl.inventory.itemsBag.stream().filter((it) -> (it.isNotNullItem() && (InventoryServiceNew.gI().hasOptionTemplateId(it,86) || InventoryServiceNew.gI().hasOptionTemplateId(it,87)))).forEachOrdered((it) -> {
            its.add(new ItemKyGui(InventoryServiceNew.gI().getIndexBag(pl, it), it.template.id, (int) pl.id, (byte) 4, -1, -1, it.quantity, (byte) -1, it.itemOptions, false, System.currentTimeMillis()));
        });
        return its;
    }

    public int getMaxId() {
        try {
            List<Integer> id = new ArrayList<>();
            ShopKyGuiManager.gI().listItem.stream().filter((it) -> (it != null)).forEachOrdered((it) -> {
                id.add(it.id);
            });
            return Collections.max(id);
        } catch (Exception e) {
                System.out.println("        loi maxid");
            return 0;
        }
    }

    public byte getTabKiGui(Item it) {
        if (it.template.type >= 0 && it.template.type <= 2) {
            return 0;
        } else if ((it.template.type >= 3 && it.template.type <= 4) || it.template.type == 12 || it.template.type == 33) {
            return 1;
        } else if (it.template.type == 29) {
            return 2;
        } else {
            return 3;
        }
    }

    public void KiGui(Player pl, int id, int money, byte moneyType, int quantity) {
        try {
            if (pl.inventory.ruby < 5) {
                Service.gI().sendThongBao(pl, "Bạn cần có ít nhất 5 hồng ngọc để làm phí ký gửi");
                openShopKyGui(pl);
                return;
            }
            Item it = ItemService.gI().copyItem(pl.inventory.itemsBag.get(id));
            if (money <= 0 || quantity > it.quantity) {
                Service.gI().sendThongBao(pl, "Có lỗi xảy ra");
                openShopKyGui(pl);
                return;
            }
            if (quantity < 1 || quantity > 999) {
                Service.gI().sendThongBao(pl, "Ký gửi tối đa x999 vật phẩm");
                openShopKyGui(pl);
                return;
            }
            pl.inventory.ruby -= 5;
            switch (moneyType) {
                case 0:// vàng
                    InventoryServiceNew.gI().subQuantityItemsBag(pl, pl.inventory.itemsBag.get(id), quantity);
                    ShopKyGuiManager.gI().listItem.add(new ItemKyGui(getMaxId() + 1, it.template.id, (int) pl.id, getTabKiGui(it), money, -1, quantity, (byte) 0, it.itemOptions, false, System.currentTimeMillis()));
                    InventoryServiceNew.gI().sendItemBags(pl);
                    openShopKyGui(pl);
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "Đăng bán thành công");
                    break;
                case 1:// hồng ngọc
                    InventoryServiceNew.gI().subQuantityItemsBag(pl, pl.inventory.itemsBag.get(id), quantity);
                    ShopKyGuiManager.gI().listItem.add(new ItemKyGui(getMaxId() + 1, it.template.id, (int) pl.id, getTabKiGui(it), -1, money, quantity, (byte) 0, it.itemOptions, false, System.currentTimeMillis()));
                    InventoryServiceNew.gI().sendItemBags(pl);
                    openShopKyGui(pl);
                    Service.gI().sendMoney(pl);
                    Service.gI().sendThongBao(pl, "Đăng bán thành công");
                    break;
                default:
                    Service.gI().sendThongBao(pl, "Có lỗi xảy ra");
                    openShopKyGui(pl);
                    break;
            }
        } catch (Exception e) {
            System.out.println("ggggg");
        }
    }

    public void openShopKyGui(Player pl) {
        Message msg = null;
        try {
            msg = new Message(-44);
            msg.writer().writeByte(2);
            msg.writer().writeByte(5);
            for (byte i = 0; i < 5; i++) {
                if (i == 4) {
                    msg.writer().writeUTF(ShopKyGuiManager.gI().tabName[i]);
                    msg.writer().writeByte(0);
                    msg.writer().writeByte(getItemCanKiGui(pl).size());
                    for (int j = 0; j < getItemCanKiGui(pl).size(); j++) {
                        ItemKyGui itk = getItemCanKiGui(pl).get(j);
                        if (itk == null) {
                            continue;
                        }
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        if (getItemBuy(pl, itk.id) == null) {
                            msg.writer().writeByte(0); // buy type
                        } else if (itk.isBuy) {
                            msg.writer().writeByte(2);
                        } else {
                            msg.writer().writeByte(1);
                        }
                        if (pl.getSession().version >= 222) {
                            msg.writer().writeInt(itk.quantity);
                        } else {
                            msg.writer().writeByte(itk.quantity);
                        }
                        msg.writer().writeByte(1); // isMe
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                } else {
                    List<ItemKyGui> items = getItemKyGui(pl, i);
                    List<ItemKyGui> itemsSend = getItemKyGui(pl, i, (byte) 100);
                    msg.writer().writeUTF(ShopKyGuiManager.gI().tabName[i]);
                    byte tab = (byte) (items.size() / 100 > 0 ? items.size() / 100 : 1);
                    msg.writer().writeByte(tab); // max page
                    msg.writer().writeByte(itemsSend.size());
                    for (int j = 0; j < itemsSend.size(); j++) {
                        ItemKyGui itk = itemsSend.get(j);
                        Item it = ItemService.gI().createNewItem(itk.itemId);
                        it.itemOptions.clear();
                        if (itk.options.isEmpty()) {
                            it.itemOptions.add(new ItemOption(73, 0));
                        } else {
                            it.itemOptions.addAll(itk.options);
                        }
                        msg.writer().writeShort(it.template.id);
                        msg.writer().writeShort(itk.id);
                        msg.writer().writeInt(itk.goldSell);
                        msg.writer().writeInt(itk.gemSell);
                        msg.writer().writeByte(0); // buy type
                        if (pl.getSession().version >= 222) {
                            msg.writer().writeInt(itk.quantity);
                        } else {
                            msg.writer().writeByte(itk.quantity);
                        }
                        msg.writer().writeByte(itk.player_sell == pl.id ? 1 : 0); // isMe
                        msg.writer().writeByte(it.itemOptions.size());
                        for (int a = 0; a < it.itemOptions.size(); a++) {
                            msg.writer().writeByte(it.itemOptions.get(a).optionTemplate.id);
                            msg.writer().writeShort(it.itemOptions.get(a).param);
                        }
                        msg.writer().writeByte(0);
                        msg.writer().writeByte(0);
                    }
                }
            }
            pl.sendMessage(msg);
        } catch (Exception e) {
            System.out.println("hhhhh");
        } finally {
            if (msg != null) {
                msg.cleanup();
                msg = null;
            }
        }
    }
}
