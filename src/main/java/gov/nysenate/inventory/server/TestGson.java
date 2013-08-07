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
import gov.nysenate.inventory.model.Commodity;
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
import org.xml.sax.InputSource;
/**
 *
 * @author senateuser
 */
public class TestGson {
    
    public static void main (String[] args) throws ParserConfigurationException {

        System.out.println (convertTime((long)((3*60*60*1000)+(32*60*1000)+(18*1000)+383)));
        System.out.println (convertTime((long)1533));
        System.out.println (convertTime((long)434));
        
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
