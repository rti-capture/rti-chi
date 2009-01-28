// Copyright Cultural Heritage Imaging 2008

#include "stdafx.h"
#include "capture.h"
#include "CameraControl.h"
#include "CameraControlErrors.h"
#include "CameraControlDescriptions.h"
#include <iostream>
#include "captureDlg.h"
#include <process.h>

using namespace std;

CameraControl::CameraControl(void)
{
	EdsError err = EDS_ERR_OK;

	camera = NULL;
	strCameraList = NULL;
	strSaveNextFile = "";

	shutteropen = false;
	camerabusy = false;

	// Fill up the errorMap, used later by checkError()
	predefinedErrorMap();
	// Fill up the property descriptors
	fillTvDescriptions();
	fillAvDescriptions();
	fillISODescriptions();

}

CameraControl::~CameraControl(void)
{
	EdsError	 err = EDS_ERR_OK;
	
	//Release Camera
	if( camera != NULL )
	{
		EdsRelease(camera);
		camera = NULL;
	}

	//Delete list of cameras if it was filled
	if (strCameraList != NULL) 
	{
		delete []strCameraList;
	}

}

int CameraControl::populateCameraList(void) 
{
	EdsError	 err = EDS_ERR_OK;
	EdsCameraListRef cameraList = NULL;
	EdsUInt32	 count = 0;	

	//Acquisition of camera list
	err = EdsGetCameraList(&cameraList);
	checkError(err);

	//Acquisition of number of Cameras
	if(err == EDS_ERR_OK)
	{
		err = EdsGetChildCount(cameraList, &count);
		if(count == 0)
		{
			err = EDS_ERR_DEVICE_NOT_FOUND;
		}
		checkError(err);
	}

	numCameraList = count;
	if (numCameraList > 0) strCameraList = new std::string[numCameraList];

	for (int i=0; i<numCameraList; i++) 
	{
		//Acquisition of camera at the i-th place in the list
		EdsCameraRef camera = NULL;

		if(err == EDS_ERR_OK)
		{	
			err = EdsGetChildAtIndex(cameraList , i , &camera);	
			checkError(err);
		}

		//Acquisition of camera information
		EdsDeviceInfo deviceInfo;
		if(err == EDS_ERR_OK)
		{	
			err = EdsGetDeviceInfo(camera , &deviceInfo);	
			if(err == EDS_ERR_OK && camera == NULL)
			{
				err = EDS_ERR_DEVICE_NOT_FOUND;
			}
			else
			{
				strCameraList[i] = deviceInfo.szDeviceDescription;
			}
			checkError(err);
		}
	}

	//Release camera list
	if(cameraList != NULL)
	{
		EdsRelease(cameraList);
	}
 
	return numCameraList;

}

EdsError CameraControl::downloadImage(EdsDirectoryItemRef directoryItem)
{
	shutteropen = false;

	EdsStreamRef stream = NULL;
	EdsUInt32 dataSize;
	EdsError err = EDS_ERR_OK;

	// Get directory item information
	EdsDirectoryItemInfo dirItemInfo;
	err = EdsGetDirectoryItemInfo(directoryItem, & dirItemInfo);
	checkError(err);

	// Get the extension from the camera filename and append it to the stored filename.
	std::string saveFileName;
	
	if (!fileNameQueue.empty()) 
	{
		saveFileName = fileNameQueue.front();
		fileNameQueue.pop();
		std::string cameraFileName = dirItemInfo.szFileName;
		int dotpos = cameraFileName.rfind('.',cameraFileName.length());
		if (dotpos != std::string::npos)
		{
			std::string ext = cameraFileName.substr(dotpos);
			saveFileName += ext;		
		}
		else
		{
			saveFileName += ".unknown";
		}
	}
	else
	{
		saveFileName = dirItemInfo.szFileName;
	}


	// Create file stream for transfer destination
	if(err == EDS_ERR_OK)
	{
		err = EdsCreateFileStream( saveFileName.c_str(),	kEdsFileCreateDisposition_CreateAlways,
								   kEdsAccess_ReadWrite, &stream);
		checkError(err);
	}

	// Download image
	if(err == EDS_ERR_OK)
	{
		err = EdsDownload( directoryItem, dirItemInfo.size, stream);
		checkError(err);
	}

	// Issue notification that download is complete
	if(err == EDS_ERR_OK)
	{
		err = EdsDownloadComplete(directoryItem);
		checkError(err);
	}

	// Release stream
	if( stream != NULL)
	{
		EdsRelease(stream);
		stream = NULL;
	}
	
	strSaveNextFile = "";
	camerabusy = false;
	
	return err; 
}

EdsError EDSCALLBACK CameraControl::handleObjectEvent( EdsObjectEvent event, EdsBaseRef object, EdsVoid * context) 
{
	cout << "event happend! " << endl;

	CameraControl * current = (CameraControl*)context;
	switch(event)
	{
		case kEdsObjectEvent_DirItemRequestTransfer:
			current->downloadImage(object);
			break;
		default:
			break;
	}

	// Object must be released
	if(object)
	{
		EdsRelease(object);
	}
	
	return NULL;
}


void CameraControl::connectCamera(int index) 
{
	EdsError	 err = EDS_ERR_OK;
	EdsCameraListRef cameraList = NULL;

	//If a camera is already connected, disconnect it first.
	if (camera != NULL) disconnectCamera();

	//Acquisition of camera list
	err = EdsGetCameraList(&cameraList);
	checkError(err);

	for (int i=0; i<=index; i++) 
	{
		//Acquisition of camera at the i-th place in the list
		if(err == EDS_ERR_OK)
		{	
			err = EdsGetChildAtIndex(cameraList , i , &camera);	
			checkError(err);
		}
	}

	EdsDeviceInfo deviceInfo;
	err = EdsGetDeviceInfo(camera , &deviceInfo);
	checkError(err);
	if(err == EDS_ERR_OK && camera == NULL)
	{
		checkError(EDS_ERR_DEVICE_NOT_FOUND); // Is this check really needed?
	}

	// Check if we have a legacy camera
	if(deviceInfo.deviceSubType == 0)
		legacy = true;
	else
		legacy = false;

	//Release camera list
	if(cameraList != NULL)
	{
		EdsRelease(cameraList);
	}


	if(err != EDS_ERR_OK)
	{
		//If there was an error in any of the previous steps, the camera is not connected
		camera = NULL;
	}
	else
	{
		//Set the image quality description lists
		// legacy = false; // Need to fix whatever the detection issue.
		fillImageQualityDescriptions(legacy);
		fillBriefImageQualityDescriptions(legacy);

		//Set Object Event Handler
		err = EdsSetObjectEventHandler( camera, kEdsObjectEvent_All, CameraControl::handleObjectEvent , this);
		checkError(err);

		//Now open the session for that camera
		err = EdsOpenSession(camera);
		checkError(err);

		//Set property for saving the files on the host computer
		//EdsUInt32 saveTo = kEdsSaveTo_Host;
		//err = EdsSetPropertyData(camera, kEdsPropID_SaveTo, 0, sizeof(saveTo) , &saveTo);
		//checkError(err);

		//*************************************************************//
		bool locked;
		//Preservation ahead is set to PC
		if(err == EDS_ERR_OK)
		{
			EdsUInt32 saveTo = kEdsSaveTo_Host;
			err = EdsSetPropertyData(camera, kEdsPropID_SaveTo, 0, sizeof(saveTo) , &saveTo);
		}
	

		//UI lock
		if(err == EDS_ERR_OK)
		{
			err = EdsSendStatusCommand(camera, kEdsCameraStatusCommand_UILock, 0);
		}

		if(err == EDS_ERR_OK)
		{
			locked = true;
		}

		
		if(err == EDS_ERR_OK)
		{
			EdsCapacity capacity = {0x7FFFFFFF, 0x1000, 1};
			err = EdsSetCapacity(camera, capacity);
		}
		
		//It releases it when locked
		if(locked)
		{
			EdsSendStatusCommand(camera, kEdsCameraStatusCommand_UIUnLock, 0);
		}	

			//*********************************************************************//


		string s = getTvDesc();
		string s2 = getAvDesc();
		string s3 = getISODesc();
		//string s4 = getImageQualityDesc();
		checkError(err);
	}	

}

void CameraControl::disconnectCamera(void) 
{
	EdsError err;
	if (camera != NULL) 
	{
		//Close the session for the camera
		err = EdsCloseSession(camera);
		checkError(err);
		camera = NULL;
	}
}

void CameraControl::takePicture(std::string strFileName)
{
	EdsError err = EDS_ERR_OK;

	fileNameQueue.push(strFileName);

	//err = EdsSendStatusCommand(camera, kEdsCameraStatusCommand_UILock, 0);

	cout << " taking picture... " << endl;

	err = EdsSendCommand(camera, kEdsCameraCommand_TakePicture ,0);
	
	//err = EdsSendStatusCommand(camera, kEdsCameraStatusCommand_UIUnLock, 0);

	if (err == EDS_ERR_OK)
	{
		shutteropen = true;
		camerabusy = true;
	}   
	else
		checkError(err);
}




EdsError CameraControl::checkError(EdsError err)
{
	if (err != EDS_ERR_OK) 
	{
		if (errorMap.count(err)>0)
			MessageBox(NULL,errorMap[err].c_str(),"Camera Error",MB_OK);
		else
			MessageBox(NULL,"Unlisted and Unknown error!","Camera Error",MB_OK);
	}

	return err;
}

int CameraControl::getTv()
{
	return getCameraPropertyValue(kEdsPropID_Tv);
}

string CameraControl::getTvDesc()
{
	return tvMap[getTv()];
}

void CameraControl::setTv(int value)
{
	setCameraPropertyValue(kEdsPropID_Tv,value);
}

int CameraControl::getAv()
{
	return getCameraPropertyValue(kEdsPropID_Av);
}

string CameraControl::getAvDesc()
{
	return avMap[getAv()];
}

void CameraControl::setAv(int value)
{
	setCameraPropertyValue(kEdsPropID_Av,value);
}

int CameraControl::getISO()
{
	return getCameraPropertyValue(kEdsPropID_ISOSpeed);
}

string CameraControl::getISODesc()
{
	return isoMap[getISO()];
}

void CameraControl::setISO(int value)
{
	setCameraPropertyValue(kEdsPropID_ISOSpeed,value);
}

int CameraControl::getImageQuality()
{
	return getCameraPropertyValue(kEdsPropID_ImageQuality);
}

string CameraControl::getImageQualityDesc()
{
	return imageQualityMap[getImageQuality()];
}

void CameraControl::setImageQuality(int value)
{
	setCameraPropertyValue(kEdsPropID_ImageQuality,value);
}

int CameraControl::getCameraPropertyValue(EdsPropertyID id) 
{
	EdsError err = EDS_ERR_OK;
	EdsUInt32 value;
	EdsDataType dataType;
	EdsUInt32 dataSize;
	err = EdsGetPropertySize(camera, id, 0 , &dataType, &dataSize);
	checkError(err);

	err = EdsGetPropertyData(camera, id, 0, dataSize, &value);

	return value;
}

void CameraControl::setCameraPropertyValue(EdsPropertyID id, EdsUInt32 value)
{
	EdsError err = EDS_ERR_OK;
	err = EdsSetPropertyData(camera, id, 0 , sizeof(value), &value);
	checkError(err);
}