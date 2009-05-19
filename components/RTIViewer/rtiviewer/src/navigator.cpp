/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#include "navigator.h"

#include <QPainter>
#include <QMouseEvent>
#include <QApplication>

Navigator::Navigator(QWidget *parent, int w, int h, int zoom) : QWidget(parent),
	height(h),	
	width(w),
	image(NULL),
	rtiWidth(0),
	rtiHeight(0),
	browserWidth(0),
	browserHeight(0),
	timer(new QTimer(this)),
	resize(false),
	maxZoom(zoom),
	dragging(false)
{

	connect(timer, SIGNAL(timeout()), this, SLOT(update()));
	
	setAutoFillBackground(false);
	setFixedSize(w, h);
}

Navigator::~Navigator()
{}


void Navigator::paintEvent(QPaintEvent *event)
{
	QPainter painter(this);
	painter.setRenderHint(QPainter::Antialiasing);
	painter.setPen(Qt::NoPen);
	painter.setBrush(QBrush(Qt::black));
	painter.drawRect(0, 0, width, height);
	if (image)
	{
		QPen pen = QPen(QColor(255, 0, 0));
		pen.setWidth(2);
		painter.drawImage(pos, *image, image->rect());
		painter.setPen(pen);
		painter.setBrush(Qt::NoBrush);
		painter.drawRect(selection);
		painter.setBrush(QBrush(QColor(255, 0, 0)));
		QPointF points[3];
		points[0] = selection.bottomRight();
		points[1] = QPointF(points[0].x(), points[0].y() - 10);
		points[2] = QPointF(points[0].x() - 10, points[0].y());
		painter.drawPolygon(points, 3); 
	}
}


void Navigator::mousePressEvent(QMouseEvent *event)
{
	if (!image) return;
	QRect rect(selection.bottomRight().x() - 12, selection.bottomRight().y() - 12, 14, 14);
	if (event->button() == Qt::LeftButton && (selection.contains(event->pos()) || rect.contains(event->pos())))
	{
		dragging = true;
		timer->start(40);
		if (rect.contains(event->pos()))
		{
			resize = true;
			QApplication::setOverrideCursor(Qt::SizeFDiagCursor);
		}
		else
			QApplication::setOverrideCursor(Qt::SizeAllCursor);
		dragPoint = event->pos();
	}
}


void Navigator::mouseMoveEvent(QMouseEvent *event)
{
	if (!image) return;
	if (dragging)
	{
		int offx = event->x() - dragPoint.x() ;
		int offy = event->y() - dragPoint.y();
		dragPoint = event->pos();
		if (resize)
			updateSelectionSize(offx, offy);
		else
			updateSelectionPos(offx, offy);
	}
}


void Navigator::mouseReleaseEvent(QMouseEvent *event)
{
	if (!image) return;
	if (event->button() == Qt::LeftButton && dragging)
	{
		timer->stop();
		dragging = false;
		int offx = event->x() - dragPoint.x() ;
		int offy = event->y() - dragPoint.y();
		if (resize)
			updateSelectionSize(offx, offy);
		else
			updateSelectionPos(offx, offy);
		resize = false;
		updateSubImage();
		update();
		QApplication::restoreOverrideCursor();
	}
}


void Navigator::wheelEvent(QWheelEvent *event)
{
	if (!image) return;
	double off = event->delta()*0.05;

	int limitH, limitW;
	limitW = (pos.width() * browserWidth / maxZoom)/rtiWidth;
	limitH = (pos.height() * browserHeight / maxZoom)/rtiHeight;
	
	if (off > 0)
	{
		int offx, offy;
		if (2*off < selection.width() - limitW)
			offx = off;
		else
			offx = 0;
		
		
		if (2*off < selection.height() - limitH)
			offy = off;
		else
			offy = 0;
		
		if (offx > 0 && offy > 0)
		{
			selection.setX(selection.x() + offx);
			selection.setY(selection.y() + offy);
			selection.setWidth(selection.width() - offx);
			selection.setHeight(selection.height() - offy);
			updateSubImage();
		}
	}
	else
	{
		updateSelectionPos(off, off);
		updateSelectionSize(-off*2, -off*2);
		updateSubImage();
	}
	update();
}


void Navigator::setImage(QImage* img, int rtiW, int rtiH)
{
	if(image)
		delete image;
	if(!img)
	{
		image = NULL;
		update();
		return;
	}
	image = img;
	rtiWidth = rtiW;
	rtiHeight = rtiH;
	double viewHeight, viewWidth;
	double ratio = static_cast<double>(img->width()) / static_cast<double>(img->height());
	
	if (img->height() > height)
		viewHeight = height;
	else
		viewHeight = img->height();

	if (img->width() > width)
		viewWidth = width;
	else
		viewWidth = img->width();

	double ratio2 = viewWidth / viewHeight;
	if (ratio2 != ratio)
	{
		if (viewWidth / ratio <= height)
			viewHeight = viewWidth / ratio;
		else
			viewWidth = viewHeight * ratio;
	}
	pos = QRect((width - viewWidth)/2, (height - viewHeight)/2, viewWidth, viewHeight);
	selection = pos;
	update();
}


void Navigator::updateSelectionPos(int offx, int offy)
{
	int x, y;
	if (selection.x() + offx <= pos.x())
		x = pos.x();
	else if (selection.right() + offx >= pos.x() + pos.width())
		x = pos.x() + pos.width() - selection.width();
	else
		x = selection.x() + offx;

	if (selection.y() + offy <= pos.y())
		y = pos.y();
	else if (selection.bottom() + offy >= pos.y() + pos.height())
		y = pos.y() + pos.height() - selection.height();
	else
		y = selection.y() + offy;
	selection.moveTo(x, y);
}


void Navigator::updateSelectionSize(int offx, int offy)
{
	int h, w;
	if (selection.right() + offx < selection.left() + 12)
		w = 12;
	else if (selection.right() + offx + 2> pos.right())
		w = pos.right() - selection.left();
	else
		w = selection.width() + offx;

	if (selection.bottom() + offy < selection.top() + 12)
		h = 12;
	else if (selection.bottom() + offy + 2> pos.bottom())
		h = pos.bottom() - selection.top();
	else
		h = selection.height() + offy;
	
	if (browserWidth != 0 && browserHeight != 0)
	{
		int limitH, limitW;
		limitW = (pos.width() * browserWidth / maxZoom)/rtiWidth;
		limitH = (pos.height() * browserHeight / maxZoom)/rtiHeight;
		if (w < limitW)
			w = limitW;
		if (h < limitH)
			h = limitH;
	}
	
	selection.setWidth(w);
	selection.setHeight(h);
}

void Navigator::updateBrowserSize(int w, int h)
{
	browserWidth = w;
	browserHeight = h;
}

void Navigator::updateSelection(QRectF rect)
{
	double ratio = static_cast<double>(pos.height()) / static_cast<double>(rtiHeight);
	int x = rect.x() * ratio;
	int y = rect.y() * ratio;
	int w = ceil(rect.width() * ratio);
	int h = ceil(rect.height() * ratio);

	selection = QRect(x + pos.x(), y + pos.y(), w, h);
	update();
}


void Navigator::updateSubImage()
{
	QRectF sub;
	double ratio = static_cast<double>(rtiHeight) / static_cast<double>(pos.height());
	int x = ceil((selection.x() - pos.x()) * ratio);
	int y = ceil((selection.y() - pos.y()) * ratio);
	int w = selection.width() * ratio;
	int h = selection.height() * ratio;

	sub = QRectF(x, y, w, h);
	emit selectionChanged(sub);
}