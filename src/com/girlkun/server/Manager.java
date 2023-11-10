package com.girlkun.server;

import com.girlkun.consts.ConstMap;
import com.girlkun.consts.ConstPlayer;
import com.girlkun.data.DataGame;
import com.girlkun.database.GirlkunDB;
import com.girlkun.jdbc.daos.ShopDAO;
import com.girlkun.models.Template.*;
import com.girlkun.models.card.OptionCard;
import com.girlkun.models.card.RadarCard;
import com.girlkun.models.card.RadarService;
import com.girlkun.models.clan.Clan;
import com.girlkun.models.clan.ClanMember;
import com.girlkun.models.intrinsic.Intrinsic;
import com.girlkun.models.item.Item;
import com.girlkun.models.kygui.ItemKyGui;
import com.girlkun.models.kygui.ShopKyGuiManager;
import com.girlkun.models.map.WayPoint;
import com.girlkun.models.matches.TOP;
import com.girlkun.models.matches.pvp.DaiHoiVoThuat;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcFactory;
import com.girlkun.models.player.Referee;
import com.girlkun.models.player.Referee1;
import com.girlkun.models.player.TestDame;
import com.girlkun.models.reward.ItemMobReward;
import com.girlkun.models.reward.ItemOptionMobReward;
import com.girlkun.models.reward.MobReward;
import com.girlkun.models.shop.Shop;
import com.girlkun.models.skill.NClass;
import com.girlkun.models.skill.Skill;
import com.girlkun.models.task.SideTaskTemplate;
import com.girlkun.models.task.SubTaskMain;
import com.girlkun.models.task.TaskMain;
import com.girlkun.services.ItemService;
import com.girlkun.services.MapService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;


public class Manager {
    private static Manager i;
    public static byte server = 1;
    public static byte secondWaitLogin = 10;
    public static int maxPerIp = 10;
    public static int maxPlayer = 2000000;
    public static byte rateExpServer = 5;
    public static boolean local = false;
    public static byte suKien = 0;
    public static byte khuyenMaiNap = 1;
//    public static byte RATE_EXP_SERVER = 1;// sau khi chinh

    public static MapTemplate[] mapTemplates;
    public static final List<com.girlkun.models.map.Map> MAPS = new ArrayList<>();
    public static final List<ItemOptionTemplate> ITEM_OPTION_TEMPLATES = new ArrayList<>();
    public static final Map<Integer, MobReward> MOB_REWARDS = new HashMap<>();
    public static final List<ItemLuckyRound> LUCKY_ROUND_REWARDS = new ArrayList<>();
    public static final Map<String, Byte> IMAGES_BY_NAME = new HashMap<>();
    public static final List<ItemTemplate> ITEM_TEMPLATES = new ArrayList<>();
    public static final List<MobTemplate> MOB_TEMPLATES = new ArrayList<>();
    public static final List<NpcTemplate> NPC_TEMPLATES = new ArrayList<>();
    public static final List<String> CAPTIONS = new ArrayList<>();
    public static final List<TaskMain> TASKS = new ArrayList<>();
    public static final List<SideTaskTemplate> SIDE_TASKS_TEMPLATE = new ArrayList<>();
    public static final List<Intrinsic> INTRINSICS = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_TD = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_NM = new ArrayList<>();
    public static final List<Intrinsic> INTRINSIC_XD = new ArrayList<>();
    public static final List<HeadAvatar> HEAD_AVATARS = new ArrayList<>();
    public static final List<FlagBag> FLAGS_BAGS = new ArrayList<>();
    public static final List<NClass> NCLASS = new ArrayList<>();
    public static final List<Npc> NPCS = new ArrayList<>();
    public static List<Shop> SHOPS = new ArrayList<>();
    public static final List<Clan> CLANS = new ArrayList<>();
    public static final List<String> NOTIFY = new ArrayList<>();
    public static final ArrayList<DaiHoiVoThuat> LIST_DHVT = new ArrayList<>();
    public static final List<Item> RUBY_REWARDS = new ArrayList<>();
    public static final String queryTopSM = """
            SELECT id,
                   CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_point, ',', 2), ',', -1) AS UNSIGNED) AS sm
            FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1)
            ORDER BY sm DESC
            LIMIT 10;""";
    public static final String queryTopSD = """
            SELECT id,
                   CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_point, ',', 8), ',', -1) AS UNSIGNED) AS sd
            FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1)
            ORDER BY sd DESC
            LIMIT 10;""";
    public static final String queryTopHP = """
            SELECT id,
                   CAST(SUBSTRING_INDEX(SUBSTRING_INDEX(data_point, ',', 6), ',', -1) AS UNSIGNED) AS hp
            FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1)
            ORDER BY hp DESC
            LIMIT 10;""";
    public static final String queryTopKI = "SELECT id, CAST( split_str(data_point,',',7) AS UNSIGNED) AS ki FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( split_str(data_point,',',7)  AS UNSIGNED) DESC LIMIT 20;";
    public static final String queryTopNV = "SELECT id, CAST(SPLIT_STR(SPLIT_STR(data_task, ',', 1), '[', 2) AS UNSIGNED) AS nv\n" +
            "FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY "
            + " CAST(SPLIT_STR(SPLIT_STR(data_task, ',', 1), '[', 2) AS UNSIGNED) DESC,"
            + " CAST(SPLIT_STR(data_task, ',', 2) AS UNSIGNED) DESC,"
            + " CAST(SPLIT_STR(data_point, ',', 2) AS UNSIGNED) DESC LIMIT 10;";
    public static final String querytopSB = "SELECT id, CAST( split_str( data_inventory,',',5)  AS UNSIGNED) AS event FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( split_str( data_inventory,',',5)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String querytopSK = "SELECT id, CAST( split_str( data_inventory,',',4)  AS UNSIGNED) AS event FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( split_str( data_inventory,',',4)  AS UNSIGNED) DESC LIMIT 10;";
    public static final String queryTopPVP = "SELECT id, CAST( pointPvp AS UNSIGNED) AS pointPvp FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( pointPvp AS UNSIGNED) DESC LIMIT 10;";
    public static final String queryTopNHS = "SELECT id, CAST( NguHanhSonPoint AS UNSIGNED) AS nhs FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( NguHanhSonPoint AS UNSIGNED) DESC LIMIT 10;";
    public static final String queryTopVND = "SELECT id, CAST( vnd AS UNSIGNED) AS vnd FROM player WHERE id NOT IN (SELECT id FROM account WHERE is_admin = 1) ORDER BY CAST( vnd AS UNSIGNED) DESC LIMIT 10;";
    public static final String queryTopSieuHang = "SELECT id, CAST( split_str(data_point,',',18) AS UNSIGNED) AS rank FROM player ORDER BY CAST( split_str(data_point,',',18) AS UNSIGNED) ASC LIMIT 100";

    public static List<TOP> topSM;
    public static List<TOP> topSD;
    public static List<TOP> topHP;
    public static List<TOP> topKI;
    public static List<TOP> topNV;
    public static List<TOP> topSB;
    public static List<TOP> topSK;
    public static List<TOP> topPVP;
    public static List<TOP> topNHS;
    public static List<TOP> topSieuHang;
    public static List<TOP> topVND;
    public static long timeRealTop = 0;
    public static final short[] itemIds_CUI = {233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277, 281};
    public static final short[] itemIds_TL = {555, 557, 559, 556, 558, 560, 562, 564, 566, 563, 565, 567, 561};
    public static final byte[] itemIds_NR_SB = {17, 16};
    public static final byte[] itemIds_NR = {20, 19, 18, 17};
    public static final short[] SuKien_TrungThu = {1338, 1339, 1340};
    public static final short[] spl = {441, 442, 443, 444, 445, 446, 447};
    public static final short[] manhradaVIP = {859, 956, 1257, 1258, 1259};
    public static final short[] manhradaThuong = {828, 829, 830, 831, 832, 833, 834, 835, 836, 837, 838, 839, 840, 841, 842};
    public static final short[] danangcap = {220, 221, 222, 223, 224};
    public static final short[] dachienthan = {1263, 1264, 1265, 1266};
    public static final short[] itemDC12 = {233, 237, 241, 245, 249, 253, 257, 261, 265, 269, 273, 277};
    public static final short[] itemSkien = {537, 538, 539};
    public static final short[] itemManh = {1066, 1067, 1068, 1069, 1070};
    public static final short[] thucan = {663, 664, 665, 666, 667};

    public static final short[] aotd = {138, 139, 230, 231, 232, 233, 555};
    public static final short[] quantd = {142, 143, 242, 243, 244, 245, 556};
    public static final short[] gangtd = {146, 147, 254, 255, 256, 257, 562};
    public static final short[] giaytd = {150, 151, 266, 267, 268, 269, 563};
    public static final short[] aoxd = {170, 171, 238, 239, 240, 241, 559};
    public static final short[] quanxd = {174, 175, 250, 251, 252, 253, 560};
    public static final short[] gangxd = {178, 179, 262, 263, 264, 265, 566};
    public static final short[] giayxd = {182, 183, 274, 275, 276, 277, 567};
    public static final short[] aonm = {154, 155, 234, 235, 236, 237, 557};
    public static final short[] quannm = {158, 159, 246, 247, 248, 249, 558};
    public static final short[] gangnm = {162, 163, 258, 259, 260, 261, 564};
    public static final short[] giaynm = {166, 167, 270, 271, 272, 273, 565};

    public static final short[] radaSKHVip = {186, 187, 278, 279, 280, 281, 561};
    public static final short[] radaSKHThuong = {12};


    public static final short[][][] doSKHVip = {{aotd, quantd, gangtd, giaytd}, {aonm, quannm, gangnm, giaynm}, {aoxd, quanxd, gangxd, giayxd}};
    public static final short[][] doSKHThuong = {{0, 6, 21, 27}, {1, 7, 22, 28}, {2, 8, 23, 29}};
    //doSKHVip[gender][typeDo][randomLVDo]
    public static final List<AchievementTemplate> ACHIEVEMENTS = new ArrayList<>();

    public static Manager gI() {
        if (i == null) {
            i = new Manager();
        }
        return i;
    }

    private Manager() {
        try {
            loadProperties();
        } catch (IOException ex) {
            Logger.logException(Manager.class, ex, "Lỗi load properites");
            System.exit(0);
        }
        this.loadDatabase();
        NpcFactory.createNpcConMeo();
        NpcFactory.createNpcRongThieng();
        NpcFactory.createNpcRongXuong();
        this.initMap();
    }

    private void initMap() {
        int[][] tileTyleTop = readTileIndexTileType();
        for (MapTemplate mapTemp : mapTemplates) {
            int[][] tileMap = readTileMap(mapTemp.id);
            int[] tileTop = tileTyleTop[mapTemp.tileId - 1];
            com.girlkun.models.map.Map map = new com.girlkun.models.map.Map(mapTemp.id,
                    mapTemp.name, mapTemp.planetId, mapTemp.tileId, mapTemp.bgId,
                    mapTemp.bgType, mapTemp.type, tileMap, tileTop,
                    mapTemp.zones,
                    mapTemp.maxPlayerPerZone, mapTemp.wayPoints);
            MAPS.add(map);
            map.initMob(mapTemp.mobTemp, mapTemp.mobLevel, mapTemp.mobHp, mapTemp.mobX, mapTemp.mobY);
            map.initNpc(mapTemp.npcId, mapTemp.npcX, mapTemp.npcY);
            new Thread(map, "Update map " + map.mapName).start();
        }
        Referee r = new Referee();
        r.initReferee();

        Referee1 r1 = new Referee1();
        r1.initReferee1();

        TestDame r2 = new TestDame();
        r2.initTestDame();
    }

    public static void loadPart() {
        JSONArray dataArray;
        PreparedStatement ps;
        ResultSet rs;
        try (Connection con = GirlkunDB.getConnection()) {
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (Object o : dataArray) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(o));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/girlkun/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            dos.close();
            Logger.success("Load part thành công (" + parts.size() + ")\n");
        } catch (Exception e) {
            Logger.logException(Manager.class, e);
        }
    }

    private void loadDatabase() {
        long st = System.currentTimeMillis();
        JSONArray dataArray;
        JSONObject dataObject = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try (Connection con = GirlkunDB.getConnection()) {
            ps = con.prepareStatement("select * from part");
            rs = ps.executeQuery();
            List<Part> parts = new ArrayList<>();
            while (rs.next()) {
                Part part = new Part();
                part.id = rs.getShort("id");
                part.type = rs.getByte("type");
                dataArray = (JSONArray) JSONValue.parse(rs.getString("data").replaceAll("\\\"", ""));
                for (Object o : dataArray) {
                    JSONArray pd = (JSONArray) JSONValue.parse(String.valueOf(o));
                    part.partDetails.add(new PartDetail(Short.parseShort(String.valueOf(pd.get(0))),
                            Byte.parseByte(String.valueOf(pd.get(1))),
                            Byte.parseByte(String.valueOf(pd.get(2)))));
                    pd.clear();
                }
                parts.add(part);
                dataArray.clear();
            }
            DataOutputStream dos = new DataOutputStream(new FileOutputStream("data/girlkun/update_data/part"));
            dos.writeShort(parts.size());
            for (Part part : parts) {
                dos.writeByte(part.type);
                for (PartDetail partDetail : part.partDetails) {
                    dos.writeShort(partDetail.iconId);
                    dos.writeByte(partDetail.dx);
                    dos.writeByte(partDetail.dy);
                }
            }
            dos.flush();
            dos.close();
            Logger.success("Load part thành công (" + parts.size() + ")\n");

            //load small version
            ps = con.prepareStatement("select count(id) from small_version");
            rs = ps.executeQuery();
            List<byte[]> smallVersion = new ArrayList<>();
            if (rs.first()) {
                int size = rs.getInt(1);
                for (int i = 0; i < 4; i++) {
                    smallVersion.add(new byte[size]);
                }
            }
            ps = con.prepareStatement("select * from small_version order by id");
            rs = ps.executeQuery();
            int index = 0;
            while (rs.next()) {
                smallVersion.get(0)[index] = rs.getByte("x1");
                smallVersion.get(1)[index] = rs.getByte("x2");
                smallVersion.get(2)[index] = rs.getByte("x3");
                smallVersion.get(3)[index] = rs.getByte("x4");
                index++;
            }
            for (int i = 0; i < 4; i++) {
                dos = new DataOutputStream(new FileOutputStream("data/girlkun/data_img_version/x" + (i + 1) + "/img_version"));
                dos.writeShort(smallVersion.get(i).length);
                for (int j = 0; j < smallVersion.get(i).length; j++) {
                    dos.writeByte(smallVersion.get(i)[j]);
                }
                dos.flush();
                dos.close();
            }

            //load clan
            ps = con.prepareStatement("select * from clan_sv" + server);
            rs = ps.executeQuery();
            while (rs.next()) {
                Clan clan = new Clan();
                clan.id = rs.getInt("id");
                clan.name = rs.getString("name");
                clan.slogan = rs.getString("slogan");
                clan.imgId = rs.getByte("img_id");
                clan.powerPoint = rs.getLong("power_point");
                clan.maxMember = rs.getByte("max_member");
                clan.capsuleClan = rs.getInt("clan_point");
                clan.level = rs.getByte("level");
                clan.createTime = (int) (rs.getTimestamp("create_time").getTime() / 1000);
                dataArray = (JSONArray) JSONValue.parse(rs.getString("members"));
                for (Object o : dataArray) {
                    dataObject = (JSONObject) JSONValue.parse(String.valueOf(o));
                    ClanMember cm = new ClanMember();
                    cm.clan = clan;
                    cm.id = Integer.parseInt(String.valueOf(dataObject.get("id")));
                    cm.name = String.valueOf(dataObject.get("name"));
                    cm.head = Short.parseShort(String.valueOf(dataObject.get("head")));
                    cm.body = Short.parseShort(String.valueOf(dataObject.get("body")));
                    cm.leg = Short.parseShort(String.valueOf(dataObject.get("leg")));
                    cm.role = Byte.parseByte(String.valueOf(dataObject.get("role")));
                    cm.donate = Integer.parseInt(String.valueOf(dataObject.get("donate")));
                    cm.receiveDonate = Integer.parseInt(String.valueOf(dataObject.get("receive_donate")));
                    cm.memberPoint = Integer.parseInt(String.valueOf(dataObject.get("member_point")));
                    cm.clanPoint = Integer.parseInt(String.valueOf(dataObject.get("clan_point")));
                    cm.joinTime = Integer.parseInt(String.valueOf(dataObject.get("join_time")));
                    cm.timeAskPea = Long.parseLong(String.valueOf(dataObject.get("ask_pea_time")));
                    try {
                        cm.powerPoint = Long.parseLong(String.valueOf(dataObject.get("power")));
                    } catch (Exception e) {
                        Logger.logException(Manager.class, e);
                    }
                    clan.addClanMember(cm);
                }
                dataArray = (JSONArray) JSONValue.parse(rs.getString("doanh_trai"));
                if (!dataArray.isEmpty()) {
                    clan.doanhTrai_lastTimeOpen = Long.parseLong(String.valueOf(dataArray.get(0)));
                    clan.doanhTrai_playerOpen = (String) dataArray.get(1);
                }
//                dataArray = (JSONArray) JSONValue.parse(rs.getString("ban_do_kho_bau"));
//                if (!dataArray.isEmpty()) {
//                    clan.banDoKhoBau_lastTimeOpen = Long.parseLong(String.valueOf(dataArray.get(0)));
//                    clan.banDoKhoBau_playerOpen = (String) dataArray.get(1);
//                }
                CLANS.add(clan);
                dataArray.clear();
                dataObject.clear();
            }

            ps = con.prepareStatement("select id from clan_sv" + server + " order by id desc limit 1");
            rs = ps.executeQuery();
            if (rs.first()) {
                Clan.NEXT_ID = rs.getInt("id") + 1;
            }

            Logger.success("Load clan thành công (" + CLANS.size() + "), clan next id: " + Clan.NEXT_ID + "\n");

            ps = con.prepareStatement("select * from dhvt_template");
            rs = ps.executeQuery();
            if (rs.next()) {
                DaiHoiVoThuat dhvt = new DaiHoiVoThuat();
                dhvt.NameCup = rs.getString(2);
                dhvt.Time = rs.getString(3).split("\n");
                dhvt.gem = rs.getInt(4);
                dhvt.gold = rs.getInt(5);
                dhvt.min_start = rs.getInt(6);
                dhvt.min_start_temp = rs.getInt(6);
                dhvt.min_limit = rs.getInt(7);
                LIST_DHVT.add(dhvt);
            }

            Logger.success("Load DHVT thành công (" + LIST_DHVT.size() + ")\n");

            //load skill
            ps = con.prepareStatement("select * from skill_template order by nclass_id, slot");
            rs = ps.executeQuery();
            byte nClassId = -1;
            NClass nClass = null;
            while (rs.next()) {
                byte id = rs.getByte("nclass_id");
                if (id != nClassId) {
                    nClassId = id;
                    nClass = new NClass();
                    nClass.name = id == ConstPlayer.TRAI_DAT ? "Trái Đất" : id == ConstPlayer.NAMEC ? "Namếc" : "Xayda";
                    nClass.classId = nClassId;
                    NCLASS.add(nClass);
                }
                SkillTemplate skillTemplate = new SkillTemplate();
                skillTemplate.classId = nClassId;
                skillTemplate.id = rs.getByte("id");
                skillTemplate.name = rs.getString("name");
                skillTemplate.maxPoint = rs.getByte("max_point");
                skillTemplate.manaUseType = rs.getByte("mana_use_type");
                skillTemplate.type = rs.getByte("type");
                skillTemplate.iconId = rs.getShort("icon_id");
                skillTemplate.damInfo = rs.getString("dam_info");
                nClass.skillTemplatess.add(skillTemplate);

                dataArray = (JSONArray) JSONValue.parse(
                        rs.getString("skills")
                                .replaceAll("\\[\"", "[")
                                .replaceAll("\"\\[", "[")
                                .replaceAll("\"\\]", "]")
                                .replaceAll("\\]\"", "]")
                                .replaceAll("\\}\",\"\\{", "},{")
                );
                for (Object o : dataArray) {
                    JSONObject dts = (JSONObject) JSONValue.parse(String.valueOf(o));
                    Skill skill = new Skill();
                    skill.template = skillTemplate;
                    skill.skillId = Short.parseShort(String.valueOf(dts.get("id")));
                    skill.point = Byte.parseByte(String.valueOf(dts.get("point")));
                    skill.powRequire = Long.parseLong(String.valueOf(dts.get("power_require")));
                    skill.manaUse = Integer.parseInt(String.valueOf(dts.get("mana_use")));
                    skill.coolDown = Integer.parseInt(String.valueOf(dts.get("cool_down")));
                    skill.dx = Integer.parseInt(String.valueOf(dts.get("dx")));
                    skill.dy = Integer.parseInt(String.valueOf(dts.get("dy")));
                    skill.maxFight = Integer.parseInt(String.valueOf(dts.get("max_fight")));
                    skill.damage = Short.parseShort(String.valueOf(dts.get("damage")));
                    skill.price = Short.parseShort(String.valueOf(dts.get("price")));
                    skill.moreInfo = String.valueOf(dts.get("info"));
                    skillTemplate.skillss.add(skill);
                }
            }
            Logger.success("Load skill thành công (" + NCLASS.size() + ")\n");

            //load head avatar
            ps = con.prepareStatement("select * from head_avatar");
            rs = ps.executeQuery();
            while (rs.next()) {
                HeadAvatar headAvatar = new HeadAvatar(rs.getInt("head_id"), rs.getInt("avatar_id"));
                HEAD_AVATARS.add(headAvatar);
            }
            Logger.success("Load head avatar thành công (" + HEAD_AVATARS.size() + ")\n");

            //load flag bag
            ps = con.prepareStatement("select * from flag_bag");
            rs = ps.executeQuery();
            while (rs.next()) {
                FlagBag flagBag = new FlagBag();
                flagBag.id = rs.getInt("id");
                flagBag.name = rs.getString("name");
                flagBag.gold = rs.getInt("gold");
                flagBag.gem = rs.getInt("gem");
                flagBag.iconId = rs.getShort("icon_id");
                String[] iconData = rs.getString("icon_data").split(",");
                flagBag.iconEffect = new short[iconData.length];
                for (int j = 0; j < iconData.length; j++) {
                    flagBag.iconEffect[j] = Short.parseShort(iconData[j].trim());
                }
                FLAGS_BAGS.add(flagBag);
            }
            Logger.success("Load flag bag thành công (" + FLAGS_BAGS.size() + ")\n");

            //load intrinsic
            ps = con.prepareStatement("select * from intrinsic");
            rs = ps.executeQuery();
            while (rs.next()) {
                Intrinsic intrinsic = new Intrinsic();
                intrinsic.id = rs.getByte("id");
                intrinsic.name = rs.getString("name");
                intrinsic.paramFrom1 = rs.getShort("param_from_1");
                intrinsic.paramTo1 = rs.getShort("param_to_1");
                intrinsic.paramFrom2 = rs.getShort("param_from_2");
                intrinsic.paramTo2 = rs.getShort("param_to_2");
                intrinsic.icon = rs.getShort("icon");
                intrinsic.gender = rs.getByte("gender");
                switch (intrinsic.gender) {
                    case ConstPlayer.TRAI_DAT:
                        INTRINSIC_TD.add(intrinsic);
                        break;
                    case ConstPlayer.NAMEC:
                        INTRINSIC_NM.add(intrinsic);
                        break;
                    case ConstPlayer.XAYDA:
                        INTRINSIC_XD.add(intrinsic);
                        break;
                    default:
                        INTRINSIC_TD.add(intrinsic);
                        INTRINSIC_NM.add(intrinsic);
                        INTRINSIC_XD.add(intrinsic);
                }
                INTRINSICS.add(intrinsic);
            }
            Logger.success("Load intrinsic thành công (" + INTRINSICS.size() + ")\n");

            //load task
            ps = con.prepareStatement("SELECT id, task_main_template.name, detail, "
                    + "task_sub_template.name AS 'sub_name', max_count, notify, npc_id, map "
                    + "FROM task_main_template JOIN task_sub_template ON task_main_template.id = "
                    + "task_sub_template.task_main_id");
            rs = ps.executeQuery();
            int taskId = -1;
            TaskMain task = null;
            while (rs.next()) {
                int id = rs.getInt("id");
                if (id != taskId) {
                    taskId = id;
                    task = new TaskMain();
                    task.id = taskId;
                    task.name = rs.getString("name");
                    task.detail = rs.getString("detail");
                    TASKS.add(task);
                }
                SubTaskMain subTask = new SubTaskMain();
                subTask.name = rs.getString("sub_name");
                subTask.maxCount = rs.getShort("max_count");
                subTask.notify = rs.getString("notify");
                subTask.npcId = rs.getByte("npc_id");
                subTask.mapId = rs.getShort("map");
                task.subTasks.add(subTask);
            }
            Logger.success("Load task thành công (" + TASKS.size() + ")\n");
            // load nhiem vu bo mong
            ps = con.prepareStatement("SELECT * FROM achievement");
            rs = ps.executeQuery();
            while (rs.next()) {
                AchievementTemplate achi = new AchievementTemplate(
                        rs.getInt("id"),
                        rs.getString("info1"),
                        rs.getString("info2"),
                        rs.getDouble("count_purpose"),
                        rs.getInt("gem"));
                ACHIEVEMENTS.add(achi);
            }
            Logger.success("Load achievement thành công (" + ACHIEVEMENTS.size() + ")\n");
            //load side task
            ps = con.prepareStatement("select * from side_task_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                SideTaskTemplate sideTask = new SideTaskTemplate();
                sideTask.id = rs.getInt("id");
                sideTask.name = rs.getString("name");
                String[] mc1 = rs.getString("max_count_lv1").split("-");
                String[] mc2 = rs.getString("max_count_lv2").split("-");
                String[] mc3 = rs.getString("max_count_lv3").split("-");
                String[] mc4 = rs.getString("max_count_lv4").split("-");
                String[] mc5 = rs.getString("max_count_lv5").split("-");
                sideTask.count[0][0] = Integer.parseInt(mc1[0]);
                sideTask.count[0][1] = Integer.parseInt(mc1[1]);
                sideTask.count[1][0] = Integer.parseInt(mc2[0]);
                sideTask.count[1][1] = Integer.parseInt(mc2[1]);
                sideTask.count[2][0] = Integer.parseInt(mc3[0]);
                sideTask.count[2][1] = Integer.parseInt(mc3[1]);
                sideTask.count[3][0] = Integer.parseInt(mc4[0]);
                sideTask.count[3][1] = Integer.parseInt(mc4[1]);
                sideTask.count[4][0] = Integer.parseInt(mc5[0]);
                sideTask.count[4][1] = Integer.parseInt(mc5[1]);
                SIDE_TASKS_TEMPLATE.add(sideTask);
            }
            Logger.success("Load side task thành công (" + SIDE_TASKS_TEMPLATE.size() + ")\n");

            //load item template
            ps = con.prepareStatement("select * from item_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemTemplate itemTemp = new ItemTemplate();
                itemTemp.id = rs.getShort("id");
                itemTemp.type = rs.getByte("type");
                itemTemp.gender = rs.getByte("gender");
                itemTemp.name = rs.getString("name");
                itemTemp.description = rs.getString("description");
                itemTemp.iconID = rs.getShort("icon_id");
                itemTemp.part = rs.getShort("part");
                itemTemp.isUpToUp = rs.getBoolean("is_up_to_up");
                itemTemp.strRequire = rs.getInt("power_require");
                itemTemp.gold = rs.getInt("gold");
                itemTemp.gem = rs.getInt("gem");
                itemTemp.head = rs.getInt("head");
                itemTemp.body = rs.getInt("body");
                itemTemp.leg = rs.getInt("leg");
                ITEM_TEMPLATES.add(itemTemp);
            }
            Logger.success("Load map item template thành công (" + ITEM_TEMPLATES.size() + ")\n");

            //load item option template
            ps = con.prepareStatement("select id, name from item_option_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                ItemOptionTemplate optionTemp = new ItemOptionTemplate();
                optionTemp.id = rs.getInt("id");
                optionTemp.name = rs.getString("name");
                ITEM_OPTION_TEMPLATES.add(optionTemp);
            }
            Logger.success("Load map item option template thành công (" + ITEM_OPTION_TEMPLATES.size() + ")\n");

            //load shop
            SHOPS = ShopDAO.getShops(con);
            Logger.success("Load shop thành công (" + SHOPS.size() + ")\n");

            //load reward lucky round
            File folder = new File("data/girlkun/data_lucky_round_reward");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line = null;
                    while ((line = br.readLine()) != null) {
                        line = line.replaceAll("[{}\\[\\]]", "");
                        String[] arrSub = line.split("\\|");
                        String[] data1 = arrSub[0].split(":");
                        ItemLuckyRound item = new ItemLuckyRound();
                        item.temp = ItemService.gI().getTemplate(Integer.parseInt(data1[0]));
                        item.ratio = Integer.parseInt(data1[1]);
                        item.typeRatio = Integer.parseInt(data1[2]);
                        if (arrSub.length > 1) {
                            String[] data2 = arrSub[1].split(";");
                            for (String str : data2) {
                                String[] data = str.split(":");
                                ItemOptionLuckyRound io = new ItemOptionLuckyRound();
                                io.itemOption = new Item.ItemOption(Integer.parseInt(data[0]), 0);
                                String[] param = data[1].split("-");
                                io.param1 = Integer.parseInt(param[0]);
                                if (param.length == 2) {
                                    io.param2 = Integer.parseInt(param[1]);
                                }
                                item.itemOptions.add(io);
                            }
                        }
                        LUCKY_ROUND_REWARDS.add(item);
                    }
                }
            }
            Logger.success("Load reward lucky round thành công (" + LUCKY_ROUND_REWARDS.size() + ")\n");
            //load reward mob
            folder = new File("data/girlkun/mob_reward");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    DataInputStream dis = new DataInputStream(new FileInputStream(fileEntry));
                    int size = dis.readInt();
                    for (int i = 0; i < size; i++) {
                        int mobId = dis.readInt();
                        MobReward mobReward = MOB_REWARDS.get(mobId);
                        if (mobReward == null) {
                            mobReward = new MobReward(mobId);
                            MOB_REWARDS.put(mobId, mobReward);
                        }
                        int itemId = dis.readInt();
                        String[] quantity = dis.readUTF().split("-");
                        String[] ratio = dis.readUTF().split("-");
                        int gender = dis.readInt();
                        String map = dis.readUTF();
                        String[] arrMap = map.replaceAll("[\\]\\[]", "").split(",");
                        int[] mapDrop = new int[arrMap.length];
                        for (int g = 0; g < mapDrop.length; g++) {
                            mapDrop[g] = Integer.parseInt(arrMap[g]);
                        }
                        ItemMobReward item = new ItemMobReward(itemId, mapDrop,
                                new int[]{Integer.parseInt(quantity[0]), Integer.parseInt(quantity[1])},
                                new int[]{Integer.parseInt(ratio[0]), Integer.parseInt(ratio[1])}, gender);
                        if (item.getTemp().type == 30) { // sao pha lê
                            item.setRatio(new int[]{20, Integer.parseInt(ratio[1])});
                        }
                        if (item.getTemp().type == 14) { //14 đá nâng cấp
                            item.setRatio(new int[]{20, Integer.parseInt(ratio[1])});
                        }
                        if (item.getTemp().type < 5) {
                            item.setRatio(new int[]{Integer.parseInt(ratio[0]), Integer.parseInt(ratio[1]) / 4 * 3});
                        }

                        if (item.getTemp().type == 9) { //vàng
                            mobReward.getGoldReward().add(item);
                        } else {
                            mobReward.getItemReward().add(item);
                        }
                        int sizeOption = dis.readInt();
                        for (int j = 0; j < sizeOption; j++) {
                            int optionId = dis.readInt();
                            String[] param = dis.readUTF().split("-");
                            String[] ratioOption = dis.readUTF().split("-");
                            ItemOptionMobReward option = new ItemOptionMobReward(optionId,
                                    new int[]{Integer.parseInt(param[0]), Integer.parseInt(param[1])},
                                    new int[]{Integer.parseInt(ratioOption[0]), Integer.parseInt(ratioOption[1])});
                            item.getOption().add(option);
                        }
                    }

                }
            }
            Logger.success("Load reward mob thành công (" + MOB_REWARDS.size() + ")\n");

            //load notify
            folder = new File("data/girlkun/notify");
            for (File fileEntry : folder.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    StringBuffer notify = new StringBuffer(fileEntry.getName().substring(0, fileEntry.getName().lastIndexOf("."))).append("<>");
                    BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(fileEntry), StandardCharsets.UTF_8));
                    String line;
                    while ((line = br.readLine()) != null) {
                        notify.append(line).append("\n");
                    }
                    NOTIFY.add(notify.toString());
                }
            }
            Logger.success("Load notify thành công (" + NOTIFY.size() + ")\n");

            //load caption
            ps = con.prepareStatement("select * from caption");
            rs = ps.executeQuery();
            while (rs.next()) {
                CAPTIONS.add(rs.getString("name"));
            }
            Logger.success("Load caption thành công (" + CAPTIONS.size() + ")\n");


            //load image by name
            ps = con.prepareStatement("select name, n_frame from img_by_name");
            rs = ps.executeQuery();
            while (rs.next()) {
                IMAGES_BY_NAME.put(rs.getString("name"), rs.getByte("n_frame"));
            }
            Logger.success("Load images by name thành công (" + IMAGES_BY_NAME.size() + ")\n");

            //load mob template
            ps = con.prepareStatement("select * from mob_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                MobTemplate mobTemp = new MobTemplate();
                mobTemp.id = rs.getByte("id");
                mobTemp.type = rs.getByte("type");
                mobTemp.name = rs.getString("name");
                mobTemp.hp = rs.getInt("hp");
                mobTemp.rangeMove = rs.getByte("range_move");
                mobTemp.speed = rs.getByte("speed");
                mobTemp.dartType = rs.getByte("dart_type");
                mobTemp.percentDame = rs.getByte("percent_dame");
                mobTemp.percentTiemNang = rs.getByte("percent_tiem_nang");
                MOB_TEMPLATES.add(mobTemp);
            }
            Logger.success("Load mob template thành công (" + MOB_TEMPLATES.size() + ")\n");

            ps = con.prepareStatement("SELECT * FROM shop_ky_gui");
            rs = ps.executeQuery();
            while (rs.next()) {
                int i = rs.getInt("id");
                int idPl = rs.getInt("player_id");
                byte tab = rs.getByte("tab");
                short itemId = rs.getShort("item_id");
                int gold = rs.getInt("gold");
                int gem = rs.getInt("gem");
                int quantity = rs.getInt("quantity");
                byte isUp = rs.getByte("isUpTop");
                boolean isBuy = rs.getByte("isBuy") == 1;
                long thoigian = rs.getLong("Thoi_gian");
                List<Item.ItemOption> op = new ArrayList<>();
                JSONArray jsa2 = (JSONArray) JSONValue.parse(rs.getString("itemOption"));
                for (int j = 0; j < jsa2.size(); ++j) {
                    JSONObject jso2 = (JSONObject) jsa2.get(j);
                    int idOptions = Integer.parseInt(jso2.get("id").toString());
                    int param = Integer.parseInt(jso2.get("param").toString());
                    op.add(new Item.ItemOption(idOptions, param));
                }
                ShopKyGuiManager.gI().listItem.add(new ItemKyGui(i, itemId, idPl, tab, gold, gem, quantity, isUp, op, isBuy, thoigian));
            }
            Logger.success("Thông báo tải dữ liệu item ký gửi [" + ShopKyGuiManager.gI().listItem.size() + "]!\n");

            //load npc template
            ps = con.prepareStatement("select * from npc_template");
            rs = ps.executeQuery();
            while (rs.next()) {
                NpcTemplate npcTemp = new NpcTemplate();
                npcTemp.id = rs.getByte("id");
                npcTemp.name = rs.getString("name");
                npcTemp.head = rs.getShort("head");
                npcTemp.body = rs.getShort("body");
                npcTemp.leg = rs.getShort("leg");
                npcTemp.avatar = rs.getInt("avatar");
                NPC_TEMPLATES.add(npcTemp);
            }
            Logger.success("Load npc template thành công (" + NPC_TEMPLATES.size() + ")\n");

            //load map template
            ps = con.prepareStatement("select count(id) from map_template");
            rs = ps.executeQuery();
            if (rs.first()) {
                int countRow = rs.getShort(1);
                mapTemplates = new MapTemplate[countRow];
                ps = con.prepareStatement("select * from map_template");
                rs = ps.executeQuery();
                short i = 0;
                while (rs.next()) {
                    MapTemplate mapTemplate = new MapTemplate();
                    int mapId = rs.getInt("id");
                    String mapName = rs.getString("name");
                    mapTemplate.id = mapId;
                    mapTemplate.name = mapName;
                    //load data

//                    dataArray = (JSONArray) jv.parse(rs.getString("data"));
//                    mapTemplate.type = Byte.parseByte(String.valueOf(dataArray.get(0)));
//                    mapTemplate.planetId = Byte.parseByte(String.valueOf(dataArray.get(1)));
//                    mapTemplate.bgType = Byte.parseByte(String.valueOf(dataArray.get(2)));
//                    mapTemplate.tileId = Byte.parseByte(String.valueOf(dataArray.get(3)));
//                    mapTemplate.bgId = Byte.parseByte(String.valueOf(dataArray.get(4)));
//                    dataArray.clear();
                    mapTemplate.type = rs.getByte("type");
                    mapTemplate.planetId = rs.getByte("planet_id");
                    mapTemplate.bgType = rs.getByte("bg_type");
                    mapTemplate.tileId = rs.getByte("tile_id");
                    mapTemplate.bgId = rs.getByte("bg_id");
                    mapTemplate.zones = rs.getByte("zones");
                    mapTemplate.maxPlayerPerZone = rs.getByte("max_player");
                    //load waypoints
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("waypoints")
                            .replaceAll("\\[\"\\[", "[[")
                            .replaceAll("\\]\"\\]", "]]")
                            .replaceAll("\",\"", ",")
                    );
                    for (int j = 0; j < dataArray.size(); j++) {
                        WayPoint wp = new WayPoint();
                        JSONArray dtwp = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        wp.name = String.valueOf(dtwp.get(0));
                        wp.minX = Short.parseShort(String.valueOf(dtwp.get(1)));
                        wp.minY = Short.parseShort(String.valueOf(dtwp.get(2)));
                        wp.maxX = Short.parseShort(String.valueOf(dtwp.get(3)));
                        wp.maxY = Short.parseShort(String.valueOf(dtwp.get(4)));
                        wp.isEnter = Byte.parseByte(String.valueOf(dtwp.get(5))) == 1;
                        wp.isOffline = Byte.parseByte(String.valueOf(dtwp.get(6))) == 1;
                        wp.goMap = Short.parseShort(String.valueOf(dtwp.get(7)));
                        wp.goX = Short.parseShort(String.valueOf(dtwp.get(8)));
                        wp.goY = Short.parseShort(String.valueOf(dtwp.get(9)));
                        mapTemplate.wayPoints.add(wp);
                        dtwp.clear();
                    }
                    dataArray.clear();
                    //load mobs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("mobs").replaceAll("\\\"", ""));
                    mapTemplate.mobTemp = new byte[dataArray.size()];
                    mapTemplate.mobLevel = new byte[dataArray.size()];
                    mapTemplate.mobHp = new double[dataArray.size()];
                    mapTemplate.mobX = new short[dataArray.size()];
                    mapTemplate.mobY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtm = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.mobTemp[j] = Byte.parseByte(String.valueOf(dtm.get(0)));
                        mapTemplate.mobLevel[j] = Byte.parseByte(String.valueOf(dtm.get(1)));
                        mapTemplate.mobHp[j] = Double.parseDouble(String.valueOf(dtm.get(2)));
                        mapTemplate.mobX[j] = Short.parseShort(String.valueOf(dtm.get(3)));
                        mapTemplate.mobY[j] = Short.parseShort(String.valueOf(dtm.get(4)));
                        dtm.clear();
                    }
                    dataArray.clear();
                    //load npcs
                    dataArray = (JSONArray) JSONValue.parse(rs.getString("npcs").replaceAll("\\\"", ""));
                    mapTemplate.npcId = new byte[dataArray.size()];
                    mapTemplate.npcX = new short[dataArray.size()];
                    mapTemplate.npcY = new short[dataArray.size()];
                    for (int j = 0; j < dataArray.size(); j++) {
                        JSONArray dtn = (JSONArray) JSONValue.parse(String.valueOf(dataArray.get(j)));
                        mapTemplate.npcId[j] = Byte.parseByte(String.valueOf(dtn.get(0)));
                        mapTemplate.npcX[j] = Short.parseShort(String.valueOf(dtn.get(1)));
                        mapTemplate.npcY[j] = Short.parseShort(String.valueOf(dtn.get(2)));
                        dtn.clear();
                    }
                    dataArray.clear();
                    mapTemplates[i++] = mapTemplate;
                }
                Logger.success("Load map template thành công (" + mapTemplates.length + ")\n");
                RUBY_REWARDS.add(Util.sendDo(861, 0, new ArrayList<>()));
            }
            ps = con.prepareStatement("select * from radar");
            rs = ps.executeQuery();
            while (rs.next()) {
                RadarCard rd = new RadarCard();
                rd.Id = rs.getShort("id");
                rd.IconId = rs.getShort("iconId");
                rd.Rank = rs.getByte("rank");
                rd.Max = rs.getByte("max");
                rd.Type = rs.getByte("type");
                rd.Template = rs.getShort("template");
                rd.Name = rs.getString("name");
                rd.Info = rs.getString("info");
                JSONArray arr = (JSONArray) JSONValue.parse(rs.getString("body"));
                for (Object o : arr) {
                    JSONObject ob = (JSONObject) o;
                    if (ob != null) {
                        rd.Head = Short.parseShort(ob.get("head").toString());
                        rd.Body = Short.parseShort(ob.get("body").toString());
                        rd.Leg = Short.parseShort(ob.get("leg").toString());
                        rd.Bag = Short.parseShort(ob.get("bag").toString());
                    }
                }
                rd.Options.clear();
                arr = (JSONArray) JSONValue.parse(rs.getString("options"));
                for (Object o : arr) {
                    JSONObject ob = (JSONObject) o;
                    if (ob != null) {
                        rd.Options.add(new OptionCard(Integer.parseInt(ob.get("id").toString()),
                                Short.parseShort(ob.get("param").toString()),
                                Byte.parseByte(ob.get("activeCard").toString())));
                    }
                }
                rd.Require = rs.getShort("require");
                rd.RequireLevel = rs.getShort("require_level");
                rd.AuraId = rs.getShort("aura_id");
                RadarService.gI().RADAR_TEMPLATE.add(rd);
            }
            Logger.success("Load radar template thành công (" + RadarService.gI().RADAR_TEMPLATE.size() + ")\n");
            topSM = realTop(queryTopSM, con);
            Logger.success("Load top sm thành công (" + topSM.size() + ")\n");
            topNV = realTop(queryTopNV, con);
            Logger.success("Load top nv thành công (" + topNV.size() + ")\n");
            topSB = realTop(querytopSB, con);
            Logger.success("Load top săn boss thành công (" + topSB.size() + ")\n");
            topPVP = realTop(queryTopPVP, con);
            Logger.success("Load top pvp thành công (" + topSB.size() + ")\n");
            topNHS = realTop(queryTopNHS, con);
            Logger.success("Load top NHS thành công (" + topSB.size() + ")\n");
            topSK = realTop(querytopSK, con);
            Logger.success("Load top Sự kiện thành công (" + topSK.size() + ")\n");
//            topSieuHang = realTopSieuHang(con);
//            Logger.success("Load top Siêu hạng thành công (" + topSieuHang.size() + ")\n");
            topSieuHang = realTop(queryTopSieuHang, con);
            Logger.success("Load top Siêu hạng thành công (" + topSieuHang.size() + ")\n");
            topSD = realTop(queryTopSD, con);
            Logger.success("Load top Sức đánh thành công (" + topSD.size() + ")\n");
            topHP = realTop(queryTopHP, con);
            Logger.success("Load top HP thành công (" + topHP.size() + ")\n");
            topVND = realTop(queryTopVND, con);
            Logger.success("Load top Nạp thành công (" + topVND.size() + ")\n");
            Manager.timeRealTop = System.currentTimeMillis();
            try {
                rs.close();
                ps.close();
            } catch (SQLException ex) {
                Logger.logException(Manager.class, ex, "Error close sql");
            }
        } catch (Exception e) {
            Logger.logException(Manager.class, e, "Lỗi load database");
            System.exit(0);
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
                if (ps != null) {
                    ps.close();
                }
            } catch (SQLException ex) {
                Logger.logException(Manager.class, ex, "Error close sql");
            }
        }
        Logger.log(Logger.PURPLE, "Tổng thời gian load database: " + (System.currentTimeMillis() - st) + "(ms)\n");
    }

    public static List<TOP> realTop(String query, Connection con) {
        List<TOP> tops = new ArrayList<>();
        try {
            PreparedStatement ps = con.prepareStatement(query);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TOP top = TOP.builder().id_player(rs.getInt("id")).build();
                switch (query) {
                    case queryTopSM -> {
                        top.setInfo1(Util.powerToString(rs.getLong("sm")) + " Sức Mạnh");
                        top.setInfo2(Util.format(rs.getLong("sm")) + " Sức Mạnh");
                    }
                    case queryTopNV -> {
                        top.setInfo1(rs.getByte("nv") + "");
                        top.setInfo2(TASKS.get(rs.getByte("nv")).name);
                    }
                    case queryTopVND -> {
                        top.setInfo1(Util.format(rs.getInt("vnd")) + " VNĐ");
                        top.setInfo2(Util.format(rs.getInt("vnd")) + " VNĐ");
                    }
                    case queryTopHP -> {
                        top.setInfo1(Util.powerToString(rs.getInt("hp")) + " HP");
                        top.setInfo2(Util.format(rs.getInt("hp")) + " HP");
                    }
                    case querytopSB, querytopSK -> {
                        top.setInfo1(rs.getInt("event") + " điểm");
                        top.setInfo2(rs.getInt("event") + " điểm");
                    }
                    case queryTopPVP -> {
                        top.setInfo1(rs.getInt("pointPvp") + " điểm");
                        top.setInfo2(rs.getInt("pointPvp") + " điểm");
                    }
                    case queryTopNHS -> {
                        top.setInfo1(rs.getInt("nhs") + " điểm");
                        top.setInfo2(rs.getInt("nhs") + " điểm");
                    }
                    case queryTopSD -> {
                        top.setInfo1(Util.powerToString(rs.getInt("sd")) + " Sức đánh");
                        top.setInfo2(Util.format(rs.getInt("sd")) + " Sức đánh");
                    }
                    case queryTopSieuHang -> {
                        long rank = rs.getLong("rank");
                        if (rank > 0 && rank <= 100) {
                            top.setInfo1("Hạng: " + rank);
                            top.setInfo2("Hạng: " + rank);
                            tops.add(top);
                        }
                    }
                }
                tops.add(top);
            }
        } catch (Exception e) {
            Logger.logException(Manager.class, e);
        }
        return tops;
    }


    public void loadProperties() throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream("data/girlkun/girlkun.properties"));
        Object value = null;
        //###Config sv
        if ((value = properties.get("server.girlkun.port")) != null) {
            ServerManager.PORT = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.name")) != null) {
            ServerManager.NAME = String.valueOf(value);
        }
        if ((value = properties.get("server.girlkun.sv")) != null) {
            server = Byte.parseByte(String.valueOf(value));
        }
        StringBuilder linkServer = new StringBuilder();
        for (int i = 1; i <= 10; i++) {
            value = properties.get("server.girlkun.sv" + i);
            if (value != null) {
                linkServer.append(value).append(":0,");
            }
        }
        DataGame.LINK_IP_PORT = linkServer.substring(0, linkServer.length() - 1);
        if ((value = properties.get("server.girlkun.waitlogin")) != null) {
            secondWaitLogin = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.maxperip")) != null) {
            maxPerIp = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.maxplayer")) != null) {
            maxPlayer = Integer.parseInt(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.expserver")) != null) {
            rateExpServer = Byte.parseByte(String.valueOf(value));
        }
        if ((value = properties.get("server.girlkun.local")) != null) {
            local = String.valueOf(value).equalsIgnoreCase("true");
        }
    }


    private int[][] readTileIndexTileType() {
        int[][] tileIndexTileType = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/girlkun/map/tile_set_info"));
            int numTileMap = dis.readByte();
            tileIndexTileType = new int[numTileMap][];
            for (int i = 0; i < numTileMap; i++) {
                int numTileOfMap = dis.readByte();
                for (int j = 0; j < numTileOfMap; j++) {
                    int tileType = dis.readInt();
                    int numIndex = dis.readByte();
                    if (tileType == ConstMap.TILE_TOP) {
                        tileIndexTileType[i] = new int[numIndex];
                    }
                    for (int k = 0; k < numIndex; k++) {
                        int typeIndex = dis.readByte();
                        if (tileType == ConstMap.TILE_TOP) {
                            tileIndexTileType[i][k] = typeIndex;
                        }
                    }
                }
            }
        } catch (Exception e) {
            Logger.logException(MapService.class, e);
        }
        return tileIndexTileType;
    }

    private int[][] readTileMap(int mapId) {
        int[][] tileMap = null;
        try {
            DataInputStream dis = new DataInputStream(new FileInputStream("data/girlkun/map/tile_map_data/" + mapId));
            int w = dis.readByte();
            int h = dis.readByte();
            tileMap = new int[h][w];
            for (int i = 0; i < tileMap.length; i++) {
                for (int j = 0; j < tileMap[i].length; j++) {
                    tileMap[i][j] = dis.readByte();
                }
            }
            dis.close();
        } catch (Exception e) {
            Logger.logException(Manager.class, e);
        }
        return tileMap;
    }


    public static void addClan(Clan clan) {
        CLANS.add(clan);
    }

    public static int getNumClan() {
        return CLANS.size();

    }

    public static MobTemplate getMobTemplateByTemp(int mobTempId) {
        for (MobTemplate mobTemp : MOB_TEMPLATES) {
            if (mobTemp.id == mobTempId) {
                return mobTemp;
            }
        }
        return null;
    }

    public static byte getNFrameImageByName(String name) {
        Object n = IMAGES_BY_NAME.get(name);
        if (n != null) {
            return Byte.parseByte(String.valueOf(n));
        } else {
            return 0;
        }
    }
}
