(ns ops-game.rendering.theming)

(defn int-colour-to-floats 
  "converts an integer colour sequence to floats"
  [& ints] 
  {:pre (every? #(>= 255 % 0) ints)}
  (vec (map #(float (/ % 255)) ints)))

(def ^{:private true :doc "base colours for terrain"} terrain-base-colours
  {:plain (int-colour-to-floats 234 204 92)
   :woods (int-colour-to-floats 92 219 91)
   :forest (int-colour-to-floats 5 95 4)
   :village (int-colour-to-floats 170 136 0)
   :urban (int-colour-to-floats 139 116 22)})

(defn terrain-colour 
  "gets the colour for the terrain"
  [terrain-type] 
  {:pre [(terrain-type terrain-base-colours)]}
  (terrain-type terrain-base-colours))

