(ns ops-game.rendering
  (:require [ops-game.opengl :as g])
  (:use [ops-game.rendering theming constants]))


(defn- draw-hex []
  "draws a hex AROUND 0,0"
  (g/draw-shape
   :triangle-fan
   [0 0]
   [0 (- s*)]
   [r* (- h*)]
   [r* h*]
   [0 s*]
   [(- r*) h*]
   [(- r*) (- h*)]
   [0 (- s*)]))

(defn- draw-unit-counter []
  "draws a unit counter around 0,0"
  (let [l (+ h 2)]
    (g/draw-shape
     :quads
     [(- l) (- l)]
     [l (- l)]
     [l l]
     [(- l) l])))

(defn setup-rendering
  "setup for the rendering routines"
  []
  (def hex-list (g/display-list-generate draw-hex))
  (def unit-counter-list (g/display-list-generate draw-unit-counter))
  (def font (g/create-font "Arial" true 8)))

(defn- draw-hexes [map hovered]
  "draws the hexes"
  (doseq [[cells row] (clojure.core/map #(vector %1 %2) map (range))]
    (g/with-pushed-matrix
      (doseq [[cell col] (clojure.core/map #(vector %1 %2) cells (range))]
        (let [hex-colour (terrain-colour cell)]
          (apply g/colour (conj hex-colour (if (= [row col] hovered) 1.0 0.75))))
        (g/display-list-call hex-list)
        (g/draw-text font 0 0 (str row "," col))
        (g/translate a 0)))
    (g/translate (if (even? row) r (- r)) b-h)))

(defn draw-unit 
  "draws a unit at it's location"
  [{:keys [name location type]} multiple? selected?] 
  (let [[row col] location]
    (g/translate (+ (* a col) (if (odd? row) r 0)) (* row b-h))
    (when multiple?
      (g/with-pushed-matrix
        (g/translate -2 -2)
        (g/colour 0.5 0.5 0.5 1)
        (g/display-list-call unit-counter-list))
      (g/translate 1 1))
    (let [unit-colour (get-in unit-info (conj type :colour))]
      (apply g/colour (conj unit-colour (if selected? 0.5 1))))
    (g/display-list-call unit-counter-list)
    (g/draw-text font -14 0 name)))

(defn- draw-units [locs units selected-unit]
  "draws the units on the map"
  (doseq [[loc {top-id :top ids :units}] locs]
    (g/with-pushed-matrix
      (draw-unit (units top-id) (next ids) (= selected-unit top-id)))))

(defn draw-game-map
  "this is the main function for drawing the game map"
  [{:keys [map hovered units locs selected-unit] :as game-data}
   {:keys [left top width height] :as dims}]
  {:pre [(map? game-data) (map? dims) (every? #(>= % 0) [left top width height])]}

  (g/clear-colour :black)
  (g/load-identity)
  
  (g/with-scissor-test left top width height
    (g/clear-colour-buffer)
    (g/translate r s) ;;basic offset of the mid point of the 0,0 hex
    (g/with-pushed-matrix
      (draw-hexes map hovered))
    (g/with-pushed-matrix
      (draw-units locs units selected-unit))))

(defn screen-to-hex 
  "gets the hex coordinates from the screen coordinates"
  [[x y]] 
  {:pre [(>= x 0) (>= y 0)]}
  (let [x (+ x) y (+ y)
        x-sect (quot x (* 2 r))
        y-sect (quot y (+ h s))
        x-sect-pix (rem x (* 2 r))
        y-sect-pix (rem y (+ h s))
        sect-type (even? y-sect)
        m (/ (float h) r)]
    (if sect-type
      (cond (< y-sect-pix (- h (* x-sect-pix m))) [(dec y-sect) (dec x-sect)]
            (< y-sect-pix (+ (- h) (* x-sect-pix m))) [(dec y-sect) x-sect]
            :else [y-sect x-sect])
      
      (if (>= x-sect-pix r)
        (if (< y-sect-pix (- (* 2 h) (* x-sect-pix m)))
          [(dec y-sect) x-sect]
          [y-sect x-sect])
        (if (< y-sect-pix (* x-sect-pix m))
          [(dec y-sect) x-sect]
          [y-sect (dec x-sect)])))))
