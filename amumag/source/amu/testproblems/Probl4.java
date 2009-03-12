package amu.testproblems;

import amu.mag.field.StaticField;
import amu.geom.*;
import amu.mag.*;
import amu.mag.config.*;
import amu.mag.time.*;
import amu.io.*;
import amu.geom.solid.*;
import amu.data.*;
import amu.mag.adapt.TestAdaptiveMesh2;
import amu.mag.time.*;
//import static java.lang.Math.*;

public class Probl4 extends Problem {

  public void init() {
    setOutputDir("/home/arne/Desktop/probl4.amu");
    setMs(800E3);
    setA(13E-12);
    setAlpha(0.02);
    setBoxSizeX(500E-9);
    setBoxSizeY(125E-9);
    setBoxSizeZ(3E-9);
    setMaxCellSizeX(4E-9);
    setMaxCellSizeY(4E-9);
    setMaxCellSizeZ(8E-9);
    setFmmOrder(2);
    //setFmmAlpha(0.9);
    setKernelIntegrationAccuracy(4);
    setMagnetization(new Uniform(1, 1, 1));
    setSolver(new RK4("dphi", 0.005));
    setAdaptivity(new TestAdaptiveMesh2(5.0, 4));
  }


  //@Override
  public void run() throws Exception {
    save("stepTime", 1);
    save("realtime", 1);
    save(new CellCount(sim), 1);
    save(new QNeededCount(sim), 1);
    save(new QFacesCount(sim), 1);
    save(new QChildrenCount(sim), 1);
    
    save("m", 100);
    save("dt", 1);
    //save(new Integral(getData("energyDensity")), 10);
    save(new SpaceAverage(getData("m")), 1);
    //setPrecession(false);
    setAlpha(2);
    runTime(2E-9);

    //setExternalField(new StaticField(-35.5E-3, -6.3E-3, 0)); // type2
    setExternalField(new StaticField(-24.6E-3, 4.3E-3, 0)); // type1

    //setPrecession(true);
    setAlpha(0.02);
    //setSolver(new AmuSolver5(0.05, 2, 2));
    //setDt(1E-5);

    //save(new SpaceAverage(getData("m")), 10);
    runTime(1E-9);
  }
}