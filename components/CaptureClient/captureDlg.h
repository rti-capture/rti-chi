// Copyright Cultural Heritage Imaging 2008

// captureDlg.h : header file
//

#pragma once
#include "ConfigFile.h"
#include "resource.h"
#include <EDSDK.h>
#include "afxwin.h"
#include "CaptureSequence.h"
#include "CameraControl.h"
#include "StatusBuffer.h"

// CcaptureDlg dialog
class CcaptureDlg : public CDialog
{
// Construction
public:
	CcaptureDlg(CWnd* pParent = NULL);	// standard constructor

	CameraControl cameracontrol; // Main camera object, used for connecting and taking pics, etc.

	CaptureSequence capturesequence; // Sequence object for shooting sets of images and setting lights

	StatusBuffer status;	// Keeps a history of the activity. TODO : Expand to logging.

	static void threadProc(void* lParam);

// Dialog Data
	enum { IDD = IDD_CAPTURE_DIALOG };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);	// DDX/DDV support


// Implementation
protected:
	HICON m_hIcon;

	// Generated message map functions
	virtual BOOL OnInitDialog();
	afx_msg void OnSysCommand(UINT nID, LPARAM lParam);
	afx_msg void OnPaint();
	afx_msg HCURSOR OnQueryDragIcon();
	DECLARE_MESSAGE_MAP()

public:
	afx_msg void OnBnClickedButton1();
	afx_msg void OnBnClickedButton2();
	afx_msg void OnBnClickedButtonConnectcamera();
	afx_msg void OnBnClickedCancel();
	afx_msg void OnBnClickedButtonDisconnectcamera();
	afx_msg void OnBnClickedButtonBrowse();

	CString strPort;
	int numSequenceSize;
	CString strSavePath;
	CString strRootPrefix;
	int numLightsDelay;
	CEdit editStatus;
	CString strStatus;
	//void AppendStatusMsg( char *szStatusStr, char *szStatusDataStr, BOOL verboseOnlyMsg );
	//void AppendStatusMsg( char *szStatusStr, BOOL verboseOnlyMsg );
	//void AppendStatusMsg( char *szStatusStr, int number, BOOL verboseOnlyMsg ) ;
private:
	BOOL PreTranslateMessage(MSG* pMsg);

	bool boolVerboseStatus;
	
	void RefreshCameraInfo();

	bool OpenConfigFile(std::string filename);

	afx_msg void OnBnClickedButtonCaptureptm();
	BOOL boolVerbose;
	BOOL boolCameraDryRun;
	BOOL boolLightsDryRun;
	CButton btnCapturePTM;
	CButton btnPauseCapture;
	CButton btnCancelCapture;
	afx_msg void OnBnClickedButtonClear();
	afx_msg void OnBnClickedCheckVerbose();
	CString strCameraInfo;
	CButton btnBrowseSavePath;
	afx_msg void OnBnClickedButtonSaveconfig();
	CString strConfigFile;
	afx_msg void OnBnClickedButtonLoadconfig();
	CButton btnSaveConfig;
	CButton btnLoadConfig;
	CComboBox cmbImageQuality;
	afx_msg void OnCbnSelchangeComboImagequality();
	BOOL boolTurntableDryRun;
	BOOL boolEnableTurntable;
	CString strTurntablePort;
	int numViewpoints;
	float numViewSeperation;
	int numRotationDelay;
	afx_msg void OnBnClickedCheckEnableturntable();
	CEdit editTurntablePort;
	CEdit editNumViewpoints;
	CEdit editViewSeperation;
	CEdit editRotationDelay;
	CButton chkDryRunTurntable;
	afx_msg void OnEnChangeEditTurntablespeed();
	int numTurntableSpeed;
	CEdit editTurntableSpeed;
public:
	afx_msg void OnBnClickedCheckMultispectral();
	BOOL bEnableMultispectral;
	CComboBox comboYellowColor;
	CComboBox comboBlueColor;
	CButton radioMulti;
	CButton radioBoth;
	afx_msg void OnBnClickedRadioMulti();
	afx_msg void OnBnClickedRadioBoth();
	BOOL boolMultispectralOnly;
	BOOL boolMultispectralAndNormal;
};
