/*===========================================================================
    CaptureTool
    IconWindow.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#include <QtGui>
#include <QPushButton>
#include <QDialogButtonBox>
#include "IconWindow.h"
#include "IconSelectionRect.h"
#include "CustomTemplatesWindow.h"
#include "PixelLabel.h"
#include "StringDefs.h"
#include "XMLReader.h"
#include "globals.h"


IconWindow::IconWindow(StringDefs *pStringDefs, QString category, QString equipmentroot, QString projectroot, QWidget *parent)
    : QMainWindow(parent),
      m_pIconSelectionWindow(NULL),
      m_pCancelButton       (NULL),
      m_pCloseButton        (NULL),
      m_pApplyButton        (NULL),
      m_category            (category),
      m_pStringDefs         (pStringDefs),
      m_pButtonBox          (NULL),
      m_pTableWidget        (NULL)
{

    getNames(equipmentroot, category, pStringDefs);

    centralWidget = new QWidget;
    setCentralWidget(centralWidget);
    initLayout();


    QGridLayout *mainLayout = new QGridLayout;
    m_pPreviewArea = new IconSelectionRect(m_pStringDefs, m_names, equipmentroot, projectroot, category, this);
    if(category.compare("Selection") == 0) {
        m_pPreviewArea->resize(800, 200);
    }

    mainLayout->addWidget(m_pPreviewArea, 0, 0);
    if(category.compare("Selection") == 0) {
        initButtons();
        mainLayout->addWidget(m_pButtonBox, 1, 0);
    }

    centralWidget->setLayout(mainLayout);
    populate(m_names);
}


void IconWindow::getNames(QString path, QString category, StringDefs *pStringDefs)
{
    QString type(SAVED_TEMPLATE_ROOT);
    QString version(SAVED_TEMPLATE_FILE_VERSION);
    XMLReader xmlr(pStringDefs);

    if(category.compare("Selection") == 0) {
        m_names << "C:/cygwin/home/Corey/ChiCaptureApp/appdata/icons/win/support.bmp" << "C:/cygwin/home/Corey/ChiCaptureApp/appdata/icons/win/lights.jpg";
        return;
    }

    QDir d(path + "/" + m_pStringDefs->templates() + "/" + category  + "/" + pStringDefs->metadata());
    QFileInfoList sub = d.entryInfoList();
    for(int i=0; i < (int)sub.size(); ++i) {
        if(sub[i].isFile()) {
            if(sub[i].fileName().indexOf(".xml") != -1) {
                QString g = xmlr.getData(sub[i].absoluteFilePath(), pStringDefs->icon(), pStringDefs->path(), type, version);
                if(!g.isNull() ||  !g.isEmpty())
                    m_names  << g;
            } 
        }
     }
}


void IconWindow::updateSelectButton(bool stat)
{
    if(!m_category.isNull())
        return;

    if(!(m_category.compare("Selection") == 0))
        return;

    if(!m_pApplyButton)
        return;

    m_pApplyButton->setDisabled(!stat);
}


void IconWindow::initButtons(void)
{
    m_pCancelButton = new QPushButton(tr("&Cancel"));
    m_pCloseButton  = new QPushButton(tr("&Close"));
    m_pApplyButton  = new QPushButton(tr("&Select"));
     
    m_pButtonBox = new QDialogButtonBox(Qt::Horizontal);
    m_pButtonBox->addButton(m_pCancelButton,   QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pCloseButton,    QDialogButtonBox::ActionRole);
    m_pButtonBox->addButton(m_pApplyButton,    QDialogButtonBox::ActionRole);

    connect(m_pCancelButton,SIGNAL(clicked()), this, SLOT(close()));
    connect(m_pCloseButton, SIGNAL(clicked()), this, SLOT(close()));
    connect(m_pApplyButton, SIGNAL(clicked()), this, SLOT(selectIconStyle()));

    //m_pApplyButton->setDisabled(true);
}


void IconWindow::selectIconStyle(void)
{
    m_newIconFile = m_pPreviewArea->getContext()->iconFileName();

    QString iconfname = m_newIconFile;
    QString icontitle = m_curTitle;
    QString datafname = m_pStringDefs->getDataFileName(m_selectionroot, icontitle, m_pStringDefs->templates(), m_selectioncategory);

    XMLReader xmlr(m_pStringDefs);
    QString type(SAVED_TEMPLATE_ROOT);
    QString version(SAVED_TEMPLATE_FILE_VERSION);
    if(!icontitle.isNull() || icontitle.isEmpty())
        xmlr.update(datafname, m_pStringDefs->icon(), m_pStringDefs->path(), iconfname, type, version);

    close();

    m_pStringDefs->setiswap(iconfname);

}


void IconWindow::createNewItem(void)
{
    m_pPreviewArea->createNewItem();
    QString  basename = m_pPreviewArea->getContext()->title();
    CustomTemplatesWindow *pCustomTemplateWindow = (CustomTemplatesWindow *)(this->parent());
    pCustomTemplateWindow->create(m_pPreviewArea->equipmentRoot(), basename, m_category);
    this->show();
}


void IconWindow::removeItem(void)
{
    m_pPreviewArea->removeItem();
    this->show();
}


void IconWindow::changeSize(bool checked)
{
    if (!checked)
        return;

    int extent = 64;
    m_pPreviewArea->setSize(QSize(extent, extent));
}


void IconWindow::changeIcon()
{
    QIcon icon;

    for (int row = 0; row < m_pTableWidget->rowCount(); ++row) {
        QTableWidgetItem *item0 = m_pTableWidget->item(row, 0);
        if (item0->checkState() == Qt::Checked) {

            QString fileName = item0->data(Qt::UserRole).toString();
            QImage image(fileName);
            if (!image.isNull())
                icon.addPixmap(QPixmap::fromImage(image), QIcon::Normal, QIcon::On);
        }
    }

    m_pPreviewArea->setIcon(icon);
}


void IconWindow::populate(QStringList fileNames)
{
    if (!fileNames.isEmpty()) {
        foreach (QString fileName, fileNames) {
            int row = m_pTableWidget->rowCount();
            m_pTableWidget->setRowCount(row + 1);
            QString imageName = QFileInfo(fileName).baseName();
            QTableWidgetItem *item0 = new QTableWidgetItem(imageName);
            item0->setData(Qt::UserRole, fileName);
            item0->setFlags(item0->flags() & ~Qt::ItemIsEditable);
            m_pTableWidget->setItem(row, 0, item0);
            item0->setCheckState(Qt::Checked);
        }
    }
}


void IconWindow::initLayout(void)
{
    QGroupBox *pGrBox = new QGroupBox(tr("selections"));
    m_pTableWidget = new QTableWidget;

    m_pTableWidget->setSelectionMode(QAbstractItemView::NoSelection);
    connect(m_pTableWidget, SIGNAL(itemChanged(QTableWidgetItem*)), this, SLOT(changeIcon()));

    QVBoxLayout *layout = new QVBoxLayout;
    layout->addWidget(m_pTableWidget);
    pGrBox->setLayout(layout);
}



void IconWindow::iconSelectionWindow(QString equipmentroot, QString title, QString category)
{
    CustomTemplatesWindow *p = (CustomTemplatesWindow *)this->parent();
    p->selectNewIcon(equipmentroot, title, category);
}


QString IconWindow::selectedIconName(void)
{
    QString n;
    n = m_pIconSelectionWindow->selectedIconName();
    return(n);
}


void IconWindow::setCurTitle(QString title)
{
    m_curTitle = title;
}


void IconWindow::setSelectionRoot(QString path)
{
    m_selectionroot = path;
}


void IconWindow::setSelectionCategory(QString c)
{
    m_selectioncategory = c;
}

