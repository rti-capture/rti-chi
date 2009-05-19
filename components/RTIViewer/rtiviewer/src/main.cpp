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
#include <QString>


int main( int argc, char ** argv )
{
	QApplication app( argc, argv );
	
	RtiViewerDlg *maindlg = new RtiViewerDlg();
	maindlg->show();
	if (argc > 1)
		maindlg->openFile(QString(argv[1]));
	return app.exec();
}
