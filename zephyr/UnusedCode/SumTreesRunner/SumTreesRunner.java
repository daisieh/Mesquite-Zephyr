package mesquite.zephyr.SumTreesRunner;
import java.awt.Button;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.Random;

import mesquite.lib.*;
import mesquite.lib.duties.FileCoordinator;
import mesquite.lib.duties.TreeSource;
import mesquite.lib.duties.TreesManager;
import mesquite.lib.ProgressIndicator;
import mesquite.zephyr.lib.ZephyrUtil;
import mesquite.zephyr.lib.SimpleLogger;
import mesquite.zephyr.lib.Subprocess;

public class SumTreesRunner extends MesquiteModule implements ActionListener {
	int currentTree=0;
	TreeSource treeSource;
	Taxa taxa=null;
	MesquiteString treeSourceName, consenserName; 
	MesquiteCommand tlsC;
	
	boolean assigned = false;
	Random rng;
	MesquiteModule owner;
	boolean preferencesSet = false;
	SimpleLogger logger;
	
	//GUI 
	SingleLineTextField exePathField;
	SingleLineTextField pythonPathField;
	
	String supportFileName;
	String targetFileName;
	String outputFileName;
	
	// User preferences control the following
	String sumTreesPath;
	String pythonPath;
	
	
	public boolean startJob(String arguments, Object condition, boolean hiredByName) {

		loadPreferences(null);
		this.rng = new Random(System.currentTimeMillis());
		this.logger = new SimpleLogger(this);
		
		this.treeSource = (TreeSource) hireEmployee(TreeSource.class, "Source of Trees for support ");
		if (this.treeSource == null) {
			return sorry("failing due to lack of source of trees");
		}
		this.tlsC = makeCommand("setTreeSource",  this);
		this.treeSource.setHiringCommand(tlsC);
		this.treeSourceName = new MesquiteString();
		if (numModulesAvailable(TreeSource.class) > 1) {
			MesquiteSubmenuSpec mss = addSubmenu(null, "Tree Source for SumTrees", tlsC, TreeSource.class);
			mss.setSelected(treeSourceName);
		}
		
		return (MesquiteThread.isScripting() || this.queryOptions());
	}

	
	public void initialize(MesquiteModule ownerArg, Taxa taxaArg) { //, Taxa taxaArg) {
		this.owner = ownerArg;
		this.taxa = taxaArg;
	}



	public Class getDutyClass() {
		// TODO Auto-generated method stub
		return SumTreesRunner.class;
	}


	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return "SumTreesRunner";
	}
	
	public void findSupportForSplitsInTree(Tree tree) throws Exception {
		invokeSumTrees(tree);
	}
	
	public Tree invokeSumTrees(Tree tree) throws Exception {
		
		String rootDir = ZephyrUtil.createDirectoryForFiles(this, ZephyrUtil.BESIDE_HOME_FILE, "SumTrees");
		if (rootDir == null)
			return null;

		supportFileName = "tempSupportTrees.nex";
	  	ZephyrUtil.writeNEXUSTreeFile(taxa, this.treeSource, rootDir, supportFileName);
	  	
	  	this.targetFileName = null;
	  	if (tree != null) {
	  		targetFileName = "tempTargetTrees.nex";
	  		ZephyrUtil.writeNEXUSTreeFile(taxa, tree, rootDir, targetFileName);
	  	}
	  	
	  	this.outputFileName = "out.nex";
	  	
	  	
	  	Subprocess sumTreesInstance = new Subprocess(this.pythonPath);
	  	sumTreesInstance.setLogger(this.logger);
	  	sumTreesInstance.setWorkingDirPath(rootDir);
	  	if (this.targetFileName != null) {
	  		String [] args = {this.sumTreesPath, "--target=" + this.targetFileName, "--output=" + this.outputFileName, this.supportFileName};
	  		sumTreesInstance.setArguments(args);
	  	}
	  	else {
	  		String [] noTargetArgs = {this.sumTreesPath, "--output=" + this.outputFileName, this.supportFileName};
	  		sumTreesInstance.setArguments(noTargetArgs);
	  	}
	  	sumTreesInstance.setWaitForExecution(true);
	  	
	  	// lock the project so that we can read the last trees block and know that it is ours.
	  	getProject().incrementProjectWindowSuppression();
	  	Tree toReturn = null;
		try {
		  	ProgressIndicator progIndicator = new ProgressIndicator(getProject(), null, "SumTrees", 0, true);
			if (progIndicator!=null)
				progIndicator.start();
		
			int retcode = sumTreesInstance.execute();
			boolean success = (retcode == 0);
	
			if (progIndicator != null)
				progIndicator.goAway();
	
			
			if (success) {
				success = false;
				FileCoordinator coord = getFileCoordinator();
				MesquiteFile tempDataFile = null;
				CommandRecord oldCR = MesquiteThread.getCurrentCommandRecord();
				CommandRecord scr = new CommandRecord(true);
				MesquiteThread.setCurrentCommandRecord(scr);
				File outFile = new File(rootDir, this.outputFileName);
				tempDataFile = (MesquiteFile) coord.doCommand("includeTreeFile", StringUtil.tokenize(outFile.getAbsolutePath()) + " " + StringUtil.tokenize("#InterpretNEXUS") + " suppressImportFileSave ", CommandChecker.defaultChecker); //TODO: never scripting???
				MesquiteThread.setCurrentCommandRecord(oldCR);
	
				TreesManager manager = (TreesManager)findElementManager(TreeVector.class);
				Tree t =null;
				int numTB = manager.getNumberTreeBlocks(taxa);
				TreeVector tv = manager.getTreeBlock(taxa, numTB - 1);
				if (tv!=null) {
					t = tv.getTree(0);
					if ( t != null)
						success=true;
					if (tv.getNumberOfTrees() != 1) {
						logger.warn("No Trees generated by SumTrees");
						return null;
					}
					toReturn = tv.getTree(0);

				}
				if (tempDataFile!=null)
					tempDataFile.close();
				manager.deleteElement(tv);  // get rid of temporary tree block
			}
			try {
				logger.warn("Deletion of temp dir suppressed.");
				//deleteSupportDirectory();
			}
			catch (Exception x) {
				logger.warn("Could not delete the directory " + rootDir);
				throw x;
			}
		}
		finally {
			getProject().decrementProjectWindowSuppression();
		}
		return toReturn;
		
	}


	public boolean getPreferencesSet() {
		return preferencesSet;
	}
	public void setPreferencesSet(boolean b) {
		preferencesSet = b;
	}
	/*.................................................................................................................*/
	public void processSingleXMLPreference (String tag, String content) {
		if ("pythonPath".equalsIgnoreCase(tag)) 
			pythonPath = StringUtil.cleanXMLEscapeCharacters(content);
		if ("sumTreesPath".equalsIgnoreCase(tag)) 
			sumTreesPath = StringUtil.cleanXMLEscapeCharacters(content);
		preferencesSet = true;
	}
	/*.................................................................................................................*/
	public String preparePreferencesForXML () {
		StringBuffer buffer = new StringBuffer(200);
		StringUtil.appendXMLTag(buffer, 2, "sumTreesPath", sumTreesPath);  
		StringUtil.appendXMLTag(buffer, 2, "pythonPath", pythonPath);  
		preferencesSet = true;
		return buffer.toString();
	}

	/*.................................................................................................................*/
	public boolean queryOptions() {
		MesquiteInteger buttonPressed = new MesquiteInteger(1);
		ExtensibleDialog dialog = new ExtensibleDialog(containerOfModule(), "SumTrees Options & Locations", buttonPressed); 
		
		//dialog.addLabel("Garli - Options and Locations");

		String helpString = "This module will run SumTrees to calculate support values for nodes in a particular tree.  A command-line version of SumTrees must be installed. ";

		dialog.appendToHelpString(helpString);

		MesquiteTabbedPanel tabbedPanel = dialog.addMesquiteTabbedPanel();

		tabbedPanel.addPanel("SumTrees Location", true);
		this.exePathField = dialog.addTextField("Path to SumTrees:", this.sumTreesPath, 40);
		Button exeBrowseButton = dialog.addAListenedButton("Browse...", null, this);
		exeBrowseButton.setActionCommand("sumTreesBrowse");

		this.pythonPathField = dialog.addTextField("Path to Python:", this.pythonPath, 40);
		Button pythonBrowseButton = dialog.addAListenedButton("Browse...", null, this);
		pythonBrowseButton.setActionCommand("pythonBrowse");

		
		tabbedPanel.cleanup();
		dialog.nullifyAddPanel();

		dialog.completeAndShowDialog(true);
		
		if (buttonPressed.getValue() == 0)  {
			this.sumTreesPath = exePathField.getText();
			this.pythonPath = pythonPathField.getText();
			storePreferences();
		}
		dialog.dispose();
		return (buttonPressed.getValue()==0) && !StringUtil.blank(this.sumTreesPath) && !StringUtil.blank(this.pythonPath);
	}


	public boolean isPrerelease() {
		return true;
	}
	public boolean requestPrimaryChoice() {
		return true; 
	}


	/*.................................................................................................................*/
	public String[] modifyOutputPaths(String[] outputFilePaths){
		return outputFilePaths;
	}
	/*.................................................................................................................*/
	public String getOutputFileToReadPath(String originalPath) {
		File file = new File(originalPath);
		File fileCopy = new File(originalPath + "2");
		if (file.renameTo(fileCopy))
			return originalPath + "2";
		return originalPath;
	}
	/*.................................................................................................................*/

	public void processOutputFile(String[] outputFilePaths, int fileNum) {}
	/*.................................................................................................................*/

	public void processCompletedOutputFiles(String[] outputFilePaths) {
	}


	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equalsIgnoreCase("sumTreesBrowse")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			this.sumTreesPath = MesquiteFile.openFileDialog("Choose SumTrees", directoryName, fileName);
			this.exePathField.setText(this.sumTreesPath);
		}
		if (e.getActionCommand().equalsIgnoreCase("pythonBrowse")) {
			MesquiteString directoryName = new MesquiteString();
			MesquiteString fileName = new MesquiteString();
			this.pythonPath = MesquiteFile.openFileDialog("Choose Python", directoryName, fileName);
			this.pythonPathField.setText(this.pythonPath);
		}
		
	}


}

