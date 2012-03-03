/*===========================================================================
    CaptureTool
    CustomTemplateSetup.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef _CUSTOMTEMPLATESSETUP_H_
#define _CUSTOMTEMPLATESSETUP_H_


#include <QDialog>
#include <map> 
#include "SetUpTab.h"


QT_BEGIN_NAMESPACE
class QDialogButtonBox;
class QFileInfo;
class QTabWidget;
class QLabel;
class QComboBox;
class QListWidget;
class QListWidgetItem;
class QLineEdit;
class QTreeWidget;
class QCheckBox;
QT_END_NAMESPACE



class CustomTemplateSetup : public SetUpTab
{
    Q_OBJECT

public:

                                        CustomTemplateSetup                     (StringDefs *pStringDefs, const QFileInfo &fileInfo, QString equipmentroot, QWidget *parent = 0);
                                        ~CustomTemplateSetup                    (void);

    void                                save                                    (QString equipmentroot, QString category, QString basename);
    void                                load                                    (QString fname);

public Q_SLOTS:

protected:

    StringDefs          *m_pStringDefs;

    void                                writeSettingsFile                       (QString path, QString filename, QString username, QString notes);
    void                                import                                  (QDomElement root);


};


#endif //_CUSTOMTEMPLATESSETUP_H_