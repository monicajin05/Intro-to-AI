package edu.ncsu.csc411.ps01.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Reads the files from map/ and converts them into a nested array
 * structure that can be processed by the environment.
 * DO NOT MODIFY.
 */
public class MapManager {
  /**
   * Reads in a text file and loads each line as an ArrayList.
   * Once finished, converts the ArrayList into a String array.

   * @param filename - a text file from map/
   * @return an array of the values from the filename
   */
  public static String[] loadMap(String filename) {
    ArrayList<String> map = new ArrayList<String>();
    try {
      File fi = new File(filename);
      Scanner fiReader = new Scanner(fi);
      while (fiReader.hasNextLine()) {
        String line = fiReader.nextLine();
        map.add(line);
      }
      fiReader.close();
    } catch (FileNotFoundException fnf) {
      String errorMsg = "%s is not a valid filename\n";
      System.out.printf(errorMsg, filename);
      fnf.printStackTrace();
    }

    return map.toArray(new String[0]);
  }

}
