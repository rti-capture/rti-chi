/*===========================================================================
    CaptureTool
    utilities.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include <qdom.h>

#ifndef UTILITIES_H
#define UTILITIES_H


#include<vector>
#include <QDir>

bool                                    isCompatible                    (QString version, QString reference);

std::vector<QString>                    readLinesFromFile               (QString fName);

void                                    saveTextToFile                  (QString fName, QString msg);

void                                    emptywait                       (int seconds);

bool                                    makeDirectory                   (QString name);

QString                                 getUserPixelMapImage            (QString filename);

QString                                 uid                             (void);
            
#endif // UTILITIES_H
