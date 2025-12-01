package Items;

import javax.swing.Icon;

public class Wares extends Item {
private int healing;

public Wares(String info, String name, String id, int healing){
    super(info, name, id);
    setHealing(healing);
    
}

    public int getHealing() {
        return healing;
    }

    public void setHealing(int healing) {
        this.healing = healing;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Icon getIcon() {
        return icon;
    }

    public void setIcon(Icon icon) {
        this.icon = icon;
    }


}
