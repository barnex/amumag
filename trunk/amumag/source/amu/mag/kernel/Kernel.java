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
package amu.mag.kernel;

import amu.debug.Bug;
import amu.mag.*;
import amu.geom.Vector;
import java.io.Serializable;

public final class Kernel implements Serializable {

  /** faces to be used with a kernel element */
  public final Face[] nearFaces;
  /** kernel elements corresponding to the nearFaces. */
  public final Vector[] kernel;

  public Kernel(Face[] nearFaces, Vector[] kernel) {
    assert nearFaces.length == kernel.length;
    this.nearFaces = nearFaces;
    this.kernel = kernel;
  }

  // calulates the kernel part of the demag field and puts the result in v.
  public final void update(final Vector v) {
    v.x = 0.0;
    v.y = 0.0;
    v.z = 0.0;
    for (int f = 0; f < nearFaces.length; f++) {
      final Face face = nearFaces[f];
      final Vector kern = kernel[f];
      final double charge = face.charge;			// 2007-06-20

      if(face.adhocChargeCounter == -1)
        throw new Bug(); //todo: rm me

      v.x += kern.x * charge;               // todo absorb these - somewhere
      v.y += kern.y * charge;
      v.z += kern.z * charge;
    }
  }
}
