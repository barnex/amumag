//  Copyright 2010  Arne Vansteenkiste
//  Use of this source code is governed by the GNU General Public License version 3
//  (as published by the Free Software Foundation) that can be found in the license.txt file.
//  Note that you are welcome to modify this code under the condition that you do not remove any 
//  copyright notices and prominently state that you modified it, giving a relevant date.

// Magnetic simulation package
package sim

import (
	"tensor"
	"time"
	"fmt"
	"os"
)

// Sim has an "input" member of type "Input".
//
// In this struct, all parameters are STILL IN SI UNITS.
// When Sim.init() is called, a solver is initiated
// with these values converted to internal units.
// We need to keep the originial SI values in case a
// parameter gets changed during the simulation and
// we need to re-initialize everything.
//
// This struct is not embedded in Sim but appears as
// a member "input" so that we have to write, e.g.,
// sim.input.dt to make clear it is not necessarily the
// same as sim.dt (which is in internal units)
//
type Input struct {
	aexch          float
	msat           float
	alpha          float
	size           [3]int
	cellSize       [3]float
	demag_accuracy int
	dt             float
	solvertype     string
}


// The Sim struct stores a simulation state.
//
// The Sim struct has a lot of exported methods.
// When an input file is processed, reflection is used
// to resolve commands in the file to methods and call them.
// (See sim*.go, refsh/)
// All these methods may be called repeatedly and in any
// order; we use decentralized initialization to make sure
// everything works out.
//
// TODO order of initialization is too important in input file, should be more versatile
//
type Sim struct {
	input        Input           // stores the original input parameters in SI units
	valid        bool            // false when an init() is needed, e.g. when the input parameters have changed and do not correspond to the simulation anymore
	BeenValid    bool            // true if the sim has been valid at some point. used for idiot-proof input file handling (i.e. no "run" commands)
	backend      *Backend        // GPU or CPU TODO already stored in Conv, sim.backend <-> sim.Backend is not the same, confusing.
	mLocal       *tensor.Tensor4 // a "local" copy of the magnetization (i.e., not on the GPU) use for I/O
	Material                     // Stores material parameters and manages the internal units
	Mesh                         // Stores the size of the simulation grid
	Conv                         // Convolution plan for the magnetostatic field
	AppliedField                 // returns the externally applied in function of time
	hext         [3]float        // stores the externally applied field returned by AppliedField
	mDev, h      *DevTensor      // magnetization/effective field on the device (GPU), 4D tensor
	mComp, hComp [3]*DevTensor   // magnetization/field components, 3 x 3D tensors
	Solver                       // Does the time stepping, can be euler, heun, ...
	time         float64         // The total time (internal units)
	dt           float           // The time step (internal units). May be updated by adaptive-step solvers
	maxDm        float           // The maximum magnetization step ("delta m") to be taken by the solver. 0 means not used. May be ignored by certain solvers.
	maxError     float           // The maximum error per step to be made by the solver. 0 means not used. May be ignored by certain solvers.
	stepError    float           // The actual error estimate of the last step. Not all solvers update this value.
	steps        int             // The total number of steps taken so far
	starttime    int64           // Walltime when the simulation was started, seconds since unix epoch. Used by dashboard.go
	outschedule  []Output        // List of things to output. Used by simoutput.go. TODO make this a Vector, clean up
	autosaveIdx  int             // Unique identifier of output state. Updated each time output is saved.
	outputdir    string          // Where to save output files.
	mUpToDate    bool            // Is mLocal up to date with mDev? If not, a copy form the device is needed before storing output.
	silent       bool
	out          *os.File
}

func New(outputdir string) *Sim {
	return NewSim(outputdir)
}

func NewSim(outputdir string) *Sim {
	sim := new(Sim)
	sim.starttime = time.Seconds()
	sim.backend = CPU
	sim.outschedule = make([]Output, 50)[0:0]
	sim.mUpToDate = false
	sim.input.demag_accuracy = 8
	sim.autosaveIdx = -1 // so we will start at 0 after the first increment
	sim.outputDir(outputdir)
	sim.initWriters()
	sim.invalidate() //just to make sure we will init()
	return sim
}

// When a parmeter is changed, the simulation state is invalidated until it gets (re-)initialized by init().
func (s *Sim) invalidate() {
	if s.IsValid() {
		s.Println("Simulation state invalidated")
	}
	s.valid = false
}

// When it returns false, init() needs to be called before running.
func (s *Sim) IsValid() bool {
	return s.valid
}

func (s *Sim) initSize() {
	s.size4D[0] = 3 // 3-component vectors
	for i := range s.size {
		s.size[i] = s.input.size[i]
		assert(s.size[i] > 0)
		s.size4D[i+1] = s.size[i]
	}
	s.Println("Simulation size ", s.size, " = ", s.size[0]*s.size[1]*s.size[2], " cells")
}

func (s *Sim) initMLocal() {
	s.initSize()
	if s.mLocal == nil {
		s.Println("Allocating local memory " + fmt.Sprint(s.size4D))
		s.mLocal = tensor.NewTensor4(s.size4D[0:])
	}

	if !tensor.EqualSize(s.mLocal.Size(), Size4D(s.input.size[0:])) {
		s.Println("Resampling magnetization from ", s.mLocal.Size(), " to ", Size4D(s.input.size[0:]))
		s.mLocal = resample(s.mLocal, Size4D(s.input.size[0:]))
	}
}

// (Re-)initialize the simulation tree, necessary before running.
func (s *Sim) init() {
	if s.IsValid() {
		return //no work to do
	}
	s.Println("Initializing simulation state")

	dev := s.backend
	// 	dev.InitBackend()
	assert(s.backend != nil)
	assert(s != nil)

	// (1) Material parameters control the units,
	// so they need to be set up first
	s.InitMaterial()
	s.mSat = s.input.msat
	s.aExch = s.input.aexch
	s.alpha = s.input.alpha

	// (2) Size must be set before memory allocation
	s.initSize()
	L := s.UnitLength()
	for i := range s.size {
		s.cellSize[i] = s.input.cellSize[i] / L
		assert(s.cellSize[i] > 0.)
	}

	// (3) Allocate memory, but only if needed
	// Free previous memory only if it has the wrong size
	// Todo device should not have been changed
	// 	if s.mDev != nil && !tensor.EqualSize(s.mDev.Size(), s.size4D[0:]) {
	// 		// TODO: free
	// 		s.mDev = nil
	// 		s.h = nil
	// 	}

	// 	if s.mDev == nil {
	s.Println("Allocating device memory " + fmt.Sprint(s.size4D))
	s.mDev = NewTensor(dev, s.size4D[0:])
	s.h = NewTensor(dev, s.size4D[0:])
	s.printMem()
	s.mComp, s.hComp = [3]*DevTensor{}, [3]*DevTensor{}
	for i := range s.mComp {
		s.mComp[i] = s.mDev.Component(i)
		s.hComp[i] = s.h.Component(i)
	}
	// 	}


	s.initMLocal()

	TensorCopyTo(s.mLocal, s.mDev)
	// 	s.Normalize(s.mDev)

	// (4) Calculate kernel & set up convolution

	s.paddedsize = padSize(s.size[0:])

	s.Println("Calculating kernel (may take a moment)") // --- In fact, it takes 3 moments, one in each direction.
	demag := FaceKernel6(s.paddedsize, s.cellSize[0:], s.input.demag_accuracy)
	exch := Exch6NgbrKernel(s.paddedsize, s.cellSize[0:])
	// Add Exchange kernel to demag kernel
	for i := range demag {
		D := demag[i].List()
		E := exch[i].List()
		for j := range D {
			D[j] += E[j]
		}
	}
	s.Conv = *NewConv(dev, s.size[0:], demag)
	s.printMem()

	// (5) Time stepping
	s.Println("Initializing solver: ", s.input.solvertype)
	s.dt = s.input.dt / s.UnitTime()
	s.Solver = NewSolver(s.input.solvertype, s)
	s.printMem()

	s.valid = true // we can start the real work now
	s.BeenValid = true
}


// OBSOLETE: CLI flag
// Set how much debug info is printed. Level=0,1,2 or 3 for none, normal, verbose and very verbose.
// func (s *Sim) Verbosity(level int) {
// 	Verbosity = level
// 	// does not invalidate
// }


func resample(in *tensor.Tensor4, size2 []int) *tensor.Tensor4 {
	assert(len(size2) == 4)
	out := tensor.NewTensor4(size2)
	out_a := out.Array()
	in_a := in.Array()
	size1 := in.Size()
	for c := range out_a {
		for i := range out_a[c] {
			i1 := (i * size1[1]) / size2[1]
			for j := range out_a[0][i] {
				j1 := (j * size1[2]) / size2[2]
				for k := range out_a[0][i][j] {
					k1 := (k * size1[3]) / size2[3]
					out_a[c][i][j][k] = in_a[c][i1][j1][k1]
				}
			}
		}
	}
	return out
}
