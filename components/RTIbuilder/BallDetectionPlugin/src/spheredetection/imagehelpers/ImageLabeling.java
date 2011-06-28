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

package spheredetection.imagehelpers;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class ImageLabeling {
    public static float[] find(BufferedImage img, float xc, float yc) {
        float[] ret = new float[4];
        float[] result = new float[3];
        float[] center = new float[4];
        float[] min = new float[2];
        float[] max = new float[2];
        int[] color = new int[1];
        int[] cor = {127};

        center[0] = 0; // X

        center[1] = 0; // Y

        center[2] = 0; // Radius

        center[3] = 0; // Area

        LinkedList<Point> pqueue = new LinkedList<Point>();
        java.awt.image.WritableRaster data = img.getRaster();


        for (int y = 0; y < img.getHeight(); y++) {
            // Linha
            for (int x = 0; x < img.getWidth(); x++) {
                // Coluna
                data.getPixel(x, y, color);
                if (color[0] == 255) {
                    pqueue.add(new Point(x, y));
                    data.setPixel(x, y, cor);
                    min[0] = x;
                    min[1] = y;
                    max[0] = x;
                    max[1] = y;

                    ret[0] = x;
                    ret[1] = y;
                    ret[3] = 1;

                    while (!pqueue.isEmpty()) {
                        Point p = pqueue.removeFirst();
                        ret[0] += p.x; // SUM(Xi)

                        ret[1] += p.y; // SUM(Yj)

                        ret[3]++; // Area

                        if (p.x < min[0]) {
                            min[0] = p.x;
                        }
                        if (p.y < min[1]) {
                            min[1] = p.y;
                        }
                        if (p.x > max[0]) {
                            max[0] = p.x;
                        }
                        if (p.y > max[1]) {
                            max[1] = p.y;
                        }
                        for (int y1 = -1; y1 <= 1; y1++) {
                            // Linha
                            for (int x1 = -1; x1 <= 1; x1++) {
                                // Coluna
                                data.getPixel(p.x + x1, p.y + y1, color);
                                if (color[0] == 255) {
                                    pqueue.add(new Point(p.x + x1, p.y + y1));
                                    data.setPixel(p.x + x1, p.y + y1, cor);
                                }
                            }
                        }
                    }

                    //cor[0]-=20;
                    float xr = (ret[0] / ret[3]) - xc + 0.5f;
                    float yr = yc - (ret[1] / ret[3]) + 0.5f;
                    float r = Math.min(max[0] - min[0], max[1] - min[1]) / 2;
                    float cxr = center[0] - xc;
                    float cyr = yc - center[1];
                    float sr = xr * xr + yr * yr;
                    float sc = cxr * cxr + cyr * cyr;


                    // center - highlight already detected
                    // ret - highlight under detection
                    if (ret[3] >= center[3] * 0.2) {
                        // Select if >= 20% previous hl area
                        if (ret[3] >= (center[3] * 5)) {
                            // Select if 5 times larger then hl
                            center[0] = ret[0] / ret[3];
                            center[1] = ret[1] / ret[3];
                            center[2] = r;
                            center[3] = ret[3];
                        } else {
                            if (sr < sc) {
                                center[0] = ret[0] / ret[3];
                                center[1] = ret[1] / ret[3];
                                center[2] = r;
                                center[3] = ret[3];
                            }
                        }
                    }
                }
            }
        }
        result[0] = center[0];
        result[1] = center[1];
        result[2] = center[2];
        return result;
    }

    public static float[] findTh(BufferedImage img, int th) {
        float[] ret = new float[4];
        float[] result = new float[3];
        float[] center = new float[4];
        float[] min = new float[2];
        float[] max = new float[2];
        int[] color = new int[1];
        int[] cor = {th - 1};

        float xc = img.getWidth() / 2.0f, yc = img.getHeight() / 2.0f;

        center[0] = -1; // X

        center[1] = -1; // Y

        center[2] = 0; // Radius

        center[3] = 0; // Area

        LinkedList<Point> pqueue = new LinkedList<Point>();
        java.awt.image.WritableRaster data = img.getRaster();


        for (int y = 0; y < img.getHeight(); y++) {
            // Linha
            for (int x = 0; x < img.getWidth(); x++) {
                // Coluna
                data.getPixel(x, y, color);
                if (color[0] >= th) {
                    pqueue.add(new Point(x, y));
                    data.setPixel(x, y, cor);
                    min[0] = x;
                    min[1] = y;
                    max[0] = x;
                    max[1] = y;

                    ret[0] = x;
                    ret[1] = y;
                    ret[3] = 1;

                    while (!pqueue.isEmpty()) {
                        Point p = pqueue.removeFirst();
                        ret[0] += p.x; // SUM(Xi)

                        ret[1] += p.y; // SUM(Yj)

                        ret[3]++; // Area

                        if (p.x < min[0]) {
                            min[0] = p.x;
                        }
                        if (p.y < min[1]) {
                            min[1] = p.y;
                        }
                        if (p.x > max[0]) {
                            max[0] = p.x;
                        }
                        if (p.y > max[1]) {
                            max[1] = p.y;
                        }
                        data.setPixel(p.x, p.y, cor);
                        for (int y1 = -1; y1 <= 1; y1++) {
                            // Linha
                            for (int x1 = -1; x1 <= 1; x1++) {
                                // Coluna
                                int px = p.x + x1;
                                int py = p.y + y1;
                                color[0] = 0;
                                if (px > 0 && px < data.getWidth() && py > 0 && py < data.getHeight()) {
                                    
                                    data.getPixel(px, py, color);

                                    if (color[0] >= th) {
                                        pqueue.add(new Point(px, py));
                                        data.setPixel(px, py, cor);
                                    }
                                }

                            }
                        }
                    }

                    //cor[0]-=20;
                    float xr = (ret[0] / ret[3]) - xc + 0.5f;
                    float yr = yc - (ret[1] / ret[3]) + 0.5f;
                    float r = Math.min(max[0] - min[0], max[1] - min[1]) / 2;
                    float cxr = center[0] - xc;
                    float cyr = yc - center[1];
                    float sr = xr * xr + yr * yr;
                    float sc = cxr * cxr + cyr * cyr;


                    // center - highlight already detected
                    // ret - highlight under detection
                    if (ret[3] >= center[3] * 0.2) {
                        // Select if >= 20% previous hl area
                        if (ret[3] >= (center[3] * 5)) {
                            // Select if 5 times larger then hl
                            center[0] = ret[0] / ret[3];
                            center[1] = ret[1] / ret[3];
                            center[2] = r;
                            center[3] = ret[3];
                        } else {
                            if (sr < sc) {
                                center[0] = ret[0] / ret[3];
                                center[1] = ret[1] / ret[3];
                                center[2] = r;
                                center[3] = ret[3];
                            }
                        }
                    }
                }
            }
        }
        result[0] = center[0];
        result[1] = center[1];
        result[2] = center[2];
        return result;
    }

    public static float[] findBall(BufferedImage img, int th) {
        float[] ret = new float[4];
        float[] min = new float[2];
        float[] max = new float[2];
        int[] color = new int[1];
        float[] Ball = new float[3];
        int[] cor = {th - 1};
        java.util.ArrayList<Circulo> circulos = new ArrayList<Circulo>();
        LinkedList<Point> pqueue = new LinkedList<Point>();
        java.awt.image.WritableRaster data = img.getRaster();


        for (int y = 2; y < img.getHeight() - 2; y++) {
            // Linha
            //JC.setMessage("Analysing line " + y + " of " + img.getHeight());
            for (int x = 2; x < img.getWidth() - 2; x++) {
                // Coluna
                data.getPixel(x, y, color);
                if (color[0] >= th) {
                    pqueue.add(new Point(x, y));
                    data.setPixel(x, y, cor);
                    min[0] = x;
                    min[1] = y;
                    max[0] = x;
                    max[1] = y;

                    ret[0] = x;
                    ret[1] = y;
                    ret[3] = 1;

                    while (!pqueue.isEmpty()) {
                        Point p = pqueue.removeFirst();
                        ret[0] += p.x; // SUM(Xi)

                        ret[1] += p.y; // SUM(Yj)

                        ret[3]++; // Area

                        if (p.x < min[0]) {
                            min[0] = p.x;
                        }
                        if (p.y < min[1]) {
                            min[1] = p.y;
                        }
                        if (p.x > max[0]) {
                            max[0] = p.x;
                        }
                        if (p.y > max[1]) {
                            max[1] = p.y;
                        }
                        for (int y1 = -1; y1 <= 1; y1++) {
                            // Linha
                            for (int x1 = -1; x1 <= 1; x1++) {
                                // Coluna
                                try {
                                    data.getPixel(p.x + x1, p.y + y1, color);
                                    if (color[0] >= th) {
                                        pqueue.add(new Point(p.x + x1, p.y + y1));
                                        data.setPixel(p.x + x1, p.y + y1, cor);
                                    }
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                    float xc = ret[0] / ret[3];//min[0]+(max[0]-min[0])/2; // ret[0] / ret[3];

                    float yc = ret[1] / ret[3];//min[1]+(max[1]-min[1])/2; // ret[1] / ret[3];

                    float rc = ((max[0] - min[0]) + (max[1] - min[1])) / 4;

                    if (rc > 10) {
                        circulos.add(new Circulo(xc, yc, rc));
                    }
                }
            }
        }

        try {
            Object[] r = circulos.toArray();

            Arrays.sort(r);
            //int t = 0;

            Ball[0] = ((Circulo) r[0]).getX();
            Ball[1] = ((Circulo) r[0]).getY();
            Ball[2] = ((Circulo) r[0]).getR();

        } catch (Exception e) {
            //e.printStackTrace();
        }
        return Ball;
    }
}
