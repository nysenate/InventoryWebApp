/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;


import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import gov.nysenate.inventory.exception.BlankMessageException;
import gov.nysenate.inventory.exception.InvalidParameterException;
import gov.nysenate.inventory.exception.ParameterNotUsedException;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.EmailData;
import gov.nysenate.inventory.model.LoginStatus;
import gov.nysenate.inventory.model.SimpleListItem;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;

import gov.nysenate.inventory.model.SimpleListItem;
import gov.nysenate.inventory.util.EmailValidator;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.util.Date;
import java.sql.Timestamp;

/*
 * @author senateuser
 */
public class TestGson {
    
    public static void main (String[] args) throws ParserConfigurationException {
        DbConnect db = new DbConnect();
        EmailData warningEmailData = new EmailData(db, "EMAILWARNING");
        try {
           /*if (testingMode) {
              emailData.setPreMessage(sbTestMsg.toString());
           }*/
           warningEmailData.put("EmailType", "FAKE EMAIL TYPE");
           warningEmailData.put("ReceiptURL", "http://www.google.com?test=1022");
           //warningEmailData.put("ReceiptURL", "http://www.google.com?test=1022" );
           warningEmailData.put("ProblemRecipients", "PROBLEM RECIPIENTS!!!");
           
           System.out.println (warningEmailData.getMessage());
           System.out.println (warningEmailData.getFormattedMessage());
            
         } catch (InvalidParameterException ex) {
            Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
         } catch (ParameterNotUsedException ex) {
            Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
         } catch (BlankMessageException ex) {
            Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
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
    };

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
    
    
}
