/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

// Local headers
#include "gui.h"

// Qt headers
#include <QApplication>
#include <QObject>


int main( int argc, char ** argv )
{
	QApplication app( argc, argv );
	
	RtiViewerDlg *maindlg = new RtiViewerDlg();
	QObject::connect(&app, SIGNAL(lastWindowClosed()), &app, SLOT(quit()));
	return maindlg->exec();
}
