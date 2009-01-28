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

import static java.lang.Double.NaN;

public final class RKF54 extends RKF {

  public RKF54(double errorPerStep) {
    super(errorPerStep);
    butcher = new double[][]{
    {   NaN,         NaN,           NaN,           NaN,           NaN},
    {   1.0/4.0,     NaN,           NaN,           NaN,           NaN},
    {   3.0/32.0,    9.0/32.0,      NaN,           NaN,           NaN},
    {1932.0/2197.0, -7200.0/2197.0, 7296.0/2197.0, NaN,           NaN},
    { 439.0/216.0,  -8.0,           3680.0/513.0, -845.0/4104.0,  NaN},
    {  -8.0/27.0,    2.0,          -3544.0/2565.0, 1859.0/4104.0, -11.0/40.0}};

    h = new double[]{NaN, 1.0/4.0, 3.0/8.0, 12.0/13.0, 1.0, 1.0/2.0};

    weight = new double[]{16.0/135.0, 0.0, 6656.0/12825.0, 28561.0/56430.0, -9.0/50.0, 2.0/55.0};
    weightForError = new double[]{25.0/216.0, 0.0, 1408.0/2565.0,  2197.0/4104.0,   -1.0/5.0,  0.0};
  }
}