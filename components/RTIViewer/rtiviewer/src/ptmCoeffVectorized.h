#pragma once

//#include <emmintrinsics.h>

#include <emmintrin.h>

#define ALIGN_SIZE 32

#if (_MSC_VER == 0)
        #define __forceinline inline
#endif


#if _MSC_VER
  #define ALGNW __declspec(align(ALIGN_SIZE))
  #define ALGNL
#else
  #include <stdlib.h>
  #define ALGNW
  #define ALGNL __attribute__((aligned(ALIGN_SIZE)))
#endif

struct ALGNW LightMemoized {

public:
	float _aligned[6];

	__forceinline operator float *() const { return (float*)&_aligned[0];}

	__forceinline LightMemoized() { for(int i=0;i <6; i++) _aligned[i]=0; };
	__forceinline LightMemoized(float lx, float ly) { _aligned[0]=lx*lx; _aligned[1]=ly*ly; _aligned[2]=lx*ly; _aligned[3]=lx; _aligned[4]=ly; _aligned[5]=1.0f;  };

        __forceinline void *operator new (size_t size) {
            #if _MSC_VER
            void* ptr = _aligned_malloc(size,ALIGN_SIZE);
            #else
            #ifdef __APPLE__
            void* ptr = malloc(size);
            #else
            void* ptr = _mm_malloc(size,ALIGN_SIZE);
            #endif
            #endif
            return ptr;
        };
        __forceinline void operator delete (void * ptr ) {
            #if _MSC_VER
                _aligned_free(ptr);
            #else
            free(ptr);
#ifdef __APPLE__
free(ptr);
#else
_mm_free(ptr);
#endif
            #endif
        };
        __forceinline void *operator new[] (size_t size) {
            #if _MSC_VER
            void* ptr = _aligned_malloc(size,ALIGN_SIZE);
            #else
            #ifdef __APPLE__
            void* ptr = malloc(size);
            #else
            void* ptr = _mm_malloc(size,ALIGN_SIZE);
            #endif
            #endif
            return ptr;
        };
        __forceinline void operator delete[] (void * ptr) {
            #if _MSC_VER
                _aligned_free(ptr);
            #else
#ifdef __APPLE__
free(ptr);
#else
_mm_free(ptr);
#endif
            #endif
        };

} ALGNL;

struct ALGNW PTMCoefficient {

public:
	int _aligned[6];

	__forceinline operator int *() const { return (int*)&_aligned[0];}

	__forceinline PTMCoefficient() { for(int i=0;i <6; i++) _aligned[i]=0; };


	__forceinline float evalPoly(const LightMemoized &Light) const {

        float r = _aligned[5];
        for(int i=0; i < 5 ; i++) r+= _aligned[i]*Light[i];
		return r;
	}

        __forceinline float evalPoly(const float &lx,const float &ly) const {

			LightMemoized Light(lx,ly);
            return evalPoly(Light);
       }

        __forceinline void *operator new (size_t size) {
            #if _MSC_VER
            void* ptr = _aligned_malloc(size,ALIGN_SIZE);
            #else
            void* ptr = malloc(size);
            #endif
            return ptr;
        };


        __forceinline void operator delete (void * ptr ) {
            #if _MSC_VER
                _aligned_free(ptr);
            #else
            free(ptr);
            #endif
        };
        __forceinline void *operator new[] (size_t size) {
            #if _MSC_VER
            void* ptr = _aligned_malloc(size,ALIGN_SIZE);
            #else
            void* ptr = malloc(size);
            #endif
            return ptr;
        };
        __forceinline void operator delete[] (void * ptr) {
            #if _MSC_VER
                _aligned_free(ptr);
            #else
               free(ptr);
            #endif
        };


} ALGNL;
