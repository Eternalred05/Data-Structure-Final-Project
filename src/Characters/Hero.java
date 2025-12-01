package Characters;

import Items.*;
import Logic.Game;
import cu.edu.cujae.ceis.tree.general.GeneralTree;
import Misc.*;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Queue;
import javafx.scene.image.Image;

public class Hero implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Location {
        MAP, FIELD_VILLAGE, UNKNOWN
    }

    private String name;
    private String spritePath;
    private transient Image fxImage;
    private int velocidad;
    private int attack;
    private int magic;
    private int defense;
    private int level;
    private int life;
    private int actualLife;
    private LinkedList<Item> items;
    private LinkedList<Weapon> weapons;
    private Weapon actualWeapon;
    private Armor armor;
    private GeneralTree<Classes> unlockedClasses;
    private Classes actualClass;
    private Queue<Task> tasks;
    private Deque<Task> completedTasks;

    private Location lastLocation = Location.UNKNOWN;
    private double lastPosX = 0.0;
    private double lastPosY = 0.0;

    public Hero(String name, Weapon weapon, Armor armor) {
        setName(name);
        setLife(100);
        setActualLife(100);
        setSpritePath("/Resources/sprites/hero.png");
        setAttack(5);
        setMagic(20);
        setDefense(4);
        setLevel(1);
        setVelocidad(10);
        setArmor(armor);
        items = new LinkedList<>();
        weapons = new LinkedList<>();
        actualWeapon = weapon;
        unlockedClasses = new GeneralTree<>();
        this.actualClass = null;
        tasks = new ArrayDeque<>();
        completedTasks = new ArrayDeque<>();
        loadFxImage();
    }

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location lastLocation) {
        this.lastLocation = lastLocation != null ? lastLocation : Location.UNKNOWN;
    }

    public double getLastPosX() {
        return lastPosX;
    }

    public void setLastPosX(double lastPosX) {
        this.lastPosX = lastPosX;
    }

    public double getLastPosY() {
        return lastPosY;
    }

    public void setLastPosY(double lastPosY) {
        this.lastPosY = lastPosY;
    }

    public Armor getArmor() {
        return armor;
    }

    public void setArmor(Armor armor) {
        this.armor = armor;
    }

    public GeneralTree<Classes> getUnlockedClasses() {
        return unlockedClasses;
    }

    public void setUnlockedClasses(GeneralTree<Classes> unlockedClasses) {
        this.unlockedClasses = unlockedClasses;
    }

    public Classes getActualClass() {
        return actualClass;
    }

    public void setActualClass(Classes actualClass) {
        this.actualClass = actualClass;
    }

    public int getLife() {
        return life;
    }

    public void setLife(int life) {
        if (life > 0) {
            this.life = life;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public int getActualLife() {
        return actualLife;
    }

    public void setActualLife(int actualLife) {
        this.actualLife = actualLife;
    }

    private void loadFxImage() {
        if (!(spritePath == null || spritePath.isEmpty())) {
            try {
                fxImage = new Image(getClass().getResourceAsStream(spritePath));
                if (fxImage.isError()) {
                    fxImage = null;
                }
            } catch (Throwable ignored) {
                fxImage = null;
            }
        }
    }

    public int getVelocidad() {
        return velocidad;
    }

    public void setVelocidad(int velocidad) {
        if (velocidad >= 1) {
            this.velocidad = velocidad;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public Image getImage() {
        if (fxImage != null) {
            return fxImage;
        }
        loadFxImage();
        return fxImage;
    }

    public void setSpritePath(String spritePath) {
        this.spritePath = spritePath;
        this.fxImage = null;
    }

    public String getSpritePath() {
        return spritePath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (!name.isEmpty()) {
            this.name = name;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public int getAttack() {
        return attack;
    }

    public void setAttack(int attack) {
        if (attack >= 1) {
            this.attack = attack;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public int getMagic() {
        return magic;
    }

    public void setMagic(int magic) {
        if (magic >= 1) {
            this.magic = magic;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        if (defense >= 1) {
            this.defense = defense;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        if (level >= 1) {
            this.level = level;
        } else {
            throw new IllegalArgumentException("Debe ser mayor que 0");
        }
    }

    public LinkedList<Item> getItems() {
        return items;
    }

    public void setItems(LinkedList<Item> items) {
        this.items = items;
    }

    public LinkedList<Weapon> getWeapons() {
        return weapons;
    }

    public void setWeapons(LinkedList<Weapon> weapons) {
        this.weapons = weapons;
    }

    public Weapon getActualWeapon() {
        return actualWeapon;
    }

    public void setActualWeapon(Weapon actualWeapon) {
        this.actualWeapon = actualWeapon;
    }

    public Deque<Task> getCompletedTasks() {
        return completedTasks;
    }

    public void addCompletedTasks(Task t) {
        completedTasks.push(t);
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public void addTasks(Task t) {
        tasks.offer(t);
    }
}
