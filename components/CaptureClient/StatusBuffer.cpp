// Copyright Cultural Heritage Imaging 2008

#include "StdAfx.h"
#include "StatusBuffer.h"

StatusBuffer::StatusBuffer(void)
{
	verbose = true;
	strStatus = "";
}

StatusBuffer::~StatusBuffer(void)
{
}

void StatusBuffer::AppendStatusMsg( char *szStatusStr, char *szStatusDataStr, BOOL verboseOnlyMsg ) 
{
	if ( !verbose && verboseOnlyMsg ) 
	{
		return;
	} 
	else 
	{
		strStatus += szStatusStr;
		strStatus += szStatusDataStr;
		strStatus += "\r\n";
		editBox->SetWindowText(strStatus.c_str());
		editBox->LineScroll(editBox->GetLineCount());
		editBox->UpdateWindow();

	}
}

void StatusBuffer::AppendStatusMsg( char *szStatusStr, BOOL verboseOnlyMsg ) 
{
	AppendStatusMsg( szStatusStr, "", verboseOnlyMsg );
}

void StatusBuffer::AppendStatusMsg( char *szStatusStr, int number, BOOL verboseOnlyMsg ) 
{
	char buffer[50];
	_itoa_s(number, buffer, 50, 10);
	AppendStatusMsg(szStatusStr , buffer, verboseOnlyMsg);
}
