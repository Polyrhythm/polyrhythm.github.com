(ns misaki.test.config
  (:require
    [misaki [config   :refer :all]
            [tester   :refer :all]]
    [misaki.util.file :refer [normalize-path]]
    [clj-time.core    :refer [date-time year month day]]
    [clojure.test     :refer :all]
    [clojure.java.io  :as io])
  (:import [java.io FileNotFoundException]))

(defn dummy0 [] ())
(defn dummy1 [a] ())
(defn dummy2 [a b] ())
(defn dummy3 ([a] ()) ([a b c] ()))
(defn dummy4 [a & b] ())

(set-base-dir! "test/files/config/")

; read-config
(deftest* read-config-test
  (testing "normal pattern"
    (let [config (read-config)]
      (are [x y] (= x y)
        "public/"   (:public-dir config)
        "template/" (:template-dir config)
        "posts/"    (:post-dir config)
        "layouts/"  (:layout-dir config))))

  (testing "error pattern"
    (binding [*base-dir* "/foo/bar/"]
      (is (thrown? FileNotFoundException (read-config)))))

  (testing "specify default value"
    (binding [*base-dir* "/foo/bar/"]
      (is (= {} (read-config {}))))))

;;; load-compiler-publics-test
(deftest* load-compiler-publics-test
  (testing "default compiler"
    (let [c (load-compiler-publics "default")]
      (are [x y] (= x y)
        true      (every? #(contains? c %) '(-extension -config -compile))
        "default" (:name c))))

  ;(testing "wrong named compiler(read dummy_default)"
  ;  (binding [*compiler-namespace-format* "misaki.test.compiler.dummy_{{name}}"]
  ;    (let [c (load-compiler-publics "different")]
  ;      (are [x y] (= x y)
  ;        true      (every? #(contains? c %) '(-extension -config -compile))
  ;        "default" (:name c)))))

  (testing "unknown compiler(read default)"
    (let [c (load-compiler-publics "foo")]
      (are [x y] (= x y)
        true      (every? #(contains? c %) '(-extension -config -compile))
        "default" (:name c))))

  (testing "multiple compilers"
    (let [[c1 c2 :as cs] (load-compiler-publics ["default" "demo"])]
      (are [x y] (= x y)
        2         (count cs)
        "default" (:name c1)
        "demo"    (:name c2)
        true      (every? #(contains? c1 %) '(-extension -config -compile))
        true      (every? #(contains? c2 %) '(-extension -config -compile))))))

;;; get-date-from-file
(deftest* get-date-from-file-test
  ; test config: #"(\d{4})[.](\d{1,2})[.](\d{1,2})[-_](.+)$"
  (testing "valid date"
    (let [date (get-date-from-file (io/file "2000.11.22-dummy.clj"))]
      (are [x y] (= x y)
        2000 (year date)
        11   (month date)
        22   (day date))))

  (testing "invalid date"
    (are [filename] (nil? (get-date-from-file (io/file filename)))
      "2000.11.xx.dummy.clj"
      "2000.xx.22-dummy.clj"
      "xxxx.11.22-dummy.clj"
      "2000.11.dummy.clj"
      "2000.dummy.clj"
      "dummy.clj"
      ""
      nil)))

;;; remove-date-from-name
(deftest* remove-date-from-name-test
  (is (= "dummy.clj" (remove-date-from-name "2000.11.22-dummy.clj"))))

;;; sort-type->sort-fn
(deftest* sort-type->sort-fn-test
  (let [[c b a :as sample-posts] (list (io/file "2011.01.01-ccc.html.clj")
                                       (io/file "2022.02.02-bbb.html.clj")
                                       (io/file "2033.03.03-aaa.html.clj"))]
    (testing "sort by date"
      (bind-config [:post-sort-type :date]
        (let [[c* b* a*] ((sort-type->sort-fn) sample-posts)]
          (is (= c c*)) (is (= b b*)) (is (= a a*)))))

    (testing "sort by date-desc"
      (bind-config [:post-sort-type :date-desc]
        (let [[a* b* c*] ((sort-type->sort-fn) sample-posts)]
          (is (= c c*)) (is (= b b*)) (is (= a a*)))))

    (testing "sort by unknown type => sort by date"
      (bind-config [:post-sort-type :unknown]
        (let [[c* b* a*] ((sort-type->sort-fn) sample-posts)]
          (is (= c c*)) (is (= b b*)) (is (= a a*))))))

  (let [[c b a :as sample-posts] (list (io/file "ccc.html.clj")
                                       (io/file "bbb.html.clj")
                                       (io/file "aaa.html.clj"))]
    (testing "sort by name"
      (bind-config [:post-sort-type :name]
        (let [[a* b* c*] ((sort-type->sort-fn) sample-posts)]
          (is (= c c*)) (is (= b b*)) (is (= a a*)))))

    (testing "sort by name-desc"
      (bind-config [:post-sort-type :name-desc]
        (let [[c* b* a*] ((sort-type->sort-fn) sample-posts)]
          (is (= c c*)) (is (= b b*)) (is (= a a*)))))))

;;; make-post-output-filename
(deftest* make-post-output-filename-test
  ; test config: "{{year}}-{{month}}/{{filename}}"
  (testing "filename with date"
    (let [file (io/file "2000.11.22-dummy.html")]
      (is (= "2000-11/dummy.html" (#'misaki.config/make-post-output-filename file)))))

  (testing "filename without date"
    (are [x y] (= x (#'misaki.config/make-post-output-filename (io/file y)))
      "-/foo.html"       "foo.html"
      "-/01.foo.html"    "01.foo.html"
      "-/01.02.foo.html" "01.02.foo.html")))

(deftest* make-index-output-filename
  (let [f #(apply #'misaki.config/make-index-output-filename %&)]
    (bind-config [:page-filename-format "{{page}}/{{filename}}"]
      (binding [*page-index* 0]
        (are [x y] (= x y)
          "foo.bar"   (f (io/file "foo.bar"))
          "foo.bar"   (f (io/file "foo.bar") :page 0)
          "2/foo.bar" (f (io/file "foo.bar") :page 1)))
      (binding [*page-index* 1]
        (are [x y] (= x y)
          "2/foo.bar" (f (io/file "foo.bar"))
          "foo.bar"   (f (io/file "foo.bar") :page 0)
          "2/foo.bar" (f (io/file "foo.bar") :page 1))))

    (bind-config [:page-filename-format "{{name}}{{page}}{{ext}}"]
      (binding [*page-index* 0]
        (are [x y] (= x y)
          "foo.bar"      (f (io/file "foo.bar"))
          "foo.bar"      (f (io/file "foo.bar") :page 0)
          "foo2.bar"     (f (io/file "foo.bar") :page 1)
          "foo.bar2.baz" (f (io/file "foo.bar.baz") :page 1)))
      (binding [*page-index* 1]
        (are [x y] (= x y)
          "foo2.bar"     (f (io/file "foo.bar"))
          "foo.bar"      (f (io/file "foo.bar") :page 0)
          "foo2.bar"     (f (io/file "foo.bar") :page 1)
          "foo.bar2.baz" (f (io/file "foo.bar.baz") :page 1))))
    )
  )

;;; make-output-filename
(deftest* make-output-filename-test
  (testing "normal template file"
    (are [x y] (= x y)
      "foo.html" (make-output-filename (io/file "foo.html"))
      "foo/bar.html" (make-output-filename (io/file "foo/bar.html"))))

  (testing "post file"
    (is (= "2000-11/dummy.html"
           (make-output-filename
             (post-file "2000.11.22-dummy.html")))))

  (testing "index file without pagenation"
    (binding [*config* (assoc *config* :posts-per-page nil
                                       :index-template-regexp #"^index")]
      (is (= "index.html" (make-output-filename (io/file "index.html"))))))

  (testing "index file with pagenation"
    (let [file (io/file "index.html")]
      (binding [*config* (assoc *config* :posts-per-page 1
                                         :index-template-regexp #"^index")]
        (binding [*page-index* 0]
          (is (= "index.html" (make-output-filename file))))
        (binding [*page-index* 1]
          (is (= "page2/index.html" (make-output-filename file))))
        (binding [*page-index* 2]
          (is (= "page3/index.html" (make-output-filename file)))))))

  (testing "index file with specific page number"
    (let [file (io/file "index.html")]
      (bind-config [:posts-per-page 1, :index-template-regexp #"^index"]
        (is (= "index.html" (make-output-filename file :page 0)))
        (is (= "page2/index.html" (make-output-filename file :page 1)))
        (is (= "page3/index.html" (make-output-filename file :page 2)))))))

;;; make-output-url
(deftest* make-output-url-test
  (let [file (io/file "foo.html")]
    (testing "default url-base"
      (is (= "/foo.html" (make-output-url file))))

    (testing "custom url-base"
      (bind-config [:url-base (normalize-path "/bar/baz")]
        (is (= "/bar/baz/foo.html" (make-output-url file))))))

  (let [file (io/file "index.html")]
    (testing "index file without pagenation"
      (binding [*config* (assoc *config* :posts-per-page nil
                                         :index-template-regexp #"^index")]
        (is (= "/index.html" (make-output-url file)))))

    (testing "index file with pagenation"
      (binding [*config* (assoc *config* :posts-per-page 1
                                         :index-template-regexp #"^index")]
        (binding [*page-index* 0]
          (is (= "/index.html" (make-output-url file))))
        (binding [*page-index* 1]
          (is (= "/page2/index.html" (make-output-url file))))
        (binding [*page-index* 2]
          (is (= "/page3/index.html" (make-output-url file)))))))

  (testing "index file with specific page number"
    (let [file (io/file "index.html")]
      (bind-config [:posts-per-page 1, :index-template-regexp #"^index"]
        (is (= "/index.html" (make-output-url file :page 0)))
        (is (= "/page2/index.html" (make-output-url file :page 1)))
        (is (= "/page3/index.html" (make-output-url file :page 2)))))))

;;; absolute-path
(deftest* absolute-path-test
  (testing "should be added url-base"
    (with-config
      (are [x y] (= x (absolute-path y))
        "/a.htm"                  "a.htm"
        "/bar/a.htm"              "bar/a.htm"
        "/a.htm"                  "/a.htm"
        "/bar/a.htm"              "/bar/a.htm"
        "http://localhost/a.htm"  "http://localhost/a.htm"
        "https://localhost/a.htm" "https://localhost/a.htm")

      (bind-config [:url-base "/foo/"]
        (are [x y] (= x (absolute-path y))
          "/foo/a.htm"     "a.htm"
          "/foo/a.htm"     "/a.htm"
          "/foo/bar/a.htm" "/bar/a.htm"
          "/foo/bar/a.htm" "bar/a.htm"))))

  (testing "should not be added url-base if specified path has url-base"
    (are [x y] (= x (absolute-path y))
      "http://foo.bar"  "http://foo.bar"
      "https://foo.bar" "https://foo.bar"))

  (testing "should not be added url-base if specified path has url-base"
    (bind-config [:url-base "/foo/"]
      (are [x y] (= x (absolute-path y))
        "/foo/bar"     "/foo/bar"
        "/foo/bar/baz" "/foo/bar/baz"))
    (bind-config [:url-base "/foo"]
      (are [x y] (= x (absolute-path y))
        "/foo/bar"     "/foo/bar"
        "/foo/bar/baz" "/foo/bar/baz")))

  (testing "should not be added url-base if specified path is relative"
    (bind-config [:url-base "/foo/"]
      (are [x y] (= x (absolute-path y))
        "./foo"     "./foo"
        "./foo/bar" "./foo/bar")))

  (testing "should not be added url-base if specified path is relative"
    (bind-config [:url-base "/foo/"]
      (are [x y] (= x (absolute-path y))
        "#foo"     "#foo"))))

(deftest* get-page-posts-test
  (let [ls '(1 2 3)]
    (bind-config [:posts-per-page 1]
      (binding [*page-index* 0]
        (is (= '(1) (get-page-posts ls))))
      (binding [*page-index* 1]
        (is (= '(2) (get-page-posts ls))))
      (binding [*page-index* 2]
        (is (= '(3) (get-page-posts ls))))
      (binding [*page-index* 3]
        (is (= () (get-page-posts ls)))))

    (bind-config [:posts-per-page 2]
      (binding [*page-index* 0]
        (is (= '(1 2) (get-page-posts ls))))
      (binding [*page-index* 1]
        (is (= '(3) (get-page-posts ls))))
      (binding [*page-index* 2]
        (is (= () (get-page-posts ls)))))))
