/*===========================================================================
    CaptureTool
    PixelLabel.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include "PixelLabel.h"
#include "IconSelectionRect.h"
#include <QLineEdit>
#include <QPainter>
#include <QFileInfo>
#include "utilities.h"
#include "IconWindow.h"
#include "XMLReader.h"
#include "globals.h"
#include "StringDefs.h"



PixelLabel::PixelLabel (StringDefs *pStringDefs, int mode, QWidget *parent)
    :QLabel(parent),
    m_bIsSelected           (false),
    m_mode                  (mode),
    m_pStringDefs           (pStringDefs)
{
    m_pLineEdit = new QLineEdit("untitled");
}


PixelLabel::~PixelLabel (void)
{
    if(m_pLineEdit)
        delete(m_pLineEdit);
}


void PixelLabel::deselect(void)
{
    QIcon icon;
    icon.addPixmap(QPixmap::fromImage(m_image), QIcon::Selected, QIcon::On);
    QPixmap pixmap = icon.pixmap(this->size().width(), this->size().height(), QIcon::Normal, QIcon::On);
    this->setPixmap(pixmap);
    IconSelectionRect * pPW =  (IconSelectionRect *)this->parent();
    IconWindow * pIW = (IconWindow *)pPW->parent();
    pIW->updateSelectButton(false);
    m_bIsSelected =  false;

}


void PixelLabel::initData(QString name, int i, bool bInit)
{

    QFileInfo f(name);
    m_filename = f.absoluteFilePath();

    QImage image(name);
    QString id = f.baseName();

    if (bInit) {
        id = "untitled";
    }

    if(i >= 0) {
        QString append;
        append.sprintf("_%03d", i);
        id = id + append;
        setTitle(id);
    }

    setImage(image);

    if(image.isNull()){
        int debugstop;
        debugstop = 1;
    }

}

void PixelLabel::select(QIcon icon)
{
    QPixmap pixmap = icon.pixmap(this->pixmap()->size(), QIcon::Selected, QIcon::On);
    this->setPixmap(pixmap);
    m_bIsSelected =  true;
}


void PixelLabel::select(void)
{
    // first set the mode
    QIcon::Mode mode = QIcon::Selected;

    // always use an on state
    QIcon::State state = QIcon::On;

    // Next get the filename for this mode
    QString name = getUserPixelMapImage(m_filename);

    // make the icon 
    QIcon icon;
    QImage image(name);
    if (!image.isNull()) {
        QPixmap pm = QPixmap::fromImage(image);
        icon.addPixmap(QPixmap::fromImage(image), QIcon::Selected, QIcon::On);
        IconSelectionRect *p = (IconSelectionRect *)this->parent();
        p->setIcon(icon);
        setPixmap(pm);
    }


    // update
    m_bIsSelected =  true;
    IconSelectionRect * pPW =  (IconSelectionRect *)this->parent();
    IconWindow * pIW = (IconWindow *)pPW->parent();
    pIW->updateSelectButton(true);
}


void PixelLabel::mousePressEvent(QMouseEvent *event)
{
    // is the current pointed to

     IconSelectionRect *p = (IconSelectionRect *)this->parent();
        if(!p)
            return;

    if(m_mode == 0) {
        p->setContextPL(this);
        p->deselectAll();
        select(p->getIcon());
    }
    else if(m_mode == 1) {
        p->setContextPL(this);
        p->deselectAll();
        select();
    }
}


void PixelLabel::setTitle(QString title)
{

    QFileInfo f(m_filename);
    QString newDataFile = f.absolutePath() + "/" + "data" + "/" + title + ".xml";
    QString oldDataFile = f.absolutePath() + "/" + "data" + "/" + m_pLineEdit->text() + ".xml";

    QFile file(oldDataFile);
    if(file.exists())
        file.copy(newDataFile);

    m_pLineEdit->setText(title);

}

bool PixelLabel::hasData(QString fname)
{
    QString type(SAVED_TEMPLATE_ROOT);
    QString version(SAVED_TEMPLATE_FILE_VERSION);

    XMLReader xmlr(m_pStringDefs);
    if(xmlr.hasNode(fname, "setup", type, version))
        return(true);
    return(false);

}


QLineEdit *PixelLabel::lineEdit(void)
{
    return(m_pLineEdit);
}


void PixelLabel::setTitleSize(int w, int h)
{
    m_pLineEdit->resize(QSize(w, h));
}


QString PixelLabel::title(void)
{
    return(m_pLineEdit->text());
}


void PixelLabel::paintEvent (QPaintEvent *event)
{   
 
    QFont f = font();
    const QPixmap *map = this->pixmap();
    QSize s = this->size();
    QPainter painter;
    painter.begin(this);
    f.setBold(true);
    painter.setFont(f);
    painter.drawPixmap(0, 0, s.width(), s.height(), *map);
    painter.drawText(5,s.height()-3,m_pLineEdit->text());
    painter.end();
    setFrameShape(QFrame::Box);
}
