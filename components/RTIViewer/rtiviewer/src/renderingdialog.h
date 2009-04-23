/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef RENDERINGDIALOG_H
#define RENDERINGDIALOG_H

#include "renderingMode.h"

#include <QDialog>
#include <QComboBox>
#include <QLabel>

//! Rendering dialog class.
/*!
  The class defines the widget to select the rendering mode and to show the dialog used to set the parameters. 
*/
class RenderingDialog: public QWidget 
{

Q_OBJECT

// private data members
private:
	
	QComboBox* modeList; /*!< Combobox to select the rendering mode. */
	QWidget* control; /*!< Widget to set the parameters of the rendering mode. */
	QVector<RenderingMode*>* list; /*!< List of rendering modes. */

// construction
public:

	//! Constructor.
	/*!
	  \param l list of rendering modes.
	  \param currRendering current rendeing mode.
	  \param parent
	  \param remote flag for remote RTI image.
	*/
	RenderingDialog(QVector<RenderingMode*>* l, int currRendering, QWidget *parent = 0, bool remote = false);


// private Qt slots
private slots:
	
	/*!
	  Invoked when the user changes the rendeing mode.
	*/
	void renderingModeUpdate(int index);

// public Qt slots
public slots:

	/*!
	  Updates the current rendering mode.
	  \param currRendering current rendering mode.
	  \param remote  flag for remote RTI image.
	*/
	void updateRenderingList(int currRendering, bool remote);

//public Qt signal
signals:

	/*!
	  Emitted when the user changes the rendering mode.
	*/
	void renderingModeChanged(int mode);

	/*!
	  Emitted to update the image in the browser.
	*/
	void updateImage();

	/*!
	  Emitted to indicate the finish of the downloading of a remote RTI.
	*/
	void resetRemote();

//accessor
public:
	
	/*!
	  Sets a new rendering mode list.
	*/
	void setRenderingMode(QVector<RenderingMode*>* l, int currRendering, bool remote = false);
	
};

#endif /* RENDERINGDIALOG_H */
