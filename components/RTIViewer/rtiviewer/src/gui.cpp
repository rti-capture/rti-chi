/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


// Local headers
#include "gui.h"
#include "ui_about.h"
#include "configdlg.h"
#include "loadingdlg.h"
#include "openremotedlg.h"
#include "renderingmode.h"

// Qt headers
#include <QMessageBox>
#include <QHBoxLayout>
#include <QFileDialog>
#include <QUrl>
#include <QDomDocument>
#include <QDomElement>
#include <QDomNode>

#include <vcg/space/point3.h>


RtiViewerDlg::RtiViewerDlg(QWidget *parent/*=0*/):
        QWidget(parent),
	rendDlg(NULL),
	title("RTIViewer 1.0.1 (Beta)"),
	filterStr("All (*.ptm *.hsh *.rti *.mview);;Polynamial Texture Maps (*.ptm);; Hemispherical Harmonics Map (*.hsh);; Universal RTI (*.rti);; Multiview RTI (*.mview)"),
	maxZoom(2)
{
        //Browser
	browserFrame = new QFrame(this);
	browserFrame->setFrameStyle(QFrame::Panel | QFrame::Sunken);
	browserFrame->setLineWidth(3);
	browser = new RtiBrowser(650, 650, NULL, maxZoom, browserFrame);
	QHBoxLayout* browserLayout = new QHBoxLayout;
	browserLayout->setContentsMargins(0, 0, 0, 0);
	browserLayout->addWidget(browser);
	browserFrame->setLayout(browserLayout);
	
	//Navigator
	navFrame = new QFrame(this);
	navFrame->setFrameStyle(QFrame::Panel| QFrame::Sunken);
	navFrame->setLineWidth(3);
	navigator = new Navigator(navFrame, 360, 240, maxZoom);
	QHBoxLayout* navLayout = new QHBoxLayout;
	navLayout->setContentsMargins(0, 0, 0, 0);
	navLayout->addWidget(navigator);
	navFrame->setFixedSize(366, 246);
	navFrame->setLayout(navLayout);
	

	connect(browser, SIGNAL(sizeChanged(int, int)), navigator, SLOT(updateBrowserSize(int, int)));
	connect(browser, SIGNAL(viewChanged(QRectF)), navigator, SLOT(updateSelection(QRectF)));
	connect(navigator, SIGNAL(selectionChanged(QRectF, bool)), browser, SLOT(updateView(QRectF, bool)));
	connect(browser, SIGNAL(updateZoomValue(float, float)), this, SLOT(setZoomValue(float, float)));
	
	//Toolbar
	openLocalAct = new QAction(QIcon(":/images/openlocal.png"),tr("&Open..."), this);
	openRemoteAct = new QAction(QIcon(":/images/openremote.png"), tr("Open remote file"), this);
	aboutAct = new QAction(QIcon(":images/info.png"), tr("Info"), this);
	configAct = new QAction(QIcon(":images/config.png"), tr("Configuration"), this);
	snapshotAct = new QAction(QIcon(":images/snapshot.png"), tr("Snapshot"), this);

	toolBar = new QToolBar(this);
	toolBar ->setOrientation(Qt::Vertical);
	toolBar->setIconSize(QSize(48,48));
	toolBar->addAction(openLocalAct);
	connect(openLocalAct, SIGNAL(triggered()), this, SLOT(open()));
	toolBar->addAction(openRemoteAct);
	connect(openRemoteAct, SIGNAL(triggered()), this, SLOT(openRemote()));
	toolBar->addAction(snapshotAct);
	connect(snapshotAct, SIGNAL(triggered()), browser, SLOT(snapshotActivated()));
	toolBar->addAction(configAct);
	connect(configAct, SIGNAL(triggered()), this, SLOT(configure()));
	toolBar->addAction(aboutAct);
	connect(aboutAct, SIGNAL(triggered()), this, SLOT(about()));
	
	//Light control
	light = new LightControl(this, 180);
	connect(light, SIGNAL(lightChanged(vcg::Point3f, bool)), browser, SLOT(setLight(vcg::Point3f, bool)));
	connect(browser, SIGNAL(setInteractiveLight(bool)), light, SLOT(setInteractive(bool)));
	connect(browser, SIGNAL(setEnabledLight(bool)), light, SLOT(setEnabled(bool)));
	connect(browser, SIGNAL(moveLight(float, float)), light, SLOT(moveLightPosition(float, float)));
	connect(browser, SIGNAL(setLightDir(const vcg::Point3f&, bool)), light, SLOT(setLight(const vcg::Point3f&, bool)));
	
	//Rendering mode widget
	QGroupBox* rendGroup = new QGroupBox("Rendering mode", this);
	rendGroup->setFixedWidth(365);
	rendDlg = new RenderingDialog(NULL, -1, rendGroup);
	QVBoxLayout* rendLayout = new QVBoxLayout;
	rendLayout->setContentsMargins(0, 0, 0, 0);
	rendLayout->addWidget(rendDlg);
	rendGroup->setLayout(rendLayout);
	connect(rendDlg, SIGNAL(renderingModeChanged(int)), browser, SLOT(setRenderingMode(int)));
	connect(rendDlg, SIGNAL(updateImage()), browser, SLOT(updateImage()));

	QGroupBox* infoGroup = new QGroupBox("File info", this);
	infoGroup->setFixedWidth(365);
	QLabel* label1 = new QLabel("File");
	QLabel* label2 = new QLabel("Size");
	QLabel* label3 = new QLabel("Format");
	filename = new QLineEdit(infoGroup);
	filename->setReadOnly(true);
	filesize = new QLineEdit(infoGroup);
	filesize->setReadOnly(true);
	fileformat = new QLineEdit(infoGroup);
	fileformat->setReadOnly(true);
	QGridLayout* infoLayout = new QGridLayout;
	
	infoLayout->addWidget(label1, 0, 0);
	infoLayout->addWidget(filename, 0, 1, 1, 3);
	infoLayout->addWidget(label2, 1, 0);
	infoLayout->addWidget(filesize, 1, 1);
	infoLayout->addWidget(label3, 1, 2);
	infoLayout->addWidget(fileformat, 1, 3);
	infoGroup->setLayout(infoLayout);

	QLabel* label4 = new QLabel("Zoom");
	zoomFact = new QSpinBox(infoGroup);
	zoomFact->setRange(1, maxZoom*100);
	zoomFact->setSuffix("%");
	zoomFact->setValue(100);
	zoomFact->setKeyboardTracking(false);
	zoomFact->setEnabled(false);

	connect(zoomFact, SIGNAL(valueChanged(int)), browser, SLOT(updateZoom(int)));

	QGridLayout* toolLight = new QGridLayout;
	toolLight->setColumnMinimumWidth(1, 240);
	toolLight->addWidget(toolBar, 0, 0, Qt::AlignTop | Qt::AlignLeft );
	toolLight->addWidget(light, 0, 1, Qt::AlignCenter);
	QVBoxLayout* zoomLayout = new QVBoxLayout;
	zoomLayout->addWidget(label4, 0, Qt::AlignHCenter);
	zoomLayout->addWidget(zoomFact, 0, Qt::AlignHCenter);
	toolLight->addLayout(zoomLayout, 0, 2, Qt::AlignVCenter); 

	//Main window layout
	QGridLayout* layout = new QGridLayout;
	layout->setContentsMargins(5, 5, 5 ,5);
	layout->setColumnStretch(0, 5);
	layout->setColumnStretch(1, 0);
		
	layout->setRowStretch(0, 3);
	layout->setRowStretch(1, 2);
	layout->setRowStretch(2, 1);
	layout->setRowStretch(3, 3);
	 
	layout->addWidget(browserFrame, 0, 0, 4, 1);
	layout->addLayout(toolLight, 0, 1, 1, 1, Qt::AlignTop | Qt::AlignHCenter);
	layout->addWidget(rendGroup, 1, 1, 1, 1, Qt::AlignCenter);
	layout->addWidget(infoGroup, 2, 1, 1, 1, Qt::AlignCenter);
	layout->addWidget(navFrame, 3, 1, 1, 1, Qt::AlignBottom | Qt::AlignHCenter);
	
        setLayout(layout);

        // widget attributes
	setWindowState(Qt::WindowMaximized);
	setWindowTitle(title);
	
	//Http thread
	mutex = new QMutex;
	infoReady = new QWaitCondition;
	getter = new HttpThread(*mutex, *infoReady);
	connect(getter, SIGNAL(errorOccurred(QString)), this, SLOT(httpErrorOccurred(QString)), Qt::QueuedConnection);
	connect(getter, SIGNAL(imageUpdated()), browser, SLOT(updateImage()));
	connect(getter, SIGNAL(downloadFinished()), browser, SLOT(downloadFinished()));
	connect(browser, SIGNAL(viewChanged(QRectF)), getter, SIGNAL(viewUpdated(QRectF)));
	connect(browser, SIGNAL(updateRenderingList(int, bool)), rendDlg, SLOT(updateRenderingList(int, bool)));
	getter->start();

	//Application settings
	settings = new QSettings("VCG", "RTIViewer");
	int tempW = settings->value("maxWindowWidth", 2000).toInt();
	int tempH = settings->value("maxWindowHeight", 2000).toInt();
	dir.setPath(settings->value("workingDir", "").toString()); 
	lastUrl.setUrl(settings->value("lastUrl", "").toString());
	browser->setMaxWindowSize(tempW, tempH);

	setAcceptDrops(true);
	
}

int RtiViewerDlg::openFile(QString path)
{
	if (path == "") return -1;
	QFileInfo info(path);
	QFile data(path);
	dir.setPath(info.path());
	settings->setValue("workingDir", dir.path());
	if (info.suffix() != "ptm" && info.suffix() != "hsh" && info.suffix() != "rti" && info.suffix() != "mview")
	{
		QMessageBox::critical(this, tr("Opening error"), tr("The file: \n%1\n is invalid.\n Internal format unknown.").arg(path));
		return 0;
	}
	if (data.open(QFile::ReadOnly))
	{
		Rti* image;
		LoadingDlg* loading;
		if (info.suffix() == "ptm")
		{
			loading = new LoadingDlg(this);
			loading->show();
			QTextStream input(&data);
			image = Ptm::getPtm(input);
			data.close();
			image->setFileName(path);
			QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
		}
		else if (info.suffix() == "hsh")
		{
			loading = new LoadingDlg(this);
			loading->show();
			image = new Hsh();
			image->setFileName(path);
			QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
		}
		else if (info.suffix() == "rti")
		{
			loading = new LoadingDlg(this);
			loading->show();
			image = new UniversalRti();
			image->setFileName(path);
			QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
		}
		else if (info.suffix() == "mview")
		{
			loading = new LoadingDlg(this);
			loading->show();
			image = new MultiviewRti();
			image->setFileName(path);
			QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
		}
		browser->setImage(NULL);
		navigator->setImage(NULL, 0, 0);
		rendDlg->setRenderingMode(NULL, 0);
		filename->setText("");
		filesize->setText("");
		fileformat->setText("");
		if (image->load(LoadingDlg::QCallBack)== 0) //Loads the image info
		{
			zoomFact->setEnabled(true);
			//Sets the browser image
			browser->setImage(image);
			//Sets the navigator image
			navigator->setImage(image->createPreview(360, 240), image->width(), image->height());
			QApplication::restoreOverrideCursor();
			rendDlg->setRenderingMode(browser->getRenderingMode(), browser->getCurrentRendering());
			loading->close();
			//Sets file info
			filename->setText(path);
			filesize->setText(tr("%1 x %2").arg(image->width()).arg(image->height()));
			fileformat->setText(image->typeFormat());
			light->setInteractive(true);
		}
		else
		{
			loading->close();
			QApplication::restoreOverrideCursor();
			QMessageBox::critical(this, tr("Opening error"), tr("The file: \n%1\n is invalid.\n Internal format unknown.").arg(path));
		}
		delete loading;
	}
	return 0;
}


void RtiViewerDlg::configure()
{
	int currentW = settings->value("maxWindowWidth").toInt();
	int currentH = settings->value("maxWindowHeight").toInt();
	//Shows the configuration dialog.
	ConfigDlg* dlg = new ConfigDlg(currentW, currentH, browser->getSize(), this);
	if (dlg->exec() == 1) //User changed the application settings.
	{
		QSize newSize = dlg->getCurrentSize();
		if (newSize.height() != currentH  || newSize.width() != currentW)
		{
			//Saves the new appication settings.
			settings->setValue("maxWindowWidth", newSize.width());
			settings->setValue("maxWindowHeight", newSize.height());
			settings->sync();
			browser->setMaxWindowSize(newSize.width(), newSize.height());
		}
	}
	delete dlg;
}

void RtiViewerDlg::about()
{
	//QDialog *dlg = new QDialog(this);
	AboutDlg *dlg = new AboutDlg(this);
	Ui::aboutDialog aboutdlg;
	aboutdlg.setupUi(dlg);
	dlg->exec();
	delete dlg;
}


int RtiViewerDlg::open()
{
	getter->closeConnection();
	QString prova = dir.path();
	QString path = QFileDialog::getOpenFileName(this, tr("Open File"), dir.path() , filterStr);
	return openFile(path);
}


int RtiViewerDlg::openRemote()
{
	OpenRemoteDlg dlg(lastUrl, this);
	if (dlg.exec() == QDialog::Accepted)
	{
		settings->setValue("lastUrl", lastUrl.toString());
		QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
		QString path = lastUrl.path();
		QUrl* url = new QUrl(lastUrl);
		Rti* imageRti;
		getter->closeConnection();
		QFileInfo pathinfo(path);
		url->setPath(pathinfo.path() + "/");
		getter->setUrl(url, pathinfo.fileName());
		//Gets the xml file
		QBuffer info;
		info.open(QIODevice::ReadWrite);
		getter->getInfo(&info);
		mutex->lock();
		infoReady->wait(mutex);
		mutex->unlock();
		info.close();
		if (getter->checkHttpError()) 
		{
			getter->resetHttpError();
			QApplication::restoreOverrideCursor();
			return -1;
		}
		imageRti = parseXml(&info);
		if (!imageRti)
		{
			QApplication::restoreOverrideCursor();
			QMessageBox::critical(this, "Error", "Remote file contains invalid xml.");
			return -1;
		}
		browser->setImage(NULL);
		navigator->setImage(NULL, 0, 0);
		rendDlg->setRenderingMode(NULL, 0);
		filename->setText("");
		filesize->setText("");
		fileformat->setText("");
		if (imageRti->allocateRemoteImage(&info) != 0)
		{
			QApplication::restoreOverrideCursor();
			QMessageBox::critical(this, "Error", "Remote file contains invalid xml.");
			return -1;
		}
		
		//Gets the thumbnail
		QBuffer thumb;
		thumb.open(QIODevice::ReadWrite);
		getter->getThumb(&thumb);
		mutex->lock();
		infoReady->wait(mutex);
		mutex->unlock();
		thumb.close();
		if (getter->checkHttpError()) 
		{
			delete imageRti;
			QApplication::restoreOverrideCursor();
			getter->resetHttpError();
			return -1;
		}
		QImage* image = new QImage();
		if (!image->loadFromData(thumb.buffer()))
		{
			delete imageRti;
			QApplication::restoreOverrideCursor();
			QMessageBox::critical(this, "Error", "Remote thumb-nail invalid.");
			return -1;
		}
		zoomFact->setEnabled(true);
		//Allocates image
		emit getter->setRti(imageRti);
		browser->setImage(imageRti);
		navigator->setImage(image, imageRti->width(), imageRti->height());
		rendDlg->setRenderingMode(browser->getRenderingMode(), browser->getCurrentRendering(), true);
		filename->setText(lastUrl.toString());
		filesize->setText(tr("%1 x %2").arg(imageRti->width()).arg(imageRti->height()));
		fileformat->setText(imageRti->typeFormat());
		light->setInteractive(true);
		//Gets tiles
		getter->getTiles();
		QApplication::restoreOverrideCursor();
	}
	return 0;
}


Rti* RtiViewerDlg::parseXml(const QBuffer* b)
{
	QDomDocument doc;
	doc.setContent(b->buffer(), false);
	QDomNode root = doc.firstChild();
	QDomElement infoNode = root.firstChildElement("Info");
	if (infoNode.isNull())
		return NULL;
	bool error;
	//type info
	QString type = infoNode.attribute("type");
	if (type.isEmpty())
		return NULL;
	if (type == "LRGB PTM" || type == "JPEG-LRGB PTM")
		return new LRGBPtm();
	else if (type == "HSH")
		return new Hsh();
	return NULL;
}


void RtiViewerDlg::httpErrorOccurred(QString error)
{
	getter->resetHttpError();
	QMessageBox::critical(this, "HTTP Error", error);
}


void RtiViewerDlg::setZoomValue(float value, float minValue)
{
	zoomFact->setMinimum(minValue*100);
	disconnect(zoomFact, SIGNAL(valueChanged(int)), 0, 0);
	zoomFact->setValue(value*100);
	connect(zoomFact, SIGNAL(valueChanged(int)), browser, SLOT(updateZoom(int)));
}
