package org.unitime.timetable.gwt.client;

import java.util.List;

import org.unitime.timetable.gwt.services.MenuService;
import org.unitime.timetable.gwt.services.MenuServiceAsync;
import org.unitime.timetable.gwt.shared.MenuInterface;
import org.unitime.timetable.gwt.widgets.LoadingWidget;
import org.unitime.timetable.gwt.widgets.PageLabel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.Event.NativePreviewEvent;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.Frame;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.MenuItemSeparator;
import com.google.gwt.user.client.ui.RootPanel;

public class UniTimeMenuBar extends Composite {
	private final MenuServiceAsync iService = GWT.create(MenuService.class);
	
	private MenuBar iMenu;

	public UniTimeMenuBar() {
		
		iMenu = new MenuBar();
		iMenu.setVisible(false);
		iMenu.addStyleName("unitime-NoPrint");
		
		initWidget(iMenu);
		
		iService.getMenu(new AsyncCallback<List<MenuInterface>>() {
			
			@Override
			public void onSuccess(List<MenuInterface> result) {
				initMenu(iMenu, result, 0);
				iMenu.setVisible(true);
			}
			
			@Override
			public void onFailure(Throwable caught) {
				Window.alert("Failed to load the menu, reason: " + caught.getMessage());
			}
		});
		
		/*
		
		// Make some sub-menus that we will cascade from the top menu.
		Command cmd = new Command() {
			
			@Override
			public void execute() {
				Window.alert("Menu clicked.");
			}
		};
		
	    MenuBar coursesMenu = new MenuBar(true);
	    
	    MenuBar inputData = new MenuBar(true);
	    inputData.addItem("Instructional Offerings", new Command() {
			
			@Override
			public void execute() {
				LoadingWidget.getInstance().show();
				open("instructionalOfferingShowSearch.do?gwt.codesvr=127.0.0.1:9997");
			}
		});
	    inputData.addItem("Classes", cmd);
	    inputData.addItem("Curricula", new Command() {
			@Override
			public void execute() {
				openPageAsync("curricula");
			}
		});
	    inputData.addSeparator();
	    inputData.addItem("Instructors", cmd);
	    inputData.addItem("Designator List", cmd);
	    inputData.addSeparator();
	    inputData.addItem("Rooms", cmd);
	    inputData.addItem("Room Features", cmd);
	    inputData.addItem("Room Groups", cmd);
	    coursesMenu.addItem(new MenuItem("Input Data", inputData));

	    MenuBar solver = new MenuBar(true);
	    solver.addItem("Timetabling", cmd);
	    solver.addItem("Solver", cmd);
	    solver.addItem("Timetable", cmd);
	    coursesMenu.addItem(new MenuItem("Timetables", solver));

	    MenuBar barMenu = new MenuBar(true);
	    barMenu.addItem("the", cmd);
	    barMenu.addItem("bar", cmd);
	    barMenu.addItem("menu", cmd);

	    MenuBar bazMenu = new MenuBar(true);
	    bazMenu.addItem("the", cmd);
	    bazMenu.addItem("baz", cmd);
	    bazMenu.addItem("menu", cmd);

	    // Make a new menu bar, adding a few cascading menus to it.
	    MenuBar menu = new MenuBar();
	    menu.addItem("Courses", coursesMenu);
	    menu.addItem("Students", barMenu);
	    menu.addItem("Examinations", bazMenu);
	    menu.addItem("Events", cmd);
	    menu.addItem("Personal&nbsp;Schedule", true, cmd);
	    menu.addSeparator();
	    menu.addItem("Administration", cmd);
	    MenuItemSeparator s = new MenuItemSeparator();
	    s.setWidth("100%");
	    s.setTitle("Huray");
	    menu.addSeparator(s);
	    menu.addSeparator();
	    menu.setAnimationEnabled(true);
	    menu.addItem("Preferneces", cmd);
	    menu.addItem("Logout", cmd);
	    
	    menu.addStyleName("unitime-NoPrint");
	
		initWidget(menu);
		*/
	}
	
	private void initMenu(MenuBar menu, List<MenuInterface> items, int level) {
		MenuItemSeparator lastSeparator = null;
		for (final MenuInterface item: items) {
			if (item.isSeparator()) {
				lastSeparator = new MenuItemSeparator();
				menu.addSeparator(lastSeparator);
			} else if (item.hasSubMenus()) {
				MenuBar m = new MenuBar(true);
				initMenu(m, item.getSubMenus(), level + 1);
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, m));
			} else {
				menu.addItem(new MenuItem(item.getName().replace(" ", "&nbsp;"), true, new Command() {
					@Override
					public void execute() {
						if (item.getPage() != null) {
							if (item.isGWT()) 
								//openPageAsync(item.getPage());
								openUrl(item.getName(), "gwt.html?page=" + item.getPage(), item.getTarget());
							else {
								openUrl(item.getName(), item.getPage(), item.getTarget());
							}
						}
					}
				}));
			}
		}
		if (level == 0 && lastSeparator != null) {
			lastSeparator.setStyleName("unitime-BlankSeparator");
			lastSeparator.setWidth("100%");
		}
	}
	
	private void openUrl(String name, String url, String target) {
		/*
		if (url.indexOf('?') >= 0)
			url += "&gwt.codesvr=127.0.0.1:9997";
		else
			url += "?gwt.codesvr=127.0.0.1:9997";
		*/
		if (target == null)
			LoadingWidget.getInstance().show();
		if ("dialog".equals(target)) {
			final DialogBox dialog = new MyDialogBox();
			dialog.setAutoHideEnabled(true);
			dialog.setModal(true);
			final Frame frame = new Frame();
			frame.getElement().getStyle().setBorderWidth(0, Unit.PX);
			dialog.setGlassEnabled(true);
			dialog.setAnimationEnabled(true);
			dialog.setWidget(frame);
			dialog.setText(name);
			frame.setUrl(url);
			frame.setSize(String.valueOf(Window.getClientWidth() * 3 / 4), String.valueOf(Window.getClientHeight() * 3 / 4));
			dialog.center();
		} else {
			open(GWT.getHostPageBaseURL() + url);
		}
	}
	
	private native void open(String url) /*-{
		$wnd.location = url;
	}-*/;
	
	private void openPageAsync(final String page) {
		LoadingWidget.getInstance().show();
		if (RootPanel.get("UniTimeGWT:Content") == null || RootPanel.get("UniTimeGWT:TitlePanel") == null) {
			open("gwt.html?page=" + page);
			return;
		}
		RootPanel.get("UniTimeGWT:Content").clear();
		RootPanel.get("UniTimeGWT:Content").getElement().setInnerHTML(null);
		RootPanel.get("UniTimeGWT:TitlePanel").clear();
		RootPanel.get("UniTimeGWT:TitlePanel").getElement().setInnerHTML(null);
		GWT.runAsync(new RunAsyncCallback() {
			public void onSuccess() {
				openPage(page);
				LoadingWidget.getInstance().hide();
			}
			public void onFailure(Throwable reason) {
				Label error = new Label("Failed to load the page (" + reason.getMessage() + ")");
				error.setStyleName("unitime-ErrorMessage");
				RootPanel.get("UniTimeGWT:Content").add(error);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	private void openPage(String page) {
		try {
			for (Pages p: Pages.values()) {
				if (p.name().equals(page)) {
					LoadingWidget.getInstance().setMessage("Loading " + p.title() + " ...");
					RootPanel title = RootPanel.get("UniTimeGWT:Title");
					if (title != null) {
						title.clear();
						PageLabel label = new PageLabel(); label.setPageName(p.title());
						title.add(label);
					}
					RootPanel.get("UniTimeGWT:Content").add(p.widget());
					return;
				}
			}
			Label error = new Label("Failed to load the page (" + (page == null ? "page not provided" : "page " + page + " not registered" ) + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		} catch (Exception e) {
			Label error = new Label("Failed to load the page (" + e.getMessage() + ")");
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Content").add(error);
		}
	}
	
	public void insert(final RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
	}
	
	private class MyDialogBox extends DialogBox {
		private MyDialogBox() { super(); }
		protected void onPreviewNativeEvent(NativePreviewEvent event) {
			super.onPreviewNativeEvent(event);
			if (DOM.eventGetKeyCode((Event) event.getNativeEvent()) == KeyCodes.KEY_ESCAPE)
				MyDialogBox.this.hide();
		}
	}
}
