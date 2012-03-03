/*===========================================================================
    CaptureTool
    CustomSettingsWindow.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef _CUSTOMSETTINGSWINDOW_H
#define _CUSTOMSETTINGSWINDOW_H


#include <QDialog>
#include <map>

QT_BEGIN_NAMESPACE
class QIcon;
class QFileInfo;
class QListWidget;
class QListWidget;
class QLineEdit;
class QTreeWidget;
class QListWidgetItem;
class QTreeWidget;
class QPushButton;
class QDialogButtonBox;
class QTextEdit;
QT_END_NAMESPACE



class CustomSettingsWindow : public QDialog
{
    Q_OBJECT

public:
        CustomSettingsWindow (QTreeWidget *pEquipmentList, QWidget *parent = 0);
        void                setProcessSwitch                    (bool flag);


public Q_SLOTS:

        void                        saveCaptureSettings (void);
        void                        updateSavedSettings (int current, QStringList names);
        void                        switchSettings      (void);
        void                        updateNotes         (QString notes);


private:
    




        bool                        m_bProcessSwitch;

        QString                     m_root;

        QListWidget                 *m_pListWidget;

        QLineEdit                   *m_pNameLineEdit;

        QIcon                       *m_pIcon;

        QTextEdit                   *m_pSmallEditor;

        QTreeWidget                 *m_pEquipmentList;

        QPushButton                 *m_pSaveButton;
        QPushButton                 *m_pCancelButton;
        QPushButton                 *m_pCloseButton;
        QDialogButtonBox            *m_pButtonBox;


        QString                     getNotes                (void);
        void                        initButtons             (void);

};

#endif //_CUSTOMSETTINGSWINDOW_H