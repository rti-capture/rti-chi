/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#include "universalrti.h"
#include "ptm.h"
#include "hsh.h"

#include <QTime>

UniversalRti::UniversalRti() :
	Rti()
{
	image = NULL;
}


UniversalRti::~UniversalRti()
{
	if (image)
		delete image;
}


int UniversalRti::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int UniversalRti::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading URTI...");
	filename = name;

#ifdef WIN32
  #ifndef __MINGW32__
	FILE* file;
	if (fopen_s(&file, filename.toStdString().c_str(), "rb") != 0)
		return -1;
  #else
	FILE* file = fopen(filename.toStdString().c_str(), "rb");
	if (file == NULL)
		return -1;
  #endif
#else
	FILE* file = fopen(filename.toStdString().c_str(), "rb");
	if (file == NULL)
		return -1;
#endif

	unsigned char c;

	//parse comments		
	c = fgetc(file);
	if (feof(file))
		return -1;
	while(c=='#')		
	{
		while (c != '\n')
		{
			c = fgetc(file);
			if (feof(file))
				return -1;
		}
		c = fgetc(file);
	}
	if (feof(file))
		return -1;
	//rewind one character
	fseek(file, -1, SEEK_CUR);

	bool eof, error;
	QString str = getLine(file, &eof);
	if (eof) return -1;
	int rtiType = str.toInt(&error);
	if (!error) return -1;
	

	//Gets width, height, number of color
	str = getLine(file, &eof);
	if (eof) return -1;
	QStringList list = str.split(' ',  QString::SkipEmptyParts);
	if (list.size() != 3)
		return -1;
	w = list[0].toInt(&error);
	if (!error) return -1;
	h = list[1].toInt(&error);
	if (!error) return -1;
	int ncolor = list[2].toInt(&error);
	if (!error) return -1;

	//Gets number of basis term, basis type, element size.
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ',  QString::SkipEmptyParts);
	if (list.size() != 3)
		return -1;
	int basisTerm = list[0].toInt(&error);
	if (!error) return -1;
	int basisType = list[1].toInt(&error);
	if (!error) return -1;
	int elemSize = list[2].toInt(&error);
	if (!error) return -1;

	switch(rtiType)
	{
		case 0: type = "URTI"; return -1; break;
		case 1: 
			type = "URTI PTM";
			if (basisType == 1)
				image = new LRGBPtm();
			else
				image = new RGBPtm();
			((Ptm*)image)->setVersion("PTM_1.2");
			return -1;
			break;
		case 2: type = "URTI SH"; return -1;break;
		case 3: 
			type = "URTI HSH";
			image = new Hsh();
			break;
		case 4: type = "URTI ADAPTIVE PTM"; return -1; break;
		default: type = "URTI"; return -1;
	}
	QString text = "Loading URTI...";
	image->loadData(file, w, h, basisTerm, true, cb, text);
	

	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("URTI Loading: %.5f s\n", diff);
#endif

	return 0;
}


int UniversalRti::loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb, QString& text)
{
	return 0;
}

int UniversalRti::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int UniversalRti::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int UniversalRti::loadCompressed(QString name)
{
	return loadCompressed(0,0,w,h,name);
}


int UniversalRti::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	
	return 0;
}


int UniversalRti::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int UniversalRti::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	return 0;
}


int UniversalRti::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
	image->createImage(buffer, width, height, light, rect, level, mode);
	return 0;
}


QImage* UniversalRti::createPreview(int width, int height)
{
	return image->createPreview(width, height);
}


int UniversalRti::allocateRemoteImage(int width, int height, int maxResLevel)
{
	
	return 0;
}


int UniversalRti::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	
	return 0;
}
