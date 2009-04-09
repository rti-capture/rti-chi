// hshfitUIDlg.cpp : implementation file
//

#include "stdafx.h"
#include "hshfitUI.h"
#include "hshfitUIDlg.h"

#include "boost/filesystem.hpp"  

using namespace boost::filesystem;                                         


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


// ChshfitUIDlg dialog




ChshfitUIDlg::ChshfitUIDlg(CWnd* pParent /*=NULL*/)
	: CDialog(ChshfitUIDlg::IDD, pParent)
	, strMainPath(_T("c:/prabath/data/vase"))
	, strLightFile(_T(""))
	, strCorrectionFile(_T(""))
	, strPrefix(_T("v"))
	, strStatus(_T(""))
	, numOrder(3)
	, strOutputFilename(_T(""))
	, bRowByRow(TRUE)
	, strRectifyFile(_T(""))
	, numViews(1)
	, numLights(90)
{
	m_hIcon = AfxGetApp()->LoadIcon(IDR_MAINFRAME);
}

void ChshfitUIDlg::DoDataExchange(CDataExchange* pDX)
{
	CDialog::DoDataExchange(pDX);
	DDX_Text(pDX, IDC_EDIT_MAIN_PATH, strMainPath);
	DDX_Text(pDX, IDC_EDIT_LIGHT_FILE, strLightFile);
	DDX_Text(pDX, IDC_EDIT_CORRECTION_FILE, strCorrectionFile);
	DDX_Text(pDX, IDC_EDIT_PREFIX, strPrefix);
	DDX_Text(pDX, IDC_EDIT_STATUS, strStatus);
	DDX_Control(pDX, IDC_BUTTON_PROCESS, btnProcess);
	DDX_Text(pDX, IDC_EDIT_HSH_ORDER, numOrder);
	DDV_MinMaxInt(pDX, numOrder, 0, 5);
	DDX_Text(pDX, IDC_EDIT_OUTPUT_FILENAME, strOutputFilename);
	DDX_Check(pDX, IDC_CHECK_ROW_BY_ROW, bRowByRow);
	DDX_Text(pDX, IDC_EDIT_RECTIFY_FILE, strRectifyFile);
	DDX_Text(pDX, IDC_EDIT_VIEWS, numViews);
	DDV_MinMaxInt(pDX, numViews, 1, 360);
	DDX_Text(pDX, IDC_EDIT_LIGHTS, numLights);
	DDV_MinMaxInt(pDX, numLights, 1, 200);
	DDX_Control(pDX, IDC_BUTTON_RECTIFY, btnRectify);
	DDX_Control(pDX, IDC_BUTTON_VALIDATE2, btnValidate);
	DDX_Control(pDX, IDC_BUTTON_MRTI, btnGenerateMRTI);
}

BEGIN_MESSAGE_MAP(ChshfitUIDlg, CDialog)
	ON_WM_SYSCOMMAND()
	ON_WM_PAINT()
	ON_WM_QUERYDRAGICON()
	//}}AFX_MSG_MAP
	ON_BN_CLICKED(IDC_BUTTON_BROWSE_FOLDER, &ChshfitUIDlg::OnBnClickedButtonBrowseFolder)
	ON_EN_CHANGE(IDC_EDIT_MAIN_PATH, &ChshfitUIDlg::OnEnChangeEditMainPath)
	ON_BN_CLICKED(IDC_BUTTON_LIGHT_FILE, &ChshfitUIDlg::OnBnClickedButtonLightFile)
	ON_BN_CLICKED(IDC_BUTTON_CORRECTION_FILE, &ChshfitUIDlg::OnBnClickedButtonCorrectionFile)
	ON_BN_CLICKED(IDC_BUTTON_VALIDATE2, &ChshfitUIDlg::OnBnClickedButtonValidate2)
	ON_BN_CLICKED(IDC_BUTTON_PROCESS, &ChshfitUIDlg::OnBnClickedButtonProcess)
	ON_BN_CLICKED(IDC_BUTTON_SCAN, &ChshfitUIDlg::OnBnClickedButtonScan)
	ON_BN_CLICKED(IDC_BUTTON_RECTIFY_FILE, &ChshfitUIDlg::OnBnClickedButtonRectifyFile)
	ON_BN_CLICKED(IDC_BUTTON_RECTIFY, &ChshfitUIDlg::OnBnClickedButtonRectify)
	ON_BN_CLICKED(IDC_BUTTON_MRTI, &ChshfitUIDlg::OnBnClickedButtonMrti)
END_MESSAGE_MAP()


// ChshfitUIDlg message handlers

BOOL ChshfitUIDlg::OnInitDialog()
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

	// TODO: Add extra initialization here

	return TRUE;  // return TRUE  unless you set the focus to a control
}

void ChshfitUIDlg::OnSysCommand(UINT nID, LPARAM lParam)
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

void ChshfitUIDlg::OnPaint()
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
HCURSOR ChshfitUIDlg::OnQueryDragIcon()
{
	return static_cast<HCURSOR>(m_hIcon);
}


void ChshfitUIDlg::OnBnClickedButtonBrowseFolder()
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
				strMainPath = szPath;
			}
			pMalloc->Free(pidl);
		}	
		pMalloc->Release();
	}

	UpdateData(false);
}

void ChshfitUIDlg::OnEnChangeEditMainPath()
{
	// TODO:  If this is a RICHEDIT control, the control will not
	// send this notification unless you override the CDialog::OnInitDialog()
	// function and call CRichEditCtrl().SetEventMask()
	// with the ENM_CHANGE flag ORed into the mask.

	// TODO:  Add your control notification handler code here
}

void ChshfitUIDlg::OnBnClickedButtonLightFile()
{
	UpdateData(true);
	CFileDialog dlgFile(true,"*.lp",strLightFile, OFN_OVERWRITEPROMPT ,"Light Config Files (*.lp)|*.lp|");
	if (dlgFile.DoModal()==IDOK) 
	{
		strLightFile = dlgFile.GetFileName();
	}
	UpdateData(false);
}

void ChshfitUIDlg::OnBnClickedButtonCorrectionFile()
{
	UpdateData(true);
	CFileDialog dlgFile(true,"*.dat",strCorrectionFile, OFN_OVERWRITEPROMPT ,"Color Correction Files (*.dat)|*.dat|");
	if (dlgFile.DoModal()==IDOK) 
	{
		strCorrectionFile = dlgFile.GetFileName();
	}
	UpdateData(false);
}

std::string tostr(CString instr)
{
	  // Convert a TCHAR string to a LPCSTR
  CT2CA pszConvertedAnsiString (instr);

  // construct a std::string using the LPCSTR input
  return std::string (pszConvertedAnsiString);
}

void ChshfitUIDlg::OnBnClickedButtonValidate2()
{ 
	UpdateData(true);
	hshfitter = new HshFitter(tostr(strMainPath), strViewPrefix.at(0), tostr(strLightFile), tostr(strCorrectionFile), (ofstream *)&output);
	hshfitter->set_order(numOrder);
	if (hshfitter->read_inputs()) 
	{
		strStatus = output.str().c_str();
		btnProcess.EnableWindow(true);
		btnRectify.EnableWindow(true);
	}
	else
	{
		btnProcess.EnableWindow(false);
		btnRectify.EnableWindow(false);
		strStatus = output.str().c_str();
		strStatus += " Path / Files invalid! \r\n";
	}
	delete hshfitter;
	UpdateData(false);
}

void ChshfitUIDlg::OnBnClickedButtonProcess()
{
	UpdateData(true);
	strStatus = "";
	strStatus.Format("Processing %d",numViews) ;
	path new_folder(tostr(strMainPath) + "/" + tostr(strPrefix) + "_" + tostr(strOutputFilename));
	create_directory(new_folder);
	for (int i=0; i<numViews; i++) 
	{

		hshfitter = new HshFitter(tostr(strMainPath), strViewPrefix.at(i), tostr(strLightFile), tostr(strCorrectionFile), (ofstream *)&output);
		hshfitter->set_order(numOrder);
		hshfitter->set_row_by_row(bRowByRow);

		string outputfilename = new_folder.string() + "/" + strViewPrefix.at(i) + "_" + tostr(strOutputFilename) + ".rti";
		
		hshfitter->set_output_filename(outputfilename);

		if (hshfitter->read_inputs()) 
		{
			hshfitter->compute_loop();
			strStatus += output.str().c_str();
			strStatus += outputfilename.c_str();
			strStatus += " saved! \r\n\r\n";
			output.clear();
		}
		else
			strStatus += "Error opening input files... \r\n";

		delete hshfitter;
	}
	btnProcess.EnableWindow(false);
	if (numViews>1) 
		btnGenerateMRTI.EnableWindow(true);
	else
		btnGenerateMRTI.EnableWindow(false);
	UpdateData(false);
}

void ChshfitUIDlg::OnBnClickedButtonScan()
{
	list<string> allfiles; // the list of files matching the prefix
	UpdateData(true);
	//hshfitter = new HshFitter(tostr(strMainPath), tostr(strPrefix), tostr(strLightFile), tostr(strCorrectionFile), (ofstream *)&output);
	int numAllFiles = find_file(tostr(strMainPath),tostr(strPrefix), allfiles,false);
	if (numAllFiles>0) 
	{
		allfiles.sort();

		strStatus.Format("%d",numAllFiles);
		strStatus += " files found! \r\n";

		string filename = allfiles.front();
		int currentpos = strPrefix.GetLength();
		if (filename.substr(0,currentpos) == tostr(strPrefix))
		{
			//string left = filename.substr(currentpos,filename.length()-currentpos);
			if (filename.at(currentpos) == '_')  
				currentpos++;

			strStatus.Append("*");

			int startviewpos = currentpos;
			while (filename.at(currentpos)>='0' && filename.at(currentpos)<='9') {
				strStatus += (filename.at(currentpos));
				currentpos++;
			}
			int endviewpos = currentpos - 1;

			int startlightpos, endlightpos;
			if (filename.at(currentpos) == '.') 
			{
				startlightpos = startviewpos;
				endlightpos = endviewpos;
				endviewpos = startviewpos-1;
			}
			else
			{
				currentpos++;

				strStatus.Append("$");

				startlightpos = currentpos;
				while (filename.at(currentpos)>='0' && filename.at(currentpos)<='9') {
					strStatus += (filename.at(currentpos));
					currentpos++;
				}
				endlightpos = currentpos - 1;
			}


			string viewpointprefix = filename.substr(0,endviewpos+1);
			list <string> lightfiles;
			int numLightFiles = find_file(tostr(strMainPath), viewpointprefix, lightfiles, false);

			CString strN;
			strN.Format("%d",numLightFiles);

			strStatus += " Number of lighting conditions " + strN;

			numTotalViews = numAllFiles/numLightFiles;
			strN.Format("%d",numTotalViews);

			strStatus += " Number of view points " + strN;

			int count = 0;
			int viewcount = 0;
			string oldviewpointprefix = "";
			//strViewPrefix.resize(numTotalViews);
			for (list<string>::iterator it=allfiles.begin() ; it != allfiles.end(); it++ )
			{
				viewpointprefix = (*it).substr(0,endviewpos+1);
				if (viewpointprefix.compare(oldviewpointprefix) != 0)
				{
					strViewPrefix.push_back(viewpointprefix);
					viewcount++;
					strStatus += viewpointprefix.c_str();
					strStatus += "   ";
				}
				count++;

				oldviewpointprefix = viewpointprefix;
			}

			numViews = numTotalViews;
			numLights = numLightFiles;

			btnProcess.EnableWindow(false);
			btnRectify.EnableWindow(false);
			btnValidate.EnableWindow(true);

		}
		else
			strStatus += "Something is wrong";

	}
	else 
	{
		strStatus = "No files found! \r\n";
		btnProcess.EnableWindow(false);
		btnValidate.EnableWindow(false);
		btnRectify.EnableWindow(false);
	}

	UpdateData(false);
}

void ChshfitUIDlg::OnBnClickedButtonRectifyFile()
{
	// TODO: Add your control notification handler code here
}

void ChshfitUIDlg::OnBnClickedButtonRectify()
{
	// TODO: Add your control notification handler code here
}

void ChshfitUIDlg::OnBnClickedButtonMrti()
{
	// TODO: Add your control notification handler code here
}
