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


package amu.core;
import static java.lang.Math.*;

//might be replaced by BigDecimal.
public final class Bucket {

    private double[] buckets;
    private double checksum;
    private boolean underflow;
    private boolean overflow;
    private int offset;
    
    public Bucket(double min, double max){
        offset = -(int)log(min);
        buckets = new double[(int)log(max)+1+offset];
    }
    
    private int getBucket(double d){
        int index = (int)(log(d)/log(16)) + offset;
        if (index < 0){
            index = 0;
            underflow = true;
        }
        else if(index >= buckets.length-1){
            index = buckets.length - 1;
            overflow = true;
        }
        return index;
    }
    
    public void add(double d){   
        add_impl(d);
        checksum += d;
    }
    
    private void add_impl(double d){
        int index = getBucket(d);
        buckets[index] += d;
        if(index != getBucket(buckets[index])){
            add_impl(buckets[index]);
            buckets[index] = 0.0;
        }
    }
    
    public double getSum(){
       double sum = 0.0;
       for(int i = 0; i < buckets.length; i++)
           sum += buckets[i];
       return sum;
    }
    
    public void reset(){
       for(int i = 0; i < buckets.length; i++)
           buckets[i] = 0.0;
       checksum = 0.0;
       underflow = false;
       overflow = false;
    }
    
    @Override
    public String toString(){
        StringBuffer buf = new StringBuffer("Bucket: sum=" + getSum() + ", checksum=" + checksum + 
                ", overflow=" + overflow + ", underflow=" + underflow + "\nbuckets=\n");
        for(int i=0; i<buckets.length; i++)
            buf.append(buckets[i] + "\n");
        return buf.toString();
    }
}
