# Cycle-Lanes
Team Project Submission for Group 19
Adam Regan x19401956
Conor FitzGerald x19444092
Stephen Costello x19111371

/*******************************************************************************************************************************************
This application allows you to interact with a map and find a route. The route will then show the user where the cycle lanes are along that route.

This application was made in android studio and is written in java

It uses the Google Maps API to display the map and find the routes 
https://developers.google.com/maps/documentation/android-sdk/overview

The data we used to find the cycle lanes is stored in GEOJSON format which is an extension of the JSON format and can be parsed in the same way
Link to dataset: https://data.smartdublin.ie/dataset/greater-dublin-area-cycle-lanes-nta

Using the GEOJSON Layer utility from the Google Maps API Utitlites, we can display the coordinates on the map from the geojson object
https://developers.google.com/maps/documentation/android-sdk/utility/geojson

To show bike lanes on the route, we compare the route to the bike lane coordinates and make a new json object where they match

/*******************************************************************************************************************************************

How to run the application:
1. pull project into desktop folder from git bash
2. Import project into android studio
3. turn on developer mode in phone by searching for build number in about phone and tapping build number 7 times
4. turn on usb debugging in developer options
5. Connect phone to pc, ensuring it comes up in android studio. If not use AVD manager troubleshooter to find device
6. run application and it will install on device.
7. note this application only works if user location is on so be sure to allow the app to get your location.
