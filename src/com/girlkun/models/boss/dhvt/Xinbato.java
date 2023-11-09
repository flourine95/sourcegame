package com.girlkun.models.boss.dhvt;

import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.BossesData;
import com.girlkun.models.player.Player;

/**
 * @author BTH sieu cap vippr0 
 */
public class Xinbato extends BossDHVT {

    public Xinbato(Player player) throws Exception {
        super(BossID.XINBATO, BossesData.XINBATO);
        this.playerAtt = player;
    }
}