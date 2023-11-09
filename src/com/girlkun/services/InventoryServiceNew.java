package com.girlkun.services;

import com.girlkun.models.item.Item;
import com.girlkun.models.map.blackball.BlackBallWar;
import com.girlkun.models.npc.specialnpc.MabuEgg;
import com.girlkun.models.npc.specialnpc.Timedua;
import com.girlkun.models.player.Inventory;
import com.girlkun.models.player.Pet;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Logger;

import java.util.ArrayList;
import java.util.List;

public class InventoryServiceNew {

    private static InventoryServiceNew I;

    public static InventoryServiceNew gI() {
        if (InventoryServiceNew.I == null) {
            InventoryServiceNew.I = new InventoryServiceNew();
        }
        return InventoryServiceNew.I;
    }

    public Item findItem(List<Item> list, int tempId) {
        for (Item item : list) {
            if (item.isNotNullItem() && item.template.id == tempId) {
                return item;
            }
        }
        return null;
    }

    public Item findItemBody(Player player, int tempId) {
        return this.findItem(player.inventory.itemsBody, tempId);
    }

    public Item findItemBag(Player player, int tempId) {
        return this.findItem(player.inventory.itemsBag, tempId);
    }

    public boolean isItemIdInBag(Player player, int tempId) {
        Item item;
        try {
            item = InventoryServiceNew.gI().findItemBag(player, tempId);
            return !(item == null || item.quantity < 1);
        } catch (Exception e) {
            return true;
        }
    }


    public boolean checkNotItemBag(Player player, int tempId) {
        return !this.isItemIdInBag(player, tempId);
    }

    public List<Item> copyList(List<Item> items) {
        List<Item> list = new ArrayList<>();
        for (Item item : items) {
            list.add(ItemService.gI().copyItem(item));
        }
        return list;
    }

    public List<Item> copyItemsBag(Player player) {
        return copyList(player.inventory.itemsBag);
    }

    public void throwItem(Player player, int where, int index) {
        Item itemThrow;
        if (where == 0) {
            removeItemBody(player, index);
            sendItemBody(player);
            Service.getInstance().Send_Caitrang(player);
        } else if (where == 1) {
            itemThrow = player.inventory.itemsBag.get(index);
            if (itemThrow.template.id != 457 && itemThrow.template.id != 1066 && itemThrow.template.id != 1067
                    && itemThrow.template.id != 1068 && itemThrow.template.id != 1069 && itemThrow.template.id != 1070
                    && itemThrow.template.id != 1322 && itemThrow.template.id != 1132) {
                if (itemThrow.template.type != 74) {
                    removeItemBag(player, index);
                    sortItems(player.inventory.itemsBag);
                    sendItemBags(player);
//                    if ((itemThrow.template.type >= 0 && itemThrow.template.type <= 4) || (itemThrow.template.id >= 222 && itemThrow.template.id <= 226)) {
//                        if (!itemThrow.isSKH() && !itemThrow.isDHD() && !itemThrow.isDTL() && !itemThrow.isDTS()) {
//                            ItemMap item = new ItemMap(player.zone, itemThrow.template.id, itemThrow.quantity, Util.nextInt((player.location.x - 50), (player.location.x + 50)), player.location.y, player.id);
//                            item.options = itemThrow.itemOptions;
//                            Service.getInstance().dropItemMap(player.zone, item);
//                        }
//                    }
                } else {
                    Service.getInstance().sendThongBao(player, "|7|Không thể vứt bỏ Chân mệnh");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Ngu mới vứt mấy cái này. THẰNG NGU !!!");
            }
        }
        Service.gI().sendFlagBag(player);
        Service.getInstance().Send_Caitrang(player);
        Service.getInstance().point(player);

    }


    public void removeItem(List<Item> items, int index) {
        Item item = ItemService.gI().createItemNull();
        items.set(index, item);
    }

    public void removeItem(List<Item> items, Item item) {
        if (item == null) {
            return;
        }
        Item it = ItemService.gI().createItemNull();
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).equals(item)) {
                items.set(i, it);
                item.dispose();
                break;
            }
        }
    }

    public void removeItemBag(Player player, int index) {
        this.removeItem(player.inventory.itemsBag, index);
    }

    public void removeItemBag(Player player, Item item) {
        this.removeItem(player.inventory.itemsBag, item);
    }

    public void removeItemBody(Player player, int index) {
        this.removeItem(player.inventory.itemsBody, index);
    }

    public void subQuantityItemsBag(Player player, Item item, int quantity) {
        subQuantityItem(player.inventory.itemsBag, item, quantity);
    }

    public void subQuantityItemsBody(Player player, Item item, int quantity) {
        subQuantityItem(player.inventory.itemsBody, item, quantity);
    }

    public void subQuantityItemsBox(Player player, Item item, int quantity) {
        subQuantityItem(player.inventory.itemsBox, item, quantity);
    }

    public void subQuantityItem(List<Item> items, Item item, int quantity) {
        if (item != null) {
            for (Item it : items) {
                if (item.equals(it)) {
                    it.quantity -= quantity;
                    if (it.quantity <= 0) {
                        this.removeItem(items, item);
                    }
                    break;
                }
            }
        }
    }

    public void sortItems(List<Item> list) {
        int first = -1;
        int last = -1;
        Item tempFirst = null;
        Item tempLast = null;
        for (int i = 0; i < list.size(); i++) {
            if (!list.get(i).isNotNullItem()) {
                first = i;
                tempFirst = list.get(i);
                break;
            }
        }
        for (int i = list.size() - 1; i >= 0; i--) {
            if (list.get(i).isNotNullItem()) {
                last = i;
                tempLast = list.get(i);
                break;
            }
        }
        if (first != -1 && last != -1 && first < last) {
            list.set(first, tempLast);
            list.set(last, tempFirst);
            sortItems(list);
        }
    }

    private Item putItemBag(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
            if (!player.inventory.itemsBag.get(i).isNotNullItem()) {
                player.inventory.itemsBag.set(i, item);
                return ItemService.gI().createItemNull();
            }
        }
        return item;
    }

    private Item putItemBox(Player player, Item item) {
        for (int i = 0; i < player.inventory.itemsBox.size(); i++) {
            if (!player.inventory.itemsBox.get(i).isNotNullItem()) {
                player.inventory.itemsBox.set(i, item);
                return ItemService.gI().createItemNull();
            }
        }
        return item;
    }

    private Item putItemBody(Player player, Item item) {
        Item sItem = item;
        if (!item.isNotNullItem()) {
            return sItem;
        }
        switch (item.template.type) {
            case 0, 1, 2, 3, 4, 5, 32, 23, 24, 11, 75, 74, 72, 27 -> {
            }
            default -> {
                Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
                return sItem;
            }
        }
        if (item.template.gender < 3 && item.template.gender != player.gender) {
            Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Trang bị không phù hợp!");
            return sItem;
        }
        long powerRequire = item.template.strRequire;
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 21) {
                powerRequire = io.param * 1000000000L;
                break;
            }
        }
        if (player.nPoint.power < powerRequire) {
            Service.getInstance().sendThongBaoOK(player.isPet ? ((Pet) player).master : player, "Sức mạnh không đủ yêu cầu!");
            return sItem;
        }
        int index = -1;
        switch (item.template.type) {
            case 0, 1, 2, 3, 4, 5 -> index = item.template.type;
            case 32 -> index = 6;
            case 23, 24 -> index = 9;
            case 11 -> {
                if (player.isPet) {
                    index = 7;
                } else {
                    index = 8;
                }
            }
            case 75, 72 -> index = 10;
            case 27 -> index = 7;
            case 74 -> index = 11;
        }
        sItem = player.inventory.itemsBody.get(index);
        player.inventory.itemsBody.set(index, item);
        return sItem;
    }

    public void itemBagToBody(Player player, int index) {
        Item item = player.inventory.itemsBag.get(index);
        Item pettt = player.inventory.itemsBody.get(7);
        if (item.isNotNullItem()) {
            player.inventory.itemsBag.set(index, putItemBody(player, item));
            if (item.template.id > 1299 && item.template.id < 1309) {
                Service.gI().removeTitle(player);
                Service.gI().sendFoot(player, item.template.id);
            }
            if (pettt != null && player.newpet != null && item.template.type == 27) {
                ChangeMapService.gI().exitMap(player.newpet);
                player.newpet.dispose();
                player.newpet = null;
            }
            sendItemBags(player);
            sendItemBody(player);
            Service.gI().sendFlagBag(player);
            Service.getInstance().Send_Caitrang(player);
            Service.getInstance().point(player);
        }
    }

    public void itemBodyToBag(Player player, int index) {
        Item item = player.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            if (index == 10) {
                Service.getInstance().sendPetFollow(player, (short) 0);
            }
            if (index == 7) {
                if (player.newpet != null) {
                    ChangeMapService.gI().exitMap(player.newpet);
                    player.newpet.dispose();
                    player.newpet = null;
                }
            }
            if (item.template.id > 1299 && item.template.id < 1309) {
                Service.gI().removeTitle(player);
                Service.gI().sendFoot(player, item.template.id);
            }
            player.inventory.itemsBody.set(index, putItemBag(player, item));
            sendItemBags(player);
            sendItemBody(player);
            Service.getInstance().sendFlagBag(player);
            Service.getInstance().Send_Caitrang(player);
            Service.getInstance().point(player);
        }
    }

    public void itemBagToPetBody(Player player, int index) {
        if (player.pet != null && player.pet.nPoint.power >= 1500000) {
            if (index >= player.inventory.itemsBag.size()) {
                return;
            }
            Item item = player.inventory.itemsBag.get(index);
            if (item != null && (item.template.id == 1326 || (item.template.type >= 0 && item.template.type <= 5) || item.template.type == 32)) {
                if (item.isNotNullItem()) {
                    Item itemSwap = putItemBody(player.pet, item);
                    player.inventory.itemsBag.set(index, itemSwap);
                    sendItemBags(player);
                    sendItemBody(player);
                    Service.getInstance().sendFlagBagPet(player.pet);
                    Service.getInstance().Send_Caitrang(player.pet);
                    Service.getInstance().Send_Caitrang(player);
                    if (!itemSwap.equals(item)) {
                        Service.getInstance().point(player);
                        Service.getInstance().showInfoPet(player);
                    }
                }
            } else {
                Service.getInstance().sendThongBaoOK(player, "Đệ tử không mang được vật phẩm này");
            }
        } else {
            Service.getInstance().sendThongBaoOK(player, "Đệ tử phải đạt 1tr5 sức mạnh mới có thể mặc");
        }
    }

    public void itemPetBodyToBag(Player player, int index) {
        Item item = player.pet.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            player.pet.inventory.itemsBody.set(index, putItemBag(player, item));
            sendItemBags(player);
            sendItemBody(player);
            Service.getInstance().sendFlagBagPet(player.pet);
            Service.getInstance().Send_Caitrang(player.pet);
            Service.getInstance().Send_Caitrang(player);
            Service.getInstance().point(player);
            Service.getInstance().showInfoPet(player);
        }
    }

    public void itemBoxToBodyOrBag(Player player, int index) {
        if (index < 0) {
            return;
        }
        Item item = player.inventory.itemsBox.get(index);
        if (item.isNotNullItem()) {
            boolean done = false;
            if (item.template.type >= 0 && item.template.type <= 5 || item.template.type == 32) {
                Item itemBody = player.inventory.itemsBody.get(item.template.type == 32 ? 6 : item.template.type);
                if (!itemBody.isNotNullItem()) {
                    if (item.template.gender == player.gender || item.template.gender == 3) {
                        long powerRequire = item.template.strRequire;
                        for (Item.ItemOption io : item.itemOptions) {
                            if (io.optionTemplate.id == 21) {
                                powerRequire = io.param * 1000000000L;
                                break;
                            }
                        }
                        if (powerRequire <= player.nPoint.power) {
                            player.inventory.itemsBody.set(item.template.type == 32 ? 6 : item.template.type, item);
                            player.inventory.itemsBox.set(index, itemBody);
                            done = true;

                            sendItemBody(player);
                            Service.getInstance().Send_Caitrang(player);
                            Service.getInstance().point(player);
                        }
                    }
                }
            }
            if (!done) {
                if (addItemBag(player, item)) {
                    if (item.quantity == 0) {
                        Item sItem = ItemService.gI().createItemNull();
                        player.inventory.itemsBox.set(index, sItem);
                    }
                    sendItemBags(player);
                }
            }
            sendItemBox(player);
        }
    }

    public void itemBagToBox(Player player, int index) {
        Item item = player.inventory.itemsBag.get(index);
        if (item != null) {
//            if (item.template.id == 457) {
//                Service.getInstance().sendThongBao(player, "Không thể cất vàng vào rương");
//                return;
//            }
//            if (item.template.id == 1066 || item.template.id == 1067 || item.template.id == 1068 || item.template.id == 1069 || item.template.id == 1070) {
//                Service.getInstance().sendThongBao(player, "Không thể cất vàng vào rương");
//                return;
//            }
            if (addItemBox(player, item)) {
                if (item.quantity == 0) {
                    Item sItem = ItemService.gI().createItemNull();
                    player.inventory.itemsBag.set(index, sItem);
                }
                sortItems(player.inventory.itemsBag);
                sendItemBags(player);
                sendItemBox(player);
            }
        }
    }

    public void itemBodyToBox(Player player, int index) {
        Item item = player.inventory.itemsBody.get(index);
        if (item.isNotNullItem()) {
            player.inventory.itemsBody.set(index, putItemBox(player, item));
            sortItems(player.inventory.itemsBag);
            sendItemBody(player);
            sendItemBox(player);
            Service.getInstance().Send_Caitrang(player);
            sendItemBody(player);
            Service.getInstance().point(player);
        }
    }

    public void sendItemBags(Player player) {
        sortItems(player.inventory.itemsBag);
        Message msg;
        try {
            msg = new Message(-36);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBag.size());
            for (int i = 0; i < player.inventory.itemsBag.size(); i++) {
                Item item = player.inventory.itemsBag.get(i);
                if (!item.isNotNullItem()) {
                    continue;
                }
                msg.writer().writeShort(item.template.id);
                msg.writer().writeInt(item.quantity);
                msg.writer().writeUTF(item.getInfo());
                msg.writer().writeUTF(item.getContent());
                msg.writer().writeByte(item.itemOptions.size()); //options
                for (int j = 0; j < item.itemOptions.size(); j++) {
                    msg.writer().writeByte(item.itemOptions.get(j).optionTemplate.id);
                    msg.writer().writeShort(item.itemOptions.get(j).param);
                }
            }

            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(InventoryServiceNew.class, e, "Error player: " + player.name);

        }
    }

    public void sendItemBody(Player player) {
        Message msg;
        try {
            msg = new Message(-37);
            msg.writer().writeByte(0);
            msg.writer().writeShort(player.getHead());
            msg.writer().writeByte(player.inventory.itemsBody.size());
            for (Item item : player.inventory.itemsBody) {
                if (!item.isNotNullItem()) {
                    msg.writer().writeShort(-1);
                } else {
                    msg.writer().writeShort(item.template.id);
                    msg.writer().writeInt(item.quantity);
                    msg.writer().writeUTF(item.getInfo());
                    msg.writer().writeUTF(item.getContent());
                    List<Item.ItemOption> itemOptions = item.itemOptions;
                    msg.writer().writeByte(itemOptions.size());
                    for (Item.ItemOption itemOption : itemOptions) {
                        msg.writer().writeByte(itemOption.optionTemplate.id);
                        msg.writer().writeShort(itemOption.param);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(InventoryServiceNew.class, e, "Error player: " + player.name);
        }
        Service.getInstance().Send_Caitrang(player);
    }

    public void sendItemBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(0);
            msg.writer().writeByte(player.inventory.itemsBox.size());
            for (Item it : player.inventory.itemsBox) {
                msg.writer().writeShort(it.isNotNullItem() ? it.template.id : -1);
                if (it.isNotNullItem()) {
                    msg.writer().writeInt(it.quantity);
                    msg.writer().writeUTF(it.getInfo());
                    msg.writer().writeUTF(it.getContent());
                    msg.writer().writeByte(it.itemOptions.size());
                    for (Item.ItemOption io : it.itemOptions) {
                        msg.writer().writeByte(io.optionTemplate.id);
                        msg.writer().writeShort(io.param);
                    }
                }
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(InventoryServiceNew.class, e, "Error player: " + player.name);

        }
        this.openBox(player);
    }

    public void openBox(Player player) {
        Message msg;
        try {
            msg = new Message(-35);
            msg.writer().writeByte(1);
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(InventoryServiceNew.class, e, "Error player: " + player.name);

        }
    }


    private boolean addItemSpecial(Player player, Item item) {
        //bùa
        if (item.template.type == 13) {
            int min = 0;
            try {
                String tagShopBua = player.iDMark.getShopOpen().tagName;
                min = switch (tagShopBua) {
                    case "BUA_1H" -> 60;
                    case "BUA_8H" -> 60 * 8;
                    case "BUA_1M" -> 60 * 24 * 30;
                    default -> min;
                };
            } catch (Exception e) {
                Logger.logException(InventoryServiceNew.class, e, "Error player: " + player.name);

            }
            player.charms.addTimeCharms(item.template.id, min);
            return true;
        }

        switch (item.template.id) {
            case 568 -> { //quả trứng
                if (player.mabuEgg == null) {
                    MabuEgg.createMabuEgg(player);
                }
                return true;
            }
            case 569 -> { //dưa hấu
                if (player.timedua == null) {
                    Timedua.createTimedua(player);
                }
                return true;
            }
            case 453 -> { //tàu tennis
                player.haveTennisSpaceShip = true;
                return true;
            }
            case 74 -> { //đùi gà nướng
                player.nPoint.setFullHpMp();
                PlayerService.gI().sendInfoHpMp(player);
                return true;
            }
        }
        return false;
    }

    public boolean addItemBag(Player player, Item item) {
        //ngọc rồng đen
        if (ItemMapService.gI().isBlackBall(item.template.id)) {
            return BlackBallWar.gI().pickBlackBall(player, item);
        }
        if (addItemSpecial(player, item)) {
            return true;
        }

        //gold, gem, ruby
        switch (item.template.type) {
            case 9 -> {
                if (player.inventory.gold + item.quantity <= Inventory.LIMIT_GOLD) {
                    player.inventory.gold += item.quantity;
                    Service.getInstance().sendMoney(player);
                    return true;
                } else {
                    Service.getInstance().sendThongBao(player, "Vàng sau khi nhặt quá giới hạn cho phép");
                    return false;
                }
            }
            case 10 -> {
                player.inventory.gem += item.quantity;
                Service.getInstance().sendMoney(player);
                return true;
            }
            case 34 -> {
                player.inventory.ruby += item.quantity;
                Service.getInstance().sendMoney(player);
                return true;
            }
        }

        //mở rộng hành trang - rương đồ
        if (item.template.id == 517) {
            if (player.inventory.itemsBag.size() < Inventory.MAX_ITEMS_BAG) {
                player.inventory.itemsBag.add(ItemService.gI().createItemNull());
                Service.getInstance().sendThongBaoOK(player, "Hành trang của bạn đã được mở rộng thêm 1 ô");
                return true;
            } else {
                Service.getInstance().sendThongBaoOK(player, "Hành trang của bạn đã đạt tối đa");
                return false;
            }
        } else if (item.template.id == 518) {
            if (player.inventory.itemsBox.size() < Inventory.MAX_ITEMS_BOX) {
                player.inventory.itemsBox.add(ItemService.gI().createItemNull());
                Service.getInstance().sendThongBaoOK(player, "Rương đồ của bạn đã được mở rộng thêm 1 ô");
                return true;
            } else {
                Service.getInstance().sendThongBaoOK(player, "Rương đồ của bạn đã đạt tối đa");
                return false;
            }
        }
        return addItemList(player.inventory.itemsBag, item);
    }

    public boolean addItemBox(Player player, Item item) {
        return addItemList(player.inventory.itemsBox, item);
    }

    public boolean addItemList(List<Item> items, Item itemAdd) {
        //nếu item ko có option, add option rỗng vào
        if (itemAdd.itemOptions.isEmpty()) {
            itemAdd.itemOptions.add(new Item.ItemOption(73, 0));
        }
        //item cộng thêm chỉ số param: tự động luyện tập
        int[] idParam = isItemIncrementalOption(itemAdd);
        if (idParam[0] != -1) {
            for (Item it : items) {
                if (it.isNotNullItem() && it.template.id == itemAdd.template.id) {
                    for (Item.ItemOption io : it.itemOptions) {
                        if (io.optionTemplate.id == idParam[0]) {
                            io.param += idParam[1];
                        }
                    }
                    return true;
                }
            }
        }
        //item tăng số lượng
        if (itemAdd.template != null && itemAdd.template.isUpToUp) {
            for (Item it : items) {
                if (!it.isNotNullItem() || it.template.id != itemAdd.template.id || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(itemAdd, 30))) {
                    continue;
                }
                if (!it.isNotNullItem() || it.template.id != itemAdd.template.id || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(itemAdd, 73))) {
                    continue;
                }
                if (!it.isNotNullItem() || it.template.id != itemAdd.template.id
                        || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(itemAdd, 86))
                        || (hasOptionTemplateId(it, 30) && hasOptionTemplateId(itemAdd, 87))
                        || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(itemAdd, 86))
                        || (hasOptionTemplateId(it, 73) && hasOptionTemplateId(itemAdd, 87))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(itemAdd, 30))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(itemAdd, 30))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(itemAdd, 73))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(itemAdd, 73))
                        || (hasOptionTemplateId(it, 87) && hasOptionTemplateId(itemAdd, 86))
                        || (hasOptionTemplateId(it, 86) && hasOptionTemplateId(itemAdd, 87))) {
                    continue;
                }
                //457-thỏi vàng; 590-bí kiếp
                if (itemAdd.template.id == 457 || itemAdd.template.type == 27 || itemAdd.template.type == 14 || itemAdd.template.type == 6
                        || itemAdd.template.type == 12 || itemAdd.template.type == 29 || itemAdd.template.type == 33 || itemAdd.template.type == 31) {
                    it.quantity += itemAdd.quantity;
                    itemAdd.quantity = 0;
                    return true;
                }
                if (it.quantity < 99) {
                    int add = 99 - it.quantity;
                    if (itemAdd.quantity <= add) {
                        it.quantity += itemAdd.quantity;
                        itemAdd.quantity = 0;
                        return true;
                    } else {
                        it.quantity = 99;
                        itemAdd.quantity -= add;
                    }
                }
            }
        }
        //add item vào ô mới
        if (itemAdd.quantity > 0) {
            for (int i = 0; i < items.size(); i++) {
                if (!items.get(i).isNotNullItem()) {
                    items.set(i, ItemService.gI().copyItem(itemAdd));
                    itemAdd.quantity = 0;
                    return true;
                }
            }
        }
        return false;
    }

    public boolean hasOptionTemplateId(Item item, int optionTemplateId) {
        for (Item.ItemOption option : item.itemOptions) {
            if (option.optionTemplate.id == optionTemplateId) {
                return true;
            }
        }
        return false;
    }

    private int[] isItemIncrementalOption(Item item) {
        for (Item.ItemOption io : item.itemOptions) {
            if (io.optionTemplate.id == 1) {
                return new int[]{io.optionTemplate.id, io.param};
            }
        }
        return new int[]{-1, -1};
    }

    public byte getCountEmptyBag(Player player) {
        return getCountEmptyListItem(player.inventory.itemsBag);
    }

    public byte getCountEmptyBody(Player player) {
        return getCountEmptyListItem(player.inventory.itemsBody);
    }

    public byte getCountEmptyListItem(List<Item> list) {
        byte count = 0;
        for (Item item : list) {
            if (!item.isNotNullItem()) {
                count++;
            }
        }
        return count;
    }

    public byte getIndexBag(Player pl, Item it) {
        for (byte i = 0; i < pl.inventory.itemsBag.size(); ++i) {
            Item item = pl.inventory.itemsBag.get(i);
            if (it.equals(item)) {
                return i;
            }
        }
        return -1;
    }

    public boolean finditemWoodChest(Player player) {
        for (Item item : player.inventory.itemsBag) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        for (Item item : player.inventory.itemsBox) {
            if (item.isNotNullItem() && item.template.id == 570) {
                return false;
            }
        }
        return true;
    }
}
