//
// LightControl Class
// Handles the communication with the lighting rig. Has functions to
// turn an individual light on and off, initialize the lighting rig.
// Also has a function to read the mapping of the lights to the hardware
// port numbers.

#pragma once

#include "stdafx.h"
#include <string>
#include "SerialClass.h"

class LightControl {
public:
	bool initializeLights(std::string comPort);
	void turnOnLight(int n);
	void turnOffLight(int n);
	void resetAllLights();
	bool loadLightToControllerMapping();
	void flush();
	void close();
	bool isLightMappingLoaded() { return lightMappingLoaded; };
private:
	void readSerialOutput();
	void writeByte(char c);
	char getEncodedByte(int latch, int relay);
	static const int NUM_LIGHTS = 45;
	int relayMapping[NUM_LIGHTS+1];
	int latchMapping[NUM_LIGHTS+1];
	Serial* serialPort;
	bool lightMappingLoaded;
};
