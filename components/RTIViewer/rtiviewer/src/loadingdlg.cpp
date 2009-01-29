/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#include "loadingdlg.h"
#include <QApplication>

QLabel *LoadingDlg::info = 0;
QProgressBar *LoadingDlg::progress = 0;

LoadingDlg::LoadingDlg(QWidget *parent) : QDialog(parent)
{
	info = new QLabel(this);
	info->setText("Loading...");
	info->setFixedWidth(150);
	progress = new QProgressBar(this);
	progress->setFixedWidth(150);
	progress->setMinimum(0);
	progress->setMaximum(100);
	QHBoxLayout* layout = new QHBoxLayout;
	layout->addWidget(info);
	layout->addWidget(progress);
	setLayout(layout);
	setWindowFlags(Qt::Window | Qt::CustomizeWindowHint | Qt::WindowTitleHint);
	setWindowTitle("Loading...");
	setModal(true);
}


bool LoadingDlg::QCallBack(int pos, QString str)
{
	if(pos==-1) return true;
	info->setText(str);
	progress->show();
	progress->setEnabled(true);
	progress->setValue(pos);
	progress->update();
	info->update();
	QApplication::processEvents();
	return true;
}