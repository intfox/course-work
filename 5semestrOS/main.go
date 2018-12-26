// Copyright 2012 The Go Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

// +build windows

// Example service program that beeps.
//
// The program demonstrates how to create Windows service and
// install / remove it on a computer. It also shows how to
// stop / start / pause / continue any service, and how to
// write to event log. It also shows how to use debug
// facilities available in debug package.
//
package main

import (
	"fmt"
	"flag"
	"log"
	"os"
	"strings"

	"github.com/intfox/course-work/5semestrOS/service"
	"golang.org/x/sys/windows/svc"
)

func usage(errmsg string) {
	fmt.Fprintf(os.Stderr,
		"%s\n\n"+
			"usage: %s <command>\n"+
			"       where <command> is one of\n"+
			"       install, remove, debug, start, stop, pause or continue.\n",
		errmsg, os.Args[0])
	os.Exit(2)
}

func main() {
	const svcName = "myservice"
	// var err error
	isIntSess, err := svc.IsAnInteractiveSession()
	if err != nil {
		log.Fatalf("failed to determine if we are running in an interactive session: %v", err)
	}
	if !isIntSess {
		service.RunService(svcName, false)
		return
	}

	port := flag.String("port", "8080", "port http service: string")
	flag.Parse()
	if len(flag.Args()) < 1 {
		usage("no command specified")
	}
	cmd := strings.ToLower(flag.Args()[0])

	switch cmd {
	case "debug":
		service.RunService(svcName, true)
		return
	case "install":
		err = service.InstallService(svcName, "rest task manager")
	case "remove":
		err = service.RemoveService(svcName)
	case "start":
		err = service.StartService(svcName, *port)
	case "stop":
		err = service.ControlService(svcName, svc.Stop, svc.Stopped)
	case "pause":
		err = service.ControlService(svcName, svc.Pause, svc.Paused)
	case "continue":
		err = service.ControlService(svcName, svc.Continue, svc.Running)
	default:
		usage(fmt.Sprintf("invalid command %s", cmd))
	}
	if err != nil {
		log.Fatalf("failed to %s %s: %v", cmd, svcName, err)
	}
	return
}
