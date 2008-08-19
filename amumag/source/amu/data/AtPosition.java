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

package amu.data;

import amu.geom.Vector;
import amu.core.Index;
import java.io.IOException;
import static amu.data.Names.*;

/**
 * Removes the dependence on the position of simulation data by probing
 * at a given position.
 */
public class AtPosition extends SpaceProbe {

    private Index r;

    public AtPosition(DataModel model, Index r) {
        super(model);
        this.r = r;
    }

    public AtPosition(DataModel m, int x, int y, int z) {
        this(m, new Index(x, y, z));
    }

    @Override
    public void put(int time, Vector v) throws IOException {
        originalModel.put(time, r, v);
    }

    @Override
    public String getName() {
        return originalModel.getName() + OPERATOR + "atPosition" +
                r.x + DELIMITER + r.y + DELIMITER + r.z;
    }
}
