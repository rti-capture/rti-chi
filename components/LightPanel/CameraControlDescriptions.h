// Copyright Cultural Heritage Imaging 2008

#pragma once

#include "CameraControl.h"

void CameraControl::fillTvDescriptions(void) 
{
	tvMap.clear();
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x0c,"Bulb"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x10,"30h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x13,"25h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x14,"20h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x15,"20h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x18,"15h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x1B,"13h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x1C,"10h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x1D,"10h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x20,"8h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x23,"6h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x24,"6h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x25,"5h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x28,"4h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x2B,"3h2"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x2C,"3h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x2D,"2h5"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x30,"2h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x33,"1h6"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x34,"1h5"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x35,"1h3"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x38,"1h"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x3B,"0h8"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x3C,"0h7"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x3D,"0h6"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x40,"0h5"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x43,"0h4"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x44,"0h3"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x45,"0h3"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x48,"4"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x4B,"5"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x4C,"6"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x4D,"6"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x50,"8"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x53,"10"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x54,"10"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x55,"13"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x58,"15"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x5B,"20"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x5C,"20"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x5D,"25"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x60,"30"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x63,"40"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x64,"45"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x65,"50"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x68,"60"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x6B,"80"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x6C,"90"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x6D,"100"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x70,"125"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x73,"160"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x74,"180"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x75,"200"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x78,"250"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x7B,"320"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x7C,"350"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x7D,"400"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x80,"500"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x83,"640"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x84,"750"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x85,"800"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x88,"1000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x8B,"1250"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x8C,"1500"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x8D,"1600"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x90,"2000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x93,"2500"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x94,"3000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x95,"3200"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x98,"4000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x9B,"5000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x9C,"6000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0x9D,"6400"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0xA0,"8000"));
	tvMap.insert( std::pair<EdsUInt32, std::string>(0xffffffff,"unkown"));
}

void CameraControl::fillAvDescriptions(void) 
{
	avMap.clear();
	avMap.insert( std::pair<EdsUInt32, std::string>(0x00,"00"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x08,"1"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x0B,"1.1"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x0C,"1.2"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x0D,"1.2"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x10,"1.4"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x13,"1.6"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x14,"1.8"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x15,"1.8"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x18,"2"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x1B,"2.2"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x1C,"2.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x1D,"2.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x20,"2.8"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x23,"3.2"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x24,"3.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x25,"3.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x28,"4"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x2B,"4"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x2C,"4.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x2D,"5.6"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x30,"5.6"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x33,"6.3"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x34,"6.7"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x35,"7.1"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x38,"8"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x3B,"9"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x3C,"9.5"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x3D,"10"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x40,"11"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x43,"13"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x44,"13"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x45,"14"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x48,"16"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x4B,"18"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x4C,"19"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x4D,"20"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x50,"22"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x53,"25"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x54,"27"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x55,"29"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x58,"32"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x5B,"36"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x5C,"38"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x5D,"40"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x60,"45"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x63,"51"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x64,"54"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x65,"57"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x68,"64"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x6B,"72"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x6C,"76"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x6D,"80"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0x70,"91"));
	avMap.insert( std::pair<EdsUInt32, std::string>(0xffffffff,"unkown"));
}

void CameraControl::fillISODescriptions(void) 
{
	isoMap.clear();
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x28,"6"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x30,"12"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x38,"25"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x40,"50"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x48,"100"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x4b,"125"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x4d,"160"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x50,"200"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x53,"250"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x55,"320"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x58,"400"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x5b,"500"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x5d,"640"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x60,"800"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x63,"1000"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x65,"1250"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x68,"1600"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x70,"3200"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0x78,"6400"));
	isoMap.insert( std::pair<EdsUInt32, std::string>(0xffffffff,"unkown"));
}

void CameraControl::fillImageQualityDescriptions(bool legacy) 
{
	imageQualityMap.clear();
	if (!legacy) 
	{
		// PTP Camera
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640f0f, "RAW"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640f0f, "Small RAW"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640013, "RAW + Large Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640113, "RAW + Middle Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640213, "RAW + Small Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640012, "RAW + Large Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640112, "RAW + Middle Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640212, "RAW + Small Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00130f0f, "Large Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01130f0f, "Middle Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02130f0f, "Small Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00120f0f, "Large Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01120f0f, "Middle Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02120f0f, "Small Normal Jpeg"));

		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640010, "RAW + Large Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640510, "RAW + Middle1 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640610, "RAW + Middle2 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640210, "RAW + Small Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00100f0f, "Large Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x05100f0f, "Middle1 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x06100f0f, "Middle2 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02100f0f, "Small Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640010, "Small RAW + Large Jpegg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640510, "Small RAW + Middle1 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640610, "Small RAW + Middle2 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640210, "Small RAW + Small Jpeg"));
	} 
	else
	{
		// Legacy Camera
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240000, "RAW"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240013, "RAW + Large Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240113, "RAW + Middle Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240213, "RAW + Small Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240012, "RAW + Large Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240112, "RAW + Middle Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240212, "RAW + Small Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00130000, "Large Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01130000, "Middle Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02130000, "Small Fine Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00120000, "Large Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01120000, "Middle Normal Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02120000, "Small Normal Jpeg"));

		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x002f000f, "RAW"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x002f001f, "RAW + Large Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x002f051f, "RAW + Middle1 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x002f061f, "RAW + Middle2 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x002f021f, "RAW + Small Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x001f000f, "Large Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x051f000f, "Middle1 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x061f000f, "Middle2 Jpeg"));
		imageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x021f000f, "Small Jpeg"));
	}
}

void CameraControl::fillBriefImageQualityDescriptions(bool legacy) 
{
	briefImageQualityMap.clear();
	if (!legacy) 
	{
		// PTP Camera
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00640f0f, "RAW"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02640f0f, "Small RAW"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00130f0f, "Large Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01130f0f, "Middle Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02130f0f, "Small Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00120f0f, "Large Normal Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01120f0f, "Middle Normal Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02120f0f, "Small Normal Jpeg"));
	} 
	else
	{
		// Legacy Camera
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00240000, "RAW"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00130000, "Large Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01130000, "Middle Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02130000, "Small Fine Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x00120000, "Large Normal Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x01120000, "Middle Normal Jpeg"));
		briefImageQualityMap.insert( std::pair<EdsUInt32, std::string>(0x02120000, "Small Normal Jpeg"));
	}
}