#include <LiquidCrystal.h>

const int rs = 12, en = 11, d4 = A0, d5 = A1, d6 = A2, d7 = A3;
LiquidCrystal lcd(rs, en, d4, d5, d6, d7);

int sensorValue = 0; 
void setup(){
    lcd.begin(16, 2);
    Serial.begin(9600);
    for(int i=2; i<11; i++){
        pinMode(i,OUTPUT);
        digitalWrite(i, LOW);
    }
}

void loop(){
    char TestData;
        //Data Readings & print to serial    
        sensorValue = analogRead(A5);
        Serial.println(sensorValue);
        delay(500);
        
    if (Serial.available()){
        
        TestData = Serial.read();
        char w=0;
        //((char)30)Muhammed((char)30)
        if(TestData == 30){
            while(true){
                delay(10);
                w = (char)Serial.read();
                if(w==30)
                    break;
                lcd.print(w);
            }

        }

        else if(TestData == '|'){
            lcd.clear();
            lcd.print("CLEARING DATA");
            delay(500);
            lcd.clear();     
        }

        else{
            
            for(int i=2; i<11; i++){

                if(TestData==i+18){
                    digitalWrite(i,HIGH);
                    lcd.clear();
                    lcd.print("Led On");
                    delay(500);
                    lcd.clear();
                }

                else if(TestData==i+8){
                    digitalWrite(i,LOW);
                    lcd.clear();
                    lcd.print("Led Off");
                    delay(500);
                    lcd.clear();
                }
            }
            
        }


        

    }
}
