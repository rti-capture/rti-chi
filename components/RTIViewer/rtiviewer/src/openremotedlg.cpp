/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#include "openremotedlg.h"

#include <QGridLayout>
#include <QSpacerItem>
#include <QMessageBox>

OpenRemoteDlg::OpenRemoteDlg(QUrl& u, QWidget *parent) : QDialog(parent),
	url(u)
{
	QLabel* info = new QLabel("Insert the url of the file you want to load");
	QLabel* label = new QLabel("URL");
	input = new QLineEdit("http://");
	input->setMinimumWidth(500);
	
	okBtn = new QPushButton("OK");
	okBtn->setDefault(true);
	cancelBtn = new QPushButton("Cancel");
	cancelBtn->setAutoDefault(false);

	buttonBox = new QDialogButtonBox;
	buttonBox->addButton(okBtn, QDialogButtonBox::ActionRole);
	buttonBox->addButton(cancelBtn, QDialogButtonBox::RejectRole);
	
	connect(okBtn, SIGNAL(clicked()), this, SLOT(okPressed()));
	connect(cancelBtn, SIGNAL(clicked()), this, SLOT(close()));

	QSpacerItem* spacer = new QSpacerItem(10, 10, QSizePolicy::Expanding, QSizePolicy::Expanding);
	
	QGridLayout* layout = new QGridLayout;
	layout->addWidget(info, 0, 0, 1, 3, Qt::AlignLeft);
	layout->addWidget(label, 1, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(input, 1, 1, 1, 2);
	layout->addItem(spacer, 2, 1, 1, 1);
	layout->addWidget(buttonBox, 2, 2, 1, 1);
	setLayout(layout);
	
	setModal(true);
	setWindowTitle(tr("HTTP"));
	input->setFocus();
}

void OpenRemoteDlg::okPressed()
{
	QString text = input->text();
	url.setUrl(text, QUrl::StrictMode);
	if (!url.isValid())
		QMessageBox::critical(this, tr("Error"), tr("Invalid Url."));
	else
	{
		QString path = url.path();
		if (!path.endsWith(".ptm", Qt::CaseInsensitive))
			QMessageBox::critical(this, tr("Error"), tr("Unkwon file type. Insert a valid url\n(Example: http://host/filename.ptm)"));
		else
			done(QDialog::Accepted);
	}
}
