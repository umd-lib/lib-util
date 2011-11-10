=== lib-util ===

Here is a short instruction of the lib-util project.  
User should follow the instruction below before running the test. An error may occur otherwise.

== Instruction ==

1. In order to run the SMBConnection.java, you should first have a file named "connection.properties"
The file can be either obtained from DITS, or written by yourself.

--------------------
Note: 

To create your own connection.properties file, you need to have following attributes in your file:
* username : your login username for the server
* password : your login password
* domain   : can be the name of workgroup
* sharepath: url of sharepath in the server, no scheme needed.

---------------------

2. When connection.properties file is ready, create a "ref" folder under lib-util/
3. put connection.properties in  lib-util/ref/

4. Setting up finished, now you can run the test.