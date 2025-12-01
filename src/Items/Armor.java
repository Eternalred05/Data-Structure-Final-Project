package Items;

public class Armor extends Item{
    protected int defense;
    protected String effect;
   
    
    public Armor(String info, String name, String id ,int defense, String effect){
        super(info, name, id);
        setDefense(defense);
        setEffect(effect);
    }

    public int getDefense() {
        return defense;
    }

    public void setDefense(int defense) {
        this.defense = defense;
    }

    public String getEffect() {
        return effect;
    }

    public void setEffect(String effect) {
        this.effect = effect;
    }
    
    
    
    
}
