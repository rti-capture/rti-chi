/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Builder  o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


// Local headers
#include "rti.h"
#include "ptm.h"
#include "zorder.h"
#include <jpeg2000.h>

// Standard headers
#include <iostream>

// Qt headers
#include <qapplication.h>
#include <QDir>
#include <QFileInfo>
#include <QFile>
#include <QImage>
#include <QColor>
#include <QtXml/QDomDocument>

using std::cout;
using std::endl;

int main( int argc, char ** argv )
{
	QApplication app( argc, argv );

	if (argc < 3)
	{
		cout << std::endl << "Usage:" << endl << endl;
		cout << "        rtibuilder <input> <level>" << endl << endl;
		cout << "        <input>  RTI file to decompose (only LRGB-PTM)." << endl;
		cout << "        <level>  Levels of resolution (default: 3)." << endl;
		exit(0);
	}

	// parse arguments
	QString str1(argv[1]);
	QString str2(argv[2]);

	QString filename = str1;
	int level = str2.toInt();

	QFileInfo fi(filename);
	if (fi.suffix() != "ptm")
	{
		cout << "Unsupported file format. The tool accepts only LRGB-PTM file." << endl;
		exit(0);
	}

	// load rti image
	//////////////////////////////////////////////////////////////

	QFile data(filename);
	if (!data.open(QFile::ReadOnly))
	{
		cout << "I/0 error." << endl;
		exit(0);
	}
	QTextStream input(&data);
	Rti *image = Ptm::getPtm(input);
	data.close();
	LRGBPtm *ptm;
	if (dynamic_cast<LRGBPtm*>(image))
	{
		ptm = dynamic_cast<LRGBPtm*>(image);
	}
	else
	{
		cout << "Unsupported file format. The tool accepts only LRGB-PTM file." << endl;
		exit(0);
	}



	//LRGBPtm *ptm = new LRGBPtm();
	ptm->load(filename);

	int width = ptm->width();
	int height = ptm->height();

	// folder creation

	
	QString name = fi.baseName();
	QString pathname = fi.absoluteFilePath();
	QDir dir(fi.absolutePath());

	if (dir.exists(name))
	{
		dir.cd(name);

		// remove all files
		dir.setFilter(QDir::Files);
		QFileInfoList filelist = dir.entryInfoList();
		for (int i = 0; i < filelist.size(); ++i) 
		{
			QFileInfo fileInfo = filelist.at(i);
			QFile f(fileInfo.absoluteFilePath());
			f.remove();
		}

		dir.cdUp();
		dir.rmdir(name);
	}

	dir.mkdir(name);
	dir.cd(name);

	// decomposition and compression
	//////////////////////////////////////////////////////////////

	float aspect_ratio = static_cast<float>(ptm->height()) / static_cast<float>(ptm->width());
	int previewWidth = 400;
	int previewHeight = previewWidth * aspect_ratio;
	QImage *img = ptm->createPreview(previewWidth, previewHeight);
	QString previewname = fi.absolutePath() + QString("/") + name + QString("/") + QString("thumb.jpg");
	img->save(previewname);

	int x1,y1,x2,y2;

	for (int k = 1; k <= level; k++)
	{
		int *zm = ZOrder::createZMatrix(k);
		int size = 1;
		for (int jj=0; jj < k; jj++)
			size <<= 1;

		float deltaW = static_cast<float>(width) / static_cast<float>(size);
		float deltaH = static_cast<float>(height) / static_cast<float>(size);

		for (int i = 0; i < size; i++)
			for (int j = 0; j < size; j++)
			{
				QString patchname = fi.absolutePath() + QString("/") + name + QString("/");
				patchname += QString("tile_lvl%1_%2.ptm").arg(k).arg(zm[j + i * size]);

				cout << patchname.toStdString() << endl;

				x1 = static_cast<int>(deltaW * j);
				y1 = static_cast<int>(deltaH * i);
				x2 = static_cast<int>(deltaW * (j+1));
				y2 = static_cast<int>(deltaH * (i+1));

				cout << "Tile Size: " << (x2-x1) << " x " << (y2-y1) << endl;

				// save compressed tile
				ptm->saveCompressed(x1,y1,x2,y2,level-k,patchname);
			}

		delete [] zm;
	}

	// Save XML information file
	//////////////////////////////////////////////////////////////

	QDomDocument doc;
	QDomElement root = doc.createElement("RTIBuilderInfo");
	doc.appendChild(root);

	QDomElement info = doc.createElement("Info");
	info.setAttribute(QString("width"), QString("%1").arg(width));
	info.setAttribute(QString("height"), QString("%1").arg(height));
	info.setAttribute(QString("levels"), QString("%1").arg(level));
	root.appendChild(info);

	QString infofilename = fi.absolutePath() + QString("/") + name + QString("/") + QString("info.xml");
	QFile infofile(infofilename);
	if (infofile.open(QFile::WriteOnly | QFile::Truncate))
	{
		QTextStream out(&infofile);
		doc.save(out, 2);
	}

	delete ptm;
	delete img;
	return 0;
}
