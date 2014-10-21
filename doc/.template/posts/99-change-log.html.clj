; @layout  post
; @title   Change log

(h2 "0.2.3 beta")
(p "2012-09-29")
(ul [(list "Add `prev`, `next` site option to access previous/next post data."
           (ul ["Add misaki.util.seqence/get-prev-next function"
                "Add misaki.compiler.default.html.core/prev-next-post-link function"]))
     (list "Support using multiple compiler at the same time."
           (ul ["See document: [Use multiple compilers](title: Switch Compiler#multiple_compilers)"]))
     "Add copy compiler to copy template file without any changes."
     "Support `:*` as compiler watching extension."
     (list "Add/Update utility functions"
           (ul [
                "Update misaki.util.sequence/find-first to handle lazy sequence."
                "Update misaki.util.file/has-extension? to handle all extension(*)."
                "Add misaki.util.file/normalize-extension."
                "Update misaki.compiler.default.html.core/heading to generate self heading link."
                "Update misaki.compiler.default.html.conv/post-title->url to handle #jump_position."
                ]))])
(p "2012-10-21")
(ul ["Update [develop compiler](title: Develop Compiler) document to use [misaki-compiler-template](https://github.com/liquidz/misaki-compiler-template)."])

(h2 "0.2.4 beta")
(p "2012-10-23")
(ul ["Fixed to compile templates under directories correctly. [GitHub Issue](https://github.com/liquidz/misaki/issues/26)"])

(h2 "0.2.6 beta")
(p "2013-02-16")
(ul ["Fix filename generation problem. [#28](https://github.com/liquidz/misaki/issues/28)"
     "Fix misaki.util.notify to only support GNTP. [#29](https://github.com/liquidz/misaki/issues/29)"
     "Add pagination method. [#31](https://github.com/liquidz/misaki/issues/31)"
     "Fixed some other bugs."])

(h2 "0.2.6.2 beta")
(p "2013-05-11")
(ul ["Fix `set-base-dir!` problem. [#32](https://github.com/liquidz/misaki/issues/33)"
     "Update atom.xml to use base directory in _config.clj. [#36](https://github.com/liquidz/misaki/issues/36)"
     "Update to use [cuma](https://github.com/liquidz/cuma) as a template engine instead of [clostache](https://github.com/fhd/clostache)."])
