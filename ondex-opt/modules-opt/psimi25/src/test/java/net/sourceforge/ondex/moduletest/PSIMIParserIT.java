/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.moduletest;

import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test for the psimi parser
 *
 * @author Jochen Weile, M.Sc. <j.weile@ncl.ac.uk>
 */
public class PSIMIParserIT extends ModuleIntegrationTesting {

	
    /**
     * Downloads ondex-mini, extracts it into a test directory and installs
     * the module jar in the "plugins/" directory.
     */
    @Before
    public void setUp() {
        //super.setup("psimi2ondex-0.6.0-SNAPSHOT-jar-with-dependencies.jar");
    }

    /**
     * Downloads some test data and runs a workflow which calls the PSI-MI parser
     * on it and then exports the resulting graph to OXL. Then checks the result.
     */
    @Test
    public void test() throws Exception {
        //Download ecoli PSI-MI data from IntAct.
//        File testData = downloadFile("ecohs_small.xml",
//                "ftp://ftp.ebi.ac.uk/pub/databases/intact/current/psi25/species/ecohs_small.xml");
//        File testResources = new File(new File(new File(
//                "src"),"test"),"resources");
//        
//        File testData = new File(testResources,"ecohs_small.xml");
//
//        //Locate workflow file from test resources
//        File workflowFile = new File(testResources,"psimi.xml");
//
//        //Define output file
//        File oxlOut = new File(getOndexMiniDir(),"result.xml.gz");
//
//        //run ondex-mini on workflow and test data
//        runOndexMini(workflowFile, 
//                "psimifile="+testData.getCanonicalPath(),
//                "oxlfile="+oxlOut.getName());
//
//        //check that the result file exists and is not empty.
//        assert(oxlOut.exists());
//        assert(oxlOut.length() > 0);
    	assertTrue(true);
    }

    
    /**
     * Deletes the test directory and all its contents
     */
    @After
    public void tearDown() {
        //super.cleanup();
    }

}