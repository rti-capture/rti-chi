/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#include "hsh.h"

#include <QTime>

Hsh::Hsh() :
	Rti()
{
	currentRendering = NORMAL_HSH;
	// Create list of supported rendering mode.
	list = new QVector<RenderingMode*>();
	list->append(new DefaultRenderingHsh());
}


Hsh::~Hsh()
{

}


int Hsh::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int Hsh::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading HSH...");
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

	type = "HSH";

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

	//read width
	fread(&w, sizeof(int), 1, file);
	//read height
	fread(&h, sizeof(int), 1, file);	
	//read number of colors per pixel
	fread(&bands, sizeof(int), 1, file);
	//read number of coefficients per pixel
	fread(&ordlen, sizeof(int), 1, file);

	if (feof(file))
		return -1;
				
	ordlen *= ordlen;
	
	fread(gmin, sizeof(float), ordlen, file);
	fread(gmax, sizeof(float), ordlen, file);

	if (feof(file))
		return -1;

	int offset = 0;

	int size = w * h * ordlen;
	float* redPtr = new float[size];
	float* greenPtr = new float[size];
	float* bluePtr = new float[size];

	for(int j = 0; j < h; j++)
	{
		if (cb != NULL)(*cb)(j * 70.0 / h, "Loading HSH...");
		for(int i = 0; i < w; i++)
		{				
			offset = j * w + i;
			for (int k = 0; k < ordlen; k++)
			{
				if (feof(file))
					return -1;
				fread(&c, sizeof(unsigned char), 1, file);
				redPtr[offset*ordlen + k] = (((float)c) / 255.0) * (gmax[k] - gmin[k]) + gmin[k];
			}
			for (int k = 0; k < ordlen; k++)
			{
				if (feof(file))
					return -1;
				fread(&c, sizeof(unsigned char), 1, file);
				greenPtr[offset*ordlen + k] = (((float)c) / 255.0) * (gmax[k] - gmin[k]) + gmin[k];
			}
			for (int k = 0; k < ordlen; k++)
			{
				if (feof(file))
					return -1;
				fread(&c, sizeof(unsigned char), 1, file);
				bluePtr[offset*ordlen + k] = (((float)c) / 255.0) * (gmax[k] - gmin[k]) + gmin[k];
			}
		}
	}
	
	fclose(file);

	mipMapSize[0] = QSize(w, h);

	redCoefficients.setLevel(redPtr, size, 0);
	greenCoefficients.setLevel(greenPtr, size, 0);
	blueCoefficients.setLevel(bluePtr, size, 0);
	
	// Computes mip-mapping.
	if (cb != NULL)	(*cb)(70, "Mip mapping generation...");
	
	for (int level = 1; level < MIP_MAPPING_LEVELS; level++)
	{
		int width = mipMapSize[level - 1].width();
		int height = mipMapSize[level - 1].height();
		int width2 = ceil(width / 2.0);
		int height2 = ceil(height / 2.0);
		size = width2*height2*ordlen;
		redCoefficients.setLevel(new float[size], size, level);
		greenCoefficients.setLevel(new float[size], size, level);
		blueCoefficients.setLevel(new float[size], size, level);
		for (int i = 0; i < height - 1; i+=2)
		{
			if (cb != NULL)	(*cb)(70 + (level-1)*10 + i*10.0/height, "Mip mapping generation...");
			for (int j = 0; j < width - 1; j+=2)
			{
				int index1 = (i * width + j);
				int index2 = (i * width + j + 1);
				int index3 = ((i + 1) * width + j);
				int index4 = ((i + 1) * width + j + 1);
				int offset = (i/2 * width2 + j/2);
				for (int k = 0; k < ordlen; k++)
				{
					redCoefficients.calcMipMapping(level, offset, index1, index2, index3, index4);
					greenCoefficients.calcMipMapping(level, offset, index1, index2, index3, index4);
					blueCoefficients.calcMipMapping(level, offset, index1, index2, index3, index4);
				}
			}
		}
		if (width2 % 2 != 0)
		{
			for (int i = 0; i < height - 1; i+=2)
			{
				int index1 = ((i + 1) * width - 1);
				int index2 = ((i + 2) * width - 1);
				int offset = ((i/2 + 1) * width2 - 1);
				for (int k = 0; k < ordlen; k++)
				{
					redCoefficients.calcMipMapping(level, offset, index1, index2);
					greenCoefficients.calcMipMapping(level, offset, index1, index2);
					blueCoefficients.calcMipMapping(level, offset, index1, index2);
				}
			}
		}
		if (height % 2 != 0)
		{
			for (int i = 0; i < width - 1; i+=2)
			{
				int index1 = ((height - 1) * width + i);
				int index2 = ((height - 1) * width + i + 1);
				int offset = ((height2 - 1) * width2 + i/2);
				for (int k = 0; k < ordlen; k++)
				{
					redCoefficients.calcMipMapping(level, offset, index1, index2);
					greenCoefficients.calcMipMapping(level, offset, index1, index2);
					blueCoefficients.calcMipMapping(level, offset, index1, index2);
				}
			}
		}
		if (height % 2 != 0 && width % 2 != 0)
		{
			int index1 = (height*width - 1);
			int offset = (height2*width2 - 1);
			for (int k = 0; k < ordlen; k++)
			{
				redCoefficients.calcMipMapping(level, offset, index1);
				greenCoefficients.calcMipMapping(level, offset, index1);
				blueCoefficients.calcMipMapping(level, offset, index1);
			}
		}
		mipMapSize[level] = QSize(width2, height2);
	}	

	
	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("HSH Loading: %.5f s\n", diff);
#endif

	return 0;
}


int Hsh::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int Hsh::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int Hsh::loadCompressed(QString name)
{
	return loadCompressed(0,0,w,h,name);
}


int Hsh::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	
	return 0;
}


int Hsh::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int Hsh::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	return 0;
}


int Hsh::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	// Computes the width and the height of the texture.
	width = ceil(rect.width());
	height = ceil(rect.height());
	int offx = rect.x();
	int offy = rect.y();
	(*buffer) = new unsigned char[width*height*4];
	int offsetBuf = 0;
	
	const float* redPtr = redCoefficients.getLevel(level);
	const float* greenPtr = greenCoefficients.getLevel(level);
	const float* bluePtr = blueCoefficients.getLevel(level);
	int tempW = mipMapSize[level].width();
	float hweights[9];
	vcg::Point3d temp(light.X(), light.Y(), light.Z());
	temp.Normalize();
	double phi = atan2(temp.Y(), temp.X());
	double theta = acos(temp.Z()/temp.Norm());
	if (theta > 1.52)
		theta = 1.52;
	int offset = 0;
	hweights[0] = 1/sqrt(2*M_PI);
	hweights[1] = sqrt(6/M_PI)      *  (cos(phi)*sqrt(cos(theta)-cos(theta)*cos(theta)));
	hweights[2] = sqrt(3/(2*M_PI))  *  (-1. + 2.*cos(theta));
	hweights[3] = sqrt(6/M_PI)      *  (sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));
	hweights[4] = sqrt(30/M_PI)     *  (cos(2.*phi)*(-cos(theta) + cos(theta)*cos(theta)));
	hweights[5] = sqrt(30/M_PI)     *  (cos(phi)*(-1. + 2.*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta)));
	hweights[6] = sqrt(5/(2*M_PI))  *  (1 - 6.*cos(theta) + 6.*cos(theta)*cos(theta));
	hweights[7] = sqrt(30/M_PI)     *  ((-1 + 2.*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));
	hweights[8] = sqrt(30/M_PI)     *  ((-cos(theta) + cos(theta)*cos(theta))*sin(2.*phi));

	for (int y = offy; y < offy + height; y++)
	{
		for (int x = offx; x < offx + width; x++)
		{
			int offset= y * tempW + x;
			double val = 0;
			for (int k = 0; k < ordlen; k++)
				val += redPtr[offset*ordlen + k] * hweights[k];
			(*buffer)[offsetBuf + 0] = tobyte(val*255);
			val = 0;
			for (int k = 0; k < ordlen; k++)
				val += greenPtr[offset*ordlen + k] * hweights[k];
			(*buffer)[offsetBuf + 1] = tobyte(val*255);
			val = 0;
			for (int k = 0; k < ordlen; k++)
				val += bluePtr[offset*ordlen + k] * hweights[k];
			(*buffer)[offsetBuf + 2] = tobyte(val*255);
			(*buffer)[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("Default rendering: %.5f s\n", diff);
	
#endif

	return 0;
}


QImage* Hsh::createPreview(int width, int height)
{
	// Computes the height and the width of the preview.
	int level = MIP_MAPPING_LEVELS;
	int imageH = mipMapSize[level].height();
	int imageW = mipMapSize[level].width();
	for (int i = 0; i < 4; i++)
	{
		if (mipMapSize[i].width() <= width || mipMapSize[i].height() <= height)
		{
			if (mipMapSize[i].width() < width && mipMapSize[i].height() < height && i > 0)
				i--;
			imageH = mipMapSize[i].height();
			imageW = mipMapSize[i].width();
			level = i;
			break;
		}
	}
	
	// Creates the preview.
	unsigned char* buffer = new unsigned char[imageH*imageW*4];
	int offsetBuf = 0;

	const float* redPtr = redCoefficients.getLevel(level);
	const float* greenPtr = greenCoefficients.getLevel(level);
	const float* bluePtr = blueCoefficients.getLevel(level);
	int tempW = mipMapSize[level].width();
	float hweights[9];
	float phi = 0.0f;
	float theta = acos(1.0);
	int offset = 0;
	hweights[0] = 1/sqrt(2*M_PI);
	hweights[1] = sqrt(6/M_PI)      *  (cos(phi)*sqrt(cos(theta)-cos(theta)*cos(theta)));
	hweights[2] = sqrt(3/(2*M_PI))  *  (-1. + 2.*cos(theta));
	hweights[3] = sqrt(6/M_PI)      *  (sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));
	hweights[4] = sqrt(30/M_PI)     *  (cos(2.*phi)*(-cos(theta) + cos(theta)*cos(theta)));
	hweights[5] = sqrt(30/M_PI)     *  (cos(phi)*(-1. + 2.*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta)));
	hweights[6] = sqrt(5/(2*M_PI))  *  (1 - 6.*cos(theta) + 6.*cos(theta)*cos(theta));
	hweights[7] = sqrt(30/M_PI)     *  ((-1 + 2.*cos(theta))*sqrt(cos(theta) - cos(theta)*cos(theta))*sin(phi));
	hweights[8] = sqrt(30/M_PI)     *  ((-cos(theta) + cos(theta)*cos(theta))*sin(2.*phi));
	
	for (int y = 0; y < imageH; y++)
	{
		for (int x = 0; x < imageW; x++)
		{
			offset= y * imageW + x;
			double val = 0;
			for (int k = 0; k < ordlen; k++)
				val += redPtr[offset*ordlen + k] * hweights[k];
			buffer[offsetBuf + 2] = tobyte(val*255);
			val = 0;
			for (int k = 0; k < ordlen; k++)
				val += greenPtr[offset*ordlen + k] * hweights[k];
			buffer[offsetBuf + 1] = tobyte(val*255);
			val = 0;
			for (int k = 0; k < ordlen; k++)
				val += bluePtr[offset*ordlen + k] * hweights[k];
			buffer[offsetBuf + 0] = tobyte(val*255);
			buffer[offsetBuf + 3] = 255;
			offsetBuf += 4;
		}
	}
	QImage* image = new QImage(buffer, imageW, imageH, QImage::Format_RGB32);
	return image;
}


int Hsh::allocateRemoteImage(int width, int height, int maxResLevel)
{
	
	return 0;
}


int Hsh::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	
	return 0;
}
