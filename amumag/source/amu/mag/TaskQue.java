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

import java.util.Vector;

public final class TaskQue{

    private final Vector<Task> tasks = new Vector<Task>();
    private int nextTaskIndex;
    
    private final int threads;
    private int busy;
    
    public TaskQue(){
	threads = Runtime.getRuntime().availableProcessors();
    }
    
    public TaskQue(int capacity){
        this();
        tasks.ensureCapacity(capacity);
    }
    
    public TaskQue(Task[] todo){
        this(todo.length);
        for(int i=0; i<todo.length; i++)
            tasks.add(todo[i]);
    }
    
    public synchronized void add(Task task){
        tasks.add(task);
    }
    
    public String toString(){
       return "Que: tasks=" + tasks.size() + ", CPU's=" + threads;
    }
    
    public synchronized void run(){
	Task next = getNextTask();
	while(next != null){			// as long as there are tasks left
	    while(next != null && busy < threads){				// start as many tasks as you can
		//System.err.println(">>> " + next);
		new QueThread(this, next).start();
		busy++;
		next = getNextTask();
	    }
	    try{			
		//System.out.println("Que::wait.");
		wait();					// wait for tasks to finish.
	    }
	    catch(InterruptedException e){}
	}
    }
    
    
    private Task getNextTask(){
	if(nextTaskIndex < tasks.size()){
	    Task task = tasks.get(nextTaskIndex);
	    nextTaskIndex++;
	    return task;
	}
	else
	    return null;
    }
    
   private final class QueThread extends Thread{
	
       private final Task task;
       private final TaskQue lock;
       
       public QueThread(TaskQue lock, Task task){
	    this.lock = lock;
	    this.task = task;
       }
       
        @Override
	public void run(){
	    try{
		task.run();
	    }
	    catch(Exception e){
		e.printStackTrace();
	    }
	    synchronized(lock){
		busy--;
		lock.notifyAll();
	    }
        }
	
	@Override
	public String toString(){
	    return task.toString();
	}
   }
}
