// Copyright Cultural Heritage Imaging 2008

// captureDlg.cpp : implementation file
//

#include "stdafx.h"
#include "capture.h"
#include "captureDlg.h"
#include "CameraControl.h"
#include "selectCameraDialog.h"
#include <iostream>
#include <process.h>

using namespace std;

#ifdef _DEBUG
#define new DEBUG_NEW
#endif


// CAboutDlg dialog used for App About

class CAboutDlg : public CDialog
{
public:
	CAboutDlg();

// Dialog Data
	enum { IDD = IDD_ABOUTBOX };

	protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

// Implementation
protected:
	DECLARE_MESSAGE_MAP()
};

CAboutDlg::CAboutDlg() : CDialog(CAboutDlg::IDD)
{
}

void CAboutDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
}

BEGIN_MESSAGE_MAP(CAboutDlg, CDialog)
END_MESSAGE_MAP()


// CcaptureDlg dialog




CcaptureDlg::CcaptureDlg(CWnd* pParent /*=NULL*/)
	: CDialog(CcaptureDlg::IDD, pParent)
	, strPort(_T("COM1"))
	, numSequenceSize(48)
	, strSavePath(_T(""))
	, strRootPrefix(_T(""))
	, strStatus(_T(""))
	, boolVerbose(TRUE)
	, boolCameraDryRun(TRUE)
	, boolLightsDryRun(TRUE)
	, strCameraInfo(_T(""))
	, strConfigFile(_T(""))
	, numLightsDelay(1000)
	, boolTurntableDryRun(TRUE)
	, boolEnableTurntable(FALSE)
	, strTurntablePort(_T(""))
	, numViewpoints(1)
	, numViewSeperation(4)
	, numRotationDelay(1000)
	, numTurntableSpeed(100)
	, bEnableMultispectral(FALSE)
	, boolMultispectralOnly(FALSE)
	, boolMultispectralAndNormal(FALSE)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
	status.editBox = &editStatus;
	capturesequence.status = &status;

	
}

void CcaptureDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_PORT, strPort);
	DDX_Text(pDX, IDC_EDIT_SEQUENCESIZE, numSequenceSize);
	DDX_Text(pDX, IDC_EDIT_SAVEPATH, strSavePath);
	DDX_Text(pDX, IDC_EDIT_ROOTPREFIX, strRootPrefix);
	DDX_Control(pDX, IDC_EDIT_STATUS, editStatus);
	DDX_Text(pDX, IDC_EDIT_STATUS, strStatus);
	DDX_Check(pDX, IDC_CHECK_VERBOSE, boolVerbose);
	DDX_Check(pDX, IDC_CHECK_CAMERADRYRUN, boolCameraDryRun);
	DDX_Check(pDX, IDC_CHECK_LIGHTSDRYRUN, boolLightsDryRun);
	DDX_Control(pDX, IDC_BUTTON_CAPTUREPTM, btnCapturePTM);
	DDX_Control(pDX, IDC_BUTTON_CAPTUREPAUSE, btnPauseCapture);
	DDX_Control(pDX, IDC_BUTTON_CAPTURECANCEL, btnCancelCapture);
	DDX_Text(pDX, IDC_EDIT_CAMERAINFO, strCameraInfo);
	DDX_Control(pDX, IDC_BUTTON_BROWSE, btnBrowseSavePath);
	DDX_Text(pDX, IDC_EDIT_CONFIGFILE, strConfigFile);
	DDX_Control(pDX, IDC_BUTTON_SAVECONFIG, btnSaveConfig);
	DDX_Control(pDX, IDC_BUTTON_LOADCONFIG, btnLoadConfig);
	DDX_Control(pDX, IDC_COMBO_IMAGEQUALITY, cmbImageQuality);
	DDX_Text(pDX, IDC_EDIT_LIGHT_DELAY, numLightsDelay);
	DDX_Check(pDX, IDC_CHECK_TURNTABLEDRYRUN, boolTurntableDryRun);
	DDX_Check(pDX, IDC_CHECK_ENABLETURNTABLE, boolEnableTurntable);
	DDX_Text(pDX, IDC_EDIT_TURNTABLEPORT, strTurntablePort);
	DDX_Text(pDX, IDC_EDIT_NUMVIEWPOINTS, numViewpoints);
	DDX_Text(pDX, IDC_EDIT_VIEWSEPERATION, numViewSeperation);
	DDX_Text(pDX, IDC_EDIT_ROTATIONDELAY, numRotationDelay);
	DDX_Control(pDX, IDC_EDIT_TURNTABLEPORT, editTurntablePort);
	DDX_Control(pDX, IDC_EDIT_NUMVIEWPOINTS, editNumViewpoints);
	DDX_Control(pDX, IDC_EDIT_VIEWSEPERATION, editViewSeperation);
	DDX_Control(pDX, IDC_EDIT_ROTATIONDELAY, editRotationDelay);
	DDX_Control(pDX, IDC_CHECK_TURNTABLEDRYRUN, chkDryRunTurntable);
	DDX_Text(pDX, IDC_EDIT_TURNTABLESPEED, numTurntableSpeed);
	DDX_Control(pDX, IDC_EDIT_TURNTABLESPEED, editTurntableSpeed);
	DDX_Check(pDX, IDC_CHECK_MULTISPECTRAL, bEnableMultispectral);
	DDX_Control(pDX, IDC_COMBO_YELLOW_COLOR, comboYellowColor);
	DDX_Control(pDX, IDC_COMBO_BLUE_COLOR, comboBlueColor);
	DDX_Control(pDX, IDC_RADIO_MULTI, radioMulti);
	DDX_Control(pDX, IDC_RADIO_BOTH, radioBoth);
	DDX_Radio(pDX, IDC_RADIO_MULTI, boolMultispectralOnly);
	DDX_Radio(pDX, IDC_RADIO_BOTH, boolMultispectralAndNormal);
}

BEGIN_MESSAGE_MAP(CcaptureDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_BN_CLICKED(IDC_BUTTON1, &CcaptureDlg::OnBnClickedButton1)
	ON_BN_CLICKED(IDC_BUTTON2, &CcaptureDlg::OnBnClickedButton2)
	ON_BN_CLICKED(IDC_BUTTON_CONNECTCAMERA, &CcaptureDlg::OnBnClickedButtonConnectcamera)
	ON_BN_CLICKED(IDCANCEL, &CcaptureDlg::OnBnClickedCancel)
	ON_BN_CLICKED(IDC_BUTTON_DISCONNECTCAMERA, &CcaptureDlg::OnBnClickedButtonDisconnectcamera)
	ON_BN_CLICKED(IDC_BUTTON_BROWSE, &CcaptureDlg::OnBnClickedButtonBrowse)
	ON_BN_CLICKED(IDC_BUTTON_CAPTUREPTM, &CcaptureDlg::OnBnClickedButtonCaptureptm)
	ON_BN_CLICKED(IDC_BUTTON_CLEAR, &CcaptureDlg::OnBnClickedButtonClear)
	ON_BN_CLICKED(IDC_CHECK_VERBOSE, &CcaptureDlg::OnBnClickedCheckVerbose)
	ON_BN_CLICKED(IDC_BUTTON_SAVECONFIG, &CcaptureDlg::OnBnClickedButtonSaveconfig)
	ON_BN_CLICKED(IDC_BUTTON_LOADCONFIG, &CcaptureDlg::OnBnClickedButtonLoadconfig)
	ON_CBN_SELCHANGE(IDC_COMBO_IMAGEQUALITY, &CcaptureDlg::OnCbnSelchangeComboImagequality)
	ON_BN_CLICKED(IDC_CHECK_ENABLETURNTABLE, &CcaptureDlg::OnBnClickedCheckEnableturntable)
	ON_EN_CHANGE(IDC_EDIT_TURNTABLESPEED, &CcaptureDlg::OnEnChangeEditTurntablespeed)
	ON_BN_CLICKED(IDC_CHECK_MULTISPECTRAL, &CcaptureDlg::OnBnClickedCheckMultispectral)
	ON_BN_CLICKED(IDC_RADIO_MULTI, &CcaptureDlg::OnBnClickedRadioMulti)
	ON_BN_CLICKED(IDC_RADIO_BOTH, &CcaptureDlg::OnBnClickedRadioBoth)
END_MESSAGE_MAP()


// CcaptureDlg message handlers

BOOL CcaptureDlg::OnInitDialog()
{
	CDialog::OnInitDialog();

	// Add "About..." menu item to system menu.

	// IDM_ABOUTBOX must be in the system command range.
	ASSERT((IDM_ABOUTBOX & 0xFFF0) == IDM_ABOUTBOX);
	ASSERT(IDM_ABOUTBOX < 0xF000);

	CMenu* pSysMenu = GetSystemMenu(FALSE);
	if (pSysMenu != NULL)
	{
		CString strAboutMenu;
		strAboutMenu.LoadString(IDS_ABOUTBOX);
		if (!strAboutMenu.IsEmpty())
		{
			pSysMenu->AppendMenu(MF_SEPARATOR);
			pSysMenu->AppendMenu(MF_STRING, IDM_ABOUTBOX, strAboutMenu);
		}
	}

	// Set the icon for this dialog.  The framework does this automatically
	//  when the application's main window is not a dialog
	SetIcon(m_hIcon, TRUE);			// Set big icon
	SetIcon(m_hIcon, FALSE);		// Set small icon

	// Loading the PatchPanel Data
	capturesequence.loadSettings();
	
	// Enable and disable capture buttons
	btnCapturePTM.EnableWindow(true);
	btnPauseCapture.EnableWindow(false);
	btnCancelCapture.EnableWindow(false);

	// Load the icons to the three load and save buttons
	btnBrowseSavePath.SetIcon(LoadIcon(GetModuleHandle(NULL), MAKEINTRESOURCE(IDI_ICON_BROWSE))) ;
	btnLoadConfig.SetIcon(LoadIcon(GetModuleHandle(NULL), MAKEINTRESOURCE(IDI_ICON_OPEN))) ;
	btnSaveConfig.SetIcon(LoadIcon(GetModuleHandle(NULL), MAKEINTRESOURCE(IDI_ICON_SAVE))) ;

	// Set dialog title and controller version
	char titlebuffer[100];
	strcpy(titlebuffer,"RTI Capture ");
	strcat(titlebuffer,CONTROLLER_VERSION);
	SetWindowTextA(titlebuffer);

	// Try to open the default config file, if it exists
	if (OpenConfigFile("default.config")) 
	{
		UpdateData(false);
	}

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void CcaptureDlg::OnSysCommand(UINT nID, LPARAM lParam)
{
	if ((nID & 0xFFF0) == IDM_ABOUTBOX)
	{
		CAboutDlg dlgAbout;
		dlgAbout.DoModal();
	}
	else
	{
		CDialog::OnSysCommand(nID, lParam);
	}
}

// If you add a minimize button to your dialog, you will need the code below
//  to draw the icon.  For MFC applications using the document/view model,
//  this is automatically done for you by the framework.

void CcaptureDlg::OnPaint()
{
	if (IsIconic())
	{
		CPaintDC dc(this); // device context for painting

		SendMessage(WM_ICONERASEBKGND, reinterpret_cast<WPARAM>(dc.GetSafeHdc()), 0);

		// Center icon in client rectangle
		int cxIcon = GetSystemMetrics(SM_CXICON);
		int cyIcon = GetSystemMetrics(SM_CYICON);
		CRect rect;
		GetClientRect(&rect);
		int x = (rect.Width() - cxIcon + 1) / 2;
		int y = (rect.Height() - cyIcon + 1) / 2;

		// Draw the icon
		dc.DrawIcon(x, y, m_hIcon);
	}
	else
	{
		CDialog::OnPaint();
	}
}

// The system calls this function to obtain the cursor to display while the user drags
//  the minimized window.
HCURSOR CcaptureDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


// Stop the dialog from exiting on ENTER or ESC
BOOL CcaptureDlg::PreTranslateMessage(MSG* pMsg)
{
  // TODO: Add your specialized code here and/or call the base class
  if(pMsg->message==WM_KEYDOWN)
  {
      if(pMsg->wParam==VK_RETURN || pMsg->wParam==VK_ESCAPE)
          pMsg->wParam=NULL ;
  }

  return CDialog::PreTranslateMessage(pMsg);
}

   

void CcaptureDlg::OnBnClickedButton1()
{
	//	HICON hIcon = (HICON)LoadImage(NULL, "openfold.ico", IMAGE_ICON, 32, 32, LR_LOADFROMFILE);
	//btnBrowseSavePath.SetIcon(hIcon);
	UpdateData(true);
	/*	CameraControl *c = new CameraControl();
	string dummy;
	c->populateCameraList();
	cout << c->getCameraList(0);
	c->connectCamera(0);
	cout << "connected...";
	Sleep(2000);
	c->takePicture("test");
	Sleep(3000);
	c->disconnectCamera();
	cout << "disconnected...";
	cin >> dummy;
	delete c;*/

	btnBrowseSavePath.SetIcon(LoadIcon(GetModuleHandle(NULL), MAKEINTRESOURCE(IDI_ICON_BROWSE))) ;
	UpdateData(false);
}

void CcaptureDlg::OnBnClickedButton2()
{
	capturesequence.loadSettings();

}

void CcaptureDlg::OnBnClickedButtonConnectcamera()
{
	UpdateData(true);

	CselectCameraDialog dlg;

	dlg.selectCamera(&cameracontrol);

	RefreshCameraInfo();

	// 03-10 Commented the ImageQuality Combo Box handling
	/*
	if (cameracontrol.camera != NULL) 
	{
		// Adds the elements to the image quality selection combo box,
		// makes the proper setting the currently selected one.
		int index = 0;
		cmbImageQuality.Clear();
		int setting = cameracontrol.getImageQuality();
		bool knownsetting = false;
		for( map<int, string>::iterator iter = cameracontrol.briefImageQualityMap.begin(); iter != cameracontrol.briefImageQualityMap.end(); iter++ ) 
		{
			cmbImageQuality.AddString(iter->second.c_str());
			if (iter->first == setting) 
			{
				knownsetting = true;
				cmbImageQuality.SetCurSel(index);
			}
			index++;
		}
		// If the setting is unknown (possible because only 6 descriptions from many are displayed)
		// say "<custom>"
		if (!knownsetting) 
		{
			cmbImageQuality.AddString("<custom>");
			cmbImageQuality.SetCurSel(cmbImageQuality.GetCount() - 1);
		}
	}
	*/

	UpdateData(false);
}

void CcaptureDlg::RefreshCameraInfo()
{
	string strTemp;
	if (cameracontrol.camera != NULL) 
	{
		strTemp = "Connected to Camera. \r\n";
		strTemp += "Av : " + cameracontrol.getAvDesc() + "\r\n";
		strTemp += "Tv : " + cameracontrol.getTvDesc() + "\r\n";
		strTemp += "ISO : " + cameracontrol.getISODesc() + "\r\n";
		strTemp += "Image Quality : " + cameracontrol.getImageQualityDesc() + "\r\n";
	} 
	else 
	{
		strTemp = "Not connected to a camera. \r\n";
	}

	strCameraInfo = strTemp.c_str();
}

void CcaptureDlg::OnBnClickedCancel()
{
	OnCancel();
}

void CcaptureDlg::OnBnClickedButtonDisconnectcamera()
{
	UpdateData(true);
	cameracontrol.disconnectCamera();
	strCameraInfo = "Disconnected from camera.";
	UpdateData(false);
}

void CcaptureDlg::OnBnClickedButtonBrowse()
{
	LPMALLOC		pMalloc;
	BROWSEINFO		bi;
	char			szPath[MAX_PATH];
	LPITEMIDLIST	pidl;

	UpdateData(true);
	
	if( ::SHGetMalloc( &pMalloc ) == NOERROR ) {
		/* Data is set to a BROWSEINFO structure object. */
		bi.hwndOwner = GetSafeHwnd();
		bi.pidlRoot = NULL;
		bi.pszDisplayName = szPath;
		bi.lpszTitle = _T("Select a Save Directory");
		bi.ulFlags = BIF_RETURNFSANCESTORS | BIF_RETURNONLYFSDIRS;
		bi.lpfn = NULL;
		bi.lParam = 0;
		
		/* Display a dialog. */
		if( (pidl=::SHBrowseForFolder(&bi)) != NULL ) {
			if( ::SHGetPathFromIDList(pidl, szPath) ) {
				/* Set up file save path. */
				strSavePath = szPath;
			}
			pMalloc->Free(pidl);
		}	
		pMalloc->Release();
	}

	UpdateData(false);
}



void CcaptureDlg::OnBnClickedButtonCaptureptm()
{
	UpdateData(true);
	if (cameracontrol.camera == NULL && boolCameraDryRun) 
	{
		MessageBox("Camera not connected!","Connection Error",MB_OK);
		return;
	}
	if (strPort == "" && boolLightsDryRun)
	{
		MessageBox("Communications Port not entered!","Connection Error",MB_OK);
		return;
	}
	if (strSavePath == "" )
	{
		MessageBox("File save path is empty!","Connection Error",MB_OK);
		return;
	}
	if (strRootPrefix == "" )
	{
		MessageBox("Root Prefix is empty!","Settings Error",MB_OK);
		return;
	}
	if (numSequenceSize  < 1 )
	{
		MessageBox("Sequence size must be a positive integer!","Settings Error",MB_OK);
		return;
	} 
	if (capturesequence.numLights < numSequenceSize)
	{
		MessageBox("Sequence size must be smaller than the number of lights!","Settings Error",MB_OK);
		return;
	}
	
	capturesequence.strSavePath = strSavePath;
	capturesequence.strPort = strPort;
	capturesequence.strRootPrefix = strRootPrefix;
	capturesequence.numSequenceSize = numSequenceSize;
	capturesequence.numLightsDelay = numLightsDelay;

	capturesequence.cameraDryRun = !boolCameraDryRun;
	capturesequence.lightsDryRun = !boolLightsDryRun;

	capturesequence.useTurntable = boolEnableTurntable;
	capturesequence.turntableDryRun = !boolTurntableDryRun;

	capturesequence.cameracontrol = &cameracontrol;
	capturesequence.status = &status;

	if (boolEnableTurntable) 
	{
		status.AppendStatusMsg( "Turntable enabled.", true);
	
		if (strTurntablePort == "" && !boolTurntableDryRun)
		{
			MessageBox("Turntable Port not entered!","Connection Error",MB_OK);
			return;
		}
		if (numViewpoints  < 1 )
		{
			MessageBox("Number of viewpoints must be a positive integer!","Settings Error",MB_OK);
			return;
		} 
		if (numViewSeperation  <= 0 )
		{
			MessageBox("Viewpoint seperation must be a positive degree amount!","Settings Error",MB_OK);
			return;
		} 

		if (numTurntableSpeed  <= 1 || numTurntableSpeed  >= 4000)
		{
			MessageBox("Turntable speed must be between 1 and 4000!","Settings Error",MB_OK);
			return;
		} 

		capturesequence.strTurntablePort = strTurntablePort;
		capturesequence.numViewpoints = numViewpoints;
		capturesequence.numViewSeperation = numViewSeperation;
		capturesequence.numRotationDelay = numRotationDelay;
		capturesequence.numTurntableSpeed = numTurntableSpeed;
	
	}
	
	if (bEnableMultispectral) 
	{
		capturesequence.enablemultispectral = true;
		capturesequence.multionly = ((boolMultispectralOnly==0)?true:false);
		capturesequence.multiandnormal = ((boolMultispectralAndNormal==0)?true:false);
	}
	else 
	{
		capturesequence.enablemultispectral = false;
	}



	status.AppendStatusMsg( "Capture Started...", true);

	btnCapturePTM.EnableWindow(false);
	btnPauseCapture.EnableWindow(true);
	btnCancelCapture.EnableWindow(true);

	// call thread now.
	// Executed by another thread
	HANDLE hThread = (HANDLE)_beginthread(threadProc, 0, &capturesequence);

	btnCapturePTM.EnableWindow(true);
	btnPauseCapture.EnableWindow(false);
	btnCancelCapture.EnableWindow(false);

	UpdateData(false);
}

void CcaptureDlg::threadProc(void* lParam)
{
	CaptureSequence * cs = (CaptureSequence*)lParam;
	CoInitializeEx( NULL, COINIT_APARTMENTTHREADED );
	cs->startSequence();
	CoUninitialize();
	_endthread();
}

void CcaptureDlg::OnBnClickedButtonClear()
{
	editStatus.SetWindowTextA("");
	// TODO: Add your control notification handler code here
}

void CcaptureDlg::OnBnClickedCheckVerbose()
{
	UpdateData(true);
	status.verbose = boolVerbose;
}

void CcaptureDlg::OnBnClickedButtonSaveconfig()
{
	UpdateData(true);
	CFileDialog dlgFile(false,"*.config",strConfigFile, OFN_OVERWRITEPROMPT ,"Config Files (*.config)|*.config|");
	if (dlgFile.DoModal()==IDOK) 
	{
		ofstream configfile;
		configfile.open(dlgFile.GetFileName());
		configfile << "# Camera Configuration and settings file" << endl;
		if (cameracontrol.camera != NULL) 
		{
			// configfile << "Tv = " << cameracontrol.getTv() << " # " << cameracontrol.getTvDesc() << endl;
			// configfile << "Av = " << cameracontrol.getAv() << " # " << cameracontrol.getAvDesc() << endl;
			// configfile << "ISO = " << cameracontrol.getISO() << " # " << cameracontrol.getISODesc()<< endl;
			// configfile << "ImageQuality = " << cameracontrol.getImageQuality() << " # " << cameracontrol.getImageQualityDesc() << endl;
		} else {
			configfile << "# No camera connected. " << endl;
		}
		configfile << "LightPort = " << strPort << endl;
		configfile << "SequenceSize = " << numSequenceSize << endl;
		configfile << "SavePath = " << strSavePath << endl;
		configfile << "RootPrefix = " << strRootPrefix << endl;
		configfile << "LightsDelay = " << numLightsDelay << endl;
		
		//configfile << "TurntableEnabled = " << boolEnableTurntable << endl;
		//configfile << "TurntablePort = " << strTurntablePort << endl;
		//configfile << "Viewpoints = " << numViewpoints << endl;
		//configfile << "ViewSeperation = " << numViewSeperation << endl;
		//configfile << "RotationDelay = " << numRotationDelay << endl;
		//configfile << "TurntableSpeed = " << numTurntableSpeed << endl;
		configfile.close();
		strConfigFile = dlgFile.GetFileName();
	}
	UpdateData(false);
}

bool CcaptureDlg::OpenConfigFile(std::string filename)
{
	try
	{
		ConfigFile config(filename);
		strPort = config.read<string>("LightPort").c_str();
		numSequenceSize = config.read<int>("SequenceSize");
		strSavePath = config.read<string>("SavePath").c_str();
		strRootPrefix = config.read<string>("RootPrefix").c_str();
		if (config.keyExists("LightsDelay")) 
			numLightsDelay = config.read<int>("LightsDelay");
		else 
			numLightsDelay = 1000;
		
		if (config.keyExists("TurntableEnabled"))
		{
			boolEnableTurntable = config.read<bool>("TurntableEnabled");
			strTurntablePort = config.read<string>("TurntablePort").c_str();
			numViewpoints = config.read<int>("Viewpoints");
			numViewSeperation = config.read<float>("ViewSeperation");
			numRotationDelay = config.read<int>("RotationDelay");			
			numTurntableSpeed = config.read<int>("TurntableSpeed");
			UpdateData(false);
			OnBnClickedCheckEnableturntable();
		}
		else
		{
			boolEnableTurntable = false;
			UpdateData(false);
			OnBnClickedCheckEnableturntable();
		}

		if (cameracontrol.camera != NULL) 
		{
			// int av = config.read<int>("Av");
			// int tv = config.read<int>("Tv");
			// int iso = config.read<int>("ISO");
			//int imageQuality = config.read<int>("ImageQuality");

			// if (av>0) cameracontrol.setAv(av);
			// if (tv>0) cameracontrol.setTv(tv);
			// if (iso>0) cameracontrol.setISO(iso);
			//if (imageQuality>0) cameracontrol.setImageQuality(imageQuality);
			RefreshCameraInfo();
		}
		strConfigFile = filename.c_str();
		return true;
	}
	catch (ConfigFile::file_not_found f) 
	{
		return false;
	}
}

void CcaptureDlg::OnBnClickedButtonLoadconfig()
{
	UpdateData(true);
	CFileDialog dlgFile(true,"*.config",strConfigFile, OFN_OVERWRITEPROMPT ,"Config Files (*.config)|*.config|");
	if (dlgFile.DoModal()==IDOK) 
	{
		std::string s(dlgFile.GetFileName());
		OpenConfigFile(s);
	}
	UpdateData(false);
}

void CcaptureDlg::OnCbnSelchangeComboImagequality()
{
	// 03-10 Commented the ImageQuality Combo Box handling
	/*
	UpdateData(true);

	if (cmbImageQuality.GetCurSel()+1 == cmbImageQuality.GetCount())
		cmbImageQuality.SetCurSel(cmbImageQuality.GetCount()-2); // User tried to select '<Custom>' from the combo box

	int index = 0;

	for( map<int, string>::iterator iter = cameracontrol.briefImageQualityMap.begin(); iter != cameracontrol.briefImageQualityMap.end(); iter++ ) 
	{
	  if (index == cmbImageQuality.GetCurSel()) 
	  {
		  cameracontrol.setImageQuality(iter->first);
		  break;
	  }
	  index++;
	}

	RefreshCameraInfo();
	UpdateData(false);
	*/
}

void CcaptureDlg::OnBnClickedCheckEnableturntable()
{
	UpdateData(true);

	editTurntablePort.EnableWindow(boolEnableTurntable);
	editNumViewpoints.EnableWindow(boolEnableTurntable);
	editViewSeperation.EnableWindow(boolEnableTurntable);
	editRotationDelay.EnableWindow(boolEnableTurntable);
	chkDryRunTurntable.EnableWindow(boolEnableTurntable);
	editTurntableSpeed.EnableWindow(boolEnableTurntable);

	UpdateData(false);
}

void CcaptureDlg::OnEnChangeEditTurntablespeed()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CDialog::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
}

void CcaptureDlg::OnBnClickedCheckMultispectral()
{
	UpdateData(true);

	radioMulti.EnableWindow(bEnableMultispectral);
	radioBoth.EnableWindow(bEnableMultispectral);
	//comboYellowColor.EnableWindow(bEnableMultispectral);
	//comboBlueColor.EnableWindow(bEnableMultispectral);
	//radioMulti.SetCheck(false);
	//radioBoth.SetCheck(true);
	boolMultispectralAndNormal = true;
	boolMultispectralOnly = false;

	UpdateData(false);
}

void CcaptureDlg::OnBnClickedRadioMulti()
{
	radioBoth.SetCheck(false);
}

void CcaptureDlg::OnBnClickedRadioBoth()
{
	radioMulti.SetCheck(false);
}
