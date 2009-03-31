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
import java.io.IOException;
import x.*;

public final class Test extends Problem {

  public void init() throws IOException {
    setOutputDir("/home/arne/Desktop/test.amu");
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
    setKernelIntegrationAccuracy(1);
    setMagnetization(new Saved("/home/arne/Desktop/relaxed"));
    //setMagnetization(new Vortex(1));
    setSolver(new RK4("dm", 0.02));

    setAdaptivity(new MaxAngle(5.0, 10));
  }

  //@Override
  public void run() throws Exception {

    setAlpha(1);
    runTime(10E-12);
    
    setAlpha(0.01);
    setExternalField(new StaticField(10E-3, 0, 0).multiply(new Ramp(100E-12)));
    
    for(int i=0; i<700; i++){
      snapshot();
      runTime(10E-12);
    } 

    setExternalField(new StaticField(0, 0, 0));

    for(int i=0; i<10000; i++){
      snapshot();
      runTime(10E-12);
    }

//
//    save(new CellCount(sim), 1);
//    save(new QNeededCount(sim), 1);
//    save(new SpaceAverage(new Adaptivity(sim)), 1);
//    //save(new Adaptivity(sim), 1);
//    save("m", 10);
//    //save("maxTorque", 1);
//    save("dt", 1);
//    //save("badSteps", 1);
//    //save("lastError", 1);
//    save("stepTime", 1);
//    //save(new SpaceAverage(getData("m")), 1);
//    //save(new SpaceAverage(getData("hExt")), 1);
//    //save(new SpaceAverage(getData("energyDensity")), 1);//
//    setAlpha(1);
//
//
//    //DR2DRenderer renderer = new DR2DRender(getData("m"));
//
//    runTime(10E-9);
//
//    save(getData("m"), "relaxed");
//    setExternalField(new StaticField(0, 0, 0));
//    setAlpha(0.01);
//    runTime(1E-10);
  }

}
