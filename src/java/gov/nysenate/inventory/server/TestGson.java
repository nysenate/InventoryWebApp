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
import com.google.gson.reflect.TypeToken;
/**
 *
 * @author senateuser
 */
public class TestGson {
    
    public static void main (String[] args) {
        
        //Make Serial 

        Gson gson = new Gson();
        Type listOfTestObject = new TypeToken<List<PickupGroup>>(){}.getType();

        //Make Serial 
        List<PickupGroup> list = Collections.synchronizedList(new ArrayList<PickupGroup>() );
 //       list.add(new PickupGroup(102, "05/02/13 09:00:30AM", "HEITNER", "JEFFS", "AG42B", 20));
 //       list.add(new PickupGroup(102, "05/01/13 10:02:00AM", "PATIL", "THOMS", "AG42B", 30));
  //      System.out.println (gson.toJson(list));
        
        /*        Type locationInfoListType = (Type) new TypeToken<List<PickupGroup>>() {}.getType();

        Gson gson = new GsonBuilder()
        .registerTypeAdapter(PickupGroupTypeAdapter, new PickupGroupTypeAdapter())
        .create();        
        
         System. out.println("{\"nusenate\":\""+"1"+"\",\"nuxrefsn\":\""+"2"+"\",\"dtissue\":\""+"3"+",\"cdlocatto\":\""+"4"+"\",\"cdlocatto\":\""+"5"+"\",\"cdcategory\":\""+"6"+"\",\"decommodityf\":\""+"7"+"\"}");*/
    
/*         ArrayList<Employee> empList = new ArrayList<Employee>();
         Employee curEmp = new Employee(6221, "Brian", "Heitner" );
         String json = new Gson().toJson(curEmp);
         System.out.println (json);*/
    };

    
}
