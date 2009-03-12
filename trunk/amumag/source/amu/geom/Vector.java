/*
 *  This file is part of amumag,
 *  a finite-element micromagnetic simulation program.
 *  Copyright (C) 2006-2008 Arne Vansteenkiste
 * 
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 * 
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details (licence.txt).
 */
package amu.geom;

import amu.core.Equality;
import amu.mag.fmm.IntVector;
import amu.core.Index;
import amu.io.Message;
import java.io.Serializable;
import static java.lang.Math.*;

public final class Vector implements Comparable<Vector>, Serializable {

  public static final int X = 0,  Y = 1,  Z = 2;
  /**
   * Immutable unit vectors in X,Y and Z direction.
   */
  public static final Vector[] UNIT = new Vector[]{
    new Vector(1, 0, 0, true),
    new Vector(0, 1, 0, true),
    new Vector(0, 0, 1, true)
  };
  //__________________________________________________________________________
  public double x,  y,  z;                                                      // not quite immutable yet...
  // 2009-03-02: We are now at a point where no immutability test has failed
  // since a year, let's disable it.
  //private boolean immutable;                                                  // to avoid accidental math operations without copy constructor.

  //__________________________________________________________________________
  public Vector() {
    this(0, 0, 0);
  }

  public Vector(Vector v) {
    this(v.x, v.y, v.z);
  }

  public Vector(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  //immutable = false;
  }

  public Vector(Vector v, boolean immutable) {
    this(v.x, v.y, v.z, immutable);
  }

  public Vector(double x, double y, double z, boolean immutable) {
    this.x = x;
    this.y = y;
    this.z = z;
    //this.immutable = immutable;
  }

  public void makeImmutable() {
    //immutable = true;
  }

  //__________________________________________________________________________
  /**
   * Squared norm.
   */
  public double norm2() {
    return x * x + y * y + z * z;
  }

  /**
   * Norm (length).
   */
  public double norm() {
    return Math.sqrt(x * x + y * y + z * z);
  }

  /**
   * Max Norm: maximum abs of the components.
   */
  public double maxNorm() {
    double norm = abs(x);
    if (abs(y) > norm) {
      norm = abs(y);
    }
    if (abs(z) > norm) {
      norm = abs(z);
    }
    return norm;
  }

  /**
   * Normalizes the vector.
   */
  public void normalize() {
    //divide(norm());
    final double invnorm = 1.0/Math.sqrt(x*x + y*y + z*z);
    x *= invnorm;
    y *= invnorm;
    z *= invnorm;
  }

  /**
   * Normalizes the vector, unless it is zero.
   */
  public void normalizeSafe() {
    double norm = norm();
    if (norm != 0.0) {
      divide(norm);
    }
  }

  public void normalizeVerySafe() {
    double norm = norm();
    if (norm != 0.0) {
      divide(norm);
    } else {
      Message.warning("Zero Ms !");
      set(1, 0, 0);
    }
  }

  //__________________________________________________________________________
  /**
   * Add v to this vector.
   */
  public void add(Vector v) {
    //assert !immutable;
    x += v.x;
    y += v.y;
    z += v.z;
  }

  /**
   * Add x,y,z to this vector.
   */
  public void add(double x, double y, double z) {
    //assert !immutable;
    this.x += x;
    this.y += y;
    this.z += z;
  }

  /**
   * Adds r*v to this vector.
   **/
  public void add(double r, Vector v) {
    //assert !immutable;
    x += r * v.x;
    y += r * v.y;
    z += r * v.z;
  }

  //__________________________________________________________________________
  /**
   * Subtract v from this vector.
   */
  public void subtract(Vector v) {
    //assert !immutable;
    x -= v.x;
    y -= v.y;
    z -= v.z;
  }

  /**
   * Subtract x,y,z form this vector.
   */
  public void subtract(double x, double y, double z) {
    //assert !immutable;
    this.x -= x;
    this.y -= y;
    this.z -= z;
  }

  public Vector minus(Vector v) {
    return new Vector(x - v.x, y - v.y, z - v.z);
  }

  //__________________________________________________________________________
  /**
   * Scalar multiplication.
   */
  public void multiply(double r) {
    //assert !immutable;
    x *= r;
    y *= r;
    z *= r;
  }

  /*public Vector times(double r) {
  Vector v = new Vector(this);
  v.multiply(r);
  return v;
  }*/
  /**
   * Pointwise multiplication.
   */
  public void multiply(Vector v) {
    //assert !immutable;
    x *= v.x;
    y *= v.y;
    z *= v.z;
  }

  /**
   * Dot (inner) product.
   */
  public double dot(Vector other) {
    return this.x * other.x + this.y * other.y + this.z * other.z;
  }

  /**
   * Vector cross product.
   */
  public void cross(Vector other) {
    //assert !immutable;
    double xcross = y * other.z - other.y * z;
    double ycross = -x * other.z + other.x * z;
    double zcross = x * other.y - other.x * y;
    x = xcross;
    y = ycross;
    z = zcross;
  }

  //__________________________________________________________________________
  /**
   * Scalar division by 1/r.
   */
  public void divide(double r) {
    //assert !immutable;
    r = 1.0 / r;
    x *= r;
    y *= r;
    z *= r;
  }

  /**
   * Pointwise division.
   */
  public void divide(int ix, int iy, int iz) {
    //assert !immutable;
    x /= ix;
    y /= iy;
    z /= iz;
  }

  /**
   * Pointwise division.
   */
  public void divide(Index n) {
    //assert !immutable;
    x /= n.x;
    y /= n.y;
    z /= n.z;
  }

  /**
   * Pointwise division.
   */
  public void divide(Vector n) {
    //assert !immutable;
    x /= n.x;
    y /= n.y;
    z /= n.z;
  }

  //__________________________________________________________________________
  /**
   * Checks if at least one of the components is NaN.
   */
  public boolean isNaN() {
    return Double.isNaN(x) || Double.isNaN(y) || Double.isNaN(z);
  }

  //__________________________________________________________________________
  public double pow(IntVector p) {
    return Math.pow(x, p.x) * Math.pow(y, p.y) * Math.pow(z, p.z);
  }

  //__________________________________________________________________________
  public void set(Vector v) {
    //assert !immutable;
    this.x = v.x;
    this.y = v.y;
    this.z = v.z;
  }

  public void set(double x, double y, double z) {
    //assert !immutable;
    this.x = x;
    this.y = y;
    this.z = z;
  }

  //__________________________________________________________________________
  public void setComponent(int component, double value) {
    //assert !immutable;
    switch (component) {
      case 0:
        x = value;
        break;
      case 1:
        y = value;
        break;
      case 2:
        z = value;
        break;
      default:
        throw new IllegalArgumentException();
    }
  }

  public double getComponent(int component) {
    switch (component) {
      case 0:
        return x;
      case 1:
        return y;
      case 2:
        return z;
      default:
        throw new IllegalArgumentException();
    }
  }

  //__________________________________________________________________________
  /**
   * Tests for equality up to float accuracy.
   **/
  @Override
  public boolean equals(Object other) {
    assert other instanceof Vector;
    Vector v = (Vector) other;
    //2007-dec-06: changed equality, removed (float) cast.
    return Equality.equals(v.x, x) &&
            Equality.equals(v.y, y) &&
            Equality.equals(v.z, z);
  }

  //__________________________________________________________________________
  /**
   * This may not be the most optimal hashcode, but this does not really matter: it's used
   * only once during initialization. Coordinates are cast to float to prevent that round-off
   * errors cause almost equal Vectors to become non-equal.
   */
  @Override
  public int hashCode() {
    return Equality.hashCode(x) + 97 * Equality.hashCode(y) + 97 * 97 * Equality.hashCode(z);
  }

  //__________________________________________________________________________
  @Override
  public String toString() {
    return x + " " + y + " " + z;
  }

  public boolean sizeLargerThan(Vector other) {
    return x > other.x || y > other.y || z > other.z;
  }

  /*public boolean isImmutable() {
    return immutable;
  }*/

  public void reset() {
    //assert !immutable;
    set(0, 0, 0);
  }

  /**
   * Artificial comparison, allows arrays of vectors (e.g., the micromagnetic
   * kernel) to be sorted so that much more of those arrays will be equal and
   * can be pooled.
   * @param other
   * @return
   */
  public int compareTo(Vector other) {
    if (this.x > other.x) {
      return 1;
    } else if (this.x < other.x) {
      return -1;
    } else if (this.y > other.y) {
      return 1;
    } else if (this.y < other.y) {
      return -1;
    } else if (this.z > other.z) {
      return 1;
    } else if (this.z < other.z) {
      return -1;
    } else {
      return 0;
    }
  }
}
