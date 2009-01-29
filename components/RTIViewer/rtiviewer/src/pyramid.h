/****************************************************************************
* RTIViewer                                                         o o     *
* Single and Multi-View Reflectance Transformation Image Viewer   o     o   *
*                                                                _   O  _   *
* Copyright(C) 2008                                                \/)\/    *
* Visual Computing Lab                                            /\/|      *
* ISTI - Italian National Research Council                           |      *
*                                                                    \      *
****************************************************************************/

#ifndef PYRAMID_H
#define PYRAMID_H

#include "util.h"
#include <vcg/space/point3.h>

//! Wrapper class for mip-mapping
/*!
  A wrapper class for mip-mapping of elements of type \a T and with a number of level \a nLevel. 
*/
template <typename T, int nLevel>
class Pyramid
{
	
protected:
	T* value[nLevel]; /*<! Pointer to the mip-mapping levels. */
	int lenght[nLevel]; /*<! Lenghts of the mip-mapping levels. */

public:

	//! Constructor
	Pyramid()
	{
		for(int i = 0; i < nLevel; i++)
			value[i] = NULL;
	}

	//! Deconstructor
	~Pyramid()
	{
		for(int i = 0; i < nLevel; i++)
			if (value[i])
				delete value[i];
	}
	

	/*!
	  Sets the array \a data as level of index \a level.
	  \param data array to set as mip-mapping level.
	  \param l lenght of \a data.
	  \param level index of mip-mapping level.
	  \return true if the level exists, false otherwise.
	*/
	bool setLevel(T* data, int l, int level)
	{
		if (level < nLevel)
		{
			if (value[level])
				delete value[level];
			value[level] = data;
			lenght[level] = l;
			return true;
		}
		return false;
	}


	/*!
	  Return a constant pointer to the mip-mapping level of index \a level.
	  \param level index of mip-mapping level.
	  \return a constant pointer to the mip-mapping level. If the level don't exist, it returns a null pointer.
	*/
	const T* getLevel(int level) const
	{
		if (level < nLevel)
			return value[level];
		return NULL;
	}


	/*!
	  Returns the lenght of the mip-mapping level of index \a level.
	  \param level index of mip-mapping level.
	  \return the level's lenght if the level exists, -1 otherwise.
	*/
	int getLevelLenght(int level) const
	{
		if (level < nLevel)
			return lenght[level];
		return -1;
	}


	/*!
	  Returns the number of mip-mapping levels.
	  \return the \a nLevel
	*/
	int getLevelsNumber() const
	{
		return nLevel;
	}


	/*!
	  Allocates the level of index \a level.
	  \param level index of mip-mapping level.
	  \param l lenght of the level.
	  \return true if the level is successfully allocated, false otherwise.
	*/
	bool allocateLevel(int level, int l)
	{
		if (level < nLevel)
		{
			if (value[level])
				delete value[level];
			value[level] = new T[l];
			lenght[level] = l;
			return true;
		}
		return false;
	}

	/*!
	  Inserts the element \a data at the position \a offset of the level of index \a level.
	  \param level index of mip-mapping level.
	  \param offset position of the element in the level.
	  \param data element to insert.
	  \return true if the element is successfully inserted, false otherwise.
	*/
	bool setElement(int level, int offset, T data)
	{
		if (level < nLevel && offset < lenght[level])
		{
			value[level][offset] = data;
			return true;
		}
		return false;
	}
};


//! Extension to calculate the mip-mapping.
/*!
  The class extends the \a Pyramid class to permit the calculation of the mip-mapping.
  The class requires that the type \a T defines the operators '+' and '='.
*/
template <typename T, int nLevel>
class MipMapPyramid: public Pyramid<T, nLevel>
{

public:

	/*!
	  Sets the element of position \a offset and level \a level equal to the element of the level \a level-1 
	  and position \a index1.  

	  value[level][offset] = value[level -1][index1].

	  \param level level of destination element.
	  \param offset position of destination element.
	  \param index1 positions of the level \a level-1.
	*/
	inline void calcMipMapping(int level, int offset, int index1)
	{
		if (level > 0) 
			this->value[level][offset] = this->value[level -1][index1];
	}

	/*!
	  Calculates the element of position \a offset and level \a level as average of two elements of the level \a level-1 
	  and positions \a index1 and \a index2.  

	  value[level][offset] = (value[level -1][index1] + value[level -1][index2])*0.5;	
	  
	  \param level level of destination element.
	  \param offset position of destination element.
	  \param index1, index2 positions of the level \a level-1.
	*/
	inline void calcMipMapping(int level, int offset, int index1, int index2)
	{
		if (level > 0) 
			this->value[level][offset] = (this->value[level -1][index1] + this->value[level -1][index2])*0.5f;	
	}


	/*!
	  Calculates the element of position \a offset and level \a level as average of four elements of the level \a level-1 
	  and positions \a index1, \a index2, \a index3 and \a index4.  

	  value[level][offset] = (value[level -1][index1] + value[level -1][index2] + value[level -1][index3] + value[level -1][index4])*0.25;	
	  
	  \param level level of destination element.
	  \param offset position of destination element.
	  \param index1, index2, index3, index4 positions of the level \a level-1.
	*/
	inline void calcMipMapping(int level, int offset, int index1, int index2, int index3, int index4)
	{
		if (level > 0) 
			this->value[level][offset] = (this->value[level -1][index1] + this->value[level -1][index2] + this->value[level -1][index3] + this->value[level -1][index4])*0.25f;	
	}

};

/*!
  Type for the mip-mapping of integer coefficients.
*/
typedef MipMapPyramid<int, MIP_MAPPING_LEVELS> PyramidCoeff;

/*!
  Type for mip-mapping of float coefficients.
*/
typedef MipMapPyramid<float, MIP_MAPPING_LEVELS> PyramidCoeffF;

/*!
  Type for the mip-mapping of rgb components.
*/
typedef MipMapPyramid<unsigned char, MIP_MAPPING_LEVELS> PyramidRGB;

/*!
  Type for the mip-mapping of normals.
*/
typedef Pyramid<vcg::Point3f, MIP_MAPPING_LEVELS> PyramidNormals;

#endif //PYRAMID_H
