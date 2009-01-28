// Copyright Cultural Heritage Imaging 2008

#pragma once

#include <iostream>
#include <fstream>
#include "CameraControl.h"
#include "StatusBuffer.h"
#include <string>

const int MAX_LIGHTS = 200;

class CaptureSequence
{
public:
	CaptureSequence(void);
public:
	~CaptureSequence(void);
	CameraControl * cameracontrol;
	std::string strSavePath;
	std::string strRootPrefix;
	std::string strPort;
	std::string strFullFolder;
	int numSequenceSize;
	int numLights;
	int numLightsDelay;

	StatusBuffer * status;

	bool cameraDryRun;
	bool lightsDryRun;
	bool cancelled;
	bool paused;

	int startSequence();
	bool loadSettings(); 

	std::string strPatchPanel;

	// multiview capture, turntable related variables
	bool turntableDryRun;
	bool useTurntable;

	std::string strTurntablePort;
	int numViewpoints;
	float numViewSeperation;
	int numRotationDelay;


private:
	int photosTaken;

	std::string strViewPrefix;

	int  PatchPanel[MAX_LIGHTS];
	void threadProc(void* lParam);
	std::ofstream logfile;

	void SingleRelease(int numShot);
	void ToggleOneFiber( int  numFiber, BOOL on ); 
	void captureTurntableSequence(void);
};
