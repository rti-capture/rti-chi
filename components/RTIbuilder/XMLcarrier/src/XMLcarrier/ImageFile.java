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

package XMLcarrier;

/**
 * This class is used to add and retrieve images(not the binary data but all the information related).
 *     <br> 1. URL;
 *     <br> 2. MIMETYPE;
 *     <br> 3. UUID;
 *     <br> 4. CHECKSUM;
 */

public class ImageFile {
	String url;
	String mimetype;
	String uuid;
	String loctype;
	String checksum;

        /**
         * Constructor no no arguments.
         *
         */

	public ImageFile(){
		this.mimetype = "";
		this.url = "";
		this.uuid = "";
		this.checksum = "";
                this.loctype = "";
	}

        /**
         * Constructor receiving all parameters except the loctype. Compatibilty with previous versions.
         *
         * @param mimetype
         *      String : MIMETYPE;
         * @param url
         *      String : URL;
         * @param uuid
         *      String : UUID;
         * @param che
         *      String : CHECKSUM;
         */
	
	public ImageFile(String mimetype, String url, String uuid,String che) {
		this.mimetype = mimetype;
		this.url = url;
		this.uuid = uuid;
		this.checksum = che;
	}
        
        /**
         * Constructor receiving all parameters.
         *
         * @param mimetype
         *      String : MIMETYPE;
         * @param url
         *      String : URL;
         * @param uuid
         *      String : UUID;
         * @param che
         *      String : CHECKSUM;
         * @param loctype
         *      String : LOCTYPE;
         */
	
	public ImageFile(String mimetype, String url, String uuid,String che,String loctype) {
		this.mimetype = mimetype;
		this.url = url;
		this.uuid = uuid;
		this.checksum = che;
                this.loctype = loctype;
	}

        /**
         * Return the image's checksum.
         *
         * @return
         *      String : CHECKSUM;
         */

	public String getChecksum() {
		return checksum;
	}

        /**
         * Set the image's checksum.
         *
         * @param checksum
         *      String : CHECKSUM.
         */

	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

        /**
         * Return the image's url.
         *
         * @return
         *      String: image's url.
         */

	public String getUrl() {
		return url;
	}

        /**
         * Set the image url.
         *
         * @param url
         *      String: image's url.
         */

	public void setUrl(String url) {
		this.url = url;
	}

        /**
         * Return the image mimetype.
         * @return
         *      String : image's mimetype.
         */

	public String getMimetype() {
		return mimetype;
	}

        /**
         * Set the image mimetype;
         *
         * @param mimetype
         *      String : image's mimetype.
         */

	public void setMimetype(String mimetype) {
		this.mimetype = mimetype;
	}

        /**
         * Return the image UUID.
         *
         * @return
         *      String : image's UUID.
         */

	public String getUuid() {
		return uuid;
	}

        /**
         * Set the image UUID.
         *
         * @param uuid
         *      String : image's UUID.
         */

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
        
        /**
         * Return the image's LOCTYPE;
         * 
         * @return
         *      String : LOCTYPE;
         */

        public String getLoctype() {
            return loctype;
        }

        /**
         * Set the image's LOCTYPE;
         *
         * @param loctype
         *      String : LOCTYPE;
         */

        public void setLoctype(String loctype) {
            this.loctype = loctype;
        }



}
