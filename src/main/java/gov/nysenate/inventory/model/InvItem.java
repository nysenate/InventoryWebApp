package gov.nysenate.inventory.model;

import com.google.gson.annotations.Expose;

public class InvItem
{
   @Expose  String decommodityf = "";
   @Expose  String type = "";
   @Expose  String nusenate = "";
   @Expose  String cdcategory = "";
   @Expose  String cdlocat = "";
    @Expose String cdlocatto = ""; // TODO: not part of an item
   @Expose  String cdintransit = "";
   @Expose  String decomments = "";
    @Expose String nuxrefco = ""; // TODO: needed here?
   @Expose  String cdcommodity = "";
   
    boolean selected = false;
    
    final transient int DECOMMODITYF = -101;
    final transient int TYPE = -102;
    final transient int NUSENATE = -103;
    final transient int CDCATEGORY = -104;
    final transient int SELECTED = -105;
    final transient int CDLOCAT = -106;
    final transient int CDLOCATTO = -107;
    final transient int CDINTRANSIT = -108;  // Note: Does not match the Andriod CDINTRANSIT value
    final transient int DECOMMENTS = -109;
    final transient int NUXREFCO = -110;
    final transient int CDCOMMODITY = -111;
    
    public InvItem(String nusenate, String cdcategory, String type,
            String decommodityf) {
        this.nusenate = nusenate;
        this.cdcategory = cdcategory;
        this.type = type;
        this.decommodityf = decommodityf;
    }

    public InvItem() {

    }

    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }

    public String getNusenate() {
        return nusenate;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getCdcategory() {
        return cdcategory;
    }

    public void setCdcategory(String cdcategory) {
        this.cdcategory = cdcategory;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCdlocat() {
        return cdlocat;
    }

    public void setCdlocat(String cdlocat) {
        this.cdlocat = cdlocat;
    }

    public String getCdlocatto() {
        return cdlocatto;
    }

    public void setCdlocatto(String cdlocatto) {
        this.cdlocatto = cdlocatto;
    }

    
   public String getCdintransit() {
        return cdintransit;
    }
    
    public void setCdintransit(String cdlocat) {
        this.cdintransit = cdintransit;
    }

    public String getDecomments() {
        return decomments;
    }
    
    public void setDecomments(String decomments) {
        this.decomments = decomments;
    }
    
    public void setCdcommodity(String cdcommodity) {
        this.cdcommodity = cdcommodity;
    }

    public String getCdcommodity() {
        return cdcommodity;
    }    
    
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean getSelected() {
        return this.selected;
    }

    @Override
    public String toString() {
        return decommodityf;
    }
}
