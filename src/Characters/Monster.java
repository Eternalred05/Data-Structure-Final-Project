package Characters;

import Items.*;
import java.util.ArrayList;
import Misc.*;
import javafx.scene.image.Image;

public class Monster extends NPC {
    
    private Weapon actualWeapon;
    private int attack;
    private int life;
    private int actualLife;
    private int magic;
    private int defense;
    private int velocidad;
    private ArrayList<Item> loot;
    private int level;
    
    public Monster(Weapon actualWeapon, int attack, int magic, int defense, int velocidad, int level, String name, String sprite, int life, int actualLife) {
        super(name, sprite);
        setActualWeapon(actualWeapon);
        setAttack(attack);
        setMagic(magic);
        setDefense(defense);
        setVelocidad(velocidad);
        setLevel(level);
        setActualLife(actualLife);
        setLife(life);
        this.loot = new ArrayList<>();
    }
    
    public int getLife() {
        return life;
    }
    
    public void setLife(int life) {
        this.life = life;
    }
    
    public int getActualLife() {
        return actualLife;
    }
    
    public void setActualLife(int actualLife) {
        this.actualLife = actualLife;
    }
    
    public Weapon getActualWeapon() {
        return actualWeapon;
    }
    
    public void setActualWeapon(Weapon actualWeapon) {
        this.actualWeapon = actualWeapon;
    }
    
    public int getVelocidad() {
        return velocidad;
    }
    
    public void setVelocidad(int velocidad) {
        this.velocidad = velocidad;
    }
    
    public int getAttack() {
        return attack;
    }
    
    public void setAttack(int attack) {
        this.attack = attack;
    }
    
    public int getMagic() {
        return magic;
    }
    
    public void setMagic(int magic) {
        this.magic = magic;
    }
    
    public int getDefense() {
        return defense;
    }
    
    public void setDefense(int defense) {
        this.defense = defense;
    }
    
    public ArrayList<Item> getLoot() {
        return loot;
    }
    
    public void setLoot(ArrayList<Item> loot) {
        this.loot = loot;
    }
    
    public int getLevel() {
        return level;
    }
    
    public void setLevel(int level) {
        this.level = level;
    }
    
    public ArrayList<String> getDialogue() {
        return dialogue;
    }
    
    public void setDialogue(ArrayList<String> dialogue) {
        this.dialogue = dialogue;
    }
    
}
