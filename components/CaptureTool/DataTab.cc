/*===========================================================================
    CaptureTool
    DataTab.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QtGui>
#include <assert.h>
#include <utility>
#include "globals.h"
#include "DataTab.h"
#include "utilities.h"
#include "GUIUtility.h"






//----------------------------------------------------------
//
// Dataset location and file paths
//----------------------------------------------------------
DataTab::DataTab(const QFileInfo &fileInfo, QWidget *parent)
    : QWidget(parent)
{

    m_bUpdateEquipment = false;

    m_pGUIUtility = new GUIUtility();
    
    QGroupBox *dataGr = new QGroupBox(tr("Path"));
    QGroupBox *statGr = new QGroupBox(tr("Database Statistics"));



    m_pDirectoryLabel = new QLabel(tr("Directory:"));
    m_pLogFileLabel = new QLabel(tr("Log File:"));
    m_pEquipmentLabel = new QLabel(tr("Equipment:"));

    m_pDirectoryComboBox = new QComboBox(this);
    m_pLogFileComboBox = new QComboBox(this);
    m_pEquipmentComboBox = new QComboBox(this);
    m_pDirectoryComboBox->addItem(QDir::currentPath());
    m_pLogFileComboBox->addItem(QDir::currentPath() + "/" + "capture.log");
    m_pEquipmentComboBox->addItem(QDir::currentPath() + "/" + "equipment");

    m_pBrowseButton = new QPushButton(tr("&Browse..."), this);
    m_pLogFileBrowseButton = new QPushButton(tr("&Browse..."), this);
    m_pStatsButton = new QPushButton(tr("&Stats..."), this);
    m_pEquipmentBrowseButton = new QPushButton(tr("&Browse..."), this);

    connect(m_pBrowseButton,            SIGNAL(clicked()), this, SLOT(browseProjectDir()));
    connect(m_pLogFileBrowseButton,     SIGNAL(clicked()), this, SLOT(browseLogFile()));
    connect(m_pEquipmentBrowseButton,   SIGNAL(clicked()), this, SLOT(browseEquipmentDir()));
    m_pStatsButton->setDisabled(true);

    // connect comboBoxes
    connect(m_pEquipmentComboBox, SIGNAL(currentIndexChanged(int)), this, SLOT(switchEquipmentDirectory(int)));
    connect(m_pDirectoryComboBox, SIGNAL(currentIndexChanged(int)), this, SLOT(switchProjectDirectory(int)));

    QGridLayout *dirLayout = new QGridLayout;
    dirLayout->addWidget(m_pDirectoryLabel, 0, 0);
    dirLayout->addWidget(m_pDirectoryComboBox, 0, 1);
    dirLayout->addWidget(m_pBrowseButton, 0, 2);
    dirLayout->addWidget(m_pEquipmentLabel, 1, 0);
    dirLayout->addWidget(m_pEquipmentComboBox, 1, 1);
    dirLayout->addWidget(m_pEquipmentBrowseButton, 1, 2);
    QGridLayout *logLayout = new QGridLayout;
    logLayout->addWidget(m_pLogFileLabel, 0, 0);
    logLayout->addWidget(m_pLogFileComboBox, 0, 1);
    logLayout->addWidget(m_pLogFileBrowseButton, 0, 2);
    logLayout->addWidget(m_pStatsButton, 1, 0);

    dataGr->setLayout(dirLayout);
    statGr->setLayout(logLayout);

    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(dataGr);
    mainLayout->addWidget(statGr);
    mainLayout->addStretch(1);
    setLayout(mainLayout);
}


void DataTab::switchEquipmentDirectory(int index)
{
    m_bUpdateEquipment = true;
}


void DataTab::browseLogFile(void)
{
    openFile(m_pLogFileComboBox, "Select Log File...", "*.log");
}


void DataTab::browseEquipmentDir(void)
{
    openDirectory(m_pEquipmentComboBox, "Select Equipment Directory...");
    m_bUpdateEquipment = true;
}

void DataTab::resetEquipmentFlag(void)
{
    m_bUpdateEquipment = false;
}

bool DataTab::equipmentHasChanged(void)
{
    return(m_bUpdateEquipment);
}

void DataTab::browseProjectDir(void)
{
    openDirectory(m_pDirectoryComboBox, "Select Project Directory...");
}


QString DataTab::equipmentRoot(void)
{
    return (m_pEquipmentComboBox->currentText());
}


void DataTab::clear(void)
{
    if(m_pDirectoryComboBox->findText(QDir::currentPath()) == -1)
        m_pDirectoryComboBox->addItem(QDir::currentPath());
    if(m_pLogFileComboBox->findText(QDir::currentPath() + "/" + "capture.log") == -1)
        m_pLogFileComboBox->addItem(QDir::currentPath() + "/" + "capture.log");

    m_pDirectoryComboBox->setCurrentIndex(m_pDirectoryComboBox->findText(QDir::currentPath()));
    m_pLogFileComboBox->setCurrentIndex(m_pLogFileComboBox->findText(QDir::currentPath() + "/" + "capture.log"));

}


void DataTab::update(QString path, QString logfile)
{
    if(m_pDirectoryComboBox->findText(path) == -1)
        m_pDirectoryComboBox->addItem(path);
    if(m_pLogFileComboBox->findText(logfile) == -1)
        m_pLogFileComboBox->addItem(logfile);

    m_pDirectoryComboBox->setCurrentIndex(m_pDirectoryComboBox->findText(path));
    m_pLogFileComboBox->setCurrentIndex(m_pLogFileComboBox->findText(logfile));
}


QString DataTab::currentPath(void)
{
    QString data = m_pDirectoryComboBox->itemText(m_pDirectoryComboBox->currentIndex());
    return (data);
}

QString DataTab::dataPath(void)
{
    QString data = m_pEquipmentComboBox->itemText(m_pEquipmentComboBox->currentIndex());
    return (data);
}


QString DataTab::exportToXML(void)
{
    // format the xml data for export
    QString fullstring, finaltext;
    fullstring = fullstring + "<data root=\"%s\"  logfile=\"%s\" equipment=\"%s\" />\n";
    finaltext.sprintf(fullstring.toLocal8Bit().data(), m_pDirectoryComboBox->currentText().toLocal8Bit().data(), m_pLogFileComboBox->currentText().toLocal8Bit().data(), m_pEquipmentComboBox->currentText().toLocal8Bit().data());
    return(finaltext);
}


void DataTab::exportToRDF(QString path)
{
    // legacy RDF write
    QString lpath = path + "/" + "logfilepath";
    QString epath = path + "/" + "equipmentpath";
    QString ppath = path + "/" + "projectpath";

    makeDirectory(lpath);
    makeDirectory(epath);
    makeDirectory(ppath);

    QString lf(lpath + "/" + "logfilepath.rdf");
    QString ef(epath + "/" + "equipmentpath.rdf");
    QString pf(ppath + "/" + "projectpath.rdf");

    writePathToRDF(lf, m_pLogFileComboBox->currentText());
    writePathToRDF(ef, m_pEquipmentComboBox->currentText());
    writePathToRDF(pf, m_pDirectoryComboBox->currentText());

    // saprano --here

}

void DataTab::writePathToRDF(QString fname, QString data)
{
}

void DataTab::import(QDomElement root)
{
    QString str;
    QDomElement node =  root;

    // parse the  information and store the data
    for (; !node.isNull(); node=node.nextSiblingElement("data")) {

        if (node.hasAttribute("root")) {
            str = node.attribute("root");
            m_pDirectoryComboBox->clear();
            m_pDirectoryComboBox->addItem(str);
            m_pDirectoryComboBox->setCurrentIndex(m_pDirectoryComboBox->findText(str));
        }
        if (node.hasAttribute("logfile")) {
            str = node.attribute("logfile");
            m_pLogFileComboBox->clear();
            m_pLogFileComboBox->addItem(str);
            m_pLogFileComboBox->setCurrentIndex(m_pLogFileComboBox->findText(str));
        }
        if (node.hasAttribute("equipment")) {
            str = node.attribute("equipment");
            m_pEquipmentComboBox->clear();
            m_pEquipmentComboBox->addItem(str);
            m_pEquipmentComboBox->setCurrentIndex(m_pEquipmentComboBox->findText(str));
        }
    }
}


void DataTab::openDirectory(QComboBox *item, QString label)
{
    // open the file browser to the user specified directory
    QFileDialog::Options options = QFileDialog::DontResolveSymlinks;


    QString mydir = item->currentText();
    QDir dir(mydir);
    QFileDialog dialog;
    dialog.setDirectory(dir);


    // get the project filename
    QString filename = dialog.getExistingDirectory(this, label, dir.absolutePath(), options);
    QFileInfo theFile(filename);

   if(!filename.isNull()) {
        // save the data
        m_pGUIUtility->setComboBox(item, theFile.absoluteFilePath());
        this->show();
   }

}


void DataTab::openFile(QComboBox *item, QString label, QString filter)
{
    // open the file browser to the user specified directory
    QFileDialog::Options options = QFileDialog::DontResolveSymlinks;


    QString mydir = item->currentText();
    QDir dir(mydir);
    QFileDialog dialog;
    dialog.setDirectory(dir);


    // get the project filename
    QString filename = dialog.getSaveFileName(this, 
                                              label,
                                              dir.absolutePath(),
                                              filter,
                                              NULL,
                                              options);
    QFileInfo theFile(filename);

   if(!filename.isNull()) {
        // save the data
        m_pGUIUtility->setComboBox(item, theFile.absoluteFilePath());
        this->show();
   }

}

