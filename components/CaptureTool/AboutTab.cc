/*===========================================================================
    CaptureTool
    AboutTab.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/






#include <QtGui>
#include <assert.h>
#include <utility>
#include "globals.h"
#include "AboutTab.h"
#include "RDFReader.h"
#include "utilities.h"
#include "StringDefs.h"



AboutTab::AboutTab(StringDefs *pStringDefs, const QFileInfo &fileInfo, QWidget *parent)
    : QWidget(parent),
    m_pStringDefs           (pStringDefs)
{
    m_pAbout = new QLabel(tr("<b>CaptureTool 1.0\nCopyright (C) Corey Toler-Franklin, 2011.\n All rights reserved.\ncontact: corey.tolerfranklin@gmail.com</b>"
           "<a href=\"http://www.coreytoler.com/\"></a>"));

    QVBoxLayout *mainLayout = new QVBoxLayout;
    mainLayout->addWidget(m_pAbout);
    setLayout(mainLayout);
}


AboutTab::~AboutTab(void)
{
}


void AboutTab::exportToRDF(QString path)
{
    // legacy
    RDFReader rdfr(m_pStringDefs);

    QString outfile(path + "/" + "metadata");
    makeDirectory(outfile);
    outfile = outfile + "/" + "License";
    makeDirectory(outfile);
    outfile = outfile + "/" + "License.rdf";

    QString data = rdfr.license(m_pAbout->text());
    saveTextToFile(outfile, data);

    //swap soprano here
}