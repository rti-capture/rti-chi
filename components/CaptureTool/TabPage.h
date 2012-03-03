/*===========================================================================
    CaptureTool
    TabPage.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#ifndef  TABPAGE_H
#define TABPAGE_H


#include <QDialog>
#include <QTabWidget> 

QT_BEGIN_NAMESPACE
class QTabWidget;
QT_END_NAMESPACE



class TabPage: public QTabWidget
{
  Q_OBJECT

public:    

                                                TabPage                                 (QWidget* parent = 0);
                                                ~TabPage                                ();

    void                                        connectSignals                          (void);


public Q_SLOTS:

    void                                        changeTab                               (int index);
    

};
#endif // TABPAGE_H