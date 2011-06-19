(ns ops-game.data.unit-dsl
  (:require [clojure.string :as str])
  (:use ops-game.data.types)
  (:import [ops-game.data.types Unit]))

(def ^{:private true} unit-templates
  {:us
   {:foot-hq {:type [:us :hq] :movement 8 :strength 1}
    :foot-engineer {:type [:us :engineer] :movement 8 :strength 3}
    :foot-support {:movement 8 :strength 2}
    :foot-infantry {:movement 8 :strength 4 :type [:us :infantry]}}
   :germany
   {:motorised-hq {:type [:germany :hq] :movement 15 :strength 1}
    :foot-hq {:type [:germany :hq] :movement 8 :strength 1}
    :foot-support {:movement 8 :strength 2}
    :foot-infantry {:movement 8 :strength 4 :type [:germany :infantry]}
    :motorised-support {:movement 15 :strength 2}}})

(def ^{:private true} shortenings
  [[#"[Ii]nfantry|[Ii]nfanterie" "Inf"]
   [#"[Bb]attalion" "Btn"]
   [#"[Cc]ompany" "Coy"]
   [#"[K]ompanie" "Komp"]
   [#"[Pp]latoon" "Pl"]
   [#"[Mm]achine[\- ]?[Gg]un" "MG"]
   [#"[Mm]ortar" "Mtr"]
   [#"[Ee]ngineer" "Eng"]
   [#"[Hh]eavy\s+" "Hvy"]
   [#"[Mm]edium\s+" "Med"]
   [#"[Ll]ight\s+" "Lt"]
   ])

(defn- shorten-name [name]
  (let [name (reduce #(apply str/replace %1 %2) name shortenings)]
    (str/join " " (take 3 (str/split name #"[\s/]+")))))

(defn unit
  ([name template-country template-type overrides* subs]
     (let [template-type [template-country template-type]
           template-type (get-in unit-templates template-type)
           template-type (if overrides* (merge template-type overrides*) template-type)
           {:keys [type movement strength]} template-type
           movement (if (sequential? movement) movement [movement movement])
           strength (if (sequential? strength) strength [strength strength])]
       (cons (Unit. type name (shorten-name name) movement strength nil nil) (flatten subs))))
  ([name template-country template-type]
     (unit name template-country template-type nil []))
  ([name template-country template-type overrides-or-subs]
     (if (map? overrides-or-subs)
       (unit name template-country template-type overrides-or-subs [])
       (unit name template-country template-type nil overrides-or-subs))))

(defn side [side-name units]
  (map #(merge % {:side side-name}) units))

(def ^{:private true} units
  (concat (side :allies
                (unit "2/505 Battalion HQ" :us :foot-hq
                      [(unit "2/505 Engineer Platoon" :us :foot-engineer {:strength [3 2]})
                       (unit "2/505 Machine Gun Platoon" :us :foot-support {:type :machine-gun})
                       (unit "2/505 medium Mortar Platoon (81mm)" :us :foot-support {:type :machine-gun})
                       (map #(unit (str % " Infantry Company") :us :foot-hq
                                   (map (fn [num] (unit (str num "/" % " Infantry Platoon") :us :foot-infantry))
                                        [1 2 3])) "DEF")]))
          (side :axis
                (unit "II/631 Battalion HQ" :germany :motorised-hq
                      (concat (map #(unit (str % " Infanterie Kompanie") :germany :foot-hq
                                          (map (fn [num] (unit (str %"." num " Infanterie Zug") :germany :foot-infantry)) [1 2 3])) [5 6 7])
                              (unit "8 Heavy Kompanie" :germany :foot-hq
                                    [(unit "8/Machine Gun Zug" :germany :foot-support)
                                     (unit "8/Medium Mortar Zug (8cm)" :germany :foot-support)
                                     (unit "8/Heavy Mortar Zug (12cm)" :germany :foot-support)]))))))
