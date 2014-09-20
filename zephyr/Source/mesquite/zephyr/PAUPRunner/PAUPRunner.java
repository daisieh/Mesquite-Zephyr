/* Mesquite.zephyr source code.  Copyright 2007 and onwards D. Maddison and W. Maddison. 

Mesquite.zephyr is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.
Zephry's web site is http://mesquitezephyr.wikispaces.com

This source code and its compiled class files are free and modifiable under the terms of 
GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)
*/

package mesquite.zephyr.PAUPRunner;

import java.util.*;

import mesquite.categ.lib.*;
import mesquite.lib.*;
import mesquite.lib.characters.*;
import mesquite.lib.duties.*;
import mesquite.zephyr.lib.*;

/* TODO:
 * 	- get it so that either the shell doesn't pop to the foreground, or the runs are all done in one shell script, rather than a shell script for each
 */

public class PAUPRunner extends MesquiteModule implements OutputFileProcessor , ShellScriptWatcher {
	public static final String SCORENAME = "PAUPScore";
	Random rng;
	String PAUPPath;
	String datafname = null;
	String ofprefix = "output";
	String PAUPCommandFileMiddle ="";
	long  randseed = -1;
	String fileName = "";
	String treeFileName = "";
	boolean writeOnlySelectedTaxa = false;

	public boolean startJob(String arguments, Object condition, boolean hiredByName) {
		rng = new Random(System.currentTimeMillis());
		return true;
	}

/*	public PAUPRunner (MesquiteModule ownerModule, String PAUPPath, String datafname) {
		this.ownerModule= ownerModule;
		rng = new Random(System.currentTimeMillis());
		this.PAUPPath = PAUPPath;
		this.datafname = datafname;
	}
	*/


	/*.................................................................................................................*/
   	public String getDataFileName(){
   		return fileName;
   	}
	/*.................................................................................................................*/
   	public String getOutputTreeFileName(){
   		return treeFileName;
   	}
	/*.................................................................................................................*/
   	public String PAUPCommandFileStart(){
   		return "#NEXUS\n\nbegin paup;\n\tset torder=right tcompress increase=auto outroot=monophyl taxlabels=full nowarnreset nowarnroot NotifyBeep=no nowarntree nowarntsave;\n";
   	}
	/*.................................................................................................................*/
   	public String PAUPCommandFileEnd(){
   		return "\tquit;\nend;\n";
   	}
	/*.................................................................................................................*/
   	public void setPAUPCommandFileMiddle(String PAUPCommandFileMiddle){
   		this.PAUPCommandFileMiddle = PAUPCommandFileMiddle;   		
   	}
	/*.................................................................................................................*/
   	public String getPAUPCommandFile(PAUPCommander paupCommander, String fileName, String treeFileName, CategoricalData data){
   		StringBuffer sb = new StringBuffer();
   		sb.append(PAUPCommandFileStart());
   		sb.append(paupCommander.getPAUPCommandFileMiddle(fileName, treeFileName, data));
   		sb.append(PAUPCommandFileEnd());
   		return sb.toString();
   	}
	/*.................................................................................................................*/
   	public void setDataFName(String datafname){
   		this.datafname = datafname;
   	}
	/*.................................................................................................................*/
   	public void setPAUPSeed(long seed){
   		this.randseed = seed;
   	}

	ProgressIndicator progIndicator;
	int count=0;
	
	double finalValue = MesquiteDouble.unassigned;
	
	/*.................................................................................................................*/
	public Tree getTrees(TreeVector trees, Taxa taxa, MCharactersDistribution matrix, long seed, MesquiteDouble finalScore, String searchName, PAUPCommander paupCommander) {
		if (matrix==null || paupCommander==null)
			return null;
	/*
	 * 	if (!(matrix.getParentData() != null && matrix.getParentData() instanceof MolecularData)){
			MesquiteMessage.discreetNotifyUser("Sorry, PAUPTree works only if given a full MolecularData object");
			return null;
		}
*/

		setPAUPSeed(seed);
		
		CategoricalData data = (CategoricalData)matrix.getParentData();
	
		getProject().incrementProjectWindowSuppression();

		data.setEditorInhibition(true);

		String rootDir = MesquiteFileUtil.createDirectoryForFiles(this, MesquiteFileUtil.BESIDE_HOME_FILE, "PAUP*", "-Run.");  
		if (rootDir==null)
			return null;

		String unique = MesquiteTrunk.getUniqueIDBase() + Math.abs(rng.nextInt());
		fileName = "tempData" + MesquiteFile.massageStringToFilePathSafe(unique) + ".nex";   //replace this with actual file name?
		String filePath = rootDir +  fileName;

	  	ZephyrUtil.writeNEXUSFile(taxa,  rootDir,  fileName,  filePath,  data,false, writeOnlySelectedTaxa, true, false);
	  		 

		String runningFilePath = rootDir + "running" + MesquiteFile.massageStringToFilePathSafe(unique);
		//String outFilePath = rootDir + "tempTree" + MesquiteFile.massageStringToFilePathSafe(unique) + ".tre";

		StringBuffer shellScript = new StringBuffer(1000);
		
		String commandFileName =  "PAUPCommands.txt";
		treeFileName = ofprefix+".tre";
		String commandFilePath = rootDir + commandFileName;
		String treeFilePath = rootDir + treeFileName;
		String logFileName = ofprefix+".log00.log";
		String[] logFilePath = {rootDir + logFileName};

		MesquiteFile.putFileContents(commandFilePath,getPAUPCommandFile(paupCommander, fileName, treeFileName, data), true);   // saving the PAUP command file
		
		shellScript.append(ShellScriptUtil.getChangeDirectoryCommand(rootDir)+ StringUtil.lineEnding());
		shellScript.append(getPAUPCommand()+ " " + ShellScriptUtil.protectForShellScript(commandFilePath)+ StringUtil.lineEnding());
	
		shellScript.append(ShellScriptUtil.getRemoveCommand(runningFilePath));

		String scriptPath = rootDir + "PAUPScript" + MesquiteFile.massageStringToFilePathSafe(unique) + ".bat";
		MesquiteFile.putFileContents(scriptPath, shellScript.toString(), true);


		progIndicator = new ProgressIndicator(getProject(),searchName, "PAUP Search", 0, true);
		if (progIndicator!=null){
			count = 0;
			progIndicator.start();
		}

		boolean success = ShellScriptUtil.executeLogAndWaitForShell(scriptPath, "PAUP", logFilePath, this,this);
		//executeAndWaitForPAUP(scriptPath, runningFilePath, outFilePath);


		if (progIndicator!=null)
			progIndicator.goAway();

		
		if (success){
			success = false;
			FileCoordinator coord = getFileCoordinator();
			MesquiteFile tempDataFile = null;
			CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
			CommandRecord scr = new CommandRecord(true);
			MesquiteThread.setCurrentCommandRecord(scr);
			tempDataFile = (MesquiteFile)coord.doCommand("includeTreeFile", StringUtil.tokenize(treeFilePath) + " " + StringUtil.tokenize("#InterpretNEXUS") + " suppressImportFileSave taxa "+StringUtil.tokenize(taxa.getName()), CommandChecker.defaultChecker); //TODO: never scripting???
			MesquiteThread.setCurrentCommandRecord(oldCR);

			TreesManager manager = (TreesManager)findElementManager(TreeVector.class);
			Tree t =null;
			int numTB = manager.getNumberTreeBlocks(taxa);
			TreeVector tv = manager.getTreeBlock(taxa,numTB-1);
			if (tv!=null) {
				t = tv.getTree(0);
				if (t!=null)
					success=true;
				if (tv.getNumberOfTrees()>=1 && trees !=null) {
					for (int i=0; i<tv.getNumberOfTrees(); i++)
						trees.addElement(tv.getTree(i), false);
				} 
			}
			//int numTB = manager.getNumberTreeBlocks(taxa);
			
			getProject().decrementProjectWindowSuppression();
			if (tempDataFile!=null)
				tempDataFile.close();
			deleteSupportDirectory();
			data.setEditorInhibition(false);
			manager.deleteElement(tv);  // get rid of temporary tree block
			if (success) { 
				postBean("successful", false);
				return t;
			}
			postBean("failed", false);
			return null;
		}
		deleteSupportDirectory();
		getProject().decrementProjectWindowSuppression();
		data.setEditorInhibition(false);

		return null;
	}	
	
	public boolean continueShellProcess(Process proc){
		return true;
	}


	/*.................................................................................................................*/
	String getPAUPCommand(){
		if (MesquiteTrunk.isWindows())
			return StringUtil.protectForWindows(PAUPPath);
		else
			return StringUtil.protectForUnix(PAUPPath);
	}

	Parser parser = new Parser();
	
	/*.................................................................................................................*/
	public String[] modifyOutputPaths(String[] outputFilePaths){
		return outputFilePaths;
	}

	/*.................................................................................................................*/

	public void processOutputFile(String[] outputFilePaths, int fileNum) {
		if (fileNum==0 && outputFilePaths.length>0 && !StringUtil.blank(outputFilePaths[0])) {
		//	String s = MesquiteFile.getFileLastContents(outputFilePaths[0]);
		//	if (!StringUtil.blank(s))
		}
		
	}
	/*.................................................................................................................*/

	public void processCompletedOutputFiles(String[] outputFilePaths) {
		if ( outputFilePaths.length>0 && !StringUtil.blank(outputFilePaths[0])) {
		}

	}

	/*.................................................................................................................*/
   	public void setPAUPPath(String PAUPPath){
   		this.PAUPPath = PAUPPath;
   	}

	public Class getDutyClass() {
		return PAUPRunner.class;
	}

	public String getName() {
		return "PAUPRunner";
	}

	public boolean isWriteOnlySelectedTaxa() {
		return writeOnlySelectedTaxa;
	}

	public void setWriteOnlySelectedTaxa(boolean writeOnlySelectedTaxa) {
		this.writeOnlySelectedTaxa = writeOnlySelectedTaxa;
	}


}
