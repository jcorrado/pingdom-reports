(ns pingdom-reports.api
  (:gen-class)
  (:require [clj-http.client :as client]))

(def pingdom-checks-url "https://api.pingdom.com/api/2.1/checks")
(def pingdom-results-url "https://api.pingdom.com/api/2.1/results")
(def pingdom-probes-url "https://api.pingdom.com/api/2.1/probes")

(defn- api-req
  [url user pass api-key]
  (:body (client/get url
                     {:basic-auth [user pass]
                      :headers {"App-Key" api-key}
                      :as :json})))

(defn- get-check
  [user pass api-key name]
  "Return Pingdom defined check map, by name"
  (first (filter #(= name (:name %))
                 (:checks (api-req pingdom-checks-url user pass api-key)))))

(defn get-results
  [user pass api-key name from to]
  "Return a list of check results maps for check name, in requested
  range.  Note we get epoch ms, not s"
  (let [[from to] (map #(quot % 1000) [from to])
        id (:id (get-check user pass api-key name))
        url (format "%s/%d?from=%d&to=%d" pingdom-results-url id from to)]
    (:results (api-req url user pass api-key))))

(defn get-probes
  [user pass api-key]
  "Return a list of Pingdom probe server maps"
  (:probes (api-req pingdom-probes-url user pass api-key)))

(defn get-probe-to-city-map
  [user pass api-key]
  "Return map of Pingdom probe-ids to City names"
  (apply hash-map
         (mapcat #(list (:id %) (:city %)) (get-probes user pass api-key))))
