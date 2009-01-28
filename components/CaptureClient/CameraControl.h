// Copyright Cultural Heritage Imaging 2008

// CameraControl Class 
// Handles the connection with a single camera. Maintains the camera settings (i.e. ISO, Aperture, Shutter, ImageQuality)
//
// The normal usage scenario would start with a call to populateCameraList() to get a list of the plugged in cameras,
// followed by a call to connectCamera() for a selected camera.
// Then depending on the user choices, any of the get/set functions for camera settings (eg. getTv(), setTv())
// or the takePicture() function can be called. 
// takePicture() releases the shutter, eventually (asynchronously) this will trigger the event which leads downloadImage()
// being called.
// Finally, disconnectCamera() can be called to finish the camera session.


#pragma once
#include "stdafx.h"
#include <EDSDK.h>
#include <string>
#include <map>
#include <queue>

// Capture Client Version Info
#define CONTROLLER_VERSION_INFO "1.0.0"

class CameraControl
{
public:
	CameraControl(void);
	~CameraControl(void);
	int populateCameraList(void);
	void connectCamera(int index);
	void disconnectCamera(void);
	void takePicture(std::string strFileName);

	int getTv();
	std::string getTvDesc();
	void setTv(int value);

	int getAv();
	std::string getAvDesc();
	void setAv(int value);

	int getISO();
	std::string getISODesc();
	void setISO(int value);

	int getImageQuality();
	std::string getImageQualityDesc();
	void setImageQuality(int value);

	static EdsError EDSCALLBACK handleObjectEvent( EdsObjectEvent event, EdsBaseRef object, EdsVoid * context); 

public: 
	std::string getCameraList(int index) {return strCameraList[index];};
	int getCameraCount() {
		return numCameraList;
	};
	bool isShutterOpen() {return shutteropen;};
	bool isCameraBusy() {return camerabusy;};
	bool isLegacy() {return legacy;};

	EdsCameraRef camera;
	std::string strSaveNextFile;
	std::map<int, std::string> briefImageQualityMap;  // Commonly used subset of image quality settings, for the control interface

private:
	bool isSDKLoaded;
	std::string * strCameraList;
	int numCameraList;
	bool shutteropen; 
	bool camerabusy;
	bool legacy;
		
	std::map<int, std::string> errorMap;
	std::map<int, std::string> tvMap;
	std::map<int, std::string> avMap;
	std::map<int, std::string> isoMap;
	std::map<int, std::string> imageQualityMap;

	EdsError checkError(EdsError err);
	void predefinedErrorMap(void);
	void fillTvDescriptions(void);
	void fillAvDescriptions(void);
	void fillISODescriptions(void);
	void fillImageQualityDescriptions(bool legacy);
	void fillBriefImageQualityDescriptions(bool legacy);

	std::queue<std::string> fileNameQueue;

	int getCameraPropertyValue(EdsPropertyID id);
	void setCameraPropertyValue(EdsPropertyID id, EdsUInt32 value);
	EdsError downloadImage(EdsDirectoryItemRef directoryItem);
};
