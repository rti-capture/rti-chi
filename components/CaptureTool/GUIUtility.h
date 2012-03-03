/*===========================================================================
    CaptureTool
    GUIUtility.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/

#pragma once


#include<vector>

QT_BEGIN_NAMESPACE
class QTreeWidgetItem;
class QComboBox;
QT_END_NAMESPACE

class GUIUtility 
{

public:

                                        GUIUtility                              (void);
                                        ~GUIUtility                             (void);

    void                                updateTreeItem3Cols                     (bool bAddSiblings, QTreeWidgetItem *pTreeItem, QTreeWidget *pTreeWidget, std::vector<QString> *items, std::vector<QString> *selections, std::multimap<int, QString> *optionalDescription);
    void                                updateTreeItemTree3Cols                 (QTreeWidgetItem *pTreeItem, QString name, QListWidget *list);

    QStringList                         getComboBoxItems                        (QComboBox *pComboBox);

    void                                initComboBox                            (QComboBox *item, QString fileNameFullPath);
    void                                setComboBox                             (QComboBox *item, QString entry);
    QComboBox                           *comboBox                               (const QString &label);



    void                                browseCurrentDirectory                  (QWidget *item);


};


