/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.timetable.gwt.client.admin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedEvent;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.DataChangedListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasFocus;
import org.unitime.timetable.gwt.resources.GwtResources;
import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.services.SimpleEditService;
import org.unitime.timetable.gwt.services.SimpleEditServiceAsync;
import org.unitime.timetable.gwt.shared.SimpleEditException;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Focusable;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class SimpleEditPage extends Composite {
	public static final GwtResources RESOURCES =  GWT.create(GwtResources.class);
	private final SimpleEditServiceAsync iService = GWT.create(SimpleEditService.class);
	private final MenuServiceAsync iMenuService = GWT.create(MenuService.class);

	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iBottom;
	private SimpleEditInterface.Type iType;
	private UniTimeTable<Record> iTable;
	
	private SimpleEditInterface iData;
	
	private boolean iEditable = false;
	
	public SimpleEditPage() throws SimpleEditException {
		String typeString = Window.Location.getParameter("type");
		if (typeString == null) throw new SimpleEditException("Edit type is not provided.");
		iType = SimpleEditInterface.Type.valueOf(typeString);
		if (iType == null) throw new SimpleEditException("Edit type not recognized.");
		UniTimePageLabel.getInstance().setPageName(iType.getTitle());
		
		ClickHandler save = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iData.getRecords().clear();
				iData.getRecords().addAll(iTable.getData());
				iHeader.setMessage("Saving data...");
				iService.save(iData, new AsyncCallback<SimpleEditInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						iHeader.setErrorMessage("Save failed (" + caught.getMessage() + ").");
					}
					@Override
					public void onSuccess(SimpleEditInterface result) {
						iData = result;
						iEditable = false;
						refreshTable();
						saveOrder();
					}
				});
			}
		};
		
		ClickHandler edit = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = true;
				iHeader.setEnabled("edit", false);
				refreshTable();
			}
		};
		
		ClickHandler back = new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				iEditable = false;
				load();
			}
		};

		iPanel = new SimpleForm();
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("edit", "<u>E</u>dit", 'e', 75, edit);
		iHeader.addButton("save", "<u>S</u>ave", 's', 75, save);
		iHeader.addButton("back", "<u>B</u>ack", 'b', 75, back);
		iPanel.addHeaderRow(iHeader);
		
		iTable = new UniTimeTable<Record>();
		iPanel.addRow(iTable);
		
		iBottom = iHeader.clonePanel();
		iPanel.addNotPrintableBottomRow(iBottom);
		
		initWidget(iPanel);
		
		final Timer timer = new Timer() {
			@Override
			public void run() {
				saveOrder();
			}
		};
		iTable.addDataChangedListener(new DataChangedListener<Record>() {
			@Override
			public void onDataInserted(DataChangedEvent<Record> event) {
			}

			@Override
			public void onDataMoved(List<DataChangedEvent<Record>> event) {
				timer.schedule(5000);
			}

			@Override
			public void onDataRemoved(DataChangedEvent<Record> event) {
			}

			@Override
			public void onDataSorted(List<DataChangedEvent<Record>> event) {
			}
		});
		
		load();
	}
	
	public void load() {
		iBottom.setVisible(false);
		iHeader.setEnabled("save", false);
		iHeader.setEnabled("edit", false);
		iHeader.setEnabled("back", false);
		iHeader.setMessage("Loading data...");
		iTable.clearTable();

		iService.load(iType, new AsyncCallback<SimpleEditInterface>() {
			
			@Override
			public void onSuccess(SimpleEditInterface result) {
				iData = result;
				final Comparator<Record> cmp = iData.getComparator();
				
				Set<String> ordRequest = new HashSet<String>();
				ordRequest.add("SimpleEdit.Order[" + iType.toString() + "]");
				iMenuService.getUserData(ordRequest, new AsyncCallback<HashMap<String,String>>() {
					@Override
					public void onSuccess(HashMap<String, String> result) {
						final String order = "|" + result.get("SimpleEdit.Order[" + iType.toString() + "]") + "|";
						Collections.sort(iData.getRecords(), new Comparator<Record>() {
							public int compare(Record r1, Record r2) {
								int i1 = (r1.getUniqueId() == null ? -1 : order.indexOf("|" + r1.getUniqueId() + "|"));
								if (i1 >= 0) {
									int i2 = (r2.getUniqueId() == null ? -1 : order.indexOf("|" + r2.getUniqueId() + "|"));
									if (i2 >= 0) {
										return (i1 < i2 ? -1 : i1 > i2 ? 1 : cmp.compare(r1, r2));
									}
								}
								return cmp.compare(r1, r2);
							}
						});
						refreshTable();
					}
					@Override
					public void onFailure(Throwable caught) {
						Collections.sort(iData.getRecords(), cmp);
						refreshTable();
					}
				});
			}
			
			@Override
			public void onFailure(Throwable caught) {
				iHeader.setErrorMessage("Unable to load data (" + caught.getMessage() + ")");
			}
		});
	}
	
	private void refreshTable() {
		iTable.clearTable();

		List<Widget> header = new ArrayList<Widget>();
		int col = 0;
		for (final Field field: iData.getFields()) {
			UniTimeTableHeader cell = new UniTimeTableHeader(field.getName(), col + 1 == iData.getFields().length && iData.isEditable() && iEditable ? 3 : 1);
			header.add(cell);
			final int index = col;
			cell.addOperation(new UniTimeTableHeader.Operation() {
				@Override
				public void execute() {
					iTable.sort(new Comparator<Record>() {
						public int compare(Record a, Record b) {
							String f = a.getField(index);
							String g = b.getField(index);
							if (f == null) {
								if (g != null) return 1;
							} else {
								if (g == null) return -1;
								int cmp = f.compareTo(g);
								if (cmp != 0) return cmp;
							}
							return (a.getUniqueId() == null ? b.getUniqueId() == null ? 0 : 1 : b.getUniqueId() == null ? -1 : a.getUniqueId().compareTo(b.getUniqueId()));
						}
					});
					saveOrder();
				}
				
				@Override
				public boolean isApplicable() {
					return true;
				}
				
				@Override
				public boolean hasSeparator() {
					return false;
				}
				
				@Override
				public String getName() {
					return "Sort by " + field.getName();
				}
			});
			col++;
		}
		iTable.addRow(null, header);
		
		boolean empty = false;
		int row = 1;
		for (Record r: iData.getRecords()) {
			fillRow(r, row++);
			empty = r.isEmpty();
		}
		if (!empty && iEditable && iData.isEditable())
			fillRow(iData.addRecord(null), row);
		
		iBottom.setVisible(true);
		if (iData.isEditable()) {
			iHeader.setEnabled("back", iEditable);
			iHeader.setEnabled("save", iEditable);
			iHeader.setEnabled("edit", !iEditable);
		}
		iHeader.clearMessage();
	}
	
	private void fillRow(Record record, int row) {
		List<Widget> line = new ArrayList<Widget>();
		int col = 0;
		for (Field field: iData.getFields()) {
			MyCell cell = new MyCell(iData.isEditable() && iEditable, field, record, col++);
			line.add(cell);
		}
		if (iData.isEditable() && iEditable) {
			Image add = new Image(RESOURCES.add());
			add.getElement().getStyle().setCursor(Cursor.POINTER);
			add.setTitle("Insert a new row above this row.");
			add.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					int row = iTable.getCellForEvent(event).getRowIndex();
					fillRow(iData.addRecord(null), iTable.insertRow(row));
				}
			});
			line.add(add);
			Image delete = new Image(RESOURCES.delete());
			delete.setTitle("Delete this row.");
			delete.getElement().getStyle().setCursor(Cursor.POINTER);
			delete.addClickHandler(new ClickHandler() {
				@Override
				public void onClick(ClickEvent event) {
					int row = iTable.getCellForEvent(event).getRowIndex();
					iData.getRecords().remove(iTable.getData(row));
					iTable.removeRow(row);
				}
			});
			line.add(delete);
		}
		iTable.setRow(row, record, line);
	}
	
	public class MyCell extends Composite implements HasFocus {
		private Field iField;
		private Record iRecord;
		private int iIndex;
		
		public MyCell(boolean editable, Field field, final Record record, final int index) {
			iField = field; iRecord = record; iIndex = index;
			if (editable) {
				if (field.getType() == FieldType.text) {
					final TextBox text = new TextBox();
					text.setStyleName("unitime-TextBox");
					text.setMaxLength(field.getLength());
					text.setText(record.getField(index));
					text.setWidth(field.getWidth() + "px");
					text.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, text.getText());
						}
					});
					initWidget(text);
				} else if (field.getType() == FieldType.list) {
					final ListBox list = new ListBox(false);
					list.setStyleName("unitime-TextBox");
					if (record.getField(index) == null) {
						list.addItem("", "");
					}
					for (ListItem item: field.getValues())
						list.addItem(item.getText(), item.getValue());
					for (int i = 0; i < list.getItemCount(); i++)
						if (list.getValue(i).equals(record.getField(index)))
							list.setSelectedIndex(i);
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, (list.getSelectedIndex() < 0 || list.getValue(list.getSelectedIndex()).isEmpty() ? null : list.getValue(list.getSelectedIndex())));
						}
					});
					initWidget(list);
				} else if (field.getType() == FieldType.multi) {
					final ListBox list = new ListBox(true);
					list.setStyleName("unitime-TextBox");
					list.setVisibleItemCount(3);
					for (ListItem item: field.getValues())
						list.addItem(item.getText(), item.getValue());
					String[] vals = record.getValues(index);
					if (vals != null) {
						for (String val: vals) {
							for (int i = 0; i < list.getItemCount(); i++)
								if (list.getValue(i).equals(val))
									list.setItemSelected(i, true);
						}
					}
					list.addChangeHandler(new ChangeHandler() {
						@Override
						public void onChange(ChangeEvent event) {
							record.setField(index, null);
							for (int i = 0; i < list.getItemCount(); i++ ) {
								if (list.isItemSelected(i)) record.addToField(index, list.getValue(i));
							}
						}
					});
					initWidget(list);
				} else if (field.getType() == FieldType.toggle) {
					final CheckBox check = new CheckBox();
					check.setValue(record.getField(index) == null ? null : "true".equalsIgnoreCase(record.getField(index)));
					check.addClickHandler(new ClickHandler() {
						@Override
						public void onClick(ClickEvent event) {
							record.setField(index, check.getValue() == null ? null : check.getValue() ? "true" : "false");
						}
					});
					initWidget(check);
				}
			} else {
				Label label = new Label(getValue());
				initWidget(label);
			}
		}
				
		public String getValue() {
			String value = iRecord.getField(iIndex);
			if (value == null) return "";
			if (iField.getType() == FieldType.list) {
				for (ListItem item: iField.getValues()) {
					if (item.getValue().equals(value)) return item.getText();
				}
			} else if (iField.getType() == FieldType.multi) {
				String text = "";
				for (String val: iRecord.getValues(iIndex)) {
					for (ListItem item: iField.getValues()) {
						if (item.getValue().equals(val)) {
							if (!text.isEmpty()) text += ", ";
							text += item.getText();
						}
					}
				}
				return text;
			}
			return value;
		}
		
		public Record getRecord() { return iRecord; }
		
		public boolean focus() { 
			if (getWidget() instanceof Focusable) {
				((Focusable)getWidget()).setFocus(true);
				if (getWidget() instanceof TextBox)
					((TextBox)getWidget()).selectAll();
				return true;
			}
			return false;
		}
	}

	public void saveOrder() {
		iHeader.setMessage("Saving order...");
		String ord = "";
		for (int i = 0; i < iTable.getRowCount(); i++) {
			Record r = iTable.getData(i);
			if (r == null || r.getUniqueId() == null) continue;
			if (!ord.isEmpty()) ord += "|";
			ord += r.getUniqueId();
		}
		List<String[]> data = new ArrayList<String[]>();
		data.add(new String[] {"SimpleEdit.Order[" + iType.toString() + "]", ord});
		iMenuService.setUserData(data, new AsyncCallback<Boolean>() {
			@Override
			public void onFailure(Throwable caught) {
				// iHeader.setErrorMessage("Failed to save table order (" + caught.getMessage() + ")");
				iHeader.clearMessage();
			}
			@Override
			public void onSuccess(Boolean result) {
				iHeader.clearMessage();
			}
		});
	}
}
