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

import amu.mag.field.ExternalField;
import amu.core.Bucket;
import amu.core.Index;
import amu.io.Message;
import amu.mag.fmm.DerivativeMap;
import amu.mag.fmm.FMM;
import amu.mag.fmm.ShiftMap;
import amu.geom.Mesh;
import amu.geom.Vector;
import amu.mag.config.Configuration;
import amu.mag.exchange.DzyaloshinskyModule;
import amu.mag.exchange.Exchange6Ngbr;
import amu.mag.fmm.*;
import amu.mag.kernel.*;
import amu.mag.time.AdamsEvolver2;
import amu.io.Message;
import amu.io.OutputModule;
import amu.data.DataModel;
import amu.data.LiveMeshDataModel;
import amu.data.LiveTableDataModel;
import amu.mag.field.StaticField;
import java.io.File;
import java.io.IOException;
import static java.lang.Math.sqrt;

/**
 * The Simulation class brings together all the building blocks for the actual 
 * simulation in a transparent way. All settings can be accessed through this
 * class, and will automatically be passed on to the different building blocks
 * of the simulation.
 * 
 */
public final class Simulation {
    
    // Computational mesh.
    public Mesh mesh;
    
    // FMM settings
    //private fmm ? units (material?)
    public int order = 0;
    public double alpha = -1;
    private int kernelIntegrationAccuracy = 0;
    
    // exchange settings
    //private ExchangeModule exch;
    private DzyaloshinskyModule dzyaloshinsky; 
    
    // Evolver settings
    private ExternalField externalField;
    public AdamsEvolver2 evolver;
    public double totalTime;
    public int totalIteration; //refactor: totalSteps
    public boolean precess = true;
    
    // Output settings
    public final OutputModule output;
     
    //__________________________________________________________________________
    
    public Simulation(File baseDir) throws IOException{
       output = new OutputModule(this, baseDir);
    }
    
    //________________________________________________________________________io
    
    /**
     * Looks for a variable with the given field name in Cell and Evolver
     * and returns a MataModel of this variable (if found).
     * @param field The name of the field, eg "m", "dt".
     * @return A DataModel wrapping the field.
     * @throws java.lang.NoSuchFieldException if the field does not exist in
     * either Cell or Evolver.
     */
    public DataModel getModel(String field) throws NoSuchFieldException{
        try {
            // see if the field name is a variable of Cell
            return new LiveMeshDataModel(this, field);
        } catch (NoSuchFieldException ex) {
            // if not, see if it's a variable of Evolver
            return new LiveTableDataModel(this, field);
            // if not, NoSuchFieldException is thrown.
        }
    }
    
    //_______________________________________________________________________run
    
    private boolean initiated = false;

    private static final String UP = Message.ESC + "4F";
    private static final String CLEAR = Message.ESC + "K";
    
    private void printStatus() {
        
        System.out.print("step  : ");
        System.out.print(totalIteration);
        System.out.println(CLEAR);
        
        System.out.print("time  : ");
        System.out.print((float)(totalTime * Unit.TIME));
        System.out.print(" s");
        System.out.println(CLEAR);
        
        System.out.print("torque: ");
        System.out.print((float)(evolver.maxTorque));
        System.out.println(CLEAR);
        
        System.out.print("dt    : ");
        System.out.print((float)(evolver.dt * Unit.TIME));
        System.out.print(" s");
        System.out.println(CLEAR);
        
        System.out.print(UP);
        
    }
    
    private void runStepWithOutput() throws IOException{
            // if this is the very first iteration, make sure the sim is updated.
            if(!initiated){
                update();
                initiated = true;
            }
            //first notify output: saves initial configuration.
            output.notifyStep();
            printStatus();
            evolver.stepImpl();
            //totalIteration++; is done by evolver.
    }
    
    public void runIterations(int iterations) throws IOException{
        Message.title("run: " + iterations + " iterations");
        Message.hrule();
        for(int i=0; i<iterations; i++){
            runStepWithOutput();
        }
    }
    
    /**
     * Runs the simulation for a specified time.
     * @param duration The time in seconds.
     * @throws java.lang.Exception
     */
    public void runTime(double duration) throws IOException{
        Message.title("run: " + duration + " s");
        Message.hrule();
        //duration /= Unit.TIME;
        double startTime = totalTime;
        do{
            runStepWithOutput();
        }
        while(totalTime - startTime < duration);
        // make sure we just go over the specified duration.
        runStepWithOutput();
    }
    
    public void runTorque(double maxtorque) throws IOException{
        Message.title("run: " + maxtorque + " torque");
        Message.hrule();
        if(maxtorque <= 0)
            throw new IllegalArgumentException("maxTorque must be > 0");
       do{
            runStepWithOutput();
        } while(get_maxTorque() > maxtorque);
    }
    
    //_______________________________________________________________________set  
    //_________________________________________________________________set::mesh
    
    public void setMesh(Mesh mesh){
        setMeshNoUpdate(mesh);
        initExchange();
    }
    
    
    private void initExchange(){
        Message.title("exchange");
        Message.startProgress(mesh.baseCells);
        int count = 0;
        
        Index index = new Index();
        //2008-07-14 : adaptive mesh
        for (int l = mesh.coarseLevelIndex; l < mesh.levels.length; l++) {
            Cell[][][] level = mesh.levels[l];
            for (int i = 0; i < level.length; i++) {
                for (int j = 0; j < level[i].length; j++) {
                    for (int k = 0; k < level[i][j].length; k++) {
                        index.set(i, j, k);
                        Cell cell = mesh.getCell(index);
                        if (cell != null) {
                            cell.exchange = new Exchange6Ngbr(mesh, index);
                            count++;
                            Message.progress(count);
                        }
                    }
                }
            }
        }
        Message.stopProgress();
        Message.indent("type: 6-neighbour, linear");
        Exchange6Ngbr.purgePool();
    }
    
    /**
     * Depends on: nothing.
     * Invalidates: exchangeModule, 
     */
    private void setMeshNoUpdate(Mesh mesh){
        if(!mesh.isFinal()){
            mesh.makeFinal();
	    Message.warning("Made mesh final");	
        }
        this.mesh = mesh;
    }
    
    //________________________________________________________________set::alpha
    /**
     * Sets the FMM alpha (proximity) parameter.
     */
    public void setAlpha(double alpha) {
        setAlphaNoUpdate(alpha);
        
        //if order had already been set.
        if(order != 0)
            updateOrderAndAlphaDependend();
        
        if(kernelIntegrationAccuracy != 0)
            setKernelIntegrationAccuracy(kernelIntegrationAccuracy);
    }

    /**
     * Depends on: mesh.
     * Invalidates: shiftMap, derivativeMap, kernelIntegration
     */
    private void setAlphaNoUpdate(double alpha){
        Message.debug("MESH DIM=" + mesh.dimension);
        this.alpha = alpha;
        if(alpha >= 0.0)
            new WireModule(mesh, new OpeningAngleProximity(alpha, mesh.dimension)).wire();
        else
            new WireModule(mesh, new TouchProximity()).wire();
    }
    
    //________________________________________________________________set::order
    
    public void setOrder(int order){
        setOrderNoUpdate(order);  
        updateOrderAndAlphaDependend();
        for(Cell c = mesh.rootCell; c != null; c = c.next)
          c.initField();
        //Has currently no adjustable accuracy.
        new FaceUnitQ(mesh).init();    
    }
    
    /**
     * Depends on: wiring (alpha)
     * Invalidates: Cell.unitQ, ShiftMap, DerivativeMap
     */
    private void setOrderNoUpdate(int order){
        FMM.setOrder(order);  
        this.order = order;
        Message.println("FMM order = " + order);
    }
    
    private void updateOrderAndAlphaDependend(){
        new ShiftMap().hash(mesh.rootCell); //is this order-dependend?
        
        DerivativeMap.hash(mesh.rootCell);
    }
    
    //_______________________________________________________________set::kernel
    
    //must be followed by setOrder???
    /**
     * Depends on: mesh, wiring
     * Invalidates: nothing.
     * @param accuracy
     */
    public void setKernelIntegrationAccuracy(int accuracy){
        this.kernelIntegrationAccuracy = accuracy;
        if(isWired())
            new FaceKernel(mesh, kernelIntegrationAccuracy).init();
        
        //if(order != 0)
            //setOrder(order); //// ?????????????
    }
    //___________________________________________________________________set::dm
    
    /**
     * Depends on: mesh
     * Invalidates: nothing
     */
    public void setDzyaloshinsky(double d) {
        dzyaloshinsky = new DzyaloshinskyModule(this, new Vector(0, 0, d));
    }
    
    //_________________________________________________________________set::misc
    
   
    protected void setAlphaLLG(double alpha){
        evolver = new AdamsEvolver2(this, alpha);
        evolver.init();
    }
    
    protected void setMagnetization(Configuration config){
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next){
            Vector r = cell.center;
            config.putM(r.x, r.y, r.z, cell.m);
            Configuration.normalizeVerySafe(cell.m);
        }
    }
    
    protected void addMagnetization(Configuration config){
        Vector buffer = new Vector();
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next){
            Vector r = cell.center;
            Vector m = cell.m;
            buffer.set(cell.m);
            config.putM(r.x, r.y, r.z, m);
            m.add(buffer);
            Configuration.normalizeVerySafe(m);
        }
    }
    
    //_______________________________________________________________________get
    
    public File getBaseDirectory(){
        return output.getBaseDir();
    }
    
    /**
     * is the mesh wired? 
     * 2008-07-17 alpha smaller than 0 means: touchProximity 
     */
    private boolean isWired() {
        return alpha != 0.0;
    }
 
    //____________________________________________________________________update
    
    private static final Vector ZERO = new Vector(0, 0, 0, true);
    private final Vector rSiUnits = new Vector();
    
    public void update(){
       
        mesh.aMRules.update();
        
        //(1) update magnetic charges and moments
	updateCharges();
        mesh.rootCell.updateQ(Main.LOG_CPUS);
        
        //(2) set external field for the root cell
        /*if (externalField == null) {
            hExt = ZERO;
        } else {
            hExt = externalField.get(totalTime);
        }
        mesh.rootCell.smooth.field[1] = -hExt.x;
        mesh.rootCell.smooth.field[2] = -hExt.y;
        mesh.rootCell.smooth.field[3] = -hExt.z;*/
        
        //(2) set the space-dependend external field.
        for(Cell cell=mesh.coarseRoot; cell != null; cell = cell.next){
            rSiUnits.set(cell.center);
            rSiUnits.multiply(Unit.LENGTH);
            cell.hExt.set(externalField.get(totalTime, rSiUnits));
        }
        
        //(3) update all other fields and torque, added to the already present external field.
        Cell.precess = precess;
	mesh.rootCell.updateHParallel(Main.LOG_CPUS);
    }
    
    /**
     * Currently only updates the base level.
     */
    private void updateCharges(){
	for(Face face = mesh.rootFace; face != null; face = face.next)
	    face.charge = 0.0;
	for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
	    cell.chargeFaces();
    }
    
    
    //____________________________________________________________output "beans"
    
    private static Bucket bucket = new Bucket(0.001, 10000);
    
    public double get_energy(){
        bucket.reset();
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
            bucket.add(cell.get_energyDensity());
        return bucket.getSum();
    }
    
    public double get_exchangeEnergy(){
        bucket.reset();
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
            bucket.add(cell.get_exchangeEnergyDensity());
        return bucket.getSum();
    }
    
    public double get_demagEnergy(){
        bucket.reset();
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
            bucket.add(cell.get_demagEnergyDensity());
        return bucket.getSum();
    }
    
    public double get_damping(){
        bucket.reset();
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
            bucket.add(cell.get_damping());
        return bucket.getSum();
    }
    
    public double get_maxTorque(){
        double max2 = 0.0;
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next)
            if(cell.torque.norm2() > max2)
                max2 = cell.torque.norm2();
        return sqrt(max2);
    }
    
    public double get_mx(){
        double sum = 0.0;
        int count = 0;
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next){
            sum += cell.m.x;
            count++;
        }
        return sum/count;
    }
    
    public double get_my(){
        double sum = 0.0;
        int count = 0;
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next){
            sum += cell.m.y;
            count++;
        }
        return sum/count;
    }
    
    public double get_mz(){
        double sum = 0.0;
        int count = 0;
        for(Cell cell = mesh.baseRoot; cell != null; cell = cell.next){
            sum += cell.m.z;
            count++;
        }
        return sum/count;
    }

    public void setExternalField(ExternalField externalField) {
        this.externalField = externalField;
        // set the time zero in seconds.
        externalField.timeZero = totalTime * Unit.TIME;
    }
}
