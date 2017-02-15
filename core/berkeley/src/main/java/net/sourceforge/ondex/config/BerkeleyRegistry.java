package net.sourceforge.ondex.config;

import java.util.HashMap;

import net.sourceforge.ondex.core.persistent.BerkeleyEnv;

public class BerkeleyRegistry {
	public static HashMap<Long,BerkeleyEnv> sid2berkeleyEnv = new HashMap<Long,BerkeleyEnv>();
}
