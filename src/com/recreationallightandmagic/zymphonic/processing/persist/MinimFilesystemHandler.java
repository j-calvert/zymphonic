package com.recreationallightandmagic.zymphonic.processing.persist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.recreationallightandmagic.zymphonic.processing.Constants;

public class MinimFilesystemHandler {

	public InputStream createInput(String fileName) {
		try {
			return new FileInputStream(new File(Constants.SAMPLE_DIRECTORY
					+ fileName));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
	}

}
