/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef RTIVIEWERDLG_H
#define RTIVIEWERDLG_H

// Local headers
#include "ui_rtiviewer.h"
#include "rtiBrowser.h"
#include "lightControl.h"
#include "navigator.h"
#include "renderingdialog.h"
#include "rti.h"
#include "ptm.h"
#include "hsh.h"
#include "universalrti.h"
#include "httpthread.h"

// Qt headers
#include <QWidget>
#include <QPushButton>
#include <QToolBar>
#include <QAction>
#include <QFrame>
#include <QMutex>
#include <QWaitCondition>
#include <QSettings>
#include <QDir>

//! Main window class.
/*!
  The class defines and manages the main window.
*/
class RtiViewerDlg : public QWidget
{
	Q_OBJECT

// private data members
private:
	
	QFrame* browserFrame; /*!< Frame for the browser. */
	RtiBrowser* browser; /*!< Browser for RTI image. */
	LightControl* light; /*!< Widget to set the light direction. */

	QFrame* navFrame; /*!< Frame for the navigator. */
	Navigator* navigator; /*!< Widget to navigate the image. */
		
	QToolBar* toolBar; /*!< Toolbar. */
	QAction* openLocalAct; /*!< Action to open file from local disk. */
	QAction* openRemoteAct; /*!< Action to open file from remote disk. */
	QAction* aboutAct; /*!< Action to show about info. */
	QAction* configAct; /*!< Action to open application settings window. */
	QAction* snapshotAct; /*!< Action to save a snapshot of current RTI image. */

	
	RenderingDialog* rendDlg; /*!< Widget to choose the rendering mode to apply to RTI image. */

	QLineEdit* filename; /*!< Read-only text box to show the file-path of RTI image. */
	QLineEdit* filesize; /*!< Read-only text box to show the size of RTI image. */
	QLineEdit* fileformat; /*!< Read-only text box to show the format of RTI image. */

	HttpThread* getter; /*!< Secondary thread to get the RTI image from a remote server. */
	QMutex* mutex; /*!< Mutex to provide a mutual exclusion lock between the GUI thread and the HTTP thread. */
	QWaitCondition* infoReady; /*!< Wait condition to synchronize the GUI thread and the HTTP thread. */
	
	QSettings* settings; /*!< Application settings. */

	QDir dir; /*!< Current working directory. */


public:

	//! Constructor
	/*!
	  Constructs the main windows with parent \a parent. 
	  \param parent parent window.
	*/
	RtiViewerDlg(QWidget *parent=0);


// private methods
private:

	/*!
	  Parses xml file used in the visualization of remote RTI image.
	  \param b pointer to file buffer
	  \param level reference to the destination of image level info.
	  \param w refecence to the destination of image width info.
	  \param h reference to the destination of image height info.
	  \return \a true if the file is valid, \a false otherwise.
	*/
	bool parseXml(QBuffer* b, int& level, int& w, int& h);

// private Qt slots
private slots:

	/*!
	  Manages error of HTTP thread.
	  \param error description of error
	*/
	void httpErrorOccurred(QString error);

// public Qt slots
public slots:

	/*!
	  Manages the opening of a file from the local disk. 
	  \return -1 if the opening fails, 0 otherwise
	*/
	int open();

	/*!
	  Manages the opening of a file from a remote disk.
	  \return -1 if the openig fails, 0 otherwise
	*/
	int openRemote();

	/*!
	  Shows the about dialog.
	*/
	void about();

	/*!
	  Shows and manages the application settings.
	*/
	void configure();

};

#endif /* RTIVIEWERDLG_H */

