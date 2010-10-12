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
#include <QThread>

#include <omp.h>
#include "util.h"
#include "SysInfo.h"

int main( int argc, char ** argv )
{
	QApplication app( argc, argv );

#if _MSC_VER

	MEMORYSTATUSEX statex;
	statex.dwLength = sizeof (statex);
	GlobalMemoryStatusEx (&statex);
	printf ("There are %d total Mbytes of virtual memory.\n", statex.ullTotalVirtual/(1024*1024));
	printf ("There are %d free Mbytes of virtual memory.\n", statex.ullAvailVirtual/(1024*1024));

	omp_set_num_threads(getProcessorCount());
#endif
	RtiViewerDlg *maindlg = new RtiViewerDlg();
	maindlg->show();
	FileOpenEater *filterObj = new FileOpenEater(maindlg);
	app.installEventFilter(filterObj);
	app.processEvents();
	if (argc > 1)
		maindlg->openFile(QString(argv[1]));
	return app.exec();
}
