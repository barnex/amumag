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
package amu.mag;

import amu.core.Equality;
import amu.core.Pool;
import amu.debug.Bug;
import amu.mag.adapt.AdaptiveMeshRules;
import amu.mag.fmm.FMM;
import amu.mag.fmm.IntVector;
import amu.mag.fmm.Multipole;
import amu.mag.fmm.SmoothField;
import amu.geom.Vector;
import amu.mag.exchange.Exchange6Ngbr;
import static amu.geom.Vector.X;
import static amu.geom.Vector.Y;
import static amu.geom.Vector.Z;
import amu.mag.kernel.Kernel;
import amu.mag.time.AmuSolver;
import java.io.Serializable;
import static java.lang.Math.sqrt;

/**
 * A magnetization cell in the mesh. Cells have 2 or 0 children.
 */
public final class Cell implements Serializable {

  public Vector center;
  public Vector[] vertex;
  public Vector size;                                               // currently a buffer, to be removed.

  public transient Cell child1,  child2;                                       // the child cells.
  public transient Cell parent;                                               // the parent cell of the cell.
  public transient Cell next;                                                 // linked list of cells. make final?

  public Face[] faces;
  public Vector[] normal;							// normal vectors of the corresponding faces, pointing outward

  public transient Multipole multipole;
  public transient Multipole[] unitQ;						// for each face: the multipole around the fmmCenter for a unit charge on the face.
  public transient SmoothField smooth;
  public transient Cell[] nearCells;						// nearCells is transformed to nearFaces, is set to null after init()
  public transient Kernel kernel;

  public transient final Vector m = new Vector();                             // (reduced) magnetization
  public transient final Vector h = new Vector();
  //public transient final Vector torque = new Vector();
 
  //public transient Vector debug = new Vector();
  // partial fields
  public transient final Vector hDemag = new Vector();
  public transient final Vector hKernel = new Vector();
  public transient final Vector hEx = new Vector();
  //public transient final Vector hExt = new Vector();
  //public transient final Vector hSmooth = new Vector();
  // general purpuose storage for the sake of visualizers.
  public transient Vector dataBuffer = null;
  public transient Exchange6Ngbr exchange;
  //public transient AmuSolver solver;
  //private transient final Vector hExPrevious = new Vector(),  hMagPrevious = new Vector();

  // adaptive mesh: indicates this cell is at this moment treated as a leaf,
  // because higher resolution is not necessary for the moment (or the cell
  // actually is a real leaf, of course).
  public transient boolean updateLeaf;
  public transient boolean uniform;


  // does some cell need my Q? If not, I won't calculate it.

  public transient boolean qNeeded;   //needed?
  public transient boolean qFromFaces;//if needed, take from faces or children?

  //public transient boolean chargeFree;

  // indicates when the adaptive mesh recusion can stop (stop=true: this is the last cell)
  // also temporarily (ab)used as "unlink tag" to indicate which cells have to be
  // removed by the CSGModule.
  public transient boolean unlinkTag;

  static boolean precess;
  public transient static double alphaLLG = Double.NaN;

  

  //__________________________________________________________________________constructors
  /**
   * Only cells on the base level get vertices by construction.
   */
  public Cell(Cell parent, Vector position, Vector size, Pool<Vector> vertexPool, boolean createVertices) {
    this.center = position;
    //assert position.isImmutable();

    this.size = size;

    if (createVertices) {
      createVertices(vertexPool);
    }

    this.parent = parent;
    smooth = new SmoothField();
    multipole = new Multipole();
  }

  //__________________________________________________________________________init
  // does not really belong here.
  private void createVertices(Pool<Vector> vertexPool) {
    vertex = new Vector[8];
    for (int i = 0; i <= 1; i++) {
      for (int j = 0; j <= 1; j++) {
        for (int k = 0; k <= 1; k++) {
          Vector v = new Vector(i - 0.5, j - 0.5, k - 0.5);	//position (to cell center) in relative units (-0.5 .. 0.5);
          v.multiply(size);				//position (to cell center) in absolute units
          v.add(center);					//position (absolute).
          v = vertexPool.intern(v);			//internalize
          setVertex(i, j, k, v);
        }
      }
    }
  }

  /**
   * Initiates everything that depends on FMM (smoothField, q, qIndex).
   */
  public void initField() {
    smooth.init();
    multipole.init(this);
  }

  //__________________________________________________________________________FMM

  public void updateHParallel(final int level) {

    smooth.update(parent);

    if (!updateLeaf) { //adaptive mesh //child1 != null
      if (level > 0) { // fork for parallel processing

        try {
          Thread fork = new Thread() {

            @Override
            public void run() {
              child1.updateHParallel(level - 1);
            }
          };

          fork.start();
          child2.updateHParallel(level - 1);
          fork.join();
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      } else { // use this thread

        child1.updateHParallel(level - 1);
        child2.updateHParallel(level - 1);
      }
    } else { //updateLeaf cell: update kernel, exch and torque.

      // hack
      //hExPrevious.set(hEx);
      //hMagPrevious.set(hDemag);
      //hMagPrevious.add(hExt);

      kernel.update(hKernel);
      exchange.update();

      //hSmooth.x = -smooth.field[1];
      //hSmooth.y = -smooth.field[2];
      //hSmooth.z = -smooth.field[3];

      // hSmooth = -gradient(potential_Smooth)
      // h = hKernel + hSmooth
      hDemag.x = hKernel.x -smooth.field[1];
      hDemag.y = hKernel.y -smooth.field[2];
      hDemag.z = hKernel.z -smooth.field[3];

      //hDemag.add(hKernel);

      h.set(hDemag);
      h.add(hEx);
      //h.add(hExt);
    }
  }


  /**
   * recursively sets m of all the children to a value;
   * @param m
   */
  private void setMChildren(Vector m){
    if(child1 != null){
      child1.m.x = m.x;
      child1.m.y = m.y;
      child1.m.z = m.z;

      child2.m.x = m.x;
      child2.m.y = m.y;
      child2.m.z = m.z;

      child1.setMChildren(m);
      child2.setMChildren(m);
    }
  }

  /**
   * recursively gets the magnetization from the children and sets the cells
   * own magnetization to the average. recursion stops at updateLeafs, who will
   * call setMChildren.
   */
  public void distributeMOverLevels(){
    if(updateLeaf){
      // my own magnetization is crucial, don't touch it
      // but distribute it to my children
      setMChildren(m);
    }
    else{
      // not update leaf
      // update children first
      child1.distributeMOverLevels();
      child2.distributeMOverLevels();
      // then set myself to the average
      m.x = 0.5 * (child1.m.x + child2.m.x);
      m.y = 0.5 * (child1.m.y + child2.m.y);
      m.z = 0.5 * (child1.m.z + child2.m.z);
      m.normalizeVerySafe();
    }
  }

  /**
   * Called once after initializing the magnetization configuration of the base
   * level to set all other levels.
   */
  public void distributeBaseM(){
    if(child1 != null){
      child1.distributeBaseM();
      child2.distributeBaseM();
      // then set myself to the average
      m.x = 0.5 * (child1.m.x + child2.m.x);
      m.y = 0.5 * (child1.m.y + child2.m.y);
      m.z = 0.5 * (child1.m.z + child2.m.z);
      m.normalizeVerySafe();
    }
  }
  /**
   * Adds (!) a charge to the faces, based on the magnetization.
   */
  public void chargeFaces() {
    for (int i = 0; i < faces.length; i++) {
      Face face = faces[i];
      if (face.scalarArea != 0.0) {

        if (face.sideness == 0) { // inner face should receive 2 charges
          if (face.adhocChargeCounter != 2) {
            face.adhocChargeCounter++;
            faces[i].charge += normal[i].x * m.x + normal[i].y * m.y + normal[i].z * m.z;
          }
        } 
        else {//face.sideness != 0 // outer face should recive 1 charge
          if (face.adhocChargeCounter != 1) {
            face.adhocChargeCounter++;
            faces[i].charge += normal[i].x * m.x + normal[i].y * m.y + normal[i].z * m.z;
          }
        }

      }
    }
  }

  //__________________________________________________________________________
  /**
   * Updates the cell's multipole expansion. If the cell is al leaf, this is done based on the
   * faces, else the children's multipoles are combined. Recursively updates the
   * children first.
   *
   */
  public final void updateQ(final int level) {

    // (0) reset q

    final double[] q = multipole.q;
    //multipole.reset(); //inlined:
    for(int i = 0; i < q.length; i++)                                       //set q to zero -> system.arraycopy?
	    q[i] = 0.0;


    // (1) ask to update chidren no matter what.
    // They may decide not to actually update themselves if they are not needed.
  
    if(child1 != null){

      if (level > 0) { // fork for parallel processing
        try {
          Thread fork = new Thread() {
            @Override
            public void run() {
              child1.updateQ(level - 1);
            }
          };
          fork.start();
          child2.updateQ(level - 1);
          fork.join();
        } catch (InterruptedException ex) {
          ex.printStackTrace();
        }
      } else { // use this thread
        child1.updateQ(level - 1);
        child2.updateQ(level - 1);
      }
    }

    // (2) now update myself, if needed
    if (qNeeded) {

      // (2.1) update Q from charges on the faces (if I am some small cell, perhaps)
      if (qFromFaces) {
        // update q based on charge on faces.
        for (int c = 0; c < faces.length; c++) {
          final Face face = faces[c];
          final double[] unitQc_q = unitQ[c].q;

          //multipole.add(face.charge, unitQ[c]); //inlined
          for (int i = 0; i < q.length; i++) {
            q[i] += face.charge * unitQc_q[i];
          }
        }
      }
      //(2.2) update Q form children (if I am some larger cell, perhaps)
      else {
        double[] childQ = child1.multipole.q;

        //if (!child1.chargeFree) {
        for (int in = 0; in < multipole.q.length; in++) {
          final int[] qIndexIn = child1.multipole.qIndex[in];
          final int[] smoothFieldMonoDiffIn = FMM.monoDiff[in];
          for (int i = 0; i < qIndexIn.length; i++) {
            final int ip = qIndexIn[i];
            multipole.q[in] += childQ[smoothFieldMonoDiffIn[ip]] * child1.smooth.shiftFactor[ip];
            assert child1.smooth.shiftFactor[ip] != 0.0;
          }
        }
        //}

        //if (!child2.chargeFree) {
        childQ = child2.multipole.q;
        for (int in = 0; in < multipole.q.length; in++) {
          final int[] qIndexIn = child2.multipole.qIndex[in];
          final int[] smoothFieldMonoDiffIn = FMM.monoDiff[in];
          for (int i = 0; i < qIndexIn.length; i++) {
            final int ip = qIndexIn[i];
            multipole.q[in] += childQ[smoothFieldMonoDiffIn[ip]] * child2.smooth.shiftFactor[ip];
            assert child2.smooth.shiftFactor[ip] != 0.0;
          }
        }
      }//end if not update from faces

    }// end if qNeeded
    else{
      // Q not needed: NaN it out for debug
      for (int i = 0; i < q.length; i++) {
            q[i] = Double.NaN;
          }
    }
    // adaptive mesh: update even the smallest cells (for now), their Q may be needed by others...
    //if (child1 == null) {
//    if(updateLeaf || child1 == null){
//      multipole.reset(); // overwrite recursive Q by unitQ: TODO: no childQ in this case!!
//
//    }



    /*if (Simulation.dipoleCutoff != 0.0) {
      if (child1 == null) {//leaf
        chargeFree = true;
        for (int i = 0; i < 4; i++) {
          if (Math.abs(multipole.q[i]) > Simulation.dipoleCutoff) {
            chargeFree = false;
            break;
          }
        }
      }
      else{ //no leaf
        chargeFree = child1.chargeFree && child2.chargeFree;
      }
    }*/

  }

  /**
   * The recursive updateQ() goes down to the UpdateLeafs, who use their
   * unitQ to end the recursion. However, some of the UpdateLeaf's children
   * might also need their Q, so alculate those from their unitQ's. If the update-
   * leaf can use unitQ, then it's definitely safe for its children to do so.
   */
//  private void updateQSubLeafIfNeeded() {
//    final double[] q = multipole.q;
//
//    if (qNeeded) {
//      // update q based on charge on faces.
//      for (int c = 0; c < faces.length; c++) {
//        final Face face = faces[c];
//        final double[] unitQc_q = unitQ[c].q;
//
//        //multipole.add(face.charge, unitQ[c]); //inlined
//        for (int i = 0; i < q.length; i++) {
//          q[i] += face.charge * unitQc_q[i];
//        }
//      }
//    } else {
//      throw new Bug();
//      // if not needed, we NaN it out for security.
//      //multipole.q[0] = Double.NaN;
//    }
//
//    if (child1 != null) {
//      child1.updateQSubLeafIfNeeded();
//      child2.updateQSubLeafIfNeeded();
//    }
//
//  }


  //debug: 
  // recursively sets Q to NaN. To be applied to cells whose Q are supposed
  // not to be used.
  private void invalidateQ(){
    multipole.q[0] = Double.NaN;
    if(child1 != null){
      child1.invalidateQ();
      child2.invalidateQ();
    }  
  }

//  /**
//   * Recursively updates the uniform flag based on the adaptive mesh rules.
//   * @param aThis
//   */
//  public void updateUniform(AdaptiveMeshRules aMRules) {
//    uniform = aMRules.isUniform(this);
//    if (uniform) {
//      setChildrenUniform();
//    } else if (child1 != null) {
//      child1.updateUniform(aMRules);
//      child2.updateUniform(aMRules);
//    }
//  }
//
//  /**
//   * sets the uniform flag of this cell and all its children true.
//   */
//  private void setChildrenUniform() {
//    uniform = true;
//    if (child1 != null) {
//      child1.setChildrenUniform();
//      child2.setChildrenUniform();
//    }
//  }
  //__________________________________________________________________________
  //__________________________________________________________________________accessors

  public void setShiftFactors(double[] shiftFactor, int[] shiftIndex) {
    this.smooth.shiftFactor = shiftFactor;
    this.smooth.shiftIndex = shiftIndex;
  }

  //__________________________________________________________________________
  /*private static final Vector
  pos = new Vector(),  						// static buffers
  fshift = new Vector();
  private static Vector n;
  private static boolean lock = false;					// assures no concurrent static buffer modification
   */
  //__________________________________________________________________________
  public Face getFace(int dir, int side) {
    if (dir > Z || dir < X || side > 1 || side < 0) {
      throw new IllegalArgumentException();
    } else {
      return faces[2 * dir + side];
    }
  }

  public Vector getNormal(int dir, int side) {
    if (dir > Z || dir < X || side > 1 || side < 0) {
      throw new IllegalArgumentException();
    } else {
      return normal[2 * dir + side];
    }
  }

  public Vector getVertex(int i, int j, int k) {
    return vertex[i * 4 + j * 2 + k];
  }

  public Vector getVertex(IntVector index) {
    return getVertex(index.x, index.y, index.z);
  }

  public void setVertex(int i, int j, int k, Vector v) {
    vertex[i * 4 + j * 2 + k] = v;
  }

  public void setVertex(IntVector index, Vector v) {
    setVertex(index.x, index.y, index.z, v);
  }

  //__________________________________________________________________________
  public double[] getShiftFactor() {
    return smooth.shiftFactor;
  }

  //__________________________________________________________________________
  /**
   * TODO: fast methods should either not call this, or this should be made
   * efficient.
   */
  public Vector getShift() {
    if (parent == null) {
      return new Vector(0, 0, 0);
    } else {
      Vector shift = new Vector(center);		//2007-08-29: fmmPosition -> barycenter !?
      shift.subtract(parent.center);
      return shift;
    }
  }

  //__________________________________________________________________________
  public boolean isLeaf() {
    return child1 == null;	    // then child2 should also be null.
  }

  //__________________________________________________________________________
  public double[] getSmoothField() {
    return smooth.field;
  }

  //__________________________________________________________________________
  /*public Vector getH(){
  return h;
  }*/
  //__________________________________________________________________________
  public void setSmoothField(double[] smoothField) {
    this.smooth.field = smoothField;
  }

  //__________________________________________________________________________
  public Cell[] getNearCells() {
    return nearCells;
  }

  //__________________________________________________________________________
  public void setNearCells(Cell[] near) {
    this.nearCells = near;
  }

  //__________________________________________________________________________
  public Cell[] getPartners() {
    return smooth.partners;
  }

  //__________________________________________________________________________
  public void setPartners(Cell[] partners) {
    smooth.partners = partners;
  }

  //__________________________________________________________________________
  public void setPartnerShifts(int[] partnerShifts) {
    smooth.partnerShifts = partnerShifts;
  }

  //__________________________________________________________________________
  public boolean hasChildren() {
    return child1 != null; //then child2 is also null.
  }

  public void setChildren(Cell c1, Cell c2) {
    this.child1 = c1;
    this.child2 = c2;
  }

  public void setChild(int number, Cell child) {
    switch (number) {
      case 0:
        child1 = child;
        break;
      case 1:
        child2 = child;
        break;
      default:
        throw new Bug();
    }
  }

  /**
   * Swaps a child for a new one.
   */
  public void swapChild(Cell oldChild, Cell newChild) {
    if (child1 == oldChild) {
      child1 = newChild;
    } else if (child2 == oldChild) {
      child2 = newChild;
    } else {
      throw new Bug();
    }
  }

  /**
   * Removes the child.
   */
  public void unlinkChild(Cell child) {
    swapChild(child, null);
  }

  /**
   * Number of children.
   */
  public int childCount() {
    int count = 0;
    if (child1 != null) {
      count++;
    }
    if (child2 != null) {
      count++;
    }
    return count;
  }

  //__________________________________________________________________________
  public Cell getChild(int index) {
    switch (index) {
      case 0:
        return child1;
      case 1:
        return child2;
      default:
        throw new Bug();
    }
  }

  //__________________________________________________________________________
  public Cell[] getChildren() {						// todo: remove this method.
    if (isLeaf()) {
      return new Cell[]{};
    } else {
      return new Cell[]{child1, child2};					// very inefficient
    }
  }

  //__________________________________________________________________________
  public double[] getQ() {
    return multipole.q;
  }

  //__________________________________________________________________________
  public Vector getSize() {
    return size;
  }

  //__________________________________________________________________________
  /**
   * Put position in the vector.
   * TODO: remove;
   */
  public void putPosition(Vector v) {
    v.set(center);
  }

  //__________________________________________________________________________
  @Override
  public boolean equals(Object obj) {
    assert obj instanceof Cell;
    Cell other = (Cell) obj;
    for (int v = 0; v < vertex.length; v++) {
      for (int c = 0; c <= Vector.Z; c++) {
        double a = this.vertex[v].getComponent(c) - this.center.getComponent(c);
        double b = other.vertex[v].getComponent(c) - other.center.getComponent(c);
        if (!Equality.equals(a, b)) {
          return false;
        }
      }
    }

    for (int f = 0; f < faces.length; f++) {
      if (this.faces[f].sideness != other.faces[f].sideness) {
        return false;
      }
    }
    return true;
  }

  @Override
  public int hashCode() {
    int hash = 3;
    for (int v = 0; v < vertex.length; v++) {
      for (int c = 0; c <= Vector.Z; c++) {
        double a = this.vertex[v].getComponent(c) - this.center.getComponent(c);
        hash += Equality.hashCode(Equality.round(a));
        hash *= 17;
      }
    }
    for (int f = 0; f < faces.length; f++) {
      hash += faces[f].sideness;
      hash *= 3;
    }
    return hash;
  }

  @Override
  public String toString() {
    return "Cell" + System.identityHashCode(this);
  }

  //____________________________________________________________output "beans"
  public Vector get_magnetization() {
    return m;
  }

  public Vector get_hExchange() {
    return hEx;
  }

  public Vector get_hDemag() {
    return hDemag;
  }

  public Vector get_hKernel() {
    return hKernel;
  }

  public Vector get_hSmooth() {
    return new Vector(-smooth.field[1], -smooth.field[2], -smooth.field[3]);
  }

  public Vector get_h() {
    return h;
  }

  /*public Vector get_torque() {
    return torque;
  }*/

  public double get_charge() {
    return multipole.q[0];
  }
  private static final Vector dipole_buffer = new Vector();

  public Vector get_dipole() {
    dipole_buffer.x = multipole.q[1];
    dipole_buffer.y = multipole.q[2];
    dipole_buffer.z = multipole.q[3];
    return dipole_buffer;
  }

  public double get_damping() {
    // - m cross H
    double _mxHx = -m.y * h.z + h.y * m.z;
    double _mxHy = m.x * h.z - h.x * m.z;
    double _mxHz = -m.x * h.y + h.x * m.y;

    // - m cross (m cross H)
    double _mxmxHx = m.y * _mxHz - _mxHy * m.z;
    double _mxmxHy = -m.x * _mxHz + _mxHx * m.z;
    double _mxmxHz = m.x * _mxHy - _mxHx * m.y;

    double x = _mxmxHx;
    double y = _mxmxHy;
    double z = _mxmxHz;

    return sqrt(x * x + y * y + z * z) * alphaLLG;
  }

  public double get_energyDensity() {
    return -h.dot(m);
  }

  public double get_exchangeEnergyDensity() {
    return -hEx.dot(m);
  }

  public double get_demagEnergyDensity() {
    return -hDemag.dot(m);
  }

  public double get_dampingPowerDensity() {
    // - m cross H
    double _mxHx = -m.y * h.z + h.y * m.z;
    double _mxHy = m.x * h.z - h.x * m.z;
    double _mxHz = -m.x * h.y + h.x * m.y;

    // - m cross (m cross H)
    double _mxmxHx = m.y * _mxHz - _mxHy * m.z;
    double _mxmxHy = -m.x * _mxHz + _mxHx * m.z;
    double _mxmxHz = m.x * _mxHy - _mxHx * m.y;

    // dm_damping/dt
    double dmx_dt = _mxmxHx * alphaLLG;
    double dmy_dt = _mxmxHy * alphaLLG;
    double dmz_dt = _mxmxHz * alphaLLG;

    return -(dmx_dt * h.x + dmy_dt * h.y + dmz_dt * h.z);
  }

//  public double get_exchangePowerDensity() {
//    double dhx = hEx.x - hExPrevious.x;
//    double dhy = hEx.y - hExPrevious.y;
//    double dhz = hEx.z - hExPrevious.z;
//
//    double dt = Main.sim.solver.prevDt;
//
//    return -(dhx * m.x + dhy * m.y + dhz * m.z) / dt;
//  }

  /*public double get_magneticFieldPowerDensity() {
    double dhx = hDemag.x + hExt.x - hMagPrevious.x;
    double dhy = hDemag.y + hExt.y - hMagPrevious.y;
    double dhz = hDemag.z + hExt.z - hMagPrevious.z;

    double dt = Main.sim.solver.prevDt;

    return -(dhx * m.x + dhy * m.y + dhz * m.z) / dt;
  }*/

  /*public double get_powerDensity() {
    return get_dampingPowerDensity() + get_exchangePowerDensity() + get_magneticFieldPowerDensity();
  }*/

  /*public double get_conservativePowerDensity() {
    return get_exchangePowerDensity() + get_magneticFieldPowerDensity();
  }*/

  /*public double get_chargefree(){
    if(chargeFree)
      return 1.0;
    else
      return 0.0;
  }*/
}

  /**
   * Updates the smooth ("slow") part of H, assuming all other contributions
   * are up-to date. excludes hExt. NO ADAPTIVE MESH HERE
   * @param level
   */
//  public void updateHSmoothParallel(final int level) {
//    smooth.update(parent);
//
//    if (child1 != null) { //adaptive mesh
//      if (level > 0) { // fork for parallel processing
//
//        try {
//          Thread fork = new Thread() {
//
//            @Override
//            public void run() {
//              child1.updateHSmoothParallel(level - 1);
//            }
//          };
//
//          fork.start();
//          child2.updateHSmoothParallel(level - 1);
//          fork.join();
//        } catch (InterruptedException ex) {
//          ex.printStackTrace();
//        }
//      } else { // use this thread
//        child1.updateHSmoothParallel(level - 1);
//        child2.updateHSmoothParallel(level - 1);
//      }
//    } else { //leaf cell
//      // hack
//      //hExPrevious.set(hEx);
//      //hMagPrevious.set(hDemag);
//      //hMagPrevious.add(hExt);
//
//      //hSmooth.x = -smooth.field[1];
//      //hSmooth.y = -smooth.field[2];
//      //hSmooth.z = -smooth.field[3];
//
//      hDemag.x = hKernel.x -smooth.field[1];
//      hDemag.y = hKernel.y -smooth.field[2];
//      hDemag.z = hKernel.z -smooth.field[3];
//      hDemag.add(hKernel);
//
//      h.set(hDemag);
//      h.add(hEx);
//      //h.add(hExt);
//    }
//  }

//  /**
//   * Updates the "fast" contributions to H (kernel and exchange).
//   * Invalidates other fields (hSmooth, hDemag, h, hExt);
//   * @param level
//   */
//  /*public void updateHFastParallel(final int level) {
//
//    if (child1 != null) { //adaptive mesh
//      if (level > 0) { // fork for parallel processing
//
//        try {
//          Thread fork = new Thread() {
//
//            @Override
//            public void run() {
//              child1.updateHFastParallel(level - 1);
//            }
//          };
//
//          fork.start();
//          child2.updateHFastParallel(level - 1);
//          fork.join();
//        } catch (InterruptedException ex) {
//          ex.printStackTrace();
//        }
//      } else { // use this thread
//        child1.updateHFastParallel(level - 1);
//        child2.updateHFastParallel(level - 1);
//      }
//    } else { //leaf cell: update kernel, exch
//            /*kernel.update(hKernel);
//      exchange.update();
//
//      h.set(Double.NaN, 1234, 5678);
//      hDemag.set(Double.NaN, 1234, 5678);
//      hSmooth.set(Double.NaN, 1234, 5678);
//      hExt.set(Double.NaN, 1234, 5678)


      // hack. todo: uncomment when power densities are needed.
      //hExPrevious.set(hEx);
      //hMagPrevious.set(hDemag);
      //hMagPrevious.add(hExt);

//      kernel.update(hKernel);
//      exchange.update();
//
//      //hDemag.set(hSmooth);
//      //hDemag.add(hKernel);
//      hDemag.x =  hKernel.x-smooth.field[1];
//      hDemag.y =  hKernel.y-smooth.field[2];
//      hDemag.z =  hKernel.z-smooth.field[3];
//
//      // debug, todo: comment out.
//      h.set(Double.NaN, 123, 456);
//      //hExt.set(h);
//    }
//  }*/

  /**
   * updates H, assuming hSmooth, hKernel, hExt, hEx are all up to date
   * issued after a root step of the amusolver, which has been updating
   * field contributions asynchronously.
   */
  /*public void resyncH() {

    if (child1 != null) {
      child1.resyncH();
      child2.resyncH();
    }

    // hack todo: uncomment when power densities are needed
    //hExPrevious.set(hEx);
    //hMagPrevious.set(hDemag);
    //hMagPrevious.add(hExt);

    hSmooth.x = -smooth.field[1];
    hSmooth.y = -smooth.field[2];
    hSmooth.z = -smooth.field[3];

    //hDemag.set(hSmooth);
    //hDemag.add(hKernel);
    hDemag.x = hSmooth.x + hKernel.x;
    hDemag.y = hSmooth.y + hKernel.y;
    hDemag.z = hSmooth.z + hKernel.z;

    //h.set(hDemag);
    //h.add(hEx);
    //h.add(hExt);
    h.x = hDemag.x + hEx.x;// + hExt.x;
    h.y = hDemag.y + hEx.y;// + hExt.y;
    h.z = hDemag.z + hEx.z;// + hExt.z;
  }*/

  //__________________________________________________________________________





// Donald Knuth wrote:
// "We should forget about small efficiencies, about 97% of the time. 
// Premature optimization is the root of all evil."
 
