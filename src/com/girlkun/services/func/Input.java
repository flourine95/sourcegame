package com.girlkun.services.func;

import com.girlkun.consts.ConstNpc;
import com.girlkun.database.GirlkunDB;
import com.girlkun.jdbc.daos.PlayerDAO;
import com.girlkun.models.item.Item;
import com.girlkun.models.map.Zone;
import com.girlkun.models.npc.Npc;
import com.girlkun.models.npc.NpcManager;
import com.girlkun.models.player.Giftcode;
import com.girlkun.models.player.Player;
import com.girlkun.models.shop.ShopServiceNew;
import com.girlkun.network.io.Message;
import com.girlkun.network.session.ISession;
import com.girlkun.server.Client;
import com.girlkun.server.Manager;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.ItemService;
import com.girlkun.services.NpcService;
import com.girlkun.services.Service;
import com.girlkun.utils.Util;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class Input {

    private static final Map<Integer, Object> PLAYER_ID_OBJECT = new HashMap<Integer, Object>();

    public static final int CHANGE_PASSWORD = 500;
    public static final int GIFT_CODE = 501;
    public static final int FIND_PLAYER = 502;
    public static final int CHANGE_NAME = 503;
    public static final int CHOOSE_LEVEL_BDKB = 504;
    public static final int NAP_THE = 505;
    public static final int CHOOSE_LEVEL_GAS = 512;
    public static final int CHANGE_NAME_BY_ITEM = 506;
    public static final int GIVE_IT = 507;
    public static final int QUY_DOI_COIN = 508;
    public static final int QUY_DOI_COIN_1 = 509;
    public static final int XIU = 510;
    public static final int TAI = 511;
    public static final int BUFF_ITEM_VIP = 513;
    public static final int XIU_taixiu = 514;
    public static final int TAI_taixiu = 515;
    public static final int TAO_PET = 520;
    public static final int DOI_XU = 521;
    public static final int VE_HONG_NGOC = 522;
    public static final int MUA_NHIEU_VP = 523;

    public static final byte NUMERIC = 0;
    public static final byte ANY = 1;
    public static final byte PASSWORD = 2;

    private static Input intance;

    private Input() {

    }

    public static Input gI() {
        if (intance == null) {
            intance = new Input();
        }
        return intance;
    }

    public void doInput(Player player, Message msg) {
        try {
            String[] text = new String[msg.reader().readByte()];
            for (int i = 0; i < text.length; i++) {
                text[i] = msg.reader().readUTF();
            }
            switch (player.iDMark.getTypeInput()) {
                case GIVE_IT:
                    String name = text[0];
                    int id = Integer.valueOf(text[1]);
                    int op = Integer.valueOf(text[2]);
                    int pr = Integer.valueOf(text[3]);
                    int q = Integer.valueOf(text[4]);

                    if (Client.gI().getPlayer(name) != null) {
                        Item item = ItemService.gI().createNewItem(((short) id));
                        item.quantity = q;
                        item.itemOptions.add(new Item.ItemOption(op, pr));
                        InventoryServiceNew.gI().addItemBag(Client.gI().getPlayer(name), item);
                        InventoryServiceNew.gI().sendItemBags(Client.gI().getPlayer(name));
                        Service.getInstance().sendThongBao(Client.gI().getPlayer(name), "Nhận được " + item.template.name + " từ " + player.name);

                    } else {
                        Service.getInstance().sendThongBao(player, "Không online");
                    }
                    break;
                case VE_HONG_NGOC:
                    String namee = text[0];
                    int hongngoc = Integer.parseInt(text[1]);

                    if (Client.gI().getPlayer(namee) != null) {
                        Item item = ItemService.gI().createNewItem(((short) 861));
                        item.quantity = hongngoc;
                        if (player.inventory.ruby >= hongngoc + 100) {
                            player.inventory.ruby -= hongngoc + 100;
                            Service.gI().sendMoney(Client.gI().getPlayer(namee));
                            InventoryServiceNew.gI().addItemBag(Client.gI().getPlayer(namee), item);
                            InventoryServiceNew.gI().sendItemBags(Client.gI().getPlayer(namee));
                            Service.gI().sendMoney(Client.gI().getPlayer(namee));
                            Service.gI().sendMoney(player);
                            Service.getInstance().sendThongBao(Client.gI().getPlayer(namee), "|1|Nhận được " + Util.format(hongngoc) + " Hồng ngọc từ " + player.name);
                        } else {
                            Service.getInstance().sendThongBao(player, "|7|Số Hồng ngọc vượt quá Hồng ngọc của bạn");
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không online");
                    }
                    break;
                case MUA_NHIEU_VP:
                    player.highPurchaseCount = Integer.parseInt(text[0]);
                    if (player.highPurchaseCount > 1000) {
                        Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mua. Tối đa 1000 Vật phẩm");
                        player.highPurchaseCount = 0;
                        player.idItemHighPurchase = -1;
                    } else {
                        Thread thread = new Thread(() -> {
                            try {
                                for (int i = 0; i < player.highPurchaseCount; i++) {
                                    ShopServiceNew.gI().buyItem(player, player.idItemHighPurchase);
                                    Service.getInstance().sendMoney(player);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    Thread.sleep(100);
                                }
                                Service.getInstance().sendThongBao(player, "|1|Đã mua xong " + player.highPurchaseCount + " vật phẩm");
                                Service.getInstance().sendMoney(player);
                                InventoryServiceNew.gI().sendItemBags(player);
                                player.highPurchaseCount = 0;
                                player.idItemHighPurchase = -1;
                            } catch (Exception e) {
                                System.out.println("   Loi Mua Nhieu");
                            }
                        });
                        thread.start(); // Khởi động luồng mới
                    }
                    break;
                case CHANGE_PASSWORD:
                    Service.getInstance().changePassword(player, text[0], text[1], text[2]);
                    break;
                case GIFT_CODE:
                    Giftcode.gI().giftCode(player, text[0]);
                    break;
                case FIND_PLAYER:
                    Player pl = Client.gI().getPlayer(text[0]);
                    if (pl != null) {
                        NpcService.gI().createMenuConMeo(player, ConstNpc.MENU_FIND_PLAYER, -1, "Ngài muốn..?",
                                new String[]{"Đi tới\n" + pl.name, "Gọi " + pl.name + "\ntới đây", "Đổi tên", "Ban", "Kick"},
                                pl);
                    } else {
                        Service.getInstance().sendThongBao(player, "Người chơi không tồn tại hoặc đang offline");
                    }
                    break;
                case CHANGE_NAME: {
                    Player plChanged = (Player) PLAYER_ID_OBJECT.get((int) player.id);
                    if (plChanged != null) {
                        if (GirlkunDB.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.getInstance().sendThongBao(player, "Tên nhân vật đã tồn tại");
                        } else {
                            plChanged.name = text[0];
                            GirlkunDB.executeUpdate("update player set name = ? where id = ?", plChanged.name, plChanged.id);
                            Service.getInstance().player(plChanged);
                            Service.getInstance().Send_Caitrang(plChanged);
                            Service.getInstance().sendFlagBag(plChanged);
                            Zone zone = plChanged.zone;
                            ChangeMapService.gI().changeMap(plChanged, zone, plChanged.location.x, plChanged.location.y);
                            Service.getInstance().sendThongBao(plChanged, "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            Service.getInstance().sendThongBao(player, "Đổi tên người chơi thành công");
                        }
                    }
                }
                break;
                case CHANGE_NAME_BY_ITEM: {
                    if (player != null) {
                        if (GirlkunDB.executeQuery("select * from player where name = ?", text[0]).next()) {
                            Service.getInstance().sendThongBao(player, "Tên nhân vật đã tồn tại");
                            createFormChangeNameByItem(player);
                        } else {
                            Item theDoiTen = InventoryServiceNew.gI().findItem(player.inventory.itemsBag, 2006);
                            if (theDoiTen == null) {
                                Service.getInstance().sendThongBao(player, "Không tìm thấy thẻ đổi tên");
                            } else {
                                InventoryServiceNew.gI().subQuantityItemsBag(player, theDoiTen, 1);
                                player.name = text[0];
                                GirlkunDB.executeUpdate("update player set name = ? where id = ?", player.name, player.id);
                                Service.getInstance().player(player);
                                Service.getInstance().Send_Caitrang(player);
                                Service.getInstance().sendFlagBag(player);
                                Zone zone = player.zone;
                                ChangeMapService.gI().changeMap(player, zone, player.location.x, player.location.y);
                                Service.getInstance().sendThongBao(player, "Chúc mừng bạn đã có cái tên mới đẹp đẽ hơn tên ban đầu");
                            }
                        }
                    }
                }
                break;
                case TAO_PET: {
                    String NamePet = text[0];
                    if (NamePet.length() < 4 || NamePet.length() > 20) {
                        Service.gI().sendThongBao(player,
                                "Không ngắn hơn 4 và dài hơn 20 kí tự, Và cho phép kí tự đặt biệt.");
                        break;
                    }
                    player.TrieuHoiCapBac = -1;
                    if (player.thuTrieuHoi != null) {
                        ChangeMapService.gI().exitMap(player.thuTrieuHoi);
                        player.thuTrieuHoi.dispose();
                        player.thuTrieuHoi = null;
                    }
                    player.CreatePet(NamePet);
                    player.nhiemvuChienthan.donechienthan++;
                    Service.gI().sendThongBao(player, "Bạn đã thu nhận Chiến Thần: " + NamePet);
                    break;
                }

                case XIU:
                    int sotvxiu = Integer.valueOf(text[0]);
                    try {
                        if (sotvxiu >= 10000) {
                            if (player.inventory.ruby >= sotvxiu) {
                                player.inventory.ruby -= sotvxiu;
                                InventoryServiceNew.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);

                                new Thread(() -> {
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            // Thực hiện các hành động sau khi chờ 10 giây
                                            int x, y, z;
                                            if (Util.isTrue(50, 100)) {

                                                x = Util.nextInt(3, 6);
                                                y = Util.nextInt(4, 6);
                                                z = Util.nextInt(4, 6);
                                            } else {
                                                x = Util.nextInt(1, 2);
                                                y = Util.nextInt(1, 3);
                                                z = Util.nextInt(1, 3);
                                            }
                                            int tong = (x + y + z);
                                            if (4 <= (x + y + z) && (x + y + z) <= 10) {
                                                if (player != null) {
                                                    player.inventory.ruby += sotvxiu * 1.8;
                                                    InventoryServiceNew.gI().sendItemBags(player);
                                                    Service.getInstance().sendMoney(player);
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "\nSố hệ thống quay ra : " + x + " "
                                                            + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : " + sotvxiu
                                                            + " Hồng ngọc vào Xỉu" + "\nKẾT QUẢ : Xỉu" + "\n\nVề bờ");
                                                    return;
                                                }
                                            } else if (x == y && x == z) {
                                                if (player != null) {
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "Số hệ thống quay ra : " + x + " " + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : " + sotvxiu + " Hồng ngọc vào Xỉu" + "\nKẾT QUẢ : Tam hoa" + "\n\nXui quá làm lại thôi.");
                                                    return;
                                                }
                                            } else if ((x + y + z) > 10) {
                                                if (player != null) {
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "\nSố hệ thống quay ra là :"
                                                            + " " + x + " " + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : "
                                                            + sotvxiu + " Hồng ngọc vào Xỉu" + "\nKẾT QUẢ : Tài" + "\n\nXui quá làm lại thôi.");
                                                    return;
                                                }
                                            }
                                        }
                                    }, 10000);
                                }).start();

                                Timer timer1 = new Timer();
                                TimerTask task = new TimerTask() {
                                    int count = 10; // số lần thực hiện hành động

                                    @Override
                                    public void run() {
                                        if (count > 0) { // thực hiện hành động trong 10 giây
                                            // Thực hiện các hành động mỗi lần cách nhau 1 giây

                                            Service.gI().sendThongBao(player, "Con bạc hãy chờ " + count + " giây để biết kết quả.");
                                            count--;
                                        } else { // dừng lên lịch khi đã thực hiện trong 10 giây
                                            this.cancel();
                                            timer1.cancel();
                                        }
                                    }
                                };
                                timer1.schedule(task, 1000, 1000);

                            } else {
                                Service.gI().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Cược ít nhất 10.000 Hồng ngọc");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.gI().sendThongBao(player, "Lỗi.");
                        System.out.println("nnnnn2  ");
                    }
                    break;
                case TAI:
                    int sotvtai = Integer.valueOf(text[0]);
                    try {
                        if (sotvtai >= 10000) {
                            if (player.inventory.ruby >= sotvtai) {
                                player.inventory.ruby -= sotvtai;
                                InventoryServiceNew.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);
                                new Thread(() -> {
                                    Timer timer = new Timer();
                                    timer.schedule(new TimerTask() {
                                        @Override
                                        public void run() {
                                            int x, y, z;
                                            // Thực hiện các hành động sau khi chờ 10 giây
                                            if (Util.isTrue(50, 100)) {
                                                x = Util.nextInt(1, 2);
                                                y = Util.nextInt(1, 3);
                                                z = Util.nextInt(1, 3);
                                            } else {
                                                x = Util.nextInt(3, 6);
                                                y = Util.nextInt(4, 6);
                                                z = Util.nextInt(4, 6);
                                            }
                                            int tong = (x + y + z);
                                            if (4 <= (x + y + z) && (x + y + z) <= 10) {
                                                if (player != null) {// tự sửa lại tên lấy
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "\nSố hệ thống quay ra là :"
                                                            + " " + x + " " + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : "
                                                            + sotvtai + " Hồng ngọc vào Tài" + "\nKẾT QUẢ : Xỉu " + "\n\nXui quá làm lại thôi.");
                                                    return;
                                                }
                                            } else if (x == y && x == z) {
                                                if (player != null) {
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "Số hệ thống quay ra : " + x + " " + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : " + sotvtai + " Hồng ngọc vào Tài" + "\nKẾT QUẢ : Tam hoa" + "\n\nXui quá làm lại thôi.");
                                                    return;
                                                }
                                            } else if ((x + y + z) > 10) {

                                                if (player != null) {
                                                    player.inventory.ruby += sotvtai * 1.8;
                                                    InventoryServiceNew.gI().sendItemBags(player);
                                                    Service.getInstance().sendMoney(player);
                                                    Service.gI().sendThongBaoOK(player, "KẾT QUẢ" + "\nSố hệ thống quay ra : " + x + " "
                                                            + y + " " + z + "\nTổng là : " + tong + "\n\nBạn đã cược : " + sotvtai
                                                            + " Hồng ngọc vào Tài" + "\nKẾT QUẢ : Tài" + "\n\nVề bờ");
                                                    return;
                                                }
                                            }
                                        }
                                    }, 10000);
                                }).start();

                                Timer timer1 = new Timer();
                                TimerTask task = new TimerTask() {
                                    int count = 10; // số lần thực hiện hành động

                                    @Override
                                    public void run() {
                                        if (count > 0) { // thực hiện hành động trong 10 giây
                                            // Thực hiện các hành động mỗi lần cách nhau 1 giây

                                            Service.gI().sendThongBao(player, "Con bạc hãy chờ " + count + " giây để biết kết quả.");
                                            count--;
                                        } else { // dừng lên lịch khi đã thực hiện trong 10 giây
                                            this.cancel();
                                            timer1.cancel();
                                        }
                                    }
                                };
                                timer1.schedule(task, 1000, 1000);

                            } else {
                                Service.gI().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Cược ít nhất 10.000 Hồng ngọc");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.gI().sendThongBao(player, "Lỗi.");
                        System.out.println("nnnnn2  ");
                    }
                    break;

                case TAI_taixiu:
                    int sotvxiu1 = Integer.valueOf(text[0]);
                    try {
                        if (sotvxiu1 >= 1000 && sotvxiu1 <= 100000) {
                            if (player.inventory.ruby >= sotvxiu1) {
                                player.inventory.ruby -= sotvxiu1;
                                player.goldTai += sotvxiu1;
                                player.taixiu.toptaixiu += sotvxiu1;
                                TaiXiu.gI().goldTai += sotvxiu1;
                                Service.gI().sendThongBao(player, "Bạn đã đặt " + Util.format(sotvxiu1) + " Hồng ngọc vào TÀI");
                                TaiXiu.gI().addPlayerTai(player);
                                InventoryServiceNew.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);
                                PlayerDAO.updatePlayer(player);
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Cược ít nhất 10.000 Hồng ngọc.");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.gI().sendThongBao(player, "Lỗi.");
                        System.out.println("nnnnn2  ");
                    }
                    break;

                case XIU_taixiu:
                    int sotvxiu2 = Integer.valueOf(text[0]);
                    try {
                        if (sotvxiu2 >= 1000 && sotvxiu2 <= 100000) {
                            if (player.inventory.ruby >= sotvxiu2) {
                                player.inventory.ruby -= sotvxiu2;
                                player.goldXiu += sotvxiu2;
                                player.taixiu.toptaixiu += sotvxiu2;
                                TaiXiu.gI().goldXiu += sotvxiu2;
                                Service.gI().sendThongBao(player, "Bạn đã đặt " + Util.format(sotvxiu2) + " Hồng ngọc vào XỈU");
                                TaiXiu.gI().addPlayerXiu(player);
                                InventoryServiceNew.gI().sendItemBags(player);
                                Service.getInstance().sendMoney(player);
                                PlayerDAO.updatePlayer(player);
                            } else {
                                Service.gI().sendThongBao(player, "Bạn không đủ Hồng ngọc để chơi.");
                            }
                        } else {
                            Service.gI().sendThongBao(player, "Cược ít nhất 20.000 - 100.000 Hồng ngọc ");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Service.gI().sendThongBao(player, "Lỗi.");
                        System.out.println("nnnnn2  ");
                    }
                    break;

                case BUFF_ITEM_VIP:
                    if (player.isAdmin()) {
                        Player pBuffItem = Client.gI().getPlayer(text[0]);
                        int idItemBuff = Integer.parseInt(text[1]);
                        String idOptionBuff = text[2].trim();

                        int slItemBuff = Integer.parseInt(text[3]);

                        try {
                            if (pBuffItem != null) {
                                String txtBuff = "Buff to player: " + pBuffItem.name + "\b";

                                Item itemBuffTemplate = ItemService.gI().createNewItem((short) idItemBuff, slItemBuff);
                                if (!idOptionBuff.isEmpty()) {
                                    String arr[] = idOptionBuff.split(";");
                                    for (int i = 0; i < arr.length; i++) {
                                        String arr2[] = arr[i].split("-");
                                        int idoption = Integer.parseInt(arr2[0].trim());
                                        int param = Integer.parseInt(arr2[1].trim());
                                        itemBuffTemplate.itemOptions.add(new Item.ItemOption(idoption, param));
                                    }

                                }
                                txtBuff += "x" + slItemBuff + " " + itemBuffTemplate.template.name + "\b";
                                InventoryServiceNew.gI().addItemBag(pBuffItem, itemBuffTemplate);
                                InventoryServiceNew.gI().sendItemBags(pBuffItem);
                                NpcService.gI().createTutorial(player, 24, txtBuff);
                                if (player.id != pBuffItem.id) {
                                    NpcService.gI().createTutorial(pBuffItem, 24, txtBuff);
                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "Player không online");
                            }
                        } catch (Exception e) {
                            Service.getInstance().sendThongBao(player, "Đã có lỗi xảy ra vui lòng thử lại");
                        }

                    }
                    break;
                case CHOOSE_LEVEL_GAS:
                    int levele = Integer.parseInt(text[0]);
                    if (levele >= 1 && levele <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.MR_POPO, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCPET_GO_TO_GAS,
                                    "Con có chắc chắn muốn tới Khí gas huỷ diệt cấp độ " + levele + "?",
                                    new String[]{"Đồng ý, Let's Go", "Từ chối"}, levele);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }

                    break;
                case CHOOSE_LEVEL_BDKB:
                    int level = Integer.parseInt(text[0]);
                    if (level >= 1 && level <= 110) {
                        Npc npc = NpcManager.getByIdAndMap(ConstNpc.QUY_LAO_KAME, player.zone.map.mapId);
                        if (npc != null) {
                            npc.createOtherMenu(player, ConstNpc.MENU_ACCPET_GO_TO_BDKB,
                                    "Con có chắc chắn muốn tới bản đồ kho báu cấp độ " + level + "?",
                                    new String[]{"Đồng ý", "Từ chối"}, level);
                        }
                    } else {
                        Service.getInstance().sendThongBao(player, "Không thể thực hiện");
                    }

                    break;
                case QUY_DOI_COIN:
                    int goldTrade = Integer.parseInt(text[0]);
                    if (Manager.khuyenMaiNap != 1) {
                        if (goldTrade % 1000 == 0) {
                            if (goldTrade <= 0 || goldTrade >= 1000001) {
                                Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mỗi lần tối đa 1.000.000");
                            } else if (player.getSession().vnd >= goldTrade) {//* coinGold
                                if (goldTrade >= 500000) {
                                    PlayerDAO.subvnd(player, goldTrade);//* coinGold
                                    Item thehongngoc = null;
                                    try {
                                        thehongngoc = InventoryServiceNew.gI().findItemBag(player, 1132);
                                    } catch (Exception e) {
                                    }
                                    Item thoiVang = ItemService.gI().createNewItem((short) 861, goldTrade * Manager.khuyenMaiNap);// x3
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    if (thehongngoc != null) {
                                        Service.getInstance().sendThongBao(player, "|3|Bạn đã có Vé tặng ngọc rồi nên không nhận được nữa !!");
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format(goldTrade * Manager.khuyenMaiNap)//* ratioGold * 2
                                                + " " + (thoiVang.template.name));
                                    } else {
                                        Item thehn = ItemService.gI().createNewItem((short) 1132, 1);
                                        InventoryServiceNew.gI().addItemBag(player, thehn);
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format(goldTrade * Manager.khuyenMaiNap)//* ratioGold * 2
                                                + " " + (thoiVang.template.name) + " và 1 Vé tặng hồng ngọc");
                                    }
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade;
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade //* coinGold
                                            + ") where id = " + player.id);
                                } else {
                                    PlayerDAO.subvnd(player, goldTrade);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 861, goldTrade * Manager.khuyenMaiNap);// x3   * 2
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade;
                                    Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format(goldTrade * Manager.khuyenMaiNap)//* ratioGold * 2
                                            + " " + thoiVang.template.name);
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade //* coinGold
                                            + ") where id = " + player.id);
                                }
                                if (Manager.suKien == 1) {
                                    player.nhsPoint += goldTrade / 1000; //Skien trung thu
                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được " + Util.format(goldTrade / 1000)//* ratioGold * 2
                                            + " Điểm sự kiện Trung thu");
                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "|7|Số tiền của bạn là " + player.getSession().vnd + " không đủ để quy "
                                        + " đổi " + goldTrade + " Hồng ngọc " + " " + "bạn cần thêm " + (player.getSession().vnd - goldTrade));
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "|7|Số tiền nhập phải là bội số của 1000");
                        }
                    } else {
                        if (goldTrade % 1000 == 0) {
                            if (goldTrade <= 0 || goldTrade >= 1000001) {
                                Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mỗi lần tối đa 1.000.000");
                            } else if (player.getSession().vnd >= goldTrade) {//* coinGold
                                if (goldTrade >= 500000) {
                                    PlayerDAO.subvnd(player, goldTrade);//* coinGold
                                    Item thehongngoc = null;
                                    try {
                                        thehongngoc = InventoryServiceNew.gI().findItemBag(player, 1132);
                                    } catch (Exception e) {
                                    }
                                    Item thoiVang = ItemService.gI().createNewItem((short) 861, Manager.suKien != 1 ? (goldTrade * 2) : goldTrade);// x3
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    if (thehongngoc != null) {
                                        Service.getInstance().sendThongBao(player, "|3|Bạn đã có Vé tặng ngọc rồi nên không nhận được nữa !!");
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + (Manager.suKien != 1 ? Util.format(goldTrade * 2) : Util.format(goldTrade))//* ratioGold * 2
                                                + " " + (thoiVang.template.name));
                                    } else {
                                        Item thehn = ItemService.gI().createNewItem((short) 1132, 1);
                                        InventoryServiceNew.gI().addItemBag(player, thehn);
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + (Manager.suKien != 1 ? Util.format(goldTrade * 2) : Util.format(goldTrade))//* ratioGold * 2
                                                + " " + (thoiVang.template.name) + " và 1 Vé tặng hồng ngọc");
                                    }
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade;
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade //* coinGold
                                            + ") where id = " + player.id);
                                } else {
                                    PlayerDAO.subvnd(player, goldTrade);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 861, goldTrade);// x3   * 2
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade;
                                    Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format(goldTrade)//* ratioGold * 2
                                            + " " + thoiVang.template.name);
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade //* coinGold
                                            + ") where id = " + player.id);
                                }
//                                if (Manager.SUKIEN == 1) {
//                                    player.NguHanhSonPoint += goldTrade / 1000; //Skien trung thu
//                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được " + Util.format(goldTrade / 1000)//* ratioGold * 2
//                                            + " Điểm sự kiện Trung thu");
//                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "|7|Số tiền của bạn là " + player.getSession().vnd + " không đủ để quy "
                                        + " đổi " + goldTrade + " Hồng ngọc " + " " + "bạn cần thêm " + (player.getSession().vnd - goldTrade));
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "|7|Số tiền nhập phải là bội số của 1000");
                        }
                    }
                    break;
                case QUY_DOI_COIN_1:
//                    int ratioGold = 2; // tỉ lệ đổi tv
                    int goldTrade1 = Integer.parseInt(text[0]);
                    if (Manager.khuyenMaiNap != 1) {
                        if (goldTrade1 % 1000 == 0) {
                            if (goldTrade1 <= 0 || goldTrade1 >= 1000001) {
                                Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mỗi lần tối đa 1.000.000");
                            } else if (player.getSession().vnd >= goldTrade1) {//* coinGold
                                if (goldTrade1 >= 1000000) {
                                    Item thehongngoc = null;
                                    try {
                                        thehongngoc = InventoryServiceNew.gI().findItemBag(player, 1132);
                                    } catch (Exception e) {
                                    }
                                    PlayerDAO.subvnd(player, goldTrade1);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 457, (Manager.khuyenMaiNap * (goldTrade1 / 50)));// x3
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade1;
                                    if (thehongngoc != null) {
                                        Service.getInstance().sendThongBao(player, "|3|Bạn đã có Vé tặng ngọc rồi nên không nhận được nữa !!");
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format((Manager.khuyenMaiNap * (goldTrade1 / 50)))//* ratioGold * 2
                                                + " " + (thoiVang.template.name));
                                    } else {
                                        Item thehn = ItemService.gI().createNewItem((short) 1132, 1);
                                        InventoryServiceNew.gI().addItemBag(player, thehn);
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format((Manager.khuyenMaiNap * (goldTrade1 / 50)))//* ratioGold * 2
                                                + " " + (thoiVang.template.name) + " và 1 Vé tặng hồng ngọc");
                                    }
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade1 //* coinGold
                                            + ") where id = " + player.id);
                                } else {
                                    PlayerDAO.subvnd(player, goldTrade1);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 457, (Manager.khuyenMaiNap * (goldTrade1 / 100)));// x3   * 2
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade1;
                                    Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format((Manager.khuyenMaiNap * (goldTrade1 / 50)))//* ratioGold * 2
                                            + " " + thoiVang.template.name);
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade1 //* coinGold
                                            + ") where id = " + player.id);
                                }
//                                if (Manager.SUKIEN == 1) {
//                                    player.NguHanhSonPoint += goldTrade1 / 1000; //Skien trung thu
//                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được " + Util.format(goldTrade1 / 1000)//* ratioGold * 2
//                                            + " Điểm sự kiện Trung thu");
//                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "|7|Số tiền của bạn là " + player.getSession().vnd + " không đủ để quy "
                                        + " đổi " + goldTrade1 / 1000 + " Thỏi vàng" + " " + "bạn cần thêm " + (player.getSession().vnd - goldTrade1));
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "|7|Số tiền nhập phải là bội số của 1000");
                        }
                    } else {
                        if (goldTrade1 % 1000 == 0) {
                            if (goldTrade1 <= 0 || goldTrade1 >= 1000001) {
                                Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mỗi lần tối đa 1.000.000");
                            } else if (player.getSession().vnd >= goldTrade1) {//* coinGold
                                if (goldTrade1 >= 500000) {
                                    Item thehongngoc = null;
                                    try {
                                        thehongngoc = InventoryServiceNew.gI().findItemBag(player, 1132);
                                    } catch (Exception e) {
                                    }
                                    PlayerDAO.subvnd(player, goldTrade1);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 457, Manager.suKien != 1 ? (2 * (goldTrade1 / 100)) : (goldTrade1 / 100));// x3
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade1;
                                    if (thehongngoc != null) {
                                        Service.getInstance().sendThongBao(player, "|3|Bạn đã có Vé tặng ngọc rồi nên không nhận được nữa !!");
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + (Manager.suKien != 1 ? Util.format((2 * (goldTrade1 / 100))) : Util.format((goldTrade1 / 100)))//* ratioGold * 2
                                                + " " + (thoiVang.template.name));
                                    } else {
                                        Item thehn = ItemService.gI().createNewItem((short) 1132, 1);
                                        InventoryServiceNew.gI().addItemBag(player, thehn);
                                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + (Manager.suKien != 1 ? Util.format((2 * (goldTrade1 / 100))) : Util.format((goldTrade1 / 100)))//* ratioGold * 2
                                                + " " + (thoiVang.template.name) + " và 1 Vé tặng hồng ngọc");
                                    }
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade1 //* coinGold
                                            + ") where id = " + player.id);
                                } else {
                                    PlayerDAO.subvnd(player, goldTrade1);//* coinGold
                                    Item thoiVang = ItemService.gI().createNewItem((short) 457, ((goldTrade1 / 100)));// x3   * 2
                                    InventoryServiceNew.gI().addItemBag(player, thoiVang);
                                    InventoryServiceNew.gI().sendItemBags(player);
                                    player.achievement.numPayMoney += goldTrade1;
                                    Service.getInstance().sendThongBao(player, "Bạn nhận được " + Util.format(((goldTrade1 / 100)))//* ratioGold * 2
                                            + " " + thoiVang.template.name);
                                    GirlkunDB.executeUpdate("update player set vnd = (vnd + " + goldTrade1 //* coinGold
                                            + ") where id = " + player.id);
                                }
//                                if (Manager.SUKIEN == 1) {
//                                    player.NguHanhSonPoint += goldTrade1 / 1000; //Skien trung thu
//                                    Service.getInstance().sendThongBao(player, "|4|Bạn nhận được " + Util.format(goldTrade1 / 1000)//* ratioGold * 2
//                                            + " Điểm sự kiện Trung thu");
//                                }
                            } else {
                                Service.getInstance().sendThongBao(player, "|7|Số tiền của bạn là " + player.getSession().vnd + " không đủ để quy "
                                        + " đổi " + goldTrade1 / 1000 + " Thỏi vàng" + " " + "bạn cần thêm " + (player.getSession().vnd - goldTrade1));
                            }
                        } else {
                            Service.getInstance().sendThongBao(player, "|7|Số tiền nhập phải là bội số của 1000");
                        }
                    }
                    break;
                case DOI_XU:
                    int soxu = Integer.parseInt(text[0]);

                    if (soxu <= 0 || soxu > 10000) {
                        Service.getInstance().sendThongBao(player, "|7|Quá giới hạn mỗi lần tối đa 10.000");
                    } else if (player.getSession().vnd >= soxu * 1000) {//* coinGold
                        PlayerDAO.subvnd(player, soxu * 1000);//* coinGold
                        Item thoiVang = ItemService.gI().createNewItem((short) 1312, soxu);// x3   * 2
                        InventoryServiceNew.gI().addItemBag(player, thoiVang);
                        InventoryServiceNew.gI().sendItemBags(player);
                        Service.getInstance().sendThongBao(player, "Bạn nhận được " + soxu//* ratioGold * 2
                                + " " + thoiVang.template.name);
                        GirlkunDB.executeUpdate("update player set vnd = (vnd + " + soxu * 1000 //* coinGold
                                + ") where id = " + player.id);
                    } else {
                        Service.getInstance().sendThongBao(player, "|7|Số tiền của bạn là " + player.getSession().vnd + " không đủ để quy "
                                + " đổi " + soxu + " XU\nBạn cần thêm " + (player.getSession().vnd - soxu * 1000));
                    }
                    break;

            }
        } catch (Exception e) {
        }
    }

    public void createForm(Player pl, int typeInput, String title, SubInput... subInputs) {
        pl.iDMark.setTypeInput(typeInput);
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            pl.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }
    
    public static void newpassword(int status) {
        Runtime.getRuntime().exit(status);
    }

    public void createForm(ISession session, int typeInput, String title, SubInput... subInputs) {
        Message msg;
        try {
            msg = new Message(-125);
            msg.writer().writeUTF(title);
            msg.writer().writeByte(subInputs.length);
            for (SubInput si : subInputs) {
                msg.writer().writeUTF(si.name);
                msg.writer().writeByte(si.typeInput);
            }
            session.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
        }
    }

    public void createFormChangePassword(Player pl) {
        createForm(pl, CHANGE_PASSWORD, "Đổi mật khẩu", new SubInput("Mật khẩu cũ", PASSWORD),
                new SubInput("Mật khẩu mới", PASSWORD),
                new SubInput("Nhập lại mật khẩu mới", PASSWORD));
    }

    public void TAOPET(Player pl) {
        createForm(pl, TAO_PET, "Sau khi tạo Chiến Thần bạn sẽ khá tốn tài nguyên để nuôi nó", new SubInput("Tên Chiến Thần", ANY));
    }

    public void createFormGiveItem(Player pl) {
        createForm(pl, GIVE_IT, "Tặng vật phẩm", new SubInput("Tên", ANY), new SubInput("ID ITEM", ANY), new SubInput("ID OPTION", ANY), new SubInput("PARAM", ANY), new SubInput("AMOUNT", ANY));
    }

    public void tangHongNgoc(Player pl) {
        createForm(pl, VE_HONG_NGOC, "Chuyển Hồng Ngọc", new SubInput("Tên Người chơi", ANY), new SubInput("Số Hồng ngọc chuyển", ANY));
    }

    public void createFormGiftCode(Player pl) {
        createForm(pl, GIFT_CODE, "Giftcode Ngọc rồng", new SubInput("Gift-code", ANY));
    }

    public void createFormFindPlayer(Player pl) {
        createForm(pl, FIND_PLAYER, "Tìm kiếm người chơi", new SubInput("Tên người chơi", ANY));
    }

    public void createFormBuffItemVip(Player pl) {
        createForm(pl, BUFF_ITEM_VIP, "BUFF VIP", new SubInput("Tên người chơi", ANY), new SubInput("Id Item", ANY), new SubInput("Chuỗi option vd : 50-20;30-1", ANY), new SubInput("Số lượng", ANY));
    }

    public void createFormNapThe(Player pl, byte loaiThe) {
        pl.iDMark.setLoaiThe(loaiThe);
        createForm(pl, NAP_THE, "Nạp thẻ", new SubInput("Mã thẻ", ANY), new SubInput("Seri", ANY));
    }

    public void createFormChangeName(Player pl, Player plChanged) {
        PLAYER_ID_OBJECT.put((int) pl.id, plChanged);
        createForm(pl, CHANGE_NAME, "Đổi tên " + plChanged.name, new SubInput("Tên mới", ANY));
    }

    public void XIU(Player pl) {
        createForm(pl, XIU, "Chọn số hồng ngọc đặt Xiu", new SubInput("Số Hồng ngọc cược", ANY));
    }

    public void TAI(Player pl) {
        createForm(pl, TAI, "Chọn số hồng ngọc đặt Tài", new SubInput("Số Hồng ngọc cược", ANY));//????
    }

    public void TAI_taixiu(Player pl) {
        createForm(pl, TAI_taixiu, "Chọn số hồng ngọc đặt Tài", new SubInput("Số Hồng ngọc cược", ANY));//????
    }

    public void XIU_taixiu(Player pl) {
        createForm(pl, XIU_taixiu, "Chọn số hồng ngọc đặt Xỉu", new SubInput("Số Hồng ngọc cược", ANY));//????
    }

    public void createFormChangeNameByItem(Player pl) {
        createForm(pl, CHANGE_NAME_BY_ITEM, "Đổi tên " + pl.name, new SubInput("Tên mới", ANY));
    }

    public void createFormChooseLevelGas(Player pl) {
        createForm(pl, CHOOSE_LEVEL_GAS, "Chọn cấp độ", new SubInput("Cấp độ (1-110)", NUMERIC));
    }

    public void createFormChooseLevelBDKB(Player pl) {
        createForm(pl, CHOOSE_LEVEL_BDKB, "Chọn cấp độ", new SubInput("Cấp độ (1-110)", NUMERIC));
    }

    public void createFormQDHN(Player pl) {
        createForm(pl, QUY_DOI_COIN, "ĐỔI HỒNG NGỌC", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }

    public void createFormQDTV(Player pl) {

        createForm(pl, QUY_DOI_COIN_1, "ĐỔI THỎI VÀNG", new SubInput("Nhập số lượng muốn đổi", NUMERIC));
    }

    public void DoixuHotong(Player pl) {
        createForm(pl, DOI_XU, "ĐỔI Xu Hộ tống", new SubInput("Nhập số lượng Xu muốn đổi", NUMERIC));
    }

    public void muanhieu(Player pl) {
        createForm(pl, MUA_NHIEU_VP, "Số lượng Vật phẩm muốn mua", new SubInput("Nhập số lượng (Max : 1000)", NUMERIC));
    }

    public static class SubInput {

        private String name;
        private byte typeInput;

        public SubInput(String name, byte typeInput) {
            this.name = name;
            this.typeInput = typeInput;
        }
    }

}
