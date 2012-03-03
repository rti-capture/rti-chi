/*===========================================================================
    CaptureTool
    PixelLabel.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef PIXELLABEL_H
#define PIXELLABEL_H

#include <QLabel>
#include <QIcon>
#include <QImage>

QT_BEGIN_NAMESPACE
class QLineEdit;
QT_END_NAMESPACE

class StringDefs;

class PixelLabel : public QLabel
{
    Q_OBJECT

public:

                    PixelLabel                      (StringDefs *pStringDefs, int mode, QWidget *parent = 0);
                    ~PixelLabel                     (void);

    void            deselect                        (void);
    void            select                          (QIcon icon);
    void            select                          (void);

    void            setTitle                        (QString title);
    QString         title                           (void);
    void            setTitleSize                    (int w, int h);

    QLineEdit       *lineEdit                       (void);

    void            setContextPL                    (PixelLabel *pl);

    QImage          image                           (void);
    void            setImage                        (QImage image);

    void            initData                        (QString name, int i, bool bInit);
    bool            hasData                         (QString fname);

    QString         iconFileName                    (void);
    void            setIcconFileName                (QString name, bool bUpdate);

protected:

    void            mousePressEvent                 (QMouseEvent *event);

    void            paintEvent                      (QPaintEvent *event);


private:

    bool            m_bIsSelected;

    QLineEdit       *m_pLineEdit;

    int             m_mode;

    QImage          m_image;

    QString         m_filename;

    StringDefs      *m_pStringDefs;


};


inline QString PixelLabel::iconFileName(void)
{
    return(m_filename);
}

inline void PixelLabel::setIcconFileName(QString name, bool bUpdate)
{
    m_filename = name;
    if(bUpdate) {
        select();
    }
}



inline void PixelLabel::setImage(QImage image)
{
    m_image = image;
}


inline QImage PixelLabel::image(void)
{
    return(m_image);
}

#endif
