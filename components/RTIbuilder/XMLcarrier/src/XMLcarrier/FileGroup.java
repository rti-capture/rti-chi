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
import java.util.ArrayList;
import java.util.UUID;

public class FileGroup {
    private UUID id;
    private String use;
    private ArrayList<ImageFile> list;
    private ArrayList<UUID> refList;
    
    public FileGroup(){
        this.id = UUID.randomUUID();
        this.refList = new ArrayList<UUID>();
        this.use = "";
        this.list = new ArrayList<ImageFile>();
    }
    
    public FileGroup(UUID id,String use){
        this.id = id;
        this.use = use;
        this.refList = new ArrayList<UUID>();
        this.list = new ArrayList<ImageFile>();
    }
    
    public FileGroup(UUID id,String use,ArrayList<ImageFile> list){
        this.id = id;
        this.use = use;
        this.list = list;
        this.refList = new ArrayList<UUID>();

        // BUGFIX ;_;
        for(ImageFile img : list)
        {
            refList.add(UUID.fromString(img.getUuid()));
        }
    }
    
    public FileGroup(UUID id,String use,ArrayList<ImageFile> list,ArrayList<UUID> ref){
        this.id = id;
        this.use = use;
        this.list = list;
        this.refList = ref;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public ArrayList<ImageFile> getList() {
        return list;
    }
    
    public ArrayList<UUID> getRefList() {
        return this.refList;
    }

    public void setList(ArrayList<ImageFile> list) {
        this.list = list;
    }
    
    public void setRefList(ArrayList<UUID> refList) {
        this.refList = refList;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
    }

    public void addImageFile(ImageFile i){
        this.list.add(i);
    }

    public void addRef(UUID ref){
        this.refList.add(ref);
    }

}
