// Copyright Cultural Heritage Imaging 2008

#pragma once
#include "afxwin.h"
#include "CameraControl.h"

// CselectCameraDialog dialog

class CselectCameraDialog : public CDialog
{
	DECLARE_DYNAMIC(CselectCameraDialog)

public:
	CselectCameraDialog(CWnd* pParent = NULL);   // standard constructor
	virtual ~CselectCameraDialog();
	virtual BOOL OnInitDialog();


// Dialog Data
	enum { IDD = IDD_DIALOG_SELECTCAMERA };

protected:
	virtual void DoDataExchange(CDataExchange* pDX);    // DDX/DDV support

	DECLARE_MESSAGE_MAP()
public:
	afx_msg void OnBnClickedOk();
	int selectCamera(CameraControl * cameracontrol);
public:
	CComboBox cmbCameraList;
private:
	CameraControl * cameracontrol;
	int numSelectedCamera;
	afx_msg void OnCbnSelchangeComboCameralist();
};
