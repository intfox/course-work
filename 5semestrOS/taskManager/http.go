package taskManager

import (
	"net/http"
	"encoding/json"
	"golang.org/x/sys/windows"
	"io"
	"syscall"
	"unsafe"
	"github.com/gorilla/mux"
	"strconv"
)

var server *http.Server
const TH32CS_SNAPPROCESS = 0x00000002

func Init(port string) {
	r := mux.NewRouter()
	r.HandleFunc("/process", handlerListProcess).Methods("GET")
	r.HandleFunc("/process/{id}", handlerStopProcess).Methods("DELETE")
	server = &http.Server{
		Addr: ":" + port,
		Handler: r,
	}
}

func Start() {
	go func() {
		server.ListenAndServe()
	}()
}

func Stop() {
	server.Shutdown(nil)
}

type myProcessEntry struct {
    Size            uint32
    Usage           uint32
    ProcessID       uint32
    DefaultHeapID   uintptr
    ModuleID        uint32
    Threads         uint32
    ParentProcessID uint32
    PriClassBase    int32
    Flags           uint32
    ExeFile         string
}

func toMyProcessEntry(pe windows.ProcessEntry32) myProcessEntry {
	return myProcessEntry{
	Size: pe.Size,
    Usage: pe.Usage,
    ProcessID: pe.ProcessID,
    DefaultHeapID: pe.DefaultHeapID,
    ModuleID: pe.ModuleID,
    Threads: pe.Threads,
    ParentProcessID: pe.ParentProcessID,
    PriClassBase: pe.PriClassBase,
    Flags: pe.Flags,
    ExeFile: syscall.UTF16ToString(pe.ExeFile[:]),
	}
}

func handlerListProcess(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")

	var processes struct {
		Processes []myProcessEntry
	}
	handle, err := windows.CreateToolhelp32Snapshot(TH32CS_SNAPPROCESS, 0)
	if err != nil {
		io.WriteString(w, "error CreateToolhelp32Snapshot")
		return
	}
	var bufferEntry windows.ProcessEntry32
	bufferEntry.Size = uint32(unsafe.Sizeof(bufferEntry))
	err = windows.Process32First(handle, &bufferEntry)
	processes.Processes = append(processes.Processes, toMyProcessEntry(bufferEntry))
	if err != nil {
		io.WriteString(w, "error Process32First: " + err.Error())
	}
	for err != syscall.ERROR_NO_MORE_FILES {
		processes.Processes = append(processes.Processes, toMyProcessEntry(bufferEntry))
		if err != nil {
			io.WriteString(w, "error Process32Next: " + err.Error())
			return
		}
		err = windows.Process32Next(handle, &bufferEntry)
	}
	var result []byte
	result, err = json.Marshal(processes)
	if err != nil {
		io.WriteString(w, "error marshal: " + err.Error())
		return
	}
	windows.CloseHandle(handle)
	io.WriteString(w, string(result))
}

func handlerStopProcess(w http.ResponseWriter, r *http.Request) {
	w.Header().Set("Content-Type", "application/json")
	var errorResult struct {
		Error string
	}
	errorResult.Error = ""

	pid, err := strconv.Atoi(mux.Vars(r)["id"])

	var handle syscall.Handle
	handle, err = syscall.OpenProcess(syscall.PROCESS_TERMINATE, false, uint32(pid))
	if err != nil {
		errorResult.Error = "error OpenProcess: " + err.Error()
	}

	err = syscall.TerminateProcess(handle, 0)
	if err != nil {
		errorResult.Error += "error TerminateProcess: " + err.Error()
	}
	var result []byte
	result, err = json.Marshal(errorResult)
	if err != nil {
		io.WriteString(w, "error marshal: " + err.Error())
		return
	}
	syscall.CloseHandle(handle)
	io.WriteString(w, string(result))
}