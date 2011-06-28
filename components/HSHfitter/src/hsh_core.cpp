/*  HSHFitter
 *  Copyright (C) 2009-11 UC Santa Cruz and Cultural Heritage Imaging
 *    
 *  Portions Copyright (C) 2010-11 Univ. do Minho and Cultural Heritage Imaging
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License version 3 as published
 *  by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

#include<omp.h>

#ifndef __APPLE__
extern "C" {
	/* Using external functions like done below, there is no need of including additional header
	 
	 #include <blas.h>
	 #include <lapack.h>
	 */
	//     TransA,TransB,M, N    ,K  , ALPH , A    , lda,B  , ldb   ,beta  ,C     ,ldc
	void sgemm_(char*,char*,int*,int*,int*,float*,float*,int*,float*,int*,float*,float*,int*);
	//          N   ,NRHS,  A,   LDA, IPIV,  B,   LDB,INFO)
	void sgesv_(int*,int*,float*,int*,int*,float*,int*,int*);
}

#else
#include <Accelerate/Accelerate.h>
#endif


#include <stdlib.h> //malloc use
#include "hsh_core.hpp"
#include <math.h>
#include <float.h>

typedef unsigned char uchar;

using namespace std;

HshCore::HshCore(Input * data){
	in = data;
}

void HshCore::find_min_max(float * matrix, int row, float& min, float&max)
{
	//int height = in->order*in->order;
	int width = in->width*in->height*in->channels;
	//float* data_ptr = matrix->data.fl+(width*row);
	float * data_ptr = &(matrix[width*row]);
	min = FLT_MAX;
	max = -FLT_MAX;
	for (int i=0;i<width;i++)
	{
		float current = (*data_ptr);
		if (current<min) min = current;
		if (current>max) max = current;
		data_ptr++;
	}
}


void HshCore::make_hsh_matrix()
{
	int terms = in->order * in->order;
	
	hsh_matrix = (float *)malloc(sizeof(float) * terms * in->num_used);
	
	
	cout << "HSH matrix : Rows= " << terms << " Columns=" << in->num_used << endl;
	
	for (int i=0; i < in->num_used; i++)
	{
		double& lx = in->lit_images[i].lx;
		double& ly = in->lit_images[i].ly;
		double& lz = in->lit_images[i].lz;
		double phi = atan2(ly,lx);
		if (phi<0) phi = 2*M_PI+phi;
		double theta = acos(lz);
		
		// depending on the specified order of HSH, fill in the number of terms
		// based on lx, ly and lz
		//*num_used+i
		hsh_matrix[i] = 1/sqrt(2*M_PI);
		
		hsh_matrix[in->num_used+i] = sqrt(6/M_PI)      *  (cos(phi)*sqrt(cos(theta)-cos(theta)*cos(theta)));
		
		hsh_matrix[2*in->num_used+i] = sqrt(3/(2*M_PI)) *  (-1 + 2*cos(theta));
		
		hsh_matrix[3*in->num_used+i] = sqrt(6/M_PI)      *  (sqrt(cos(theta) - pow(cos(theta),2))*sin(phi));
		
		if(in->order>2)
		{
			hsh_matrix[4*in->num_used+i] = sqrt(30/M_PI)     *  (cos(2*phi)*(-cos(theta) + pow(cos(theta),2)));
			
			hsh_matrix[5*in->num_used+i] = sqrt(30/M_PI)     *  (cos(phi)*(-1 + 2*cos(theta))*sqrt(cos(theta) - pow(cos(theta),2)));
			
			hsh_matrix[6*in->num_used+i] = sqrt(5/(2*M_PI)) *  (1 - 6*cos(theta) + 6*pow(cos(theta),2));
			
			hsh_matrix[7*in->num_used+i] = sqrt(30/M_PI)     *  ((-1 + 2*cos(theta))*sqrt(cos(theta) - pow(cos(theta),2))*sin(phi));
			
			hsh_matrix[8*in->num_used+i] = sqrt(30/M_PI)     *  ((-cos(theta) + pow(cos(theta),2))*sin(2*phi));
		}
		
		if(in->order>3)
		{
			hsh_matrix[9*in->num_used+i] = 2*sqrt(35/M_PI)  * cos(3*phi)*pow((cos(theta) - pow(cos(theta),2)),(3/2));
			
			hsh_matrix[10*in->num_used+i] = (sqrt(210/M_PI)  * cos(2*phi)*(-1 + 2*cos(theta))*(-cos(theta) + pow(cos(theta),2)));
			
			hsh_matrix[11*in->num_used+i] = 2*sqrt(21/M_PI)  * cos(phi)*sqrt(cos(theta) - pow(cos(theta),2))*(1 - 5*cos(theta) + 5*pow(cos(theta),2));
			
			hsh_matrix[12*in->num_used+i] = sqrt(7/(2*M_PI)) * (-1 + 12*cos(theta) - 30*pow(cos(theta),2) + 20*pow(cos(theta),3));
			
			hsh_matrix[13*in->num_used+i] = 2*sqrt(21/M_PI)  * sqrt(cos(theta) - pow(cos(theta),2))*(1 - 5*cos(theta) + 5*pow(cos(theta),2))*sin(phi);
			
			hsh_matrix[14*in->num_used+i] = (sqrt(210/M_PI)  * (-1 + 2*cos(theta))*(-cos(theta) + pow(cos(theta),2))*sin(2*phi));
			
			hsh_matrix[15*in->num_used+i] = 2*sqrt(35/M_PI)  * pow((cos(theta) - pow(cos(theta),2)),(3/2))*sin(3*phi);
		}
	}
	
}

void HshCore::stack_all_images2(){
	
	int size = in->height*in->width*in->channels;
	
	for (int i=0; i<in->num_used; i++)
	{
		Image * image = new Image(in->lit_images[i].filename,0);
		
		image->LoadImage();
		
		for(int j=0; j<in->height*in->width*in->channels;j++){
			mat_float[i*size+j] = image->dataFloat[j];
			
		}
		
	}
	
}

void HshCore::stack_all_rows()
{
	int size = in->width*in->channels;
	
	for (int i=0; i<in->num_used; i++)
	{
		for (int j=0; j<in->height; j++)
		{
			if(in->lit_images[i].img->LoadImageRowByRow()){
				for(int k = 0;k < in->width*in->channels;k++){
					mat_float[i*size+k] =in->lit_images[i].img->rowData[k];
				}
				
			}
			
		}
	}
	
	//return matrix;
}

void HshCore::prepareRowByRow(){
	
	//cout << "PrepareRowByRow" << endl;
	
	for(int i = 0; i < in->num_used ; i++){
		in->lit_images[i].img = new Image(in->lit_images[i].filename,0);
		in->lit_images[i].img->PrepareRowByRow();
		//cout << "Image : " << i << "Ready" << endl;
	}
}

void HshCore::destroyRowByRow(){
	
	//cout << "DestroyRowByRow" << endl;
	
	for(int i = 0; i < in->num_used ; i++){
		in->lit_images[i].img->DestroyRowByRow();
		//cout << "Image : " << i << "Destroyed" << endl;
	}
}


bool HshCore::convert_hsh_raw_to_compressed(const char *in_filename, const char * out_filename)
{
	int height, width, channels; //, order;
	float tmpf[30];
	unsigned char tmpuc[30];
	char buffer[500];
	
	
	ofstream savefile(out_filename,ios::binary);
	
	ifstream infile(in_filename,ios::binary);
	
	infile.getline(buffer,500);
	
	int file_type, terms, basis_type, element_size;
	float dummy_scale, dummy_bias;
	infile >> file_type;
	infile >> width >> height >> channels;
	infile >> terms >> basis_type >> element_size;
	infile.getline(buffer,10);
	
	for (int i=0; i<terms; i++)
		infile.read((char *)&dummy_scale,sizeof(float)); // scale
	for (int i=0; i<terms; i++)
		infile.read((char *)&dummy_bias,sizeof(float)); // scale
	
	
	
	//write header
	savefile << "#HSH1.2\r\n";
	
	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << width << " " << height << " " << channels << "\r\n";
	savefile << terms << " " << 2 << " " << 1 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 1 byte
	
	//write scaling values for each term
	for (int i=0; i<terms; i++)
	{
		float diff = max_term[i]-min_term[i];
		savefile.write((char *)&diff,sizeof(float)); // scale
	}
	for (int i=0; i<terms; i++)
		savefile.write((char *)&min_term[i],sizeof(float)); // bias
	
	
	for (int i=0; i<height; i++)
		for (int j=0; j<width; j++)
			for (int c=0; c<channels; c++)
			{
				infile.read((char *)&tmpf,sizeof(float)*terms);
				for (int t=0; t<terms; t++)
				{
					float value = tmpf[t];
					value = ((value-min_term[t])/(max_term[t]-min_term[t]))*255;
					tmpuc[t] = value;
				}
				savefile.write((char *)&tmpuc,sizeof(unsigned char)*terms);
			}
	
	infile.close();
	savefile.close();
	return true;
}


bool HshCore::hsh_save(ofstream &savefile, float *hsh_matrix)
{
	int terms = in->order*in->order;
	int M_PIxels = in->width*in->height*in->channels;
	
	ifstream ift;
	
	cout << terms << " " << M_PIxels << " ";
	for (int i=0; i<terms; i++)
	{
		find_min_max(hsh_matrix,i,min_term[i],max_term[i]);
		
		//float * float_ptr = hsh_matrix->data.fl + M_PIxels*i;
		float * float_ptr = &(hsh_matrix[M_PIxels*i]);
		//uchar* char_ptr = hsh_matrix->data.ptr + hsh_matrix->step*i;
		float diff = max_term[i]-min_term[i];
		
		//this loop goes from the first to the last M_PIxel in an image, it's in row-by-row order
		for (int j=0; j<M_PIxels; j++)
		{
			float value = (*float_ptr);
			value = ((value-min_term[i])/diff)*255;
			(*float_ptr) = value;
			float_ptr++;
		}
	}
	savefile << "#HSH1.2\r\n";
	
	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << in->width << " " << in->height << " " << in->channels << "\r\n";
	savefile << terms << " " << 2 << " " << 1 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 1 byte
	
	
	cout << endl;
	//write scaling values for each term
	for (int i=0; i<terms; i++)
	{
		float diff = max_term[i]-min_term[i];
		cout <<  diff << " , " ;
		savefile.write((char *)&diff,sizeof(float)); // scale
	}
	
	cout << endl << endl;
	for (int i=0; i<terms; i++)
	{
		cout << min_term[i] << " , " ;
		savefile.write((char *)&min_term[i],sizeof(float)); // bias
	}
	cout << endl;
	
	for (int t=0; t<terms; t++)
	{
		//term_ptr[t] = hsh_matrix->data.fl + M_PIxels*t;
		term_ptr[t] = &(hsh_matrix[M_PIxels*t]);
	}
	
	//write the raw data
	//this loop goes from the first to the last M_PIxel in an image, its in row-by-row order
	for (int i=0; i<M_PIxels; i++)
	{
		for (int t=0; t<terms; t++)
		{
			uchar char_value = (*term_ptr[t]);
			savefile.write((char*)&char_value,1);
			term_ptr[t]++;
		}
	}
	
	return true;
}


void HshCore::hsh_save_uncompressed_header(ofstream &savefile)
{
	//write the header
	savefile << "#HSH1.2\r\n";
	
	savefile << 3 << "\r\n"; // basis_type HSH
	savefile << in->width << " " << in->fullheight << " " << in->channels << "\r\n";
	savefile << in->order*in->order << " " << 2 << " " << 4 << "\r\n"; // basis_terms, basis_type == RGB seperate, element_size = 4 bytes (float)
	
	//write scaling values for each term
	float scale = 1, bias = 0;
	for (int i=0; i<in->order*in->order; i++)
		savefile.write((char *)&scale,sizeof(float));
	for (int i=0; i<in->order*in->order; i++)
		savefile.write((char *)&bias,sizeof(float));
}

bool HshCore::hsh_save_uncompressed(ofstream &savefile, float *hsh_matrix)
{
	int terms = in->order*in->order;
	int M_PIxels = in->width*in->height*in->channels;
	
	
	for (int t=0; t<terms; t++)
	{
		//term_ptr[t] = hsh_matrix->data.fl + M_PIxels*t;
		term_ptr[t] = &(hsh_matrix[M_PIxels*t]);
	}
	
	//write the raw data
	//this loop goes from the first to the last M_PIxel in an image, its in row-by-row order
	for (int i=0; i<M_PIxels; i++)
	{
		for (int t=0; t<terms; t++)
		{
			savefile.write((char *)term_ptr[t],sizeof(float));
			if (in->is_compressed) // compressed in row-by-row saving means we have to track min-max and do a second pass
			{
				float value = (*term_ptr[t]);
				if (value<min_term[t]) min_term[t]=value;
				if (value>max_term[t]) max_term[t]=value;
			}
			term_ptr[t]++;
		}
	}
	
	return true;
}


void initializeMatrix(float * matrix,int width,int height){
	for(int i = 0; i < height; i++){
		for(int j=0; j < width; j++){
			matrix[i*width+j] = 0.0;
		}
	}
}

void HshCore::delColumn(float * mat_float_b_column,int colunaI,int height,int width){
	int k=0;
	int j=colunaI;
	for(int i = 0; i < height; i++){
		mat_float_b_column[k] = mat_float_b[j];
		++k;
		j+=width;
	}
}

void HshCore::addColumn(float * mat_float_b_column,int colunaI,int height,int width){
	int k=0;
	int j=colunaI;
	for(int i = 0; i < height; i++){
		mat_float_b_backup[j] = mat_float_b_column[k];
		++k;
		j+=width;
		
	}
}

void HshCore::compute_loop()
{
	
	time_t start;
	start = time(NULL);
	ofstream outfile;
	int terms = in->order*in->order;
	
	int width = in->width*in->channels;
	
	
	if (in->is_row_by_row)
	{
		if (in->is_compressed) // row-by-row and compressed, we'll save to a temporary file and then convert
			outfile.open("temp.hsh",ios::binary);
		else			// no temporary files, the uncompressed file is the output
			outfile.open(in->str_output_filename.c_str(),ios::binary);
		
		in->height = 1;
		
		prepareRowByRow();
		
		hsh_save_uncompressed_header(outfile);
		
	}
	else
	{
		// the 'all in memory' case
		outfile.open(in->str_output_filename.c_str(),ios::binary);
	}
	
	int image = width*in->height;
	
	if (in->is_compressed)  // initialize minimum and maximum bounds for 'compressed' format
	{
		min_term.resize(terms);
		max_term.resize(terms);
		for (int i=0;i<terms;i++)
		{
			min_term[i] = FLT_MAX;
			max_term[i] = -FLT_MAX;
		}
	}
	
	term_ptr.resize(terms); // used to track term blocks when saving HSHs
	
	make_hsh_matrix();
	
	float * mat_float_a = (float*)malloc(sizeof(float)*terms*terms);
	float * mat_float_a_backup = (float*)malloc(sizeof(float)*terms*terms);
	
	/**
	 * Multiplying A * AT
	 * The result is a [TERMS][TERMS] matrix -> mat_float_a
	 *
	 */
	char trans = 'T';
	char noTrans = 'N';
	float alpha = 1.0;
	float beta = 0.0;
	
#ifndef __APPLE__	
	sgemm_(&trans, &noTrans, &terms, &terms,
		   &(in->num_used), &alpha, hsh_matrix, &(in->num_used), hsh_matrix, &(in->num_used),
		   &beta, mat_float_a, &terms);
#else
	SGEMM(&trans, &noTrans, &terms, &terms,
		   &(in->num_used), &alpha, hsh_matrix, &(in->num_used), hsh_matrix, &(in->num_used),
		   &beta, mat_float_a, &terms);
#endif
	
	
	memcpy(mat_float_a_backup,mat_float_a,sizeof(float) * terms * terms);
	
	mat_float = (float *)malloc(sizeof(float) * image * in->num_used);
	
	mat_float_b = (float *)malloc(sizeof(float) * terms * image);
	mat_float_b_backup = (float *)malloc(sizeof(float) * terms * image);
	
	omp_set_num_threads(in->numberOfThreads);
	
	cout << "Number of Threads : " << in->numberOfThreads << endl;
	
	for (int row=0; row<in->fullheight; row+=in->height)
	{
		
		if (in->is_row_by_row)
		{
			stack_all_rows();
		}
		else
		{
			stack_all_images2();
			cout << "Single large image matrix created! " << endl;
		}
		
		/**
		 Multiplication of hsh_matrix[terms][num_used] * mat_float[num_used][image]
		 The result is written in mat_float_b
		 */

#ifndef __APPLE__		
		sgemm_(&noTrans, &noTrans,
			   &image, &terms,
			   &(in->num_used), &alpha, mat_float, &(image), hsh_matrix,
			   &(in->num_used), &beta, mat_float_b, &image);
#else
		//cout << "Start SGEMM 2" << endl;
		/*cblas_sgemm(CblasRowMajor, CblasNoTrans, CblasNoTrans,
					image, terms,
					in->num_used, alpha, mat_float, image, hsh_matrix,
					in->num_used, beta, mat_float_b, image);*/
		SGEMM(&noTrans, &noTrans,
			   &image, &terms,
			   &(in->num_used), &alpha, mat_float, &(image), hsh_matrix,
			   &(in->num_used), &beta, mat_float_b, &image);
		//cout << "Start SGEMM 2" << endl;
#endif
		
		
		int * chunkMin = (int*)malloc(sizeof(int)*in->numberOfThreads);
		int * chunkMax = (int*)malloc(sizeof(int)*in->numberOfThreads);
		int chunkSize = (int)(width/in->numberOfThreads);
		
		for(int k = 0 ;k < in->numberOfThreads;k++)
		{
			chunkMin[k] = k*chunkSize;
			chunkMax[k] = k*chunkSize+chunkSize;
		}
		int um=1;
#pragma omp parallel
		{
			int singular;
			
			float * mat_float_a_backupPerThread = (float*)malloc(sizeof(float)*terms*terms);
			float* mat_float_b_column = (float*)malloc(sizeof(float) * 3 * terms);
			
#ifndef __APPLE__
			int * ipiv = (int*)malloc(sizeof(int)*terms);
#else
			__CLPK_integer * ipiv = (__CLPK_integer*)malloc(sizeof(__CLPK_integer)*terms);
#endif
			
			int id=omp_get_thread_num();
			for(int a=chunkMin[id]; a < chunkMax[id]; a++){
				
				delColumn(mat_float_b_column,a,terms,width);
				
				memcpy(mat_float_a_backupPerThread,mat_float_a,sizeof(float) * terms * terms);
				
#ifndef __APPLE__
				sgesv_(&terms,&um,
					   mat_float_a_backupPerThread,&terms,
					   ipiv,
					   mat_float_b_column,&width,&singular);
#else
				__CLPK_integer  n=terms, nrhs = um, lda = n, ldb = n, info=0;
				sgesv_(&n, &nrhs, mat_float_a_backupPerThread, &lda, ipiv,mat_float_b_column, &ldb, &info);
#endif
				
				addColumn(mat_float_b_column,a,terms,width);
				
			}// End for
		} //end parallel
		
		if (in->is_row_by_row)
			hsh_save_uncompressed(outfile,mat_float_b_backup);
		
		else
			hsh_save(outfile,mat_float_b_backup);
		
	}
	
	cout << "Time spent in compute_loop : " << (time(NULL) - start) << endl;
	
	outfile.close();
	
	if (in->is_row_by_row)
	{
		destroyRowByRow();
		if (in->is_compressed) // if row-by-row and compressed, we have to re-save the file as compressed.
			convert_hsh_raw_to_compressed("temp.hsh",in->str_output_filename.c_str());
	}
	
	
	free(mat_float_b);
	free(mat_float);
	free(hsh_matrix);
	free(mat_float_b_backup);
	
	
	
	
}
