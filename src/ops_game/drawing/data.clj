(ns ops-game.drawing.data)

(def terrain-info
  {:urban {:colour [139 116 22 128]}
   :village {:colour [170 136 0 128]}
   :plain {:colour [234 204 92 128]}
   :woods {:colour [92 219 91 128]}
   :forest {:colour [5 95 4 128]}})

(def unit-info
  {:hq {:colour [185 198 0]}
   :infantry {:colour [185 198 0]}
   :machine-gun {:colour [185 198 0]}
   :mortar {:colour [185 198 0]}})
