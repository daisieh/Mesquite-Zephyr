package mesquite.zephyr.GarliScore;


import mesquite.lib.*;
import mesquite.lib.duties.*;
import mesquite.zephyr.GarliRunner.*;


public class GarliScore extends NumberForTree {

    /* ................................................................................................................. */

    public boolean startJob(String arguments, Object condition, boolean hiredByName) {
        return true;
    }

	/*.................................................................................................................*/
  	 public boolean isPrerelease(){
  	 	return false;
  	 }

    /* ................................................................................................................. */
      public void calculateNumber(Tree tree, MesquiteNumber result, MesquiteString resultString) {
        if (result == null || tree == null)
            return;
	   	clearResultAndLastResult(result);
       if (tree instanceof Attachable){
        	Object obj = ((Attachable)tree).getAttachment(GarliRunner.SCORENAME);
        	if (obj == null){
        			if (resultString != null)
        				resultString.setValue("No Garli score is associated with this tree.  To obtain a score, use as tree source \"Garli Trees\".");
        			return;
        	}
        	if (obj instanceof MesquiteDouble)
        			result.setValue(((MesquiteDouble)obj).getValue());
			else if (obj instanceof MesquiteNumber)
				result.setValue((MesquiteNumber)obj);
        }
       
        if (resultString != null) {
            resultString.setValue("Garli score : " + result.toString());
        }
		saveLastResult(result);
		saveLastResultString(resultString);
      }

  	/*.................................................................................................................*/
   	/** returns the version number at which this module was first released.  If 0, then no version number is claimed.  If a POSITIVE integer
   	 * then the number refers to the Mesquite version.  This should be used only by modules part of the core release of Mesquite.
   	 * If a NEGATIVE integer, then the number refers to the local version of the package, e.g. a third party package*/
      	public int getVersionOfFirstRelease(){
      		return -100;  
      	}
  /* ................................................................................................................. */
    /** Explains what the module does. */

    public String getExplanation() {
        return "Supplies - ln L score from Garli";
    }

    /* ................................................................................................................. */
    /** Name of module */
    public String getName() {
        return "Garli Score";
    }
}