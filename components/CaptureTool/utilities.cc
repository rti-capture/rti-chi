/*===========================================================================
    CaptureTool
    utilities.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QTextStream>
#include <ctime>
#include "utilities.h"
#include "globals.h"


//----------------------------------------------------------------
// Compare version strings
//----------------------------------------------------------------
bool isCompatible(QString version, QString reference)
{
    int  i;
    i = version.indexOf(".");    if (i >= 0) version.truncate(i);
    i = reference.indexOf(".");  if (i >= 0) reference.truncate(i);
    return version.toInt() <= reference.toInt();
}


// To implement
QString uid(void)
{
    return("uuid: 645bf18b-9974-4ccd-b668-f2dbab30075a");
}

//---------------------------------------------------------
// Read lines from a text file.
// Input: fName - full path to the filename
// Output: list of QStrings, each string is
// a line from the input text
//---------------------------------------------------------
std::vector<QString> readLinesFromFile(QString fName)
{
    std::vector<QString> keyList;

    QFile inFile(fName);
    if(inFile.open(QIODevice::ReadOnly)) {
        QTextStream streamIn (&inFile);
        QString key;
        while (!streamIn.atEnd()) {
            key = streamIn.readLine();
            if(!key.isEmpty())
                keyList.push_back(key);
        }
        inFile.close();
    }

    return(keyList);
}


void saveTextToFile (QString fName, QString msg)
{
    QFile outFile(fName);
    if(outFile.open(QIODevice::Append)) {
        QTextStream streamOut (&outFile);
        streamOut << msg;
        outFile.close();
    }
}


void emptywait (int seconds) 
{ 
   clock_t endtime; 

   endtime = clock () + seconds * CLOCKS_PER_SEC ; 

   while (clock() < endtime) 
   { 
      ;
   } 
}


bool makeDirectory (QString name)
{
    QDir result(name);
    if (!result.exists())
        if (!result.mkdir(name))
            return false;
    return true;
}


QString getUserPixelMapImage(QString filename)
{
    QFileInfo f(filename);
    QString path = f.absolutePath();
    path = path + "/" + "selected" + "/" + f.fileName();
    return(path);
}






