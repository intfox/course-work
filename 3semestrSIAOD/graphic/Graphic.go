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

type RetDat struct {
	RetBD           []base.RecordString
	RetPage         int
	RetNumb_of_page int
}

func indexHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("html/index.html")
	if err != nil {
		fmt.Fprint(w, "error: index.html")
	}
	t.ExecuteTemplate(w, "index", nil)
}

func pageHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("html/page.html")
	if err != nil {
		fmt.Fprint(w, "error: page.html")
	}
	//	bd := base.Read()
	var ret_dat RetDat
	ret_dat.RetPage, err = strconv.Atoi(r.URL.Path[len("/page/"):])
	ret_dat.RetNumb_of_page = len(bd)/numb_elem_table - 1
	if ret_dat.RetPage < len(bd)/numb_elem_table && ret_dat.RetPage > -1 {
		ret_dat.RetBD = make([]base.RecordString, numb_elem_table)
		for i := 0; i < numb_elem_table && i+(ret_dat.RetPage*numb_elem_table) < len(bd); i++ {
			ret_dat.RetBD[i] = bd[i+(ret_dat.RetPage*numb_elem_table)].ConvertStructToString()
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
	for i := 0; i < 20; i++ {
		fmt.Println("bd[", i, "]= ", bd_string[i].Title)
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
		fmt.Println("RetBd:", ret_dat.RetBD)
		t.ExecuteTemplate(w, "page", ret_dat)
	} else {
		fmt.Fprint(w, "Элемент не найден")
	}
}

func Init() {
	bd = base.Read()
	bdSpis.Init()
	for i := 0; i < numb_elem_bd; i++ {
		bdSpis.Push(bd[i])
	}
	http.HandleFunc("/", indexHendler)
	http.HandleFunc("/page/", pageHendler)
	http.HandleFunc("/page/sort", sortHendler)
	http.HandleFunc("/search/", searchHendler)
	//	http.HandleFunc("/page/search/", searchHendler)
	http.Handle("/resourse/", http.StripPrefix("/resourse/", http.FileServer(http.Dir("./resourse/"))))
	err := http.ListenAndServe(":80", nil)
	if err != nil {
		fmt.Println("error listen: ", err)
	}
}
