package net.sourceforge.ondex.core.memorydist;

import com.hazelcast.core.HazelcastInstance;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * A simple commandline Hazelcast node. You can use this program to donate
 * the host computer's RAM resources to a Hazelcast cluster. Simply start this
 * program with a Hazelcast configuration file containing the details for the
 * network that it should join.
 * 
 * @author Keith Flanagan
 */
public class HazelcastNode 
{
  public static void main(String[] args) 
      throws FileNotFoundException
  {
    if (args.length != 1) 
    {
      System.out.println("USAGE: \n"
          + "  * Path to your Hazelcast configuration file.");
      System.exit(1);
    }
    
    File configFile = new File(args[0]);
    HazelcastInstance hz = HazelcastInstanceFactory.createInstance(configFile);
    System.out.println("\n\n\n\n");
    System.out.println("*****************************************************");
    System.out.println("Successfully joined the Hazelcast network.");
    System.out.println("Press Ctrl-C to exit.");
    System.out.println("*****************************************************");
    System.out.println("\n\n\n\n");
  }
}
