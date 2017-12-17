package base

import (
	"encoding/binary"
	"fmt"
	"os"
)

type Key func(Record, int) int
type KeySearch interface {
	Less(int, string) bool
	Equal(int, string) bool
	Section(int, int) []RecordString
	Len() int
}

type FamilyKeySearch []RecordString

func (fks FamilyKeySearch) Less(rs1 int, rs2 string) bool {
	var buff_rs1, buff_rs2 string
	var space int
	var i int
	for space < 2 {
		if fks[rs1].Title[i] == byte(32) {
			space++
		}
		i++
	}
	buff_rs1 = string(fks[rs1].Title[i:])
	i = 0
	space = 0
	for space < 2 {
		if rs2[i] == byte(32) {
			space++
		}
		i++
	}
	buff_rs2 = string(rs2[i:])
	if buff_rs1 < buff_rs2 {
		return true
	} else {
		return false
	}
}

func (fks FamilyKeySearch) Equal(rs1 int, rs2 string) bool {
	fmt.Println("LEN:", len(fks))
	fmt.Println("fks[rs1]:", fks[rs1].Title, " rs2:", rs2)
	fmt.Println("len(fks[rs1]):", len(fks[rs1].Title), "len(rs2)", len(rs2))
	fmt.Println("fks[rs1] == rs2: ", fks[rs1].Title == rs2)
	if fks[rs1].Title == rs2 {
		return true
	} else {
		return false
	}
}

func (fks FamilyKeySearch) Section(rs1 int, rs2 int) []RecordString {
	return fks[rs1:rs2]
}

func (fks FamilyKeySearch) Len() int {
	return len(fks)
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

func BinSearch(array KeySearch, searchElem string) (bool, []RecordString) {
	L := 0
	R := array.Len()
	var m int
	for L < R {
		m = (L + R) / 2
		fmt.Println("L:", L, "R:", R)
		if array.Less(m, searchElem) {
			L = m + 1
		} else {
			R = m
		}
	}
	m_last := m + 1
	for array.Equal(m_last, searchElem) {
		m_last++
	}
	if array.Equal(m, searchElem) {
		fmt.Println("m:", m, " m_last:", m_last)
		fmt.Println("array[:]", array.Section(m, m_last))
		return true, array.Section(m, m_last)
	} else {
		return false, make([]RecordString, 1)
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
