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

import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.UUID;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

/**Panel that contains the a thumbnail button, and also a checkbox for image selection or a label with the image name*/
public class ImageThumbnailButton extends javax.swing.JPanel {


	/**Label with picture name*/
    private javax.swing.JLabel picName;
	/**Picture name*/
    private String image_name;
	/**Picture unique id*/
	private UUID image_id;
	/**Image thumbnail button*/
	private JButton image_thumbnail_button;
	/**Some useful information about the image or button*/
	private String info;

    /** Creates new form ImageSelectPanel.
	 *@param image_name The button image name, shown in the panel.
	 *@param image_id The button image identifier.
	 */
    public ImageThumbnailButton(String image_name,UUID image_id) {
        initComponents();
        this.image_name = image_name;
		this.image_id = image_id;
		image_thumbnail_button =  new JButton();
//		image_thumbnail_button.setMinimumSize(new java.awt.Dimension(150, 150));
//		image_thumbnail_button.setPreferredSize(new java.awt.Dimension(150, 150));
        this.add(image_thumbnail_button, java.awt.BorderLayout.CENTER);
        this.selection_checkbox.setText(image_name);
        this.picName = new javax.swing.JLabel(image_name);
        this.selection_checkbox.setSelected(false);
		this.info = "";
    }

	/**Set the checkbox selected/deselected.
	*@param b true to select, false otherwise.
	*/
    public void setSelected(boolean selected) {
        selection_checkbox.setSelected(selected);
    }

	/**Flags if the image is selected.
	 *@return true if the button is selected, false otherwise.
	 */
    public boolean isSelected() {
        return selection_checkbox.isSelected();
    }

	/**Set the checkbox enable/disable.
	 *@param b true to set enable, false otherwise.
	 */
    public void enableCheckBox(boolean b){
            selection_checkbox.setEnabled(b);
    }    

	/**Choses to show a checkbox on button panel*/
    public void setCheckBoxMode(){
        this.jPanel1.remove(this.picName);
        this.jPanel1.add(this.selection_checkbox,0);
    }

	/**Get the picture name
	*@return a string whith the picture name;
	*/
    public javax.swing.JCheckBox getCheckBox()
    {
     return selection_checkbox;
    }

	/**Choses to show a name label on button panel*/
	public void setLabelMode(){
		this.jPanel1.remove(this.selection_checkbox);
        this.jPanel1.add(picName,0);
    }

    /**Get the picture name
	*@return a string whith the picture name;
	*/
    public String getPictureName()
    {
        return image_name;
    }

	/**Get the picture identifier associated with this button
	*@return a UUID whith the picture identifier
	*@see UUID
	*/
	public UUID getImageId(){
	    return image_id;
	}

	/**Set the picture identifier associated with this picture
	*@param id a UUID whith the picture identifier
	*@see UUID
	*/
	public void setImageId(UUID id){
	   image_id = id;
	}

    /**Set the thumbnail button icon.
	*@param image a buffered image for image icon.
	*@see BufferedImage
	*/
	public void setIcon(BufferedImage image){
	    image_thumbnail_button.setIcon(new ImageIcon(image));
	}

    /**Set the thumbnail button icon.
	*@param icon icon for the thumbnail button.
	*@see Icon
	*/
    public void setIcon(Icon icon){
	    image_thumbnail_button.setIcon(icon);
	}

    /**Get the thumbnail button.
	*@return a JButton object.  
	*@see JButton
	*/
	public JButton getImageButton(){
	    return image_thumbnail_button;
	}

	/**Add a action listener to the button.<br>
	*Remeber that the action source is not the {@link ImageThumbnailButton} but a {@link javax.swing.JButton}.
	*@param action_listener a action listener with an action to be executed on button action.
	*@see ActionListener
	*/
	public void setButtonListener(ActionListener action_listener){
	    image_thumbnail_button.addActionListener(action_listener);
	}

	/**Add a action listener to the checkbox.<br>
	*Remeber that the action source is not the {@link ImageThumbnailButton} but a {@link javax.swing.JCheckBox}.
	*@param action_listener a action listener to be associated to the checkbox.
	*@see ActionListener
	*/
    public void setCheckBoxListener(ActionListener action_listener){
	    selection_checkbox.addActionListener(action_listener);
	}

	/**Store some usefull information about the picture or the button.
	*@param info information that can be considered valuable. 
	*/
	public void setInformation(String info){
	  this.info = info;
	}

	/**Get information about the picture or the button.
	*@return a String with stored information.
	*/
	public String getInformation(){
	  return info;
	}



    /** This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        selection_checkbox = new javax.swing.JCheckBox();

        setBorder(javax.swing.BorderFactory.createEtchedBorder());
        setMinimumSize(new java.awt.Dimension(100, 100));
        setName("Form"); // NOI18N
        setPreferredSize(new java.awt.Dimension(100, 100));
        setLayout(new java.awt.BorderLayout());

        jPanel1.setName("jPanel1"); // NOI18N
        jPanel1.setLayout(new javax.swing.BoxLayout(jPanel1, javax.swing.BoxLayout.LINE_AXIS));

        selection_checkbox.setName("selection_checkbox"); // NOI18N
        jPanel1.add(selection_checkbox);

        add(jPanel1, java.awt.BorderLayout.SOUTH);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel jPanel1;
    private javax.swing.JCheckBox selection_checkbox;
    // End of variables declaration//GEN-END:variables
}