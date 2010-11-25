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
#include "rtiBrowser.h"
#include "detailenhanc.h"

// Qt headers
#include <QMouseEvent>
#include <QTime>
#include <QApplication>
#include <QColor>
#include <QFileDialog>

#include "glInfo.h"
#include <iostream>

RtiBrowser::RtiBrowser(int w, int h, Rti *image, int maxZ, QWidget *parent): QGLWidget(parent),
img(NULL),
light(0,0,1),
lightChanged(false),
lightChangedRight(false),
dxLight(0.0f),
dyLight(0.0f),
subimg(0,0,0,0),
level(0),
_height(h),
_width(w),
textureHeight(0),
textureWidth(0),
textureData(NULL),
isNewTexture(false),
texName(-1),
viewHeight(0),
viewWidth(0),
maxWidth(2500),
maxHeight(2500),
zoom(1.0),
minZoom(1.0),
zoomIn(QKeySequence::ZoomIn, this),
zoomOut(QKeySequence::ZoomOut, this),
dragPoint(QPoint(-1, -1)),
timer(new QTimer(this)),
maxZoom(maxZ),
dragging(false),
focus(false),
interactive(true),
posUpdated(false),
defaultMode(QKeySequence(Qt::ALT + Qt::Key_Q), this),
normalsMode(QKeySequence(Qt::ALT + Qt::Key_W), this),
smoothMode(QKeySequence(Qt::ALT + Qt::Key_E), this),
contrastMode(QKeySequence(Qt::ALT + Qt::Key_R), this),
enhancedMode(QKeySequence(Qt::ALT + Qt::Key_T), this),
lumUnsharpMode(QKeySequence(Qt::ALT + Qt::Key_Y), this),
lumMode(QKeySequence(Qt::ALT + Qt::Key_A), this),
rgbMode(QKeySequence(Qt::ALT + Qt::Key_S), this),
lumRMode(QKeySequence(Qt::ALT + Qt::Key_D), this),
lumGMode(QKeySequence(Qt::ALT + Qt::Key_F), this),
lumBMode(QKeySequence(Qt::ALT + Qt::Key_G), this),
a0Mode(QKeySequence(Qt::ALT + Qt::Key_U), this),
a1Mode(QKeySequence(Qt::ALT + Qt::Key_I), this),
a2Mode(QKeySequence(Qt::ALT + Qt::Key_O), this),
a3Mode(QKeySequence(Qt::ALT + Qt::Key_J), this),
a4Mode(QKeySequence(Qt::ALT + Qt::Key_K), this),
a5Mode(QKeySequence(Qt::ALT + Qt::Key_L), this),
lightVectorMode(QKeySequence(Qt::ALT + Qt::Key_M), this),
lightVectorMode2(QKeySequence(Qt::ALT + Qt::Key_N), this)
{

    currentMode = DEFAULT_MODE;

    // custom settings
    setMinimumSize(650, 650);
    setWindowFlags(Qt::SubWindow);

    // signal-slot connections
    connect(&zoomIn, SIGNAL(activated()), this, SLOT(zoomInActivated()));
    connect(&zoomOut, SIGNAL(activated()), this, SLOT(zoomOutActivated()));
    connect(&defaultMode, SIGNAL(activated()), this, SLOT(defaultModeActivated()));
    connect(&normalsMode, SIGNAL(activated()), this, SLOT(normalsModeActivated()));
    connect(&smoothMode, SIGNAL(activated()), this, SLOT(smoothModeActivated()));
    connect(&contrastMode, SIGNAL(activated()), this, SLOT(contrastModeActivated()));
    connect(&enhancedMode, SIGNAL(activated()), this, SLOT(enhancedModeActivated()));
    connect(&lumMode, SIGNAL(activated()), this, SLOT(lumModeActivated()));
    connect(&lumUnsharpMode, SIGNAL(activated()), this, SLOT(lumUnsharpModeActivated()));
    connect(&rgbMode, SIGNAL(activated()), this, SLOT(rgbModeActivated()));
    connect(&lumRMode, SIGNAL(activated()), this, SLOT(lumRModeActivated()));
    connect(&lumGMode, SIGNAL(activated()), this, SLOT(lumGModeActivated()));
    connect(&lumBMode, SIGNAL(activated()), this, SLOT(lumBModeActivated()));
    connect(&a0Mode, SIGNAL(activated()), this, SLOT(a0ModeActivated()));
    connect(&a1Mode, SIGNAL(activated()), this, SLOT(a1ModeActivated()));
    connect(&a2Mode, SIGNAL(activated()), this, SLOT(a2ModeActivated()));
    connect(&a3Mode, SIGNAL(activated()), this, SLOT(a3ModeActivated()));
    connect(&a4Mode, SIGNAL(activated()), this, SLOT(a4ModeActivated()));
    connect(&a5Mode, SIGNAL(activated()), this, SLOT(a5ModeActivated()));
    connect(&lightVectorMode, SIGNAL(activated()), this, SLOT(lightVectorModeActivated()));
    connect(&lightVectorMode2, SIGNAL(activated()), this, SLOT(lightVectorMode2Activated()));

    connect(timer, SIGNAL(timeout()), this, SLOT(fired()));

    // set RTI image if given
    if (image)
        setImage(image);
    setMouseTracking(true);
}


RtiBrowser::~RtiBrowser()
{
    if (img)
        delete img;

    if (textureData)
    {
        delete[] textureData;
        glDeleteTextures(1, &texName);
    }

    if (timer)
        delete timer;
}


void RtiBrowser::setImage(Rti* rti)
{
    if (img)
    {
        delete img;
        img = NULL;
    }

    if (!rti)
    {
        if (textureData)
            delete textureData;
        textureData = NULL;
        return;
    }
    img = rti;

    // Set view
    updateViewSize();

    // Set zoom info
    minZoom = static_cast<float>(viewWidth) / static_cast<float>(img->width());
    zoom = minZoom;

	emit updateZoomValue(zoom, minZoom);

    // Set sub-img
    subimg = QRectF(0.0, 0.0, img->width(), img->height());
    level = zoom >= 1 ? 0 :  floor(log(1.0/zoom)/log(2.0));
    updateTexture();
}


void RtiBrowser::setMaxWindowSize(int w, int h)
{
    maxWidth = w;
    maxHeight = h;
    if (img)
    {
        updateViewSize();
        minZoom = static_cast<float>(viewWidth) / static_cast<float>(img->width());
        if (zoom < minZoom)
            zoom = minZoom;
		emit updateZoomValue(zoom, minZoom);
        updateZoomimg();
        updateTexture();
    }
    int tempH = _height > maxHeight ? maxHeight: _height;
    int tempW = _width > maxWidth ? maxWidth: _width;
    emit sizeChanged(tempW, tempH);
}


QSize RtiBrowser::getSize()
{
    return QSize(_width, _height);
}

QMap<int, RenderingMode*>* RtiBrowser::getRenderingMode()
{
    if (img)
        return img->getSupportedRendering();
    return NULL;
}


int RtiBrowser::getCurrentRendering()
{
    if (img)
        return img->getCurrentRendering();
    return -1;
}


void RtiBrowser::initializeGL()
{
    glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
    glViewport(0, 0, _width, _height);
    glGenTextures(1, &texName);
    glBindTexture(GL_TEXTURE_2D, texName);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0.0f, (GLfloat)_width, (GLfloat)_height, 0.0f);

}


void RtiBrowser::paintGL()
{
    glClear(GL_COLOR_BUFFER_BIT);
    glEnable(GL_TEXTURE_2D);

    // Initializes texture for the first time
    if (textureData)
    {
            glTexEnvf(GL_TEXTURE_ENV, GL_TEXTURE_ENV_MODE, GL_REPLACE);
            glBindTexture(GL_TEXTURE_2D, texName);
            if(isNewTexture)
            {
                    glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureWidth, textureHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, textureData);
                    isNewTexture = false;
            }
    }

    glBegin(GL_POLYGON);
    if (textureData)
    {
            glTexCoord2f(0.0, 0.0);
            glVertex3f((_width - viewWidth)/2.0, (_height - viewHeight)/2.0, 0.0f);
            glTexCoord2f(1.0, 0.0);
            glVertex3f((_width + viewWidth)/2.0, (_height - viewHeight)/2.0, 0.0f);
            glTexCoord2f(1.0, 1.0);
            glVertex3f((_width + viewWidth)/2.0, (_height + viewHeight)/2.0, 0.0f);
            glTexCoord2f(0.0, 1.0);
            glVertex3f((_width - viewWidth)/2.0, (_height + viewHeight)/2.0, 0.0f);
    }
    glEnd();

    glMatrixMode(GL_PROJECTION);
	glPushMatrix();
	glLoadIdentity();
	glOrtho(-1,1,-1,1,-1,1);
	glMatrixMode(GL_MODELVIEW);
	glPushMatrix();
	glLoadIdentity();
	glPushAttrib(GL_ENABLE_BIT);
	glDisable(GL_DEPTH_TEST);
	glDisable(GL_TEXTURE_2D);
	glEnable(GL_BLEND);
	QFont font;
	font.setStyleStrategy(QFont::NoAntialias);
	font.setFamily("Helvetica");
	font.setPixelSize(12);
	glColor4f(1,0,0,1);
	QString text;
	switch(currentMode)
	{
		case NORMALS_MODE: text = "NORMALS MAP"; break;
		case SMOOTH_MODE: text = "SMOOTHED SIGNAL MAP"; break;
		case CONTRAST_MODE: text = "CONTRAST SIGNAL MAP"; break;
		case ENHANCED_MODE: text = "ENHANCED SIGNAL MAP"; break;
		case LUM_UNSHARP_MODE: text = "LUMINANCE (UNSHARP MASKING)"; break;
		case LUM_MODE: text = "LRGB LUMINACE CHANNEL "; break;
		case RGB_MODE: text = "LRGB RGB COMPONENTS"; break;
		case LUMR_MODE: text = "RGB RED COMPONENT"; break;
		case LUMG_MODE: text = "RGB GREEN COMPONENT"; break;
		case LUMB_MODE: text = "RGB BLUE COMPONENT"; break;
		case A0_MODE: text = "COEFFICIENT A0"; break;
		case A1_MODE: text = "COEFFICIENT A1"; break;
		case A2_MODE: text = "COEFFICIENT A2"; break;
		case A3_MODE: text = "COEFFICIENT A3"; break;
		case A4_MODE: text = "COEFFICIENT A4"; break;
		case A5_MODE: text = "COEFFICIENT A5"; break;
		case LIGHT_VECTOR: text = "LIGHT VECTORS"; break;
		case LIGHT_VECTOR2: text = "LIGHT VECTORS"; break;
		default: text = "";
	}
	renderText(20, _height - 20, text, font);
	glPopAttrib();
	glPopMatrix(); // restore modelview
	glMatrixMode(GL_PROJECTION);
	glPopMatrix();
        glMatrixMode(GL_MODELVIEW);
}


void RtiBrowser::resizeGL(int width, int height)
{
    _width = width;
    _height = height;
    glViewport(0,0, _width, _height);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    gluOrtho2D(0.0f, (GLfloat)_width, (GLfloat)_height, 0.0f);

    if (img)
    {
        updateViewSize();

        // Update zoom info
        minZoom = static_cast<float>(viewWidth) / static_cast<float>(img->width());
        if (zoom < minZoom)
            zoom = minZoom;

		emit updateZoomValue(zoom, minZoom);
        updateZoomimg();
        updateTexture();
    }
    int h = _height > maxHeight ? maxHeight: _height;
    int w = _width > maxWidth ? maxWidth: _width;
    emit sizeChanged(w, h);
}


void RtiBrowser::mousePressEvent(QMouseEvent *event)
{
    if (!img) return;
    if (event->button() == Qt::LeftButton)
    {
        // Begins a dragging operation.
        dragPoint = event->pos();
        timer->start(1);
        dragging = true;
    }
    else if (event->button() == Qt::MidButton)
    {
        // Begins a operation to modify the light direction.
        QApplication::setOverrideCursor(QCursor(Qt::CrossCursor));
        dragPoint = event->pos();
        timer->start(1);
        lightChanged = true;
        dxLight = 0;
        dyLight = 0;
    }
    else if (event->button() == Qt::RightButton)
    {
        QApplication::setOverrideCursor(QCursor(Qt::CrossCursor));
        dragPoint = event->pos();
        timer->start(1);
        lightChangedRight = true;
        lastLight = light;
        dxLight = 0;
        dyLight = 0;
    }

}


void RtiBrowser::mouseMoveEvent(QMouseEvent *event)
{
    if (!img) return;
    posUpdated = false;
    if (dragging)
    {
        int offx = (dragPoint.x() - event->x())/zoom;
        int offy = (dragPoint.y() - event->y())/zoom;
        if (offx != 0 || offy != 0)
        {
            if (interactive)
            {
                dragPoint = event->pos();
                updateSubImage(offx, offy);
                posUpdated = true;
            }
        }
    }
    else if (lightChanged)
    {
        dxLight = (event->x() - dragPoint.x())/(zoom*2);
        dyLight = (event->y() - dragPoint.y())/(zoom*2);
        posUpdated = true;
    }
    else if (lightChangedRight)
    {
        dxLight = ((event->x() - dragPoint.x())/ static_cast<float>(_width)) * M_PI;
        dyLight = ((event->y() - dragPoint.y())/ static_cast<float>(_height)) * M_PI;
        nextDragPoint = event->pos();
        posUpdated = true;
    }
    else if (event->button() ==  Qt::NoButton)
    {
        if (viewWidth == _width || viewHeight == _height)
        {
            QRect rect((_width - viewWidth)/2.0, (_height - viewHeight)/2.0, viewWidth, viewHeight);
            if (rect.contains(event->pos()))
            {
                if (!focus && !lightChanged && !lightChangedRight)
                {
                    focus = true;
                    QApplication::setOverrideCursor(QCursor(Qt::SizeAllCursor));
                }
            }
            else
            {
                if (focus && !lightChanged && !lightChangedRight)
                {
                    QApplication::restoreOverrideCursor();
                    focus = false;
                }
            }
        }
        else if (focus && !lightChanged && !lightChangedRight)
        {
            QApplication::restoreOverrideCursor();
            focus = false;
        }
    }
}


void RtiBrowser::mouseReleaseEvent(QMouseEvent *event)
{
    setFocus();
    if (!img) return;
    if (event->button() == Qt::LeftButton)
    {
        timer->stop();
        dragging = false;
        if (dragPoint != event->pos())
        {
            int offx = (dragPoint.x() - event->x())/zoom;
            int offy = (dragPoint.y() - event->y())/zoom;
            updateSubImage(offx, offy);
            updateTexture();
        }
    }
    else if (event->button() == Qt::MidButton)
    {
        QApplication::restoreOverrideCursor();
        timer->stop();
        lightChanged = false;
        dxLight = (event->x() - dragPoint.x())/(zoom*2);
        dyLight = (event->y() - dragPoint.y() )/(zoom*2);
        if (dxLight != 0 || dyLight != 0)
            emit moveLight(dxLight / img->width(), dyLight / img->height());
    }
    else if (event->button() == Qt::RightButton)
    {
        QApplication::restoreOverrideCursor();
        timer->stop();
        lightChangedRight = false;
        if(dynamic_cast<DetailEnhancement*>(img->getSupportedRendering()->value(img->getCurrentRendering())))
        {
            QPoint point = event->pos();
            int x = subimg.x() + (point.x() - (_width - viewWidth)/2)/zoom;
            int y = subimg.y() + (point.y() - (_height - viewHeight)/2)/zoom;
            if (x >= 0 && y >=0 && x < img->width() && y < img->height())
            {
                vcg::Point3f pixelLight = ((DetailEnhancement*) img->getSupportedRendering()->value(img->getCurrentRendering()))->getPixelLight(x, y);
                emit setLightDir(pixelLight, false);
            }
        }
        else
        {
            nextDragPoint= event->pos();
            dxLight = ((event->x() - dragPoint.x())/ static_cast<float>(_width)) * M_PI;
            dyLight = ((event->y() - dragPoint.y())/ static_cast<float>(_height)) * M_PI;
            updateLight();
        }
    }

}


void RtiBrowser::wheelEvent(QWheelEvent *event)
{
    if (!img) return;
	// Updates the zoom info.
	if (zoom == minZoom && event->delta() < 0) return;
	if (zoom == maxZoom && event->delta() > 0) return;
    zoom = zoom + event->delta()*0.001;
    if (zoom > maxZoom)
        zoom = maxZoom;
    else if (zoom < minZoom)
        zoom = minZoom;
	emit updateZoomValue(zoom, minZoom);
    updateZoomimg();
    updateTexture();
}


void RtiBrowser::mousefloatClickEvent(QMouseEvent *event)
{
    if (!img) return;
    if (event->button() == Qt::LeftButton)
    {
        QPoint point = event->pos();
        QPoint center(subimg.x() + (point.x() - (_width - viewWidth)/2)/zoom, subimg.y() + (point.y() - (_height - viewHeight)/2)/zoom);
        if (zoom < maxZoom)
        {
            zoom *= 1.4f;
            if (zoom > maxZoom)
                zoom = maxZoom;
			emit updateZoomValue(zoom, minZoom);
            updateZoomimg();
        }
        subimg.moveCenter(center);
        updateSubImage(0, 0);
        updateTexture();
    }
}


void RtiBrowser::leaveEvent(QEvent *event)
{
    if (focus)
    {
        QApplication::restoreOverrideCursor();
        focus = false;
    }
}



void RtiBrowser::fired()
{
    if(interactive && posUpdated)
    {
        if (dragging)
            updateTexture();
        else if (lightChanged)
            emit moveLight(dxLight / img->width(), dyLight / img->height());
        else if (lightChangedRight)
            updateLight();
    }
}



void RtiBrowser::zoomOutActivated()
{
    if (!img) return;
    if (zoom == minZoom) return;
    if (zoom <= 1)
    {
        int x = floor(log(2.0/zoom)/log(2.0));
        float temp = 1.0 / pow(2.0, x);
        zoom = temp < minZoom ? minZoom : temp;
    }
    else
        zoom  = ceil(zoom - 1);

	emit updateZoomValue(zoom, minZoom);
    updateZoomimg();
    updateTexture();
}


void RtiBrowser::zoomInActivated()
{
    if (!img) return;
    if (zoom == maxZoom) return;
    if (zoom >= 1)
        zoom = floor(zoom + 1);
    else
    {
        int x = ceil(log(1.0/(zoom*2.0))/log(2.0));
        zoom = 1/pow(2.0, x);
    }

	emit updateZoomValue(zoom, minZoom);
    updateZoomimg();
    updateTexture();
}


void RtiBrowser::setLight(vcg::Point3f l, bool refresh)
{
    light = l;
    if (img)
    {
        if (refresh)
            updateTexture();
    }
}


void RtiBrowser::updateLight()
{
    dragPoint = nextDragPoint;
    float ro, theta, phi;
    lastLight.ToPolarRad(ro, theta, phi);
    if (theta - dxLight < 0.0)
        dxLight = theta;
    else if (theta - dxLight > M_PI)
        dxLight = theta - M_PI;
    if (phi - dyLight > M_PI_2)
        dyLight = phi - M_PI_2;
    else if (phi - dyLight < -M_PI_2)
        dyLight = M_PI_2 + phi;
    vcg::Matrix33f rotX, rotY;
    rotX.SetRotateRad(dyLight, vcg::Point3f(1,0,0));
    rotY.SetRotateRad(dxLight, vcg::Point3f(0,1,0));
    vcg::Point3f temp = rotX*rotY*lastLight;
    if (temp[2] < 0)
        temp[2] = 0;
    temp.Normalize();
    emit setLightDir(temp, true);
    lastLight = temp;
    dxLight = 0;
    dyLight = 0;
}


void RtiBrowser::updateViewSize()
{
    float ratio = static_cast<float>(img->width()) / static_cast<float>(img->height());
    int h = _height > maxHeight ? maxHeight: _height;
    int w = _width > maxWidth ? maxWidth: _width;

    if (img->height() > h)
        viewHeight = h;
    else
        viewHeight = img->height();

    if (img->width() > w)
        viewWidth = w;
    else
        viewWidth = img->width();

    float ratio2 = static_cast<float>(viewWidth) / static_cast<float>(viewHeight);
    if (ratio2 != ratio)
    {
        if (viewWidth / ratio <= h)
            viewHeight = viewWidth / ratio;
        else
            viewWidth = viewHeight * ratio;
    }
}


void RtiBrowser::updateZoomimg()
{
    int h = _height > maxHeight ? maxHeight: _height;
    int w = _width > maxWidth ? maxWidth: _width;

    if (img->width() * zoom < w)
        viewWidth = img->width() * zoom;
    else
        viewWidth  = w;

    if (img->height() * zoom < h)
        viewHeight = img->height() * zoom;
    else
        viewHeight = h;

    float h1 = viewHeight / zoom;
    if ( h1 > img->height())
        h1 = img->height();

    float w1 = viewWidth / zoom;
    if (w1 > img->width())
        w1 = img->width();

    float px = subimg.x() + subimg.width() / 2.0 - w1 / 2.0;
    if (px < 0)
        px = 0;
    else if (px + w1 > img->width())
        px = img->width() - w1;

    float py = subimg.y() + subimg.height() / 2.0 - h1 / 2.0;
    if (py < 0)
        py = 0;
    else if (py + h1 > img->height())
        py = img->height() - h1;

    subimg = QRectF(px, py, w1, h1);
    emit viewChanged(subimg);
    level = zoom >= 1 ? 0 :  floor(log(1.0/zoom)/log(2.0));
}


void RtiBrowser::updateTexture(bool refresh)
{
    if (textureData)
        delete textureData;
    QTime first = QTime::currentTime();
    img->createImage(&textureData, textureWidth, textureHeight, light, subimg, level, currentMode);
    isNewTexture = true;
    if (refresh)
        updateGL();
    QTime second = QTime::currentTime();
    if (first.msecsTo(second) > 120)
    {
        emit setInteractiveLight(false);
        interactive = false;
    }
    else
    {
        emit setInteractiveLight(true);
        interactive = true;
    }
}


void RtiBrowser::updateSubImage(int offx, int offy)
{
    float x, y;
    if (subimg.x() + offx < 0)
        x = 0;
    else if (subimg.x() + offx + subimg.width() > img->width())
        x = img->width() - subimg.width();
    else
        x = subimg.x() + offx;

    if (subimg.y() + offy < 0)
        y = 0;
    else if (subimg.y() + offy + subimg.height() > img->height())
        y = img->height() - subimg.height();
    else
        y = subimg.y() + offy;

    subimg.moveTo(x, y);
    emit viewChanged(subimg);
}



void RtiBrowser::setRenderingMode(int mode)
{
    img->setRenderingMode(mode);
    QMap<int, RenderingMode*>* list = img->getSupportedRendering();
    RenderingMode* rendering = list->value(mode);
    emit setInteractiveLight(rendering->isLightInteractive());
    emit setEnabledLight(rendering->enabledLighting());
    interactive = rendering->isLightInteractive();
    updateTexture();
}


void RtiBrowser::updateImage()
{
    updateTexture();
}


void RtiBrowser::updateView(QRectF rect, bool resize)
{
    subimg = rect;
    int h = _height > maxHeight ? maxHeight: _height;
    int w = _width > maxWidth ? maxWidth: _width;

	if (resize)
	{
		float zoom1 = h / rect.height();
		float zoom2 = w / rect.width();
		zoom = zoom1 > zoom2? zoom2 : zoom1;
		if (zoom > maxZoom)
			zoom = maxZoom;
		else if (zoom < minZoom)
			zoom = minZoom;
	}
	
	emit updateZoomValue(zoom, minZoom);
	updateZoomimg();
    updateTexture();
}


void RtiBrowser::defaultModeActivated()
{
    if (!img) return;
    currentMode = DEFAULT_MODE;
    updateTexture();
}


void RtiBrowser::normalsModeActivated()
{
    if (!img) return;
    currentMode = NORMALS_MODE;
    updateTexture();
}


void RtiBrowser::smoothModeActivated()
{
    if (!img) return;
    currentMode = SMOOTH_MODE;
    updateTexture();
}


void RtiBrowser::contrastModeActivated()
{
    if (!img) return;
    currentMode = CONTRAST_MODE;
    updateTexture();
}


void RtiBrowser::enhancedModeActivated()
{
    if (!img) return;
    currentMode = ENHANCED_MODE;
    updateTexture();
}


void RtiBrowser::lumModeActivated()
{
    if (!img) return;
    currentMode = LUM_MODE;
    updateTexture();
}


void RtiBrowser::lumUnsharpModeActivated()
{
    if (!img) return;
    currentMode = LUM_UNSHARP_MODE;
    updateTexture();
}


void RtiBrowser::rgbModeActivated()
{
    if (!img) return;
    currentMode = RGB_MODE;
    updateTexture();
}


void RtiBrowser::lumRModeActivated()
{
    if (!img) return;
    currentMode = LUMR_MODE;
    updateTexture();
}


void RtiBrowser::lumGModeActivated()
{
    if (!img) return;
    currentMode = LUMG_MODE;
    updateTexture();
}


void RtiBrowser::lumBModeActivated()
{
    if (!img) return;
    currentMode = LUMB_MODE;
    updateTexture();
}


void RtiBrowser::downloadFinished()
{
    if(!img) return;
    img->resetRemote();
    emit updateRenderingList(img->getCurrentRendering(), false);
}


void RtiBrowser::a0ModeActivated()
{
    if (!img) return;
    currentMode = A0_MODE;
    updateTexture();
}


void RtiBrowser::a1ModeActivated()
{
    if (!img) return;
    currentMode = A1_MODE;
    updateTexture();
}


void RtiBrowser::a2ModeActivated()
{
    if (!img) return;
    currentMode = A2_MODE;
    updateTexture();
}


void RtiBrowser::a3ModeActivated()
{
    if (!img) return;
    currentMode = A3_MODE;
    updateTexture();
}


void RtiBrowser::a4ModeActivated()
{
    if (!img) return;
    currentMode = A4_MODE;
    updateTexture();
}


void RtiBrowser::a5ModeActivated()
{
    if (!img) return;
    currentMode = A5_MODE;
    updateTexture();
}


void RtiBrowser::lightVectorModeActivated()
{
    if (!img) return;
    currentMode = LIGHT_VECTOR;
    updateTexture();
}


void RtiBrowser::lightVectorMode2Activated()
{
    if (!img) return;
    currentMode = LIGHT_VECTOR2;
    updateTexture();
}


void RtiBrowser::snapshotActivated()
{
    if (!img) return;
    QString fileName = QFileDialog::getSaveFileName(this, tr("Save snapshot"), "snapshot.jpg", tr("JPEG (*.jpg *.jpeg);; PNG (*.png)"));
    if (fileName == "") return;
	unsigned char* imgBuffer;
	int tempW = 0;
	int tempH = 0;
	img->createImage(&imgBuffer, tempW, tempH, light, subimg);  
    QImage snapshotImg(tempW, tempH, QImage::Format_RGB32);
    QRgb value;
    for (int j = 0; j < tempH; j++)
    {
        for (int i = 0; i < tempW; i++)
        {
            int offset = j * tempW + i;
            value = qRgb(imgBuffer[offset*4], imgBuffer[offset*4 + 1], imgBuffer[offset*4 + 2]);
            snapshotImg.setPixel(i, j, value);
        }
    }
    snapshotImg.save(fileName, 0, 100);
	delete[] imgBuffer;
}


void RtiBrowser::updateZoom(int zoomVal)
{
	zoom = zoomVal / 100.0;
	if (zoom < minZoom)
		zoom = minZoom;
	if (zoom > maxZoom)
		zoom = maxZoom;
    updateZoomimg();
    updateTexture();
}