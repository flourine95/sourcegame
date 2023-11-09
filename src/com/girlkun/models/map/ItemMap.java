package com.girlkun.models.map;

import com.girlkun.models.Template.ItemTemplate;
import com.girlkun.models.item.Item.ItemOption;
import com.girlkun.models.player.Player;
import com.girlkun.services.ItemMapService;
import com.girlkun.services.ItemService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;

public class ItemMap {

    public Zone zone;
    public int itemMapId;
    public ItemTemplate itemTemplate;
    public int quantity;

    public int x;
    public int y;
    public long playerId;
    public List<ItemOption> options;

    private long createTime;

    public boolean isBlackBall;
    public boolean isNamecBall;
    public boolean isVetinh;
    public boolean isDoanhTraiBall;

    public ItemMap(Zone zone, int tempId, int quantity, int x, int y, long playerId) {
        this.zone = zone;
        this.itemMapId = zone.countItemAppeared++;
        if (zone.countItemAppeared >= 2000000000) {
            zone.countItemAppeared = 0;
        }
        this.itemTemplate = ItemService.gI().getTemplate((short) tempId);
        this.quantity = quantity;
        this.x = x;
        this.y = y;
        this.playerId = playerId != -1 ? Math.abs(playerId) : playerId;
        this.createTime = System.currentTimeMillis();
        this.options = new ArrayList<>();
        this.isBlackBall = ItemMapService.gI().isBlackBall(this.itemTemplate.id);
        this.isNamecBall = ItemMapService.gI().isNamecBall(this.itemTemplate.id);
        this.isVetinh = ItemMapService.gI().isVetinh(this.itemTemplate.id);
        this.lastTimeMoveToPlayer = System.currentTimeMillis();
        this.zone.addItem(this);
    }

    public ItemMap(Zone zone, ItemTemplate temp, int quantity, int x, int y, long playerId) {
        this.zone = zone;
        this.itemMapId = zone.countItemAppeared++;
        if (zone.countItemAppeared >= 2000000000) {
            zone.countItemAppeared = 0;
        }
        this.itemTemplate = temp;
        this.quantity = quantity;
        this.x = x;
        this.y = y;
        this.playerId = playerId != -1 ? Math.abs(playerId) : playerId;
        this.createTime = System.currentTimeMillis();
        this.options = new ArrayList<>();
        this.isBlackBall = ItemMapService.gI().isBlackBall(this.itemTemplate.id);
        this.isNamecBall = ItemMapService.gI().isNamecBall(this.itemTemplate.id);
        this.isVetinh = ItemMapService.gI().isVetinh(this.itemTemplate.id);
        this.lastTimeMoveToPlayer = System.currentTimeMillis();
        this.zone.addItem(this);
    }

    public ItemMap(ItemMap itemMap) {
        this.zone = itemMap.zone;
        this.itemMapId = itemMap.itemMapId;
        this.itemTemplate = itemMap.itemTemplate;
        this.quantity = itemMap.quantity;
        this.x = itemMap.x;
        this.y = itemMap.y;
        this.playerId = itemMap.playerId;
        this.options = itemMap.options;
        this.isBlackBall = itemMap.isBlackBall;
        this.isNamecBall = itemMap.isNamecBall;
        this.isVetinh = itemMap.isVetinh;
        this.lastTimeMoveToPlayer = itemMap.lastTimeMoveToPlayer;
        this.createTime = System.currentTimeMillis();
        this.zone.addItem(this);
    }

    public void update() {
        if (this.isBlackBall) {
            int timeMoveToPlayer = 10000;
            if (Util.canDoWithTime(lastTimeMoveToPlayer, timeMoveToPlayer)) {
                if (this.zone != null && !this.zone.getPlayers().isEmpty()) {
                    Player player = this.zone.getPlayers().get(0);
                    if (player.zone != null && player.zone.equals(this.zone)) {
                        this.x = player.location.x;
                        this.y = this.zone.map.yPhysicInTop(this.x, player.location.y - 24);
                        reAppearItem();
                        this.lastTimeMoveToPlayer = System.currentTimeMillis();
                    }
                }
            }
            return;
        }

        if (Util.canDoWithTime(createTime, 1800000) && this.isVetinh && !this.isNamecBall) {
            if (this.zone.map.mapId != 21 && this.zone.map.mapId != 22
                    && this.zone.map.mapId != 23 && this.itemTemplate.id != 78) {
                ItemMapService.gI().removeItemMapAndSendClient(this);
            }
        }

        if (Util.canDoWithTime(createTime, 40000) && !this.isNamecBall && !this.isVetinh) {
            if (this.isDoanhTraiBall && Util.canDoWithTime(createTime, 300_000)) {
                ItemMapService.gI().removeItemMapAndSendClient(this);
            } else {
                if (this.zone.map.mapId != 21 && this.zone.map.mapId != 22
                        && this.zone.map.mapId != 23 && this.itemTemplate.id != 78) {
                    ItemMapService.gI().removeItemMapAndSendClient(this);
                }
            }
        }
        if (Util.canDoWithTime(createTime, 15000)) {
            this.playerId = -1;
        }
    }

    private long lastTimeMoveToPlayer;

    private void reAppearItem() {
        ItemMapService.gI().sendItemMapDisappear(this);
        Service.getInstance().dropItemMap(this.zone, this);
    }

    public void dispose() {
        this.zone = null;
        this.itemTemplate = null;
        this.options = null;
    }
}
