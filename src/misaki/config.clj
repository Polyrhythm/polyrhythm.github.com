(ns misaki.config
  "Configuration Manager"
  (:require
    [misaki.util [file     :refer :all]
                 [string   :refer :all]
                 [sequence :refer :all]]
    [clj-time.core         :refer [date-time year month day]]
    [text-decoration.core  :refer [cyan red bold]]
    [clojure.string        :as str]
    [clojure.java.io       :as io])
  (:import [java.io File FileNotFoundException]))

;; ## Default Value

(def PORT     "Default dev server port." 8080)
(def LANGUAGE "Default language."        "en")
(def COMPILER "Default compiler."        "default")
(def POST_FILENAME_REGEXP
  "Default regexp to parse post filename."
  #"(\d{4})[-_](\d{1,2})[-_](\d{1,2})[-_](.+)$")
(def POST_OUTPUT_NAME_FORMAT
  "Default format to generage post output filename."
  "$(year)/$(month)/$(filename)")
(def INDEX_TEMPLATE_REGEXP
  "Default regexp to detect index template file."
  #"^index\.")
(def PAGE_FILENAME_FORMAT
  "Default format to generate page output filename."
  "page$(page)/$(filename)")
(def NOTIFY_SETTING
  {:fixed-title  "$(filename)"
   :fixed        "FIXED"
   :failed-title "$(filename) : $(line)"
   :failed       "$(message)"})

;; ## Declarations

(def ^:dynamic *base-dir*
  "Blog base directory."
  "")
(def ^:dynamic *config-file*
  "Config filename.
  Default filename is '_config.clj'."
  "_config.clj")
(def ^:dynamic *config*
  "Current config map."
  {})
(def ^:dynamic *page-index*
  "Page index. First page is 0."
  0)
(def ^:dynamic *print-stack-trace?*
  "Flag for printing stack trace."
  true)

(def ^:dynamic *compiler-namespace-format*
  "Namespace format for misaki compiler."
  "misaki.compiler.{{name}}.core")

;; ## Config Data Wrapper

; =read-config
(defn read-config
  "Load `*config-file*` from `*base-dir*`."
  ([]
   (read-string (slurp (str *base-dir* *config-file*))))
  ([default-value]
   (try (read-config)
        (catch FileNotFoundException e
          default-value))))

; =load-compiler-publics
(defn load-compiler-publics
  "Load specified compiler's public method map.
  Compiler's namespace must be **misaki.compiler.FOO.core**.
  "
  [name]
  {:pre [(or (string? name) (sequential? name))]}
  (if (sequential? name)
    (map load-compiler-publics name)
    (let [sym (symbol (render *compiler-namespace-format* {:name name}))]
      (try
        (require sym)
        (if-let [target-ns (find-ns sym)]
          (assoc (ns-publics target-ns) :name name)
          (load-compiler-publics "default"))
        (catch FileNotFoundException e
          (println (red (str " * Compiler \"" name "\" is not found."
                             " Default compiler is used.")))
          (load-compiler-publics "default"))))))

; =make-basic-config-map
(defn make-basic-config-map
  "Make basic config to pass plugin's configuration."
  []
  (let [config       (read-config)
        template-dir (path *base-dir* (:template-dir config))]
    (assoc
      config
      :public-dir     (path *base-dir* (:public-dir config))
      :template-dir   template-dir
      :post-dir       (if-let [post-dir (:post-dir config)]
                        (path template-dir post-dir))
      :post-sort-type (:post-sort-type config :date-desc)
      :port           (:port config PORT)
      :lang           (:lang config LANGUAGE)
      :site           (:site config {})
      :index-name     (:index-name config "")
      :url-base       (normalize-path (:url-base config "/"))
      :compiler       (load-compiler-publics (:compiler config COMPILER))
      :compile-with-post     (:compile-with-post config ())
      :post-filename-regexp  (:post-filename-regexp config POST_FILENAME_REGEXP)
      :post-filename-format  (:post-filename-format config POST_OUTPUT_NAME_FORMAT)
      :index-template-regexp (:index-template-regexp config INDEX_TEMPLATE_REGEXP)
      :page-filename-format  (:page-filename-format config PAGE_FILENAME_FORMAT)
      :notify?              (:notify? config false)
      :notify-setting       (merge NOTIFY_SETTING (:notify-setting config)))))

; =with-config
(defmacro with-config
  "Declare config data, and wrap sexp body with them."
  [& body]
  `(binding [*config* (make-basic-config-map)]
     ~@body))

;; ## File Cheker

; =config-file?
(defn config-file?
  "Check whether file is config file or not."
  [#^File file]
  {:pre [(file? file)]}
  (= *config-file* (.getName file)))

; =post-file?
(defn post-file?
  "Check whether file is post file or not."
  [#^File file]
  {:pre [(file? file)]}
  (and (:post-dir *config*) (str-contains? (.getAbsolutePath file)
                                           (:post-dir *config*))))

; =index-file?
(defn index-file?
  "Check whether file is index file or not."
  [#^File file]
  (re-seq (:index-template-regexp *config*) (.getName file)))


;; ## Filename Date Utility

; =get-date-from-file
(defn get-date-from-file
  "Get date from file(java.io.File) with `(:post-filename-regexp *config*)`."
  [#^File post-file]
  {:pre [(or (nil? post-file) (file? post-file))]}
  (let [date-seq (some->> post-file (.getName)
                      (re-seq (:post-filename-regexp *config*))
                      nfirst drop-last)] ; last = filename
    (if (and date-seq (= 3 (count date-seq))
             (every? #(re-matches #"^[0-9]+$" %) date-seq))
      (apply date-time (map #(Integer/parseInt %) date-seq)))))

; =remove-date-from-name
(defn remove-date-from-name
  "Remove date string from filename(String)."
  [#^String filename]
  {:pre [(string? filename)]}
  (last (first (re-seq (:post-filename-regexp *config*) filename))))


;; ## Converter

; =sort-type->sort-fn
(defn sort-type->sort-fn
  "Convert sort-type keyword to sort function."
  []
  (case (:post-sort-type *config*)
    :date       (partial sort-by-date :inc get-date-from-file)
    :name       (partial sort-alphabetically #(.getName %))
    :date-desc  (partial sort-by-date :desc get-date-from-file)
    :name-desc  (partial sort-alphabetically :desc #(.getName %))
    ; default
    (partial sort-by-date :inc get-date-from-file)))

;; ## Filename Generator

; =get-index-filename
(defn get-index-filename
  "Get index filename string."
  []
  (path (:url-base *config*) (:index-name *config*)))

; =make-post-output-filename
(defn- make-post-output-filename
  "Make post output filename from java.io.File."
  [#^File file]
  {:pre [(file? file)]}
  (let [date     (get-date-from-file file)
        filename (if date (remove-date-from-name (.getName file))
                          (.getName file))]
    (render (:post-filename-format *config*)
            {:year     (some->> date year  str)
             :month    (some->> date month (format "%02d"))
             :day      (some->> date day   (format "%02d"))
             :filename filename})))

; =make-regular-output-filename
(defn- make-regular-output-filename
  "Make regular output filename from java.io.File."
  [#^File file]
  (let [path (.getPath file)
        len  (count (:template-dir *config*))]
    (if (.startsWith path (:template-dir *config*))
      (.substring path len)
      path)))

; =make-index-output-filename
(defn- make-index-output-filename
  "Make index output filename from java.io.File.

  Filename format is defined by `:page-filename-format`.
  Following variables can be used in `:page-filename-format`.
      {{filename}}: Filename
      {{page}}    : Page number (0..N)
      {{name}}    : Filename witout last extension
      {{ext}}     : Extension"
  [#^File file & {:keys [page] :or {page nil}}]
  (let [filename   (make-regular-output-filename file)
        page-index (or page *page-index*)]
    (if (not= 0 page-index)
      (render (:page-filename-format *config*)
              {:filename filename
               :page     (inc page-index)
               :name     (remove-last-extension filename)
               :ext      (str "." (get-last-extension filename))})
      filename)))

; =make-output-filename
(defn make-output-filename
  "Make output filename from java.io.File."
  [#^File file & {:keys [page] :or {page nil}}]
  {:pre [(file? file)]}
  (cond
    (post-file? file)  (make-post-output-filename file)
    (index-file? file) (make-index-output-filename file :page page)
    :else              (make-regular-output-filename file)))

; =make-output-url
(defn make-output-url
  "Make output url from java.io.File."
  [#^File file & {:keys [page] :or {page nil}}]
  {:pre [(file? file)]}
  (path (:url-base *config*) (make-output-filename file :page page)))

; =template-name->file
(defn template-name->file
  "Convert template name to java.io.File."
  [#^String tmpl-name]
  {:pre [(string? tmpl-name)]}
  (io/file (path (:template-dir *config*) tmpl-name)))

; =absolute-path
(defn absolute-path
  "Convert path to absolute with `(:url-base *config*)`

  With following pattern, this function does not convert path.
  * Starts with 'http' or 'https'
  * Starts with './' which represents relative path
  * Starts with '#' which represents jump link
  * Starts with `(:url-base *confit*)`"
  [path-str]
  {:pre [(string? path-str)]}
  (let [{url-base :url-base} *config*]
    (if (or (.startsWith path-str url-base)
            (re-seq #"^\./" path-str)
            (re-seq #"^#" path-str)
            (re-seq #"^https?://" path-str))
      path-str
      (path url-base path-str))))

; =public-path
(defn public-path
  "Return public path from specified filename."
  [filename]
  {:pre (string? filename)}
  (path (:public-dir *config*) filename))


;; ## Pagination

; =get-page-posts
(defn get-page-posts
  "Return posts at the *page-index*.
  If posts-per-page is not defined, return all posts."
  [posts]
  (if-let [ppp (:posts-per-page *config*)]
    (nth (partition-all ppp posts) *page-index* ())
    posts))

; =make-page-data
(defn make-page-data
  "Make pagination data from page numbers."
  [file page last-page]
  {:pre [(file? file) (number? page) (number? last-page)]}
  (let [next?    (< page (dec last-page))
        prev?    (> page 0)
        page-url #(make-output-url file :page %)]
    {:page      (inc page)
     :last-page last-page
     :next-page (if next? (page-url (inc page)))
     :prev-page (if prev? (page-url (dec page)))}))
