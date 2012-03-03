/*===========================================================================
    CaptureTool
    IconWindow.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/



#ifndef IconWindow_H
#define IconWindow_H

#include <QIcon>
#include <QList>
#include <QMainWindow>
#include <QPixmap>


QT_BEGIN_NAMESPACE
class QAction;
class QActionGroup;
class QGroupBox;
class QMenu;
class QRadioButton;
class QTableWidget;
class QTableWidgetItem;
class IconSelectionArea;
class QWidget;
class QDialogButtonBox;
class QPushButton;
QT_END_NAMESPACE

class IconSelectionRect;
class IconSizeSpinBox;
class StringDefs;


class IconWindow : public QMainWindow
{
    Q_OBJECT

public:
                        IconWindow                              (StringDefs *pStringDefs, QString category, QString equipmentroot, QString projectroot, QWidget *parent = 0);

    void                createNewItem                           (void);
    void                removeItem                              (void);

    void                getNames                                (QString path, QString category, StringDefs *pStringDefs);

    void                iconSelectionWindow                     (QString equipmentroot, QString title, QString category);
    QString             selectedIconName                        (void);
    void                setCurTitle                             (QString title);

    IconSelectionRect*  preview                                 (void);

    void                updateSelectButton                      (bool stat);
    QString             equipmentRoot                           (void);
    void                setSelectionRoot                        (QString root);
    void                setSelectionCategory                    (QString c);



private slots:
    void changeSize(bool checked = true);
    void changeIcon();
    void selectIconStyle (void);

private:

    StringDefs      *m_pStringDefs;
    QString         m_fname;
    QStringList     m_names;
    QString         m_category;
    QString         m_curTitle;
    QString         m_selectionroot;
    QString         m_selectioncategory;

    QString m_newIconFile;

    IconSelectionRect *m_pPreviewArea;
    IconSelectionRect *m_pIconSelectionWindow;
    QWidget *centralWidget;

    QGroupBox *previewGroupBox;

    QGroupBox *imagesGroupBox;
    QTableWidget *m_pTableWidget;


    QPushButton                 *m_pCancelButton;
    QPushButton                 *m_pCloseButton;
    QPushButton                 *m_pApplyButton;
    QDialogButtonBox            *m_pButtonBox;



    void                                        populate                   (QStringList fileNames);

    void                                        initLayout                  (void);
    void                                        createMenus                 (void);
    void                                        createContextMenu           (void);
    void                                        checkCurrentStyle           (void);

    void                                        initButtons                 (void);
   

};


inline IconSelectionRect *IconWindow::preview(void)
{
    return(m_pPreviewArea);
}

#endif
