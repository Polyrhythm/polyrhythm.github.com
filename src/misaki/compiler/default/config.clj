(ns misaki.compiler.default.config
  "Default Compiler's Configuration Manager"
  (:require
    [misaki.util [file :refer :all]
                 [string :refer :all]
                 [sequence :refer :all]]
    [misaki.config    :refer [*config*] :as cnf]
    [clj-time.core    :refer [date-time year month day]]
    [clojure.string   :as str]
    [clojure.java.io  :as io])
  (:import [java.io File FileNotFoundException]))

;; ## Default Value

(def POST_FILENAME_REGEXP
  "Default regexp to parse post filename."
  #"(\d{4})[-_](\d{1,2})[-_](\d{1,2})[-_](.+)$")
(def POST_OUTPUT_NAME_FORMAT
  "Default format to generage post output filename."
  "{{year}}/{{month}}/{{filename}}")

(def ^:dynamic *site* {})

;; ## Utilities
(defn remove-clj-extension [filename]
  (if (has-extension? :clj filename)
    (remove-last-extension filename)
    filename))

; =plugin-config
(defn plugin-config
  "Configure for this compiler.

  * `:layout-dir`: Layout file directory.
  * `:tag-layout`: Layout file for tag page.
  * `:detailed-log`: Flag to print detailed log.
  * `:post-sort-type`: Sort type of posts.
  "
  [config]
  {:pre  [(map? config)]
   :post [#(map? %)]}
  (let [{:keys [template-dir public-dir url-base detailed-log]} config
        layout   (path template-dir (:layout-dir config))]
    (assoc
      config
      :layout-dir layout
      :tag-layout   (str layout (:tag-layout config) ".clj")
      :detailed-log (:detailed-log config false)
      :post-sort-type       (:post-sort-type config :date-desc))))

; =with-config
(defmacro with-config
  "Bind local config to `*config*`"
  [config & body]
  `(binding [*config* ~config] ~@body))

;; ## File Cheker

; =layout-file?
(defn layout-file?
  "Check whether file is layout file or not."
  [#^File file]
  {:pre [(file? file)]}
  (str-contains? (.getAbsolutePath file) (:layout-dir *config*)))

;; ## Converter

; FIXME
(defn sort-type->sort-fn
  "Convert sort-type keyword to sort function."
  []
  (case (:post-sort-type *config*)
    :title      (partial sort-alphabetically #(:title %))
    :title-desc (partial sort-alphabetically :desc #(:title %))
    :date       (partial sort-by-date :inc :date)
    :name       (partial sort-alphabetically #(.getName (:file %)))
    :date-desc  (partial sort-by-date :desc :date)
    :name-desc  (partial sort-alphabetically :desc #(.getName (:file %)))
    (cnf/sort-type->sort-fn)))

;; ## Filename Generator

; =make-tag-output-filename
(defn make-tag-output-filename
  "Make tag output filename from tag name."
  [#^String tag-name]
  {:pre [(string? tag-name)]}
  (path (:tag-out-dir *config*) (str tag-name ".html")))

; =make-template-output-filename
(defmulti make-template-output-filename
  "Make template output filename from template name."
  class)

(defmethod make-template-output-filename String
  [tmpl-name]
  (make-template-output-filename (cnf/template-name->file tmpl-name)))

(defmethod make-template-output-filename File
  [file]
  (remove-clj-extension
    (cnf/make-output-filename file)))

; =make-layout-filename
(defn make-layout-filename
  "Make layout filename from layout name(String)."
  [#^String layout-name]
  {:pre [(string? layout-name)]}
  (path (:layout-dir *config*) (str layout-name ".clj")))

;; ## URL Generator

; =make-post-url
(defn make-post-url
  "Make post url from java.io.File"
  [#^File file]
  {:pre [(file? file)]}
  (remove-clj-extension
    (cnf/make-output-url file)))

; =make-tag-url
(defn make-tag-url
  "Make tag url form tag string."
  [#^String tag-name]
  {:pre [(string? tag-name)]}

  (path (:url-base *config*)
                (make-tag-output-filename tag-name)))


