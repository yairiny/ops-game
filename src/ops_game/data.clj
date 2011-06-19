(ns ops-game.data)

(def ^{:private true :doc "the turns left sequence"}
  turns (atom (for [t (range) s [:allies :axis]] [t s])))

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
  [[:plain :plain :plain :plain :plain :plain :plain :plain]
   [:plain :plain :woods :woods :plain :plain :plain :plain]
   [:woods :woods :forest :woods :plain :plain :plain :plain]
   [:woods :forest :forest :woods :village :village :plain :plain]
   [:woods :forest :forest :woods :village :village :plain :plain]
   [:woods :woods :forest :woods :plain :plain :plain :plain]
   [:woods :woods :woods :woods :plain :plain :urban :urban]
   [:plain :plain :plain :plain :plain :urban :urban :urban]])

(defn- make-unit [type full-name name movement strength side]
  {:type type :full-name full-name :name name :movement [movement movement] :strength [strength strength] :location [(rand-int 5) (rand-int 5)] :side side})

(defn- make-inf-platoon [coy num nation side]
  (make-unit [nation :infantry] (format "%d/%s Platoon" num coy) (format "%d/%s Pl" num coy) 8 4 side))

(defn- make-pir-rifle-company [coy]
  (cons
   (make-unit [:us :hq] (format "%s Company HQ" coy) (format "%s Coy" coy) 8 1 :allies)
   (map #(make-inf-platoon coy % :us :allies) (range 1 4))))

(defn- make-pir-battalion []
  (concat [(make-unit [:us :hq] "2/505 HQ" "2/505 HQ" 8 1 :allies)
           (make-unit [:us :machine-gun] "2/505 MG Platoon" "2/505 MG" 8 2 :allies)
           (make-unit [:us :engineer] "2/505 Eng Platoon" "2/505 Eng" 8 3 :allies)
           (make-unit [:us :mortar] "2/505 Mortar Platoon" "2/505 Mtr" 6 2 :allies)]
          (mapcat make-pir-rifle-company "DEF")))

(defn- make-ger-grenadier-battalion []
  (concat [(make-unit [:germany :hq] "II/916 HQ" "II/916 HQ" 15 1 :axis)]))

(def ^{:private true
       :doc "the units, mapped from their ID"}
  units
  (atom (into {} (map vector
                      (iterate inc 100)
                      (concat (make-pir-battalion) (make-ger-grenadier-battalion))))))

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
  [] {:map game-map
      :highlight @hex-under-cursor :clicked @hex-clicked
      :units @units :locs @units-by-loc :selected-unit @selected-unit})

(defn get-status-data
  "returns the information about the hex under the cursor and the selected unit"
  [] (let [hex-type (when @hex-under-cursor (get-in game-map @hex-under-cursor))]
       {:highlighted-hex-type hex-type
        :highlighted-hex-loc @hex-under-cursor
        :highlighted-hex-cost (-> hex-type terrain-info :cost)
        :selected-unit (@units @selected-unit)
        :turn (first @turns)}))

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

(defn- calc-move-cost
  [from to]
  (-> (get-in game-map to) terrain-info :cost))

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
