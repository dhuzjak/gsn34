package hr.fer.rasip.wrappers;

import java.util.HashMap;

//Contains mappings of labels to field names
public class FieldMappings{
   private static final HashMap<String, String> fieldMap = new HashMap<String, String>();
   static {
        fieldMap.put("ta", "Temperature");
        fieldMap.put("t", "Temperature");
        fieldMap.put("b", "Battery");
        fieldMap.put("ha", "Humidity");
        fieldMap.put("ll", "Illumination");
        fieldMap.put("te", "Soil temperature");
        fieldMap.put("ea", "Soil permittivity");
        fieldMap.put("mv", "Soil moisture");
    }

   public static String getMapping(String s){
   	return fieldMap.get(s);
   }
}
