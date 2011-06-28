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

package Plugin.helpers;

import javax.swing.table.DefaultTableModel;

/**
 * 
 * Table model that permites to define wich lines, columns or cells are editable.<br>
 * It also allows to evaluate if the table was been changed.
 *
 */

public class RowEditableTableModel extends DefaultTableModel {

		/**The not editable columns*/
		int not_allowed_columns =0;
		/**The not editable lines*/
		int not_allowed_lines = 0;
		/**The not editable cells*/
		int not_allowed_cells[][]={};
		/**if the model has been changed*/
		boolean table_changed = false;		
		
		/** Default constructor take makes a empty table model, and all cells are editable*/
        public RowEditableTableModel() {
            super();
        }
		
		/**Makes a model with the given number of lines and columns<p>
		 * @param lines model line number
		 * @param columns model column number
		 */
		public RowEditableTableModel(int lines,int columns) {
            super(lines, columns);
        }
		
		/**Makes a model with the given number of lines and columns, also defines the number of lines and columns that are not editable<p>
		 * @param lines model line number
		 * @param columns model column number
		 * @param not_allowed_line_number model number of not editable rows
		 * @param not_allowed_column_number model number of not editable columns
		 */
		public RowEditableTableModel(int lines,int columns,int not_allowed_line_number, int not_allowed_column_number) {
            super(lines, columns);
			not_allowed_lines = not_allowed_line_number;
			not_allowed_columns =  not_allowed_column_number;
        }
		
		/**Makes a model with the given number of lines and columns, also defines the not editable cells<p>
		 * @param lines model line number
		 * @param columns model column number
		 * @param cells model not editable cells
		 */
		public RowEditableTableModel(int lines,int columns,int[][] cells) {
            super(lines, columns);
			not_allowed_cells = cells;
        }
		
		/**Makes a model with the given number of lines and columns, also defines the not editable number of rows and columns and the not editable cells<p>
		 * @param lines model line number
		 * @param columns model column number
		 * @param not_allowed_line_number model number of not editable rows
		 * @param not_allowed_column_number model number of not editable columns
		 * @param cells model not editable cells
		 */		
		public RowEditableTableModel(int lines,int columns,int not_allowed_line_number, int not_allowed_column_number,int[][] cells) {
            super(lines, columns);
			not_allowed_lines = not_allowed_line_number;
			not_allowed_columns =  not_allowed_column_number;
			not_allowed_cells = cells;
        }
		

		
        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            
			if (rowIndex<not_allowed_lines) {
                return false;
            }
			
			if (columnIndex<not_allowed_columns) {
                return false;
            }
			for(int x =0;x<not_allowed_cells.length;x++){
				if(not_allowed_cells[x][0]==rowIndex&&not_allowed_cells[x][1]==columnIndex)
					return false;
			}
			table_changed =  true;
			return true;
        }

		/**Returns if the table has been changed*/
		public boolean isTable_changed() {
			return table_changed;
		}

		/**Set if the table has been changed*/
		public void setTable_changed(boolean table_changed) {
			this.table_changed = table_changed;
		}
		
		/**Set the not editable table cells.<p>
		 @param not_allowed_cells  not allowed cells*/
		public void setNot_allowed_cells(int[][] not_allowed_cells) {
			this.not_allowed_cells = not_allowed_cells;
		}

		/**Set the not editable table columns.<p>
		  @param not_allowed_columns he number of not editables columns*/
		public void setNot_allowed_columns(int not_allowed_columns) {
			this.not_allowed_columns = not_allowed_columns;
		}

		/**Set the not editable table columns.<p>
		 @param not_allowed_lines  the number of not editables lines*/
		public void setNot_allowed_lines(int not_allowed_lines) {
			this.not_allowed_lines = not_allowed_lines;
		}	
		
    }