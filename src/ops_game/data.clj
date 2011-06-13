(ns ops-game.data)

(def ^{:private true
       :doc "the terrain game info"}
  terrain-info
  {:urban {}
   :village {}
   :plain {:cost 1}
   :woods {}
   :forest {}})

(def ^{:private true
       :doc "the game map"}
  game-map
  [[:plain :plain :plain :plain :plain :plain :plain :plain]
   [:plain :plain :woods :woods :plain :plain :plain :plain]
   [:woods :woods :forest :woods :plain :plain :plain :plain]
   [:woods :forest :forest :woods :village :village :plain :plain]
   [:woods :forest :forest :woods :village :village :plain :plain]
   [:woods :woods :forest :woods :plain :plain :plain :plain]
   [:woods :woods :woods :woods :plain :plain :urban :urban]
   [:plain :plain :plain :plain :plain :urban :urban :urban]])

(defn- make-unit [type full-name name movement strength]
  {:type type :full-name full-name :name name :movement [movement movement] :strength [strength (rand-int strength)] :location [(rand-int 5) (rand-int 5)]})

(defn- make-inf-platoon [coy num]
  (make-unit :infantry (format "%d/%s Platoon" num coy) (format "%d/%s Pl" num coy) 10 4))

(defn- make-rifle-company [coy]
  (cons
   (make-unit :hq (format "%s Company HQ" coy) (format "%s Coy" coy) 15 2)
   (map #(make-inf-platoon coy %) (range 1 4))))

(def ^{:private true
       :doc "the units, mapped from their ID"}
  units
  (atom (into {} (map vector
                      (iterate inc 100)
                      (concat [(make-unit :hq "2/505 HQ" "2/505 HQ" 15 1)
                               (make-unit :machine-gun "2/505 MG Platoon" "2/505 MG" 8 2)
                               (make-unit :mortar "2/505 Mortar Platoon" "2/505 Mtr" 8 2)]
                              (mapcat make-rifle-company "DEF"))))))

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

(defn update-hex-under-cursor
  "updates the hex under the cursor"
  [loc]
  (reset! hex-under-cursor loc))

(defn update-hex-clicked
  "updates the hex last clicked"
  [loc]
  (reset! hex-clicked loc))

(defn update-unit-selected
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
  [] {:map game-map
      :highlight @hex-under-cursor :clicked @hex-clicked
      :units @units :locs @units-by-loc :selected-unit @selected-unit})

(defn get-status-data
  "returns the information about the hex under the cursor and the selected unit"
  [] {:highlighted-hex-type (when @hex-under-cursor (get-in game-map @hex-under-cursor))
      :highlighted-hex-loc @hex-under-cursor
      :selected-unit (@units @selected-unit)})

(defn- get-adjacent
  "gets the vector of adjacent hexes"
  [loc]
  (let [[row col] loc
        num-rows (count game-map)
        num-cols (count (first game-map))
        adj [[(dec row) col] [row (dec col)] [row (inc col)] [(inc row) col]]
        all-adj
        (vec
         (concat adj (if (even? row)
                       [[(dec row) (dec col)] [(inc row) (dec col)]]
                       [[(dec row) (inc col)] [(inc row) (inc col)]])))]
    (filter (fn [[row col]] (and (>= row 0) (>= col 0) (<= row num-rows) (<= col num-cols))) all-adj)))

(defn move-selected-unit
  "moves the selected unit (if any) to the specified location"
  [loc]
  (if @selected-unit
    (let [old-loc (:location (@units @selected-unit))]
      (reset! units (assoc-in @units [@selected-unit :location] loc))
      (let [locs-map (-> (if (@units-by-loc loc)
                           @units-by-loc
                           (assoc @units-by-loc loc {:top nil :units (sorted-set)}))
                         (update-in [old-loc :units] disj @selected-unit)
                         (update-in [loc :units] conj @selected-unit))]
        (reset! units-by-loc (-> locs-map
                                  (assoc-in [old-loc :top]
                                            (first (get-in locs-map [old-loc :units])))
                                  (assoc-in [loc :top] @selected-unit)))))))
