/*===========================================================================
    CaptureTool
    StringDefs.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/





#ifndef STRINGDEFS_H
#define STRINGDEFS_H

#include <QDir>

class StringDefs 
{

public:
                    StringDefs                      (void);
                    ~StringDefs                     (void);

    QString         untitled                        (void);

    QString         png                             (void);
    QString         xml                             (void);


    QString         os                              (void);
    QString         icon                            (void);
    QString         icons                           (void);
    QString         templates                       (void);
    QString         path                            (void);
    QString         metadata                        (void);

    QString         appdataPath                     (void);
    QString         iconPath                        (void);
    QString         defaultIcon                     (void);

    QString         getDataFileName                 (QString path, QString filename, QString local, QString category);
    QString         getDataPath                     (QString path, QString local);


    QString         licenseInfo                     (void);

    void            setiswap                        (QString n);
    QString         iswap                           (void);


private:
    QString         m_iswap;
};

inline QString StringDefs::untitled (void)
{
    return(QString("untitled"));
}

inline QString StringDefs::png (void)
{
    return(QString("png"));
}

inline QString StringDefs::xml (void)
{
    return(QString("xml"));
}

inline QString StringDefs::os (void)
{
    return(QString("win"));
}

inline QString StringDefs::icons (void)
{
    return(QString("icons"));
}

inline QString StringDefs::icon (void)
{
    return(QString("icon"));
}

inline QString StringDefs::templates (void)
{
    return(QString("templates"));
}

inline QString StringDefs::path (void)
{
    return(QString("path"));
}

inline void StringDefs::setiswap(QString n)
{
    m_iswap = n;
}

inline QString StringDefs::iswap(void)
{
   return(m_iswap);
}

inline QString StringDefs::metadata(void)
{
   return("metadata");
}


#endif // STRINGDEFS_H

