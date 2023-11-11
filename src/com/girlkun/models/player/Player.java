package com.girlkun.models.player;

import BoMong.BoMong;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.consts.ConstTask;
import com.girlkun.models.card.Card;
import com.girlkun.models.clan.Clan;
import com.girlkun.models.clan.ClanMember;
import com.girlkun.models.intrinsic.IntrinsicPlayer;
import com.girlkun.models.item.Item;
import com.girlkun.models.item.ItemTime;
import com.girlkun.models.item.ItemTimeSieuCap;
import com.girlkun.models.kygui.ItemKyGui;
import com.girlkun.models.kygui.ShopKyGuiManager;
import com.girlkun.models.map.MapMaBu.MapMaBu;
import com.girlkun.models.map.TrapMap;
import com.girlkun.models.map.Zone;
import com.girlkun.models.map.bdkb.BanDoKhoBauService;
import com.girlkun.models.map.blackball.BlackBallWar;
import com.girlkun.models.map.doanhtrai.DoanhTraiService;
import com.girlkun.models.map.gas.GasService;
import com.girlkun.models.matches.IPVP;
import com.girlkun.models.matches.TYPE_LOSE_PVP;
import com.girlkun.models.mob.MobMe;
import com.girlkun.models.npc.specialnpc.MabuEgg;
import com.girlkun.models.npc.specialnpc.MagicTree;
import com.girlkun.models.npc.specialnpc.Timedua;
import com.girlkun.models.skill.PlayerSkill;
import com.girlkun.models.skill.Skill;
import com.girlkun.models.task.TaskPlayer;
import com.girlkun.network.io.Message;
import com.girlkun.server.Client;
import com.girlkun.server.io.MySession;
import com.girlkun.services.*;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.services.func.ChonAiDay;
import com.girlkun.services.func.CombineNew;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.girlkun.data.DataGame.MAP_MOUNT_NUM;

public class Player {

    public boolean isHighPurchaseVolume = false;
    public long goldCount = 0;
    public long rubyCount = 0;
    public boolean isResetDame = false;
    public long lastTimeDame;
    public double totalDame = 0;
    public int goldChallenge;
    public boolean receivedWoodChest;

    @Getter
    public MySession session;

    public boolean beforeDispose;
    public int highPurchaseCount = 0;
    public int idItemHighPurchase = -1;

    public boolean isAuto = false;
    public boolean isAutoHP = false;
    public boolean isAutoKI = false;
    public boolean isAutoSD = false;
    public boolean isAutoArmor = false;
    public boolean isAutoPea = false;
    public boolean isAutoFlag = false;
    public boolean isBeQuynh;
    public long lastTimeHoTong;
    public boolean isTitleItem;
    public boolean isUseTitleItem;
    public boolean isUseDanhHieu2;
    public boolean isUseDanhHieu3;
    public boolean isUseDanhHieu4;
    public long lastTimeUseDanhHieu1;
    public long lastTimeUseDanhHieu2;
    public long lastTimeUseDanhHieu3;
    public boolean isPet;
    public boolean isNewPet;
    public boolean isTrieuHoiPet;
    public boolean isBoss;
    public int nhsPoint = 0;
    public IPVP pvp;
    public int pointPVP;
    public byte maxTime = 30;
    public byte type = 0;

    public int mapIdBeforeLogout;
    public List<Zone> mapBlackBall;
    public List<Zone> mapMaBu;

    public Zone zone;
    public Zone mapBeforeCapsule;
    public List<Zone> mapCapsule;
    public List<Card> cards = new ArrayList<>();
    public Pet pet;
    public NewPet newpet;
    public SummonPet thuTrieuHoi;
    public WarGodMission nhiemvuChienthan;
    public MobMe mobMe;
    public Location location;
    public SetClothes setClothes;
    public EffectSkill effectSkill;
    public MabuEgg mabuEgg;
    public Timedua timedua;
    public TaskPlayer playerTask;
    public ItemTime itemTime;
    public ItemTimeSieuCap itemTimesieucap;
    public Fusion fusion;
    public MagicTree magicTree;
    public IntrinsicPlayer playerIntrinsic;
    public Inventory inventory;
    public Taixiu taixiu;
    public PlayerSkill playerSkill;
    public CombineNew combineNew;
    public IDMark iDMark;
    public Charms charms;
    public EffectSkin effectSkin;
    public Gift gift;
    public NPoint nPoint;
    public RewardBlackBall rewardBlackBall;
    public EffectFlagBag effectFlagBag;
    public FightMabu fightMabu;

    public SkillSpecial skillSpecial;
    public BoMong achievement;

    public Clan clan;
    public ClanMember clanMember;

    public List<Friend> friends;
    public List<Enemy> enemies;

    public long id;
    public String name;
    public byte gender;
    public boolean isNewMember;
    public short head;

    public byte typePk;

    public byte cFlag;

    public boolean haveTennisSpaceShip;

    public boolean justRevived;
    public long lastTimeRevived;

    public boolean justRevived1;
    public long lastTimeRevived1;

    public long rankSieuHang;

    public int TrieuHoiCapBac = -1;
    public String TenThuTrieuHoi;
    public int TrieuHoiThucAn;
    public long TrieuHoiDame;
    public long TrieuHoiHP;
    public long TrieuHoilastTimeThucan;
    public int TrieuHoiLevel;
    public long TrieuHoiExpThanThu;
    public Player TrieuHoiPlayerAttack;
    public long Autothucan;
    public boolean trangthai = false;

    public long diemdanh;
    public int vnd;
    public byte totalPlayerViolate;
    public long timeChangeZone;
    public long lastTimeUseOption;

    public int bdkb_countPerDay;
    public long bdkb_lastTimeJoin;
    public boolean bdkb_isJoinBdkb;

    public short idNRNM = -1;
    public short idGo = -1;
    public long lastTimePickNRNM;
    public int goldNormar;
    public int goldVIP;
    public int goldTai;
    public int goldXiu;
    public long lastTimeWin;
    public boolean isWin;
    public short idAura = -1;
    public String Hppl = "\n";
    public String HpQuai = "\n";
    public int levelWoodChest;

    public Player() {
        lastTimeUseOption = System.currentTimeMillis();
        location = new Location();
        nPoint = new NPoint(this);
        inventory = new Inventory();
        taixiu = new Taixiu();
        playerSkill = new PlayerSkill(this);
        setClothes = new SetClothes(this);
        effectSkill = new EffectSkill(this);
        fusion = new Fusion(this);
        playerIntrinsic = new IntrinsicPlayer();
        rewardBlackBall = new RewardBlackBall(this);
        effectFlagBag = new EffectFlagBag();
        fightMabu = new FightMabu(this);
        //----------------------------------------------------------------------
        iDMark = new IDMark();
        combineNew = new CombineNew();
        playerTask = new TaskPlayer();
        friends = new ArrayList<>();
        enemies = new ArrayList<>();
        itemTime = new ItemTime(this);
        itemTimesieucap = new ItemTimeSieuCap(this);
        charms = new Charms();
        gift = new Gift(this);
        effectSkin = new EffectSkin(this);
        skillSpecial = new SkillSpecial(this);
        achievement = new BoMong(this);
        nhiemvuChienthan = new WarGodMission();
    }

    //--------------------------------------------------------------------------
    public boolean isDie() {
        if (this.nPoint != null) {
            return this.nPoint.hp <= 0;
        }
        return true;
    }

    //--------------------------------------------------------------------------
    public void setSession(MySession session) {
        this.session = session;
    }

    public void sendMessage(Message msg) {
        if (this.session != null && msg != null) {
            this.session.sendMessage(msg);
        }
    }

    public void CreatePet(String NamePet) {
        this.TenThuTrieuHoi = NamePet;
        this.TrieuHoilastTimeThucan = System.currentTimeMillis();
        this.TrieuHoiThucAn = 100;
        this.TrieuHoiLevel = 0;
        this.TrieuHoiExpThanThu = 0;
        this.TrieuHoiCapBac = 0; //Util.nextInt(0, Util.nextInt(3, 10));
        this.TrieuHoiDame = Util.GioiHannext(10000, 10000L + ((this.TrieuHoiCapBac + 1) * 10000L));
        this.TrieuHoiHP = Util.GioiHannext(100000, 100000L + ((this.TrieuHoiCapBac + 1) * 50000L));
    }

    public boolean isPl() {
        return !isPet && !isBoss && !isNewPet && !isTrieuHoiPet;// && !isNewPet1
    }

    public void update() {
        if (!this.beforeDispose) {
            try {
                if (!iDMark.isBan()) {

                    if (nPoint != null) {
                        nPoint.update();
                    }
                    if (fusion != null) {
                        fusion.update();
                    }
                    if (effectSkill != null) {
                        effectSkill.update();
                    }
                    if (mobMe != null) {
                        mobMe.update();
                    }
                    if (effectSkin != null) {
                        effectSkin.update();
                    }
                    if (pet != null) {
                        pet.update();
                    }
                    if (newpet != null) {
                        newpet.update();
                    }

                    if (thuTrieuHoi != null) {
                        thuTrieuHoi.update();
                    }
                    if (magicTree != null) {
                        magicTree.update();
                    }
                    if (itemTime != null) {
                        itemTime.update();
                    }
                    if (itemTimesieucap != null) {
                        itemTimesieucap.update();
                    }
                    if (this.lastTimeUseDanhHieu1 != 0 && Util.canDoWithTime(this.lastTimeUseDanhHieu1, 6000)) {
                        lastTimeUseDanhHieu1 = 0;
                        isUseDanhHieu2 = false;
                    }
                    if (this.lastTimeUseDanhHieu2 != 0 && Util.canDoWithTime(this.lastTimeUseDanhHieu2, 6000)) {
                        lastTimeUseDanhHieu2 = 0;
                        isUseDanhHieu3 = false;
                    }

                    if (this.lastTimeUseDanhHieu3 != 0 && Util.canDoWithTime(this.lastTimeUseDanhHieu3, 6000)) {
                        lastTimeUseDanhHieu3 = 0;
                        isUseDanhHieu4 = false;
                    }
                    GasService.gI().update(this);
                    BanDoKhoBauService.gI().update(this);
                    DoanhTraiService.gI().update(this);
                    BlackBallWar.gI().update(this);
                    MapMaBu.gI().update(this);
                    TimeReset.gI().update(this); //time reset ngày
                    if (!this.isBoss && this.iDMark != null && this.iDMark.isGoToGas() && Util.canDoWithTime(this.iDMark.getLastTimeGotoGas(), 6000)) {
//                        ChangeMapService.gI().changeMapBySpaceShip(this, 149, -1, 163);
                        ChangeMapService.gI().changeMapBySpaceShip(this, 149, -1, 163);
                        this.iDMark.setGoToGas(false);
                    }
                    if (!this.isBoss && this.iDMark != null && this.iDMark.isGotoFuture() && Util.canDoWithTime(this.iDMark.getLastTimeGoToFuture(), 6000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 102, -1, Util.nextInt(60, 200));
                        this.iDMark.setGotoFuture(false);
                    }
                    if (!this.isBoss && this.iDMark != null && this.iDMark.isGoToBDKB() && Util.canDoWithTime(this.iDMark.getLastTimeGoToBDKB(), 6000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 135, -1, 35);
                        this.iDMark.setGoToBDKB(false);
                    }
                    if (this.zone != null) {
                        TrapMap trap = this.zone.isInTrap(this);
                        if (trap != null) {
                            trap.doPlayer(this);
                        }
                    }
                    if (this.isPl() && this.inventory != null && this.inventory.itemsBody != null && this.inventory.itemsBody.size() >= 8 && this.inventory.itemsBody.get(7) != null) {
                        Item it = this.inventory.itemsBody.get(7);
                        if (it != null && it.isNotNullItem() && this.newpet == null) {// && this.newpet1 == null
                            switch (it.template.id) {
                                case 942 -> PetService.Pet2(this, 966, 967, 968);
                                case 943 -> PetService.Pet2(this, 969, 970, 971);
                                case 944 -> PetService.Pet2(this, 972, 973, 974);
                                case 967 -> PetService.Pet2(this, 1050, 1051, 1052);
                                case 1107 -> PetService.Pet2(this, 1183, 1184, 1185);
                                case 1140 -> PetService.Pet2(this, 1285, 1286, 1287);
                                case 1133 -> PetService.Pet2(this, 1261, 1262, 1263);
                                case 1180 -> PetService.Pet2(this, 1270, 1271, 1272);
                                case 1181 -> PetService.Pet2(this, 1273, 1274, 1275);
                                case 1196 -> PetService.Pet2(this, 1294, 1295, 1296);
                                case 1197 -> PetService.Pet2(this, 1297, 1298, 1299);
                                case 1198 -> PetService.Pet2(this, 1300, 1301, 1302);
                                case 1221 -> PetService.Pet2(this, 1333, 1334, 1335);
                                case 1222 -> PetService.Pet2(this, 1336, 1337, 1338);
                                case 1223 -> PetService.Pet2(this, 1339, 1340, 1341);
                                case 1229 -> PetService.Pet2(this, 1345, 1346, 1347);
                                case 1230 -> PetService.Pet2(this, 1348, 1349, 1350);
                            }
                            Service.getInstance().point(this);
                        }
                    } else if (this.isPl() && newpet != null && !this.inventory.itemsBody.get(7).isNotNullItem()) {// && newpet1 != null
                        newpet.dispose();
                        newpet = null;
                    }
                    if (this.isPl() && this.thuTrieuHoi == null && this.TrieuHoiCapBac >= 0 && this.TrieuHoiCapBac <= 10) {
                        PetService.createThuTrieuHoi(this);
                    }
                    if (this.isPl() && isWin && this.zone.map.mapId == 51 && Util.canDoWithTime(lastTimeWin, 2000)) {
                        ChangeMapService.gI().changeMapBySpaceShip(this, 52, 0, -1);
                        isWin = false;
                    }
                    if (this.location != null && this.location.lastTimeplayerMove < System.currentTimeMillis() - 30 * 60 * 1000) {
                        Client.gI().kickSession(getSession());
                    }
                } else {
                    if (Util.canDoWithTime(iDMark.getLastTimeBan(), 5000)) {
                        Client.gI().kickSession(session);
                    }
                }
                if (Client.gI().getPlayer(this.name) != null) {
                    this.achievement.plusCount(8);
                }
                if (Util.canDoWithTime(this.lastTimeDame, 5000) && this.totalDame != 0) {
                    this.totalDame = 0;
                    this.isResetDame = true;
                }
                //////////////////// HOÀN TRẢ KÝ GỬI SAU 2 NGÀY ////////////////////
                Iterator<ItemKyGui> iterator1 = ShopKyGuiManager.gI().listItem.iterator();
                int countit = 0;
                while (iterator1.hasNext()) {
                    ItemKyGui it = iterator1.next();
                    if (it != null && !it.isBuy && it.player_sell == this.id && this.session != null
                            && it.thoigian <= System.currentTimeMillis() - 172800000) {
                        countit++;
                    }
                }

                Iterator<ItemKyGui> iterator = ShopKyGuiManager.gI().listItem.iterator();
                while (iterator.hasNext()) {
                    ItemKyGui it = iterator.next();
                    if (it != null && !it.isBuy && it.player_sell == this.id && this.session != null
                            && it.thoigian <= System.currentTimeMillis() - 172800000) {
                        if (InventoryServiceNew.gI().getCountEmptyBag(this) < countit) {
                            Service.getInstance().sendThongBao(this, "Hành trang không đủ chỗ trống để hoàn trả vật phẩm kí gửi");
                        } else {
                            Item item = ItemService.gI().createNewItem(it.itemId);
                            item.quantity = it.quantity;
                            item.itemOptions.addAll(it.options);
                            iterator.remove();
                            InventoryServiceNew.gI().addItemBag(this, item);
                            InventoryServiceNew.gI().sendItemBags(this);
                            Service.getInstance().sendMoney(this);
                            Service.getInstance().sendThongBao(this, "Vật phẩm kí đã quá 2 ngày. Vật phẩm đã được hoàn trả");
                        }
                    }
                }
            } catch (Exception e) {
                Logger.logException(Player.class, e, "Lỗi tại player: " + this.name);
            }
        }
    }

    //--------------------------------------------------------------------------
    /*
     * {380, 381, 382}: ht lưỡng long nhất thể xayda trái đất
     * {383, 384, 385}: ht porata xayda trái đất
     * {391, 392, 393}: ht namếc
     * {870, 871, 872}: ht c2 trái đất
     * {873, 874, 875}: ht c2 namếc
     * {867, 878, 869}: ht c2 xayda
     */
    private static final short[][] idOutfitFusion = {
            {380, 381, 382}, {383, 384, 385}, {391, 392, 393},// btc1
            {1204, 1205, 1206}, {1207, 1208, 1209}, {1210, 1211, 1212}, //btc2
            {1375, 1376, 1377}, {1372, 1373, 1374}, {1369, 1370, 1371}, //btc3
            {1255, 1256, 1257}, {1249, 1250, 1251}, {1246, 1247, 1248} //btc4
    };

    public byte getAura() {
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        Item item = this.inventory.itemsBody.get(5);
        if (!item.isNotNullItem()) {
            return -1;
        }
        switch (item.template.id) {
            case 1204, 1238, 9500, 9501, 9502 -> {
                return 11;
            }
            default -> {
                return -1;
            }
        }

    }

    public byte getEffFront() {
        return -1;
    }

    public short getHead() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return (short) ConstPlayer.HEADMONKEY[effectSkill.levelMonkey - 1];
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 412;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1321;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                return idOutfitFusion[9 + this.gender][0];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[6 + this.gender][0];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int headd = inventory.itemsBody.get(5).template.head;
            if (headd != -1) {
                return (short) headd;
            }
        }
        return this.head;
    }

    public short getBody() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return 193;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 413;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1322;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                return idOutfitFusion[9 + this.gender][1];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[6 + this.gender][1];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int body = inventory.itemsBody.get(5).template.body;
            if (body != -1) {
                return (short) body;
            }
        }
        if (inventory != null && inventory.itemsBody.get(0).isNotNullItem()) {
            return inventory.itemsBody.get(0).template.part;
        }
        return (short) (gender == ConstPlayer.NAMEC ? 59 : 57);
    }

    public short getLeg() {
        if (effectSkill != null && effectSkill.isMonkey) {
            return 194;
        } else if (effectSkill != null && effectSkill.isSocola) {
            return 414;
        } else if (effectSkill != null && effectSkill.isBinh) {
            return 1323;
        } else if (fusion != null && fusion.typeFusion != ConstPlayer.NON_FUSION) {
            if (fusion.typeFusion == ConstPlayer.LUONG_LONG_NHAT_THE) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 0][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA) {
                return idOutfitFusion[this.gender == ConstPlayer.NAMEC ? 2 : 1][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA2) {
                return idOutfitFusion[3 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA4) {
                return idOutfitFusion[9 + this.gender][2];
            } else if (fusion.typeFusion == ConstPlayer.HOP_THE_PORATA3) {
                return idOutfitFusion[6 + this.gender][2];
            }
        } else if (inventory != null && inventory.itemsBody.get(5).isNotNullItem()) {
            int leg = inventory.itemsBody.get(5).template.leg;
            if (leg != -1) {
                return (short) leg;
            }
        }
        if (inventory != null && inventory.itemsBody.get(1).isNotNullItem()) {
            return inventory.itemsBody.get(1).template.part;
        }
        return (short) (gender == 1 ? 60 : 58);
    }

    public short getFlagBag() {
        if (this.iDMark.isHoldBlackBall()) {
            return 31;
        } else if (this.idNRNM >= 353 && this.idNRNM <= 359) {
            return 30;
        }
        if (this.inventory.itemsBody.size() == 12) {
            if (this.inventory.itemsBody.get(8).isNotNullItem()) {
                return this.inventory.itemsBody.get(8).template.part;
            }
        }
        if (this.isPet) {
            if (this.inventory.itemsBody.get(7).isNotNullItem()) {
                return this.inventory.itemsBody.get(7).template.part;
            }
        }
        if (TaskService.gI().getIdTask(this) == ConstTask.TASK_3_2) {
            return 28;
        }
        if (this.clan != null) {
            return (short) this.clan.imgId;
        }
        return -1;
    }

    public short getMount() {
        if (this.inventory.itemsBody.isEmpty() || this.inventory.itemsBody.size() < 10) {
            return -1;
        }
        Item item = this.inventory.itemsBody.get(9);
        if (!item.isNotNullItem()) {
            return -1;
        }
        if (item.template.type == 24) {
            if (item.template.gender == 3 || item.template.gender == this.gender) {
                return item.template.id;
            } else {
                return -1;
            }
        } else {
            if (item.template.id < 500) {
                return item.template.id;
            } else {
                return MAP_MOUNT_NUM.get(String.valueOf(item.template.id));
            }
        }

//        for (Item item : inventory.itemsBag) {
//            if (item.isNotNullItem()) {
//                if (item.template.type == 24) {
//                    if (item.template.gender == 3 || item.template.gender == this.gender) {
//                        return item.template.id;
//                    } else {
//                        return -1;
//                    }
//                }
//                if (item.template.type == 23) {
//                    if (item.template.id < 500) {
//                        return item.template.id;
//                    } else {
//                        return (short) DataGame.MAP_MOUNT_NUM.get(String.valueOf(item.template.id));
//                    }
//                }
//            }
//        }
//        return -1;
    }

    public String getNameSkill9(int skill) {
        return switch (skill) {
            case 2 -> "Cadic Liên hoàn chưởng";
            case 1 -> "Ma Phong Ba";
            case 0 -> "Super Kamejoko";
            default -> "";
        };
    }

    public String getNhiemVuChienThan(int nhiemvu) {
        return switch (nhiemvu) {
            case 10 -> {
                this.nhiemvuChienthan.maxcount = 100;
                yield "Giết 100 Boss Thần Zeno";
            }
            case 9 -> {
                this.nhiemvuChienthan.maxcount = 10;
                yield "Giết 10 Boss Đôremon";
            }
            case 8 -> {
                this.nhiemvuChienthan.maxcount = 10;
                yield "Đi Bản đồ kho báu 10 lần";
            }
            case 7 -> {
                this.nhiemvuChienthan.maxcount = 10;
                yield "Đi map Khí Gas 10 lần";
            }
            case 6 -> {
                this.nhiemvuChienthan.maxcount = 5000;
                yield "Chưởng Chí mạng 5000 lần";
            }
            case 5 -> {
                this.nhiemvuChienthan.maxcount = 5000;
                yield "Hạ 5.000 Quái bay";
            }
            case 4 -> {
                this.nhiemvuChienthan.maxcount = 2000;
                yield "Nhặt 2000 Capsule Kì bí";
            }
            case 3 -> {
                this.nhiemvuChienthan.maxcount = 10;
                yield "Hộ tống Thành công Bunma 10 lần";
            }
            case 2 -> {
                this.nhiemvuChienthan.maxcount = 10;
                yield "Tìm được 10 Món đồ Thần linh bên Cold";
            }
            case 1 -> {
                this.nhiemvuChienthan.maxcount = 2000;
                yield "Giết 2.000 Sên 8";
            }
            default -> "";
        };
    }

    public String getNameThanThu(int capBac) {
        return switch (capBac) {
            case 10 -> "Đế Tiên";
            case 9 -> "Vực Chủ Cảnh";
            case 8 -> "Độ Kiếp Kỳ";
            case 7 -> "Đại Thừa Kỳ";
            case 6 -> "Hợp Thể Kỳ";
            case 5 -> "Luyện Hư";
            case 4 -> "Hóa Thần";
            case 3 -> "Nguyên Anh";
            case 2 -> "Kim Đan";
            case 1 -> "Trúc Cơ";
            case 0 -> "Luyện Khí";
            default -> "Phế Vật";
        };
    }

    public String getDaDotPha(int capBac) {
        return switch (capBac) {
            case 9 -> "Đế Vương Thạch";
            case 8 -> "Hỏa Hồn Thạch";
            case 7 -> "Thiên Mệnh Thạch";
            case 6 -> "Huyết Tinh Thạch";
            case 5 -> "Linh Vân Thạch";
            case 4 -> "Mịch Lâm Thạch";
            default -> "Thiên Nguyệt thạch";
        };
    }

    public String getTrieuHoiKiNang(int CapBac) {
        return switch (CapBac) {
            case 10 -> "Tìm " + ((TrieuHoiLevel + 1) * 20) + " Hồng ngọc cho Chủ nhân\n"
                    + "Tăng " + (TrieuHoiLevel + 1) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân\n";
            case 9 -> "Tìm " + ((TrieuHoiLevel + 1) * 20) + " Hồng ngọc cho Chủ nhân\n"
                    + "Tăng " + ((TrieuHoiLevel + 1) / 2) + "% HP, KI, Giáp, SD, SD chí mạng cho Chủ nhân";
            case 8 -> "Tìm " + ((TrieuHoiLevel + 1) * 10) + " Hồng ngọc cho Chủ nhân\n"
                    + "Tăng " + ((TrieuHoiLevel + 1) / 5) + "% HP, KI, Giáp, SD cho Chủ nhân";
            case 7 -> "Tăng " + ((TrieuHoiLevel + 1) / 5) + "% HP, KI, Giáp, SD cho Chủ nhân";
            case 6 -> "Tăng " + ((TrieuHoiLevel + 1) / 3) + "% SD Chí mạng cho Chủ nhân";
            case 5 -> "Tăng " + ((TrieuHoiLevel + 1) / 5) + "% SD cho Chủ nhân";
            case 4 -> "Tăng " + ((TrieuHoiLevel + 1) * 30) + " HP, KI, SD, Giáp cho Chủ nhân";
            case 3 ->
                    "Tăng " + ((TrieuHoiLevel + 1) * 20) + " HP, KI\n" + ((TrieuHoiLevel + 1) * 10) + " SD cho Chủ nhân";
            case 2 -> "Tăng " + ((TrieuHoiLevel + 1) * 10) + " Sức đánh cho Chủ nhân";
            case 1 -> "Tăng " + ((TrieuHoiLevel + 1) * 20) + " KI cho Chủ nhân";
            case 0 -> "Tăng " + ((TrieuHoiLevel + 1) * 20) + " HP cho Chủ nhân";
            default -> "Phế vật mà làm được gì !!!";
        };
    }

    //--------------------------------------------------------------------------
    public double injured(Player plAtt, double damage, boolean piercing, boolean isMobAttack) {
        if (!this.isDie()) {
            int TileChinhxac = 0;
            if (plAtt != null) {
                TileChinhxac = plAtt.nPoint.tlchinhxac;
                switch (plAtt.playerSkill.skillSelect.template.id) {
                    case Skill.KAMEJOKO:
                    case Skill.MASENKO:
                    case Skill.ANTOMIC:
                    case Skill.DRAGON:
                    case Skill.DEMON:
                    case Skill.GALICK:
                    case Skill.KAIOKEN:
                    case Skill.LIEN_HOAN:
                    case Skill.DE_TRUNG:
                        if (this.nPoint.voHieuChuong > 0) {
                            com.girlkun.services.PlayerService.gI().hoiPhuc(this, 0, Util.DoubleGioihan(damage * this.nPoint.voHieuChuong / 100));
                            return 0;
                        }
                }
            }
            if (!piercing && Util.isTrue(this.nPoint.tlNeDon - TileChinhxac, 100)) {
                return 0;
            }
            damage = this.nPoint.subDameInjureWithDeff(damage);
            if (!piercing && effectSkill.isShielding) {
                if (damage > nPoint.hpMax) {
                    EffectSkillService.gI().breakShield(this);
                }
                damage = 1;
            }
            if (isMobAttack && this.charms.tdBatTu > System.currentTimeMillis() && damage >= this.nPoint.hp) {
                damage = Util.DoubleGioihan(this.nPoint.hp - 1);
            }

            this.nPoint.subHP(damage);
            if (isDie()) {
                if (plAtt != null && this.zone.map.mapId == 129) {
                    plAtt.pointPVP++;
                }
                setDie(plAtt);
            }
            return damage;
        } else {
            return 0;
        }
    }

    protected void setDie(Player plAtt) {
        //xóa phù
        if (this.effectSkin.xHPKI > 1) {
            this.effectSkin.xHPKI = 1;
            Service.getInstance().point(this);
        }
        //xóa tụ skill đặc biệt
        this.playerSkill.prepareQCKK = false;
        this.playerSkill.prepareLaze = false;
        this.playerSkill.prepareTuSat = false;
        //xóa hiệu ứng skill
        this.effectSkill.removeSkillEffectWhenDie();
        //
        nPoint.setHp(0);
        nPoint.setMp(0);
        //xóa trứng
        if (this.mobMe != null) {
            this.mobMe.mobMeDie();
        }
        Service.getInstance().charDie(this);
        //add kẻ thù
        if (!this.isPet && !this.isNewPet && !this.isTrieuHoiPet && !this.isBoss
                && plAtt != null && !plAtt.isPet && !plAtt.isNewPet && !plAtt.isTrieuHoiPet && !plAtt.isBoss) {// && !this.isNewPet1 && !plAtt.isNewPet1
            if (!plAtt.itemTime.isUseAnDanh) {
                FriendAndEnemyService.gI().addEnemy(this, plAtt);
            }
        }
        //kết thúc pk
        if (this.pvp != null) {
            this.pvp.lose(this, TYPE_LOSE_PVP.DEAD);
        }
//        PVPServcice.gI().finishPVP(this, PVP.TYPE_DIE);
        BlackBallWar.gI().dropBlackBall(this);
    }

    //--------------------------------------------------------------------------
    public void setClanMember() {
        if (this.clanMember != null) {
            this.clanMember.powerPoint = this.nPoint.power;
            this.clanMember.head = this.getHead();
            this.clanMember.body = this.getBody();
            this.clanMember.leg = this.getLeg();
        }
    }

    public boolean isAdmin() {
        return this.session.isAdmin;
    }

    public boolean check99ThucAnHuyDiet() {
        for (Item item : this.inventory.itemsBag) {
            if (item != null && item.template != null && item.template.id >= 663 && item.template.id <= 667 && item.quantity >= 99) {
                return true;
            }
        }
        return false;
    }

    public void setJustRevivaled() {
        if (this.isPet) {
            this.justRevived = true;
            this.lastTimeRevived = System.currentTimeMillis();
        } else if (this.isTrieuHoiPet) {
            this.justRevived1 = true;
            this.lastTimeRevived1 = System.currentTimeMillis();
        }
    }


    public void dispose() {
        if (pet != null) {
            pet.dispose();
            pet = null;
        }
        if (newpet != null) {
            newpet.dispose();
            newpet = null;
        }

        if (thuTrieuHoi != null) {
            thuTrieuHoi.dispose();
            thuTrieuHoi = null;
        }
        if (mapBlackBall != null) {
            mapBlackBall.clear();
            mapBlackBall = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapMaBu != null) {
            mapMaBu.clear();
            mapMaBu = null;
        }
        zone = null;
        mapBeforeCapsule = null;
        if (mapCapsule != null) {
            mapCapsule.clear();
            mapCapsule = null;
        }
        if (mobMe != null) {
            mobMe.dispose();
            mobMe = null;
        }
        location = null;
        if (setClothes != null) {
            setClothes.dispose();
            setClothes = null;
        }
        if (effectSkill != null) {
            effectSkill.dispose();
            effectSkill = null;
        }
        if (mabuEgg != null) {
            mabuEgg.dispose();
            mabuEgg = null;
        }
        if (timedua != null) {
            timedua.dispose();
            timedua = null;
        }
        if (taixiu != null) {
            taixiu.dispose();
            taixiu = null;
        }
        if (nhiemvuChienthan != null) {
            nhiemvuChienthan.dispose();
            nhiemvuChienthan = null;
        }
        if (skillSpecial != null) {
            skillSpecial.dispose();
            skillSpecial = null;
        }
        if (playerTask != null) {
            playerTask.dispose();
            playerTask = null;
        }
        if (itemTime != null) {
            itemTime.dispose();
            itemTime = null;
        }
        if (itemTimesieucap != null) {
            itemTimesieucap.dispose();
            itemTimesieucap = null;
        }
        if (fusion != null) {
            fusion.dispose();
            fusion = null;
        }
        if (magicTree != null) {
            magicTree.dispose();
            magicTree = null;
        }
        if (playerIntrinsic != null) {
            playerIntrinsic.dispose();
            playerIntrinsic = null;
        }
        if (inventory != null) {
            inventory.dispose();
            inventory = null;
        }
        if (playerSkill != null) {
            playerSkill.dispose();
            playerSkill = null;
        }
        if (combineNew != null) {
            combineNew.dispose();
            combineNew = null;
        }
        if (iDMark != null) {
            iDMark.dispose();
            iDMark = null;
        }
        if (charms != null) {
            charms.dispose();
            charms = null;
        }
        if (effectSkin != null) {
            effectSkin.dispose();
            effectSkin = null;
        }
        if (gift != null) {
            gift.dispose();
            gift = null;
        }
        if (nPoint != null) {
            nPoint.dispose();
            nPoint = null;
        }
        if (rewardBlackBall != null) {
            rewardBlackBall.dispose();
            rewardBlackBall = null;
        }
        if (effectFlagBag != null) {
            effectFlagBag.dispose();
            effectFlagBag = null;
        }
        if (pvp != null) {
            pvp.dispose();
            pvp = null;
        }
        effectFlagBag = null;
        clan = null;
        clanMember = null;
        friends = null;
        enemies = null;
        session = null;
        name = null;
    }

    public String percentGold(int type) {
        try {
            if (type == 0) {
                double percent = ((double) this.goldNormar / ChonAiDay.gI().goldNormar) * 100;
                return String.valueOf(Math.ceil(percent));
            } else if (type == 1) {
                double percent = ((double) this.goldVIP / ChonAiDay.gI().goldVip) * 100;
                return String.valueOf(Math.ceil(percent));
            }
        } catch (ArithmeticException e) {
            return "0";
        }
        return "0";
    }
}
