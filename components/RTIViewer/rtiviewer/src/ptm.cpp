/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


// Local headers
#include "ptm.h"
#include "../../rtibuilder/src/zorder.h"

// Qt headers
#include <QStringList>
#include <QImage>
#include <QTransform>
#include <QTime>


Rti* Ptm::getPtm(QTextStream &in)
{
	QString version = in.readLine();
	QString format = in.readLine();
	
	if (format == "PTM_FORMAT_LRGB")
	{
		LRGBPtm* image = new LRGBPtm();
		return image;
	}
	else if (format == "PTM_FORMAT_RGB")
	{
		RGBPtm* image = new RGBPtm();
		return image;
	}
	else if (format == "PTM_FORMAT_JPEG_LRGB")
	{
		JPEGLRGBPtm* image = new JPEGLRGBPtm();
		return image;
	}
	return 0;
}

//////////////////////////////////////////////////////////////////////////
// RGB PTM

RGBPtm::RGBPtm() : Ptm()
{
	
}


RGBPtm::~RGBPtm()
{
	
}


int RGBPtm::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int RGBPtm::load(QString name, CallBackPos * cb)
{
#ifdef PRINT_DEBUG
	QTime first =  QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading RGB PTM...");
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
	bool eof;
	//Gets version
	version = getLine(file, &eof);
	if (eof) return -1;

	//Gets type
	type = "RGB PTM";
	getLine(file, &eof);
	if (eof) return -1;

	//Gets width and height
	bool error;
	QString str = getLine(file, &eof);
	if (eof) return -1;
	setWidth(str.toInt(&error));
	if (!error) return -1;
	str = getLine(file, &eof);
	if (eof) return -1;
	setHeight(str.toInt(&error));
	if (!error) return -1;
	QString text = "Loading RGB PTM...";
	if (loadData(file, w, h, 6, false, cb, text) != 0)
		return -1;

	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("RGB PTM  Loading: %.5f s\n", diff);
#endif

	return 0;
}


int RGBPtm::loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb, QString& text)
{
	w = width;
	h = height;

	if (!urti)
	{
		//Gets scale value
		bool eof, error; 
		QString str = getLine(file, &eof);
		if (eof) return -1;
		QStringList list = str.split(' ',  QString::SkipEmptyParts);
		if (list.size() != 6)
			return -1;
		for (int i = 0; i < 6; i++)
		{
			scale[i] = list[i].toDouble(&error);
			if (!error) return -1;
		}

		//Gets bias value
		str = getLine(file, &eof);
		if (eof) return -1;
		list = str.split(' ',  QString::SkipEmptyParts);
		if (list.size() != basisTerm)
			return -1;
		for (int i = 0; i < basisTerm; i++)
		{
			bias[i] = list[i].toInt(&error);
			if (!error) return -1;
		}
	}
	else
	{

	}

	//Allocates array for polynomial coefficients
	int* redCoeff = new int[w*h*basisTerm];
	int* greenCoeff = new int[w*h*basisTerm];
	int* blueCoeff = new int[w*h*basisTerm];
	
	//Reads polynomial coefficients
	int offset;
	unsigned char c;
	for (int j = 0; j < 3; j++)
	{
		for (int y = h - 1; y >= 0; y--)
		{
			if (cb != NULL && (y % 50) == 0) (*cb)(15*j + (h - y) * 15 / h, text);
			for (int x = 0; x < w; x++)
			{
				offset = y * w + x;
				for (int i = 0; i < basisTerm; i++)
				{
					if(feof(file))
						return -1;
					fread(&c, sizeof(unsigned char), 1, file);
					if (j == 0)
						redCoeff[offset*basisTerm + i] = static_cast<int>((c - bias[i])*scale[i]);
					else if (j == 1)
						greenCoeff[offset*basisTerm + i] = static_cast<int>((c - bias[i])*scale[i]);
					else
						blueCoeff[offset*basisTerm + i] = static_cast<int>((c - bias[i])*scale[i]);
				}
			}
		}
	}
	fclose(file);

	// Computes mip-mapping-level.
	mipMapSize[0] = QSize(w, h);
	redCoefficients.setLevel(redCoeff, w*h*basisTerm, 0);
	greenCoefficients.setLevel(greenCoeff, w*h*basisTerm, 0);
	blueCoefficients.setLevel(blueCoeff, w*h*basisTerm, 0);
	if (cb != NULL)	(*cb)(45, "Mip mapping generation...");
	generateMipMap(1, w, h, cb, 45, 15);

	// Computes the normals
	calculateNormals(normals, redCoefficients, true, cb, 60, 10);
	calculateNormals(normals, greenCoefficients, false, cb, 70, 10);
	calculateNormals(normals, blueCoefficients, false, cb, 80, 10);
	for(int level = 0; level < MIP_MAPPING_LEVELS; level++)
	{
		int lenght = normals.getLevelLenght(level);
		vcg::Point3f* tempNormals = new vcg::Point3f[lenght];
		memcpy(tempNormals, normals.getLevel(level), sizeof(vcg::Point3f)*lenght);
		for (int i = 0; i < lenght; i++)
		{
			if (cb != NULL && i%500 == 0)	(*cb)(90 + level*2.5 + 2.5 * i / lenght, "Normals generation...");
			tempNormals[i] /= 3;
			tempNormals[i].Normalize();
		}
		normals.setLevel(tempNormals, lenght, level);
	}

	return 0;
}



int RGBPtm::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int RGBPtm::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int RGBPtm::loadCompressed(QString name)
{
	remote = false;
	return loadCompressed(0,0,w,h,name);
}


int RGBPtm::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	//...TODO...
	return 0;
}


int RGBPtm::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int RGBPtm::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	//...TODO...

	return 0;
}


int RGBPtm::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	// Computes height and width of the texture.
	width = ceil(rect.width());
	height = ceil(rect.height());
	int offx = rect.x();
	int offy = rect.y();
	if (currentRendering != DETAIL_ENHANCEMENT || mode == NORMALS_MODE || mode == LUMR_MODE || mode == LUMG_MODE || mode == LUMB_MODE)
	{
		for (int i = 0; i < level; i++)
		{
			width = ceil(width/2.0);
			height = ceil(height/2.0);
			offx = offx/2;
			offy = offy/2;
		}
	}

	(*buffer) = new unsigned char[width*height*4];
	int offsetBuf = 0;
	
	if (mode == NORMALS_MODE)
	{
		// Creates normal map.
		const vcg::Point3f* normalPtr = normals.getLevel(level);
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				for (int i = 0; i < 3; i++)
					(*buffer)[offsetBuf + i] = toColor(normalPtr[offset][i]);
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}
	else if (mode == LUMR_MODE || mode == LUMG_MODE || mode == LUMB_MODE)
	{
		// Creates map of the RGB component.
		const int* coeffPtr = NULL;
		switch(mode)
		{
			case LUMR_MODE:
				coeffPtr = redCoefficients.getLevel(level); break;
			case LUMB_MODE:
				coeffPtr = greenCoefficients.getLevel(level); break;
			case LUMG_MODE:
				coeffPtr = blueCoefficients.getLevel(level); break;
		}
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				unsigned char c = tobyte(evalPoly(&coeffPtr[offset*6], light.X(), light.Y()));
				(*buffer)[offsetBuf + 0] = c;
				(*buffer)[offsetBuf + 1] = c;
				(*buffer)[offsetBuf + 2] = c;
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}

	}
	else
	{
		// Applies the current rendering mode.
		RenderingInfo info = {offx, offy, height, width, level, mode, light};
		list->at(currentRendering)->applyPtmRGB(redCoefficients, greenCoefficients, blueCoefficients, mipMapSize, normals, info, (*buffer));
	}
	
#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	if (mode == DEFAULT_MODE)
	{
		switch(currentRendering)
		{
			case NORMAL: printf("Default rendering: %.5f s\n", diff); break;
			case DIFFUSE_GAIN: printf("Diffuse gain: %.5f s\n", diff); break;
			case SPECULAR_ENHANCEMENT: printf("Specular enhancement: %.5f s\n", diff); break;
			case NORMAL_ENHANCEMENT: printf("Normal enhancement: %.5f s\n", diff); break;
			case UNSHARP_MASKING_IMG: printf("Unsharp masking image: %.5f s\n", diff); break;
			case UNSHARP_MASKING_LUM: printf("Unsharp masking luminance: %.5f s\n", diff); break;
			case COEFF_ENHANCEMENT: printf("Coefficient enhancement: %.5f s\n", diff); break;
			case DETAIL_ENHANCEMENT: printf("Detail enhancement: %.5f s\n", diff); break;
			case DYN_DETAIL_ENHANCEMENT: printf("Dynamic detail enhancement: %.5f s\n", diff); break;
		}
	}
	else
		printf("Browing mode: %.5f s\n", diff);
#endif
	return 0;
}


QImage* RGBPtm::createPreview(int width, int height)
{
	// Computes the height and the width of the preview.
	int level = 3;
	int imageH = mipMapSize[3].height();
	int imageW = mipMapSize[3].width();
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
	const int* redPtr = redCoefficients.getLevel(level);
	const int* greenPtr = greenCoefficients.getLevel(level);
	const int* bluePtr = blueCoefficients.getLevel(level);
	int offset = 0;
	for (int i = 0; i < imageH; i++)
	{
		for (int j = 0; j < imageW; j++)
		{
			offset = i * imageW + j;
			buffer[offset*4 + 2] = tobyte(evalPoly(&redPtr[offset*6], 0, 0));
			buffer[offset*4 + 1] = tobyte(evalPoly(&greenPtr[offset*6], 0, 0));
			buffer[offset*4 + 0] = tobyte(evalPoly(&bluePtr[offset*6], 0, 0));
			buffer[offset*4 + 3] = 255;
		}
	}
	QImage* image = new QImage(buffer, imageW, imageH, QImage::Format_RGB32);
	return image;
}


int RGBPtm::allocateRemoteImage(int width, int height, int maxResLevel)
{
	return -1;
}  


int RGBPtm::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	return -1;
}


void RGBPtm::allocateSubLevel(int level, int w, int h)
{
	redCoefficients.allocateLevel(level, w * h * 6);
	greenCoefficients.allocateLevel(level, w * h * 6);
	blueCoefficients.allocateLevel(level, w * h * 6);
}


void RGBPtm::calculateMipMap(int pos, int level, int i1)
{
	for (int k = 0; k < 6; k++)
	{
		redCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k);
		greenCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k);
		blueCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k);
	}
}


void RGBPtm::calculateMipMap(int pos, int level, int i1, int i2)
{
	for (int k = 0; k < 6; k++)
	{
		redCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k );
		greenCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k);
		blueCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k);
	}
}


void RGBPtm::calculateMipMap(int pos, int level, int i1, int i2, int i3, int i4)
{
	for (int k = 0; k < 6; k++)
	{
		redCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k, i3*6 + k, i4*6 + k);
		greenCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k, i3*6 + k, i4*6 + k);
		blueCoefficients.calcMipMapping(level, pos*6 + k, i1 * 6 + k, i2*6 + k, i3*6 + k, i4*6 + k);
	}
}


//////////////////////////////////////////////////////////////////////////
// LRGB PTM 

LRGBPtm::LRGBPtm() :
	Ptm()
{

}


LRGBPtm::~LRGBPtm()
{

}


int LRGBPtm::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int LRGBPtm::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading LRGB PTM...");
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
	bool eof;
	//Gets version 
	version = getLine(file, &eof);
	if (eof) return -1;
	
	//Gets type
	type = "LRGB PTM";
	getLine(file, &eof);
	if (eof) return -1;

	//Gets width and hieght
	QString str = getLine(file, &eof);
	bool error;
	if (eof) return -1;
	setWidth(str.toInt(&error));
	if (!error) return -1;
	str = getLine(file, &eof);
	if (eof) return -1;
	setHeight(str.toInt(&error));
	if (!error) return -1;

	QString text = "Loading LRGB PTM...";
	if (loadData(file, w, h, 6, false, cb, text) != 0)
		return -1;

	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("LRGB PTM Loading: %.5f s\n", diff);
#endif

	return 0;
}


int LRGBPtm::loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb, QString& text)
{
	w = width;
	h = height;

	if (!urti)
	{
		//Gets scale value
		bool eof, error;
		QString str = getLine(file, &eof);
		if (eof) return -1;
		QStringList list = str.split(' ', QString::SkipEmptyParts);
		if (list.size() != 6)
			return -1;
		for (int i = 0; i < 6; i++)
		{
			scale[i] = list[i].toDouble(&error);
			if (!error) return -1;
		}
		
		//Gets bias value
		str = getLine(file, &eof);
		if (eof) return -1;
		list = str.split(' ',  QString::SkipEmptyParts);
		if (list.size() != 6)
			return -1;
		for (int i = 0; i < 6; i++)
		{
			bias[i] = list[i].toInt(&error);
			if (!error) return -1;
		}
	}
	else
	{

	}
	
	//Allocates array for polynomial coefficients and rgb components
	int* coeffPtr = new int[w*h*basisTerm];;
	unsigned char* rgbPtr = new unsigned char[w*h*3];

	int offset;
	unsigned char c;
	
	//Reads coefficient and rgb components from file
	for (int y = h - 1; y >= 0; y--)
	{
		if (cb != NULL && (y % 50 == 0))(*cb)((h - y) * 40 / h, text);
		for (int x = 0; x < w; x++)
		{
			offset = y * w + x;
			
			for (int i = 0; i < 6; i++)
			{
				if(feof(file))
					return -1;
				fread(&c, sizeof(unsigned char), 1, file);
				coeffPtr[offset*basisTerm + i] = static_cast<int>((c - bias[i])*scale[i]);
			}

			if (version == "PTM_1.1")
			{
				for (int i = 0; i < 3; i++)
				{
					if (feof(file))
						return -1;
					fread(&c, sizeof(unsigned char), 1, file);
					rgbPtr[offset*3 + i] = c;
				}
			}
		}
	}

	if (version == "PTM_1.2")
	{
		for (int y = h - 1; y >= 0; y--)
		{
			if (cb != NULL && (h-y)%100==0)	(*cb)(40 + (h - y) * 10 / h , "Loading LRGB PTM...");
			for (int x = 0; x < w; x++)
			{
				offset = y * w + x;
				for (int i = 0; i < 3; i++)
				{
					if (feof(file))
						return -1;
					fread(&c, sizeof(unsigned char), 1, file);
					rgbPtr[offset*3 + i] = c;
				}
			}
		}
	}
	fclose(file);

	mipMapSize[0] = QSize(w, h);

	coefficients.setLevel(coeffPtr, w*h*6, 0);
	rgb.setLevel(rgbPtr, w*h*3, 0);
	
	// Computes mip-mapping and normals.
	if (cb != NULL)	(*cb)(55, "Mip mapping generation...");
	generateMipMap(1, w, h, cb, 55, 20);
	calculateNormals(normals, coefficients, true, cb, 75, 20);
	return 0;
}


int LRGBPtm::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int LRGBPtm::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int LRGBPtm::loadCompressed(QString name)
{
	return loadCompressed(0,0,w,h,name);
}


int LRGBPtm::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	remote = false;
	Jpeg2000 jpegimage(name.toStdString().c_str());
	int offset,offset2;
	for (int y = yinf; y < ysup; y++)
		for (int x = xinf; x < xsup; x++)
		{
			offset = x + y * w;
			offset2 = (x-xinf) + (y-yinf) * w;
			rgb.setElement(0, offset*3, jpegimage.componentData(0)[offset2]);
			rgb.setElement(0, offset*3 + 1, jpegimage.componentData(1)[offset2]);
			rgb.setElement(0, offset*3 + 2, jpegimage.componentData(2)[offset2]);

			for (int k = 0; k < 6; k++)
				coefficients.setElement(0, offset * 6 + k, jpegimage.componentData(k+3)[offset2]);
		}

	return 0;
}


int LRGBPtm::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int LRGBPtm::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	// coordinate adjustment
	int ww;
	if (reslevel > 0)
	{
		xinf = xinf >> reslevel;
		yinf = yinf >> reslevel;
		xsup = xsup >> reslevel;
		ysup = ysup >> reslevel;
		ww = mipMapSize[reslevel].width();
	}
	else
		ww = w;

	int tilew = (xsup - xinf);
	int tileh = (ysup - yinf);

	int **comps = new int *[9];
	for (int k = 0; k < 9; k++)
		comps[k] = new int[tilew*tileh];

	int offset, offset2;
	const unsigned char* rgbPtr = rgb.getLevel(reslevel);
	const int* coeffPtr = coefficients.getLevel(reslevel);
	for (int y = yinf; y < ysup; y++)
		for (int x = xinf; x < xsup; x++)
		{
			offset = x + y * ww;
			offset2 = (x-xinf) + (y-yinf) * tilew;
			comps[0][offset2] = rgbPtr[offset*3];
			comps[1][offset2] = rgbPtr[offset*3+1];
			comps[2][offset2] = rgbPtr[offset*3+2];

			for (int k = 0; k < 6; k++)
				comps[k+3][offset2] = coeffPtr[offset*6 + k];
				
		}


	// Saves as a JPEG2000 image with 9 gray components of 16 bit each
		Jpeg2000 jpegimage(tilew, tileh, 16, 16, 9, comps, GRAY_CLRSPC, J2K_CFMT);
	jpegimage.save(name.toStdString().c_str());

	for (int k = 0; k < 9; k++)
		delete [] comps[k];

	delete [] comps;

	return 0;
}


int LRGBPtm::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	// Computes the width and the height of the texture.
	width = ceil(rect.width());
	height = ceil(rect.height());
	int offx = rect.x();
	int offy = rect.y();
	if (remote)
	{
		if (level < maxRemoteResolution - minRemoteResolution)
		{
			int size = 1 << maxRemoteResolution;
			float deltaW = static_cast<float>(w)/static_cast<float>(size);
			float deltaH = static_cast<float>(h)/static_cast<float>(size);
			int r1 = static_cast<int>(rect.y() / deltaH);
			int c1 = static_cast<int>(rect.x() / deltaW);
			int r2 = static_cast<int>(rect.bottom() / deltaH);
			int c2 = static_cast<int>(rect.right() / deltaW);
			int result = 15;
			for(int i = r1; i <= r2; i++)
				for (int j = c1; j <= c2; j++)
					result &= tiles[ZOrder::ZIndex(i, j, maxRemoteResolution)];
			bool found = false;
			while(!found && level < maxRemoteResolution - minRemoteResolution)
			{
				if (result & (1 << level))
					found = true;
				else
					level++;
			}
		}
		else
			level = maxRemoteResolution - minRemoteResolution;
	}
	bool flag = (mode == NORMALS_MODE || mode == LUM_MODE || mode == RGB_MODE || (mode >= A0_MODE && mode <= A5_MODE));
	if (currentRendering != DETAIL_ENHANCEMENT || flag)
	{
		for (int i = 0; i < level; i++)
		{
			width = ceil(width/2.0);
			height = ceil(height/2.0);
			offx = offx/2;
			offy = offy/2;
		}
	}

	(*buffer) = new unsigned char[width*height*4];
	int offsetBuf = 0;
	if (mode == NORMALS_MODE)
	{
		// Creates the maps of normals.
		const vcg::Point3f* normalsLevel = normals.getLevel(level);
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				for (int i = 0; i < 3; i++)
					(*buffer)[offsetBuf + i] = toColor(normalsLevel[offset][i]);
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}
	else if (mode == LUM_MODE)
	{
		// Creates the map of luminance.
		const int* coeffPtr = coefficients.getLevel(level);
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				double lum = evalPoly(&coeffPtr[offset*6], light.X(), light.Y());
				unsigned char l = tobyte(lum / 2.0);
				for (int i = 0; i < 3; i++)
					(*buffer)[offsetBuf + i] = l;
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}

	}
	else if (mode == RGB_MODE)
	{
		// Creates the map of RGB components.
		const unsigned char* rgbPtr = rgb.getLevel(level);
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				for (int i = 0; i < 3; i++)
					(*buffer)[offsetBuf + i] = rgbPtr[offset*3 + i];
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}
	else if (mode >= A0_MODE && mode <= A5_MODE)
	{
		// Creates the map of the coefficient.
		const int* coeffPtr = coefficients.getLevel(level);
		for (int y = offy; y < offy + height; y++)
		{
			for (int x = offx; x < offx + width; x++)
			{
				int offset = y * mipMapSize[level].width() + x;
				unsigned char b;
				int index = mode - A0_MODE;
				b = coeffPtr[offset*6 + index]/scale[index] + bias[index];
				for (int i = 0; i < 3; i++)
					(*buffer)[offsetBuf + i] = b;
				(*buffer)[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}
	else
	{
		// Applies the current rendering mode.
		RenderingInfo info = {offx, offy, height, width, level, mode, light};
		list->at(currentRendering)->applyPtmLRGB(coefficients, rgb, mipMapSize, normals, info, (*buffer));
	}

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	if (mode == DEFAULT_MODE)
	{
		switch(currentRendering)
		{
			case NORMAL: printf("Default rendering: %.5f s\n", diff); break;
			case DIFFUSE_GAIN: printf("Diffuse gain: %.5f s\n", diff); break;
			case SPECULAR_ENHANCEMENT: printf("Specular enhancement: %.5f s\n", diff); break;
			case NORMAL_ENHANCEMENT: printf("Normal enhancement: %.5f s\n", diff); break;
			case UNSHARP_MASKING_IMG: printf("Unsharp masking image: %.5f s\n", diff); break;
			case UNSHARP_MASKING_LUM: printf("Unsharp masking luminance: %.5f s\n", diff); break;
			case COEFF_ENHANCEMENT: printf("Coefficient enhancement: %.5f s\n", diff); break;
			case DETAIL_ENHANCEMENT: printf("Detail enhancement: %.5f s\n", diff); break;
			case DYN_DETAIL_ENHANCEMENT: printf("Dynamic detail enhancement: %.5f s\n", diff); break;
		}
	}
	else
		printf("Browing mode: %.5f s\n", diff);
#endif

	return 0;
}


QImage* LRGBPtm::createPreview(int width, int height)
{
	// Computes the height and the width of the preview.
	int level = 3;
	int imageH = mipMapSize[3].height();
	int imageW = mipMapSize[3].width();
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
	int offset = 0;
	const int* coeffLevel = coefficients.getLevel(level);
	const unsigned char* rgbLevel = rgb.getLevel(level);
	for (int i = 0; i < imageH; i++)
	{
		for (int j = 0; j < imageW; j++)
		{
			offset = i * imageW + j;
			double lum = evalPoly(&coeffLevel[offset*6], 0, 0) / 255.0;
			buffer[offset*4 + 2] = tobyte(rgbLevel[offset*3] * lum);
			buffer[offset*4 + 1] = tobyte(rgbLevel[offset*3 + 1] * lum);
			buffer[offset*4 + 0] = tobyte(rgbLevel[offset*3 + 2] * lum);
			buffer[offset*4 + 3] = 255;
		}
	}
	QImage* image = new QImage(buffer, imageW, imageH, QImage::Format_RGB32);
	return image;
}


int LRGBPtm::allocateRemoteImage(int width, int height, int maxResLevel)
{
	if (width <= 0 || height <= 0 || maxResLevel <= 0)
		return -1;
	((DefaultRenderingPtm*)list->at(NORMAL))->setRemote(true);
	w = width;
	h = height;
	remote = true;
	maxRemoteResolution = maxResLevel;
	minRemoteResolution = maxResLevel - 3 > 0 ? maxResLevel - 3 : 1;
	for (int i = maxRemoteResolution; i > maxRemoteResolution - 4; i--)
	{
		int n = 1 << (maxRemoteResolution - i);
		width = ceil(static_cast<double>(w)/static_cast<double>(n));
		height = ceil(static_cast<double>(h)/static_cast<double>(n));
		allocateSubLevel(maxRemoteResolution - i, width, height);
		normals.allocateLevel(maxRemoteResolution - i ,width*height);
		mipMapSize[maxRemoteResolution - i] = QSize(width, height);
	}
	int n = 1 << maxRemoteResolution;
	tiles = new unsigned int [n*n];
	for(int i = 0; i <n*n; i++)
		tiles[i] = 0;
	type = "LRGB PTM";
	return 0;
}


int LRGBPtm::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	unsigned char* stream = (unsigned char*) b->buffer().data();
	Jpeg2000 jpegimage(stream, b->buffer().length());
	
	xinf >>= level;
	yinf >>= level;
	xsup >>= level;
	ysup >>= level;
	int offset,offset2;
	for (int y = yinf; y < ysup; y++)
		for (int x = xinf; x < xsup; x++)
		{
			offset = x + y * mipMapSize[level].width();
			offset2 = (x-xinf) + (y-yinf) * (xsup - xinf);
			rgb.setElement(level, offset*3, jpegimage.componentData(0)[offset2]);
			rgb.setElement(level, offset*3 + 1, jpegimage.componentData(1)[offset2]);
			rgb.setElement(level, offset*3 + 2, jpegimage.componentData(2)[offset2]);

			for (int k = 0; k < 6; k++)
				coefficients.setElement(level, offset * 6 + k, jpegimage.componentData(k+3)[offset2]);
			
			// Computes normals
			normals.setElement(level, offset, calculateNormal(&(coefficients.getLevel(level)[offset*6])));
		}

	return 0;
}


void LRGBPtm::allocateSubLevel(int level, int w, int h)
{
	coefficients.allocateLevel(level, w*h*6);
	rgb.allocateLevel(level, w*h*3); 
}


void LRGBPtm::calculateMipMap(int pos, int level, int i1)
{
	for (int k = 0; k < 6; k++)
		coefficients.calcMipMapping(level, pos*6 + k, i1*6 +k);
	for (int k = 0; k < 3; k++)
		rgb.calcMipMapping(level, pos*3 + k, i1*3 + k);
}


void LRGBPtm::calculateMipMap(int pos, int level, int i1, int i2)
{
	for (int k = 0; k < 6; k++)
		coefficients.calcMipMapping(level, pos*6 + k, i1*6 +k, i2*6 +k);
	for (int k = 0; k < 3; k++)
		rgb.calcMipMapping(level, pos*3 + k, i1*3 + k, i2*3 +k);
}


void LRGBPtm::calculateMipMap(int pos, int level, int i1, int i2, int i3, int i4)
{
	for (int k = 0; k < 6; k++)
		coefficients.calcMipMapping(level, pos*6 + k, i1*6 +k, i2*6 +k, i3*6 +k, i4*6 +k);
	for (int k = 0; k < 3; k++)
		rgb.calcMipMapping(level, pos*3 + k, i1*3 + k, i2*3 +k, i3*3 +k, i4*3 +k);
}


//////////////////////////////////////////////////////////////////////////
// JPEGLRGB PTM 

JPEGLRGBPtm::JPEGLRGBPtm():LRGBPtm()
{

}


JPEGLRGBPtm::~JPEGLRGBPtm()
{
}


int JPEGLRGBPtm::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int JPEGLRGBPtm::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	filename = name;
	if (cb != NULL)	(*cb)(0, "Loading JPEG-LRGB PTM...");

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
	bool eof;
	//Gets version 
	version = getLine(file, &eof);
	if (eof) return -1;
	
	//Gets type
	type = "JPEG-LRGB PTM";
	getLine(file, &eof);
	if (eof) return -1;

	//Gets width and hieght
	QString str = getLine(file, &eof);
	bool error;
	if (eof) return -1;
	setWidth(str.toInt(&error));
	if (!error) return -1;
	str = getLine(file, &eof);
	if (eof) return -1;
	setHeight(str.toInt(&error));
	if (!error) return -1;

	//Gets scale value
	str = getLine(file, &eof);
	if (eof) return -1;
	QStringList list = str.split(' ');
	if (list.size() != 6)
		return -1;
	for (int i = 0; i < 6; i++)
	{
		scale[i] = list[i].toDouble(&error);
		if (!error) return -1;
	}
	
	//Gets bias value
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	if (list.size() != 6)
		return -1;
	for (int i = 0; i < 6; i++)
	{
		bias[i] = list[i].toInt(&error);
		if (!error) return -1;
	}
	
	//Gets compression parameter
	str = getLine(file, &eof);
	if (eof) return -1;
	int compressionParameter = str.toInt(&error);
	if (!error) return -1;

	//Gets trasforms
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	int xForm[9];
	if (list.size() != 9)
		return -1;
	for (int i = 0; i < 9; i++)
	{
		xForm[i] = list[i].toInt(&error);
		if (!error) return -1;
	}

	//Gets motion vector
	getLine(file, &eof);
	getLine(file, &eof);

	//Gets order
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	int order[9];
	if (list.size() != 9)
		return -1;
	for (int i = 0; i < 9; i++)
	{
		order[i] = list[i].toInt(&error);
		if (!error) return -1;
	}

	//Gets reference plane
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	int referencePlane[9];
	if (list.size() != 9)
		return -1;
	for (int i = 0; i < 9; i++)
	{
		referencePlane[i] = list[i].toInt(&error);
		if (!error) return -1;
	}

	//Gets compressed size
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	int compressedSize[9];
	if (list.size() != 9)
		return -1;
	for (int i = 0; i < 9; i++)
	{
		compressedSize[i] = list[i].toInt(&error);
		if (!error) return -1;
	}

	//Gets side information
	str = getLine(file, &eof);
	if (eof) return -1;
	list = str.split(' ');
	int sideInformation[9];
	if (list.size() != 9)
		return -1;
	for (int i = 0; i < 9; i++)
	{
		sideInformation[i] = list[i].toInt(&error);
		if (!error) return -1;
	}

	unsigned char* plane[9];
	int planeLenght[9];
	unsigned char* info[9];
	int offset;
	for (int i = 0; i < 9; i++)
	{
		if (cb != NULL)(*cb)(5 + i * 2, "Loading JPEG-LRGB PTM...");
		//Reads plane
		unsigned char c;
		unsigned char* comprPlane = new unsigned char[compressedSize[i]];
		for (int j = 0; j < compressedSize[i]; j++)
		{
			if (feof(file))
			{
				for (int k = 0; k < i; k++)
				{
					delete[] plane[k];
					delete[] info[k];
				}
				delete[] comprPlane;
				return -1;
			}
			fread(&c, sizeof(unsigned char), 1, file);
			comprPlane[j] = c;
		}

		//Reads the side info
		info[i] = new unsigned char[sideInformation[i]];
		for (int j = 0; j < sideInformation[i]; j++)
		{
			if (feof(file))
			{
				for (int k = 0; k < i; k++)
				{
					delete[] plane[k];
					delete[] info[k];
				}
				delete[] comprPlane;
				return -1;
			}
			fread(&c, sizeof(unsigned char), 1, file);
			info[i][j] = c;
		}
		//Decodes plane
		QImage imagePlane = QImage::fromData(comprPlane, compressedSize[i], "JPEG");
		if (imagePlane.isNull())
			return -1;
		
		QImage transformed = imagePlane.mirrored(false, true);
		planeLenght[i] = transformed.height() * transformed.width();
		plane[i] = new unsigned char[planeLenght[i]];
		delete[] comprPlane;
		for (int j = 0; j < planeLenght[i]; j++)
			plane[i][j] = transformed.bits()[j];
	}

	int* coef[9];
	for (int i = 0; i < 9; i++)
	{
		if (cb != NULL)(*cb)(23 + i * 2, "Loading JPEG-LRGB PTM...");
		int index = indexOf(i, order, 9);
		if (index == -1)
		{
			for (int j = 0; j < 9; j++)
			{
				delete[] plane[j];
				delete[] info[j];
				if (j < i)
					delete[] coef[j];
			}
			return -1;
		}
		if (referencePlane[index] < 0)
		{
			coef[index]  = new int[planeLenght[index]];
			for (int j = 0; j < planeLenght[index]; j++)
				coef[index][j] = static_cast<int>(plane[index][j]);
		}
		else if (xForm[index] == 0)
			coef[index] = combine(coef[referencePlane[index]], plane[index], planeLenght[index]);
		else if (xForm[index] == 1)
		{
			int* inverse = invert(coef[referencePlane[index]], planeLenght[referencePlane[index]]);
			coef[index] = combine(inverse, plane[index], planeLenght[index]);
			delete[] inverse;
		}
		if (sideInformation[index] > 0)
			correctCoeff(coef[index], info[index], sideInformation[index], w, h);
	}
	
	int* coeffPtr = new int[w*h*6];
	unsigned char* rgbPtr = new unsigned char[w*h*3];

	for(int y = h - 1; y >= 0; y--)
	{
		for (int x = 0; x < w; x++)
		{
			if (cb != NULL && (y % 100 == 0))(*cb)(41 + (h-y)*29/h, "Loading JPEG-LRGB PTM...");
			offset = w * y + x;
			for (int i = 0; i < 6; i++)
				coeffPtr[offset*6 + i] = static_cast<int>((coef[i][offset] - bias[i])*scale[i]);
			rgbPtr[offset*3] = tobyte(coef[6][offset]);
			rgbPtr[offset*3 + 1] = tobyte(coef[7][offset]);
			rgbPtr[offset*3 + 2] = tobyte(coef[8][offset]);
		}
	}
	
	// Computes mip-mapping and normals.
	mipMapSize[0] = QSize(w, h);
	coefficients.setLevel(coeffPtr, w*h*6, 0);
	rgb.setLevel(rgbPtr, w*h*3, 0);
	if (cb != NULL)	(*cb)(70, "Calulation mip mapping...");
	generateMipMap(1, w, h, cb, 70, 10);
	if (cb != NULL)	(*cb)(80, "Calculation normals...");
	calculateNormals(normals, coefficients, true, cb, 80, 18);
	for(int i = 0; i < 9; i++)
	{
		delete[] plane[i];
		delete[] info[i];
		delete[] coef[i];
	}
	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("JPEG LRGB PTM Loading: %.5f\n s", diff);
#endif

	return 0;
}


int JPEGLRGBPtm::save(QString name)
{
	//...TODO...
	return 0;
}

