package com.girlkun.models.player;

import com.girlkun.models.item.Item;


public class SetClothes {

    private Player player;

    public SetClothes(Player player) {
        this.player = player;
    }

    public byte songoku;
    public byte thienXinHang;
    public byte kirin;

    public byte ocTieu;
    public byte pikkoroDaimao;
    public byte picolo;

    public byte kakarot;
    public byte cadic;
    public byte nappa;

    public byte tabi;
    public byte setDHD;
    public byte setDTS;
    public byte setDTL;
    public byte tinhan;
    public byte nguyetan;
    public byte nhatan;

    public boolean godClothes;
    public int ctHaiTac = -1;

    public void setup() {
//        if (!player.isPet){
//        setupGOGETA();
//        }
        setDefault();
        setupSKT();
        setupAN();
        setupDTS();
        setupDHD();
        setupDTL();
        this.godClothes = true;
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                if (item.template.id > 567 || item.template.id < 555) {
                    this.godClothes = false;
                    break;
                }
            } else {
                this.godClothes = false;
                break;
            }
        }
        Item ct = this.player.inventory.itemsBody.get(5);
        if (ct.isNotNullItem()) {
            switch (ct.template.id) {
                case 618:
                case 619:
                case 620:
                case 621:
                case 622:
                case 623:
                case 624:
                case 626:
                case 627:
                    this.ctHaiTac = ct.template.id;
                    break;

            }
        }
    }

    private void setupSKT() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 129:
                        case 141:
                            isActSet = true;
                            songoku++;
                            break;
                        case 127:
                        case 139:
                            isActSet = true;
                            thienXinHang++;
                            break;
                        case 128:
                        case 140:
                            isActSet = true;
                            kirin++;
                            break;
                        case 131:
                        case 143:
                            isActSet = true;
                            ocTieu++;
                            break;
                        case 132:
                        case 144:
                            isActSet = true;
                            pikkoroDaimao++;
                            break;
                        case 130:
                        case 142:
                            isActSet = true;
                            picolo++;
                            break;
                        case 135:
                        case 138:
                            isActSet = true;
                            nappa++;
                            break;
                        case 133:
                        case 136:
                            isActSet = true;
                            kakarot++;
                            break;
                        case 134:
                        case 137:
                            isActSet = true;
                            cadic++;
                            break;
                    }

                    if (isActSet) {
                        break;
                    }
                }
            } else {
                break;
            }
        }
    }
    private void setupAN() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSett = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 34:
                            isActSett = true;
                            tinhan++;
                            break;
                        case 35:
                            isActSett = true;
                            nguyetan++;
                            break;
                        case 36:
                            isActSett = true;
                            nhatan++;
                            break;
                    }
                    if (isActSett) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }
//    private void setupGOGETA() {
//        for (int i = 5; i < 11; i++) {
//            Item item = this.player.inventory.itemsBody.get(i);
//            if (item.isNotNullItem()) {
//                boolean isActSet = false;
//                for (Item.ItemOption io : item.itemOptions) {
//                    switch (io.optionTemplate.id) {
//                        case 192:
//                        case 193:
//                            isActSet = true;
//                            solomon++;
//                            break;
//                    }
//                    if (isActSet) {
//                        break;
//                    }
//
//                }
//            } else {
//                break;
//            }
//        }
//    }
    private void setupDTS() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 21:
                            if (io.param == 120) {
                                setDTS++;
                            }
                            break;
                    }
                    if (isActSet) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }
    private void setupDHD() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 21:
                            if (io.param == 80) {
                                setDHD++;
                            }
                            break;
                    }
                    if (isActSet) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }
    private void setupDTL() {
        for (int i = 0; i < 5; i++) {
            Item item = this.player.inventory.itemsBody.get(i);
            if (item.isNotNullItem()) {
                boolean isActSet = false;
                for (Item.ItemOption io : item.itemOptions) {
                    switch (io.optionTemplate.id) {
                        case 21:
                            if (io.param == 15) {
                                setDTL++;
                            }
                            break;
                    }
                    if (isActSet) {
                        break;
                    }

                }
            } else {
                break;
            }
        }
    }

    private void setDefault() {
        this.songoku = 0;
        this.thienXinHang = 0;
        this.kirin = 0;
        this.ocTieu = 0;
        this.pikkoroDaimao = 0;
        this.picolo = 0;
        this.kakarot = 0;
        this.cadic = 0;
        this.nappa = 0;
        this.setDHD = 0;
        this.setDTS = 0;
        this.setDTL = 0;
        this.tabi = 0;
        this.tinhan = 0;
        this.nhatan = 0;
        this.nguyetan = 0;
        this.godClothes = false;
        this.ctHaiTac = -1;
    }

    public void dispose() {
        this.player = null;
    }
}
