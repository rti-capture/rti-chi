// hshfitUIDlg.h : header file
//

#pragma once

#include "../HSHfit/hsh_fitter.h"
#include <iostream>
#include <sstream>
#include "afxwin.h"

// ChshfitUIDlg dialog
class ChshfitUIDlg : public CDialog
{
// Construction
public:
	ChshfitUIDlg(CWnd* pParent = NULL);	// standard constructor

// Dialog Data
	enum { IDD = IDD_HSHFITUI_DIALOG };

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
	afx_msg void OnBnClickedButtonBrowseFolder();
public:
	afx_msg void OnEnChangeEditMainPath();
public:
	CString strMainPath;
public:
	afx_msg void OnBnClickedButtonLightFile();
public:
	CString strLightFile;
public:
	CString strCorrectionFile;
public:
	afx_msg void OnBnClickedButtonCorrectionFile();
public:
	afx_msg void OnBnClickedButtonValidate2();
private:
	HshFitter *hshfitter;
	CString strPrefix;
	CString strStatus;
	afx_msg void OnBnClickedButtonProcess();
	CButton btnProcess;
	int numOrder;
	CString strOutputFilename;
	BOOL bRowByRow;
//	BOOL bCompressedHSH;

	ostringstream output; // output buffer
	afx_msg void OnBnClickedButtonScan();
	afx_msg void OnBnClickedButtonRectifyFile();
	CString strRectifyFile;
	int numViews;
	int numLights;
	int numTotalViews;

	vector <string> strViewPrefix;


	CButton btnRectify;
	CButton btnValidate;
	afx_msg void OnBnClickedButtonRectify();
	CButton btnGenerateMRTI;
	afx_msg void OnBnClickedButtonMrti();
};
