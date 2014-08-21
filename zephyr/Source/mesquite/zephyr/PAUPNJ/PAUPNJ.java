/* Mesquite.zephyr source code.  Copyright 2007 and onwards D. Maddison and W. Maddison. 

Mesquite.zephyr is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Zephry's web site is http://mesquitezephyr.wikispaces.com

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.zephyr.PAUPNJ;

import java.awt.*;

import mesquite.lib.*;
import mesquite.zephyr.lib.*;
import mesquite.categ.lib.*;

public class PAUPNJ extends PAUPTreeSearcher {
	int bootStrapReps = 500;
	boolean doBootstrap = false;
	protected String paupCommands = "";

	public String getExtraTreeWindowCommands (){

		String commands = "setSize 400 600; ";
		commands += " getTreeDrawCoordinator #mesquite.trees.BasicTreeDrawCoordinator.BasicTreeDrawCoordinator;\ntell It; ";
		commands += "setTreeDrawer  #mesquite.trees.SquareTree.SquareTree; tell It; orientRight; ";
		commands += "setNodeLocs #mesquite.trees.NodeLocsStandard.NodeLocsStandard;";
		if (!doBootstrap)
			commands += " tell It; branchLengthsToggle on; endTell; ";
		commands += " setEdgeWidth 3; endTell; ";
		if (doBootstrap)
			commands += "labelBranchLengths on; setNumBrLenDecimals 0; showBrLenLabelsOnTerminals off; showBrLensUnspecified off; setBrLenLabelColor 0 0 0;";
		commands += " endTell; ladderize root; ";
		return commands;
	}


	/*.................................................................................................................*/
	public void processMorePreferences (String tag, String content) {
		if ("bootStrapReps".equalsIgnoreCase(tag))
			bootStrapReps = MesquiteInteger.fromString(content);
		if ("bootstrap".equalsIgnoreCase(tag))
			doBootstrap = MesquiteBoolean.fromTrueFalseString(content);
		if ("paupCommands".equalsIgnoreCase(tag))
			paupCommands = StringUtil.cleanXMLEscapeCharacters(content);
	}
	/*.................................................................................................................*/
	public String prepareMorePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "bootStrapReps", bootStrapReps);  
		StringUtil.appendXMLTag(buffer, 2, "bootstrap", doBootstrap);  
		StringUtil.appendXMLTag(buffer, 2, "paupCommands", paupCommands);  
		return buffer.toString();
	}

	Checkbox bootstrapBox;
	IntegerField bootStrapRepsField;
	TextArea paupCommandsField;
	/*.................................................................................................................*/
	public void queryOptionsSetup(ExtensibleDialog dialog) {
		String helpString = "\nIf \"bootstrap\" is on, the PAUP will do a neighbor-joining bootstrap of the number of replicates specified; otherwise, it will do a simple neighbor-joining analysis.";
		helpString+= "\nAny PAUP commands entered in the Additional Commands field will be executed in PAUP immediately before the nj or bootstrap command.";
		dialog.appendToHelpString(helpString);

		bootstrapBox = dialog.addCheckBox("bootstrap", doBootstrap);
		bootStrapRepsField = dialog.addIntegerField("Bootstrap Reps", bootStrapReps, 8, 1, MesquiteInteger.infinite);

		dialog.addLabel("Additional commands before nj or bootstrap command: ");
		paupCommandsField =dialog.addTextAreaSmallFont(paupCommands,4);
	}

	/*.................................................................................................................*/
	public void queryOptionsProcess(ExtensibleDialog dialog) {
		bootStrapReps = bootStrapRepsField.getValue();
		doBootstrap = bootstrapBox.getState();
		paupCommands = paupCommandsField.getText();
	}

	/*.................................................................................................................*/
	public String getPAUPCommandFileMiddle(String dataFileName, String outputTreeFileName, CategoricalData data){
		StringBuffer sb = new StringBuffer();
		sb.append("\texec " + StringUtil.tokenize(dataFileName) + ";\n");
		sb.append("\tset criterion=distance;\n");
		sb.append("\tdset negbrlen=prohibit;\n");
		if (data instanceof DNAData)
			sb.append("\tdset distance=hky85;\n");
		sb.append(paupCommands+ "\n");
		if (doBootstrap && bootStrapReps>0) {
			sb.append("\tboot nreps = " + bootStrapReps + " search=nj;\n");
			sb.append("\tsavetrees from=1 to=1 SaveBootP=brlens file=" + StringUtil.tokenize(outputTreeFileName) + ";\n");
		}
		else {
			sb.append("\tnj;\n");
			sb.append("\tsavetrees brlens=yes file=" + StringUtil.tokenize(outputTreeFileName) + ";\n");
		}
		return sb.toString();
	}
	
	/*.................................................................................................................*/
	public String getTreeBlockName(){
		if (doBootstrap) {
			return "PAUP* NJ Bootstrap Tree (Matrix: " + observedStates.getName() + ")";
		} 
		else {
			return "PAUP* NJ Tree (Matrix: " + observedStates.getName() + ")";

		}
	}

	/*.................................................................................................................*/
	public boolean isSubstantive(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean isPrerelease(){
		return true;
	}
	/*.................................................................................................................*/
	public boolean requestPrimaryChoice(){
		return true;
	}
	/*.................................................................................................................*/
	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
	public int getVersionOfFirstRelease(){
		return NEXTRELEASE;  
	}



	public String getExplanation() {
		return "If PAUP is installed, will save a copy of a character matrix and script PAUP to conduct a neighbor-joining or bootstrap neighbor-joining, and harvest the resulting trees.";
	}
	public String getName() {
		return "Neighbor-Joining (PAUP)";
	}
	public String getNameForMenuItem() {
		return "Neighbor-Joining (PAUP)...";
	}


}
