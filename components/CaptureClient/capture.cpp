// Copyright Cultural Heritage Imaging 2008

// capture.cpp : Defines the class behaviors for the application.
//

#include "stdafx.h"
#include "capture.h"
#include "captureDlg.h"
#include "CameraControl.h"

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CcaptureApp

BEGIN_MESSAGE_MAP(CcaptureApp, CWinApp)
	ON_COMMAND(ID_HELP, &CWinApp::OnHelp)
END_MESSAGE_MAP()


// CcaptureApp construction

CcaptureApp::CcaptureApp()
{
	// TODO: add construction code here,
	// Place all significant initialization in InitInstance
}


// The one and only CcaptureApp object

CcaptureApp theApp;


// CcaptureApp initialization

BOOL CcaptureApp::InitInstance()
{
	CWinApp::InitInstance();

	// Standard initialization
	// If you are not using these features and wish to reduce the size
	// of your final executable, you should remove from the following
	// the specific initialization routines you do not need
	// Change the registry key under which our settings are stored
	// TODO: You should modify this string to be something appropriate
	// such as the name of your company or organization
	SetRegistryKey(_T("Local AppWizard-Generated Applications"));

	CcaptureDlg dlg;
	m_pMainWnd = &dlg;


	// Console create code, comment if the debug window is not needed.
	// AllocConsole();
    // freopen ("CONOUT$", "w", stdout ); 

	EdsError err = EdsInitializeSDK();

	INT_PTR nResponse = dlg.DoModal();
	if (nResponse == IDOK)
	{
		// TODO: Place code here to handle when the dialog is
		//  dismissed with OK
	}
	else if (nResponse == IDCANCEL)
	{
		// TODO: Place code here to handle when the dialog is
		//  dismissed with Cancel
	}

	EdsTerminateSDK();

	// Since the dialog has been closed, return FALSE so that we exit the
	//  application, rather than start the application's message pump.
	return FALSE;
}
