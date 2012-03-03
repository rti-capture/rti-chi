/*===========================================================================
    CaptureTool
    SetUpTab.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef _SETUPTAB_H_
#define _SETUPTAB_H_


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
class QCheckBox;
class QGroupBox;
QT_END_NAMESPACE

class CustomTemplatesWindow;
class CustomSettingsWindow;
class GUIUtility;
class AccessoryDialog;
class StringDefs;

class SetUpTab : public QWidget
{
    Q_OBJECT

public:

                                        SetUpTab                    (StringDefs *pStringDefs, const QFileInfo &fileInfo, QString equipmentroot, QWidget *parent = 0);
    QPushButton                         *button                     (const QString &label, const char *item);


    QString                             exportToXML                 (QString name, QString notes);
    void                                exportToRDF                 (QString path);
    void                                import                      (QString projectPath, QString equipmentPath, QDomElement root);

    void                                deviceToRDF                 (QString path);

    void                                clear                       (void);

    void                                updateEquipment             (QString root);
    QString                             equipmentRoot               (void);
    void                                setProjectRoot              (QString root);

    void                                checkAccessories            (void);
 
    void                                processSwitch               (QString name);
    void                                updateEquipmentList         (void);

    void                                enableSavedSettings         (bool flag);
    void                                enableCustomTemplates       (bool flag);


public Q_SLOTS:

    void                                saveSetup                       (void);
    void                                saveCustomTemplates             (void);
    void                                lightAccessory                  (void);
    void                                monopodAccessory                (void);
    void                                ballheadAccessory               (void);
    void                                cableAccessory                  (void);
    void                                sphereAccessory                 (void);
    void                                otherAccessory                  (void);
    void                                resetDefaults                   (void);
    void                                exportSettings                  (QString name, QString notes);
    void                                switchSettings                  (int index);
    void                                switchSettings_user              (int index);
    void                                manageCustomTemplates           (void);


protected:

    QString                             m_root;
    QString                             m_projectRoot;

    QGroupBox                           *m_pTypeGr;
    QGroupBox                           *m_pLightsGr;
    QGroupBox                           *m_pFiltersGr;
    QGroupBox                           *m_pCalibrationGr;
    QGroupBox                           *m_pSetUpGr;
    QGroupBox                           *m_pTimeGr;
    QGroupBox                           *m_pSettingsGr;

    QComboBox                           *m_pCamerasComboBox;
    QComboBox                           *m_pLightsComboBox;
    QComboBox                           *m_pTripodComboBox;
    QComboBox                           *m_pLensComboBox;
    QComboBox                           *m_pFiltersComboBox;
    QComboBox                           *m_pColorComboBox;
    QComboBox                           *m_pSetupComboBox;

    QCheckBox                           *m_pYesstringck;
    QCheckBox                           *m_pYesdomesck;
    QCheckBox                           *m_pYesCamerack;
    QCheckBox                           *m_pYesck;  
    QCheckBox                           *m_pYesfiltersck;

    void                                writeSettingsFile               (QString path, QString filename, QString username, QString notes);

    void                                parseNode                       (QDomElement node);

    void                                updateUI                        (void);
    void                                loadComboBoxes                  (void);
    void                                loadSetUpComboBox               (void);
    void                                loadFilterOptions               (void);
    void                                loadColorOptions                (void);

private:


    StringDefs                          *m_pStringDefs;

    QString                             m_notes;

    GUIUtility                          *m_pGUIUtility;





    QLabel                              *m_pLabelPower;

    QLineEdit                           *m_pEditDescription;
    QLineEdit                           *m_pEditPower;

    QLineEdit                           *m_pEditTime;
    QLineEdit                           *m_pEditString;

    CustomSettingsWindow                *m_pCustomSettingsWindow; 
    CustomTemplatesWindow               *m_pCustomTemplatesWindow;
    QTreeWidget                         *m_pEquipmentList;

    QPushButton                         *m_pMonopods;
    QPushButton                         *m_pBallheads;
    QPushButton                         *m_pCables;
    QPushButton                         *m_pSpheres;
    QPushButton                         *m_pOtherAccess;
    QPushButton                         *m_pLightsAccess;
    QPushButton                         *m_pSavesetupButton;
    QPushButton                         *m_pTemplatesButton;

    QPushButton                         *m_pResetdefaultsButton;

    void                                initLabels                    (void);

    QString                             m_lightDescription;
    QString                             m_power;
    QString                             m_stringLength;
    QString                             m_time;
    QString                             m_light;
    QString                             m_rights;
    QString                             m_tripod;
    QString                             m_lens;
    QString                             m_catalog;
    QString                             m_camera;
    QString                             m_captureType;
    QString                             m_cameraFileName;
    QString                             m_lensFileName;
    QString                             m_tripodFileName;
    QString                             m_lightsFileName;
    QString                             m_color;
    QString                             m_filters;
    QString                             m_setup;


    std::vector<QString>                m_lightAccessoryTitles;
    std::vector<QString>                m_lightAccessoryFiles;
    std::vector<QString>                m_monopodAccessoryTitles;
    std::vector<QString>                m_monopodAccessoryFiles;
    std::vector<QString>                m_ballheadAccessoryTitles;
    std::vector<QString>                m_ballheadAccessoryFiles;
    std::vector<QString>                m_cableAccessoryTitles;
    std::vector<QString>                m_cableAccessoryFiles;;
    std::vector<QString>                m_sphereAccessoryTitles;
    std::vector<QString>                m_sphereAccessoryFiles;
    std::vector<QString>                m_miscAccessoryTitles;
    std::vector<QString>                m_miscAccessoryFiles;

    QList<QListWidget *>                m_ballheadAccessoryList;
    QList<QListWidget *>                m_cableAccessoryList;
    QList<QListWidget *>                m_lightAccessoryList;
    QList<QListWidget *>                m_monopodAccessoryList;
    QList<QListWidget *>                m_sphereAccessoryList;
    QList<QListWidget *>                m_riggingAccessoryList;
    QList<QListWidget *>                m_miscAccessoryList;

    QList<QStringList *>                m_ballheadMap;
    QList<QStringList *>                m_cableMap;
    QList<QStringList *>                m_lightMap;
    QList<QStringList *>                m_monoMap;
    QList<QStringList *>                m_sphereMap;
    QList<QStringList *>                m_miscMap;


    AccessoryDialog                     *m_pAccessoryDialog_ballhead;
    AccessoryDialog                     *m_pAccessoryDialog_cable;
    AccessoryDialog                     *m_pAccessoryDialog_light;
    AccessoryDialog                     *m_pAccessoryDialog_monopod;
    AccessoryDialog                     *m_pAccessoryDialog_sphere;
    AccessoryDialog                     *m_pAccessoryDialog_misc;

    QTreeWidget                         *initEquipmentList              (void);


    void                                initMonopodAccessories          (void);

    void                                addTreeWidgetsToList            (QTreeWidget *pTree, std::vector<QString> *pTitles, QList<QListWidget *> *pWidgetList, int child, GUIUtility *pGUIUtil);



    void                                enableAccessories               (bool bEnable);
    void                                initAccessoryOptions            (QString root);
    void                                initEquipmentFiles              (QString root);
    void                                accessoryDialog                 (AccessoryDialog *pDialog, std::vector<QString> files, std::vector<QString> titles, QList<QListWidget *> *pWidgetList, QList<QStringList *> *pMap);
    QString                             exportAccessory                 (std::vector<QString> *titles, QList<QListWidget *> *widgetList);
    QString                             accessoryToXML                  (QString name, QListWidget *list);
    void                                accessoryStatus                 (QList<QListWidget *> *pList, QStringList names);
    void                                clearAccessoryMaps              (void);



    QString                             getAttribute                    (QString fname, QString id, QString name);
    QString                             readAttribute                   (QDomElement &root, QString id, QString name);

    // fix this
    void                                switchSettingHere               (QDomElement  root);

};




#endif //_SETUPTAB_H_