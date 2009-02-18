/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef URTI_H
#define URTI_H

#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

// Local headers
#include "rti.h"


//! URTI class
class UniversalRti : public Rti
{
// private data member
protected:
	
	Rti* image;

public:

	//! Constructor.
	UniversalRti();

	//! Deconstructor.
	virtual ~UniversalRti();


private:

	/*!
	  Reads from the file a line ended by char '\n'
	  \param file file pointer.
	  \param eof flag for the end of file.
	  \return returns the readed line.
	*/
	QString getLine(FILE* file, bool* eof)
	{
		char c;
		QString str = "";
		*eof = false;
		while(!feof(file) && fread(&c, sizeof(char), 1, file)!=0 && c!='\n')
			str.append(c);
		if (feof(file))
			*eof = true;
		return str;
	}

	
	
public:

	virtual int load(CallBackPos * cb = 0);
	virtual int load(QString name, CallBackPos * cb = 0);
	virtual int save(QString name);
	virtual int loadCompressed();
	virtual int loadCompressed(QString name);
	virtual int loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name);
	virtual int saveCompressed(QString name);
	virtual int saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name);
	virtual int createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level = 0, int mode = 0);
	virtual QImage* createPreview(int width, int height);
	virtual int allocateRemoteImage(int width, int height, int maxResLevel);  
	virtual int loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level);
	virtual int loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb = 0, QString& text = QString());

public:

	virtual void setRenderingMode(int a) {image->setRenderingMode(a);}

	virtual int getCurrentRendering() {return image->getCurrentRendering();}

	virtual QVector<RenderingMode*>* getSupportedRendering() {return image->getSupportedRendering();}
};

#endif //URTI_H
