(ns ops-game.rendering
  (:require [ops-game.opengl :as g] :reload-all)
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

(defn setup-rendering
  "setup for the rendering routines"
  []
  (def hex-list (g/display-list-generate draw-hex)))

(defn draw-game-map
  "this is the main function for drawing the game map"
  [{:keys [map hovered] :as game-data} {:keys [left top width height] :as dims}]
  {:pre [(map? game-data) (map? dims) (every? #(>= % 0) [left top width height])]}
  
  (g/clear-colour :black)
  (g/load-identity)
  
  (g/with-scissor-test left top width height
    (g/clear-colour-buffer)
    (g/translate r s);;basic offset of the mid point of the 0,0 hex

    (doseq [[cells row] (clojure.core/map #(vector %1 %2) map (range))]
      (g/with-pushed-matrix
        (doseq [[cell col] (clojure.core/map #(vector %1 %2) cells (range))]
          (let [hex-colour (terrain-colour cell)]

            (apply g/colour (conj hex-colour (if (= [row col] hovered) 1.0 0.75))))
          (g/display-list-call hex-list)
          (g/translate a 0)))
      (g/translate (if (even? row) r (- r)) b-h 0))))

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
