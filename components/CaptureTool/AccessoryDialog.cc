/*===========================================================================
    CaptureTool
    AccessoryDialog.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include <QtGui>
#include <assert.h>
#include <utility>
#include"AccessoryDialog.h"
#include "globals.h"
#include "utilities.h"
//#include "treemodel.h"
#include "SetUpTab.h"



AccessoryDialog::AccessoryDialog(std::vector<QString> files, std::vector<QString> titles, QList<QListWidget *> *pListOfItems, QList<QStringList *> *map, QWidget *parent)
    : QDialog(parent)
{

    assert(pListOfItems);
    m_pItemList = pListOfItems;
    assert(map);
    m_pMap = map;


    initButtons();

    std::vector<QString> dataList;
    QVBoxLayout *mainLayout = new QVBoxLayout;

    setFixedSize(300, 400);

    assert(files.size() == titles.size());
    assert(pListOfItems);

    pListOfItems->clear();
    int num = (int)files.size();

    // load data
    for(int k = 0; k < num; ++k) {

        QListWidget *pListWidget = new QListWidget();
        pListWidget->setSortingEnabled(true);

        dataList = readLinesFromFile(files[k]);
        for(int i = 0; i < (int)dataList.size(); ++i){
            QListWidgetItem *item = new QListWidgetItem(dataList[i].toLocal8Bit().data());
            item->setCheckState(Qt::Unchecked);
            pListWidget->addItem(item);
            if(APPDEBUG)
                fprintf(stderr, "%s\n", item->text().toLocal8Bit().data());
        }

        QGroupBox *aGr = new QGroupBox(tr(titles[k].toLocal8Bit().data()));
        QGridLayout *aLayout = new QGridLayout;
        aLayout->addWidget(pListWidget);
        aLayout->addWidget(m_pButtonBox);
        aGr->setLayout(aLayout);
        mainLayout->addWidget(aGr);

        if(pListOfItems)
            pListOfItems->append(pListWidget);
    }

    setLayout(mainLayout);
}


AccessoryDialog::~AccessoryDialog()
{
}


void AccessoryDialog::initButtons(void)
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
    connect(m_pApplyButton, SIGNAL(clicked()), this, SLOT(applychanges()));
}


void AccessoryDialog::applychanges(void)
{
    assert((int)m_pMap->size() == m_pItemList->count());

    for(int i = 0; i < m_pItemList->count(); ++i) {
        (*m_pMap)[i]->clear();
        for(int k = 0; k < (*m_pItemList)[i]->count(); ++k) {
            if((*m_pItemList)[i]->item(k)->checkState() == (Qt::Checked)) {
                (*m_pMap)[i]->append((*m_pItemList)[i]->item(k)->text());
            }
        }
    }
}

