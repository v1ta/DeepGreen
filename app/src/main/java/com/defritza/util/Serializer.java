package com.defritza.util;

import android.content.Context;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author Joseph, Chris
 */
public class Serializer {
  private static final String errorMessage = "Provided name cannot be null or the empty string";

  // Prevent instantiation
  private Serializer() {
  }

  /**
   * Method writes an object out into serialized file form to the given filename.
   *
   * @param object The object to be serialized.
   * @param name   The name of the file it should be written into.
   * @return Whether or not the operation succeeded.
   */
  public static boolean writeObject(Context fileContext, Serializable object, String name) {
    if (name == null || name.equals("")) throw new IllegalArgumentException(errorMessage);

    ObjectOutputStream oos = null;
    FileOutputStream fos = null;
    try {
      fos = fileContext.openFileOutput(name, Context.MODE_PRIVATE);
      oos = new ObjectOutputStream(fos);
      oos.writeObject(object);
      return true;
    } catch (Exception e) {
      return false;
    } finally {
      close(oos);
    }
  }

  public static boolean writeHistory(Context fileContext, History object) {
    return writeObject(fileContext, object, object.toString());
  }

  /**
   * Method deserializes a History object from disk. If the object in question does not exist, method returns null.
   *
   * @param name The name of the serialized file.
   * @return The object or null.
   */
  public static History readHistory(Context fileContext, String name) {
    if (name == null || name.equals("")) throw new IllegalArgumentException(errorMessage);
    else return (History) readObject(fileContext, name);
  }

  /**
   * Method deserializes all currently stored past games.
   *
   * @return An ArrayList of the History objects.
   */
  public static ArrayList<History> readHistories(Context fileContext) {
    File directory = fileContext.getFilesDir();
    String[] files = directory.list();
    ArrayList<History> histories = new ArrayList<History>();
    for (String file : files) {
      histories.add((History) readObject(fileContext, file));
    }
    return histories;
  }

  private static Object readObject(Context fileContext, String name) {
    FileInputStream fis = null;
    ObjectInputStream ois = null;
    try {
      fis = fileContext.openFileInput(name);
      ois = new ObjectInputStream(fis);
      return ois.readObject();
    } catch (Exception e) {
      return null;
    } finally {
      close(ois);
      close(fis);
    }
  }

  private static void close(Closeable stream) {
    try {
      stream.close();
    } catch (Exception e) {
      // Oh java...
    }
  }
}