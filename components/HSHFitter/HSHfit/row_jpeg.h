
#ifndef _ROW_JPEG_H_
#define _ROW_JPEG_H_

#include "grfmts.h"

#ifdef WIN32

#define XMD_H // prevent redefinition of INT32
#undef FAR  // prevent FAR redefinition

#endif

#if defined WIN32 && defined __GNUC__
typedef unsigned char boolean;
#endif

extern "C" {
#include "jpeglib.h"
}

class RowJpegReader : public GrFmtJpegReader
{
public:
    
	RowJpegReader( const char* filename ) :  GrFmtJpegReader( filename ){};
	~RowJpegReader(){};

	bool StartReading(int color);
	bool ReadRow(uchar* data);
	void FinishReading();
private:
	jpeg_decompress_struct* cinfo;
	int per_color;
	JSAMPARRAY buffer;
};

#endif/*_ROW_JPEG_H_*/

    