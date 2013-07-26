// Copyright Cultural Heritage Imaging 2008

#pragma once
#include "stdafx.h"
#include <string>

class Turntable
{
public:
	virtual void Initialize(std::string strPort) = 0;
	virtual void Rotate(float degrees) = 0;
	virtual void SetSpeed(int speed) = 0;
	virtual void Close() = 0;
};
