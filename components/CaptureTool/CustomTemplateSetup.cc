/*===========================================================================
    CaptureTool
    CustomTemplateSetup.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QtGui>
#include <assert.h>
#include <utility>
#include "CustomTemplateSetup.h"
#include "utilities.h"
#include "globals.h"
#include "StringDefs.h"
#include "XMLReader.h"



//------------------------------------------------------------
// Custom Template Setup
//------------------------------------------------------------
CustomTemplateSetup::CustomTemplateSetup(StringDefs *pStringDefs, const QFileInfo &fileInfo, QString equipmentroot, QWidget *parent)
    :   SetUpTab(pStringDefs, fileInfo, equipmentroot, parent), 
        m_pStringDefs (pStringDefs)
{

    m_pCalibrationGr->hide();
    m_pTimeGr->hide();
    m_pSettingsGr->hide();

    this->updateEquipment(equipmentroot);

    m_pYesCamerack->setChecked(true);
    m_pYesdomesck->setChecked(false);
    m_pYesstringck->setChecked(false);
    m_pYesfiltersck->setChecked(false);
    m_pYesCamerack->setDisabled(true);
    m_pYesdomesck->setDisabled(true);
    m_pCamerasComboBox->setCurrentIndex(-1);
    m_pLightsComboBox->setCurrentIndex(-1);
    m_pTripodComboBox->setCurrentIndex(-1);
    m_pLensComboBox->setCurrentIndex(-1);
    m_pFiltersComboBox->setCurrentIndex(-1);
    m_pColorComboBox->setCurrentIndex(-1);
    m_pSetupComboBox->setCurrentIndex(-1);
}


CustomTemplateSetup::~CustomTemplateSetup(void)
{
}


void CustomTemplateSetup::save(QString equipmentroot, QString category, QString basename)
{
    // export the setting info as xml data
    writeSettingsFile(equipmentroot, basename, "templates", category);
}

// migrate to xml reader class
void CustomTemplateSetup::writeSettingsFile(QString path, QString filename, QString username, QString notes)
{
    QString outfile = m_pStringDefs->getDataFileName(path, filename, username, notes);
    QFile f(outfile);

    // header
    QString fullstring = "<!DOCTYPE saved-template>\n";
    fullstring = fullstring + "<saved-template version=\"1.0\" >\n";


    fullstring = fullstring + "<icon   path=\"%s\" /> \n";
    fullstring.sprintf(fullstring.toLocal8Bit().data(), m_pStringDefs->defaultIcon().toLocal8Bit().data());

    if(f.exists()) {
        // data
        fullstring = fullstring + exportToXML(username, QString());
    }

    // footer
    fullstring = fullstring + "</saved-template>\n";

    // write the correspondences
    if(f.exists())
        f.remove();

    // write the data to the file
    saveTextToFile(outfile, fullstring);

}


void CustomTemplateSetup::load(QString fname)
{
    QString type(SAVED_TEMPLATE_ROOT);
    QString version(SAVED_TEMPLATE_FILE_VERSION);

    QDomElement root;
    QDomDocument doc;
    XMLReader xmlr(m_pStringDefs);
    bool stat = xmlr.load(root, doc, fname,  type, version);
    if(stat)
        import(root);

}


void CustomTemplateSetup::import(QDomElement root)
{
    QDomElement node =  root;
    parseNode(root);
    updateUI();
}
