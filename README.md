Notify My Android (Java Library)
================================

	Notify My Android (http://nma.usk.bz)
	NMAClientLib - Java Library to access NMA public API
	Last Stable Version: 1.0.0
	For developers: http://github.com/uskr/NMAClientLib
	Author: Adriano Maia (adriano@usk.bz)

	This library provides an easy to use API for Java that interfaces with Notify My Android public API.

### Methods implemented:

	- notify() : Send a notification
	- verify() : Checks if an API key is valid

#### notify()

	public static int notify(String app, String event, String description, int priority, String apiKey, String devKey)
	 * param app Application name (Up to 256 characters)
	 * param event Short description of the even or a subject (Up to 1000 characters)
	 * param description Long description or message body (Up to 10000 characters)
	 * param priority Priority level of the message. -2, -1, 0, 1, 2
	 * param apiKey One or more 48 bytes long API key, separated by commas.
	 * param devKey Developer key.
	 * return Return values:
	 *		 1 = Notification sent
	 *		-1 = app must have between 1 and 256 characters
	 *		-2 = event must have between 1 and 1000 characters
	 *		-3 = description must have between 1 and 10000 characters
	 *		-4 = priority must be one of these values: -2, -1, 0, 1 ,2
	 *		-5 = One or more API keys are of an invalid format. Must be a 48 characters hexadecimal string
	 *		-6 = Developer key is of an invalid format
	 *		-7 = Exception. Use getLastError() for some details.
	 *		-8 = Server returned an error. Check getLastError() for more details.
	 *		-9 = Problem sending the message. Check getLastError() for the message returned.

##### Other signatures:
public static int notify(String app, String event, String description, int priority, String apiKey)	 
public static int notify(String app, String event, String description, String apiKey)

#### verify()

	public static int verify(String apiKey, String devKey)
	 * param apiKey Only one 48 bytes long API key.
	 * param devKey Developer key.
	 * return Return values:
	 *		 1 = Key is valid
	 *		-1 = API keys is not valid. Must be a 48bytes hexadecimal string.
	 *		-2 = Developer key is of an invalid format
	 *		-3 = Exception. Use getLastError() for some details.
	 *		-4 = Server returned an error. Check getLastError() for more details.
	 *		-5 = Problem sending the message. Check getLastError() for the message returned.
	
##### Other signatures:
public static int verify(String apiKey)
	 
Example on how to use the library
---------------------------------

import com.usk.lib.NMAClientLib;

public class MyApp {

	public static void main(String[] args) {
		// Syntax
		if (args.length < 4) {
			System.out.println("Usage :");
			System.out.println("java MyApp <apikey> <application_name> <event> <description> [priority] [devkey]");
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
		
		// Sending a notification
		if (NMAClientLib.notify(lAppName, lEvent, lDesc, lPriority, lApiKey, devKey) == 1) {
			System.out.println("Message sent!");
		} else {
			System.out.println(NMAClientLib.getLastError());
		}
	}
}

License (MIT)
-------------

    Copyright (c) 2010-2011, Adriano Maia.

    Permission is hereby granted, free of charge, to any person obtaining a copy
    of this software and associated documentation files (the "Software"), to deal
    in the Software without restriction, including without limitation the rights
    to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
    copies of the Software, and to permit persons to whom the Software is
    furnished to do so, subject to the following conditions:

    The above copyright notice and this permission notice shall be included in
    all copies or substantial portions of the Software.

    THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
    IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
    FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
    AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
    LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
    OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
    THE SOFTWARE.