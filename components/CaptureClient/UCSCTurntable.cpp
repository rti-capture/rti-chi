// Copyright Cultural Heritage Imaging 2008

#include "stdafx.h"
#include <iostream>
#include "VxmDriver.h"
#include "UCSCTurntable.h"

using namespace std;

void UCSCTurntable::Initialize(string strPort) 
{
	LoadDriver(".\\VxmDriver.dll");

	if (strPort.substr(0,3).compare("COM") ==0)
	{
		string strPortNum = strPort.substr(3,strPort.length()-3);
		int numPort = atoi(strPortNum.c_str());
		if (!PortOpen(numPort,9600)) 
		{
			cout << "error: cannot connect to the com port (for turntable)\n";
		}

	}
	else 
	{
		cout << "error: Please enter the turntable COM port in form COM3 etc.\n";
	}
}

void UCSCTurntable::SetSpeed(int speed)
{
	char command[256];
	/*
	int acceleration = 1;
	sprintf(command,"F,C,A1M%i,R",acceleration);

	if (PortSendCommands(command)) 
	{
		cout << "Set acceleration command successful!\n";
	} 
	else 
	{
		cout << "Error setting acceleration !!\n";			
	} 
	*/

	sprintf(command,"F,C,S1M%i,R",speed);

	if (PortSendCommands(command)) 
	{
		cout << "Set speed command successful!\n";
	} 
	else 
	{
		cout << "Error setting speed !!\n";			
	} 
}

void UCSCTurntable::Rotate(float degrees)
{
	char command[256];
	int stepsizeturntable = degrees/.025;
	sprintf(command,"F,C,I1M%i,R",stepsizeturntable);

	if (PortSendCommands(command)) 
	{
		cout << "Send command successful!\n";
	} 
	else 
	{
		cout << "Error rotating turntable!!\n";			
	} 
}

void UCSCTurntable::Close()
{
    ReleaseDriver(); 

	//PortWrite(0);
	PortCloseVXM();
}