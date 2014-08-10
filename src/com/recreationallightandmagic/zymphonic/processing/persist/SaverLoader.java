package com.recreationallightandmagic.zymphonic.processing.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.codehaus.jackson.map.ObjectMapper;

/**
 * Could have just as well been named "FileUtil"
 */
public class SaverLoader {

	private static ObjectMapper mapper = new ObjectMapper();

	public static void save(String filename, WormholeState wormholeState)
			throws Exception {
		File file = new File(filename);
		if (file.exists()) {
			throw new RuntimeException("Overwrite file forbidden.  rm "
					+ file.getAbsolutePath() + "; if you must");
		}
		System.out.println("Writing to " + file.getAbsolutePath());
		FileOutputStream out = new FileOutputStream(file);
		mapper.writeValue(out, wormholeState);
		out.close();
	}

	public static WormholeState load(String filename) throws Exception {
		File file = new File(filename);
		if (!file.exists()) {
			throw new RuntimeException("Can't find file "
					+ file.getAbsolutePath());
		}
		return mapper.readValue(new FileInputStream(file), WormholeState.class);
	}

}
