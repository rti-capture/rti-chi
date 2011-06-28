/*
 *  RTIbuilder
 *  Copyright (C) 2008-11  Universidade do Minho and Cultural Heritage Imaging
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

package spheredetection.helpers;

import spheredetection.imagehelpers.ImageProcessing;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Arrays;
import java.util.concurrent.*;


public class HoughTransform {

    private class centroid implements java.lang.Comparable {

        int x = -1;
        int y = -1;
        int r = -1;
        long weight = -1;

        public int compareTo(Object arg0) {
            if (arg0 instanceof centroid) {
                centroid c = (centroid) arg0;
                if (c.weight > this.weight) {
                    return 1;
                } else {
                    if (c.weight < this.weight) {
                        return -1;
                    }
                }
            }
            return 0;
        }
    }

    private class HoughCircles implements java.util.concurrent.Callable {

        private int radius;
        private BufferedImage img;
        private boolean USE_BIN_HOUGH;

        public HoughCircles(BufferedImage img, int r, boolean USE_BIN_HOUGH) {
            this.img = img;
            radius = r;
            this.USE_BIN_HOUGH = USE_BIN_HOUGH;
        }

        public centroid call() throws Exception {
            centroid c = new centroid();
            int HT[][] = new int[img.getWidth()][img.getHeight()];


            c.weight = 0;
            c.x = 0;
            c.y = 0;
            c.r = radius;

            float size = (8f * (float) radius);
            float inc = 360.0f / size;

            float LUT[][] = new float[Math.round(size)][2];

            int k = 0;
            for (int i = 0; i < size; i++) {
                float theta = 2.0f * (float) Math.PI * (float) i / size;
                LUT[k][0] = Math.round(Math.cos(theta) * (float) radius);
                LUT[k][1] = Math.round(Math.sin(theta) * (float) radius);
                k++;
            }

            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    HT[x][y] = 0;
                }
            }


            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int color_i[] = new int[3];
                    img.getRaster().getPixel(x, y, color_i);

                    int strength = (3*color_i[0]+color_i[1]+color_i[2]) / 5;

                    if (strength >  85) {

                        double alpha = ((float) color_i[1] - 127.0f) / 127.0f * Math.PI;
                        double beta = ((float) color_i[2] - 127.0f) / 127.0f * Math.PI;
                        double d = 0.0174532925;

                            int xc = x - (int) Math.round(radius * Math.cos(alpha));
                            int yc = y - (int) Math.round(radius * Math.sin(beta));


                            if (xc >= 0 && xc < img.getWidth() && yc >= 0 && yc < img.getHeight()) {
                                HT[xc][yc] += (USE_BIN_HOUGH) ? 1 : strength;

                                if (HT[xc][yc] > c.weight) {
                                    c.weight = HT[xc][yc];
                                    c.x = xc;
                                    c.y = yc;
                                }
                            }
//                        }
                    }

                }
            }

            HT = null;
            System.gc();
            return c;
        }
    }
    private BufferedImage img;
    private int min_radius;
    private int max_radius;
    private ArrayList<HoughCircles> hcc;

    public HoughTransform(BufferedImage img, int min_radius, int max_radius) {
        hcc = new ArrayList<HoughCircles>();
        this.img = img;
        //this.avgSmooth();
        this.min_radius = min_radius;
        this.max_radius = max_radius;
    }

   
    public int[] searchCircle(boolean USE_BIN_HOUGH) {
       
     
        BufferedImage img_Scale = ImageProcessing.createScaleOp(img, 0.2f);
        int result[] = { -1, -1, -1};
       
        result = this.searchCircleInImage(img_Scale, Math.round(min_radius*0.2f), Math.round(max_radius*0.2f), USE_BIN_HOUGH);
       
        result[0] /= 0.2f;
        result[1] /= 0.2f;
        result[2] /= 0.2f;
//        if(result[2]>0)
			result = this.searchCircleInImage(img, Math.round(result[2]*0.995f), Math.round(result[2]*1.05f), USE_BIN_HOUGH);
       
        return result;
    }
   
   
    public int[] searchCircleInImage(BufferedImage imgSearch,int min_r, int max_r, boolean USE_BIN_HOUGH) {
        //centroid c;
       
        ArrayList<FutureTask> arrFT = new ArrayList<FutureTask>();
        ArrayList<centroid> arrCentroids = new ArrayList<centroid>();
        ExecutorService es = Executors.newCachedThreadPool();

        for (int i = min_r; i <= max_r; i++) {
            arrFT.add(new FutureTask(new HoughCircles(imgSearch, i, USE_BIN_HOUGH)));
        }

        Iterator<FutureTask> iFT = arrFT.iterator();

        while (iFT.hasNext()) {
            es.submit(iFT.next());
        }

        iFT = arrFT.iterator();

        while (iFT.hasNext()) {
            try {
                arrCentroids.add((centroid) (iFT.next().get()));
            } catch (Exception e) {
                e.printStackTrace();

            }
        }

        Object arr[] = arrCentroids.toArray();
        Arrays.sort(arr);

        int c[] = new int[3];

        c[0] = ((centroid) arr[0]).x;
        c[1] = ((centroid) arr[0]).y;
        c[2] = ((centroid) arr[0]).r;

        //img.getGraphics().drawOval(c[0] - c[2], c[1] - c[2], 2 * c[2], 2 * c[2]);

        return c;
    }

    public BufferedImage avgSmooth() {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);


        float filter[][] = {
            {-1.0f / 8.0f, -1.0f / 8.0f, -1.0f / 8.0f},
            {-1.0f / 8.0f, +9.0f / 8.0f, -1.0f / 8.0f},
            {-1.0f / 8.0f, -1.0f / 8.0f, -1.0f / 8.0f}
        };

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int count = 0;
                float acc = 0;
                for (int i = -1; i < 2; i++) {
                    for (int j = -1; j < 2; j++) {
                        int xc = x + i;
                        int yc = y + i;
                        int cor[] = new int[3];
                        if (xc >= 0 && xc < ret.getWidth() && yc >= 0 && yc < ret.getHeight()) {
                            img.getRaster().getPixel(xc, yc, cor);
                            acc += cor[0] * filter[i + 1][j + 1];
                            count++;
                        }
                    }
                }
                acc = Math.round(acc);
                if (acc > 30) {
                    int color_i[] = new int[3];
                    ret.getRaster().getPixel(x, y, color_i);
                    color_i[0] = (int) acc;
                    ret.getRaster().setPixel(x, y, color_i);
                }
            }
        }
        System.gc();
        return ret;
    }
}
