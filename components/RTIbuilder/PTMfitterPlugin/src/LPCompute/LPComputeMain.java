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

package LPCompute;

import XMLcarrier.Event;
import XMLcarrier.ImageFile;
import XMLcarrier.Info;
import XMLcarrier.RawInfo;
import XMLcarrier.XMLHandler;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import javax.swing.JOptionPane;

/**
 *
 * @author matheus
 */
public class LPComputeMain {

    private String xmlPath;

    public LPComputeMain(String xmlPath){
        this.xmlPath = xmlPath;

    }

    /**
     * This inner class is a information container.
     * It represents a sphere (X,Y,Radix).
     *
     */
    public class Sphere{
        float x,y,r;
        UUID uuid;

        public Sphere(float x,float y,float r,UUID uuid){
            this.x = x;
            this.y = y;
            this.r =r;
            this.uuid = uuid;
        }

        public float getR() {
            return r;
        }

        public void setR(float r) {
            this.r = r;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public UUID getUuid() {
            return uuid;
        }

        public void setUuid(UUID uuid) {
            this.uuid = uuid;
        }

    }

    
    /**
     * This method will compute all the Light directions and write those LDs in the
     * XML Carrier under the Computed Data section.
     * 
     */

    public void computeLP(){

        XMLHandler a = new XMLHandler(this.xmlPath);
        ArrayList<Sphere> sphereList = new ArrayList<Sphere>();
        ArrayList<RawInfo> lds = new ArrayList<RawInfo>();
        int sphereNumber;
        Event sphereDetected;


        try{
            a.loadXML();
            RawInfo ri = a.getComputedInfo("Spheres");
            ArrayList<Info> innerInfo = ri.getAllInnerInformation();

            sphereNumber = innerInfo.size();

            sphereDetected = new Event();
            sphereDetected.setLevel(Level.WARNING.toString());
            sphereDetected.setText("LPCompute started! " + "Spheres detected : " +  sphereNumber);

            a.registEvent(sphereDetected);
            
            for(Info i : innerInfo){
                Sphere s = new Sphere(Float.valueOf(i.getAttribute("x")),Float.valueOf(i.getAttribute("y")),
                        Float.valueOf(i.getAttribute("r")), UUID.fromString(i.getAttribute("ID")));
                sphereList.add(s);

                RawInfo hlts = a.getComputedInfo("HighLights","SphereID",s.getUuid().toString());
                ArrayList<Info> ltsList = hlts.getAllInnerInformation();

                Event e = new Event();
                e.setLevel(Level.WARNING.toString());
                e.setText("Getting HighLights from sphere : " + s.getUuid().toString() + "!");

                if(ltsList.size() == 0){
                   e.setText("No HLTs found!!!!");
                   JOptionPane.showMessageDialog(null, "No HighLights found for a defined sphere!");
                }

                a.registEvent(e);

                RawInfo aux = new RawInfo("LightDirections");
                aux.addAttribute("SphereID", s.getUuid().toString());
                //lds.add(aux);

                Event ev = new Event();
                ev.setLevel(Level.WARNING.toString());
                ev.setText("Writing LightDirections for sphere : " + s.getUuid().toString() + "!");

                a.registEvent(ev);

                for(Info in : ltsList){
                    if(!in.getAttribute("x").contains("NaN") && !in.getAttribute("y").contains("NaN")){

                        Info ld = new Info("lightdirection");

                        float[] hlt_pos = new float[2];
                        hlt_pos[0] = Float.valueOf(in.getAttribute("x"));
                        hlt_pos[1] = Float.valueOf(in.getAttribute("y"));

                        float[] bc  = new float[3];
                            //bc[0] = this.getSphere(sphereList, uuid).getX();
                            //bc[1] = this.getSphere(sphereList, uuid).getY();
                            //bc[2] = this.getSphere(sphereList, uuid).getR();
                            bc[0] = s.getX();
                            bc[1] = s.getY();
                            bc[2] = s.getR();

                        float[] res = calculateLightPosition(bc, hlt_pos);

                        ld.addAttribute("x", String.valueOf(res[0]));
                        ld.addAttribute("y", String.valueOf(res[1]));
                        ld.addAttribute("z", String.valueOf(res[2]));
                        ld.addAttribute("ImageID", in.getAttribute("ImageID"));
                        aux.addInnerTag(ld);
                    }else{
                        Event evt = new Event();
                        evt.setLevel(Level.SEVERE.toString());
                        evt.setText("Found NaN!Discarding HLT!");
                        a.registEvent(evt);
                    }
                }

                a.addComputedInfo(aux);

            }

            a.writeXML();

        }catch(Exception e){
            e.printStackTrace();
        }

    }

    private Sphere getSphere(ArrayList<Sphere> list, String uuid){
        for(Sphere s : list){
            if(s.getUuid().toString().equals(uuid)) return s;
        }
    return null;
    }

    private static float[] calculateLightPosition(float[] BallCenter, float[] highlight) {
        //System.out.println("BallCenter x=" + BallCenter[0] + " y=" + BallCenter[1] + " r=" + BallCenter[2]);
        //System.out.println("HLT x=" + highlight[0] + " y=" + highlight[1]);
        
		float[] lpdir = {0.0f, 0.0f, 0.0f};

		float Sx = (highlight[0] - BallCenter[0]) / BallCenter[2];
        //System.out.println("Sx : " + Sx);
		float Sy = (BallCenter[1] - highlight[1]) / BallCenter[2];
        //System.out.println("Sy : " + Sy);
		double Sz = Math.sqrt(1.0 - Sx * Sx - Sy * Sy);
        //System.out.println("Sz : " + Sz);

        
		double phi_n = Math.acos(Sz);
		double phi_l = 2.0 * phi_n;

        //System.out.println("phi_n " + phi_n);
        //System.out.println("phi_l " + phi_l);

		phi_l = (phi_l > (Math.PI/2)) ? phi_l = (Math.PI/2-phi_l) + Math.PI/2 : phi_l;

		double t = (Sx !=0 ) ? Math.atan(Sy / Sx) :  Math.PI/2;

		lpdir[0] = (float) (Math.sin(phi_l) * Math.cos(t));
		lpdir[1] = (float) (Math.sin(phi_l) * Math.sin(t));
		lpdir[2] = (float) Math.cos(phi_l);

		//lx = (Sx>=0) ? lx : -lx;

		if (Sx >= 0) {
			lpdir[0] = Math.abs(lpdir[0]);
		} else {
			lpdir[0] = -Math.abs(lpdir[0]);
		}
		if (Sy >= 0) {
			lpdir[1] = Math.abs(lpdir[1]);
		} else {
			lpdir[1] = -Math.abs(lpdir[1]);
		}

        //System.out.println("LD x=" + lpdir[0] + " y=" + lpdir[1] + " z=" + lpdir[2]);

		return lpdir;
	}


}
