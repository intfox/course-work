package graphic

import (
	"fmt"
	"html/template"
	"net/http"
	"strconv"

	"github.com/intfox/course-work/3semestrSIAOD/base"
)

var bd []base.Record
var bdSpis base.Spis
var numb_elem_bd = 4000
var numb_elem_table = 20
var pageNotFound, err = template.ParseFiles("graphic/pageNotFound.html")

type RetDat struct {
	RetBD           []base.RecordString
	RetPage         int
	RetNumb_of_page int
}

func indexHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("graphic/index.html")
	if err != nil {
		fmt.Fprint(w, "error: index.html")
	}
	t.ExecuteTemplate(w, "index", nil)
}

func pageHendler(w http.ResponseWriter, r *http.Request) {
	t, err := template.ParseFiles("graphic/page.html")
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
}

//func searchHendler(w http.ResponseWriter, r *http.Request) {
//	strconv.Atoi(r.URL.Path[len("/page/"):])
//	for i := range bd {
//		if
//	}
//}

func Init() {
	bd = base.Read()
	bdSpis.Init()
	for i := 0; i < numb_elem_bd; i++ {
		bdSpis.Push(bd[i])
	}
	http.HandleFunc("/", indexHendler)
	http.HandleFunc("/page/", pageHendler)
	http.HandleFunc("/page/sort", sortHendler)
	//	http.HandleFunc("/page/search/", searchHendler)
	http.Handle("/resourse/", http.StripPrefix("/resourse/", http.FileServer(http.Dir("./resourse/"))))
	err := http.ListenAndServe(":80", nil)
	if err != nil {
		fmt.Println("error listen: ", err)
	}
}
