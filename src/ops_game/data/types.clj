(ns ops-game.data.types)

(defrecord Unit [type full-name name movement strength side location])

(defn unit "construct a unit object from a map of values"
  ([{:keys [type full-name name movement strength side location]}]
     (Unit. type full-name name movement strength side location))
  ([k v & opts]
     {:pre (even? (count opts))}
     (unit (apply hash-map k v opts))))
