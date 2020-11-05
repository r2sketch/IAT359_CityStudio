# IAT359_CityStudio

Introduction Activity: introduction about this app and GI
Interactive Map Activity: displays GI nodes
  - GPS function to show where you are relative to the interactive map
GI Location Nodes (tooltip pop out on the map(?)): information about the location & volunteer opportunities)
////////////////////////////////////////////////////
Learning Hub Activity: educational material
Specific GI Types Activity: learn about a specific GI
  - Information for specific GI types implemented in a SQLite database where users can search using keywords (using RecyclerView)
  - Ability for users to insert GI type information into SQLite database and delete their entries (but not the pre-existing entries(?))
Learn More Link: external link to resources/sites
Volunteer Activity: information about volunteering
////////////////////////////////////////////////////
Settings Activity: change day/night mode
  - Use SharedPreferences to store whether the user wants day or night mode
  - Use ambient light sensor to detect whether it's bright or dark, when the app is first opened or detect significant change, pop up prompt to ask the user if they want to use day or night mode accordingly 
  - These settings must be in place in all the activities
Contact activity: send reports about GI, inquire for volunteer activity, general feedback
  - Submit button sends the information to (the) email
  - Option to access the device's camera / gallery to attach a picture
