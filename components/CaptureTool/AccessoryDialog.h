/*===========================================================================
    CaptureTool
    AccessoryDialog.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef _ACCESSORYDIALOG_H_
#define _ACCESSORYDIALOG_H_

#include <QDialog>

QT_BEGIN_NAMESPACE
class QListWidget;
class QStringList;
class QDialogButtonBox;
class QPushButtonBox;
QT_END_NAMESPACE


class AccessoryDialog : public QDialog
{
    Q_OBJECT

public:
                            AccessoryDialog (std::vector<QString>files, std::vector<QString> titles, QList<QListWidget *> *pListOfItems, QList<QStringList *> *map, QWidget *parent = 0);
                            ~AccessoryDialog();
public Q_SLOTS:
    void                    applychanges               (void);


private:

    void                        initButtons         (void);

    QList<QListWidget *>        *m_pItemList;

    QList<QStringList *>        *m_pMap;

    QPushButton                 *m_pCancelButton;
    QPushButton                 *m_pCloseButton;
    QPushButton                 *m_pApplyButton;
    QDialogButtonBox            *m_pButtonBox;

};

#endif _ACCESSORYDIALOG_H_