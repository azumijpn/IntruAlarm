#include <WiFi.h>
#include "FirebaseESP32.h"
#include "ESP32_MailClient.h"
#define WIFI_SSID "UI-DeviceNet"
#define WIFI_PASSWORD "UI-DeviceNet"

SMTPData smtpData;
FirebaseData firebaseData;
void sendCallback(SendStatus info);

int PIR_Pin = 32;
int determineAuth_Pin = 33;
int determineUser_Pin = 23;
int buzzerPin = 13;
bool bodyPresent = false;
bool authorized = false;


// this function is used to send an email to all registered users
void sendEmail(String message){
    Serial.println("------------------------------------");
    Serial.println("Send Email...");

    //Set the Email host, port, account and password
    smtpData.setLogin("smtp.gmail.com", 465, "jyhan043@gmail.com", "EdwardSalvatore4");

    //Set the sender name and Email
    smtpData.setSender("ESP32", "jyhan043@gmail.com");

    //Set Email priority or importance High, Normal, Low or 1 to 5 (1 is highest)
    smtpData.setPriority("High");

    //Set the subject
    smtpData.setSubject(message);

    //Set the message - normal text or html format
    smtpData.setMessage(message, true);

    //Add recipients, can add more than one recipient
    smtpData.addRecipient("lvxinrunyi@gmail.com");

    //Add attachment file (backup file) from SD card
    // smtpData.addAttachFile(firebaseData.getBackupFilename());

    smtpData.setSendCallback(sendCallback);

    //Start sending Email, can be set callback function to track the status
    if (!MailClient.sendMail(firebaseData.http, smtpData))
        Serial.println("Error sending Email, " + MailClient.smtpErrorReason());

    //Clear all data from Email object to free memory
    smtpData.empty();
    delay(1000);
    Serial.println();
}

void setup() {
    // put your setup code here, to run once:
    Serial.begin(115200);
    pinMode(PIR_Pin, INPUT);
    pinMode(determineAuth_Pin, INPUT);
    pinMode(determineUser_Pin, OUTPUT);
    pinMode(buzzerPin, OUTPUT);
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
}

void loop() {
    // put your main code here, to run repeatedly:
    authorized = digitalRead(determineAuth_Pin);
    bodyPresent = digitalRead(PIR_Pin);

    if(bodyPresent){
        if(authorized){
          digitalWrite(determineUser_Pin, HIGH);
          Serial.println("authorized");
          digitalWrite(buzzerPin, HIGH);
        }else{
          digitalWrite(determineUser_Pin, LOW);
          digitalWrite(buzzerPin, LOW);
          Serial.println("Send alarm");
          sendEmail("Intruder Detected");
          delay(2500);
        }
    }else{
        digitalWrite(determineUser_Pin, LOW);
        digitalWrite(buzzerPin, HIGH);
        Serial.println("sensor no movement");
    }
    delay(300);
}


void sendCallback(SendStatus msg)
{
  //Print the current status
  Serial.println(msg.info());

}
