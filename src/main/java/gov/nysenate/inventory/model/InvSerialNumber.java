/*
 * New Serial Item created as a quick way to return less data.
 * This has been created for Search Activity on the mobile client.
 */
package gov.nysenate.inventory.model;

import com.google.gson.annotations.Expose;

/**
 *
 * @author senateuser
 */
public class InvSerialNumber
{
   @Expose  String nuserial = "";
   @Expose  String nusenate = "";
   @Expose  String nuxrefsn = "";
   
    final int NUSERIAL = -101;
    final int NUSENATE = -103;
    final int NUXERFSN = -104;
 
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

    @Override
    public String toString() {
        return nuserial;
    }
}
