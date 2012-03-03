/*===========================================================================
    CaptureTool
    TemplateSetupWindow.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#ifndef _TEMPLATESETUPWINDOW_H_
#define _TEMPLATESETUPWINDOW_H_


#include <QIcon>
#include <QList>
#include <QMainWindow>
#include <QPixmap>


QT_BEGIN_NAMESPACE
class QAction;
class QActionGroup;
class QGroupBox;
class QMenu;
class QRadioButton;
class QTableWidget;
class QTableWidgetItem;
class IconSelectionArea;
class QWidget;
class QDialogButtonBox;
class QPushButton;
class QGridLayout;
QT_END_NAMESPACE

class CustomTemplateSetup;
class StringDefs;


class TemplateSetupWindow : public QMainWindow
{
    Q_OBJECT

public:

                                TemplateSetupWindow         (StringDefs *pStringDefs, QString equipmentroot, QString projectroot, QString category, QString basename, QWidget *parent = 0);
                                ~TemplateSetupWindow        (void);

    void                        newTemplate                 (void);


private slots:
    void test(void);
    void                        saveTemplate                (void);



private:

    StringDefs                  *m_pStringDefs;

    QString                     m_equipmentroot;
    QString                     m_projectroot;
    QString                     m_basename;
    QString                     m_category;

    QPushButton                 *m_pCancelButton;
    QPushButton                 *m_pCloseButton;
    QPushButton                 *m_pApplyButton;
    QDialogButtonBox            *m_pButtonBox;

    QWidget                     *m_pCentralWidget;

    CustomTemplateSetup         *m_pCustomTemplateSetup;

    QGridLayout                 *m_pMainLayout;

    void                        initButtons                 (void);
};

#endif //_TEMPLATESETUPWINDOW_H_
