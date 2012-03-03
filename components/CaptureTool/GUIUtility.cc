/*===========================================================================
    CaptureTool
    GUIUtility.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#include <QtGui>
#include <assert.h>
#include "globals.h"
#include "GUIUtility.h"
//#include "treemodel.h"
#include "utilities.h"


GUIUtility::GUIUtility(void)
{
}


GUIUtility::~GUIUtility(void)
{
}

void GUIUtility::updateTreeItem3Cols(bool bAddSiblings, QTreeWidgetItem *pTreeItem, QTreeWidget *pTreeWidget, std::vector<QString> *items, std::vector<QString> *selections, std::multimap<int, QString> *optionalDescription)
{
    assert(((int)items->size()) == ((int) selections->size()));

    for(int i = 0; i < (int)items->size(); ++i) {

        pTreeItem->setText(0, (*items)[i].toLocal8Bit().data());
        pTreeItem->setTextAlignment(0, Qt::AlignLeft);
        pTreeItem->setText(1, (*selections)[i].toLocal8Bit().data());
        pTreeItem->setTextAlignment(1, Qt::AlignLeft);

        if(optionalDescription->find(i) != optionalDescription->end()) {
            pTreeItem->setText(2, optionalDescription->find(i)->second.toLocal8Bit().data());
            pTreeItem->setTextAlignment(2, Qt::AlignLeft);
            fprintf(stderr, "%s\n", optionalDescription->find(i)->second.toLocal8Bit().data());
        }

        if(bAddSiblings)
            pTreeItem = new QTreeWidgetItem(pTreeWidget);
        else
            pTreeItem = new QTreeWidgetItem(pTreeItem);
    }
}


void GUIUtility::updateTreeItemTree3Cols(QTreeWidgetItem *pTreeItem, QString name, QListWidget *list)
{
    QTreeWidgetItem *pSubTree = new QTreeWidgetItem(pTreeItem);
    pSubTree->setText(1, name.toLocal8Bit().data());
    pSubTree->setTextAlignment(1, Qt::AlignLeft);

    for(int i = 0; i < (int)list->count(); ++i) {
        QListWidgetItem *entry = list->item(i);

        if(entry->checkState() == Qt::Checked) {
            QTreeWidgetItem *newentry = new QTreeWidgetItem();
            newentry->setText(1, entry->text());
            pSubTree->addChild(newentry);
            if(APPDEBUG)
                fprintf(stderr, "\n%s\n", entry->text().toLocal8Bit().data());
        }
    }
}


QStringList GUIUtility::getComboBoxItems(QComboBox *pComboBox)
{
    QStringList list;

    for(int i = 0; i < pComboBox->count(); ++ i) {
        list.append(pComboBox->itemText(i));
    }

    // return the list
    return(list);
}


void GUIUtility::initComboBox(QComboBox *item, QString fileNameFullPath)
{
    item->clear();

    std::vector<QString> list;
    list = readLinesFromFile(fileNameFullPath);

    for(int i = 0; i < (int)list.size(); ++ i) {
         item->addItem(list[i]);
    }
}


void GUIUtility::setComboBox(QComboBox *item, QString entry)
{
    if(entry.isNull() || entry.isEmpty())
        return;

    int index = item->findText(entry);
    if(index == -1) {
        item->addItem(entry);
        item->setCurrentIndex(item->findText(entry));
    }
    else {
        item->setCurrentIndex(index);
    }
}





QComboBox *GUIUtility::comboBox(const QString &label)
{
    QComboBox *comboBox = new QComboBox;
    comboBox->setEditable(true);
    comboBox->addItem(label);
    comboBox->setSizePolicy(QSizePolicy::Expanding, QSizePolicy::Preferred);
    return comboBox;
}

void GUIUtility::browseCurrentDirectory(QWidget *item)
{
    QLabel *directoryLabel = new QLabel;

    QFileDialog::Options options = QFileDialog::DontResolveSymlinks | QFileDialog::ShowDirsOnly;

    QString directory = QFileDialog::getExistingDirectory(item,
                                "QFileDialog::getExistingDirectory()",
                                directoryLabel->text(),
                                options);
    if (!directory.isEmpty())
        directoryLabel->setText(directory);
}