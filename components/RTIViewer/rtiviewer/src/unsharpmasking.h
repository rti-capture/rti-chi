/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef UNSHARPMASKING_H
#define UNSHARPMASKING_H

#include "renderingmode.h"
#include "util.h"

#include <vcg/space/point3.h>

#include <QSlider>


//! Widget for Unsharp Masking settings.
/*!
  The class defines the widget that is showed in the Rendering Dialog to set the parameters of the rendering mode Unsharp Masking.
*/
class UnsharpMControl : public QWidget
{
	Q_OBJECT

private:
	
	QSlider* sliderGain; /*!< Slider to set the gain value. */

public:

	//! Constructor.
	/*!
	  \param gain value for gain parameter.
	  \param parent
	*/
	UnsharpMControl(int gain, QWidget *parent = 0);

signals:

	/*!
	  Emitted when the user changes the gain value.
	*/
	void gainChanged(int value);

};


//! Unsharp Masking class.
/*!
  The class defines the rendering mode Unsharp Masking.
*/
class UnsharpMasking : public QObject, public RenderingMode
{

	Q_OBJECT

private:

	float minGain; /*!< Minimum gain value. */
	float maxGain; /*!< Maximum gain value. */
	float gain; /*!< Current gain value.*/

	int nIter; /*!< Number of iteration for the smooting filter. */

	int type; /*!< Type of unsharp masking: 0 Image Unsharp Masking; 1 Luminance Unsharp Masking. */
	
public:

	//! Constructor.
	/*!
	  \param type type of unsharp masking.
	*/
	UnsharpMasking(int type);

	//! Deconstructor.
	~UnsharpMasking();
	
	virtual QString getTitle();
	virtual QWidget* getControl(QWidget* parent = 0);
	virtual bool isLightInteractive();
	virtual bool supportRemoteView();
	virtual bool enabledLighting();

	virtual void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	virtual void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	void applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer){}

private:

	/*!
	  Computes the enhanced luminance.
	  \param lumMap luminance map.
	  \param width width of the map.
	  \param height height of the map.
	  \param mode special rendering mode.
	*/
        void enhancedLuminance(float* lumMap, int width, int height, int mode = 0);
	
	/*!
	  Returns the dot product between normal and light vector.
	  \param normal normal.
	  \param l light vector.
	*/
        float getLum(const vcg::Point3f& normal, const vcg::Point3f& l);
	
	/*!
	  Transforms from RGB to YUV.
	*/
        void getYUV(float r, float g, float b, float& l, float& u, float& v);
	
	/*!
	  Transform from YUV to RGB.
	*/
        void getRGB(float y, float u, float v, float& r, float& g, float& b);

public slots:

	/*!
	  Sets the gain value.
	*/
	void setGain(int value);

signals:

	/*!
	  Emitted to refresh the image in the browser.
	*/
	void refreshImage();
};

#endif /* UNSHARPMASKING_H */
