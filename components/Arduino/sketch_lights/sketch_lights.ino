/*
 * Example sketch to control the RELAY8 8-Channel Relay
 * Driver Shield. Open serial console at 38400bps, and
 * send value "0" to reset all relays, or a channel number
 * to activate that relay. Requires Arduino IDE v1.0.1
 * or newer.
 */

#include "Wire.h"

#define I2C_ADDR_1  0x20  // Jumper set address - Relay Board 1
#define I2C_ADDR_2  0x21  // Jumper set address - Relay Board 2
#define I2C_ADDR_3  0x22  // Jumper set address - Relay Board 3
#define I2C_ADDR_4  0x23  // Jumper set address - Relay Board 4
#define I2C_ADDR_5  0x24  // Jumper set address - Relay Board 5
#define I2C_ADDR_6  0x25  // Jumper set address - Relay Board 6
#define I2C_ADDR_7  0x26  // Jumper set address - Relay Board 7

void setup()
{
  Serial.begin( 38400 );
  Serial.println("Light demo starting up");

  Wire.begin(); // Wake up I2C bus

  resetAllRelays();
}

void loop() {
  // read the incoming value from serial port:
  if (Serial.available() > 0) {
    int inByte = Serial.read();

    // the input is sent as a single byte, which encodes the relayIndex and the latchIndex both
    int latchIndex = inByte / 8;
    int relayIndex = inByte % 8;
    
    if (latchIndex >= 0 && latchIndex <= 8 && relayIndex >= 1 && relayIndex <= 7) {
      int latchValue = 0;
      if (latchIndex > 0) {
        latchValue = (1<<(latchIndex-1)); // get power(2, latchIndex-1)
      }
      Serial.println("Input:" + String(inByte) + " Relay:"+String(relayIndex) + " Latch: "+String(latchIndex) + " LatchValue:"+String(latchValue));
      switch (relayIndex) {
        case 1:
          sendValue(I2C_ADDR_1,latchValue);
          break;
        case 2:
          sendValue(I2C_ADDR_2,latchValue);
          break;
        case 3:
          sendValue(I2C_ADDR_3,latchValue);
          break;
        case 4:
          sendValue(I2C_ADDR_4,latchValue);
          break;
        case 5:
          sendValue(I2C_ADDR_5,latchValue);
          break;
        case 6:
          sendValue(I2C_ADDR_6,latchValue);
          break;
        case 7:
          sendValue(I2C_ADDR_7,latchValue);
          break;
      }
    } else if (inByte == 0) {
      resetAllRelays();
      Serial.println("All relays reset!"); 
    } else {
      Serial.println("Input out of range "+String(inByte)); 
    }
  }
}

// Triggers a given relay board and latch value
void sendValue(int relayValue, int latchValue)
{
  Wire.beginTransmission(relayValue);
  Wire.write(0x12);        // Select GPIOA
  Wire.write(latchValue);  // Send value to bank A
  Wire.endTransmission();
}

void resetAllRelays() {
  // Set Relay Board #1 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_1);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
  // Set Relay Board #2 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_2);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
  // Set Relay Board #3 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_3);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
  // Set Relay Board #4 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_4);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
  // Set Relay Board #5 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_5);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
  // Set Relay Board #6 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_6);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
  
    // Set Relay Board #7 I/O bank A to outputs
  Wire.beginTransmission(I2C_ADDR_7);
  Wire.write(0x00); // IODIRA register
  Wire.write(0x00); // Set all of bank A to outputs
  Wire.endTransmission();
}
