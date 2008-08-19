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
import amu.debug.Bug;
import amu.debug.InvalidProblemDescription;
import amu.geom.Mesh;
import amu.geom.jelly.Homeomorphism;
import amu.mag.config.Configuration;
import amu.io.PerIterations;
import amu.io.PerTime;
import amu.geom.solid.Shape;
import amu.mag.adapt.AdaptiveMeshRules;
import amu.mag.adapt.FixedMesh;
import amu.io.Message;
import amu.data.DataModel;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import static java.lang.Double.POSITIVE_INFINITY;

public abstract class Problem {
    
    // has init already been called?
    private boolean initiated = false;
    
    /**
     * Easy way for the user to set all parameters necessary to initialize the
     * simulation. The parameters are at this point only stored in the memory and
     * can thus be set in any order. After this method, initImpl() will automatically
     * be called, which does the actual initialisation based on the stored
     * parameters.
     */
    public abstract void init() throws Exception;
    
    
    public abstract void run() throws Exception;
    
    //__________________________________________________________________________
    
    /** 
     * Contains the command-line parameters passed to the problem. 
     */
    public String[] args;
    
    // Fields that have to be set:
    private String outputDir;
    
    private double ms;
    private double a;
    private double alpha;
    
    private double boxSizeX;
    private double boxSizeY;
    private double boxSizeZ;
    
    private double maxCellSizeX;
    private double maxCellSizeY;
    /**
     * Default value: infinity (2D simulaition).
     */
    private double maxCellSizeZ = POSITIVE_INFINITY;
    
    private Configuration initialMagnetization;
    
    // Fields with default values:
    private int fmmOrder = 2;
    private double fmmAlpha = 0.9;//negative value means touchproximity
    private int kernelIntegrationAccuracy = 2;
    private boolean precession = true;
    private double dt = 1E-5;
    private AdaptiveMeshRules aMRules = new FixedMesh();
    
    private double targetMaxAbsError = 1E-5;
    private double targetMaxRelError = POSITIVE_INFINITY;
    private double targetRmsAbsError = POSITIVE_INFINITY;
    private double targetRmsRelError = POSITIVE_INFINITY;
    private double targetMaxDm       = 0.01;
    
    private final ArrayList shapes;
    
    // Fields for advanced use only:
    /**
     * If false, a finite-difference 'mesh' will be used instead of a smooth
     * hybrid mesh.
     */
    private boolean hybridMesh = true;
    
    // Internal fields:
    protected Simulation sim;
    
    
    //__________________________________________________________________________
    
    public Problem(){
        shapes = new ArrayList<Shape>(2);
    }
    
    //________________________________________________________________________io
    
    public DataModel getData(String field) throws NoSuchFieldException{
        return sim.getModel(field);
    }
    
    public void save(String field) throws NoSuchFieldException, IOException{
        save(sim.getModel(field));
    }
    
    public void save(DataModel model) throws IOException{
        model.incrementalSave(sim.output.getBaseDir());
    }
    
    public void save(String field, int step) throws NoSuchFieldException{
        save(sim.getModel(field), step);
    }
    
    public void save(DataModel model, int step){
        sim.output.add(new PerIterations(model, step));
    }
    
    public void save(String field, double step) throws NoSuchFieldException{
        save(sim.getModel(field), step);
    }
    
    public void save(DataModel model, double step){
        sim.output.add(new PerTime(model, step/Unit.TIME));
    }
    
    //_______________________________________________________________________run
    
    public void runSteps(int steps) throws IOException{
        sim.runIterations(steps);
    }
    
    public void runTime(double time) throws IOException{
        sim.runTime(time/Unit.TIME);
    }
    
    public void runTorque(double maxTorque) throws IOException{
        sim.runTorque(maxTorque);
    }
    
    public void setExternalField(ExternalField field){
        sim.setExternalField(field);
    }
    
    //______________________________________________________________________init
    
    /**
     * After init() has been called, the simulation parameters are stored in the
     * memory. This method then reads the parameteres and constructs a simulation 
     */
    void initImpl() throws InvalidProblemDescription, IOException{
        requireNotYetInitiated();
        
        // first set the output dir so the output can be logged.
        if(outputDir == null)
            throw new InvalidProblemDescription("OutputDirectory has not been set. Use: setOutputDirectory(\"directory_name\")");
        
        // then start creating the simulation.
        sim = new Simulation(new File(outputDir));
        Main.sim = sim; //static link :(
        sim.precess = precession;
        
        Unit.setMaterial(ms, a);
       
        // create the mesh.
        Mesh mesh = new Mesh(boxSizeX, boxSizeY, boxSizeZ, 
                               maxCellSizeX, maxCellSizeY, maxCellSizeZ, aMRules);
        
        //only so that a pointer to mesh would already exist at this point.
        sim.mesh = mesh;
        
        if(shapes.size() > 0){
            Message.title("mesh transformations");
            Message.hrule();
        }
        
        for(Object o: shapes){
            if(o instanceof Shape){
                Shape s = (Shape) o;
                // at this point, Unit.LENGTH is initialized
                Message.indent("intersect with: " + s);
                mesh.intersect(s.scale(Unit.LENGTH), hybridMesh);
            }
            else if(o instanceof Homeomorphism){
                Homeomorphism t = (Homeomorphism) o;
                Message.indent("homeomorphism: " + t);
                t.apply(mesh);
            }
            else
                throw new Bug();
        }
        mesh.makeFinal();
        
        sim.setMesh(mesh);   
        sim.output.saveMesh();
       
        
        // init FMM
        sim.setAlpha(fmmAlpha);
        sim.setKernelIntegrationAccuracy(kernelIntegrationAccuracy);
        sim.setOrder(fmmOrder);
            
        // initial magnetization
        if(initialMagnetization == null)
            throw new InvalidProblemDescription("Initial magnetization has not been set.");
        sim.setMagnetization(initialMagnetization);      
        
        // set the solver (must be last)
        sim.setAlphaLLG(alpha);
        sim.evolver.targetMaxAbsError = targetMaxAbsError;
        sim.evolver.targetMaxRelError = targetMaxRelError;
        sim.evolver.targetRmsAbsError = targetRmsAbsError;
        sim.evolver.targetRmsRelError = targetRmsRelError;
        sim.evolver.targetMaxDm = targetMaxDm;
        sim.evolver.dt = dt;
        
        initiated = true;
    }
    
    //_____________________________________________________________get/set rules
    
    private void requireNotYetInitiated() throws IllegalArgumentException{
        if(initiated)
            throw new IllegalArgumentException("Method can not be called after the problem has already been initiated, use it only inside init().");
    }
    
    private void requireInitiated() throws IllegalArgumentException{
        if(!initiated)
            throw new IllegalArgumentException("Method can only be called after the problem has been initiated, use it only inside run().");
    }   
    
    //___________________________________________________________________set/get
    
    
    /*public void setTime(double time){
        requireInitiated();
        sim.totalTime = time;
        
    }*/
    
    public String[] getArgs() {
        return args;
    }  
    
    public double getArg(int index){
        try{
            return Double.parseDouble(getArgs()[index]);
        }
        catch(ArrayIndexOutOfBoundsException e){
            throw new ArrayIndexOutOfBoundsException("Argument " + index + " has not been specified");
        }
    }
    
    public void setMagnetization(Configuration c){
        this.initialMagnetization = c;
    }
    
    public void addShape(Shape s){
        requireNotYetInitiated();
        shapes.add(s);
    }
    
    public void addTransform(Homeomorphism t){
        requireNotYetInitiated();
        shapes.add(t);
    }
    
    public void setOutputDir(String outputDir) {
        requireNotYetInitiated();
        this.outputDir = outputDir;
    }

    public void setMs(double ms) {
        requireNotYetInitiated();
        this.ms = ms;
    }

    public void setA(double a) {
        requireNotYetInitiated();
        this.a = a;
    }

    public void setAlpha(double alpha) {
        this.alpha = alpha;
        if(initiated)
            sim.alpha = alpha;
    }

    public void setBoxSizeX(double boxSizeX) {
        requireNotYetInitiated();
        this.boxSizeX = boxSizeX;
    }


    public void setBoxSizeY(double boxSizeY) {
        requireNotYetInitiated();
        this.boxSizeY = boxSizeY;
    }

    public void setBoxSizeZ(double boxSizeZ) {
        requireNotYetInitiated();
        this.boxSizeZ = boxSizeZ;
    }

    public void setMaxCellSizeX(double maxCellSizeX) {
        requireNotYetInitiated();
        this.maxCellSizeX = maxCellSizeX;
    }

    public void setMaxCellSizeY(double maxCellSizeY) {
        requireNotYetInitiated();
        this.maxCellSizeY = maxCellSizeY;
    }

    public void setMaxCellSizeZ(double maxCellSizeZ) {
        requireNotYetInitiated();
        this.maxCellSizeZ = maxCellSizeZ;
    }

    public void setFmmOrder(int fmmOrder) {
        this.fmmOrder = fmmOrder;
        if(initiated)
            sim.setOrder(fmmOrder);
    }

    public void setFmmAlpha(double fmmAlpha) {
        this.fmmAlpha = fmmAlpha;
        if(initiated)
            sim.setAlpha(fmmAlpha);
    }

    public void setKernelIntegrationAccuracy(int kernelIntegrationAccuracy) {
        requireNotYetInitiated();
        this.kernelIntegrationAccuracy = kernelIntegrationAccuracy;
    }

    public void setTargetMaxAbsError(double targetMaxAbsError) {
        this.targetMaxAbsError = targetMaxAbsError;
        if(initiated)
            sim.evolver.targetMaxAbsError = targetMaxAbsError;
            //todo: setter for this, adjusts dt.
    }


    public void setTargetMaxRelError(double targetMaxRelError) {
        this.targetMaxRelError = targetMaxRelError;
        if(initiated)
            sim.evolver.targetMaxRelError = targetMaxRelError;
    }


    public void setTargetRmsAbsError(double targetRmsAbsError) {
        this.targetRmsAbsError = targetRmsAbsError;
        if(initiated)
            sim.evolver.targetRmsAbsError = targetRmsAbsError;
    }


    public void setTargetRmsRelError(double targetRmsRelError) {
        this.targetRmsRelError = targetRmsRelError;
        if(initiated)
            sim.evolver.targetRmsRelError = targetRmsRelError;
    }


    public void setTargetMaxDm(double targetMaxDm) {
        if(initiated)
            sim.evolver.targetMaxDm = targetMaxDm;
    }

    public void setPrecession(boolean precession) {
        this.precession = precession;
        if(initiated){
            sim.precess = precession;
            // this is typically done after a relaxation, when dt can be huge
            // let's make sure the solver doesn't start with too big steps.
            if(precession = true && sim.evolver.dt > 1E-5)
                setDt(1E-5);
        }
    }

    public void setDt(double dt) {
        this.dt = dt;
        if(initiated)
            sim.evolver.dt = dt;
    }
    
    public final double square(double r){
        return r*r;
    }
}
