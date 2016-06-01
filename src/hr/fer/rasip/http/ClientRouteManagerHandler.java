package hr.fer.rasip.http;

import java.io.FileReader;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import gsn.http.RequestHandler;

public class ClientRouteManagerHandler implements RequestHandler {

	@Override
	public boolean isValid(HttpServletRequest request,
			HttpServletResponse response) throws IOException {
		return true;
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response)
			throws IOException {
		
		String content  = ClientRouteManagerHandler.readFile("conf/client-routes.json");
		
		response.setHeader("Cache-Control", "no-store");
	    response.setDateHeader("Expires", 0);
	    response.setHeader("Pragma", "no-cache");
	    response.getWriter().write(content);
   		return; 
	}
	
	
	static String readFile(String path) 
	{
		JSONParser parser = new JSONParser();
		JSONArray jsonObject=null;
	    try {

	       jsonObject = (JSONArray) parser.parse(new FileReader(path));

	    }catch(Exception e){}
	  
	    return jsonObject.toJSONString();
	        /*System.out.println(city);
			  byte[] encoded = Files.readAllBytes(Paths.get(path));
			  return new String(encoded, encoding);*/
	}   
}
