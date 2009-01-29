/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Builder  o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef JPEG2000_OPENJPEG_WRAPPER_H
#define JPEG2000_OPENJPEG_WRAPPER_H

// Open JPEG header
#include <openjpeg/openjpeg.h>

// Qt headers
#include <QObject>
#include <QImage>

enum J2_TYPE{
	J2K_CFMT=0,
	JP2_CFMT=1,
	JPT_CFMT=2
};

enum J2_COLOR_SPACE {
	UNKNOWN_CLRSPC = -1,
	SRGB_CLRSPC = 1,		/**< sRGB */
	GRAY_CLRSPC = 2,		/**< grayscale */
	SYCC_CLRSPC = 3			/**< YUV */
};

/** 
 * This class is a wrapper of the Open JPEG library.
 *
 * Reference: http://www.openjpeg.org/  (Open JPEG Library Official Web Site)
 */
class Jpeg2000: public QObject
{
	Q_OBJECT

// private data members
private:

	unsigned char *codestream;          /// code stream data (bytes)
	int codestream_length;              /// code stream length

	opj_image_t *image;                 /// openjpeg image

	opj_cparameters_t encparams;        /// encoder parameters
	opj_dparameters_t decparams;        /// decoder parameters

// constructors
public:

	//! Create an empty JPEG2000 image.
	Jpeg2000();

	//! Create a JPEG2000 image reading an encoded file.
	Jpeg2000(const char *filename);

	//! Create a JPEG2000 image starting from an encoded data stream.
	Jpeg2000(unsigned char *bytestream, int stream_length);

	//! Create a JPEG2000 image starting from raw data components.
	Jpeg2000(int w, int h, int prec, int bpp, int numcomps, int **components,
		J2_COLOR_SPACE color_space, J2_TYPE jpegtype);
	
	~Jpeg2000();


// private methods
private:

	//! Set default decoding/encoding parameters.
	void setDefaults();

	//! Free the memory allocated for the codestream.
	void cleanCodestream();

	//! Free the memory allocated by the Open JPEG image.
	void cleanImage();

	//! Decode the code stream into the Open JPEG image.
	void jpeg2000toImage();

	//! Encode the Open JPEG image.
	void imageToJpeg2000();


// public methods
public:

	bool load(const char *filename);
	bool save(const char *filename);


// accessors
public:

	//! Return the width of the image.
	int width(); 
		
	//! Return the height of the image.
	int height();
	
	//! Return the number of bits x component.
	int precision();

	//! Return the number of components.
	int numComponents();
	
	//! Return the data of the i-th component.
	int * componentData(int index);

	//! Return the data of the (encoded) stream.
	unsigned char *streamData();

	//! Return the length in bytes of the (encoded) stream.
	int streamLength();
};





#endif  /* JPEG2000_OPENJPEG_WRAPPER_H */

