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
package guicomponents;

import guicomponents.exceptions.AreaNotDefined;
import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * This class extends AreaSelectionPanel and adds Crop functionalities like ImageCropPanel.
 * It will not suport free crop for now.
 * 
 */
public class AreaSelectionPanelRefined extends AreaSelectionPanel {

    /**Crop Area*/
    private ArrayList<Point> cropPoints;
    private ArrayList<Rectangle> vertexList;

    // Crop Styles - constants
    // <editor-fold defaultstate="collapsed" desc="Crop Style Constant declarations - CropType variable">
    /**Crop type with a rectangular area*/
    public static final int RECTANGULAR_CROP = 1;
    /**Crop type with a polygon area*/
    public static final int FREE_CROP = 2;
    //</editor-fold>

    /**Crop type used in the panel*/
    private int CropType;

    // <editor-fold defaultstate="collapsed" desc="Drawing Mode Constant declarations - freeDrawMode variable">
    protected static final int NOTHING = 0;
    protected static final int MOVING = 1;
    protected static final int CHANGING_SHAPE = 2;
    //</editor-fold>

    protected int freeDrawMode;
    protected int vertexPivot;
    protected boolean definedArea = false;

    public AreaSelectionPanelRefined()
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        super();
        this.vertexList = new ArrayList<Rectangle>();
        this.cropPoints = new ArrayList<Point>();
        this.CropType = RECTANGULAR_CROP;
        this.freeDrawMode = NOTHING;
    }
    // </editor-fold>

    /**
     *Sets crop points in panel and repaints it.<p>
     *@param Crop_points a array list of points representing a crop area.
     */
    public void setCropPoints(ArrayList<Point> Crop_points)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        if (CropType == FREE_CROP) {
            this.cropPoints = Crop_points;
        } else {
            if (Crop_points.size() == 4) {
                origin_point = Crop_points.get(0);
                end_point = Crop_points.get(2);
                definedArea = true;
            } else {
                definedArea = false;
            }
        }

    }
    // </editor-fold>

    @Deprecated
    public void addToSelectedAreas(ArrayList<Point> Crop_points)
    {
        Point[] pts = this.getBoundsFromPoints(Crop_points);

        Rectangle rect = new Rectangle(pts[0].x, pts[0].y, pts[1].x - pts[0].x, pts[1].y - pts[0].y);
        this.selectedAreas.add(rect);
    }

    @Override
    public void cleanCropArea()
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        super.cleanCropArea();
        origin_point = new Point();
        end_point = new Point();
        this.cropPoints = new ArrayList<Point>();
        definedArea = false;
        selectedAreas = new ArrayList<Rectangle>();
        draw_new_areas = true;
        repaint();
    }
    // </editor-fold>

    /**
     *Get the panel current crop area through an array of points.<br>If the area is not closed a exception is thrown.
     * <br>If none area is defined, an empty arraylist is returned.
     *@return ArrayList of Points with the selected area.
     *@exception AreaNotDefined
     */
    public ArrayList<Point> getCropPoints() throws AreaNotDefined
    // <editor-fold defaultstate="collapsed" desc="Code">
    {

        ArrayList<Point> crop_points = new ArrayList<Point>();

        if(!cropPoints.isEmpty() || CropType == RECTANGULAR_CROP){
            if (CropType == FREE_CROP) {
                for (Point p : cropPoints) {
                    int x = p.x;
                    int y = p.y;
                    crop_points.add(new Point(x, y));
                }
                if (!cropPoints.get(0).equals(cropPoints.get(cropPoints.size() - 1))) {
                    throw new AreaNotDefined("The area is not closed");
                }
            } else {
                if (origin_point.equals(end_point)) {
                    return crop_points;
                }

                int min_x = (int) (Math.min(origin_point.x, end_point.x));
                int min_y = (int) (Math.min(origin_point.y, end_point.y));
                int max_x = (int) (Math.max(origin_point.x, end_point.x));
                int max_y = (int) (Math.max(origin_point.y, end_point.y));
                crop_points.add(new Point(min_x, min_y));
                crop_points.add(new Point(max_x, min_y));
                crop_points.add(new Point(max_x, max_y));
                crop_points.add(new Point(min_x, max_y));
            }

        } else {
            System.out.println("POSSIBLE BUG WARNING: AreaSelectionPanelRefined.getCropPoints called and no crop points are set!!");
        }
        return crop_points;

    }
    // </editor-fold>

    @Override
    public void formMouseDragged(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {

        switch (CropType) {

            case RECTANGULAR_CROP:

                formMouseDraggedRect(evt);

                break;

            case FREE_CROP:
                formMouseDraggedFree(evt);
                break;

            default:
                System.out.println("Probably wrong type of Crop?");
                break;

        }

    }
    //</editor-fold>

    public void formMouseDraggedRect(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        if (((evt.getModifiersEx() & java.awt.event.InputEvent.BUTTON1_DOWN_MASK) == java.awt.event.InputEvent.BUTTON1_DOWN_MASK) && draw_new_areas) {

            //Make sure that mouse position in the image

            System.out.println(selectedAreas.toString());

            int x = evt.getX(), y = evt.getY();
            x = (x < 0) ? 0 : x;
            y = (y < 0) ? 0 : y;
            x = (x > resized_width) ? resized_width : x;
            y = (y > resized_height) ? resized_height : y;

            //Work on real coordinates
            int real_x = (int) (x / resize_factor);
            int real_y = (int) (y / resize_factor);
            Point p = new Point(real_x, real_y);

            //If mouse on the upper left corner, change selected area size
            if (super.draw_mode == super.LU_CORNER) {
                Rectangle area = selectedAreas.get(current_area);
                //Get area end point
                int end_x = area.x + area.width;
                int end_y = area.y + area.height;
                //Where is the new left upper corner
                int min_x = Math.min(real_x, end_x),
                        min_y = Math.min(real_y, end_y);
                //Store the edited area
                selectedAreas.set(current_area, new Rectangle(min_x, min_y, end_x - min_x, end_y - min_y));
                changed = true;
                this.origin_point = new Point(selectedAreas.get(current_area).getLocation());
                this.end_point = new Point(selectedAreas.get(current_area).x + end_x - min_x,
                        selectedAreas.get(current_area).y + end_y - min_y);
                repaint();
            }

            //Mouse on the upper right corner
            if (draw_mode == RU_CORNER) {
                Rectangle area = selectedAreas.get(current_area);
                int end_y = area.y + area.height;
                //See where is the right upper corner
                int min_y = Math.min(real_y, end_y),
                        max_x = Math.max(area.x, real_x);
                selectedAreas.set(current_area, new Rectangle(area.x, min_y, max_x - area.x, end_y - min_y));
                changed = true;
                this.origin_point = new Point(selectedAreas.get(current_area).getLocation());
                this.end_point = new Point(selectedAreas.get(current_area).x + max_x - area.x,
                        selectedAreas.get(current_area).y + end_y - min_y);
                repaint();

            }
            //Mouse on the down left corner
            if (draw_mode == LD_CORNER) {
                Rectangle area = selectedAreas.get(current_area);
                int end_x = area.x + area.width;
                //See where is the left down corner
                int min_x = Math.min(real_x, end_x),
                        max_y = Math.max(area.y, real_y);

                selectedAreas.set(current_area, new Rectangle(min_x, area.y, end_x - min_x, max_y - area.y));
                changed = true;
                this.origin_point = new Point(selectedAreas.get(current_area).getLocation());
                this.end_point = new Point(selectedAreas.get(current_area).x + end_x - min_x,
                        selectedAreas.get(current_area).y + max_y - area.y);
                repaint();
            }
            //Mouse on the down right corner
            if (draw_mode == RD_CORNER) {
                Rectangle area = selectedAreas.get(current_area);
                //See where is the right down corner
                int max_x = Math.max(area.x, real_x),
                        max_y = Math.max(area.y, real_y);

                selectedAreas.set(current_area, new Rectangle(area.x, area.y, max_x - area.x, max_y - area.y));
                changed = true;
                this.origin_point = new Point(selectedAreas.get(current_area).getLocation());
                this.end_point = new Point(selectedAreas.get(current_area).x + max_x - area.x,
                        selectedAreas.get(current_area).y + max_y - area.y);
                repaint();
            }
            //Mouse on area
            if (draw_mode == MOVE) {
                Rectangle area = selectedAreas.get(current_area);
                //Where has the right corner in the beginnig
                int ori_x = (int) (loc_point.x);
                int ori_y = (int) (loc_point.y);

                //What has is deslocation
                int des_x = (int) ((real_x - ori_x));
                int des_y = (int) ((real_y - ori_y));
                loc_point = new Point(real_x, real_y);

                //Make sure the area is on image.
                int vertice_x = Math.min(Math.max(area.x + des_x, 0), panel_image.getWidth() - area.width);
                int vertice_y = Math.min(Math.max(area.y + des_y, 0), panel_image.getHeight() - area.height);

                selectedAreas.set(current_area, new Rectangle(vertice_x, vertice_y, area.width, area.height));
                changed = true;
                this.origin_point = new Point(selectedAreas.get(current_area).getLocation());
                this.end_point = new Point(selectedAreas.get(current_area).x + area.width,
                        selectedAreas.get(current_area).y + area.height);
                repaint();

            }

            //Mouse out of a area.
            if (draw_mode == FREE_SELECTION && (!definedArea) && (draw_new_areas)) {
                end_point = new Point((int) (x / resize_factor), (int) (y / resize_factor));
            }
            repaint();
        }
    }
    // </editor-fold>

    public void formMouseDraggedFree(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        if (((evt.getModifiersEx() & java.awt.event.InputEvent.BUTTON1_DOWN_MASK) == java.awt.event.InputEvent.BUTTON1_DOWN_MASK) && !draw_new_areas) {

            //Make sure that mouse position in the image

            int x = evt.getX(), y = evt.getY();
            x = (x < 0) ? 0 : x;
            y = (y < 0) ? 0 : y;
            x = (x > resized_width) ? resized_width : x;
            y = (y > resized_height) ? resized_height : y;

            //Work on real coordinates
            int real_x = (int) (x / resize_factor);
            int real_y = (int) (y / resize_factor);
            Point p = new Point(real_x, real_y);

            switch (this.freeDrawMode) {

                case MOVING:  // Not implemented yet
                    break;
                case CHANGING_SHAPE:

                    System.out.println("Changing shape");


                    if (this.vertexPivot == 0) {
                        this.cropPoints.set(this.cropPoints.size() - 1, new Point(p));
                    }
                    this.cropPoints.set(this.vertexPivot, p);

                    repaint();
                    break;

            }

        }

    }
    // </editor-fold>

    @Override
    public void formMousePressed(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {

        switch (CropType) {
            case RECTANGULAR_CROP:
                formMousePressedRect(evt);
                break;

            case FREE_CROP:
                formMousePressedFree(evt);
                break;
            default:
                System.out.println("Wrong type of Crop?");
                break;
        }
    }
    //</editor-fold>

    public void formMousePressedRect(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        //Detect in picture where the left mouse button as been pressed
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && draw_new_areas) {
            //Get mouse location and make sure it is on image.
            int x = evt.getX(), y = evt.getY();
            x = (x < 0) ? 0 : x;
            y = (y < 0) ? 0 : y;
            x = (x > resized_width) ? resized_width : x;
            y = (y > resized_height) ? resized_height : y;

            //Work on real image coordinates
            int real_x = (int) (x / resize_factor);
            int real_y = (int) (y / resize_factor);
            Point p = new Point(real_x, real_y);
            //If the user presses the mouse in the interior of a area, he can move it.
            if (!selectedAreas.isEmpty() && selectedAreas.get(current_area).contains(p) &&
                    !corners[0].contains(p) && !corners[1].contains(p) && !corners[2].contains(p) && !corners[3].contains(p)) {
                draw_mode = MOVE;
                loc_point = new Point((int) (x / resize_factor), (int) (y / resize_factor));
                this.setCursor(Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
            } //If the user doesn't press on any of the corners, it's free area selection.
            else if (!corners[0].contains(p) && !corners[1].contains(p) && !corners[2].contains(p) && !corners[3].contains(p) && !definedArea) {
                origin_point = new Point((int) (x / resize_factor), (int) (y / resize_factor));
                end_point = new Point((int) (x / resize_factor), (int) (y / resize_factor));
                draw_mode = FREE_SELECTION;
                this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
            } //The other option is that he pressed a corner, so he can resize.
            else {
                if (corners[0].contains(p)) {
                    draw_mode = LU_CORNER;
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.NW_RESIZE_CURSOR));
                } else if (corners[1].contains(p)) {
                    draw_mode = RU_CORNER;
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.NE_RESIZE_CURSOR));
                } else if (corners[2].contains(p)) {
                    draw_mode = LD_CORNER;
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.SW_RESIZE_CURSOR));
                } else if (corners[3].contains(p)) {
                    draw_mode = RD_CORNER;
                    this.setCursor(Cursor.getPredefinedCursor(Cursor.SE_RESIZE_CURSOR));
                }
            }
        }
    }
    //</editor-fold>

    //@SuppressWarnings("static-access")
    public void formMousePressedFree(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && evt.isControlDown()) {
            System.out.println("Form mouse pressed free");

            if (cropPoints.size() > 1) {
                //If the crop area has more than one point, the last point becomes the first to close the area.
                cropPoints.add(cropPoints.get(0));
            }
        } else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && !draw_new_areas) {

            //Get mouse location and make sure it is on image.
            //Can change size
            int x = evt.getX(), y = evt.getY();
            x = (x < 0) ? 0 : x;
            y = (y < 0) ? 0 : y;
            x = (x > resized_width) ? resized_width : x;
            y = (y > resized_height) ? resized_height : y;

            //Work on real image coordinates
            int real_x = (int) (x / resize_factor);
            int real_y = (int) (y / resize_factor);
            Point p = new Point(real_x, real_y);


            if (!draw_new_areas) {

                boolean notFound = true;
                Iterator<Rectangle> itVertex = this.vertexList.iterator();
                for (int i = 0; itVertex.hasNext() && notFound; i++) {
                    Rectangle r = itVertex.next();
                    if (r.contains(p)) {
                        System.out.print("Pivot found " + i);
                        this.freeDrawMode = this.CHANGING_SHAPE;
                        this.vertexPivot = i;
                        notFound = false;
                    }

                }
            }

        }
        repaint();
    }
    // </editor-fold>

    @Override
    public void formMouseClicked(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        switch (CropType) {
            case RECTANGULAR_CROP:
                formMouseClickedRect(evt);
                break;

            case FREE_CROP:
                formMouseClickedFree(evt);
                break;
            default:
                System.out.println("Wrong type of Crop?");
                break;
        }
    }
    // </editor-fold>

    public void formMouseClickedRect(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        //On click see if a area as been selected.
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1) {
            //Get in real coordinates the click point
            click_point = new Point((int) (evt.getX() / resize_factor), (int) (evt.getY() / resize_factor));
            //See if an area contains that point
            Iterator<Rectangle> selectp = selectedAreas.iterator();
            boolean found = false;
            int current = 0;
            while (selectp.hasNext() && !found) {
                Rectangle area = selectp.next();
                if (area.contains(click_point)) {
                    current_area = current;
                    found = true;
                }
                current++;
            }
            if (found) //If found repaint.
            {
                repaint();
            }
        }
    }
    //</editor-fold>

    public void formMouseClickedFree(java.awt.event.MouseEvent evt)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && evt.getClickCount() == 2 && draw_new_areas) {
            if (cropPoints.size() > 1) {
                //Close area, if crop points number is bigger than one.
                cropPoints.add(cropPoints.get(0));
                draw_new_areas = false;
                System.out.println("Area closed");
            }
        }//If the left mouse button is clicked
        else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1 && evt.getClickCount() == 1 && draw_new_areas) {

            int i = (int) Math.floor(evt.getX());
            int j = (int) Math.floor(evt.getY());
            //Put the point inside the picture
            i = (i > resized_width) ? (int) Math.floor(resized_width) : i;
            i = (i < 0) ? 0 : i;
            j = (j > resized_height) ? (int) Math.floor(resized_height) : j;
            j = (j < 0) ? 0 : j;
            //Store the new point
            Point newPoint = new Point((int) (i / resize_factor), (int) (j / resize_factor));
            cropPoints.add(newPoint);
        } //If the right mouse button is clicked and control key is down
        else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3 && evt.isControlDown()) {
            //Clear crop area.
            cropPoints = new ArrayList<Point>();
            draw_new_areas = true; // moot?
        } //If the right mouse button is pressed
        else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3 && !cropPoints.isEmpty()) {
            //remove last point
            cropPoints.remove(cropPoints.size() - 1);
            draw_new_areas = true;
            //freeDrawMode = NOTHING;
        }

        /*
        // Update "origins" so that we can keep the rectangular bounds up to date!
        if(cropPoints.size()>=2)
        {
            // Do iiiit.
            setBoundsFromPoints(cropPoints);
        }*/
        repaint();
    }
    //</editor-fold>

    public Point[] getBoundsFromPoints()
    {
        return getBoundsFromPoints(this.cropPoints);
    }

    /**
     * Gets the bounds of a collection of points
     * 
     * @param points
     * @return
     */
    public Point[] getBoundsFromPoints(Collection<Point> points)
    {
        Point oPoint = new Point(this.panel_image.getWidth(), this.panel_image.getHeight());
        Point ePoint = new Point();

        for(Point p : points)
        {
            int min_x = Math.abs(Math.min(oPoint.x, p.x));
            int min_y = Math.abs(Math.min(oPoint.y, p.y));
            int max_x = Math.abs(Math.max(ePoint.x, p.x));
            int max_y = Math.abs(Math.max(ePoint.y, p.y));

            oPoint.setLocation(min_x, min_y);
            ePoint.setLocation(max_x, max_y);
        }

        Point[] pl = new Point[2];
        pl[0] = oPoint;
        pl[1] = ePoint;

        return pl;
    }

    /**
     * Sets origin_point and end_point to min/max bounds of a set of points
     *
     * @param points
     */
    void setBoundsFromPoints(Collection<Point> points)
    {
        // Sanity check for fresh init'ed... no need to do it for end_point
        if(origin_point.x == 0 && origin_point.y ==0)
        {
            origin_point.setLocation(this.panel_image.getWidth(), this.panel_image.getHeight());
        }

        for(Point p : points)
        {
            int min_x = Math.abs(Math.min(origin_point.x, p.x));
            int min_y = Math.abs(Math.min(origin_point.y, p.y));
            int max_x = Math.abs(Math.max(end_point.x, p.x));
            int max_y = Math.abs(Math.max(end_point.y, p.y));

            origin_point.setLocation(min_x, min_y);
            end_point.setLocation(max_x, max_y);
            
        }
    }

    @Override
    public void paint(Graphics g)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        System.out.println("Paint");
        switch (CropType) {
            case RECTANGULAR_CROP:
                paintRect(g);
                break;

            case FREE_CROP:
                paintFree(g);
                break;
            default:
                System.out.println("Wrong type of Crop?");
                break;
        }
    }
    //</editor-fold>

    /**Confirms the current selected area and returns it.
    @return a selected area, nul, is nothing has selected.
     */
    @Override
    public Rectangle confirmSelectionArea()
    // <editor-fold defaultstate="collapsed" desc="Code">
    {

        //Define the area
        int min_x = Math.abs(Math.min(origin_point.x, end_point.x));
        int min_y = Math.abs(Math.min(origin_point.y, end_point.y));
        int max_x = Math.abs(Math.max(origin_point.x, end_point.x));
        int max_y = Math.abs(Math.max(origin_point.y, end_point.y));
        Rectangle new_rect = new Rectangle(min_x, min_y, max_x - min_x, max_y - min_y);
        //Area to return
        Rectangle area = null;
        int max = Math.max(max_x - min_x, max_y - min_y);
        //If area is not null and has a minimal width and height
        if ((max_x - min_x * max_y - min_y != 0) && (max > 30)) {
            //reset variables
            selectedAreas.add(new_rect);
            origin_point.setLocation(min_x, min_y);
            end_point.setLocation(max_x, max_y);
            current_area = selectedAreas.size() - 1;
            //define area to return
            area = new_rect;
            definedArea = true;
            //draw_new_areas = false;
            this.repaint();
        }
        return area;
    }
    // </editor-fold>

    public void paintRect(Graphics g)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        super.paint(g);

        this.setBackground(java.awt.Color.BLACK);

        if (panel_image != null) {
            Graphics2D g2d = (Graphics2D) g;

            //Area painting stroke
            float dash1[] = {10.0f};
            BasicStroke dashedStroke = new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            g2d.setStroke(dashedStroke);


            g2d.setComposite(super.makeComposite(0.1f));
            //Paint the not confirmed selection on image in green.
            g2d.setPaint(Color.GREEN);
            if (!definedArea && draw_new_areas) {
                int min_x = Math.min(origin_point.x, end_point.x),
                        min_y = Math.min(origin_point.y, end_point.y),
                        max_x = Math.max(origin_point.x, end_point.x),
                        max_y = Math.max(origin_point.y, end_point.y);
                g2d.fill(new Rectangle((int) (min_x * resize_factor), (int) (min_y * resize_factor), (int) ((max_x - min_x) * resize_factor), (int) ((max_y - min_y) * resize_factor)));
            }


            //For a all confirmed areas
            Iterator<Rectangle> selectp = selectedAreas.iterator();
            int current = 0;

            while (selectp.hasNext()) {

                Rectangle area = selectp.next();

                g2d.setComposite(makeComposite(0.5f));

                //If is selected, paint in red.
                if (current == current_area) {

                    System.out.println("Pintar quadrados");
                    g2d.setPaint(Color.GREEN);

                    g2d.fill(new Rectangle((int) (area.x * resize_factor), (int) (area.y * resize_factor), (int) (area.width * resize_factor), (int) (area.height * resize_factor)));
                    //Paint the corners
                    corners[0] = new Rectangle((int) ((area.x - 8 / resize_factor)), (int) ((area.y - 8 / resize_factor)), (int) (16 / resize_factor), (int) (16 / resize_factor));
                    g2d.draw3DRect((int) ((area.x) * resize_factor - 4), (int) ((area.y) * resize_factor - 4), (int) (8), (int) (8), true);
                    corners[1] = new Rectangle((int) ((area.x + area.width - 8 / resize_factor)), (int) ((area.y - 8 / resize_factor)), (int) (16 / resize_factor), (int) (16 / resize_factor));
                    g2d.draw3DRect((int) ((area.x + area.width) * resize_factor - 4), (int) ((area.y) * resize_factor - 4), (int) (8), (int) (8), true);
                    corners[2] = new Rectangle((int) ((area.x - 8 / resize_factor)), (int) ((area.y + area.height - 8 / resize_factor)), (int) (16 / resize_factor), (int) (16 / resize_factor));
                    g2d.draw3DRect((int) ((area.x) * resize_factor - 4), (int) ((area.y + area.height) * resize_factor - 4), (int) (8), (int) (8), true);
                    corners[3] = new Rectangle((int) ((area.x + area.width - 8 / resize_factor)), (int) ((area.y + area.height - 8 / resize_factor)), (int) (16 / resize_factor), (int) (16 / resize_factor));
                    g2d.draw3DRect((int) ((area.x + area.width) * resize_factor - 4), (int) ((area.y + area.height) * resize_factor - 4), (int) (8), (int) (8), true);
                } //else blue if not selected
                else {
                    g2d.setPaint(Color.BLUE);
                    g2d.fill(new Rectangle((int) (area.x * resize_factor), (int) (area.y * resize_factor), (int) (area.width * resize_factor), (int) (area.height * resize_factor)));
                }
                current++;
            }
        } else {
            System.out.println("Panel Image = null");
        }
    }
    // </editor-fold>

    public void paintFree(Graphics g)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        super.paint(g);

        if (panel_image != null) {

            Graphics2D g2d = (Graphics2D) g;

            //g.setColor(java.awt.Color.darkGray);
            g.setColor(java.awt.Color.RED);
            float dash1[] = {10.0f};
            BasicStroke dashedStroke = new BasicStroke(2.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash1, 0.0f);
            g2d.setStroke(dashedStroke);

            Point prev = null;
            if (!cropPoints.isEmpty()) {
                prev = cropPoints.get(0);
                //for all points
                for (Point point : cropPoints) {
                    //draw a line from the previous point to the actual.
                    g2d.setComposite(makeComposite(0.7f));
                    g2d.drawLine((int) (prev.x * resize_factor), (int) (prev.y * resize_factor), (int) (point.x * resize_factor), (int) (point.y * resize_factor));
                    prev = point;
                }
            }
            if (cropPoints.size() > 1) {
                //If the initial crop point equals the final, the area is closed
                if (cropPoints.get(0).equals(cropPoints.get(cropPoints.size() - 1))) {

                    Polygon pol = new Polygon();
                    //Make the polygon
                    for (Point p : cropPoints) {
                        pol.addPoint((int) (p.x * resize_factor), (int) (p.y * resize_factor));
                    }
                    g2d.setComposite(makeComposite(0.1f));
                    g2d.setPaint(Color.GREEN);
                    //Fill it in green.
                    g2d.fill(pol);
                }

                //Draw Rectangles for each vertice

                if (draw_new_areas == false) {
                    g2d.setPaint(Color.RED);
                    g2d.setComposite(makeComposite(0.7f));
                    Iterator<Point> itCropPoints = this.cropPoints.iterator();

                    for (int i = 1; itCropPoints.hasNext(); i++) {
                        Point p = itCropPoints.next();
                        //Paint the corners
                        vertexList.add(i - 1, new Rectangle((int) ((p.x - 8 / resize_factor)), (int) ((p.y - 8 / resize_factor)), (int) (16 / resize_factor), (int) (16 / resize_factor)));
                        g2d.draw3DRect((int) ((p.x) * resize_factor - 4), (int) ((p.y) * resize_factor - 4), (int) (10), (int) (10), true);
                    }

                }
            }

        } else {

            System.out.println("panel image = null");
        }

    }
    //</editor-fold>

    public void setCropStyle(int crop)
    // <editor-fold defaultstate="collapsed" desc="Code">
    {
        this.CropType = crop;
    }
    //</editor-fold>

    public int getCropStyle()
    {
        return this.CropType;
    }

}
