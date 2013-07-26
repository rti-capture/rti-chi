// Copyright Cultural Heritage Imaging 2008

// capture.h : main header file for the PROJECT_NAME application
//

#pragma once

#ifndef __AFXWIN_H__
	#error "include 'stdafx.h' before including this file for PCH"
#endif

#include "resource.h"		// main symbols


// CcaptureApp:
// See capture.cpp for the implementation of this class
//

class CcaptureApp : public CWinApp
{
public:
	CcaptureApp();

// Overrides
	public:
	virtual BOOL InitInstance();

// Implementation

	DECLARE_MESSAGE_MAP()
};

extern CcaptureApp theApp;