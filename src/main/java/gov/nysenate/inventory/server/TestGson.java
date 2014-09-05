/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;


import java.util.ArrayList;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.exception.BlankMessageException;
import gov.nysenate.inventory.exception.InvalidParameterException;
import gov.nysenate.inventory.exception.ParameterNotUsedException;
import gov.nysenate.inventory.model.EmailData;
import gov.nysenate.inventory.model.EmailRecord;
import gov.nysenate.inventory.model.Employee;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;

import java.sql.SQLException;
import java.util.logging.Level;

/*
 * @author senateuser
 */
public class TestGson {
    private static ArrayList<EmailRecord> problemEmailAddrs = new ArrayList<>();
    DbConnect db = new DbConnect(null);

    private static final Logger log = Logger.getLogger(TestGson.class.getName());
    
    public static void main(String[] args) throws ParserConfigurationException {
        DbConnect db = new DbConnect(null);
        try {
            Employee employee = db.getEmployee("HEITNER");
            if (employee ==null) {
                System.out.println("Employee is null");
            }
            else {
                System.out.println(employee.getEmployeeXref());
                System.out.println(employee.getNaemail());
                System.out.println(employee.getEmployeeName());
                employee.setEmployeeNameOrder(employee.FIRST_MI_LAST_SUFFIX);
                System.out.println(employee.getEmployeeName());
                
                
            }
            
    /*        DbConnect db = new DbConnect(null);
            System.out.println("serverName:" + db.serverName);
            TestGson testGson = new TestGson();
            testGson.buildTestErrorEmails();
            System.out.println(testGson.getProblemEmailString());

            /*        EmailData warningEmailData = new EmailData(db, "EMAILWARNING");
            String serverInfo = null;
            try {
            warningEmailData.put("EmailType", "FAKE EMAIL TYPE");
            warningEmailData.put("ReceiptURL", "http://www.google.com?test=1022");
            //warningEmailData.put("ReceiptURL", "http://www.google.com?test=1022" );
            warningEmailData.put("ProblemRecipients", "PROBLEM RECIPIENTS!!!");
            if (serverInfo!=null) {
            System.out.println("NOT NULL serverInfo:"+serverInfo);
            //Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.INFO, "", null);
            warningEmailData.put("ServerInfo", serverInfo);
            }
            else {
            warningEmailData.put("ServerInfo", "");
            System.out.println("!!!!!NULL serverInfo:"+serverInfo);
            }
            System.out.println (warningEmailData.getMessage());
            System.out.println (warningEmailData.getFormattedMessage());
            } catch (InvalidParameterException ex) {
            log.error(null, ex);
            } catch (ParameterNotUsedException ex) {
            log.error(null, ex);
            } catch (BlankMessageException ex) {
            log.error(null, ex);
            }
            /* String test = "EMPLOYEE";
            System.out.println (test.substring(0, 1));
            java.util.Date date = new java.util.Date();
            System.out.println ("DATE:"+date);
            Time time = new Time(date.getTime());
            System.out.println ("SQL TIME:"+time);*/


            /*    EmailValidator emailValidator = new EmailValidator();
            String[] emailAddresses = {null, "test", "test@senate.state.ny.us", "test", "test@", "@senate.state.ny.us", "wow.com", "test@senate", "test@nysenate.gov", "test@gmail.com", "TEST@YAHOO.COM"};
            for (int x=0;x<emailAddresses.length;x++) {
            String emailAddress = emailAddresses[x];
            if (emailValidator.validate(emailAddress)) {
            System.out.println(emailAddress+" is valid e-mail");
            }
            else {
            System.out.println("***"+emailAddress+" is INVALID e-mail");
            }
            }
            /*
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            LoginStatus loginStatus = new LoginStatus();
            loginStatus.setCdseclevel("01");
            loginStatus.setDestatus("This is a test of destatus.");
            loginStatus.setDtpasswdexp(new Date());
            loginStatus.setNauser("asdasdghf");
            loginStatus.setNustatus(loginStatus.INVALID_USERNAME_OR_PASSWORD);
            loginStatus.setSQLErrorCode(102);
            String json = gson.toJson(loginStatus);
            System.out.println(json);
             */
            /*        System.out.println (convertTime((long)((3*60*60*1000)+(32*60*1000)+(18*1000)+383)));
            System.out.println (convertTime((long)1533));
            System.out.println (convertTime((long)434));
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            List<SimpleListItem> list = Collections.synchronizedList(new ArrayList<SimpleListItem>() );
            SimpleListItem simpleListItem = new SimpleListItem();
            simpleListItem.setNavalue("MY FIRST REC");
            simpleListItem.setNatype("FIRST");
            list.add(simpleListItem);
            simpleListItem = new SimpleListItem();
            simpleListItem.setNatype("SECOND");
            simpleListItem.setNavalue("MY SECOND REC");
            list.add(simpleListItem);
            System.out.println (gson.toJson(list));
            //Make Serial
            List<InvItem> list2 = Collections.synchronizedList(new ArrayList<InvItem>() );
            list2.add(new InvItem("088998", "sdfsdfsd", "NEW",
            "THIS IS THE FIRST ITEM"));
            list2.add(new InvItem("392343", "fgdsgfgs", "EXISTS",
            "THIS IS THE SECOND ITEM"));
            System.out.println (gson.toJson(list2));       */
            //Make Serial
            /*
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type listOfTestObject = new TypeToken<List<InvItem>>(){}.getType();
            //Make Serial
            List<InvItem> list = Collections.synchronizedList(new ArrayList<InvItem>() );
            list.add(new InvItem("088998", "sdfsdfsd", "NEW",
            "THIS IS THE FIRST ITEM"));
            list.add(new InvItem("392343", "fgdsgfgs", "EXISTS",
            "THIS IS THE SECOND ITEM"));
            System.out.println (gson.toJson(list));
            /*
            Gson gson = new Gson();
            Type listOfTestObject = new TypeToken<List<PickupGroup>>(){}.getType();
            //Make Serial
            /*        Type locationInfoListType = (Type) new TypeToken<List<PickupGroup>>() {}.getType();
            Gson gson = new GsonBuilder()
            .registerTypeAdapter(PickupGroupTypeAdapter, new PickupGroupTypeAdapter())
            .create();
            System. out.println("{\"nusenate\":\""+"1"+"\",\"nuxrefsn\":\""+"2"+"\",\"dtissue\":\""+"3"+",\"cdlocatto\":\""+"4"+"\",\"cdlocatto\":\""+"5"+"\",\"cdcategory\":\""+"6"+"\",\"decommodityf\":\""+"7"+"\"}");*/

            /*         ArrayList<Employee> empList = new ArrayList<Employee>();
            String json = new Gson().toJson(curEmp);
            System.out.println (json);*/
        } catch (SQLException ex) {
            java.util.logging.Logger.getLogger(TestGson.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(TestGson.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    ;

    public static String convertTime(long time) {
        long secDiv = 1000;        
        long minDiv = 1000 * 60;
        long hourDiv = 1000 * 60 *60;
        long minutes = time % hourDiv;
        long seconds = minutes % minDiv;
        int hoursConverted = (int)(time/hourDiv);
        int minutesConverted = (int)(minutes/minDiv);
        int secondsConverted = (int)(seconds/secDiv);
      
        StringBuffer  returnTime = new StringBuffer();
        if (hoursConverted>0) {
            returnTime.append("Hours:");
            returnTime.append(hoursConverted);
            returnTime.append(" ");
        }
        if (hoursConverted>0||minutesConverted>0) {
            returnTime.append("Minutes:");
            returnTime.append(minutesConverted);
            returnTime.append(" ");
        }
        returnTime.append("Seconds:");
        returnTime.append(secondsConverted);
        returnTime.append(" ");
        
        return returnTime.toString();
    }

    public void buildTestErrorEmails() {
        String[] emails = {"mickmouse", "donduck", "badone", "daffduck"};
        String[] emailNames = {"Mickey Mouse", "Donald Duck", "Bad One", "Daffy Duck"};

        for (int x = 0; x < emails.length; x++) {
            problemEmailAddrs.add(currentEmailRecord(x, emails[x], emailNames[x]));
        }
    }

    public EmailRecord currentEmailRecord(int x, String email, String emailName) {
        EmailRecord emailRecord = new EmailRecord();
        String badString = null;
        try {
            emailRecord.setNaemail(email);
            emailRecord.setNaemailName(emailName);
            String badtest = badString.toUpperCase(); // cause Null Pointer Exception to occur
        } catch (Exception e) {
            emailRecord.setErrorMessage("Fake Error Message#" + x);
            emailRecord.setErrorStackTrace(e.getStackTrace());
        }
        return emailRecord;
    }
 
   public String getProblemEmailString()
  {
    StringBuilder returnString = new StringBuilder();
    if (problemEmailAddrs == null || problemEmailAddrs.isEmpty()) {
      return "<b>PROBLEM E-MAIL INFORMATION NOT AVAILABLE</b>";
    } else {
      EmailData emailAddrErrorInfo = new EmailData(db, "EMAILADDRERRORINFO");
      for (int x = 0; x < problemEmailAddrs.size(); x++) {
        /*
         * Clear the puts from previous values in the looping mechanism
         */
        if (x>0) {
            emailAddrErrorInfo.clearValues();
        }
        EmailRecord emailRecord = problemEmailAddrs.get(x);
          try {
              emailAddrErrorInfo.put("EmailName", emailRecord.getNaemailName());
          } catch (InvalidParameterException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (ParameterNotUsedException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (BlankMessageException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          }
          
          try {
              emailAddrErrorInfo.put("Email", emailRecord.getNaemail());
          } catch (InvalidParameterException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (ParameterNotUsedException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (BlankMessageException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          }
          
          try {
              emailAddrErrorInfo.put("ErrorMessage", emailRecord.getErrorMessage());
          } catch (InvalidParameterException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (ParameterNotUsedException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (BlankMessageException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          }

          
        /*returnString.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Email:</b> ");
        returnString.append(emailRecord.getNaemail());
        returnString.append("&nbsp;&nbsp;&nbsp<b>Name:</b> ");
        returnString.append(emailRecord.getNaemailName());
        returnString.append("&nbsp;&nbsp;&nbsp<b>Error Message:</b> ");
        returnString.append(emailRecord.getErrorMessage());*/
        StringBuffer stackTraceString = new StringBuffer();
        StackTraceElement[] errorStackTrace = emailRecord.getErrorStackTrace();
        if (errorStackTrace != null) {
          for (int y = 0; y < errorStackTrace.length; y++) {
            if (y>0) {
                stackTraceString.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            stackTraceString.append(errorStackTrace[y].toString());
          }
        }
          try {
              emailAddrErrorInfo.put("StackTrace", stackTraceString.toString());
          } catch (InvalidParameterException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (ParameterNotUsedException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          } catch (BlankMessageException ex) {
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
          }
         returnString.append(emailAddrErrorInfo.getFormattedMessage());
        //returnString.append("<br/>");
      }
    }
    return returnString.toString();
  }
    
}
