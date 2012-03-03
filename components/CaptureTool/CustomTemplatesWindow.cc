/*===========================================================================
    CaptureTool
    CustomTemplatesWindow.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include <QtGui>
#include <QFileInfo>
#include <assert.h>
#include <utility>
#include <QWidget>
#include <QTreeWidget>
#include <QTreeWidgetItem>
#include <QListWidget>
#include <QListWidgetItem>
#include <QPainter>
#include "globals.h"
#include "utilities.h"
#include "IconSelectionRect.h"
#include "IconWindow.h"
#include "TemplateSetupWindow.h"
#include "CustomTemplatesWindow.h"
#include "SetUpTab.h"
#include "StringDefs.h"





DisplayItem::DisplayItem(int num, QWidget *parent)
    : QWidget(parent)
{

    this->resize(60, 60); 
    this->setMinimumSize(60, 60);
}


DisplayItem::~DisplayItem()
{
}


void DisplayItem::paintEvent(QPaintEvent *event)
{
     QPainter painter;
     painter.begin(this);
     painter.drawText(20,20,"hi there testing");
     painter.end();
}


void DisplayItem::createTemplateTable(void)
{
}


CustomTemplatesWindow::CustomTemplatesWindow(StringDefs *pStringDefs, QString equipmentroot, QString projectroot, QWidget *parent)
    : QDialog(parent),
      m_pNewTemplateButton              (NULL),
      m_pTemplateButtonBox              (NULL),
      m_pTreeWidget                     (NULL),
      m_pIconSelectionWindow            (NULL),
      m_pTemplateSetupWindow            (NULL),
      m_equipmentroot                   (equipmentroot),
      m_projectroot                     (projectroot),
      m_pStringDefs                     (pStringDefs)
{

    // widget sizes
    QSize mize(800, 300);
    QSize wize(600, 300);
    QSize tsize(800, 150);
    QSize isize(600, 150);
    QSize lsize(600, 50);


    // icons and titles
    QIcon m_icon("./appdata/logo32.png");
    this->setWindowTitle("Template Manager");
    this->setWindowIcon(m_icon);
    this->setStyleSheet("PageFold"); 
    this->setFixedSize(mize);

    // control panel
    initButtonMenu();

    // tree widget
    m_pTreeWidget         = new QTreeWidget(this); 
    m_pTreeWidget->setFixedSize(wize);
    m_pTreeWidget->setWindowTitle("Custom Templates");
    QStringList headers;
    headers << tr("Custom Templates");


    QString path = m_pStringDefs->getDataPath(equipmentroot, pStringDefs->templates());
    QDir dir(path);
    QFileInfoList info = dir.entryInfoList();
    for(int i=0; i < (int)info.size(); ++i) {
        if(info[i].isDir() && (info[i].fileName().compare(".") != 0) && (info[i].fileName().compare("..") != 0)) {
            addTemplateList(m_pTreeWidget, 0, info[i].fileName(),   3, isize, tsize, lsize);
        }
     }

    m_pTreeWidget->setHeaderLabels(headers);
    m_pTreeWidget->show();

    // layout the windows
    QGridLayout *mainLayout = new QGridLayout;
    mainLayout->addWidget(m_pTemplateButtonBox, 0, 0, 1, 1);
    mainLayout->addWidget(m_pTreeWidget, 0, 1,  1, 1);
    setLayout(mainLayout);

    connect(m_pTreeWidget,   SIGNAL(currentItemChanged(QTreeWidgetItem *, QTreeWidgetItem *)), this, SLOT(checkActiveItem(QTreeWidgetItem *, QTreeWidgetItem *)));
}


void CustomTemplatesWindow::addTemplateList(QTreeWidget *root, int position, QString title, int numEntries,  QSize iconSize, QSize entrySize, QSize lsize)
{
    IconWindow *pIW = new IconWindow(m_pStringDefs, title, m_equipmentroot, m_projectroot, this);

    QLineEdit *thisedit = new QLineEdit();

    QTreeWidgetItem *pTreeItem      = new QTreeWidgetItem(root);
 

    //pTreeItem->setText(0, "top");
    pTreeItem->setText(0, title);
    pTreeItem->setSizeHint(0, lsize);

    QListWidget *pList               = new QListWidget(); 
    pList->setFixedSize(entrySize);
    pList->setMovement(QListView::Static);
    pList->setResizeMode(QListView::Adjust);
    pList->setViewMode(QListView::IconMode);

    QListWidgetItem *pListItem = new QListWidgetItem(pList);
   // pListItem->setText("this one has the list");
    pListItem->setSizeHint(iconSize);
    pList->setItemWidget(pListItem, pIW);

    pList->setAutoFillBackground(true);
    QTreeWidgetItem *pSubTreeItem = new QTreeWidgetItem(pTreeItem);
   //pSubTreeItem->setText(0, "this is my subroot");
    root->setItemWidget(pSubTreeItem, position, pList);

    m_IList.insert(std::make_pair(title, pIW));

    if(pIW->preview())
        pIW->preview()->deselectAll();
}


QToolButton *CustomTemplatesWindow::createToolButton(const QString &toolTip,
                                                     const QIcon &icon, const char *member)
{
    QToolButton *button = new QToolButton(this);
    button->setToolTip(toolTip);
    button->setIcon(icon);
    button->setIconSize(QSize(32, 32));
    connect(button, SIGNAL(clicked()), this, member);

    return button;
}


void CustomTemplatesWindow::initButtonMenu (void)
{
    m_pNewTemplateButton = createToolButton(tr("New Square"),
                                       QIcon("./appdata/win/square.png"),
                                       SLOT(createNewSquare()));
    m_pTemplateButtonBox = new QDialogButtonBox(Qt::Vertical);
    m_pTemplateButtonBox->addButton(m_pNewTemplateButton,  QDialogButtonBox::ActionRole);
    connect(m_pNewTemplateButton,   SIGNAL(clicked()), this, SLOT(newTemplate()));
    m_pNewTemplateButton->setEnabled(false);
}


void CustomTemplatesWindow::newTemplate (void)
{
    if(!m_pTreeWidget)
        return;

    QTreeWidgetItem *pTreeItem = m_pTreeWidget->currentItem();
    if(!pTreeItem)
        return;

    if(!pTreeItem->isExpanded())
        return;

    QString name = pTreeItem->text(0); 

    if(m_IList.find(name) != m_IList.end()); {
        m_IList.find(name)->second->createNewItem();
    }

}


CustomTemplatesWindow::~CustomTemplatesWindow()
{
}

void CustomTemplatesWindow::checkActiveItem(QTreeWidgetItem *current, QTreeWidgetItem *previous)
{
    if(current->isExpanded()) {
        m_pNewTemplateButton->setDisabled(false);
    }
    this->show();
}

void CustomTemplatesWindow::setDisableNewTemplates(bool flag)
{
    m_pTemplateButtonBox->setDisabled(flag);

}

void CustomTemplatesWindow::selectNewIcon(QString selectionroot, QString title, QString category)
{
    m_pIconSelectionWindow = new IconWindow(m_pStringDefs, "Selection", selectionroot, m_projectroot, this);
    m_pIconSelectionWindow->resize(QSize(800, 250));
    m_pIconSelectionWindow->setCurTitle(title);
    m_pIconSelectionWindow->setSelectionRoot(selectionroot);
    m_pIconSelectionWindow->setSelectionCategory(category);
    m_pIconSelectionWindow->show();
}


TemplateSetupWindow *CustomTemplatesWindow::displayEditWindow(QString equipmentroot, QString projectroot, QString category, QString basename)
{
    if(m_pTemplateSetupWindow)
        delete(m_pTemplateSetupWindow);
            
    m_pTemplateSetupWindow = new TemplateSetupWindow(m_pStringDefs, equipmentroot, projectroot, category, basename, this);
    m_pTemplateSetupWindow->show();
    return(m_pTemplateSetupWindow);
}


// ToDo: consolidate/move this to XMLReader
void CustomTemplatesWindow::create(QString equipmentroot, QString basename, QString category)
{

    QString outfile = m_pStringDefs->getDataFileName(equipmentroot, basename, "templates", category);
    QFile f(outfile);

    // header
    QString fullstring = "<!DOCTYPE saved-template>\n";
    fullstring = fullstring + "<saved-template version=\"1.0\" >\n";


    fullstring = fullstring + "<icon   path=\"%s\" /> \n";
    fullstring.sprintf(fullstring.toLocal8Bit().data(), m_pStringDefs->defaultIcon().toLocal8Bit().data());

    // footer
    fullstring = fullstring + "</saved-template>\n";

    // write the correspondences
    if(f.exists())
        f.remove();

    // write the data to the file
    saveTextToFile(outfile, fullstring);
}


QString CustomTemplatesWindow::equipmentRoot(void)
{
    return(m_equipmentroot);
}
