/*===========================================================================
    CaptureTool
    IconSelectionRect.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef ICONSELECTIONRECT_H
#define ICONSELECTIONRECT_H


#include <QIcon>
#include <QWidget>


QT_BEGIN_NAMESPACE
class PixelLabel;
class QLabel;
QT_END_NAMESPACE

class StringDefs;



class IconSelectionRect : public QWidget
{
    Q_OBJECT

public:
                                IconSelectionRect                   (StringDefs *pStringDefs, QStringList list, QString equipmentroot, QString projectroot, QString category, QWidget *parent = 0);

    void                        setIcon                             (const QIcon &icon);
    QIcon                       getIcon                             (void);
    
    QString                     selectedIconName                    (void);

    void                        deselectAll                         (void);

    void                        setSize                             (const QSize &size);

    void                        createNewItem                       (void);
    void                        removeItem                          (void);

    void                        setContextPL                        (PixelLabel *pl);

    void                        updatePixmapLabels                  (QIcon::Mode mode);

    PixelLabel                  *getContext                         (void);

    QString                     equipmentRoot                       (void);

protected:
        void mousePressEvent(QMouseEvent*);

private slots:

    void                        rename                              (void);
    void                        edit                                (void);
    void                        deleteItem                          (void);
    void                        viewDetails                         (void);
    void                        changeIcon                          (void);

private:

    QIcon icon;
    QSize size;

    int                 lw;
    int                 lh;

    QList<PixelLabel *> pixmapLabels;
    PixelLabel *m_pContext;

    StringDefs          *m_pStringDefs;
    QStringList         m_list;
    QString             m_name;
    QString             m_equipmentroot;
    QString             m_projectroot;
    QString             m_category;

    int                 m_next;

    QAction *editAction;
    QAction *renameAction;
    QAction *deleteAction;
    QAction *viewDetailsAction;
    QAction *changeIconAction;

    void                        setUpLayout                         (void);
    void                        updateLayout                        (void);

    QLabel                      *createHeaderLabel                  (const QString &text);
    PixelLabel                  *createPixmapLabel                  (void);

    void                        updatePixmapLabels                  (void);

    void                        contextMenuEvent                    (QContextMenuEvent *event);
    void                        createActions                       (void);
};

inline PixelLabel *IconSelectionRect::getContext (void)
{
    return(m_pContext);
}

inline QString IconSelectionRect::equipmentRoot (void)
{
    return(m_equipmentroot);
}



#endif

