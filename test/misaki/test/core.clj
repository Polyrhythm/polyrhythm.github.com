(ns misaki.test.core
  (require
    [misaki [core   :refer :all]
            [config :refer :all]
            [tester :refer :all]]
    [misaki.util.sequence :refer [find-first]]
    [clojure.test    :refer :all]
    [clojure.java.io :as io]))

(set-base-dir! "test/files/core/")

(deftest skip-compile?-test
  (testing "symbol or (symbol? (:status %)) is skip"
    (are [x y] (= x y)
      true (#'misaki.core/skip-compile? 'skip)
      true (#'misaki.core/skip-compile? {:status 'skip})))

  (testing "invalid"
    (are [x y] (= x y)
      false (#'misaki.core/skip-compile? 1)
      false (#'misaki.core/skip-compile? "str")
      false (#'misaki.core/skip-compile? {:status true})))

  (testing "iregular"
    ; if additional options exists, return false
    (are [x y] (= x y)
      false (#'misaki.core/skip-compile? {:status 'skip :stop-compile? true})
      false (#'misaki.core/skip-compile? {:status 'skip :all-compile? true}))))

;; call-compiler-fn
(deftest* call-compiler-fn-test
  (testing "single compiler"
    (bind-config [:compiler {'-extension #(list :clj)}]
      (is (= [:clj] (#'misaki.core/call-compiler-fn :-extension)))))

  (testing "multiple compiler"
    (bind-config [:compiler [{'-extension #(list :txt)}
                             {'-extension #(list :clj)}]]
      (are [x y] (= x y)
        [:txt :clj] (#'misaki.core/call-compiler-fn :-extension)
        [:js]       (#'misaki.core/call-compiler-fn {'-extension #(list :js)} :-extension))))

  (testing "_config.clj (default and cljs compiler)"
    (is (= [:clj :cljs] (#'misaki.core/call-compiler-fn :-extension)))))


;; get-watch-file-extensions
(deftest* get-watch-file-extensions-test
  (testing "single compiler"
    (bind-config [:compiler {'-extension #(list :clj)}]
      (is (= [:clj] (get-watch-file-extensions)))))

  (testing "normalized extentions"
    (bind-config [:compiler {'-extension #(list "clj" "*.txt")}]
      (is (= [:clj :txt] (get-watch-file-extensions)))))

  (testing "multiple compiler"
    (bind-config [:compiler [{'-extension #(list :clj)}
                             {'-extension #(list :txt)}]]
      (is (= [:clj :txt] (get-watch-file-extensions)))))

  (testing "multiple compiler(duplicated extention)"
    (bind-config [:compiler [{'-extension #(list :clj :txt)}
                             {'-extension #(list :txt)}]]
      (is (= [:clj :txt] (get-watch-file-extensions)))))

  (testing "_config.clj (default and cljs compiler)"
    (is (= [:clj :cljs] (get-watch-file-extensions)))))

;; get-template-files
(deftest* get-template-files-test
  (testing "default template directory"
    (let [tmpls (get-template-files)]
      (is (find-first #(= "index.html.clj" (.getName %)) tmpls))
      (is (= 6 (count tmpls)))))

  (testing "find from specified directory"
    (let [tmpls (get-template-files :dir (:post-dir *config*))]
      (is (= 3 (count tmpls)))
      (is (find-first #(= "2000.01.01-foo.html.clj" (.getName %)) tmpls))
      (is (find-first #(= "2011.01.01-foo.html.clj" (.getName %)) tmpls))
      (is (find-first #(= "2022.02.02-bar.html.clj" (.getName %)) tmpls))))

  (testing "not matched directory"
    (is (empty? (get-template-files :dir "not_existing_directory"))))

  (testing "all extensions"
    (bind-config [:compiler {'-extension #(list :*)}]
      (let [tmpls (get-template-files)]
        (is (= 7 (count tmpls)))
        (is (find-first #(= "favicon.ico" (.getName %)) tmpls)))))

  (testing "multiple compiler"
    (bind-config [:compiler [{'-extension #(list :ico)}
                             {'-extension #(list :cljs)}]]
      (let [tmpls (get-template-files)]
        (is (= 2 (count tmpls)))
        (is (find-first #(= "favicon.ico" (.getName %)) tmpls))
        (is (find-first #(= "hello.cljs" (.getName %)) tmpls))))))


;; get-post-files
(deftest* get-post-files-test
  (testing "without sort"
    (bind-config [:posts-per-page nil]
      (let [files (get-post-files)]
        (is (= 3 (count files)))
        (is (find-first #(= "2000.01.01-foo.html.clj" (.getName %)) files))
        (is (find-first #(= "2011.01.01-foo.html.clj" (.getName %)) files))
        (is (find-first #(= "2022.02.02-bar.html.clj" (.getName %)) files)))))

  (testing "with sort"
    (bind-config [:post-sort-type :date-desc
                  :posts-per-page nil]
      (let [[a b c :as files] (get-post-files :sort? true)]
        (are [x y] (= x y)
          3 (count files)
          "2022.02.02-bar.html.clj" (.getName a)
          "2011.01.01-foo.html.clj" (.getName b)
          "2000.01.01-foo.html.clj" (.getName c)))))

  (testing "with posts-per-page"
    (bind-config [:post-sort-type :date
                  :posts-per-page 1]
      (binding [*page-index* 0]
        (let [files (get-post-files :sort? true)]
          (is (= 1 (count files)))
          (is (= "2000.01.01-foo.html.clj" (.getName (first files))))))
      (binding [*page-index* 1]
        (let [files (get-post-files :sort? true)]
          (is (= 1 (count files)))
          (is (= "2011.01.01-foo.html.clj" (.getName (first files))))))
      (binding [*page-index* 2]
        (let [files (get-post-files :sort? true)]
          (is (= 1 (count files)))
          (is (= "2022.02.02-bar.html.clj" (.getName (first files))))))
      (binding [*page-index* 3]
        (is (zero? (count (get-post-files :sort? true)))))))

  (testing "with all? option"
    (is (= 3 (count (get-post-files))))
    (bind-config [:posts-per-page 1]
      (is (= 1 (count (get-post-files))))
      (is (= 3 (count (get-post-files :all? true)))))))


;; update-config
(deftest* update-config-test
  (testing "default single compiler"
    (bind-config [:compiler {'-config #(merge {:foo "bar"} %)}]
      (let [c (update-config)]
        (are [x y] (= x y)
          (base-path "public/") (:public-dir c)
          "bar"                 (:foo c)))))

  (testing "specify compiler"
    (let [c (update-config {'-config #(assoc % :foo "bar")})]
      (are [x y] (= x y)
        (base-path "public/") (:public-dir c)
        "bar"                 (:foo c))))

  (testing "multiple compilers"
    (bind-config [:compiler [{'-config #(assoc % :foo "bar")}
                             {'-config #(assoc % :bar "baz")}]]
      (let [c (update-config)]
        (are [x y] (= x y)
          true (sequential? c)
          2    (count c)
          (base-path "public/") (:public-dir (first c))
          "bar"                 (:foo (first c))
          (base-path "public/") (:public-dir (second c))
          "baz"                 (:bar (second c)))))))

;; process-compile-result
(deftest* process-compile-result-test
  (let [filename "bar.txt"]
    (testing "string result"
      (is (process-compile-result "foo" filename))
      (let [f (io/file (public-path filename))]
        (is (.exists f))
        (is (= "foo" (slurp f)))
        (.delete f)))

    (testing "boolean result"
      (is (process-compile-result true filename))
      (is (not (process-compile-result false filename)))
      (let [f (io/file (str (:public-dir *config*) filename))]
        (is (not (.exists f)))))

    (testing "detailed result"
      (are [x y] (= x y)
        false (process-compile-result {} filename)
        false (process-compile-result {:status false} filename)
        true  (process-compile-result {:status true} filename))

      (is (process-compile-result
            {:status true :filename "a.txt" :body "foo"} ""))
      (let [f (io/file (public-path "a.txt"))]
        (is (.exists f))
        (is (= "foo" (slurp f)))
        (.delete f))

      (is (not (process-compile-result
                 {:status false :body "bar"} "b.txt")))
      (let [f (io/file (public-path "b.txt"))]
        (is (.exists f))
        (is (= "bar" (slurp f)))
        (.delete f)))))

;;; handleable-compiler?
(deftest handleable-compiler?-test
  (let [default (load-compiler-publics "default")
        copy    (load-compiler-publics "copy")]
    (are [x y] (= x y)
      true  (handleable-compiler? default (io/file "foo.clj"))
      false (handleable-compiler? default (io/file "foo"))
      true  (handleable-compiler? copy    (io/file "foo.clj"))
      true  (handleable-compiler? copy    (io/file "foo")))))

;;; compile*
(deftest* compile*-test
  (testing "single compiler"
    (let [[p c] (compile* (template-file "index.html.clj"))]
      (is (not (false? p)))
      (is (not (false? c))))
    (let [file (public-file "index.html")]
      (is (.exists file))
      (.delete file)))

  (bind-config [:compiler [(load-compiler-publics "default")
                           (load-compiler-publics "copy")]]
    (testing "multiple compilers: first compiler is used"
      (let [[p c] (compile* (template-file "index.html.clj"))]
        (is (not (false? p)))
        (is (not (false? c))))
      (let [file (public-file "index.html")]
        (is (.exists file))
        (.delete file)))

    (testing "multiple compilers: second compiler is used"
      (let [[p c] (compile* (template-file "favicon.ico"))]
        (is (not (false? p)))
        (is (not (false? c))))
      (let [file (public-file "favicon.ico")]
        (is (.exists file))
        (.delete file))))

  (testing "all skip error test"
    (let [[p c] (compile* (template-file "favicon.ico"))]
      (is (true? p))
      (is (= 'skip c)))))

;;; call-index-compile
(deftest* call-index-compile-test
  (testing "call with default config"
    (bind-config [:posts-per-page 1]
      (test-index-compile (template-file "index.html.clj")))
    (let [p1 (public-file "index.html")
          p2 (public-file "page2/index.html")
          p3 (public-file "page3/index.html")]
      (is (.exists p1))
      (is (.exists p2))
      (is (.exists p3))
      (.delete p1)
      (.delete p2)
      (.delete p3)
      (.delete (public-file "page2"))
      (.delete (public-file "page3"))))
  (testing "call with optional-config"
    (bind-config [:posts-per-page 1
                  :index-template-regexp #"^pagetest"]
      (test-index-compile
        {:index-template-regexp (:index-template-regexp *config*)}
        (template-file "pagetest.html.clj")))
    (let [p1 (public-file "pagetest.html")
          p2 (public-file "page2/pagetest.html")
          p3 (public-file "page3/pagetest.html")]
      (is (.exists p1))
      (is (.exists p2))
      (is (.exists p3))
      (.delete p1)
      (.delete p2)
      (.delete p3)
      (.delete (public-file "page2"))
      (.delete (public-file "page3")))))

