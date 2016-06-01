package hr.fer.rasip.http;

import gsn.http.RequestHandler;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class NotificationControllerServlet extends HttpServlet {
	
	private static final long serialVersionUID = 1L;

	public void doGet (HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		response.setContentType ( "text/xml" );
		response.setHeader ( "Expires" , "Sat, 6 May 1995 12:00:00 GMT" );
		response.setHeader ( "Cache-Control" , "no-store, no-cache, must-revalidate" );
		response.addHeader ( "Cache-Control" , "post-check=0, pre-check=0" );
		response.setHeader ( "Pragma" , "no-cache" );

		RequestHandler handler;
		RequestHandler mqtthandler;
      
		handler = new NotificationControllerHandler();
		if (handler.isValid(request, response)) handler.handle(request, response);

		
		mqtthandler = new MQTTNotificationControllerHandler();
		if (mqtthandler.isValid(request, response)) mqtthandler.handle(request, response);
   }

   public void doPost (HttpServletRequest request, HttpServletResponse res) throws ServletException, IOException {
      doGet (request, res);
   }
}