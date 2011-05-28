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

(def ^{:private true
       :doc "holds the location of the hex under the cursor"}
  hex-under-cursor (atom [2 3]))

(defn update-hex-under-cursor
  "updates the hex under the cursor"
  [loc]
  (reset! hex-under-cursor loc))

(defn get-drawing-data
  "returns the data that is needed for drawing"
  [] {:map game-map :highlight @hex-under-cursor})
