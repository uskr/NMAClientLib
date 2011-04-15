package com.usk.lib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;

/*
 * Author: Adriano Maia (adriano@usk.bz)
 * Version: 1.0.0 (4/10/2011)
 * Description:
 * 		This library provides easy access to Notify My Android public API. Methods implemented:
 * 			- notify
 * 			- verify
 */

public class NMAClientLib {
	private static String DEFAULT_URL = "https://nma.usk.bz";
	private static String NOTIFY_PATH = "/publicapi/notify";
	private static String VERIFY_PATH = "/publicapi/verify";
	private static String METHOD_POST = "POST";
	
	// Defining some contants
	public static final String UTF_8_ENCODING = "UTF-8";
	public static final String MESSAGE_KEY_API_KEY = "apikey";
	public static final String MESSAGE_KEY_APP = "application";
	public static final String MESSAGE_KEY_EVENT = "event";
	public static final String MESSAGE_KEY_DESC = "description";
	public static final String MESSAGE_KEY_PRIORITY = "priority";
	public static final String MESSAGE_KEY_DEV_KEY = "developerkey";
	
	private static String lastError = null;
	private static String encoding = null;
	
	/**
	 * Sends a notification using NMA public API.
	 * Positive return value means success. Negative return value means a problem. See possible values below.
	 * Return values:
	 * 		1 = Notification sent
	 * 	   -1 = app must have between 1 and 256 characters
	 * 	   -2 = event must have between 1 and 1000 characters
	 *     -3 = description must have between 1 and 10000 characters
	 *     -4 = priority must be one of these values: -2, -1, 0, 1 ,2
	 *     -5 = One or more API keys are of an invalid format. Must be a 48 characters hexadecimal string
	 *     -6 = Developer key is of an invalid format
	 *     -7 = Exception. Use getLastError() for some details.
	 *     -8 = Server returned an error. Check getLastError() for more details.
	 *     -9 = Problem sending the message. Check getLastError() for the message returned.
	 * 
	 * param app Application name (Up to 256 characters)
	 * param event Short description of the even or a subject (Up to 1000 characters)
	 * param description Long description or message body (Up to 10000 characters)
	 * param priority Priority level of the message. -2, -1, 0, 1, 2
	 * param apiKey One or more 48 bytes long API key, separated by commas.
	 * param devKey Developer key.
	 * return result
	 */
	public static int notify(String app, String event, String description, int priority, String apiKey, String devKey) {
		
		// First some parameter validation. Those tests are done again server-side, but there is no need to submit it if we know it's wrong.
		if ( (app.length() == 0) || (app.length() > 256) ) return -1;
		if ( (event.length() == 0) || (event.length() > 1000) ) return -2;
		if ( (description.length() == 0) || (description.length() > 10000) ) return -3;
		if ( (priority < -2) || (priority > 2) ) return -4; 
		if ( apiKey.indexOf(',') == -1 ) {
			if ( apiKey.length() != 48 ) return -5;
		} else {
			String apiKeysArray[] = apiKey.split(",");
			for(int i=0; i<apiKeysArray.length; i++) if ( apiKeysArray[i].length()!=48 ) return -5;
		}
		if ( devKey != null ) if ( devKey.length() != 48 ) return -6;
		
		// Setup objects to submit the data
		URL url = null;
		HttpURLConnection connection = null;
		encoding = UTF_8_ENCODING;
		StringBuilder data = new StringBuilder();
		
		try {
			url = new URL(DEFAULT_URL + NOTIFY_PATH);
		} catch (MalformedURLException e) {
			lastError = e.toString();
			return -7;
		}
		
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(METHOD_POST);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Setup the POST data
			try{
				addEncodedParameter(data, MESSAGE_KEY_API_KEY, apiKey);
				addEncodedParameter(data, MESSAGE_KEY_APP, app);
				addEncodedParameter(data, MESSAGE_KEY_EVENT, event);
				addEncodedParameter(data, MESSAGE_KEY_DESC, description);
				addEncodedParameter(data, MESSAGE_KEY_PRIORITY, Integer.toString(priority));
				if ( devKey != null ) addEncodedParameter(data, MESSAGE_KEY_DEV_KEY, devKey);
			} catch (IOException e) {
				lastError = e.toString();
				return -7;
			}
			
			// Buffers and Writers to send the data
			OutputStreamWriter writer;              
			writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(data.toString());
			writer.flush();
			writer.close();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				
				StringBuffer response = new StringBuffer();
				String line = null;
				while ((line = in.readLine()) != null) {
					response.append(line);
				}
				
				boolean msgSent = false;
				
				String resultStr = response.toString();
    	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	        DocumentBuilder db = factory.newDocumentBuilder();
    	        InputSource inStream = new InputSource();
    	        inStream.setCharacterStream(new StringReader(resultStr));
    	        Document doc = db.parse(inStream);  
    	        
    	        
    	        Element root = doc.getDocumentElement();
    	        
    	        if(root.getTagName().equals("nma")) {
    	        	Node item = root.getFirstChild();
    	        	String childName = item.getNodeName();
    	        	if (childName.equals("success")) {
    	        		msgSent = true;
    	        	} else {
    	        		lastError = item.getFirstChild().getNodeValue();
    	        	}
    	        }
    	        
    	        return (msgSent)?1:-9;
			} else {
				lastError = "There was a problem contacting NMA Servers. HTTP Response code different than 200(OK). Try again or contact support@nma.bz if it persists.";
				return -8;
			}

		} catch (Exception e) {
			lastError = e.toString();
			return -7;
		}
		
	}
	
	/**
	 * 
	 * @param app
	 * @param event
	 * @param description
	 * @param priority
	 * @param apiKey
	 * @return
	 */
	public static int notify(String app, String event, String description, int priority, String apiKey) {
		return notify(app, event, description, priority, apiKey, null);
	}

	/**
	 * 
	 * @param app
	 * @param event
	 * @param description
	 * @param apiKey
	 * @return
	 */
	public static int notify(String app, String event, String description, String apiKey) {
		return notify(app, event, description, 0, apiKey, null);
	}
	
	/**
	 * Sends a notification using NMA public API.
	 * Positive return value means success. Negative return value means a problem. See possible values below.
	 * Return values:
	 * 		1 = Notification sent
	 *     -1 = API keys is not valid. Must be a 48bytes hexadecimal string.
	 *     -2 = Developer key is of an invalid format
	 *     -3 = Exception. Use getLastError() for some details.
	 *     -4 = Server returned an error. Check getLastError() for more details.
	 *     -5 = Problem sending the message. Check getLastError() for the message returned.
	 * 
	 * param apiKey Only one 48 bytes long API key.
	 * param devKey Developer key.
	 * return result
	 */
	public static int verify(String apiKey, String devKey) {
		// First some parameter validation. Those tests are done again server-side, but there is no need to submit it if we know it's wrong.
		if ( apiKey.length() != 48 ) return -1;
		if ( devKey != null ) if ( devKey.length() != 48 ) return -2;
		
		// Setup objects to submit the data
		URL url = null;
		HttpURLConnection connection = null;
		encoding = UTF_8_ENCODING;
		StringBuilder data = new StringBuilder();
		
		try {
			url = new URL(DEFAULT_URL + VERIFY_PATH);
		} catch (MalformedURLException e) {
			lastError = e.toString();
			return -5;
		}
		
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod(METHOD_POST);
			connection.setUseCaches(false);
			connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			
			// Setup the POST data
			try{
				addEncodedParameter(data, MESSAGE_KEY_API_KEY, apiKey);
				if ( devKey != null ) addEncodedParameter(data, MESSAGE_KEY_DEV_KEY, devKey);
			} catch (IOException e) {
				lastError = e.toString();
				return -5;
			}
			
			// Buffers and Writers to send the data
			OutputStreamWriter writer;              
			writer = new OutputStreamWriter(connection.getOutputStream());

			writer.write(data.toString());
			writer.flush();
			writer.close();
			BufferedReader in = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));
			
			if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
				
				StringBuffer response = new StringBuffer();
				String line = null;
				while ((line = in.readLine()) != null) {
					response.append(line);
				}
				
				boolean msgSent = false;
				
				String resultStr = response.toString();
    	        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    	        DocumentBuilder db = factory.newDocumentBuilder();
    	        InputSource inStream = new InputSource();
    	        inStream.setCharacterStream(new StringReader(resultStr));
    	        Document doc = db.parse(inStream);  
    	        
    	        
    	        Element root = doc.getDocumentElement();
    	        
    	        if(root.getTagName().equals("nma")) {
    	        	Node item = root.getFirstChild();
    	        	String childName = item.getNodeName();
    	        	if (childName.equals("success")) {
    	        		msgSent = true;
    	        	} else {
    	        		lastError = item.getFirstChild().getNodeValue();
    	        	}
    	        }
    	        
    	        return (msgSent)?1:-9;
			} else {
				lastError = "There was a problem contacting NMA Servers. HTTP Response code different than 200(OK). Try again or contact support@nma.bz if it persists.";
				return -8;
			}

		} catch (Exception e) {
			lastError = e.toString();
			return -7;
		}
	}
	
	/**
	 * 
	 * @param apiKey
	 * @return
	 */
	public static int verify(String apiKey) {
		return verify(apiKey, null);
	}	
	
	/**
	 * Dynamically adds a url-form-encoded key/value to a StringBuilder 
	 * @param sb StringBuilder buffer used to build the final url-form-encoded data
	 * @param name Key name
	 * @param value Value
	 * @throws IOException 
	 */
	private static void addEncodedParameter(StringBuilder sb, String name, String value) throws IOException 
	{
		if (sb.length() > 0) {
			sb.append("&");
		}
		try {
			sb.append(URLEncoder.encode(name, encoding));
			sb.append("=");
			if(value==null)
				throw new IOException("ERROR: " + name + " is null");
			else
				sb.append(URLEncoder.encode(value, encoding));
		} catch (UnsupportedEncodingException e) {
			// Exception handling
		}
	}
	
	public static String getLastError() {
		return lastError;
	}
	
	// Test case. Not meant to be used like a full featured command-line program.
	public static void main(String[] args) {
		// Syntax
		if (args.length < 4) {
			System.out.println("Usage :");
			System.out.println("java -jar NMAClientLib.jar <apikey> <application_name> <event> <description> [priority] [devkey]");
			return;
		}
		// Load parameters
		String lApiKey = args[0];
		String lAppName = args[1];
		String lEvent = args[2];
		String lDesc = args[3];
		int lPriority = 0;
		String devKey = null;
		if (args.length > 4) {
			try {
				lPriority = Integer.parseInt(args[4]);
			} catch (Exception e) {
				System.out.println("Parameter 'priority' must be an Integer.");
			}
			if (args.length > 5) devKey = args[5];
		}

		// lApiKey could be a list of comma separated keys, but notify only accepts one key per call
		if ( lApiKey.indexOf(',') == -1 ) {
			if(NMAClientLib.verify(lApiKey) == 1) {
				System.out.println("Key [" + lApiKey + "] is valid!");
			} else {
				System.out.println(NMAClientLib.getLastError());
			}
		} else {
			String apiKeysArray[] = lApiKey.split(",");
			for(int i=0; i<apiKeysArray.length; i++) { 
				if(NMAClientLib.verify(apiKeysArray[i]) == 0) {
					System.out.println("Key [" + apiKeysArray[i] + "] is valid!");
				} else {
					System.out.println(NMAClientLib.getLastError());
				}
			}
		}
		
		if (NMAClientLib.notify(lAppName, lEvent, lDesc, lPriority, lApiKey, devKey) == 1) {
			System.out.println("Message sent!");
		} else {
			System.out.println(NMAClientLib.getLastError());
		}
		
	}
	
}

