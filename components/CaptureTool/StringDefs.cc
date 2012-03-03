/*===========================================================================
    CaptureTool
    StringDefs.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QDir>
#include "StringDefs.h"
#include "utilities.h"




StringDefs::StringDefs (void)
{
}


StringDefs::~StringDefs (void)
{
}


QString StringDefs::appdataPath (void)
{
    QString path("./appdata");
    makeDirectory(path);
    return(path);
}


QString StringDefs::iconPath (void)
{
    QString path(appdataPath() + "/"+ icons() + "/" + os() );
    makeDirectory(path);
    return(path);
}


QString StringDefs::defaultIcon (void)
{
    QString path = iconPath() + "/" + untitled() + "." + png();
    return(path);
}


QString StringDefs::getDataFileName(QString path, QString filename, QString local, QString category)
{
    QString fpath = path + "/" + local;
    makeDirectory(fpath);

    fpath = fpath +  "/" + category;
    makeDirectory(fpath);

    fpath = fpath + "/" "metadata";
    makeDirectory(fpath);

    // init the full path
    QString outfile = fpath + "/" + filename + "." + xml();

    return(outfile);
}


QString StringDefs::getDataPath(QString path, QString local)
{
    QString fpath = path + "/" + local;
    makeDirectory(fpath);

    return(fpath);
}

QString StringDefs::licenseInfo(void)
{
    QString info = "CaptureTool\nCopyright (C) Corey Toler-Franklin, 2011. All rights reserved. \n";
    info  = info + "contact: corey.tolerfranklin@gmail.com\n";
    return(info);
}