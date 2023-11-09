package com.girlkun.models.boss.dhvt;

import com.girlkun.models.boss.BossID;
import com.girlkun.models.boss.BossesData;
import com.girlkun.models.player.Player;

/**
 * @author BTH sieu cap vippr0 
 */
public class PonPut extends BossDHVT {

    public PonPut(Player player) throws Exception {
        super(BossID.PON_PUT, BossesData.PON_PUT);
        this.playerAtt = player;
    }
}