package campuschat.wifi.bean;

/**
 * Created by vinayak ks on 4/1/2016.
 */
public class TeamMembers extends Entity {
    private int imageId;
    private String stringid;

    public TeamMembers() {
        super();
    }

    public TeamMembers(int imageId,String stringid) {
        super();
        this.imageId = imageId;
        this.stringid = stringid;
    }

    public int getImageId() {
        return imageId;
    }
    public String getStringid(){
        return stringid;
    }
    public void setStringid(String stringid){
        this.stringid = stringid;
    }

    public void setImageId(int imageId) {
        this.imageId = imageId;
    }
}