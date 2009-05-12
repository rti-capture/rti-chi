/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/


#include "renderingdialog.h"

#include <QVBoxLayout>

RenderingDialog::RenderingDialog(QMap<int, RenderingMode*>* l, int currRendering, QWidget *parent, bool remote) : QWidget(parent) 
{
	list = l;
	modeList = new QComboBox(this);
	modeList->setDuplicatesEnabled(false);
	if (l)
	{
		int selected = currRendering;
		QMapIterator<int, RenderingMode*> iter(*list);
		int i = 0;
		while (iter.hasNext())
		{
			iter.next();
			if (remote)
			{
				if ((iter.value())->supportRemoteView())
				{	
					modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
					if (currRendering == iter.key())
						selected = i;
					i++;
				}
			}
			else
			{
				modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
				i++;
			}
		}
		modeList->setCurrentIndex(selected);
		control = list->value(currRendering)->getControl(this);
	}
	else
		control = new QWidget(this);
	control->setMinimumHeight(80);
	connect(modeList, SIGNAL(activated(int)), this, SLOT(renderingModeUpdate(int)));
	QVBoxLayout* layout = new QVBoxLayout;
	layout->addWidget(modeList, 0, Qt::AlignTop);
	layout->addWidget(control, 2, Qt::AlignTop);
	setLayout(layout);
}


void RenderingDialog::renderingModeUpdate(int index)
{
	int idx = modeList->itemData(index).toInt();
	QWidget* c = list->value(idx)->getControl(this);
	c->setMinimumHeight(80);
	QBoxLayout* layout =(QBoxLayout*) this->layout();
	layout->removeWidget(control);
	layout->addWidget(c, 2, Qt::AlignTop);
	control->close();
	delete control;
	control = c;
	emit renderingModeChanged(idx);
	update();
}


void RenderingDialog::setRenderingMode(QMap<int, RenderingMode*>* l, int currRendering, bool remote)
{
	modeList->clear();
	list = l;
	if (l)
	{
		int selected = currRendering;
		QMapIterator<int, RenderingMode*> iter(*list);
		int i = 0;
		while (iter.hasNext())
		{
			iter.next();
			if (remote)
			{
				if ((iter.value())->supportRemoteView())
				{	
					modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
					if (currRendering == iter.key())
						selected = i;
					i++;
				}
			}
			else
			{
				modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
				i++;
			}
		}
		modeList->setCurrentIndex(selected);
		renderingModeUpdate(selected);
	}
	else
	{
		QWidget* c = new QWidget(this);
		c->setMinimumHeight(80);
		QBoxLayout* layout =(QBoxLayout*) this->layout();
		layout->removeWidget(control);
		layout->addWidget(c, 0, Qt::AlignTop);
		delete control;
		control = c;
		update();
	}	
}


void RenderingDialog::updateRenderingList(int currRendering, bool remote)
{
	modeList->clear();
	emit resetRemote();
	if (list)
	{
		int selected = currRendering;
		QMapIterator<int, RenderingMode*> iter(*list);
		int i = 0;
		while (iter.hasNext())
		{
			iter.next();
			if (remote)
			{
				if ((iter.value())->supportRemoteView())
				{	
					modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
					if (currRendering == iter.key())
						selected = i;
					i++;
				}
			}
			else
			{
				modeList->addItem((iter.value())->getTitle(), QVariant(iter.key()));
				i++;
			}
		}
		modeList->setCurrentIndex(selected);
		renderingModeUpdate(selected);
	}
}
