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
		if (!PortOpen(7,9600)) 
		{
			cout << "error: cannot connect to the com port (for turntable)\n";
		}

	}
}

void UCSCTurntable::Rotate(float degrees)
{
}

void UCSCTurntable::Close()
{
}