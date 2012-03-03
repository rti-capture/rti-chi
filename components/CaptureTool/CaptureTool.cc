/*===========================================================================
    Capture Tool
    CaptureTool.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/





#include <QApplication>
#include "globaldefs.h"
#include "TabSetWindow.h"
#include "IconWindow.h"
#include "StringDefs.h"




//---------------------------------------------------------
// main application loop
//---------------------------------------------------------
int main(int argc, char *argv[])
{

    StringDefs *pStringDefs = new StringDefs();
    fprintf(stderr, "%s", pStringDefs->licenseInfo().toLocal8Bit().data());

    QApplication app(argc, argv);
 
    // run the main window
    TabSetWindow tabsetwindow (pStringDefs, "fossil.xml");
    tabsetwindow.show();

    return app.exec();
}
