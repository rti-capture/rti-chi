/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef OPENREMOTEDLG_H
#define OPENREMOTEDLG_H

#include <QDialog>
#include <QLabel>
#include <QLineEdit>
#include <QPushButton>
#include <QDialogButtonBox>
#include <QUrl>

//! Remote dialog class.
/*!
  The class defines a dialog to insert the url of a remote RTI image.
*/
class OpenRemoteDlg: public QDialog 
{

Q_OBJECT

// private data members
private:
	
	QLineEdit* input; /*!< Input box. */
	QPushButton* okBtn; /*!< Ok button. */
	QPushButton* cancelBtn; /*!< Cancel button. */
	QDialogButtonBox* buttonBox;

	QUrl& url; /*!< Reference to the url. */

public:

	//! Constructor.
	/*!
	  \param u reference to the url.
	  \param parent
	*/
	OpenRemoteDlg(QUrl& u, QWidget *parent=0);

// public Qt slots
public slots:

	/*!
	  Invoked when the user presses the OK button.
	*/
	void okPressed();
};

#endif /* OPENREMOTEDLG_H */
