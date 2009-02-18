/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef PTM_H
#define PTM_H

#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

// Local headers
#include "rti.h"
#include "pyramid.h"
#include "renderingmode.h"
#include "diffusegain.h"
#include "specularenhanc.h"
#include "normalenhanc.h"
#include "unsharpmasking.h"
#include "coeffenhanc.h"
#include "detailenhanc.h"
#include "dyndetailenhanc.h"
#include <jpeg2000.h>

// Qt headers
#include <QFile>
#include <QImage>
#include <QVector>

#include <vcg/math/base.h>

/*!
  Rendering mode for PTM image.
*/
enum RenderingPtm
{
	NORMAL,
	DIFFUSE_GAIN,
	SPECULAR_ENHANCEMENT,
	NORMAL_ENHANCEMENT,
	UNSHARP_MASKING_IMG,
	UNSHARP_MASKING_LUM,
	COEFF_ENHANCEMENT,
	DETAIL_ENHANCEMENT,
	DYN_DETAIL_ENHANCEMENT,
};


//! Defaut Rending for PTM image.
/*!
  The class defines the default rendering for PTM image.
*/
class DefaultRenderingPtm : public RenderingMode
{

public:
	
	QString getTitle() {return "Default";}
	QWidget* getControl(QWidget* parent) {return new QWidget(parent);}
	bool isLightInteractive() {return true;}
	bool supportRemoteView()  {return true;}
	bool enabledLighting() {return true;}
	
	void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		int offsetBuf = 0;
		const int* coeffPtr = coeff.getLevel(info.level);
		const unsigned char* rgbPtr = rgb.getLevel(info.level);
		int tempW = mipMapSize[info.level].width();
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * tempW + x;
				double lum = evalPoly(&coeffPtr[offset*6], info.light.X(), info.light.Y()) / 255.0;
				for (int i = 0; i < 3; i++)
					buffer[offsetBuf + i] = tobyte(rgbPtr[offset*3 + i] * lum);
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}
	}


	void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
	{
		int offsetBuf = 0;
		const int* redPtr = redCoeff.getLevel(info.level);
		const int* greenPtr = greenCoeff.getLevel(info.level);
		const int* bluePtr = blueCoeff.getLevel(info.level);
		for (int y = info.offy; y < info.offy + info.height; y++)
		{
			for (int x = info.offx; x < info.offx + info.width; x++)
			{
				int offset= y * mipMapSize[info.level].width() + x;
				buffer[offsetBuf + 0] = tobyte(evalPoly(&redPtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 1] = tobyte(evalPoly(&greenPtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 2] = tobyte(evalPoly(&bluePtr[offset*6], info.light.X(), info.light.Y()));
				buffer[offsetBuf + 3] = 255;
				offsetBuf += 4;
			}
		}

	}
};


//! PTM abstract class
class Ptm : public Rti
{
// private data member
protected:
	
	QString version; /*!< Version. */
 	QSize mipMapSize[MIP_MAPPING_LEVELS]; /*!< Size of mip-mapping levels. */

	double scale[6]; /*!< Scale value. */
	int bias[6]; /*!< Bias value. */


public:

	//! Constructor.
	Ptm(): Rti()
	{
		currentRendering = NORMAL;
		// Create list of supported rendering mode.
		list = new QVector<RenderingMode*>();
		list->append(new DefaultRenderingPtm());
		list->append(new DiffuseGain());
		list->append(new SpecularEnhancement());
		list->append(new NormalEnhancement());
		list->append(new UnsharpMasking(0));
		list->append(new UnsharpMasking(1));
		list->append(new CoeffEnhancement());
		list->append(new DetailEnhancement());
		list->append(new DynamicDetailEnh());
	};

	//! Deconstructor.
	virtual ~Ptm(){};

	/*!
	  Returns the correct sub-type of PTM.
	  \param in input stream.
	*/
	static Rti* getPtm(QTextStream &in);


	// protected methods
protected:

	
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

	
	/*!
	  Computes the normal from the coefficients.
	  \param coeff array of six coefficients.
	  \return the normal.
	*/
	vcg::Point3f calculateNormal(const int* coeff)
	{
		double a[6];
		for (int k = 0; k < 6; k++)
			a[k] = coeff[k]/256.0;
		double lx, ly, lz;
		
		if (vcg::math::Abs(4 * a[1] * a[0] - a[2] * a[2]) < zerotol)
		{
			lx = 0.0;
			ly = 0.0;
		}
		else
		{
			if (vcg::math::Abs(a[2]) < zerotol)
			{
				lx = -a[3] / (2.0 * a[0]);
				ly = -a[4] / (2.0 * a[1]);
			}
			else
			{
				lx = (a[2]*a[4] - 2.0*a[1]*a[3])/(4.0*a[0]*a[1] - a[2]*a[2]);
				ly = (a[2]*a[3] - 2.0*a[0]*a[4])/(4.0*a[0]*a[1] - a[2]*a[2]);
			}
		}

		if (vcg::math::Abs(a[0]) < zerotol && vcg::math::Abs(a[1]) < zerotol && vcg::math::Abs(a[2]) < zerotol
			&& vcg::math::Abs(a[3]) < zerotol && vcg::math::Abs(a[4]) < zerotol)
		{
			lx = 0.0;
			ly = 0.0;
			lz = 1.0;
		}
		else
		{
			double length2d = lx * lx + ly * lx;
			int maxfound;
			if (4 * a[0] * a[1] - a[2] * a[2] > zerotol && a[0] < -zerotol)
				maxfound = 1;
			else
				maxfound = 0;
			if (length2d > 1 - zerotol || maxfound == 0) {
				int stat = computeMaximumOnCircle(a, lx, ly);
				if (stat == -1) // failed
				{
					length2d = sqrt(length2d);
					if (length2d > zerotol) {
						lx /= length2d;
						ly /= length2d;
					}
				}
			}
			double disc = 1.0 - lx*lx - ly*ly;
			if (disc < 0.0)
				lz = 0.0;
			else 
				lz = sqrt(disc);
		}
		vcg::Point3f temp(lx, ly, lz);
		temp.Normalize();
		return temp;
	}


	/*!
	  Computes the normals per pixel.
	  \param norm normals pyramid.
	  \param coeff coefficient.
	  \param lrgb flag to discriminate between LRGB and RGB.
	  \param cb callback to update the progress bar.
	  \param offset initial value of the progress bar.
	  \param limit maximum increment for the progress bar value.
	*/
	void calculateNormals(PyramidNormals& norm, const PyramidCoeff& coeff, bool lrgb = true, CallBackPos * cb = 0, int offset = 0, int limit = 0)
	{
		if (cb != NULL)	(*cb)(offset, "Normals generation...");
		for (int level = 0; level < MIP_MAPPING_LEVELS; level++)
		{
			QSize size = mipMapSize[level];
			const int* coeffLevel = coeff.getLevel(level);
			int lenght = size.width()*size.height();
			vcg::Point3f* normalsLevel = new vcg::Point3f[lenght];
			if (!lrgb)
				memcpy(normalsLevel, norm.getLevel(level), sizeof(vcg::Point3f)*lenght);
			for (int j = 0; j < size.height(); j++)
			{
				if (cb != NULL)	(*cb)(offset + level*limit/4.0 + limit/4.0 * j / size.height(), "Normals generation...");
				for (int i = 0; i < size.width(); i++)
				{
					int offset = j * size.width() + i;
					vcg::Point3f temp = calculateNormal(&coeffLevel[offset*6]);
					if (lrgb)
						normalsLevel[offset] = temp;
					else
						normalsLevel[offset] += temp;
				}
			}
			norm.setLevel(normalsLevel, lenght, level);
		}
	}


	/*!
	  Computes a specific mip-mapping level.
	  \param level mip-mapping level to compute.
	  \param width width of the previus level.
	  \param height height of the previus level.
	  \param cb callback to update the progress bar.
	  \param offset initial value of the progress bar.
	  \param limit maximum increment for the progress bar value.
	*/
	void generateMipMap(int level, int width, int height, CallBackPos * cb = 0, int offset = 0, int limit = 0)
	{
		if (level > 3) return;
		int width2 = ceil(width/2.0);
		int height2 = ceil(height/2.0);
		allocateSubLevel(level, width2, height2);
		for (int i = 0; i < height - 1; i+=2)
		{
			if (cb != NULL)	(*cb)(offset + static_cast<double>(i*(limit/2.0)/height), "Mip mapping generation...");
			for (int j = 0; j < width - 1; j+=2)
			{
				int index1 = (i * width + j);
				int index2 = (i * width + j + 1);
				int index3 = ((i + 1) * width + j);
				int index4 = ((i + 1) * width + j + 1);
				int offset = (i/2 * width2 + j/2);
				for (int k = 0; k < 6; k++)
					calculateMipMap(offset, level, index1, index2, index3, index4);
			}
		}
		if (width2 % 2 != 0)
		{
			for (int i = 0; i < height - 1; i+=2)
			{
				int index1 = ((i + 1) * width - 1);
				int index2 = ((i + 2) * width - 1);
				int offset = ((i/2 + 1) * width2 - 1);
				for (int k = 0; k < 6; k++)
					calculateMipMap(offset, level, index1, index2);
			}
		}
		if (height % 2 != 0)
		{
			for (int i = 0; i < width - 1; i+=2)
			{
				int index1 = ((height - 1) * width + i);
				int index2 = ((height - 1) * width + i + 1);
				int offset = ((height2 - 1) * width2 + i/2);
				for (int k = 0; k < 6; k++)
					calculateMipMap(offset, level, index1, index2);
			}
		}
		if (height % 2 != 0 && width % 2 != 0)
		{
			for (int k = 0; k < 6; k++)
				calculateMipMap((height2*width2 - 1), level, (height * width -1));
		}
		mipMapSize[level] = QSize(width2, height2);
		generateMipMap(level+1, width2, height2, cb, offset + limit/2.0, limit/2.0);
	}

// public methods
public:

	/*!
	  Loads the compressed image.
	*/
	virtual int loadCompressed() = 0;

	/*!
	  Loads the specific compressed image.
	  \param name file name.
    */
	virtual int loadCompressed(QString name) = 0;

	/*!
	  Loads the specific compressed tile.
	  \param xinf, yinf, xsup, ysup coordinates of the tile.
	  \param name file name.
	*/
	virtual int loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name) = 0;
	
	/*!
	  Saves the compressed image.
	  \param name file name.
	*/
	virtual int saveCompressed(QString name) = 0;

	/*!
	  Saves a compressed tiles.
	  \param xinf, yinf, xsup, ysup coordinates of the tile.
	  \param reslevel mip-mapping level.
	  \param name file name.
	*/
	virtual int saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name) = 0;
	
private:

	/*!
	  Allocates a specific mip-mapping level.
	  \param level mip-mapping level.
	  \param w width of the level.
	  \param h height of the level.
	*/
	virtual void allocateSubLevel(int level, int w, int h) = 0;
	
	/*!
	  Computes mip-mapping.
	*/
	virtual void calculateMipMap(int pos, int level, int i1) = 0;
	
	/*!
	  Computes mip-mapping.
	*/
	virtual void calculateMipMap(int pos, int level, int i1, int i2) = 0;
	
	/*!
	  Computes mip-mapping.
	*/
	virtual void calculateMipMap(int pos, int level, int i1, int i2, int i3, int i4) = 0;

//accessors
public:

	/*!
	  Sets the version.
	*/
	void setVersion(QString s){version = s;}

};



//! RGB-PTM class
/*!
  The class manages the RGB-PTM image format.
*/
class RGBPtm : public Ptm
{
//private data member
private:

	PyramidCoeff redCoefficients; /*!< Coefficients for red component. */
	PyramidCoeff greenCoefficients; /*!< Coefficients for green component. */
	PyramidCoeff blueCoefficients; /*!< Coefficients for blue component. */

	PyramidNormals normals; /*!< Normals. */

// constructors
public:

	//! Constructor.
	RGBPtm();

	//! Deconstructor.
	virtual ~RGBPtm();


// public methods
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

private:
	virtual void allocateSubLevel(int level, int w, int h);
	virtual void calculateMipMap(int pos, int level, int i1);
	virtual void calculateMipMap(int pos, int level, int i1, int i2);
	virtual void calculateMipMap(int pos, int level, int i1, int i2, int i3, int i4);

};



//! LRGB-PTM class.
/*!
  The class manages the LRGB-PTM image format.
*/
class LRGBPtm : public Ptm
{
//private data member
protected:

	PyramidCoeff coefficients; /*!< Luminance coefficients. */
	PyramidRGB rgb; /*!< RGB components. */
	PyramidNormals normals; /*!< Normals. */

// constructor
public:

	//! Constructor.
	LRGBPtm();

	//! Deconstructor.
	~LRGBPtm();

// public methods
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

private:
	virtual void allocateSubLevel(int level, int w, int h);
	virtual void calculateMipMap(int pos, int level, int i1);
	virtual void calculateMipMap(int pos, int level, int i1, int i2);
	virtual void calculateMipMap(int pos, int level, int i1, int i2, int i3, int i4);

};


//! JPEG LRG-PTM class.
/*!
  The class manages the JPEG LRGB-PTM image format.
*/
class JPEGLRGBPtm : public LRGBPtm
{

public:

	//! Constructor.
	JPEGLRGBPtm();

	//! Deconstructor.
	~JPEGLRGBPtm();

	virtual int load(CallBackPos * cb = 0);
	virtual int load(QString name, CallBackPos * cb = 0);
	virtual int save(QString name);

private:
	
	int indexOf(int x, int* a, int size)
	{
		int answer = -1;
		for (int i = 0; i < size; i++) {
			if (a[i] == x)
				answer = i;
		}
		return answer;
	}

	
	int* combine(int* ref, unsigned char* plane, int size)
	{
		int* result = new int[size];
		for (int i = 0; i < size; i++)
		{
			result[i] = ref[i] + plane[i] - 128;
			if (result[i] < 0)
				result[i] += 256;
		}
		return result;
	}


	int* invert(int* source, int size)
	{
		int* result = new int [size];
		for(int i = 0; i < size; i++)
			result[i] = 255 - source[i];
		return result;
	}

	
	void correctCoeff(int* c, unsigned char* info, int sizeInfo, int w1, int h1)
	{
		for(int i = 0; i < sizeInfo; i+=5)
		{
			int p3 = info[i];
			int p2 = info[i+1];
			int p1 = info[i+2];
			int p0 = info[i+3];
			int v = info[i+4];
			int idx = p3<<24 | p2<<16 | p1<<8 | p0;
			int w2 = idx % w1;
			int h3 = idx / w1;
			int h2 = h1 - h3 - 1;
			int idx2 = h2*w1 + w2;
			c[idx2] = v;
		}
	}

};


#endif  /* PTM_H */

