package com.defritza.util;

import java.io.Serializable;

/**
 * Location object
 * @author Joseph, Chris
 */
public class Location implements Serializable {
  private static final long serialVersionUID = 1;
  private int i;
  private int j;

  public Location(int i, int j) {
    this.i = i;
    this.j = j;
  }

  public Location(int x, int y, int dimension) {
    this.i = x / (dimension / 8);
    this.j = y / (dimension / 8);
    if (this.j > 7) {
      this.j = 7;
    } else if (this.j < 0) {
      this.j = 0;
    }
    if (this.i > 7) {
      this.i = 7;
    } else if (this.i < 0) {
      this.i = 0;
    }
  }

  public int getI() {
    return this.i;
  }

  public int getJ() {
    return this.j;
  }

  public String toString() {
    return this.i + " " + this.j;
  }

  public boolean equals(Object o) {
    return o instanceof Location && ((Location) o).getI()
        == this.getI() && ((Location) o).getJ() == this.getJ();
  }
}