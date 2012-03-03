/*===========================================================================
    CaptureTool
    AboutTab.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef _ABOUTTAB_H_
#define _ABOUTTAB_H_


#include <QDialog>
#include <map>


QT_BEGIN_NAMESPACE
class QFileInfo;
class QLabel;
QT_END_NAMESPACE

class StringDefs;


class AboutTab : public QWidget
{
    Q_OBJECT

public:

                    AboutTab                            (StringDefs *pStringDefs, const QFileInfo &fileInfo, QWidget *parent = 0);
                    ~AboutTab                           (void);

    void            exportToRDF                         (QString path);

private:

    QLabel *    m_pAbout;
    StringDefs  *m_pStringDefs;

};

#endif //_ABOUTTAB_H_