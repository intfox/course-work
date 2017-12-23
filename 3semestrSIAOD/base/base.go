package base

import (
	"encoding/binary"
	"fmt"
	"math"
	"os"
	"strconv"
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
	var buff_rs1, buff_rs2, buff_rs1_b string
	var space int
	var i int
	for space < 2 {
		if fks[rs1].Title[i] == byte(32) {
			space++
		}
		i++
	}
	buff_rs1_b = string(fks[rs1].Title[i:])
	for i = len(buff_rs1_b) - 1; i > 0; i-- {
		if buff_rs1_b[i] != byte(32) {
			break
		}
	}
	buff_rs1 = string(buff_rs1_b[:i+1])
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
	var i int
	for i = len(fks[rs1].Title) - 1; i > 0; i-- {
		if fks[rs1].Title[i] != byte(32) {
			break
		}
	}
	buff := string(fks[rs1].Title[:i+1])
	if buff == rs2 {
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
	fmt.Println("digital sort")
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
	fmt.Println("read file")
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
	fmt.Println("bin search")
	L := 0
	R := array.Len()
	var m int
	for L < R {
		m = (L + R) / 2
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
	if array.Equal(m+1, searchElem) {
		return true, array.Section(m+1, m_last)
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

type AVLTree struct {
	head  *elemTree
	grows bool
	Quant int
}

type elemTree struct {
	Elem    RecordString
	Left    *elemTree
	Right   *elemTree
	Balance int
}

func (tr *AVLTree) Push(new_elem RecordString) {
	tr.Quant++
	tr.add(&tr.head, new_elem)
}

func (tr *AVLTree) add(p **elemTree, new_elem RecordString) {
	if (*p) == nil {
		(*p) = new(elemTree)
		(*p).Elem = new_elem
		(*p).Balance = 0
		tr.grows = true
	} else {
		if tr.less(new_elem, (*p).Elem) {
			tr.add(&(*p).Left, new_elem)
			if tr.grows {
				if (*p).Balance > 0 {
					(*p).Balance = 0
					tr.grows = false
				} else if (*p).Balance == 0 {
					(*p).Balance = -1
				} else {
					if (*p).Left.Balance < 0 {
						tr.turnLL(p)
						tr.grows = false
					} else {
						tr.turnLR(p)
						tr.grows = false
					}
				}
			}
		} else {
			tr.add(&(*p).Right, new_elem)
			if tr.grows {
				if (*p).Balance < 0 {
					(*p).Balance = 0
					tr.grows = false
				} else if (*p).Balance == 0 {
					(*p).Balance = 1
				} else {
					if (*p).Right.Balance > 0 {
						tr.turnRR(p)
						tr.grows = false
					} else {
						tr.turnRL(p)
						tr.grows = false
					}
				}
			}
		}
	}
}

func (tr *AVLTree) turnLL(p **elemTree) {
	q := (*p).Left
	q.Balance = 0
	(*p).Balance = 0
	(*p).Left = q.Right
	q.Right = (*p)
	*p = q
}

func (tr *AVLTree) turnLR(p **elemTree) {
	q := (*p).Left
	r := q.Right
	if r.Balance < 0 {
		(*p).Balance = 1
	} else {
		(*p).Balance = 0
	}
	if r.Balance > 0 {
		q.Balance = -1
	} else {
		q.Balance = 0
	}
	r.Balance = 0
	(*p).Left = r.Right
	q.Right = r.Left
	r.Left = q
	r.Right = (*p)
	*p = r
}

func (tr *AVLTree) turnRR(p **elemTree) {
	q := (*p).Right
	q.Balance = 0
	(*p).Balance = 0
	(*p).Right = q.Left
	q.Left = (*p)
	*p = q
}

func (tr *AVLTree) turnRL(p **elemTree) {
	q := (*p).Right
	r := q.Left
	if r.Balance > 0 {
		(*p).Balance = -1
	} else {
		(*p).Balance = 0
	}
	if r.Balance < 0 {
		q.Balance = 1
	} else {
		q.Balance = 0
	}
	r.Balance = 0
	(*p).Right = r.Left
	q.Left = r.Right
	r.Left = (*p)
	r.Right = q
	(*p) = r
}

func (tr *AVLTree) less(rs1 RecordString, rs2 RecordString) bool {
	var buff_rs1, buff_rs2 string
	var space int
	var i int
	for space < 2 && i < len(rs1.Title) {
		if rs1.Title[i] == byte(32) {
			space++
		}
		i++
	}
	buff_rs1 = string(rs1.Title[i:])
	i = 0
	space = 0
	for space < 2 && i < len(rs2.Title) {
		if rs2.Title[i] == byte(32) {
			space++
		}
		i++
	}
	buff_rs2 = string(rs2.Title[i:])
	return buff_rs1 < buff_rs2
}

func (tr *AVLTree) RoundTreeLR(bd []RecordString) { //left-right
	if len(bd) < tr.Quant {
		bd = make([]RecordString, tr.Quant)
	}
	var i int
	tr.roundTreeRecureLR(tr.head, bd, &i)
}

func (tr *AVLTree) roundTreeRecureLR(pointer *elemTree, bd []RecordString, i *int) {
	if pointer == nil {
		return
	}
	tr.roundTreeRecureLR(pointer.Left, bd, i)
	bd[(*i)] = pointer.Elem
	(*i)++
	tr.roundTreeRecureLR(pointer.Right, bd, i)
}

func (tr *AVLTree) RoundTreeTD(bd []RecordString) { //top-down
	if len(bd) < tr.Quant {
		bd = make([]RecordString, tr.Quant)
	}
	var i int
	tr.roundTreeRecureTD(tr.head, bd, &i)
}

func (tr *AVLTree) roundTreeRecureTD(pointer *elemTree, bd []RecordString, i *int) {
	if pointer == nil {
		return
	}
	bd[(*i)] = pointer.Elem
	(*i)++
	tr.roundTreeRecureTD(pointer.Left, bd, i)
	tr.roundTreeRecureTD(pointer.Right, bd, i)
}

//Кодирование
func ReadByteFile() []byte {
	file, err := os.Open("testBase1.dat")
	if err != nil {
		fmt.Println("error: file testBase1.dat not found")
	}
	fileInfo, _ := os.Stat("testBase1.dat")
	buff := make([]byte, fileInfo.Size())
	err = binary.Read(file, binary.LittleEndian, &buff)
	return buff
}

func InCode(array []byte) map[byte]string {
	fmt.Println("code file")
	dict := make(map[byte]int)
	for i := range array {
		if _, exist := dict[array[i]]; exist {
			dict[array[i]] += 1
		} else {
			dict[array[i]] = 1
		}
	}
	dict_p := make(map[byte]float64)
	for i, quant := range dict {
		dict_p[i] = float64(quant) / float64(len(array))
	}
	dict_q := make(map[byte]float64)
	var q_buff float64
	for i := range dict_p {
		dict_q[i] = q_buff + dict_p[i]/2
		q_buff += dict_p[i]
	}
	dict_codeWord := make(map[byte]string)
	for i := range dict_q {
		dict_codeWord[i] = codeWord(dict_q[i], (-int(math.Log2(dict_p[i])))+1)
	}
	return dict_codeWord
}

func codeWord(Q float64, L int) string {
	var result string
	for i := 0; i < L; i++ {
		Q -= float64(int(Q))
		Q *= 2
		result += strconv.Itoa(int(Q))
	}
	return result
}
