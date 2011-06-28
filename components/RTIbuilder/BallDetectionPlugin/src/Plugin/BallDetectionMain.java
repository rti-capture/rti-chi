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

package Plugin;

import Exceptions.ModuleException;
import java.io.File;
import java.net.URI;
import java.util.Random;
import java.util.UUID;

/**
 *
 * @author jbarbosa
 */
public class BallDetectionMain implements ModuleInterfaces.ComponentInterface {

    public StringBuffer executeModule(StringBuffer XMLcarrier) throws ModuleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void executeModule(String XMLcarrierPath) throws ModuleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void executeModule(File XMLcarrierFile) throws ModuleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void executeModule(URI XMLcarrierURI) throws ModuleException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public UUID getModuleUUID() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getModuleNAME() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getModuleVersion() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

	public void setNumber(javax.swing.JLabel label,int num){
		  Random r = new Random();
		  label.setText("Random number: "+r.nextInt(num+10));


	}


}
