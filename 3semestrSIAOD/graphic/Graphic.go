package graphic

import (
	"fmt"
	"html/template"
	"net/http"
	"strconv"

	"github.com/intfox/course-work/3semestrSIAOD/base"
)

var bd []base.Record
var bd_string []base.RecordString
var bdSpis base.Spis
var numb_elem_bd = 4000
var numb_elem_table = 20
var pageNotFound, err = template.ParseFiles("html/pageNotFound.html")
var bdTree base.AVLTree
var tree_status bool
var bdTreeArr []base.RecordString
var dict_code map[byte]string

type RetDat struct {
	RetBD           []base.RecordString
	RetPage         int
	RetNumb_of_page int
}

func indexHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("html/index.html")
	if err != nil {
		fmt.Fprint(w, "error: index.html")
		return
	}
	t.ExecuteTemplate(w, "index", nil)
}

func pageHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("html/page.html")
	if err != nil {
		fmt.Fprint(w, "error: page.html")
		return
	}
	//	bd := base.Read()
	var ret_dat RetDat
	ret_dat.RetPage, err = strconv.Atoi(r.URL.Path[len("/page/"):])
	ret_dat.RetNumb_of_page = len(bd)/numb_elem_table - 1
	if ret_dat.RetPage < len(bd)/numb_elem_table && ret_dat.RetPage > -1 {
		ret_dat.RetBD = make([]base.RecordString, numb_elem_table)
		for i := 0; i < numb_elem_table && i+(ret_dat.RetPage*numb_elem_table) < len(bd); i++ {
			if tree_status {
				ret_dat.RetBD[i] = bdTreeArr[i+(ret_dat.RetPage*numb_elem_table)]
			} else {
				ret_dat.RetBD[i] = bd[i+(ret_dat.RetPage*numb_elem_table)].ConvertStructToString()
			}
			ret_dat.RetBD[i].Numb = (ret_dat.RetPage * numb_elem_table) + i
		}
		t.ExecuteTemplate(w, "page", ret_dat)
	} else {
		pageNotFound.ExecuteTemplate(w, "pageNotFound", nil)
	}
}

func sortHendler(w http.ResponseWriter, r *http.Request) {
	base.DigitalSort(base.FamilyKey, bdSpis, 3)
	bd = bdSpis.CreateArray()
	http.Redirect(w, r, "/page/0", 302)
	bd_string = make([]base.RecordString, numb_elem_bd)
	for i := 0; i < numb_elem_bd; i++ {
		bd_string[i] = bd[i].ConvertStructToString()
		bd_string[i].Numb = i
	}
}

func searchHendler(w http.ResponseWriter, r *http.Request) {
	if len(bd_string) == 0 {
		fmt.Fprint(w, "База данных не отсортированна")
		return
	}
	t, err := template.ParseFiles("html/page.html")
	if err != nil {
		fmt.Fprint(w, "Error page.html not found")
	}
	search_elem_string := r.FormValue("search_elem")
	var ret_dat RetDat
	var search_elem base.RecordString
	search_elem.Author = search_elem_string
	var search_bool bool
	search_bool, ret_dat.RetBD = base.BinSearch(base.FamilyKeySearch(bd_string), search_elem_string)
	ret_dat.RetNumb_of_page = len(ret_dat.RetBD)/numb_elem_table - 1
	ret_dat.RetPage = 0
	if search_bool {
		t.ExecuteTemplate(w, "page", ret_dat)
	} else {
		fmt.Fprint(w, "Элемент не найден")
	}
}

func treeHendlerLR(w http.ResponseWriter, r *http.Request) {
	bdTree.RoundTreeLR(bdTreeArr)
	tree_status = true
	http.Redirect(w, r, "/page/0", 302)
}

func treeHendlerTD(w http.ResponseWriter, r *http.Request) {
	bdTree.RoundTreeTD(bdTreeArr)
	tree_status = true
	http.Redirect(w, r, "/page/0", 302)
}

func arrHendler(w http.ResponseWriter, r *http.Request) {
	tree_status = false
	http.Redirect(w, r, "/page/0", 302)
}

func codeHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("html/code.html")
	if err != nil {
		fmt.Fprint(w, "error: code.html")
		fmt.Fprint(w, "err: ", err)
		return
	}
	t.ExecuteTemplate(w, "code", dict_code)
}

func Init() {
	fmt.Println("Init")
	bd = base.Read()
	bdSpis.Init()
	for i := 0; i < numb_elem_bd; i++ {
		bdSpis.Push(bd[i])
	}
	fmt.Println("init AVL-tree")
	for i := range bd {
		bdTree.Push(bd[i].ConvertStructToString())
	}
	bdTreeArr = make([]base.RecordString, numb_elem_bd)
	bdByte := base.ReadByteFile()
	dict_code = base.InCode(bdByte)
	http.HandleFunc("/", indexHendler)
	http.HandleFunc("/page/", pageHendler)
	http.HandleFunc("/page/sort", sortHendler)
	http.HandleFunc("/search/", searchHendler)
	http.HandleFunc("/treeLR", treeHendlerLR)
	http.HandleFunc("/treeTD", treeHendlerTD)
	http.HandleFunc("/arr", arrHendler)
	http.HandleFunc("/code", codeHendler)
	http.Handle("/resourse/", http.StripPrefix("/resourse/", http.FileServer(http.Dir("./resourse/"))))
	fmt.Println("Listen port 9001")
	err := http.ListenAndServe(":9001", nil)
	if err != nil {
		fmt.Println("error listen: ", err)
	}
}
