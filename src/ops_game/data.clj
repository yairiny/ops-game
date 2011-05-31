(ns ops-game.data)

(def ^{:private true
       :doc "the terrain game info"}
  terrain-info
  {:urban {}
   :village {}
   :plain {}
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
       :doc "the units list"}
  units
  (let [unit-seq
        (concat [(make-unit :hq "2/505 HQ" "2/505 HQ" 15 1)
                  (make-unit :machine-gun "2/505 MG Platoon" "2/505 MG" 8 2)
                  (make-unit :mortar "2/505 Mortar Platoon" "2/505 Mtr" 8 2)]
                (mapcat make-rifle-company "DEF"))
        units-by-loc (partition-by :location (sort-by :location unit-seq))]
    (atom (reduce #(assoc %1 (:location (first %2)) %2) {} units-by-loc))))

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
  (let [loc-units (@units loc)
        unit-shown (last loc-units)
        old-selected @selected-unit]
    (if (and old-selected (= loc (:location old-selected)))
      (let [loc-units (cons old-selected (butlast loc-units)) ;old in front
            unit-shown (last loc-units)]
        (reset! units (assoc @units loc loc-units))
        (reset! selected-unit unit-shown))
      (reset! selected-unit unit-shown))))

(defn get-drawing-data
  "returns the data that is needed for drawing"
  [] {:map game-map
      :highlight @hex-under-cursor :clicked @hex-clicked
      :units @units :selected-unit @selected-unit})

(defn move-selected-unit
  "moves the selected unit (if any) to the specified location"
  [loc]
  (if @selected-unit
    (let [old-loc (:location @selected-unit)
          updated-unit (assoc @selected-unit :location loc)
          old-loc-units (vec (butlast (@units old-loc)))
          new-loc-units (conj (vec (@units loc)) updated-unit)
          new-units (assoc @units loc new-loc-units)]
      (if (empty? old-loc-units)
        (reset! units (dissoc new-units old-loc))
        (reset! units (assoc new-units old-loc old-loc-units)))
      (reset! selected-unit nil))))
