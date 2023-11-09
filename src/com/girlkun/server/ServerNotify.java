package com.girlkun.server;

import com.girlkun.models.player.Giftcode;
import com.girlkun.models.player.Player;
import com.girlkun.network.io.Message;
import com.girlkun.services.Service;
import com.girlkun.utils.Logger;
import com.girlkun.utils.Util;

import java.util.ArrayList;
import java.util.List;


public class ServerNotify extends Thread {

    private long lastTimeGK;

    private final List<String> notifies;

    private static ServerNotify i;

    private ServerNotify() {
        this.notifies = new ArrayList<>();
        this.start();
    }

    public static ServerNotify gI() {
        if (i == null) {
            i = new ServerNotify();
        }
        return i;
    }

    @Override
    public void run() {
        while (!Maintenance.isRuning) {
            try {
                long st = System.currentTimeMillis();
                synchronized (this) {
                    wait(Math.max(1000 - (System.currentTimeMillis() - st), 0));
                    while (!notifies.isEmpty()) {
                        sendThongBaoBenDuoi(notifies.remove(0));
                    }
                    if (Util.canDoWithTime(this.lastTimeGK, 60000)) {
                        sendThongBaoBenDuoi("Cảm ơn bạn đã chơi game nha");
                        this.lastTimeGK = System.currentTimeMillis();
                    }
                }
            } catch (Exception e) {
                Logger.logException(ServerNotify.class, e);
            }
        }
    }

    private void sendThongBaoBenDuoi(String text) {
        Message msg;
        try {
            msg = new Message(93);
            msg.writer().writeUTF(text);
            Service.getInstance().sendMessAllPlayer(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(ServerNotify.class, e, "Error sendThongBaoBenDuoi: " + text);
        }
    }

    public void notify(String text) {
        this.notifies.add(text);
    }

    public void sendNotifyTab(Player player) {
        Message msg;
        try {
            msg = new Message(50);
            msg.writer().writeByte(10);

            msg.writer().writeShort(0);
            msg.writer().writeUTF("Thông tin về game");
            msg.writer().writeUTF("""
                - Hồng ngọc săn Boss rơi đá đổi capsule hồng
                - Vàng làm nhiệm vụ hằng ngày
                - Set kích hoạt up map sau làng
                - Nạp thẻ, đăng ký vui lòng lên Web\
            """);

            msg.writer().writeShort(1);
            msg.writer().writeUTF("Lệnh hổ trợ người chơi");
            msg.writer().writeUTF("""
            \tCÁC LỆNH CHO MEMBER
            - muanhieu: Bật/Tắt mua nhiều
            - adau: Bật/Hủy Auto buff đậu khi HP, KI đệ dưới 30%
            - chiso: Bật/Hủy cộng chỉ số nhanh
            - hp: Xem HP Boss và Dame thực lên Boss
            - tt: Hiện thông tin cơ bản khi quá chỉ số hiển thị
            - th: Lệnh triệu hồi khi có Chiến Thần
            - quai: Xem HP Quái đang đánh
            - stop: Dừng Tất cả lệnh Auto
            """);

            msg.writer().writeShort(2);
            msg.writer().writeUTF("GIFTCODE");
            msg.writer().writeUTF(Giftcode.gI().checkInfomationGiftCode());

            if (player.TrieuHoiCapBac != -1) {
                String ttpet = "Name: " + player.TenThuTrieuHoi;
                ttpet += "\nLevel: " + player.TrieuHoiLevel + " (" + (player.TrieuHoiExpThanThu * 100 / (3000000L + player.TrieuHoiLevel * 1500000L)) + "%)";
                ttpet += "\nKinh nghiệm: " + Util.format(player.TrieuHoiExpThanThu);
                ttpet += "\nCấp bậc: " + player.getNameThanThu(player.TrieuHoiCapBac);
                ttpet += "\nThức ăn: " + player.TrieuHoiThucAn + "%";
                ttpet += "\nSức Đánh: " + Util.getFormatNumber(player.TrieuHoiDame);
                ttpet += "\nMáu: " + Util.getFormatNumber(player.TrieuHoiDame);
                ttpet += "\nKĩ năng: " + player.getTrieuHoiKiNang(player.TrieuHoiCapBac);
                msg.writer().writeShort(3);
                msg.writer().writeUTF("Thông tin CHIẾN THẦN");
                msg.writer().writeUTF(ttpet);
            }
            player.sendMessage(msg);
            msg.cleanup();
        } catch (Exception e) {
            Logger.logException(ServerNotify.class, e, "Error sendNotifyTab player: " + player.name);
        }
    }
}
