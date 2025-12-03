package Misc;

public abstract class Classes {
    protected String description;
    protected boolean unlocked;
    protected boolean actived;
    
    public Classes(String description, boolean unlocked, boolean actived){
        setDescription(description);
        setUnlocked(unlocked);
        setActived(actived);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isUnlocked() {
        return unlocked;
    }

    public void setUnlocked(boolean unlocked) {
        this.unlocked = unlocked;
    }

    public boolean isActived() {
        return actived;
    }

    public void setActived(boolean actived) {
        this.actived = actived;
    }
    
    
           
}
