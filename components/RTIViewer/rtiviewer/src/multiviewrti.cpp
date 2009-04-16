/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#include "multiviewrti.h"

#include <QTime>
#include <QFileInfo>



MultiviewRti::MultiviewRti(): Rti()
{
	currentRendering = NORMAL_MULTIVIEW;
	// Create list of supported rendering mode.
	list = new QVector<RenderingMode*>();
	list->append(new DefaultRenderingMRti());

}

MultiviewRti::~MultiviewRti()
{

}


int MultiviewRti::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int MultiviewRti::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading Multiview RTI...");
	filename = name;

	type = "Multiview RTI";

	QFile data(filename);
	if (!data.open(QFile::ReadOnly))
		return -1;

	bool error;
	QTextStream stream(&data);
	QString line = "";
	do 
	{
		line = stream.readLine();
	} while (line.startsWith("#"));
	QStringList strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 3)
		return -1;
	maxViewX = strList.at(0).toInt(&error);
	if (!error) return -1;
	maxViewY = strList.at(1).toInt(&error);
	if (!error) return -1;
	nViewpoint = strList.at(2).toInt(&error);
	if (!error) return -1;

	line = stream.readLine();
	strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 2)
		return -1;
	startX = strList.at(0).toInt(&error);
	if (!error) return -1;
	startY = strList.at(1).toInt(&error);
	if (!error) return -1;
	
	line = stream.readLine();
	strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 4)
		return -1;
	int temp = strList.at(0).toInt(&error);
	if (!error) return -1;
	useFlow = (temp == 1);
	separationX = strList.at(2).toInt(&error);
	if (!error) return -1;
	separationY = strList.at(3).toInt(&error);
	if (!error) return -1;

	QFileInfo info(filename);
	images = std::vector<UniversalRti>(nViewpoint);
	for (int i = 0; i < nViewpoint; i++)
	{
		if (cb != NULL)(*cb)(i * 60 / nViewpoint, "Loading RTI file...");
		line = stream.readLine();
		strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
		QFile image(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(1)));
		if (!image.exists()) return -1;
		images.push_back(UniversalRti());
		images[i].setFileName(image.fileName());
		images[i].load();
	}
	
	w = images[0].width();
	h = images[0].height();






//#ifdef WIN32
//  #ifndef __MINGW32__
//	FILE* file;
//	if (fopen_s(&file, filename.toStdString().c_str(), "rb") != 0)
//		return -1;
//  #else
//	FILE* file = fopen(filename.toStdString().c_str(), "rb");
//	if (file == NULL)
//		return -1;
//  #endif
//#else
//	FILE* file = fopen(filename.toStdString().c_str(), "rb");
//	if (file == NULL)
//		return -1;
//#endif
//
//	unsigned char c;
//
//	

	

	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("HSH Loading: %.5f s\n", diff);
#endif

	return 0;
}


int MultiviewRti::loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb, QString& text)
{
	return 0;
}


int MultiviewRti::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int MultiviewRti::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int MultiviewRti::loadCompressed(QString name)
{
	return loadCompressed(0,0,w,h,name);
}


int MultiviewRti::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	
	return 0;
}


int MultiviewRti::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int MultiviewRti::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	return 0;
}


int MultiviewRti::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("Default rendering: %.5f s\n", diff);
	
#endif

	return 0;
}


QImage* MultiviewRti::createPreview(int width, int height)
{
	
	return NULL;
}


int MultiviewRti::allocateRemoteImage(int width, int height, int maxResLevel)
{
	
	return 0;
}


int MultiviewRti::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	
	return 0;
}
