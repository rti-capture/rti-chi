/*===========================================================================
    CaptureTool
    CustomSettingsWindow.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include "CustomSettingsWindow.h"
#include <QtGui>
#include <assert.h>
#include <utility>
#include "globals.h"
#include "utilities.h"
#include "GUIUtility.h"
#include "SetUpTab.h"




CustomSettingsWindow::CustomSettingsWindow (QTreeWidget *pEquipmentList, QWidget *parent)
    : QDialog(parent),
    m_bProcessSwitch(true)
  {
    assert(pEquipmentList);
    m_pEquipmentList = pEquipmentList;

    initButtons();

    QIcon thisicon("./appdata/logo32.png");
    this->setWindowIcon(thisicon);
    this->setFixedSize(700, 400);

    // init groupbox and layout
    QGroupBox *captureGrBox = new QGroupBox(tr("Capture Settings"));
    QGridLayout *layout = new QGridLayout;

    // The saved list of camera settings
    layout->addWidget(new QLabel(tr("Setup List")), 0, 0, 1, 1);
    m_pIcon = new QIcon("./appdata/camera_32.png");    
    m_pListWidget = new QListWidget(this);
    layout->addWidget(m_pListWidget, 1, 0, 1, 1);

   // add the text edit for the name
    QLabel *nameLabel = new QLabel(tr("Name:"));
    m_pNameLineEdit = new QLineEdit;
    layout->addWidget(nameLabel, 2, 0, 1, 1);
    layout->addWidget(m_pNameLineEdit, 3, 0, 1, 1);

    // add the text edit for the name
    QLabel *notesLabel = new QLabel(tr("Notes:"));
    layout->addWidget(notesLabel, 4, 0, 1, 1);
    m_pSmallEditor = new QTextEdit;
    layout->addWidget(m_pSmallEditor, 5, 0, 1, 1);

    // add the equipment list
    layout->addWidget(m_pEquipmentList, 0, 1, 6, 1);
    layout->addWidget(m_pButtonBox, 6, 0, 1, 1);

    // adjust layout
    layout->setColumnStretch(0, 1);
    layout->setColumnStretch(1, 2);

    // connect signals
    connect(m_pNameLineEdit, SIGNAL(editingFinished()), this, SLOT(saveCaptureSettings()));
    connect(m_pListWidget, SIGNAL(itemSelectionChanged()), this, SLOT(switchSettings()));

    // add widgets to group box
    captureGrBox->setLayout(layout);

    // add the main layout
    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(captureGrBox);
    setLayout(mainLayout);
}


void CustomSettingsWindow::saveCaptureSettings(void)
{

    if(!(m_pNameLineEdit->text().isEmpty())) {

       QList<QListWidgetItem *> data;

        // insert the icon
        data = m_pListWidget->findItems(m_pNameLineEdit->text(), Qt::MatchExactly);
        if(!data.count()) {
            new QListWidgetItem(*m_pIcon, tr(m_pNameLineEdit->text().toLocal8Bit().data()), m_pListWidget);
            data = m_pListWidget->findItems(m_pNameLineEdit->text(), Qt::MatchExactly);
        }

        m_pListWidget->setCurrentItem(data[0]);
        SetUpTab *p = (SetUpTab *)this->parent();
        p->exportSettings(m_pNameLineEdit->text(), getNotes());
    }
}


QString CustomSettingsWindow::getNotes(void)
{
    QString fullstring;
    QTextDocument *pDoc = m_pSmallEditor->document();

   for (QTextBlock it = pDoc->begin(); it != pDoc->end(); it = it.next()) {
        fullstring = fullstring + it.text();
    }

    return(fullstring);
}


void CustomSettingsWindow::updateSavedSettings(int current, QStringList names)
{
    m_pListWidget->clear();
    int itemremove = -1;

    for(int i = 0; i < (int)names.size(); ++i) {
        new QListWidgetItem(*m_pIcon, names[i].toLocal8Bit().data(), m_pListWidget);

        if(names[i].compare("select...") == 0)
            itemremove = i;

    }
    m_pListWidget->setCurrentRow(current, QItemSelectionModel::SelectCurrent);

    if(APPDEBUG) {
        fprintf(stderr, "remove this %s \n", m_pListWidget->item(itemremove)->text().toLocal8Bit().data());
    }

    m_pNameLineEdit->setText(names[current]);
    if(itemremove !=  -1)
        delete (m_pListWidget->takeItem(itemremove));


}

void CustomSettingsWindow::switchSettings(void)
{
    QString newname;
    QString select = "select...";

    SetUpTab *p = (SetUpTab *) this->parent();

    if((m_pListWidget) && 
       (m_pListWidget->currentItem()) &&
       (select.compare(m_pListWidget->currentItem()->text()) != 0)) {
           newname = m_pListWidget->currentItem()->text();
    }

    m_pNameLineEdit->setText(newname);
    if((p) && (m_bProcessSwitch)) {
        p->processSwitch(newname);
        p->updateEquipmentList();
    }
}


void CustomSettingsWindow::updateNotes(QString notes)
{
    m_pSmallEditor->clear();
    m_pSmallEditor->setText(notes);

}


void CustomSettingsWindow::setProcessSwitch(bool flag)
{
    m_bProcessSwitch = flag;
}


void CustomSettingsWindow::initButtons(void)
{

    m_pSaveButton  = new QPushButton(tr("&Save"));
    m_pCancelButton  = new QPushButton(tr("&Cancel"));
    m_pCloseButton  = new QPushButton(tr("&Close"));
     
    m_pButtonBox = new QDialogButtonBox(Qt::Horizontal);
    m_pButtonBox->addButton(m_pSaveButton,   QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pCancelButton,   QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pCloseButton,   QDialogButtonBox::ActionRole);

    connect(m_pSaveButton, SIGNAL(clicked()), this, SLOT(saveCaptureSettings()));
    connect(m_pCancelButton, SIGNAL(clicked()), this, SLOT(close()));
    connect(m_pCloseButton, SIGNAL(clicked()), this, SLOT(close()));
}



