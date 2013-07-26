// Copyright Cultural Heritage Imaging 2008

#include "StdAfx.h"
#include "CaptureSequence.h"
#include "port.h"
#include "direct.h"
#include <time.h>
#include <iostream>
#include <math.h>
#include "UCSCTurntable.h"
#include "LightControl.h"

using namespace std;

CaptureSequence::CaptureSequence(void)
{
	paused = false;
	cancelled = false;
}

CaptureSequence::~CaptureSequence(void)
{
}

int CaptureSequence::startSequence()
{
	cout << "  sequence start  " << endl;

	time_t rawtime;
    time ( &rawtime );
    string strTime = ctime(&rawtime);

	strFullFolder = strSavePath + "\\" + strRootPrefix;
	/* Make the save directory if not a camera dry run */
	if ( !cameraDryRun ) {
		int errno;
		if ((errno = _mkdir(strFullFolder.c_str())) != 0) {
			status->AppendStatusMsg( "ERROR: ", (errno == ENOENT ? "path name not found" : "could not create directory"),FALSE );
			return -1;
		}
		string strLogFile = strFullFolder + "\\" + strRootPrefix + ".log";
		logfile.open(strLogFile.c_str());
		logfile << "Capture Client Version : " << CONTROLLER_VERSION << endl;
		logfile << "Capture started at " << strTime << endl;
		
		if (strPatchPanel.empty()) 
			logfile << "No patch panel file loaded for lights. Using default mapping. " << endl;
		else
			logfile << "Patch Panel File : " << strPatchPanel << endl;

		logfile << "Camera Parameters " << endl;
		logfile << "Av : " << cameracontrol->getAvDesc() << endl;
		logfile << "Tv : " << cameracontrol->getTvDesc() << endl;
		logfile << "ISO : " << cameracontrol->getISODesc() << endl;
		logfile << "Image Quality : " << cameracontrol->getImageQualityDesc() << endl << endl;

		photosTaken = 0;
	}
	
	logfile << "LightPort = " << strPort << endl;
	logfile << "SequenceSize = " << numSequenceSize << endl;
	logfile << "SavePath = " << strSavePath << endl;
	logfile << "RootPrefix = " << strRootPrefix << endl;
	logfile << "LightsDelay = " << numLightsDelay << endl;
	logfile << "TurntableEnabled = " << useTurntable << endl;
	logfile << "TurntablePort = " << strTurntablePort << endl;
	logfile << "Viewpoints = " << numViewpoints << endl;
	logfile << "ViewSeperation = " << numViewSeperation << endl;
	logfile << "RotationDelay = " << numRotationDelay << endl;
	logfile << endl;

	if ( !lightsDryRun ) {
		if (!lightControl.isLightMappingLoaded()) {
		  	status->AppendStatusMsg("Error loading LightMapping.txt file!", false);
			status->AppendStatusMsg("Make sure the file is in the same folder as the capture client executable.", false);
			return 1;
		}
		
		bool lightsReady = lightControl.initializeLights(strPort);
		if (lightsReady) {
			status->AppendStatusMsg( "Initialized light controller.", false);
			lightControl.resetAllLights();
			Sleep(numLightsDelay);
		} else {
			status->AppendStatusMsg( "Error initializing light controller!", false);
			status->AppendStatusMsg( "Make sure the controller is connected and the COM port is specified correctly.", false);
			return 1;
		}
	}

	status->AppendStatusMsg((char *) strTime.c_str(), false);
	status->AppendStatusMsg( "Staring capture sequence #", false);

	if (useTurntable) 
	{  // capturing multiple viewpoints with turntable, call seperate method
		captureTurntableSequence();
	}
	else
	{  // no turntable, this is just a single view capture
		strViewPrefix = "";

		for (int i = 1; i <= numSequenceSize; i++) {
			if (cancelled) {
				break;	// In case of interrupt, simply bail out
			}
			cout << " checking for busy wait ";
			while (cameracontrol->isCameraBusy()) {/*cout << ".";*/}
			cout << endl;

			SingleRelease(i);
		}
	}

	cout << " sequence end " << endl ;
	if ( !cameraDryRun ) {
		logfile << photosTaken << " photos taken." << endl;
		logfile.close();
		status->AppendStatusMsg( "Photos taken : ", photosTaken, false);
	}
	if ( !lightsDryRun )
		lightControl.close();
	return 0;
}


void CaptureSequence::captureTurntableSequence(void) 
{
	Turntable *table = new UCSCTurntable();
	char buffer[10];

	table->Initialize(strTurntablePort);
	table->SetSpeed(numTurntableSpeed);
	for (int view=0; view<numViewpoints; view++) {
		sprintf_s(buffer,"_%03d",view);
		strViewPrefix.assign(buffer);
		for (int i = 1; i <= numSequenceSize; i++) {
			if (cancelled) {
				break;	// In case of interrupt, simply bail out
			}
			cout << " checking for busy wait ";
			while (cameracontrol->isCameraBusy()) {/*cout << ".";*/}
			cout << endl;

			SingleRelease(i);
		}
		if (view<numViewpoints-1) table->Rotate(numViewSeperation);
		Sleep(numRotationDelay);
	}
	table->Close();
}

void CaptureSequence::SingleRelease(int numShot) 
{
	int    numFiber = PatchPanel[numShot-1]; 

	if (numFiber == 0) {
	  status->AppendStatusMsg( "Patch panel is 0, skipping image #", numShot, TRUE);
	  return;
	}

	cout << " Shot = " << numShot << " Light = " << numFiber << endl;

	status->AppendStatusMsg( "Turning on light #", numFiber, TRUE /* Verbose only */ );
	if ( !lightsDryRun ) {
		lightControl.flush();
		Sleep(100);
		lightControl.turnOnLight(numFiber);
	}
		
	if (numShot == 1) {
		// first shot is treated as a special case, give it two seconds OR numLightsDelay.
		Sleep(max(2000,numLightsDelay));
	} else {
		// Use the delay specified in the dialog box
		Sleep(numLightsDelay);
	}

	status->AppendStatusMsg( "Taking image #", numShot, TRUE /* Verbose only */ );

	if ( !cameraDryRun ) 
	{
		char buffer[20];
		std::string strSaveFile;
		sprintf_s(buffer,"_%02d",numShot);
		strSaveFile = strFullFolder + "\\" + strRootPrefix + strViewPrefix + buffer;
		status->AppendStatusMsg( "Saving as ", (char *)strSaveFile.c_str(), TRUE /* Verbose only */ );
		//Sleep(2000);
		cameracontrol->takePicture(strSaveFile);
		while (cameracontrol->isShutterOpen()) {/*cout << ".";*/}
		//Sleep(1000);

		logfile << strRootPrefix << strViewPrefix << buffer << endl;
		photosTaken++;
	}
	
	status->AppendStatusMsg( "Turning off light #", numFiber, TRUE /* Verbose only */ );
	if ( !lightsDryRun ) {
		lightControl.turnOffLight(numFiber);
	}
	Sleep(100);
}
/*
void CaptureSequence::ToggleOneFiber( int  numFiber, BOOL on ) {
	char buffer[64];

	//char szPort[MAX_PATH];

	const int kCmdBytes = 14;
	unsigned char CommandByte;
	unsigned char cmd[kCmdBytes];
	unsigned int i;
	int numbytes;

	// kNumFibers will define the length of the bit string that gets written to the port
	// needs to be a multiple of 8 to ensure the bytes are properly padded.
	unsigned int kNumFibers = numLights;
	if (numLights == MAX_LIGHTS)
	{
		status->AppendStatusMsg( "Max lights set to numSequence", TRUE);
		kNumFibers = numSequenceSize;
	}
	kNumFibers = ceil((double)kNumFibers/8)*8;

	// Can we check that one of these is actually on, and give status after?
	if ( !on ) 
	{
		status->AppendStatusMsg( "Turning off light #", numFiber, true );
		numFiber = 0;	// actually turn off all the lights just for safety
	} 
	else 
	{
		status->AppendStatusMsg( "Turning on light #", numFiber, true );
	}

	CommandByte = 0;
	cmd[0] = (unsigned char)'A';
	cmd[1] = (unsigned char)0;
	numbytes = 2;
	cout << "Num Fiber = " << numFiber << "  Status = " << on << "  " ;
	for (i = 0; i < kNumFibers; i++) 
	{
		CommandByte >>= 1;
		if (numFiber == i+1) CommandByte |= 0x80;
		if (i % 8 == 7) 
		{
			cmd[numbytes++] = CommandByte;
			//cout << " (" << i << " " << (int)CommandByte << " " << " * " << ")" ;
			CommandByte = 0;
		}
	 }
	cout << endl;
 
	while (numbytes < kCmdBytes) cmd[numbytes++] = 0;

	sprintf_s(buffer, 64, "%X %X %X %X %X %X %X %X %X %X %X %X %X %X", cmd[0], cmd[1], cmd[2], cmd[3],
		cmd[4], cmd[5], cmd[6], cmd[7], cmd[8], cmd[9], cmd[10], cmd[11], cmd[12], cmd[13]);

	status->AppendStatusMsg( "Control message: ", buffer, true );

	// If not dry run mode, open the selected serial port and write control message
	if ( !lightsDryRun ) 
	{
		cout << "num bytes = " << numbytes << endl;
		cout << "Command = " ;
		for (int i=0; i<numbytes; i++) 
			cout << (int)cmd[i] << " ";
		cout << endl;

		if (!PortInitialize(strPort.c_str())) 
		{
			// Should this error be fatal?  Especially w/o a cancel button working?
			status->AppendStatusMsg( "ERROR: failed to initialize light control port ", (char *)strPort.c_str(), false );
		} 
		else 
		{
			WriteBlock(cmd, numbytes);
			PortClose();
		}
	}
}
*/



bool CaptureSequence::loadSettings() 
{
  //load the light mappings file
  lightControl.loadLightToControllerMapping();
  if (!lightControl.isLightMappingLoaded()) {
	  	status->AppendStatusMsg("Error loading LightMapping.txt file!", false);
  }

  // load the patch panel
  HANDLE handle = CreateFile("PatchPanel.txt",  // file name
                              GENERIC_READ,     // open for reading
                              0,                // do not share
                              NULL,             // default security
                              OPEN_EXISTING,    // existing file only
                              FILE_ATTRIBUTE_NORMAL, // normal file
                              NULL);            // no template
  if (handle == INVALID_HANDLE_VALUE) 
  {
    // Assume that no patch panel file is OK, but always give a message
	for (int i = 0; i < MAX_LIGHTS; i++) 
	{
      PatchPanel[i] = i+1;
	}
	status->AppendStatusMsg("No patch panel file found! Default installed", FALSE /* Not verbose only */ );
	strPatchPanel = "";
	numLights = MAX_LIGHTS;
    return FALSE;
  } 
  else 
  {
	// Get the complete path name for the file.
	char temppath[200];
    GetFullPathName("PatchPanel.txt",200,temppath,NULL);
	strPatchPanel.assign(temppath);

	DWORD size, read;
    char *buf, *p, c, buffer[64];
    int fiber, image, count;

    size = GetFileSize(handle, NULL);
    if (size == 0xFFFFFFFF) 
	{
      CloseHandle(handle);
	  status->AppendStatusMsg("ERROR: Could not read patch panel, bogus file size", FALSE /* Not verbose only */ );
      return FALSE;
    }

    // Read the entire file
    buf = (char *)malloc(size+1);
    ReadFile(handle, buf, size, &read, NULL);
	buf[size] = 0; // zero-terminate the buffer

    CloseHandle(handle);
   
    // Parse the file contents and fill PatchPanel array; image is the
    // image number that will be associated with a (possibly different)
    // fiber, and not all images will be associated with any fiber.  The
    // first N images should have fibers associated with them, and all
    // subsequent ones should have 0's (i.e. an invalid fiber).  Some
    // minimal error checking is done on this, and the mapping is printed
    // to the console even in non-verbose mode.  It is an error for there
    // not to be 32 patch panel entries.  If the shot count is reduced to
    // N < 32, the first N of the mappings from the patch panel are used,
    // but all still need to be there.
    //
    // Patch panel entries are of the form N:M where N and M are [1,32].
    // That is, numbering starts with 1, not 0.  Spaces are not allowed.
    p = buf;
    count = 0;
    while (p && *p) 
	{
      count++;
      image = atoi(p);
      if (image != count) 
	  {
		_itoa_s(count, buffer, 64, 10);
	    status->AppendStatusMsg("ERROR: didn't find valid image number: expected ", buffer, FALSE /* Not verbose only */ );
        return FALSE;
      }
      p = strchr(p, ':');
      if (!p) 
	  {
	    _itoa_s(image, buffer, 64, 10);
	    status->AppendStatusMsg("ERROR: didn't find colon separator for image ", buffer, FALSE /* Not verbose only */ );
        return FALSE;
      }
      p++;
      fiber = atoi(p);
	  p++;
      if (fiber < 0 || fiber > MAX_LIGHTS) 
	  {
		_itoa_s(image, buffer, 64, 10);
        status->AppendStatusMsg("ERROR: didn't find valid light number for image ", buffer, FALSE /* Not verbose only */ );
        return FALSE;
      }
      p++;
      PatchPanel[image-1] = fiber;

      // Skip over newlines and anything else until another digit is found
      // FIX ME: Will not terminate because no zero terminator
	  while (0 != (c=*p)) 
	  {
        if (c < '0' || c > '9') p++;
		else break;
      }
 
    }

	free(buf);

	numLights = count;  // in the multispectral case, how about the lights 49-56? CHANGE
	
	status->AppendStatusMsg("Patch panel installed from PatchPanel.txt", FALSE);
	for (int i = 1; i <= count; i++) 
	{
	if (PatchPanel[i-1] == 0) 
	{
	  sprintf_s(buffer, 64, "image %d will be skipped", i);
	} 
	else 
	{
	  sprintf_s(buffer, 64, "image %d will use light %d", i, PatchPanel[i-1]);
	}
	status->AppendStatusMsg("Patch panel: ", buffer, FALSE);
	}
  }
 
  return true;
}

