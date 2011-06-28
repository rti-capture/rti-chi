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
import java.awt.Image;
import java.awt.image.BufferedImage;

/**
 *Preview panel for image display.
 */
public class PreviewPanel extends javax.swing.JPanel {

    /**Panel image*/
    protected BufferedImage panel_image;
    /**Image resized for display*/
    protected Image resized_image;
    /**Scale factor between the original image and the resized one*/
    protected float resize_factor = 0;
    /**Image width in panel*/
    protected int resized_width = 0;
    /**Image height in panel*/
    protected int resized_height = 0;
    /**Flags if is the user that defines the panel scale factor*/
    protected boolean user_defined_scale;
    /**Flags if the image needs to be repainted*/
    private boolean image_needs_repaint;


    /**Creates new form PreviewPanel */
    public PreviewPanel() {
        initComponents();
    }

    /**Sets the panel picture to be displayed. Note that after this for display the panel needs repaiting.
     *@param image panel to display in the panel.
     */
    public synchronized void setImage(java.awt.image.BufferedImage image) {

        this.panel_image = image;
        //force image repainting
        if (user_defined_scale) {
            image_needs_repaint = true;
        } //Reset the scale to force repaint, only if the sacle is not user defined
        else {
            resize_factor = -1;
        }
        if (image != null) {
            this.setPreferredSize(new java.awt.Dimension((int) (panel_image.getWidth() * resize_factor), (int) (panel_image.getHeight() * resize_factor)));
        }
    }

    /**Get current panel image.
    @return a Buffered Image from the panel.
     *@see BufferedImage
     */
    public BufferedImage getImage() {
        return panel_image;
    }

    /**Sets the scale used to design the image in the panel.<br> 
     *This method should be used only if the panel scale is suposed to be user defined.
     *@param scale image scale.
     */
    public void setImageScale(float scale) {
        resize_factor = scale;
        image_needs_repaint = true;
        if (panel_image != null) {
            this.setPreferredSize(new java.awt.Dimension((int) (panel_image.getWidth() * scale), (int) (panel_image.getHeight() * scale)));
        }
    }

    /**Get the scale used to design the image in the panel.<br>
     *@return a float with the used scale.
     */
    public float getImageScale() {
        return resize_factor;
    }

    /**If true sets the scale as user defined, else the scale is determined automatically.<br>
     *@param user_defined true to set the scale as user defined, false otherwise.
     */
    public void setUserDefinedScale(boolean user_defined) {
        user_defined_scale = user_defined;
    }

    @Override
    public void paint(Graphics g)
 {
        super.paint(g);

        //Set black background.
        this.setBackground(java.awt.Color.BLACK);

        if (panel_image != null) {
            //The best scale factor for image display.
            float scale_factor = 0.0f;
            if (!user_defined_scale) {
                scale_factor = Math.min(((float) this.getWidth() / (float) panel_image.getWidth()), (float) ((float) this.getHeight() / (float) panel_image.getHeight()));
            } else {
                scale_factor = resize_factor;
            }

            //Calcute the resized image width and height for resizing.
            resized_width = (int) (panel_image.getWidth() * scale_factor);
            resized_height = (int) (panel_image.getHeight() * scale_factor);

            //If scale has been changed, a new resize is needed.
            if ((resize_factor != scale_factor && !user_defined_scale) || (user_defined_scale && image_needs_repaint)) {
                resize_factor = scale_factor;
                //Resize
                resized_image = panel_image.getScaledInstance((int) resized_width, (int) resized_height, java.awt.Image.SCALE_FAST);
                image_needs_repaint = false;
            }

            //Draw the image.
            g.drawImage(resized_image, 0, 0, null);
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
      // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
      private void initComponents() {

            org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(0, 400, Short.MAX_VALUE)
            );
            layout.setVerticalGroup(
                  layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                  .add(0, 300, Short.MAX_VALUE)
            );
      }// </editor-fold>//GEN-END:initComponents


      // Variables declaration - do not modify//GEN-BEGIN:variables
      // End of variables declaration//GEN-END:variables

}
