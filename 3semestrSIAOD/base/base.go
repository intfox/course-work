package base

import (
	"encoding/binary"
	"fmt"
	"os"
)

type Key func(Record, int) int
type KeySearch interface {
	Less(RecordString, RecordString) bool
	Equal(RecordString, RecordString) bool
}

type Spis struct {
	Head *Elem
}

type Elem struct {
	Element Record
	Next    *Elem
}

func (s *Spis) Init() {
	s.Head = new(Elem)
}

func (s *Spis) Push(rec Record) {
	elem := s.Head
	for elem.Next != nil {
		elem = elem.Next
	}
	elem.Next = new(Elem)
	elem.Next.Element = rec
}

func (s *Spis) CreateArray() []Record {
	var elem *Elem
	elem = s.Head
	var n int
	for elem.Next != nil {
		n++
		elem = elem.Next
	}
	elem = s.Head
	retArray := make([]Record, n)
	for i := 0; i < n; i++ {
		retArray[i] = elem.Next.Element
		elem = elem.Next
	}
	return retArray
}

func (s Spis) Print() {
	var elem *Elem
	elem = s.Head
	for elem != nil {
		fmt.Println(elem.Next.Element)
		elem = elem.Next
	}
}

func DigitalSort(key Key, spis Spis, keyNumb int) {
	Q := make([]Spis, 256)
	for i := 0; i < 256; i++ {
		Q[i].Init()
	}
	var elem *Elem
	var Qelem *Elem
	for i := keyNumb - 1; i >= 0; i-- {
		elem = spis.Head
		for elem.Next != nil {
			Qelem = Q[key(elem.Next.Element, i)].Head
			for Qelem.Next != nil {
				Qelem = Qelem.Next
			}
			Qelem.Next = elem.Next
			elem.Next = elem.Next.Next
			Qelem.Next.Next = nil
		}
		elem = spis.Head
		for j := 0; j < 256; j++ {
			Qelem = Q[j].Head
			for elem.Next != nil {
				elem = elem.Next
			}
			elem.Next = Qelem.Next
			Qelem.Next = nil
		}
	}
}

func TestKey(rec Record, a int) int {
	return int(rec.Author[a])
}

func FamilyKey(rec Record, a int) int {
	var space int
	var i int
	for space < 2 {
		if rec.Title[i] == byte(32) {
			space++
		}
		i++
	}
	return int(rec.Title[a+i])
}

type ByAuthor []Record

func (rec ByAuthor) Len() int {
	return len(rec)
}

func (rec ByAuthor) Swap(a, b int) {
	rec[a], rec[b] = rec[b], rec[a]
}

func (rec ByAuthor) Less(a, b int) bool {
	for i := range rec[a].Author {
		if rec[a].Title[i] < rec[b].Title[i] {
			return true
		} else if rec[a].Title[i] > rec[b].Title[i] {
			return false
		}
	}
	return false
}

type Record struct {
	Author      [12]byte
	Title       [32]byte
	Publisher   [16]byte
	Year        int16
	Num_of_page int16
}

type RecordString struct {
	Author      string
	Title       string
	Publisher   string
	Year        int
	Num_of_page int
	Numb        int
}

func Read() []Record {
	const sizeBD = 4000
	file, err := os.Open("testBase1.dat")
	if err != nil {
		fmt.Println("error: file testBase1.dat not found")
		return make([]Record, 1)
	}
	bd := make([]Record, sizeBD)
	err = binary.Read(file, binary.LittleEndian, &bd)
	return bd
}

func (rec Record) ConvertStructToString() RecordString {
	var returnRec RecordString
	returnRec.Author = ConvertToString(rec.Author[0:12])
	returnRec.Title = ConvertToString(rec.Title[0:32])
	returnRec.Publisher = ConvertToString(rec.Publisher[0:16])
	returnRec.Year = int(rec.Year)
	returnRec.Num_of_page = int(rec.Num_of_page)
	return returnRec
}

func BinSearch(array []RecordString, searchElem RecordString, f KeySearch) (bool, RecordString) {
	L := 1
	R := len(array)
	var m int
	for L < R {
		m = (L + R) / 2
		if f.Less(array[m], searchElem) {
			L = m + 1
		} else {
			R = m
		}
	}
	if f.Equal(array[m], searchElem) {
		return true, array[m]
	} else {
		return false, array[m]
	}
}

func ConvertToString(str []byte) string {
	var returnStr string
	for i := range str {
		if str[i] == 0 {
		} else if str[i] < 127 {
			returnStr += string(str[i])
		} else if str[i] < 176 {
			returnStr += string(int(str[i]) + (1040 - 128))
		} else {
			returnStr += string(int(str[i]) + (1088 - 224))
		}
	}
	return returnStr
}
