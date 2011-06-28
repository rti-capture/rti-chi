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

import java.awt.image.*;
import java.util.ArrayList;
import java.util.Arrays;

public class ImageFilters {

    public static float gaussFunction(int x, int y, float r) {
        return (float) ((1.0f / (2.0f * Math.PI * r * r)) * Math.exp(-(x * x + y * y) / (2.0f * r * r)));
    }

    public static BufferedImage gaussFilter(BufferedImage img, int size, float std) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);//copyGray(img);

        float gaussMatrix[] = new float[size * size];

        for (int k1 = -size / 2; k1 <= size / 2; k1++) {
            for (int k2 = -size / 2; k2 <= size / 2; k2++) {
                gaussMatrix[(k1 + size / 2) * size + (k2 + size / 2)] = gaussFunction(k1, k2, std);
            }
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int sum = 0;
                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        if ((x + i) >= 0 && (y + j) >= 0 && (x + i) < img.getWidth() && (y + j) < img.getHeight()) {
                            img.getRaster().getPixel(x + i, y + j, color);
                        } else {
                            img.getRaster().getPixel(x, y, color);

                        }

                        sum += color[0];

                    }
                }

                int color[] = new int[1];

                color[0] = sum / (size * size);

                ret.getRaster().setPixel(x, y, color);

            }
        }


        return ret;
    }

    public static BufferedImage DoGfilter(BufferedImage img, int size, float std) {
        BufferedImage img_g1 = gaussFilter(img, (int) Math.sqrt(size), std);
        BufferedImage img_g2 = gaussFilter(img, size, std);

        return subtractImages(img_g1, img_g2);

    }

    public static float laplaceFunction(int x, int y, float r) {
        return (float) ((-1.0f / (Math.PI * r * r * r * r)) * (1.0f - (x * x + y * y) / (2.0f * r * r)) * Math.exp(-(x * x + y * y) / (2 * r * r)));
    }

    public static BufferedImage laplaceFilter(BufferedImage img, int size, float dp) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);//copyGray(img);

        float laplaceMatrix[] = new float[size * size];

        for (int k1 = -size / 2; k1 <= size / 2; k1++) {
            for (int k2 = -size / 2; k2 <= size / 2; k2++) {
                laplaceMatrix[(k1 + size / 2) * size + (k2 + size / 2)] = laplaceFunction(k1, k2, dp);
            }
        }

        laplaceMatrix[(size / 2) * size + size / 2] = 1.0f + laplaceMatrix[(size / 2) * size + size / 2];

        Kernel k = new Kernel(size, size, laplaceMatrix);

        ConvolveOp cop = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);

        cop.filter(img, ret);

        return ret;
    }

    public static BufferedImage medianFilter(BufferedImage img, int size) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = size / 2; y < img.getHeight() - size / 2; y++) {
            for (int x = size / 2; x < img.getWidth() - size / 2; x++) {
                int c[] = {0};
                ret.getRaster().setPixel(x, y, c);
            }
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int c[] = new int[size * size];
                int k = 0;
                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        if (x + i >= 0 && y + j >= 0 && x + i < img.getWidth() && y + j < img.getHeight()) {
                            img.getRaster().getPixel(x + i, y + j, color);
                        } else {
                            img.getRaster().getPixel(x, y, color);

                        }

                        c[k] = color[0];
                        k++;
                    }
                }

                Arrays.sort(c);
                int color[] = new int[1];

                color[0] = c[(size / 2) * size + size / 2];

                ret.getRaster().setPixel(x, y, color);

            }
        }
        return ret;
    }

    public static BufferedImage varianceFilter(BufferedImage img, int size) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        float[][] arrVar = new float[img.getWidth()][img.getHeight()];
        float max = 0.0f;

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                float mean = 0.0f;
                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        if (x + i >= 0 && y + j >= 0 && x + i < img.getWidth() && y + j < img.getHeight()) {
                            img.getRaster().getPixel(x + i, y + j, color);
                        } else {
                            img.getRaster().getPixel(x, y, color);

                        }

                        mean += (float) color[0];
                    }
                }



                float total = (float) (size * size);
                mean /= total;
                float var = 0.0f;

                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        if (x + i >= 0 && y + j >= 0 && x + i < img.getWidth() && y + j < img.getHeight()) {
                            img.getRaster().getPixel(x + i, y + j, color);
                        } else {
                            img.getRaster().getPixel(x, y, color);

                        }
                        float local = (float) color[0];
                        float v = (local - mean);
                        var += v * v;

                    }
                }

                arrVar[x][y] = var / total;

                if (arrVar[x][y] > max) {
                    max = arrVar[x][y];
                }
            }
        }


        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = {Math.round((arrVar[x][y] / max) * 255)};
                ret.getRaster().setPixel(x, y, color);
            }
        }


        return ret;
    }

    public static BufferedImage modeFilter(BufferedImage img, int size) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        int H[] = new int[256];
        int max = 0;
        for (int i = 0; i < 256; i++) {
            H[0] = 0;
        }
        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];

                img.getRaster().getPixel(x, y, color);
                ret.getRaster().setPixel(x, y, color);
            }
        }
        int mfColor[] = new int[1];
        for (int y = (size / 2); y < img.getHeight() - (size / 2); y++) {
            for (int x = (size / 2); x < img.getWidth() - (size / 2); x++) {
                //int c[] = new int[size * size];
                for (int i = 0; i < 256; i++) {
                    H[0] = 0;
                }
                max = 0;
                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        img.getRaster().getPixel(x + i, y + j, color);
                        H[color[0]]++;
                        if (H[color[0]] > max) {
                            mfColor[0] = color[0];
                            max = H[color[0]];
                        }

                    }
                }

                ret.getRaster().setPixel(x, y, mfColor);

            }
        }
        return ret;
    }

    public static BufferedImage medianScale(BufferedImage img, int size) {
        int inc = size / 2;
        BufferedImage ret = new BufferedImage(img.getWidth() / inc, img.getHeight() / inc, BufferedImage.TYPE_BYTE_GRAY);

        for (int y = size / 2; y < img.getHeight() - size / 2; y += inc) {
            for (int x = size / 2; x < img.getWidth() - size / 2; x += inc) {
                int c[] = new int[size * size];
                int k = 0;
                for (int j = -(size / 2); j < (size / 2); j++) {
                    for (int i = -(size / 2); i < (size / 2); i++) {
                        int color[] = new int[1];
                        img.getRaster().getPixel(x + i, y + j, color);
                        c[k] = color[0];
                        k++;
                    }
                }

                Arrays.sort(c);
                int color[] = new int[1];

                color[0] = c[(size / 2) * size + size / 2];

                ret.getRaster().setPixel(x / inc, y / inc, color);
            }
        }



        return ret;
    }

    public static BufferedImage medianBlend(ArrayList<BufferedImage> IMGS) {
        if (IMGS != null) {

            for (int i = 0; i < IMGS.size(); i++) {
                try {
                    IMGS.set(i, ImageProcessing.convertToGrayscale(IMGS.get(i)));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            BufferedImage imgRet = new BufferedImage(IMGS.get(0).getWidth(), IMGS.get(0).getHeight(), BufferedImage.TYPE_BYTE_GRAY);
            for (int y = 0; y < imgRet.getHeight(); y++) {
                for (int x = 0; x < imgRet.getWidth(); x++) {
                    int color[] = new int[1];
                    int array[] = new int[IMGS.size()];
                    for (int i = 0; i < IMGS.size(); i++) {
                        try {
                            IMGS.get(i).getRaster().getPixel(x, y, color);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        array[i] = color[0];
                    }
                    Arrays.sort(array);
                    color[0] = array[IMGS.size() / 2];
                    imgRet.getRaster().setPixel(x, y, color);
                }
            }
            return imgRet;
        }
        return null;
    }

    public static BufferedImage laplaceEdgeFilter(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);//copyGray(img);

        for (int y = 1; y < img.getHeight() - 1; y++) {
            for (int x = 1; x < img.getWidth() - 1; x++) {
                int a1 = 0, a2 = 0, a3 = 0;
                int b1 = 0, b2 = 0, b3 = 0;
                int c1 = 0, c2 = 0, c3 = 0;
                int color[] = {0};
                img.getRaster().getPixel(x - 1, y - 1, color);
                a1 = color[0];
                img.getRaster().getPixel(x, y - 1, color);
                a2 = color[0];
                img.getRaster().getPixel(x + 1, y - 1, color);
                a3 = color[0];

                img.getRaster().getPixel(x - 1, y, color);
                b1 = color[0];
                img.getRaster().getPixel(x, y, color);
                b2 = color[0];
                img.getRaster().getPixel(x + 1, y, color);
                b3 = color[0];

                img.getRaster().getPixel(x - 1, y + 1, color);
                c1 = color[0];
                img.getRaster().getPixel(x, y + 1, color);
                c2 = color[0];
                img.getRaster().getPixel(x + 1, y + 1, color);
                c3 = color[0];

                int v = 8 * b2 - (a1 + a2 + a3 + b1 + b3 + c1 + c2 + c3);

                int a[] = {v};

                ret.getRaster().setPixel(x, y, a);

            }
        }
        return ret;
    }

    public static BufferedImage sobelFilter(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);//copyGray(img);

        for (int y = 1; y < img.getHeight() - 1; y++) {
            for (int x = 1; x < img.getWidth() - 1; x++) {
                int a1 = 0, a2 = 0, a3 = 0;
                @SuppressWarnings("unused")
				int b1 = 0, b2 = 0, b3 = 0;
                int c1 = 0, c2 = 0, c3 = 0;
                int color[] = {0};
                img.getRaster().getPixel(x - 1, y - 1, color);
                a1 = color[0];
                img.getRaster().getPixel(x, y - 1, color);
                a2 = color[0];
                img.getRaster().getPixel(x + 1, y - 1, color);
                a3 = color[0];

                img.getRaster().getPixel(x - 1, y, color);
                b1 = color[0];
                img.getRaster().getPixel(x, y, color);
                b2 = color[0];
                img.getRaster().getPixel(x + 1, y, color);
                b3 = color[0];

                img.getRaster().getPixel(x - 1, y + 1, color);
                c1 = color[0];
                img.getRaster().getPixel(x, y + 1, color);
                c2 = color[0];
                img.getRaster().getPixel(x + 1, y + 1, color);
                c3 = color[0];

                int v = 0;
                int fx = -(a1 + 2 * a2 + a3) + (c1 + 2 * c2 + c3);
                int fy = -(a1 + 2 * b1 + c1) + (a3 + 2 * b3 + c3);
                //int fxy = -(a2 + 2 * a1 + b1) + (b3 + 2 * c3 + c2);
                //int fyx = -(a2 + 2 * a3 + b3) + (b2 + 2 * c1 + c2);
                v = (int) Math.round(Math.sqrt(fx * fx + fy * fy));




                float at = (v != 0) ? (float) (Math.acos((float) fy / (float) v) / Math.PI) : 0.0f;
                float bt = (v != 0) ? (float) (Math.asin((float) fx / (float) v) / Math.PI) : 0.0f;

                v = (int) (Math.abs(fx) + Math.abs(fy));
                if (v > 255) {
                    v = 255;
                }

                //int k = (v + Math.round(at * 127) + 127 + Math.round(bt * 127) + 127) / 3;

                int a[] = {v, Math.round(at * 127) + 127, Math.round(bt * 127) + 127};

                ret.getRaster().setPixel(x, y, a);

            }
        }
        return ret;
    }

    public static BufferedImage sobelMagFilter(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);//copyGray(img);

        for (int y = 1; y < img.getHeight() - 1; y++) {
            for (int x = 1; x < img.getWidth() - 1; x++) {
                int a1 = 0, a2 = 0, a3 = 0;
                @SuppressWarnings("unused")
				int b1 = 0, b2 = 0, b3 = 0;
                int c1 = 0, c2 = 0, c3 = 0;
                int color[] = {0};
                img.getRaster().getPixel(x - 1, y - 1, color);
                a1 = color[0];
                img.getRaster().getPixel(x, y - 1, color);
                a2 = color[0];
                img.getRaster().getPixel(x + 1, y - 1, color);
                a3 = color[0];

                img.getRaster().getPixel(x - 1, y, color);
                b1 = color[0];
                img.getRaster().getPixel(x, y, color);
                b2 = color[0];
                img.getRaster().getPixel(x + 1, y, color);
                b3 = color[0];

                img.getRaster().getPixel(x - 1, y + 1, color);
                c1 = color[0];
                img.getRaster().getPixel(x, y + 1, color);
                c2 = color[0];
                img.getRaster().getPixel(x + 1, y + 1, color);
                c3 = color[0];

                int v = 0;
                int fx = -(a1 + 2 * a2 + a3) + (c1 + 2 * c2 + c3);
                int fy = -(a1 + 2 * b1 + c1) + (a3 + 2 * b3 + c3);
                //int fxy = -(a2 + 2 * a1 + b1) + (b3 + 2 * c3 + c2);
                //int fyx = -(a2 + 2 * a3 + b3) + (b2 + 2 * c1 + c2);
                v = (int) Math.round(Math.sqrt(fx * fx + fy * fy));




                //float at = (v != 0) ? (float) (Math.acos((float) fy / (float) v) / Math.PI) : 0.0f;
                //float bt = (v != 0) ? (float) (Math.asin((float) fx / (float) v) / Math.PI) : 0.0f;

                v = (int) (Math.abs(fx) + Math.abs(fy));
                if (v > 255) {
                    v = 255;
                }

                //int k = (v + Math.round(at * 127) + 127 + Math.round(bt * 127) + 127) / 3;

                int a[] = {v}; //, Math.round(at * 127) + 127, Math.round(bt * 127) + 127};

                ret.getRaster().setPixel(x, y, a);

            }
        }
        return ret;
    }

    public static BufferedImage subtractImages(BufferedImage img1, BufferedImage img2) {
        BufferedImage img_ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int color1[] = new int[1];
                int color2[] = new int[1];
                int colorr[] = new int[1];

                img1.getRaster().getPixel(x, y, color1);
                img2.getRaster().getPixel(x, y, color2);

                colorr[0] = Math.abs(color1[0] - color2[0]);

                img_ret.getRaster().setPixel(x, y, colorr);

            }
        }

        return img_ret;
    }

    public static BufferedImage addImages(BufferedImage img1, BufferedImage img2) {
        BufferedImage img_ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int color1[] = new int[1];
                int color2[] = new int[1];
                int colorr[] = new int[1];

                img1.getRaster().getPixel(x, y, color1);
                img2.getRaster().getPixel(x, y, color2);

                colorr[0] = Math.abs(color1[0] + color2[0]);

                img_ret.getRaster().setPixel(x, y, colorr);

            }
        }

        return img_ret;
    }

    public static BufferedImage multImages(BufferedImage img1, BufferedImage img2) {
        BufferedImage img_ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int color1[] = new int[1];
                int color2[] = new int[1];
                int colorr[] = new int[1];

                img1.getRaster().getPixel(x, y, color1);
                img2.getRaster().getPixel(x, y, color2);

                colorr[0] = Math.round(((float) color1[0] / 255.0f * (float) color2[0] / 255.0f) * 255);

                img_ret.getRaster().setPixel(x, y, colorr);

            }
        }

        return img_ret;
    }

    public static BufferedImage factorImage(BufferedImage img1, int value) {
        BufferedImage img_ret = new BufferedImage(img1.getWidth(), img1.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        for (int y = 0; y < img1.getHeight(); y++) {
            for (int x = 0; x < img1.getWidth(); x++) {
                int color1[] = new int[1];
                int colorr[] = new int[1];

                img1.getRaster().getPixel(x, y, color1);

                colorr[0] = color1[0] * value;

                if (colorr[0] > 255) {
                    colorr[0] = 255;
                }

                img_ret.getRaster().setPixel(x, y, colorr);

            }
        }

        return img_ret;
    }

    public static BufferedImage normalizeFilter(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);

        int max = 0;
        int min = 255;


        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];
                img.getRaster().getPixel(x, y, color);
                if (color[0] > max) {
                    max = color[0];
                }
                if (color[0] < min) {
                    min = color[0];
                }
            }
        }



        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];
                int colorr[] = new int[1];

                img.getRaster().getPixel(x, y, color);


                colorr[0] = Math.round((((float) color[0] - (float) min) / (float) max) * 255.0f);
                ret.getRaster().setPixel(x, y, colorr);
            }
        }

        return ret;
    }

    public static BufferedImage histEqualize(BufferedImage img) {
        BufferedImage ret = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        int H[] = new int[256];
        float Hp[] = new float[256];
        float HP[] = new float[256];

        for (int i = 0; i < 256; i++) {
            H[i] = 0;
        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];
                img.getRaster().getPixel(x, y, color);
                H[color[0]]++;
            }
        }

        for (int i = 0; i < 256; i++) {
            Hp[i] = (float) ((float) H[i] / (float) (img.getWidth() * img.getHeight()));

        }
        HP[0] = Hp[0];
        for (int i = 1; i < 256; i++) {
            HP[i] = Hp[i] + HP[i - 1];

        }

        for (int y = 0; y < img.getHeight(); y++) {
            for (int x = 0; x < img.getWidth(); x++) {
                int color[] = new int[1];
                int colorr[] = new int[1];

                img.getRaster().getPixel(x, y, color);


                colorr[0] = Math.round(HP[color[0]] * 255.0f);
                ret.getRaster().setPixel(x, y, colorr);
            }
        }

        return ret;
    }
}
