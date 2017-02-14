/*
 * Copyright 2012 Keith Flanagan
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * File created: 08-Oct-2012, 11:39:03
 */

package net.sourceforge.ondex.core.memorydist;

import com.hazelcast.config.ClasspathXmlConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.FileSystemXmlConfig;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import java.io.File;
import java.io.FileNotFoundException;

/**
 *
 * @author Keith Flanagan
 */
public class HazelcastInstanceFactory
{
  private static final String DEFAULT_CONFIG_RES = "/ondex-hazelcast.xml";
  private static final String DEFAULT_HZ_FILENAME = "ondex-hazelcast.xml";
  
  public static HazelcastInstance createInstance() throws FileNotFoundException
  {
      /*
       * Note that 'Config.ondexDir' actually points to $ONDEX_HOME/data, 
       * rather than $ONDEX_HOME/config, as you might expect.
       */
      File configFile = new File(
          net.sourceforge.ondex.config.Config.ondexDir, DEFAULT_HZ_FILENAME);
      return createInstance(configFile);
  }
  
  
  public static HazelcastInstance createInstance(String hazelcastConfigResourcePath)
  {
    Config hzConfig = new ClasspathXmlConfig(hazelcastConfigResourcePath);
    HazelcastInstance hz = Hazelcast.newHazelcastInstance(hzConfig);
    return hz;
  }
  
  public static HazelcastInstance createInstance(File configFile)
      throws FileNotFoundException
  {
    Config hzConfig = new FileSystemXmlConfig(configFile);
    HazelcastInstance hz = Hazelcast.newHazelcastInstance(hzConfig);
    return hz;
  }
}
