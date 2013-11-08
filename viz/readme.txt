In order for this to work your computer will need to be set up as a webserver (Or some security settings will have to be very special to allow files open in browsers to open other files.. setting up a web server is simpler)

Move the entire contents of this folder into your webserver directory (/www/html or ~/Sites ... whatever)

When running the databridge code, all entries to the database are being replicated and stored in JSON code for the purposes of using this visualization. This JSON is located at {Databridge-git-home}/DataBridge/server/data/JSON/test.json. Copy this file to Xing-Similarity.json and it should display when you open dbvis.html from a browser (using http, not file, protocol).

