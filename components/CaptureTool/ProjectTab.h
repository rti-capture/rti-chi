/*===========================================================================
    CaptureTool
    ProjectTab.h
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/


#ifndef _PROJECTTAB_H_
#define _PROJECTTAB_H_



#include <QDialog>
#include <qdom.h> 
#include <QReadWriteLock>
#include <QDir>


QT_BEGIN_NAMESPACE
class QFileInfo;
class QLineEdit;
QT_END_NAMESPACE


class StringDefs;


class ProjectTab : public QWidget
{
    Q_OBJECT

public:

                                        ProjectTab                          (StringDefs *pStringDefs, const QFileInfo &fileInfo, QWidget *parent = 0);

    QString                             exportToXML                         (void);
    void                                exportToRDF                         (QString path);

    void                                open                                (QString filename);
    void                                create                              (void);

    bool                                promptOnSave                        (void);
    void                                resetSavedFlag                      (void);
    bool                                savedFlag                           (void);
    void                                setSavedFlag                        (bool flag);
    void                                updateSavedSettings                 (QString fname);

    QString                             enddatetime                         (void);
    QString                             startdatetime                       (void);
    QString                             leadfirstname                       (void);
    QString                             leadlastname                        (void);
    QString                             leadlegalbody                       (void);
;

    QString                             projectFile                         (void);

    void                                leadToRDF                           (QString path);
    void                                timeToRDF                           (QString path);
    void                                subjectToRDF                        (QString path);
    void                                teamToRDF                           (QString path);
    void                                collectionToRDF                     (QString path);
    void                                permissionsToRDF                    (QString path);
    void                                locationToRDF                       (QString path);
    void                                contactToRDF                        (QString path);

    void                                import                              (QDomElement root);

private:


    StringDefs                          *m_pStringDefs;

    bool                                m_bSavedFlag;

    QString                             m_subject;
    QString                             m_location;
    QString                             m_collection;
    QString                             m_catalog;
    QString                             m_rights;
    QString                             m_room;
    QString                             m_lead;
    QString                             m_team;
    QString                             m_phone;
    QString                             m_email;
    QString                             m_address;
    QString                             m_date;


    QLineEdit                           *m_pEditDate;
    QLineEdit                           *m_pEditSubject;
    QLineEdit                           *m_pEditLocation;
    QLineEdit                           *m_pEditCollection;
    QLineEdit                           *m_pEditCatalog;
    QLineEdit                           *m_pEditRoom;
    QLineEdit                           *m_pEditRights;
    QLineEdit                           *m_pEditEmail;
    QLineEdit                           *m_pEditLead;
    QLineEdit                           *m_pEditTeam;
    QLineEdit                           *m_pEditPhone;
    QLineEdit                           *m_pEditAddress;

    QString                             m_projectFile;

    void                                initLabels                          (void);

    void                                updateData                          (void);
    void                                clearData                           (void);

};

#endif
