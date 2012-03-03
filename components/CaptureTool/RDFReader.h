/*===========================================================================
    Capture Tool
    RDFReader.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#ifndef _RDFREADER_H
#define _RDFREADER_H

#include <QDir>

class StringDefs;


class RDFReader 
{

public:
                                        RDFReader                       (StringDefs *pStringDefs);
                                        ~RDFReader                      (void);

    QString                             header                          (void);

    QString                             time                            (QString startdatetime, QString enddatetime);
    QString                             room                            (QString data);
    QString                             person                          (QString uid, QString firstname, QString lastname, QString legalbody);
    QString                             device                          (QString url_main, int serialnumber, QString preferedlabel, QString url_type, QString url_model, QString url_maker, QString note);
    QString                             license                         (QString data);
    QString                             subject                         (QString uid, QString description, QString id, QString url_type);
    QString                             software                        (void);

private:

    StringDefs                          *m_pStringDefs;

};

#endif // RDFREADER_H

