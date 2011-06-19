(ns ops-game.drawing.data)

(def terrain-info
  {:urban {:colour [139 116 22 128]}
   :village {:colour [170 136 0 128]}
   :plain {:colour [234 204 92 128]}
   :woods {:colour [92 219 91 128]}
   :forest {:colour [5 95 4 128]}})

(defn- basic-us-unit [] {:colour [185 198 0]})
(defn- basic-ger-unit [] {:colour [167 167 185]})

(def unit-info
  {:us
   (apply hash-map (interleave [:hq :infantry :machine-gun :mortar :engineer] (repeat (basic-us-unit))))
   :germany
   (apply hash-map (interleave [:hq :infantry :machine-gun :mortar :engineer] (repeat (basic-ger-unit))))})
