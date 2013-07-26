// Copyright Cultural Heritage Imaging 2008

// selectCameraDialog.cpp : implementation file
//

#include "stdafx.h"
#include "capture.h"
#include "selectCameraDialog.h"


// CselectCameraDialog dialog

IMPLEMENT_DYNAMIC(CselectCameraDialog, CDialog)

CselectCameraDialog::CselectCameraDialog(CWnd* pParent /*=NULL*/)
	: CDialog(CselectCameraDialog::IDD, pParent)
{

}

CselectCameraDialog::~CselectCameraDialog()
{
}

void CselectCameraDialog::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Control(pDX, IDC_COMBO_CAMERALIST, cmbCameraList);
}


BEGIN_MESSAGE_MAP(CselectCameraDialog, CDialog)
	ON_BN_CLICKED(IDOK, &CselectCameraDialog::OnBnClickedOk)
	ON_CBN_SELCHANGE(IDC_COMBO_CAMERALIST, &CselectCameraDialog::OnCbnSelchangeComboCameralist)
END_MESSAGE_MAP()


// CselectCameraDialog message handlers

void CselectCameraDialog::OnBnClickedOk()
{
	OnOK();
}

int CselectCameraDialog::selectCamera(CameraControl * cameracontrol)
{
	this->cameracontrol = cameracontrol;

	numSelectedCamera = 0;
	INT_PTR nResponse = DoModal();
	
	if (nResponse == IDOK)
	{
		//If user clicks 'connect', connect to the selected camera
		cameracontrol->connectCamera(numSelectedCamera);
		return 1;
	}
	else
		return 0;
}

BOOL CselectCameraDialog::OnInitDialog() 
{
	CDialog::OnInitDialog();

	//Get the list of cameras and add to the combo box
	if (cameracontrol->populateCameraList() == 0) 
	{
		OnCancel(); // No cameras connected.
	}
	else
	{
		cmbCameraList.Clear();
		for (int i=0; i<cameracontrol->getCameraCount(); i++)
		{
			cmbCameraList.AddString(cameracontrol->getCameraList(i).c_str());
		}	

		cmbCameraList.SetCurSel(0);
	}
	return true;
}
void CselectCameraDialog::OnCbnSelchangeComboCameralist()
{
	// Set the selected camera number so its' visible from the calling function.
	numSelectedCamera = cmbCameraList.GetCurSel();
}
