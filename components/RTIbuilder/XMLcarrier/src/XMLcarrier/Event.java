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

import java.util.UUID;

public class Event {

    private String dateStarted;
    private String dateFinished;
    private UUID componentID;
    private String level;
    private String user;
    private String text;

    public Event(){
        this.dateStarted = "";
        this.dateFinished = "";
        this.componentID = UUID.randomUUID();
        this.level = "Information";
        this.user = "";
        this.text = "";
    }

    public Event(String dateStarted, String dateFinished, UUID componentID, String level, String user,String text) {
        this.dateStarted = dateStarted;
        this.dateFinished = dateFinished;
        this.componentID = componentID;
        this.level = level;
        this.user = user;
        this.text = text;
    }

    public UUID getComponentID() {
        return componentID;
    }

    public void setComponentID(UUID componentID) {
        this.componentID = componentID;
    }

    public String getDateFinished() {
        return dateFinished;
    }

    public void setDateFinished(String dateFinished) {
        this.dateFinished = dateFinished;
    }

    public String getDateStarted() {
        return dateStarted;
    }

    public void setDateStarted(String dateStarted) {
        this.dateStarted = dateStarted;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String mode) {
        this.level = mode;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

}
