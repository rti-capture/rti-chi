/*===========================================================================
    CaptureTool
    TabSetWindow.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include <QtGui>
#include <assert.h>
#include <utility>
#include "globals.h"
#include "TabSetWindow.h"
#include "DataTab.h"
#include "ProjectTab.h"
#include "SetUpTab.h"
#include "TextEditor.h"
#include "utilities.h"
#include "GUIUtility.h"
//#include "treemodel.h"
#include "TabPage.h"
#include "NotesTab.h"
#include "AboutTab.h"
#include "StringDefs.h"
#include "RDFReader.h"



//------------------------------------------------------------------------
// CTT ToDo: Destructors and cleanup
// CTT ToDo: Class Files
// CTT ToDo: Localization and file paths
//------------------------------------------------------------------------



TabSetWindow::TabSetWindow(StringDefs *pStringDefs, const QString &fileName, QWidget *parent)
    :   QDialog(parent, Qt::WindowMinimizeButtonHint),
        m_pStringDefs(pStringDefs)
{

    // title
    m_titleformatString = "CaptureTool - ";
    QString projectLabel = m_titleformatString + "untitled.xml";

    // icons and titles
    QIcon m_icon("./appdata/logo32.png");
    this->setWindowTitle(projectLabel);
    this->setWindowIcon(m_icon);
    this->setStyleSheet("PageFold"); 
    this->setFixedSize(520, 680);

    // tabs
    initTabs(fileName);

    // buttons
    initButtonMenu();

    // add layout items
    QGridLayout *gridLayout = new QGridLayout;
    gridLayout->setSpacing(18);
    gridLayout->addWidget(m_pTabs, 0, 0);
    gridLayout->addWidget(buttonBox, 0, 1);
    setLayout(gridLayout);

    m_pTabs->connectSignals();
}


void TabSetWindow::initButtonMenu (void)
{
    m_pOpenButton = new QPushButton(tr("&Open"));
    m_pSaveButton = new QPushButton(tr("&Save"));
    m_pSaveasButton = new QPushButton(tr("&Save As"));
    m_pCloseButton = new QPushButton(tr("&Close"));
    m_pNewButton = new QPushButton(tr("&New"));
    m_pPrintButton = new QPushButton(tr("&Print"));
    m_pHelpButton = new QPushButton(tr("&Help"));
    m_pExitButton = new QPushButton(tr("&Exit"));
     
    buttonBox = new QDialogButtonBox(Qt::Vertical);
    buttonBox->addButton(m_pOpenButton,  QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pSaveButton,  QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pSaveasButton,QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pCloseButton,  QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pNewButton,   QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pPrintButton, QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pHelpButton,  QDialogButtonBox::ActionRole);
    buttonBox->addButton(m_pExitButton,  QDialogButtonBox::ActionRole);

    connect(m_pNewButton,   SIGNAL(clicked()), this, SLOT(newProject()));
    connect(m_pSaveButton,  SIGNAL(clicked()), this, SLOT(saveProject()));
    connect(m_pOpenButton,  SIGNAL(clicked()), this, SLOT(openProject()));
    connect(m_pSaveasButton,SIGNAL(clicked()), this, SLOT(saveAsProject()));
    connect(m_pCloseButton, SIGNAL(clicked()), this, SLOT(newProject()));
    connect(m_pExitButton,  SIGNAL(clicked()), this, SLOT(close()));
}


void TabSetWindow::disableButtons (bool bDisable) 
{

    m_pOpenButton->setDisabled(bDisable);
    m_pSaveButton->setDisabled(bDisable);
    m_pSaveasButton->setDisabled(bDisable);
    m_pCloseButton->setDisabled(bDisable);
    m_pNewButton->setDisabled(bDisable);
    m_pPrintButton->setDisabled(bDisable);
    m_pHelpButton->setDisabled(bDisable);
    m_pExitButton->setDisabled(bDisable);
}


void TabSetWindow::initTabs(const QString &fileName)
{
    QFileInfo fileInfo(fileName);

    m_pTabs = new TabPage(this);
    m_pProjectTab = new ProjectTab(m_pStringDefs, fileInfo);
    m_pDataTab = new DataTab(fileInfo);
    m_pSetUpTab = new SetUpTab(m_pStringDefs, fileInfo, QString());
    m_pNotesTab = new NotesTab(fileInfo);
    m_pAboutTab = new AboutTab(m_pStringDefs, fileInfo);

    m_pTabs->addTab(m_pProjectTab,  tr("Project"));
    m_pTabs->addTab(m_pDataTab,     tr("File"));
    m_pTabs->addTab(m_pSetUpTab,    tr("Setup"));
    m_pTabs->addTab(m_pNotesTab,    tr("Notes"));
    m_pTabs->addTab(m_pAboutTab,    tr("About"));
}


void TabSetWindow::updateEquipment(void)
{
    assert(m_pSetUpTab);
    assert(m_pDataTab);
    if(m_pDataTab->equipmentHasChanged()) {
        m_pSetUpTab->updateEquipment(m_pDataTab->equipmentRoot());
        m_pSetUpTab->enableCustomTemplates(true);
        m_pDataTab->resetEquipmentFlag();
    }
}


void TabSetWindow::updateProjectRoot(void)
{
    assert(m_pSetUpTab);
    assert(m_pDataTab);
    m_pSetUpTab->setProjectRoot(m_pDataTab->currentPath());
}


void TabSetWindow::saveAsProject(void) 
{
    assert(m_pProjectTab);

    bool oldSavedSetting = m_pProjectTab->savedFlag();
    m_pProjectTab->resetSavedFlag();
    saveProject();

    // tracking saved files
    bool newSavedSetting = m_pProjectTab->savedFlag();
    if((!newSavedSetting) && (oldSavedSetting))
        m_pProjectTab->setSavedFlag(true);
}


void TabSetWindow::saveProject(void)
{
    assert(m_pProjectTab);
    QString projectLabel = m_titleformatString + m_pProjectTab->projectFile();

    if(m_pProjectTab->promptOnSave()) {

        assert(m_pDataTab);
        QString mydir = m_pDataTab->currentPath();
        QDir dir(mydir + "/" + "untitled.xml");
        QFileDialog dialog;
        dialog.setDirectory(dir);

        // open the file browser to the user specified directory
        QFileDialog::Options options = QFileDialog::DontResolveSymlinks;

        // get the project filename
        QString filename = dialog.getSaveFileName(this, 
                                                  tr("Save Project"),
                                                  dir.path(),
                                                  tr("*.xml"),
                                                  NULL,
                                                  options);
        if(!filename.isNull()) {
            QFileInfo theFile(filename);
            writeProjectFile(theFile.absolutePath(), theFile.fileName());
            m_pProjectTab->updateSavedSettings(theFile.fileName());
            projectLabel = m_titleformatString + theFile.baseName() +".xml";
            assert(m_pDataTab);
            m_pDataTab->update(theFile.absolutePath(), theFile.absolutePath() + "/" + theFile.baseName() + ".log");
        }
    }
    else {
        writeProjectFile();
        m_pProjectTab->updateSavedSettings(m_pProjectTab->projectFile());
        projectLabel = m_titleformatString + m_pProjectTab->projectFile();
        QString saving = "The CaptureTool is saving " + m_pProjectTab->projectFile() + "...";
        this->setWindowTitle(saving);
        this->show();
     
        emptywait(3);
    }



    this->setWindowTitle(projectLabel);
    this->show();
}


void TabSetWindow::openProject(void)
{
    // open the file browser to the user specified directory
    QFileDialog::Options options = QFileDialog::DontResolveSymlinks;

    assert(m_pDataTab);
    QString mydir = m_pDataTab->currentPath();
    QDir dir(mydir);
    QFileDialog dialog;
    dialog.setDirectory(dir);


    // get the project filename
    QString filename = dialog.getOpenFileName(this, 
                                              tr("Open Project"),
                                              dir.absolutePath(),
                                              tr("*.xml"),
                                              NULL,
                                              options);
    QFileInfo theFile(filename);

   if(!filename.isNull()) {
        // save the data
        assert(m_pProjectTab);
        importProjectFile(theFile.absolutePath(), theFile.fileName());

        assert(m_pSetUpTab);
        m_pSetUpTab->enableSavedSettings(true);

        assert(m_pDataTab);
        m_pDataTab->update(theFile.absolutePath(), theFile.absolutePath() + "/" + theFile.baseName() + ".log");
        m_pDataTab->resetEquipmentFlag();

        QString projectLabel = m_titleformatString + theFile.baseName() + ".xml";
        this->setWindowTitle(projectLabel);
        this->show();
   }

}


void TabSetWindow::openCurDir(void)
{

    QLabel *dirLabel = new QLabel;

    QFileDialog::Options options = QFileDialog::DontResolveSymlinks | QFileDialog::ShowDirsOnly;
    QString directory = QFileDialog::getExistingDirectory(this,
                                tr("QFileDialog::getExistingDirectory()"),
                                dirLabel->text(),
                                options);
    if (!directory.isEmpty())
        dirLabel->setText(directory);
}


void TabSetWindow::newProject(void)
{

    assert(m_pProjectTab);
    m_pProjectTab->create();

    assert(m_pDataTab);
    m_pDataTab->clear();

    assert(m_pNotesTab);
    m_pNotesTab->clear();

    assert(m_pSetUpTab);
    m_pSetUpTab->clear();

    updateEquipment();

    QString projectLabel = m_titleformatString + m_pProjectTab->projectFile();
    this->setWindowTitle(projectLabel);
    this->show();
}


void TabSetWindow::writeProjectFile(void)
{
    QFileInfo f(m_pProjectTab->projectFile());

    assert(m_pProjectTab);
    assert(m_pDataTab);
    writeProjectFile(m_pDataTab->currentPath(), m_pProjectTab->projectFile());
    writeProjectKnowledge(m_pDataTab->currentPath());
}


// migrate to xml reader
void TabSetWindow::writeProjectFile(QString path, QString filename)
{

    // error checking
    assert(m_pProjectTab);
    assert(m_pDataTab);
    assert(m_pSetUpTab);
    assert(m_pNotesTab);

    // init the full path
    QString outfile = path + "/" + filename;

    // header
    QString fullstring = "<!DOCTYPE digitization-project>\n";
    fullstring = fullstring + "<digitization-project version=\"1.0\" >\n";

    // project
    fullstring = fullstring + m_pProjectTab->exportToXML();

    // data
    fullstring = fullstring + m_pDataTab->exportToXML();

    // equipment
    fullstring = fullstring + m_pSetUpTab->exportToXML("", "");

    // notes
    fullstring = fullstring + m_pNotesTab->exportToXML();

    // footer
    fullstring = fullstring + "</digitization-project>\n";

    // write the correspondences
    QFile f(outfile);
    if(f.exists())
        f.remove();

    // write the data to the file
    saveTextToFile(outfile, fullstring);

}


void TabSetWindow::writeProjectKnowledge(QString path)
{

    // error checking
    assert(m_pProjectTab);
    assert(m_pDataTab);
    assert(m_pSetUpTab);
    assert(m_pNotesTab);
    assert(m_pAboutTab);

    // softare
    exportToRDF(path);

    // project
    m_pProjectTab->exportToRDF(path);

    // data
    m_pDataTab->exportToRDF(path);

    // equipment
    m_pSetUpTab->exportToRDF(path);

    // notes
    m_pNotesTab->exportToRDF(path);

    // about
    m_pAboutTab->exportToRDF(path);

    // now merge RDF files to create digitization project
    createDigitizationProject(path);

}


// create the final digitization project rdf file
void TabSetWindow::createDigitizationProject(QString path)
{
    RDFReader rdfr(m_pStringDefs);


    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Digitization_Project";
    makeDirectory(outfile);
    outfile = outfile + "/" + "DP.rdf";

    // legacy RDF write
    QString fullstring = rdfr.header();
    saveTextToFile(outfile, fullstring);


    // saprano - here
 


}


void TabSetWindow::exportToRDF(QString path)
{
    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "Software";
    makeDirectory(outfile);
    outfile = outfile + "/" + "software.rdf";

    RDFReader rdfr(m_pStringDefs);
    QString data = rdfr.software();
    saveTextToFile(outfile, data);
}




void TabSetWindow::importProjectFile(QString projectPath, QString fname)
{
    QString filefullpath = projectPath + "/" + fname;

    loadDataFromFile(projectPath,  filefullpath);

    m_pProjectTab->open(fname);
}


// migrate to xml reader
bool  TabSetWindow::loadDataFromFile(QString projectPath, QString fname)
{

    assert(!(fname.isNull()));

    QFile file(fname);  
    if (file.open(QIODevice::ReadOnly)) {
        QDomDocument doc;
        bool status = doc.setContent(&file);
        file.close();
        if (status) {
            QDomElement  root(doc.documentElement());
            return (readXML(projectPath, root));
        } 
        else {
            qDebug("couldn't read from `%s'", fname.toUtf8().data());
        } 
    }
    else
        qDebug("couldn't open `%s'", fname.toUtf8().data());

    return(false);
}


// migrate to xml reader
bool TabSetWindow::readXML(QString path, QDomElement &root)
{

    bool stat = false;
    QString str;

    // verify valid version and file info
    if (root.tagName() == DIGITIZATION_PROJCECT_ROOT) {
        if (root.hasAttribute("version")) {
            if (isCompatible(root.attribute("version"), DIGITIZATION_PROJCECT_FILE_VERSION)) {
                QDomElement  node = root.firstChildElement("project");
                m_pProjectTab->import(node);
                m_pDataTab->import(node);
                m_pSetUpTab->import(path, m_pDataTab->dataPath(), node);
                m_pNotesTab->import(node);
            }
            else {
                qDebug("incompatible file version: `%s' instead of `%s'; bailing out...", root.attribute("version").toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
                return(false);
            }
        }
        else {
            qDebug("unknown file version -- add version attribute; bailing out...");
            return(false);
        }
    }
    else {
        qDebug("wrong root tag: `%s' instead of `%s'", root.tagName().toUtf8().data(), DIGITIZATION_PROJCECT_ROOT);
        return(false);
    }

    // made it
    return (true);
}

