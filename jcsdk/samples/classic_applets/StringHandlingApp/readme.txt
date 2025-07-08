/* Copyright (c) 1998, 2020, Oracle and/or its affiliates. All rights reserved. */

How to run this sample:
1. Start cref with -o stringapp
2. Go to the lib dir; run ant all
3. Start cref with -i stringapp -o stringapp
4. Go to the liblocal dir; run ant all
5. Start cref with -i stringapp
6. Go to the applet dir; run ant all

Output is directed into the file default.output.
The expected output for the applet is in test.expected.output.




General Sample Description:
The StringHandlingApp sample showcases the use of
javacardx.annotations.StringDef and javacardx.annotations.StringPool
annotations as well as the string utility methods defined in javacardx.framework.string.StringUtil. This sample is composed of two Java Card Classic libraries and two Java Card Classic applets. The libraries use string annotations to define string constants. The applets use these same annotations to define their own set of string constants as well as import string constants from the libraries.  This sample specifically demonstrates how an applet can use string constants that it imports from a library that itself imports constants from another library.  It also exemplifies the usage of two applets, each in a different context, that can both access the same string constant from a library.  





StringHandlingApp Description:
This applet focuses on the use of javacardx.annotations.StringDef and javacardx.annotations.StringPool annotations. It defines its own set of string constants and also imports string constants from the main library, StringHandlingLib. It also helps demonstrate the use of a two applets in different contexts both importing a single string constant from a common library.  It imports and uses one of the same string constants from StringHandlingLib as StringUtilApp.  

StringHandlingApp imports a string constant from the StringHandlingLib, and also defines some string constants for itself. When the StringHandlingApp is selected, in the process method, it uses each of the test methods defined in the StringHandlingLib library.  If the results from each of the tested methods match with the expected string constants that have already been defined in the app, a response message is created containing a "Hello World!" message and a copy of the incoming message appended to the end.  In the case that the tested methods do not produce the expected outcome, a message containing the header bytes from the buffer and a copy of the incoming message appended to the end is sent instead.  


StringHandlingLib Description:
The main library, StringHandlingLib, is used for both applets.  It contains string constants for a default location, hello greeting, and error message.  It also contains examples of substring, startsWith, and endsWith methods showing how to implement them using the offsetByCodePoints method from StringUtil. Included in this library are also test methods for each of these aforementioned methods. 


StringHandlingLibLocal Description:
The local library, StringHandlingLibLocal, is an example of a library that contains some location specific string constants.  For example, as used in StringUtilApp, it controls the formatting of input and output strings including delimiters for arguments and command string terminators.  It also provides the location to be used in the welcome message, and a default error message. In the case of the error message, it shows how a library can use string constants from another library, and how an applet can in turn use the constants from either library.    


StringUtilApp Description:
This sample applet explores the usage of the javacardx.framework.string.StringUtil class.  It combines the use of string annotations, two libraries with string constants, and various methods from the StringUtil class.  It imports string constants from a library that itself imports constants from another library.  In addition, it also helps demonstrate the use of a two applets in different contexts both importing a single string constant from a common library.  It imports and uses one of the same string constants from StringHandlingLib as StringHandlingApp.

The process method handles APDUs containing a command string composed of a command type and  optional arguments. It then sends a response APDU based on the command string it received.  Contained in this applet's string pool are string constants defining various stored items. As an example, this applet uses default contacts with a name and an e-mail address.  It also contains default settings and the values that each setting can take on.  When the applet receives a command for contacts, it sends a response message with the contact name, corresponding to the number in the command argument, along with the associated e-mail address value for that name.  When a settings command is received with only one argument, the setting corresponding to the number in the command argument, along with the default value for that setting, is sent in the response message.  If the setting command has two arguments, the second argument signifies the state that the setting should be placed in.  The applet then responds with the name of the setting and the value that was selected in the command.  

Command and response strings are represented as a series of bytes following utf-8 representation of strings.  To demonstrate the applet's functionality, string versions of the byte sequences are used in the examples below.  

Command String Requirements:
- Command strings must be terminated by a period "."
- Command types must be separated from arguments with a space " "
- Command types are case insensitive
- Command types are: "Welcome" "Contacts" "Settings"
- Arguments for "Contacts" or "Settings" are: "1" or "2"
- An optional second argument of "1" or "2" can be used for "Settings"
- Example valid command strings are:
	1. "Welcome."
	2. "Settings 1."
	3. "contacts 2."
	4. "Settings 2 2."

Response String Requirements:
- If the command string is invalid, a default response string will be sent.
- If the welcome command is received, a welcome response string will be sent.
- For either of the command types with arguments, a string will be sent corresponding to the argument(s) and command type received.  This string is composed of a name and value, separated by a comma ","
- Example response strings corresponding to the command strings above are:
	1. "Hello California!"
	2. "AutoCorrect, Off"
	3. "John Adams, John.Adams@123.com"
	4. "Wifi, On"


