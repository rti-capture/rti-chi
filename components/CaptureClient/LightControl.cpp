
#include "stdafx.h"
//#include "capture.h"
#include "LightControl.h"
#include <string>
#include <fstream>
#include <iostream>
#include "SerialClass.h"

using namespace std;

bool LightControl::initializeLights(string comPort) {
	string paddedComPort = "\\\\.\\" + comPort;
	char cstr[255];
	strcpy(cstr,paddedComPort.c_str());
	serialPort = new Serial(cstr); 

	return serialPort->IsConnected();
}

void LightControl::writeByte(char outByte) {
	char outBuffer[1];
	outBuffer[0] = outByte;
	cout << "Writing byte : " << (int)outByte << "\n";
	serialPort->WriteData(outBuffer, 1);
}

char LightControl::getEncodedByte(int latch, int relay) {
	return latch * 8 + relay;
}

void LightControl::turnOnLight(int n) {
	if (serialPort->IsConnected()) {
		readSerialOutput();
		char outByte = getEncodedByte(latchMapping[n],relayMapping[n]);
		cout << "light on: " << n << " relay: " << relayMapping[n] << " latch: " << latchMapping[n] << endl;
		writeByte(outByte);
	}
}

void LightControl::turnOffLight(int n){
	if (serialPort->IsConnected()) {
		readSerialOutput();
		char outByte = getEncodedByte(0,relayMapping[n]); // latch == 0 for turning off all lights in that relay
		cout << "light off: " << n << " relay: " << relayMapping[n] << " latch: " << 0 << endl;
		writeByte(outByte);
	}
}


void LightControl::resetAllLights(){
	if (serialPort->IsConnected()) {
		readSerialOutput();
		writeByte(0); // Just send 0 to turn off all lights across all the relays
	}
}

bool LightControl::loadLightToControllerMapping() {
  string line;
  lightMappingLoaded = false;
  ifstream infile ("LightMapping.txt");
  if (infile.is_open()) {
    while ( infile.good() ) {
      int light, relay, latch;
      getline (infile,line);
	  sscanf(line.c_str(), "%i:%i,%i", &light, &relay, &latch);
	  if (light<1 || light>NUM_LIGHTS) {
		  cout << "Error in LightMapping file" << endl;
		  return false;
	  } else {
		  relayMapping[light]=relay;
		  latchMapping[light]=latch;
	  }
    }
    infile.close();
	lightMappingLoaded = true;
	return true;
  }
  else {
	  cout << "Unable to open LightMapping file" << endl;
	  return false;
  }
}

void LightControl::flush() {
	if (serialPort->IsConnected())	{
		readSerialOutput();
	}
}

void LightControl::readSerialOutput() {
	char incomingData[256] = "";			// don't forget to pre-allocate memory
	//printf("%s\n",incomingData);
	int dataLength = 256;
	int readResult = 0;

	readResult = serialPort->ReadData(incomingData,dataLength);
	cout << "Bytes read: (-1 means no data available) " << readResult << "\n";
    cout << incomingData << "\n";
}

void LightControl::close() {
    delete serialPort;
}