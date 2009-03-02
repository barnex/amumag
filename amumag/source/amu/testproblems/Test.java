package amu.testproblems;

import amu.geom.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.time.*;
import amu.mag.field.*;
import amu.data.*;
import amu.geom.jelly.*;
import amu.geom.solid.*;
import amu.mag.adapt.*;
import x.*;

public class Test extends Problem {

  public void init() {
    setOutputDir("/home/arne/Desktop/test2.amu");
    setMs(860E3);
    setA(13E-12);
    setAlpha(0.01);
    setBoxSizeX(500E-9);
    setBoxSizeY(500E-9);
    setBoxSizeZ(50E-9);
    setMaxCellSizeX(4E-9);
    setMaxCellSizeY(4E-9);
    setMaxCellSizeZ(10000000);
    setFmmOrder(2);
    setKernelIntegrationAccuracy(2);
    setMagnetization(new Vortex(1));
    setSolver(new RKF54(1E-5));

    setAdaptivity(new TestAdaptiveMesh2(5.0, 8));
  }

  //@Override
  public void run() throws Exception {

    //setExternalField(new StaticField(100E-3, 0, 0));
   
    save(new SpaceAverage(new Adaptivity(sim)), 10);
    //save(new Adaptivity(sim), 1);
    save("m", 10);
    /*save("maxTorque", 1);
    save("dt", 1);
    save("badSteps", 1);
    save("lastError", 1);
    save(new SpaceAverage(getData("m")), 1);
    save(new SpaceAverage(getData("hExt")), 1);
    save(new SpaceAverage(getData("energyDensity")), 10);*/
    setAlpha(10);


    //DR2DRenderer renderer = new DR2DRender(getData("m"));

    runTime(1E-9);
    setAdaptivity(new TestAdaptiveMesh2(5.0, 8));
    runTime(1E-9);
    setAdaptivity(new TestAdaptiveMesh2(2.0, 8));
    runTime(1E-9);

    save(getData("m"), "relaxed");
    setExternalField(new StaticField(0, 0, 0));
    setAlpha(0.01);
    runTime(1E-10);
  }
}
