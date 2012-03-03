/*===========================================================================
    CaptureTool
    NotesTab.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include <QtGui>
#include <assert.h>
#include <utility>
#include <QTextEdit>
#include "globals.h"
#include "NotesTab.h"



//------------------------------------------------------------
// Project notes
//------------------------------------------------------------
NotesTab::NotesTab(const QFileInfo &fileInfo, QWidget *parent)
    : QWidget(parent)
{
    m_pTextEditor = new QTextEdit;
    m_pTextEditor->clear();

    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(m_pTextEditor);
    mainLayout->addStretch(1);
    setLayout(mainLayout);
}


// extract the text as an xml block
QString NotesTab::exportToXML(void)
{
    QString fullstring, finaltext;
    fullstring = "<notes   text=\"%s\" /> \n";
    finaltext.sprintf(fullstring.toLocal8Bit().data(), getText().toLocal8Bit().data());
    return(finaltext);
}


// extract the text as an xml block
void NotesTab::exportToRDF(QString path)
{
}


void NotesTab::import(QDomElement root)
{
    QString str;
    QDomElement node = root;

    // parse the  information and store the data
    for (; !node.isNull(); node=node.nextSiblingElement("notes")) {

        if (node.hasAttribute("text")) {
            str = node.attribute("text");
            m_pTextEditor->setText(str);
        }
    }
}


void NotesTab::clear(void)
{
    m_pTextEditor->clear();
}


QString NotesTab::getText(void)
{
    QString fullstring;
    QTextDocument *pDoc = m_pTextEditor->document();

   for (QTextBlock it = pDoc->begin(); it != pDoc->end(); it = it.next()) {
        fullstring = fullstring + it.text();
    }

    return(fullstring);
}

