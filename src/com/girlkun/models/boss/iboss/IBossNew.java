package com.girlkun.models.boss.iboss;

import com.girlkun.models.boss.BossStatus;
import com.girlkun.models.player.Player;


public interface IBossNew {

    void update();

    void initBase();

    void changeStatus(BossStatus status);

    Player getPlayerAttack();

    void changeToTypePK();

    void changeToTypeNonPK();
    
    void moveToPlayer(Player player);
    
    void moveTo(int x, int y);
    
    void checkPlayerDie(Player player);
    
    void wakeupAnotherBossWhenAppear();
    
    void wakeupAnotherBossWhenDisappear();
    
    void reward(Player plKill);
    
    void attack();

    //loop
    void rest();

    void respawn();

    void joinMap();

    boolean chatS();
    
    void doneChatS();

    void active();
    
    void chatM();

    void die(Player plKill);

    boolean chatE();
    
    void doneChatE();

    void leaveMap();
    //end loop
}

/**
 * Vui lòng không sao chép mã nguồn này dưới mọi hình thức
 */
