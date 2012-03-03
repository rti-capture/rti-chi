/*===========================================================================
    CaptureTool
    RDFReader.cc
    Copyright (C) Corey Toler-Franklin, 2011. All rights reserved.
    contact: corey.tolerfranklin@gmail.com
===========================================================================*/




#include "RDFReader.h"
#include "StringDefs.h"

#if 0
#include <Soprano/Soprano>
#include <QDebug>
#endif



RDFReader::RDFReader(StringDefs *pStringDefs) 
    : m_pStringDefs (pStringDefs)
{


#if 0
    // soprano test
    Soprano::Model* model = Soprano::createModel();
    model->addStatement( QUrl( "http://www.coreytoler.com"),
                          Soprano::Vocabulary::RDFS::label(),
                          Soprano::LiteralValue( "A test resource" ) );
     Soprano::StatementIterator it = model->listStatements();
     while( it.next() )
         qDebug() << *it;
#endif
}


RDFReader::~RDFReader(void)
{
}


QString RDFReader::header(void)
{
    // legacy write

    QString str;
    str = "<rdf:RDF xml:lang=\"en\"";
    str = str + "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"";
    str = str + "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"";
    str = str + "xmlns:rdfs=\"xmlns:crm=\"http://www.ics.forth.gr/is/rdfs/3D-COFORM_CIDOC-CRM.rdfs#\"";
    str = str + "xmlns:crmdig=\"http://www.ics.forth.gr/isl/rdfs/3D-COFORM_CRMdig.rdfs#\">\n";

    // swap in saprano here
#if 0 
    // todo fill in saprano models
    model->addStatement(Soprano::Statement( QUrl( "http://www.w3.org/1999/02/22-rdf-syntax-ns#\""), Soprano::Vocabulary::RDFS::label(), Soprano::LiteraValue( "en" ) ) );
#endif

    return(str);
}


QString RDFReader::software(void)
{
    QString str;

    // legacy write
    str = "<crmdig:D14.Software rdf:about=\"http://www.coreytoler.com/dig/Software/CaptureTool\">\n";
    str = str + "<crmdig:L4F.has_preferred_label>CaptureTool</crmdig:L4F.has_preferred_label>\n";
    str = str + "<crm:P2F.has_type>\n";
    str = str + "<crm:E55.Type rdf:about=\"http://www.coreytoler.com/dig/Software/Type/processing\"/>\n";
    str = str + "</crm:P2F.has_type>\n";
    str = str + "<crm:P2F.has_type>\n";
    str = str + "<crm:E55.Type rdf:about=\"http://www.coreytoler.com/dig/version/version_1.0\"/>\n";
    str = str + "</crm:P2F.has_type>\n";
    str = str + "<crmdig:L33F.has_maker>\n";
    str = str + "<crm:E39.Actor rdf:about=\"http://www.coreytoler.com/dig\">\n";
    str = str + "<crmdig:L4F.has_preferred_label>CaptureTool</crmdig:L4F.has_preferred_label>\n";
    str = str + "</crm:E39.Actor>\n";
    str = str + "</crmdig:L33F.has_maker>\n";
    str = str + "</crmdig:D14.Software>\n";


    // swap in saprano here

    return(str);
}


QString RDFReader::time(QString startdatetime, QString enddatetime)
{
    // legacy write

    QString str;

    str = "<crmdig:L31F.has_starting_date-time rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">\n";
    str = str + "%s\n";
    str.sprintf(startdatetime.toLocal8Bit().data());
    str = str + "</crmdig:L31F.has_starting_date-time>\n";
    str = str + "<crmdig:L32F.has_ending_date-time rdf:datatype=\"http://www.w3.org/2001/XMLSchema#dateTime\">\n";
    str = str + "%s\n";
    str.sprintf(enddatetime.toLocal8Bit().data());
    str = str + "</crmdig:L32F.has_ending_date-time>";

    // swap in saprano here

    return(str);

}


QString RDFReader::room(QString data)
{
    // legacy write

    QString str;

    // legacy write
    str = "<crm:P7F.took_place_at>\n";
    str = str + "<crmdig:D23.Room rdf:about=\"%s\"/>\n";
    str = str.sprintf(str.toLocal8Bit().data(), data.toLocal8Bit().data());
    str = str + "</crm:P7F.took_place_at>\n";

    // swap in saprano here

    return(str);
}


QString RDFReader::person(QString uid, QString firstname, QString lastname, QString legalbody)
{
 
    QString str;

    // legacy write
    str = "<crm:E21.Person rdf:about=\"%s\">\n";
    str.sprintf(str.toLocal8Bit().data(), uid.toLocal8Bit().data());
    str = str + "<crm:P131F.is_identified_by>\n";
    str = str + "<crmdig:D21.Person_Name>\n";
    str = str + "<crmdig:L51F.has_first_name>%s</crmdig:L51F.has_first_name>\n";
    str.sprintf(str.toLocal8Bit().data(), firstname.toLocal8Bit().data());
    str = str + "<crmdig:L52F.has_last_name>%s</crmdig:L52F.has_last_name>\n";
    str.sprintf(str.toLocal8Bit().data(), lastname.toLocal8Bit().data());
    str = str + "</crmdig:D21.Person_Name>\n";
    str = str + "</crm:P131F.is_identified_by>\n";
    str = str + "<crm:P107B.is_current_or_former_member_of>\n";
    str = str + "<crm:E40.Legal_Body rdf:about=\"%s\"/>\n";
    str.sprintf(str.toLocal8Bit().data(), legalbody.toLocal8Bit().data());
    str = str + "</crm:P107B.is_current_or_former_member_of>\n";
    str = str + "</crm:E21.Person>\n";

    // swap in saprano here

    return(str);
}


QString RDFReader::device(QString url_main, int serialnumber, QString preferedlabel, QString url_type, QString url_model, QString url_maker, QString note)
{


    QString str;

    // legacy write
    str = "<crmdig:L12F.happened_on_device>\n";
    str = str + "<crmdig:D8.Digital_Device rdf:about=\"%s\">\n";
    str.sprintf(str.toLocal8Bit().data(), url_main.toLocal8Bit().data());
    str = str + "<crmdig:L59F.has_serial_number>\"%d\"</crmdig:L59F.has_serial_number>\n";
    str.sprintf(str.toLocal8Bit().data(), serialnumber);
    str = str + "<crmdig:L4F.has_preferred_label>%s</crmdig:L4F.has_preferred_label>\n";
    str.sprintf(str.toLocal8Bit().data(), preferedlabel.toLocal8Bit().data());
    str = str + "<crm:P2F.has_type>\n";
    str = str + "<crm:E55.Type rdf:about=\"%s\"/>\n";
    str.sprintf(str.toLocal8Bit().data(), url_type.toLocal8Bit().data());
    str = str + "</crm:P2F.has_type>\n";
    str = str + "<crm:P2F.has_type>\n";
    str = str + "<crm:E55.Type rdf:about=\"%s\"/>\n";
    str.sprintf(str.toLocal8Bit().data(), url_type.toLocal8Bit().data());
    str = str + "</crm:P2F.has_type>\n";
    str = str + "<crmdig:L33F.has_maker>\n";
    str = str + "<crm:E39.Actor rdf:about=\"%s\">\n";
    str.sprintf(str.toLocal8Bit().data(), url_maker.toLocal8Bit().data());
    str = str + "<crmdig:L4F.has_preferred_label>%s</crmdig:L4F.has_preferred_label>\n";
    str.sprintf(str.toLocal8Bit().data(), url_maker.toLocal8Bit().data());
    str = str + "</crm:E39.Actor>\n";
    str = str + "</crmdig:L33F.has_maker>\n";
    str = str + "<crm:P3F.has_note>%s</crm:P3F.has_note>\n";
    str.sprintf(str.toLocal8Bit().data(), note.toLocal8Bit().data());
    str = str + "</crmdig:D8.Digital_Device>\n";
    str = str + "</crmdig:L12F.happened_on_device>\n";


    // swap in saprano here

    return(str);
}


QString RDFReader::license (QString data)
{

    QString str;

    // legacy write
    str = "<crm:P3F.has_note>\n%s\n</crm:P3F.has_note>\n";
    str.sprintf(str.toLocal8Bit().data(), data.toLocal8Bit().data());

    // swap in saprano here

    return(str);
}


QString RDFReader::subject(QString uid, QString description, QString id, QString url_type)
{
    // legacy write

    QString str;

    // legacy write
    str = "<crm:E22.Man-Made_Object rdf:about=\"%s\">\n";
    str.sprintf(str.toLocal8Bit().data(), uid.toLocal8Bit().data());
    str = str + "<crmdig:L4F.has_preferred_label>%s\n";
    str.sprintf(str.toLocal8Bit().data(), description.toLocal8Bit().data());
    str = str + "</crmdig:L4F.has_preferred_label>\n";
    str = str + "<crmdig:L55F.has_inventory_no>%s</crmdig:L55F.has_inventory_no>\n";
    str.sprintf(str.toLocal8Bit().data(), id.toLocal8Bit().data());
    str = str +"<crm:P2F.has_type>\n";
    str = str +"<crm:E55.Type rdf:about=\"%s\"/>\n";
    str.sprintf(str.toLocal8Bit().data(), url_type.toLocal8Bit().data());
    str = str +"</crm:P2F.has_type>\n";
    str = str +"</crm:E22.Man-Made_Object>\n";


    // swap in saprano here

    return(str);
}






