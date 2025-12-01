package Characters;

import Items.Weapon;
import Misc.Classes;

public class Boss extends Monster {

    public Boss(Weapon actualWeapon, int attack, int magic, int defense, int velocidad, int level, String name, String sprite, int life, int actualLife) {
        super(actualWeapon, attack, magic, defense, velocidad, level, name, sprite, life, actualLife);
    }

}
