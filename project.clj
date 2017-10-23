(defproject pingdom_reports "0.2.0"
  :description "Pingom Report Generator"
  :license {:name "GNU General Public License, VERSION 3"
            :url "https://www.gnu.org/licenses/gpl.txt"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-http "3.7.0"]
                 [cheshire "5.8.0"]
                 [org.clojure/tools.cli "0.3.5"]
                 [selmer "1.11.1"]]
  :main ^:skip-aot pingdom-reports.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
