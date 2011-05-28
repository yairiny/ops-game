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
  hex-under-cursor (atom nil))

(def ^{:private true
       :doc "the hex that was last clicked"}
  hex-clicked (atom [3 2]))

(defn update-hex-under-cursor
  "updates the hex under the cursor"
  [loc]
  (reset! hex-under-cursor loc))

(defn update-hex-clicked
  "updates the hex last clicked"
  [loc]
  (reset! hex-clicked loc))

(defn get-drawing-data
  "returns the data that is needed for drawing"
  [] {:map game-map :highlight @hex-under-cursor :clicked @hex-clicked})
