/*===========================================================================
    CaptureTool
    ProjectTab.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QtGui>
#include <assert.h>
#include <utility>
#include"ProjectTab.h"
#include "globals.h"
#include "utilities.h"
//#include "treemodel.h"
#include "RDFReader.h"
#include "StringDefs.h"



//-----------------------------------------------------------------------
// Project information
//-----------------------------------------------------------------------
ProjectTab::ProjectTab(StringDefs *pStringDefs, const QFileInfo &fileInfo, QWidget *parent)
    : QWidget(parent),
      m_bSavedFlag          (false), 
      m_pStringDefs         (pStringDefs)
{

    m_projectFile = "untitled.xml";


    QGroupBox *descriptionGr = new QGroupBox(tr("Description"));

    // add labels
    QLabel *labelDate = new QLabel(tr("Date:"));
    m_pEditDate = new QLineEdit();
    QLabel *labelSubject = new QLabel(tr("Subject:"));
    m_pEditSubject = new QLineEdit();
    QLabel *labelLocation = new QLabel(tr("Location:"));
    m_pEditLocation = new QLineEdit();
    QLabel *labelCollection = new QLabel(tr("Collection:"));
    m_pEditCollection = new QLineEdit();
    QLabel *labelCatalog = new QLabel(tr("Catalog/Accession:"));
    m_pEditCatalog = new QLineEdit();
    QLabel *labelRoom = new QLabel(tr("Room:"));
    m_pEditRoom = new QLineEdit();
    QLabel *labelRights = new QLabel(tr("Rights/Permissions:"));
    m_pEditRights = new QLineEdit();
    QLabel *labelEmail = new QLabel(tr("email:"));
    m_pEditEmail = new QLineEdit();

    // add labels to UI
    QVBoxLayout *projectLayout = new QVBoxLayout;
    projectLayout->addWidget(labelDate);
    projectLayout->addWidget(m_pEditDate);
    projectLayout->addWidget(labelSubject);
    projectLayout->addWidget(m_pEditSubject);
    projectLayout->addWidget(labelLocation);
    projectLayout->addWidget(m_pEditLocation);
    projectLayout->addWidget(labelCollection);
    projectLayout->addWidget(m_pEditCollection);
    projectLayout->addWidget(labelCatalog);
    projectLayout->addWidget(m_pEditCatalog);
    projectLayout->addWidget(labelRoom);
    projectLayout->addWidget(m_pEditRoom);
    projectLayout->addWidget(labelRights);
    projectLayout->addWidget(m_pEditRights);
    descriptionGr->setLayout(projectLayout);

    QGroupBox *contactGr = new QGroupBox(tr("Contact"));

    // set labels
    QLabel *labelLead = new QLabel(tr("Lead:"));
    m_pEditLead = new QLineEdit();
    QLabel *labelTeam = new QLabel(tr("Team:"));
    m_pEditTeam = new QLineEdit();
    QLabel *labelPhone = new QLabel(tr("Ph:"));
    m_pEditPhone = new QLineEdit();
    QLabel *labelAddress = new QLabel(tr("Address:"));
    m_pEditAddress = new QLineEdit();

    // update the layout
    QVBoxLayout *contactLayout = new QVBoxLayout;
    contactLayout->addWidget(labelLead);
    contactLayout->addWidget(m_pEditLead);
    contactLayout->addWidget(labelTeam);
    contactLayout->addWidget(m_pEditTeam);
    contactLayout->addWidget(labelPhone);
    contactLayout->addWidget(m_pEditPhone);
    contactLayout->addWidget(labelEmail);
    contactLayout->addWidget(m_pEditEmail);
    contactLayout->addWidget(labelAddress);
    contactLayout->addWidget(m_pEditAddress);
    contactGr->setLayout(contactLayout);


    // set the layout
    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(descriptionGr);
    mainLayout->addWidget(contactGr);
    mainLayout->addStretch(1);
    setLayout(mainLayout);
}


QString  ProjectTab::exportToXML(void)
{
    updateData();

    // format the xml data for export
    QString fullstring, finaltext;
    fullstring = fullstring + "<project subject=\"%s\"  location=\"%s\"  collection=\"%s\" catalog=\"%s\" rights=\"%s\" lead=\"%s\" team=\"%s\" phone=\"%s\" email=\"%s\" address=\"%s\" room=\"%s\" date=\"%s\" />\n";
    finaltext.sprintf(fullstring.toLocal8Bit().data(), m_subject.toLocal8Bit().data(), m_location.toLocal8Bit().data(), m_collection.toLocal8Bit().data(), m_catalog.toLocal8Bit().data(), m_rights.toLocal8Bit().data(), m_lead.toLocal8Bit().data(), m_team.toLocal8Bit().data(), m_phone.toLocal8Bit().data(), m_email.toLocal8Bit().data(), m_address.toLocal8Bit().data(), m_room.toLocal8Bit().data(), m_date.toLocal8Bit().data());
    return(finaltext);
}


void  ProjectTab::exportToRDF(QString path)
{
    subjectToRDF(path);
    locationToRDF(path);
    teamToRDF(path);
    timeToRDF(path);
    leadToRDF(path);
    collectionToRDF(path);
    permissionsToRDF(path);
    contactToRDF(path);
}


void ProjectTab::timeToRDF(QString path)
{
    RDFReader rdfr(m_pStringDefs);

    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Time";
    makeDirectory(outfile);
    outfile = outfile + "/" + "DP_Time.rdf";

    QString data = rdfr.time(startdatetime(),  enddatetime());
    saveTextToFile(outfile, data);
}


void ProjectTab::subjectToRDF(QString path)
{
    RDFReader rdfr(m_pStringDefs);

    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Subject";
    makeDirectory(outfile);
    outfile = outfile + "/" + "Subject.rdf";

    // need ruid generator here 

    QString data = rdfr.subject("", m_pEditCatalog->text(), m_pEditSubject->text(), "https://www.coreytoler.com/#object");
    saveTextToFile(outfile, data);
}


void ProjectTab::leadToRDF(QString path)
{
    RDFReader rdfr(m_pStringDefs);

    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Lead";
    makeDirectory(outfile);
    outfile = outfile + "/" + "DP_Lead.rdf";

    QString data = rdfr.person(uid(), leadfirstname(),  leadlastname(), leadlegalbody());
    saveTextToFile(outfile, data);
}


void ProjectTab::teamToRDF(QString path)
{
}

void ProjectTab::collectionToRDF(QString path)
{
}

void ProjectTab::permissionsToRDF(QString path)
{
}


void ProjectTab::contactToRDF(QString path)
{
}

void ProjectTab::locationToRDF(QString path)
{
}

void ProjectTab::open(QString fname)
{
    m_projectFile = fname;
    m_bSavedFlag = true;

}


void ProjectTab::create(void)
{

    m_bSavedFlag = false;

    // load data from file
    clearData();

    m_projectFile = "untitled.xml";

}


void ProjectTab::import(QDomElement root)
{
    QString str;
    QDomElement node = root;

    // parse the  information and store the data
    for (; !node.isNull(); node=node.nextSiblingElement("project")) {

        if (node.hasAttribute("date")) {
            str = node.attribute("date");
            m_pEditDate->setText(str);
        }
        if (node.hasAttribute("subject")) {
            str = node.attribute("subject");
            m_pEditSubject->setText(str);
        }
        if (node.hasAttribute("location")) {
            str = node.attribute("location");
            m_pEditLocation->setText(str);
        }
        if (node.hasAttribute("collection")) {
            str = node.attribute("collection");
            m_pEditCollection->setText(str);
        }
        if (node.hasAttribute("catalog")) {
            str = node.attribute("catalog");
            m_pEditCatalog->setText(str);
        }
        if (node.hasAttribute("room")) {
            str = node.attribute("room");
            m_pEditRoom->setText(str);
        }
        if (node.hasAttribute("rights")) {
            str = node.attribute("rights");
            m_pEditRights->setText(str);
        }
        if (node.hasAttribute("email")) {
            str = node.attribute("email");
            m_pEditEmail->setText(str);
        }
        if (node.hasAttribute("lead")) {
            str = node.attribute("lead");
            m_pEditLead->setText(str);
        }
        if (node.hasAttribute("team")) {
            str = node.attribute("team");
            m_pEditTeam->setText(str);
        }
        if (node.hasAttribute("phone")) {
            str = node.attribute("phone");
            m_pEditPhone->setText(str);
        }
        if (node.hasAttribute("address")) {
            str = node.attribute("address");
            m_pEditAddress->setText(str);
       }
    }
}


void ProjectTab::updateData(void)
{
    m_date = m_pEditDate->text();
    m_subject = m_pEditSubject->text();
    m_location = m_pEditLocation->text();
    m_collection = m_pEditCollection->text();
    m_catalog = m_pEditCatalog->text();
    m_room = m_pEditRoom->text();
    m_rights = m_pEditRights->text();
    m_email = m_pEditEmail->text();
    m_date = m_pEditDate->text();
    m_subject = m_pEditSubject->text();
    m_location = m_pEditLocation->text();
    m_collection = m_pEditCollection->text();
    m_catalog = m_pEditCatalog->text();
    m_room = m_pEditRoom->text();
    m_rights = m_pEditRights->text();
    m_lead = m_pEditLead->text();
    m_team = m_pEditTeam->text();
    m_phone = m_pEditPhone->text();
    m_address = m_pEditAddress->text();

}


void ProjectTab::clearData(void)
{
    m_pEditDate->clear();
    m_pEditSubject->clear();
    m_pEditLocation->clear();
    m_pEditCollection->clear();
    m_pEditCatalog->clear();
    m_pEditRoom->clear();
    m_pEditRights->clear();
    m_pEditEmail->clear();
    m_pEditDate->clear();
    m_pEditSubject->clear();
    m_pEditLocation->clear();
    m_pEditCollection->clear();
    m_pEditCatalog->clear();
    m_pEditRoom->clear();
    m_pEditRights->clear();
    m_pEditLead->clear();
    m_pEditTeam->clear();
    m_pEditPhone->clear();
    m_pEditAddress->clear();
}


bool ProjectTab::promptOnSave(void)
{
    return(!m_bSavedFlag);
}


QString ProjectTab::projectFile(void)
{
    return(m_projectFile);
}


void ProjectTab::resetSavedFlag(void)
{
    m_bSavedFlag = false;
}


bool ProjectTab::savedFlag(void)
{
    return(m_bSavedFlag);
}


void ProjectTab::setSavedFlag(bool flag)
{
    m_bSavedFlag = flag;
}


void ProjectTab::updateSavedSettings(QString fname)
{
    m_projectFile = fname;
    m_bSavedFlag = true;
}


QString ProjectTab::startdatetime (void)
{
    return(m_pEditDate->text());
}


QString ProjectTab::enddatetime (void)
{
    return(m_pEditDate->text());
}


QString ProjectTab::leadfirstname (void)
{
    return(m_pEditLead->text());
}


QString ProjectTab::leadlastname (void)
{
    return(m_pEditLead->text());
}


QString ProjectTab::leadlegalbody (void)
{
    return(m_pEditLead->text());
}