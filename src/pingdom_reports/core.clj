(ns pingdom-reports.core
  (:gen-class)
  (:require [pingdom-reports.api :refer [get-probe-to-city-map get-probes get-results]]
            [pingdom-reports.city-distance :refer [distance-from-nyc]]
            [clojure.tools.cli :refer [parse-opts]]
            [selmer.parser :refer [render-file]]))

(def report-title "Pingdom 95% Response Times")
(def gnuplot-template "templates/pingdom_report.gp.in")
(def gnuplot-output-file "pingdom_report.gp")

(defn valid-result?
  [probe-to-city result]
  "Predicate for basic Pingdom result filtering"
  (and (contains? distance-from-nyc (probe-to-city (:probeid result)))
       (> (:responsetime result) 0)
       (= (:statusdesc result) "OK")))

(defn group-by-city
  "Group our results by city.  Results have a probeid attribute that
  we must first convert to a city"
  [probe-to-city coll result]
  (let [city (get probe-to-city (:probeid result))]
    (assoc coll city (conj (get coll city) result))))

;; This is a little dubious.  Looking up by city name is brittle, but
;; so is assuming stable probe-ids.
(defn sort-cities-by-distance
  [coll]
  "Sort a collection of cities by distance from NYC"
  (sort (fn [a b]
          (compare
           (distance-from-nyc a)
           (distance-from-nyc b))) coll))

(defn get-95p
  [key coll]
  "Return 95th (ish) percentile of a collection, sorted by key"
  (let [n (int (* 0.95 (count coll)))]
    (take n (sort-by key coll))))

(defn mk-gnuplot-script
  [template check-name from to color]
  (let [[from to] (map #(java.util.Date. %) [from to])
        title (format "%s - %s\\n%s - %s" report-title check-name from to)]
    (println title)
    (render-file template {:title title :color color})))

(def cli-options
  [["-u" "--pingdom-user USER" "Pingdom user" :id :user]
   ["-p" "--pingdom-password PASSWORD" "Pingdom password" :id :pass]
   ["-a" "--pingdom-api-key API-KEY" "Pingdom API key" :id :api-key]
   ["-n" "--pingdom-check-name CHECK-NAME" "Pingdom Check Name" :id :check-name]
   ["-c" "--color COLOR" "Report Box Color" :id :color]])

(defn -main
  [& args]
  (let [{:keys [user pass api-key check-name color]} (:options (parse-opts args cli-options))
        from (- (System/currentTimeMillis) (* 1000 60 60 24))
        to (System/currentTimeMillis)
        probe-to-city (get-probe-to-city-map user pass api-key)
        results (filter (partial valid-result? probe-to-city)
                        (get-results user pass api-key check-name from to))
        results-by-city (reduce (partial group-by-city probe-to-city) {} results)
        cities (sort-cities-by-distance (keys results-by-city))]

    ;; Output Gnuplot script
    (spit gnuplot-output-file (mk-gnuplot-script gnuplot-template check-name from to color))

    ;; Output datafile
    (loop [[city & tail] cities]
      (if city
        (do
          (doseq [rec (get-95p :responsetime (get results-by-city city))]
            (println (str city "\t" (:responsetime rec))))
          (recur tail))))))
