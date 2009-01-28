// Copyright Cultural Heritage Imaging 2008

#pragma once
#include <string>

class StatusBuffer
{
public:
	StatusBuffer(void);
public:
	~StatusBuffer(void);
	CEdit * editBox;
	bool verbose;

	void AppendStatusMsg( char *szStatusStr, char *szStatusDataStr, BOOL verboseOnlyMsg ); 
	void AppendStatusMsg( char *szStatusStr, BOOL verboseOnlyMsg ) ;
	void AppendStatusMsg( char *szStatusStr, int number, BOOL verboseOnlyMsg ); 

	std::string strStatus;
};
