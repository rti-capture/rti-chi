// Copyright Cultural Heritage Imaging 2008

#pragma once
#include "stdafx.h"
#include "Turntable.h"

class UCSCTurntable : public Turntable 
{
public:
	void Initialize(std::string strPort);
	void Rotate(float degrees);
	void Close();
private:
	int position;
};