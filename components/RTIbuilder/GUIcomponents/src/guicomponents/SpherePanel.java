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


import java.awt.Graphics;
import java.awt.Graphics2D;



/**Image panel used for image preview and sphere detection representation and edition on the image.<br>
 *By default the image scale on panel is user defined and sphere edition is enabled, these options can be changed by the available methods
 *{@link setUserDefinedScale()} and {@link setSphereEditable()}
 */
public class SpherePanel extends PreviewPanel {

	/**Edition mode where the user changes the sphere center*/
    private final int CHANGE_CENTER = 1;
	/**Edition mode where the user changes the sphere radius*/
    private final int CHANGE_RADIUS = 2;
    /**No edition*/
    private final int NO_EDITION = 0;


	/**Draw the sphere if true.*/
    private boolean enable_draw = false;
	/**Flags if sphere movement and adjustment is allowed*/
	private boolean enable_edition =  true;
	/**The center x coordinate.*/
    private float xc;
	/**The center y coordinate.*/
    private float yc;
	/**The center radius coordinate.*/
    private float rc = 5;
	/**The sphere coordinates and radius*/
    private float center[] = {0, 0, 0};
    /**The area location where the sphere is represented.*/
	private int area_x = 0,area_y =0;
	/**Sphere edition mode*/
	private int edition_type = 0;



    /** Creates new form SpherePanel */
    public SpherePanel() {
		super();
		super.setUserDefinedScale(true);
        initComponents();
        this.setEnabledraw(true);
        this.resize_factor = 1.0f;
        this.repaint();
    }
	
    /** Creates new form SpherePanel
	 *@param bi panel buffered image.
	 *@param center the sphere coordinates and radius
	 *@param scale the image drawing scale
	 *@param EnableDraw true if sphere drawing is enable, false otherwise
	 */
    public SpherePanel(java.awt.image.BufferedImage bi, float center[], float scale, boolean EnableDraw) {
        super();
		super.setUserDefinedScale(true);
		super.setImage(bi);
        initComponents();
        this.center = center;
		this.xc = center[0] * scale;
        this.yc = center[1] * scale;
        this.rc = center[2] * scale;
        this.setEnabledraw(EnableDraw);
        this.resize_factor = 1.0f;
        this.repaint();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
      // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
      private void initComponents() {

            setBorder(javax.swing.BorderFactory.createEtchedBorder());
            setMaximumSize(new java.awt.Dimension(249, 229));
            setMinimumSize(new java.awt.Dimension(249, 229));
            addMouseListener(new java.awt.event.MouseAdapter() {
                  public void mousePressed(java.awt.event.MouseEvent evt) {
                        formMousePressed(evt);
                  }
                  public void mouseReleased(java.awt.event.MouseEvent evt) {
                        formMouseReleased(evt);
                  }
            });
            addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                  public void mouseDragged(java.awt.event.MouseEvent evt) {
                        formMouseDragged(evt);
                  }
            });
      }// </editor-fold>//GEN-END:initComponents


private void formMouseReleased(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseReleased

	//Get distance between the center and the mouse
	float xr = this.xc - evt.getX()/resize_factor;

    //If changing the radius
    if(this.edition_type == CHANGE_RADIUS) {
        //Set the radius
		this.rc = Math.abs(xr);
        this.repaint();
    }
    //If changing the center
    if(this.edition_type == CHANGE_CENTER) {
		//Set the center on mouse location
        this.xc = evt.getX() / resize_factor;
        this.yc = evt.getY() / resize_factor;
        this.repaint();
    }
	//Set sphere data
    this.center[0] = this.xc;
    this.center[1] = this.yc;
    this.center[2] = this.rc;
    
    this.edition_type = NO_EDITION;
}//GEN-LAST:event_formMouseReleased

private void formMousePressed(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMousePressed
   
	//Distance between mouse point and sphere center
	float xr = this.xc - evt.getX()/resize_factor;
    float yr = this.yc - evt.getY()/resize_factor;

	//The distance between the mouse point and the radius adjuster.
    float xrr = this.xc+this.rc - evt.getX()/resize_factor;

	//If the mouse pointer is in the center square
   if(this.enable_edition && Math.abs(xr*resize_factor) < 5 && Math.abs(yr*resize_factor) < 5) {
        //Set new center
		this.xc = evt.getX() / resize_factor;
        this.yc = evt.getY() / resize_factor;
        //change edition type for the other listeners
		this.edition_type = CHANGE_CENTER;
        this.repaint();
    }

    if(this.enable_edition && Math.abs(xrr*resize_factor) < 5 && Math.abs(yr*resize_factor) < 5) {
		//change radius
        this.rc = Math.abs(xr);
        //change edition type for the other listeners
		this.edition_type = CHANGE_RADIUS;
        this.repaint();
    }

	 //set new location
     this.center[0] = this.xc;
     this.center[1] = this.yc;
     this.center[2] = this.rc;
}//GEN-LAST:event_formMousePressed

    private void formMouseDragged(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseDragged
        
        float xr = this.xc - evt.getX()/resize_factor;
        
        if(this.edition_type == CHANGE_RADIUS) {
            //change radius
			this.rc = Math.abs(xr);
            this.repaint();
        }
        
        if(this.edition_type == CHANGE_CENTER) {
            //change center
			this.xc = evt.getX() / resize_factor;
            this.yc = evt.getY() / resize_factor;
            this.repaint();
        }

		//set new center
		this.center[0] = this.xc;
		this.center[1] = this.yc;
		this.center[2] = this.rc;
    }//GEN-LAST:event_formMouseDragged
        
      // Variables declaration - do not modify//GEN-BEGIN:variables
      // End of variables declaration//GEN-END:variables
    
     @Override
    public void paint(Graphics g) {
        super.paint(g);
        try {
            if(panel_image!=null&&enable_draw){

				//set center points
                xc = center[0];
                yc = center[1];
                rc = center[2];


                int x1 = Math.round(xc*resize_factor+0.5f*resize_factor);
				int y1 = Math.round(yc*resize_factor+0.5f*resize_factor);
				int r =   Math.round(rc*resize_factor);

                int r2 =  (int)((float)r*0.25f);
                r2 = (r2<2) ? 2 : r2;

                Graphics2D g2D = (Graphics2D)g;
                g2D.setColor(java.awt.Color.red);
                g2D.setStroke(new java.awt.BasicStroke(1));

				//draw center lines
                g2D.drawLine(x1, y1-r2,x1,y1-1);
                g2D.drawLine(x1, y1+r2,x1,y1+1);
                g2D.drawLine(x1-r2, y1,x1-1,y1);
                g2D.drawLine(x1+r2, y1,x1+1,y1);

				//draw center square
                g2D.drawRect(x1-5, y1-5,10,10);
				//draw circle
                g2D.drawArc(x1-r, y1-r, 2*r, 2*r, 0, 360);
                //draw radius adjustement square
				g2D.drawRect(x1+r-5, y1-5,10,10);
                
            }
        } catch (java.lang.NullPointerException ex) {
            return ;
        }
    }

    /**Return true if is drawing the sphere on image.
	*@return  true if sphere drawing is enable.
	*/
    public boolean isEnabledraw() {
        return enable_draw;
    }

	/**Enables/Disables sphere drawing on image
	 *@param enabledraw true to enable drawing, false otherwise.
	 */
    public void setEnabledraw(boolean enabledraw) {
        this.enable_draw = enabledraw;
    }

	/**Return true if the user can move or adjust the sphere, false otherwise.
	*@return  true if sphere editing is enabled .
	*/
    public boolean isSphereEditable() {
        return enable_edition;
    }

	/**Enables/Disables sphere edition.
	 *@param enable_edition true to enable sphere edition, false otherwise.
	 */
    public void setSphereEditable(boolean enable_edition) {
        this.enable_edition = enable_edition;
    }

    /**Return the sphere's coordinates and radius in the full size scale.
	*@return  a float array with the x, y coordinates and radius.
	*/
    public float[] getCenter() {
		//Return the ball center in real world coordinates
        float[] center_float = {(center[0])+area_x,(center[1])+area_y,center[2]};
        return center_float;
    }

	/**Set the sphere's coordinates and radius.
	*@param c a three size array with x,y coordinates and radius.
	*/
    public void setCenter(float c[]) {
        this.center = new float[3];
		//convert the real coordinates to area located coordinates.
        this.center[0] = ((c[0]-area_x));
        this.center[1] = ((c[1]-area_y));
        this.center[2] = (c[2]);
    }

	/**Set the image area location. Because the sphere is represented in a image segment, this must be located.
	 *@param the rigt upper corner of the area where the sphere is located
	 */
	public void setImageLocation(int x,int y){
		area_x =x;
		area_y =y;
	}
    
}
