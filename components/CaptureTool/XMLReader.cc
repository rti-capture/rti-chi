/*===========================================================================
    CaptureTool
    XMLReader.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include <QDir>
#include <assert.h>
#include <QTextStream>
#include "XMLReader.h"
#include "utilities.h"
#include "StringDefs.h"




XMLReader::XMLReader (StringDefs *pStringDefs)
    : m_pStringDefs         (pStringDefs)
{
}


XMLReader::~XMLReader (void)
{
}


bool XMLReader::load(QDomElement &node, QDomDocument &doc, QString fname, QString type, QString version)
{
    QFile file(fname); 
    if(!file.exists())
        return(false);

    if (file.open(QIODevice::ReadOnly)) {
        bool status = doc.setContent(&file);
        file.close();
        if (status) {
            node = doc.documentElement();
            return(true);
        } 
        else {
            qDebug("couldn't read from `%s'", fname.toUtf8().data());
        } 
    }
    else
        qDebug("couldn't open `%s'", fname.toUtf8().data());

    return(false);

}


bool XMLReader::read(QString path, QDomElement &root, QDomElement &node, QString type, QString version, QString name)
{

    // verify valid version and file info
    if (root.tagName() == type.toLocal8Bit().data()) {
        if (root.hasAttribute("version")) {
            if (isCompatible(root.attribute("version"), version.toLocal8Bit().data())) {
                node = root.firstChildElement(name);
                return(true);
            }
            else {
                qDebug("incompatible file version: `%s' instead of `%s'; bailing out...", root.attribute("version").toUtf8().data(), version.toLocal8Bit().data());
                return(false);
            }
        }
        else {
            qDebug("unknown file version -- add version attribute; bailing out...");
            return(false);
        }
    }
    else {
        qDebug("wrong root tag: `%s' instead of `%s'", root.tagName().toUtf8().data(), type.toLocal8Bit().data());
        return(false);
    }

    // made it
    return(false);
}


void XMLReader::update(QString fname, QString nodename, QString attribute, QString data, QString type, QString version)
{
    QFile f(fname);
    if(!f.exists())
        return;

    QDomElement root;
    QDomDocument doc;

    bool stat = load(root, doc, fname, type, version);
    if(!stat)
        return;

    if (root.firstChildElement(nodename).hasAttribute(attribute)) {
        root.firstChildElement(nodename).setAttribute(attribute, data);
    }

    // write
    f.remove();
    f.setFileName(fname);
    if(f.open(QIODevice::WriteOnly) ) {
        QTextStream TextStream(&f);
        TextStream << doc.toString() ;
        f.close();
    }
}


QString XMLReader::getData(QString fname, QString nodename, QString attribute, QString type, QString version)
{
    QString data;

    QFile f(fname);
    if(!f.exists())
        return (data);

    QDomElement root;
    QDomDocument doc;

    bool stat = load(root, doc, fname, type, version);
    if(!stat)
        return (data);

    if (root.firstChildElement(nodename).hasAttribute(attribute)) {
        data = root.firstChildElement(nodename).attribute(attribute);
    }

    return(data);
}



bool XMLReader::hasNode(QString fname, QString nodename, QString type, QString version)
{
    QString data;

    QFile f(fname);
    if(!f.exists())
        return (false);

    QDomElement root;
    QDomDocument doc;

    bool stat = load(root, doc, fname, type, version);
    if(!stat)
        return (false);

    if (!(root.firstChildElement(nodename).isNull())) {
        return(true);
    }

    return(false);
}