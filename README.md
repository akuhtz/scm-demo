# scm-demo

Clone git repo. 

# Import project in Eclipse

In Eclipse:
 * File > Import... > Maven > Existing Maven Projects
 * Select the cloned repo: Browse... > Ok 
 * The project is listed under projects > Select project > Finish
 * The project is opened in Eclipse ... this might take some time because some dependencies are downloaded.
 
Note: Eclipse has built-in maven support, so this should work out of the box.
 
# Run

Navigate to ```src/main/java``` > open ```org.bidib.scmdemo``` package > ByteStreamIntegrationApp is the main app

You can specify the port to open as argument in the debug or run configurations on the argument tab.
