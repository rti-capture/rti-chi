/*===========================================================================
    CaptureTool
    XMLReader.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#ifndef _XMLREADER_H
#define _XMLREADER_H

#include <QDir>
#include <qdom.h> 

class StringDefs;


class XMLReader 
{

public:
                                        XMLReader                       (StringDefs *pStringDefs);
                                        ~XMLReader                      (void);

bool                                    read                            (QString path, QDomElement &root, QDomElement &node, QString type, QString version, QString name);

void                                    write                           (QDomElement &root, QString fname);
void                                    write                           (QString fname, QDomElement &root, QString type, QString version);

bool                                    load                            (QDomElement &node, QDomDocument &doc, QString fname, QString type, QString version);
void                                    update                          (QString fname, QString nodename, QString attribute, QString data, QString type, QString version);
QString                                 getData                         (QString fname, QString nodename, QString attribute, QString type, QString version);

bool                                    hasNode                         (QString fname, QString nodename, QString type, QString version);

private:

    StringDefs                  *m_pStringDefs;

};

#endif // XMLREADER_H

