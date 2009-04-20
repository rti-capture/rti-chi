/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#include "multiviewrti.h"

#include <QTime>
#include <QFileInfo>
#include <QLabel>
#include <QGridLayout>



ViewpointControl::ViewpointControl(int initValueX, int nViewX, bool enableFlow, bool useFlow, QWidget *parent) : QWidget(parent)
{
	maxViewX = nViewX;
	QLabel* label = new QLabel("Horizontal shift");
	viewpointSlider = new QSlider(Qt::Horizontal);
	viewpointSlider->setTickPosition(QSlider::TicksBothSides);
	viewpointSlider->setTracking(false);
	snapNearest = new QCheckBox("Nearest viewpoint");
	if (!enableFlow)
	{
		viewpointSlider->setRange(0, maxViewX - 1);
		viewpointSlider->setValue(initValueX);
		viewpointSlider->setTickInterval(1);
		snapNearest->setCheckState(Qt::Checked);
		snapNearest->setDisabled(true);
	}
	else
	{
		if (useFlow)
		{
			viewpointSlider->setRange(0, 100*(maxViewX - 1));
			viewpointSlider->setValue(100*(initValueX));
			viewpointSlider->setTickInterval(100);
 			snapNearest->setCheckState(Qt::Unchecked);
		}
		else
		{
			viewpointSlider->setRange(0, maxViewX - 1);
			viewpointSlider->setValue(initValueX);
			viewpointSlider->setTickInterval(1);
			snapNearest->setCheckState(Qt::Checked);
		}
	}
	connect(viewpointSlider, SIGNAL(valueChanged(int)), this, SIGNAL(viewpointChanged(int)));
	connect(snapNearest, SIGNAL(stateChanged(int)), this, SLOT(updateSlider(int)));
	
	QGridLayout* layout = new QGridLayout;
	layout->addWidget(label, 0, 0, 1 ,1);
	layout->addWidget(viewpointSlider, 0, 1, 1, 1);
	layout->addWidget(snapNearest, 1, 1, 1, 1);
	setLayout(layout);
}



MultiviewRti::MultiviewRti(): Rti(),
	posX(-1),
	posY(-1)
{
	currentRendering = NORMAL_MULTIVIEW;
	// Create list of supported rendering mode.
	list = new QVector<RenderingMode*>();
}

MultiviewRti::~MultiviewRti()
{
	delete viewpointLayout;
	for(int i = 0; i < images.size(); i++)
		delete images[i];
	for(int i = 0; i < flow.size(); i++)
	{
		if (flow[i].down) delete flow[i].down;
		if (flow[i].up) delete flow[i].up;
		if (flow[i].left) delete flow[i].left;
		if (flow[i].right) delete flow[i].right;
	}
}


int MultiviewRti::load(CallBackPos *cb)
{
	if (filename.isEmpty())
		return -1;
	else
		return load(filename, cb);
}


int MultiviewRti::load(QString name, CallBackPos *cb)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	remote = false;
	if (cb != NULL)	(*cb)(0, "Loading Multiview RTI...");
	filename = name;

	type = "Multiview RTI";

	QFile data(filename);
	if (!data.open(QFile::ReadOnly))
		return -1;

	bool error;
	QTextStream stream(&data);
	QString line = "";
	do 
	{
		line = stream.readLine();
	} while (line.startsWith("#"));
	QStringList strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 3)
		return -1;
	maxViewX = strList.at(0).toInt(&error);
	if (!error) return -1;
	maxViewY = strList.at(1).toInt(&error);
	if (!error) return -1;
	nViewpoint = strList.at(2).toInt(&error);
	if (!error) return -1;

	line = stream.readLine();
	strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 2)
		return -1;
	startX = strList.at(0).toInt(&error);
	if (!error) return -1;
	startY = strList.at(1).toInt(&error);
	if (!error) return -1;
	//posX = startX - 1;
	//posY = startY - 1;
	
	line = stream.readLine();
	strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 4)
		return -1;
	int temp = strList.at(0).toInt(&error);
	if (!error) return -1;
	useFlow = (temp == 1);
	separationX = strList.at(2).toInt(&error);
	if (!error) return -1;
	separationY = strList.at(3).toInt(&error);
	if (!error) return -1;

	list->append(new DefaultMRti(startX, startY, maxViewX, maxViewY, useFlow, true));
	QFileInfo info(filename);
	images = std::vector<UniversalRti*>(nViewpoint);
	for (int i = 0; i < nViewpoint; i++)
	{
		if (cb != NULL)(*cb)(i * 60 / nViewpoint, "Loading RTI file...");
		line = stream.readLine();
		strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
		if (strList.count() < 2)
			return -1;
		QFile image(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(1)));
		if (!image.exists()) return -1;
		images[i] = new UniversalRti();
		images[i]->setFileName(image.fileName());
		images[i]->load();
	}
	
	w = images[0]->width();
	h = images[0]->height();

	viewpointLayout = new vcg::ndim::Matrix<int>(maxViewY, maxViewX);
	for (int i = 0; i < maxViewY; i++)
	{
		line = stream.readLine();
		strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
		if (strList.count() < maxViewX)
			return -1;
		for (int j = 0; j < maxViewX; j++)
		{
			(*viewpointLayout)[i][j] = strList.at(j).toInt(&error) - 1;
			if (!error) return -1;
		}
	}
	
	if (useFlow)
	{
		leftImage.hFlow = new float[w*h];
		//leftImage.vFlow = new float[w*h];
		rightImage.hFlow = new float[w*h];
		//rightImage.vFlow = new float[w*h];
		//leftUpImage.hFlow = new float[w*h];
		//leftUpImage.vFlow = new float[w*h];
		//rightUpImage.hFlow = new float[w*h];
		//rightUpImage.vFlow = new float[w*h];
		
		flow = std::vector<OpticalFlowData>(nViewpoint);
		for (int i = 0; i < nViewpoint; i++)
		{
			if (cb != NULL)(*cb)(60 + i * 40 / nViewpoint, "Loading Optical flow data ...");
			line = stream.readLine();
			strList = line.split(" ", QString::SplitBehavior::SkipEmptyParts);
			if (strList.count() < 5)
				return -1;
			for (int j = 0; j < 4; j++)
			{
				if (strList.at(j+1) == "0")
				{
					switch(j)
					{ 
						case 0: flow[i].left = NULL; break;
						case 1: flow[i].right = NULL; break;
						case 2: flow[i].up = NULL; break;
						case 3: flow[i].down = NULL; break;
					}
				}
				else
				{
					int retVal;
					switch(j)
					{ 
						case 0: retVal = loadFlowData(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(j+1)), &flow[i].left); break;
						case 1: retVal = loadFlowData(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(j+1)), &flow[i].right); break;
						case 2: retVal = loadFlowData(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(j+1)), &flow[i].up); break;
						case 3: retVal = loadFlowData(QString("%1/%2").arg(info.absolutePath()).arg(strList.at(j+1)), &flow[i].down); break;
					}
					if (retVal != 0)
						return -1;

				}
			}
		}
	}

	data.close();
	if (cb != NULL)	(*cb)(99, "Done");

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("Multiview RTI Loading: %.5f s\n", diff);
#endif

	return 0;
}


int MultiviewRti::loadFlowData(const QString &path, std::vector<float>** output)
{
#ifdef WIN32
  #ifndef __MINGW32__
	FILE* file;
	if (fopen_s(&file, path.toStdString().c_str(), "rb") != 0)
		return -1;
  #else
	FILE* file = fopen(path.toStdString().c_str(), "rb");
	if (file == NULL)
		return -1;
  #endif
#else
	FILE* file = fopen(path.toStdString().c_str(), "rb");
	if (file == NULL)
		return -1;
#endif

	bool eof, error;
	QString str;
	do
	{
		str = getLine(file, &eof);
		if (eof) return -1;
	} while (str.startsWith("#"));

	QStringList strList = str.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 2) return -1;
	int width = strList.at(0).toInt(&error);
	if (!error) return -1;
	int height = strList.at(1).toInt(&error);
	if (!error) return -1;

	str = getLine(file, &eof);
	if (eof) return -1;
	strList = str.split(" ", QString::SplitBehavior::SkipEmptyParts);
	if (strList.count() < 3) return -1;
	int elementSize = strList.at(0).toInt(&error);
	if (!error) return -1;
	float scale = strList.at(1).toFloat(&error);
	if (!error) return -1;
	float bias = strList.at(2).toFloat(&error);
	if (!error) return -1;
	*output = new std::vector<float>(width*height);

	void* temp;
	unsigned char c;
	short s;
	float f;
	
	for (int j = 0; j < height; j++)
	{
		for (int i = 0; i < width; i++)
		{
			switch(elementSize)
			{
				case 1: fread(&c, sizeof(unsigned char), elementSize, file); f = (float)c /255.0; break;
				case 2: fread(&s, sizeof(unsigned char), elementSize, file); f = (float)s /255.0; break;
				case 4: fread(&f, sizeof(unsigned char), elementSize, file); f /= 255.0; break;
				default: fread(&c, sizeof(unsigned char), 1, file);
			}
			f = f * scale + bias;
			(*output)->at((height - j - 1)*width + i) = f;
			/*if (f - floor(f) < 0.5)
				(*output)->at((height - j - 1)*width + i) = floor(f);
			else
				(*output)->at((height - j - 1)*width + i) = ceil(f);*/
		}
	}
	fclose(file);
	return 0;
}


int MultiviewRti::loadData(FILE* file, int width, int height, int basisTerm, bool urti, CallBackPos * cb, QString& text)
{
	return 0;
}


int MultiviewRti::save(QString name)
{
	// Not implemented for now...
	return 0;
}


int MultiviewRti::loadCompressed()
{
	if (filename.isEmpty())
		return -1;
	else
		return loadCompressed(filename);
}


int MultiviewRti::loadCompressed(QString name)
{
	return loadCompressed(0,0,w,h,name);
}


int MultiviewRti::loadCompressed(int xinf, int yinf, int xsup, int ysup, QString name)
{
	
	return 0;
}


int MultiviewRti::saveCompressed(QString name)
{
	return saveCompressed(0,0,w,h,0,name);
}


int MultiviewRti::saveCompressed(int xinf, int yinf, int xsup, int ysup, int reslevel, QString name)
{
	return 0;
}


int MultiviewRti::createImage(unsigned char** buffer, int& width, int& height, const vcg::Point3f& light, const QRectF& rect, int level, int mode)
{
#ifdef PRINT_DEBUG
	QTime first = QTime::currentTime();
#endif

	DefaultMRti* rend = (DefaultMRti*)list->at(NORMAL_MULTIVIEW);
	float newPosX = rend->getCurrentPosX();
	float newPosY = rend->getCurrentPosY();
	bool flag =	rend->useFlowData();
	//if (newPosX != posX || newPosY != posY)
	//{
		if (flag)
		{
			//with optical flow
			int tempW, tempH;
			int left, right, up, down;
			int newLeft, newRight, newUp, newDown;
			
			left = floorf(posX);
			right = ceilf(posX);
			up = floorf(posY);
			down = ceilf(posY);

			newLeft = floorf(newPosX);
			newRight = ceilf(newPosX);
			newUp = floorf(newPosY);
			newDown = ceilf(newPosY);

			float distX = newPosX - newLeft;
			float distY = newPosY - newDown;

			if (newLeft != newRight && newUp != newDown)
			{

			}
			else if( newLeft == newRight && newUp != newDown)
			{

			}
			else if( newLeft != newRight && newUp == newDown)
			{
				leftUpImage.valid = false;
				rightUpImage.valid = false;
				int leftIndex = (*viewpointLayout)[newDown][newLeft];
				if (leftImage.buffer)
					delete[] leftImage.buffer;
				images[leftIndex]->createImage(&leftImage.buffer, tempW, tempH, light, QRectF(0,0,w,h));
				leftImage.valid = true;
				unsigned char* tLeft = new unsigned char[tempW*tempH*4];
				int rightIndex = (*viewpointLayout)[newDown][newRight];
				if (rightImage.buffer)
					delete[] rightImage.buffer;
				images[rightIndex]->createImage(&rightImage.buffer, tempW, tempH, light, QRectF(0,0,w,h));
				rightImage.valid = true;
				unsigned char* tRight = new unsigned char[tempW*tempH*4];

				applyOpticalFlow(leftImage.buffer, (*flow[leftIndex].right), distX, tLeft, leftImage.hFlow);
				applyOpticalFlow(rightImage.buffer, (*flow[rightIndex].left), 1.0 - distX, tRight, rightImage.hFlow);
				
				/*QImage provaleft(leftImage.buffer, w, h, QImage::Format_RGB32);
				provaleft.save("left.jpg");
				QImage provaright(rightImage.buffer, w, h, QImage::Format_RGB32);
				provaright.save("right.jpg");
				QImage provaleft2(tLeft, w, h, QImage::Format_RGB32);
				provaleft2.save("leftT.jpg");
				QImage provaright2(tRight, w, h, QImage::Format_RGB32);
				provaright2.save("rightT.jpg");*/

				width = ceil(rect.width());
				height = ceil(rect.height());
				int offx = rect.x();
				int offy = rect.y();

				(*buffer) = new unsigned char[width*height*4];
				unsigned char* ptrBuffer = (*buffer);

				int offsetBuf = 0;
				for (int y = offy; y < offy + height; y++)
				{
					for (int x = offx; x < offx + width; x++)
					{
						int offset = y * w + x;
						if (leftImage.hFlow[offset] < 1 && rightImage.hFlow[offset] < 1)
						{
							if (distX <= 0.5)
							{
								for (int i = 0; i < 3; i++)
									ptrBuffer[offsetBuf + i] = tLeft[offset*4 + i];
							}
							else
							{
								for (int i = 0; i < 3; i++)
									ptrBuffer[offsetBuf + i] = tRight[offset*4 + i];
							}

							/*for (int i = 0; i < 3; i++)
								ptrBuffer[offsetBuf + i] = tobyte(tLeft[offset*4  + i]*distX + tRight[offset*4 + i]*(1.0 - distX));*/
						}
						else if(leftImage.hFlow[offset] < 1 || leftImage.hFlow[offset] < rightImage.hFlow[offset])
						{
							for (int i = 0; i < 3; i++)
								ptrBuffer[offsetBuf + i] = tLeft[offset*4 + i];
						}
						else if (rightImage.hFlow[offset] < 1 || leftImage.hFlow[offset] > rightImage.hFlow[offset])
						{
							for (int i = 0; i < 3; i++)
								ptrBuffer[offsetBuf + i] = tRight[offset*4 + i];
						}
						else 
						{
							for (int i = 0; i < 3; i++)
								ptrBuffer[offsetBuf + i] = 0;
						}
						ptrBuffer[offsetBuf + 3] = 255;
						offsetBuf += 4;
					}
				}

				QImage provaleft((*buffer), w, h, QImage::Format_RGB32);
				provaleft.save("prova.jpg");

				delete[] tLeft;
				delete[] tRight;
			}
			else if( newLeft == newRight && newUp == newDown)
			{
				leftImage.valid = false;
				rightImage.valid = false;
				leftUpImage.valid = false;
				rightUpImage.valid = false;
				images[(*viewpointLayout)[newDown][newLeft]]->createImage(buffer, width, height, light, rect);
			}
		}
		else
		{
			//without optical flow
			leftImage.valid = false;
			rightImage.valid = false;
			leftUpImage.valid = false;
			rightUpImage.valid = false;
			images[(*viewpointLayout)[(int)newPosY][(int)newPosX]]->createImage(buffer, width, height, light, rect, level, mode);
		}
		posX = newPosX;
		posY = newPosY;

	//}
	//else
	//{
	//	//si è aggiornato la luce o si è eseguito pan o zoom;

	//}

	

#ifdef PRINT_DEBUG
	QTime second = QTime::currentTime();
	double diff = first.msecsTo(second) / 1000.0;
	printf("Default rendering Multiview RTI: %.5f s\n", diff);
	
#endif

	return 0;
}



void MultiviewRti::applyOpticalFlow(const unsigned char* image, const std::vector<float>& flowData, float dist, unsigned char* outImg, float* outFlow)
{
	int offset = 0;
	for (int y = 0; y < h; y++)
		for (int x = 0; x < w; x++)
			outFlow[offset++] = 30;

	float* tempImg = new float[w*h*4];
	memset(tempImg, 0, sizeof(float)*w*h*4);
	float* contrib = new float[w*h];
	memset(contrib, 0, sizeof(float)*w*h);

	int lastMap = 0;
	bool continuos = false;
	
	offset = 0;
	for (int y = 0; y < h; y++)
	{
		for (int x = 0; x < w; x++)
		{
			offset = y*w + x;
			float shift = flowData[offset];
			if (shift != 9999)
			{
				if (x + shift < w && x + shift >= 0)
				{
					float value = shift * dist;
					int left = floor(value);
					int right = ceil(value);
					if (left != right)
					{
						float leftRem = abs(value - left);
						float rightRem = abs(value - right);
						int leftIndex = offset + left;
						contrib[leftIndex] += rightRem;
						if (outFlow[leftIndex] > leftRem)
							outFlow[leftIndex] = leftRem;
						if (x < w - 1)
						{
							contrib[leftIndex + 1] += leftRem;
							if (outFlow[leftIndex + 1] > rightRem)
								outFlow[leftIndex + 1] = rightRem;
							for (int i = 0; i < 3; i++)
							{
								tempImg[(offset + left)*4 + i] += image[offset*4 + i] * rightRem;
								tempImg[(offset + left + 1)*4 + i] += image[offset*4 + i] * leftRem;
							}
						}
						else
						{
							for (int i = 0; i < 3; i++)
								tempImg[(offset + left)*4 + i] += image[offset*4 + i] * rightRem;
						}
					}
					else
					{
						float fraction = abs(value - left);
						if (outFlow[offset + left] > fraction)
						{
							outFlow[offset + left] = fraction;
							contrib[offset + left] = 1;
							for (int i = 0; i < 3; i++)
								tempImg[(offset + left)*4 + i] = image[offset*4 + i];
						}
					}

					int gap = x + left - lastMap;
					if (continuos && gap > 1 && gap < 15)
					{
						for (int i = lastMap + 1; i < x + left; i++)
						{
							int currentPx = y*w + i;
							if (outFlow[currentPx] > gap)
							{
								int leftPx = y*w + lastMap;
								int rightPx = offset + left;
								float leftColor[3] = {tempImg[leftPx*4]/contrib[leftPx], tempImg[leftPx*4 + 1]/contrib[leftPx], tempImg[leftPx*4 + 2]/contrib[leftPx]};
								float rightColor[3] = {tempImg[rightPx*4]/contrib[rightPx], tempImg[rightPx*4 + 1]/contrib[rightPx], tempImg[rightPx*4 + 2]/contrib[rightPx]};
								outFlow[currentPx] = gap;
								contrib[currentPx] = 1;
								for (int j = 0; j < 3; j++)
									tempImg[currentPx*4 + j] = leftColor[j] + (i - lastMap)*(rightColor[j] - leftColor[j]) / gap;
							}
						}

					}
					continuos = true;
					lastMap = x + right;
				}
			}
			else
				continuos = true;
		}
		continuos = false;
	}
							
	for (int y = 0; y < h; y++)
	{
		for (int x = 0; x < w; x++)
		{
			offset = y*w + x;
			if (contrib[offset] > 0)
			{
				for (int i = 0; i < 3; i++)
					outImg[offset*4 + i] = tobyte(tempImg[offset*4 + i]/contrib[offset]);
			}
			else
			{
				for (int i = 0; i < 3; i++)
					outImg[offset*4 + i] = 0;
			}
		}
	}
	
	//QImage provaleft2(outImg, w, h, QImage::Format_RGB32);
	//provaleft2.rgbSwapped().save("prova.jpg");

	delete[] contrib;
	delete[] tempImg;
}




QImage* MultiviewRti::createPreview(int width, int height)
{
	
	return images[2]->createPreview(width, height);
}


int MultiviewRti::allocateRemoteImage(int width, int height, int maxResLevel)
{
	
	return 0;
}


int MultiviewRti::loadCompressedHttp(QBuffer* b, int xinf, int yinf, int xsup, int ysup, int level)
{
	
	return 0;
}









/*if (newLeft == left && newDown == down)
				{
					if (!leftImage)
						images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
					if (!rightImage)
						images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect);
				}
				else if (newLeft == left && newDown == up)
				{
					if (leftImage)
						delete[] leftImage;
					if (leftUpImage)
					{
						leftImage = leftUpImage;
						delete[] leftUpImage;
						leftUpImage = NULL;
					}
					else
						images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
					if (rightImage)
						delete[] rightImage;
					if (rightUpImage)
					{
						rightImage = rightUpImage;
						delete[] rightUpImage;
						leftUpImage = NULL;
					}
					else
						images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect);
				}
				else if (newLeft == right && newDown == down)
				{
					if(leftImage)
						delete[] leftImage;
					if (rightImage)
					{
						leftImage = rightImage;
						delete[] rightImage;
					}
					else
						images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
					images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect); 
				}
				else if (newLeft == right && newDown == up)
				{
					if (leftImage)
						delete[] leftImage;
					if (rightUpImage)
					{
						leftImage = rightUpImage;
						delete[] rightUpImage;
						rightUpImage = NULL;
					}
					else
						images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
					if (rightImage)
						delete[] rightImage;
					images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect); 	
				}
				else if (newRight == left && newDown == down)
				{
					if (rightImage)
						delete[] rightImage;
					if (leftImage)
					{
						rightImage = leftImage;
						delete[] leftImage;
					}
					else
						images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect);
					images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
				}
				else if (newRight == left && newDown == up)
				{
					if (rightImage)
						delete[] rightImage;
					if (leftUpImage)
					{
						rightImage = leftUpImage;
						delete[] leftUpImage;
						leftUpImage = NULL;
					}
					else
						images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect);
					if (leftImage)
						delete[] leftImage;
					images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
				}
				else
				{
					images[(*viewpointLayout)[newDown][newRight]]->createImage(&rightImage, tempW, tempH, light, rect);
					images[(*viewpointLayout)[newDown][newLeft]]->createImage(&leftImage, tempW, tempH, light, rect);
				}*/