#include <stdio.h>
#include <windows.h>
//#include "port.h"

HANDLE hPort = INVALID_HANDLE_VALUE;
int trace = 1;

/***********************************************************************

  BOOL PortInitialize(LPTSTR lpszPortName)

***********************************************************************/
BOOL PortInitialize(LPTSTR lpszPortName) {
  DWORD dwError;
  DCB PortDCB;

  // Open the serial port. 
  hPort = CreateFile (lpszPortName,     // Pointer to name of the port
                      GENERIC_READ | GENERIC_WRITE,
                                        // Access (read/write) mode
                      0,                // Share mode
                      NULL,             // Pointer to security attribute
                      OPEN_EXISTING,    // How to open
                      0,                // Port attributes
                      NULL);            // Handle to port with attribute
                                        // to copy

  // If it fails, open the port. Return FALSE.
  if ( hPort == INVALID_HANDLE_VALUE ) {
    // Could not open the port
    dwError = GetLastError ();
    fprintf(stderr, "Unable to open the port: %x\n",dwError);
    return FALSE;
  }

  PortDCB.DCBlength = sizeof (DCB);     // sizeof(DCB) 

  // Get the default port setting data. 
  GetCommState (hPort, &PortDCB);

  // Change the DCB structure settings. 
  PortDCB.BaudRate = 9600;              // Current baud rate 
  PortDCB.fBinary = TRUE;               // Binary mode, no EOF check 
  PortDCB.fParity = TRUE;               // Enable parity checking 
  PortDCB.fOutxCtsFlow = FALSE;         // No CTS output flow control 
  PortDCB.fOutxDsrFlow = FALSE;         // No DSR output flow control 
  PortDCB.fDtrControl = FALSE;		//DTR_CONTROL_ENABLE; 
                                        // DTR flow control type 
  PortDCB.fDsrSensitivity = FALSE;      // DSR sensitivity 
  PortDCB.fTXContinueOnXoff = TRUE;     // XOFF continues Tx 
  PortDCB.fOutX = FALSE;                // No XON/XOFF out flow control 
  PortDCB.fInX = FALSE;                 // No XON/XOFF in flow control 
  PortDCB.fErrorChar = FALSE;           // Disable error replacement 
  PortDCB.fNull = FALSE;                // Disable null stripping 
  PortDCB.fRtsControl = FALSE; 		//RTS_CONTROL_ENABLE; 
                                        // RTS flow control 
  PortDCB.fAbortOnError = FALSE;        // Do not abort read/write on 
                                        // error
  PortDCB.ByteSize = 8;                 // Number of bits/byte, 4-8 
  PortDCB.Parity = NOPARITY;            // 0-4=no,odd,even,mark,space 
  PortDCB.StopBits = ONESTOPBIT;        // 0,1,2 = 1, 1.5, 2 

  // Configure the port according to the specifications of DCB structure
  if (!SetCommState (hPort, &PortDCB)) {
    // Could not create the read thread
    dwError = GetLastError ();
    fprintf(stderr, "Unable to configure the serial port: %x\n",dwError);
    return FALSE;
  } else {
    return TRUE;
  }
}

/***********************************************************************

  int WriteBlock(unsigned char* buf, int len)

 ***********************************************************************/
int WriteBlock(const unsigned char *buf, int len) {
  int retlen;

  if (hPort == INVALID_HANDLE_VALUE) return -1;

  if (trace == 1) {
    int i;
    printf("WriteBlock:");
    for (i = 0; i < len; i++) printf(" %x\n", (int)buf[i]);
    printf("\n");
  }

  if (WriteFile(hPort,       // Port handle
                buf,         // Pointer to data to write 
                len,         // Number of bytes to write
                &retlen,     // Pointer to number of bytes written
                NULL))       // Must be NULL for Windows CE
  {
    return retlen;
  } else {
    return -1;
  }
}

/***********************************************************************

  int WriteChar(unsigned char uchar)

 ***********************************************************************/
int WriteChar(unsigned char uchar) {
  int retlen;

  if (hPort == INVALID_HANDLE_VALUE) return -1;

  if (trace == 1) printf("WriteChar: %x\n", (int)uchar);

  if (WriteFile(hPort,       // Port handle
                &uchar,      // Pointer to data to write 
                1,           // Number of bytes to write
                &retlen,     // Pointer to number of bytes written
                NULL))       // Must be NULL for Windows CE
  {
    return retlen;
  } else {
    return -1;
  }
}

/***********************************************************************

  BOOL PortClose()

 ***********************************************************************/
BOOL PortClose()
{
  if (hPort != INVALID_HANDLE_VALUE) {
    // Close the communications port.
    if (!CloseHandle(hPort))
    {
      return FALSE;
    }
    else
    {
      hPort = INVALID_HANDLE_VALUE;
      return TRUE;
    }
  }

  return FALSE;
}

