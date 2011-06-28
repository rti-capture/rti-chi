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
package RTIbuilder;

import java.awt.Image;
import java.awt.Toolkit;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import RTIbuilder.GUI.LPtrackerView;
import org.jdesktop.application.Application;
import org.jdesktop.application.SingleFrameApplication;

/**
 * The main class of the application.
 */
public class RTIbuilderApp extends SingleFrameApplication {

    /**
     * Variable that defines the option to use the triangulation process in interface.
     */
    public static boolean use_triangulation = false;

    /**
     * At startup create and show the main frame of the application.
     */
    @Override
    protected void startup() {
        show(new LPtrackerView(this));
    }

    /**
     * This method is to initialize the specified window by injecting resources.
     * Windows shown in our application come fully initialized from the GUI
     * builder, so this additional configuration is not needed.
     */
    @Override
    protected void configureWindow(java.awt.Window root) {
    }

    /**
     * A convenient static getter for the application instance.
     * @return the instance of LPtrackerApp
     */
    public static RTIbuilderApp getApplication() {
        return Application.getInstance(RTIbuilderApp.class);
    }

    /**
     * Main method launching the application.
     */
    public static void main(String[] args) {

        //only for mac os
        if (System.getProperty("os.name").equalsIgnoreCase("Mac OS X")) {
            System.out.println("OS: MAC OS X");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
//            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "RTIbuilder");
        }

        if (Runtime.getRuntime().maxMemory() - 1 < 400 * (int) Math.pow(2, 20)) {
            try {
                if (System.getProperty("os.name").toUpperCase().contains("WINDOWS")) {
                    Runtime.getRuntime().exec("javaw -Xmx1600M -jar RTIbuilder.jar -opt=1");
                    System.exit(0);
                } else {
                    Runtime.getRuntime().exec("java -Xmx1600M -jar RTIbuilder.jar -opt=1");
                    System.exit(0);
                }
                System.exit(0);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        launch(RTIbuilderApp.class, args);
    }
}
