/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.annotations.Expose;


public class InvItem
{
   @Expose  String decommodityf = "blah blah blah";
   @Expose  String type = "blah";
   @Expose  String nusenate = "blah";
   @Expose  String cdcategory = "blah";
   @Expose  String cdlocat = "";
   @Expose  String cdlocatto = "";
    
    boolean selected = false;
    
    final int DECOMMODITYF = -101;
    final int TYPE = -102;
    final int NUSENATE = -103;
    final int CDCATEGORY = -104;
    final int SELECTED = -105;   
    final int CDLOCAT = -106;   
    final int CDLOCATTO = -107;   

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

    //public String toJSON() {

        /*
         * Add the Ability to convert Android Object to JSON without any
         * external libraries.
         */

      /*  JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("nusenate", getNusenate());
            jsonObject.put("type", getType());
            jsonObject.put("cdcategory", getCdcategory());
            jsonObject.put("decommodityf", getDecommodityf());
            jsonObject.put("selected", getSelected());

            // Log.i("InvItem ToJSON", jsonObject.toString());

            return jsonObject.toString();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "";
        }

    }*/

//    public void parseJSON(String JSONString) {

        /*
         * Add the Ability to convert Android Object from JSON without any
         * external libraries.
         */

  /*      try {
            JSONObject jsonObject = new JSONObject(JSONString);
            try {
                this.setNusenate(jsonObject.getString("nusenate"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setType(jsonObject.getString("type"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setCdcategory(jsonObject.getString("cdcategory"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }
            try {
                this.setDecommodityf(jsonObject.getString("decommodityf"));
            } catch (JSONException e2) {
                e2.printStackTrace();
            }

            try {
                if (jsonObject.getString("selected").trim().toUpperCase()
                        .startsWith("T")) {
                    this.setSelected(true);
                } else {
                    this.setSelected(false);
                }
            } catch (JSONException e2) {
                this.setSelected(false);
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }*/

}
