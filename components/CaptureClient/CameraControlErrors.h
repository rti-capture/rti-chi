// Copyright Cultural Heritage Imaging 2008

#pragma once

#include "CameraControl.h"

void CameraControl::predefinedErrorMap(void)
{
	errorMap[EDS_ERR_UNIMPLEMENTED] 	 =	"Not implemented ";
	errorMap[EDS_ERR_INTERNAL_ERROR] 	 =	"Internal error ";
	errorMap[EDS_ERR_MEM_ALLOC_FAILED] 	 =	"Memory allocation error ";
	errorMap[EDS_ERR_MEM_FREE_FAILED] 	 =	"Memory release error ";
	errorMap[EDS_ERR_OPERATION_CANCELLED] 	 =	"Operation canceled ";
	errorMap[EDS_ERR_INCOMPATIBLE_VERSION] 	 =	"Version error ";
	errorMap[EDS_ERR_NOT_SUPPORTED] 	 =	"Not supported ";
	errorMap[EDS_ERR_UNEXPECTED_EXCEPTION] 	 =	"Unexpected exception ";
	errorMap[EDS_ERR_PROTECTION_VIOLATION] 	 =	"Protection violation ";
	errorMap[EDS_ERR_MISSING_SUBCOMPONENT] 	 =	"Missing subcomponent ";
	errorMap[EDS_ERR_SELECTION_UNAVAILABLE] 	 =	"Selection unavailable ";
	 
	errorMap[EDS_ERR_FILE_IO_ERROR] 	 =	"IO error ";
	errorMap[EDS_ERR_FILE_TOO_MANY_OPEN] 	 =	"Too many files open ";
	errorMap[EDS_ERR_FILE_NOT_FOUND] 	 =	"File does not exist ";
	errorMap[EDS_ERR_FILE_OPEN_ERROR] 	 =	"Open error ";
	errorMap[EDS_ERR_FILE_CLOSE_ERROR] 	 =	"Close error ";
	errorMap[EDS_ERR_FILE_SEEK_ERROR] 	 =	"Seek error ";
	errorMap[EDS_ERR_FILE_TELL_ERROR] 	 =	"Tell error ";
	errorMap[EDS_ERR_FILE_READ_ERROR] 	 =	"Read error ";
	errorMap[EDS_ERR_FILE_WRITE_ERROR] 	 =	"Write error ";
	errorMap[EDS_ERR_FILE_PERMISSION_ERROR] 	 =	"Permission error ";
	errorMap[EDS_ERR_FILE_DISK_FULL_ERROR] 	 =	"Disk full ";
	errorMap[EDS_ERR_FILE_ALREADY_EXISTS] 	 =	"File already exists ";
	errorMap[EDS_ERR_FILE_FORMAT_UNRECOGNIZED] 	 =	"Format error ";
	errorMap[EDS_ERR_FILE_DATA_CORRUPT] 	 =	"Invalid data ";
	errorMap[EDS_ERR_FILE_NAMING_NA] 	 =	"File naming error ";
	 
	errorMap[EDS_ERR_DIR_NOT_FOUND] 	 =	"Directory does not exist ";
	errorMap[EDS_ERR_DIR_IO_ERROR] 	 =	"I/O error ";
	errorMap[EDS_ERR_DIR_ENTRY_NOT_FOUND] 	 =	"No file in directory ";
	errorMap[EDS_ERR_DIR_ENTRY_EXISTS] 	 =	"File in directory ";
	errorMap[EDS_ERR_DIR_NOT_EMPTY] 	 =	"Directory full ";
	 
	errorMap[EDS_ERR_PROPERTIES_UNAVAILABLE] 	 =	"Property unavailable ";
	errorMap[EDS_ERR_PROPERTIES_MISMATCH] 	 =	"Property mismatch ";
	errorMap[EDS_ERR_PROPERTIES_NOT_LOADED] 	 =	"Property not loaded ";
	 
	errorMap[EDS_ERR_INVALID_PARAMETER] 	 =	"Invalid function parameter ";
	errorMap[EDS_ERR_INVALID_HANDLE] 	 =	"Handle error ";
	errorMap[EDS_ERR_INVALID_POINTER] 	 =	"Pointer error ";
	errorMap[EDS_ERR_INVALID_INDEX] 	 =	"Index error ";
	errorMap[EDS_ERR_INVALID_LENGTH] 	 =	"Length error ";
	errorMap[EDS_ERR_INVALID_FN_POINTER] 	 =	"FN pointer error ";
	errorMap[EDS_ERR_INVALID_SORT_FN] 	 =	"Sort FN error ";
	 
	errorMap[EDS_ERR_DEVICE_INVALID] 	 =	"Device error ";
	errorMap[EDS_ERR_DEVICE_EMERGENCY] 	 =	"Device emergency ";
	errorMap[EDS_ERR_DEVICE_MEMORY_FULL] 	 =	"Device memory full ";
	errorMap[EDS_ERR_DEVICE_INTERNAL_ERROR] 	 =	"Internal device error ";
	errorMap[EDS_ERR_DEVICE_INVALID_PARAMETER] 	 =	"Device parameter invalid ";
	errorMap[EDS_ERR_DEVICE_NO_DISK] 	 =	"No disk ";
	errorMap[EDS_ERR_DEVICE_DISK_ERROR] 	 =	"Disk error ";
	errorMap[EDS_ERR_DEVICE_CF_GATE_CHANGED] 	 =	"The CF gate has been changed ";
	errorMap[EDS_ERR_DEVICE_DIAL_CHANGED] 	 =	"The dial has been changed ";
	errorMap[EDS_ERR_DEVICE_NOT_INSTALLED] 	 =	"Device not installed ";
	errorMap[EDS_ERR_DEVICE_STAY_AWAKE] 	 =	"Device connected in awake mode ";
	errorMap[EDS_ERR_DEVICE_NOT_FOUND] 	 =	"Device not found ";
	errorMap[EDS_ERR_DEVICE_BUSY] 	 =	"Device busy ";
	errorMap[EDS_ERR_DEVICE_NOT_RELEASED] 	 =	"Device not released ";
	 
	errorMap[EDS_ERR_STREAM_IO_ERROR] 	 =	"Stream I/O error ";
	errorMap[EDS_ERR_STREAM_NOT_OPEN] 	 =	"Stream open error ";
	errorMap[EDS_ERR_STREAM_ALREADY_OPEN] 	 =	"Stream already open ";
	errorMap[EDS_ERR_STREAM_OPEN_ERROR] 	 =	"Failed to open stream ";
	errorMap[EDS_ERR_STREAM_CLOSE_ERROR] 	 =	"Failed to close stream ";
	errorMap[EDS_ERR_STREAM_SEEK_ERROR] 	 =	"Stream seek error ";
	errorMap[EDS_ERR_STREAM_TELL_ERROR] 	 =	"Stream tell error ";
	errorMap[EDS_ERR_STREAM_READ_ERROR] 	 =	"Failed to read stream ";
	errorMap[EDS_ERR_STREAM_WRITE_ERROR] 	 =	"Failed to write stream ";
	errorMap[EDS_ERR_STREAM_PERMISSION_ERROR] 	 =	"Permission error ";
	errorMap[EDS_ERR_STREAM_COULDNT_BEGIN_THREAD] 	 =	"Could not start reading thumbnail ";
	errorMap[EDS_ERR_STREAM_BAD_OPTIONS] 	 =	"Invalid stream option ";
	errorMap[EDS_ERR_STREAM_END_OF_STREAM] 	 =	"Invalid stream termination ";
	 
	errorMap[EDS_ERR_COMM_PORT_IS_IN_USE] 	 =	"Port in use ";
	errorMap[EDS_ERR_COMM_DISCONNECTED] 	 =	"Port disconnected ";
	errorMap[EDS_ERR_COMM_DEVICE_INCOMPATIBLE] 	 =	"Incompatible device ";
	errorMap[EDS_ERR_COMM_BUFFER_FULL] 	 =	"Buffer full ";
	errorMap[EDS_ERR_COMM_USB_BUS_ERR] 	 =	"USB bus error ";
	 
	errorMap[EDS_ERR_USB_DEVICE_LOCK_ERROR] 	 =	"Failed to lock the UI ";
	errorMap[EDS_ERR_USB_DEVICE_UNLOCK_ERROR] 	 =	"Failed to unlock the UI ";
	 
	errorMap[EDS_ERR_STI_UNKNOWN_ERROR] 	 =	"Unknown STI ";
	errorMap[EDS_ERR_STI_INTERNAL_ERROR] 	 =	"Internal STI error ";
	errorMap[EDS_ERR_STI_DEVICE_RELEASE_ERROR] 	 =	"Device release error ";
	errorMap[EDS_ERR_DEVICE_NOT_LAUNCHED] 	 =	"Device startup failed ";
	errorMap[EDS_ERR_STI_DEVICE_CREATE_ERROR] 	 =	"Device creation error ";
	 
	errorMap[EDS_ERR_ENUM_NA] 	 =	"Enumeration terminated (there was no suitable enumeration item) ";
	errorMap[EDS_ERR_INVALID_FN_CALL] 	 =	"Called in a mode when the function could not be used ";
	errorMap[EDS_ERR_HANDLE_NOT_FOUND] 	 =	"Handle not found ";
	errorMap[EDS_ERR_INVALID_ID] 	 =	"Invalid ID ";
	errorMap[EDS_ERR_WAIT_TIMEOUT_ERROR] 	 =	"Timeout ";
	errorMap[EDS_ERR_LAST_GENERIC_ERROR_PLUS_ONE] 	 =	"Not used. ";
	 
	errorMap[EDS_ERR_SESSION_NOT_OPEN] 	 =	"Session open error ";
	errorMap[EDS_ERR_INVALID_TRANSACTIONID] 	 =	"Invalid transaction ID ";
	 
	errorMap[EDS_ERR_INCOMPLETE_TRANSFER] 	 =	"Transfer problem ";
	errorMap[EDS_ERR_INVALID_STRAGEID] 	 =	"Storage error ";
	errorMap[EDS_ERR_DEVICEPROP_NOT_SUPPORTED] 	 =	"Unsupported device property ";
	errorMap[EDS_ERR_INVALID_OBJECTFORMATCODE] 	 =	"Invalid object format code ";
	errorMap[EDS_ERR_SELF_TEST_FAILED] 	 =	"Failed self-diagnosis ";
	errorMap[EDS_ERR_PARTIAL_DELETION] 	 =	"Failed in partial deletion ";
	errorMap[EDS_ERR_SPECIFICATION_BY_FORMAT_UNSUPPORTED] 	 =	"Unsupported format specification ";
	errorMap[EDS_ERR_NO_VALID_OBJECTINFO] 	 =	"Invalid object information ";
	errorMap[EDS_ERR_INVALID_CODE_FORMAT] 	 =	"Invalid code format ";
//	errorMap[EDS_ERR_UNKNOWN_VENDER_CODE] 	 =	"Unknown vendor code ";
	errorMap[EDS_ERR_CAPTURE_ALREADY_TERMINATED] 	 =	"Capture already terminated ";
	errorMap[EDS_ERR_INVALID_PARENTOBJECT] 	 =	"Invalid parent object ";
	errorMap[EDS_ERR_INVALID_DEVICEPROP_FORMAT] 	 =	"Invalid property format ";
	errorMap[EDS_ERR_INVALID_DEVICEPROP_VALUE] 	 =	"Invalid property value ";
	errorMap[EDS_ERR_SESSION_ALREADY_OPEN] 	 =	"Session already open ";
	errorMap[EDS_ERR_TRANSACTION_CANCELLED] 	 =	"Transaction canceled ";
	errorMap[EDS_ERR_SPECIFICATION_OF_DESTINATION_UNSUPPORTED] 	 =	"Unsupported destination specification ";
	errorMap[EDS_ERR_UNKNOWN_COMMAND] 	 =	"Unknown command ";
	errorMap[EDS_ERR_OPERATION_REFUSED] 	 =	"Operation refused ";
	errorMap[EDS_ERR_LENS_COVER_CLOSE] 	 =	"Lens cover closed ";
	errorMap[EDS_ERR_OBJECT_NOTREADY] 	 =	"Image data set not ready for live view ";
	 
	errorMap[EDS_ERR_TAKE_PICTURE_AF_NG] 	 =	"Focus failed ";
	errorMap[EDS_ERR_TAKE_PICTURE_RESERVED] 	 =	"Reserved";
	errorMap[EDS_ERR_TAKE_PICTURE_MIRROR_UP_NG] 	 =	"Currently configuring mirror up ";
	errorMap[EDS_ERR_TAKE_PICTURE_SENSOR_CLEANING_NG] 	 =	"Currently cleaning sensor ";
	errorMap[EDS_ERR_TAKE_PICTURE_SILENCE_NG] 	 =	"Currently performing silent operations";
	errorMap[EDS_ERR_TAKE_PICTURE_NO_CARD_NG] 	 =	"Card not installed ";
	errorMap[EDS_ERR_TAKE_PICTURE_CARD_NG] 	 =	"Error writing to card ";
	errorMap[EDS_ERR_TAKE_PICTURE_CARD_PROTECT_NG] 	 =	"Card write protected ";
} 
