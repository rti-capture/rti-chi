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
package RTIbuilder.pluginhelpers;

import ModuleInterfaces.PluginMetaInfo;
import ModuleInterfaces.UserInteractionInterface;
import java.util.UUID;

public class Plugin {

    private UserInteractionInterface user_interface = null;
    private UUID uuid;
    private String PluginName;
    private String version;
    private String FilePath;
	private boolean executed;
	private boolean acessible;

    public Plugin(Class user_interface, String PluginName, String FilePath, UUID uuid, String version) throws InstantiationException, IllegalAccessException {
        this.user_interface = (UserInteractionInterface) user_interface.newInstance();
        this.PluginName = PluginName;
        this.FilePath = FilePath;
        this.uuid = uuid;
        this.version = version;
		executed = false;
		acessible = true;
    }

    public Plugin(String PluginName, String FilePath, UUID uuid) {
        this.PluginName = PluginName;
        this.FilePath = FilePath;
        this.uuid = uuid;
    }

    public String getFilePath() {
        return FilePath;
    }

    public void setFilePath(String FilePath) {
        this.FilePath = FilePath;
    }

    public String getPluginName() {
        return PluginName;
    }

    public void setPluginName(String PluginName) {
        this.PluginName = PluginName;
    }

    public UserInteractionInterface getUser_interface() {
        return user_interface;
    }

    public void setUser_interface(UserInteractionInterface user_interface) {
        this.user_interface = user_interface;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public boolean isAcessible() {
		return acessible;
	}

	public void setAcessible(boolean acessible) {
		this.acessible = acessible;
	}

    public void setMetaInfo(PluginMetaInfo pI)
    {
            this.user_interface.setMetaInfo(pI);
    }
}
