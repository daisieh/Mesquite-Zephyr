/* Mesquite source code.  Copyright 1997 and onward, W. Maddison and D. Maddison. 


 Disclaimer:  The Mesquite source code is lengthy and we are few.  There are no doubt inefficiencies and goofs in this code. 
 The commenting leaves much to be desired. Please approach this source code with the spirit of helping out.
 Perhaps with your help we can be more than a few, and make Mesquite better.

 Mesquite is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
 Mesquite's web site is http://mesquiteproject.org

 This source code and its compiled class files are free and modifiable under the terms of 
 GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
 */
package mesquite.zephyr.TreeInferenceCoordinator;
/*~~  */

import java.util.*;

import mesquite.lib.*;
import mesquite.zephyr.lib.*;
import mesquite.lib.duties.*;

/* ======================================================================== */
/* this hires handlers, which run the tree inferences.  It is a central manager, not detail-oriented.*/
public class TreeInferenceCoordinator extends FileInit {
	Vector handlers;
	MesquiteHTMLWindow window = null;
	MesquiteCommand linkTouchedCommand;
	/*.................................................................................................................*/
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		if (numModulesAvailable(TreeInferer.class)>0){  //ExternalTreeSearcher
			getFileCoordinator().addSubmenu(MesquiteTrunk.analysisMenu, "Tree Inference", makeCommand("inferTrees",  this), TreeInferer.class);
			handlers = new Vector();
			linkTouchedCommand = new MesquiteCommand("linkTouched", this);
			makeMenu("Inference");
			addMenuItem("Stop All Inferences", makeCommand("stopAll", this));
			return true;
		}
		return false;
	}
	public Class getHireSubchoice(){  //somewhat nonstandard; this is not to hire a direct employee of this module, but rather to make the submenu and receive the indication as to what inferer the handler is supposed to hire
		return TreeInferer.class;
	}
	/*.................................................................................................................*/
	/** Generated by an employee who quit.  The MesquiteModule should act accordingly. */
	public void employeeQuit(MesquiteModule employee) {
		handlers.removeElement(employee);
		resetWindow();
	}

	//DW: get rid of progress indicators now that window is available???  They are very annoying
	//DW: if any inference going then add menu item to kill all inferences
	//DW: where to put autosave? -- best to put burden of choice on inferer's interface, and add method so handler can query inferer as to whether autosave is in effect
	//DW: currently ALWAYS on separate thread; therefore LOCK taxa block to editing?
	//DW: add window snapshot
	//DW: move to Analysis menu?
	/*.................................................................................................................*/
	public Snapshot getSnapshot(MesquiteFile file) { 
		Snapshot temp = new Snapshot();
		if (file == null || file == getProject().getHomeFile()){

			for (int i = 0; i<handlers.size(); i++) {
				MesquiteModule e=(MesquiteModule)handlers.elementAt(i);
				temp.addLine("restoreInference ", e); 
			}
		}
		if (handlers.size() >0)
			temp.addLine("resetWindow");
		return temp;
	}

	/*.................................................................................................................*/
	TreeInferenceHandler findHandlerByID(int id){
		for (int i = 0; i<handlers.size(); i++) {
			TreeInferenceHandler e=(TreeInferenceHandler)handlers.elementAt(i);
			if(e.getID() == id)
				return e;
		}
		return null;
	}
	/*.................................................................................................................*/
	String getStatusHTML(int numLinesPerHandler){
		if (handlers.size() == 0)
			return "No inferences running";
		String s = "<h2>Inferences in progress</h2><hr size=\"3\" noshade=\"noshade\" />";
		for (int i = 0; i<handlers.size(); i++) {
			TreeInferenceHandler e=(TreeInferenceHandler)handlers.elementAt(i);
			s += e.getHTMLDescriptionOfStatus(numLinesPerHandler) + " <a href = \"kill-" + e.getID() + "\">Stop</a><p><hr size=\"3\" noshade=\"noshade\" />";
		}
		return s;
	}
	/*.................................................................................................................*/
	void initiateWindow(){
		if (window == null) {
			window = new MesquiteHTMLWindow(this, linkTouchedCommand, "Tree Inference in Progress", false);
			window.setBackEnabled(false);
		}
		window.setText(getStatusHTML(getNumLinesPerHandler()));
		window.setPopAsTile(true);
		window.popOut(true);
		lastWindowStatePopped = true;
		window.setVisible(true);
		window.show();
		resetAllMenuBars();

	}
	
	int getNumLinesPerHandler(){
		if (window == null)
			return 0;
		
		int numLines = (window.getHeight()-30 - handlers.size()*50)/16;
		if (handlers.size()== 0)
			return numLines;
		return numLines/handlers.size();
		//rough guess: 100 pixels for extras, then 16 pixels per line
	}
	boolean lastWindowStatePopped = true;
	int resetCount = 0;
	/*.................................................................................................................*/
	void resetWindow(){
		if (window != null) {
			if (handlers.size()> 0){
				if (!window.isVisible()){
					window.setVisible(true);
				}

				if (lastWindowStatePopped && !window.isPoppedOut()){
					window.setPopAsTile(true);
					window.popOut(true);
				}
				
				window.setText(getStatusHTML(getNumLinesPerHandler()));
			}
			else {
				lastWindowStatePopped = window.isPoppedOut();
				window.setVisible(false);
			}

		}
	}
	/*.................................................................................................................*/
	public void employeeParametersChanged(MesquiteModule employee, MesquiteModule source, Notification notification) {
		resetWindow();
	}
	/*.................................................................................................................*/
	public Object doCommand(String commandName, String arguments, CommandChecker checker) {
		if (checker.compare(this.getClass(), "Hires a tree inferer and infers trees", null, commandName, "inferTrees")) {
			initiateWindow();
			TreeInferenceHandler handler = (TreeInferenceHandler)hireEmployee(TreeInferenceHandler.class, "Tree inference handler");
			if (handler !=null){
				handlers.addElement(handler);
				handler.doCommand("startInference", arguments);  //inferer name passed along to handler
				resetWindow();
				return handler;
			}
		}
		else if (checker.compare(this.getClass(), "Reconnects to a tree inferer ", null, commandName, "restoreInference")) {
			initiateWindow();
			TreeInferenceHandler handler = (TreeInferenceHandler)hireNamedEmployee(TreeInferenceHandler.class, arguments);
			if (handler !=null){
				handlers.addElement(handler);
				resetWindow();
				return handler;
			}
		}
		else if (checker.compare(this.getClass(), "resets the window", null, commandName, "resetWindow")) {
			resetWindow();

		}
		else if (checker.compare(this.getClass(), "Stops all inferences", null, commandName, "stopAll")) {
			alert("Sorry, not built yet!");
			//stop all!
		}
		else if (checker.compare(this.getClass(), "link touched", null, commandName, "linkTouched")) {
			String token = parser.getFirstToken(arguments);
			if (token != null && token.startsWith("kill")){
				String idS = token.substring(5, token.length());
				int id = MesquiteInteger.fromString(idS);
				TreeInferenceHandler handler = findHandlerByID(id);
				if (handler != null) {
					int response = 1;
					if (handler.canStoreLatestTree()){
						response = AlertDialog.query(containerOfModule(), "Save tree?", "Save the current tree in the inference?", "Save", "Don't Save", "Cancel", 2);
						if (response==0)
							handler.storeLatestTree();
					}
					if (response<2)
						handler.stopInference();
					return null;
				}
			}
		}
		else
			return  super.doCommand(commandName, arguments, checker);
		return null;
	}


	/*.................................................................................................................*/
	public boolean isPrerelease() { 
		return true;
	}
	/*.................................................................................................................*/
	public String getNameForMenuItem() {
		return "Tree Inference";
	}
	/*.................................................................................................................*/
	public String getName() {
		return "Tree Inference Coordinator";
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return 304;  
	}
	/*.................................................................................................................*/
	/** returns an explanation of what the module does.*/
	public String getExplanation() {
		return "Coordinates tree inferers." ;  
	}

}


