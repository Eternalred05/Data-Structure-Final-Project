package Items;

public class Gun extends Weapon {

    private double range;
    
    public Gun(String info, String name, String id, int attack, int lifeSpan,
            String effect, String type, double range){
        super(info, name, id, attack, lifeSpan, effect, type);
        setRange(range);
    }

    public double getRange() {
        return range;
    }

    public void setRange(double range) {
        this.range = range;
    }
    
    
}
