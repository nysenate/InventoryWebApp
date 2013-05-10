package gov.nysenate.inventory.server;



import com.google.gson.annotations.SerializedName;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author HEITNER
 */
public class Employee {
  private int nuxrefem;
  private transient String nafirst = null;
  private transient String nalast = null;
  private transient String namidinit = null;
  private transient String nasuffix = null;
  private String naemployee = null;
  
  public Employee () {
  
  }

  public Employee (int nuxrefem, String nafirst, String nalast) {
        this(nuxrefem, nafirst, nalast, null, null);
  }
  
  public Employee (int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix) {
       this.nuxrefem = nuxrefem;
       this.nafirst = nafirst;
       this.nalast = nalast;
       this.namidinit = namidinit;
       this.nasuffix = nasuffix;
       StringBuilder s = new StringBuilder();
       s.append(nalast);
       if (nasuffix != null && nasuffix.trim().length()>0) {
            s.append(" ");
            s.append(nasuffix);
       }
       s.append(", ");
       s.append(nafirst);
       if (namidinit != null && namidinit.trim().length()>0) {
            s.append(" ");
            s.append(namidinit);
       }
       this.naemployee = s.toString();
  }
  
  public void setEmployeeData(int nuxrefem, String nafirst, String nalast) {
       setEmployeeData(nuxrefem, nafirst, nalast, null, null);
  }
  
  public void setEmployeeData(int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix) {
       this.nuxrefem = nuxrefem;
       this.nafirst = nafirst;
       this.nalast = nalast;
       this.namidinit = namidinit;
       this.nasuffix = nasuffix;
       StringBuilder s = new StringBuilder();
       s.append(nalast);
       if (nasuffix != null && nasuffix.trim().length()>0) {
            s.append(" ");
            s.append(nasuffix);
       }
       s.append(", ");
       s.append(nafirst);
       if (namidinit != null && namidinit.trim().length()>0) {
            s.append(" ");
            s.append(namidinit);
       }
       this.naemployee = s.toString();
  }
  
  public String getEmployeeName() {
      return naemployee;
  }
    
  public int getEmployeeXref() {
      return nuxrefem;
  }
    
}
