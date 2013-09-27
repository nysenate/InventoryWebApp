package gov.nysenate.inventory.model;


public abstract class Transaction {

    protected int nuxrpd;
    protected Location origin;
    protected Location destination;
    // SignatureView signature;
    // byte[] sigBytes;

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public Location getOrigin() {
      /*
       *   If origin is not set, then 
       *   create a new origin. This is much 
       *   better than having the Server Crash
       * 
       */
        if (origin==null) {
          origin = new Location();
        }
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
      /*
       *   If destination is not set, then 
       *   create a new destination. This is much 
       *   better than having the Server Crash
       * 
       */
        if (destination==null) {
          destination = new Location();
        }
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }
}
