/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef RTIBROWSER_H
#define RTIBROWSER_H

#include "rti.h"
#include "renderingmode.h"
#include "util.h"

#include <vcg/space/point3.h>

#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

// Qt headers
#include <QGLWidget>
#include <QShortcut>
#include <QTimer>


//! RTI browser class.
/*!
  The class defines the browser for RTI image.
*/
class RtiBrowser : public QGLWidget
{
	Q_OBJECT


public:
	
	//! Constructors
	/*!
	  \param w width of the browser.
	  \param h height of the browser.
	  \param image RTI image to display.
	  \param maxZ maximum zoom level.
	  \param parent
	*/
	RtiBrowser(int w, int h, Rti *image, int maxZ, QWidget * parent);
	~RtiBrowser();

	/*!
	  Sets the RTI image to display in the browser.
	*/
	void setImage(Rti* rti);

	/*!
	  Returns the browser size.
	*/
	QSize getSize();

	/*!
	  Returns the list of rendering mode of the current RTI image.
	*/
	QVector<RenderingMode*>* getRenderingMode();

	/*!
	  Return the index of the rendering mode applied in the browser.
	*/
	int getCurrentRendering();
	
protected:

	/*!
	  Initialazes the OpenGL environment.
	*/
	void initializeGL();

	/*!
	  Paint event handler.
	*/
	void paintGL();

	/*!
	  Resize event handler.
	*/
	void resizeGL(int width, int height);
	
	/*!
	  Mouse press event handler.
	*/
	void mousePressEvent(QMouseEvent *event);
	
	/*!
	  Mouse move event handler.
	*/
	void mouseMoveEvent(QMouseEvent *event);
	
	/*!
	  Mouse release event handler.
	*/
	void mouseReleaseEvent(QMouseEvent *event);
	
	/*!
	  Mouse double click event handler.
	*/
	void mouseDoubleClickEvent(QMouseEvent *event);
	
	/*!
	  Wheel event handler.
	*/
	void wheelEvent(QWheelEvent * event);
	
// private data member
private:

	Rti* img; /*!< RTI image to display. */
	
	vcg::Point3f light; /*!< Light vector. */
	bool lightChanged; /*!< Holds whether the light direction is changing by the user with a mouse action. */
	float dxLight; /*!< Offset on x-axis of light vector. */
	float dyLight; /*!< Offset on y-axis of light vector. */
	
	QRectF subimg; /*!< Sub-image diplaied in the browser. */
	
	int level; /*!< Mip-mapping level used. */
	
	int _height; /*!< Height of the browser. */
	int _width; /*!< Width of the browser. */

	int maxHeight; /*!< Max browsing window height. */
	int maxWidth; /*!< Max browsing window width. */
	
	int textureHeight; /*!< Height of the texture. */
	int textureWidth; /*!< Width of the texture. */
	unsigned char* textureData; /*!< Texture buffer.  */
	bool isNewTexture; /*!< Holds whether the texture is new. */
	GLuint texName; /*!< Texture name. */

	int viewHeight; /*!< Height of the current view. */
	int viewWidth; /*!< Width of the current view. */

	double zoom; /*!< Current zoom level. */
	double minZoom; /*!< Minimum zoom level. */ 
	const double maxZoom; /*!< Maximum zoom level. */
	QShortcut zoomIn; /*!< Shortcut for zoom in action.  */
	QShortcut zoomOut; /*!< Shortcut for zoom out action. */
	
	QPoint dragPoint; /*!< Last point received from a move event */ 

	bool dragging; /*!< Holds whether the user executes a dragging operation. */
	
	QTimer* timer; /*!< Timer to manage the paint event. */
	
	bool interactive; /*!< Holds whether the browser can update the texture troughout the mouse interaction. If it is false the texture is update only at release event. */
	bool posUpdated; /*!< Holds whether the position of the image in the browser is changed. */ 

	int currentMode; /*!< Current rendering mode applied to the image. */

	// Shortcut for special rendering mode.
	QShortcut defaultMode;
	QShortcut normalsMode;
	QShortcut lumUnsharpMode;
	QShortcut smoothMode;
	QShortcut contrastMode;
	QShortcut enhancedMode;
	QShortcut lumMode;
	QShortcut rgbMode;
	QShortcut lumRMode;
	QShortcut lumGMode;
	QShortcut lumBMode;
	QShortcut a0Mode;
	QShortcut a1Mode;
	QShortcut a2Mode;
	QShortcut a3Mode;
	QShortcut a4Mode;
	QShortcut a5Mode;
	QShortcut lightVectorMode;
	QShortcut lightVectorMode2;
	
// private method
private:
	
	/*!
	  Updates the view size
	*/
	void updateViewSize();

	/*!
	  Updates the view size according to the zoom
	*/
	void updateZoomimg();
	
	/*!
	  Updates the texture info. If \a refresh is true the texture in the browser is updated.
	*/
	void updateTexture(bool refresh = true);

	/*!
	  Moves the sub-image.
	  \param offx, offy offset.
    */
	void updateSubImage(int offx, int offy);


// private Qt slots
private slots:

	// Slots for shortcut.
	void zoomInActivated();
	void zoomOutActivated();
	void defaultModeActivated();
	void normalsModeActivated();
	void lumModeActivated();
	void smoothModeActivated();
	void contrastModeActivated();
	void enhancedModeActivated();
	void lumUnsharpModeActivated();
	void rgbModeActivated();
	void lumRModeActivated();
	void lumGModeActivated();
	void lumBModeActivated();
	void a0ModeActivated();
	void a1ModeActivated();
	void a2ModeActivated();
	void a3ModeActivated();
	void a4ModeActivated();
	void a5ModeActivated();
	void lightVectorModeActivated();
	void lightVectorMode2Activated();
	void fired();

// Qt signal
signals:

	/*!
	  Emitted to keep updated the size of the browser.
	*/
	void sizeChanged(int w, int h);

	/*!
	  Emitted to keep updated the size of the current view.
	*/
	void viewChanged(QRectF rect);

	
	/*!
	  Emitted to permit the interactive updating of the light.
	*/
	void setInteractiveLight(bool value);
	
	/*!
	  Emitted to enable the change of the light direction.
	*/
	void setEnabledLight(bool value);

	/*!
	  Emitted to update the current rendering mode.
	*/
	void updateRenderingList(int currentRendering, bool remote);

	/*!
	  Emitted to move the light direction.
	*/
	void moveLight(float offx, float offy);

	/*!
	  Emitted to set the light direction.
	*/
	void setLightDir(const vcg::Point3f& l);

// public Qt slots
public slots:
	
	/*!
	  Updates the sub-image rectangle.
	*/
	void updateView(QRectF rect);

	/*!
	  Updates the light vector.
	*/
	void setLight(vcg::Point3f l, bool refresh = true); 
	
	/*!
	  Sets the current rendeing mode.
	*/
	void setRenderingMode(int mode);

	/*!
	  Updates the texture in the browser.
	*/
	void updateImage();

	/*!
	  Updates info of a remote RTI image.
	*/
	void downloadFinished();

	/*!
	  Sets the size of the browsing windows.
	*/
	void setMaxWindowSize(int w, int h);

	/*!
	  Saves a snapshot of the image in the current view of the browser.
	*/
	void snapshotActivated();

};


#endif /* RTIBROWER_H */
