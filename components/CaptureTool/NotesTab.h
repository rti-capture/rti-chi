/*===========================================================================
    CaptureTool
    NotesTab.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef _NOTESTAB_H_
#define _NOTESTAB_H_


#include <QDialog>
#include <map>
#include <qdom.h> 

QT_BEGIN_NAMESPACE
class QFileInfo;
class QTextEdit;
QT_END_NAMESPACE


class NotesTab : public QWidget
{
    Q_OBJECT

public:
                                        NotesTab                                (const QFileInfo &fileInfo, QWidget *parent = 0);

    QString                             exportToXML                             (void);
    void                                exportToRDF                             (QString path);
    void                                import                                  (QDomElement root);

    void                                clear                                   (void);
    QString                             getText                                 (void);

private:
    QTextEdit  *m_pTextEditor;
};

#endif //_NOTESTAB_H_