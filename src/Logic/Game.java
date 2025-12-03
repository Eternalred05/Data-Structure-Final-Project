package Logic;

import Items.*;
import Characters.*;
import Misc.*;
import cu.edu.cujae.ceis.tree.general.GeneralTree;
import Utils.*;
import cu.edu.cujae.ceis.tree.binary.BinaryTreeNode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

public class Game {

    private File save;
    private File archives;
    private Hero hero;
    private ArrayList<NPC> characters;
    private Queue<Task> tasks;
    private ArrayList<Item> items;
    private GeneralTree<Classes> classes;
    private LocalDateTime playedTime;

    public Hero getHero() {
        return hero;
    }

    public void createHero(String name) {
        hero = new Hero(name, (Weapon) items.get(0), (Armor) items.get(6));
    }

    public LocalDateTime getPlayedTime() {
        return playedTime;
    }

    public void setPlayedTime(LocalDateTime playedTime) {
        this.playedTime = playedTime;
    }

    public File getSave() {
        return save;
    }

    public void setSave(File save) {
        this.save = save;
    }

    public ArrayList<NPC> getCharacters() {
        return characters;
    }

    public void addCharacters(NPC npc) {
        characters.add(npc);
    }

    public Queue<Task> getTasks() {
        return tasks;
    }

    public void addTasks(Task t) {
        tasks.offer(t);
    }

    public ArrayList<Item> getItems() {
        return items;
    }

    public void addItems(Item i) {
        items.add(i);
    }

    public File getArchives() {
        return archives;
    }

    public void setArchives(File archives) {
        this.archives = archives;
    }

    public GeneralTree<Classes> getClasses() {
        return classes;
    }

    public void setClasses(GeneralTree<Classes> classes) {
        this.classes = classes;
    }

    public Game() {
        this.save = new File("C:\\Ale\\Codigos\\Filetest", "file0.sav");
        this.archives = new File("C:\\Ale\\Codigos\\Filetest", "file.sav");

        File parent = save.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        this.characters = new ArrayList<>();
        this.tasks = new ArrayDeque<>();
        this.items = new ArrayList<>();
        this.classes = new GeneralTree<>();
    }

    public boolean createSaveGame() {
        boolean created = false;

        File parent = save.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        try (RandomAccessFile raf = new RandomAccessFile(save, "rw")) {
            byte[] data = Convert.toBytes(hero);
            raf.writeInt(data.length);
            raf.write(data);
            created = true;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return created;
    }

    public boolean readSaveGame() {
        boolean correct = false;
        try (RandomAccessFile raf = new RandomAccessFile(save, "rw")) {
            int len = raf.readInt();
            byte[] data = new byte[len];
            raf.readFully(data);
            hero = (Hero) Convert.toObject(data);
            correct = true;
            raf.close();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        return correct;
    }

    public boolean deleteSaveGame() {
        boolean correct = false;
        if (save.exists()) {
            correct = save.delete();
        }
        return correct;
    }

    public boolean combat(Monster m) {
        int attack = m.getAttack() + m.getActualWeapon().getAttack();
        int defense = hero.getDefense() + hero.getArmor().getDefense();
        int damage = (attack - defense);
        boolean attacked = false;
        if (damage > 0) {
            int life = (hero.getActualLife()) - damage;
            if (life < 0) {
                hero.setActualLife(0);
            } else {
                hero.setActualLife(life);
            }
            attacked = true;
        }
        return attacked;
    }

    public boolean heroCombat(Monster m) {
        int attack = hero.getAttack() + hero.getActualWeapon().getAttack();
        int defense = m.getDefense();
        int damage = (attack - defense);
        boolean attacked = false;
        if (damage > 0) {
            int life = (m.getActualLife()) - damage;
            if (life < 0) {
                m.setActualLife(0);
            } else {
                m.setActualLife(life);
            }
            attacked = true;
        }
        return attacked;
    }

    public boolean checkGameOver(int life) {
        boolean dead = false;
        if (life == 0) {
            dead = true;
        }
        return dead;

    }

    public boolean heal(Wares w) {
        boolean cured = false;
        if (hero.getActualLife() < hero.getLife()) {
            if (hero.getLife() < w.getHealing() + hero.getActualLife()) {
                hero.setActualLife(hero.getLife());

            } else {
                hero.setActualLife(hero.getActualLife() + w.getHealing());
            }
            cured = true;
        }
        return cured;
    }

    public void createItems() {
        items.add(new Fist("It punches.", "Bare Hands", "H000", 5, 120000, "Inflicts damage.",
                "A"));
        items.add(new Gun("It shoots.", "Desert Eagle", "GUN01", 20, 100, "Inflict damage.",
                "A", 50.0));
        items.add(new Sword("It cuts.", "Guardian Sword", "SW01", 30, 100, "Inflict damage.",
                "A"));
        items.add(new Spear("It drills.", "Guardian Spear", "SP01", 18, 100, "Inflict damage.",
                "A"));
        items.add(new Claymore("It cuts but longer.", "Royal Claymore", "CLY01", 100, 100, "Inflict damage", "A"));
        items.add(new Wares("It cures.", "Healing Bandages", "WS01", 50));
        items.add(new Armor("Basic Armor", "Broken Cloath", "A000", 2, "Pasen Polnito"));
    }

    public void createMonsters() {
        characters.add(new Monster((Weapon) items.get(1), 5, 5, 5, 5, 3, "Gnome", null, 20, 20));
        characters.add(new Monster((Weapon) items.get(2), 7, 3, 5, 7, 3, "Goblin", null, 20, 20));
        characters.add(new Monster((Weapon) items.get(0), 5, 3, 10, 3, 3, "Mystical Crab", null, 20, 20));
    }
    
    public void createClassTree(){
        
        BinaryTreeNode<Classes> warrior = new BinaryTreeNode<>(
            new WarriorClass("The basic class. Hits using fists.", true, true));
        BinaryTreeNode<Classes> swordman = new BinaryTreeNode<>(
            new SwordmanClass("Hits using a sword. Can be upgraded to anothers blades.", false, false));
        BinaryTreeNode<Classes> spearman = new BinaryTreeNode<>(
            new SpearClass("Hits using a spear. Can be upgraded to anothers spears-like.", false, false));
        BinaryTreeNode<Classes> gunner = new BinaryTreeNode<>(
            new GunnerClass("Hits using a gun. Can be upgraded to anothers fire weapons.", false, false));
        BinaryTreeNode<Classes> claymoreUser = new BinaryTreeNode<>(
            new ClaymoreUserClass("Hits using a claymore.", false, false));
        BinaryTreeNode<Classes> sabreUser = new BinaryTreeNode<>(
            new SabreUserClass("Hits using a sabre.", false, false));
        BinaryTreeNode<Classes> rifleUser = new BinaryTreeNode<>(
            new RifleUserClass("Hits using a rifle.", false, false));
        BinaryTreeNode<Classes> shotgunUser = new BinaryTreeNode<>(
            new ShotgunUserClass("Hits using a shotgun.", false, false));
        BinaryTreeNode<Classes> halberdUser = new BinaryTreeNode<>(
            new HalberdUserClass("Hits using a halberd.", false, false));
        BinaryTreeNode<Classes> pikeUser = new BinaryTreeNode<>(
            new PikeUserClass("Hits using a pike.", false, false));
        
        classes.setRoot(warrior);
        
        classes.insertAsFirstSon(swordman, warrior);
        classes.insertAsFirstSon(claymoreUser, swordman);
        classes.insertNode(sabreUser, swordman);
        
        classes.insertNode(spearman, warrior);
        classes.insertAsFirstSon(halberdUser, spearman);
        classes.insertNode(pikeUser, spearman);
        
        classes.insertNode(gunner, warrior);
        classes.insertAsFirstSon(shotgunUser, gunner);
        classes.insertNode(rifleUser, gunner);
    }

}
