/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef LOADINGDLG_H
#define LOADINGDLG_H

#include <QDialog>
#include <QHBoxLayout>
#include <QProgressBar>
#include <QLabel>

//! Loading window
/*!
  The class defines and manages a window with a progress bar. 
*/
class LoadingDlg: public QDialog 
{

Q_OBJECT

// private data members
private:
	
	static QLabel* info; /*!< Label. */
	static QProgressBar* progress; /*!< Progress bar. */


public:

	//! Constructor
	LoadingDlg(QWidget *parent=0);

	/*!
	  Static method to update the label and the progress bar.
	  \param pos value of the pogress bar.
	  \param str label
	*/
	static bool QCallBack(int pos, QString str);
};

#endif /* LOADINGDLG_H */
