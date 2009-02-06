/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#ifndef _USE_MATH_DEFINES
#define _USE_MATH_DEFINES
#include <cmath>
#endif

#include <QApplication>
#include <QLabel>
#include <QGridLayout>
# include <QPainter>

#include "dyndetailenhanc.h"

#include "../../rtibuilder/src/zorder.h"


DynamicDetailEControl::DynamicDetailEControl(unsigned int tileSize, unsigned int offset, QWidget *parent) 
:	QWidget(parent),
	config(NULL)
{
	QLabel* label1 = new QLabel("Tile size (px)");
	QLabel* label2 = new QLabel("Offset (1-20°)");

	tileSizeCmb = new QComboBox(this);
	tileSizeCmb->setDuplicatesEnabled(false);
	int v = 8;
	for (int i = 0; v*i <= MAX_TILE_SIZE; i++)
		tileSizeCmb->addItem(tr("%1").arg(v*i), QVariant(v*i));
	tileSize /= 8;
	tileSize *= 8;
	tileSizeCmb->setCurrentIndex(tileSizeCmb->findData(QVariant(tileSize)));
	connect(tileSizeCmb, SIGNAL(activated(int)), this, SLOT(changeTileSize(int)));

	offsetCmb = new QSpinBox(this);
	offsetCmb->setRange(MIN_OFFSET, MAX_OFFSET);
	offsetCmb->setSuffix(" °");
	offsetCmb->setValue(offset);
	offsetCmb->setKeyboardTracking(false);
	connect(offsetCmb, SIGNAL(valueChanged(int)), this, SIGNAL(offsetChanged(int)));

	advancedBtn = new QPushButton("Advanced Settings", this);
	advancedBtn->setFixedSize(170, 20);
	connect(advancedBtn, SIGNAL(clicked()), this, SLOT(showAdvancedSettings()));
	
	QGridLayout* layout = new QGridLayout(this);
	layout->addWidget(label1, 0, 0, 1, 1);
	layout->addWidget(label2, 1, 0, 1, 1);
	layout->addWidget(tileSizeCmb, 0, 1, 1, 1);
	layout->addWidget(offsetCmb, 1, 1, 1, 1);
	layout->addWidget(advancedBtn, 2, 1, 1, 1);
	layout->setContentsMargins(5, 5, 5, 0);
	setLayout(layout);
}

DynamicDetailEControl::~DynamicDetailEControl()
{
	if (config != NULL)
		delete config;
}

void DynamicDetailEControl::setAdvacedDlg(QDialog *dlg)
{
	config = (DynamicDetConfDlg*) dlg;
}

void DynamicDetailEControl::changeTileSize(int s)
{
	int value = tileSizeCmb->itemData(s).toInt();
	emit tileSizeChanged(value);
}


void DynamicDetailEControl::showAdvancedSettings()
{
	if (config != NULL)
		config->show();
}

DynamicDetailEnh::DynamicDetailEnh():
	degreeOffset(10),
	tileSize(32),
	sharpnessOp(DYN_MAX_ENERGY_LAPLACE),
	sphereSampl(DYN_NON_UNIFORM),
	k1(0.7f),
	k2(0.3f),
	threshold(0.7f),
	filter(DYN_3x3),
	nIterFilter(2)
{

}

DynamicDetailEnh::~DynamicDetailEnh()
{

}


QString DynamicDetailEnh::getTitle() 
{
	return "Dynamic Detail Enhancement";
}


QWidget* DynamicDetailEnh::getControl(QWidget* parent)
{
	DynamicDetConfDlg* advancedControl = new DynamicDetConfDlg(parent);
	advancedControl->setCurrentValue(sharpnessOp, sphereSampl, k1, k2, threshold, filter, nIterFilter);
	DynamicDetailEControl* control = new DynamicDetailEControl(tileSize, degreeOffset, parent);
	control->setAdvacedDlg(advancedControl);
	connect(control, SIGNAL(tileSizeChanged(int)), this, SLOT(setTileSize(int)));
	connect(control, SIGNAL(offsetChanged(int)), this, SLOT(setOffset(int)));
	connect(advancedControl, SIGNAL(configUpdated(SharpnessMeasuresDyn, SphereSamplingDyn, float, float, float, SmoothingFilterDyn, int)),
			this, SLOT(updateConfig(SharpnessMeasuresDyn, SphereSamplingDyn, float, float, float, SmoothingFilterDyn, int)));
	disconnect(this, SIGNAL(refreshImage()), 0, 0);
	connect(this, SIGNAL(refreshImage()), parent, SIGNAL(updateImage()));
	return control;
}


bool DynamicDetailEnh::isLightInteractive()
{
	return false;
}


bool DynamicDetailEnh::supportRemoteView()
{
	return false;
}


bool DynamicDetailEnh::enabledLighting()
{
	return true;
}


void DynamicDetailEnh::applyPtmLRGB(const PyramidCoeff& coeff, const PyramidRGB& rgb, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int m;
	switch(info.mode)
	{
		case LIGHT_VECTOR: m = 1; break;
		case LIGHT_VECTOR2: m = 2; break;
		default: m = 0;
	}
	
	QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
	bufferPtr = buffer;
	coefficient = coeff.getLevel(info.level);
	color = rgb.getLevel(info.level);
	w = info.width;
	h = info.height;
	lrgb = true;
	drawingMode = m; 
	calcDetails(info, mipMapSize[info.level].width());
	QApplication::restoreOverrideCursor();
}


void DynamicDetailEnh::applyPtmRGB(const PyramidCoeff& redCoeff, const PyramidCoeff& greenCoeff, const PyramidCoeff& blueCoeff, const QSize* mipMapSize, const PyramidNormals& normals, const RenderingInfo& info, unsigned char* buffer)
{
	int m;
	switch(info.mode)
	{
		case LIGHT_VECTOR: m = 1; break;
		case LIGHT_VECTOR2: m = 2; break;
		default: m = 0;
	}
	
	QApplication::setOverrideCursor(QCursor(Qt::WaitCursor));
	bufferPtr = buffer;
	red = redCoeff.getLevel(info.level);
	green = greenCoeff.getLevel(info.level);
	blue = blueCoeff.getLevel(info.level);
	w = info.width;
	h = info.height;
	lrgb = false;
	drawingMode = m; 
	calcDetails(info, mipMapSize[info.level].width());
	QApplication::restoreOverrideCursor();
	
}


void DynamicDetailEnh::calcDetails(RenderingInfo info, int levelWidth)
{
	int deltaW, deltaH, ni, nj;
	// Computes the size of the grid of tiles.
	if (tileSize == 0)
	{
		ni = 1;
		nj = 1;
		deltaW = info.width;
		deltaH = info.height;
	}
	else
	{ 
		ni = ceil(1.0 * info.width / (tileSize + 1));
		nj = ceil(1.0 * info.height / (tileSize + 1));
		deltaW = tileSize + 1;
		deltaH = tileSize + 1;
	}
	std::vector<vcg::Point3f>* samples = getLightSamples(info.light);
	lights.empty();
	lights = std::vector<vcg::Point3f>(ni*nj);
	std::vector<std::vector<vcg::Point3f>*> tempLight(ni*nj);
	// Selects the better light vectors for each tile.
	for (int j = 0; j < nj; j++)
	{
		for (int i = 0; i < ni; i++)
		{
			int x0 = i*deltaW;
			int y0 = j*deltaH;
			if (y0 + deltaH > info.height)
				tempLight[j*ni + i] = tempLight[(j-1)*ni +i];
			else if (x0 + deltaW > info.width)
				tempLight[j*ni + i] = tempLight[j*ni + i - 1];
			else
				tempLight[j*ni + i] = getBestLight(x0 + info.offx , y0 + info.offy, deltaW, deltaH, levelWidth, *samples);
		}
	}
	// Computes a global smoothing.
	calcLocalLight(tempLight, lights, ni, nj);
	// Applie the final smothing filter.
	calcSmoothing(lights, ni, nj);
	QImage vectImage;
	if (drawingMode != 0)
	{
		// Draws the light vectors selected for each tile.
		vectImage = QImage(info.width, info.height,  QImage::Format_ARGB32);
		int value = qRgba(255, 255, 255, 0);
		vectImage.fill(value);
		QPainter painter(&vectImage);
		painter.setRenderHint(QPainter::NonCosmeticDefaultPen);
		QPen pen(QColor(255, 0, 0));
		pen.setWidth(1);
		painter.setPen(pen);
		if (drawingMode == 1)
		{
			for (int j = 0; j < nj - 1; j++)
			{
				for (int i = 0; i < ni - 1; i++)
				{
					vcg::Point3f light = lights[j*ni + i];
					vcg::Point2f center = vcg::Point2f(i*deltaW + deltaW/2, j*deltaH + deltaH/2);
					int xEnd, yEnd;
					xEnd = center.X() + tileSize*light.X();
					yEnd = center.Y() - tileSize*light.Y();
					painter.drawLine(center.X(), center.Y(), xEnd, yEnd);
					painter.drawEllipse(QPoint(center.X(), center.Y()), 1, 1);
				}
			}
		}
		else if (drawingMode == 2)
		{
			for(unsigned int i = 0; i < 9; i++)
			{
				vcg::Point2f center = vcg::Point2f(info.width/2, info.height/2);
				int xEnd, yEnd;
				xEnd = center.X() + (info.width/2)*samples->at(i).X();
				yEnd = center.Y() - (info.height/2)*samples->at(i).Y();
				painter.drawLine(center.X(), center.Y(), xEnd, yEnd);
				painter.drawEllipse(QPoint(center.X(), center.Y()), 1, 1);
			}

		}
	}
	// Creates the output texture.
	if (lrgb)
	{
		for (int j = 0; j < info.height ; j++)
		{
			for(int i = 0; i < info.width; i++)
			{
				int offset = j*info.width + i;
				int offset2 = (j + info.offy)*levelWidth + (i + info.offx);
				vcg::Point3f l = getLight(i, j, deltaW, deltaH, ni, nj);
				double lum = evalPoly(&coefficient[offset2*6], l.X(), l.Y()) / 255.0;
				if (drawingMode == 0)
				{
					for (int k = 0; k < 3; k++)
						bufferPtr[offset*4 + k] = tobyte(lum*color[offset2*3 +k]);
				}
				else
				{
					QRgb rgb = vectImage.pixel(i, j);
					if (qAlpha(rgb) != 0)
					{
						int red = qRed(rgb);
						int green = qGreen(rgb);
						int blue = qBlue(rgb);
						bufferPtr[offset*4] = red;
						bufferPtr[offset*4 + 1] = green;
						bufferPtr[offset*4 + 2] = blue;
					}
					else
					{
						for (int k = 0; k < 3; k++)
							bufferPtr[offset*4 + k] = tobyte(lum*color[offset2*3 +k]);
					}
				}
				bufferPtr[offset*4 + 3] = 255;
			}
		}
	}
	else
	{
		for (int j = 0; j < info.height ; j++)
		{
			for(int i = 0; i < info.width; i++)
			{
				int offset = j*info.width + i;
				int offset2 = (j + info.offy)*levelWidth + (i + info.offx);
				vcg::Point3f l = getLight(i, j, deltaW, deltaH, ni, nj);
				if (drawingMode == 0)
				{
					bufferPtr[offset*4] = tobyte(evalPoly(&red[offset2*6], l.X(), l.Y()));
					bufferPtr[offset*4 + 1] = tobyte(evalPoly(&green[offset2*6], l.X(), l.Y()));
					bufferPtr[offset*4 + 2] = tobyte(evalPoly(&blue[offset2*6], l.X(), l.Y()));
				}
				else
				{
					QRgb rgb = vectImage.pixel(i, j);
					if (qAlpha(rgb) != 0)
					{
						int red = qRed(rgb);
						int green = qGreen(rgb);
						int blue = qBlue(rgb);
						bufferPtr[offset*4] = red;
						bufferPtr[offset*4 + 1] = green;
						bufferPtr[offset*4 + 2] = blue;
					}
					else
					{
						bufferPtr[offset*4] = tobyte(evalPoly(&red[offset2*6], l.X(), l.Y()));
						bufferPtr[offset*4 + 1] = tobyte(evalPoly(&green[offset2*6], l.X(), l.Y()));
						bufferPtr[offset*4 + 2] = tobyte(evalPoly(&blue[offset2*6], l.X(), l.Y()));
					}
				}
				bufferPtr[offset*4 + 3] = 255;
			}
		}
	}
	delete samples;
	for (int j = 0; j < nj - 1; j++)
		for (int i = 0; i < ni - 1; i++)
			delete tempLight[j*ni + i];
}


std::vector<vcg::Point3f>* DynamicDetailEnh::getLightSamples(const vcg::Point3f& base)
{
	std::vector<vcg::Point3f>* coneDirVec = new std::vector<vcg::Point3f>;
	if (sphereSampl == DYN_NON_UNIFORM)
	{
		// Anisotropic sampling.
		float sinb = sin(vcg::math::ToRad(static_cast<float>(degreeOffset)));
		vcg::Point3f baseDir, baseN, baseTan, baseDirS, baseTanS;
		baseDir = base;
		baseDir.Z() = 0;
		if (vcg::math::Abs(baseDir.X()) < 0.00001 && vcg::math::Abs(baseDir.Y()) < 0.00001)
			baseN = vcg::Point3f(1,0,0);
		else
		{
			baseN = baseDir;
			baseN.Normalize();
		}
		baseTan = vcg::Point3f(baseN.Y(), -baseN.X(), baseN.Z());
		baseDirS = baseN * sinb * base.Z();
		baseTanS = baseTan * sinb;
		for (int i = -1; i <= 1; i++)
		{
			for (int j = -1; j <= 1; j++)
			{
				vcg::Point3f temp = baseDir + baseDirS*i + baseTanS*j;
				double x2 = temp.X();
				x2 *= x2;
				double y2 = temp.Y();
				y2 *= y2;
				if (x2 + y2 > 0.999)
					temp.Z() = 0.0;
				else
					temp.Z() = vcg::math::Sqrt(1 - x2  - y2);
				temp.Normalize();
				coneDirVec->push_back(temp);
			}
		}
	}
	else if (sphereSampl == DYN_UNIFORM)
	{
		// Isotropic sampling.
		vcg::Point3f n1, n2;
		if (base == vcg::Point3f(0, 1, 0) || base == vcg::Point3f(0, -1, 0))
			n1 = vcg::Point3f(1, 0, 0);
		else
			n1 = vcg::Point3f(0, 1, 0)^base;
		vcg::Matrix33f matrixAx, matrixAy, matrix;
		matrix.SetRotateDeg(90.0, base);
		n2 = matrix * n1;
		vcg::Point3f Ax, Ay, deltaX1, deltaY1, deltaX2, deltaY2;
		matrixAx.SetRotateDeg(degreeOffset, n1);
		matrixAy.SetRotateDeg(degreeOffset, n2);
		Ax = matrixAx*base;
		Ay = matrixAy*base;
		coneDirVec->push_back(Ax.Normalize());
		coneDirVec->push_back(Ay.Normalize());
		deltaX1 = base - Ax;
		deltaY1 = base - Ay;
		matrixAx.SetRotateDeg(-degreeOffset, n1);
		matrixAy.SetRotateDeg(-degreeOffset, n2);
		Ax = matrixAx*base;
		Ay = matrixAy*base;
		coneDirVec->push_back(Ax.Normalize());
		coneDirVec->push_back(Ay.Normalize());
		deltaX2 = base - Ax;
		deltaY2 = base - Ay;
		vcg::Point3f t1, t2;
		float k = static_cast<float>(M_SQRT2/2);
		coneDirVec->push_back((base - deltaX1*k - deltaY1*k).Normalize());
		coneDirVec->push_back((base - deltaX1*k - deltaY2*k).Normalize());
		coneDirVec->push_back((base - deltaX2*k - deltaY1*k).Normalize());
		coneDirVec->push_back((base - deltaX2*k - deltaY2*k).Normalize());
		coneDirVec->push_back(base);
	}
	return coneDirVec;
}


std::vector<vcg::Point3f>* DynamicDetailEnh::getBestLight(int x, int y, int tileW, int tileH, int width, const std::vector<vcg::Point3f>& lightSamples)
{
	double gradient[9];
	double lightness[9];
	int maxGrad = 0;
	int maxL = 0;
	unsigned char rgb[3];
	double max = 0;
	int index = 0;
	std::vector<vcg::Point3f>* vector = new std::vector<vcg::Point3f>();
	for(int k = 0; k < 9; k++)
	{
		gradient[k] = 0;
		lightness[k] = 0;
		if (lightSamples[k].Z() != -1)
		{
			int* image = new int[tileW*tileH];
			int offsetBuf = 0;
			// Creates a image from the tile.
			if (lrgb)
			{
				for(int j = y; j < y + tileH; j++)
				{
					for(int i = x; i < x + tileW; i++)
					{
						int offset = j*width + i;
						double lum = evalPoly(&coefficient[offset*6], lightSamples[k].X(), lightSamples[k].Y()) / 255.0;
						rgb[0] = tobyte(lum * color[offset*3]);
						rgb[1] = tobyte(lum * color[offset*3 + 1]);
						rgb[2] = tobyte(lum * color[offset*3 + 1]);
						lightness[k] += 0.299*rgb[0] + 0.587*rgb[1] + 0.114*rgb[2];
						image[offsetBuf] = 0x0 |(rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
						offsetBuf++;
					}
				}
			}
			else
			{
				for(int j = y; j < y + tileH; j++)
				{
					for(int i = x; i < x + tileW; i++)
					{
						int offset = j*width + i;
						rgb[0] = tobyte(evalPoly(&red[offset*6], lightSamples[k].X(), lightSamples[k].Y()));
						rgb[1] = tobyte(evalPoly(&green[offset*6], lightSamples[k].X(), lightSamples[k].Y()));
						rgb[2] = tobyte(evalPoly(&blue[offset*6], lightSamples[k].X(), lightSamples[k].Y()));
						lightness[k] += 0.299*rgb[0] + 0.587*rgb[1] + 0.114*rgb[2];
						image[offsetBuf] = 0x0 |(rgb[0] << 16) | (rgb[1] << 8) | rgb[2];
						offsetBuf++;
					}
				}

			}
			// Computes the sharpness operator on the image.
			gradient[k] = computeSharpOperator(image, tileW, tileH);
			if (gradient[k] > gradient[maxGrad])
				maxGrad = k;
			if (lightness[k] > lightness[maxL])
				maxL = k;
			delete[] image;
		}
	}
	// Selects the light vector with an enhancement measure greater than a threshold.
	double value[9];
	for (int k = 0; k < 9; k++)
	{
		value[k] = k1*gradient[k]/gradient[maxGrad]+ k2*lightness[k]/lightness[maxL];
		if (value[k] > max)
		{
			max = value[k];
			index = k;
		}
	}
	vector->push_back(lightSamples[index]);
	double limit = threshold*value[index];
	for (int k = 0; k < 9; k++)
	{
		if (k != index && value[k] > limit)
			vector->push_back(lightSamples[k]);
	}
	return vector;
}


double DynamicDetailEnh::computeSharpOperator(int* image, int width, int height)
{
	double gradient = 0;
	if (sharpnessOp == DYN_MAX_LAPLACE || sharpnessOp == DYN_MAX_ENERGY_LAPLACE)
	{
		for(int i = 1; i < width - 1; i+=2)
		{
			for(int j = 1; j < height - 1; j+=2)
			{
				double g = 0;
				// Computes Laplace operator.
				g += image[(j-1)*width + i - 1];
				g += 4*image[(j-1)*width + i];
				g += image[(j-1)*width + i + 1];
				g += 4*image[j*width + i - 1];
				g += -20 * image[j*width + i];
				g += 4*image[j*width + i + 1];
				g += image[(j+1)*width + i - 1];
				g += 4*image[(j+1)*width + i ];
				g += image[(j+1)*width + i + 1];
				
				if (sharpnessOp == DYN_MAX_LAPLACE && g > gradient)
					gradient = g;
				else if (sharpnessOp == DYN_MAX_ENERGY_LAPLACE)
					gradient += g*g;
			} 
		}
	}
	else if (sharpnessOp == DYN_NORM_L1_SOBEL || sharpnessOp == DYN_NORM_L2_SOBEL)
	{
		for(int i = 1; i < width - 1; i++)
		{
			for(int j = 1; j < height - 1; j++)
			{
				double gx = 0, gy = 0;
				
				// Computes Sobel operator.
				gy += 1 * image[(j-1)*width + i - 1];
				gy += 2 * image[(j-1)*width + i];
				gy += 1 * image[(j-1)*width + i + 1];
				gy += -1 *image[(j+1)*width + i - 1];
				gy += -2 * image[(j+1)*width + i ];
				gy += -1 * image[(j+1)*width + i + 1];

				gx += -1 * image[(j-1)*width + i - 1];
				gx += 1 * image[(j-1)*width + i + 1];
				gx += -2 * image[j*width + i - 1];
				gx += 2 * image[j*width + i + 1];
				gx += -1 *image[(j+1)*width + i - 1];
				gx += 1 * image[(j+1)*width + i + 1];
				
				if (sharpnessOp == DYN_NORM_L1_SOBEL)
				{
					gx = vcg::math::Abs(gx);
					gy = vcg::math::Abs(gy);
					gradient += (gx + gy);
				}
				else if (sharpnessOp == DYN_NORM_L2_SOBEL)
				{
					gx *= gx;
					gy *= gy;
					gradient += (gx + gy);
				}
			} 
		}
	}
	return gradient;
}


vcg::Point3f DynamicDetailEnh::getLight(int x, int y, int width, int height, int nx, int ny)
{
	int ytile = y / height;
	int xtile = x / width;
	
	vcg::Point3f l1, l2, l3, l4, final;
	vcg::Point2f v1, v2, v3, v4;
	int id1, id2, id3, id4, id5, id6, id7, id8, id9;
	
	//Computes light vector as linear interpolation of nine neighbouring tiles.

	if (ytile > 0 && ytile < ny - 1 && xtile > 0 && xtile < nx -1)
	{
		id1 = ytile*nx + xtile;
		id3 = id1 - nx;
		id2 = id3 - 1;
		id4 = id3 + 1;
		id5 = id1 - 1;
		id6 = id1 + 1;
		id8 = id1 + nx;
		id7 = id8 - 1;
		id9 = id8 + 1;
		l1 = (lights[id2] + lights[id3] + lights[id5] + lights[id1])/4;
		l2 = (lights[id3] + lights[id4] + lights[id6] + lights[id1])/4;
		l3 = (lights[id5] + lights[id1] + lights[id7] + lights[id8])/4;
		l4 = (lights[id1] + lights[id6] + lights[id8] + lights[id9])/4;
		v1 = vcg::Point2f(width*xtile, height*ytile);
		v2 = v1 + vcg::Point2f(width, 0);
		v3 = v1 + vcg::Point2f(0, height);
		v4 = v1 + vcg::Point2f(width, height);
	}
	else if (ytile == 0 && xtile == 0)
	{
		id1 = 0;
		id2 = 1;
		id3 = nx;
		id4 = nx + 1;
		l1 = lights[id1];
		l2 = (lights[id1] + lights[id2])/2;
		l3 = (lights[id1] + lights[id3])/2;
		l4 = (lights[id1] + lights[id2] + lights[id3] + lights[id4])/4; 
		v1 = vcg::Point2f(0, 0);
		v2 = vcg::Point2f(width, 0);
		v3 = vcg::Point2f(0, height);
		v4 = vcg::Point2f(width, height);
	}
	else if (ytile == 0 && xtile == nx - 1)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - 1;
		id4 = id1 + nx; 
		id3 = id4 - 1;
		l1 = (lights[id1] + lights[id2])/2;
		l2 = lights[id1];
		l4 = (lights[id1] + lights[id4])/2;
		l3 = (lights[id1] + lights[id2] + lights[id3] + lights[id4])/4; 
		v2 = vcg::Point2f(w - 1, 0);
		v1 = vcg::Point2f(width*xtile,0);
		v3 = v1 + vcg::Point2f(0, height);
		v4 = v2 + vcg::Point2f(0, height);
	}
	else if (ytile == ny - 1 && xtile == 0)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - nx;
		id3 = id2 + 1;
		id4 = id1 + 1;
		l3 = lights[id1];
		l1 = (lights[id1] + lights[id2])/2;
		l4 = (lights[id1] + lights[id4])/2;
		l3 = (lights[id1] + lights[id2] + lights[id3] + lights[id4])/4; 
		v3 = vcg::Point2f(0, h - 1);
		v1 = vcg::Point2f(0, height*ytile);
		v2 = v1 + vcg::Point2f(width, 0);
		v4 = v3 + vcg::Point2f(width, 0);
	}
	else if (ytile == ny - 1 && xtile == nx - 1)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - nx;
		id3 = id2 - 1;
		id4 = id1 - 1;
		l4 = lights[id1];
		l2 = (lights[id1] + lights[id2])/2;
		l3 = (lights[id1] + lights[id4])/2;
		l1 = (lights[id1] + lights[id2] + lights[id3] + lights[id4])/4; 
		v4 = vcg::Point2f(w - 1, h - 1);
		v1 = vcg::Point2f(width*xtile, height*ytile);
		v2 = vcg::Point2f(w - 1, height*ytile);
		v3 = vcg::Point2f(width*xtile, h - 1);
	}
	else if (ytile == 0)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - 1;
		id3 = id1 + 1;
		id5 = id1 + nx;
		id4 = id5 - 1;
		id6 = id5 + 1;
		l1 = (lights[id1] + lights[id2])/2;
		l2 = (lights[id1] + lights[id3])/3;
		l3 = (lights[id1] + lights[id2] + lights[id4] + lights[id5])/4; 
		l4 = (lights[id1] + lights[id3] + lights[id5] + lights[id6])/4; 
		v1 = vcg::Point2f(width*xtile, 0);
		v2 = v1 + vcg::Point2f(width, 0);
		v3 = v1 + vcg::Point2f(0, height);
		v4 = v1 + vcg::Point2f(width, height);
	}
	else if (ytile == ny - 1)
	{
		id1 = ytile*nx + xtile;
		id3 = id1 - nx;
		id2 = id3 - 1;
		id4 = id3 + 1;
		id5 = id1 - 1;
		id6 = id1 + 1;
		l3 = (lights[id1] + lights[id5])/2;
		l4 = (lights[id1] + lights[id6])/3;
		l1 = (lights[id1] + lights[id2] + lights[id3] + lights[id5])/4; 
		l2 = (lights[id1] + lights[id3] + lights[id4] + lights[id6])/4; 
		v1 = vcg::Point2f(width*xtile, height*ytile);
		v2 = v1 + vcg::Point2f(width, 0);
		v3 = vcg::Point2f(width*xtile, w - 1);
		v4 = v3 + vcg::Point2f(width, 0);
	}
	else if (xtile == 0)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - nx;
		id3 = id2 + 1;
		id4 = id1 + 1;
		id6 = id1 + nx;
		id5 = id6 + 1;
		l1 = (lights[id1] + lights[id2])/2;
		l3 = (lights[id1] + lights[id6])/3;
		l2 = (lights[id1] + lights[id2] + lights[id4] + lights[id3])/4; 
		l4 = (lights[id1] + lights[id4] + lights[id5] + lights[id6])/4; 
		v1 = vcg::Point2f(0, height*ytile);
		v2 = v1 + vcg::Point2f(width, 0);
		v3 = v1 + vcg::Point2f(0, height);
		v4 = v1 + vcg::Point2f(width, height);
	}
	else if (xtile == nx - 1)
	{
		id1 = ytile*nx + xtile;
		id2 = id1 - nx;
		id3 = id2 - 1;
		id4 = id1 - 1;
		id6 = id1 + nx;
		id5 = id6 - 1;
		l2 = (lights[id1] + lights[id2])/2;
		l4 = (lights[id1] + lights[id6])/3;
		l1 = (lights[id1] + lights[id2] + lights[id4] + lights[id3])/4; 
		l3 = (lights[id1] + lights[id4] + lights[id5] + lights[id6])/4; 
		v1 = vcg::Point2f(width*xtile, height*ytile);
		v3 = v1 + vcg::Point2f(0, height);
		v2 = vcg::Point2f(w - 1, height*ytile);
		v4 = v2 + vcg::Point2f(0, height);
	}
	float a = vcg::math::Abs(v1.X() - v2.X());
	float x1 = vcg::math::Abs(v1.X() - x);
	float x2 = a - x1;
	float b = vcg::math::Abs(v1.Y() - v3.Y());
	float y1 = vcg::math::Abs(v1.Y() - y);
	float y2 = b - y1;
	vcg::Point3f la, lc;
	la = (l1*x2 + l2*x1)/a;
	la.Normalize();
	lc = (l3*x2 + l4*x1)/a;
	lc.Normalize();
	final = (la*y2 + lc*y1)/b;
	final.Normalize();
	return final;
}


void DynamicDetailEnh::calcLocalLight(std::vector<std::vector<vcg::Point3f>*>& source, std::vector<vcg::Point3f>& dest, int nx, int ny)
{
	// Sets the size of the filter.
	int dist;
	switch(tileSize)
	{
		case 32: dist = 1; break;
		case 24: dist = 1; break;
		case 16: dist = 2; break;
		case 8: dist = 3;
	}

	// Computes the average of the light vectors of the neighbouring tiles.
	std::vector<vcg::Point3f> avg(nx*ny);
	std::vector<int> nKernel(nx*ny);
	for (int y = 0; y < ny; y++)
	{
		for (int x = 0; x < nx; x++)
		{
			int offset = y * nx + x;
			int sx, ex, sy, ey;
			sx = x - dist < 0? 0: x - dist;
			ex = x >= nx - dist ? nx - 1: x + dist; 
			sy = y - dist < 0? 0: y - dist;
			ey = y >= ny - dist? ny - 1: y + dist;
			int n = (ex - sx + 1)*(ey - sy + 1);
			nKernel[offset] = n;
			if (x > 0)
			{
				avg[offset] = avg[y*nx + x - 1];
				if (x <= dist)
				{
					for(int jj = sy; jj <= ey; jj++)
						avg[offset] += source[jj*nx + x + dist]->at(0);
				}
				else
				{
					for(int jj = sy; jj <= ey; jj++)
					{
						avg[offset] -= source[jj*nx + x - dist - 1]->at(0);
						if (x + dist < nx)
							avg[offset] += source[jj*nx + x + dist]->at(0);
					}
				}
			}
			else
			{
				avg[offset] = vcg::Point3f(0,0,0);
				for (int ii = sx; ii <= ex; ii++)
					for(int jj = sy; jj <= ey; jj++)
						avg[offset] += source[jj*nx + ii]->at(0);
			}
		}
	}
	// Selects for aech tile the light vector nearer to the average vector.
	for (int ii = 0; ii < nx*ny; ii++)
	{
		avg[ii] /= static_cast<float>(nKernel[ii]);
		avg[ii].Normalize();
		float max = -2;
		int index = 0;
		for (unsigned int jj = 0; jj < source[ii]->size(); jj++)
		{
			float dot = source[ii]->at(jj)*avg[ii];
			if (dot > max)
			{
				max = dot;
				index = jj;
			}
		}
		dest[ii] = source[ii]->at(index);
	}
}


void DynamicDetailEnh::calcSmoothing(std::vector<vcg::Point3f>& light, int nx, int ny)
{
	int dist = filter/2;
	std::vector<vcg::Point3f> tempLight(nx*ny);
	std::copy(light.begin(), light.end(), tempLight.begin());
	std::vector<int> nKernel(nx*ny);
	for (int i = 0; i < nIterFilter; i++)
	{
		for (int y = 0; y < ny; y++)
		{
			for (int x = 0; x < nx; x++)
			{
				int offset = y * nx + x;
				int sx, ex, sy, ey;
				sx = x - dist < 0? 0: x - dist;
				ex = x >= nx - dist ? nx - 1: x + dist; 
				sy = y - dist < 0? 0: y - dist;
				ey = y >= ny - dist? ny - 1: y + dist;
				int n = (ex - sx + 1)*(ey - sy + 1);
				nKernel[offset] = n;
				if (x > 0)
				{
					tempLight[offset] = tempLight[y*nx + x - 1];
					if (x <= dist)
					{
						for(int jj = sy; jj <= ey; jj++)
							tempLight[offset] += light[jj*nx + x + dist];
					}
					else
					{
						for(int jj = sy; jj <= ey; jj++)
						{
							tempLight[offset] -= light[jj*nx + x - dist - 1];
							if (x + dist < nx)
								tempLight[offset] += light[jj*nx + x + dist];
						}
					}
				}
				else
				{
					tempLight[offset] = vcg::Point3f(0,0,0);
					for (int ii = sx; ii <= ex; ii++)
						for(int jj = sy; jj <= ey; jj++)
							tempLight[offset] += light[jj*nx + ii];
				}
			}
		}
		for (int ii = 0; ii < nx*ny; ii++)
		{
			tempLight[ii] /= static_cast<float>(nKernel[ii]);
			tempLight[ii].Normalize();
		}
		std::copy(tempLight.begin(), tempLight.end(), light.begin());
	}
}


void DynamicDetailEnh::setOffset(int x)
{
	degreeOffset = x;
	emit refreshImage();
}




void DynamicDetailEnh::setTileSize(int s)
{
	tileSize = s;
	emit refreshImage();
}


void DynamicDetailEnh::updateConfig(SharpnessMeasuresDyn m, SphereSamplingDyn ss, float v1, float v2, float t, SmoothingFilterDyn f, int nIter)
{
	sharpnessOp = m;
	sphereSampl = ss;
	k1 = v1;
	k2 = v2;
	threshold = t;
	filter = f;
	nIterFilter = nIter;
	emit refreshImage();
}


DynamicDetConfDlg::DynamicDetConfDlg(QWidget *parent) : QDialog(parent)
{
	QLabel* label1 = new QLabel("Operator");
	QLabel* label2 = new QLabel("Light sampling");
	QLabel* label3 = new QLabel("K1 (sharpness) (0-1)");
	QLabel* label4 = new QLabel("K2 (lightness) (0-1)");
	QLabel* label5 = new QLabel("Thresold (0-1)");
	QLabel* label6 = new QLabel("Smoothing filter");
	QLabel* label7 = new QLabel("N. iteration smoothing");

	sharpnessOpCmb = new QComboBox(this);
	sharpnessOpCmb->setDuplicatesEnabled(false);
	sharpnessOpCmb->addItem("Energy of Laplacian", QVariant(DYN_MAX_ENERGY_LAPLACE));
	sharpnessOpCmb->addItem("Max Laplacian", QVariant(DYN_MAX_LAPLACE));
	sharpnessOpCmb->addItem("L1 norm Sobel", QVariant(DYN_NORM_L1_SOBEL));
	sharpnessOpCmb->addItem("L2 norm Sobel", QVariant(DYN_NORM_L2_SOBEL));
	
	sphereSamplCmb = new QComboBox(this);
	sphereSamplCmb->setDuplicatesEnabled(false);
	sphereSamplCmb->addItem("Anisotropic", QVariant(DYN_NON_UNIFORM));
	sphereSamplCmb->addItem("Isotropic", QVariant(DYN_UNIFORM));

	k1Snb = new QDoubleSpinBox(this);
	k1Snb->setRange(0.0, 1.0);
	k1Snb->setDecimals(2);
	k1Snb->setSingleStep(0.01);
	k1Snb->setValue(0.7);
	connect(k1Snb, SIGNAL(valueChanged(double)), this, SLOT(k1Changed(double)));
	
	k2Snb = new QDoubleSpinBox(this);
	k2Snb->setRange(0.0, 1.0);
	k2Snb->setDecimals(2);
	k2Snb->setSingleStep(0.01);
	k2Snb->setValue(0.3);
	connect(k2Snb, SIGNAL(valueChanged(double)), this, SLOT(k2Changed(double)));
	
	thresholdSnb = new QDoubleSpinBox(this);
	thresholdSnb->setRange(0.0, 1.0);
	thresholdSnb->setDecimals(2);
	thresholdSnb->setSingleStep(0.01);
	thresholdSnb->setValue(0.7);
	
	smootingFilterCmb = new QComboBox(this);
	smootingFilterCmb->setDuplicatesEnabled(false);
	smootingFilterCmb->addItem("3x3", QVariant(DYN_3x3));
	smootingFilterCmb->addItem("5x5", QVariant(DYN_5x5));
	smootingFilterCmb->addItem("7x7", QVariant(DYN_7x7));

	iterSmoothingSnb = new QSpinBox(this);
	iterSmoothingSnb->setRange(0, 10);
	iterSmoothingSnb->setValue(2);
	
	applyBtn = new QPushButton("Apply");
	applyBtn->setDefault(true);
	exitBtn = new QPushButton("Exit");
	exitBtn->setAutoDefault(false);

	buttonBox = new QDialogButtonBox;
	buttonBox->addButton(applyBtn, QDialogButtonBox::ActionRole);
	buttonBox->addButton(exitBtn, QDialogButtonBox::RejectRole);

	connect(exitBtn, SIGNAL(clicked()), this, SLOT(close()));
	connect(applyBtn, SIGNAL(clicked()), this, SLOT(applyPressed()));

	QGridLayout* layout = new QGridLayout;
	layout->addWidget(label1, 0, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label2, 1, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label3, 2, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label4, 3, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label5, 4, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label6, 5, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(label7, 6, 0, 1, 1, Qt::AlignLeft);
	layout->addWidget(sharpnessOpCmb, 0, 1, 1, 1);
	layout->addWidget(sphereSamplCmb, 1, 1, 1, 1);
	layout->addWidget(k1Snb, 2, 1, 1, 1);
	layout->addWidget(k2Snb, 3, 1, 1, 1);
	layout->addWidget(thresholdSnb, 4, 1, 1, 1);
	layout->addWidget(smootingFilterCmb, 5, 1, 1, 1);
	layout->addWidget(iterSmoothingSnb, 6, 1, 1, 1);
	layout->addWidget(buttonBox, 7, 1, 1, 1);
	setLayout(layout);
	
	setModal(false);
	setWindowFlags(Qt::Tool);
	setWindowTitle(tr("Configure Dynamic Detail Enhancement"));
}


void DynamicDetConfDlg::setCurrentValue(SharpnessMeasuresDyn sharpnessOp, SphereSamplingDyn sphereSampl, float k1, float k2, float threshold, SmoothingFilterDyn smoothFilter, int nIter)
{
	sharpnessOpCmb->setCurrentIndex(sharpnessOpCmb->findData(QVariant(sharpnessOp)));
	sphereSamplCmb->setCurrentIndex(sphereSamplCmb->findData(QVariant(sphereSampl)));
	smootingFilterCmb->setCurrentIndex(smootingFilterCmb->findData(QVariant(smoothFilter)));
	k1Snb->setValue(k1);
	k2Snb->setValue(k2);
	thresholdSnb->setValue(threshold);
	iterSmoothingSnb->setValue(nIter);
}


void DynamicDetConfDlg::applyPressed()
{
	SharpnessMeasuresDyn sharpOp = static_cast<SharpnessMeasuresDyn>(sharpnessOpCmb->itemData(sharpnessOpCmb->currentIndex()).toInt());
	SphereSamplingDyn sphereSampl = static_cast<SphereSamplingDyn>(sphereSamplCmb->itemData(sphereSamplCmb->currentIndex()).toInt());
	float k1 = k1Snb->value();
	float k2 = k2Snb->value();
	float threshold = thresholdSnb->value();
	SmoothingFilterDyn filter = static_cast<SmoothingFilterDyn>(smootingFilterCmb->itemData(smootingFilterCmb->currentIndex()).toInt());
	int iterSmooth = iterSmoothingSnb->value();
	emit configUpdated(sharpOp, sphereSampl, k1, k2, threshold, filter, iterSmooth);
}


void DynamicDetConfDlg::k1Changed(double value)
{
	k2Snb->setValue(1 - value);
}


void DynamicDetConfDlg::k2Changed(double value)
{
	k1Snb->setValue(1 - value);
}
