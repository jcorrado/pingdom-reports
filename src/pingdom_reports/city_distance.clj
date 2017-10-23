(ns pingdom-reports.city-distance
  (:gen-class))

;; Miles, as the crow flies...
;; https://www.distancecalculator.net/
(def distance-from-nyc
  {"New York" 0
   "Matawan" 24
   "Philadelphia" 81
   "Washington" 204
   "Toronto" 491
   "Charlotte" 532
   "Chicago" 711
   "Atlanta" 746
   "St. Louis" 872
   "Tampa" 987
   "Denver" 1627
   "Phoenix" 2140
   "Las Vegas" 2227
   "Seattle" 2402
   "Portland" 2439
   "Los Angeles" 2445
   "San Jose" 2549})
