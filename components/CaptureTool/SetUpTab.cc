/*===========================================================================
    CaptureTool
    SetUpTab.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include <QtGui>
#include <assert.h>
#include <utility>
#include "globals.h"
#include "SetUpTab.h"
#include "AccessoryDialog.h"
#include "utilities.h"
#include "GUIUtility.h"
//#include "treemodel.h"
#include "CustomSettingsWindow.h"
#include "CustomTemplatesWindow.h"
#include "TabSetWindow.h"
#include "StringDefs.h"
#include "RDFReader.h"


//------------------------------------------------------------
// Equipment settings
//------------------------------------------------------------
SetUpTab::SetUpTab(StringDefs *pStringDefs, const QFileInfo &fileInfo, QString equipmentroot, QWidget *parent)
    :   QWidget(parent),
        m_pCustomSettingsWindow                 (NULL),
        m_pCustomTemplatesWindow                (NULL),
        m_pEquipmentList                        (NULL),
        m_pAccessoryDialog_ballhead             (NULL),
        m_pAccessoryDialog_cable                (NULL),
        m_pAccessoryDialog_light                (NULL),
        m_pAccessoryDialog_monopod              (NULL),
        m_pAccessoryDialog_sphere               (NULL),
        m_pAccessoryDialog_misc                 (NULL),
        m_root                                  (equipmentroot), 
        m_pStringDefs                           (pStringDefs)
{


    m_pGUIUtility = new GUIUtility();

    m_ballheadMap.append(new QStringList);
    m_cableMap.append(new QStringList);
    m_cableMap.append(new QStringList);
    m_lightMap.append(new QStringList);
    m_lightMap.append(new QStringList);
    m_monoMap.append(new QStringList);
    m_monoMap.append(new QStringList);
    m_sphereMap.append(new QStringList);
    m_miscMap.append(new QStringList);
    m_miscMap.append(new QStringList);

    // files
    if(m_root.isNull())
        m_root = ".";
    m_projectRoot = ".";
    initEquipmentFiles(m_root);

    //labels
    QLabel *lcamera = new QLabel(tr("camera:"));
    QLabel *llens = new QLabel(tr("lens:"));
    QLabel *ltripod = new QLabel(tr("tripod:"));

    // group boxes
    m_pTypeGr = new QGroupBox(tr("Type"));
    m_pLightsGr = new QGroupBox(tr("Lights"));
    m_pFiltersGr = new QGroupBox(tr("Filters"));
    m_pCalibrationGr = new QGroupBox(tr("Calibration"));
    m_pSetUpGr = new QGroupBox(tr("Support"));
    m_pTimeGr = new QGroupBox(tr("Duration"));
    m_pSettingsGr = new QGroupBox(tr("Capture Settings"));

    // check boxes
    m_pYesck = new QCheckBox(tr("Yes"));
    m_pYesck->setChecked(false);
    m_pYesfiltersck = new QCheckBox(tr("Yes"));
    m_pYesfiltersck->setChecked(false);
    m_pYesCamerack = new QCheckBox(tr("Camera"));
    m_pYesCamerack->setChecked(true);
    m_pYesdomesck = new QCheckBox(tr("Dome"));
    m_pYesdomesck->setChecked(false);
    m_pYesdomesck->setEnabled(false);
    m_pYesstringck = new QCheckBox(tr("String"));
    m_pYesstringck->setChecked(false);

    // combo boxes
    m_pColorComboBox = new QComboBox;
    m_pColorComboBox->setEditable(true);
    m_pColorComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pFiltersComboBox = new QComboBox;
    m_pFiltersComboBox->setEditable(true);
    m_pFiltersComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pCamerasComboBox = new QComboBox;
    m_pCamerasComboBox->setEditable(true);
    m_pCamerasComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pLensComboBox = new QComboBox;
    m_pLensComboBox->setEditable(true);
    m_pLensComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pTripodComboBox = new QComboBox;
    m_pTripodComboBox->setEditable(true);
    m_pTripodComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pLightsComboBox = new QComboBox;
    m_pLightsComboBox->setEditable(true);
    m_pLightsComboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);

    m_pSetupComboBox = new QComboBox;
    m_pSetupComboBox->setEditable(false);
    m_pSetupComboBox->resize(10,20);

    // add buttons
    m_pSavesetupButton = button(tr("&Save Setup"), SLOT(saveSetUp()));
    m_pResetdefaultsButton = new QPushButton(tr("&Reset Defaults"));
    m_pTemplatesButton = new QPushButton(tr("&Templates"));
    m_pSavesetupButton->setDisabled(true);
    
    // space
    QSpacerItem *place1 = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);
    QSpacerItem *place2 = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);
    QSpacerItem *place3 = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);
    QSpacerItem *place4 = new QSpacerItem(40, 20, QSizePolicy::Expanding, QSizePolicy::Minimum);

    // add the widgets
    QGridLayout *calibrationLayout = new QGridLayout;
    calibrationLayout->addWidget(m_pYesck, 0, 0);
    calibrationLayout->addWidget(m_pColorComboBox, 0, 1);
    m_pCalibrationGr->setLayout(calibrationLayout);

    QGridLayout *filtersLayout = new QGridLayout;
    filtersLayout->addWidget(m_pYesfiltersck, 0, 0);
    filtersLayout->addWidget(m_pFiltersComboBox, 0, 1);
    m_pFiltersGr->setLayout(filtersLayout);

    QGridLayout *typeLayout = new QGridLayout;
    typeLayout->addWidget(m_pYesCamerack, 0, 0);
    typeLayout->addWidget(m_pYesdomesck, 0, 1);
    typeLayout->addWidget(lcamera, 0, 2);
    typeLayout->addWidget(m_pCamerasComboBox, 0, 3);
    typeLayout->addWidget(llens, 1, 2);
    typeLayout->addWidget(m_pLensComboBox, 1, 3);
    typeLayout->addWidget(ltripod, 3, 2);
    typeLayout->addWidget(m_pTripodComboBox, 3, 3);
    m_pTypeGr->setLayout(typeLayout);

    QGridLayout *lightsLayout = new QGridLayout;
    QLabel *labelDescription = new QLabel(tr("Description:"));
    m_pEditDescription = new QLineEdit(m_lightDescription);
    m_pLabelPower = new QLabel(tr("Power:"));
    m_pEditPower = new QLineEdit(m_power);
    m_pLightsAccess = button(tr("&Accessories"), SLOT(lightAccessory()));
    m_pLightsAccess->setDisabled(true);

    //lightsLayout->setColumnStretch(0, 3);
    lightsLayout->addWidget(m_pLightsComboBox, 0, 1);
    lightsLayout->addWidget(labelDescription, 1, 0);
    lightsLayout->addWidget(m_pEditDescription, 1, 1);
    lightsLayout->addWidget(m_pLabelPower, 1, 2);
    lightsLayout->addWidget(m_pEditPower, 1, 3);

    lightsLayout->addWidget(m_pLightsAccess, 0, 0);
    m_pLightsGr->setLayout(lightsLayout);

    QGridLayout *setUpLayout = new QGridLayout;
    QLabel *labelString = new QLabel(tr("String Length:"));
    m_pEditString = new QLineEdit(m_stringLength);
    m_pMonopods = button(tr("&Monopods"), SLOT(monopodAccessory()));
    m_pBallheads = button(tr("&Ball Heads"), SLOT(ballheadAccessory()));
    m_pCables = button(tr("&Cables"), SLOT(cableAccessory()));
    m_pSpheres = button(tr("&Spheres"), SLOT(sphereAccessory()));
    m_pOtherAccess = button(tr("&Other"), SLOT(otherAccessory()));
    enableAccessories(false);


    loadColorOptions();
    loadFilterOptions();


    setUpLayout->addWidget(m_pYesstringck, 0, 0);
    setUpLayout->addWidget(labelString, 0, 1);
    setUpLayout->addWidget(m_pEditString, 0, 2);
    setUpLayout->addWidget(m_pMonopods, 1, 0);
    setUpLayout->addWidget(m_pBallheads, 1, 1);
    setUpLayout->addWidget(m_pSpheres, 1, 2);
    setUpLayout->addWidget(m_pCables, 1, 3);
    setUpLayout->addWidget(m_pOtherAccess, 1, 4);
    m_pSetUpGr->setLayout(setUpLayout);

    QGridLayout *timeLayout = new QGridLayout;
    QLabel *labelTime = new QLabel(tr("Time:"));
    m_pEditTime = new QLineEdit;
    timeLayout->addWidget(labelTime, 0, 0);
    timeLayout->addWidget(m_pEditTime, 0, 1);
    m_pTimeGr->setLayout(timeLayout);

    QGridLayout *settingsLayout = new QGridLayout;
    settingsLayout->setColumnStretch(0, 0);
    settingsLayout->addWidget(m_pSetupComboBox, 0, 0);
    settingsLayout->addWidget(m_pSavesetupButton, 0, 1);
    settingsLayout->addWidget(m_pResetdefaultsButton, 0, 2);
    settingsLayout->addWidget(m_pTemplatesButton, 0, 3);
    m_pSettingsGr->setLayout(settingsLayout);

    // main layout
    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(m_pTimeGr);
    mainLayout->addWidget(m_pTypeGr);
    mainLayout->addWidget(m_pLightsGr);
    mainLayout->addWidget(m_pFiltersGr);
    mainLayout->addWidget(m_pCalibrationGr);
    mainLayout->addWidget(m_pSetUpGr);
    mainLayout->addWidget(m_pSettingsGr);
    mainLayout->addStretch(1);
    setLayout(mainLayout);

    m_pTemplatesButton->setDisabled(true);

    // connect buttons
    connect(m_pSavesetupButton, SIGNAL(clicked()), this, SLOT(saveSetup()));
    connect(m_pTemplatesButton, SIGNAL(clicked()), this, SLOT(manageCustomTemplates()));   
    connect(m_pResetdefaultsButton, SIGNAL(clicked()), this, SLOT(resetDefaults()));
    connect(m_pSetupComboBox, SIGNAL(activated(int)), this, SLOT(switchSettings_user(int)));
}


void SetUpTab::enableAccessories(bool bEnable)
{
    m_pLightsAccess->setDisabled(!bEnable);
    m_pMonopods->setDisabled(!bEnable);
    m_pBallheads->setDisabled(!bEnable);
    m_pCables->setDisabled(!bEnable);
    m_pSpheres->setDisabled(!bEnable);
    m_pOtherAccess->setDisabled(!bEnable);

    if(m_lightAccessoryTitles.size() == 0)
        m_pLightsAccess->setDisabled(true);
    if(m_monopodAccessoryTitles.size() == 0)
        m_pMonopods->setDisabled(true);
    if(m_ballheadAccessoryTitles.size() == 0)
        m_pBallheads->setDisabled(true);
    if(m_cableAccessoryTitles.size() == 0)
        m_pCables->setDisabled(true);
    if(m_sphereAccessoryTitles.size() == 0)
        m_pSpheres->setDisabled(true);
    if(m_miscAccessoryTitles.size() == 0)
        m_pOtherAccess->setDisabled(true);
}


void SetUpTab::initEquipmentFiles(QString root)
{
    m_cameraFileName = root + "/cameras.txt";
    m_lensFileName   = root + "/lenses.txt";
    m_tripodFileName = root + "/tripods.txt";
    m_lightsFileName = root + "/lights.txt";
}


void SetUpTab::initLabels(void)
{
    m_lightDescription = "speedlite 580 EX II";
    m_power = "1/4 power";
    m_stringLength = "24 in";
    m_time = "1:00:00 - 5:30:00";
}


void SetUpTab::lightAccessory(void)
{
    accessoryDialog(m_pAccessoryDialog_light, m_lightAccessoryFiles, m_lightAccessoryTitles, &m_lightAccessoryList, &m_lightMap);
}
   

void SetUpTab::monopodAccessory(void)
{    
    accessoryDialog(m_pAccessoryDialog_monopod, m_monopodAccessoryFiles, m_monopodAccessoryTitles, &m_monopodAccessoryList, &m_monoMap);
}  


void SetUpTab::ballheadAccessory(void)
{
    accessoryDialog(m_pAccessoryDialog_ballhead, m_ballheadAccessoryFiles, m_ballheadAccessoryTitles, &m_ballheadAccessoryList, &m_ballheadMap);
}
   

void SetUpTab::cableAccessory(void)
{
    accessoryDialog(m_pAccessoryDialog_cable, m_cableAccessoryFiles, m_cableAccessoryTitles, &m_cableAccessoryList, &m_cableMap);
}
    

void SetUpTab::sphereAccessory(void)
{  
    accessoryDialog(m_pAccessoryDialog_sphere, m_sphereAccessoryFiles, m_sphereAccessoryTitles, &m_sphereAccessoryList, &m_sphereMap);
}


void SetUpTab::otherAccessory(void)
{
    accessoryDialog(m_pAccessoryDialog_misc, m_miscAccessoryFiles, m_miscAccessoryTitles, &m_miscAccessoryList, &m_miscMap);
}


void SetUpTab::resetDefaults(void)
{
    updateEquipment(m_root);
    loadFilterOptions();
    loadColorOptions();

}


void SetUpTab::manageCustomTemplates(void)
{
    fprintf(stderr, "manageCustomTemplates\n");
    if(!m_pCustomTemplatesWindow)
        m_pCustomTemplatesWindow = new CustomTemplatesWindow(m_pStringDefs, m_root, m_projectRoot, this);

     m_pCustomTemplatesWindow->show();
}

QPushButton *SetUpTab::button(const QString &label, const char *item)
{
    QPushButton *b = new QPushButton(label);
    connect(b, SIGNAL(clicked()), this, item);
    return b;
}


void SetUpTab::saveSetup(void)
{

    assert(m_pSetupComboBox);
    QStringList list  = m_pGUIUtility->getComboBoxItems(m_pSetupComboBox);
    updateEquipmentList();

    TabSetWindow *p = (TabSetWindow*) this->parent();

    if(m_pCustomSettingsWindow == NULL)
        m_pCustomSettingsWindow = new CustomSettingsWindow(m_pEquipmentList, this);
     
    m_pCustomSettingsWindow->setProcessSwitch(false);
    m_pCustomSettingsWindow->updateNotes(m_notes);
    m_pCustomSettingsWindow->updateSavedSettings(m_pSetupComboBox->currentIndex(), list);
    m_pCustomSettingsWindow->show();
    m_pCustomSettingsWindow->setProcessSwitch(true);
}




void SetUpTab::exportSettings(QString name, QString notes)
{


    //get the path to the setting
    QString usableFileName = name.replace(" ", "_");

    if(m_pSetupComboBox->findText(name) == -1)
        m_pSetupComboBox->addItem(name);
    m_pSetupComboBox->setCurrentIndex(m_pSetupComboBox->findText(name));

    // export the setting info as xml data
    writeSettingsFile(m_projectRoot, usableFileName + ".xml", name, notes);


}


QTreeWidget *SetUpTab::initEquipmentList(void)
{
    QTreeWidget *pTreeWidget = new QTreeWidget;

    pTreeWidget->setColumnCount(3);
    pTreeWidget->header()->resizeSection(0, 90);
    pTreeWidget->header()->resizeSection(1, 170);
    pTreeWidget->header()->resizeSection(2, 180);
    QStringList headers;
    headers << tr("Item") << tr("Selection") << tr("Description");
    pTreeWidget->setHeaderLabels(headers);

    return(pTreeWidget);
}


void SetUpTab::updateEquipment(QString root)
{
    m_root = root;

    initEquipmentFiles(root);
    initAccessoryOptions(root);
    loadComboBoxes();
    enableAccessories(true);
}

void SetUpTab::enableCustomTemplates(bool flag)
{
    m_pTemplatesButton->setEnabled(flag);
}


void SetUpTab::setProjectRoot(QString root)
{
    m_projectRoot = root;
}


void SetUpTab::updateEquipmentList(void)
{
    checkAccessories();

    // clear list
    if(m_pEquipmentList == NULL)
        m_pEquipmentList = initEquipmentList();
    else
        m_pEquipmentList->clear();

    // create tree item
    QTreeWidgetItem *treeitem = new QTreeWidgetItem(m_pEquipmentList);
 

    std::vector<QString> items, selections, lightItems, lightSelections;
    std::multimap<int, QString> description, lightDescriptions;
    items.push_back("Camera");
    items.push_back("Light");
    items.push_back("Tripod");
    items.push_back("Lens");
    items.push_back("Accessories");
    selections.push_back(m_pCamerasComboBox->currentText());
    selections.push_back(m_pLightsComboBox->currentText());
    selections.push_back(m_pTripodComboBox->currentText());
    selections.push_back(m_pLensComboBox->currentText());
    selections.push_back("");
    description.insert(std::make_pair(0, m_pEditPower->text() + " " + m_pEditDescription->text()));

    // add item categories
    m_pGUIUtility->updateTreeItem3Cols(true, treeitem, m_pEquipmentList, &items, &selections, &description);
    addTreeWidgetsToList(m_pEquipmentList, &m_lightAccessoryTitles, &m_lightAccessoryList, 4, m_pGUIUtility);
    addTreeWidgetsToList(m_pEquipmentList, &m_monopodAccessoryTitles, &m_monopodAccessoryList, 4, m_pGUIUtility);
    addTreeWidgetsToList(m_pEquipmentList, &m_sphereAccessoryTitles, &m_sphereAccessoryList, 4, m_pGUIUtility);
    addTreeWidgetsToList(m_pEquipmentList, &m_miscAccessoryTitles, &m_miscAccessoryList, 4, m_pGUIUtility);
    addTreeWidgetsToList(m_pEquipmentList, &m_cableAccessoryTitles, &m_cableAccessoryList, 4, m_pGUIUtility);
    addTreeWidgetsToList(m_pEquipmentList, &m_ballheadAccessoryTitles, &m_ballheadAccessoryList, 4, m_pGUIUtility);
}


void SetUpTab::addTreeWidgetsToList(QTreeWidget *pTree, std::vector<QString> *pTitles, QList<QListWidget *> *pWidgetList, int child, GUIUtility *pGUIUtil)
{
    // error checking
    assert(pTree);
    assert(pTitles);
    assert(pWidgetList);
    assert(pGUIUtil);
    assert(pTitles->size() == pWidgetList->size());

    for (int i = 0; i < (int)pTitles->size(); ++i) {

        QTreeWidgetItem *accesoryRoot = pTree->topLevelItem(4);
        if(APPDEBUG) {
            fprintf(stderr, "%s\n\n", pTree->topLevelItem(child)->text(0).toLocal8Bit().data());
        }
       pGUIUtil->updateTreeItemTree3Cols(accesoryRoot, (*pTitles)[i], (*pWidgetList)[i]);
    }
}

void SetUpTab::accessoryDialog(AccessoryDialog *pDialog, std::vector<QString> files, std::vector<QString> titles, QList<QListWidget *> *pWidgetList, QList<QStringList *> *pMap)
{
    if(!pDialog)
         pDialog = new AccessoryDialog(files, titles, pWidgetList, pMap);
    checkAccessories();
    pDialog->show();
}


//void SetUpTab::applyAccessories(QList<QListWidget> *pList, std::vector<QString> *titles)
//{
//    for(int i = 0; i < (int)titles->size(); ++i) {
//        if((*tiles)[i].compare(
//    }
//
//
//    for(int i = 0; i < (int)m_pItemList->count(); ++i) {
//        for(int k = 0; k < (*m_pItemList)[i]->count(); ++k) {
//            if((*m_pItemList)[i]->item(k)->checkState == (Qt::Checked)) {
//                map->append((*m_pItemList)[i]->item(k)->text());
//            }
//        }
//    }
//}
//
//
//void SetUpTab::updateAccessoryMap(QListWidgetItem *pList, QStringList *map)
//{
//    map->clear();
//    for(int k = 0; k < (*pList)[i]->count(); ++k) {
//        if((*pList)[i]->item(k)->checkState == (Qt::Checked)) {
//            map->append((*pList)[i]->item(k)->text());
//        }
//    }
//}


void SetUpTab::checkAccessories(void)
{

    accessoryStatus(&m_lightAccessoryList,   QStringList((*(m_lightMap[0])) + (*(m_lightMap[1])) ));
    accessoryStatus(&m_monopodAccessoryList, QStringList((*(m_monoMap[0]))  + (*(m_monoMap[1])) ));
    accessoryStatus(&m_ballheadAccessoryList, (*(m_ballheadMap[0])) );
    accessoryStatus(&m_cableAccessoryList,   QStringList((*(m_cableMap[0])) + (*(m_cableMap[1])) ));
    accessoryStatus(&m_miscAccessoryList,    QStringList((*(m_miscMap[0]))  + (*(m_miscMap[1])) ));
    accessoryStatus(&m_sphereAccessoryList,  (*(m_sphereMap[0])) );

#if 0
    if(APPDEBUG) {
        for(int i = 0; i < (int)m_sphereAccessoryList.count(); ++i) {
            for(int k = 0; k < m_sphereAccessoryList[i]->count(); ++k) {
                if( m_sphereAccessoryList[i]->item(k)->checkState() == Qt::Checked) {
                        m_sphereAccessoryList[i]->item(k)->setCheckState(Qt::Checked);
                        fprintf(stderr, "%s: - CHECKED \n", m_sphereAccessoryList[i]->item(k)->text().toLocal8Bit().data());
                    }
                else {
                        fprintf(stderr, "%s: - NOT CHECKED \n", m_sphereAccessoryList[i]->item(k)->text().toLocal8Bit().data());
                }
            }
        }
   }
#endif
}

// CTT ToDo optimize
void SetUpTab::accessoryStatus(QList<QListWidget *> *list, QStringList names)
{


    for(int i = 0; i < (int)list->count(); ++i) {
        for(int k = 0; k < (*list)[i]->count(); ++k) {

            (*list)[i]->item(k)->setCheckState(Qt::Unchecked);

            for(int j = 0; j < (int)names.size(); ++j) {

                if(APPDEBUG) {
                    fprintf(stderr, "the name is %s, the entry is %s \n", names[j].toLocal8Bit().data(), (*list)[i]->item(k)->text().toLocal8Bit().data());
                }

                if((*list)[i]->item(k)->text().compare(names[j]) == 0) {
                    (*list)[i]->item(k)->setCheckState(Qt::Checked);
                    break;
                }
            }
        }
    }
}


void SetUpTab::clearAccessoryMaps(void)
{
    m_ballheadMap[0]->clear();
    m_cableMap[0]->clear();
    m_cableMap[1]->clear();
    m_lightMap[0]->clear();
    m_lightMap[1]->clear();
    m_monoMap[0]->clear();
    m_monoMap[1]->clear();
    m_sphereMap[0]->clear();
    m_miscMap[0]->clear();
    m_miscMap[1]->clear();
}



void SetUpTab::initAccessoryOptions(QString root)
{
    QFile files;

    m_lightAccessoryFiles.clear();
    m_lightAccessoryTitles.clear();
    m_lightAccessoryList.clear();
    m_monopodAccessoryFiles.clear();
    m_monopodAccessoryTitles.clear();
    m_monopodAccessoryList.clear();
    m_ballheadAccessoryFiles.clear();
    m_ballheadAccessoryTitles.clear();
    m_ballheadAccessoryList.clear();
    m_cableAccessoryFiles.clear();
    m_cableAccessoryTitles.clear();
    m_cableAccessoryList.clear();
    m_miscAccessoryFiles.clear();
    m_miscAccessoryTitles.clear();
    m_miscAccessoryList.clear();
    m_sphereAccessoryFiles.clear();
    m_sphereAccessoryTitles.clear();
    m_sphereAccessoryList.clear();




    files.setFileName(root + "/lightAccessories.txt");
    if(files.exists()) {
        m_lightAccessoryFiles.push_back( root + "/lightAccessories.txt");
        m_lightAccessoryTitles.push_back("Light_Accessories");
    }
    files.setFileName(root + "/flashtriggers.txt");
    if(files.exists()) {
        m_lightAccessoryFiles.push_back( root + "/flashtriggers.txt");
        m_lightAccessoryTitles.push_back("RF_Triggers");
    }
    m_pAccessoryDialog_light = new AccessoryDialog(m_lightAccessoryFiles, m_lightAccessoryTitles, &m_lightAccessoryList, &m_lightMap);



    files.setFileName(root + "/monopods.txt");
    if(files.exists()) {
        m_monopodAccessoryFiles.push_back( root + "/monopods.txt");
        m_monopodAccessoryTitles.push_back("Monopods");
    }
    files.setFileName(root + "/minipods.txt");
    if(files.exists()) {
        m_monopodAccessoryFiles.push_back( root + "/minipods.txt");
        m_monopodAccessoryTitles.push_back("Minipods");
    }
    m_pAccessoryDialog_monopod = new AccessoryDialog(m_monopodAccessoryFiles, m_monopodAccessoryTitles, &m_monopodAccessoryList, &m_monoMap);


    files.setFileName(root + "/ballheads.txt");
    if(files.exists()) {
        m_ballheadAccessoryFiles.push_back(root + "/ballheads.txt");
        m_ballheadAccessoryTitles.push_back("Ballheads");
    }
    m_pAccessoryDialog_ballhead = new AccessoryDialog(m_ballheadAccessoryFiles, m_ballheadAccessoryTitles, &m_ballheadAccessoryList, &m_ballheadMap);


    files.setFileName(root + "/cables.txt");
    if(files.exists()) {
        m_cableAccessoryFiles.push_back(root + "/cables.txt");
        m_cableAccessoryTitles.push_back("Cables");
    }
    files.setFileName(root + "/cablerelease.txt");
    if(files.exists()) {
        m_cableAccessoryFiles.push_back(root + "/cablerelease.txt");
        m_cableAccessoryTitles.push_back("Cable_Release");
    }
    m_pAccessoryDialog_cable  = new AccessoryDialog(m_cableAccessoryFiles, m_cableAccessoryTitles, &m_cableAccessoryList, &m_cableMap);



    files.setFileName(root + "/rigginghardware.txt");
    if(files.exists()) {
        m_miscAccessoryFiles.push_back(root + "/rigginghardware.txt");
        m_miscAccessoryTitles.push_back("Rigging_Hardware");
    }
    files.setFileName(root + "/kit.txt");
    if(files.exists()) {
        m_miscAccessoryFiles.push_back(root + "/kit.txt");
        m_miscAccessoryTitles.push_back("Other");
    }
    m_pAccessoryDialog_misc = new AccessoryDialog(m_miscAccessoryFiles, m_miscAccessoryTitles, &m_miscAccessoryList, &m_miscMap);



    files.setFileName(root + "/spheres.txt");
    if(files.exists()) {
        m_sphereAccessoryFiles.push_back(root + "/spheres.txt");
        m_sphereAccessoryTitles.push_back("Spheres");
    }
    m_pAccessoryDialog_sphere = new AccessoryDialog(m_sphereAccessoryFiles, m_sphereAccessoryTitles, &m_sphereAccessoryList, &m_sphereMap);
}


void SetUpTab::saveCustomTemplates(void)
{
    if(!m_pCustomTemplatesWindow)
        m_pCustomTemplatesWindow = new CustomTemplatesWindow(m_pStringDefs, m_root, m_projectRoot, this);

    m_pCustomTemplatesWindow->show();
}


void SetUpTab::exportToRDF(QString path)
{
    deviceToRDF(path);
}


void SetUpTab::deviceToRDF(QString path)
{
    RDFReader rdfr(m_pStringDefs);

    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Device";
    makeDirectory(outfile);
    outfile = outfile + "/" + "SetUp_Device.rdf";

    // CTT: TODO - update on UI redesign for metadata


    //QString data = rdfr.device("http://www.canon.com", 20054, m_pCamerasComboBox->currentText(), "http://www.coreytoler.com/#equipment/#camera/", "http://www.coreytoler.com/#equipment/#camera/7D", "http://www.canon", "placeholder");
    //QString data = rdfr.device("testing", 20054, "testing", "Testing", "Testing", "Testing", "placeholder");
    //saveTextToFile(outfile, data);
}


void SetUpTab::writeSettingsFile(QString path, QString filename, QString username, QString notes)
{

    QString fpath = path + "/" + "settings";
    makeDirectory(fpath);

    // init the full path
    QString outfile = fpath + "/" + filename;

    // header
    QString fullstring = "<!DOCTYPE saved-settings>\n";
    fullstring = fullstring + "<saved-settings version=\"1.0\" >\n";

    // project
    fullstring = fullstring + exportToXML(username, notes);

    // footer
    fullstring = fullstring + "</saved-settings>\n";

    // write the correspondences
    QFile f(outfile);
    if(f.exists())
        f.remove();

    // write the data to the file
    saveTextToFile(outfile, fullstring);

}


QString SetUpTab::exportToXML(QString name, QString notes)
{
    if(name.isNull())
        name = "__current__";
    if(notes.isNull())
        notes = "  ";
 
    QString capturetype = "camera";
    assert(m_pYesdomesck->checkState() != m_pYesCamerack->checkState());
    if(m_pYesdomesck->checkState() == Qt::Checked)
        capturetype = "dome";

    // format the xml data for export
    QString fullstring, finaltext, accessorystring;
    fullstring = fullstring + "<setup name=\"%s\" filtertype=\"%s\" color=\"%s\"  capturetype=\"%s\" camera=\"%s\"   lens=\"%s\" tripod=\"%s\" lightDescription=\"%s\"  power=\"%s\"   lights=\"%s\"  stringlength=\"%s\"                         time=\"%s\"   savedsetup=\"%s\" ";
    finaltext.sprintf(fullstring.toLocal8Bit().data(), name.toLocal8Bit().data(), m_pFiltersComboBox->currentText().toLocal8Bit().data(), m_pColorComboBox->currentText().toLocal8Bit().data(), capturetype.toLocal8Bit().data(), m_pCamerasComboBox->currentText().toLocal8Bit().data(), m_pLensComboBox->currentText().toLocal8Bit().data(), m_pTripodComboBox->currentText().toLocal8Bit().data(), m_pEditDescription->text().toLocal8Bit().data(), m_pEditPower->text().toLocal8Bit().data(), m_pLightsComboBox->currentText().toLocal8Bit().data(),  m_pEditString->text().toLocal8Bit().data(), m_pEditTime->text().toLocal8Bit().data(), m_pSetupComboBox->currentText().toLocal8Bit().data());
    
    // for each title, add its selected items in a comma separated list
    accessorystring = accessorystring + exportAccessory(&m_lightAccessoryTitles, &m_lightAccessoryList);
    accessorystring = accessorystring + exportAccessory(&m_monopodAccessoryTitles, &m_monopodAccessoryList);
    accessorystring = accessorystring + exportAccessory(&m_ballheadAccessoryTitles, &m_ballheadAccessoryList);
    accessorystring = accessorystring + exportAccessory(&m_cableAccessoryTitles, &m_cableAccessoryList);
    accessorystring = accessorystring + exportAccessory(&m_miscAccessoryTitles, &m_miscAccessoryList);
    accessorystring = accessorystring + exportAccessory(&m_sphereAccessoryTitles, &m_sphereAccessoryList);


    finaltext = finaltext + "notes=\"" + notes + "\"" + "  " + accessorystring;
    finaltext = finaltext + "/> \n";
    
    return(finaltext);
}


QString SetUpTab::exportAccessory(std::vector<QString> *titles, QList<QListWidget *> *widgetList)
{
    QString fullstring;

    for(int i = 0; i < (int) titles->size(); ++i) {
        fullstring = fullstring + accessoryToXML((*titles)[i], (*widgetList)[i]);
    }

    return(fullstring);

}


QString SetUpTab::accessoryToXML(QString name, QListWidget *list)
{
    bool bEndString = false;
    QString fullstring;
    QString startstring = name + "=" + "\"";

    for(int i = 0; i < (int)list->count(); ++i) {
        QListWidgetItem *entry = list->item(i);

        if((entry->checkState() == Qt::Checked) && (!entry->text().isNull())) {
            if(bEndString)
                fullstring = fullstring +",";
            QString entrystring = entry->text();
            fullstring = fullstring + entrystring;
            bEndString = true;
        }
    }

    if(bEndString) {
        fullstring = startstring + fullstring + "\"  ";
    }

    return(fullstring);
}

void SetUpTab::import(QString projectPath, QString equipmentPath, QDomElement root)
{
    m_projectRoot= projectPath;
    m_root = equipmentPath;
    QDomElement node =  root;

    clearAccessoryMaps();

    initEquipmentFiles(equipmentPath);
    parseNode(node);
    
    loadComboBoxes();
    initAccessoryOptions(m_root);
    enableAccessories(true);
    updateUI();

}


void SetUpTab::parseNode(QDomElement node)
{
    QString str;

    // parse the  information and store the data
    for (; !node.isNull(); node=node.nextSiblingElement("setup")) {

        if (node.hasAttribute("filtertype")) {
            str = node.attribute("filtertype");
            m_filters = str;
        }
        if (node.hasAttribute("color")) {
            str = node.attribute("color");
            m_color = str; 
        }
        if (node.hasAttribute("capturetype")) {
            str = node.attribute("capturetype");
            m_captureType = str;
        }
        if (node.hasAttribute("camera")) {
            str = node.attribute("camera");
            m_camera = str;
        }
        if (node.hasAttribute("catalog")) {
            str = node.attribute("catalog");
            m_catalog = str;
        }
        if (node.hasAttribute("lens")) {
            str = node.attribute("lens");
            m_lens = str;
        }
        if (node.hasAttribute("tripod")) {
            str = node.attribute("tripod");
            m_tripod = str;
        }
        if (node.hasAttribute("lightDescription")) {
            str = node.attribute("lightDescription");
            m_lightDescription = str;
        }
        if (node.hasAttribute("power")) {
            str = node.attribute("power");
            m_power = str;
        }
        if (node.hasAttribute("lights")) {
            str = node.attribute("lights");
            m_light = str;
        }
        if (node.hasAttribute("stringlength")) {
            str = node.attribute("stringlength");
            m_stringLength = str;
        }
        if (node.hasAttribute("time")) {
            str = node.attribute("time");
            m_time = str;
       }
        if (node.hasAttribute("savedsetup")) {
            str = node.attribute("savedsetup");
            m_setup = str;
       }
        if (node.hasAttribute("Light_Accessories")) {
            str = node.attribute("Light_Accessories");
            (*(m_lightMap[0])) = str.split(",");
       }
        if (node.hasAttribute("RF_Triggers")) {
            str = node.attribute("RF_Triggers");
             (*(m_lightMap[1])) = str.split(",");
       }
        if (node.hasAttribute("Monopods")) {
            str = node.attribute("Monopods");
             (*(m_monoMap[0])) = str.split(",");
       }
        if (node.hasAttribute("Minipods")) {
            str = node.attribute("Minipods");
             (*(m_monoMap[1])) = str.split(",");
       }
        if (node.hasAttribute("Cable_Release")) {
            str = node.attribute("Cable_Release");
             (*(m_cableMap[1])) = str.split(",");
       }
        if (node.hasAttribute("Cables")) {
            str = node.attribute("Cables");
             (*(m_cableMap[0])) = str.split(",");
       }
        if (node.hasAttribute("Ballheads")) {
            str = node.attribute("Ballheads");
            (*(m_ballheadMap[0])) = str.split(",");
       }
        if (node.hasAttribute("Rigging_Hardware")) {
            str = node.attribute("Rigging_Hardware");
             (*(m_miscMap[0])) = str.split(",");
       }
        if (node.hasAttribute("Other")) {
            str = node.attribute("Other");
             (*(m_miscMap[1])) = str.split(",");
       }
        if (node.hasAttribute("Spheres")) {
            str = node.attribute("Spheres");
             (*(m_sphereMap[0])) = str.split(",");
       }
       if (node.hasAttribute("notes")) {
            str = node.attribute("notes");
            m_notes = str;
       }
    }
}


void SetUpTab::loadComboBoxes(void)
{
    // init combos from this project file
    m_pGUIUtility->initComboBox(m_pCamerasComboBox, m_cameraFileName);
    m_pGUIUtility->initComboBox(m_pLensComboBox, m_lensFileName);
    m_pGUIUtility->initComboBox(m_pTripodComboBox, m_tripodFileName);
    m_pGUIUtility->initComboBox(m_pLightsComboBox, m_lightsFileName);

    loadSetUpComboBox();
    loadFilterOptions();
    loadColorOptions();
}

void  SetUpTab::switchSettings_user(int index)
{

    QString name = m_pSetupComboBox->currentText();
    assert(m_pSetupComboBox->findText(name) == index);
    processSwitch(name);

    return;
}

// CTT ToDo factor code
void SetUpTab::switchSettingHere(QDomElement  root)
{
    // verify valid version and file info
    if (root.tagName() == SAVED_SETTING_ROOT) {
        if (root.hasAttribute("version")) {
            if (isCompatible(root.attribute("version"), SETTINGS_PROJCECT_FILE_VERSION)) {
                QDomElement  node = root.firstChildElement("setup");
                import(m_projectRoot, m_root, node);
            }
            else {
                qDebug("incompatible file version: `%s' instead of `%s'; bailing out...", root.attribute("version").toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
                return;
            }
        }
        else {
            qDebug("unknown file version -- add version attribute; bailing out...");
            return;
        }
    }
    else {
        qDebug("wrong root tag: `%s' instead of `%s'", root.tagName().toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
        return;
    }

    // made it
    return;
}


// factor code CTT ToDo
void  SetUpTab::switchSettings(int index)
{

}

void SetUpTab::processSwitch(QString name)
{
    if(name.isNull())
        return;

    //get the path to the setting
    QString usableFileName = name.replace(" ", "_");
    QString filefullpath = m_projectRoot + "/" + "settings" + "/" +  name + ".xml";

    QFile file(filefullpath);  
    if (file.open(QIODevice::ReadOnly)) {
        QDomDocument doc;
        bool status = doc.setContent(&file);
        file.close();
        if (status) {
            QDomElement  root(doc.documentElement());
            switchSettingHere(root);
        } 
        else {
            qDebug("couldn't read from `%s'", filefullpath.toUtf8().data());
        } 
    }
    else
        qDebug("couldn't open `%s'", filefullpath.toUtf8().data());
}


void SetUpTab::loadSetUpComboBox(void)
{
    m_pSetupComboBox->clear();

    QDir setupdir(m_projectRoot + "/" + "settings");

    if(setupdir.exists()) {
        QFileInfoList info = setupdir.entryInfoList();
        for(int i=0; i < (int)info.size(); ++i) {
            if(info[i].isFile()) {
                // get the name;
                QString username = getAttribute(info[i].absoluteFilePath(), "setup", "name");
                if((!username.isNull()) && (!username.isEmpty()))
                    m_pSetupComboBox->addItem(username);
            }
        }
    }

    if((m_pSetupComboBox->findText("select...")) == -1)
        m_pSetupComboBox->addItem("select...");
    m_pSetupComboBox->setCurrentIndex(m_pSetupComboBox->findText("select..."));
}


// need to move this CTT ToDo
QString SetUpTab::getAttribute(QString fname, QString id, QString name)
{

    assert(!(fname.isNull()));

    QFile file(fname);  
    if (file.open(QIODevice::ReadOnly)) {
        QDomDocument doc;
        bool status = doc.setContent(&file);
        file.close();
        if (status) {
            QDomElement  root(doc.documentElement());
            return (readAttribute(root, id, name));
        } 
        else {
            qDebug("couldn't read from `%s'", fname.toUtf8().data());
        } 
    }
    else
        qDebug("couldn't open `%s'", fname.toUtf8().data());

    return(false);
}



QString SetUpTab::readAttribute(QDomElement &root, QString id, QString name)
{


    QString str = "";


    // verify valid version and file info
    if (root.tagName() == SAVED_SETTING_ROOT) {
        if (root.hasAttribute("version")) {
            if (isCompatible(root.attribute("version"), SETTINGS_PROJCECT_FILE_VERSION)) {
                QDomElement  node = root.firstChildElement(id);
                for (; !node.isNull(); node=node.nextSiblingElement(id)) {

                    if (node.hasAttribute(name)) {
                        str = node.attribute(name);
                        return(str);
                    }
                }
            }
            else {
                qDebug("incompatible file version: `%s' instead of `%s'; bailing out...", root.attribute("version").toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
                return(str);
            }
        }
        else {
            qDebug("unknown file version -- add version attribute; bailing out...");
            return(str);
        }
    }
    else {
        qDebug("wrong root tag: `%s' instead of `%s'", root.tagName().toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
        return(str);
    }

    // made it
    return (str);
}




void SetUpTab::loadFilterOptions(void)
{
    m_pFiltersComboBox->clear();
    m_pFiltersComboBox->addItem("neutral density");
    m_pFiltersComboBox->addItem("band pass");
    m_pFiltersComboBox->addItem("cut-off filters");
}


void SetUpTab::loadColorOptions(void)
{
    m_pColorComboBox->clear();
    m_pColorComboBox->addItem("In image");
    m_pColorComboBox->addItem("Separate Shot");
    m_pColorComboBox->addItem("Color Profile");
}


void SetUpTab::updateUI(void)
{

    m_pGUIUtility->setComboBox(m_pCamerasComboBox, m_camera);
    m_pGUIUtility->setComboBox(m_pTripodComboBox, m_tripod);
    m_pGUIUtility->setComboBox(m_pLightsComboBox, m_light);
    m_pGUIUtility->setComboBox(m_pSetupComboBox, m_setup);
    m_pGUIUtility->setComboBox(m_pFiltersComboBox, m_filters);
    m_pGUIUtility->setComboBox(m_pColorComboBox, m_color);
    m_pGUIUtility->setComboBox(m_pSetupComboBox, m_setup);
    m_pGUIUtility->setComboBox(m_pLensComboBox, m_lens);
    // now set the current selections
    m_pEditTime->setText(m_time);
    m_pEditString->setText(m_stringLength);
    m_pEditPower->setText(m_power);
    m_pEditDescription->setText(m_lightDescription);

    if(!m_stringLength.isNull())
        m_pYesstringck->setChecked(true);
    else
        m_pYesstringck->setChecked(false);


    if(m_captureType.compare("Dome")== 0) {
        m_pYesdomesck->setChecked(true);
        m_pYesCamerack->setChecked(false);
    }
    else {
        m_pYesdomesck->setChecked(false);
        m_pYesCamerack->setChecked(true);
    }


    if(!m_color.isNull()) {
        m_pYesck->setChecked(true);
    }
    else {
       m_pYesck->setChecked(false);
    }

    if(!m_filters.isNull())
        m_pYesfiltersck->setChecked(true);
    else
       m_pYesfiltersck->setChecked(false);

    checkAccessories();
    enableCustomTemplates(true);
}


void SetUpTab::clear(void)
{

    m_pCamerasComboBox->clear();
    m_pLensComboBox->clear();
    m_pTripodComboBox->clear();
    m_pLightsComboBox->clear();
    m_pSetupComboBox->clear();
    m_pFiltersComboBox->clear();
    m_pColorComboBox->clear();

    m_pEditTime->clear();
    m_pEditString->clear();
    m_pEditPower->clear();
    m_pEditDescription->clear();

    m_pYesstringck->setChecked(false);
    m_pYesdomesck->setChecked(false);
    m_pYesCamerack->setChecked(true);
    m_pYesck->setChecked(false);
    m_pYesfiltersck->setChecked(false);

    loadColorOptions();
    loadFilterOptions();

}


void SetUpTab::enableSavedSettings(bool flag)
{
    m_pSavesetupButton->setDisabled(!flag);
}


QString  SetUpTab::equipmentRoot(void)
{
    return(m_root);
}

