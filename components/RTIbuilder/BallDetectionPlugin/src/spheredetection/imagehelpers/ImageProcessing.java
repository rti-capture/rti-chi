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

import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.awt.image.renderable.ParameterBlock;
import javax.media.jai.InterpolationBicubic2;
import javax.media.jai.JAI;

public class ImageProcessing {

    public static BufferedImage convertToGrayscale(BufferedImage source) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(source, null);
    }

    static public BufferedImage createScaleOp(RenderedImage src, float factor) {
        ParameterBlock pb = new ParameterBlock();
        pb.addSource(src);
        pb.add(factor);
        pb.add(factor);
        pb.add(1.0F);
        pb.add(1.0F);
        pb.add(new InterpolationBicubic2(10));
        return JAI.create("scale", pb).getAsBufferedImage();
    }

    static public BufferedImage RGBtoHSB(BufferedImage source) {
        BufferedImage result = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        int[] black = {0, 0, 0};
        for (int i = 0; i < source.getWidth(); i++) {
            for (int j = 0; j < source.getHeight(); j++) {
                source.getRaster().getPixel(i, j, black);
                float[] hsb = java.awt.Color.RGBtoHSB(black[0], black[1], black[2], null);
                for (int k = 0; k < 3; k++) {
                    black[k] = (int) (hsb[2] * 1.2f * 255);
                }
                result.getRaster().setPixel(i, j, black);
            }
        }
        return result;
    }

    static public BufferedImage clearOutside(BufferedImage source, float[] circle) {
        int[] black = {0};
        for (int i = 0; i < source.getWidth(); i++) {
            for (int j = 0; j < source.getHeight(); j++) {
                float x = (float) (i - source.getWidth() / 2);
                float y = (float) (j - source.getHeight() / 2);
                if (Math.sqrt(x * x + y * y) > 0.95 * circle[2]) {
                    source.getRaster().setPixel(i, j, black);
                }
            }
        }

        return source;
    }

    
    private static int[] tryToEnter(int pixel, int[] max, int sizeOfMax) {
        int i;
        for (i = (sizeOfMax - 1); i >= 0; i--) {
            if (pixel > max[i]) {
                max = moveOneLeft(max, i);
                max[i] = pixel;
                i = -1;
            }
        }
        return max;
    }

    private static int[] moveOneLeft(int[] array, int i) {
        int j;
        for (j = 0; j < i; j++) {
            array[j] = array[j + 1];
        }
        return array;
    }

    public static int guessThreshold(BufferedImage image, float precentage) {
        int threshold = 0;
        int width = image.getWidth();
        int height = image.getHeight();
        int i;
        int j;
        int pixel;

        int sizeOfMax = (int) (precentage * width * height);
        int[] max = new int[sizeOfMax];


        for (i = 0; i < sizeOfMax; i++) {
            max[i] = 0;
        }
        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                pixel = image.getRGB(i, j) & 0xff;
                max = tryToEnter(pixel, max, sizeOfMax);
            }
        }
        threshold = max[0];
        return threshold;
    }

    public static BufferedImage autoBinarization(BufferedImage image) {

        int threshold;
        int width = image.getWidth();
        int height = image.getHeight();
        int i;
        int j;
        int[] black = {0};
        int[] white = {255};

        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        threshold = guessThreshold(image, 0.01f);


        for (i = 0; i < width; i++) {
            for (j = 0; j < height; j++) {
                if ((image.getRGB(i, j) & 0xff) >= threshold) {
                    output.getRaster().setPixel(i, j, white);
                } else {
                    output.getRaster().setPixel(i, j, black);
                }
            }
        }
        return output;
    }

    public static BufferedImage binarization(BufferedImage image, int threshold) {
        int width = image.getWidth();
        int height = image.getHeight();
        int x;
        int y;
        int[] black = {0};
        int[] white = {255};
        int[] pcolor = {0};
        BufferedImage output = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (y = 0; y < height; y++) {
            //linha
            for (x = 0; x < width; x++) {
                //coluna
                image.getRaster().getPixel(x, y, pcolor);
                if (pcolor[0] >= threshold) {
                    output.getRaster().setPixel(x, y, white);
                } else {
                    output.getRaster().setPixel(x, y, black);
                }
            }
        }
        return output;
    }

    public static BufferedImage skeleton(BufferedImage pic) {


        int IMG_X = pic.getWidth();
        int IMG_Y = pic.getHeight();
        // vizinhança do ponto
        final int[][] viz = {{-1, 0}, {-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}};
        int removidos = 1;
        int iter = 0;
        int[] black = {0};
        int[] white = {255};
        int[] tmp = {0};
        BufferedImage npic = new BufferedImage(IMG_X, IMG_Y, BufferedImage.TYPE_BYTE_GRAY); // resultado da iteração actual

        while (removidos > 0) {
            removidos = 0;

            // 1ª passagem
            for (int i = 1; i < IMG_X - 1; i++) {
                for (int j = 1; j < IMG_Y - 1; j++) {
                    if ((pic.getRGB(i, j) & 0xff) == 255) {
                        // pixels brancos
                        int np1 = 0; // np1 = vizinhos a negro

                        for (int w = 0; w < 8; w++) {
                            if ((pic.getRGB(i + viz[w][0], j + viz[w][1]) & 0xff) == 255) {
                                np1++;
                            }
                        }
                        int sp1 = 0; // sp1 = transições 0-1 na vizinhança

                        for (int w = 0; w < 7; w++) {
                            if ((pic.getRGB(i + viz[w][0], j + viz[w][1]) & 0xff) == 0 && (pic.getRGB(i + viz[w + 1][0], j + viz[w + 1][1]) & 0xff) == 255) {
                                sp1++;
                            }
                        }
                        if ((pic.getRGB(i + viz[7][0], j + viz[7][1]) & 0xff) == 0 && (pic.getRGB(i + viz[0][0], j + viz[0][1]) & 0xff) == 255) {
                            sp1++;
                        }
                        boolean cond1 = ((pic.getRGB(i + 1, j) & 0xff) == 0) || ((pic.getRGB(i, j + 1) & 0xff) == 0) || ((pic.getRGB(i, j - 1) & 0xff) == 0 && (pic.getRGB(i - 1, j) & 0xff) == 0);
                        //boolean cond2 = (pic[i][j-1]==0) || (pic[i-1][j]==0) || (pic[i+1][j]==0&&pic[i][j+1]==0);
                        if (np1 >= 2 && np1 <= 6 && sp1 == 1 && cond1) {
                            npic.getRaster().setPixel(i, j, black);
                            removidos++;
                        } else {
                            npic.getRaster().setPixel(i, j, white);
                        }
                    } else {
                        npic.getRaster().setPixel(i, j, black);
                    }
                }
            }
            // actualiza imagem
            for (int i = 1; i < IMG_X - 1; i++) {
                for (int j = 1; j < IMG_Y - 1; j++) {
                    tmp[0] = npic.getRGB(i, j);
                    pic.getRaster().setPixel(i, j, tmp);
                }
            }
            // 2ª passagem
            for (int i = 1; i < IMG_X - 1; i++) {
                for (int j = 1; j < IMG_Y - 1; j++) {
                    if ((pic.getRGB(i, j) & 0xff) == 255) {
                        // pixels negros
                        int np1 = 0; // np1 = vizinhos a negro

                        for (int w = 0; w < 8; w++) {
                            if ((pic.getRGB(i + viz[w][0], j + viz[w][1]) & 0xff) == 255) {
                                np1++;
                            }
                        }
                        int sp1 = 0; // sp1 = transições 0-1 na vizinhança

                        for (int w = 0; w < 7; w++) {
                            if ((pic.getRGB(i + viz[w][0], j + viz[w][1]) & 0xff) == 0 && (pic.getRGB(i + viz[w + 1][0], j + viz[w + 1][1]) & 0xff) == 255) {
                                sp1++;
                            }
                        }
                        if ((pic.getRGB(i + viz[7][0], j + viz[7][1]) & 0xff) == 0 && (pic.getRGB(i + viz[0][0], j + viz[0][1]) & 0xff) == 255) {
                            sp1++;
                        }
                        //boolean cond1 = (pic[i+1][j]==0) || (pic[i][j+1]==0) || (pic[i][j-1]==0&&pic[i-1][j]==0);
                        boolean cond2 = ((pic.getRGB(i, j - 1) & 0xff) == 0) || ((pic.getRGB(i - 1, j) & 0xff) == 0) || ((pic.getRGB(i + 1, j) & 0xff) == 0 && (pic.getRGB(i, j + 1) & 0xff) == 0);

                        if (np1 >= 2 && np1 <= 6 && sp1 == 1 && cond2) {
                            npic.getRaster().setPixel(i, j, black);
                            removidos++;
                        } else {
                            npic.getRaster().setPixel(i, j, white);
                        }
                    } else {
                        npic.getRaster().setPixel(i, j, black);
                    }
                }
            }
            // actualiza imagem
            for (int i = 1; i < IMG_X - 1; i++) {
                for (int j = 1; j < IMG_Y - 1; j++) {
                    tmp[0] = npic.getRGB(i, j);
                    pic.getRaster().setPixel(i, j, tmp);
                }
            }
            iter++;
        }
        return pic;
    }

    public static BufferedImage copyGray(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img.getHeight() - 0; y++) {
            for (int x = 0; x < img.getWidth() - 0; x++) {
                int color[] = new int[1];
                img.getRaster().getPixel(x, y, color);
                ret.getRaster().setPixel(x, y, color);
            }
        }

        return ret;

    }

    public static BufferedImage invGray(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img.getHeight() - 0; y++) {
            for (int x = 0; x < img.getWidth() - 0; x++) {
                int color[] = new int[1];



                img.getRaster().getPixel(x, y, color);

                color[0] = 255 - color[0];

                ret.getRaster().setPixel(x, y, color);
            }
        }

        return ret;

    }

    public static BufferedImage avgGray(BufferedImage img1, BufferedImage img2) {
        BufferedImage ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight() - 0; y++) {
            for (int x = 0; x < img1.getWidth() - 0; x++) {
                int color1[] = new int[1];
                int color2[] = new int[1];
                img1.getRaster().getPixel(x, y, color1);
                img2.getRaster().getPixel(x, y, color2);

                color1[0] = Math.round(color1[0] + color2[0]) / 2;

                ret.getRaster().setPixel(x, y, color1);
            }
        }

        return ret;

    }

    public static BufferedImage multGray(BufferedImage img1, BufferedImage img2) {
        BufferedImage ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight() - 0; y++) {
            for (int x = 0; x < img1.getWidth() - 0; x++) {
                int color1[] = new int[1];
                int color2[] = new int[1];
                img1.getRaster().getPixel(x, y, color1);
                img2.getRaster().getPixel(x, y, color2);

                color1[0] = Math.round(color1[0] * ((float) color2[0]) / 256.0f);

                ret.getRaster().setPixel(x, y, color1);
            }
        }

        return ret;

    }

    public static BufferedImage clearBorder(BufferedImage img, int size) {

        BufferedImage imgRet = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);


        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];
                img.getRaster().getPixel(x, y, color);
                color[0] = (x < size * 2 || x > img.getWidth() - size * 2 || y < size * 2 || y > img.getHeight() - size * 2) ? 0 : color[0];
                imgRet.getRaster().setPixel(x, y, color);
            }
        }

        return imgRet;

    }

    public static void blendFilter(BufferedImage pic, BufferedImage pic1) {

        int Xdim = pic1.getWidth();
        int Ydim = pic1.getHeight();

        int[] tmp = new int[3];
        int[] tmp1 = new int[3];
        int[] tmp2 = new int[3];

        if (pic == null) {
            pic = new BufferedImage(Xdim, Ydim, pic1.getType());
        }
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {
                pic.getRaster().getPixel(j, i, tmp1);
                pic1.getRaster().getPixel(j, i, tmp2);
                tmp[0] = Math.max(tmp1[0], tmp2[0]);
                tmp[1] = Math.max(tmp1[1], tmp2[1]);
                tmp[2] = Math.max(tmp1[2], tmp2[2]);
                pic.getRaster().setPixel(j, i, tmp);
            }
        }
    }

    public static void blendFilterGray(BufferedImage pic, BufferedImage pic1) {

        int Xdim = pic1.getWidth();
        int Ydim = pic1.getHeight();

        int[] tmp = new int[1];
        int[] tmp1 = new int[1];
        int[] tmp2 = new int[1];


        if (pic == null) {
            pic = new BufferedImage(Xdim, Ydim, pic1.getType());
        }
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {
                pic.getRaster().getPixel(j, i, tmp1);
                pic1.getRaster().getPixel(j, i, tmp2);
                tmp[0] = Math.max(tmp1[0], tmp2[0]);
                pic.getRaster().setPixel(j, i, tmp);
            }
        }
    }

    public static BufferedImage selectRed(BufferedImage pic1) {

        int Xdim = pic1.getWidth();
        int Ydim = pic1.getHeight();
        int[] tmp = new int[1];
        int[] tmp1 = new int[3];
        BufferedImage pic = new BufferedImage(Xdim, Ydim, BufferedImage.TYPE_BYTE_GRAY);
        for (int i = 0; i < Ydim; i++) {
            for (int j = 0; j < Xdim; j++) {


                pic1.getRaster().getPixel(j, i, tmp1);

                if (tmp1[0] > tmp1[1] * 3.0 && tmp1[0] > tmp1[2] * 3.0) {
                    tmp[0] = tmp1[0];
                } else {
                    tmp[0] = 0;
                }
                pic.getRaster().setPixel(j, i, tmp);
            }
        }
        return pic; 

    }


}

