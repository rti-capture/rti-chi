/*===========================================================================
    CaptureTool
    DataTab.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef DATATAB_H
#define DATATAB_H


#include <QDialog>
#include <map>
#include <qdom.h> 

QT_BEGIN_NAMESPACE
class QDialogButtonBox;
class QFileInfo;
class QTabWidget;
class QLabel;
class QComboBox;
class QLineEdit;
QT_END_NAMESPACE

class GUIUtility;




class DataTab : public QWidget
{
    Q_OBJECT

public:

                                        DataTab                     (const QFileInfo &fileInfo, QWidget *parent = 0);


    void                                clear                       (void);
    void                                update                      (QString path, QString logfile);

    QString                             currentPath                 (void);
    QString                             dataPath                    (void);
    QString                             equipmentPath               (void);
    QString                             exportToXML                 (void);
    void                                exportToRDF                 (QString path);
    void                                writePathToRDF              (QString fname, QString data);
    void                                import                      (QDomElement root);

    void                                resetEquipmentFlag          (void);
    bool                                equipmentHasChanged         (void);
    QString                             equipmentRoot               (void);


public Q_SLOTS:

    void                                browseLogFile               (void);
    void                                browseProjectDir            (void);
    void                                browseEquipmentDir          (void);;
    void                                switchEquipmentDirectory    (int index);

private:
    
    GUIUtility                          *m_pGUIUtility;

    bool                                m_bUpdateEquipment;

    QLabel                              *m_pLogFileLabel;
    QLabel                              *m_pDirectoryLabel;
    QLabel                              *m_pEquipmentLabel;

    QComboBox                           *m_pDirectoryComboBox;
    QComboBox                           *m_pLogFileComboBox;
    QComboBox                           *m_pEquipmentComboBox;

    QPushButton                         *m_pStatsButton;
    QPushButton                         *m_pBrowseButton;
    QPushButton                         *m_pEquipmentBrowseButton;
    QPushButton                         *m_pLogFileBrowseButton;

    void                                openDirectory               (QComboBox *item, QString label);
    void                                openFile                    (QComboBox *item, QString label, QString filter);


};


#endif // DATATAB_H