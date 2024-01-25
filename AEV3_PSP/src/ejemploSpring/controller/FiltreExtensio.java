package ejemploSpring.controller;

import java.io.File;
import java.io.FilenameFilter;

public class FiltreExtensio implements FilenameFilter {
	String extensio;

	FiltreExtensio(String extensio){
 this.extensio = extensio;
 }

	public boolean accept(File dir, String name) {
		return name.endsWith(extensio);
	}
}