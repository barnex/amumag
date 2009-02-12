package amu.testproblems;

import amu.geom.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.time.*;
import amu.mag.field.*;
import amu.data.*;

public class Test extends Problem {

  public void init() {
    setOutputDir("/home/arne/Desktop/test.amu");
    setMs(860E3);
    setA(13E-12);
    setAlpha(0.01);
    setBoxSizeX(300E-9);
    setBoxSizeY(300E-9);
    setBoxSizeZ(30E-9);
    setMaxCellSizeX(8E-9);
    setMaxCellSizeY(8E-9);
    setMaxCellSizeZ(1.0 / 0.0);
    setFmmOrder(2);
    setFmmAlpha(0.6);
    setKernelIntegrationAccuracy(4);
    setMagnetization(new Vortex(1));
    //setMagnetization(new Saved("relaxed"));
    setSolver(new RKF54(1E-6));
    setDipoleCutoff(0.02);
    //setSolver(new AmuSolver5(0.02, 2, 2));
  }

  //@Override
  public void run() throws Exception {

    setExternalField(new StaticField(10E-3, 0, 0));

//    save("m", 10);
//    save("maxTorque", 1);
//    save("dt", 1);
//    save("badSteps", 1);
//    save("lastError", 1);
//    save(new SpaceAverage(getData("m")), 1);
//    save(new SpaceAverage(getData("hExt")), 1);
//    save(new SpaceAverage(getData("energyDensity")), 10);
    //setAlpha(10);
    //runTime(5E-9);

    //save(getData("m"), "relaxed");
    setExternalField(new StaticField(0, 0, 0));
    setAlpha(0.01);
    runTime(1E-10);
  }
}
