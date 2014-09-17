package amu.testproblems;

import amu.mag.field.StaticField;
import amu.geom.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.time.*;
import amu.io.*;
import amu.geom.solid.*;
import amu.data.*;
import amu.mag.adapt.MaxAngle;
import amu.mag.time.*;
//import static java.lang.Math.*;

public class Logo extends Problem {

  public void init() {
    setOutputDir("/home/arne/Desktop/logo.amu");
    setMs(800E3);
    setA(13E-12);
    setAlpha(0.02);
    setBoxSizeX(1800E-9);
    setBoxSizeY(300E-9);
    setBoxSizeZ(20E-9);
    setMaxCellSizeX(8E-9);
    setMaxCellSizeY(8E-9);
    setMaxCellSizeZ(1);
    setFmmOrder(2);
    //setFmmAlpha(0.9);
    setKernelIntegrationAccuracy(1);//6!!!!!!!!!!!!!!!!!!!!!
    setMagnetization(new Uniform(0.2, 0.1, 1.0));
    setSolver(new RK4("dphi", 0.005));
    setAdaptivity(new MaxAngle(0.0, 0));

    double w = 50E-9;
    Shape cyl = new Cylinder(2*w);
    cyl = cyl.subtract(new Cylinder(w));
    

    Shape cap = cyl.subtract(new SemiSpace().rotateZ(Math.PI/2));
    Shape beam = new Cuboid(w/2, 2*w, 1).translate(0, 2*w);

    Shape shoe = cap.join(beam.translate(1.5*w, 0).join(beam.translate(-1.5*w, 0)));
    shoe = shoe.translate(0, -w);

    Shape a = shoe.join(new Cuboid(2*w, 0.5*w, 1).translate(0, 0.4*w));

    Shape m = shoe.translate(1.5*w, 0).join(shoe.translate(-1.5*w, 0));

    Shape u = shoe.rotateZ(Math.PI);

    Shape c = shoe.subtract(new SemiSpace().rotateZ(Math.PI/2));
    Shape o = c.join(c.rotateZ(Math.PI));

    Shape g = o.subtract(new Cuboid(2*w, 0.5*w, 1).translate(w, -0.5*w)).join(new Cuboid(w, 0.5*w, 1).translate(0.5*w, 0.45*w));
    
    Shape amumag = a.translate(-10*w, 0);
    amumag = amumag.join(m.translate(-4*w, 0));
    amumag = amumag.join(u.translate(2*w, 0));
    amumag = amumag.join(m.translate(8*w, 0));
    amumag = amumag.join(a.translate(14*w, 0));
    amumag = amumag.join(g.translate(19*w, 0));
    addShape(amumag.translate(-4.5*w, 0));
  }


  //@Override
  public void run() throws Exception {

    save("m", 100E-12);
    setAlpha(0.5);
    runTime(1000E-9);

  }
}