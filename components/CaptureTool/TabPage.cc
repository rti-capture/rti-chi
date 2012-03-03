/*===========================================================================
    CaptureTool
    TabPage.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include "TabPage.h"
#include "TabSetWindow.h"


TabPage::TabPage(QWidget* parent)
    :QTabWidget(parent)
{
}


TabPage::~TabPage(void)
{
}

void TabPage::changeTab(int index)
{
    TabSetWindow *p = (TabSetWindow *)this->parent();


    switch(index) {
        case 0:  p->disableButtons(false); break;
        case 1:  p->disableButtons(true); break;
        case 2:  p->updateEquipment();  p->updateProjectRoot(); p->disableButtons(true); break;
        case 3:  p->disableButtons(true); break;
        case 4:  p->disableButtons(true); break;
        default: p->disableButtons(true); break;
    }
}

void TabPage::connectSignals(void)
{
    connect(this , SIGNAL(currentChanged(int)),this,SLOT(changeTab(int)));
}
  
