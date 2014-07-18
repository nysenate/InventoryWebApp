package gov.nysenate.inventory.model;

/**
 *
 * @author Brian Heitner
 */
public class Commodity
{
    private int id;
    private String type = "";
    private String category = "";
    private String code = "";
    private String description = "";

    public Commodity(int id, String type, String category, String code) {
        this.id = id;
        this.type = type;
        this.category = category;
        this.code = code;
    }

    public int getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getCategory() {
        return category;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
