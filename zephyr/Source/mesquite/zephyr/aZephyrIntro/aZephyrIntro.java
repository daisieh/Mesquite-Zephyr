/* Mesquite.zephyr source code.  Copyright 2007 and onwards D. Maddison and W. Maddison. Mesquite.zephyr is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY.Zephry's web site is http://mesquitezephyr.wikispaces.comThis source code and its compiled class files are free and modifiable under the terms of GNU Lesser General Public License.  (http://www.gnu.org/copyleft/lesser.html)*/package mesquite.zephyr.aZephyrIntro;import mesquite.lib.MesquiteTrunk;import mesquite.lib.duties.*;/* ======================================================================== */public class aZephyrIntro extends PackageIntro {	/*.................................................................................................................*/	public boolean startJob(String arguments, Object condition, boolean hiredByName) { 		return true;  	 }  	 public Class getDutyClass(){  	 	return aZephyrIntro.class;  	 } 	/*.................................................................................................................*/	 public String getExplanation() {	return "Zephyr is a package of Mesquite modules providing tools for connecting to other phylogenetic inference programs.";	 }   	/*.................................................................................................................*/    	 public String getName() {		return "Zephyr Package";   	 }	/*.................................................................................................................*/	/** Returns the name of the package of modules (e.g., "Basic Mesquite Package", "Rhetenor")*/ 	public String getPackageName(){ 		return "Zephyr Package"; 	}	/*.................................................................................................................*/	/** Returns citation for a package of modules*/ 	public String getPackageCitation(){ 		return "Maddison, DR & Maddison WP.  2015.  Zephyr: A Mesquite package for interacting with external phylogeny inference programs. Version "+getPackageVersion(); 	}	/*.................................................................................................................*/	/** Returns whether there is a splash banner*/	public boolean hasSplash(){ 		return true; 	}	/*.................................................................................................................*/	/** Returns version for a package of modules*/	public String getPackageVersion(){				return "1.1+";	}	/*.................................................................................................................*/	/** Returns version for a package of modules as an integer*/	public int getPackageVersionInt(){		return 1100;	}	/*.................................................................................................................*/	public String getPackageDateReleased(){		return "8 June 2015";	}	/*.................................................................................................................*/	/** Returns build number for a package of modules as an integer*/	public int getPackageBuildNumber(){		return 32;	}	/*  Release dates:	 *   1.0 (no build number stated):  29 August 2014	 *   1.0 (build 11):  20 September 2014	 *   1.1 (build 23):  6 January 2015	 * */	/*.................................................................................................................*/	public boolean isPrerelease(){		return true;  	}	/*.................................................................................................................*/	/** Returns the kind of repository*/	public int getRepositoryKind(){		return GITHUBREPOSITORY;	}	/** Returns the URL for the release package*/	public String getReleaseURL(){		return "https://github.com/MesquiteProject/Mesquite-Zephyr/releases/download/1.1/Zephyr.1.1.zip";	}	/*.................................................................................................................*/	/** Returns the tag for the release tag*/	public String getReleaseTag(){		return "1.1";	}	/*.................................................................................................................*/	/** Returns the repository path on the repository server*/	public String getRepositoryPath(){		return "MesquiteProject/Mesquite-Zephyr";	}	/*.................................................................................................................*/	/** Returns the repository URL*/	public String getRepositoryFullURL(){		return "https://github.com/MesquiteProject/Mesquite-Zephyr";	}		/*.................................................................................................................*/	/** Returns the URL for the web page about the package*/	public String getPackageURL(){		return "https://mesquitezephyr.wikispaces.com";	}/*.................................................................................................................*/	/** returns the URL of the notices file for this module so that it can phone home and check for messages */	public String  getHomePhoneNumber(){ 		if (MesquiteTrunk.debugMode)			return "http://mesquiteproject.org/packages/zephyr/noticesDev.xml";		else if (isPrerelease()) 			return "http://mesquiteproject.org/packages/zephyr/noticesPrerelease.xml";		else			return "http://mesquiteproject.org/packages/zephyr/notices.xml";	}	/*.................................................................................................................*/	public int getVersionOfFirstRelease(){		return 300;  	}}