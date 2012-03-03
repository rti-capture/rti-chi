/*===========================================================================
    Capture Tool
    TemplateSetupWindow.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QtGui>
#include <QPushButton>
#include <QDialogButtonBox>
#include <QFileInfo>
#include "TemplateSetupWindow.h"
#include "CustomTemplateSetup.h"
#include "CustomTemplatesWindow.h"
#include  "StringDefs.h"




TemplateSetupWindow::TemplateSetupWindow(StringDefs *pStringDefs, QString equipmentroot, QString projectroot, QString category, QString basename, QWidget *parent)
    : QMainWindow(parent),
      m_equipmentroot               (equipmentroot), 
      m_projectroot                 (projectroot),
      m_category                    (category),
      m_basename                    (basename), 
      m_pStringDefs                 (pStringDefs)
{

    QIcon m_icon("./appdata/logo32.png");
    this->setWindowTitle("Custom Template - " + basename);
    this->setWindowIcon(m_icon);
    QFileInfo f("fossil.xml");

    initButtons();

    m_pCustomTemplateSetup = new CustomTemplateSetup(m_pStringDefs, f, equipmentroot);

    m_pCentralWidget = new QWidget();
    setCentralWidget(m_pCentralWidget);

    m_pMainLayout = new QGridLayout();
    m_pMainLayout->addWidget(m_pCustomTemplateSetup, 0, 0);
    m_pMainLayout->addWidget(m_pButtonBox, 1, 0);
    m_pCentralWidget->setLayout(m_pMainLayout);

    QString datafilename = pStringDefs->getDataFileName(m_equipmentroot, m_basename, pStringDefs->templates(), m_category);
    m_pCustomTemplateSetup->load(datafilename);

}


TemplateSetupWindow::~TemplateSetupWindow(void)
{
}


void TemplateSetupWindow::initButtons(void)
{
    m_pCancelButton = new QPushButton(tr("&Cancel"));
    m_pCloseButton  = new QPushButton(tr("&Close"));
    m_pApplyButton  = new QPushButton(tr("&Apply"));
     
    m_pButtonBox = new QDialogButtonBox(Qt::Horizontal);
    m_pButtonBox->addButton(m_pCancelButton,   QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pCloseButton,    QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pApplyButton,    QDialogButtonBox::ActionRole);


    connect(m_pCancelButton,SIGNAL(clicked()), this, SLOT(close()));
    connect(m_pCloseButton, SIGNAL(clicked()), this, SLOT(close()));
    connect(m_pApplyButton, SIGNAL(clicked()), this, SLOT(saveTemplate()));

}

void TemplateSetupWindow::test(void)
{
    fprintf(stderr, "test\n");
}


void TemplateSetupWindow::saveTemplate(void)
{
    m_pCustomTemplateSetup->save(m_equipmentroot, m_category, m_basename);
    close();
}


void TemplateSetupWindow::newTemplate(void)
{
    m_pCustomTemplateSetup->save(m_equipmentroot, m_category, m_basename);
    close();
}


