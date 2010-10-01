//  Copyright 2010  Arne Vansteenkiste
//  Use of this source code is governed by the GNU General Public License version 3
//  (as published by the Free Software Foundation) that can be found in the license.txt file.
//  Note that you are welcome to modify this code under the condition that you do not remove any 
//  copyright notices and prominently state that you modified it, giving a relevant date.

package sim

// TODO: ALL OF THIS CODE SHOULD BE MOVED INTO THE sim PACKAGE

// TODO automatic backend selection

// TODO read magnetization + scale
// TODO Time-dependent quantities

// TODO Nice output / table
// TODO draw output immediately
// TODO movie output

import (
	"flag"
	"fmt"
	"os"
	"refsh"
	"runtime"
)

var (
	silent    *bool = flag.Bool("silent", false, "Do not show simulation output on the screen, only save to output.log")
	daemon    *bool = flag.Bool("daemon", false, "Watch directories for new input files and run them automatically.")
	watch     *int  = flag.Int("watch", 2, "When running with -daemon, re-check for new input files every N seconds. -watch=0 disables watching, program exits when no new input files are left.")
	verbosity *int  = flag.Int("verbosity", 2, "Control the debug verbosity (0 - 3)")
	gpuid     *int  = flag.Int("gpu", 0, "Select a GPU when more than one is present. Default GPU = 0") //TODO: also for master
	cpu       *bool = flag.Bool("cpu", false, "Run on the CPU instead of GPU.")
	updatedb  *int  = flag.Int("updatedisp", 100, "Update the terminal output every x milliseconds")
	// 	dryrun    *bool   = flag.Bool("dryrun", false, "Go quickly through the simulation sequence without calculating anything. Useful for debugging") // todo implement
	//  server    *bool   = flag.Bool("server", false, "Run as a slave node in a cluster")
	//  port      *int    = flag.Int("port", 2527, "Which network port to use")
	//  transport *string = flag.String("transport", "tcp", "Which transport to use (tcp / udp)")
)

// to be called by main.main()
func Main() {
	defer crashreport()                 // if we crash, catch it here and print a nice crash report
	defer fmt.Print(RESET + SHOWCURSOR) // make sure the cursor does not stay hidden if we crash

	flag.Parse()
	Verbosity = *verbosity
	if *daemon {
		DAEMON_WATCHTIME = *watch
		DaemonMain()
		return
	}

	// 	if *server {
	// 		main_slave()
	// 	} else {
	main_master()
	// 	}
}

// when running in the normal "master" mode, i.e. given an input file to process locally
func main_master() {

	Debugvv("Locked OS thread")
	runtime.LockOSThread()

	if flag.NArg() == 0 {
		fmt.Fprintln(os.Stderr, "No input files.")
		os.Exit(-1)
	}

	UpdateDashboardEvery = int64(*updatedb * 1000 * 1000)

	// Process all input files
	for i := 0; i < flag.NArg(); i++ {
		infile := flag.Arg(i)
		in, err := os.Open(infile, os.O_RDONLY, 0666)
		if err != nil {
			fmt.Fprintln(os.Stderr, err)
			os.Exit(-2)
		}
		defer in.Close()

		//TODO it would be safer to abort when the output dir is not empty
		sim := NewSim(removeExtension(infile) + ".out")
		defer sim.out.Close()
		sim.silent = *silent
		// Set the device
		if *cpu {
			sim.backend = CPU
			sim.backend.init()
		} else {
      fmt.Fprintln(os.Stderr, "GPU support not yet implemented")
		}
		refsh := refsh.New()
		refsh.CrashOnError = true
		refsh.AddAllMethods(sim)
		refsh.Output = sim
		refsh.Exec(in)

		// Idiot-proof error reports
		if refsh.CallCount == 0 {
			sim.Errorln("Input file contains no commands.")
		}
		if !sim.BeenValid {
			sim.Errorln("Input file does not contain any commands to make the simulation run. Use, e.g., \"run\".")
		}
		// The next two lines cause a nil pointer panic when the simulation is not fully initialized
		if sim.BeenValid && Verbosity > 2 {
			sim.TimerPrintDetail()
			sim.PrintTimer(os.Stdout)
		}

		// TODO need to free sim

	}
}

// Removes a filename extension.
// I.e., the part after the dot, if present.
func removeExtension(str string) string {
	dotpos := len(str) - 1
	for dotpos >= 0 && str[dotpos] != '.' {
		dotpos--
	}
	return str[0:dotpos]
}

// when running in "slave" mode, i.e. accepting commands over the network as part of a cluster
// func main_slave() {
// 	server := NewDeviceServer(*device, *transport, *port)
// 	server.Listen()
// }

// This function is deferred from Main(). If a panic()
// occurs, it prints a nice explanation and asks to
// mail the crash report.
func crashreport() {
	error := recover()
	if error != nil {
		fmt.Fprintln(os.Stderr,
			`
			
---------------------------------------------------------------------
Aw snap, the program has crahsed.
If you would like to see this issue fixed, please mail a bugreport to
Arne.Vansteenkiste@UGent.be and/or Ben.VandeWiele@UGent.be.
Be sure to include the output of your terminal, both the parts above
and below this message (in most terminals you can copy the output
with Ctrl+Shift+C).
---------------------------------------------------------------------

`)
		panic(error)
	}
}
