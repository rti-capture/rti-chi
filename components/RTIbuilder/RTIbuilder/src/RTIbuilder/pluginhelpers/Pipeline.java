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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class Pipeline {

	/**The name of the pipeline*/
	private String name;
	/**The list of plugins*/
	private HashMap<UUID, PluginOrder> plugins;
	/**The currrent plugin stage*/
	private int stage;
	/** If true, addPlugin is used too add original plugins */
	private boolean loading = true;

	public Pipeline() {
		name = "";
		plugins = new HashMap<UUID, PluginOrder>();
		stage = 0;
	}

	public void loadPlugins(ArrayList<Plugin> plugin_list) {
		for (Plugin p : plugin_list) {
			if (plugins.containsKey(p.getUuid())) {
				for (UUID id : this.plugins.keySet()) {
					if (id.equals(p.getUuid())) {
						this.plugins.get(id).plugin = p;
					}
				}
			}
		}
		loading = false;
	}

	public void addPlugin(int order, Plugin p) {
		if (plugins.containsKey(p.getUuid())) {
			if (plugins.get(p.getUuid()).order != order) {
				PluginOrder po2 = null;
				for (PluginOrder po : plugins.values()) {
					if (po.order == order) {
						po2 = po;
					}
				}
				if (po2 == null) {
					plugins.get(p.getUuid()).order = order;
				} else {
					swapPlugins(po2.order, plugins.get(p.getUuid()).order);
				}
			}
		} else {
			for (PluginOrder po : plugins.values()) {
				if (po.order >= order) {
					po.order++;
				}
			}
			PluginOrder po = new PluginOrder(p, order);
			plugins.put(p.getUuid(), po);
		}

		normalize();

	}

	private void normalize() {
		ArrayList<UUID> used_uuids = new ArrayList<UUID>();

		for (int i = 0; i < plugins.size(); i++) {
			int min = Integer.MAX_VALUE;
			UUID min_uuid = null;
			for (UUID id : plugins.keySet()) {
				if (!used_uuids.contains(id)) {
					if(loading) {
						if (plugins.get(id).original_order < min) {
							min = plugins.get(id).original_order;
							min_uuid = id;
						}
					} else {
						if (plugins.get(id).order < min) {
							min = plugins.get(id).order;
							min_uuid = id;
						}
					}
				}
			}
			used_uuids.add(min_uuid);
			plugins.get(min_uuid).order = i + 1;
		}

//		System.out.println("\nPLUGIN ADD:\n");
//		for (PluginOrder po : plugins.values()) {
//			System.out.println("\torder: " + po.order + " name: " + po.plugin.getPluginName() + "!!!");
//		}
	}

	public void removePlugin(Plugin p) {
		if (!plugins.containsKey(p.getUuid())) {
			return;
		}
		for (PluginOrder po : plugins.values()) {
			int order = plugins.get(p.getUuid()).order;
			if (po.order > order) {
				po.order--;
			}
		}
		plugins.remove(p.getUuid());
	}

	public void swapPlugins(int a, int b) {
		PluginOrder pa = null, pb = null;
		for (PluginOrder p : plugins.values()) {
			if (p.order == a) {
				pa = p;
			}
			if (p.order == b) {
				pb = p;
			}
		}
		if (pa != null && pb != null) {
			pa.order = b;
			pb.order = a;
		}
	}

	public int getPluginOrder(Plugin p) {
		return plugins.get(p.getUuid()).order;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public HashMap<UUID, Plugin> getPlugins() {
		HashMap<UUID, Plugin> res = new HashMap<UUID, Plugin>();
		for (UUID id : plugins.keySet()) {
			res.put(id, plugins.get(id).plugin);
		}
		return res;
	}

	public int getStage() {
		return stage;
	}

	public void setStage(int stage) {
		this.stage = stage;
	}

	public boolean isLast(){
		return (plugins.size()==stage);
	}

	public void addStage(int i) {
		this.stage = this.stage + i;
	}

	public Plugin getStagePlugin() {
		Plugin res = null;
		for (PluginOrder p : plugins.values()) {
			if (p.order == stage) {
				res = p.plugin;
			}
		}
		return res;
	}

	public boolean hasNextStage() {
		return (this.stage < this.plugins.size());
	}

	private class PluginOrder {

		/**The plugin*/
		Plugin plugin;
		/**The order in execution*/
		int order;
		/**The original order in pipeline xml*/
		int original_order;

		public PluginOrder(Plugin plugin, int order) {
			this.plugin = plugin;
			if(loading) {
				this.original_order = order;
			} else {
				this.order = order;
			}
		}
	}
}


