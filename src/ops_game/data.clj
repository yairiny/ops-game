(ns ops-game.data
  (:use ops-game.data.unit-dsl)
  (:require [ops-game.data.persistence :as pst]))

(def ^{:private true :doc "basic seq of turns"} turns-seq
  (for [t (range) s [:allies :axis]] [t s]))

(def ^{:private true :doc "the turns left sequence"}
  turns (atom turns-seq))

(def ^{:private true
       :doc "the terrain game info"}
  terrain-info
  {:urban {:cost 3}
   :village {:cost 2}
   :plain {:cost 1}
   :woods {:cost 3}
   :forest {:cost 4}})

(def ^{:private true
       :doc "the game map"}
  game-map
  (atom [[:plain :plain :plain :plain :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :woods :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:woods :woods :forest :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:woods :forest :forest :woods :village :village :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:woods :forest :forest :woods :village :village :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:woods :woods :forest :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:woods :woods :woods :woods :plain :plain :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         [:plain :plain :plain :plain :plain :urban :urban :urban :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain :woods :plain :plain :plain :plain]
         ]))

(defn change-map-size! 
  "changes the size of the game map"
  [width height]
  (let [themap @game-map
        curr-width (count (first themap))
        curr-height (count themap)
        themap
        (if (< height curr-height)
          (take height themap)
          (apply conj themap (repeat (- height curr-height) [])))]
    (reset! game-map (if (< width curr-width)
       (vec (map #(vec (take width %)) themap))
       (vec (map #(apply conj % (repeat (- width (count %)) :plain)) themap))))))


(defn- make-pir-rifle-company [letter]
  (unit (str letter " Infantry Company") :us :foot-hq
        (map #(unit (format "%d/%s Infantry Platoon" % letter) :us :foot-infantry) [1 2 3])))

(defn- make-pir-battalion []
  (unit "2/505 Battalion HQ" :us :foot-hq
        [(unit "2/505 Machine Gun Platoon" :us :foot-support {:type [:us :machine-gun]})
         (unit "2/505 Eng Platoon" :us :foot-engineer)
         (unit "2/505 Medium Mortar Platoon (81mm)" :us :foot-support {:type [:us :mortar]})
         (map make-pir-rifle-company "DEF")]))

(defn- make-german-infantry-company [number]
  (unit (str number " Infanterie Kompanie") :germany :foot-hq
        (map #(unit (format "%d.%d Infanterie Zug"number %) :germany :foot-infantry) [1 2 3])))

(defn- make-german-grenadier-battalion []
  (unit "II/916 Infanterie Battalion HQ" :germany :motorised-hq
        [(unit "4 Heavy Kompanie" :germany :foot-hq
               [(unit "4/Machine Gun Zug" :germany :foot-support {:type [:germany :machine-gun]})
                (unit "4/Medium Mortar Zug (8cm)" :germany :foot-support {:type [:germany :mortar]})
                (unit "4/Heavy Mortar Zug (12cm)" :germany :motorised-support {:type [:germany :mortar]})])
         (map make-german-infantry-company [5 6 7])]))

(defn- make-random-units []
  (map #(merge % {:location [(rand-int 10) (rand-int 10)]})
       (concat (side :allies (make-pir-battalion))
               (side :axis (make-german-grenadier-battalion)))))

(def ^{:private true
       :doc "the units, mapped from their ID"}
  units
  (atom (into {} (map vector
                      (iterate inc 100)
                      (make-random-units)))))

(defn- make-units-by-loc
  "turns a unit map into a map from location to units"
  [units-map]
  (let [ids-and-locs (map vector (map :location (vals units-map)) (keys units-map))
        loc-groups (partition-by first (sort-by first ids-and-locs))
        mapped-by-loc (map #(vector (-> % first first)
                                    {:top (-> % first second)
                                     :units (apply sorted-set (map second %))}) loc-groups)]
    (into {} mapped-by-loc)))

(def ^{:private true
       :doc "the location to unit ids map"}
  units-by-loc
  (atom (make-units-by-loc @units)))

(def ^{:private true
       :doc "the selected unit"}
  selected-unit (atom nil))

(def ^{:private true
       :doc "holds the location of the hex under the cursor"}
  hex-under-cursor (atom nil))

(def ^{:private true
       :doc "the hex that was last clicked"}
  hex-clicked (atom nil))

(defn update-hex-under-cursor!
  "updates the hex under the cursor"
  [loc]
  (reset! hex-under-cursor loc))

(defn update-hex-clicked!
  "updates the hex last clicked"
  [loc]
  (reset! hex-clicked loc))

(defn update-unit-selected!
  "updates the selected unit"
  [loc]
  (let [loc-units (@units-by-loc loc)
        top (:top loc-units)
        old-selected (@units @selected-unit)]
    (if (and old-selected (= loc (:location old-selected)))
      (let [next-top (second (drop-while #(not= top %) (cycle (:units loc-units))))]
        (reset! units-by-loc (assoc-in @units-by-loc [loc :top] next-top))
        (reset! selected-unit next-top))
      (reset! selected-unit top))))

(defn get-drawing-data
  "returns the data that is needed for drawing"
  [] {:map @game-map
      :hovered @hex-under-cursor :clicked @hex-clicked
      :units @units :locs @units-by-loc :selected-unit @selected-unit})

(defn get-status-data
  "returns the information about the hex under the cursor and the selected unit"
  [] (let [hex-type (when @hex-under-cursor (get-in @game-map @hex-under-cursor))]
       {:highlighted-hex-type hex-type
        :highlighted-hex-loc @hex-under-cursor
        :highlighted-hex-cost (-> hex-type terrain-info :cost)
        :selected-unit (@units @selected-unit)
        :turn (first @turns)}))

(defn- get-adjacent
  "gets the vector of adjacent hexes"
  [loc]
  (let [[row col] loc
        num-rows (count @game-map)
        num-cols (count (first @game-map))
        adj [[(dec row) col] [row (dec col)] [row (inc col)] [(inc row) col]]
        all-adj
        (vec
         (concat adj (if (even? row)
                       [[(dec row) (dec col)] [(inc row) (dec col)]]
                       [[(dec row) (inc col)] [(inc row) (inc col)]])))]
    (filter (fn [[row col]] (and (>= row 0) (>= col 0) (<= row num-rows) (<= col num-cols))) all-adj)))

(defn- move-unit! "helper function to move a unit from one location to another"
  [unit from to]
  (reset! units (assoc-in @units [unit :location] to))
  (let [locs-map (-> (if (@units-by-loc to)
                       @units-by-loc
                       (assoc @units-by-loc to {:top nil :units (sorted-set)}))
                     (update-in [from :units] disj unit)
                     (update-in [to :units] conj unit))]
    (reset! units-by-loc (-> locs-map
                             (assoc-in [from :top]
                                       (first (get-in locs-map [from :units])))
                             (assoc-in [to :top] unit)))))

(defn get-move-cost
  "gets the movement cost of a single hex"
  [loc]
  (let [loc (get-in @game-map loc)]
    (if loc (-> loc terrain-info :cost)
        Integer/MAX_VALUE)))

(defn- calc-move-cost
  [from to] (get-move-cost to))

(defn- can-unit-move?
  [unit from to]
  (let [unit (@units unit)]
    (and (some #{to}  (get-adjacent from))
         (>= (second (:movement unit)) (calc-move-cost from to)))))

(defn- update-move-cost!
  "updates the movement cost for this unit"
  [unit from to]
  (reset! units (update-in @units [unit :movement] (fn [[a b]] [a (- b (calc-move-cost from to))]))))

(defn move-selected-unit!
  "moves the selected unit (if any) to the specified location"
  [loc]
  (if @selected-unit
    (let [old-loc (:location (@units @selected-unit))]
      (if (can-unit-move? @selected-unit old-loc loc)
        (do (move-unit! @selected-unit old-loc loc)
            (update-move-cost! @selected-unit old-loc loc))))))

(defn- reset-units-movement! "resets movement values for all units"
  []  (reset! units (into {} (map (fn [[id unit]] [id (update-in unit [:movement] (fn [[a _]] [a a]))]) @units))))

(defn get-current-turn []
  (first @turns))

(defn next-turn! "ends this turn and goes to the next one"
  []
  (swap! turns next)
  (reset-units-movement!))

(defn is-unit-current-side? "checks if the current selected unit is the same side as is playing now"
  []
  (and @selected-unit (= (get-in @units [@selected-unit :side]) ((get-current-turn) 1))))

(defn save-game
  "saves the current data for the game into a save file"
  [filename]
  (pst/save-game filename :map @game-map :units @units :unit-locs @units-by-loc :turn (get-current-turn)))

(defn load-game
  "loads the data from a save file"
  [filename]
  (let [{load-map :map load-units :units  load-unit-locs :unit-locs load-turn :turn} (pst/load-game filename)]
    (reset! game-map load-map)
    (reset! units load-units)
    (reset! turns (drop-while #(not= load-turn %) turns-seq))
    (reset! units-by-loc load-unit-locs)))


