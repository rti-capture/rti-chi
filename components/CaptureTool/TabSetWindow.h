/*===========================================================================
    CaptureTool
    TabSetWindow.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef TABSETWINDOW_H
#define TABSETWINDOW_H

#include <QDialog>
#include <map>
#include <qdom.h> 

QT_BEGIN_NAMESPACE
class QDialogButtonBox;
class QFileInfo;
class QTabWidget;
class QLabel;
class QComboBox;
class QListWidget;
class QListWidgetItem;
class QLineEdit;
class QTreeWidget;
QT_END_NAMESPACE

class GUIUtility;
class DataTab;
class ProjectTab;
class TabPage;
class SetUpTab;
class NotesTab;
class AboutTab;
class StringDefs;


class TabSetWindow : public QDialog
{
    Q_OBJECT

public:

                                TabSetWindow                        (StringDefs *pStringDefs, const QString &fileName, QWidget *parent = 0);

    void                        initTabs                            (const QString &fileName);
    void                        initButtonMenu                      (void);
    void                        disableButtons                      (bool bDisable);

    void                        updateEquipment                     (void);
    void                        updateProjectRoot                   (void);
    


public Q_SLOTS:

    void                        openCurDir                          (void);
    void                        saveProject                         (void);
    void                        saveAsProject                       (void);
    void                        openProject                         (void);
    void                        newProject                          (void);



private:

    StringDefs                          *m_pStringDefs;

    TabPage                             *m_pTabs;
    ProjectTab                          *m_pProjectTab;
    DataTab                             *m_pDataTab;
    SetUpTab                            *m_pSetUpTab;
    NotesTab                            *m_pNotesTab;
    AboutTab                            *m_pAboutTab;

    QPushButton                         *m_pOpenButton;
    QPushButton                         *m_pSaveButton;
    QPushButton                         *m_pSaveasButton;
    QPushButton                         *m_pCloseButton;
    QPushButton                         *m_pNewButton;
    QPushButton                         *m_pPrintButton;
    QPushButton                         *m_pHelpButton;
    QPushButton                         *m_pExitButton;


    QDialogButtonBox                    *buttonBox;

    QString                             m_titleformatString;

    void                                writeProjectKnowledge           (QString path);
    void                                writeProjectFile                (QString path, QString filename);
    void                                writeProjectFile                (void);
    void                                importProjectFile               (QString projectPath, QString fname);
    bool                                loadDataFromFile                (QString projectPath, QString fname);

    void                                exportToRDF                     (QString path);
    bool                                readXML                         (QString path, QDomElement &root);
    void                                createDigitizationProject       (QString path);
};






#endif
