/*===========================================================================
    CaptureTool
    CustomTemplatesWindow.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef _CUSTOMTEMPLATESWINDOW_H
#define _CUSTOMTEMPLATESWINDOW_H

#include <QDialog>
#include <map>

QT_BEGIN_NAMESPACE
class QWidget;
class QTreeWidget;
class QTreeWidgetItem;
class QToolButton;
class QDialogButtonBox;
QT_END_NAMESPACE

class TemplateSetupWindow;
class IconWindow;
class StringDefs;


class DisplayItem : public QWidget
{
    Q_OBJECT

public:
    DisplayItem(int num, QWidget *parent = 0);
    ~DisplayItem();



    void createTemplateTable(void);

private:
    
void paintEvent(QPaintEvent *event);

};

class CustomTemplatesWindow : public QDialog
{
    Q_OBJECT

public:
                            CustomTemplatesWindow                   (StringDefs *pStringDefs, QString equipmentroot, QString projectroot, QWidget *parent = 0);
                            ~CustomTemplatesWindow                  ();


        void                create                                  (QString equipmentroot, QString basename, QString category);
        void                setDisableNewTemplates                  (bool flag);
        void                selectNewIcon                           (QString selectionroot, QString title, QString category);

        TemplateSetupWindow *displayEditWindow                      (QString equipmentroot, QString projectroot, QString category, QString basename);



        QString             equipmentRoot                           (void);



public Q_SLOTS:
    void                    newTemplate                             (void);


private slots:
    void                    checkActiveItem                         (QTreeWidgetItem *current, QTreeWidgetItem *previous);


private:

    StringDefs                  *m_pStringDefs;

    QString                     m_equipmentroot;
    QString                     m_projectroot;
    QString                     m_basename;


    QToolButton                 *m_pNewTemplateButton;
    QDialogButtonBox            *m_pTemplateButtonBox;
    QTreeWidget                 *m_pTreeWidget; 

    void                        addTemplateList             (QTreeWidget *root, int position, QString title, int numEntries, QSize iconSize, QSize entrySize, QSize lsize);
    
    QToolButton                 *createToolButton           (const QString &toolTip, const QIcon &icon, const char *member);

    void                        initButtonMenu              (void);

    std::multimap<QString, IconWindow *> m_IList;

    IconWindow                  *m_pIconSelectionWindow;
    TemplateSetupWindow         *m_pTemplateSetupWindow;

};




#endif //_CUSTOMTEMPLATESWINDOW_H