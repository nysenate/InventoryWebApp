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
 @SerializedName("nuxrefem")
  private int nuxrefem;
  private transient String nafirst = null;
  private transient String nalast = null;
  private transient String namidinit = null;
  private transient String nasuffix = null;
  private transient String naemail = null;
 @SerializedName("naemployee")
  private String naemployee = null;
  public final int LAST_SUFFIX_FIRST_MI = -1000;
  public final int LAST_FIRST_MI = -1001;
  public final int LAST_SUFFIX_FIRST = -1002;
  public final int FIRST_MI_LAST_SUFFIX = -1003;
  public final int FIRST_MI_LAST = -1004;
  public final int FIRST_LAST = -1005;
  public final int LAST_FIRST = -1006;
  public final int FIRST_LAST_SUFFIX = -1007;
  private int employeeNameOrder = FIRST_MI_LAST_SUFFIX; 
  
  public Employee () {
  
  }

  public Employee (int nuxrefem, String nafirst, String nalast) {
        this(nuxrefem, nafirst, nalast, null, null);
        formatEmployeeName(this.FIRST_MI_LAST_SUFFIX);        
  }
  public Employee (int nuxrefem, String nafirst, String nalast,int employeeNameOrder) {
        this(nuxrefem, nafirst, nalast, null, null);
        formatEmployeeName(this.FIRST_MI_LAST_SUFFIX);        
  }
  
  public Employee (int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix) {
       this.nuxrefem = nuxrefem;
       this.nafirst = nafirst;
       this.nalast = nalast;
       this.namidinit = namidinit;
       this.nasuffix = nasuffix;
       formatEmployeeName(this.FIRST_MI_LAST_SUFFIX);
  }

  public Employee (int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix,int employeeNameOrder) {
       this.nuxrefem = nuxrefem;
       this.nafirst = nafirst;
       this.nalast = nalast;
       this.namidinit = namidinit;
       this.nasuffix = nasuffix;
       formatEmployeeName(employeeNameOrder);
  }
  
  
  public void setEmployeeData(int nuxrefem, String nafirst, String nalast) {
       setEmployeeData(nuxrefem, nafirst, nalast, null, null, this.FIRST_MI_LAST_SUFFIX);
  }
  
  public void setEmployeeData(int nuxrefem, String nafirst, String nalast, int employeeNameOrder) {
       setEmployeeData(nuxrefem, nafirst, nalast, null, null, employeeNameOrder);
  }

  public void setEmployeeData(int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix) {
      setEmployeeData(nuxrefem, nafirst, nalast,namidinit,nasuffix, FIRST_MI_LAST_SUFFIX);
  }

  public void setEmployeeData(int nuxrefem, String nafirst, String nalast,String namidinit,String nasuffix, int employeeNameOrder) {
       this.nuxrefem = nuxrefem;
       this.nafirst = nafirst;
       this.nalast = nalast;
       this.namidinit = namidinit;
       this.nasuffix = nasuffix;
       formatEmployeeName(employeeNameOrder);
  }

  public void setNafirst(String nafirst) {
      setNafirst(nafirst, true);
  }
  
  public void setNafirst(String nafirst, boolean formatName) {
       this.nafirst = nafirst;
       if (formatName) {
          formatEmployeeName(employeeNameOrder);
       }
  }    
  
  public void setNalast(String nalast) {
      setNalast(nalast, true);
  }
  
  public void setNalast(String nalast, boolean formatName) {
       this.nalast = nalast;
       if (formatName) {
          formatEmployeeName(employeeNameOrder);
       }
  }    

  public void setNamidinit(String namidinit) {
      setNamidinit(namidinit, true);
  }
  
  public void setNamidinit(String namidinit, boolean formatName) {
       this.nalast = nalast;
       if (formatName) {
          formatEmployeeName(employeeNameOrder);
       }
  }  
  
  public void setNasuffix(String nasuffix) {
      setNasuffix(nasuffix, true);
  }
  
  public void setNasuffix(String nasuffix, boolean formatName) {
       this.nasuffix = nasuffix;
       if (formatName) {
          formatEmployeeName(employeeNameOrder);
       }
  }  
  
  public void setNaemail(String naemail) {
      this.naemail = naemail;
  }

  public String getNalast() {
    return this.nalast;
  }
  
  public String getNafirst() {
    return this.nalast;
  }
  
  public String getNamidinit() {
    return this.namidinit;
  }
  
  public String getNasuffix() {
    return this.nasuffix;
  }
  
  public String getNaemail() {
    return this.naemail;
  }

  
  private void formatEmployeeName(int employeeNameOrder) {
      StringBuilder s = new StringBuilder();     
      switch (employeeNameOrder) {
          case FIRST_LAST:
          case FIRST_LAST_SUFFIX:
          case FIRST_MI_LAST:
          case FIRST_MI_LAST_SUFFIX:
          case LAST_FIRST:
          case LAST_FIRST_MI:
          case LAST_SUFFIX_FIRST:
          case LAST_SUFFIX_FIRST_MI:
              break;
          default:
              employeeNameOrder = FIRST_MI_LAST_SUFFIX;
      }
       
      this.employeeNameOrder = employeeNameOrder;

      if (employeeNameOrder==LAST_SUFFIX_FIRST_MI||employeeNameOrder==LAST_FIRST_MI||employeeNameOrder==LAST_SUFFIX_FIRST||employeeNameOrder==LAST_FIRST) {
            s.append(nalast);
            if (employeeNameOrder==LAST_SUFFIX_FIRST_MI||employeeNameOrder==LAST_SUFFIX_FIRST) {
                if (nasuffix != null && nasuffix.trim().length()>0) {
                        s.append(" ");
                        s.append(nasuffix);
                }
            }
            s.append(", ");
            s.append(nafirst);
            if (employeeNameOrder==LAST_SUFFIX_FIRST_MI||employeeNameOrder==LAST_FIRST_MI) {

                if (namidinit != null && namidinit.trim().length()>0) {
                      s.append(" ");
                      s.append(namidinit);
                }
            }
       }
      else  {
            s.append(nafirst);
            if (employeeNameOrder==FIRST_MI_LAST_SUFFIX||employeeNameOrder==FIRST_MI_LAST) {

                if (namidinit != null && namidinit.trim().length()>0) {
                      s.append(" ");
                      s.append(namidinit);
                }
            }
            s.append(" ");
            s.append(nalast);
            if (employeeNameOrder==FIRST_MI_LAST_SUFFIX||employeeNameOrder==FIRST_LAST_SUFFIX) {
                if (nasuffix != null && nasuffix.trim().length()>0) {
                        s.append(" ");
                        s.append(nasuffix);
                }
            }
       } 
       this.naemployee = s.toString();      
  }
  
  public int getEmployeeNameOrder() {
      return this.employeeNameOrder;
  }

  public void setEmployeeNameOrder(int employeeNameOrder) {
      formatEmployeeName(employeeNameOrder); 
      // formatEmployeeName is meant to format the name, but it also sets the
      // employeeNameOrder value, it is kept private since the name would be to
      // to confusing. setEmployeeNameOrder is more descriptive on what the developer
      // wants to do, and in the process the Employee Name has to get formated again.
  }
  
  public String getEmployeeName() {
      return naemployee;
  }
    
  public int getEmployeeXref() {
      return nuxrefem;
  }
    
}
