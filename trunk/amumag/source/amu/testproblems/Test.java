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
    setOutputDir("/home/arne/Desktop/tensoreta.amu");
    setMs(860E3);
    setA(13E-12);
    setAlpha(0.01);
    setBoxSizeX(250E-9);
    setBoxSizeY(250E-9);
    setBoxSizeZ(50E-9);
    setMaxCellSizeX(4E-9);
    setMaxCellSizeY(4E-9);
    setMaxCellSizeZ(50E-9/0.0);
    setFmmOrder(2);
    setKernelIntegrationAccuracy(3);
    setMagnetization(new Landau(1));
    setSolver(new RK4("dt", 0.01));
  }

  //@Override
  public void run() throws Exception {

    save(new SpaceAverage(getData("m")), 1E-12);

//    //relax
//    setAlpha(2);
//    setExternalField(new StaticField(40E-3, 0, 0));
//    runTime(0.2E-9);
//    save(getData("m"), "relaxed");

    //run
    setAlpha(0.00);
    sim.dampingTensorAlpha = 0.01;
    sim.dampingTensorEta = (0.5 * 1E-9*1E-9) / (Unit.LENGTH * Unit.LENGTH);
    System.out.println("Eta = " + sim.dampingTensorEta);
    
    setExternalField(new StaticField(0, 0, 0));
    runTime(5E-9);
  }

}
