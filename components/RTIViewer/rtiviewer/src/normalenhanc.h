/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef NORMALENHANCEMENT_H
#define NORMALENHANCEMENT_H

#include "renderingmode.h"
#include "util.h"

#include <vcg/space/point3.h>

#include <QSlider>


//! Widget for Normal Enhancement settings.
/*!
  The class defines the widget that is showed in the Rendering Dialog to set the parameters of the rendering mode Normal Enhancement.
*/
class NormalEControl : public QWidget
{
	Q_OBJECT

private:
	
	QSlider* sliderGain; /*!< Slider to set the gain value. */
	QSlider* sliderKd; /*!< Slider to set the diffusive constant. */
	QSlider* sliderEnvIll; /*!< Slider to set the ambiental term. */

public:

	//! Constructor.
	/*!
	  \param gain gain value.
	  \param kd diffusive constant value.
	  \param envIll ambiental term value.
	  \param parent
	*/
	NormalEControl(int gain, int kd, int envIll, QWidget *parent = 0);

signals:

	/*!
	  Emitted when the user changes the gain value.
	*/
	void gainChanged(int value);

	/*!
	  Emitted when the user changes the diffusive constant value.
	*/
	void kdChanged(int value);

	/*!
	  Emitted when the user changes the ambient term value.
	*/
	void envIllChanged(int value);

};


//! Normal Enhancement class.
/*!
  The class defines the rendering mode Normal Enhancement.
*/
class NormalEnhancement : public QObject, public RenderingMode
{

	Q_OBJECT

private:

	float minGain; /*!< Minimum gain value. */
	float maxGain; /*!< Maximum gain value. */
	float gain; /*!< Current gain value. */

	float minKd; /*!< Mimimun diffusive constant value. */
	float maxKd; /*!< Maximum diffusive constant value. */
	float kd; /*!< Current diffusive constant value. */

	float minEnvIll; /*!< Minimum ambiental term value. */
	float maxEnvIll; /*!< Maximum ambiental term value. */
	float envIll; /*!< Current ambiental term value.*/

	int nIter; /*!< Number of iteration for the smoothing. */

	PyramidNormals normalsL; /*!< Smoothed normals. */

	bool smooted; /*!< Holds whether the smoothed normals is already computed. */

	QWidget* loadParent; /*!< Parent for the loading window. */

public:

	//! Constructor.
	NormalEnhancement();

	//! Deconstructor.
	~NormalEnhancement();
	
	virtual QString getTitle();
	virtual QWidget* getControl(QWidget* parent);
	virtual bool isLightInteractive();
	virtual bool supportRemoteView();
	virtual bool enabledLighting();

	virtual void applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);
	
	virtual void applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer);

	void applyHSH(const PyramidCoeffF& redCoeff, const PyramidCoeffF& greenCoeff, const PyramidCoeffF& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer){}

private:

	/*!
	  Computes the smoothed normals.
	  \param normals original normals.
	  \param mipMapSize size of mip-mapping levels.
	*/
	void calcSmooting(const PyramidNormals& normals, const QSize* mipMapSize);
	
	/*!
	  Computes the illumination model defined as: kd(Ne*light) + envIll
	  with the enhanced normal Ne equals to: Ne = N + k (N - Nl).
	  \param normal original normal.
	  \param normalL smoothed normal.
	  \param light light vector.
	*/
	double applyModel(const vcg::Point3f& normal, const vcg::Point3f& normalL, const vcg::Point3f& light);

	/*!
	  Computes the contrast signal N - Nl.
	  \param normal original normal.
	  \param normalL smoothed normal.
	*/
	vcg::Point3f getContrastNormal(const vcg::Point3f& normal, const vcg::Point3f& normalL);
	
	/*!
	  Computes the enhanced normal.
	  \param normal original normal.
	  \param normalL smoothed normal.
	*/
	vcg::Point3f getEnhancedNormal(const vcg::Point3f& normal, const vcg::Point3f& normalL);

public slots:

	/*!
	  Sets the gain value.
	*/
	void setGain(int value);

	/*!
	  Sets the diffusive constant value.
	*/
	void setKd(int value);

	/*!
	  Sets the ambiental term value.
	*/
	void setEnvIll(int value);

signals:

	/*!
	  Emitted to refresh the image in the browser.
	*/
	void refreshImage();
};

#endif /* NORMALENHANCEMENT_H */
