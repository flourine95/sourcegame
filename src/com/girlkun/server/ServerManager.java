package com.girlkun.server;

import com.girlkun.database.GirlkunDB;
import com.girlkun.models.boss.BossManager;
import com.girlkun.models.item.Item;
import com.girlkun.models.kygui.ShopKyGuiManager;
import com.girlkun.models.map.challenge.MartialCongressManager;
import com.girlkun.models.matches.pvp.DaiHoiVoThuat;
import com.girlkun.models.player.Player;
import com.girlkun.network.example.MessageSendCollect;
import com.girlkun.network.server.GirlkunServer;
import com.girlkun.network.server.ISessionAcceptHandler;
import com.girlkun.network.session.ISession;
import com.girlkun.server.io.MyKeyHandler;
import com.girlkun.server.io.MySession;
import com.girlkun.services.ClanService;
import com.girlkun.services.InventoryServiceNew;
import com.girlkun.services.NgocRongNamecService;
import com.girlkun.services.Service;
import com.girlkun.services.func.TaiXiu;
import com.girlkun.services.func.TopService;
import com.girlkun.utils.Logger;
import com.girlkun.utils.TimeUtil;
import com.girlkun.utils.Util;

import javax.swing.*;
import java.net.ServerSocket;
import java.util.*;
import java.util.logging.Level;

public class ServerManager {

    public static String timeStart;

    public static final Map<String,Integer> CLIENTS = new HashMap<>();

    public static String NAME = "Girlkun75";
    public static int PORT = 14445;

    private static ServerManager instance;

    public static ServerSocket listenSocket;
    public static boolean isRunning;
    public static long delaylogin;

    public void init() {
        Manager.gI();
        try {
            if (Manager.local) {
                return;
            }
            GirlkunDB.executeUpdate("update account set last_time_login = '2000-01-01', "
                    + "last_time_logout = '2001-01-01'");
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }
    }

    public static ServerManager gI() {
        if (instance == null) {
            instance = new ServerManager();
            instance.init();
        }
        return instance;
    }

    public static void main(String[] args) {
        timeStart = TimeUtil.getTimeNow("dd/MM/yyyy HH:mm:ss");
        ServerManager.gI().run();
    }

    public void run() {
        long delay = 500;
        delaylogin = System.currentTimeMillis();
        isRunning = true;
        JFrame frame = new JFrame("Ngọc rồng Tabi");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("C:\\Users\\vt220\\Desktop\\CBRO Potara\\data\\girlkun\\icon\\icon.png");
        frame.setIconImage(icon.getImage());
        JPanel panel = new panel();
        frame.add(panel);
        frame.pack();
        frame.setVisible(true);
        activeCommandLine();
        activeGame();
        activeServerSocket();
        new Thread(DaiHoiVoThuat.gI(), "Thread DHVT").start();
        TaiXiu.gI().lastTimeEnd = System.currentTimeMillis() + 50000;
        new Thread(TaiXiu.gI(), "Thread TaiXiu").start();
        NgocRongNamecService.gI().initNgocRongNamec((byte) 0);
        new Thread(NgocRongNamecService.gI(), "Thread NRNM").start();
        new Thread(TopService.gI(), "Thread TOP").start();
        new Thread(() -> {
            while (isRunning) {
                try {
                    long start = System.currentTimeMillis();
                    MartialCongressManager.gI().update();
                    ShopKyGuiManager.gI().save();
                    long timeUpdate = System.currentTimeMillis() - start;
                    if (timeUpdate < delay) {
                        Thread.sleep(delay - timeUpdate);
                    }
                } catch (Exception e) {
                    System.out.println("qwert");
                }
            }
        }, "Update dai hoi vo thuat").start();
        try {
            Thread.sleep(1000);
            BossManager.gI().loadBoss();
            Manager.MAPS.forEach(com.girlkun.models.map.Map::initBoss);
        } catch (InterruptedException ex) {
            java.util.logging.Logger.getLogger(BossManager.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    private void act() throws Exception {
        GirlkunServer.gI().init().setAcceptHandler(new ISessionAcceptHandler() {
                    @Override
                    public void sessionInit(ISession is) {
                        if (!canConnectWithIp(is.getIP())) {
                            is.disconnect();
                            return;
                        }

                        is = is.setMessageHandler(Controller.getInstance())
                                .setSendCollect(new MessageSendCollect())
                                .setKeyHandler(new MyKeyHandler())
                                .startCollect();
                    }

                    @Override
                    public void sessionDisconnect(ISession session) {
                        Client.gI().kickSession((MySession) session);
                    }
                }).setTypeSessioClone(MySession.class)
                .setDoSomeThingWhenClose(() -> {
                    System.out.println("server close");
                    System.exit(0);
                })
                .start(PORT);

    }

    private void activeServerSocket() {
        Logger.log(Logger.PURPLE, "Start server......... Current thread: " + Thread.activeCount() + "\n");
        try {
            this.act();
        } catch (Exception e) {
            Logger.logException(ServerManager.class, e);
        }
    }

    private boolean canConnectWithIp(String ipAddress) {
        Object o = CLIENTS.get(ipAddress);
        if (o == null) {
            CLIENTS.put(ipAddress, 1);
            return true;
        } else {
            int n = Integer.parseInt(String.valueOf(o));
            if (n < Manager.maxPerIp) {
                n++;
                CLIENTS.put(ipAddress, n);
                return true;
            } else {
                return false;
            }
        }
    }

    public void disconnect(MySession session) {
        Object o = CLIENTS.get(session.getIP());
        if (o != null) {
            int n = Integer.parseInt(String.valueOf(o));
            n--;
            if (n < 0) {
                n = 0;
            }
            CLIENTS.put(session.getIP(), n);
        }
    }

    private void activeCommandLine() {
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine();
                if (line.equals("baotri")) {
                    Maintenance.gI().start(60 * 2);
                } else if (line.equals("athread")) {
                    ServerNotify.gI().notify("Tabi debug server: " + Thread.activeCount());
                } else if (line.equals("nplayer")) {
                    Logger.error("Player in game: " + Client.gI().getPlayers().size() + "\n");
                } else if (line.equals("admin")) {
                    new Thread(() -> {
                        Client.gI().close();
                    }).start();
                } else if (line.startsWith("bang")) {
                    new Thread(() -> {
                        try {
                            ClanService.gI().close();
                            Logger.error("Save " + Manager.CLANS.size() + " bang");
                        } catch (Exception e) {
                            Logger.error("Lỗi save clan!...................................\n");
                        }
                    }).start();
                } else if (line.startsWith("a")) {
                    String a = line.replace("a ", "");
                    Service.getInstance().sendThongBaoAllPlayer(a);
                } else if (line.startsWith("qua")) {
//                    qua=1-1-1-1=1-1-1-1=
//                     qua=playerId - soluong - itemId - so_saophale = optioneId - param=
                    try {
                        List<Item.ItemOption> ios = new ArrayList<>();
                        String[] pagram1 = line.split("=")[1].split("-");
                        String[] pagram2 = line.split("=")[2].split("-");
                        if (pagram1.length == 4 && pagram2.length % 2 == 0) {
                            Player p = Client.gI().getPlayer(Integer.parseInt(pagram1[0]));
                            if (p != null) {
                                for (int i = 0; i < pagram2.length; i += 2) {
                                    ios.add(new Item.ItemOption(Integer.parseInt(pagram2[i]), Integer.parseInt(pagram2[i + 1])));
                                }
                                Item i = Util.sendDo(Integer.parseInt(pagram1[2]), Integer.parseInt(pagram1[3]), ios);
                                i.quantity = Integer.parseInt(pagram1[1]);
                                InventoryServiceNew.gI().addItemBag(p, i);
                                InventoryServiceNew.gI().sendItemBags(p);
                                Service.getInstance().sendThongBao(p, "Admin trả đồ. anh em thông cảm nhé...");
                            } else {
                                System.out.println("Người chơi không online");
                            }
                        }
                    } catch (Exception e) {
                        System.out.println("Lỗi quà");
                    }
                } else if (line.equals("clean")) {
                    System.gc();
                    System.err.println("Clean.........");
                }
            }
        }, "Active line").start();
    }

    private void activeGame() {
    }

    public void close(long delay) {
        GirlkunServer.gI().stopConnect();

        isRunning = false;
        try {
            ClanService.gI().close();
        } catch (Exception e) {
            Logger.error("Lỗi save clan!...................................\n");
        }
        ShopKyGuiManager.gI().save();
        Client.gI().close();
        Logger.success("SUCCESSFULLY MAINTENANCE!...................................\n");
        System.exit(0);
    }
}
