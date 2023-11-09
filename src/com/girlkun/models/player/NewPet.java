package com.girlkun.models.player;

import com.girlkun.services.PlayerService;
import com.girlkun.services.Service;
import com.girlkun.services.func.ChangeMapService;
import com.girlkun.utils.Util;


public class NewPet extends Player {

    public Player master;
    public short body;
    public short leg;
    public static int idb = -400000;
    public static final int DISTANCE = 50;

    public NewPet(Player master, short h, short b, short l) {
        this.master = master;
        this.isNewPet = true;
        this.id = idb;
        idb--;
        this.head = h;
        this.body = b;
        this.leg = l;
    }

    @Override
    public short getHead() {
        return head;
    }

    @Override
    public short getBody() {
        return body;
    }

    @Override
    public short getLeg() {
        return leg;
    }

    public void joinMapMaster() {
        if (master == null || this.zone == null) {
            return;
        }
        this.location.x = master.location.x + Util.nextInt(-10, 10);
        this.location.y = master.location.y;
        ChangeMapService.gI().goToMap(this, master.zone);
        this.zone.loadMeToAnother(this);
    }

    public long LasttimeHspet;
    private long lastTimeMoveIdle;
    private int timeMoveIdle;
    public boolean idle;

    private void moveIdle() {
        if (idle && Util.canDoWithTime(lastTimeMoveIdle, timeMoveIdle)) {
            int dir = this.location.x - master.location.x <= 0 ? -1 : 1;
            PlayerService.gI().playerMove(this, master.location.x
                    + Util.nextInt(dir == -1 ? 30 : -50, dir == -1 ? 50 : 30), master.location.y);
            lastTimeMoveIdle = System.currentTimeMillis();
            timeMoveIdle = Util.nextInt(5000, 8000);
        }
    }

    @Override
    public void update() {
        super.update();
        if (this.isDie()) {
            if (System.currentTimeMillis() - LasttimeHspet > 10000) {
                Service.gI().hsChar(this, nPoint.hpMax, nPoint.mpMax);
            } else {
                return;
            }
        }
        if (master != null && (this.zone == null || this.zone != master.zone)) {
            joinMapMaster();
        }
        if (master != null && master.isDie()) {
            return;
        }
        moveIdle();
    }


    public void followMaster() {
        int mX = master.location.x;
        int mY = master.location.y;
        int disX = this.location.x - mX;
        if (Math.sqrt(Math.pow(mX - this.location.x, 2) + Math.pow(mY - this.location.y, 2)) >= DISTANCE) {
            if (disX < 0) {
                this.location.x = mX - Util.nextInt(0, DISTANCE);
            } else {
                this.location.x = mX + Util.nextInt(0, DISTANCE);
            }
            this.location.y = mY;
            PlayerService.gI().playerMove(this, this.location.x, this.location.y);
        }
    }

    @Override
    public void dispose() {
        this.master = null;
        super.dispose();
    }
}
