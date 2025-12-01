
package Items;

import java.io.Serializable;
import javax.swing.Icon;

public abstract class Item implements Serializable{
protected String info;
protected String name;
protected String id;
protected Icon icon;

protected static final long serialVersionUID = 1L;

public Item(String info, String name, String id){
    setInfo(info);
    setName(name);
    setId(id);
    icon = null;
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
