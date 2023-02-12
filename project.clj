(defproject aws-cost-notifier "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/tools.namespace "1.4.0"]
                 [amazonica "0.3.163"]
                 [defun "0.3.1"]]
  :repl-options {:init-ns aws-cost-notifier.core}
  :plugins [[lein-cljfmt "0.9.2"]])
