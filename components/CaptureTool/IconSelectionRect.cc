/*===========================================================================
    CaptureTool
    IconSelectionRect.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include <QtGui>
#include <assert.h>
#include <QContextMenuEvent>
#include <QMessageBox>
#include "IconSelectionRect.h"
#include "PixelLabel.h"
#include "IconWindow.h"
#include "CustomTemplateSetup.h"
#include "CustomTemplatesWindow.h"
#include "utilities.h"
#include "StringDefs.h"
#include "XMLReader.h"
#include "globals.h"



IconSelectionRect::IconSelectionRect(StringDefs *pStringDefs, QStringList list, QString equipmentroot, QString projectroot, QString category, QWidget *parent)
    : QWidget(parent),
      lw                    (84),
      lh                    (84),
      m_list                (list),
      m_pContext            (NULL),
      m_next                (0),
      m_equipmentroot       (equipmentroot),
      m_projectroot         (projectroot),
      m_category            (category),
      m_pStringDefs         (pStringDefs)
{
    setUpLayout();
    createActions();

    if(list.size())
        m_name = list[0];
}


void IconSelectionRect::setUpLayout(void) 
{
    QGridLayout *mainLayout = new QGridLayout;
    mainLayout->setHorizontalSpacing(0);
    setLayout(mainLayout);

    for (int i = 0; i < m_list.count(); ++i) {
        pixmapLabels.append(createPixmapLabel());
        pixmapLabels[i]->initData(m_list[i], i, false);
        QImage image = pixmapLabels[i]->image();
        assert(!image.isNull());
        QIcon icon;
        icon.addPixmap(QPixmap::fromImage(image), QIcon::Normal, QIcon::On);
        QPixmap pixmap = icon.pixmap(size, QIcon::Normal, QIcon::On);
        pixmapLabels[i]->setPixmap(pixmap);
        mainLayout->addWidget(pixmapLabels[i], 0, i+1);
   }
}


void IconSelectionRect::updateLayout(void)
{
    QGridLayout *pOldLayout = (QGridLayout *)this->layout();
    delete(pOldLayout);

    QGridLayout *mainLayout = new QGridLayout;
    mainLayout->setHorizontalSpacing(0);
    setLayout(mainLayout);

    for (int i = 0; i < pixmapLabels.count(); ++i) {
        mainLayout->addWidget(pixmapLabels[i], 0, i+1);
    }
}


void IconSelectionRect::createNewItem(void)
{
    pixmapLabels.insert(pixmapLabels.begin(), createPixmapLabel());
    pixmapLabels[0]->initData(m_name, m_next, true);
    ++m_next;
    updateLayout();

    QString fname = m_pStringDefs->defaultIcon();
    QImage image(fname);
    if (!image.isNull()) {
        icon.addPixmap(QPixmap::fromImage(image), QIcon::Normal, QIcon::On);
    }   
    QPixmap pixmap = icon.pixmap(this->size, QIcon::Normal, QIcon::On);
    pixmapLabels[0]->setPixmap(pixmap);
    pixmapLabels[0]->setEnabled(!pixmap.isNull());

    setContextPL(pixmapLabels[0]);
    deselectAll();
    pixmapLabels[0]->select();

}


void IconSelectionRect::removeItem(void)
{
    for (int i = 0; i < pixmapLabels.count(); ++i) {
        if(pixmapLabels[i]->title().compare(m_pContext->title()) == 0) {
            PixelLabel *data = pixmapLabels[i];
            pixmapLabels.removeAt(i);
            delete(data);
            break;
        }
    }
    updateLayout();
}


void IconSelectionRect::setIcon(const QIcon &icon)
{
    this->icon = icon;
    updatePixmapLabels(QIcon::Normal);
}


void IconSelectionRect::setSize(const QSize &size)
{
    if (size != this->size) {
        this->size = size;
        lw = this->size.width() + 30;
        lh = this->size.height() + 30;
        updatePixmapLabels(QIcon::Normal);
    }
}


QLabel *IconSelectionRect::createHeaderLabel(const QString &text)
{
    QLabel *label = new QLabel(tr("<b>%1</b>").arg(text));
    label->setAlignment(Qt::AlignCenter);
    return label;
}


PixelLabel *IconSelectionRect::createPixmapLabel()
{
    // create pixel label
    PixelLabel *pPixelLabel = new PixelLabel(m_pStringDefs, 1, this);

    // pixel label parameters
    pPixelLabel->setEnabled(false);
    pPixelLabel->setAlignment(Qt::AlignCenter);
    pPixelLabel->setFrameShape(QFrame::Box);
    pPixelLabel->setFixedSize(lw, lh);
    pPixelLabel->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Expanding);
    pPixelLabel->setBackgroundRole(QPalette::Base);
    pPixelLabel->setAutoFillBackground(true);
    pPixelLabel->setMinimumSize(lw, lh);

    // return the label
    return pPixelLabel;
}


void IconSelectionRect::updatePixmapLabels(QIcon::Mode mode)
{
    for (int i = 0; i < pixmapLabels.count(); ++i) {
        QImage image = pixmapLabels[i]->image();
        assert(!image.isNull());
        icon.addPixmap(QPixmap::fromImage(image), mode, QIcon::On);
        QPixmap pixmap = icon.pixmap(size, mode, QIcon::On);
        pixmapLabels[i]->setPixmap(pixmap);
        pixmapLabels[i]->setEnabled(!pixmap.isNull());
    }
}


void IconSelectionRect::deselectAll(void)
{
     for (int i = 0; i < pixmapLabels.count(); ++i) {
        pixmapLabels[i]->deselect();
     }
}


void IconSelectionRect::rename(void)
{
   if(!m_pContext)
        return;

   bool ok;
   QString text = QInputDialog::getText(this, tr("QInputDialog::getText()"),
                                          tr("New name:"), QLineEdit::Normal,
                                          m_pContext->title(), &ok);
    if (ok && !text.isEmpty())
        m_pContext->setTitle(text);
}


void IconSelectionRect::viewDetails(void)
{
    QString dataFile = m_pStringDefs->getDataFileName(m_equipmentroot, m_pContext->title(), "templates", m_category);

    if(m_pContext->hasData(dataFile)) {
        edit();
    }
    else {
        QMessageBox msgBox;
        QIcon icon("./appdata/logo32.png");
        msgBox.setIcon(QMessageBox::Information);
        msgBox.addButton(QMessageBox::Ok);
        msgBox.setText("This template is empty. Choose \"Edit\" to add information.");
        msgBox.exec();

    }

}


void IconSelectionRect::edit(void)
{
    QString  basename = m_pContext->title();
    IconWindow *pIconWindow = (IconWindow *) this->parent();
    CustomTemplatesWindow *pCustomTemplateWindow = (CustomTemplatesWindow *)(pIconWindow->parent());
    TemplateSetupWindow *pTemplateSetupWindow = pCustomTemplateWindow->displayEditWindow(m_equipmentroot, m_projectroot, m_category, basename);
}


void IconSelectionRect::changeIcon(void)
{
    QString newname;
    IconWindow *p = (IconWindow *)this->parent();
    if(p)
        p->iconSelectionWindow(m_equipmentroot, m_pContext->title(), m_category);

   show();

   CustomTemplatesWindow *cw = (CustomTemplatesWindow*)p->parent();
   newname = m_pStringDefs->iswap();
   getContext()->setIcconFileName(newname, true);
 
}


void IconSelectionRect::deleteItem(void)
{
    removeItem();
}


void IconSelectionRect::setContextPL(PixelLabel *pl)
{
    m_pContext = pl;
}


QString IconSelectionRect::selectedIconName(void)
{
    return(QString());
    return(m_pContext->title());
}


 void IconSelectionRect::createActions(void)
{
    editAction = new QAction(tr("&Edit"), this);
    connect(editAction, SIGNAL(triggered()), this, SLOT(edit()));

    viewDetailsAction = new QAction(tr("&View Details"), this);
    connect(viewDetailsAction, SIGNAL(triggered()), this, SLOT(viewDetails()));

    renameAction = new QAction(tr("&Rename"), this);
    connect(renameAction, SIGNAL(triggered()), this, SLOT(rename()));

    changeIconAction = new QAction(tr("&Change Icon"), this);
    connect(changeIconAction, SIGNAL(triggered()), this, SLOT(changeIcon()));

    deleteAction = new QAction(tr("&Delete"), this);
    connect(deleteAction, SIGNAL(triggered()), this, SLOT(deleteItem()));
 }


 void IconSelectionRect::contextMenuEvent(QContextMenuEvent *event)
 {/*
    QMenu menu(this);
    menu.addAction(editAction);
    menu.addAction(viewDetailsAction);
    menu.addAction(changeIconAction);
    menu.addAction(renameAction);
    menu.addSeparator();
    menu.addAction(deleteAction);
    menu.exec(event->globalPos());
    menu.show();*/
 }


QIcon IconSelectionRect::getIcon(void)
{
    return(this->icon);
}


void IconSelectionRect::mousePressEvent(QMouseEvent *event)
{
    deselectAll();
}

