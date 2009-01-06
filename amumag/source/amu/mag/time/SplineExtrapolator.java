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

package amu.mag.time;

import amu.geom.Vector;

/**
 * Extrapolates with piecewise quadratic functions, ensuring C1 continuity.
 * This scheme fails to approximate curves that suddenly become straight, so
 * it turns out to be unsuited for most micromagnetic problems.
 * @author arne
 */
public final class SplineExtrapolator extends Extrapolator{

    // Coefficients of the parabola for the x-component of the data.
    private double Ax, Bx, Cx;
    // backup for replaceLastPoint()
    private double BxPrev, CxPrev;

    private double Ay, By, Cy;
    private double ByPrev, CyPrev;

    private double Az, Bz, Cz;
    private double BzPrev, CzPrev;

    /**
     * Should be initialized
     */
    public SplineExtrapolator(){
        
    }

    /**
     * Initializes to extrapolate 0th order (B=A=0).
     */
    public void init(Vector v){
        Cx = v.x;
        Cy = v.y;
        Cz = v.z;
    }

    @Override
    public void addPoint(double dt, Vector v) {

        double a = dt*dt;
        double b = -dt;
        double c = -2.0*dt;
        double d = 1.0;
        double n = 1.0 / (a*d - b*c);

        {
            BxPrev = Bx;
            CxPrev = Cx;

            double e = CxPrev - v.x;
            double f = BxPrev;

            Ax = (e * d - b * f) * n;
            Bx = (a * f - e * c) * n;
            Cx = v.x;
        }

        {
            ByPrev = By;
            CyPrev = Cy;

            double e = CyPrev - v.y;
            double f = ByPrev;

            Ay = (e * d - b * f) * n;
            By = (a * f - e * c) * n;
            Cy = v.y;
        }

        {
            BzPrev = Bz;
            CzPrev = Cz;

            double e = CzPrev - v.z;
            double f = BzPrev;

            Az = (e * d - b * f) * n;
            Bz = (a * f - e * c) * n;
            Cz = v.z;
        }
    }

    @Override
    public void replaceLastPoint(double dt, Vector v) {
        Bx = BxPrev;
        Cx = CxPrev;

        By = ByPrev;
        Cy = CyPrev;

        Bz = BzPrev;
        Cz = CzPrev;

        addPoint(dt, v);
    }

    @Override
    public void extrapolate(double dt, Vector target) {
        double dt2 = dt*dt;
        target.x = Ax * dt2 + Bx * dt + Cx;
        target.y = Ay * dt2 + By * dt + Cy;
        target.z = Az * dt2 + Bz * dt + Cz;
    }

    public void set(SplineExtrapolator o){
        Ax = o.Ax;
        Ay = o.Ay;
        Az = o.Az;

        Bx = o.Bx;
        By = o.By;
        Bz = o.Bz;

        Cx = o.Cx;
        Cy = o.Cy;
        Cz = o.Cz;

        BxPrev = o.BxPrev;
        ByPrev = o.ByPrev;
        BzPrev = o.BzPrev;

        CxPrev = o.CxPrev;
        CyPrev = o.CyPrev;
        CzPrev = o.CzPrev;
    }
}