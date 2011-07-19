(ns ops-game.data.persistence
  (:require [clojure.java.io :as io]
            [ops-game.data.types :as dt]))

(defn save-game 
  "saves the given game data, requires the following data parts:
:map, :units, :unit-locs, :turn"
  [& parts] 
  {:pre [(seq? parts) (even? (count parts))]}
  (let [{:keys [map units unit-locs turn]} (apply hash-map parts)]
    (assert (and map units unit-locs turn))
    (with-open [writer (io/writer "./data/save.dat")]
      (binding [*out* writer]
        (pr map unit-locs turn)
        (pr (clojure.core/map
             (fn [[id rec]] [id (apply hash-map (mapcat identity rec))])
             units))))))

(defn load-game
  "loads the given game save and returns a map with the following parts:
:map, :units, :unit-locs, :turn"
  []
  (with-open [reader (io/reader "./data/save.dat")
              pushback-reader (java.io.PushbackReader. reader)]
    (binding [*in* pushback-reader]
      (let [map (read)
            unit-locs (read)
            turn (read)
            units-as-kvs (read)
            units-map (reduce
                       (fn [m [id unit-as-map]] (assoc m id (dt/unit unit-as-map)))
                       {}  units-as-kvs)]
        {:map map :unit-locs unit-locs :turn turn :units units-map}))))
