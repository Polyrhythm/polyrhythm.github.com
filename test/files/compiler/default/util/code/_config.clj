{
 ;; directory setting
 :public-dir   "public/"
 :template-dir "template/"
 :post-dir     "posts/"
 :layout-dir   "layouts/"

 ;; default site data
 :site {:default-title "default title"}

 :tag-layout "tag.test"
 :tag-out-dir "tag/"

 ;; templates which compiled with post data
 :compile-with-post ["index.html.clj"]

 :post-filename-regexp #"(\d{4})[.](\d{1,2})[.](\d{1,2})[-_](.+)$"
 :post-filename-format "$(year)-$(month)/$(filename)"

 ;; clojurescript compile options
 ;; src-dir base is `:template-dir`
 ;; output-dir base is `:public-dir`
 :cljs {:optimizations :advanced}

 ;; highlight setting
 :code-highlight {:CLJ "lang-clj"}

 :page-filename-format "page$(page)/$(filename)"

 :compiler ["default" "cljs"]
 }

