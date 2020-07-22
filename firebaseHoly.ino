
//This example shows how to read, store and update database using get, set, push and update functions.
//Required HTTPClientESP32Ex library to be installed  https://github.com/mobizt/HTTPClientESP32Ex

#include <WiFi.h>
#include <NTPClient.h>
#include <WiFiUdp.h>
#include "FirebaseESP32.h"
#include "ESP32_MailClient.h"

#define FIREBASE_HOST "iot-project-5e43f.firebaseio.com" //Do not include https:// in FIREBASE_HOST
#define FIREBASE_AUTH "HhY8xjpH6FY2kknHp7cFHGqUKZqbiwkNSwHjMFl6"
#define WIFI_SSID "UI-DeviceNet"
#define WIFI_PASSWORD "UI-DeviceNet"

WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP);
String formattedDate;
String dayStamp;
String timeStamp;

int numUsers = 4;
int authorizePin = 33;
int userPin = 23;
FirebaseData firebaseData;
bool authorized = false;
bool doorOpened = false;
bool newUser = false;
void sendCallback(SendStatus info);

void setup()
{
  Serial.begin(115200);
  pinMode(authorizePin, OUTPUT);
  pinMode(userPin, INPUT);
  Serial.println();
  Serial.println();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("Connecting to Wi-Fi");
  while (WiFi.status() != WL_CONNECTED)
  {
    Serial.print(".");
    delay(300);
  }
  Serial.println();
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());
  Serial.println();
  // Initialize a NTPClient to get time
  timeClient.begin();
  // Set offset time in seconds to adjust for your timezone, for example:
  // GMT +1 = 3600
  // GMT +8 = 28800
  // GMT -1 = -3600
  // GMT 0 = 0
  timeClient.setTimeOffset(-18000);
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  Firebase.reconnectWiFi(true);
}

void loop()
{ 
    String path = "/Authorization";
    //String jsonStr;
    if(Firebase.getBool(firebaseData, path)){
      //Serial.println("Got authorization");
      //Serial.println("PATH: " + firebaseData.dataPath());
      authorized = firebaseData.boolData();
    }else{
      Serial.println("FAILED Geting authorization");
      Serial.println("REASON: " + firebaseData.errorReason());
    }
    path = "/NewUser";
    if(Firebase.getBool(firebaseData, path)){
      //Serial.println("Got authorization");
      //Serial.println("PATH: " + firebaseData.dataPath());
      newUser = firebaseData.boolData();
    }else{
      Serial.println("FAILED Geting new User");
      Serial.println("REASON: " + firebaseData.errorReason());
    }
    if(newUser){
      numUsers++;
      newUser = false;
      Firebase.setBool(firebaseData, "/NewUser", false);
    }
    if(authorized){
      //Serial.println("authorized");
      digitalWrite(authorizePin, HIGH);
      doorOpened = digitalRead(userPin);
      if(doorOpened){
        Serial.println("DOOR OPENED");
        if(Firebase.getString(firebaseData, "/CurrUser")){
          //Serial.println("Got current user");
          //Serial.println("PATH: " + firebaseData.dataPath());
        }else{
          Serial.println("FAILED Geting current user");
          Serial.println("REASON: " + firebaseData.errorReason());
        }
        String currUser = firebaseData.stringData();
        for(int i = 1; i <= numUsers; i++){
          if(Firebase.getString(firebaseData, "/Users/User" + String(i))){
            //Serial.println("Got user" + String(i));
            //Serial.println("PATH: " + firebaseData.dataPath());
            if(currUser.equals(firebaseData.stringData())){
              if(Firebase.getInt(firebaseData, "/NumAccess/User" + String(i))){
                //Serial.println("Got user" + String(i) + " access");
                int currNum = firebaseData.intData();
                while(!timeClient.update()) {
                    Serial.println("time update failed!!!");
                }
                Serial.println("time updated successfully");
                formattedDate = timeClient.getFormattedDate();
                Serial.println(formattedDate);
                int day = timeClient.getDay();
                // Extract date
                int splitT = formattedDate.indexOf("T");
                dayStamp = formattedDate.substring(0, splitT);
                Serial.print("DATE: ");
                Serial.println(dayStamp);
                // Extract time
                timeStamp = formattedDate.substring(splitT+1, formattedDate.length()-1);
                Serial.print("HOUR: ");
                Serial.println(timeStamp);
                if (Firebase.pushString(firebaseData, "/TimeAccess/User" + String(i), formattedDate + "Day" + String(day)))
                {
                  //
                }
                if(Firebase.setInt(firebaseData, "/NumAccess/User" + String(i), currNum + 1)){
                  Serial.println("Set numAccess success");
                }else{
                  Serial.println("Failed setting numAccess");
                }
              }else{
                Serial.println("Failed getting user" + String(i) + " access");
              }
            }
          }else{
            Serial.println("FAILED Geting user" + String(i));
            Serial.println("REASON: " + firebaseData.errorReason());
          }
        }
        delay(4000);
      }else{
            Serial.println("firebase no movement");
      }
    }else{
      digitalWrite(authorizePin, LOW);
      Serial.println("unauthorized");
    }
    delay(500);
    //Firebase.end(firebaseData);
}
