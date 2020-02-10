/*
 * New Serial Item created as a quick way to return less data.
 * This has been created for Search Activity on the mobile client.
 */
package gov.nysenate.inventory.model;

import com.google.gson.annotations.Expose;

import java.util.List;

/**
 *
 * @author senateuser
 */
public class InvSerialNumber
{
   @Expose  String nuserial = "";
   @Expose  String nusenate = "";
   @Expose  String nuxrefsn = "";
   @Expose  String cdcommodity = "";
   @Expose  String decommodityf = "";
   @Expose  String statusNum = "0";
   @Expose  List<Location> locations;

    final int NUSERIAL = -101;
    final int NUSENATE = -103;
    final int NUXERFSN = -104;
    final int CDCOMMODITY = -105;
    final int DECOMMODITYF = -106;
    final int STATUSNUM = -107;
     
    public InvSerialNumber() {
    }

    public String getNuserial() {
        return nuserial;
    }

    public void setNuserial(String nuserial) {
        this.nuserial = nuserial;
    }

    public String getNusenate() {
        return nusenate;
    }

    public void setNusenate(String nusenate) {
        this.nusenate = nusenate;
    }

    public String getNuxrefsn() {
        return nuxrefsn;
    }

    public void setNuxrefsn(String nuxrefsn) {
        this.nuxrefsn = nuxrefsn;
    }

    public String getCdcommodty() {
        return cdcommodity;
    }

    public void setCdcommodity(String cdcommodity) {
        this.cdcommodity = cdcommodity;
    }
    
    public String getDecommodityf() {
        return decommodityf;
    }

    public void setDecommodityf(String decommodityf) {
        this.decommodityf = decommodityf;
    }    

    public String getStatusNum() {
        return statusNum;
    }

    public void setStatusNum(String statusNum) {
        this.statusNum = statusNum;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    public List<Location> getLocations() {
        return locations;
    }

    @Override
    public String toString() {
        return nuserial;
    }
}
