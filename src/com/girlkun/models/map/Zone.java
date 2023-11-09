package com.girlkun.models.map;

import com.girlkun.consts.ConstPlayer;
import com.girlkun.consts.ConstTask;
import com.girlkun.models.boss.dhvt.BossDHVT;
import com.girlkun.models.item.Item;
import com.girlkun.models.mob.Mob;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcManager;
import com.girlkun.models.player.*;
import com.girlkun.network.io.Message;
import com.girlkun.services.*;
import com.girlkun.utils.FileIO;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

import static com.girlkun.services.NgocRongNamecService.TIME_BL;
import static com.girlkun.services.NgocRongNamecService.TIME_OP;


public class Zone {
    public static final byte PLAYERS_TIEU_CHUAN_TRONG_MAP = 7;
    public int countItemAppeared = 0;
    public Map map;
    public int zoneId;
    public int maxPlayer;
    @Getter
    private final List<Player> humanoids; //player, boss, pet
    @Getter
    public final List<Player> notBosses; //player, pet
    @Getter
    public final List<Player> players; //player
    @Getter
    private final List<Player> bosses; //boss
    private final List<Player> pets; //pet
    public final List<Mob> mobs;
    public final List<ItemMap> items;
    public long lastTimeDropBlackBall;
    public boolean finishBlackBallWar;
    public boolean finishMapMaBu;
    public List<TrapMap> trapMaps;
    public boolean haveBeQuynh;
    @Setter
    @Getter
    private Player referee;

    public boolean isTrungUyTrangAlive;
    public boolean isBulon13Alive;
    public boolean isBulon14Alive;

    public boolean isFullPlayer() {
        return this.players.size() >= this.maxPlayer;
    }

    private void updateMob() {
        for (Mob mob : this.mobs) {
            mob.update();
        }
    }

    private void updatePlayer() {
        if (!this.notBosses.isEmpty()) {
            for (int i = this.notBosses.size() - 1; i >= 0; i--) {
                if (this.notBosses.size() > i) {
                    Player pl = this.notBosses.get(i);
                    if (!pl.isPet && !pl.isNewPet && !pl.isTrieuHoiPet) {
                        this.notBosses.get(i).update();
                    }
                }
            }
        }
    }

    private void updateItem() {
        for (int i = this.items.size() - 1; i >= 0; i--) {
            this.items.get(i).update();
        }
    }

    public void update() {
        updateMob();
        updatePlayer();
        updateItem();
    }

    public Zone(Map map, int zoneId, int maxPlayer) {
        this.map = map;
        this.zoneId = zoneId;
        this.maxPlayer = maxPlayer;
        this.humanoids = new ArrayList<>();
        this.notBosses = new ArrayList<>();
        this.players = new ArrayList<>();
        this.bosses = new ArrayList<>();
        this.pets = new ArrayList<>();
        this.mobs = new ArrayList<>();
        this.items = new ArrayList<>();
        this.trapMaps = new ArrayList<>();
    }

    public int getNumOfPlayers() {
        return this.players.size();
    }

    public void addPlayer(Player player) {
        if (player != null) {
            if (!this.humanoids.contains(player)) {
                this.humanoids.add(player);
            }
            if (!player.isBoss && !this.notBosses.contains(player)) {
                this.notBosses.add(player);
            }
            if (!player.isBoss && !player.isNewPet && !player.isPet && !player.isTrieuHoiPet && !this.players.contains(player)) {
                this.players.add(player);
            }
            if (player.isBoss) {
                this.bosses.add(player);
            }
            if (player.isPet || player.isNewPet || player.isTrieuHoiPet) {
                this.pets.add(player);
            }
        }
    }

    public boolean isKhongCoTrongTaiTrongKhu() {
        boolean notJoin = true;
        for (Player pl : players) {
            if (!pl.isPl()) {
                notJoin = false;
                break;
            }
            if ((pl.zone.map.mapId >= 21 && pl.zone.map.mapId <= 23) || pl.zone.map.mapId == 170 || pl.zone.map.mapId == 153
                    || pl.zone.map.mapId == 52 || pl.zone.map.mapId == 113 || pl.zone.map.mapId == 129
                    || MapService.gI().isMapDoanhTrai(pl.zone.map.mapId)
                    || MapService.gI().isMapBlackBallWar(pl.zone.map.mapId)
                    || MapService.gI().isMapBanDoKhoBau(pl.zone.map.mapId)
                    || MapService.gI().isMapKhiGas(pl.zone.map.mapId)
                    || MapService.gI().isMapMaBu(pl.zone.map.mapId)
                    || MapService.gI().isMapOffline(pl.zone.map.mapId)) {
                notJoin = false;
            }
        }
        return notJoin;
    }

    public void removePlayer(Player player) {
        this.humanoids.remove(player);
        this.notBosses.remove(player);
        this.players.remove(player);
        this.bosses.remove(player);
        this.pets.remove(player);
    }

    public ItemMap getItemMapByItemMapId(int itemId) {
        for (ItemMap item : this.items) {
            if (item.itemMapId == itemId) {
                return item;
            }
        }
        return null;
    }

    public ItemMap getItemMapByTempId(int tempId) {
        for (ItemMap item : this.items) {
            if (item.itemTemplate.id == tempId) {
                return item;
            }
        }
        return null;
    }

    public List<ItemMap> getItemMapsForPlayer(Player player) {
        List<ItemMap> list = new ArrayList<>();
        for (ItemMap item : items) {
            if (item.itemTemplate.id == 78) {
                if (TaskService.gI().getIdTask(player) != ConstTask.TASK_3_1) {
                    continue;
                }
            }
            if (item.itemTemplate.id == 74) {
                if (TaskService.gI().getIdTask(player) < ConstTask.TASK_3_0) {
                    continue;
                }
            }
            list.add(item);
        }
        return list;
    }

    public Player getPlayerInMap(long idPlayer) {
        for (Player pl : humanoids) {
            if (pl.id == idPlayer) {
                return pl;
            }
        }
        return null;
    }

    public void pickItem(Player player, int itemMapId) {
        ItemMap itemMap = getItemMapByItemMapId(itemMapId);
        if (itemMap != null) {
            // type 22 là vệ tinh
            if (itemMap.itemTemplate.type != 22) {
                if (itemMap.playerId == player.id * 100000 || itemMap.playerId == player.id || itemMap.playerId == -1) {
                    Item item = ItemService.gI().createItemFromItemMap(itemMap);
                    boolean picked = true;
                    // giới hạn nhặt item mỗi ngày
//                    if (item.template.id == 934 && player.taixiu.bongtai >= 400) {
//                        Service.getInstance().sendThongBao(player, "Mỗi ngày chỉ nhặt được 400 mảnh hồn");
//                        return;
//                    } else if (item.template.id == 934) {
//                        player.taixiu.bongtai++;
//                        InventoryServiceNew.gI().sendItemBags(player);
//                        Service.getInstance().sendToAntherMePickItem(player, itemMapId);
//                    }
                    if (!ItemMapService.gI().isNamecBall(item.template.id)) {
                        picked = InventoryServiceNew.gI().addItemBag(player, item);
                    }
                    if (picked) {
                        int itemType = item.template.type;
                        Message msg;
                        try {
                            msg = new Message(-20);
                            msg.writer().writeShort(itemMapId);
                            switch (itemType) {
                                case 9, 10, 34 -> {
                                    msg.writer().writeUTF("Bạn mới nhặt được " + item.quantity + " " + item.template.name);
                                    PlayerService.gI().sendInfoHpMpMoney(player);
                                }
                                default -> {
                                    switch (item.template.id) {
                                        case 362 ->
                                                Service.getInstance().sendThongBao(player, "Chỉ là cục đá thôi, nhặt làm gì?");
                                        case 353, 354, 355, 356, 357, 358, 359 -> {
                                            if (System.currentTimeMillis() >= TIME_OP && System.currentTimeMillis() <= TIME_BL) {
                                                if (player.idNRNM == -1) {
                                                    PlayerService.gI().changeAndSendTypePK(player, ConstPlayer.PK_ALL);
                                                    player.idNRNM = item.template.id;
                                                    NgocRongNamecService.gI().mapNrNamec[item.template.id - 353] = player.zone.map.mapId;
                                                    NgocRongNamecService.gI().nameNrNamec[item.template.id - 353] = player.zone.map.mapName;
                                                    NgocRongNamecService.gI().zoneNrNamec[item.template.id - 353] = (byte) player.zone.zoneId;
                                                    NgocRongNamecService.gI().pNrNamec[item.template.id - 353] = player.name;
                                                    NgocRongNamecService.gI().idpNrNamec[item.template.id - 353] = (int) player.id;
                                                    player.lastTimePickNRNM = System.currentTimeMillis();
                                                    Service.getInstance().sendFlagBag(player);
                                                    msg.writer().writeUTF("Bạn đã nhặt được " + item.template.name);
                                                    msg.writer().writeShort(item.quantity);
                                                    player.sendMessage(msg);

                                                } else {
                                                    Service.getInstance().sendThongBao(player, "Bạn đã mang ngọc rồng trên người");
                                                    ItemMap itm = new ItemMap(itemMap);
                                                    itm.x = player.location.x + Util.nextInt(-20, 20);
                                                    itm.y = itm.zone.map.yPhysicInTop(itm.x, player.location.y);
                                                    Service.getInstance().dropItemMap(player.zone, itm);
                                                }
                                            } else {
                                                Service.getInstance().sendThongBao(player, "NRNM chỉ nhặt được trong khung giờ 14h đến 16h hằng ngày !!!!");
                                                ItemMap itm = new ItemMap(itemMap);
                                                itm.x = player.location.x + Util.nextInt(-20, 20);
                                                itm.y = itm.zone.map.yPhysicInTop(itm.x, player.location.y);
                                                Service.getInstance().dropItemMap(player.zone, itm);
                                            }
                                        }
                                        case 73 -> {
                                            msg.writer().writeUTF("");
                                            msg.writer().writeShort(item.quantity);
                                            player.sendMessage(msg);
                                        }
                                        case 74 -> msg.writer().writeUTF("Bạn mới vừa ăn " + item.template.name);
                                        case 78 -> {
                                            msg.writer().writeUTF("Thằng bé đáng iu chưa kìa");
                                            msg.writer().writeShort(item.quantity);
                                            player.sendMessage(msg);
                                        }
                                        default -> {
                                            if (item.template.type >= 0 && item.template.type < 5) {
                                                msg.writer().writeUTF(item.template.name + " mlem mlem...");
                                            } else {
                                                msg.writer().writeUTF("Bạn mới nhặt được " + item.template.name);
                                            }
                                            InventoryServiceNew.gI().sendItemBags(player);
                                        }
                                    }
                                }
                            }
                            msg.writer().writeShort(item.quantity);
                            player.sendMessage(msg);
                            msg.cleanup();
                            Service.getInstance().sendToAntherMePickItem(player, itemMapId);
                            if (!(this.map.mapId >= 21 && this.map.mapId <= 23
                                    && itemMap.itemTemplate.id == 74
                                    || this.map.mapId >= 42 && this.map.mapId <= 44
                                    && itemMap.itemTemplate.id == 78)) {
                                removeItemMap(itemMap);
                            }

                        } catch (Exception e) {
                            Logger.logException(Zone.class, e, "Error zone player: " + player.name);
                        }
                    } else {
                        if (!ItemMapService.gI().isBlackBall(item.template.id)) {
                            String text = "Hành trang không còn chỗ trống";
                            Service.getInstance().sendThongBao(player, text);
                        }
                    }
                } else {
                    Service.getInstance().sendThongBao(player, "Không thể nhặt vật phẩm của người khác");
                }
            } else {
                Service.getInstance().sendThongBao(player, "Không thể nhặt Vệ tinh");
            }
        } else {
            Service.getInstance().sendThongBao(player, "Không thể thực hiện");
        }
        TaskService.gI().checkDoneTaskPickItem(player, itemMap);
        TaskService.gI().checkDoneSideTaskPickItem(player, itemMap);
    }

    public void addItem(ItemMap itemMap) {
        if (itemMap != null && !items.contains(itemMap)) {
            items.add(0, itemMap);
        }
    }

    public void removeItemMap(ItemMap itemMap) {
        this.items.remove(itemMap);
    }

    public Player getRandomPlayerInMap() {
        if (!this.notBosses.isEmpty()) {
            return this.notBosses.get(Util.nextInt(0, this.notBosses.size() - 1));
        } else {
            return null;
        }
    }

    public Player getRandomPlayerInMapPlayer() {
        if (!this.players.isEmpty()) {
            return this.players.get(Util.nextInt(0, this.players.size() - 1));
        } else {
            return null;
        }
    }

    public Player playerPKInMap() {
        if (!this.humanoids.isEmpty()) {
            return this.humanoids.get(Util.nextInt(0, this.humanoids.size() - 1));
        } else {
            return null;
        }
    }

    //load thông tin người chơi cho những người chơi khác
    public void loadMeToAnother(Player player) {
        try {
            if (player.zone != null) {
                if (MapService.gI().isMapOffline(this.map.mapId)) {
                    if (player.isPet && this.equals(((Pet) player).master.zone)) {
                        infoPlayer(((Pet) player).master, player);
                    } else {
                        if (player.isTrieuHoiPet) {
                            assert player instanceof ThuTrieuHoi;
                            if (this.equals(((ThuTrieuHoi) player).masterr.zone)) {
                                infoPlayer(((ThuTrieuHoi) player).masterr, player);
                            }
                        }
                    }
                } else {
                    for (Player pl : players) {
                        if (!player.equals(pl)) {
                            infoPlayer(pl, player);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Error Zone player: " + player.name);
        }
    }

    //load những player trong map và gửi cho player vào map
    public void loadAnotherToMe(Player player) {
        try {
            if (MapService.gI().isMapOffline(this.map.mapId)) {
                for (Player pl : this.humanoids) {
                    if (pl.id == -player.id || (pl instanceof TestDame)) {
                        infoPlayer(player, pl);
                        break;
                    }
                }
            } else {
                for (Player pl : this.humanoids) {
                    if (pl != null && !player.equals(pl)) {
                        infoPlayer(player, pl);
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Error Zone player: " + player.name);
        }
    }

    private void infoPlayer(Player plReceive, Player plInfo) {
        Message msg;
        try {
            msg = new Message(-5);
            msg.writer().writeInt((int) plInfo.id);
            if (plInfo.clan != null) {
                msg.writer().writeInt(plInfo.clan.id);
            } else {
                msg.writer().writeInt(-1);
            }
//            if (plInfo.clan != null) {
//                msg.writer().writeInt(plInfo.clan.id);
//                name = "[" + plInfo.clan.name + "]" + plInfo.name;//test
//            } else {
//                msg.writer().writeInt(-1);
//                name = plInfo.name; //test
//            }
            msg.writer().writeByte(Service.getInstance().getCurrLevel(plInfo));
            msg.writer().writeBoolean(false);
            msg.writer().writeByte(plInfo.typePk);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeByte(plInfo.gender);
            msg.writer().writeShort(plInfo.getHead());
            msg.writer().writeUTF(plInfo.name);//plInfo.name
            msg.writer().writeInt(Util.DoubleGioihan(plInfo.nPoint.hp));
            msg.writer().writeInt(Util.DoubleGioihan(plInfo.nPoint.hpMax));
            msg.writer().writeShort(plInfo.getBody());
            msg.writer().writeShort(plInfo.getLeg());
            msg.writer().writeByte(plInfo.getFlagBag()); //bag
            msg.writer().writeByte(-1);
            msg.writer().writeShort(plInfo.location.x);
            msg.writer().writeShort(plInfo.location.y);
            msg.writer().writeShort(0);
            msg.writer().writeShort(0);

            msg.writer().writeByte(0);

            msg.writer().writeByte(plInfo.iDMark.getIdSpaceShip());

            msg.writer().writeByte(plInfo.effectSkill.isMonkey ? 1 : 0);
            msg.writer().writeShort(plInfo.getMount());
            msg.writer().writeByte(plInfo.cFlag);
            msg.writer().writeByte(0);

//            if (!plInfo.isPet && !plInfo.isBoss && plInfo.isAdmin()) {
            msg.writer().writeShort(-1); //idauraeff plInfo.getAura()
            msg.writer().writeByte(-1); //seteff plInfo.getEffFront()
//            }
            plReceive.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Error Zone player: " + plInfo.name + " " + plInfo.name);
        }
        Service.getInstance().sendFlagPlayerToMe(plReceive, plInfo);
//        Service.gI().sendFlagPlayerToMe(plInfo, plReceive);
        if (!plInfo.isBoss && !plInfo.isPet && !plInfo.isNewPet && !plInfo.isTrieuHoiPet && !(plInfo instanceof BossDHVT) && !(plInfo instanceof Referee) & !(plInfo instanceof Referee1) & !(plInfo instanceof TestDame)) {
            Service.getInstance().sendPetFollowToMe(plReceive, plInfo);
            if (plInfo.inventory.itemsBody.get(11).isNotNullItem()) {
                Service.getInstance().sendFootRv(plInfo, plReceive, plInfo.inventory.itemsBody.get(11).template.id);
            }
        }
        if (plInfo.isPl() && plInfo.name.compareTo("TABI") != 0 && plInfo.name.compareTo("TEST DAME") != 0) {
            if (plInfo.inventory.itemsBody.get(5).isNotNullItem()) {
                Service.getInstance().sendTitleRv(plInfo, plReceive, plInfo.inventory.itemsBody.get(5).template.id);
            }
        }
        Service.getInstance().sendTitleRv(plInfo, plReceive, (short) 888);
        Service.getInstance().sendTitleRv(plInfo, plReceive, (short) 889);
        Service.getInstance().sendTitleRv(plInfo, plReceive, (short) 890);
        try {
            if (plInfo.isDie()) {
                msg = new Message(-8);
                msg.writer().writeInt((int) plInfo.id);
                msg.writer().writeByte(0);
                msg.writer().writeShort(plInfo.location.x);
                msg.writer().writeShort(plInfo.location.y);
                plReceive.sendMessage(msg);
                msg.cleanup();
            }
        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Error Zone player: " + plInfo.name + " " + plInfo.name);
        }
    }

    public void mapInfo(Player pl) {
        Message msg;
        try {
            msg = new Message(-24);
            msg.writer().writeByte(this.map.mapId);
            msg.writer().writeByte(this.map.planetId);
            msg.writer().writeByte(this.map.tileId);
            msg.writer().writeByte(this.map.bgId);
            msg.writer().writeByte(this.map.type);
            msg.writer().writeUTF(this.map.mapName);
            msg.writer().writeByte(this.zoneId);
            msg.writer().writeShort(pl.location.x);
            msg.writer().writeShort(pl.location.y);
            // waypoint
            List<WayPoint> wayPoints = this.map.wayPoints;
            msg.writer().writeByte(wayPoints.size());
            for (WayPoint wp : wayPoints) {
                msg.writer().writeShort(wp.minX);
                msg.writer().writeShort(wp.minY);
                msg.writer().writeShort(wp.maxX);
                msg.writer().writeShort(wp.maxY);
                msg.writer().writeBoolean(wp.isEnter);
                msg.writer().writeBoolean(wp.isOffline);
                msg.writer().writeUTF(wp.name);
            }
            // mob
            List<Mob> mobs = this.mobs;
            msg.writer().writeByte(mobs.size());
            for (Mob mob : mobs) {
                msg.writer().writeBoolean(false); //is disable
                msg.writer().writeBoolean(false); //is don't move
                msg.writer().writeBoolean(false); //is fire
                msg.writer().writeBoolean(false); //is ice
                msg.writer().writeBoolean(false); //is wind
                msg.writer().writeByte(mob.tempId);
                msg.writer().writeByte(0);
                msg.writer().writeInt(Util.DoubleGioihan(mob.point.gethp()));
                msg.writer().writeByte(mob.level);
                msg.writer().writeInt((Util.DoubleGioihan(mob.point.getHpFull())));
                msg.writer().writeShort(mob.location.x);
                msg.writer().writeShort(mob.location.y);
                msg.writer().writeByte(mob.status);
                msg.writer().writeByte(mob.lvMob);
                msg.writer().writeBoolean(false);
            }

            msg.writer().writeByte(0);

            // npc
            List<Npc> npcs = NpcManager.getNpcsByMapPlayer(pl);
            msg.writer().writeByte(npcs.size());
            for (Npc npc : npcs) {
                msg.writer().writeByte(npc.status);
                msg.writer().writeShort(npc.cx);
                msg.writer().writeShort(npc.cy);
                msg.writer().writeByte(npc.tempId);
                msg.writer().writeShort(npc.avartar);
            }

            // item
            List<ItemMap> itemsMap = this.getItemMapsForPlayer(pl);
            msg.writer().writeByte(itemsMap.size());
            for (ItemMap it : itemsMap) {
                msg.writer().writeShort(it.itemMapId);
                msg.writer().writeShort(it.itemTemplate.id);
                msg.writer().writeShort(it.x);
                msg.writer().writeShort(it.y);
                msg.writer().writeInt((int) it.playerId);
            }

            // bg item
//                msg.writer().writeShort(0);
            try {
                byte[] bgItem = FileIO.readFile("data/girlkun/map/item_bg_map_data/" + this.map.mapId);
                assert bgItem != null;
                msg.writer().write(bgItem);
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Error Zone player:" + pl.name + " " +map.mapId);
                msg.writer().writeShort(0);
            }

            // eff item
//                msg.writer().writeShort(0);
            try {
                byte[] effItem = FileIO.readFile("data/girlkun/map/eff_map/" + this.map.mapId);
                assert effItem != null;
                msg.writer().write(effItem);
            } catch (Exception e) {
                Logger.logException(Zone.class, e, "Error Zone player:" + pl.name + " " +map.mapId);
                msg.writer().writeShort(0);
            }

            msg.writer().writeByte(this.map.bgType);
            msg.writer().writeByte(pl.iDMark.getIdSpaceShip());
            msg.writer().writeByte(0);
            pl.sendMessage(msg);

            msg.cleanup();

        } catch (Exception e) {
            Logger.logException(Zone.class, e, "Error Zone player:" + pl.name);
        }
    }

    public TrapMap isInTrap(Player player) {
        for (TrapMap trap : this.trapMaps) {
            if (player.location.x >= trap.x && player.location.x <= trap.x + trap.w
                    && player.location.y >= trap.y && player.location.y <= trap.y + trap.h) {
                return trap;
            }
        }
        return null;
    }

    public int getNumOfBosses() {
        return this.bosses.size();
    }
}
