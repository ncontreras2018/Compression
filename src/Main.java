import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;

import javax.swing.JFileChooser;

/**
 * @author Nicholas
 * @Taco Jeremy
 * 
 */

public class Main {

	public static void main(String[] args) {
		new Main();
	}

	public Main() {

		JFileChooser fileChooser = new JFileChooser();

		fileChooser.showOpenDialog(null);

		File targetFile = fileChooser.getSelectedFile();

		try {

			byte[] bytes = Files.readAllBytes(targetFile.toPath());

			FileOutputStream stream = null;

			if (targetFile.getName().endsWith(".compressedFile")) {
				bytes = decompressFile(bytes);
				stream = new FileOutputStream("C:/Users/Nicholas/Desktop/DECOMP-"
						+ targetFile.getName().substring(0, targetFile.getName().indexOf(".compressedFile")));
			} else {
				bytes = compressFile(bytes);
				stream = new FileOutputStream("C:/Users/Nicholas/Desktop/" + targetFile.getName() + ".compressedFile");
			}
			try {
				stream.write(bytes);
			} finally {
				stream.close();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private HashMap<String, Integer> patterns;
	private byte curCompressId = 93;

	private byte[] compressFile(byte[] bytes) {

		String stringForm = "";

		for (byte cur : bytes) { // Go through each char

			char toAdd = (char) cur;

			stringForm += toAdd; // Add it to the string

			if (toAdd == '\\') { // Escape the replacement key
				stringForm += '\\';
			}

			if (toAdd == (char) Byte.MAX_VALUE) { // Escape the "END KEYS" key
				stringForm += (char) Byte.MAX_VALUE;
			}
		}

		stringForm = ((char) Byte.MAX_VALUE) + stringForm; // Add the "END KEYS"
															// key to the start
															// of the file

		while (curCompressId < Byte.MAX_VALUE) { // While we still available
													// compression keys

			patterns = new HashMap<String, Integer>();

			for (int i = 4; i <= stringForm.length(); i++) { // record every
																// pattern in
																// the file
				String curPattern = stringForm.substring(i - 4, i);

				Integer oldValue = patterns.get(curPattern);

				if (oldValue == null) {
					patterns.put(curPattern, 1);
				} else {
					patterns.put(curPattern, oldValue + 1);
				}
			}

			Entry<String, Integer> largest = null;

			for (Entry<String, Integer> curPair : patterns.entrySet()) { // Find
																			// the
																			// most
																			// common
																			// pattern

				if (curPair.getValue() > 1 && (largest == null || curPair.getValue() > largest.getValue())) {
					largest = curPair;
				}
			}

			if (largest == null) { // if there is no pattern more with more than
									// 1 use, we're done
				return stringForm.getBytes();
			}

			String targetString = largest.getKey();
			String compressKey = "\\" + (char) curCompressId;

			stringForm = stringForm.replace(targetString, compressKey); // replace
																		// those
																		// patterns
																		// with
																		// our
																		// keys

			stringForm = compressKey + targetString + stringForm; // register
																	// the key
																	// at the
																	// front of
																	// the file

			curCompressId++; // next key

			System.out.println(
					"Compressed: " + targetString + " " + largest.getValue() + " times with key " + compressKey);
		}

		return stringForm.getBytes();
	}

	private byte[] decompressFile(byte[] bytes) {

		String stringForm = "";

		for (byte cur : bytes) {
			stringForm += (char) cur;
		}


		while (stringForm.charAt(0) != (char) Byte.MAX_VALUE) {

			String key = stringForm.substring(0, 2);
			String value = stringForm.substring(2, 6);
			
			stringForm = stringForm.substring(6);
			
			stringForm = stringForm.replace(key, value);
			
			System.out.println("Decompressed " + key);
		}
		return stringForm.substring(stringForm.indexOf((char) Byte.MAX_VALUE) + 1).getBytes();
	}
}
